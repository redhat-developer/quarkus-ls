# Quarkus extension for MicroProfile Language Server Changelog

## [0.21.1](https://github.com/redhat-developer/quarkus-ls/milestone/25?closed=1) (April 10, 2025)

### Bug Fixes
- Use progress monitor when finding types in project, in order to resolve nested types. See [#1037](https://github.com/redhat-developer/quarkus-ls/pull/1037).
- Update snakeyaml to 2.4. See [#1044](https://github.com/redhat-developer/quarkus-ls/pull/1044).

### Build
- Migrate release job from Jenkins to GitHub Actions. See [#1039](https://github.com/redhat-developer/quarkus-ls/pull/1039).

## [0.21.0](https://github.com/redhat-developer/quarkus-ls/milestone/24?closed=1) (December 3, 2024)

### Enhancements

- Support for global namespace. See [#1025](https://github.com/redhat-developer/quarkus-ls/pull/1025).
- Data model template matcher support. See [#1021](https://github.com/redhat-developer/quarkus-ls/pull/1021).
- Add completion support for `quarkus.langchain4j.ollama.embedding-model.model-id`. See [#1014](https://github.com/redhat-developer/quarkus-ls/issues/1014).
- Support for multiple Qute template root paths. See [#1005](https://github.com/redhat-developer/quarkus-ls/pull/1005).
- Roq DataMapping support. See [#1017](https://github.com/redhat-developer/quarkus-ls/pull/1017).

### Bug Fixes

- Escape sequence \{ in Qute is not supported. See [#897](https://github.com/redhat-developer/quarkus-ls/issues/897).
- False-positive error with section name which contains underscore. See [#1028](https://github.com/redhat-developer/quarkus-ls/pull/1028).
- False-positive error with section name which contains number. See [#1022](https://github.com/redhat-developer/quarkus-ls/pull/1022).
- False-positive error on let-defined variable. See [#993](https://github.com/redhat-developer/quarkus-ls/issues/993).
- Fix the qute.jdt tests. See [#1019](https://github.com/redhat-developer/quarkus-ls/pull/1019).
- Support TemplateExtension matchNames. See [#1013](https://github.com/redhat-developer/quarkus-ls/issues/1013).
- Qute validation fails on `{config:property}` if property contains is a string. See [#933](https://github.com/redhat-developer/quarkus-ls/issues/933).
- Proper fix for comments in TemplateExtension quickfix test. See [#1020](https://github.com/redhat-developer/quarkus-ls/pull/1020).

## [0.20.0](https://github.com/redhat-developer/quarkus-ls/milestone/23?closed=1) (October 22, 2024)

### Enhancements

- Completion on available Ollama models support for `quarkus.langchain4j.ollama.chat-model.model-id` property value. See [#985](https://github.com/redhat-developer/quarkus-ls/pull/985).
- `quarkus.rest.path` support. See [#1003](https://github.com/redhat-developer/quarkus-ls/pull/1003).

### Bug Fixes

- Fix false-positive error when string parameter contains a comma. See [#1011](https://github.com/redhat-developer/quarkus-ls/issues/1011).
- Support for Vert.x Integration. See [#1009](https://github.com/redhat-developer/quarkus-ls/issues/1009).
- TemplateExtension match any support. See [#1008](https://github.com/redhat-developer/quarkus-ls/pull/1008).
- Fix false positive error with param name and user tag. See [#1007](https://github.com/redhat-developer/quarkus-ls/pull/1007).
- Fix add languageId filter for options similar to microprofile. See [#999](https://github.com/redhat-developer/quarkus-ls/pull/999).
- Fix Message: Request qute/java/documentLink failed with message: Cannot invoke "org.eclipse.jdt.core.dom.ITypeBinding.getQualifiedName()". See [#996](https://github.com/redhat-developer/quarkus-ls/pull/996).
- Fix NPE with {# and Qute inlay hint. See [#991](https://github.com/redhat-developer/quarkus-ls/issues/991).


## [0.19.1](https://github.com/redhat-developer/quarkus-ls/milestone/22?closed=1) (August 29, 2024)

### Bug Fixes

- Qute - incorrect behavior for nested template records. See [#987](https://github.com/redhat-developer/quarkus-ls/issues/987).
- Version 18 release duplicate key issue. See [#975](https://github.com/redhat-developer/quarkus-ls/issues/988).

## [0.19.0](https://github.com/redhat-developer/quarkus-ls/milestone/21?closed=1) (August 26, 2024)

### Enhancements

- Qute: add arguments metadata for user-defined tags. See [#928](https://github.com/redhat-developer/quarkus-ls/issues/928).
- Validate if a BuildItem is `final` or `abstract`. See [#926](https://github.com/redhat-developer/quarkus-ls/issues/926).
- Support @Route from vertx to display codelens URL. See [#914](https://github.com/redhat-developer/quarkus-ls/issues/914).
- Support customized template path for @CheckedTemplate. See [#893](https://github.com/redhat-developer/quarkus-ls/issues/893).

### Bug Fixes

- Qute: Cannot locate hyphenated template name. See [#975](https://github.com/redhat-developer/quarkus-ls/issues/975).
- fix: qute(NoMatchingTemplate) if using @CheckedTemplate with basePath. See [#972](https://github.com/redhat-developer/quarkus-ls/pull/972).
- fix: separate user tag parameters inlay hint. See [#968](https://github.com/redhat-developer/quarkus-ls/pull/968).
- Check if record implement TemplateInstance to provide the support. See [#974](https://github.com/redhat-developer/quarkus-ls/pull/974).
- In qute, allow spaces between method parameters and only validate the return type of the method invocation when iterating over a method invocation. See [#966](https://github.com/redhat-developer/quarkus-ls/issues/966).
- fix: allow spaces inside section branch. See [#965](https://github.com/redhat-developer/quarkus-ls/pull/965).
- fix: allow inject: with constructor class which have arguments. See [#964](https://github.com/redhat-developer/quarkus-ls/pull/964).
- Support for inner Enum in Qute files. See [#963](https://github.com/redhat-developer/quarkus-ls/issues/963).
- Support for void in property part of Qute template. See [#961](https://github.com/redhat-developer/quarkus-ls/issues/961).
- Template files generated by clicking on a code lens were missing the closing `</html>`. See [#955](https://github.com/redhat-developer/quarkus-ls/issues/955).
- quarkus.hibernate-orm.database.generation invalid validate value. See [#945](https://github.com/redhat-developer/quarkus-ls/issues/945).
- Qute fragments doesn't work with non inner Templates Java file. See [#937](https://github.com/redhat-developer/quarkus-ls/issues/937).
- inject:csrf is not retrieved in Qute template. See [#901](https://github.com/redhat-developer/quarkus-ls/issues/901).

### Other

- Update eclipse.jdt.ls to 1.37.0-SNAPSHOT.. See [#959](https://github.com/redhat-developer/quarkus-ls/pull/959).

## 0.18.0 (February 5, 2024)

### Enhancements

 * Delegate kotlin file *.kt to java document service. See [#948](https://github.com/redhat-developer/quarkus-ls/pull/948).

### Bug Fixes

 * Fixed Javadoc for Qute completion. See [#949](https://github.com/redhat-developer/quarkus-ls/pull/949).

### Build

 * Increase XmX to 2G for quarkus.jdt.ext tests. See [#946](https://github.com/redhat-developer/quarkus-ls/pull/946).

## 0.17.0 (October 5, 2023)

### Enhancements

 * Qute support for multi module project. See [#930](https://github.com/redhat-developer/quarkus-ls/issues/930).

### Bug Fixes

 * Fix `java.lang.StackOverflowError` while editing Quarkus extension. See [#924](https://github.com/redhat-developer/quarkus-ls/issues/924).

### Build

 * Move projectAdded / projectRemoved to language server. See [#941](https://github.com/redhat-developer/quarkus-ls/pull/941).
 * Update eclipse.jdt.ls to 1.29.0-SNAPSHOT. See [#942](https://github.com/redhat-developer/quarkus-ls/pull/942).
 * Update to Tycho 3.0.5 and Maven 3.9.4. See [#936](https://github.com/redhat-developer/quarkus-ls/pull/936).

## 0.16.0 (August 8, 2023)

### Enhancements

 * Telemetry for `qute-ls`. See [#765](https://github.com/redhat-developer/quarkus-ls/issues/765) and [#917](https://github.com/redhat-developer/quarkus-ls/pull/917).
 * Support `completionList/itemDefaults` for Qute parameter declaration. See [#900](https://github.com/redhat-developer/quarkus-ls/issues/900).

### Bug Fixes

 * Support `?` in parameter `let` section. See [#904](https://github.com/redhat-developer/quarkus-ls/issues/904) and [#906](https://github.com/redhat-developer/quarkus-ls/issues/906).
 * Support Qute optional end tags for sections. See [#879](https://github.com/redhat-developer/quarkus-ls/issues/879).
 * Don't report error when `javaType` is null. See [#908](https://github.com/redhat-developer/quarkus-ls/issues/908).
 * Fix NPE with Qute resolve completion when data is not filled. See [#903](https://github.com/redhat-developer/quarkus-ls/pull/903).
 * Detected path in plugin are not following renarde paths spec. See [#892](https://github.com/redhat-developer/quarkus-ls/issues/892).
 * `{name. ?: "Qute"} ` doesn't report a validation error. See [#884](https://github.com/redhat-developer/quarkus-ls/issues/884).

### Build

 * Use `DetectVMInstallationsJob.disabled=true` property from JDT Debug. See [#898](https://github.com/redhat-developer/quarkus-ls/pull/898).
 * Update eclipse.jdt.ls to `1.27.0-SNAPSHOT`. See [#920](https://github.com/redhat-developer/quarkus-ls/pull/920).
 * Update lsp4mp to `0.8.0`. See[#895](https://github.com/redhat-developer/quarkus-ls/pull/895).

## 0.15.0 (June 15, 2023)

### Enhancements

 * Initialize support for Qute Type-safe Message Bundles. See [#800](https://github.com/redhat-developer/quarkus-ls/issues/800).

### Bug Fixes

 * Fix URI Renarde description. See [#877](https://github.com/redhat-developer/quarkus-ls/pull/877).
 * Missing required "title" property when starting Qute LS leads to NPE. See [#865](https://github.com/redhat-developer/quarkus-ls/issues/865).
 * `ClassCastException` seen in `textDocument/codelens`. See [#859](https://github.com/redhat-developer/quarkus-ls/issues/859).
 * `textDocument/inlayHint` fails when classpath is incorrect. See [#858](https://github.com/redhat-developer/quarkus-ls/issues/858).
 * Code action to insert missing parameters overwrites optional parameter. See [#856](https://github.com/redhat-developer/quarkus-ls/issues/856).
 * Quick fix to insert all missing attributes for user tag in Qute file adds a `nested-content` attribute. See [#841](https://github.com/redhat-developer/quarkus-ls/issues/841).
 * NPE during Qute validation when project is not available. See [#886](https://github.com/redhat-developer/quarkus-ls/issues/886).
 * Ignore `UndefinedObject` + `UndefinedNamespace` problem code action should appear only if LSP client can support update configuration. See [#862](https://github.com/redhat-developer/quarkus-ls/pull/862).
 * Don't implement by default custom language client API. See [#870](https://github.com/redhat-developer/quarkus-ls/issues/870).

### Build

 * Update eclipse.jdt.ls to 1.24.0-SNAPSHOT. See [#874](https://github.com/redhat-developer/quarkus-ls/pull/874).
 * Use lsp4mp `0.8.0-SNAPSHOT`. See [#863](https://github.com/redhat-developer/quarkus-ls/pull/863).

## 0.14.1 (April 4, 2023)

### Bug Fixes

 * `uri:` and `uriabs:` (for Qute templates in Renarde projects) no longer work as expected. See [#850](https://github.com/redhat-developer/quarkus-ls/issues/850).

## 0.14.0 (April 3, 2023)

### Enhancements

 * Quick fixes to resolve unclosed sections in Qute templates. See [#821](https://github.com/redhat-developer/quarkus-ls/issues/821).
 * Support for uri/uriabs Qute resolvers from Quarkus Renarde. See [#571](https://github.com/redhat-developer/quarkus-ls/issues/571).
 * Workspace symbols and CodeLens for Renarde application REST endpoints. See [#777](https://github.com/redhat-developer/quarkus-ls/issues/777).
 * CodeAction to insert expected `<input>` for Renarde `#form` section in Qute template. See [#778](https://github.com/redhat-developer/quarkus-ls/issues/778).
 * Show validation errors for all Qute templates in a project (even unopened ones). See [#813](https://github.com/redhat-developer/quarkus-ls/issues/813).
 * User tag snippets and completion in Qute templates account for the tag parameters. See [#784](https://github.com/redhat-developer/quarkus-ls/issues/784).
 * Validatation and go to definition for user tag parameters in Qute templates. See [#788](https://github.com/redhat-developer/quarkus-ls/issues/788), [#789](https://github.com/redhat-developer/quarkus-ls/issues/789).
 * CodeAction to insert required user tag parameters in Qute template. See [#797](https://github.com/redhat-developer/quarkus-ls/issues/797).
 * Make Java file snippets context-aware. See [#782](https://github.com/redhat-developer/quarkus-ls/issues/782).
 * Add support for Qute `#fragment` section. See [#768](https://github.com/redhat-developer/quarkus-ls/pull/768), [#769](https://github.com/redhat-developer/quarkus-ls/pull/769).
 * When applying the Qute template quick fix to generate a missing class member, generate the member at the end of the class. See [#692](https://github.com/redhat-developer/quarkus-ls/pull/692).
 * Support for validation, completion and document link for `#include` and `#insert` sections in Qute templates. See [#438](https://github.com/redhat-developer/quarkus-ls/issues/438).
 * "Show references" CodeLens for `#insert` section in Qute templates. See [#814](https://github.com/redhat-developer/quarkus-ls/issues/814).
 * Support surround with section/comments command for Qute templates. See [#801](https://github.com/redhat-developer/quarkus-ls/pull/801)

### Bug Fixes

 * Treat `CompletionStage` and `Uni` objects as their resolved type for validation and completion in Qute templates. See [#826](https://github.com/redhat-developer/quarkus-ls/issues/826).
 * `{#if}` sections in Qute templates of the form `{#if !inject:beanName}{/if}` are marked as invalid. See [#828](https://github.com/redhat-developer/quarkus-ls/issues/828).
 * Quick fix to generate a `@TemplateExtension` class from a Qute template might place the class in the wrong folder. See [#831](https://github.com/redhat-developer/quarkus-ls/issues/831).
 * Incomplete `{#if}` section with operator may generate `ClassCastException`. See [#816](https://github.com/redhat-developer/quarkus-ls/issues/816).
 * Use quarkus-ls's Qute parser for syntax validation in order to get consistent diagnostic positions and ranges. See [#812](https://github.com/redhat-developer/quarkus-ls/issues/812).
 * Completion in Qute template should generate only start section when an empty end section is found. See [#805](https://github.com/redhat-developer/quarkus-ls/issues/805).
 * Quarkus 3 support. See [#779](https://github.com/redhat-developer/quarkus-ls/issues/779).
 * Qute "Generate missing member" quick fix cannot generate new template extension when `TemplateExtensions` and `TemplateExtensions0` exist. See [#712](https://github.com/redhat-developer/quarkus-ls/issues/712).
 * Prevent resolving code actions that run commands. See [#823](https://github.com/redhat-developer/quarkus-ls/pull/823).

### Build

 * Add `.gitleaks.toml` to ignore false positive leaks. See [#808](https://github.com/redhat-developer/quarkus-ls/pull/808).
 * Use Maven settings from `ENV` during Nexus deploy. See [#771](https://github.com/redhat-developer/quarkus-ls/pull/771).

## 0.13.0 (December 1, 2022)

### Enhancements

 * Add new snippet for `@QuarkusIntegrationTest`. See [#754](https://github.com/redhat-developer/quarkus-ls/issues/754).
 * Generic support for Java data model in Qute templates. See [#503](https://github.com/redhat-developer/quarkus-ls/issues/503).
 * Show documentation on hover of operator for `#case`, `#is`. See [#716](https://github.com/redhat-developer/quarkus-ls/issues/716).
 * Clickable inlay hint for Java type in Qute templates. See [#702](https://github.com/redhat-developer/quarkus-ls/issues/702).
 * Validation, completion & definition support for enum in `#switch` section. See [#689](https://github.com/redhat-developer/quarkus-ls/issues/689), [#690](https://github.com/redhat-developer/quarkus-ls/issues/690), [#691](https://github.com/redhat-developer/quarkus-ls/issues/691).
 * Create a code action class per error code. See [#688](https://github.com/redhat-developer/quarkus-ls/pull/688).
 * Added Qute code actions for similar text suggestions. See [#602](https://github.com/redhat-developer/quarkus-ls/issues/602), [#685](https://github.com/redhat-developer/quarkus-ls/issues/685) .
 * Use `resolve` for code action in Qute templates. See [#604](https://github.com/redhat-developer/quarkus-ls/issues/604).
 * Code actions to create Java field / getter method / template extension. See [#536](https://github.com/redhat-developer/quarkus-ls/issues/536), [#676](https://github.com/redhat-developer/quarkus-ls/issues/676), [#677](https://github.com/redhat-developer/quarkus-ls/issues/677).
 * Display Javadoc on hover for property/method . See [#452](https://github.com/redhat-developer/quarkus-ls/issues/452).

### Bug Fixes

 * Prevent duplicate template data when using type safe expressions. See [#750](https://github.com/redhat-developer/quarkus-ls/issues/750).
 * NPE when validating Qute template where a method is invoked. See [#748](https://github.com/redhat-developer/quarkus-ls/issues/748).
 * Qute parser does not parse operator parameters with '=' correctly. See [#742](https://github.com/redhat-developer/quarkus-ls/issues/742).
 * Indent snippet new line if LSP client doesn't support `InsertTextMode#AdjustIndentation`. See [#727](https://github.com/redhat-developer/quarkus-ls/issues/727).
 * Detect cyclical class hierarchy when calculating all fields and methods of an object. See [#725](https://github.com/redhat-developer/quarkus-ls/issues/725).
 * Ignore synthetic methods in Qute templates. See [#723](https://github.com/redhat-developer/quarkus-ls/issues/723).
 * `ClassCastException` with code action and method part. See [#717](https://github.com/redhat-developer/quarkus-ls/issues/717).
 * `JavaFileTextDocumentService` doesn't gracefully handle lack of rename support. See [#700](https://github.com/redhat-developer/quarkus-ls/issues/700).
 * Do not give code actions for stale diagnostics. See [#694](https://github.com/redhat-developer/quarkus-ls/pull/694).
 * `AbstractQuteTemplateLinkCollector` wrongly identifies template fields. See [#683](https://github.com/redhat-developer/quarkus-ls/issues/683).
 * Fix cancel support with `CompletableFuture` compose. See [#679](https://github.com/redhat-developer/quarkus-ls/pull/679).
 * Completion proposes invalid `{inject:*}` items. See [#590](https://github.com/redhat-developer/quarkus-ls/issues/590).

### Build

 * Exports missing from Quarkus test bundle. See [#752](https://github.com/redhat-developer/quarkus-ls/issues/752).
 * Update Target Platform to 1.16.0-SNAPSHOT version of JDT-LS target. See [#746](https://github.com/redhat-developer/quarkus-ls/pull/746).
 * Update dependencies to latest versions. See [#695](https://github.com/redhat-developer/quarkus-ls/pull/695).
 * Fix tests by taking into account new property from LSP4MP. See [#757](https://github.com/redhat-developer/quarkus-ls/pull/757).
 * Fix test failures due to new JAX-RS code lenses. See [#760](https://github.com/redhat-developer/quarkus-ls/issues/760).

## 0.12.0 (July 25, 2022)

### Enhancements

 * Support for `@TemplateGlobal` annotation. See [#605](https://github.com/redhat-developer/quarkus-ls/issues/605).
 * Support for `textDocument/InlayHint` in Qute templates. See [#595](https://github.com/redhat-developer/quarkus-ls/issues/595).
 * Suppress undefined variable errors in certain contexts. See [#548](https://github.com/redhat-developer/quarkus-ls/issues/548).
 * Add Rename support within Qute templates. See [#492](https://github.com/redhat-developer/quarkus-ls/issues/492).
 * Support missing attributes for `@TemplateData` / `@RegisterForReflection`. See [#631](https://github.com/redhat-developer/quarkus-ls/pull/631).
 * Provide `qute.native.enabled` setting. See [#629](https://github.com/redhat-developer/quarkus-ls/issues/629).
 * Code action to add `??` at the end of the object part for `UndefinedObject`. See [#613](https://github.com/redhat-developer/quarkus-ls/issues/613).
 * Completion for nested block section. See [#497](https://github.com/redhat-developer/quarkus-ls/issues/497).

### Performance

 * Delay revalidation of Java files, Qute template files, and improve cancel checking. See [#659](https://github.com/redhat-developer/quarkus-ls/pull/659), [#666](https://github.com/redhat-developer/quarkus-ls/pull/666).
 * Improve memory and performance of Qute language server. See [#654](https://github.com/redhat-developer/quarkus-ls/issues/654).

### Bug Fixes

 * Fix NPE with data model template. See [#664](https://github.com/redhat-developer/quarkus-ls/pull/664).
 * Template validation complains about strings containing spaces. See [#639](https://github.com/redhat-developer/quarkus-ls/issues/639).
 * Expression indexes are wrong. See [#627](https://github.com/redhat-developer/quarkus-ls/issues/627).
 * Simplify the resolve signature logic. See [#652](https://github.com/redhat-developer/quarkus-ls/pull/652).
 * `QuarkusConfigPropertiesProvider` void return type check doesn't work. See [#650](https://github.com/redhat-developer/quarkus-ls/issues/650).
 * Linked editing doesn't work if variable is used as a parameter into a section. See [#638](https://github.com/redhat-developer/quarkus-ls/pull/638).

### Build

 * Update Jenkinsfile to use Java 17. See [#669](https://github.com/redhat-developer/quarkus-ls/pull/669).
 * Adapt to new version of m2e in JDT-LS. See [#668](https://github.com/redhat-developer/quarkus-ls/pull/668).
 * Remove unnecessary 2019-06 release repository from target platform. See [#658](https://github.com/redhat-developer/quarkus-ls/pull/658).
 * Updates sites are not deployed anymore to download.jboss.org. See [#623](https://github.com/redhat-developer/quarkus-ls/issues/623).
 * P2 update sites (quarkus.jdt, qute.jdt) are not archived for new releases/tags. See [#617](https://github.com/redhat-developer/quarkus-ls/issues/617).
 * Remove unnecessary Gson dependency in pom files. See [#672](https://github.com/redhat-developer/quarkus-ls/pull/672).
 * Move to LSP4J 0.14.0. See [#644](https://github.com/redhat-developer/quarkus-ls/issues/644).
 * Update Quarkus LS to use LSP4MP 0.5.0 Snapshots. See [#621](https://github.com/redhat-developer/quarkus-ls/pull/621).

### Documentation

 * Add DCO information to `CONTRIBUTING.md`. See [#662](https://github.com/redhat-developer/quarkus-ls/issues/662).

## 0.11.0 (March 24, 2022)

### Enhancements

 * Create a Qute Language Server. See [#176](https://github.com/redhat-developer/quarkus-ls/issues/176).
 * Support for method parameters in Qute templates. See [#486](https://github.com/redhat-developer/quarkus-ls/issues/486).
 * Support Java type hover for Qute templates. See [#502](https://github.com/redhat-developer/quarkus-ls/issues/502).
 * Completion for available section after `{#`. See [#538](https://github.com/redhat-developer/quarkus-ls/issues/538).
 * Code action to disable validation. See [#531](https://github.com/redhat-developer/quarkus-ls/issues/531).
 * Add support for `raw` and `safe`, `orEmpty` extension methods, to escape characters. See [#498](https://github.com/redhat-developer/quarkus-ls/issues/498) & [#533](https://github.com/redhat-developer/quarkus-ls/issues/533).
 * User tag support. See [#551](https://github.com/redhat-developer/quarkus-ls/issues/551), [#560](https://github.com/redhat-developer/quarkus-ls/issues/560), [#561](https://github.com/redhat-developer/quarkus-ls/issues/561), [#564](https://github.com/redhat-developer/quarkus-ls/pull/564), [#566](https://github.com/redhat-developer/quarkus-ls/pull/566), [#567](https://github.com/redhat-developer/quarkus-ls/pull/567), [#568](https://github.com/redhat-developer/quarkus-ls/pull/568).
 * Support for injecting Beans directory. See [#546](https://github.com/redhat-developer/quarkus-ls/issues/546).
 * Support for `@TemplateExtension` matchName. See [#583](https://github.com/redhat-developer/quarkus-ls/issues/582).
 * Provide completion for cache name in configuration file. See [#404](https://github.com/redhat-developer/quarkus-ls/issues/404).
 * Definition & Validation support for `@Scheduled/cron`. See [#377](https://github.com/redhat-developer/quarkus-ls/issues/377), [#378](https://github.com/redhat-developer/quarkus-ls/issues/378).
 * Support for `@ConfigMapping`. See [#413](https://github.com/redhat-developer/quarkus-ls/issues/413), [#424](https://github.com/redhat-developer/quarkus-ls/issues/424).
 * Support `application-${profile}.properties`. See [#411](https://github.com/redhat-developer/quarkus-ls/pull/411).
 * Add settings to disable CodeLens. See [#472](https://github.com/redhat-developer/quarkus-ls/issues/472).

### Bug Fixes

 * Use SafeConstructor for Yaml parser instantation. See [#527](https://github.com/redhat-developer/quarkus-ls/pull/527).
 * CodeLens URL does not respect `quarkus.http.root-path property`. See [#368](https://github.com/redhat-developer/quarkus-ls/issues/368), [#414](https://github.com/redhat-developer/quarkus-ls/pull/414).

### Build

 * Update o.e.jdt.ls.tp dependency to 1.7.0-SNAPSHOT. See [#478](https://github.com/redhat-developer/quarkus-ls/pull/478).
 * Qute artifacts deployment is missing. See [#448](https://github.com/redhat-developer/quarkus-ls/issues/448).

### Other

 * Skip Java 17 related tests. See [#576](https://github.com/redhat-developer/quarkus-ls/pull/576).
 * Qute JDT unit tests are not exported. See [#573](https://github.com/redhat-developer/quarkus-ls/issues/573).
 * Update to lsp4j 0.11.0 to reflect change in lsp4mp. See [#417](https://github.com/redhat-developer/quarkus-ls/issues/417).
 * Use Qute 2.7.0. See [#549](https://github.com/redhat-developer/quarkus-ls/pull/549).

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
