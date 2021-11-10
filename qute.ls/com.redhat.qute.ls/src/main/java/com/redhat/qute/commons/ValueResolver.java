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
package com.redhat.qute.commons;

import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;

/**
 * Qute value resolver.
 * 
 * @author Angelo ZERR
 *
 */
public class ValueResolver extends JavaMethodInfo {

	private String namespace;

	private String sourceType;

	public boolean match(ResolvedJavaTypeInfo javaType) {
		JavaMethodParameterInfo parameter = getParameterAt(0);
		if (parameter == null) {
			return false;
		}
		String parameterType = parameter.getType();
		if (parameterType.equals(javaType.getClassName())) {
			return true;
		}
		if (javaType.getExtendedTypes() != null) {
			for (String extendedType : javaType.getExtendedTypes()) {
				if (parameterType.equals(extendedType)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns the namespace of the resolver and null otherwise.
	 * 
	 * @return the namespace of the resolver and null otherwise.
	 */
	public String getNamespace() {
		return namespace;
	}

	/**
	 * Set the namespace of the resolver and null otherwise.
	 * 
	 * @param namespace the namespace of the resolver and null otherwise.
	 */
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	@Override
	public String getSourceType() {
		return sourceType;
	}

	/**
	 * Set the source type.
	 * 
	 * @param sourceType the source type.
	 */
	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}

	/**
	 * Returns true if the given property matches the resolver and false otherwise.
	 * 
	 * @param property the property to match
	 * 
	 * @return true if the given property matches the resolver and false otherwise.
	 */
	public boolean match(String property) {
		return ResolvedJavaTypeInfo.isMatchMethod(this, property);
	}

	@Override
	public boolean hasParameters() {
		if (namespace != null) {
			return !getParameters().isEmpty();
		}
		return getParameters().size() - 1 > 0;
	}

	@Override
	public String getSignature() {
		String signature = super.getSignature();
		return namespace != null ? namespace + ":" + signature : signature;
	}

	@Override
	public String toString() {
		ToStringBuilder b = new ToStringBuilder(this);
		b.add("namespace", this.namespace);
		b.add("signature", this.getSignature());
		b.add("sourceType", this.sourceType);
		return b.toString();
	}
}
