import { defineConfig } from 'vitepress';
import * as versions from './versions.mts';

const websiteUrl = 'https://shulker.jeremylvln.fr';
const repositoryUrl = 'https://github.com/jeremylvln/Shulker';

const titleTemplate = ':title — The modern way of putting Minecraft in boxes';
const description =
  'A Kubernetes operator for managing complex and dynamic Minecraft infrastructures, including game servers and proxies.';

const renderTitle = (title: string) => titleTemplate.replace(':title', title);

export default defineConfig({
  title: 'Shulker',
  titleTemplate,
  description,

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

    // Open Graph
    ['meta', { name: 'og:type', content: 'website' }],
    ['meta', { name: 'og:url', content: websiteUrl }],
    [
      'meta',
      {
        name: 'og:title',
        content: renderTitle('Shulker'),
      },
    ],
    ['meta', { name: 'og:description', content: description }],
    ['meta', { name: 'og:image', content: `${websiteUrl}/banner.png` }],

    // Twitter
    ['meta', { name: 'twitter:card', content: 'summary_large_image' }],
    ['meta', { name: 'twitter:url', content: websiteUrl }],
    ['meta', { name: 'twitter:title', content: renderTitle('Shulker') }],
    ['meta', { name: 'twitter:description', content: description }],
    ['meta', { name: 'twitter:image', content: `${websiteUrl}/banner.png` }],
  ],

  markdown: {
    lineNumbers: true,
  },

  themeConfig: {
    logo: '/logo.png',

    editLink: {
      pattern: `${repositoryUrl}/edit/main/docs/src/:path`,
    },

    footer: {
      message: 'Released under the AGPL License.',
      copyright: 'Copyright © 2023-present Jérémy Levilain',
    },

    socialLinks: [{ icon: 'github', link: repositoryUrl }],

    nav: [
      {
        text: 'Getting Started',
        link: versions.getLatestLink('/guide/getting-started/prerequisites'),
      },
      { text: 'Guide', link: versions.getLatestLink('/guide/') },
      {
        text: 'Recipes',
        link: versions.getLatestLink('/guide/recipes/adding-custom-content'),
      },
      {
        text: 'Addons',
        link: versions.getLatestLink('/guide/addons/what-are-addons'),
      },
    ],

    search: {
      provider: 'local',
    },

    sidebar: versions.sidebars,
  },
});
