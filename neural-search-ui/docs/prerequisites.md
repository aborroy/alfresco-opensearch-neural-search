# Prerequisites

## Node.js and NPM

Install the LTS (Long-Term Support) version of the Node.js: <https://nodejs.org/en/download/>

### MacOS

You can use the official macOS installer from the <https://nodejs.org/en/download/> page, it includes the NPM as well.

### Ubuntu

Use this article to get details on how to install Node.js and NPM on Ubuntu:
<https://linuxize.com/post/how-to-install-node-js-on-ubuntu-20-04/>

It is important to install the developer tools:

```sh
sudo apt install build-essential
```

### CentOS

Use this article to get details on how to install Node.js and NPM on CentOS:
<https://linuxize.com/post/how-to-install-node-js-on-centos-7/>

It is important to install the developer tools:

```sh
sudo yum install gcc-c++ make
```

## Nx Build System

The project is based on the [Nx](https://nx.dev/) build system. It is recommended that you install the tool globally:

```shell
npm add --global nx@latest
```

> See [Getting Started](https://nx.dev/getting-started/installation#installing-nx-globally) section in the official docs for more details

### Nx Console for IDEs

Nx Console is the UI for all Nx workspaces.

- [Nx for VS Code](https://marketplace.visualstudio.com/items?itemName=nrwl.angular-console)
- [Nx for IntelliJ](https://plugins.jetbrains.com/plugin/21060-nx-console)
