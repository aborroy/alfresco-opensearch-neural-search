import { MainActionEntry } from '@hx-devkit/sdk';

/** Default global application header entries */
export const appToolbar: Array<MainActionEntry> = [
  {
    icon: 'search',
    text: 'Search',
    action: ['router.navigate', ['/content/search']]
  },
  {
    icon: 'help_outline',
    text: 'about',
    action: ['router.navigate', ['/about']]
  }
];
