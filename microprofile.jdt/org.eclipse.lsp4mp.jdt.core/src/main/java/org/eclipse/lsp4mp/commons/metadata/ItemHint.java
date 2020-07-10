/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.commons.metadata;

import java.util.List;

/**
 * Configuration item hint.
 * 
 * @author Angelo ZERR
 * 
 * @see https://docs.spring.io/spring-boot/docs/current/reference/html/appendix-configuration-metadata.html
 */
public class ItemHint extends ItemBase {

	private List<ValueHint> values;

	public List<ValueHint> getValues() {
		return values;
	}

	public void setValues(List<ValueHint> values) {
		this.values = values;
	}

	/**
	 * A hint for a value.
	 */
	public static class ValueHint {

		private String value;

		private String description;

		private String sourceType;

		/**
		 * Returns the value.
		 * 
		 * @return the value.
		 */
		public String getValue() {
			return value;
		}

		/**
		 * Returns the converted value by using the given converter.
		 * 
		 * @param converterKind the converter
		 * @return the converted value by using the given converter.
		 */
		public String getValue(ConverterKind converterKind) {
			return ConverterKind.convert(getValue(), converterKind);
		}

		/**
		 * Returns the preferred value according the given converters.
		 * 
		 * @param converterKinds supported converters and null otherwise.
		 * 
		 * @return the preferred value according the given converters.
		 */
		public String getPreferredValue(List<ConverterKind> converterKinds) {
			ConverterKind preferredConverter = converterKinds != null && !converterKinds.isEmpty()
					? converterKinds.get(0)
					: null;
			return getValue(preferredConverter);
		}

		public void setValue(String value) {
			this.value = value;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public String getSourceType() {
			return sourceType;
		}

		public void setSourceType(String sourceType) {
			this.sourceType = sourceType;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((description == null) ? 0 : description.hashCode());
			result = prime * result + ((sourceType == null) ? 0 : sourceType.hashCode());
			result = prime * result + ((value == null) ? 0 : value.hashCode());
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
			ValueHint other = (ValueHint) obj;
			if (description == null) {
				if (other.description != null)
					return false;
			} else if (!description.equals(other.description))
				return false;
			if (sourceType == null) {
				if (other.sourceType != null)
					return false;
			} else if (!sourceType.equals(other.sourceType))
				return false;
			if (value == null) {
				if (other.value != null)
					return false;
			} else if (!value.equals(other.value))
				return false;
			return true;
		}
	}

	/**
	 * Returns the value hint from the given <code>value</code> and supported
	 * converters <code>converterKinds</code> and null otherwise.
	 * 
	 * @param value          the value
	 * @param converterKinds the supported converters.
	 * @return the value hint from the given <code>value</code> and supported
	 *         converters <code>converterKinds</code> and null otherwise.
	 */
	public ValueHint getValue(String value, List<ConverterKind> converterKinds) {
		if (values == null || value == null) {
			return null;
		}
		for (ValueHint valueHint : values) {
			if (converterKinds != null) {
				for (ConverterKind converterKind : converterKinds) {
					if (value.equals(valueHint.getValue(converterKind))) {
						return valueHint;
					}
				}
			} else if (value.equals(valueHint.getValue())) {
				return valueHint;
			}

		}
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((values == null) ? 0 : values.hashCode());
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
		ItemHint other = (ItemHint) obj;
		if (values == null) {
			if (other.values != null)
				return false;
		} else if (!values.equals(other.values))
			return false;
		return true;
	}

}
