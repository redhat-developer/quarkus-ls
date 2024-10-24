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

import com.redhat.qute.commons.JavaFieldInfo;
import com.redhat.qute.commons.JavaTypeInfo;
import com.redhat.qute.commons.datamodel.resolvers.ValueResolverKind;
import com.redhat.qute.parser.template.JavaTypeInfoProvider;
import com.redhat.qute.parser.template.Node;

/**
 * Qute field value resolver.
 *
 * <code>
 * &#64;Named
 * private String foo;
 * </code>
 * 
 * becomes :
 * 
 * <code>
 * {inject:foo}
 * </code>
 * 
 *
 * @author Angelo ZERR
 *
 */
public class FieldValueResolver extends JavaFieldInfo implements ValueResolver, JavaTypeInfoProvider {

	private String namespace;

	private String sourceType;

	private String named;

	private Boolean globalVariable;

	private ValueResolverKind kind;

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
	public List<String> getMatchNames() {
		return null;
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
	public ValueResolverKind getKind() {
		return kind;
	}

	public void setKind(ValueResolverKind kind) {
		this.kind = kind;
	}

	@Override
	public String toString() {
		ToStringBuilder b = new ToStringBuilder(this);
		b.add("named", this.getNamed());
		b.add("namespace", this.getNamespace());
		b.add("signature", this.getSignature());
		b.add("sourceType", this.getSourceType());
		b.add("globalVariable", this.isGlobalVariable());
		b.add("kind", this.getKind());
		return b.toString();
	}
}
