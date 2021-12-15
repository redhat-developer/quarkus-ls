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
import com.redhat.qute.parser.template.JavaTypeInfoProvider;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.NodeKind;
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.ParameterDeclaration;
import com.redhat.qute.parser.template.RangeOffset;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.SectionKind;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.parser.template.sections.BaseWhenSection;
import com.redhat.qute.parser.template.sections.ElseSection;
import com.redhat.qute.parser.template.sections.LoopSection;
import com.redhat.qute.parser.template.sections.WithSection;

/**
 * Qute search utilities to search:
 * 
 * <ul>
 * <li>referenced object</li>
 * <li>declared object</li>
 * </ul>
 * 
 * @author Angelo ZERR
 *
 */
public class QuteSearchUtils {

	private enum PartNameMatcher {
		ONLY_NAME, //
		ONLY_NAMESPACE, //
		BOTH
	}

	// ------------------- Search declared object

	/**
	 * Search the declared object of the given object part.
	 * 
	 * @param part          the object part.
	 * @param collector     the collector used to collect node and range.
	 * @param includeNode   true if the origin part must be collected and false
	 *                      otherwise.
	 * @param cancelChecker the cancel checker.
	 */
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
					if (!iterableSection.isInElseBlock(part.getStart())) {
						String alias = iterableSection.getAlias();
						if (partName.equals(alias) || section.isMetadata(partName)) {
							// the part name matches
							// - the #for alias of the section (ex : item)
							// - or the metadata of the section (ex : item_count)
							return iterableSection.getAliasParameter();
						}
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

	// ------------------- Search referenced objects

	/**
	 * Search referenced object of the given node.
	 * 
	 * @param node          the origin node.
	 * @param offset        the offset.
	 * @param collector     the collector to collect node and range.
	 * @param includeNode   true if the origin node must be collected and false
	 *                      otherwise.
	 * @param cancelChecker the cancel checker.
	 */
	public static void searchReferencedObjects(Node node, int offset, BiConsumer<Node, Range> collector,
			boolean includeNode, CancelChecker cancelChecker) {
		switch (node.getKind()) {
		case ParameterDeclaration: {
			ParameterDeclaration parameterDeclaration = (ParameterDeclaration) node;
			if (parameterDeclaration.isInJavaTypeName(offset)) {
				// {@org.acme.It|em item}
				RangeOffset rangeOffset = parameterDeclaration.getJavaTypeNameRange(offset);
				if (rangeOffset != null) {
					Range range = QutePositionUtility.createRange(rangeOffset, node.getOwnerTemplate());
					collector.accept(parameterDeclaration, range);
				}
			} else if (parameterDeclaration.isInAlias(offset)) {
				// {@org.acme.Item it|em}
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

	private static void searchReferencedObjects(String partName, PartNameMatcher matcher, Node parent, Node ownerNode,
			BiConsumer<Node, Range> collector, CancelChecker cancelChecker) {
		if (parent != ownerNode) {
			switch (parent.getKind()) {
			case Expression:
				Expression expression = (Expression) parent;
				tryToCollectObjectPartOrParameter(partName, matcher, expression, ownerNode, collector);
				break;
			case Section:
				Section section = (Section) parent;
				switch (section.getSectionKind()) {
				case EACH:
				case FOR: {
					LoopSection iterableSection = (LoopSection) parent;
					Parameter iterableParameter = iterableSection.getIterableParameter();
					if (iterableParameter != null) {
						Expression parameterExpr = iterableParameter.getJavaTypeExpression();
						tryToCollectObjectPartOrParameter(partName, matcher, parameterExpr, ownerNode, collector);
						Parameter aliasParameter = iterableSection.getAliasParameter();
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
								tryToCollectObjectPartOrParameter(partName, matcher, parameterExpr, ownerNode,
										collector);
							}
						}
					}
					break;
				}
				case WITH: {
					Parameter parameter = ((WithSection) section).getObjectParameter();
					Expression parameterExpr = parameter.getJavaTypeExpression();
					tryToCollectObjectPartOrParameter(partName, matcher, parameterExpr, ownerNode, collector);
					break;
				}
				case WHEN:
				case SWITCH: {
					Parameter parameter = ((BaseWhenSection) section).getValueParameter();
					Expression parameterExpr = parameter.getJavaTypeExpression();
					tryToCollectObjectPartOrParameter(partName, matcher, parameterExpr, ownerNode, collector);
					break;
				}
				default:
					break;
				}
			default:
				break;
			}
		}

		Node stopWhenNode = null;
		if (ownerNode.getKind() == NodeKind.Section) {
			Section section = (Section) ownerNode;
			if (section.getSectionKind() == SectionKind.EACH || section.getSectionKind() == SectionKind.FOR) {
				stopWhenNode = ((LoopSection) section).getElseSection();
			}
		}
		
		List<Node> children = parent.getChildren();
		for (Node node : children) {
			if (node == stopWhenNode) {
				break;
			}
			searchReferencedObjects(partName, matcher, node, ownerNode, collector, cancelChecker);
		}
	}

	public static void tryToCollectObjectPartOrParameter(String partName, PartNameMatcher matcher,
			Expression expression, Node ownerNode, BiConsumer<Node, Range> collector) {
		// Check if the current expression references the given part name
		if (!tryToCollectObjectPart(partName, matcher, expression, collector)) {
			if (ownerNode.getKind() == NodeKind.Section) {
				Section onwerSection = (Section) ownerNode;
				// Check the current expression references a metadata of the section
				ObjectPart objectPart = expression.getObjectPart();
				if (objectPart != null) {
					JavaTypeInfoProvider metadata = onwerSection.getMetadata(objectPart.getPartName());
					if (metadata != null) {
						// Adjust the object part range with the given part name
						// Example if expression is like {item_count}
						// Range should be {|item|_count}
						Range range = QutePositionUtility.createRange(objectPart.getStart(),
								objectPart.getStart() + partName.length(), objectPart.getOwnerTemplate());
						collector.accept(objectPart, range);
					}
				}
			}
		}
	}

	private static boolean tryToCollectObjectPart(String partName, PartNameMatcher matcher, Expression parameterExpr,
			BiConsumer<Node, Range> collector) {
		ObjectPart objectPart = parameterExpr.getObjectPart();
		if (isMatch(objectPart, partName, matcher)) {
			Range range = QutePositionUtility.createRange(objectPart);
			collector.accept(objectPart, range);
			return true;
		}
		return false;
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
