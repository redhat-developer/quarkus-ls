package com.redhat.qute.project.extensions.roq.data;

import com.redhat.qute.commons.ResolvedJavaTypeInfo;

public class ArrayDataMapping extends ResolvedJavaTypeInfo {

	public ArrayDataMapping(ResolvedJavaTypeInfo iterableType) {
		super.setResolvedType(iterableType);
		super.setSignature("");
	}

	@Override
	public boolean isIterable() {
		return true;
	}

	@Override
	public String getIterableOf() {
		return "";
	}

}
