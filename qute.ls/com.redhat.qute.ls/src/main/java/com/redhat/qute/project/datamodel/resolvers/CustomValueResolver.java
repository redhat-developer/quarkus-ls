package com.redhat.qute.project.datamodel.resolvers;

import java.util.List;

import com.redhat.qute.commons.JavaElementKind;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.commons.datamodel.resolvers.ValueResolverKind;
import com.redhat.qute.parser.template.JavaTypeInfoProvider;
import com.redhat.qute.parser.template.Node;

public abstract class CustomValueResolver extends ResolvedJavaTypeInfo implements ValueResolver, JavaTypeInfoProvider {

	private String namespace;

	@Override
	public String getNamed() {
		return null;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	@Override
	public String getNamespace() {
		return namespace;
	}

	@Override
	public List<String> getMatchNames() {
		return null;
	}

	@Override
	public String getSignature() {
		return null;
	}

	@Override
	public String getSourceType() {
		return null;
	}

	@Override
	public boolean isGlobalVariable() {
		return false;
	}

	@Override
	public JavaElementKind getJavaElementKind() {
		return JavaElementKind.CUSTOM;
	}

	@Override
	public ValueResolverKind getKind() {
		return ValueResolverKind.File;
	}

	@Override
	public String getJavaElementType() {
		return null;
	}

	@Override
	public Node getJavaTypeOwnerNode() {
		return null;
	}
}
