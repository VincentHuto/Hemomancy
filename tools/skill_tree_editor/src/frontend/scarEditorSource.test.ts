import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';
import { describe, expect, it } from 'vitest';

describe('scar editor entrypoint', () => {
  it('is a Vite page backed by the scar API and shared graph controls', () => {
    const root = process.cwd();
    const html = readFileSync(resolve(root, 'scars.html'), 'utf8');
    const vite = readFileSync(resolve(root, 'vite.config.ts'), 'utf8');
    const source = readFileSync(resolve(root, 'src/frontend/scars.ts'), 'utf8');
    const server = readFileSync(resolve(root, 'src/server/httpServer.ts'), 'utf8');

    expect(html).toContain('/src/frontend/scars.ts');
    expect(vite).toContain("scars: 'scars.html'");
    expect(server).toContain("url.pathname === '/api/scars'");
    expect(server).toContain("url.pathname === '/api/scars/preview'");
    expect(source).toContain("from './connectionEditing'");
    expect(source).toContain("from './dragPan'");
    expect(source).toContain("from './layoutEditing'");
    expect(source).toContain("from './movementHistory'");
    expect(source).toContain("from './viewportZoom'");
  });
});
