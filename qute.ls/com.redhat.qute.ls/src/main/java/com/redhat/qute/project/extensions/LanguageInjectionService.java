package com.redhat.qute.project.extensions;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.DocumentLink;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.ls.commons.TextDocument;
import com.redhat.qute.parser.NodeBase;
import com.redhat.qute.parser.injection.LanguageInjectionNode;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.services.completions.CompletionRequest;
import com.redhat.qute.services.hover.HoverRequest;
import com.redhat.qute.settings.QuteCommandCapabilities;
import com.redhat.qute.settings.QuteCompletionSettings;
import com.redhat.qute.settings.QuteFormattingSettings;

public interface LanguageInjectionService {

	String getLanguageId();

	NodeBase<?> parse(TextDocument text, int start, int end, com.redhat.qute.parser.CancelChecker cancelChecker);

	CompletableFuture<CompletionList> doComplete(LanguageInjectionNode languageInjection,
			CompletionRequest completionRequest, QuteCompletionSettings completionSettings,
			QuteFormattingSettings formattingSettings, QuteCommandCapabilities commandCapabilities,
			CancelChecker cancelChecker);

	void findDocumentLinks(LanguageInjectionNode languageInjection, Template template, List<DocumentLink> links,
			CancelChecker cancelChecker);

	CompletableFuture<Hover> doHover(LanguageInjectionNode languageInjection, HoverRequest hoverRequest,
			CancelChecker cancelChecker);

}
