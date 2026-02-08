package com.redhat.qute.project.extensions.roq.frontmatter;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.DocumentLink;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.ls.commons.TextDocument;
import com.redhat.qute.ls.commons.client.CommandCapabilities;
import com.redhat.qute.parser.NodeBase;
import com.redhat.qute.parser.injection.LanguageInjectionNode;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.parser.yaml.YamlDocument;
import com.redhat.qute.parser.yaml.YamlParser;
import com.redhat.qute.project.extensions.LanguageInjectionService;
import com.redhat.qute.services.completions.CompletionRequest;
import com.redhat.qute.services.hover.HoverRequest;
import com.redhat.qute.settings.QuteCommandCapabilities;
import com.redhat.qute.settings.QuteCompletionSettings;
import com.redhat.qute.settings.QuteFormattingSettings;

public class YamlFrontMatterLanguageInjectionService implements LanguageInjectionService {

	private final YamlFrontMatterCompletion completion;
	private final YamlFrontMatterHover hover;
	private final YamlFrontMatterDocumentLink documentLink;

	public YamlFrontMatterLanguageInjectionService() {
		completion = new YamlFrontMatterCompletion();
		hover = new YamlFrontMatterHover();
		documentLink = new YamlFrontMatterDocumentLink();
	}

	@Override
	public String getLanguageId() {
		return YamlFrontMatterDetector.YAML_FRONT_MATTER_LANGUAGE_ID;
	}

	@Override
	public NodeBase<?> parse(TextDocument textDocument, int start, int end,
			com.redhat.qute.parser.CancelChecker cancelChecker) {
		return YamlParser.parse(textDocument, start, end, cancelChecker);
	}

	@Override
	public CompletableFuture<CompletionList> doComplete(LanguageInjectionNode node, CompletionRequest completionRequest,
			QuteCompletionSettings completionSettings, QuteFormattingSettings formattingSettings,
			QuteCommandCapabilities commandCapabilities, CancelChecker cancelChecker) {
		YamlDocument document = (YamlDocument) node.getInjectedNode(() -> cancelChecker.checkCanceled());
		Template template = completionRequest.getTemplate();
		int offset = completionRequest.getOffset();
		return completion.doComplete(document, template, offset, completionSettings, formattingSettings,
				commandCapabilities, cancelChecker);
	}

	@Override
	public void findDocumentLinks(LanguageInjectionNode node, Template template, List<DocumentLink> links,
			CancelChecker cancelChecker) {
		YamlDocument document = (YamlDocument) node.getInjectedNode(() -> cancelChecker.checkCanceled());
		documentLink.findDocumentLinks(document, template, links, cancelChecker);
	}

	@Override
	public CompletableFuture<Hover> doHover(LanguageInjectionNode node, HoverRequest hoverRequest,
			CancelChecker cancelChecker) {
		YamlDocument document = (YamlDocument) node.getInjectedNode(() -> cancelChecker.checkCanceled());
		int offset = hoverRequest.getOffset();
		return hover.doHover(document, offset, hoverRequest, cancelChecker);
	}

}
