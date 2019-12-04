/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.services;

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

import com.redhat.microprofile.commons.MicroProfileProjectInfo;
import com.redhat.microprofile.ls.api.MicroProfilePropertyDefinitionProvider;
import com.redhat.microprofile.model.PropertiesModel;
import com.redhat.microprofile.model.values.ValuesRulesManager;
import com.redhat.microprofile.settings.MicroProfileCommandCapabilities;
import com.redhat.microprofile.settings.MicroProfileCompletionSettings;
import com.redhat.microprofile.settings.MicroProfileFormattingSettings;
import com.redhat.microprofile.settings.MicroProfileHoverSettings;
import com.redhat.microprofile.settings.MicroProfileValidationSettings;

/**
 * The Quarkus language service.
 * 
 * @author Angelo ZERR
 *
 */
public class MicroProfileLanguageService {

	private final MicroProfileCompletions completions;
	private final MicroProfileSymbolsProvider symbolsProvider;
	private final MicroProfileHover hover;
	private final MicroProfileDefinition definition;
	private final MicroProfileDiagnostics diagnostics;
	private final MicroProfileFormatter formatter;
	private final MicroProfileCodeActions codeActions;
	private final ValuesRulesManager valuesRulesManager;

	public MicroProfileLanguageService() {
		this(new ValuesRulesManager(true));
	}

	public MicroProfileLanguageService(ValuesRulesManager valuesRulesManger) {
		this.completions = new MicroProfileCompletions();
		this.symbolsProvider = new MicroProfileSymbolsProvider();
		this.hover = new MicroProfileHover();
		this.definition = new MicroProfileDefinition();
		this.diagnostics = new MicroProfileDiagnostics();
		this.formatter = new MicroProfileFormatter();
		this.codeActions = new MicroProfileCodeActions();
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
	public CompletionList doComplete(PropertiesModel document, Position position, MicroProfileProjectInfo projectInfo,
			MicroProfileCompletionSettings completionSettings, MicroProfileFormattingSettings formattingSettings,
			CancelChecker cancelChecker) {
		return completions.doComplete(document, position, projectInfo, getValuesRulesManager(), completionSettings,
				formattingSettings, cancelChecker);
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
	public Hover doHover(PropertiesModel document, Position position, MicroProfileProjectInfo projectInfo,
			MicroProfileHoverSettings hoverSettings) {
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
			PropertiesModel document, Position position, MicroProfileProjectInfo projectInfo,
			MicroProfilePropertyDefinitionProvider provider, boolean definitionLinkSupport) {
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
	public List<? extends TextEdit> doFormat(PropertiesModel document,
			MicroProfileFormattingSettings formattingSettings) {
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
			MicroProfileFormattingSettings formattingSettings) {
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
	public List<Diagnostic> doDiagnostics(PropertiesModel document, MicroProfileProjectInfo projectInfo,
			MicroProfileValidationSettings validationSettings, CancelChecker cancelChecker) {
		return diagnostics.doDiagnostics(document, projectInfo, getValuesRulesManager(), validationSettings,
				cancelChecker);
	}

	/**
	 * Returns code actions for the given diagnostics of the application.properties
	 * <code>document</code> by using the given Quarkus properties metadata
	 * <code>projectInfo</code>.
	 * 
	 * @param context             the code action context
	 * @param range               the range
	 * @param document            the properties model.
	 * @param projectInfo         the Quarkus properties
	 * @param formattingSettings  the formatting settings.
	 * @param commandCapabilities the command capabilities
	 * @return the result of the code actions.
	 */
	public List<CodeAction> doCodeActions(CodeActionContext context, Range range, PropertiesModel document,
			MicroProfileProjectInfo projectInfo, MicroProfileFormattingSettings formattingSettings,
			MicroProfileCommandCapabilities commandCapabilities) {
		return codeActions.doCodeActions(context, range, document, projectInfo, getValuesRulesManager(),
				formattingSettings, commandCapabilities);
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
