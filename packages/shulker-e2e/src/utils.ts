import { pino } from 'pino';
import { default as pretty } from 'pino-pretty';

import { spawn } from 'child_process';

const VERBOSE = process.env.VERBOSE === 'true';

export const logger = pino({ level: 'debug' }, pretty({ sync: true }));

export function execAndWait(
  command: string | string[],
  options?: { silent?: boolean },
): Promise<string> {
  const args = Array.isArray(command) ? command : [command];

  logger.debug(`running command '${args.join(' ')}'`);
  const child = spawn('bash', ['-c', args.join(' ')]);

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
