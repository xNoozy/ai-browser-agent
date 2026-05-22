import { GoogleGenerativeAI } from '@google/generative-ai';
import { GeminiActionPlan, BrowserAction } from '../types';

const SYSTEM_PROMPT = `You are an AI browser automation agent. You control a headless Chromium browser via Playwright.
You receive the user's instruction and the current state of the browser (URL, page content, interactive elements).

Your job is to:
1. Analyze the user's request
2. Plan a sequence of browser actions to accomplish it
3. Return a structured JSON response

Available actions:
- navigate: { url: string } — Go to a URL
- click: { selector: string } — Click an element (use CSS selectors)
- type: { selector: string, text: string } — Type text into an input/textarea
- scroll: { direction: "up"|"down", amount: number } — Scroll the page
- screenshot: {} — Take a screenshot
- wait: { ms?: number, selector?: string } — Wait for time or element
- select: { selector: string, value: string } — Select dropdown option
- newTab: { url?: string } — Open a new tab
- closeTab: { tabId?: string } — Close a tab
- switchTab: { tabId: string } — Switch to another tab
- goBack: {} — Navigate back
- goForward: {} — Navigate forward
- refresh: {} — Refresh the page
- evaluate: { script: string } — Run JavaScript in the page

IMPORTANT RULES:
- Use specific CSS selectors. Prefer #id, then [name], then [placeholder], then text-based selectors.
- For clicking links, use the exact text: 'a:has-text("Link Text")' or 'button:has-text("Button Text")'
- For input fields, prefer: 'input[name="fieldname"]', 'input[placeholder="..."]', '#inputId'
- Always include a "thought" explaining your reasoning
- Always include a "response" message for the user
- Return ONLY valid JSON, no markdown code blocks

You MUST respond with this exact JSON structure:
{
  "thought": "your reasoning about what to do",
  "actions": [
    {
      "type": "action_type",
      "params": { ... },
      "description": "human readable description"
    }
  ],
  "response": "message to show the user"
}

If the user asks something that doesn't need browser actions (like a question), respond with an empty actions array and answer in the response field.`;

export class GeminiService {
  private ai: GoogleGenerativeAI;
  private modelName: string = 'gemini-2.0-flash';

  constructor(apiKey: string) {
    this.ai = new GoogleGenerativeAI(apiKey);
  }

  async planActions(
    userMessage: string,
    pageContext: string,
    conversationHistory: Array<{ role: string; content: string }>
  ): Promise<GeminiActionPlan> {
    const model = this.ai.getGenerativeModel({
      model: this.modelName,
      generationConfig: {
        temperature: 0.2,
        topP: 0.8,
        maxOutputTokens: 4096,
      },
    });

    const historyMessages = conversationHistory.slice(-10).map((msg) => ({
      role: msg.role === 'assistant' ? 'model' : 'user',
      parts: [{ text: msg.content }],
    }));

    const chat = model.startChat({
      history: [
        { role: 'user', parts: [{ text: SYSTEM_PROMPT }] },
        { role: 'model', parts: [{ text: 'Understood. I am ready to help automate browser actions. Send me instructions and I will respond with structured JSON action plans.' }] },
        ...historyMessages.filter(m => m.role === 'user' || m.role === 'model'),
      ],
    });

    const prompt = pageContext
      ? `Current browser state:\n${pageContext}\n\nUser instruction: ${userMessage}`
      : `User instruction: ${userMessage}\n\n(No browser is currently open or no page is loaded)`;

    const result = await chat.sendMessage(prompt);
    const responseText = result.response.text();

    return this.parseResponse(responseText);
  }

  private parseResponse(text: string): GeminiActionPlan {
    let cleaned = text.trim();

    const jsonBlockMatch = cleaned.match(/```(?:json)?\s*([\s\S]*?)```/);
    if (jsonBlockMatch) {
      cleaned = jsonBlockMatch[1].trim();
    }

    const jsonMatch = cleaned.match(/\{[\s\S]*\}/);
    if (!jsonMatch) {
      return {
        thought: 'Could not parse AI response',
        actions: [],
        response: cleaned || 'I encountered an issue processing the response. Please try again.',
      };
    }

    try {
      const parsed = JSON.parse(jsonMatch[0]);
      return {
        thought: parsed.thought || '',
        actions: (parsed.actions || []).map((a: { type: BrowserAction['type']; params: Record<string, unknown>; description: string }) => ({
          type: a.type,
          params: a.params || {},
          description: a.description || '',
        })),
        response: parsed.response || '',
      };
    } catch {
      return {
        thought: 'Failed to parse JSON response',
        actions: [],
        response: text.substring(0, 500),
      };
    }
  }
}
