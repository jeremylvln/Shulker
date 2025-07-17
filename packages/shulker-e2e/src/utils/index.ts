import { pino } from 'pino';
import { default as pretty } from 'pino-pretty';

import path from 'node:path';

export const VERBOSE = process.env.VERBOSE === 'true';
export const REPOSITORY_ROOT = path.resolve(
  import.meta.dirname,
  '..',
  '..',
  '..',
  '..',
);

export const logger = pino(
  { level: 'debug' },
  pretty({ sync: true, singleLine: true }),
);
