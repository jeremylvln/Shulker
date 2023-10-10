const fileReplacements = [
  {
    files: ['packages/shulker-operator/src/reconcilers/proxy_fleet/fleet.rs'],
    from: 'const PROXY_SHULKER_PROXY_AGENT_VERSION: &str = ".*";',
    to: 'const PROXY_SHULKER_PROXY_AGENT_VERSION: &str = "${nextRelease.version}";',
    results: [
      {
        file: 'packages/shulker-operator/src/reconcilers/proxy_fleet/fleet.rs',
        hasChanged: true,
        numMatches: 1,
        numReplacements: 1,
      },
    ],
    countMatches: true,
  },
  {
    files: [
      'packages/shulker-operator/src/reconcilers/minecraft_server/gameserver.rs',
    ],
    from: 'const MINECRAFT_SERVER_SHULKER_PROXY_AGENT_VERSION: &str = ".*";',
    to: 'const MINECRAFT_SERVER_SHULKER_PROXY_AGENT_VERSION: &str = "${nextRelease.version}";',
    results: [
      {
        file: 'packages/shulker-operator/src/reconcilers/minecraft_server/gameserver.rs',
        hasChanged: true,
        numMatches: 1,
        numReplacements: 1,
      },
    ],
    countMatches: true,
  },
  {
    files: ['packages/shulker-proxy-agent/src/bungeecord/resources/plugin.yml'],
    from: 'version: .*',
    to: 'version: ${nextRelease.version}',
    results: [
      {
        file: 'packages/shulker-proxy-agent/src/bungeecord/resources/plugin.yml',
        hasChanged: true,
        numMatches: 1,
        numReplacements: 1,
      },
    ],
    countMatches: true,
  },
  {
    files: [
      'packages/shulker-proxy-agent/src/velocity/kotlin/io/shulkermc/proxyagent/ShulkerProxyAgent.kt',
    ],
    from: 'version = ".*"',
    to: 'version = "${nextRelease.version}"',
    results: [
      {
        file: 'packages/shulker-proxy-agent/src/velocity/kotlin/io/shulkermc/proxyagent/ShulkerProxyAgent.kt',
        hasChanged: true,
        numMatches: 1,
        numReplacements: 1,
      },
    ],
    countMatches: true,
  },
  {
    files: ['packages/shulker-server-agent/src/paper/resources/plugin.yml'],
    from: 'version: .*',
    to: 'version: ${nextRelease.version}',
    results: [
      {
        file: 'packages/shulker-server-agent/src/paper/resources/plugin.yml',
        hasChanged: true,
        numMatches: 1,
        numReplacements: 1,
      },
    ],
    countMatches: true,
  },
];

module.exports = {
  branches: ['main'],
  plugins: [
    '@semantic-release/commit-analyzer',
    '@semantic-release/release-notes-generator',
    [
      '@semantic-release/changelog',
      {
        changelogFile: 'CHANGELOG.md',
      },
    ],
    [
      'semantic-release-replace-plugin',
      {
        replacements: fileReplacements,
      },
    ],
    [
      '@codedependant/semantic-release-docker',
      {
        dockerRegistry: 'ghcr.io',
        dockerProject: 'jeremylvln',
        dockerImage: 'shulker-operator',
        dockerFile: 'packages/shulker-operator/Dockerfile',
        dockerBuildFlags: {
          platform: ['linux/amd64', 'linux/arm64/v8'].join(','),
        },
        dockerLogin: false,
      },
    ],
    [
      '@semantic-release/git',
      {
        assets: [
          'package.json',
          'package-lock.json',
          'CHANGELOG.md',
          'gradle.properties',
          ...fileReplacements.flatMap((replacement) =>
            replacement.results.map((result) => result.file)
          ),
        ],
      },
    ],
    [
      '@semantic-release/github',
      {
        assets: ['CHANGELOG.md'].map((path) => ({ path })),
        failComment: false,
        releasedLabels: false,
        discussionCategoryName: 'Announcements',
      },
    ],
  ],
};
