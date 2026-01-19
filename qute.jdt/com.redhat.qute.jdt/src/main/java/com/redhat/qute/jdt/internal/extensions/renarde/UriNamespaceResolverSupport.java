/*******************************************************************************
* Copyright (c) 2023 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.jdt.internal.extensions.renarde;

import static com.redhat.qute.jdt.internal.QuteJavaConstants.JAVA_LANG_OBJECT_TYPE;
import static com.redhat.qute.jdt.internal.extensions.renarde.RenardeJavaConstants.RENARDE_CONTROLLER_TYPE;
import static com.redhat.qute.jdt.internal.extensions.renarde.RenardeUtils.isRenardeProject;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchPattern;

import com.redhat.qute.commons.datamodel.resolvers.ValueResolverInfo;
import com.redhat.qute.commons.datamodel.resolvers.ValueResolverKind;
import com.redhat.qute.jdt.template.datamodel.AbstractDataModelProvider;
import com.redhat.qute.jdt.template.datamodel.SearchContext;
import com.redhat.qute.jdt.utils.JDTTypeUtils;

/**
 * uri, uriabs renarde support.
 * 
 * @author Angelo ZERR
 * 
 * @see https://github.com/quarkiverse/quarkus-renarde/blob/main/docs/modules/ROOT/pages/index.adoc#obtaining-a-uri-in-qute-views
 *
 */
public class UriNamespaceResolverSupport extends AbstractDataModelProvider {

	private static final Logger LOGGER = Logger.getLogger(UriNamespaceResolverSupport.class.getName());

	private static final String URI_NAMESPACE = "uri";

	private static final String URIABS_NAMESPACE = "uriabs";

	@Override
	public void beginSearch(SearchContext context, IProgressMonitor monitor) {
		IJavaProject javaProject = context.getJavaProject();
		IType type = JDTTypeUtils.findType(javaProject, RENARDE_CONTROLLER_TYPE);
		if (type != null) {
			try {
				// Find all classes which extends 'io.quarkiverse.renarde.Controller'
				collectRenardeController(type, context, monitor);
			} catch (JavaModelException e) {
				LOGGER.log(Level.SEVERE, "Error while collecting Renarde Controller.", e);
			}

		}
	}

	public void collectRenardeController(IType type, SearchContext context, IProgressMonitor monitor)
			throws JavaModelException {
		if (type == null) {
			return;
		}
		ITypeHierarchy typeHierarchy = type.newTypeHierarchy(monitor);
		IType[] controllerTypes = typeHierarchy.getAllSubtypes(type);
		List<ValueResolverInfo> resolvers = context.getDataModelProject().getValueResolvers();
		for (IType controllerType : controllerTypes) {
			if (isRenardeController(controllerType)) {
				addRenardeController(URI_NAMESPACE, controllerType, resolvers);
				addRenardeController(URIABS_NAMESPACE, controllerType, resolvers);
			}
		}
	}

	/**
	 * Returns true if the given Java type is a non abstract Renarde controller and
	 * false otherwise.
	 * 
	 * @param controllerType the renarde controller type.
	 * 
	 * @return true if the given Java type is a non abstract Renarde controller and
	 *         false otherwise.
	 * @throws JavaModelException
	 */
	private static boolean isRenardeController(IType controllerType) throws JavaModelException {
		if (Flags.isAbstract(controllerType.getFlags())) {
			return false;
		}
		String typeName = controllerType.getFullyQualifiedName();
		return !(JAVA_LANG_OBJECT_TYPE.equals(typeName) || RENARDE_CONTROLLER_TYPE.equals(typeName));
	}

	/**
	 * Add renarde controller as Qute resolver.
	 * 
	 * @param namespace      the uri, uriabs renarde namespace.
	 * @param controllerType the controller type.
	 * @param resolvers      the resolvers to fill.
	 */
	private static void addRenardeController(String namespace, IType controllerType,
			List<ValueResolverInfo> resolvers) {
		String className = controllerType.getFullyQualifiedName();
		String named = controllerType.getElementName();
		ValueResolverInfo resolver = new ValueResolverInfo();
		resolver.setNamed(named);
		resolver.setSourceType(className);
		resolver.setSignature(className);
		resolver.setNamespace(namespace);
		resolver.setKind(ValueResolverKind.Renarde);
		if (!resolvers.contains(resolver)) {
			resolvers.add(resolver);
		}
	}

	@Override
	protected boolean isNamespaceAvailable(String namespace, SearchContext context, IProgressMonitor monitor) {
		// uri, and uriabs are available only for renarde project
		return isRenardeProject(context.getJavaProject());
	}

	@Override
	public void collectDataModel(SearchMatch match, SearchContext context, IProgressMonitor monitor) {
		// Do nothing
	}

	@Override
	protected String[] getPatterns() {
		return null;
	}

	@Override
	protected SearchPattern createSearchPattern(String pattern) {
		return null;
	}

}