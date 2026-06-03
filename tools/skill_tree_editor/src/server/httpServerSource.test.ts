import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';

test('item asset server can serve direct memory layer textures', () => {
  const source = readFileSync(resolve(process.cwd(), 'src/server/httpServer.ts'), 'utf8');

  expect(source).toContain('textures/item/memories/${id}.png');
  expect(source).toContain('textures/item/memories/${id}_overlay.png');
});
