package com.redhat.quarkus.ls;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;

import com.redhat.quarkus.commons.ExtendedConfigDescriptionBuildItem;

public class QuarkusTextDocumentService implements TextDocumentService {

	private static final Logger LOGGER = Logger.getLogger(QuarkusLanguageServer.class.getName());

	private final QuarkusLanguageServer quarkusLanguageServer;

	public QuarkusTextDocumentService(QuarkusLanguageServer quarkusLanguageServer) {
		this.quarkusLanguageServer = quarkusLanguageServer;
	}

	public void didOpen(DidOpenTextDocumentParams params) {
		// TODO Auto-generated method stub

	}

	public void didChange(DidChangeTextDocumentParams params) {
		// TODO Auto-generated method stub

	}

	public void didClose(DidCloseTextDocumentParams params) {
		// TODO Auto-generated method stub

	}

	public void didSave(DidSaveTextDocumentParams params) {
		// TODO Auto-generated method stub

	}

	@Override
	public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams params) {
		String projectName = "TODO";
		return quarkusLanguageServer.getLanguageClient().getQuarkusProjectInfo(projectName)
				.thenApplyAsync(projectInfo -> {
					if (!projectInfo.isQuarkusProject()) {
						// return CompletableFuture.completedFuture();
					}
					final List<CompletionItem> completions = new ArrayList<>();
					for (ExtendedConfigDescriptionBuildItem property : projectInfo.getProperties()) {
						completions.add(new CompletionItem(property.getPropertyName()));
					}
					return Either.forLeft(completions);
				});
//        
//        
//		final List<CompletionItem> completions = new ArrayList<>();
//        return CompletableFuture.supplyAsync(() -> {
//        	// TODO: check the project is a Quarkus project
//        	// TODO: store in cache properties
//        	
//        	
//        	completions.add(new CompletionItem("quarkus.option1"));
//        	completions.add(new CompletionItem("quarkus.option2"));
//        	completions.add(new CompletionItem("quarkus.option3"));
//        	completions.add(new CompletionItem("quarkus.option4"));
//        	completions.add(new CompletionItem("quarkus.option5"));
//        	completions.add(new CompletionItem("quarkus.option6"));
//        	completions.add(new CompletionItem("quarkus.option7"));
//        	
//        	return Either.forLeft(completions);
		// });
	}

}
