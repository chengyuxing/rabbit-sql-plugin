# IDEA [rabbit-sql plugin](https://plugins.jetbrains.com/plugin/21403-rabbit-sql)

- Support the identification of xql(.xql) file type.
- Support dynamic sql expression script syntax highlight.
- Support dynamic sql expression script live templates(e.g: `xql:if`).
- Support quick look sql definition by name(e.g: `&my.users`).
- Baki interface Support inject SQL language(e.g: `.query("select ...")`).
- Support java and xql file navigate each other.
- Support copy sql by sql name.(macOS: `Option + Enter`, Windows: `Alt + Enter`).
- Support sql name suggestions auto complete in java string literal where start with `"&"`.
- Create `xql-file-manager.properties` in `/src/main/resources` and register xql file to enable some features above.
- Toolbar menu: **File | New | [Xql File | Xql File Manager Config]**

## Getting Started

### For working correctly in your project, some steps are required:

1. **Rabbit-sql 7.1.0+** or **rabbit-sql-spring-boot-starter 2.2.0+**;
2. Create `xql-file-manager.properties` in source root: `.../src/main/resources/`;
3. Register your xql files on property: `filenames` or `files`;
4. Configure your own [XQLFileManager](https://github.com/chengyuxing/rabbit-sql#XQLFileManager);
5. Configure [BakiDao#setXqlFileManager](https://github.com/chengyuxing/rabbit-sql#bakidao);

### Springboot support:

Just **2nd** step is required.

Get more information from [Rabbit-sql](https://github.com/chengyuxing/rabbit-sql) homepage
and [Springboot support document](https://github.com/chengyuxing/rabbit-sql-spring-boot-starter).