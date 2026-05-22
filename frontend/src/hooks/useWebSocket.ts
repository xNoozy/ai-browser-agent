import { useEffect, useRef, useCallback, useState } from 'react';

interface WSMessage {
  type: 'screenshot' | 'log' | 'tabs' | 'action' | 'chat' | 'status' | 'error';
  sessionId: string;
  data: unknown;
}

interface UseWebSocketOptions {
  sessionId: string | null;
  onScreenshot?: (data: string) => void;
  onLog?: (data: LogEntry) => void;
  onTabs?: (data: BrowserTab[]) => void;
  onStatus?: (data: { connected: boolean }) => void;
}

export interface LogEntry {
  id: string;
  timestamp: string;
  level: 'info' | 'warn' | 'error' | 'action' | 'ai';
  message: string;
  details?: string;
}

export interface BrowserTab {
  id: string;
  title: string;
  url: string;
  isActive: boolean;
}

export function useWebSocket({ sessionId, onScreenshot, onLog, onTabs, onStatus }: UseWebSocketOptions) {
  const wsRef = useRef<WebSocket | null>(null);
  const [connected, setConnected] = useState(false);
  const reconnectTimerRef = useRef<ReturnType<typeof setTimeout>>();

  const connect = useCallback(() => {
    if (!sessionId) return;

    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const wsUrl = `${protocol}//${window.location.host}/ws`;

    const ws = new WebSocket(wsUrl);
    wsRef.current = ws;

    ws.onopen = () => {
      setConnected(true);
      ws.send(JSON.stringify({ type: 'subscribe', sessionId }));
    };

    ws.onmessage = (event) => {
      try {
        const msg: WSMessage = JSON.parse(event.data);
        switch (msg.type) {
          case 'screenshot':
            onScreenshot?.(msg.data as string);
            break;
          case 'log':
            onLog?.(msg.data as LogEntry);
            break;
          case 'tabs':
            onTabs?.(msg.data as BrowserTab[]);
            break;
          case 'status':
            onStatus?.(msg.data as { connected: boolean });
            break;
        }
      } catch {
        // Invalid message
      }
    };

    ws.onclose = () => {
      setConnected(false);
      reconnectTimerRef.current = setTimeout(connect, 3000);
    };

    ws.onerror = () => {
      ws.close();
    };
  }, [sessionId, onScreenshot, onLog, onTabs, onStatus]);

  useEffect(() => {
    connect();
    return () => {
      clearTimeout(reconnectTimerRef.current);
      wsRef.current?.close();
    };
  }, [connect]);

  return { connected };
}
