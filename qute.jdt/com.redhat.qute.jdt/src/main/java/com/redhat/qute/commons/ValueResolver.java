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

import java.util.List;

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

	private String description;

	private List<String> sample;

	private String url;

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
	 * Returns the description of the value resolver and null otherwise.
	 * 
	 * @return the description of the value resolver and null otherwise.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Set the description of the value resolver.
	 * 
	 * @param description the description of the value resolver.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Returns a sample with the value resolver and null otherwise.
	 * 
	 * @return a sample with the value resolver and null otherwise.
	 */
	public List<String> getSample() {
		return sample;
	}

	/**
	 * Set a sample with the value resolver.
	 * 
	 * @param sample a sample with the value resolver.
	 */
	public void setSample(List<String> sample) {
		this.sample = sample;
	}

	/**
	 * Returns the documentation Url of the value resolver and null otherwise.
	 * 
	 * @return the documentation Url of the value resolver and null otherwise.
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Set the documentation Url of the value resolver.
	 * 
	 * @param url the documentation Url of the value resolver.
	 */
	public void setUrl(String url) {
		this.url = url;
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

	/**
	 * Returns the resolved type if return type has generic (ex : T,
	 * java.util.List<T>) by using the given java type argument.
	 * 
	 * @param argType argument Java type.
	 * 
	 * @return the resolved type if return type has generic (ex : T,
	 *         java.util.List<T>) by using the given java type argument.
	 */
	@Override
	public String resolveJavaElementType(ResolvedJavaTypeInfo argType) {
		// Example with following signature:
		// "orEmpty(arg : java.lang.Iterable<T>) : java.util.List<T>"
		JavaParameterInfo parameter = getParameterAt(0); // arg : java.lang.Iterable<T>
		if (parameter == null) {
			return null;
		}
		return resolveReturnType(argType, parameter.getJavaType());
	}

	@Override
	public String toString() {
		ToStringBuilder b = new ToStringBuilder(this);
		b.add("namespace", this.getNamespace());
		b.add("signature", this.getSignature());
		b.add("sourceType", this.getSourceType());
		b.add("description", this.getDescription());
		b.add("sample", this.getSample());
		b.add("url", this.getUrl());
		return b.toString();
	}
}
