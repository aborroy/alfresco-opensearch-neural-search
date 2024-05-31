# App Config

To customize global application configuration, edit the `src/app/app.config.ts` file:

```ts
import { AppConfigProps } from '@hx-devkit/sdk';

export const appConfig: AppConfigProps = {
  
  // if defined, shows application title in the toolbar
  name: 'My Application',

  // if defined, provides a tooltip for the application title in the toolbar
  description: 'My application description',

  // copyright info, used by the Login dialog
  copyright: '© 2024 My Copyright Text'
  
};
```

## Accessing Configuration

To access the configuration at runtime, you can inject either whole set of fields via the `APP_CONFIG` token,
or inject individual properties via corresponding tokens.

```ts
import { inject } from '@angular/core';
import { APP_CONFIG, AppConfigProps } from '@hx-devkit/sdk';

export class MyComponent {
  private config = inject<AppConfigProps>(APP_CONFIG);

  constructor() {
    console.log(this.config);
  }
}
```

Accessing individual properties:

```ts
import { inject } from '@angular/core';
import { APP_DESCRIPTION, APP_NAME } from '@hx-devkit/sdk';

export class MyComponent {
  appName?: string = inject(APP_NAME, { optional: true });
  appDescription?: string = inject(APP_DESCRIPTION, { optional: true });
  copyrightText?: string = inject(APP_COPYRIGHT, { optional: true });
}
```
