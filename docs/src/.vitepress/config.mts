import { defineConfig } from 'vitepress';

const REPOSITORY_HOME = 'https://github.com/jeremylvln/Shulker';

export default defineConfig({
  title: 'Shulker',
  description: 'Put Minecraft in a box',

  head: [
    [
      'link',
      {
        rel: 'icon',
        type: 'image/png',
        size: '32x32',
        href: '/favicon-32x32.png',
      },
    ],
    [
      'link',
      {
        rel: 'icon',
        type: 'image/png',
        size: '16x16',
        href: '/favicon-16x16.png',
      },
    ],
    [
      'link',
      {
        rel: 'apple-touch-icon',
        size: '152x152',
        href: '/apple-touch-icon.png',
      },
    ],
    ['link', { rel: 'manifest', href: '/site.webmanifest' }],
    [
      'link',
      { rel: 'mask-icon', color: '#5bbad5', href: '/safari-pinned-tab.svg' },
    ],
    ['meta', { name: 'msapplication-TileColor', content: '#603cba' }],
    ['meta', { name: 'theme-color', content: '#7f00ff' }],
  ],

  markdown: {
    lineNumbers: true,
  },

  themeConfig: {
    logo: '/logo.png',

    editLink: {
      pattern: `${REPOSITORY_HOME}/edit/main/docs/src/:path`,
    },

    footer: {
      message: 'Released under the AGPL License.',
      copyright: 'Copyright © 2023-present Jérémy Levilain',
    },

    socialLinks: [{ icon: 'github', link: REPOSITORY_HOME }],

    nav: [
      { text: 'Getting Started', link: '/guide/getting-started/prerequisites' },
      { text: 'Guide', link: '/guide/' },
      { text: 'Recipes', link: '/guide/recipes' },
    ],

    sidebar: {
      '/guide/': [
        {
          text: 'Introduction',
          items: [
            { text: 'What is Shulker?', link: '/guide/' },
            { text: 'Architecture', link: '/guide/architecture' },
          ],
        },
        {
          text: 'Getting Started',
          items: [
            {
              text: 'Prerequisites',
              link: '/guide/getting-started/prerequisites',
            },
            {
              text: 'Installation',
              link: '/guide/getting-started/installation',
            },
            {
              text: 'Your First Cluster',
              link: '/guide/getting-started/your-first-cluster',
            },
          ],
        },
        {
          text: 'Recipes',
          items: [
            {
              text: 'Adding custom content',
              link: '/guide/recipes/adding-custom-content',
            },
            {
              text: 'Enabling proxy protocol',
              link: '/guide/recipes/enabling-proxy-protocol',
            },
            {
              text: 'Overriding pod properties',
              link: '/guide/recipes/overriding-pod-properties',
            },
          ],
        },
      ],
    },
  },
});
