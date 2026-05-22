import { v4 as uuidv4 } from 'uuid';
import { Session, LogEntry } from '../types';
import { PlaywrightAgent } from './playwright-agent';

export class SessionManager {
  private sessions: Map<string, Session> = new Map();
  private agents: Map<string, PlaywrightAgent> = new Map();
  private onLog?: (sessionId: string, log: LogEntry) => void;
  private onScreenshot?: (sessionId: string, screenshot: string) => void;
  private onTabs?: (sessionId: string, tabs: Session['tabs']) => void;

  setLogHandler(handler: (sessionId: string, log: LogEntry) => void) {
    this.onLog = handler;
  }

  setScreenshotHandler(handler: (sessionId: string, screenshot: string) => void) {
    this.onScreenshot = handler;
  }

  setTabsHandler(handler: (sessionId: string, tabs: Session['tabs']) => void) {
    this.onTabs = handler;
  }

  createSession(): Session {
    const session: Session = {
      id: uuidv4(),
      createdAt: new Date(),
      isActive: false,
      tabs: [],
      messages: [],
      logs: [],
    };
    this.sessions.set(session.id, session);
    this.addLog(session.id, 'info', 'Session created');
    return session;
  }

  getSession(sessionId: string): Session | undefined {
    return this.sessions.get(sessionId);
  }

  getAgent(sessionId: string): PlaywrightAgent | undefined {
    return this.agents.get(sessionId);
  }

  async startBrowser(sessionId: string): Promise<void> {
    const session = this.sessions.get(sessionId);
    if (!session) throw new Error('Session not found');
    if (this.agents.has(sessionId)) {
      this.addLog(sessionId, 'warn', 'Browser already running');
      return;
    }

    this.addLog(sessionId, 'info', 'Starting browser...');
    const agent = new PlaywrightAgent();

    agent.setLogHandler((log) => {
      this.addLog(sessionId, log.level, log.message, log.details);
    });

    agent.setScreenshotHandler((screenshot) => {
      this.onScreenshot?.(sessionId, screenshot);
    });

    agent.setTabsHandler((tabs) => {
      session.tabs = tabs;
      this.onTabs?.(sessionId, tabs);
    });

    await agent.launch();
    this.agents.set(sessionId, agent);
    session.isActive = true;
    this.addLog(sessionId, 'info', 'Browser started successfully');
  }

  async stopBrowser(sessionId: string): Promise<void> {
    const agent = this.agents.get(sessionId);
    if (agent) {
      this.addLog(sessionId, 'info', 'Stopping browser...');
      await agent.close();
      this.agents.delete(sessionId);
    }
    const session = this.sessions.get(sessionId);
    if (session) {
      session.isActive = false;
      session.tabs = [];
    }
    this.addLog(sessionId, 'info', 'Browser stopped');
  }

  async destroySession(sessionId: string): Promise<void> {
    await this.stopBrowser(sessionId);
    this.sessions.delete(sessionId);
  }

  addLog(sessionId: string, level: LogEntry['level'], message: string, details?: string) {
    const log: LogEntry = {
      id: uuidv4(),
      timestamp: new Date(),
      level,
      message,
      details,
    };
    const session = this.sessions.get(sessionId);
    if (session) {
      session.logs.push(log);
      if (session.logs.length > 500) {
        session.logs = session.logs.slice(-300);
      }
    }
    this.onLog?.(sessionId, log);
  }
}
