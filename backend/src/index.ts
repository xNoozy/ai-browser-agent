import express from 'express';
import cors from 'cors';
import { createServer } from 'http';
import dotenv from 'dotenv';
import { createApiRouter } from './routes/api';
import { setupWebSocket } from './websocket';
import { SessionManager } from './services/session-manager';
import { GeminiService } from './services/gemini';

dotenv.config({ path: '../.env' });
dotenv.config();

const PORT = parseInt(process.env.PORT || '3001', 10);
const GEMINI_API_KEY = process.env.GEMINI_API_KEY;

const app = express();
const server = createServer(app);

app.use(cors({
  origin: process.env.FRONTEND_URL || '*',
  credentials: true,
}));
app.use(express.json({ limit: '10mb' }));

const sessionManager = new SessionManager();

let geminiService: GeminiService | null = null;
if (GEMINI_API_KEY && GEMINI_API_KEY !== 'your_gemini_api_key_here') {
  geminiService = new GeminiService(GEMINI_API_KEY);
  console.log('✓ Gemini AI service initialized');
} else {
  console.warn('⚠ GEMINI_API_KEY not set. AI features disabled. Set it in .env file.');
}

app.use('/api', createApiRouter(sessionManager, geminiService));

app.get('/health', (_req, res) => {
  res.json({ status: 'ok', ai: !!geminiService });
});

setupWebSocket(server, sessionManager);

server.listen(PORT, '0.0.0.0', () => {
  console.log(`\n🚀 AI Browser Agent Backend`);
  console.log(`   HTTP:      http://localhost:${PORT}`);
  console.log(`   WebSocket: ws://localhost:${PORT}/ws`);
  console.log(`   Health:    http://localhost:${PORT}/health\n`);
});
