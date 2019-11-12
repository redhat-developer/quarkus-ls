/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.quarkus.services;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import com.redhat.quarkus.commons.QuarkusProjectInfo;
import com.redhat.quarkus.ls.api.QuarkusPropertyDefinitionProvider;
import com.redhat.quarkus.model.PropertiesModel;
import com.redhat.quarkus.model.values.ValuesRulesManager;
import com.redhat.quarkus.settings.QuarkusCompletionSettings;
import com.redhat.quarkus.settings.QuarkusFormattingSettings;
import com.redhat.quarkus.settings.QuarkusHoverSettings;
import com.redhat.quarkus.settings.QuarkusValidationSettings;

/**
 * The Quarkus language service.
 * 
 * @author Angelo ZERR
 *
 */
public class QuarkusLanguageService {

	private final QuarkusCompletions completions;
	private final QuarkusSymbolsProvider symbolsProvider;
	private final QuarkusHover hover;
	private final QuarkusDefinition definition;
	private final QuarkusDiagnostics diagnostics;
	private final QuarkusFormatter formatter;
	private final QuarkusCodeActions codeActions;
	private final ValuesRulesManager valuesRulesManager;

	public QuarkusLanguageService() {
		this(new ValuesRulesManager(true));
	}

	public QuarkusLanguageService(ValuesRulesManager valuesRulesManger) {
		this.completions = new QuarkusCompletions();
		this.symbolsProvider = new QuarkusSymbolsProvider();
		this.hover = new QuarkusHover();
		this.definition = new QuarkusDefinition();
		this.diagnostics = new QuarkusDiagnostics();
		this.formatter = new QuarkusFormatter();
		this.codeActions = new QuarkusCodeActions();
		this.valuesRulesManager = valuesRulesManger;
	}

	/**
	 * Returns completion list for the given position
	 * 
	 * @param document           the properties model document
	 * @param position           the position where completion was triggered
	 * @param projectInfo        the Quarkus project information
	 * @param completionSettings the completion settings
	 * @param cancelChecker      the cancel checker
	 * @return completion list for the given position
	 */
	public CompletionList doComplete(PropertiesModel document, Position position, QuarkusProjectInfo projectInfo,
			QuarkusCompletionSettings completionSettings, CancelChecker cancelChecker) {
		return completions.doComplete(document, position, projectInfo, getValuesRulesManager(), completionSettings,
				cancelChecker);
	}

	/**
	 * Returns Hover object for the currently hovered token
	 * 
	 * @param document      the properties model document
	 * @param position      the hover position
	 * @param projectInfo   the Quarkus project information
	 * @param hoverSettings the hover settings
	 * @return Hover object for the currently hovered token
	 */
	public Hover doHover(PropertiesModel document, Position position, QuarkusProjectInfo projectInfo,
			QuarkusHoverSettings hoverSettings) {
		return hover.doHover(document, position, projectInfo, getValuesRulesManager(), hoverSettings);
	}

	/**
	 * Returns symbol information list for the given properties model.
	 * 
	 * @param document      the properties model document
	 * @param cancelChecker the cancel checker
	 * @return symbol information list for the given properties model.
	 */
	public List<SymbolInformation> findSymbolInformations(PropertiesModel document, CancelChecker cancelChecker) {
		return symbolsProvider.findSymbolInformations(document, cancelChecker);
	}

	/**
	 * Returns document symbol list for the given properties model.
	 * 
	 * @param document      the properties model document
	 * @param cancelChecker the cancel checker
	 * @return document symbol list for the given properties model.
	 */
	public List<DocumentSymbol> findDocumentSymbols(PropertiesModel document, CancelChecker cancelChecker) {
		return symbolsProvider.findDocumentSymbols(document, cancelChecker);
	}

	/**
	 * Returns as promise the Java field definition location of the property at the
	 * given <code>position</code> of the given application.properties
	 * <code>document</code>.
	 * 
	 * @param document              the properties model.
	 * @param position              the position where definition was triggered
	 * @param projectInfo           the Quarkus properties
	 * @param provider              the Quarkus property definition provider.
	 * @param definitionLinkSupport true if {@link LocationLink} must be returned
	 *                              and false otherwise.
	 * @return as promise the Java field definition location of the property at the
	 *         given <code>position</code> of the given application.properties
	 *         <code>document</code>.
	 */
	public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> findDefinition(
			PropertiesModel document, Position position, QuarkusProjectInfo projectInfo,
			QuarkusPropertyDefinitionProvider provider, boolean definitionLinkSupport) {
		return definition.findDefinition(document, position, projectInfo, provider, definitionLinkSupport);
	}

	/**
	 * Returns a <code>List<TextEdit></code> that formats the application.properties
	 * file represented by <code>document</code>
	 * 
	 * @param document           the properties model
	 * @param formattingSettings the client's formatting settings
	 * @return a <code>List<TextEdit></code> that formats the application.properties
	 *         file represented by <code>document</code>
	 */
	public List<? extends TextEdit> doFormat(PropertiesModel document, QuarkusFormattingSettings formattingSettings) {
		return formatter.format(document, formattingSettings);
	}

	/**
	 * Returns a <code>List<TextEdit></code> that formats the application.properties
	 * file represented by <code>document</code>, for the given <code>range</code>
	 * 
	 * @param document           the properties model
	 * @param range              the range specifying the lines to format
	 * @param formattingSettings the client's formatting settings
	 * @return
	 */
	public List<? extends TextEdit> doRangeFormat(PropertiesModel document, Range range,
			QuarkusFormattingSettings formattingSettings) {
		return formatter.format(document, range, formattingSettings);
	}

	/**
	 * Validate the given application.properties <code>document</code> by using the
	 * given Quarkus properties metadata <code>projectInfo</code>.
	 * 
	 * @param document           the properties model.
	 * @param projectInfo        the Quarkus properties
	 * @param validationSettings the validation settings.
	 * @param cancelChecker      the cancel checker.
	 * @return the result of the validation.
	 */
	public List<Diagnostic> doDiagnostics(PropertiesModel document, QuarkusProjectInfo projectInfo,
			QuarkusValidationSettings validationSettings, CancelChecker cancelChecker) {
		return diagnostics.doDiagnostics(document, projectInfo, getValuesRulesManager(), validationSettings,
				cancelChecker);
	}

	/**
	 * Returns code actions for the given diagnostics of the application.properties
	 * <code>document</code> by using the given Quarkus properties metadata
	 * <code>projectInfo</code>.
	 * 
	 * @param context            the code action context
	 * @param range              the range
	 * @param document           the properties model.
	 * @param projectInfo        the Quarkus properties
	 * @param formattingSettings the formatting settings.
	 * @return the result of the code actions.
	 */
	public List<CodeAction> doCodeActions(CodeActionContext context, Range range, PropertiesModel document,
			QuarkusProjectInfo projectInfo, QuarkusFormattingSettings formattingSettings) {
		return codeActions.doCodeActions(context, range, document, projectInfo, getValuesRulesManager(), formattingSettings);
	}

	/**
	 * Returns the manager for values rules.
	 * 
	 * @return the manager for values rules.
	 */
	private ValuesRulesManager getValuesRulesManager() {
		return valuesRulesManager;
	}
}
