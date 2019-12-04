# Quarkus Language Server Changelog

## 0.0.4-SNAPSHOT (November 20, 2019)

### Enhancements

 * Support for `@ConfigProperties`. See [#136](https://github.com/redhat-developer/quarkus-ls/issues/136)
 * CodeAction to fix value property by proposing similar value. See [#130](https://github.com/redhat-developer/quarkus-ls/issues/130)
 * CodeAction to add required properties. See [#111](https://github.com/redhat-developer/quarkus-ls/issues/111)
 * CodeAction to fix unknown property by proposing similar name. See [#80](https://github.com/redhat-developer/quarkus-ls/issues/80)
 * Provide a better support for Quarkus property value. See [#69](https://github.com/redhat-developer/quarkus-ls/issues/69)

### Bug Fixes

 * Duplicate completion options in Gradle projects. See [#137](https://github.com/redhat-developer/quarkus-ls/issues/137)

## 0.0.3-SNAPSHOT (October 23, 2019)

### Enhancements

 * Provide default method when extending QuarkusLanguageClientAPI. See [#120](https://github.com/redhat-developer/quarkus-ls/issues/120)
 * Support glob pattern to exclude unknown properties from validation. See [#79](https://github.com/redhat-developer/quarkus-ls/issues/79)

### Others

 * quarkus.jdt sometimes fails to build because of tests timeout. See [#126](https://github.com/redhat-developer/quarkus-ls/issues/126)
 * Deploy quarkus.jdt update site to download.jboss.tools. See [#124](https://github.com/redhat-developer/quarkus-ls/issues/124)

## 0.0.2-SNAPSHOT (October 17, 2019)

### Enhancements

 * Improve documentation for default profiles. See [#89](https://github.com/redhat-developer/quarkus-ls/issues/89)
 * Validate application.properties: type value. See [#33](https://github.com/redhat-developer/quarkus-ls/issues/33)
 * Support for `textDocument/formatting` and `textDocument/rangeFormatting`. See [#24](https://github.com/redhat-developer/quarkus-ls/issues/24)
 * Validate application.properties: required properties. See [#21](https://github.com/redhat-developer/quarkus-ls/issues/21)
 * Support for `textDocument/definition` for Java fields which have Config* annotation. See [#4](https://github.com/redhat-developer/quarkus-ls/issues/4)

### Bug Fixes

 * Fix duplicate completion options for ConfigProperty. See [#101](https://github.com/redhat-developer/quarkus-ls/issues/101)
 * Fix issue where boolean completion for optional boolean value was not working. See [#88](https://github.com/redhat-developer/quarkus-ls/issues/88)
 * Fix issue where PropertiesModel start offset is -1. See [#51](https://github.com/redhat-developer/quarkus-ls/issues/51)
 * Ignore/include properties in application.properties depending on test scope. See [#5](https://github.com/redhat-developer/quarkus-ls/issues/5)

### Others

 * Update lsp4j version to 0.8.1. See [#107](https://github.com/redhat-developer/quarkus-ls/pull/107)
 * Freeze API for enumeration (String -> EnumItem). See [#99](https://github.com/redhat-developer/quarkus-ls/issues/99)
 * Add quarkus-ls README. See [#46](https://github.com/redhat-developer/quarkus-ls/issues/46)
