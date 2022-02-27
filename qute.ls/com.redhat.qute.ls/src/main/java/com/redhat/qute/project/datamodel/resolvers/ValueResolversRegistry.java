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
package com.redhat.qute.project.datamodel.resolvers;

import java.io.InputStreamReader;
import java.util.List;

import com.google.gson.Gson;

public class ValueResolversRegistry {

	private List<MethodValueResolver> resolvers;

	public ValueResolversRegistry() {
		ValueResolverLoader loader = new Gson().fromJson(
				new InputStreamReader(ValueResolversRegistry.class.getResourceAsStream("qute-resolvers.jsonc")),
				ValueResolverLoader.class);
		this.resolvers = loader.getResolvers();
	}

	public List<MethodValueResolver> getResolvers() {
		return resolvers;
	}

	private class ValueResolverLoader {
		private List<MethodValueResolver> resolvers;

		public List<MethodValueResolver> getResolvers() {
			return resolvers;
		}
	}
}
