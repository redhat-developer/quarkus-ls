# MicroProfile Language Server Changelog

## 0.0.5 (February 20, 2019)

The Quarkus language server has been refactored to the MicroProfile language server.
Package names and file names have been adjusted to reflect the changes, 
i.e., `quarkus.ls.*` to `microprofile.ls.*` and `QuarkusHover.java` to `MicroProfileHover.java`.

### Enhancements

 * Compute deployment JAR only when dependencies changed. See [#202](https://github.com/redhat-developer/quarkus-ls/pull/202)
 * Support ConfigProperties for Quarkus 1.2. See [#200](https://github.com/redhat-developer/quarkus-ls/issues/200)
 * Add support for missing mp-rest properties. See [#189](https://github.com/redhat-developer/quarkus-ls/issues/189)
 * Easily generate all-quarkus-properties.json. See [#182](https://github.com/redhat-developer/quarkus-ls/issues/182)
 * Improve computation of Quarkus/MicroProfile properties. See [#154](https://github.com/redhat-developer/quarkus-ls/issues/154)
 * Use formattingSettings to determine spacing for completion. See [#153](https://github.com/redhat-developer/quarkus-ls/pull/153)
 * Remove integer and boolean required properties. See [#152](https://github.com/redhat-developer/quarkus-ls/issues/152)
 * Provide hover for ConfigProperty name. See [#151](https://github.com/redhat-developer/quarkus-ls/pull/151)
 * Code action for ignoring unknown property keys using common parent key. See [#150](https://github.com/redhat-developer/quarkus-ls/issues/150)
 * QuarkusPropertiesScope is confusing. See [#116](https://github.com/redhat-developer/quarkus-ls/issues/116)
 * Display Quarkus CodeLenses for REST endpoints. See [#115](https://github.com/redhat-developer/quarkus-ls/issues/115)
 * Add support for YAML configuration files. See [#112](https://github.com/redhat-developer/quarkus-ls/issues/112)
 * CodeAction to add property to ignore for unknown validation. See [#81](https://github.com/redhat-developer/quarkus-ls/issues/81)

### Bug Fixes

 * Value from Java enum are not correct. See [#198](https://github.com/redhat-developer/quarkus-ls/issues/198)
 * Fix JavaDoc issues. See [#185](https://github.com/redhat-developer/quarkus-ls/issues/185)
 * Download transitive dependencies of deployment JAR. See [#179](https://github.com/redhat-developer/quarkus-ls/pull/179)
 * Missing enum value completion/validation for optional enum. See [#175](https://github.com/redhat-developer/quarkus-ls/issues/175)
 * Too many microprofile/projectInfo calls. See [#174](https://github.com/redhat-developer/quarkus-ls/issues/174)
 * Hovering over equals sign is being treated as hovering over the property key. See [#172](https://github.com/redhat-developer/quarkus-ls/issues/172)
 * CodeLens provider not enabled with LSP4E. See [#156](https://github.com/redhat-developer/quarkus-ls/issues/156)

## 0.0.4 (November 20, 2019)

### Enhancements

 * Support for `@ConfigProperties`. See [#136](https://github.com/redhat-developer/quarkus-ls/issues/136)
 * CodeAction to fix value property by proposing similar value. See [#130](https://github.com/redhat-developer/quarkus-ls/issues/130)
 * CodeAction to add required properties. See [#111](https://github.com/redhat-developer/quarkus-ls/issues/111)
 * CodeAction to fix unknown property by proposing similar name. See [#80](https://github.com/redhat-developer/quarkus-ls/issues/80)
 * Provide a better support for Quarkus property value. See [#69](https://github.com/redhat-developer/quarkus-ls/issues/69)

### Bug Fixes

 * Duplicate completion options in Gradle projects. See [#137](https://github.com/redhat-developer/quarkus-ls/issues/137)

## 0.0.3 (October 23, 2019)

### Enhancements

 * Provide default method when extending QuarkusLanguageClientAPI. See [#120](https://github.com/redhat-developer/quarkus-ls/issues/120)
 * Support glob pattern to exclude unknown properties from validation. See [#79](https://github.com/redhat-developer/quarkus-ls/issues/79)

### Others

 * quarkus.jdt sometimes fails to build because of tests timeout. See [#126](https://github.com/redhat-developer/quarkus-ls/issues/126)
 * Deploy quarkus.jdt update site to download.jboss.tools. See [#124](https://github.com/redhat-developer/quarkus-ls/issues/124)

## 0.0.2 (October 17, 2019)

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
