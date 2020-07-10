cd microprofile.jdt && .\mvnw.cmd clean install ^
&& cd ..\quarkus.jdt.ext && .\mvnw.cmd clean verify ^
&& cd ..\microprofile.ls\org.eclipse.lsp4mp.ls && .\mvnw.cmd clean install ^
&& cd ..\..\quarkus.ls.ext\com.redhat.quarkus.ls && .\mvnw.cmd clean verify ^
&& cd ..\..