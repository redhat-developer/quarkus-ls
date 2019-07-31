package com.redhat.quarkus.ls;

import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.services.LanguageClient;

import com.redhat.quarkus.commons.QuarkusProjectInfo;

public interface QuarkusLanguageClient extends LanguageClient {

	@JsonRequest("quarkus/projectInfo")
	CompletableFuture<QuarkusProjectInfo> getQuarkusProjectInfo(String projectName);
}
