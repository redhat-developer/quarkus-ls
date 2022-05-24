/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
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

import java.util.List;

import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;

import com.redhat.qute.commons.JavaMethodInfo;
import com.redhat.qute.commons.JavaParameterInfo;
import com.redhat.qute.commons.JavaTypeInfo;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.parser.template.JavaTypeInfoProvider;
import com.redhat.qute.parser.template.Node;

/**
 * Qute method value resolver.
 *
 * @author Angelo ZERR
 *
 */
public class MethodValueResolver extends JavaMethodInfo implements ValueResolver, JavaTypeInfoProvider {

	private String named;

	private String namespace;

	private String matchName;

	private String sourceType;

	private String description;

	private List<String> sample;

	private String url;
	
	private Boolean globalVariable;

	@Override
	public boolean isVirtual() {
		return namespace == null;
	}

	@Override
	public String getNamespace() {
		return namespace;
	}

	@Override
	public String getNamed() {
		return named;
	}

	public void setNamed(String named) {
		this.named = named;
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
	public String getMatchName() {
		return matchName;
	}

	public void setMatchName(String matchName) {
		this.matchName = matchName;
	}

	@Override
	public String getMethodName() {
		String name = super.getName();
		return matchName != null ? matchName : name;
	}

	@Override
	public JavaTypeInfo getJavaTypeInfo() {
		JavaTypeInfo javaType = super.getJavaTypeInfo();
		if (javaType == null && sourceType != null) {
			javaType = new JavaTypeInfo();
			javaType.setSignature(sourceType);
			super.setJavaType(javaType);
		}
		return javaType;
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
		if (getNamespace() != null) {
			return getReturnType();
		}
		// Example with following signature:
		// "orEmpty(arg : java.lang.Iterable<T>) : java.util.List<T>"
		JavaParameterInfo parameter = getParameterAt(0); // arg : java.lang.Iterable<T>
		if (parameter == null) {
			return null;
		}
		return resolveReturnType(argType, parameter.getJavaType());
	}

	/**
	 * Returns true if the virual method has a valid name and false otherwise.
	 * 
	 * A valid name is a name which doesn't start with '@' (ex
	 * : @java.lang.Integer(base : int) is not a valid name).
	 * 
	 * @return true if the virual method has a valid name and false otherwise.
	 */
	public boolean isValidName() {
		String name = getName();
		return name.length() > 0 && name.charAt(0) != '@';
	}
	
	public boolean isGlobalVariable() {
		return globalVariable != null && globalVariable.booleanValue();
	}
	
	public void setGlobalVariable(boolean globalVariable) {
		this.globalVariable = globalVariable;
	}

	@Override
	public Node getJavaTypeOwnerNode() {
		return null;
	}

	@Override
	public String getJavaType() {
		return getJavaElementType();
	}

	@Override
	public String toString() {
		ToStringBuilder b = new ToStringBuilder(this);
		b.add("named", this.getNamed());
		b.add("namespace", this.getNamespace());
		b.add("signature", this.getSignature());
		b.add("sourceType", this.getSourceType());
		b.add("description", this.getDescription());
		b.add("sample", this.getSample());
		b.add("url", this.getUrl());
		b.add("globalVariable", this.isGlobalVariable());
		return b.toString();
	}
}
