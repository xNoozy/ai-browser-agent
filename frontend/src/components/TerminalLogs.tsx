import { useEffect, useRef } from 'react';
import { Terminal, Trash2 } from 'lucide-react';
import { LogEntry } from '../hooks/useWebSocket';

interface TerminalLogsProps {
  logs: LogEntry[];
  onClear: () => void;
}

const levelColors: Record<string, string> = {
  info: 'text-dark-300',
  warn: 'text-yellow-400',
  error: 'text-red-400',
  action: 'text-blue-400',
  ai: 'text-purple-400',
};

const levelIcons: Record<string, string> = {
  info: 'ℹ',
  warn: '⚠',
  error: '✗',
  action: '▶',
  ai: '🤖',
};

export default function TerminalLogs({ logs, onClear }: TerminalLogsProps) {
  const bottomRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [logs]);

  const formatTime = (ts: string) => {
    const d = new Date(ts);
    return d.toLocaleTimeString('en-US', { hour12: false, hour: '2-digit', minute: '2-digit', second: '2-digit' });
  };

  return (
    <div className="flex flex-col h-full">
      {/* Header */}
      <div className="flex items-center justify-between px-4 py-2 border-b border-dark-700 bg-dark-800/50">
        <div className="flex items-center gap-2">
          <Terminal className="w-4 h-4 text-green-400" />
          <h2 className="font-semibold text-sm text-dark-200">Terminal</h2>
          <span className="text-xs text-dark-500">{logs.length} entries</span>
        </div>
        <button
          onClick={onClear}
          className="p-1 rounded hover:bg-dark-700 text-dark-500 hover:text-dark-300 transition-colors"
          title="Clear logs"
        >
          <Trash2 className="w-3.5 h-3.5" />
        </button>
      </div>

      {/* Log entries */}
      <div className="flex-1 overflow-y-auto p-3 font-mono text-xs space-y-0.5 scrollbar-thin bg-dark-900/50">
        {logs.length === 0 && (
          <div className="text-dark-600 text-center py-8">
            <Terminal className="w-8 h-8 mx-auto mb-2 text-dark-700" />
            No logs yet
          </div>
        )}
        {logs.map((log) => (
          <div key={log.id} className={`flex gap-2 py-0.5 ${levelColors[log.level] || 'text-dark-300'}`}>
            <span className="text-dark-600 flex-shrink-0">{formatTime(log.timestamp)}</span>
            <span className="flex-shrink-0 w-4 text-center">{levelIcons[log.level] || '·'}</span>
            <span className="break-all">
              {log.message}
              {log.details && (
                <span className="text-dark-500 ml-1">({log.details})</span>
              )}
            </span>
          </div>
        ))}
        <div ref={bottomRef} className="cursor-blink" />
      </div>
    </div>
  );
}
