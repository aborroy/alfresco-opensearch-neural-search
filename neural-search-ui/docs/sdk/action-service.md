# Action Service

`Service`

Provides access to runtime actions, allows invoking the actions with or without parameters.

## Accessing from Code

If your component requires invoking an application command, you can use `ActionService` for that:

```ts
import { Component, inject } from '@angular/core';
import { ActionService } from '@hx-devkit/sdk';

@Component({/*...*/})
class MyComponent {
  private commandService = inject(ActionService);
  
  onShowAboutPage() {
    this.commandService.execute('router.navigate', ['/about']);
  }
}
```

Please note that the example above expects a `router.navigate` command to be registered with the applicaiton.

## Creating Actions

Each application command has the following characteristics:

- implements the `Action` interface

```ts
export interface Action<TParams, TResult> {
  get name(): string;
  execute: (params?: TParams) => TResult;
}
```

- can access dependency injection

### Example: navigate action

In the following example, we create an action called `router.navigate`.
The action takes a number of parameters, and passes to the Angular Router `navigate` method.

```ts
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { Action } from '@hx-devkit/sdk';

export class RouterNavigateAction implements Action<any[], Promise<boolean>> {
  private router = inject(Router);

  get name(): string {
    return 'router.navigate';
  }

  execute(params?: any[]): Promise<boolean> {
    if (params) {
      return this.router.navigate(params);
    }
  }
}
```

### Example: preview node action

In this example, we are creating an action called `preview.node`.
The action takes a Node ID as a parameter (of `string` type),
and invokes a Viewer route to display the node. 

```ts
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { Action } from '@hx-devkit/sdk';

export class PreviewNodeAction implements Action<string> {
  private router = inject(Router);

  get name(): string {
    return 'preview.node';
  }

  execute(params?: string): void {
    if (params) {
      void this.router.navigate([{ outlets: { overlay: ['preview', params] } }]);
    }
  }
}
```

Note the separation of concerns.
The plugins using this command do not rely on exact Viewer component implementation, and route settings.
Updating the command internals does not break existing plugins using this command.

## Registering Actions

The commands are registered at the application level in the `src/app/app.actions.ts`, and might look like the following:

```ts
import { Type } from '@angular/core';
import { Action } from '@hx-devkit/sdk';
import { PreviewNodeAction, RouterNavigateAction } from './commands';

export const appActions: Type<Action>[] = [
  PreviewNodeAction, 
  RouterNavigateAction
];
```

Additional actions can use registered via the `provideActions` function from `@hx-devkit/sdk`:

```ts
import { NgModule } from '@angular/core';
import { provideActions } from '@hx-devkit/sdk';

@NgModule({
  providers: [
    provideActions([/*...*/])
  ]
})
class AppModule {}
```

## Declaring Actions

With the command above, you can use it with all building blocks that natively support `ActionService`.

For example toolbar or sidebar components:

`src/app/app.toolbar.ts`

```ts
import { AppToolbarEntry } from '@hx-devkit/sdk';

/** Default global application header entries */
export const appToolbar: Array<AppToolbarEntry> = [
  {
    text: 'Page 1',
    action: ['router.navigate', ['/page1']]
  },
  {
    text: 'Page 2',
    action: ['router.navigate', ['/page2']]
  },
];
```
