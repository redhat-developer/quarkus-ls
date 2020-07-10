/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.ls;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4mp.commons.MicroProfilePropertyDefinitionParams;
import org.eclipse.lsp4mp.ls.api.MicroProfilePropertyDefinitionProvider;
import org.eclipse.lsp4mp.services.MicroProfileAssert;

import com.google.gson.Gson;

/**
 * Mock for MicroProfile property definition provider.
 * 
 * @author Angelo ZERR
 *
 */
public class MockMicroProfilePropertyDefinitionProvider implements MicroProfilePropertyDefinitionProvider {

	private static class PropertyDefinition extends MicroProfilePropertyDefinitionParams {

		private Location location;

		public Location getLocation() {
			return location;
		}

	}

	private final Map<String /* property source */, Location> cache;

	public MockMicroProfilePropertyDefinitionProvider() {
		cache = new HashMap<>();
		PropertyDefinition[] definitions = new Gson().fromJson(
				new InputStreamReader(MicroProfileAssert.class.getResourceAsStream("all-quarkus-definitions.json")),
				PropertyDefinition[].class);
		for (PropertyDefinition propertyDefinition : definitions) {
			cache.put(getKey(propertyDefinition), propertyDefinition.getLocation());
		}
	}

	@Override
	public CompletableFuture<Location> getPropertyDefinition(MicroProfilePropertyDefinitionParams params) {
		return CompletableFuture.completedFuture(cache.get(getKey(params)));
	}

	private static String getKey(MicroProfilePropertyDefinitionParams params) {
		return params.getSourceType() + "#" + params.getSourceField() + "#" + params.getSourceMethod();
	}

}
