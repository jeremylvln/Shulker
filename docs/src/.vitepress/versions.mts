import { DefaultTheme } from 'vitepress/types/default-theme';

import nextSidebar from '../next/sidebar.mts';
import latestSidebar from '../latest/sidebar.mts';

type Version = {
  slug: string;
  name: string;
  sidebar: DefaultTheme.Sidebar;
};

const createVersion = (
  slug: string,
  name: string,
  sidebar: DefaultTheme.Sidebar,
): Version => ({
  slug,
  name,
  sidebar,
});

export const next = createVersion('next', 'Next', nextSidebar);
export const latest = createVersion('latest', 'v0 (latest)', latestSidebar);
export const all: Version[] = [next, latest];

export const sidebars: DefaultTheme.Sidebar = all.reduce(
  (acc, { slug, sidebar }) => {
    acc[`/${slug}`] = sidebar;
    return acc;
  },
  {},
);

export const getLatestLink = (link: string) => `/${latest.slug}${link}`;
