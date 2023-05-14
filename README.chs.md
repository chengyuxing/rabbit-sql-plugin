# IDEA [rabbit-sql plugin](https://plugins.jetbrains.com/plugin/21403-rabbit-sql)

语言: [English](README.md) | 简体中文

- 支持识别 xql（.xql） 文件类型；
- 支持动态sql表达式脚本语法高亮；
- 支持动态sql表达式脚本 live templates（例如：`xql:if`）；
- 支持通过sql名字快速查看sql语句（例如：`&my.users`）；
- `Baki` 接口支持注入sql语言（例如：`.query（"select ..."）`）；
- 支持java文件和xql文件sql引用导航；
- 支持拷贝sql语句内容.（macOS: `Option + Enter`, Windows: `Alt + Enter`）；
- java文件中字符串字面量以 `&` 开头支持sql名自动完成提示建议；
- 在 `/src/main/resources` 目录下创建文件 `xql-file-manager.yml` 并配置xql文件可以开启支持以上全部功能；
- 工具栏菜单: **File | New | [Xql File 或 Xql File Manager Config]**。

## 开始使用

### 为了在你项目中正确的工作，下面一些步骤是必须的:

1. **rabbit-sql 7.1.12+** 或 **rabbit-sql-spring-boot-starter 2.2.14+**；
2. 在源文件根目录: `.../src/main/resources/` 下创建 `xql-file-manager.yml`；
3. 为属性: `filenames` or `files` 配置你的xql文件；
4. 配置 [XQLFileManager](https://github.com/chengyuxing/rabbit-sql#XQLFileManager);
5. 配置 [BakiDao#setXqlFileManager](https://github.com/chengyuxing/rabbit-sql#bakidao);

### Springboot 支持:

只有第1，2，3步是必须的.

从 [rabbit-sql](https://github.com/chengyuxing/rabbit-sql) 主页
和 [Springboot 支持文档](https://github.com/chengyuxing/rabbit-sql-spring-boot-starter) 获取更多的帮助信息。