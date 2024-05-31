# Custom Page Layouts

Page Layouts are regular Angular components.
There are no restrictions to the content of the Layout components.

Each layout must provide a RouterOutlet with the ID `main-content-outlet`.
That is where the content of the page is projected.

```html
<router-outlet id="main-content-outlet"></router-outlet>
```

To assemble a custom layout you can extend the `BaseLayoutComponent` or start from an empty one leveraging the services and components the `@hx-devkit/sdk` provides.

## Base Layout

The abstract `BaseAppLayoutComponent` provide a quick way to create a custom layout. Extending this component your layout will inherit `navigation$` and `mainActions$` observables.
Any configuration of the layout is expected to be passed as input in the routes configuration `data.layout`.
The abstract method `applySettings(settings)` is expected to be implemented to manage the configuration.

Example

```ts
// New Layout Component
export type CustomLayoutSettings = {
  title?: string;
};

@Component({
  selector: 'app-custom-layout',
  standalone: true,
  imports: [CommonModule, RouterModule, AppToolbarComponent, AppSidebarComponent],
  templateUrl: 'custom.component.html',
  styleUrls: ['custom.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class CustomLayoutComponent extends BaseAppLayoutComponent {
  @Input()
  title = 'No Title';

  applySettings(settings: CustomLayoutSettings) {
    if (settings.title !== undefined) {
      this.title = settings.title;
    }
  }
}
```

```ts
// app.routes.ts
    path: '',
    component: CustomLayoutComponent,
    // optional: configuring the StandardLayoutComponent settings
    data: {
      layout: {
        title: 'Hello Custom Layout',
      }
    },
```

## Components

- `<app-toolbar />` - application toolbar
- `<app-sidebar />` - application sidebar
- `<app-title />` - application title

## Services

- Navigation Service
- Preview Service

## Creating Basic Layout

The following structure resembles the `Standard Layout` component coming with the `@hx-devkit/sdk` library:

```html
<app-toolbar [entries]="[/* array of AppToolbarEntry */]" (toggleSidebar)="sidebar?.toggle()"> </app-toolbar>

<app-sidebar #sidebar [entries]="[/* array of AppToolbarEntry */]">
  <router-outlet id="main-content-outlet"></router-outlet>
</app-sidebar>
```

If you want to render the runtime set of toolbar buttons, use the `NavigationService` to access all the entries:

```ts
export class MyCustomLayout {
  private navigationService = inject(NavigationService);

  headerEntries$ = this.navigationService.headerEntries$;
  sidebarEntries$ = this.navigationService.sidebarEntries$;
}
```

Next, forward the sidebar and header entries to the corresponding components:

```html
<app-toolbar [entries]="headerEntries$ | async" (toggleSidebar)="sidebar?.toggle()"> </app-toolbar>

<app-sidebar #sidebar [entries]="sidebarEntries$ | async">
  <router-outlet id="main-content-outlet"></router-outlet>
</app-sidebar>
```

You can modify the entry collections as part of your layout implementation, or plugin requirements.

### Integrating at the Application Level

```ts
export const appRoutes: Routes = [
  // Using custom layout for all child routes/components
  {
    path: 'pages',
    component: MyCustomLayout,
    children: [
      {
        path: 'page1',
        component: Page1Component
      }
    ]
  }
];
```

### Integrating at the Plugin level

If your plugin provides a nested structure of routes and components,
you can modify the `src/lib/lib.routes.ts` file to set the global layout for all your plugin routes:

```ts
import { Route } from '@angular/router';

export const myPluginRoutes: Route[] = [
  {
    path: '/my-plugin',
    component: MyCustomLayout,
    children: [
      {
        path: '/page1',
        component: MyCustomPlugin
      }
    ]
  }
];
```
