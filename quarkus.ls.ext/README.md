# Quarkus LS extension

Provides additional functionality for [Quarkus](https://quarkus.io/) to the MicroProfile Language Server ([microprofile-ls](https://github.com/redhat-developer/quarkus-ls/blob/master/microprofile.ls)) using Java SPI contributions.

## Building the Quarkus language server extension

- Clone this repository
- Navigate to the `quarkus-ls/quarkus.ls.ext/com.redhat.quarkus.ls` folder in your terminal or command line
- Run `./mvnw clean verify` (OSX, Linux) or `mvnw.cmd clean verify` (Windows)
- After successful compilation you can find the resulting `com.redhat.quarkus.ls-uber.jar` in the
  `quarkus-ls/quarkus.ls.ext/com.redhat.quarkus.ls/target` folder.

Java and properties completion snippets are managed by the MicroProfile LS (snippets on server side) by using Java SPI.
