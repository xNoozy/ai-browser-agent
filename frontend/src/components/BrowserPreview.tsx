import { Monitor, RefreshCw, Loader2 } from 'lucide-react';

interface BrowserPreviewProps {
  screenshot: string | null;
  isActive: boolean;
  currentUrl?: string;
}

export default function BrowserPreview({ screenshot, isActive, currentUrl }: BrowserPreviewProps) {
  return (
    <div className="flex flex-col h-full">
      {/* Browser Chrome */}
      <div className="flex items-center gap-2 px-4 py-2 border-b border-dark-700 bg-dark-800/50">
        <Monitor className="w-4 h-4 text-dark-400" />
        <h2 className="font-semibold text-sm text-dark-200">Browser Preview</h2>
        <div className={`ml-2 w-2 h-2 rounded-full ${isActive ? 'bg-green-500 animate-pulse' : 'bg-dark-500'}`} />
        {isActive && (
          <div className="flex-1 ml-2">
            <div className="bg-dark-700 rounded-md px-3 py-1 text-xs text-dark-300 truncate max-w-full">
              {currentUrl || 'about:blank'}
            </div>
          </div>
        )}
      </div>

      {/* Preview Area */}
      <div className="flex-1 relative bg-dark-900 overflow-hidden">
        {!isActive ? (
          <div className="absolute inset-0 flex flex-col items-center justify-center text-dark-500 gap-3">
            <Monitor className="w-16 h-16 text-dark-700" />
            <p className="text-sm">Browser is not running</p>
            <p className="text-xs text-dark-600">Click "Start Browser" to begin</p>
          </div>
        ) : !screenshot ? (
          <div className="absolute inset-0 flex flex-col items-center justify-center text-dark-400 gap-3">
            <Loader2 className="w-8 h-8 animate-spin text-blue-400" />
            <p className="text-sm">Loading browser view...</p>
          </div>
        ) : (
          <div className="absolute inset-0 flex items-center justify-center p-1">
            <img
              src={`data:image/jpeg;base64,${screenshot}`}
              alt="Browser preview"
              className="max-w-full max-h-full object-contain rounded shadow-lg"
              draggable={false}
            />
          </div>
        )}

        {/* Loading bar */}
        {isActive && !screenshot && (
          <div className="absolute top-0 left-0 h-0.5 bg-blue-500 loading-bar" />
        )}

        {/* Refresh overlay */}
        {isActive && (
          <button
            className="absolute top-2 right-2 w-7 h-7 rounded-md bg-dark-800/80 hover:bg-dark-700 flex items-center justify-center text-dark-400 hover:text-dark-200 transition-colors"
            title="Refresh"
          >
            <RefreshCw className="w-3.5 h-3.5" />
          </button>
        )}
      </div>
    </div>
  );
}
