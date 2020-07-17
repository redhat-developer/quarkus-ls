# Quarkus LS extension

Provides [Quarkus](https://quarkus.io/) specific functionality to the MicroProfile Language Server [lsp4mp](https://github.com/eclipse/lsp4mp/blob/master/microprofile.ls) using Java SPI contributions.

## Building the Quarkus lsp4mp extension

- Clone this repository
- Navigate to the `quarkus-ls/quarkus.ls.ext/com.redhat.quarkus.ls` folder in your terminal or command line
- Run `./mvnw clean verify` (OSX, Linux) or `mvnw.cmd clean verify` (Windows)
- After successful compilation you can find the resulting `com.redhat.quarkus.ls-uber.jar` in the
  `quarkus-ls/quarkus.ls.ext/com.redhat.quarkus.ls/target` folder.

Java and properties completion snippets are managed by
[lsp4mp](https://github.com/eclipse/lsp4mp) and are expanded on by this
extension using Java SPI.
