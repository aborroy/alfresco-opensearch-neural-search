# Quick start

```sh
npm install
npm start
```

Navigate to `http://localhost:4200/`.
The app will automatically reload if you change any of the source files.

Running with a custom backend URL:

```sh
BASE_URL="https://your.alfresco.backend.com" npm start
```

## .env File

Alternatively, you can create a `.env` file in your project root folder:

```yml
BASE_URL="https://your.alfresco.backend.com"
```

## Proxy settings

The template provides certain proxy settings to allow running web application locally without CORS setup.
You can find details in the `proxy.conf.js` file.

### OAuth2 configuration

When you need to connect via OIDC/OAuth2, change the authorization type to OAuth `"authType": "OAUTH"` in `app.config.json` file,
and optionally set the `IDENTITY_SERVICE_URL` environment variable.

> The `IDENTITY_SERVICE_URL` value defaults to the `BASE_URL`, and falls back to the `http://localhost:3000`.

Examples:

```sh
# Using same URL for everything
BASE_URL="https://my.alfresco.backend.com" npm start

# Using custom URL for OAUTH
BASE_URL="https://my.alfresco.backend.com" IDENTITY_SERVICE_URL="https://my.auth.com/auth" npm start
```

## Build

Run `npm run build` to build the project.  
The build artifacts will be stored in the `dist/` directory.

Use the `npm run build:prod` for a production build.

## Linting

Run `npm run lint` to lint the project.
