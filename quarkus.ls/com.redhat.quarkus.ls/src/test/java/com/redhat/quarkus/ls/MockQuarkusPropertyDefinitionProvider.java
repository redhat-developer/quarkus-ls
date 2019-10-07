/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.quarkus.ls;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.Location;

import com.google.gson.Gson;
import com.redhat.quarkus.commons.QuarkusPropertyDefinitionParams;
import com.redhat.quarkus.ls.api.QuarkusPropertyDefinitionProvider;
import com.redhat.quarkus.services.QuarkusAssert;

/**
 * Mock for Quarkus property definition provider.
 * 
 * @author Angelo ZERR
 *
 */
public class MockQuarkusPropertyDefinitionProvider implements QuarkusPropertyDefinitionProvider {

	private static class PropertyDefinition {

		private String propertySource;

		private Location location;

		public String getPropertySource() {
			return propertySource;
		}

		public Location getLocation() {
			return location;
		}

	}

	private final Map<String /* property source */, Location> cache;

	public MockQuarkusPropertyDefinitionProvider() {
		cache = new HashMap<>();
		PropertyDefinition[] definitions = new Gson().fromJson(
				new InputStreamReader(QuarkusAssert.class.getResourceAsStream("all-quarkus-definitions.json")),
				PropertyDefinition[].class);
		for (PropertyDefinition propertyDefinition : definitions) {
			cache.put(propertyDefinition.getPropertySource(), propertyDefinition.getLocation());
		}
	}

	@Override
	public CompletableFuture<Location> getPropertyDefinition(QuarkusPropertyDefinitionParams params) {
		return CompletableFuture.completedFuture(cache.get(params.getPropertySource()));
	}

}
