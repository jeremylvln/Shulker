const fs = require('node:fs');
const path = require('node:path');
const { readCachedProjectGraph } = require('@nx/devkit');
const YAML = require('yaml');

const PROJECTS_TO_EXCLUDE = ['docs'];

const graph = readCachedProjectGraph();
const codecovConfigPath = path.join(__dirname, '..', 'codecov.yml');
const codecovConfig = YAML.parse(fs.readFileSync(codecovConfigPath, 'utf8'));

codecovConfig.flags = {};
Object.keys(codecovConfig.coverage.status.project)
  .filter((name) => name !== 'default')
  .forEach((name) => {
    delete codecovConfig.coverage.status.project[name];
  });

const projectsToRoot = Object.values(graph.nodes)
  .filter((project) => !PROJECTS_TO_EXCLUDE.includes(project.name))
  .reduce((acc, project) => {
    acc[project.name] = project.data.root;
    return acc;
  }, {});

Object.entries(projectsToRoot).forEach(([name, root]) => {
  codecovConfig.coverage.status.project[name] = {
    flags: [name],
  };

  codecovConfig.flags[name] = {
    paths: [root],
    carryforward: true,
  };
});

fs.writeFileSync(codecovConfigPath, YAML.stringify(codecovConfig));
