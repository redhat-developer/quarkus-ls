Qute Language Server (qute-ls)
===========================

The Qute language server is an implementation of the 
[Language Server Protocol](https://github.com/Microsoft/language-server-protocol), providing
language features for [Qute template engine](https://quarkus.io/guides/qute-reference): 

 * Qute template files.
 * Java files to provide Quarkus support.

Features
--------------

* [textDocument/completion](https://microsoft.github.io/language-server-protocol/specifications/specification-3-14/#textDocument_completion)
* [textDocument/definition](https://microsoft.github.io/language-server-protocol/specifications/specification-3-14#textDocument_definition)
* [textDocument/documentSymbol](https://microsoft.github.io/language-server-protocol/specifications/specification-3-14/#textDocument_documentSymbol)
* [textDocument/hover](https://microsoft.github.io/language-server-protocol/specifications/specification-3-14/#textDocument_hover)
* [textDocument/publishDiagnostics](https://microsoft.github.io/language-server-protocol/specifications/specification-3-14/#textDocument_publishDiagnostics)

Demo
--------------

TODO

Building the Language Server
--------------
* Clone this repository
* Navigate to the `quarkus-ls/qute.ls/com.redhat.qute.ls` folder in your terminal or command line
* Run `./mvnw clean verify` (OSX, Linux) or `mvnw.cmd clean verify` (Windows)
* After successful compilation you can find the resulting `com.redhat.qute.ls-uber.jar` in the
`quarkus-ls/qute.ls/com.redhat.qute.ls/target` folder.

Clients
-------

Here are some clients consuming the Qute language server:

 * Visual Studio Code with [vscode-quarkus](https://github.com/redhat-developer/vscode-quarkus)