import * as path from 'node:path';
import { DefaultTheme } from 'vitepress';

const versionName = path.basename(__dirname);
const link = (link: string) => `/${versionName}${link}`;

export default [
  {
    text: 'Introduction',
    items: [
      { text: 'What is Shulker?', link: link('/guide/') },
      { text: 'Architecture', link: link('/guide/architecture') },
    ],
  },
  {
    text: 'Getting Started',
    items: [
      {
        text: 'Prerequisites',
        link: link('/guide/getting-started/prerequisites'),
      },
      {
        text: 'Installation',
        link: link('/guide/getting-started/installation'),
      },
      {
        text: 'Your First Cluster',
        link: link('/guide/getting-started/your-first-cluster'),
      },
    ],
  },
  {
    text: 'Recipes',
    items: [
      {
        text: 'Adding custom content',
        link: link('/guide/recipes/adding-custom-content'),
      },
      {
        text: 'Enabling proxy protocol',
        link: link('/guide/recipes/enabling-proxy-protocol'),
      },
      {
        text: 'Overriding pod properties',
        link: link('/guide/recipes/overriding-pod-properties'),
      },
      {
        text: 'Defining network administrators',
        link: link('/guide/recipes/defining-network-administrators'),
      },
      {
        text: 'Using custom server JAR',
        link: link('/guide/recipes/using-custom-server-jar'),
      },
      {
        text: 'Understanding Lifecycle Strategy',
        link: link('/guide/recipes/understanding-lifecycle-strategies'),
      },
    ],
  },
  {
    text: 'Addons',
    items: [
      {
        text: 'What are addons?',
        link: link('/guide/addons/what-are-addons'),
      },
      {
        text: 'Matchmaking (alpha)',
        link: link('/guide/addons/matchmaking'),
      },
    ],
  },
  {
    text: 'API & SDK Reference',
    link: link('/sdk/'),
  },
] satisfies DefaultTheme.Sidebar;
