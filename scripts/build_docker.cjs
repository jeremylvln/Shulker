const { execSync } = require('child_process');

const showUsage = () => {
  console.log(
    'Usage: node build_docker.cjs <appName> <dockerfilePath> <version>',
  );

  process.exit(1);
};

const appName = process.argv[2];
const dockerfilePath = process.argv[3];
const version = process.argv[4];

if (
  typeof appName !== 'string' ||
  appName.length === 0 ||
  typeof dockerfilePath !== 'string' ||
  dockerfilePath.length === 0
) {
  showUsage();
}

const baseTag = `ghcr.io/jeremylvln/${appName}`;
const tags =
  typeof version === 'string'
    ? [
        `${baseTag}:latest`,
        `${baseTag}:${version.split('.')[0]}-latest`,
        `${baseTag}:${version}`,
      ]
    : [`${baseTag}:next`];

const command = [
  'docker',
  'build',
  '--push',
  '-f',
  dockerfilePath,
  ...tags.flatMap((tag) => ['-t', tag]),
  ...(process.env.DOCKER_BUILD_PLATFORMS
    ? ['--platform', process.env.DOCKER_BUILD_PLATFORMS]
    : []),
  '.',
].join(' ');

console.log(`Executing command: ${command}`);

execSync(command, {
  stdio: 'inherit',
});
