/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.services.codeactions;

import static com.redhat.qute.ls.commons.CodeActionFactory.insert;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;

import com.redhat.qute.ls.commons.BadLocationException;
import com.redhat.qute.ls.commons.CodeActionFactory;
import com.redhat.qute.ls.commons.TextDocument;
import com.redhat.qute.parser.expression.ObjectPart;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.NodeKind;
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.ParameterDeclaration;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.SectionKind;
import com.redhat.qute.parser.template.SectionMetadata;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.parser.template.sections.LoopSection;
import com.redhat.qute.project.QuteProject;
import com.redhat.qute.project.datamodel.ExtendedDataModelParameter;
import com.redhat.qute.project.datamodel.ExtendedDataModelTemplate;
import com.redhat.qute.project.datamodel.resolvers.ValueResolver;
import com.redhat.qute.services.diagnostics.QuteErrorCode;
import com.redhat.qute.utils.StringUtils;
import com.redhat.qute.utils.UserTagUtils;

/**
 * Code actions for {@link QuteErrorCode#UndefinedObject}.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteCodeActionForUndefinedObject extends AbstractQuteCodeAction {

	private static final Logger LOGGER = Logger.getLogger(QuteCodeActionForUndefinedObject.class.getName());

	private static final String UNDEFINED_OBJECT_CODEACTION_TITLE = "Declare `{0}` with parameter declaration.";

	private static final String UNDEFINED_OBJECT_SEVERITY_SETTING = "qute.validation.undefinedObject.severity";

	@Override
	public void doCodeActions(CodeActionRequest request, List<CompletableFuture<Void>> codeActionResolveFutures,
			List<CodeAction> codeActions) {
		try {
			Node node = request.getCoveredNode();
			if (node == null) {
				return;
			}
			ObjectPart part = (ObjectPart) node;
			Template template = request.getTemplate();
			Diagnostic diagnostic = request.getDiagnostic();

			// CodeAction(s) to replace text with similar suggestions
			doCodeActionsForSimilarValues(part, template, diagnostic, codeActions);

			// CodeAction to insert parameter declaration {@java.lang.String name}
			doCodeActionToInsertParameterDeclaration(part, template, diagnostic, codeActions);

			// CodeAction to append ?? to object to make it optional
			doCodeActionToAddOptionalSuffix(template, diagnostic, codeActions);

			// CodeAction to set validation severity to ignore
			doCodeActionToSetIgnoreSeverity(template, diagnostic, QuteErrorCode.UndefinedObject, codeActions,
					UNDEFINED_OBJECT_SEVERITY_SETTING);

		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "Creation of undefined object code action failed", e);
		}

	}

	private void doCodeActionsForSimilarValues(ObjectPart part, Template template, Diagnostic diagnostic,
			List<CodeAction> codeActions) throws BadLocationException {
		int offset = template.offsetAt(diagnostic.getRange().getStart());
		QuteProject project = template.getProject();
		if (project == null) {
			return;
		}
		Set<String> existingProperties = new HashSet<>();

		Section section = part != null ? part.getParentSection() : null;
		if (section != null) {
			if (section.getKind() == NodeKind.Section) {
				boolean collect = true;
				if (section.getSectionKind() == SectionKind.FOR || section.getSectionKind() == SectionKind.EACH) {
					LoopSection iterableSection = ((LoopSection) section);
					if (iterableSection.isInElseBlock(offset)) {
						// Completion is triggered after a #else inside a #for, we don't provide
						// completion for metadata or aliases
						collect = false;
					} else {
						// Completion for iterable section like #each, #for
						String alias = iterableSection.getAlias();
						if (!StringUtils.isEmpty(alias)) {
							doCodeActionsForSimilarValue(part, alias, template, existingProperties, diagnostic,
									codeActions);
						}
					}
				}
				if (collect) {
					List<SectionMetadata> metadatas = section.getMetadata();
					for (SectionMetadata metadata : metadatas) {
						doCodeActionsForSimilarValue(part, metadata.getName(), template, existingProperties, diagnostic,
								codeActions);
					}
				}
			}

			switch (section.getSectionKind()) {
				case LET:
				case SET: {
					// completion for parameters coming from #let, #set
					List<Parameter> parameters = section.getParameters();
					if (parameters != null) {
						for (Parameter parameter : parameters) {
							String parameterName = parameter.getName();
							doCodeActionsForSimilarValue(part, parameterName, template, existingProperties, diagnostic,
									codeActions);
						}
					}
					break;
				}
				case IF: {
					// completion for parameters coming from #if
					List<Parameter> parameters = section.getParameters();
					if (parameters != null) {
						for (Parameter parameter : parameters) {
							if (parameter.isOptional()) {
								// {#if foo??}
								String parameterName = parameter.getName();
								doCodeActionsForSimilarValue(part, parameterName, template, existingProperties,
										diagnostic,
										codeActions);
							}
						}
					}
					break;
				}
				default:
			}
		}

		List<ValueResolver> globalResolvers = project.getGlobalVariables().getNow(null);
		if (globalResolvers != null) {
			for (ValueResolver resolver : globalResolvers) {
				doCodeActionsForSimilarValue(part, resolver.getName(), template, existingProperties, diagnostic,
						codeActions);
			}
		}

		List<String> aliases = template.getChildren().stream() //
				.filter(n -> n.getKind() == NodeKind.ParameterDeclaration) //
				.map(n -> ((ParameterDeclaration) n).getAlias()) //
				.filter(alias -> alias != null) //
				.collect(Collectors.toList());
		for (String alias : aliases) {
			doCodeActionsForSimilarValue(part, alias, template, existingProperties, diagnostic, codeActions);
		}

		ExtendedDataModelTemplate dataModel = project.getDataModelTemplate(template).getNow(null);
		if (dataModel != null) {
			for (ExtendedDataModelParameter parameter : dataModel.getParameters()) {
				doCodeActionsForSimilarValue(part, parameter.getKey(), template, existingProperties, diagnostic,
						codeActions);
			}
		}

		if (UserTagUtils.isUserTag(template)) {
			Collection<SectionMetadata> specialKeysMetadatas = UserTagUtils.getSpecialKeys();
			for (SectionMetadata metadata : specialKeysMetadatas) {
				String name = metadata.getName();
				doCodeActionsForSimilarValue(part, name, template, existingProperties, diagnostic, codeActions);
			}
		}

	}

	private static void doCodeActionToInsertParameterDeclaration(ObjectPart part, Template template,
			Diagnostic diagnostic, List<CodeAction> codeActions) throws BadLocationException {
		String partName = part.getPartName();
		TextDocument document = template.getTextDocument();
		String lineDelimiter = document.lineDelimiter(0);
		String title = MessageFormat.format(UNDEFINED_OBJECT_CODEACTION_TITLE, partName);
		Position position = new Position(0, 0);

		// Check if object part belongs to an iterable section like #each or #list
		// ex : {#list item in items}
		// here items must be a java.util.list.
		Section ownerSection = part.getParent().getParent().getOwnerSection();
		boolean isIterable = ownerSection != null && ownerSection.isIterable();

		StringBuilder insertText = new StringBuilder("{@");
		if (isIterable) {
			insertText.append("java.util.List");
		} else {
			insertText.append("java.lang.String");
		}
		insertText.append(" ");
		insertText.append(partName);
		insertText.append("}");
		insertText.append(lineDelimiter);

		CodeAction insertParameterDeclarationQuickFix = CodeActionFactory.insert(title, position, insertText.toString(),
				document, diagnostic);
		codeActions.add(insertParameterDeclarationQuickFix);
	}

	/**
	 * CodeAction to append ?? to object to make it optional
	 *
	 * @param template    the Qute template
	 * @param diagnostic  the UndefinedVariable diagnostic
	 * @param codeActions list of CodeActions
	 * @throws BadLocationException
	 */
	private static void doCodeActionToAddOptionalSuffix(Template template, Diagnostic diagnostic,
			List<CodeAction> codeActions) throws BadLocationException {
		Position objectEnd = diagnostic.getRange().getEnd();
		int diagnosticStartOffset = template.offsetAt(diagnostic.getRange().getStart());
		int diagnosticEndOffset = template.offsetAt(objectEnd);
		String title = MessageFormat.format("Append ?? to undefined object `{0}`",
				template.getText(diagnosticStartOffset, diagnosticEndOffset));
		CodeAction appendOptionalSuffix = insert(title, objectEnd, "??", template.getTextDocument(), diagnostic);
		codeActions.add(appendOptionalSuffix);
	}

}
