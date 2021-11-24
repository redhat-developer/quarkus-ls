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
package com.redhat.qute.jdt.internal.java;

import static com.redhat.qute.jdt.internal.QuteJavaConstants.CHECKED_TEMPLATE_ANNOTATION;
import static com.redhat.qute.jdt.internal.QuteJavaConstants.OLD_CHECKED_TEMPLATE_ANNOTATION;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.lsp4j.Range;

import com.redhat.qute.jdt.utils.AnnotationUtils;
import com.redhat.qute.jdt.utils.IJDTUtils;
import com.redhat.qute.jdt.utils.JDTQuteProjectUtils;
import com.redhat.qute.jdt.utils.JDTTypeUtils;

/**
 * Abstract class which collects {@link MethodDeclaration} or
 * {@link FieldDeclaration} which defines a Qute template link:
 * 
 * <ul>
 * <li>declared methods which have class annotated with @CheckedTemplate.</li>
 * <li>declared field which have Template as type.</li>
 * </ul>
 * 
 * @author Angelo ZERR
 *
 */
public abstract class AbstractQuteTemplateLinkCollector extends ASTVisitor {

	private static final Logger LOGGER = Logger.getLogger(AbstractQuteTemplateLinkCollector.class.getName());

	private static String[] suffixes = { ".qute.html", ".qute.json", ".qute.txt", ".qute.yaml", ".html", ".json",
			".txt", ".yaml" };

	private static final String PREFERRED_SUFFIX = ".qute.html";

	protected final ITypeRoot typeRoot;
	protected final IJDTUtils utils;
	protected final IProgressMonitor monitor;

	private int levelTypeDecl;

	public AbstractQuteTemplateLinkCollector(ITypeRoot typeRoot, IJDTUtils utils, IProgressMonitor monitor) {
		this.typeRoot = typeRoot;
		this.utils = utils;
		this.monitor = monitor;
		this.levelTypeDecl = 0;
	}

	@Override
	public boolean visit(FieldDeclaration node) {
		Type type = node.getType();
		if (type.isSimpleType()) {
			if ("Template".equals(((SimpleType) type).getName().toString())) {
				processTemplateLink(node);
			}
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		levelTypeDecl++;
		List modifiers = node.modifiers();
		for (Object modifier : modifiers) {
			if (modifier instanceof Annotation) {
				Annotation annotation = (Annotation) modifier;
				if (AnnotationUtils.isMatchAnnotation(annotation, CHECKED_TEMPLATE_ANNOTATION)
						|| AnnotationUtils.isMatchAnnotation(annotation, OLD_CHECKED_TEMPLATE_ANNOTATION)) {
					// @CheckedTemplate
					// public static class Templates {
					// public static native TemplateInstance book(Book book);
					List body = node.bodyDeclarations();
					for (Object declaration : body) {
						if (declaration instanceof MethodDeclaration) {
							processTemplateLink((MethodDeclaration) declaration, node);
						}
					}
				}
			}

		}
		return super.visit(node);
	}

	@Override
	public void endVisit(TypeDeclaration node) {
		levelTypeDecl--;
		super.endVisit(node);
	}

	private void processTemplateLink(FieldDeclaration node) {
		List modifiers = node.modifiers();
		if (modifiers != null) {
			for (Object modifier : modifiers) {
				if (modifier instanceof SingleMemberAnnotation) {
					SingleMemberAnnotation annotation = (SingleMemberAnnotation) modifier;
					if ("Location".equals(annotation.getTypeName().getFullyQualifiedName())) {
						Expression expression = annotation.getValue();
						if (expression != null && expression instanceof StringLiteral) {
							String location = ((StringLiteral) expression).getLiteralValue();
							if (StringUtils.isNotBlank(location)) {
								processTemplateLink(node, (TypeDeclaration) node.getParent(), null, null, location);
							}
							return;
						}
					}
				}
			}
		}

		List fragments = node.fragments();
		if (fragments != null && !fragments.isEmpty()) {
			VariableDeclaration variable = (VariableDeclaration) fragments.get(0);
			String fieldName = variable.getName().toString();
			processTemplateLink(node, (TypeDeclaration) node.getParent(), null, fieldName, null);
		}
	}

	private void processTemplateLink(MethodDeclaration methodDeclaration, TypeDeclaration type) {
		String className = null;
		boolean innerClass = levelTypeDecl > 1;
		if (innerClass) {
			className = JDTTypeUtils.getSimpleClassName(typeRoot.getElementName());
		}
		String methodName = methodDeclaration.getName().getIdentifier();
		processTemplateLink(methodDeclaration, type, className, methodName, null);
	}

	private void processTemplateLink(ASTNode fieldOrMethod, TypeDeclaration type, String className,
			String fieldOrMethodName, String location) {
		try {
			IProject project = typeRoot.getJavaProject().getProject();
			String templateFilePath = location != null ? JDTQuteProjectUtils.getTemplatePath(null, location)
					: JDTQuteProjectUtils.getTemplatePath(className, fieldOrMethodName);
			IFile templateFile = null;
			if (location == null) {
				templateFile = getTemplateFile(project, templateFilePath);
				templateFilePath = templateFile.getLocation().makeRelativeTo(project.getLocation()).toString();
			} else {
				templateFile = project.getFile(templateFilePath);
			}
			processTemplateLink(fieldOrMethod, type, className, fieldOrMethodName, location, templateFile,
					templateFilePath);
		} catch (JavaModelException e) {
			LOGGER.log(Level.SEVERE, "Error while creating Qute CodeLens for Java file.", e);
		}
	}

	private IFile getTemplateFile(IProject project, String templateFilePathWithoutExtension) {
		for (String suffix : suffixes) {
			IFile templateFile = project.getFile(templateFilePathWithoutExtension + suffix);
			if (templateFile.exists()) {
				return templateFile;
			}
		}
		return project.getFile(templateFilePathWithoutExtension + PREFERRED_SUFFIX);
	}

	protected abstract void processTemplateLink(ASTNode node, TypeDeclaration type, String className,
			String fieldOrMethodName, String location, IFile templateFile, String templateFilePath)
			throws JavaModelException;

	protected Range createRange(ASTNode fieldOrMethod) throws JavaModelException {
		if (fieldOrMethod.getNodeType() == ASTNode.FIELD_DECLARATION) {
			FieldDeclaration field = (FieldDeclaration) fieldOrMethod;
			if (!field.fragments().isEmpty()) {
				VariableDeclarationFragment fragment = (VariableDeclarationFragment) field.fragments().get(0);
				return utils.toRange(typeRoot, fragment.getStartPosition(), fragment.getLength());
			}
			return utils.toRange(typeRoot, field.getStartPosition(), field.getLength());
		}
		MethodDeclaration method = (MethodDeclaration) fieldOrMethod;
		SimpleName methodName = method.getName();
		return utils.toRange(typeRoot, methodName.getStartPosition(), methodName.getLength());
	}
}
