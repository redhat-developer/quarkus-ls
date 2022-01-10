package com.redhat.qute.commons.resolvers;

import com.redhat.qute.commons.JavaParameterInfo;

public interface ValueResolver {

	JavaParameterInfo getMatchParameter();

	boolean isMethod();

	String getName();

}
