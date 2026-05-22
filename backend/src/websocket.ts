import { WebSocketServer, WebSocket } from 'ws';
import { Server } from 'http';
import { SessionManager } from './services/session-manager';
import { WSMessage } from './types';

interface ClientConnection {
  ws: WebSocket;
  sessionId: string;
}

export function setupWebSocket(server: Server, sessionManager: SessionManager) {
  const wss = new WebSocketServer({ server, path: '/ws' });
  const clients: Map<string, ClientConnection[]> = new Map();

  function broadcastToSession(sessionId: string, message: WSMessage) {
    const sessionClients = clients.get(sessionId);
    if (!sessionClients) return;
    const data = JSON.stringify(message);
    for (const client of sessionClients) {
      if (client.ws.readyState === WebSocket.OPEN) {
        client.ws.send(data);
      }
    }
  }

  sessionManager.setLogHandler((sessionId, log) => {
    broadcastToSession(sessionId, {
      type: 'log',
      sessionId,
      data: log,
    });
  });

  sessionManager.setScreenshotHandler((sessionId, screenshot) => {
    broadcastToSession(sessionId, {
      type: 'screenshot',
      sessionId,
      data: screenshot,
    });
  });

  sessionManager.setTabsHandler((sessionId, tabs) => {
    broadcastToSession(sessionId, {
      type: 'tabs',
      sessionId,
      data: tabs,
    });
  });

  wss.on('connection', (ws: WebSocket) => {
    let currentSessionId = '';

    ws.on('message', (raw: Buffer) => {
      try {
        const msg = JSON.parse(raw.toString());
        if (msg.type === 'subscribe' && msg.sessionId) {
          currentSessionId = msg.sessionId as string;
          if (!clients.has(currentSessionId)) {
            clients.set(currentSessionId, []);
          }
          clients.get(currentSessionId)!.push({ ws, sessionId: currentSessionId });

          ws.send(JSON.stringify({
            type: 'status',
            sessionId: currentSessionId,
            data: { connected: true },
          }));
        }
      } catch {
        // Invalid message
      }
    });

    ws.on('close', () => {
      if (currentSessionId) {
        const sessionClients = clients.get(currentSessionId);
        if (sessionClients) {
          const filtered = sessionClients.filter((c) => c.ws !== ws);
          if (filtered.length > 0) {
            clients.set(currentSessionId, filtered);
          } else {
            clients.delete(currentSessionId);
          }
        }
      }
    });
  });

  return wss;
}
