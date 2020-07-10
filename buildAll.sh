# cd microprofile.jdt && ./mvnw clean install && cd .. && \
# cd quarkus.jdt.ext && ./mvnw clean verify  && cd .. && \
cd microprofile.ls/org.eclipse.lsp4mp.ls && ./mvnw clean install && cd ../.. && \
cd quarkus.ls.ext/com.redhat.quarkus.ls && ./mvnw clean verify && cd ../.. 