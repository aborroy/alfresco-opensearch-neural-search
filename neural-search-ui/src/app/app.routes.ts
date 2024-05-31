import { Routes } from '@angular/router';
import { Page1Component } from './pages/page1/page1.component';
import { Page2Component } from './pages/page2/page2.component';
import { StandardLayoutComponent, BlankLayoutComponent } from '@hx-devkit/sdk';
import { AboutComponent } from './pages/about/about.component';

/** Global application routes */
export const appRoutes: Routes = [
  // Using `Standard` layout for all child routes/components
  {
    path: '',
    component: StandardLayoutComponent,
    // optional: configuring the StandardLayoutComponent settings
    data: {
      layout: {
        showToolbar: true,
        showSidebar: true
      }
    },
    children: [
      {
        path: '',
        loadChildren: () => import('@/plugins/neural-search').then((m) => m.routes)
      }
    ]
  },
  // Redirect every undefined route to the root
  {
    path: '**',
    redirectTo: ''
  }
];
