import { createServer, type IncomingMessage, type ServerResponse } from 'node:http';
import { existsSync, readFileSync } from 'node:fs';
import { extname, join } from 'node:path';
import type { ManipulationPreviewRequest, PreviewRequest } from '../shared/types';
import {
  applyPreview,
  defaultRepoRoot,
  loadWorkspace,
  previewWorkspaceChanges,
  safeResolve
} from './workspace';
import { loadManipulationWorkspace, previewManipulationWorkspaceChanges } from './manipulationWorkspace';

const repoRoot = process.env.HEMO_REPO_ROOT ?? defaultRepoRoot();
const port = Number(process.env.SKILL_TREE_API_PORT ?? 5185);

const server = createServer(async (req, res) => {
  try {
    if (!req.url) return send(res, 404, { error: 'Missing URL' });
    const url = new URL(req.url, `http://${req.headers.host ?? 'localhost'}`);

    if (req.method === 'GET' && url.pathname === '/api/workspace') {
      return send(res, 200, await loadWorkspace(repoRoot));
    }

    if (req.method === 'GET' && url.pathname === '/api/manipulations') {
      return send(res, 200, await loadManipulationWorkspace(repoRoot));
    }

    if (req.method === 'POST' && url.pathname === '/api/preview') {
      return send(res, 200, await previewWorkspaceChanges(repoRoot, await readJson(req) as PreviewRequest));
    }

    if (req.method === 'POST' && url.pathname === '/api/manipulations/preview') {
      return send(res, 200, await previewManipulationWorkspaceChanges(repoRoot, await readJson(req) as ManipulationPreviewRequest));
    }

    if (req.method === 'POST' && url.pathname === '/api/apply') {
      const body = await readJson(req) as { id?: string };
      if (!body.id) return send(res, 400, { error: 'Missing preview id' });
      await applyPreview(repoRoot, body.id);
      return send(res, 200, { ok: true });
    }

    if (req.method === 'GET' && url.pathname.startsWith('/asset/item/')) {
      return serveItemAsset(res, decodeURIComponent(url.pathname.slice('/asset/item/'.length)));
    }

    if (req.method === 'GET' && url.pathname.startsWith('/asset/block/')) {
      return serveBlockAsset(res, decodeURIComponent(url.pathname.slice('/asset/block/'.length)));
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
  console.log(`Skill Tree Editor API listening on http://localhost:${port}`);
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

function serveItemAsset(res: ServerResponse, fileName: string): void {
  const id = fileName.replace(/\.png$/i, '');
  if (!/^[a-z0-9_]+$/.test(id)) {
    return send(res, 400, { error: 'Invalid item asset id' });
  }
  const candidates = [
    `src/main/resources/assets/hemomancy/textures/item/${id}.png`,
    `src/main/resources/assets/hemomancy/textures/item/scars/${id}.png`,
    `src/main/resources/assets/hemomancy/textures/item/memories/${id}.png`,
    `src/main/resources/assets/hemomancy/textures/item/memories/${id}_overlay.png`
  ];
  for (const candidate of candidates) {
    const abs = safeResolve(repoRoot, candidate);
    if (existsSync(abs)) {
      res.writeHead(200, { 'content-type': 'image/png' });
      res.end(readFileSync(abs));
      return;
    }
  }
  const letter = id.replace(/^skill_/, '').charAt(0).toUpperCase() || '?';
  const fallback = `<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24">
    <rect width="24" height="24" fill="#180304"/>
    <text x="12" y="16" text-anchor="middle" font-family="monospace" font-size="12" fill="#d64c42">${letter}</text>
  </svg>`;
  res.writeHead(200, { 'content-type': 'image/svg+xml; charset=utf-8' });
  res.end(fallback);
}

function serveBlockAsset(res: ServerResponse, fileName: string): void {
  const id = fileName.replace(/\.png$/i, '');
  if (!/^[a-z0-9_]+$/.test(id)) {
    return send(res, 400, { error: 'Invalid block asset id' });
  }
  const candidates = [
    `src/main/resources/assets/hemomancy/textures/block/${id}.png`,
    `src/main/resources/assets/hemomancy/textures/block/${id}_top.png`,
    `src/main/resources/assets/hemomancy/textures/block/${id}_front.png`,
    `src/main/resources/assets/hemomancy/textures/block/${id}_side.png`
  ];
  for (const candidate of candidates) {
    const abs = safeResolve(repoRoot, candidate);
    if (existsSync(abs)) {
      res.writeHead(200, { 'content-type': 'image/png' });
      res.end(readFileSync(abs));
      return;
    }
  }
  const letter = id.charAt(0).toUpperCase() || '?';
  const fallback = `<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24">
    <rect width="24" height="24" fill="#11140e"/>
    <rect x="4" y="4" width="16" height="16" fill="#25301d" stroke="#7c9a4b"/>
    <text x="12" y="16" text-anchor="middle" font-family="monospace" font-size="10" fill="#d7e5b5">${letter}</text>
  </svg>`;
  res.writeHead(200, { 'content-type': 'image/svg+xml; charset=utf-8' });
  res.end(fallback);
}
