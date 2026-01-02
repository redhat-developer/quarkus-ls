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
package com.redhat.qute.services.inlayhint;

import static com.redhat.qute.services.commands.QuteClientCommandConstants.COMMAND_JAVA_DEFINITION;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.InlayHint;
import org.eclipse.lsp4j.InlayHintKind;
import org.eclipse.lsp4j.InlayHintLabelPart;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import com.redhat.qute.commons.QuteJavaDefinitionParams;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.parser.expression.Part;
import com.redhat.qute.parser.template.ASTVisitor;
import com.redhat.qute.parser.template.Expression;
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.parser.template.sections.CustomSection;
import com.redhat.qute.parser.template.sections.ForSection;
import com.redhat.qute.parser.template.sections.IfSection;
import com.redhat.qute.parser.template.sections.LetSection;
import com.redhat.qute.parser.template.sections.SetSection;
import com.redhat.qute.project.QuteProject;
import com.redhat.qute.project.datamodel.resolvers.MessageValueResolver;
import com.redhat.qute.project.extensions.InlayHintParticipant;
import com.redhat.qute.project.tags.UserTag;
import com.redhat.qute.project.tags.UserTagParameter;
import com.redhat.qute.services.QuteCompletableFutures;
import com.redhat.qute.services.ResolvingJavaTypeContext;
import com.redhat.qute.settings.QuteInlayHintSettings;
import com.redhat.qute.settings.SharedSettings;

/**
 * AST visitor used to show inferred Java type for section parameter as inlay
 * hint.
 *
 * @author Angelo ZERR
 *
 */
public class InlayHintASTVistor extends ASTVisitor {

	private static final String OPEN_JAVA_TYPE_MESSAGE = "Open `{0}` Java type.";

	private static final String EDIT_JAVA_MESSAGE_MESSAGE = "Edit `{0}` Java message.";

	private static final Logger LOGGER = Logger.getLogger(InlayHintASTVistor.class.getName());

	private final int startOffset;

	private final int endOffset;
	private final QuteInlayHintSettings inlayHintSettings;

	private final ResolvingJavaTypeContext resolvingJavaTypeContext;

	private final List<InlayHint> inlayHints;

	private boolean canSupportJavaDefinition;

	private CancelChecker cancelChecker;

	public InlayHintASTVistor(int startOffset, int endOffset, SharedSettings settings,
			ResolvingJavaTypeContext resolvingJavaTypeContext, CancelChecker cancelChecker) {
		this.startOffset = startOffset;
		this.endOffset = endOffset;
		this.inlayHintSettings = settings.getInlayHintSettings();
		this.resolvingJavaTypeContext = resolvingJavaTypeContext;
		this.inlayHints = new ArrayList<InlayHint>();
		this.cancelChecker = cancelChecker;
		canSupportJavaDefinition = settings.getCommandCapabilities().isCommandSupported(COMMAND_JAVA_DEFINITION);
	}

	public List<InlayHint> getInlayHints() {
		return inlayHints;
	}

	@Override
	public boolean visit(ForSection node) {
		cancelChecker.checkCanceled();

		if (!isAfterStartParameterVisible(node)) {
			return false;
		}
		if (inlayHintSettings.isShowSectionParameterType()) {
			// {#for item[:Item] in items}
			Parameter aliasParameter = node.getAliasParameter();
			if (aliasParameter != null) {
				Parameter iterableParameter = node.getIterableParameter();
				if (iterableParameter != null) {
					Template template = node.getOwnerTemplate();
					QuteProject project = template.getProject();
					createJavaTypeInlayHint(aliasParameter, iterableParameter, template, project);
				}
			}
		}
		return isAfterEndParameterVisible(node);
	}

	@Override
	public boolean visit(IfSection node) {
		cancelChecker.checkCanceled();

		if (!isAfterStartParameterVisible(node)) {
			return false;
		}
		if (inlayHintSettings.isShowSectionParameterType()) {
			List<Parameter> parameters = node.getParameters();
			for (Parameter parameter : parameters) {
				if (parameter.isOptional()) {
					// {#if foo??}
					Template template = node.getOwnerTemplate();
					QuteProject project = template.getProject();
					createJavaTypeInlayHint(parameter, template, project);
				}
			}
		}
		return isAfterEndParameterVisible(node);
	}

	@Override
	public boolean visit(LetSection node) {
		cancelChecker.checkCanceled();

		if (!isAfterStartParameterVisible(node)) {
			return false;
		}
		// {#let user[:User]=item.owner }
		createInlayHintParametersSection(node);
		return isAfterEndParameterVisible(node);
	}

	public boolean visit(SetSection node) {
		cancelChecker.checkCanceled();

		if (!isAfterStartParameterVisible(node)) {
			return false;
		}
		// {#set user[:User]=item.owner }
		createInlayHintParametersSection(node);
		return isAfterEndParameterVisible(node);
	}

	public boolean visit(CustomSection node) {
		cancelChecker.checkCanceled();

		if (!isAfterStartParameterVisible(node)) {
			return false;
		}
		// {#form id[:String]=item.id }
		// {#form item.id[:String] }
		createInlayHintParametersSection(node);
		return isAfterEndParameterVisible(node);
	}

	@Override
	public boolean visit(Expression node) {
		cancelChecker.checkCanceled();

		if (inlayHintSettings.isShowMessages()) {

			Template template = node.getOwnerTemplate();
			QuteProject project = template.getProject();
			if (project != null) {

				Part objectOrMethodPart = node.getObjectPart();
				if (objectOrMethodPart == null) {
					objectOrMethodPart = node.getMethodPart();
				}
				if (objectOrMethodPart != null) {
					String namespace = objectOrMethodPart.getNamespace();
					if (namespace != null) {
						String partName = objectOrMethodPart.getPartName();

						CompletableFuture<MessageValueResolver> messageFuture = project
								.findMessageValueResolver(namespace, partName);
						MessageValueResolver message = messageFuture.getNow(null);
						if (message != null) {
							// It is a message value resolver
							// {msg:hello}
							// {msg:hello_name('Lucie')}

							String messageContent = message.getMessage();
							if (messageContent != null) {
								try {
									// Display the message as inlay hint
									// {msg:hello} [Hello!]
									// {msg:hello_name('Lucie')} [Hello {name}!]
									InlayHint hint = new InlayHint();
									hint.setKind(InlayHintKind.Type);
									if (canSupportJavaDefinition) {
										// Clickable Message to edit it (ex : 'Hello {name}!')
										InlayHintLabelPart messagePart = new InlayHintLabelPart(messageContent);
										Command javaDefCommand = createEditJavaMessageCommand(messageContent,
												message.getSourceType(), message.getName(), project.getUri());
										messagePart.setCommand(javaDefCommand);
										messagePart.setTooltip(javaDefCommand.getTitle());
										hint.setLabel(Either.forRight(Arrays.asList(messagePart)));
									} else {
										hint.setLabel(Either.forLeft(messageContent));
									}
									Position position = template.positionAt(node.getEnd());
									hint.setPosition(position);
									inlayHints.add(hint);
								} catch (Exception e) {
									LOGGER.log(Level.SEVERE, "Error while creating inlay hint for message", e);
								}
							}
						}
					}
				}

				// ex: {m:application.index.subtitle}
				for (InlayHintParticipant inlayHintParticipant : project.getExtensions()) {
					if (inlayHintParticipant.isEnabled()) {
						inlayHintParticipant.inlayHint(node, inlayHints, cancelChecker);
					}
				}

			}
		}
		return super.visit(node);

	}

	private boolean isAfterStartParameterVisible(Section node) {
		if (startOffset == -1) {
			return true;
		}
		return node.getStart() >= startOffset;
	}

	private boolean isAfterEndParameterVisible(Section node) {
		if (endOffset == -1) {
			return true;
		}
		return node.getEndParametersOffset() < endOffset;
	}

	private void createInlayHintParametersSection(Section node) {
		boolean isShowSectionParameterType = inlayHintSettings.isShowSectionParameterType();
		boolean isShowSectionParameterDefaultValue = inlayHintSettings.isShowSectionParameterDefaultValue();
		if (!isShowSectionParameterType && !isShowSectionParameterDefaultValue) {
			return;
		}
		List<Parameter> parameters = node.getParameters();

		Template template = node.getOwnerTemplate();
		QuteProject project = template.getProject();

		// In case of the section is an user tag, collect all parameters which have a
		// default value (#let name?="foo")
		List<String> userTagParameterDefaultValues = null;
		UserTag userTag = isShowSectionParameterDefaultValue && project != null ? project.findUserTag(node.getTag())
				: null;
		if (userTag != null) {
			userTagParameterDefaultValues = userTag.getParameters().stream().filter(p -> p.getDefaultValue() != null)
					.map(p -> p.getName()).collect(Collectors.toList());

		}

		// Loop for declared section parameters
		for (Parameter parameter : parameters) {
			if (userTagParameterDefaultValues != null) {
				userTagParameterDefaultValues.remove(parameter.getName());
			}
			if (isShowSectionParameterType) {
				// {#let user[:User]=item.owner }
				// {#set user[:User]=item.owner }
				// {#form id[:String]=item.id }
				// {#form item.id[:String] }
				createJavaTypeInlayHint(parameter, template, project);
			}
		}

		if (userTag != null && userTagParameterDefaultValues != null) {
			// The section is an user tag and some default value parameters are not declared
			// Display them with inlay hint
			for (String name : userTagParameterDefaultValues) {
				UserTagParameter userTagParameter = userTag.findParameter(name);
				String defaultValue = userTagParameter.getDefaultValue();
				if (defaultValue != null) {
					// {#bundleScript [name="main.js"] /}
					createInlayHint(node, template, name, defaultValue);
				}
			}
		}
	}

	private void createInlayHint(Section node, Template template, String name, String defaultValue) {
		try {
			InlayHint hint = new InlayHint();
			hint.setKind(InlayHintKind.Parameter);
			hint.setLabel(Either.forLeft(name + "=" + defaultValue));
			hint.setPaddingRight(Boolean.TRUE);
			int end = node.getEndParametersOffset();
			Position position = template.positionAt(end);
			hint.setPosition(position);
			inlayHints.add(hint);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error while creating inlay hint for user tag default value parameter", e);
		}
	}

	private void createJavaTypeInlayHint(Parameter parameter, Template template, QuteProject project) {
		createJavaTypeInlayHint(parameter, parameter, template, project);
	}

	private void createJavaTypeInlayHint(Parameter parameter, Parameter javaTypeParameter, Template template,
			QuteProject project) {
		CompletableFuture<ResolvedJavaTypeInfo> javaTypeFuture = getJavaType(javaTypeParameter, project);
		if (javaTypeFuture == null) {
			return;
		}
		ResolvedJavaTypeInfo javaType = javaTypeFuture.getNow(QuteCompletableFutures.RESOLVING_JAVA_TYPE);
		if (QuteCompletableFutures.isResolvingJavaType(javaType)) {
			resolvingJavaTypeContext.add(javaTypeFuture);
			return;
		}
		if ((!parameter.isOptional() && javaType == null)) {
			return;
		}
		createInlayHint(parameter, javaType, template);
	}

	private void createInlayHint(Parameter parameter, ResolvedJavaTypeInfo javaType, Template template) {
		try {
			InlayHint hint = new InlayHint();
			hint.setKind(InlayHintKind.Type);
			// The Java type is resolved, display it as inlay hint
			updateJavaType(hint, javaType, template.getProjectUri());
			int end = parameter.hasValueAssigned() ? parameter.getEndName() : parameter.getEnd();
			Position position = template.positionAt(end);
			hint.setPosition(position);
			inlayHints.add(hint);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error while creating inlay hint for Java parameter type", e);
		}
	}

	private static CompletableFuture<ResolvedJavaTypeInfo> getJavaType(Parameter parameter, QuteProject project) {
		if (project == null) {
			return null;
		}
		Expression expression = parameter.getJavaTypeExpression();
		if (expression == null) {
			return null;
		}

		CompletableFuture<ResolvedJavaTypeInfo> javaType = null;
		String literalJavaType = expression.getLiteralJavaType();
		if (literalJavaType != null) {
			javaType = project.resolveJavaType(literalJavaType);
		} else {
			javaType = project.resolveJavaType(parameter);
		}

		Section section = parameter.getOwnerSection();
		if (section.isIterable()) {
			return javaType.thenCompose(javaTypeIterable -> {
				if (javaTypeIterable == null) {
					return CompletableFuture.completedFuture(null);
				}
				if (javaTypeIterable.isIterable()) {
					return project.resolveJavaType(javaTypeIterable.getIterableOf());
				}
				return CompletableFuture.completedFuture(null);
			});
		}
		return javaType;
	}

	private void updateJavaType(InlayHint hint, ResolvedJavaTypeInfo javaType, String projectUri) {
		String type = getLabel(javaType);
		if (javaType != null && canSupportJavaDefinition) {
			// first part : ":"
			InlayHintLabelPart separatorPart = new InlayHintLabelPart(":");
			// second part label : clickable Java type (ex : String)
			InlayHintLabelPart javaTypePart = new InlayHintLabelPart(type);
			Command javaDefCommand = createOpenJavaTypeCommand(javaType.getName(), projectUri);
			javaTypePart.setCommand(javaDefCommand);
			javaTypePart.setTooltip(javaDefCommand.getTitle());
			hint.setLabel(Either.forRight(Arrays.asList(separatorPart, javaTypePart)));
		} else {
			hint.setLabel(Either.forLeft(":" + type));
		}
	}

	public static Command createOpenJavaTypeCommand(String sourceType, String projectUri) {
		String title = MessageFormat.format(OPEN_JAVA_TYPE_MESSAGE, sourceType);
		return new Command(title, COMMAND_JAVA_DEFINITION,
				Arrays.asList(new QuteJavaDefinitionParams(sourceType, projectUri)));
	}

	public static Command createEditJavaMessageCommand(String messageContent, String sourceType, String sourceMethod,
			String projectUri) {
		String title = MessageFormat.format(EDIT_JAVA_MESSAGE_MESSAGE, messageContent);
		QuteJavaDefinitionParams params = new QuteJavaDefinitionParams(sourceType, projectUri);
		params.setSourceMethod(sourceMethod);
		return new Command(title, COMMAND_JAVA_DEFINITION, Arrays.asList(params));
	}

	private static String getLabel(ResolvedJavaTypeInfo javaType) {
		return javaType != null ? javaType.getJavaElementSimpleType() : "?";
	}
}
