const {
  buildProjectGraphWithoutDaemon,
} = require('nx/src/project-graph/project-graph');

const EXTRA_SCOPES = ['deps'];

const listNxProjects = async () => {
  const graph = await buildProjectGraphWithoutDaemon();
  return Object.values(graph.nodes).map((project) => project.name);
};

module.exports = {
  extends: ['@commitlint/config-conventional'],
  rules: {
    'scope-enum': () =>
      listNxProjects().then((packages) => [
        2,
        'always',
        [...packages, ...EXTRA_SCOPES],
      ]),
  },
};
