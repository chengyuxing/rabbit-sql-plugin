# IDEA [rabbit-sql plugin](https://plugins.jetbrains.com/plugin/21403-rabbit-sql)

For support all features, rabbit-sql version >= 7.0.27, rabbit-sql-spring-boot-starter version >= 2.1.17.

- Support the identification of xql(.xql) file type.
- Support dynamic sql expression script syntax highlight.
- Support dynamic sql expression script live templates(e.g: `xql:if`).
- Support quick look sql definition by name(e.g: `&my.users`).
- Baki interface Support inject SQL language(e.g: `.query("select ...")`).
- Support java and xql file navigate each other.
- Support copy sql by sql name.(macOS: `Option + Enter`, Windows: `Alt + Enter`).
- Support sql name suggestions auto complete in java string literal where start with `"&"`.
- Create `${projectBaseDir}/src/main/resources/xql-file-manager.properties` to enable features above.
- Toolbar menu: **File | New | [Xql File | Xql File Manager Config]**

## Getting Started

For working correct in your project, you need configure your
own [XQLFileManager](https://github.com/chengyuxing/rabbit-sql#XQLFileManager) in ioc or anywhere
for [Baki](https://github.com/chengyuxing/rabbit-sql#bakidao) currently, hope more features in the future.

Get more information from [rabbit-sql](https://github.com/chengyuxing/rabbit-sql) homepage
and Springboot support [document](https://github.com/chengyuxing/rabbit-sql-spring-boot-starter).