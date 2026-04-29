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
package com.redhat.qute.project.extensions.flags;

import static com.redhat.qute.project.datamodel.resolvers.ValueResolversRegistry.loadValueResolvers;

import java.util.List;

import com.redhat.qute.commons.config.flags.FlagsConfig;
import com.redhat.qute.commons.datamodel.resolvers.NamespaceResolverInfo;
import com.redhat.qute.project.datamodel.ExtendedDataModelProject;
import com.redhat.qute.project.datamodel.resolvers.MethodValueResolver;
import com.redhat.qute.project.extensions.AbstractProjectExtension;
import com.redhat.qute.project.extensions.ProjectExtensionContext;

/**
 * Qute project extension for Flags integration.
 * 
 * @see <a href=
 *      "https://quarkus.io/blog/quarkus-feature-flags/">quarkus-feature-flags</a>
 */
public class FlagsProjectExtension extends AbstractProjectExtension {

	private static final NamespaceResolverInfo FLAG_NAMESPACE;

	private List<MethodValueResolver> flagValueResolver;

	static {
		FLAG_NAMESPACE = new NamespaceResolverInfo();
		FLAG_NAMESPACE.setNamespaces(List.of("flag"));
		FLAG_NAMESPACE.setDescription(
				"Quarkus Feature Flags aims to provide a lightweight and extensible feature flag Quarkus extension.");
		FLAG_NAMESPACE.setUrl("https://quarkus.io/blog/quarkus-feature-flags/");
	}

	public FlagsProjectExtension() {
		super(FlagsConfig.PROJECT_FEATURE);
	}

	@Override
	protected void initialize(ExtendedDataModelProject dataModelProject, boolean onLoad, boolean enabled,
			ProjectExtensionContext context) {
		if (enabled) {
			// register flag: namespace support
			dataModelProject.registerNamespaceResolver(FLAG_NAMESPACE);
			if (flagValueResolver == null) {
				flagValueResolver = loadValueResolvers("qute-flags-resolvers.jsonc", FlagsProjectExtension.class);
			}
			dataModelProject.getMethodValueResolvers().addAll(flagValueResolver);

		} else {
			// unregister flag: namespace support
			dataModelProject.unregisterNamespaceResolver(FLAG_NAMESPACE);
			if (flagValueResolver != null) {
				dataModelProject.getMethodValueResolvers().removeAll(flagValueResolver);
			}
		}
	}

}
