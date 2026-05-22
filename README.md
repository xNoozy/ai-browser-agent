# AI Browser Agent

A full-stack web application that combines AI chat (Google Gemini) with automated browser control via Playwright. Similar to Devin AI — users chat with an AI agent that can autonomously browse the web, fill forms, click buttons, take screenshots, and more.

## Features

- **AI Chat Interface** — Chat with Gemini AI like ChatGPT
- **Automated Browser Control** — AI runs Playwright commands based on your instructions
- **Live Browser Preview** — See the headless browser in real-time via screenshots streamed over WebSocket
- **Multi-Tab Support** — AI can manage multiple browser tabs simultaneously
- **Session Sandbox** — Each user gets an isolated browser session
- **Terminal Logs** — Real-time logs of all browser actions and AI reasoning
- **Dark Mode UI** — Modern, responsive dark-themed interface
- **Start/Stop Browser** — Manual control over browser lifecycle
- **Secure API Keys** — Server-side only, never exposed to frontend

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Frontend | React 18 + TypeScript + Vite + TailwindCSS |
| Backend | Node.js + Express + TypeScript |
| AI | Google Gemini API (`@google/generative-ai`) |
| Browser | Playwright (Chromium headless) |
| Realtime | WebSocket (`ws`) |
| Deploy | Docker + Docker Compose |

## Quick Start

### Prerequisites
- Node.js 18+
- A Google Gemini API key ([get one here](https://aistudio.google.com/app/apikey))

### 1. Clone & Setup

```bash
git clone <repo-url>
cd ai-browser-agent

# Copy env and add your Gemini API key
cp .env.example .env
# Edit .env and set GEMINI_API_KEY
```

### 2. Install & Run Backend

```bash
cd backend
npm install
npx playwright install chromium
npm run dev
```

### 3. Install & Run Frontend

```bash
cd frontend
npm install
npm run dev
```

### 4. Open the App

Navigate to `http://localhost:5173` in your browser.

## Docker Deployment

```bash
docker-compose up --build
```

The app will be available at `http://localhost:5173`.

## Project Structure

```
ai-browser-agent/
├── backend/
│   ├── src/
│   │   ├── index.ts              # Express server entry
│   │   ├── websocket.ts          # WebSocket server
│   │   ├── routes/
│   │   │   └── api.ts            # REST API routes
│   │   ├── services/
│   │   │   ├── gemini.ts         # Gemini AI integration
│   │   │   ├── playwright-agent.ts # Browser automation
│   │   │   └── session-manager.ts  # User session management
│   │   └── types/
│   │       └── index.ts          # TypeScript types
│   ├── package.json
│   └── tsconfig.json
├── frontend/
│   ├── src/
│   │   ├── App.tsx               # Main app layout
│   │   ├── main.tsx              # Entry point
│   │   ├── components/
│   │   │   ├── ChatPanel.tsx     # AI chat interface
│   │   │   ├── BrowserPreview.tsx # Live browser view
│   │   │   ├── TerminalLogs.tsx  # Real-time logs
│   │   │   └── TabBar.tsx        # Browser tab management
│   │   ├── hooks/
│   │   │   └── useWebSocket.ts   # WebSocket hook
│   │   └── index.css             # TailwindCSS + dark theme
│   ├── package.json
│   ├── vite.config.ts
│   └── tailwind.config.js
├── docker-compose.yml
├── Dockerfile.backend
├── Dockerfile.frontend
├── .env.example
└── README.md
```

## Usage

1. Click **Start Browser** to launch a headless Chromium instance
2. Type instructions in the chat, e.g.:
   - "Open google.com and search for 'Playwright automation'"
   - "Go to github.com and login with my credentials"
   - "Fill out the contact form on example.com"
   - "Take a screenshot of the current page"
3. Watch the AI execute steps in real-time in the browser preview
4. View detailed logs in the terminal panel
5. Click **Stop Browser** when done

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/chat` | Send message to AI agent |
| POST | `/api/browser/start` | Start browser session |
| POST | `/api/browser/stop` | Stop browser session |
| GET | `/api/browser/screenshot` | Get current screenshot |
| GET | `/api/session/:id` | Get session info |

## License

MIT
