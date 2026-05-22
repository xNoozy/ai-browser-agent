import { Globe, X, Plus } from 'lucide-react';
import { BrowserTab } from '../hooks/useWebSocket';

interface TabBarProps {
  tabs: BrowserTab[];
  isActive: boolean;
}

export default function TabBar({ tabs, isActive }: TabBarProps) {
  if (!isActive || tabs.length === 0) return null;

  return (
    <div className="flex items-center gap-1 px-2 py-1.5 bg-dark-800 border-b border-dark-700 overflow-x-auto scrollbar-thin">
      {tabs.map((tab) => (
        <div
          key={tab.id}
          className={`flex items-center gap-1.5 px-3 py-1.5 rounded-md text-xs max-w-[180px] cursor-pointer transition-colors ${
            tab.isActive
              ? 'bg-dark-700 text-dark-100'
              : 'bg-dark-800 text-dark-400 hover:bg-dark-700/50 hover:text-dark-300'
          }`}
        >
          <Globe className="w-3 h-3 flex-shrink-0" />
          <span className="truncate">{tab.title || 'New Tab'}</span>
          <button className="ml-1 hover:text-red-400 flex-shrink-0">
            <X className="w-3 h-3" />
          </button>
        </div>
      ))}
      <button className="w-6 h-6 rounded-md flex items-center justify-center text-dark-500 hover:text-dark-300 hover:bg-dark-700/50 transition-colors flex-shrink-0">
        <Plus className="w-3.5 h-3.5" />
      </button>
    </div>
  );
}
