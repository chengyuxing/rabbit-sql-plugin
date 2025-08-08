# IDEA rabbit-sql plugin

[![HomePage][badge:homepage]][homepage]
[![License][badge:license]][license]
[![Version][badge:version]][versions]
[![Downloads][badge:downloads]][homepage]

语言: [English](README.md) | 简体中文

IDEA 版本兼容 2020.3.* - 2025.2.* 。

- 支持识别 xql（`.xql`） 文件类型；
- 支持动态sql表达式脚本 live templates（例如：`xql:if`）；
- 支持通过sql名字快速查看sql语句（例如：`&my.users`）；
- 支持通过注册的xql文件生成映射器接口；
- 支持java文件和xql文件sql引用导航；
- 鼠标点击sql名支持拷贝sql语句内容.（macOS:<kbd>Option</kbd> + <kbd>Enter</kbd> , Windows: <kbd>Alt</kbd> + <kbd>Enter</kbd>) > <kbd>Copy sql definition</kbd> ；
- 鼠标点击sql名支持测试动态sql.(macOS:<kbd>Option</kbd> + <kbd>Enter</kbd> , Windows: <kbd>Alt</kbd> + <kbd>Enter</kbd>) > <kbd>Execute Dynamic sql</kbd> )；
- java文件中字符串字面量以 `&` 开头支持sql名自动完成提示建议；
- 在 `/src/main/resources` 目录下创建文件 `xql-file-manager.yml` 并配置xql文件可以开启支持以上全部功能；
- **工具栏菜单**: <kbd>File</kbd> > <kbd>New</kbd> > <kbd>XQL File</kbd> | <kbd>XQL File Manager</kbd>。

## 安装

- 通过IDEA插件商店进行安装：
  - <kbd>Preferences(Settings)</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search and find <b>"rabbit sql"</b></kbd> > <kbd>Install Plugin</kbd>；
- 通过插件[资源库][versions]手动下载安装：
  - <kbd>Preferences(Settings)</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd> > 选择插件安装包（不需要解压）。

## 开始使用

1. 项目中引入 **rabbit-sql 9.0.19+(jdk17+) or 8.1.21+(jdk8)**；
2. 在源文件根目录: `.../src/main/resources/` 下创建 `xql-file-manager.yml`；
3. 为属性: `files` 配置你的xql文件；
4. 配置 [XQLFileManager](https://github.com/chengyuxing/rabbit-sql#XQLFileManager);
5. 配置 [BakiDao#setXqlFileManager](https://github.com/chengyuxing/rabbit-sql#bakidao);

### Springboot支持

1. 项目中引入 **rabbit-sql-spring-boot-starter 4.0.17+(jdk17+) or 3.2.11+(jdk8)**；
2. 在源文件根目录: `.../src/main/resources/` 下创建 `xql-file-manager.yml`；
3. 为属性: `files` 配置你的xql文件；

> 每当修改了 `xql` 文件或 `xql-file-manager.yml` 时，按 <kbd>Ctrl</kbd> + <kbd>s</kbd> 或者 <kbd>Tools</kbd> > <kbd>Reload XQL File Manager</kbd> 来触发更新缓存；
>
> :warning: 插件仅支持解析 xql 文件。

从 [rabbit-sql](https://github.com/chengyuxing/rabbit-sql) 主页
和 [Springboot 支持文档](https://github.com/chengyuxing/rabbit-sql-spring-boot-starter) 获取更多的帮助信息。


[badge:homepage]:https://img.shields.io/badge/plugin%20homepage-rabbit--sql-success
[badge:version]:https://img.shields.io/jetbrains/plugin/v/21403
[badge:downloads]:https://img.shields.io/jetbrains/plugin/d/21403
[badge:license]:https://img.shields.io/github/license/chengyuxing/rabbit-sql-plugin

[homepage]:https://plugins.jetbrains.com/plugin/21403-rabbit-sql
[versions]:https://plugins.jetbrains.com/plugin/21403-rabbit-sql/versions
[license]:https://github.com/chengyuxing/rabbit-sql-plugin/blob/main/LICENSE