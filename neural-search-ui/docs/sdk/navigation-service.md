# Navigation Service

`Service`

<div class="warning">
This section is experimental

The content and API in this chapter are subject to change.
</div>

Provides access to the navigation management and settings:

- Header menu entries
- Sidebar menu entries

```ts
class NavigationService {
  headerEntries$: Observable<AppToolbarEntry[]>;
  sidebarEntries$: Observable<AppSidebarEntry[]>;
}
```

Plugins can register additional entries by utilizing the provider functions:

```ts
import { provideHeaderEntries, provideSidebarEntries } from '@hx-devkit/sdk';

@NgModule({
  providers: [
    provideHeaderEntries([
      {
        text: 'Extra Page 1',
        action: ['router.navigate', ['/extra-page-1']]
      }
    ]),
    provideSidebarEntries([
      {
        text: 'Extra Page 1',
        icon: 'assignment',
        action: ['router.navigate', ['/extra-page-1']]
      }
    ])
  ]
})
class AppModule {}
```

You can register additional content either from the main application module, or from within the plugin modules.

When providing customizations with the plugin modules, import them into the root application one:

```ts
import { Plugin1Module } from '@my-org/plugin1';

@NgModule({
  imports: [Plugin1Module]
})
class AppModule {}


// where Plugin1Module is

@NgModule({
  providers: [
    provideHeaderEntries([
      /*...*/
    ]),
    provideSidebarEntries([
      /*...*/
    ])
  ]
})
export class Plugin1Module {}
```
