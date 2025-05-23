# IDEA rabbit-sql plugin

[![HomePage][badge:homepage]][homepage]
[![License][badge:license]][license]
[![Version][badge:version]][versions]
[![Downloads][badge:downloads]][homepage]

Language: English | [简体中文](README.chs.md)

IDEA 2020.3.* - 2025.1.* is required.

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

## Installation

- Installing from IDEA plugin marketplace:
  - <kbd>Preferences(Settings)</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search and find <b>"rabbit sql"</b></kbd> > <kbd>Install Plugin</kbd>.
- Installing manually: 
  - Download from [plugin repository][versions] ;
  - <kbd>Preferences(Settings)</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd> > choose installation package. (no need to unzip)

## Getting Started

1. Add dependency **rabbit-sql 9.0.8+(jdk17+) or 8.1.11+(jdk8)** to your project;
2. Create `xql-file-manager.yml` in source root: `.../src/main/resources/`;
3. Register your xql files on property: `files`;
4. Configure [XQLFileManager](https://github.com/chengyuxing/rabbit-sql#XQLFileManager);
5. Configure [BakiDao#setXqlFileManager](https://github.com/chengyuxing/rabbit-sql#bakidao);

### Springboot support

1. Add dependency **rabbit-sql-spring-boot-starter 4.0.8+(jdk17+) or 3.2.1+(jdk8)** to your project;
2. Create `xql-file-manager.yml` in source root: `.../src/main/resources/`;
3. Register your xql files on property: `files`;

> Press <kbd>Ctrl</kbd> + <kbd>s</kbd> or <kbd>Tools</kbd> > <kbd>Reload XQL File Manager</kbd> to update sql resource cache when you modify the `xql` file or `xql-file-manager.yml` ;
>
> :warning: Plugin only parse xql file.

Get more information from [Rabbit-sql](https://github.com/chengyuxing/rabbit-sql) homepage
and [Springboot support document](https://github.com/chengyuxing/rabbit-sql-spring-boot-starter).


[badge:homepage]:https://img.shields.io/badge/plugin%20homepage-rabbit--sql-success
[badge:version]:https://img.shields.io/jetbrains/plugin/v/21403
[badge:downloads]:https://img.shields.io/jetbrains/plugin/d/21403
[badge:license]:https://img.shields.io/github/license/chengyuxing/rabbit-sql-plugin

[homepage]:https://plugins.jetbrains.com/plugin/21403-rabbit-sql
[versions]:https://plugins.jetbrains.com/plugin/21403-rabbit-sql/versions
[license]:https://github.com/chengyuxing/rabbit-sql-plugin/blob/main/LICENSE