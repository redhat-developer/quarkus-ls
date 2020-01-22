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

/**
 * Configuration metadata
 * 
 * @author Angelo ZERR
 * 
 * @see https://docs.spring.io/spring-boot/docs/current/reference/html/appendix-configuration-metadata.html
 */
public class ConfigurationMetadata {

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

	/**
	 * Returns the item hint from the given item metadata name or type and null
	 * otherwise.
	 * 
	 * @param property the item metadata
	 * @return the item hint from the given item metadata name or type and null
	 *         otherwise.
	 */
	public ItemHint getHint(ItemMetadata property) {
		return getHint(property.getName(), property.getHintType());
	}

	/**
	 * Returns the item hint from the given possible hint and null otherwise.
	 * 
	 * @param hint possibles hint
	 * @return the item hint from the given possible hint and null otherwise.
	 */
	public ItemHint getHint(String... hint) {
		if (hints == null || hint == null) {
			return null;
		}
		for (ItemHint itemHint : hints) {
			for (String name : hint) {
				if (itemHint.getName().equals(name)) {
					return itemHint;
				}
			}
		}
		return null;
	}

	public boolean isValidEnum(ItemMetadata metadata, String value) {
		ItemHint itemHint = getHint(metadata);
		if (itemHint == null) {
			return true;
		}
		return itemHint.getValue(value) != null;
	}

}
