/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.services;

import static com.redhat.qute.services.commands.QuteClientCommandConstants.COMMAND_JAVA_DEFINITION;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.commons.JavaElementInfo;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.NodeKind;
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.parser.template.sections.FragmentSection;
import com.redhat.qute.project.QuteProject;
import com.redhat.qute.project.QuteProjectRegistry;
import com.redhat.qute.project.datamodel.DataModelSourceProvider;
import com.redhat.qute.project.datamodel.ExtendedDataModelFragment;
import com.redhat.qute.project.datamodel.ExtendedDataModelParameter;
import com.redhat.qute.project.datamodel.ExtendedDataModelTemplate;
import com.redhat.qute.services.commands.QuteClientCommandConstants;
import com.redhat.qute.settings.SharedSettings;
import com.redhat.qute.utils.QutePositionUtility;
import com.redhat.qute.utils.StringUtils;
import com.redhat.qute.utils.UserTagUtils;

/**
 * Qute code lens support.
 *
 * @author Angelo ZERR
 *
 */
class QuteCodeLens {

	private static final Range LEFT_TOP_RANGE = new Range(new Position(0, 0), new Position(0, 0));
	private final QuteProjectRegistry projectRegistry;

	public QuteCodeLens(QuteProjectRegistry projectRegistry) {
		this.projectRegistry = projectRegistry;
	}

	public CompletableFuture<List<? extends CodeLens>> getCodelens(Template template, SharedSettings settings,
			CancelChecker cancelChecker) {
		return projectRegistry.getDataModelTemplate(template) //
				.thenApply(templateDataModel -> {
					cancelChecker.checkCanceled();
					List<CodeLens> lenses = new ArrayList<>();

					// Collect checked template code lenses at the root of the template
					collectDataModelFoTemplateCodeLenses(templateDataModel, template, settings, lenses, cancelChecker);

					// Visit AST template to collect codelenses:
					// * for #insert
					// * for #fragment
					QuteProject project = template.getProject();
					collectCodeLenses(templateDataModel, template, template, settings, project, lenses,
							cancelChecker);

					if (UserTagUtils.isUserTag(template)) {
						// Template is an user tag
						collectUserTagCodeLenses(template, cancelChecker, lenses);
					}
					return lenses;
				});
	}

	private static void collectDataModelFoTemplateCodeLenses(DataModelSourceProvider templateDataModel,
			Template template, SharedSettings settings, List<CodeLens> lenses, CancelChecker cancelChecker) {
		collectDataModelCodeLenses(LEFT_TOP_RANGE, templateDataModel, template.getProjectUri(), settings, lenses,
				cancelChecker);
	}

	private static void collectDataModelCodeLenses(Range range, DataModelSourceProvider templateDataModel,
			String projectUri, SharedSettings settings, List<CodeLens> lenses, CancelChecker cancelChecker) {
		if (templateDataModel == null || templateDataModel.getSourceType() == null) {
			return;
		}

		cancelChecker.checkCanceled();

		boolean canSupportJavaDefinition = settings.getCommandCapabilities()
				.isCommandSupported(COMMAND_JAVA_DEFINITION);

		// Method/Field which is bound with the template
		String title = createCheckedTemplateTitle(templateDataModel);
		Command command = !canSupportJavaDefinition ? new Command(title, "")
				: new Command(title, COMMAND_JAVA_DEFINITION,
						Arrays.asList(templateDataModel.toJavaDefinitionParams(projectUri)));
		CodeLens codeLens = new CodeLens(range, command, null);
		lenses.add(codeLens);

		// Parameters of the template
		List<ExtendedDataModelParameter> parameters = templateDataModel.getParameters();
		if (parameters != null) {
			for (ExtendedDataModelParameter parameter : parameters) {
				String parameterTitle = createParameterTitle(parameter);
				Command parameterCommand = !canSupportJavaDefinition ? new Command(title, "")
						: new Command(parameterTitle, COMMAND_JAVA_DEFINITION,
								Arrays.asList(parameter.toJavaDefinitionParams(projectUri)));
				CodeLens parameterCodeLens = new CodeLens(range, parameterCommand, null);
				lenses.add(parameterCodeLens);
			}
		}
	}

	private static String createParameterTitle(ExtendedDataModelParameter parameter) {
		StringBuilder title = new StringBuilder();
		title.append(parameter.getKey());
		title.append(" : ");
		title.append(JavaElementInfo.getSimpleType(parameter.getSourceType()));
		return title.toString();
	}

	private static String createCheckedTemplateTitle(DataModelSourceProvider dataModel) {
		String className = dataModel.getSourceType();
		int index = className.lastIndexOf('.');
		className = className.substring(index + 1, className.length());
		StringBuilder title = new StringBuilder(className);
		boolean hasHash = dataModel.getSourceMethod() != null || dataModel.getSourceField() != null;
		if (hasHash) {
			title.append("#");
			if (dataModel.getSourceMethod() != null) {
				title.append(dataModel.getSourceMethod());
				title.append("(...)");
			} else {
				title.append(dataModel.getSourceField());
			}
		}
		return title.toString();
	}

	private static void collectCodeLenses(ExtendedDataModelTemplate templateDataModel, Node parent,
			Template template, SharedSettings settings, QuteProject project,
			List<CodeLens> lenses, CancelChecker cancelChecker) {
		cancelChecker.checkCanceled();
		String showReferencesCommandId = settings.getCommandCapabilities()
				.isCommandSupported(QuteClientCommandConstants.COMMAND_SHOW_REFERENCES)
						? QuteClientCommandConstants.COMMAND_SHOW_REFERENCES
						: "";
		if (parent.getKind() == NodeKind.Section) {
			Section section = (Section) parent;
			switch (section.getSectionKind()) {
				case INSERT:
					collectInsertCodeLens(project, section, template, showReferencesCommandId, lenses);
					break;
				case FRAGMENT:
					collectFragmentCodeLens(templateDataModel, section, settings, project, lenses, cancelChecker);
					break;
				default:
			}
		}
		List<Node> children = parent.getChildren();
		for (Node node : children) {
			collectCodeLenses(templateDataModel, node, template, settings, project, lenses, cancelChecker);
		}
	}

	private static void collectFragmentCodeLens(ExtendedDataModelTemplate templateDataModel, Section section,
			SharedSettings settings, QuteProject project, List<CodeLens> lenses, CancelChecker cancelChecker) {
		if (templateDataModel == null) {
			return;
		}
		FragmentSection fragment = (FragmentSection) section;
		String fragmentId = fragment.getId();
		if (StringUtils.isEmpty(fragmentId)) {
			return;
		}
		ExtendedDataModelFragment fragmentDataModel = (ExtendedDataModelFragment) templateDataModel
				.getFragment(fragmentId);
		if (fragmentDataModel == null) {
			return;
		}
		Range range = QutePositionUtility.selectStartTagName(section);
		collectDataModelCodeLenses(range, fragmentDataModel, project.getUri(), settings, lenses,
				cancelChecker);
	}

	private static void collectInsertCodeLens(QuteProject project, Section section, Template template,
			String showReferencesCommandId, List<CodeLens> lenses) {
		if (project != null) {
			Parameter parameter = section.getParameterAtIndex(0);
			if (parameter != null) {
				String tag = parameter.getValue();
				int nbReferences = project.findSectionsByTag(tag).size();
				if (nbReferences > 0) {
					String title = nbReferences == 1 ? "1 reference" : nbReferences + " references";
					Range range = QutePositionUtility.createRange(parameter);
					Command command = new Command(title, showReferencesCommandId);
					if (!showReferencesCommandId.isEmpty()) {
						String uri = template.getUri();
						command.setArguments(Arrays.asList(uri, range.getStart()));
					}
					CodeLens codeLens = new CodeLens(range, command, null);
					lenses.add(codeLens);
				}
			}
		}
	}

	private static void collectUserTagCodeLenses(Template template, CancelChecker cancelChecker,
			List<CodeLens> lenses) {
		// 1) display the user tag name as codelens
		String userTagTitle = "User tag #" + UserTagUtils.getUserTagName(template.getUri());
		Command userTagCommand = new Command(userTagTitle, "");
		CodeLens userTagCodeLens = new CodeLens(LEFT_TOP_RANGE, userTagCommand, null);
		lenses.add(userTagCodeLens);

		// 2) display a codelens for each expression object part
		UserTagUtils.collectUserTagParameters(template, //
				objectPart -> {
					String title = objectPart.getPartName();
					Range range = LEFT_TOP_RANGE;
					Command command = new Command(title, "");
					CodeLens codeLens = new CodeLens(range, command, null);
					lenses.add(codeLens);
				}, cancelChecker);
	}
}