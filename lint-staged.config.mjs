export default {
  '*': 'prettier --ignore-unknown --write',
  '*.rs': () => 'cargo fmt',
  'packages/shulker-crds/src/**/*.rs': () => [
    'cargo run --bin crdgen',
    'git add "kube/helm/**/crds/*.yaml"',
  ],
};
