package com.redhat.qute.commons;

public interface JavaTypeResolver {

	 ResolvedJavaTypeInfo resolveJavaTypeSync(String className);
}
