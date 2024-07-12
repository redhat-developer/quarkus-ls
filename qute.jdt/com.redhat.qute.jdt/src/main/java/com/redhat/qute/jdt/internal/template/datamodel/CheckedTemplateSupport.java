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
package com.redhat.qute.jdt.internal.template.datamodel;

import static com.redhat.qute.jdt.internal.QuteJavaConstants.CHECKED_TEMPLATE_ANNOTATION;
import static com.redhat.qute.jdt.internal.QuteJavaConstants.CHECKED_TEMPLATE_ANNOTATION_BASE_PATH;
import static com.redhat.qute.jdt.internal.QuteJavaConstants.CHECKED_TEMPLATE_ANNOTATION_IGNORE_FRAGMENTS;
import static com.redhat.qute.jdt.internal.QuteJavaConstants.OLD_CHECKED_TEMPLATE_ANNOTATION;
import static com.redhat.qute.jdt.utils.JDTQuteProjectUtils.getTemplatePath;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import com.redhat.qute.commons.datamodel.DataModelBaseTemplate;
import com.redhat.qute.commons.datamodel.DataModelFragment;
import com.redhat.qute.commons.datamodel.DataModelParameter;
import com.redhat.qute.commons.datamodel.DataModelTemplate;
import com.redhat.qute.jdt.internal.resolver.ITypeResolver;
import com.redhat.qute.jdt.internal.template.TemplateDataSupport;
import com.redhat.qute.jdt.template.datamodel.AbstractAnnotationTypeReferenceDataModelProvider;
import com.redhat.qute.jdt.template.datamodel.SearchContext;
import com.redhat.qute.jdt.utils.AnnotationUtils;
import com.redhat.qute.jdt.utils.JDTTypeUtils;
import com.redhat.qute.jdt.utils.TemplatePathInfo;

/**
 * CheckedTemplate support for template files:
 * 
 * <code>
 * &#64;CheckedTemplate
 *	static class Templates {
 *		static native TemplateInstance items(List<Item> items);
 *	}
 * 
 *  ...
 *  
 *  &#64;GET
 *	&#64;Produces(MediaType.TEXT_HTML)
 *	public TemplateInstance get() {
 * 		List<Item> items = new ArrayList<>();
 * 		items.add(new Item(new BigDecimal(10), "Apple"));
 * 		items.add(new Item(new BigDecimal(16), "Pear"));
 * 		items.add(new Item(new BigDecimal(30), "Orange"));
 * 		return Templates.items(items);
 *	}
 * </code>
 * 
 * @see <a href=
 *      "https://quarkus.io/guides/qute-reference#typesafe_templates">TypeSafe
 *      Templates</a>
 * @author Angelo ZERR
 *
 */
public class CheckedTemplateSupport extends AbstractAnnotationTypeReferenceDataModelProvider {

	private static final Logger LOGGER = Logger.getLogger(CheckedTemplateSupport.class.getName());

	private static final String[] ANNOTATION_NAMES = { CHECKED_TEMPLATE_ANNOTATION, OLD_CHECKED_TEMPLATE_ANNOTATION };

	@Override
	protected String[] getAnnotationNames() {
		return ANNOTATION_NAMES;
	}

	@Override
	protected void processAnnotation(IJavaElement javaElement, IAnnotation checkedTemplateAnnotation,
			String annotationName, SearchContext context, IProgressMonitor monitor) throws JavaModelException {
		if (javaElement instanceof IType) {
			IType type = (IType) javaElement;
			boolean ignoreFragments = isIgnoreFragments(checkedTemplateAnnotation);
			String basePath = getBasePath(checkedTemplateAnnotation);
			collectDataModelTemplateForCheckedTemplate(type, basePath, ignoreFragments, context.getTypeResolver(type),
					context.getDataModelProject().getTemplates(), monitor);
		}
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
	private static boolean isIgnoreFragments(IAnnotation checkedTemplateAnnotation) {
		try {
			Boolean ignoreFragments = AnnotationUtils.getAnnotationMemberValueAsBoolean(checkedTemplateAnnotation,
					CHECKED_TEMPLATE_ANNOTATION_IGNORE_FRAGMENTS);
			return ignoreFragments != null ? ignoreFragments.booleanValue() : false;
		} catch (Exception e) {
			// Do nothing
			return false;
		}
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
	private static String getBasePath(IAnnotation checkedTemplateAnnotation) {
		String basePath = null;
		try {
			for (IMemberValuePair pair : checkedTemplateAnnotation.getMemberValuePairs()) {
				if (CHECKED_TEMPLATE_ANNOTATION_BASE_PATH.equalsIgnoreCase(pair.getMemberName())) {
					basePath = AnnotationUtils.getValueAsString(pair);
				}
			}
		} catch (Exception e) {
			// Do nothing
		}
		return basePath;
	}

	/**
	 * Collect data model template from @CheckedTemplate.
	 * 
	 * @param type            the Java type.
	 * @param basePath        the base path relative to the templates root
	 * @param ignoreFragments true if fragments must be ignored and false otherwise.
	 * @param typeResolver    the Java type resolver.
	 * @param templates       the data model templates to update with collect of
	 *                        template.
	 * @param monitor         the progress monitor.
	 * @throws JavaModelException
	 */
	private static void collectDataModelTemplateForCheckedTemplate(IType type, String basePath, boolean ignoreFragments,
			ITypeResolver typeResolver, List<DataModelTemplate<DataModelParameter>> templates, IProgressMonitor monitor)
			throws JavaModelException {
		boolean innerClass = type.getParent() != null && type.getParent().getElementType() == IJavaElement.TYPE;
		String className = !innerClass ? null
				: JDTTypeUtils.getSimpleClassName(
						type.getCompilationUnit() != null ? type.getCompilationUnit().getElementName()
								: type.getClassFile().getElementName());

		// Loop for each methods (book, book) and create a template data model per
		// method.
		IMethod[] methods = type.getMethods();
		for (IMethod method : methods) {

			// src/main/resources/templates/${className}/${methodName}.qute.html
			TemplatePathInfo templatePathInfo = getTemplatePath(basePath, className, method.getElementName(), ignoreFragments);

			// Get or create template
			String templateUri = templatePathInfo.getTemplateUri();
			String fragmentId = templatePathInfo.getFragmentId();

			DataModelTemplate<DataModelParameter> template = null;
			Optional<DataModelTemplate<DataModelParameter>> existingTemplate = templates.stream()
					.filter(t -> t.getTemplateUri().equals(templateUri)) //
					.findFirst();
			if (existingTemplate.isEmpty()) {
				template = createTemplateDataModel(templateUri, method, type);
				templates.add(template);
			} else {
				template = existingTemplate.get();
				if (fragmentId == null) {
					template.setSourceMethod(method.getElementName());
				}
			}

			if (fragmentId != null && fragmentId.length() > 0) {
				// The method name has '$' to define fragment id (ex : foo$bar)
				// Create fragment
				DataModelFragment<DataModelParameter> fragment = createFragmentDataModel(fragmentId, method, type);
				template.addFragment(fragment);
				// collect parameters for the fragment
				collectParameters(method, typeResolver, fragment, monitor);
			} else {
				// collect parameters for the template
				collectParameters(method, typeResolver, template, monitor);
			}
		}
	}

	private static DataModelTemplate<DataModelParameter> createTemplateDataModel(String templateUri, IMethod method,
			IType type) {

		// Create template data model with:
		// - template uri : Qute template file which must be bind with data model.
		// - source type : the Java class which defines Templates
		// - source method: : the Java method which defines Template
		DataModelTemplate<DataModelParameter> template = new DataModelTemplate<DataModelParameter>();
		template.setParameters(new ArrayList<>());
		template.setTemplateUri(templateUri);
		template.setSourceType(type.getFullyQualifiedName());
		template.setSourceMethod(method.getElementName());
		return template;
	}

	private static DataModelFragment<DataModelParameter> createFragmentDataModel(String fragmentId, IMethod method,
			IType type) {
		DataModelFragment<DataModelParameter> template = new DataModelFragment<DataModelParameter>();
		template.setParameters(new ArrayList<>());
		template.setId(fragmentId);
		template.setSourceType(type.getFullyQualifiedName());
		template.setSourceMethod(method.getElementName());
		return template;
	}

	public static void collectParameters(IMethod method, ITypeResolver typeResolver,
			DataModelBaseTemplate<DataModelParameter> templateOrFragment, IProgressMonitor monitor) {
		try {
			ILocalVariable[] parameters = method.getParameters();
			if (parameters.length > 0) {
				boolean varargs = Flags.isVarargs(method.getFlags());
				for (int i = 0; i < parameters.length; i++) {
					DataModelParameter parameter = createParameterDataModel(parameters[i],
							varargs && i == parameters.length - 1, typeResolver);
					templateOrFragment.getParameters().add(parameter);
				}
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE,
					"Error while getting method template parameter of '" + method.getElementName() + "'.", e);
		}
		// Collect data parameters for the given template
		TemplateDataSupport.collectParametersFromDataMethodInvocation(method, templateOrFragment, monitor);
	}

	private static DataModelParameter createParameterDataModel(ILocalVariable methodParameter, boolean varags,
			ITypeResolver typeResolver) throws JavaModelException {
		String parameterName = methodParameter.getElementName();
		String parameterType = typeResolver.resolveLocalVariableSignature(methodParameter, varags);

		DataModelParameter parameter = new DataModelParameter();
		parameter.setKey(parameterName);
		parameter.setSourceType(parameterType);
		return parameter;
	}

}
