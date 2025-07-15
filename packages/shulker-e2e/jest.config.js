/** @type {import("jest").Config} **/
export default {
  testEnvironment: 'node',
  globalSetup: '<rootDir>/src/setup.ts',
  globalTeardown: '<rootDir>/src/teardown.ts',
  rootDir: '.',
};
