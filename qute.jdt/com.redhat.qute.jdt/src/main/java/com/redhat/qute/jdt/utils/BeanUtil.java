package com.redhat.qute.jdt.utils;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;

/**
 * 
 * @author V. Kabanovich
 *
 */
public class BeanUtil {
	public static final String GET = "get"; //$NON-NLS-1$
	public static final String SET = "set"; //$NON-NLS-1$
	public static final String IS = "is"; //$NON-NLS-1$

	public static boolean isGetter(String methodName, int numberOfParameters) {
		return (((methodName.startsWith(GET) && !methodName.equals(GET))
				|| (methodName.startsWith(IS) && !methodName.equals(IS))) && numberOfParameters == 0);
	}

	public static boolean isSetter(String methodName, int numberOfParameters) {
		return (((methodName.startsWith(SET) && !methodName.equals(SET))) && numberOfParameters == 1);
	}

	public static boolean isGetter(IMethod method) {
		return method != null && isGetter(method.getElementName(), method.getNumberOfParameters())
				&& checkPropertyReturnType(method);
	}

	public static boolean checkPropertyReturnType(String typeName, String methodName) {
		if (typeName == null || typeName.equals("void")) { //$NON-NLS-1$
			return false;
		}
		if (methodName.startsWith(BeanUtil.IS)) {
			if (!"boolean".equals(typeName) && !"java.lang.Boolean".equals(typeName)) { //$NON-NLS-1$ //$NON-NLS-2$
				return false;
			}
		}
		return true;
	}

	private static boolean checkPropertyReturnType(IMethod method) {
		return method != null && checkPropertyReturnType(getMemberTypeAsString(method), method.getElementName());
	}

	public static boolean isSetter(IMethod method) {
		return method != null && isSetter(method.getElementName(), method.getNumberOfParameters());
	}

	public static String getPropertyName(String methodName) {
		if (isGetter(methodName, 0) || isSetter(methodName, 1)) {
			StringBuffer name = new StringBuffer(methodName);
			if (methodName.startsWith(IS)) {
				name.delete(0, 2);
			} else {
				name.delete(0, 3);
			}
			if (name.length() < 2 || !Character.isUpperCase(name.charAt(1))) {
				name.setCharAt(0, Character.toLowerCase(name.charAt(0)));
			}
			return name.toString();
		}
		return null;
	}

	/**
	 * Converts Java Class Name to name of Bean
	 * 
	 * @param className is short name or fully qualified name
	 * @return Bean Name
	 */
	public static String getDefaultBeanName(String className) {
		int lastDotPosition = className.lastIndexOf("."); //$NON-NLS-1$
		if (lastDotPosition >= 0 && className.length() > lastDotPosition) {
			className = className.substring(lastDotPosition + 1);
		}
		if (className.length() > 0) {
			className = className.substring(0, 1).toLowerCase() + className.substring(1);
		}
		return className;
	}

	/**
	 * Returns name of Bean for the given IType
	 * 
	 * @param type
	 * @return Bean Name
	 */
	public static String getDefaultBeanName(IType type) {
		return getDefaultBeanName(type.getElementName());
	}

	/**
	 * Converts name of Bean to Java Class Name
	 * 
	 * @param beanName is short name or fully qualified name
	 * @return Java Class Name
	 */
	public static String getClassName(String beanName) {
		int lastDotPosition = beanName.lastIndexOf("."); //$NON-NLS-1$
		String beforeLastDot = "";
		if (lastDotPosition >= 0 && beanName.length() > lastDotPosition) {
			beforeLastDot = beanName.substring(0, lastDotPosition + 1);
			lastDotPosition++;
		} else {
			lastDotPosition = 0;
		}
		if (beanName.length() > lastDotPosition) {
			beanName = beforeLastDot + beanName.substring(lastDotPosition, lastDotPosition + 1).toUpperCase()
					+ beanName.substring(lastDotPosition + 1);
		}
		return beanName;
	}

	private static String getMemberTypeAsString(IMethod m) {
		if (m == null)
			return null;
		try {
			return resolveTypeAsString(m.getDeclaringType(), m.getReturnType());
		} catch (JavaModelException e) {
			// CommonCorePlugin.getPluginLog().logError(e);
		}
		return null;
	}

	private static String resolveTypeAsString(IType type, String typeName) {
		if (type == null || typeName == null)
			return null;
		typeName = new String(Signature.toCharArray(typeName.toCharArray()));
		int i = typeName.indexOf(Signature.C_GENERIC_START);
		if (i > 0)
			typeName = typeName.substring(0, i);
		return resolveType(type, typeName);
	}

	private static String resolveType(IType type, String typeName) {
		try {
			String resolvedArray[][] = type.resolveType(typeName);
//			resolvedArray == null for primitive types
			if (resolvedArray == null)
				return typeName;
			typeName = ""; //$NON-NLS-1$
			for (int i = 0; i < resolvedArray[0].length; i++)
				typeName += (!"".equals(typeName) ? "." : "") + resolvedArray[0][i]; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			return typeName;
		} catch (JavaModelException e) {
			// CommonCorePlugin.getPluginLog().logError(e);
		} catch (IllegalArgumentException e) {
			// CommonCorePlugin.getPluginLog().logError(e);
		}
		return null;
	}
}