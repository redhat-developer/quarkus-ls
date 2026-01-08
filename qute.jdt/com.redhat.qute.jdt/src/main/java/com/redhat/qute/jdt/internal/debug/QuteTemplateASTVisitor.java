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
package com.redhat.qute.jdt.internal.debug;

import java.util.ArrayDeque;
import java.util.Deque;

import org.eclipse.jdt.core.dom.*;

/**
 * Single ASTVisitor responsible for: - navigating to the correct (possibly
 * inner) type - optionally filtering by method - extracting the annotation
 * value expression - computing the correct start line
 */
public class QuteTemplateASTVisitor extends ASTVisitor {

	private final CompilationUnit cu;
	private final String[] typePath;
	private final String methodName;
	private final String annotationSimpleName;

	private final Deque<String> typeStack = new ArrayDeque<>();

	private boolean insideTargetType = false;
	private boolean insideTargetMethod = false;

	private int startLine = -1;

	public QuteTemplateASTVisitor(CompilationUnit cu, String[] typePath, String methodName, String annotationFqn) {

		this.cu = cu;
		this.typePath = typePath;
		this.methodName = methodName;
		this.annotationSimpleName = simpleName(annotationFqn);
	}

	public int getStartLine() {
		return startLine;
	}

	/* ---------------------------------------------------------------------- */
	/* Type handling (class + record + inner types) */
	/* ---------------------------------------------------------------------- */

	@Override
	public boolean visit(TypeDeclaration node) {
		return visitType(node.getName().getIdentifier());
	}

	@Override
	public boolean visit(RecordDeclaration node) {
		return visitType(node.getName().getIdentifier());
	}

	private boolean visitType(String name) {
		typeStack.push(name);
		insideTargetType = matchesTypePath();
		return true;
	}

	@Override
	public void endVisit(TypeDeclaration node) {
		endType();
	}

	@Override
	public void endVisit(RecordDeclaration node) {
		endType();
	}

	private void endType() {
		typeStack.pop();
		insideTargetType = matchesTypePath();
	}

	private boolean matchesTypePath() {
		if (typeStack.size() != typePath.length) {
			return false;
		}

		int i = typePath.length - 1;
		for (String s : typeStack) {
			if (!s.equals(typePath[i--])) {
				return false;
			}
		}
		return true;
	}

	/* ---------------------------------------------------------------------- */
	/* Method handling (optional) */
	/* ---------------------------------------------------------------------- */

	@Override
	public boolean visit(MethodDeclaration node) {
		if (!insideTargetType) {
			return false;
		}

		if (methodName == null || methodName.equals(node.getName().getIdentifier())) {
			insideTargetMethod = true;
			return true;
		}
		return false;
	}

	@Override
	public void endVisit(MethodDeclaration node) {
		insideTargetMethod = false;
	}

	/* ---------------------------------------------------------------------- */
	/* Annotation handling */
	/* ---------------------------------------------------------------------- */

	@Override
	public boolean visit(NormalAnnotation node) {
		handleAnnotation(node);
		return false;
	}

	@Override
	public boolean visit(SingleMemberAnnotation node) {
		handleAnnotation(node);
		return false;
	}

	private void handleAnnotation(Annotation node) {
		if (startLine != -1) {
			return;
		}

		if (!insideTargetType && !insideTargetMethod) {
			return;
		}

		if (!node.getTypeName().getFullyQualifiedName().equals(annotationSimpleName)) {
			return;
		}

		Expression value = extractValue(node);
		if (value == null) {
			return;
		}

		int line = cu.getLineNumber(value.getStartPosition()) - 1;

		// TextBlock starts at """ — move to first content line
		if (value instanceof TextBlock) {
			line += 1;
		}

		startLine = line;
	}

	private Expression extractValue(Annotation ann) {
		if (ann instanceof SingleMemberAnnotation sma) {
			return sma.getValue();
		}

		if (ann instanceof NormalAnnotation na) {
			for (Object o : na.values()) {
				MemberValuePair pair = (MemberValuePair) o;
				if ("value".equals(pair.getName().getIdentifier())) {
					return pair.getValue();
				}
			}
		}
		return null;
	}

	private static String simpleName(String fqn) {
		if (fqn == null) {
			return null;
		}
		int idx = fqn.lastIndexOf('.');
		return idx == -1 ? fqn : fqn.substring(idx + 1);
	}
}
