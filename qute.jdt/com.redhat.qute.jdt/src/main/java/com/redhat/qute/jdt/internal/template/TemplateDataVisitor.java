package com.redhat.qute.jdt.internal.template;

import java.util.List;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodInvocation;

public abstract class TemplateDataVisitor extends ASTVisitor {

	private static final String DATA_METHOD = "data";

	private IMethod method;

	@Override
	public boolean visit(MethodInvocation node) {
		String methodName = node.getName().getIdentifier();
		if (DATA_METHOD.equals(methodName)) {
			// .data("book", book)
			@SuppressWarnings("rawtypes")
			List arguments = node.arguments();
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
		return super.visit(node);
	}

	public void setMethod(IMethod method) {
		this.method = method;
	}

	public IMethod getMethod() {
		return method;
	}
	
	protected abstract boolean visitParameter(Object paramName, Object paramType);

}
