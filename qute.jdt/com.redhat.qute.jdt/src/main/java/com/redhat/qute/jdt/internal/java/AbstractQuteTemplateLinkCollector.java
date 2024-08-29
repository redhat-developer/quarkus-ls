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
import static com.redhat.qute.jdt.internal.QuteJavaConstants.CHECKED_TEMPLATE_ANNOTATION_BASE_PATH;
import static com.redhat.qute.jdt.internal.QuteJavaConstants.CHECKED_TEMPLATE_ANNOTATION_DEFAULT_NAME;
import static com.redhat.qute.jdt.internal.QuteJavaConstants.CHECKED_TEMPLATE_ANNOTATION_DEFAULT_NAME_HYPHENATED_ELEMENT_NAME;
import static com.redhat.qute.jdt.internal.QuteJavaConstants.CHECKED_TEMPLATE_ANNOTATION_DEFAULT_NAME_UNDERSCORED_ELEMENT_NAME;
import static com.redhat.qute.jdt.internal.QuteJavaConstants.CHECKED_TEMPLATE_ANNOTATION_IGNORE_FRAGMENTS;
import static com.redhat.qute.jdt.internal.QuteJavaConstants.OLD_CHECKED_TEMPLATE_ANNOTATION;
import static com.redhat.qute.jdt.internal.QuteJavaConstants.TEMPLATE_CLASS;

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
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.RecordDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.lsp4j.Range;

import com.redhat.qute.jdt.internal.AnnotationLocationSupport;
import com.redhat.qute.jdt.internal.QuteJavaConstants;
import com.redhat.qute.jdt.utils.AnnotationUtils;
import com.redhat.qute.jdt.utils.IJDTUtils;
import com.redhat.qute.jdt.utils.JDTQuteProjectUtils;
import com.redhat.qute.jdt.utils.JDTTypeUtils;
import com.redhat.qute.jdt.utils.TemplateNameStrategy;
import com.redhat.qute.jdt.utils.TemplatePathInfo;

/**
 * Abstract class which collects {@link MethodDeclaration} or
 * {@link FieldDeclaration} which defines a Qute template link:
 * 
 * <ul>
 * <li>declared methods which have class annotated with @CheckedTemplate.</li>
 * <li>declared field which have Template as type.</li>
 * <li>declared record which implements TemplateInstance.</li>
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

	/**
	 * Support for "Template Fields"
	 * 
	 * <p>
	 * private Template items;
	 * </p>
	 * 
	 * @see <a href=
	 *      "https://quarkus.io/guides/qute-reference#quarkus_integration">Quarkus
	 *      Integration</a>
	 */
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
				collectTemplateLink(null, node, locationExpression, getTypeDeclaration(node), null, fieldName, false,
						TemplateNameStrategy.ELEMENT_NAME);
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

	/**
	 * Support for "TypeSafe Templates"
	 * 
	 * <p>
	 * 
	 * @CheckedTemplate public static class Templates { public static native
	 *                  TemplateInstance book(Book book);
	 *                  </p>
	 * 
	 * @see <a href=
	 *      "https://quarkus.io/guides/qute-reference#typesafe_templates">TypeSafe
	 *      Templates</a>
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public boolean visit(TypeDeclaration node) {
		levelTypeDecl++;
		Annotation checkedAnnotation = getCheckedAnnotation(node);
		if (checkedAnnotation != null) {
			// @CheckedTemplate
			// public static class Templates {
			// public static native TemplateInstance book(Book book);
			boolean ignoreFragments = isIgnoreFragments(checkedAnnotation);
			String basePath = getBasePath(checkedAnnotation);
			TemplateNameStrategy templateNameStrategy = getDefaultName(checkedAnnotation);
			List body = node.bodyDeclarations();
			for (Object declaration : body) {
				if (declaration instanceof MethodDeclaration methodDeclaration) {
					String methodName = methodDeclaration.getName().getIdentifier();
					collectTemplateLinkForMethodOrRecord(basePath, methodDeclaration, methodName, node, ignoreFragments,
							templateNameStrategy);
				}
			}
		}
		return super.visit(node);
	}

	/**
	 * Support for "Template Records"
	 * 
	 * @see <a href=
	 *      "https://quarkus.io/guides/qute-reference#template-records">Template
	 *      Records</a>
	 */
	@Override
	public boolean visit(RecordDeclaration node) {
		if (isImplementTemplateInstance(node)) {
			// public class HelloResource {
			// record Hello(String name) implements TemplateInstance {}
			String recordName = node.getName().getIdentifier();
			Annotation checkedAnnotation = getCheckedAnnotation(node);
			boolean ignoreFragments = isIgnoreFragments(checkedAnnotation);
			String basePath = getBasePath(checkedAnnotation);
			TemplateNameStrategy templateNameStrategy = getDefaultName(checkedAnnotation);
			collectTemplateLinkForMethodOrRecord(basePath, node, recordName, node, ignoreFragments,
					templateNameStrategy);
		}
		return super.visit(node);
	}

	/**
	 * Returns true if the record implements the "io.quarkus.qute.TemplateInstance"
	 * interface and false otherwise.
	 * 
	 * @param node the record node.
	 * @return true if the record implements the "io.quarkus.qute.TemplateInstance"
	 *         interface and false otherwise.
	 */
	private static boolean isImplementTemplateInstance(RecordDeclaration node) {
		ITypeBinding binding = node.resolveBinding();
		if (binding == null) {
			return false;
		}
		ITypeBinding[] interfaces = binding.getInterfaces();
		if (interfaces == null || interfaces.length == 0) {
			return false;
		}
		for (ITypeBinding current : interfaces) {
			if (QuteJavaConstants.TEMPLATE_INSTANCE_INTERFACE.equals(current.getQualifiedName())) {
				return true;
			}
		}
		return false;
	}

	private static Annotation getCheckedAnnotation(BodyDeclaration node) {
		List modifiers = node.modifiers();
		for (Object modifier : modifiers) {
			if (modifier instanceof Annotation) {
				Annotation annotation = (Annotation) modifier;
				if (AnnotationUtils.isMatchAnnotation(annotation, CHECKED_TEMPLATE_ANNOTATION)
						|| AnnotationUtils.isMatchAnnotation(annotation, OLD_CHECKED_TEMPLATE_ANNOTATION)) {
					return annotation;
				}
			}
		}
		return null;
	}

	/**
	 * Returns true if @CheckedTemplate annotation declares that fragment must be
	 * ignored and false otherwise.
	 * 
	 * <code>
	 * @CheckedTemplate(ignoreFragments=true)
	 * </code>
	 * 
	 * @param checkedTemplateAnnotation the CheckedTemplate annotation.
	 * 
	 * @return true if @CheckedTemplate annotation declares that fragment must be
	 *         ignored and false otherwise.
	 */
	private static boolean isIgnoreFragments(Annotation checkedTemplateAnnotation) {
		Boolean ignoreFragment = null;
		if (checkedTemplateAnnotation != null) {
			try {
				Expression ignoreFragmentExpr = AnnotationUtils.getAnnotationMemberValueExpression(
						checkedTemplateAnnotation, CHECKED_TEMPLATE_ANNOTATION_IGNORE_FRAGMENTS);
				ignoreFragment = AnnotationUtils.getBoolean(ignoreFragmentExpr);
			} catch (Exception e) {
				// Do nothing
			}
		}
		return ignoreFragment != null ? ignoreFragment.booleanValue() : false;
	}

	/**
	 * Returns the <code>basePath</code> value declared in the @CheckedTemplate
	 * annotation, relative to the templates root, to search the templates from.
	 * <code>
	 * @CheckedTemplate(basePath="somewhere")
	 *</code>
	 *
	 * @param checkedTemplateAnnotation the CheckedTemplate annotation.
	 * @return the <code>basePath</code> value declared in the @CheckedTemplate
	 *         annotation
	 */
	private static String getBasePath(Annotation checkedTemplateAnnotation) {
		String basePath = null;
		if (checkedTemplateAnnotation != null) {
			try {
				Expression basePathExpr = AnnotationUtils.getAnnotationMemberValueExpression(checkedTemplateAnnotation,
						CHECKED_TEMPLATE_ANNOTATION_BASE_PATH);
				basePath = AnnotationUtils.getString(basePathExpr);
			} catch (Exception e) {
				// Do nothing
			}
		}
		return basePath;
	}

	/**
	 * Returns the <code>defaultName</code> value declared in the @CheckedTemplate
	 * annotation, relative to the templates root, to search the templates from.
	 * <code>
	 * @CheckedTemplate(defaultName=@CheckedTemplate.HYPHENATED_ELEMENT_NAME)
	 *</code>
	 *
	 * @param checkedTemplateAnnotation the CheckedTemplate annotation.
	 * @return the <code>defaultName</code> value declared in the @CheckedTemplate
	 *         annotation
	 */
	private static TemplateNameStrategy getDefaultName(Annotation checkedTemplateAnnotation) {
		TemplateNameStrategy defaultName = TemplateNameStrategy.ELEMENT_NAME;
		if (checkedTemplateAnnotation != null) {
			try {
				Expression defaultNameExpr = AnnotationUtils.getAnnotationMemberValueExpression(
						checkedTemplateAnnotation, CHECKED_TEMPLATE_ANNOTATION_DEFAULT_NAME);
				defaultName = getDefaultName(AnnotationUtils.getString(defaultNameExpr));
			} catch (Exception e) {
				// Do nothing
			}
		}
		return defaultName;
	}

	private static TemplateNameStrategy getDefaultName(String defaultName) {
		switch (defaultName) {
		case CHECKED_TEMPLATE_ANNOTATION_DEFAULT_NAME_HYPHENATED_ELEMENT_NAME:
			return TemplateNameStrategy.HYPHENATED_ELEMENT_NAME;

		case CHECKED_TEMPLATE_ANNOTATION_DEFAULT_NAME_UNDERSCORED_ELEMENT_NAME:
			return TemplateNameStrategy.UNDERSCORED_ELEMENT_NAME;
		}
		return TemplateNameStrategy.ELEMENT_NAME;
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

	private void collectTemplateLinkForMethodOrRecord(String basePath, ASTNode methodOrRecord,
			String methodOrRecordName, AbstractTypeDeclaration type, boolean ignoreFragment,
			TemplateNameStrategy templateNameStrategy) {
		String className = null;
		boolean innerClass = methodOrRecord instanceof RecordDeclaration ? levelTypeDecl >= 1 : levelTypeDecl > 1;
		if (innerClass) {
			className = JDTTypeUtils.getSimpleClassName(typeRoot.getElementName());
		}
		collectTemplateLink(basePath, methodOrRecord, null, type, className, methodOrRecordName, ignoreFragment,
				templateNameStrategy);
	}

	private void collectTemplateLink(String basePath, ASTNode fieldOrMethod, StringLiteral locationAnnotation,
			AbstractTypeDeclaration type, String className, String fieldOrMethodName, boolean ignoreFragment,
			TemplateNameStrategy templateNameStrategy) {
		try {
			String location = locationAnnotation != null ? locationAnnotation.getLiteralValue() : null;
			IProject project = typeRoot.getJavaProject().getProject();
			TemplatePathInfo templatePathInfo = location != null
					? JDTQuteProjectUtils.getTemplatePath(basePath, null, location, ignoreFragment,
							templateNameStrategy)
					: JDTQuteProjectUtils.getTemplatePath(basePath, className, fieldOrMethodName, ignoreFragment,
							templateNameStrategy);
			IFile templateFile = null;
			if (location == null) {
				templateFile = getTemplateFile(project, templatePathInfo.getTemplateUri());
				templatePathInfo = new TemplatePathInfo(
						templateFile.getLocation().makeRelativeTo(project.getLocation()).toString(),
						templatePathInfo.getFragmentId());
			} else {
				templateFile = project.getFile(templatePathInfo.getTemplateUri());
			}
			collectTemplateLink(basePath, fieldOrMethod, locationAnnotation, type, className, fieldOrMethodName,
					location, templateFile, templatePathInfo);
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
		case ASTNode.RECORD_DECLARATION: {
			RecordDeclaration recordDecl = (RecordDeclaration) fieldOrMethod;
			SimpleName recordName = recordDecl.getName();
			return utils.toRange(typeRoot, recordName.getStartPosition(), recordName.getLength());
		}
		default:
			return utils.toRange(typeRoot, fieldOrMethod.getStartPosition(), fieldOrMethod.getLength());
		}
	}

	protected abstract void collectTemplateLink(String basePath, ASTNode node, ASTNode locationAnnotation,
			AbstractTypeDeclaration type, String className, String fieldOrMethodName, String location,
			IFile templateFile, TemplatePathInfo templatePathInfo) throws JavaModelException;

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
		return (TEMPLATE_CLASS.equals(((SimpleType) type).resolveBinding().getQualifiedName().toString()));
	}
}
