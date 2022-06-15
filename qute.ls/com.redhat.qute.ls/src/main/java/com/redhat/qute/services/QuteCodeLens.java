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
import com.redhat.qute.commons.datamodel.DataModelTemplate;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.NodeKind;
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.SectionKind;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.project.QuteProject;
import com.redhat.qute.project.datamodel.ExtendedDataModelParameter;
import com.redhat.qute.project.datamodel.ExtendedDataModelTemplate;
import com.redhat.qute.project.datamodel.JavaDataModelCache;
import com.redhat.qute.settings.SharedSettings;
import com.redhat.qute.utils.QutePositionUtility;
import com.redhat.qute.utils.UserTagUtils;

/**
 * Qute code lens support.
 *
 * @author Angelo ZERR
 *
 */
class QuteCodeLens {

	private static final Range LEFT_TOP_RANGE = new Range(new Position(0, 0), new Position(0, 0));
	private final JavaDataModelCache javaCache;

	public QuteCodeLens(JavaDataModelCache javaCache) {
		this.javaCache = javaCache;
	}

	public CompletableFuture<List<? extends CodeLens>> getCodelens(Template template, SharedSettings settings,
			CancelChecker cancelChecker) {
		return javaCache.getDataModelTemplate(template) //
				.thenApply(templateDataModel -> {
					cancelChecker.checkCanceled();
					List<CodeLens> lenses = new ArrayList<>();

					// #insert code lens references
					QuteProject project = template.getProject();
					collectInsertCodeLenses(template, template, project, lenses, cancelChecker);

					// checked template code lenses
					collectDataModelCodeLenses(templateDataModel, template, settings, lenses, cancelChecker);

					if (UserTagUtils.isUserTag(template)) {
						// Template is an user tag
						collectUserTagCodeLenses(template, cancelChecker, lenses);
					}
					return lenses;
				});
	}

	private static void collectDataModelCodeLenses(ExtendedDataModelTemplate templateDataModel, Template template,
			SharedSettings settings, List<CodeLens> lenses, CancelChecker cancelChecker) {
		if (templateDataModel == null || templateDataModel.getSourceType() == null) {
			return;
		}

		cancelChecker.checkCanceled();

		String projectUri = template.getProjectUri();

		boolean canSupportJavaDefinition = settings.getCommandCapabilities()
				.isCommandSupported(COMMAND_JAVA_DEFINITION);

		// Method/Field which is bound with the template
		String title = createCheckedTemplateTitle(templateDataModel);
		Range range = LEFT_TOP_RANGE;
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

	private static String createCheckedTemplateTitle(DataModelTemplate<?> dataModel) {
		String className = dataModel.getSourceType();
		int index = className.lastIndexOf('.');
		className = className.substring(index + 1, className.length());
		StringBuilder title = new StringBuilder(className) //
				.append("#"); //
		if (dataModel.getSourceMethod() != null) {
			title.append(dataModel.getSourceMethod());
			title.append("(...)");
		} else {
			title.append(dataModel.getSourceField());
		}
		return title.toString();
	}

	private static void collectInsertCodeLenses(Node parent, Template template, QuteProject project,
			List<CodeLens> lenses, CancelChecker cancelChecker) {
		cancelChecker.checkCanceled();
		if (parent.getKind() == NodeKind.Section) {
			Section section = (Section) parent;
			if (section.getSectionKind() == SectionKind.INSERT) {

				if (project != null) {
					Parameter parameter = section.getParameterAtIndex(0);
					if (parameter != null) {
						String tag = parameter.getValue();
						// TODO : implement findNbreferencesOfInsertTag correctly
						int nbReferences = 0; // project.findNbreferencesOfInsertTag(template.getTemplateId(), tag);
						if (nbReferences > 0) {
							String title = nbReferences == 1 ? "1 reference" : nbReferences + " references";
							Range range = QutePositionUtility.createRange(parameter);
							Command command = new Command(title, "");
							CodeLens codeLens = new CodeLens(range, command, null);
							lenses.add(codeLens);
						}
					}
				}

			}

		}
		List<Node> children = parent.getChildren();
		for (Node node : children) {
			collectInsertCodeLenses(node, template, project, lenses, cancelChecker);
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
