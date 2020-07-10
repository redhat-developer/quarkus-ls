MicroProfile Language Server (microprofile-ls)
===========================

The MicroProfile language server is an implementation of the 
[Language Server Protocol](https://github.com/Microsoft/language-server-protocol), providing
language features for: 

 * Properties files: microprofile-config.properties, application.properties, application.yaml files from a
[MicroProfile](https://microprofile.io/) or [Quarkus](https://quarkus.io/) project.
 * Java files (URL CodeLens for JAX-RS, Java MicroProfile snippets, etc).

The MicroProfile language server works alongside the [MicroProfile jdt.ls extension](https://github.com/redhat-developer/quarkus-ls/tree/master/microprofile.jdt)
which is also located in this repository. The MicroProfile jdt.ls extension is able to index
a MicroProfile project for metadata (config properties, documentation, sources, etc.) to provide the 
MicroProfile language server the information required for the language features.
The relationship between the MicroProfile language server and the MicroProfile jdt.ls extension is explained 
in more detail in the 
[vscode-quarkus contributing guide](https://github.com/redhat-developer/vscode-quarkus/blob/master/CONTRIBUTING.md).

Features
--------------

* [textDocument/completion](https://microsoft.github.io/language-server-protocol/specifications/specification-3-14/#textDocument_completion)
* [textDocument/definition](https://microsoft.github.io/language-server-protocol/specifications/specification-3-14#textDocument_definition)
* [textDocument/documentSymbol](https://microsoft.github.io/language-server-protocol/specifications/specification-3-14/#textDocument_documentSymbol)
* [textDocument/formatting](https://microsoft.github.io/language-server-protocol/specifications/specification-3-14/#textDocument_formatting)
* [textDocument/rangeFormatting](https://microsoft.github.io/language-server-protocol/specifications/specification-3-14/#textDocument_rangeFormatting)
* [textDocument/hover](https://microsoft.github.io/language-server-protocol/specifications/specification-3-14/#textDocument_hover)
* [textDocument/publishDiagnostics](https://microsoft.github.io/language-server-protocol/specifications/specification-3-14/#textDocument_publishDiagnostics)

Demo
--------------
The following gif demonstrates the `textDocument/completion` request and response in Visual Studio Code.
![key completion](./demos/textDocument_completion.gif)
Click [here](./demos/DEMO.md) to view a demo for the rest of the language features.

Building the Language Server
--------------
* Clone this repository
* Navigate to the `quarkus-ls/microprofile.ls/org.eclipse.lsp4mp.ls` folder in your terminal or command line
* Run `./mvnw clean verify` (OSX, Linux) or `mvnw.cmd clean verify` (Windows)
* After successful compilation you can find the resulting `org.eclipse.lsp4mp.ls-uber.jar` in the
`quarkus-ls/microprofile.ls/org.eclipse.lsp4mp.ls/target` folder.

Clients
-------

Here are some clients consuming the MicroProfile language server:

 * Eclipse with [quarkus-lsp4e (POC)](https://github.com/angelozerr/quarkus-lsp4e)
 * IntelliJ with [intellij-quarkus](https://github.com/jeffmaury/intellij-quarkus)
 * Visual Studio Code with [vscode-quarkus](https://github.com/redhat-developer/vscode-quarkus)
 
Code Snippets
-------

Java and properties completion snippets are managed by the MicroProfile LS (snippets on server side) by using Java SPI.

## Describing snippets in JSON

`Snippets` are described in JSON files using the [vscode snippet format](https://code.visualstudio.com/docs/editor/userdefinedsnippets#_create-your-own-snippets).

The `"context"` key can be provided to specify the condition in which the snippet should appear:

 * for `properties files`: the context/properties (can be String or String array) can be declared by the snippet to show the snippet only if the declared property belongs to the project. If an array of properties were provided, the snippet will be displayed if all properties belongs to the project.

```json
	"Add datasource properties": {
		"prefix": "qds",
		"body": [
			"quarkus.datasource.db-kind=...",
			...
			"quarkus.datasource.jdbc.url=..."
		],
		"context": {
			"properties": "quarkus.datasource.jdbc.url"
		}
	}
```
		
means that the snippet is shown only if the project has the `quarkus.datasource.jdbc.url` property.
    
 * for `Java files`: the context/type can be declared in the snippet to show the snippet only if the Java type belongs to the project.

```json
  "@Operation": {
    "prefix": [
      "@Operation"
    ],
    "body": [
      ...
    ],
    "context": {
    	"type": "org.eclipse.microprofile.openapi.annotations.Operation"
    }
  }
```

means that the snippet is shown only if the project has the `org.eclipse.microprofile.openapi.annotations.Operation` Java Annotation in the classpath. In other words, only when the Java project has a dependency on MicroProfile Open API.
 
## Adding new internal snippets

To register a snippet, it must be added in:

 * [MicroProfileJavaSnippetRegistryLoader](https://github.com/redhat-developer/quarkus-ls/blob/master/microprofile.ls/org.eclipse.lsp4mp.ls/src/main/java/com/redhat/microprofile/snippets/MicroProfileJavaSnippetRegistryLoader.java) if the new snippet is for Java files. 
 * [MicroProfilePropertiesSnippetRegistryLoader](https://github.com/redhat-developer/quarkus-ls/blob/master/microprofile.ls/org.eclipse.lsp4mp.ls/src/main/java/com/redhat/microprofile/snippets/MicroProfilePropertiesSnippetRegistryLoader.java) if the new snippet is for properties files.

## Adding new external snippets

To add external snippets (like Quarkus snippets) an implementation of `ISnippetRegistryLoader` must be created and registered with Java SPI. See for Quarkus snippets:

 * [Java Quarkus snippets loader](https://github.com/redhat-developer/quarkus-ls/tree/master/microprofile.ls/org.eclipse.lsp4mp.ls/src/main/java/com/redhat/quarkus/snippets).
 * [JSON Quarkus snippet](https://github.comredhat-developer/quarkus-ls/tree/master/microprofile.ls/org.eclipse.lsp4mp.ls/src/main/resources/com/redhat/quarkus/snippets).
 * Java Quarkus snippets loader must be declared in [META-INF/services/org.eclipse.lsp4mp.ls.commons.snippets.ISnippetRegistryLoader](https://github.com/redhat-developer/quarkus-ls/blob/master/microprofile.ls/org.eclipse.lsp4mp.ls/src/main/resources/META-INF/services/org.eclipse.lsp4mp.ls.commons.snippets.ISnippetRegistryLoader) 
