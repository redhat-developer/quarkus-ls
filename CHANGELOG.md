# Quarkus extension for MicroProfile Language Server Changelog

## 0.10.1 (July 20, 2021)

## Build

 * Do not commit, tag, or push in the release pipeline. See [#393](https://github.com/redhat-developer/quarkus-ls/issues/393).
 * Update lsp4mp to 0.3.0. See [#388](https://github.com/redhat-developer/quarkus-ls/issues/388).
 * Add existing build script to the repo as a Jenkinsfile. See [#392](https://github.com/redhat-developer/quarkus-ls/pull/392).

## 0.10.0 (April 02, 2021)

### Enhancements

 * Hover support for `@Scheduled/cron`. See [#376](https://github.com/redhat-developer/quarkus-ls/issues/376).
 * `quarkus.hibernate-orm.database.generation` values should be enumerated. See [#374](https://github.com/redhat-developer/quarkus-ls/pull/374).

### Bug Fixes

 * Schedule properties are shown as unknown. See [#375](https://github.com/redhat-developer/quarkus-ls/pull/375).
 * REST endpoint codelenses should be computed from `%dev.quarkus.http.port` by default. See [#371](https://github.com/redhat-developer/quarkus-ls/pull/371).
 * Wrong/Missing Log Levels in application.properties. See [#370](https://github.com/redhat-developer/quarkus-ls/pull/370).
 * `mp.messaging` properties now work for Emitters. See [#369](https://github.com/redhat-developer/quarkus-ls/issues/369).

## 0.9.0 (September 21, 2020)

Since `0.9.0`, quarkus-ls becomes an extension of [lsp4mp](https://github.com/eclipse/lsp4mp), the Language Server for Eclipse MicroProfile, see the upstream [lsp4mp CHANGELOG](https://github.com/eclipse/lsp4mp/blob/master/CHANGELOG.md#010-september-21-2020).

### Bug Fixes

 * Unit tests are not exported. See [#357](https://github.com/redhat-developer/quarkus-ls/issues/357).
 * Quarkus container-image incorrect default value highlighting. See [#351](https://github.com/redhat-developer/quarkus-ls/issues/351).

### Build

 * quarkus.jdt.ext should refer to quarkus-jdt sub folder. See [#353](https://github.com/redhat-developer/quarkus-ls/issues/353).
 * Create a new project com.redhat.quarkus.ls. See [#295](https://github.com/redhat-developer/quarkus-ls/issues/295).
 * Split Quarkus / MicroProfile LS support. See [#268](https://github.com/redhat-developer/quarkus-ls/issues/268).

### Other

 * Remove jax-rs snippets. See [#362](https://github.com/redhat-developer/quarkus-ls/pull/362).
 * Adapt README with LSP4MP. See [#352](https://github.com/redhat-developer/quarkus-ls/issues/352).
 * Rename `quarkus` prefix settings (used by language server) with `microprofile` . See [#325](https://github.com/redhat-developer/quarkus-ls/issues/325).

## 0.8.0 (July 9, 2020)

### Enhancements

* Let quarkus-ls extensions advertise their formatting support. See [#332](https://github.com/redhat-developer/quarkus-ls/pull/332).
* Send project name to the client along with labels. See [#330](https://github.com/redhat-developer/quarkus-ls/pull/330).

### Bug Fixes

 * Quarkus datasource snippet is invalid. See [#316](https://github.com/redhat-developer/quarkus-ls/issues/316).
 * Missing support for container-image properties. See [#315](https://github.com/redhat-developer/quarkus-ls/issues/315).
 * Filter properties snippet with properties instead of dependency. See [#312](https://github.com/redhat-developer/quarkus-ls/issues/312).

### Build 
 * Remove test fragment to prevent Eclipse build errors. See [#319](https://github.com/redhat-developer/quarkus-ls/pull/319).

## 0.7.0 (April 30, 2020)

### Enhancements

 * Update Quarkus datasource snippet. See [#310](https://github.com/redhat-developer/quarkus-ls/pull/310)
 * Code snippets for MicroProfile fault tolerance annotations. See [#307](https://github.com/redhat-developer/quarkus-ls/issues/307)
 * Support for MicroProfile Context Propagation properties. See [#242](https://github.com/redhat-developer/quarkus-ls/issues/242)

### Bug Fixes

 * `quarkus.banner.enabled` marked as error. See [#309](https://github.com/redhat-developer/quarkus-ls/pull/309)
 * Completion in non-Quarkus and non-MicroProfile project causes errors. See [#308](https://github.com/redhat-developer/quarkus-ls/pull/308)

## 0.0.6 (April 15, 2020)

### Enhancements

 * Hover support for `@ConfigProperty` name bounded to method parameters. See [#286](https://github.com/redhat-developer/quarkus-ls/pull/286)
 * Filter for Java (server) snippets. See [#265](https://github.com/redhat-developer/quarkus-ls/issues/265)
 * Support for `java.math.BigDecimal` values. See [#261](https://github.com/redhat-developer/quarkus-ls/issues/261)
 * Support for MicroProfile RestClient CodeAction. See [#255](https://github.com/redhat-developer/quarkus-ls/issues/255)
 * Manage client snippet on server side. See [#251](https://github.com/redhat-developer/quarkus-ls/pull/251)
 * Code complete snippets for Open API Annotations. See [#246](https://github.com/redhat-developer/quarkus-ls/issues/246)
 * Support for MicroProfile LRA properties. See [#243](https://github.com/redhat-developer/quarkus-ls/issues/243)
 * Support for MicroProfile Metrics properties. See [#241](https://github.com/redhat-developer/quarkus-ls/issues/241)
 * Support for MicroProfile OpenTracing properties. See [#240](https://github.com/redhat-developer/quarkus-ls/issues/240)
 * CodeAction to Generate Open API Annotations. See [#239](https://github.com/redhat-developer/quarkus-ls/issues/239)
 * Support for MicroProfile Health CodeAction. See [#236](https://github.com/redhat-developer/quarkus-ls/issues/236)
 * Provide codeLens participant. See [#232](https://github.com/redhat-developer/quarkus-ls/pull/232)
 * Provide hover participant. See [#231](https://github.com/redhat-developer/quarkus-ls/pull/231)
 * Support for MicroProfile RestClient/Health Diagnostics. See [#217](https://github.com/redhat-developer/quarkus-ls/issues/217)
 * Support for MicroProfile Open API properties. See [#216](https://github.com/redhat-developer/quarkus-ls/issues/216)

### Bug Fixes

 * Duplicate static properties after saving Java files. See [#301](https://github.com/redhat-developer/quarkus-ls/issues/301)
 * Bad performance when working with non Quarkus/MP projects. See [#290](https://github.com/redhat-developer/quarkus-ls/pull/290)
 * Allow excluded unknown property pattern * to match forward slashes. See [#284](https://github.com/redhat-developer/quarkus-ls/pull/284)
 * Hide OpenAPI source action if it is not applicable. See [#280](https://github.com/redhat-developer/quarkus-ls/issues/280)
 * Parse PropertyValue when spanning multiple lines. See [#254](https://github.com/redhat-developer/quarkus-ls/pull/254)
 * Classpath changed sends too many microprofile/propertiesChanged notifications. See [#235](https://github.com/redhat-developer/quarkus-ls/pull/235)
 * Fixed duplicated comments on range formatting. See [#233](https://github.com/redhat-developer/quarkus-ls/pull/233)
 * Empty completion after an error from microprofile/projectInfo. See [#228](https://github.com/redhat-developer/quarkus-ls/issues/228)

### Build

 * Deploy test projects for reuse. See [#288](https://github.com/redhat-developer/quarkus-ls/issues/288)
 * Missing exports for reusing unit tests. See [#263](https://github.com/redhat-developer/quarkus-ls/issues/263)
 * Remove Quarkus name in MicroProfile LS. See [#262](https://github.com/redhat-developer/quarkus-ls/issues/262)
 * Refactor unit test so that they can be reused. See [#258](https://github.com/redhat-developer/quarkus-ls/pull/258)
 * Consume LSP4J 0.9.0. See [#237](https://github.com/redhat-developer/quarkus-ls/issues/237)
 * `com.redhat.microprofile.ls` cannot be released to JBoss Nexus. See [#226](https://github.com/redhat-developer/quarkus-ls/issues/226)

### Others

 * Split Quarkus/MicroProfile support. See [#229](https://github.com/redhat-developer/quarkus-ls/issues/229)

## 0.0.5 (February 20, 2020)

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
