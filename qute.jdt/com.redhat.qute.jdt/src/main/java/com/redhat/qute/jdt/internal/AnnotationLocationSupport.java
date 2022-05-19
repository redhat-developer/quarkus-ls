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
package com.redhat.qute.jdt.internal;

import static com.redhat.qute.jdt.internal.QuteJavaConstants.LOCATION_ANNOTATION;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;

import com.redhat.qute.jdt.utils.AnnotationUtils;

/**
 * The @Location support gives the capability to retrieve the @Location declared
 * in a parameter of constructor which assigns a Java field .
 * 
 * @author Angelo ZERR
 *
 */
public class AnnotationLocationSupport extends ASTVisitor {

	// the Map of assigned fields initialized by the parameters constructor.
	private final Map<String /* the field name */, SingleVariableDeclaration /*
																				 * the parameter constructor annotated
																				 * with @Location
																				 */> assignedFields;

	// Constructor parameters which have an @Location annotation
	// ex: public SomePage(@Location("foo/bar/page.qute.html") Template page)
	private Set<SingleVariableDeclaration> constructorParametersAnnotatedWithLocation;

	public AnnotationLocationSupport(CompilationUnit root) {
		assignedFields = new HashMap<>();
		root.accept(this);
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		boolean visitBody = false;
		if (node.isConstructor()) {
			@SuppressWarnings("rawtypes")
			List parameters = node.parameters();
			for (Object parameter : parameters) {
				if (parameter instanceof SingleVariableDeclaration) {
					SingleVariableDeclaration variable = (SingleVariableDeclaration) parameter;
					if (getLocationExpression(variable, variable.modifiers()) != null) {
						// The current constructor parameter has @Location annotation
						// SomePage(@Location("foo/bar/page.qute.html") Template page)
						if (constructorParametersAnnotatedWithLocation == null) {
							constructorParametersAnnotatedWithLocation = new HashSet<>();
						}
						constructorParametersAnnotatedWithLocation.add(variable);
						visitBody = true;
					}
				}
			}
		}
		// There is one parameter of the constructor which is annotated with @Location
		// visit the constructor body to fill the assigned fields.
		return visitBody;
	}

	@Override
	public boolean visit(Assignment node) {
		// Visit the body of the constructor to find the assigned fields with
		// constructor parameters.
		Expression left = node.getLeftHandSide();
		Expression right = node.getRightHandSide();
		if (left == null || right == null) {
			return false;
		}

		FieldAccess fieldAccess = (left instanceof FieldAccess) ? (FieldAccess) left : null;
		if (fieldAccess == null) {
			return false;
		}
		// Here we have a field access:
		// private Template page;
		// ...
		// public SomePage(...)
		// this.page = ... // here we have a field access

		if (right instanceof SimpleName) {
			// public SomePage(Template page)
			// this.page = page;
			SingleVariableDeclaration variable = getMatchedParameter(((SimpleName) right),
					constructorParametersAnnotatedWithLocation);
			if (variable != null) {
				assignedFields.put(fieldAccess.getName().getIdentifier(), variable);
			}
		} else if (right instanceof MethodInvocation) {
			// public SomePage(Template page)
			// this.page = requireNonNull(page, "page is required");
			MethodInvocation methodInvocation = (MethodInvocation) right;
			SingleVariableDeclaration variable = getMatchedParameter(methodInvocation,
					constructorParametersAnnotatedWithLocation);
			if (variable != null) {
				assignedFields.put(fieldAccess.getName().getIdentifier(), variable);
			}
		}
		return super.visit(node);
	}

	private static SingleVariableDeclaration getMatchedParameter(SimpleName simpleName,
			Set<SingleVariableDeclaration> constructorParametersAnnotatedWithLocation) {
		String name = simpleName.getIdentifier();
		for (SingleVariableDeclaration variable : constructorParametersAnnotatedWithLocation) {
			if (variable.getName().getIdentifier().equals(name)) {
				return variable;
			}
		}
		return null;
	}

	private static SingleVariableDeclaration getMatchedParameter(MethodInvocation methodInvocation,
			Set<SingleVariableDeclaration> constructorParametersAnnotatedWithLocation) {
		for (Object arg : methodInvocation.arguments()) {
			if (arg instanceof SimpleName) {
				SingleVariableDeclaration variable = getMatchedParameter((SimpleName) arg,
						constructorParametersAnnotatedWithLocation);
				if (variable != null) {
					return variable;
				}
			}
		}
		return null;
	}

	/**
	 * Returns the @Location expression defined in the constructor parameter which
	 * initializes the given field name and null otherwise.
	 * 
	 * @param fieldName the Java field name.
	 * 
	 * @return the @Location expression defined in the constructor parameter which
	 *         initializes the given field name and null otherwise.
	 */
	public StringLiteral getLocationExpressionFromConstructorParameter(String fieldName) {
		SingleVariableDeclaration variable = assignedFields.get(fieldName);
		if (variable == null) {
			return null;
		}
		return getLocationExpression(variable, variable.modifiers());
	}

	/**
	 * Returns the @Location expression annotated for the given AST node field.
	 * 
	 * @param node      the AST node field.
	 * 
	 * @param modifiers the AST node modifiers.
	 * 
	 * @return the @Location expression annotated for the given AST node field.
	 */
	public static StringLiteral getLocationExpression(ASTNode node, @SuppressWarnings("rawtypes") List modifiers) {
		if (modifiers == null || modifiers.isEmpty()) {
			return null;
		}
		for (Object modifier : modifiers) {
			if (modifier instanceof SingleMemberAnnotation) {
				SingleMemberAnnotation annotation = (SingleMemberAnnotation) modifier;
				if (AnnotationUtils.isMatchAnnotation(annotation, LOCATION_ANNOTATION)) {
					// @Location("/items/my.items.qute.html")
					// Template items;
					Expression expression = annotation.getValue();
					if (expression != null && expression instanceof StringLiteral) {
						String location = ((StringLiteral) expression).getLiteralValue();
						if (StringUtils.isNotBlank(location)) {
							return (StringLiteral) expression;
						}
					}
				}
			}
		}
		return null;
	}
}
