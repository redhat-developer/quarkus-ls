/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.quarkus.commons;

import java.util.List;

public class ExtendedConfigDescriptionBuildItem {

	/**
	 * Values are read and available for usage at build time.
	 */
	public static final int CONFIG_PHASE_BUILD_TIME = 1;
	/**
	 * Values are read and available for usage at build time, and available on a
	 * read-only basis at run time.
	 */
	public static final int CONFIG_PHASE_BUILD_AND_RUN_TIME_FIXED = 2;
	/**
	 * Values are read and available for usage at run time and are re-read on each
	 * program execution.
	 */
	public static final int CONFIG_PHASE_RUN_TIME = 3;

	private String propertyName;
	private String type;
	private String defaultValue;
	private String docs;

	private String extensionName;
	private String location;
	private String source;
	private boolean required;
	private int phase;
	private List<EnumItem> enums;

	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getDocs() {
		return docs;
	}

	public void setDocs(String docs) {
		this.docs = docs;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getExtensionName() {
		return extensionName;
	}

	public void setExtensionName(String extensionName) {
		this.extensionName = extensionName;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	/**
	 * Set the enumeration list of the property
	 * 
	 * @param enums the enumeration list of the property
	 */
	public void setEnums(List<EnumItem> enums) {
		this.enums = enums;
	}

	/**
	 * Returns the enumeration list of the property and null otherwise.
	 * 
	 * @return the enumeration list of the property and null otherwise.
	 */
	public List<EnumItem> getEnums() {
		return enums;
	}

	/**
	 * Returns the enumeration item from the given property value and null
	 * otherwise.
	 * 
	 * @param propertyValue the property value.
	 * @return the enumeration item from the given property value and null
	 *         otherwise.
	 */
	public EnumItem getEnumItem(String propertyValue) {
		return getEnumItem(propertyValue, enums);
	}

	/**
	 * Returns true if the given property value is a valid enumeration and false
	 * otherwise.
	 * 
	 * @param propertyValue the property value.
	 * @return true if the given property value is a valid enumeration and false
	 *         otherwise.
	 */
	public boolean isValidEnum(String propertyValue) {
		return isValidEnum(propertyValue, enums);
	}

	/**
	 * Returns the enumeration item from the given property value and given
	 * enumerations and null otherwise.
	 * 
	 * @param propertyValue the property value.
	 * @param enums         enumerations.
	 * @return the enumeration item from the given property value and given
	 *         enumerations and null otherwise.
	 */
	public static EnumItem getEnumItem(String propertyValue, List<EnumItem> enums) {
		if (enums == null || propertyValue == null || propertyValue.isEmpty()) {
			return null;
		}
		for (EnumItem enumItem : enums) {
			if (propertyValue.equals(enumItem.getName())) {
				return enumItem;
			}
		}
		return null;
	}

	/**
	 * Returns true if the given property value is a valid enumeration and false
	 * otherwise.
	 * 
	 * @param propertyValue the property value.
	 * @param enums         enumerations.
	 * @return true if the given property value is a valid enumeration and false
	 *         otherwise.
	 */
	public static boolean isValidEnum(String propertyValue, List<EnumItem> enums) {
		if (enums == null || enums.isEmpty()) {
			return true;
		}
		return getEnumItem(propertyValue, enums) != null;
	}

	public int getPhase() {
		return phase;
	}

	public void setPhase(int phase) {
		this.phase = phase;
	}

	public boolean isAvailableAtRun() {
		return phase == CONFIG_PHASE_BUILD_AND_RUN_TIME_FIXED || phase == CONFIG_PHASE_RUN_TIME;
	}

	public boolean isBooleanType() {
		return "boolean".equals(getType()) || //
				"java.lang.Boolean".equals(getType()) || //
				"java.util.Optional<java.lang.Boolean>".equals(getType());
	}

	public boolean isIntegerType() {
		return "int".equals(getType()) || //
				"java.lang.Integer".equals(getType()) || //
				"java.util.OptionalInt".equals(getType()) || //
				"java.util.Optional<java.lang.Integer>".equals(getType());
	}

	public boolean isFloatType() {
		return "float".equals(getType()) || //
				"java.lang.Float".equals(getType()) || //
				"java.util.Optional<java.lang.Float>".equals(getType());
	}

	public boolean isLongType() {
		return "long".equals(getType()) || //
				"java.lang.Long".equals(getType()) || //
				"java.util.OptionalLong".equals(getType()) || //
				"java.util.Optional<java.lang.Long>".equals(getType());
	}

	public boolean isDoubleType() {
		return "double".equals(getType()) || //
				"java.lang.Double".equals(getType()) || //
				"java.util.OptionalDouble".equals(getType()) || //
				"java.util.Optional<java.lang.Double>".equals(getType());
	}

	public boolean isShortType() {
		return "short".equals(getType()) || //
				"java.lang.Short".equals(getType()) || //
				"java.util.Optional<java.lang.Short>".equals(getType());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((defaultValue == null) ? 0 : defaultValue.hashCode());
		result = prime * result + ((docs == null) ? 0 : docs.hashCode());
		result = prime * result + ((enums == null) ? 0 : enums.hashCode());
		result = prime * result + ((extensionName == null) ? 0 : extensionName.hashCode());
		result = prime * result + ((location == null) ? 0 : location.hashCode());
		result = prime * result + phase;
		result = prime * result + ((propertyName == null) ? 0 : propertyName.hashCode());
		result = prime * result + (required ? 1 : 0);
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExtendedConfigDescriptionBuildItem other = (ExtendedConfigDescriptionBuildItem) obj;
		if (defaultValue == null) {
			if (other.defaultValue != null)
				return false;
		} else if (!defaultValue.equals(other.defaultValue))
			return false;
		if (docs == null) {
			if (other.docs != null)
				return false;
		} else if (!docs.equals(other.docs))
			return false;
		if (enums == null) {
			if (other.enums != null)
				return false;
		} else if (!enums.equals(other.enums))
			return false;
		if (extensionName == null) {
			if (other.extensionName != null)
				return false;
		} else if (!extensionName.equals(other.extensionName))
			return false;
		if (location == null) {
			if (other.location != null)
				return false;
		} else if (!location.equals(other.location))
			return false;
		if (phase != other.phase)
			return false;
		if (propertyName == null) {
			if (other.propertyName != null)
				return false;
		} else if (!propertyName.equals(other.propertyName))
			return false;
		if (required != other.required)
			return false;
		if (source == null) {
			if (other.source != null)
				return false;
		} else if (!source.equals(other.source))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

}
