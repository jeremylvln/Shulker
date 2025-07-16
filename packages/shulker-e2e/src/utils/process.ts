import { spawn } from 'node:child_process';
import { logger, REPOSITORY_ROOT, VERBOSE } from './index.ts';

export function execAndWait(
  command: string | string[],
  options?: { silent?: boolean },
): Promise<string> {
  const args = Array.isArray(command) ? command : [command];

  logger.debug(`running command '${args.join(' ')}'`);
  const child = spawn('bash', ['-c', args.join(' ')], {
    cwd: REPOSITORY_ROOT,
  });

  let stdout = '';
  child.stdout.on('data', (data) => {
    stdout += data.toString();
  });

  if (VERBOSE && options?.silent !== true) {
    child.stdout.on('data', (data) => {
      process.stdout.write(data.toString());
    });
    child.stderr.on('data', (data) => {
      process.stderr.write(data.toString());
    });
  }

  return new Promise((resolve, reject) => {
    child.on('close', (code) => {
      if (code === 0) {
        resolve(stdout.trimEnd());
      } else {
        reject(new Error(`Process ended with code ${code}`));
      }
    });
  });
}
