package com.redhat.qute.commons.resolvers;

import com.redhat.qute.commons.JavaFieldInfo;
import com.redhat.qute.commons.JavaParameterInfo;

public class FieldValueResolver extends JavaFieldInfo implements ValueResolver {

	private String applyFor;
	
	@Override
	public JavaParameterInfo getMatchParameter() {
		if(applyFor == null) {
			return null;
		}
		JavaParameterInfo param = new JavaParameterInfo(null, applyFor);
		return param;
	}

	@Override
	public boolean isMethod() {
		return false;
	}

}
