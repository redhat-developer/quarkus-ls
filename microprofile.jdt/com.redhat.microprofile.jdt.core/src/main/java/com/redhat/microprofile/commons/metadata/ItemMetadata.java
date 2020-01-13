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

/**
 * Configuration item metadata.
 * 
 * @author Angelo ZERR
 * 
 * @see https://docs.spring.io/spring-boot/docs/current/reference/html/appendix-configuration-metadata.html
 */
public class ItemMetadata extends ItemBase {

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

	private String type;

	private String sourceField;

	private String sourceMethod;

	private String defaultValue;

	private String extensionName;
	private boolean required;
	private int phase;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSourceField() {
		return sourceField;
	}

	public void setSourceField(String sourceField) {
		this.sourceField = sourceField;
	}

	public String getSourceMethod() {
		return sourceMethod;
	}

	public void setSourceMethod(String sourceMethod) {
		this.sourceMethod = sourceMethod;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
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

	public int getPhase() {
		return phase;
	}

	public void setPhase(int phase) {
		this.phase = phase;
	}

	public boolean isAvailableAtRun() {
		return phase == CONFIG_PHASE_BUILD_AND_RUN_TIME_FIXED || phase == CONFIG_PHASE_RUN_TIME;
	}

	public boolean isStringType() {
		return "java.lang.String".equals(getType()) || //
				"java.util.Optional<java.lang.String>".equals(getType());
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

	/**
	 * Returns the paths of the metadata.
	 * 
	 * @return the paths of the metadata.
	 */
	public String[] getPaths() {
		String name = getName();
		if (name != null) {
			return name.split("//.");
		}
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((defaultValue == null) ? 0 : defaultValue.hashCode());
		result = prime * result + ((extensionName == null) ? 0 : extensionName.hashCode());
		result = prime * result + phase;
		result = prime * result + (required ? 1231 : 1237);
		result = prime * result + ((sourceField == null) ? 0 : sourceField.hashCode());
		result = prime * result + ((sourceMethod == null) ? 0 : sourceMethod.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ItemMetadata other = (ItemMetadata) obj;
		if (defaultValue == null) {
			if (other.defaultValue != null)
				return false;
		} else if (!defaultValue.equals(other.defaultValue))
			return false;
		if (extensionName == null) {
			if (other.extensionName != null)
				return false;
		} else if (!extensionName.equals(other.extensionName))
			return false;
		if (phase != other.phase)
			return false;
		if (required != other.required)
			return false;
		if (sourceField == null) {
			if (other.sourceField != null)
				return false;
		} else if (!sourceField.equals(other.sourceField))
			return false;
		if (sourceMethod == null) {
			if (other.sourceMethod != null)
				return false;
		} else if (!sourceMethod.equals(other.sourceMethod))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

}
