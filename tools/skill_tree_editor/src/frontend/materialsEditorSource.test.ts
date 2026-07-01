import { existsSync, readFileSync } from 'node:fs';
import { resolve } from 'node:path';

test('materials editor page is wired into Vite API routing and dev output', () => {
  const htmlPath = resolve(process.cwd(), 'materials.html');
  const html = readFileSync(htmlPath, 'utf8');
  const vite = readFileSync(resolve(process.cwd(), 'vite.config.ts'), 'utf8');
  const dev = readFileSync(resolve(process.cwd(), 'scripts/dev.mjs'), 'utf8');
  const server = readFileSync(resolve(process.cwd(), 'src/server/httpServer.ts'), 'utf8');

  expect(existsSync(htmlPath)).toBe(true);
  expect(html).toContain('/src/frontend/materials.ts');
  expect(vite).toContain('materials.html');
  expect(dev).toContain('/materials.html');
  expect(server).toContain("'/api/materials'");
  expect(server).toContain("'/api/materials/preview'");
});

test('materials frontend exposes graph layers inspector creation and editor navigation', () => {
  const source = readFileSync(resolve(process.cwd(), 'src/frontend/materials.ts'), 'utf8');

  expect(source).toContain('material-node');
  expect(source).toContain('bucket-root');
  expect(source).toContain('label-plaque');
  expect(source).toContain('atlas-trace');
  expect(source).toContain('beginDragPan');
  expect(source).toContain('updateDragPan');
  expect(source).toContain("classList.add('panning')");
  expect(source).toContain('create-entry');
  expect(source).toContain('Unknown material');
  expect(source).toContain('Skills');
  expect(source).toContain('Manipulations');
});
