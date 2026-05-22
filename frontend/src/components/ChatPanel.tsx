import React, { useState, useRef, useEffect } from 'react';
import { Send, Bot, User, Loader2, AlertCircle } from 'lucide-react';

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

interface ChatPanelProps {
  messages: ChatMessage[];
  onSend: (message: string) => void;
  isLoading: boolean;
  browserActive: boolean;
}

export default function ChatPanel({ messages, onSend, isLoading, browserActive }: ChatPanelProps) {
  const [input, setInput] = useState('');
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLTextAreaElement>(null);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!input.trim() || isLoading) return;
    onSend(input.trim());
    setInput('');
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSubmit(e);
    }
  };

  return (
    <div className="flex flex-col h-full">
      {/* Header */}
      <div className="flex items-center gap-2 px-4 py-3 border-b border-dark-700 bg-dark-800/50">
        <Bot className="w-5 h-5 text-blue-400" />
        <h2 className="font-semibold text-sm">AI Agent</h2>
        {!browserActive && (
          <span className="ml-auto text-xs text-yellow-400/80 flex items-center gap-1">
            <AlertCircle className="w-3 h-3" />
            Browser offline
          </span>
        )}
      </div>

      {/* Messages */}
      <div className="flex-1 overflow-y-auto p-4 space-y-4 scrollbar-thin">
        {messages.length === 0 && (
          <div className="flex flex-col items-center justify-center h-full text-dark-400 gap-3">
            <Bot className="w-12 h-12 text-dark-600" />
            <p className="text-sm text-center">
              Start a conversation with the AI agent.<br />
              It can browse the web for you.
            </p>
            <div className="text-xs text-dark-500 space-y-1 mt-2">
              <p>Try: "Open google.com"</p>
              <p>Try: "Search for Playwright automation"</p>
              <p>Try: "Take a screenshot"</p>
            </div>
          </div>
        )}

        {messages.map((msg) => (
          <div
            key={msg.id}
            className={`message-enter flex gap-3 ${msg.role === 'user' ? 'justify-end' : 'justify-start'}`}
          >
            {msg.role !== 'user' && (
              <div className="w-7 h-7 rounded-lg bg-blue-500/20 flex items-center justify-center flex-shrink-0 mt-1">
                <Bot className="w-4 h-4 text-blue-400" />
              </div>
            )}
            <div
              className={`max-w-[85%] rounded-xl px-4 py-2.5 text-sm leading-relaxed ${
                msg.role === 'user'
                  ? 'bg-blue-600 text-white'
                  : 'bg-dark-700/80 text-dark-100'
              }`}
            >
              {msg.thought && (
                <div className="text-xs text-dark-400 italic mb-2 pb-2 border-b border-dark-600">
                  💭 {msg.thought}
                </div>
              )}
              <p className="whitespace-pre-wrap">{msg.content}</p>
              {msg.actions && msg.actions.length > 0 && (
                <div className="mt-2 pt-2 border-t border-dark-600/50 space-y-1">
                  {msg.actions.map((action, i) => (
                    <div
                      key={i}
                      className={`text-xs flex items-center gap-1.5 ${
                        action.status === 'completed'
                          ? 'text-green-400'
                          : action.status === 'failed'
                          ? 'text-red-400'
                          : 'text-dark-400'
                      }`}
                    >
                      <span>{action.status === 'completed' ? '✓' : action.status === 'failed' ? '✗' : '○'}</span>
                      <span>{action.type}</span>
                      {action.error && <span className="text-red-400/70">— {action.error}</span>}
                    </div>
                  ))}
                </div>
              )}
            </div>
            {msg.role === 'user' && (
              <div className="w-7 h-7 rounded-lg bg-dark-600 flex items-center justify-center flex-shrink-0 mt-1">
                <User className="w-4 h-4 text-dark-300" />
              </div>
            )}
          </div>
        ))}

        {isLoading && (
          <div className="flex gap-3 message-enter">
            <div className="w-7 h-7 rounded-lg bg-blue-500/20 flex items-center justify-center flex-shrink-0">
              <Bot className="w-4 h-4 text-blue-400" />
            </div>
            <div className="bg-dark-700/80 rounded-xl px-4 py-3 flex items-center gap-2">
              <Loader2 className="w-4 h-4 text-blue-400 animate-spin" />
              <span className="text-sm text-dark-300">Thinking...</span>
            </div>
          </div>
        )}
        <div ref={messagesEndRef} />
      </div>

      {/* Input */}
      <form onSubmit={handleSubmit} className="p-3 border-t border-dark-700 bg-dark-800/50">
        <div className="flex items-end gap-2">
          <textarea
            ref={inputRef}
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder="Type your instruction..."
            rows={1}
            className="flex-1 bg-dark-700 text-dark-100 rounded-xl px-4 py-2.5 text-sm placeholder-dark-500 border border-dark-600 focus:border-blue-500/50 focus:outline-none resize-none max-h-32 scrollbar-thin"
            style={{ minHeight: '40px' }}
          />
          <button
            type="submit"
            disabled={!input.trim() || isLoading}
            className="w-10 h-10 rounded-xl bg-blue-600 hover:bg-blue-500 disabled:bg-dark-700 disabled:text-dark-500 text-white flex items-center justify-center transition-colors flex-shrink-0"
          >
            <Send className="w-4 h-4" />
          </button>
        </div>
      </form>
    </div>
  );
}
