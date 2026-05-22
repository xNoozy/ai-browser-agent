const API_BASE = '/api';

async function request<T>(url: string, options?: RequestInit): Promise<T> {
  const res = await fetch(`${API_BASE}${url}`, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  });
  if (!res.ok) {
    const data = await res.json().catch(() => ({ error: res.statusText }));
    throw new Error(data.error || `Request failed: ${res.status}`);
  }
  return res.json();
}

export const api = {
  createSession: () =>
    request<{ sessionId: string }>('/session', { method: 'POST' }),

  getSession: (id: string) =>
    request<{ id: string; isActive: boolean; tabs: unknown[]; messageCount: number; logCount: number }>(`/session/${id}`),

  startBrowser: (sessionId: string) =>
    request<{ success: boolean }>('/browser/start', {
      method: 'POST',
      body: JSON.stringify({ sessionId }),
    }),

  stopBrowser: (sessionId: string) =>
    request<{ success: boolean }>('/browser/stop', {
      method: 'POST',
      body: JSON.stringify({ sessionId }),
    }),

  sendMessage: (sessionId: string, message: string) =>
    request<{
      message: {
        id: string;
        role: string;
        content: string;
        timestamp: string;
        actions?: Array<{
          type: string;
          params: Record<string, unknown>;
          status: string;
          result?: string;
          error?: string;
        }>;
      };
      thought: string;
      actionsExecuted: number;
    }>('/chat', {
      method: 'POST',
      body: JSON.stringify({ sessionId, message }),
    }),

  getScreenshot: (sessionId: string) =>
    request<{ screenshot: string }>(`/browser/screenshot/${sessionId}`),
};
