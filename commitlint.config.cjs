const {
  buildProjectGraphAndSourceMapsWithoutDaemon,
} = require('nx/src/project-graph/project-graph');

const EXTRA_SCOPES = ['release', 'deps', 'deploy'];

const listNxProjects = async () => {
  const { projectGraph } = await buildProjectGraphAndSourceMapsWithoutDaemon();
  return Object.values(projectGraph.nodes).map((project) => project.name);
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
