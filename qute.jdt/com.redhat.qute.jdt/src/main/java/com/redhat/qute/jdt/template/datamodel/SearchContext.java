/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
* which is available at https://www.apache.org/licenses/LICENSE-2.0.
*
* SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.jdt.template.datamodel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;

import com.redhat.qute.commons.QuteProjectScope;
import com.redhat.qute.commons.datamodel.DataModelParameter;
import com.redhat.qute.commons.datamodel.DataModelProject;
import com.redhat.qute.commons.datamodel.DataModelTemplate;
import com.redhat.qute.jdt.QuteSupportForTemplate;
import com.redhat.qute.jdt.internal.resolver.ITypeResolver;

/**
 * The search context used to collect properties.
 *
 * @author Angelo ZERR
 *
 */
public class SearchContext extends BaseContext {
	private final DataModelProject<DataModelTemplate<DataModelParameter>> dataModelProject;

	private Map<IType, ITypeResolver> typeResolvers;

	public SearchContext(IJavaProject javaProject,
			DataModelProject<DataModelTemplate<DataModelParameter>> dataModelProject, List<QuteProjectScope> scopes) {
		super(javaProject, scopes);
		this.dataModelProject = dataModelProject;
	}

	public DataModelProject<DataModelTemplate<DataModelParameter>> getDataModelProject() {
		return dataModelProject;
	}

	/**
	 * Returns the {@link ITypeResolver} of the given Java type <code>type</code>.
	 * 
	 * @param type the Java type.
	 * 
	 * @return the {@link ITypeResolver} of the given Java type <code>type</code>.
	 */
	public ITypeResolver getTypeResolver(IType type) {
		if (typeResolvers == null) {
			typeResolvers = new HashMap<IType, ITypeResolver>();
		}
		ITypeResolver typeResolver = typeResolvers.get(type);
		if (typeResolver == null) {
			typeResolver = QuteSupportForTemplate.createTypeResolver(type);
			typeResolvers.put(type, typeResolver);
		}
		return typeResolver;
	}

}
