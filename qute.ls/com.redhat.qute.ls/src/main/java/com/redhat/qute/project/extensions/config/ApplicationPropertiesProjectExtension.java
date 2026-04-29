/*******************************************************************************
* Copyright (c) 2026 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.project.extensions.config;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.FileChangeType;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.commons.TemplateRootPath;
import com.redhat.qute.commons.config.PropertyConfig;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.project.QuteTextDocument;
import com.redhat.qute.project.datamodel.ExtendedDataModelProject;
import com.redhat.qute.project.extensions.AbstractProjectExtension;
import com.redhat.qute.project.extensions.CodeLensParticipant;
import com.redhat.qute.project.extensions.DidChangeWatchedFilesParticipant;
import com.redhat.qute.project.extensions.ProjectExtensionContext;
import com.redhat.qute.services.commands.QuteClientCommandConstants;
import com.redhat.qute.settings.SharedSettings;
import com.redhat.qute.utils.QutePositionUtility;

/**
 * Application properties project extension.
 */
public class ApplicationPropertiesProjectExtension extends AbstractProjectExtension
		implements DidChangeWatchedFilesParticipant, CodeLensParticipant {

	public static final String APPLICATION_PROPERTIES_PROJECT_EXTENSION_ID = "application-properties";

	public static final PropertyConfig ALT_EXPR_SYNTAX = new PropertyConfig("quarkus.qute.alt-expr-syntax", "false");

	private final ApplicationPropertiesFileRegistry applicationPropertiesRegistry;

	private final DotQuteFileRegistry dotQuteRegistry;

	private Set<Path> allSourcePaths;

	private List<TemplateRootPath> templateRootPaths;

	private boolean currentAltExprSyntax;

	public ApplicationPropertiesProjectExtension() {
		super(APPLICATION_PROPERTIES_PROJECT_EXTENSION_ID);
		this.applicationPropertiesRegistry = new ApplicationPropertiesFileRegistry();
		this.dotQuteRegistry = new DotQuteFileRegistry();
	}

	@Override
	protected void initialize(ExtendedDataModelProject dataModelProject, boolean onLoad, boolean enabled,
			ProjectExtensionContext context) {
		allSourcePaths = null;
		templateRootPaths = null;
		if (onLoad) {
			boolean altExprSyntax = getDataModelProject().getConfigAsBoolean(ALT_EXPR_SYNTAX);
			if (altExprSyntax) {
				reparseTemplates(dataModelProject, context);
			}
			this.currentAltExprSyntax = altExprSyntax;
		}
	}

	private static void reparseTemplates(ExtendedDataModelProject dataModelProject, ProjectExtensionContext context) {
		// Reparse opened/closed source document
		for (QuteTextDocument document : dataModelProject.getSourceDocuments()) {
			context.reparseTemplate(document);
		}
	}

	/**
	 * Reparses templates belonging to a specific template root path.
	 *
	 * @param templateRootPath the template root path
	 * @param dataModelProject the data model project
	 * @param context          the project extension context
	 */
	private static void reparseTemplatesFor(TemplateRootPath templateRootPath,
			ExtendedDataModelProject dataModelProject, ProjectExtensionContext context) {
		// Reparse templates in this template root path
		for (QuteTextDocument document : dataModelProject.getSourceDocuments()) {
			if (templateRootPath.isIncluded(document.getTemplatePath())) {
				if (context != null) {
					context.reparseTemplate(document);
				} else {
					document.reparseTemplate();
				}
			}
		}
	}

	/**
	 * Gets a configuration property value from application.properties.
	 *
	 * @param property the property config
	 * @return the property value, or the default value if not found
	 */
	public String getConfig(PropertyConfig property) {
		initFilesIfNeeded();
		return applicationPropertiesRegistry.getConfig(property);
	}

	private void initFilesIfNeeded() {
		var dataModelProject = getDataModelProject();
		if (dataModelProject == null) {
			return;
		}
		if (allSourcePaths != null) {
			return;
		}
		initFilesSync();
	}

	private synchronized void initFilesSync() {
		var dataModelProject = getDataModelProject();
		if (dataModelProject == null) {
			return;
		}

		if (allSourcePaths != null) {
			return;
		}

		// config/application.properties
		Set<Path> allSourcePaths = new HashSet<>();
		Path projectFolder = getDataModelProject().getProjectFolder();
		if (projectFolder != null) {
			allSourcePaths.add(projectFolder.resolve("config"));
		}

		// src/main/resources/application.properties
		Set<Path> sourcePaths = getDataModelProject().getSourcePaths();
		if (sourcePaths != null && !sourcePaths.isEmpty()) {
			allSourcePaths.addAll(sourcePaths);
		}
		applicationPropertiesRegistry.load(allSourcePaths);

		// Load .qute files from template root paths
		List<TemplateRootPath> templateRootPaths = getDataModelProject().getTemplateRootPaths();
		dotQuteRegistry.load(templateRootPaths);

		this.allSourcePaths = allSourcePaths;
		this.templateRootPaths = templateRootPaths;

		// Reparse templates for template root paths with altExprSyntax enabled
		if (templateRootPaths != null) {
			for (TemplateRootPath templateRootPath : templateRootPaths) {
				if (isAltExprSyntaxEnabled(templateRootPath)) {
					reparseTemplatesFor(templateRootPath, dataModelProject, null);
				}
			}
		}

	}

	@Override
	public boolean didChangeWatchedFile(Path filePath, Set<FileChangeType> changeTypes,
			ProjectExtensionContext context) {
		boolean dotQuteChanged = handleDotQuteFileChange(filePath);
		boolean applicationPropertiesChanged = handleApplicationPropertiesChange(filePath, changeTypes, context);
		return dotQuteChanged || applicationPropertiesChanged;
	}

	/**
	 * Checks if alternative expression syntax is enabled for a template root path.
	 * <p>
	 * If the template root path has a .qute file, uses its value. Otherwise,
	 * delegates to application.properties global configuration.
	 * </p>
	 *
	 * @param templateRootPath the template root path
	 * @return true if alt-expr-syntax is enabled
	 */
	private boolean isAltExprSyntaxEnabled(TemplateRootPath templateRootPath) {
		Boolean altExprSyntax = templateRootPath.getAltExprSyntax();
		if (altExprSyntax != null) {
			// .qute file exists, use its value
			return altExprSyntax;
		}
		// No .qute file, delegate to application.properties
		return currentAltExprSyntax;
	}

	/**
	 * Handles changes to .qute files.
	 *
	 * @param filePath the changed file path
	 * @return true if a .qute file was changed and templates were reparsed
	 */
	private boolean handleDotQuteFileChange(Path filePath) {
		String fileName = filePath.getName(filePath.getNameCount() - 1).toString();
		if (!".qute".equals(fileName) || templateRootPaths == null) {
			return false;
		}

		// Find which template root path contains this .qute file
		TemplateRootPath affectedTemplateRootPath = null;
		for (TemplateRootPath templateRootPath : templateRootPaths) {
			Path basePath = templateRootPath.getBasePath();
			if (basePath != null && filePath.equals(basePath.resolve(".qute"))) {
				affectedTemplateRootPath = templateRootPath;
				break;
			}
		}

		if (affectedTemplateRootPath == null) {
			return false;
		}

		// Store old value before reload
		boolean oldAltExprSyntax = isAltExprSyntaxEnabled(affectedTemplateRootPath);

		// Reload the .qute file
		dotQuteRegistry.didChangeWatchedFile(filePath, templateRootPaths);

		// Reparse templates if altExprSyntax changed
		boolean newAltExprSyntax = isAltExprSyntaxEnabled(affectedTemplateRootPath);
		if (oldAltExprSyntax != newAltExprSyntax) {
			var dataModelProject = getDataModelProject();
			if (dataModelProject != null) {
				reparseTemplatesFor(affectedTemplateRootPath, dataModelProject, null);
			}
		}

		return true;
	}

	/**
	 * Handles changes to application.properties files.
	 *
	 * @param filePath    the changed file path
	 * @param changeTypes the types of changes
	 * @param context
	 * @return true if application.properties was changed and templates were
	 *         reparsed
	 */
	private boolean handleApplicationPropertiesChange(Path filePath, Set<FileChangeType> changeTypes,
			ProjectExtensionContext context) {
		boolean applicationPropertiesChanged = applicationPropertiesRegistry.didChangeWatchedFile(filePath,
				allSourcePaths, changeTypes);
		if (!applicationPropertiesChanged) {
			return false;
		}

		var dataModelProject = getDataModelProject();
		if (dataModelProject == null) {
			return false;
		}

		boolean altExprSyntax = dataModelProject.getConfigAsBoolean(ALT_EXPR_SYNTAX);
		boolean altExprSyntaxChanged = currentAltExprSyntax != altExprSyntax;
		if (altExprSyntaxChanged) {
			currentAltExprSyntax = altExprSyntax;
			reparseTemplates(dataModelProject, context);
			return true;
		}

		return false;
	}

	@Override
	public void collectCodeLenses(Template template, SharedSettings settings, List<CodeLens> lenses,
			CancelChecker cancelChecker) {
		var dataModelProject = getDataModelProject();
		if (dataModelProject == null) {
			return;
		}
		QuteTextDocument document = dataModelProject.findDocumentFor(template.getTemplateId());
		if (document == null) {
			return;
		}
		if (document.isBinary()) {
			if (document.getExpressionCommand() != null) {
				addAlternativeCodeLens(null, lenses);
			}
		} else {
			TemplateRootPath templateRootPath = document.getTemplateRootPath();
			if (templateRootPath != null && templateRootPath.getAltExprSyntax() != null) {
				if (templateRootPath.getAltExprSyntax()) {
					addAlternativeCodeLens(templateRootPath.getBasePath().resolve(".qute").toUri().toASCIIString(),
							lenses);
				}
			} else {
				for (PropertiesFile propertiesFile : applicationPropertiesRegistry.getPropertiesFiles()) {
					String value = propertiesFile.getProperty(ALT_EXPR_SYNTAX.getName());
					if ("true".equals(value)) {
						addAlternativeCodeLens(propertiesFile.getPropertiesFile().toUri().toASCIIString(), lenses);
						break;
					}
				}
			}
		}
	}

	private void addAlternativeCodeLens(String applicationPropertiesOrDotQuteFileUri, List<CodeLens> lenses) {
		CodeLens codeLens = new CodeLens(QutePositionUtility.ZERO_RANGE);
		Command command = new Command("[Alternative]",
				applicationPropertiesOrDotQuteFileUri != null ? QuteClientCommandConstants.COMMAND_OPEN_URI : "");
		if (applicationPropertiesOrDotQuteFileUri != null) {
			command.setArguments(List.of(applicationPropertiesOrDotQuteFileUri));
		}
		codeLens.setCommand(command);
		lenses.add(codeLens);
	}

}
