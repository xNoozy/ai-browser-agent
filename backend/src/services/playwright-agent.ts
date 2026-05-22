import { chromium, Browser, BrowserContext, Page } from 'playwright';
import { v4 as uuidv4 } from 'uuid';
import { BrowserAction, BrowserTab, LogEntry } from '../types';

export class PlaywrightAgent {
  private browser: Browser | null = null;
  private context: BrowserContext | null = null;
  private pages: Map<string, Page> = new Map();
  private activePageId: string | null = null;
  private screenshotInterval: ReturnType<typeof setInterval> | null = null;
  private onLog?: (log: Pick<LogEntry, 'level' | 'message' | 'details'>) => void;
  private onScreenshot?: (screenshot: string) => void;
  private onTabs?: (tabs: BrowserTab[]) => void;

  setLogHandler(handler: (log: Pick<LogEntry, 'level' | 'message' | 'details'>) => void) {
    this.onLog = handler;
  }

  setScreenshotHandler(handler: (screenshot: string) => void) {
    this.onScreenshot = handler;
  }

  setTabsHandler(handler: (tabs: BrowserTab[]) => void) {
    this.onTabs = handler;
  }

  private log(level: LogEntry['level'], message: string, details?: string) {
    this.onLog?.({ level, message, details });
  }

  async launch(): Promise<void> {
    this.browser = await chromium.launch({
      headless: true,
      args: [
        '--no-sandbox',
        '--disable-setuid-sandbox',
        '--disable-dev-shm-usage',
        '--disable-gpu',
      ],
    });

    this.context = await this.browser.newContext({
      viewport: { width: 1280, height: 720 },
      userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
    });

    const page = await this.context.newPage();
    const pageId = uuidv4();
    this.pages.set(pageId, page);
    this.activePageId = pageId;

    this.setupPageListeners(pageId, page);
    this.startScreenshotStream();
    this.emitTabs();

    this.log('info', 'Browser launched with initial tab');
  }

  async close(): Promise<void> {
    this.stopScreenshotStream();
    if (this.context) {
      await this.context.close().catch(() => {});
      this.context = null;
    }
    if (this.browser) {
      await this.browser.close().catch(() => {});
      this.browser = null;
    }
    this.pages.clear();
    this.activePageId = null;
    this.log('info', 'Browser closed');
  }

  private setupPageListeners(pageId: string, page: Page) {
    page.on('load', () => {
      this.log('info', `Page loaded: ${page.url()}`);
      this.emitTabs();
    });
    page.on('close', () => {
      this.pages.delete(pageId);
      if (this.activePageId === pageId) {
        const remaining = Array.from(this.pages.keys());
        this.activePageId = remaining.length > 0 ? remaining[remaining.length - 1] : null;
      }
      this.emitTabs();
    });
    page.on('console', (msg) => {
      if (msg.type() === 'error') {
        this.log('warn', `Console error: ${msg.text()}`);
      }
    });
  }

  private startScreenshotStream() {
    this.screenshotInterval = setInterval(async () => {
      try {
        const page = this.getActivePage();
        if (page) {
          const buffer = await page.screenshot({ type: 'jpeg', quality: 60 });
          const base64 = buffer.toString('base64');
          this.onScreenshot?.(base64);
        }
      } catch {
        // Page might be navigating
      }
    }, 1000);
  }

  private stopScreenshotStream() {
    if (this.screenshotInterval) {
      clearInterval(this.screenshotInterval);
      this.screenshotInterval = null;
    }
  }

  private getActivePage(): Page | null {
    if (!this.activePageId) return null;
    return this.pages.get(this.activePageId) || null;
  }

  private emitTabs() {
    const tabs: BrowserTab[] = [];
    for (const [id, page] of this.pages) {
      tabs.push({
        id,
        title: page.url().substring(0, 50),
        url: page.url(),
        isActive: id === this.activePageId,
      });
    }
    this.onTabs?.(tabs);
  }

  async executeAction(action: BrowserAction): Promise<string> {
    const page = this.getActivePage();

    try {
      switch (action.type) {
        case 'navigate': {
          if (!page) throw new Error('No active page');
          const url = action.params.url as string;
          this.log('action', `Navigating to ${url}`);
          await page.goto(url, { waitUntil: 'domcontentloaded', timeout: 30000 });
          return `Navigated to ${url}`;
        }

        case 'click': {
          if (!page) throw new Error('No active page');
          const selector = action.params.selector as string;
          this.log('action', `Clicking: ${selector}`);
          await page.click(selector, { timeout: 10000 });
          return `Clicked: ${selector}`;
        }

        case 'type': {
          if (!page) throw new Error('No active page');
          const typeSelector = action.params.selector as string;
          const text = action.params.text as string;
          this.log('action', `Typing "${text}" into ${typeSelector}`);
          await page.fill(typeSelector, text);
          return `Typed "${text}" into ${typeSelector}`;
        }

        case 'scroll': {
          if (!page) throw new Error('No active page');
          const direction = (action.params.direction as string) || 'down';
          const amount = (action.params.amount as number) || 500;
          const scrollY = direction === 'up' ? -amount : amount;
          this.log('action', `Scrolling ${direction} by ${amount}px`);
          await page.evaluate(`window.scrollBy(0, ${scrollY})`);
          return `Scrolled ${direction} by ${amount}px`;
        }

        case 'screenshot': {
          if (!page) throw new Error('No active page');
          this.log('action', 'Taking screenshot');
          const buffer = await page.screenshot({ type: 'jpeg', quality: 80 });
          const base64 = buffer.toString('base64');
          this.onScreenshot?.(base64);
          return 'Screenshot taken';
        }

        case 'wait': {
          if (!page) throw new Error('No active page');
          const ms = (action.params.ms as number) || 1000;
          const waitSelector = action.params.selector as string | undefined;
          if (waitSelector) {
            this.log('action', `Waiting for element: ${waitSelector}`);
            await page.waitForSelector(waitSelector, { timeout: 15000 });
            return `Element found: ${waitSelector}`;
          }
          this.log('action', `Waiting ${ms}ms`);
          await page.waitForTimeout(ms);
          return `Waited ${ms}ms`;
        }

        case 'select': {
          if (!page) throw new Error('No active page');
          const selSelector = action.params.selector as string;
          const value = action.params.value as string;
          this.log('action', `Selecting "${value}" in ${selSelector}`);
          await page.selectOption(selSelector, value);
          return `Selected "${value}" in ${selSelector}`;
        }

        case 'newTab': {
          if (!this.context) throw new Error('No browser context');
          const newPage = await this.context.newPage();
          const newPageId = uuidv4();
          this.pages.set(newPageId, newPage);
          this.activePageId = newPageId;
          this.setupPageListeners(newPageId, newPage);
          this.emitTabs();
          const tabUrl = action.params.url as string | undefined;
          if (tabUrl) {
            await newPage.goto(tabUrl, { waitUntil: 'domcontentloaded', timeout: 30000 });
          }
          this.log('action', `Opened new tab${tabUrl ? `: ${tabUrl}` : ''}`);
          return `New tab opened${tabUrl ? `: ${tabUrl}` : ''}`;
        }

        case 'closeTab': {
          const tabId = action.params.tabId as string | undefined;
          const targetId = tabId || this.activePageId;
          if (!targetId) throw new Error('No tab to close');
          const targetPage = this.pages.get(targetId);
          if (targetPage) {
            await targetPage.close();
          }
          this.log('action', 'Closed tab');
          return 'Tab closed';
        }

        case 'switchTab': {
          const switchTabId = action.params.tabId as string;
          if (!this.pages.has(switchTabId)) throw new Error('Tab not found');
          this.activePageId = switchTabId;
          this.emitTabs();
          this.log('action', `Switched to tab: ${switchTabId}`);
          return `Switched to tab`;
        }

        case 'goBack': {
          if (!page) throw new Error('No active page');
          this.log('action', 'Going back');
          await page.goBack();
          return 'Navigated back';
        }

        case 'goForward': {
          if (!page) throw new Error('No active page');
          this.log('action', 'Going forward');
          await page.goForward();
          return 'Navigated forward';
        }

        case 'refresh': {
          if (!page) throw new Error('No active page');
          this.log('action', 'Refreshing page');
          await page.reload();
          return 'Page refreshed';
        }

        case 'evaluate': {
          if (!page) throw new Error('No active page');
          const script = action.params.script as string;
          this.log('action', `Evaluating script`);
          const result = await page.evaluate(script);
          const resultStr = JSON.stringify(result, null, 2) || 'undefined';
          return `Script result: ${resultStr}`;
        }

        default:
          throw new Error(`Unknown action type: ${action.type}`);
      }
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : String(err);
      this.log('error', `Action failed: ${action.type}`, errorMessage);
      throw err;
    }
  }

  async getPageContent(): Promise<string> {
    const page = this.getActivePage();
    if (!page) return '';
    try {
      const title = await page.title();
      const url = page.url();
      const bodyText = String(await page.evaluate(`
        (() => {
          const body = document.body;
          if (!body) return '';
          const walker = document.createTreeWalker(body, NodeFilter.SHOW_TEXT, null);
          const texts = [];
          let node;
          while ((node = walker.nextNode())) {
            const text = node.textContent?.trim();
            if (text && text.length > 1) texts.push(text);
          }
          return texts.slice(0, 100).join('\n');
        })()
      `));

      const interactiveElements = String(await page.evaluate(`
        (() => {
          const elements = [];
          const selectors = ['a[href]', 'button', 'input', 'textarea', 'select', '[role="button"]', '[onclick]'];
          for (const sel of selectors) {
            document.querySelectorAll(sel).forEach((el) => {
              const tag = el.tagName.toLowerCase();
              const text = (el.textContent || '').trim().substring(0, 50);
              const id = el.id ? '#' + el.id : '';
              const cls = el.className && typeof el.className === 'string' ? '.' + el.className.split(' ')[0] : '';
              const href = el.getAttribute('href') || '';
              const type = el.getAttribute('type') || '';
              const placeholder = el.getAttribute('placeholder') || '';
              elements.push('<' + tag + id + cls + (type ? ' type="' + type + '"' : '') + (href ? ' href="' + href + '"' : '') + (placeholder ? ' placeholder="' + placeholder + '"' : '') + '>' + text + '</' + tag + '>');
            });
          }
          return elements.slice(0, 50).join('\n');
        })()
      `));

      return `Title: ${title}\nURL: ${url}\n\nVisible Text:\n${bodyText.substring(0, 3000)}\n\nInteractive Elements:\n${interactiveElements.substring(0, 3000)}`;
    } catch {
      return `URL: ${page.url()}\n(Could not extract page content)`;
    }
  }

  async getCurrentScreenshot(): Promise<string | null> {
    const page = this.getActivePage();
    if (!page) return null;
    try {
      const buffer = await page.screenshot({ type: 'jpeg', quality: 80 });
      return buffer.toString('base64');
    } catch {
      return null;
    }
  }

  getTabs(): BrowserTab[] {
    const tabs: BrowserTab[] = [];
    for (const [id, page] of this.pages) {
      tabs.push({
        id,
        title: page.url().substring(0, 50),
        url: page.url(),
        isActive: id === this.activePageId,
      });
    }
    return tabs;
  }
}
