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
import com.redhat.qute.commons.resolvers.FieldValueResolver;
import com.redhat.qute.commons.resolvers.MethodValueResolver;
import com.redhat.qute.commons.resolvers.ValueResolver;

public class ValueResolversRegistry {

	private List<ValueResolver> resolvers;

	public ValueResolversRegistry() {
		ValueResolverLoader loader = new Gson().fromJson(
				new InputStreamReader(ValueResolversRegistry.class.getResourceAsStream("qute-resolvers.json")),
				ValueResolverLoader.class);
		this.resolvers = loader.getResolvers();
	}

	public List<ValueResolver> getResolvers() {
		return resolvers;
	}

	private class ValueResolverLoader {
		private List<FieldValueResolver> fields;

		private List<MethodValueResolver> methods;

		public List<ValueResolver> getResolvers() {
			List<ValueResolver> resolvers = new ArrayList<>();
			resolvers.addAll(fields);
			resolvers.addAll(methods);
			return resolvers;
		}
	}
}
