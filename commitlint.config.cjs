const {
  utils: { getProjects },
} = require('@commitlint/config-nx-scopes');

const EXTRA_SCOPES = ['deps'];

module.exports = {
  extends: ['@commitlint/config-conventional'],
  rules: {
    'scope-enum': (ctx) =>
      getProjects(ctx).then((packages) => [
        2,
        'always',
        [...packages, ...EXTRA_SCOPES],
      ]),
  },
};
