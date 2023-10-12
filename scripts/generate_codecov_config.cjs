const fs = require('node:fs');
const path = require('node:path');
const YAML = require('yaml');
const {
  buildProjectGraphWithoutDaemon,
} = require('nx/src/project-graph/project-graph');

const PROJECTS_TO_EXCLUDE = ['docs'];

async function main() {
  const graph = await buildProjectGraphWithoutDaemon();
  const codecovConfigPath = path.join(__dirname, '..', 'codecov.yml');
  const codecovConfig = YAML.parse(fs.readFileSync(codecovConfigPath, 'utf8'));

  codecovConfig.flags = {};
  Object.keys(codecovConfig.coverage.status.project)
    .filter((name) => name !== 'default')
    .forEach((name) => {
      delete codecovConfig.coverage.status.project[name];
    });

  const projects = Object.values(graph.nodes).filter(
    (project) =>
      !PROJECTS_TO_EXCLUDE.includes(project.name) &&
      'test' in project.data.targets &&
      'outputs' in project.data.targets.test,
  );

  projects.forEach((project) => {
    codecovConfig.coverage.status.project[project.name] = {
      flags: [project.name],
    };

    codecovConfig.flags[project.name] = {
      paths: [project.data.root],
      carryforward: true,
    };
  });

  fs.writeFileSync(codecovConfigPath, YAML.stringify(codecovConfig));

  const uploadCommands = projects.map((project) => {
    const coverageOutputs = project.data.targets.test.outputs;
    const args = ['-t', '$CODECOV_TOKEN', '-F', project.name];

    coverageOutputs.forEach((coverageOutput) => {
      args.push('-f', coverageOutput.replace('{workspaceRoot}', '.'));
    });

    return ['$CODECOV', ...args].join(' ');
  });

  const uploadScript = [
    '#!/bin/bash',
    'CODECOV="${CODECOV:-codecov}"',
    ...uploadCommands,
    '',
  ].join('\n');
  fs.writeFileSync(
    path.join(__dirname, 'upload_codecov_files.sh'),
    uploadScript,
  );
}

main();
