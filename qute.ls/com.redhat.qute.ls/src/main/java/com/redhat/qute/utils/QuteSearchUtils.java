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
package com.redhat.qute.utils;

import java.util.List;
import java.util.function.BiConsumer;

import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.parser.expression.NamespacePart;
import com.redhat.qute.parser.expression.ObjectPart;
import com.redhat.qute.parser.template.Expression;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.NodeKind;
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.ParameterDeclaration;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.parser.template.sections.BaseWhenSection;
import com.redhat.qute.parser.template.sections.LoopSection;
import com.redhat.qute.parser.template.sections.WithSection;

public class QuteSearchUtils {

	private enum PartNameMatcher {
		ONLY_NAME, //
		ONLY_NAMESPACE, //
		BOTH
	}

	public static void searchDeclaredObject(ObjectPart part, BiConsumer<Node, Range> collector, boolean includeNode,
			CancelChecker cancelChecker) {
		if (includeNode) {
			Range range = QutePositionUtility.createRange(part);
			collector.accept(part, range);
		}
		String namespace = part.getNamespace();
		if (NamespacePart.DATA_NAMESPACE.equals(namespace)) {
			searchDeclaredObjectInParameterDeclaration(part, collector);
		} else {
			Parameter parameter = searchDeclaredObjectInParameter(part, cancelChecker);
			if (parameter != null) {
				Range targetRange = QutePositionUtility.selectParameterName(parameter);
				collector.accept(parameter, targetRange);
			} else {
				searchDeclaredObjectInParameterDeclaration(part, collector);
			}
		}
	}

	private static void searchDeclaredObjectInParameterDeclaration(ObjectPart part, BiConsumer<Node, Range> collector) {
		String partName = part.getPartName();
		ParameterDeclaration parameterDeclaration = part.getOwnerTemplate().findInParameterDeclarationByAlias(partName);
		if (parameterDeclaration != null) {
			Range targetRange = QutePositionUtility.selectAlias(parameterDeclaration);
			collector.accept(parameterDeclaration, targetRange);
		}
	}

	private static Parameter searchDeclaredObjectInParameter(ObjectPart part, CancelChecker cancelChecker) {
		String partName = part.getPartName();
		Node parent = part.getParentSection();
		while (parent != null) {
			if (parent.getKind() == NodeKind.Section) {
				Section section = (Section) parent;
				switch (section.getSectionKind()) {
				case EACH:
				case FOR:
					LoopSection iterableSection = (LoopSection) section;
					String alias = iterableSection.getAlias();
					if (partName.equals(alias)) {
						return iterableSection.getAliasParameter();
					}
					break;
				case LET:
				case SET:
					List<Parameter> parameters = section.getParameters();
					for (Parameter parameter : parameters) {
						if (partName.equals(parameter.getName())) {
							return parameter;
						}
					}
					break;
				default:
				}
			}
			parent = parent.getParent();
		}
		return null;
	}

	public static void searchReferencedObjects(Node node, int offset, BiConsumer<Node, Range> collector,
			boolean includeNode, CancelChecker cancelChecker) {
		switch (node.getKind()) {
		case ParameterDeclaration: {
			ParameterDeclaration parameterDeclaration = (ParameterDeclaration) node;
			if (parameterDeclaration.isInAlias(offset)) {
				String alias = parameterDeclaration.getAlias();
				if (includeNode) {
					Range range = QutePositionUtility.selectAlias(parameterDeclaration);
					collector.accept(parameterDeclaration, range);
				}
				searchReferencedObjects(alias, PartNameMatcher.BOTH, node, collector, cancelChecker);
			}
			break;
		}
		case Parameter: {
			Parameter parameter = (Parameter) node;
			if (includeNode) {
				Range range = QutePositionUtility.selectParameterName(parameter);
				collector.accept(parameter, range);
			}
			String alias = parameter.getName();
			searchReferencedObjects(alias, PartNameMatcher.ONLY_NAME, parameter.getParent(), collector, cancelChecker);
			break;
		}
		default:
			break;
		}
	}

	private static void searchReferencedObjects(String partName, PartNameMatcher matcher, Node owerNode,
			BiConsumer<Node, Range> collector, CancelChecker cancelChecker) {
		Template template = owerNode.getOwnerTemplate();
		Node parent = owerNode.getKind() == NodeKind.ParameterDeclaration ? template : owerNode;
		searchReferencedObjects(partName, matcher, parent, owerNode, collector, cancelChecker);
	}

	private static void searchReferencedObjects(String partName, PartNameMatcher matcher, Node parent, Node owerNode,
			BiConsumer<Node, Range> collector, CancelChecker cancelChecker) {
		if (parent != owerNode) {
			switch (parent.getKind()) {
			case Expression:
				Expression expression = (Expression) parent;
				tryToCollectObjectPart(partName, matcher, expression, collector);
				break;
			case Section:
				Section section = (Section) parent;
				switch (section.getSectionKind()) {
				case EACH:
				case FOR: {
					LoopSection loopSection = (LoopSection) parent;
					Parameter iterableParameter = loopSection.getIterableParameter();
					if (iterableParameter != null) {
						Expression parameterExpr = iterableParameter.getJavaTypeExpression();
						tryToCollectObjectPart(partName, matcher, parameterExpr, collector);
						Parameter aliasParameter = loopSection.getAliasParameter();
						if (aliasParameter != null && partName.equals(aliasParameter.getName())) {
							matcher = PartNameMatcher.ONLY_NAMESPACE;
						}
					}
					break;
				}
				case LET:
				case SET: {
					List<Parameter> parameters = section.getParameters();
					if (parameters != null) {
						for (Parameter parameter : parameters) {
							if (parameter != null && parameter.hasValueAssigned()) {
								Expression parameterExpr = parameter.getJavaTypeExpression();
								tryToCollectObjectPart(partName, matcher, parameterExpr, collector);
							}
						}
					}
					break;
				}
				case WITH: {
					Parameter parameter = ((WithSection) section).getObjectParameter();
					Expression parameterExpr = parameter.getJavaTypeExpression();
					tryToCollectObjectPart(partName, matcher, parameterExpr, collector);
					break;
				}
				case WHEN:
				case SWITCH: {
					Parameter parameter = ((BaseWhenSection) section).getValueParameter();
					Expression parameterExpr = parameter.getJavaTypeExpression();
					tryToCollectObjectPart(partName, matcher, parameterExpr, collector);
					break;
				}
				default:
					break;
				}
			default:
				break;
			}
		}

		List<Node> children = parent.getChildren();
		for (Node node : children) {
			searchReferencedObjects(partName, matcher, node, owerNode, collector, cancelChecker);
		}
	}

	private static void tryToCollectObjectPart(String partName, PartNameMatcher matcher, Expression parameterExpr,
			BiConsumer<Node, Range> collector) {
		ObjectPart objectPart = parameterExpr.getObjectPart();
		if (isMatch(objectPart, partName, matcher)) {
			Range range = QutePositionUtility.createRange(objectPart);
			collector.accept(objectPart, range);
		}
	}

	private static boolean isMatch(ObjectPart objectPart, String partName, PartNameMatcher matcher) {
		if (objectPart == null || !partName.equals(objectPart.getPartName())) {
			return false;
		}
		switch (matcher) {
		case BOTH:
			return true;
		case ONLY_NAME:
			return objectPart.getNamespace() == null;
		case ONLY_NAMESPACE:
			return NamespacePart.DATA_NAMESPACE.equals(objectPart.getNamespace());
		}
		return true;
	}
}
