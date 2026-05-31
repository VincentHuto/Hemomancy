import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';

test('dev server watches API sources so preview/apply metadata stays current', () => {
  const source = readFileSync(resolve(process.cwd(), 'scripts/dev.mjs'), 'utf8');

  expect(source).toContain("['API', 'npx', ['tsx', 'watch', 'src/server/httpServer.ts']]");
});
