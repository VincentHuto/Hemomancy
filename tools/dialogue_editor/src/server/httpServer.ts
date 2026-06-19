import { createServer, type IncomingMessage, type ServerResponse } from 'node:http';
import { readFileSync } from 'node:fs';
import { extname, join } from 'node:path';
import type { NpcMetadata, PreviewRequest } from '../shared/types';
import {
  applyPreview,
  defaultRepoRoot,
  loadDialogueFile,
  loadMetadata,
  loadWorkspace,
  previewWorkspaceChanges,
  safeResolve,
  saveMetadata
} from './workspace';

const repoRoot = process.env.HEMO_REPO_ROOT ?? defaultRepoRoot();
const port = Number(process.env.DIALOGUE_API_PORT ?? 5175);

const server = createServer(async (req, res) => {
  try {
    if (!req.url) return send(res, 404, { error: 'Missing URL' });
    const url = new URL(req.url, `http://${req.headers.host ?? 'localhost'}`);

    if (req.method === 'GET' && url.pathname === '/api/workspace') {
      return send(res, 200, await loadWorkspace(repoRoot));
    }

    if (req.method === 'GET' && url.pathname.startsWith('/api/dialogue/')) {
      const file = decodeURIComponent(url.pathname.slice('/api/dialogue/'.length));
      return send(res, 200, await loadDialogueFile(repoRoot, file));
    }

    if (req.method === 'POST' && url.pathname === '/api/preview') {
      return send(res, 200, await previewWorkspaceChanges(repoRoot, await readJson(req) as PreviewRequest));
    }

    if (req.method === 'POST' && url.pathname === '/api/apply') {
      const body = await readJson(req) as { id?: string };
      if (!body.id) return send(res, 400, { error: 'Missing preview id' });
      await applyPreview(repoRoot, body.id);
      return send(res, 200, { ok: true });
    }

    if (req.method === 'GET' && url.pathname.startsWith('/api/metadata/')) {
      const speaker = decodeURIComponent(url.pathname.slice('/api/metadata/'.length));
      return send(res, 200, loadMetadata(process.cwd(), speaker));
    }

    if (req.method === 'POST' && url.pathname.startsWith('/api/metadata/')) {
      const speaker = decodeURIComponent(url.pathname.slice('/api/metadata/'.length));
      const body = await readJson(req) as NpcMetadata;
      saveMetadata(process.cwd(), speaker, body);
      return send(res, 200, { ok: true });
    }

    if (req.method === 'GET' && !url.pathname.startsWith('/api/')) {
      return serveStatic(res, url.pathname);
    }

    return send(res, 404, { error: 'Not found' });
  } catch (err) {
    return send(res, 500, { error: err instanceof Error ? err.message : String(err) });
  }
});

server.listen(port, () => {
  console.log(`Dialogue Workspace API listening on http://localhost:${port}`);
});

async function readJson(req: IncomingMessage): Promise<unknown> {
  const chunks: Buffer[] = [];
  for await (const chunk of req) chunks.push(Buffer.from(chunk));
  return JSON.parse(Buffer.concat(chunks).toString('utf8') || '{}');
}

function send(res: ServerResponse, status: number, body: unknown): void {
  res.writeHead(status, {
    'content-type': 'application/json; charset=utf-8',
    'access-control-allow-origin': '*'
  });
  res.end(JSON.stringify(body));
}

function serveStatic(res: ServerResponse, pathname: string): void {
  const staticRoot = join(process.cwd(), 'dist');
  const file = pathname === '/' ? 'workspace.html' : pathname.slice(1);
  const abs = safeResolve(staticRoot, file);
  const body = readFileSync(abs);
  const type = {
    '.html': 'text/html',
    '.js': 'text/javascript',
    '.css': 'text/css'
  }[extname(abs)] ?? 'application/octet-stream';
  res.writeHead(200, { 'content-type': type });
  res.end(body);
}
