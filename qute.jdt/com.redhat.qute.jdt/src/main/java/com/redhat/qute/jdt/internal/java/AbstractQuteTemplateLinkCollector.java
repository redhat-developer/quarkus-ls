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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.lsp4j.Range;

import com.redhat.qute.jdt.internal.AnnotationLocationSupport;
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

	private static final String PREFERRED_SUFFIX = ".html"; // TODO make it configurable

	private static final String TEMPLATE_TYPE = "Template";

	protected final ITypeRoot typeRoot;
	protected final IJDTUtils utils;
	protected final IProgressMonitor monitor;

	private int levelTypeDecl;

	private AnnotationLocationSupport annotationLocationSupport;

	private CompilationUnit compilationUnit;

	public AbstractQuteTemplateLinkCollector(ITypeRoot typeRoot, IJDTUtils utils, IProgressMonitor monitor) {
		this.typeRoot = typeRoot;
		this.utils = utils;
		this.monitor = monitor;
		this.levelTypeDecl = 0;
	}

	@Override
	public boolean visit(CompilationUnit node) {
		this.compilationUnit = node;
		return super.visit(node);
	}

	@Override
	public boolean visit(FieldDeclaration node) {
		Type type = node.getType();
		if (isTemplateType(type)) {
			// The field type is the Qute template
			// private Template items;

			// Try to get the @Location annotation
			// @Location("detail/items2_v1.html")
			// Template items2;
			StringLiteral locationExpression = AnnotationLocationSupport.getLocationExpression(node, node.modifiers());

			@SuppressWarnings("rawtypes")
			List fragments = node.fragments();
			if (fragments != null && !fragments.isEmpty()) {
				VariableDeclaration variable = (VariableDeclaration) fragments.get(0);
				if (locationExpression == null) {
					// The field doesn't declare @Location,
					// try to find the @Location declared in the constructor parameter which
					// initializes the field

					// private final Template page;
					// public SomePage(@Location("foo/bar/page.qute.html") Template page) {
					// this.page = requireNonNull(page, "page is required");
					// }
					locationExpression = getAnnotationLocationSupport()
							.getLocationExpressionFromConstructorParameter(variable.getName().getIdentifier());
				}
				String fieldName = variable.getName().getIdentifier();
				collectTemplateLink(node, locationExpression, getTypeDeclaration(node), null, fieldName);
			}
		}
		return super.visit(node);
	}

	/**
	 * Returns the @Location support.
	 * 
	 * @return the @Location support.
	 */
	private AnnotationLocationSupport getAnnotationLocationSupport() {
		if (annotationLocationSupport == null) {
			// Initialize the @Location support to try to find an @Location in the
			// constructor which initializes some fields
			annotationLocationSupport = new AnnotationLocationSupport(compilationUnit);
		}
		return annotationLocationSupport;
	}

	@SuppressWarnings("rawtypes")
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
							collectTemplateLink((MethodDeclaration) declaration, node);
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

	private static TypeDeclaration getTypeDeclaration(ASTNode node) {
		ASTNode parent = node.getParent();
		while (parent != null && parent.getNodeType() != ASTNode.TYPE_DECLARATION) {
			parent = parent.getParent();
		}
		return parent != null && parent.getNodeType() == ASTNode.TYPE_DECLARATION ? (TypeDeclaration) parent : null;
	}

	private void collectTemplateLink(MethodDeclaration methodDeclaration, TypeDeclaration type) {
		String className = null;
		boolean innerClass = levelTypeDecl > 1;
		if (innerClass) {
			className = JDTTypeUtils.getSimpleClassName(typeRoot.getElementName());
		}
		String methodName = methodDeclaration.getName().getIdentifier();
		collectTemplateLink(methodDeclaration, null, type, className, methodName);
	}

	private void collectTemplateLink(ASTNode fieldOrMethod, StringLiteral locationAnnotation, TypeDeclaration type,
			String className, String fieldOrMethodName) {
		try {
			String location = locationAnnotation != null ? locationAnnotation.getLiteralValue() : null;
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
			collectTemplateLink(fieldOrMethod, locationAnnotation, type, className, fieldOrMethodName, location,
					templateFile, templateFilePath);
		} catch (JavaModelException e) {
			LOGGER.log(Level.SEVERE, "Error while creating Qute CodeLens for Java file.", e);
		}
	}

	protected Range createRange(ASTNode fieldOrMethod) throws JavaModelException {
		switch (fieldOrMethod.getNodeType()) {
		case ASTNode.FIELD_DECLARATION: {
			FieldDeclaration field = (FieldDeclaration) fieldOrMethod;
			if (!field.fragments().isEmpty()) {
				VariableDeclarationFragment fragment = (VariableDeclarationFragment) field.fragments().get(0);
				return utils.toRange(typeRoot, fragment.getStartPosition(), fragment.getLength());
			}
			return utils.toRange(typeRoot, field.getStartPosition(), field.getLength());
		}
		case ASTNode.METHOD_DECLARATION: {
			MethodDeclaration method = (MethodDeclaration) fieldOrMethod;
			SimpleName methodName = method.getName();
			return utils.toRange(typeRoot, methodName.getStartPosition(), methodName.getLength());
		}
		default:
			return utils.toRange(typeRoot, fieldOrMethod.getStartPosition(), fieldOrMethod.getLength());
		}
	}

	protected abstract void collectTemplateLink(ASTNode node, ASTNode locationAnnotation, TypeDeclaration type,
			String className, String fieldOrMethodName, String location, IFile templateFile, String templateFilePath)
			throws JavaModelException;

	private static IFile getTemplateFile(IProject project, String templateFilePathWithoutExtension) {
		for (String suffix : suffixes) {
			IFile templateFile = project.getFile(templateFilePathWithoutExtension + suffix);
			if (templateFile.exists()) {
				return templateFile;
			}
		}
		return project.getFile(templateFilePathWithoutExtension + PREFERRED_SUFFIX);
	}

	/**
	 * Returns true if the given Java type is Qute Template type and false
	 * otherwise.
	 * 
	 * @param type the Java type.
	 * 
	 * @return true if the given Java type is Qute Template type and false
	 *         otherwise.
	 */
	private static boolean isTemplateType(Type type) {
		if (type == null || !type.isSimpleType()) {
			return false;
		}
		return (TEMPLATE_TYPE.equals(((SimpleType) type).getName().toString()));
	}
}
