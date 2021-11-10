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
package com.redhat.qute.project.datamodel;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.commons.ValueResolver;

public class ValueResolversRegistry {

	private List<ValueResolver> resolvers;

	public ValueResolversRegistry() {
		ValueResolverLoader loader = new Gson().fromJson(
				new InputStreamReader(ValueResolversRegistry.class.getResourceAsStream("qute-resolvers.json")),
				ValueResolverLoader.class);
		this.resolvers = loader.getResolvers();
	}

	public List<ValueResolver> getResolversFor(ResolvedJavaTypeInfo javaType) {
		List<ValueResolver> matches = new ArrayList<>();
		for (ValueResolver resolver : resolvers) {
			if (resolver.match(javaType)) {
				matches.add(resolver);
			}
		}
		return matches;
	}

	private class ValueResolverLoader {
		private List<ValueResolver> resolvers;

		public List<ValueResolver> getResolvers() {
			return resolvers;
		}
	}
}
