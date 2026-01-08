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
package com.redhat.qute.jdt;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import com.redhat.qute.jdt.debug.JavaSourceLocationArguments;
import com.redhat.qute.jdt.debug.JavaSourceLocationResponse;
import com.redhat.qute.jdt.internal.debug.QuteTemplateASTVisitor;
import com.redhat.qute.jdt.utils.IJDTUtils;

/**
 * AST-based implementation for resolving Java source locations referenced from
 * Qute templates.
 *
 * Supported: - annotation-based template resolution - StringLiteral and
 * TextBlock - class and record - inner types - optional method filtering
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
			LOGGER.log(Level.INFO, "Resolving Java source location of " + args.getJavaElementUri());
			IType type = findType(args.getTypeName(), monitor);
			if (type == null) {
				return null;
			}

			ICompilationUnit cu = type.getCompilationUnit();
			if (cu == null) {
				return null;
			}

			CompilationUnit ast = parse(cu);

			String[] typePath = splitTypePath(args.getTypeName());

			QuteTemplateASTVisitor visitor = new QuteTemplateASTVisitor(ast, typePath, args.getMethod(),
					args.getAnnotation());

			ast.accept(visitor);

			int startLine = visitor.getStartLine();
			if (startLine == -1) {
				return null;
			}

			String javaFileUri = utils.toUri(cu);
			LOGGER.log(Level.INFO, "Resolved Java source location of " + args.getJavaElementUri() + ", javaFileUri="
					+ javaFileUri + ", startLine=" + startLine);

			return new JavaSourceLocationResponse(javaFileUri, startLine);

		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Failed to resolve Java source location of " + args.getJavaElementUri(), e);
			return null;
		}
	}

	/* ---------------------------------------------------------------------- */
	/* Helpers */
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
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error while finding type " + className, e);
		}
		return null;
	}

	/**
	 * Removes the package and splits on both '.' and '$' to support inner types.
	 */
	private static String[] splitTypePath(String typeName) {
		int idx = typeName.lastIndexOf('.');
		String simple = (idx == -1) ? typeName : typeName.substring(idx + 1);
		return simple.split("[.$]");
	}

	private static CompilationUnit parse(ICompilationUnit cu) {
		ASTParser parser = ASTParser.newParser(AST.JLS_Latest);
		parser.setSource(cu);
		parser.setResolveBindings(false);
		return (CompilationUnit) parser.createAST(null);
	}
}
