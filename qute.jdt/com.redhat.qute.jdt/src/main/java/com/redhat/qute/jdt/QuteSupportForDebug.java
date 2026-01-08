/*******************************************************************************
 * Copyright (c) 2026 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package com.redhat.qute.jdt;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import com.redhat.qute.jdt.debug.JavaSourceLocationArguments;
import com.redhat.qute.jdt.debug.JavaSourceLocationResponse;
import com.redhat.qute.jdt.utils.IJDTUtils;

/**
 * JDT-side implementation for resolving Java source locations referenced from
 * Qute templates.
 *
 * Supports: - type resolution - method resolution - annotation-based template
 * resolution (@TemplateContents) - text blocks (""" """)
 */
public class QuteSupportForDebug {

	private static final Logger LOGGER = Logger.getLogger(QuteSupportForDebug.class.getName());

	private static final QuteSupportForDebug INSTANCE = new QuteSupportForDebug();

	public static QuteSupportForDebug getInstance() {
		return INSTANCE;
	}

	public JavaSourceLocationResponse resolveJavaSource(JavaSourceLocationArguments args, IJDTUtils utils,
			IProgressMonitor monitor) {

		try {

			IType type = findType(args.getTypeName(), monitor);
			if (type == null) {
				return null;
			}

			ICompilationUnit cu = type.getCompilationUnit();
			if (cu == null) {
				return null;
			}

			String javaFileUri = utils.toUri(cu);

			// 1️⃣ Annotation-based template (highest priority)
			if (args.getAnnotation() != null) {
				IAnnotation annotation = findAnnotation(type, args.getAnnotation());

				if (annotation != null) {
					int offset = computeTemplateContentOffset(cu, annotation);
					int startLine = computeLine(cu, offset);
					return new JavaSourceLocationResponse(javaFileUri, startLine);
				}
			}

			// 2️⃣ Method-based resolution
			if (args.getMethod() != null) {
				IMethod method = findMethod(type, args.getMethod());
				if (method != null) {
					int offset = method.getNameRange().getOffset();
					int startLine = computeLine(cu, offset);
					return new JavaSourceLocationResponse(javaFileUri, startLine);
				}
			}
			return null;

		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Failed to resolve Java source location", e);
		}

		return null;
	}

	/* ---------------------------------------------------------------------- */
	/* Resolution helpers */
	/* ---------------------------------------------------------------------- */

	private static IType findType(String className, IProgressMonitor monitor) {
		try {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

			for (IProject project : root.getProjects()) {
				if (!project.isOpen() || !project.hasNature(JavaCore.NATURE_ID)) {
					continue;
				}

				IJavaProject javaProject = JavaCore.create(project);

				IType type = javaProject.findType(className, monitor);
				if (type != null) {
					return type;
				}

				// Fallback java.lang
				if (!className.contains(".")) {
					type = javaProject.findType("java.lang." + className, monitor);
					if (type != null) {
						return type;
					}
				}
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error while finding type '" + className + "'", e);
		}
		return null;
	}

	private static IMethod findMethod(IType type, String methodName) throws JavaModelException {

		for (IMethod method : type.getMethods()) {
			if (method.getElementName().equals(methodName)) {
				return method;
			}
		}
		return null;
	}

	private static IAnnotation findAnnotation(IType type, String annotationFqn) throws JavaModelException {

		String simpleName = simpleName(annotationFqn);

		for (IAnnotation ann : type.getAnnotations()) {
			if (ann.getElementName().equals(simpleName)) {
				return ann;
			}
		}
		return null;
	}

	private static String simpleName(String fqn) {
		int idx = fqn.lastIndexOf('.');
		return idx == -1 ? fqn : fqn.substring(idx + 1);
	}

	/* ---------------------------------------------------------------------- */
	/* Offset & line computation */
	/* ---------------------------------------------------------------------- */

	private static int computeTemplateContentOffset(ICompilationUnit cu, IAnnotation annotation)
			throws JavaModelException {

		ISourceRange range = annotation.getSourceRange();
		String source = cu.getSource();

		int start = range.getOffset();
		int end = start + range.getLength();

		String annotationSource = source.substring(start, end);

		int paren = annotationSource.indexOf('(');
		if (paren == -1) {
			return start;
		}

		int offset = start + paren + 1;

		// Skip whitespace
		while (offset < source.length() && Character.isWhitespace(source.charAt(offset))) {
			offset++;
		}

		// Text block
		if (source.startsWith("\"\"\"", offset)) {
			return offset + 3;
		}

		// Classic string
		if (source.charAt(offset) == '"') {
			return offset + 1;
		}

		return offset;
	}

	private static int computeLine(ICompilationUnit cu, int offset) throws JavaModelException {

		CompilationUnit ast = parse(cu);
		return ast.getLineNumber(offset) - 1; // DAP is 0-based
	}

	private static CompilationUnit parse(ICompilationUnit cu) {
		ASTParser parser = ASTParser.newParser(AST.JLS_Latest);
		parser.setSource(cu);
		parser.setResolveBindings(false);
		return (CompilationUnit) parser.createAST(null);
	}
}
