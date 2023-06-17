# IDEA rabbit-sql plugin

[![HomePage][badge:homepage]][homepage]
[![License][badge:license]][license]
[![Version][badge:version]][versions]
[![Downloads][badge:downloads]][homepage]

Language: English | [简体中文](README.chs.md)

- Support the identification of xql(`.xql`) file type.
- Support dynamic sql expression script live templates(e.g: `xql:if`).
- Support quick look sql definition by name(e.g: `&my.users`).
- `Baki` interface Support inject SQL language(e.g: `.query("select ...")`).
- Support java and xql file navigate each other.
- Support copy sql by focus in sql name.(macOS:<kbd>Option</kbd> + <kbd>Enter</kbd> , Windows: <kbd>Alt</kbd> + <kbd>Enter</kbd>) > <kbd>Copy sql definition</kbd> .
- Support test Dynamic SQL by focus in sql name.(macOS:<kbd>Option</kbd> + <kbd>Enter</kbd> , Windows: <kbd>Alt</kbd> + <kbd>Enter</kbd>) > <kbd>Test dynamic sql</kbd> .
- Support sql name suggestions auto complete in java string literal where start with `"&"`.
- Create `xql-file-manager.yml` in `/src/main/resources` and register xql file to enable some features above.
- ToolBar menu: <kbd>File</kbd> > <kbd>New</kbd> > <kbd>XQL File</kbd> | <kbd>XQL File Manager</kbd> | <kbd>XQL File Manager CHS</kbd>

## Getting Started

1. **Rabbit-sql 7.1.12+** or **rabbit-sql-spring-boot-starter 2.2.14+**;
2. Create `xql-file-manager.yml` in source root: `.../src/main/resources/`;
3. Register your xql files on property: `files`;
4. Configure [XQLFileManager](https://github.com/chengyuxing/rabbit-sql#XQLFileManager);
5. Configure [BakiDao#setXqlFileManager](https://github.com/chengyuxing/rabbit-sql#bakidao);

### Springboot support:

Just 1, 2, 3 steps are required.

Get more information from [Rabbit-sql](https://github.com/chengyuxing/rabbit-sql) homepage
and [Springboot support document](https://github.com/chengyuxing/rabbit-sql-spring-boot-starter).



[badge:homepage]:https://img.shields.io/badge/plugin%20homepage-rabbit--sql-success
[badge:version]:https://img.shields.io/jetbrains/plugin/v/21403
[badge:downloads]:https://img.shields.io/jetbrains/plugin/d/21403
[badge:license]:https://img.shields.io/github/license/chengyuxing/rabbit-sql-plugin



[homepage]:https://plugins.jetbrains.com/plugin/21403-rabbit-sql
[versions]:https://plugins.jetbrains.com/plugin/21403-rabbit-sql/versions
[license]:https://github.com/chengyuxing/rabbit-sql-plugin/blob/main/LICENSE