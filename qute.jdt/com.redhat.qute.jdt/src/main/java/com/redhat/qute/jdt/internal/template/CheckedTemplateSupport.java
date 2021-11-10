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
package com.redhat.qute.jdt.internal.template;

import static com.redhat.qute.jdt.internal.template.QuarkusIntegrationForQute.resolveSignature;
import static com.redhat.qute.jdt.utils.JDTQuteProjectUtils.getTemplatePath;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.core.manipulation.dom.ASTResolving;
import org.eclipse.jdt.internal.core.search.BasicSearchEngine;

import com.redhat.qute.commons.datamodel.DataModelParameter;
import com.redhat.qute.commons.datamodel.DataModelTemplate;
import com.redhat.qute.jdt.utils.JDTTypeUtils;

/**
 * CheckedTemplate support for template files.
 * 
 * @author Angelo ZERR
 *
 */
class CheckedTemplateSupport {

	private static final Logger LOGGER = Logger.getLogger(CheckedTemplateSupport.class.getName());

	public static void collectDataModelTemplateForCheckedTemplate(IType type,
			List<DataModelTemplate<DataModelParameter>> templates, IProgressMonitor monitor) throws JavaModelException {
		boolean innerClass = type.getParent() != null && type.getParent().getElementType() == IJavaElement.TYPE;
		String className = !innerClass ? null
				: JDTTypeUtils.getSimpleClassName(
						type.getCompilationUnit() != null ? type.getCompilationUnit().getElementName()
								: type.getClassFile().getElementName());

		// Loop for each methods (book, book) and create a template data model per
		// method.
		IMethod[] methods = type.getMethods();
		for (IMethod method : methods) {
			DataModelTemplate<DataModelParameter> template = createTemplateDataModel(method, className, type, monitor);
			templates.add(template);
		}
	}

	private static DataModelTemplate<DataModelParameter> createTemplateDataModel(IMethod method, String className,
			IType type, IProgressMonitor monitor) {
		String methodName = method.getElementName();
		// src/main/resources/templates/${className}/${methodName}.qute.html
		String templateUri = getTemplatePath(className, methodName);

		// Create template data model with:
		// - template uri : Qute template file which must be bind with data model.
		// - source type : the Java class which defines Templates
		// -
		DataModelTemplate<DataModelParameter> template = new DataModelTemplate<DataModelParameter>();
		template.setParameters(new ArrayList<>());
		template.setTemplateUri(templateUri);
		template.setSourceType(type.getFullyQualifiedName());
		template.setSourceMethod(methodName);

		try {
			for (ILocalVariable methodParameter : method.getParameters()) {
				DataModelParameter parameter = createParameterDataModel(methodParameter, type);
				template.getParameters().add(parameter);
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE,
					"Error while getting method template parameter of '" + method.getElementName() + "'.", e);
		}
		collectDataForTemplate(method, template, monitor);
		return template;
	}

	public static void collectDataForTemplate(IMember fieldOrMethod, DataModelTemplate<DataModelParameter> template,
			IProgressMonitor monitor) {
		SearchEngine engine = new SearchEngine();
		SearchPattern pattern = SearchPattern.createPattern(fieldOrMethod, IJavaSearchConstants.REFERENCES);
		int searchScope = IJavaSearchScope.SOURCES;
		IJavaSearchScope scope = BasicSearchEngine.createJavaSearchScope(true,
				new IJavaElement[] { fieldOrMethod.getJavaProject() }, searchScope);
		try {
			engine.search(pattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() }, scope,
					new SearchRequestor() {

						@Override
						public void acceptSearchMatch(SearchMatch match) throws CoreException {
							Object o = match.getElement();
							if (o instanceof IMethod) {
								IMethod method = (IMethod) o;
								CompilationUnit cu = getASTRoot(method.getCompilationUnit());
								cu.accept(new TemplateDataCollector(template, monitor));
							}
						}
					}, monitor);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static DataModelParameter createParameterDataModel(ILocalVariable methodParameter, IType type)
			throws JavaModelException {
		String parameterName = methodParameter.getElementName();
		String parameterType = resolveSignature(methodParameter, type);

		DataModelParameter parameter = new DataModelParameter();
		parameter.setKey(parameterName);
		parameter.setSourceType(parameterType);
		return parameter;
	}

	private static CompilationUnit getASTRoot(ITypeRoot typeRoot) {
		return ASTResolving.createQuickFixAST((ICompilationUnit) typeRoot, null);
	}

}
