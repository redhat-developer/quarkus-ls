/*******************************************************************************
* Copyright (c) 2023 Red Hat Inc. and others.
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
package com.redhat.qute.jdt.internal.template.resolvedtype;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;

import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.commons.datamodel.resolvers.ValueResolverKind;
import com.redhat.qute.jdt.internal.AbstractQuteExtensionPointRegistry;

/**
 * Registry to handle instances of {@link IResolvedJavaTypeFactory}
 *
 * @author Angelo ZERR
 */
public class ResolvedJavaTypeFactoryRegistry extends AbstractQuteExtensionPointRegistry<IResolvedJavaTypeFactory> {

	private static final String RESOLVED_JAVA_TYPE_FACTORIES_EXTENSION_POINT_ID = "resolvedJavaTypeFactories";

	private static final ResolvedJavaTypeFactoryRegistry INSTANCE = new ResolvedJavaTypeFactoryRegistry();

	private ResolvedJavaTypeFactoryRegistry() {
		super();
	}

	public static ResolvedJavaTypeFactoryRegistry getInstance() {
		return INSTANCE;
	}

	@Override
	public String getProviderExtensionId() {
		return RESOLVED_JAVA_TYPE_FACTORIES_EXTENSION_POINT_ID;
	}

	public ResolvedJavaTypeInfo create(IType type, ValueResolverKind kind) throws CoreException {
		return getFactory(kind).create(type);
	}

	private IResolvedJavaTypeFactory getFactory(ValueResolverKind kind) {
		for (IResolvedJavaTypeFactory factory : getProviders()) {
			if (factory.isAdaptedFor(kind)) {
				return factory;
			}
		}
		return DefaultResolvedJavaTypeFactory.getInstance();
	}
}
