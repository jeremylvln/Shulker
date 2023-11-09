const fileReplacements = [
  {
    files: ['package.json'],
    from: '"version": ".*"',
    to: '"version": "${nextRelease.version}"',
    results: [
      {
        file: 'package.json',
        hasChanged: true,
        numMatches: 1,
        numReplacements: 1,
      },
    ],
    countMatches: true,
  },
  {
    files: ['Cargo.toml'],
    from: '\\[workspace\\.package\\]\nversion = ".*"',
    to: '[workspace.package]\nversion = "${nextRelease.version}"',
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
    from: 'version = .*',
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
  {
    files: ['kube/helm/Chart.yaml', 'kube/helm/charts/*/Chart.yaml'],
    from: 'appVersion: .*',
    to: "appVersion: '${nextRelease.version}'",
    results: [
      {
        file: 'kube/helm/Chart.yaml',
        hasChanged: true,
        numMatches: 1,
        numReplacements: 1,
      },
      {
        file: 'kube/helm/charts/shulker-addon-matchmaking/Chart.yaml',
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
    [
      '@semantic-release/release-notes-generator',
      {
        preset: 'conventionalcommits',
        parserOpts: {
          noteKeywords: ['BREAKING CHANGE', 'BREAKING CHANGES', 'BREAKING'],
        },
        presetConfig: {
          types: [
            ['feat', 'Features', ':sparkles:'],
            ['fix', 'Bug Fixes', ':bug:'],
            ['perf', 'Performance Improvements', ':bar_chart:'],
            ['revert', 'Reverts', ':warning:'],
            ['docs', 'Documentation', ':books:'],
          ].map(([type, name, emoji]) => ({
            type,
            section: `${emoji} ${name} ${emoji}`,
            hidden: false,
          })),
        },
      },
    ],
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
      '@semantic-release/exec',
      {
        publishCmd:
          'npx nx run-many --target=docker --all --parallel 1 --output-style stream -- ${nextRelease.version}',
      },
    ],
    [
      '@semantic-release/exec',
      {
        publishCmd: './gradlew publish',
      },
    ],
    [
      '@semantic-release/exec',
      {
        publishCmd: 'cd kube/manifests && ./generate_from_helm.sh',
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
          'kube/manifests/*.yaml',
        ],
      },
    ],
    [
      '@semantic-release/github',
      {
        assets: [
          'CHANGELOG.md',
          'kube/manifests/stable.yaml',
          'kube/manifests/stable-with-prometheus.yaml',
        ].map((path) => ({ path })),
        failComment: false,
        releasedLabels: false,
        discussionCategoryName: 'Announcements',
      },
    ],
  ],
};
