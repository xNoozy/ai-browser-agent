export interface BrowserTab {
  id: string;
  title: string;
  url: string;
  isActive: boolean;
}

export interface Session {
  id: string;
  createdAt: Date;
  isActive: boolean;
  tabs: BrowserTab[];
  messages: ChatMessage[];
  logs: LogEntry[];
}

export interface ChatMessage {
  id: string;
  role: 'user' | 'assistant' | 'system';
  content: string;
  timestamp: Date;
  actions?: BrowserAction[];
}

export interface BrowserAction {
  type: 'navigate' | 'click' | 'type' | 'scroll' | 'screenshot' | 'wait' | 'select' | 'newTab' | 'closeTab' | 'switchTab' | 'goBack' | 'goForward' | 'refresh' | 'evaluate';
  params: Record<string, unknown>;
  status: 'pending' | 'running' | 'completed' | 'failed';
  result?: string;
  error?: string;
}

export interface LogEntry {
  id: string;
  timestamp: Date;
  level: 'info' | 'warn' | 'error' | 'action' | 'ai';
  message: string;
  details?: string;
}

export interface WSMessage {
  type: 'screenshot' | 'log' | 'tabs' | 'action' | 'chat' | 'status' | 'error';
  sessionId: string;
  data: unknown;
}

export interface GeminiActionPlan {
  thought: string;
  actions: Array<{
    type: BrowserAction['type'];
    params: Record<string, unknown>;
    description: string;
  }>;
  response: string;
}
