# IDEA rabbit-sql plugin

[![HomePage][badge:homepage]][homepage]
[![License][badge:license]][license]
[![Version][badge:version]][versions]
[![Downloads][badge:downloads]][homepage]

Language: English | [简体中文](README.chs.md)

IDEA 2023.1.* - 2026.3.* is required.

Current release: **2.4.64.231-263**. Bundled libraries: **rabbit-sql 10.3.21** and **rabbit-common 3.2.13**.

- Support the identification of xql(`.xql`) file type.
- Support dynamic sql expression script live templates(e.g: `xql:if`).
- Support quick look sql definition by name(e.g: `&my.users`).
- Support generate mapper interface by registered xql file.
- Support java and xql file references to navigate each other.
- Support copy sql by focus in sql name.(macOS:<kbd>Option</kbd> + <kbd>Enter</kbd> , Windows: <kbd>Alt</kbd> + <kbd>Enter</kbd>) > <kbd>Copy sql definition</kbd> .
- Support Execute Dynamic SQL by focus in sql name.(macOS:<kbd>Option</kbd> + <kbd>Enter</kbd> , Windows: <kbd>Alt</kbd> + <kbd>Enter</kbd>) > <kbd>Execute Dynamic sql</kbd> .
- Support sql name suggestions auto complete in java string literal where start with `"&"`.
- Create `xql-file-manager.yml` in `/src/main/resources` and register xql file to enable some features above.
- **ToolBar menu**: <kbd>File</kbd> > <kbd>New</kbd> > <kbd>XQL File</kbd> | <kbd>XQL File Manager</kbd>.

## 2.4.64 release notes

- Bundle rabbit-sql 10.3.21 and rabbit-common 3.2.13; recommend starter 5.3.22.
- Include core updates for entity primary-key lookup, date mapping and conversion, and SQL log highlighting.

## 2.4.63 release notes

- Bundle rabbit-sql 10.3.20 and rabbit-common 3.2.12; recommend starter 5.3.21.
- Reload compiled pipe classes from Maven and Gradle outputs, and refresh registered `.xql` and `.sql` files.
- Isolate dynamic SQL consoles and database execution sessions between files.
- Preserve the active configuration on reload and use snapshots for concurrent reads.
- Improve mapper output paths, notification scheduling and project shutdown handling.

## Installation

- Installing from IDEA plugin marketplace:
  - <kbd>Preferences(Settings)</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search and find <b>"rabbit sql"</b></kbd> > <kbd>Install Plugin</kbd>.
- Installing manually: 
  - Download from [plugin repository][versions] ;
  - <kbd>Preferences(Settings)</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd> > choose installation package. (no need to unzip)

## Getting Started

1. Add dependency **rabbit-sql 10.3.21** (recommended) to your project;
2. Create `xql-file-manager.yml` in source root: `.../src/main/resources/`;
3. Register your xql files on property: `files`;
4. Configure [XQLFileManager](https://github.com/chengyuxing/rabbit-sql#XQLFileManager);
5. Configure [BakiDao#setXqlFileManager](https://github.com/chengyuxing/rabbit-sql#bakidao);

### Springboot support

1. Add dependency **rabbit-sql-spring-boot-starter 5.3.22** (recommended) to your project;
2. Create `xql-file-manager.yml` in source root: `.../src/main/resources/`;
3. Register your xql files on property: `files`;

> Press <kbd>Ctrl</kbd> + <kbd>s</kbd> or <kbd>Tools</kbd> > <kbd>Reload XQL File Manager</kbd> to update sql resource cache when you modify a registered `.xql` or `.sql` file, or `xql-file-manager.yml` ;


Get more information from [Rabbit-sql](https://github.com/chengyuxing/rabbit-sql) homepage
and [Springboot support document](https://github.com/chengyuxing/rabbit-sql-spring-boot-starter).


## Developer build

Before building with unpublished core versions, run `mvn install -DskipTests -Dmaven.javadoc.skip=true -Dgpg.skip=true` first in the rabbit-common 3.2.13 source checkout, then in the rabbit-sql 10.3.21 checkout. This plugin uses `mavenLocal()` and bundles both libraries.

Use JDK 17 to run `./gradlew test buildPlugin` in this project. The plugin ZIP is written to `build/distributions/rabbit-sql-plugin-2.4.64.231-263.zip`.

[badge:homepage]:https://img.shields.io/badge/plugin%20homepage-rabbit--sql-success
[badge:version]:https://img.shields.io/jetbrains/plugin/v/21403
[badge:downloads]:https://img.shields.io/jetbrains/plugin/d/21403
[badge:license]:https://img.shields.io/github/license/chengyuxing/rabbit-sql-plugin

[homepage]:https://plugins.jetbrains.com/plugin/21403-rabbit-sql
[versions]:https://plugins.jetbrains.com/plugin/21403-rabbit-sql/versions
[license]:https://github.com/chengyuxing/rabbit-sql-plugin/blob/main/LICENSE