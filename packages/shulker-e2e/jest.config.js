/** @type {import("jest").Config} **/
export default {
  testEnvironment: 'node',
  globalSetup: '<rootDir>/src/setup/global-setup.ts',
  globalTeardown: '<rootDir>/src/setup/global-teardown.ts',
  setupFilesAfterEnv: ['jest-extended/all'],
  rootDir: '.',
  extensionsToTreatAsEsm: ['.ts'],
  transform: {
    '\\.ts$': '<rootDir>/jest.typestripping.mjs',
  },
  verbose: true,
  maxWorkers: 1,
  reporters: ['default', '<rootDir>/jest.failfast.mjs'],
  forceExit: true,
  testTimeout: 300_000,
};
