## [0.3.0](https://github.com/jeremylvln/Shulker/compare/v0.2.0...v0.3.0) (2023-10-31)

### :sparkles: Features :sparkles:

- **shulker-crds:** add redis provided spec to cluster ([490db51](https://github.com/jeremylvln/Shulker/commit/490db51685f025c3c8ae4833022aa037d7dd58ce))
- **shulker-operator:** add ability to register hook with order ([76f6730](https://github.com/jeremylvln/Shulker/commit/76f673071587c9ee0e5840d3437a4e24b412870b))
- **shulker-operator:** deploy a managed single node redis ([#183](https://github.com/jeremylvln/Shulker/issues/183)) ([3e99751](https://github.com/jeremylvln/Shulker/commit/3e9975110eae92a5cee4ebfbf84ada1996fae6cd))
- **shulker-proxy-agent:** add glist, gtp and gfind commands ([850af26](https://github.com/jeremylvln/Shulker/commit/850af2615347fc7b0384ad1a33d7d305c7baf3d2))
- **shulker-proxy-agent:** display global player count on ping ([c29eac1](https://github.com/jeremylvln/Shulker/commit/c29eac17501fbf8942247de8c9b4e73546a6146a))
- **shulker-proxy-agent:** listen on teleport player pub sub ([e4f8d17](https://github.com/jeremylvln/Shulker/commit/e4f8d17804bdd4a1b08d584a461375be1cfdf576))
- **shulker-proxy-agent:** query mojang api to get unknown player names or ids ([e7eb734](https://github.com/jeremylvln/Shulker/commit/e7eb734e52cf655459314b3d65bccb3e8df05eaf))
- **shulker-proxy-agent:** sync proxies and players in Redis ([ec0211c](https://github.com/jeremylvln/Shulker/commit/ec0211c7a723e42b02f0c72ced46782d2bcb2b0e))
- **shulker-sdk:** add convenient Java wrapper ([4ac0c8c](https://github.com/jeremylvln/Shulker/commit/4ac0c8c25f9a5f70515f7dcf9fad03d334609b0f))

### :bug: Bug Fixes :bug:

- publish java libraries as normal jar ([d4083d1](https://github.com/jeremylvln/Shulker/commit/d4083d1e7f82741d52a41f028b94c16e6e1f5574))
- **shulker-operator:** assert valid cluster ref before reconciling ([#184](https://github.com/jeremylvln/Shulker/issues/184)) ([c207cce](https://github.com/jeremylvln/Shulker/commit/c207ccee0526dfc0bcebd75b47da91f81f5feeba))
- **shulker-operator:** keep extra annotations when reconciling a resource ([229b50c](https://github.com/jeremylvln/Shulker/commit/229b50c7347fbdeb8ad63c31081e8822f8e7184f))
- **shulker-operator:** patch existing resources with a fresh spec ([d2fcea3](https://github.com/jeremylvln/Shulker/commit/d2fcea387399679a5f09da6e7a697b017c8af65c))
- **shulker-operator:** plugins and patches joined with bad separator ([13d142e](https://github.com/jeremylvln/Shulker/commit/13d142ee40bef5c4553e114aaf9d808e641dfd84))
- **shulker-operator:** resolve maven secret in correct namespace ([a7b8d14](https://github.com/jeremylvln/Shulker/commit/a7b8d14ee7573646d06138616c12f5065df98670))
- **shulker-proxy-agent:** sync players, proxies and servers in Redis ([06529f6](https://github.com/jeremylvln/Shulker/commit/06529f68d34b53ba79443b1f668d8b2d89d7eb9a))

### :books: Documentation :books:

- fix dead link in README ([048bb66](https://github.com/jeremylvln/Shulker/commit/048bb66fe4cff3302948471f8872820ebed98442))

## [0.2.0](https://github.com/jeremylvln/Shulker/compare/v0.1.0...v0.2.0) (2023-10-25)

### :sparkles: Features :sparkles:

- add nodeselector to pod overrides ([123f169](https://github.com/jeremylvln/Shulker/commit/123f1697ed12de9d3c042e450f23556b8fa0e969))
- add tolerations to pod overrides ([4290b19](https://github.com/jeremylvln/Shulker/commit/4290b19d8c16256379faf34b23be10de119c1d21))
- **java-sdk:** create java sdk ([745b299](https://github.com/jeremylvln/Shulker/commit/745b299bc4e05533d906dba5a94bbf86707eebad))
- move API source to agents projects [breaking change] ([c307c02](https://github.com/jeremylvln/Shulker/commit/c307c02731bb778abae0761352ebe02cdb4a5cf4))
- **operator:** add built-in autoscaling to fleets ([#112](https://github.com/jeremylvln/Shulker/issues/112)) ([1dc72a1](https://github.com/jeremylvln/Shulker/commit/1dc72a1c4ca0a8affb304ec4239e111b2af4f15c))
- **server-agent:** add explicit api-version in paper yml ([5ab4147](https://github.com/jeremylvln/Shulker/commit/5ab4147151ade8098808bac42921638529a95ec9))

### :bug: Bug Fixes :bug:

- correctly update proxy service annotations ([40db68b](https://github.com/jeremylvln/Shulker/commit/40db68bb599834d79a898741889184ed1c695963))
- **deps:** manual bump ([#128](https://github.com/jeremylvln/Shulker/issues/128)) ([7d9dbdf](https://github.com/jeremylvln/Shulker/commit/7d9dbdfb20f82eb6337116435566df960dda39a9))
- **deps:** update dependency net.kyori:adventure-api to v4.14.0 ([#137](https://github.com/jeremylvln/Shulker/issues/137)) ([8cea5eb](https://github.com/jeremylvln/Shulker/commit/8cea5eb3b3627faefa12e95748234cb79e46b6b5))
- **deps:** update dependency net.kyori:adventure-platform-bungeecord to v4.3.0 ([#138](https://github.com/jeremylvln/Shulker/issues/138)) ([293d43d](https://github.com/jeremylvln/Shulker/commit/293d43d8d584b13bd1695c80c06bfbd400684610))
- **deps:** update dependency net.kyori:adventure-platform-bungeecord to v4.3.1 ([#145](https://github.com/jeremylvln/Shulker/issues/145)) ([e5e2c84](https://github.com/jeremylvln/Shulker/commit/e5e2c84cc14c347d7bf72711ffd74c3c04915fa7))
- **deps:** update dependency net.md-5:bungeecord-api to v1.20-r0.1 ([#139](https://github.com/jeremylvln/Shulker/issues/139)) ([355db1e](https://github.com/jeremylvln/Shulker/commit/355db1ee660793b54052be13abc2f8b3f230e9cb))
- **deps:** update fabric8-kubernetes-client monorepo to v6.8.1 ([#140](https://github.com/jeremylvln/Shulker/issues/140)) ([47704cf](https://github.com/jeremylvln/Shulker/commit/47704cf864c9d5ef582794a9c10230e8d324e556))
- **deps:** update fabric8-kubernetes-client monorepo to v6.9.0 ([#168](https://github.com/jeremylvln/Shulker/issues/168)) ([62b1517](https://github.com/jeremylvln/Shulker/commit/62b1517379b8ac714f4069d394092b3c524ba6a8))
- listen and update correctly the resources ([e69d3de](https://github.com/jeremylvln/Shulker/commit/e69d3dea285878e73626b19f2dd7587151d41d8f))
- **shulker-crds:** use native default system whenever possible ([#153](https://github.com/jeremylvln/Shulker/issues/153)) ([2f89a51](https://github.com/jeremylvln/Shulker/commit/2f89a51f3d7028fa03e054b8f958effbdf907dd3))

### :books: Documentation :books:

- add CODE_OF_CONDUCT.md ([d7fcc0b](https://github.com/jeremylvln/Shulker/commit/d7fcc0b51abb7745ed72023602980dde2e315f42))
- add coverage badge ([1756f67](https://github.com/jeremylvln/Shulker/commit/1756f676ec902be1dc905016040891a0b646661e))
- add open graph meta ([7560590](https://github.com/jeremylvln/Shulker/commit/756059073717cdae4bbca7c80c57356074ac5b06))
- add recipes for plugins, pod overrides, and proxy protocol ([#117](https://github.com/jeremylvln/Shulker/issues/117)) ([ad935a2](https://github.com/jeremylvln/Shulker/commit/ad935a26b4221909a097a0b18c4985f1412b5426))
- add text about commercial license ([40ecd71](https://github.com/jeremylvln/Shulker/commit/40ecd7124be49b55e791e36b78184c8f7f2ff96e))
- fix header dead link ([037575b](https://github.com/jeremylvln/Shulker/commit/037575b244d1df7c6339889091d08537b33e7c8b))
- fix proxyfleet in example ([#110](https://github.com/jeremylvln/Shulker/issues/110)) ([99d7ca0](https://github.com/jeremylvln/Shulker/commit/99d7ca0691fa4052afaad7599a0eee92725e9431))
- migrate to vitepress ([#165](https://github.com/jeremylvln/Shulker/issues/165)) ([fe7a7e4](https://github.com/jeremylvln/Shulker/commit/fe7a7e4a54171e072d1fad767de0ad10005e2eb8))
