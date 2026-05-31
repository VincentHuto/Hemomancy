import { spawn } from 'node:child_process';

const commands = [
  ['API', 'npx', ['tsx', 'src/server/httpServer.ts']],
  ['UI', 'npx', ['vite', '--host', '127.0.0.1']]
];

for (const [label, cmd, args] of commands) {
  const child = spawn(cmd, args, {
    stdio: 'pipe',
    shell: true,
    env: { ...process.env }
  });
  child.stdout.on('data', data => process.stdout.write(`[${label}] ${data}`));
  child.stderr.on('data', data => process.stderr.write(`[${label}] ${data}`));
  child.on('exit', code => {
    if (code && code !== 0) process.exitCode = code;
  });
}

console.log('Skill Tree Editor dev servers starting...');
console.log('Open http://127.0.0.1:5184/workspace.html');
