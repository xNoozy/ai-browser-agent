import { Router, Request, Response } from 'express';
import { SessionManager } from '../services/session-manager';
import { GeminiService } from '../services/gemini';
import { BrowserAction, ChatMessage } from '../types';
import { v4 as uuidv4 } from 'uuid';

export function createApiRouter(sessionManager: SessionManager, geminiService: GeminiService | null) {
  const router = Router();

  router.post('/session', (_req: Request, res: Response) => {
    const session = sessionManager.createSession();
    res.json({ sessionId: session.id });
  });

  router.get('/session/:id', (req: Request, res: Response) => {
    const session = sessionManager.getSession(req.params.id as string);
    if (!session) {
      res.status(404).json({ error: 'Session not found' });
      return;
    }
    res.json({
      id: session.id,
      isActive: session.isActive,
      tabs: session.tabs,
      messageCount: session.messages.length,
      logCount: session.logs.length,
    });
  });

  router.post('/browser/start', async (req: Request, res: Response) => {
    const { sessionId } = req.body;
    if (!sessionId) {
      res.status(400).json({ error: 'sessionId is required' });
      return;
    }
    try {
      await sessionManager.startBrowser(sessionId);
      res.json({ success: true });
    } catch (err) {
      const msg = err instanceof Error ? err.message : 'Unknown error';
      res.status(500).json({ error: msg });
    }
  });

  router.post('/browser/stop', async (req: Request, res: Response) => {
    const { sessionId } = req.body;
    if (!sessionId) {
      res.status(400).json({ error: 'sessionId is required' });
      return;
    }
    try {
      await sessionManager.stopBrowser(sessionId);
      res.json({ success: true });
    } catch (err) {
      const msg = err instanceof Error ? err.message : 'Unknown error';
      res.status(500).json({ error: msg });
    }
  });

  router.get('/browser/screenshot/:sessionId', async (req: Request, res: Response) => {
    const agent = sessionManager.getAgent(req.params.sessionId as string);
    if (!agent) {
      res.status(404).json({ error: 'No active browser' });
      return;
    }
    const screenshot = await agent.getCurrentScreenshot();
    if (!screenshot) {
      res.status(500).json({ error: 'Could not take screenshot' });
      return;
    }
    res.json({ screenshot });
  });

  router.post('/chat', async (req: Request, res: Response) => {
    const { sessionId, message } = req.body;
    if (!sessionId || !message) {
      res.status(400).json({ error: 'sessionId and message are required' });
      return;
    }

    const session = sessionManager.getSession(sessionId);
    if (!session) {
      res.status(404).json({ error: 'Session not found' });
      return;
    }

    if (!geminiService) {
      res.status(503).json({ error: 'AI service not configured. Set GEMINI_API_KEY in .env' });
      return;
    }

    const userMsg: ChatMessage = {
      id: uuidv4(),
      role: 'user',
      content: message,
      timestamp: new Date(),
    };
    session.messages.push(userMsg);
    sessionManager.addLog(sessionId, 'info', `User: ${message}`);

    try {
      const agent = sessionManager.getAgent(sessionId);
      let pageContext = '';
      if (agent) {
        pageContext = await agent.getPageContent();
      }

      const history = session.messages.map((m) => ({
        role: m.role,
        content: m.content,
      }));

      sessionManager.addLog(sessionId, 'ai', 'Thinking...');
      const plan = await geminiService.planActions(message, pageContext, history);
      sessionManager.addLog(sessionId, 'ai', `Thought: ${plan.thought}`);

      const actions: BrowserAction[] = [];
      if (agent && plan.actions.length > 0) {
        for (const actionPlan of plan.actions) {
          const action: BrowserAction = {
            type: actionPlan.type,
            params: actionPlan.params,
            status: 'running',
          };
          actions.push(action);
          sessionManager.addLog(sessionId, 'action', actionPlan.description);

          try {
            const result = await agent.executeAction(action);
            action.status = 'completed';
            action.result = result;
            sessionManager.addLog(sessionId, 'info', `✓ ${result}`);
            await new Promise((resolve) => setTimeout(resolve, 500));
          } catch (err) {
            action.status = 'failed';
            action.error = err instanceof Error ? err.message : String(err);
            sessionManager.addLog(sessionId, 'error', `✗ ${action.error}`);
          }
        }
      } else if (!agent && plan.actions.length > 0) {
        sessionManager.addLog(sessionId, 'warn', 'Browser not started. Start the browser first to execute actions.');
        plan.response += '\n\n⚠️ Please start the browser first before I can execute browser actions.';
      }

      const assistantMsg: ChatMessage = {
        id: uuidv4(),
        role: 'assistant',
        content: plan.response,
        timestamp: new Date(),
        actions,
      };
      session.messages.push(assistantMsg);

      res.json({
        message: assistantMsg,
        thought: plan.thought,
        actionsExecuted: actions.length,
      });
    } catch (err) {
      const msg = err instanceof Error ? err.message : 'AI request failed';
      sessionManager.addLog(sessionId, 'error', `AI Error: ${msg}`);
      res.status(500).json({ error: msg });
    }
  });

  return router;
}
