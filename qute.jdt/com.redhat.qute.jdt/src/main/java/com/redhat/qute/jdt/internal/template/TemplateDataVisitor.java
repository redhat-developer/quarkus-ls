package com.redhat.qute.jdt.internal.template;

import java.util.List;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodInvocation;

public abstract class TemplateDataVisitor extends ASTVisitor {

	private static final String DATA_METHOD = "data";

	protected static final String THIS_PARAMETER_NAME = "this";

	private IMethod method;

	@Override
	public boolean visit(MethodInvocation node) {
		String methodName = node.getName().getIdentifier();
		if (DATA_METHOD.equals(methodName)) {
			// .data("book", book)
			return visitDataMethodInvocation(node);
		}
		return super.visit(node);
	}

	private boolean visitDataMethodInvocation(MethodInvocation node) {
		@SuppressWarnings("rawtypes")
		List arguments = node.arguments();
		if (arguments.size() == 1) {
			// One parameter
			Object paramType = arguments.get(0);
			boolean result = visitParameter(THIS_PARAMETER_NAME, paramType);
			if (!result) {
				return false;
			}
		} else {
			// Several parameters
			Object paramName = null;
			for (int i = 0; i < arguments.size(); i++) {
				if (i % 2 == 0) {
					paramName = null;
					paramName = arguments.get(i);
				} else {
					if (paramName != null) {
						Object paramType = arguments.get(i);
						boolean result = visitParameter(paramName, paramType);
						if (!result) {
							return false;
						}
					}
				}
			}
		}
		return true;
	}

	public void setMethod(IMethod method) {
		this.method = method;
	}

	public IMethod getMethod() {
		return method;
	}

	protected abstract boolean visitParameter(Object paramName, Object paramType);

}
