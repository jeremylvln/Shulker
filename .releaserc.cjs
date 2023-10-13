const fileReplacements = [
  {
    files: ['Cargo.toml'],
    from: '^version = ".*"$',
    to: 'version = "${nextRelease.version}"',
    results: [
      {
        file: 'Cargo.toml',
        hasChanged: true,
        numMatches: 1,
        numReplacements: 1,
      },
    ],
    countMatches: true,
  },
  {
    files: ['gradle.properties'],
    from: '^version = .*$',
    to: 'version = ${nextRelease.version}',
    results: [
      {
        file: 'gradle.properties',
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
      '@semantic-release/exec',
      {
        publishCmd: './gradlew publish',
      },
    ],
    [
      '@semantic-release/git',
      {
        assets: [
          'package.json',
          'package-lock.json',
          'CHANGELOG.md',
          ...fileReplacements.flatMap((replacement) =>
            replacement.results.map((result) => result.file),
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
