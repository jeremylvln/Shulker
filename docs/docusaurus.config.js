// @ts-check

const lightCodeTheme = require('prism-react-renderer/themes/github');
const darkCodeTheme = require('prism-react-renderer/themes/dracula');

/** @type {import('@docusaurus/types').Config} */
const config = {
  title: 'Shulker',
  tagline: 'Put Minecraft in a box',
  url: 'https://shulker.jeremylvln.fr',
  baseUrl: '/',
  onBrokenLinks: 'throw',
  onBrokenMarkdownLinks: 'warn',
  favicon: 'img/favicon.ico',
  organizationName: 'IamBlueSlime',
  projectName: 'Shulker',
  i18n: {
    defaultLocale: 'en',
    locales: ['en', 'fr'],
  },
  presets: [
    [
      'classic',
      /** @type {import('@docusaurus/preset-classic').Options} */
      ({
        docs: {
          routeBasePath: '/',
          sidebarPath: require.resolve('./sidebars.js'),
          editUrl: ({locale, docPath}) => locale === 'en'
            ? `https://github.com/IamBlueSlime/Shulker/edit/main/docs/docs/${docPath}`
            : `https://crowdin.com/project/shulker/${locale}`,
        },
        blog: false,
        theme: {
          customCss: require.resolve('./src/css/custom.css'),
        },
      }),
    ],
  ],
  themeConfig:
    /** @type {import('@docusaurus/preset-classic').ThemeConfig} */
    ({
      navbar: {
        title: 'Shulker',
        logo: {
          alt: 'Shulker Logo',
          src: 'img/logo.png',
        },
        items: [
          {
            type: 'doc',
            docId: 'getting-started/installation',
            position: 'left',
            label: 'Getting Started',
          },
          {
            type: 'doc',
            docId: 'recipes/minecraft-cluster',
            position: 'left',
            label: 'Recipes',
          },
          {
            type: 'localeDropdown',
            position: 'right',
          },
          {
            href: 'https://github.com/IamBlueSlime/Shulker',
            label: 'GitHub',
            position: 'right',
          },
        ],
      },
      footer: {
        style: 'dark',
        links: [
          {
            title: 'Docs',
            items: [
              {
                label: 'Understanding the Basics',
                to: '/understanding-the-basics/installation',
              },
              {
                label: 'Getting Started',
                to: '/getting-started/installation',
              },
              {
                label: 'Recipes',
                to: '/recipes/minecraft-cluster',
              },
            ],
          },
          {
            title: 'Community',
            items: [
              {
                label: 'GitHub Discussions',
                href: 'https://github.com/IamBlueSlime/Shulker/discussions',
              },
            ],
          },
          {
            title: 'More',
            items: [
              {
                label: 'GitHub',
                href: 'https://github.com/IamBlueSlime/Shulker',
              },
            ],
          },
        ],
        copyright: `Copyright © ${new Date().getFullYear()} Jérémy Levilain. Built with Docusaurus.`,
      },
      prism: {
        theme: lightCodeTheme,
        darkTheme: darkCodeTheme,
      },
    }),
};

module.exports = config;
