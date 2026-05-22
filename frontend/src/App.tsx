import { useState, useCallback, useEffect } from 'react';
import { Play, Square, Wifi, WifiOff, Cpu } from 'lucide-react';
import ChatPanel from './components/ChatPanel';
import BrowserPreview from './components/BrowserPreview';
import TabBar from './components/TabBar';
import TerminalLogs from './components/TerminalLogs';
import { useWebSocket, LogEntry, BrowserTab } from './hooks/useWebSocket';
import { api } from './services/api';

interface ChatMessage {
  id: string;
  role: 'user' | 'assistant' | 'system';
  content: string;
  timestamp: Date;
  actions?: Array<{
    type: string;
    status: string;
    result?: string;
    error?: string;
  }>;
  thought?: string;
}

export default function App() {
  const [sessionId, setSessionId] = useState<string | null>(null);
  const [browserActive, setBrowserActive] = useState(false);
  const [screenshot, setScreenshot] = useState<string | null>(null);
  const [logs, setLogs] = useState<LogEntry[]>([]);
  const [tabs, setTabs] = useState<BrowserTab[]>([]);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [isBrowserLoading, setIsBrowserLoading] = useState(false);

  const handleScreenshot = useCallback((data: string) => setScreenshot(data), []);
  const handleLog = useCallback((data: LogEntry) => setLogs((prev) => [...prev.slice(-499), data]), []);
  const handleTabs = useCallback((data: BrowserTab[]) => setTabs(data), []);

  const { connected } = useWebSocket({
    sessionId,
    onScreenshot: handleScreenshot,
    onLog: handleLog,
    onTabs: handleTabs,
  });

  useEffect(() => {
    api.createSession().then((res) => {
      setSessionId(res.sessionId);
    }).catch(console.error);
  }, []);

  const handleStartBrowser = async () => {
    if (!sessionId) return;
    setIsBrowserLoading(true);
    try {
      await api.startBrowser(sessionId);
      setBrowserActive(true);
    } catch (err) {
      console.error('Failed to start browser:', err);
    } finally {
      setIsBrowserLoading(false);
    }
  };

  const handleStopBrowser = async () => {
    if (!sessionId) return;
    setIsBrowserLoading(true);
    try {
      await api.stopBrowser(sessionId);
      setBrowserActive(false);
      setScreenshot(null);
      setTabs([]);
    } catch (err) {
      console.error('Failed to stop browser:', err);
    } finally {
      setIsBrowserLoading(false);
    }
  };

  const handleSendMessage = async (content: string) => {
    if (!sessionId) return;

    const userMsg: ChatMessage = {
      id: `user-${Date.now()}`,
      role: 'user',
      content,
      timestamp: new Date(),
    };
    setMessages((prev) => [...prev, userMsg]);
    setIsLoading(true);

    try {
      const res = await api.sendMessage(sessionId, content);
      const assistantMsg: ChatMessage = {
        id: res.message.id,
        role: 'assistant',
        content: res.message.content,
        timestamp: new Date(res.message.timestamp),
        actions: res.message.actions,
        thought: res.thought,
      };
      setMessages((prev) => [...prev, assistantMsg]);
    } catch (err) {
      const errorMsg: ChatMessage = {
        id: `error-${Date.now()}`,
        role: 'assistant',
        content: `Error: ${err instanceof Error ? err.message : 'Something went wrong'}`,
        timestamp: new Date(),
      };
      setMessages((prev) => [...prev, errorMsg]);
    } finally {
      setIsLoading(false);
    }
  };

  const currentUrl = tabs.find((t) => t.isActive)?.url;

  return (
    <div className="h-screen flex flex-col bg-dark-900 overflow-hidden">
      {/* Top Bar */}
      <header className="flex items-center justify-between px-4 py-2.5 border-b border-dark-700 bg-dark-800/80 backdrop-blur-sm">
        <div className="flex items-center gap-3">
          <div className="flex items-center gap-2">
            <Cpu className="w-5 h-5 text-blue-400" />
            <h1 className="font-bold text-base tracking-tight">
              AI Browser <span className="text-blue-400">Agent</span>
            </h1>
          </div>
          <div className="hidden sm:flex items-center gap-1.5 ml-3 px-2 py-0.5 rounded-full bg-dark-700/50 text-xs">
            {connected ? (
              <>
                <Wifi className="w-3 h-3 text-green-400" />
                <span className="text-green-400">Connected</span>
              </>
            ) : (
              <>
                <WifiOff className="w-3 h-3 text-red-400" />
                <span className="text-red-400">Disconnected</span>
              </>
            )}
          </div>
        </div>

        <div className="flex items-center gap-2">
          {!browserActive ? (
            <button
              onClick={handleStartBrowser}
              disabled={!sessionId || isBrowserLoading}
              className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-green-600 hover:bg-green-500 disabled:bg-dark-700 disabled:text-dark-500 text-white text-sm font-medium transition-colors"
            >
              <Play className="w-3.5 h-3.5" />
              {isBrowserLoading ? 'Starting...' : 'Start Browser'}
            </button>
          ) : (
            <button
              onClick={handleStopBrowser}
              disabled={isBrowserLoading}
              className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-red-600 hover:bg-red-500 disabled:bg-dark-700 disabled:text-dark-500 text-white text-sm font-medium transition-colors"
            >
              <Square className="w-3.5 h-3.5" />
              {isBrowserLoading ? 'Stopping...' : 'Stop Browser'}
            </button>
          )}
        </div>
      </header>

      {/* Main Content */}
      <div className="flex-1 flex flex-col lg:flex-row overflow-hidden">
        {/* Left: Chat Panel */}
        <div className="w-full lg:w-[380px] xl:w-[420px] border-b lg:border-b-0 lg:border-r border-dark-700 flex flex-col min-h-0 h-[40vh] lg:h-auto">
          <ChatPanel
            messages={messages}
            onSend={handleSendMessage}
            isLoading={isLoading}
            browserActive={browserActive}
          />
        </div>

        {/* Right: Browser + Terminal */}
        <div className="flex-1 flex flex-col min-h-0 min-w-0">
          {/* Tab Bar */}
          <TabBar tabs={tabs} isActive={browserActive} />

          {/* Browser Preview */}
          <div className="flex-1 min-h-0">
            <BrowserPreview
              screenshot={screenshot}
              isActive={browserActive}
              currentUrl={currentUrl}
            />
          </div>

          {/* Terminal Logs */}
          <div className="h-[200px] lg:h-[220px] border-t border-dark-700 flex-shrink-0">
            <TerminalLogs
              logs={logs}
              onClear={() => setLogs([])}
            />
          </div>
        </div>
      </div>
    </div>
  );
}
