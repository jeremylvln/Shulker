/** @type {import("jest").Config} **/
export default {
  testEnvironment: 'node',
  globalSetup: '<rootDir>/src/global-setup.ts',
  globalTeardown: '<rootDir>/src/global-teardown.ts',
  setupFilesAfterEnv: ['jest-extended/all'],
  rootDir: '.',
  extensionsToTreatAsEsm: ['.ts'],
  transform: {
    '\\.ts$': '<rootDir>/jest.typestripping.mjs',
  },
  verbose: true,
};
