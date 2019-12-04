/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.commons.metadata;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Configuration metadata
 * 
 * @author Angelo ZERR
 * 
 * @see https://docs.spring.io/spring-boot/docs/current/reference/html/appendix-configuration-metadata.html
 */
public class ConfigurationMetadata {

	private transient Map<String, ItemHint> hintsCache;

	private List<ItemMetadata> properties;

	private List<ItemHint> hints;

	public List<ItemMetadata> getProperties() {
		return properties;
	}

	public void setProperties(List<ItemMetadata> properties) {
		this.properties = properties;
	}

	public List<ItemHint> getHints() {
		return hints;
	}

	public void setHints(List<ItemHint> hints) {
		this.hints = hints;
	}

	public ItemHint getHint(ItemMetadata property) {
		if (hints == null) {
			return null;
		}
		if (hintsCache == null) {
			hintsCache = hints.stream().collect(Collectors.toMap(ItemHint::getName, Function.identity()));
		}
		ItemHint item = hintsCache.get(property.getName());
		if (item == null) {
			item = hintsCache.get(property.getType());
		}
		return item;
	}

	public boolean isValidEnum(ItemMetadata metadata, String value) {
		ItemHint itemHint = getHint(metadata);
		if (itemHint == null) {
			return true;
		}
		return itemHint.getValue(value) != null;
	}

}
