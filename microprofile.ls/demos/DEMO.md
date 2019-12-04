LSP Language Features Demo
===========================

textDocument/completion
--------------

#### Quarkus property key completion provided by the `textDocument/completion` request
![key completion](./textDocument_completion.gif)

#### Completion provides default value is exists
![completion default value](./textDocument_completion2.gif)

#### Quarkus property value completion for enumerated types and booleans
![completion enum boolean](./textDocument_completion3.gif)

#### Completion can be returned as a snippet, if supported by the client
![completion map property](./textDocument_completion4.gif)

textDocument/definition
--------------
#### Goto Definition provided by `textDocument/definition` request
![goto definition](./textDocument_definition.gif)

textDocument/documentSymbol
--------------
#### Document symbols provided by the `textDocument/documentSymbol` request
![document symbol](./textDocument_symbol.png)

textDocument/hover
--------------
#### Hover supported provided by the `textDocument/hover` request
![hover](./textDocument_hover.png)

#### Hover support for Quarkus property keys and default profiles
![hover profile and key](./textDocument_hover2.gif)

textDocument/publishDiagnostics
--------------
#### Diagnostics supplied to client by `textDocument/publishDiagnostics` notification
#### Supported diagnostics:
* unknown properties
* duplicate properties
* missing equals signs

![diagnostics](./textDocument_publishDiagnostics.png)
