/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.core.utils;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJarEntryResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;

/**
 * JDT Type utilities.
 * 
 * @author Angelo ZERR
 *
 */
public class JDTTypeUtils {

	private static final List<String> NUMBER_TYPES = Arrays.asList("short", "int", "long", "double", "float");

	public static IType findType(IJavaProject project, String name) {
		try {
			return project.findType(name);
		} catch (JavaModelException e) {
			return null;
		}
	}

	/**
	 * Returns the resolved type name of the
	 * <code>javaElement</code> and null otherwise
	 * 
	 * @param javaElement the Java element
	 * @return the resolved type name of the
	 * <code>javaElement</code> and null otherwise
	 */
	public static String getResolvedTypeName(IJavaElement javaElement) {
		switch (javaElement.getElementType()) {
		case IJavaElement.LOCAL_VARIABLE:
			return getResolvedTypeName((ILocalVariable) javaElement);
		case IJavaElement.FIELD:
			return getResolvedTypeName((IField) javaElement);
		default:
			return null;
		}
	}

	/**
	 * Returns the resolved type name of the given
	 * <code>localVar</code> and null otherwise
	 * 
	 * @param localVar the local variable
	 * @return the resolved type name of the given
	 * <code>localVar</code> and null otherwise
	 */
	public static String getResolvedTypeName(ILocalVariable localVar) {
		try {
			String signature = localVar.getTypeSignature().replace("/", ".");
			IType primaryType = localVar.getTypeRoot().findPrimaryType();
			return JavaModelUtil.getResolvedTypeName(signature, primaryType);
		} catch (JavaModelException e) {
			return null;
		}
	}

	/**
	 * Returns the resolved type name of the
	 * given <code>field</code> and null otherwise
	 * 
	 * @param field the field
	 * @return the resolved type name of the
	 * given <code>field</code> and null otherwise
	 */
	public static String getResolvedTypeName(IField field) {
		try {
			String signature = field.getTypeSignature();
			IType primaryType = field.getTypeRoot().findPrimaryType();
			return JavaModelUtil.getResolvedTypeName(signature, primaryType);
		} catch (JavaModelException e) {
			return null;
		}
	}

	/**
	 * Returns the resolved return type name of the
	 * given <code>method</code> and null otherwise
	 * 
	 * @param method the method
	 * @return the resolved return type name of the
	 * given <code>method</code> and null otherwise
	 */
	public static String getResolvedResultTypeName(IMethod method) {
		try {
			String signature = method.getReturnType();
			IType primaryType = method.getTypeRoot().findPrimaryType();
			return JavaModelUtil.getResolvedTypeName(signature, primaryType);
		} catch (JavaModelException e) {
			return null;
		}
	}

	public static String getDefaultValue(IMethod method) {
		try {
			IMemberValuePair defaultValue = method.getDefaultValue();
			if (defaultValue == null || defaultValue.getValue() == null) {
				return null;
			}
			switch (defaultValue.getValueKind()) {
			case IMemberValuePair.K_BOOLEAN:
			case IMemberValuePair.K_INT:
			case IMemberValuePair.K_LONG:
			case IMemberValuePair.K_SHORT:
			case IMemberValuePair.K_DOUBLE:
			case IMemberValuePair.K_FLOAT:
			case IMemberValuePair.K_STRING:
				String value = defaultValue.getValue().toString();
				return value.isEmpty() ? null : value;
			case IMemberValuePair.K_QUALIFIED_NAME:
			case IMemberValuePair.K_SIMPLE_NAME:
				String qualifiedName = defaultValue.getValue().toString();
				int index = qualifiedName.lastIndexOf('.');
				return index != -1 ? qualifiedName.substring(index + 1, qualifiedName.length()) : qualifiedName;
			case IMemberValuePair.K_UNKNOWN:
				return null;
			default:
				return null;
			}
		} catch (JavaModelException e) {
			return null;
		}
	}

	public static String getPropertyType(IType type, String typeName) {
		return type != null ? type.getFullyQualifiedName('.') : typeName;
	}

	/**
	 * Returns true if the given <code>javaElement</code>
	 * is from a Java binary, and false otherwise
	 * 
	 * @param javaElement the Java element
	 * @return true if the given <code>javaElement</code>
	 * is from a Java binary, and false otherwise
	 */
	public static boolean isBinary(IJavaElement javaElement) {
		if (javaElement instanceof IMember) {
			return ((IMember) javaElement).isBinary();
		} else if (javaElement instanceof ILocalVariable) {
			return isBinary(((ILocalVariable) javaElement).getDeclaringMember());
		}
		return false;
	}

	/**
	 * Returns the source type of the given
	 * <code>javaElement</code> and null otherwise
	 * 
	 * @param javaElement the Java element
	 * @return the source type of the <code>javaElement</code>
	 */
	public static String getSourceType(IJavaElement javaElement) {
		switch (javaElement.getElementType()) {
		case IJavaElement.LOCAL_VARIABLE:
			return getSourceType((ILocalVariable) javaElement);
		case IJavaElement.FIELD:
			return getSourceType((IField) javaElement);
		case IJavaElement.METHOD:
			return getSourceType((IMethod) javaElement);
		default:
			return null;
		}
	}

	/**
	 * Returns the source type of the given local
	 * variable <code>member</code> and null otherwise
	 * 
	 * @param member the local variable to get the source type from
	 * @return the source type of the given local
	 * variable <code>member</code> and null otherwise
	 */
	public static String getSourceType(ILocalVariable member) {
		return getSourceType(member.getDeclaringMember());
	}

	/**
	 * Returns the source type of the given <code>member</code>
	 * and null otherwise
	 * 
	 * @param member the member
	 * @return the source type of the given <code>member</code>
	 * and null otherwise
	 */
	public static String getSourceType(IMember member) {
		return getPropertyType(member.getDeclaringType(), null);
	}

	/**
	 * Returns the source field of the given <code>field</code>
	 * 
	 * @param field the field
	 * @return the source field of the given <code>field</code>
	 */
	public static String getSourceField(IJavaElement field) {
		return field.getElementName();
	}

	public static String getSourceMethod(IMethod method) throws JavaModelException {
		return method.getElementName() + method.getSignature();
	}

	public static boolean isOptional(String fieldTypeName) {
		return fieldTypeName.startsWith("java.util.Optional");
	}

	/**
	 * Returns the enclosed type declared in the given <code>typeName</code> and
	 * null otherwise.
	 * 
	 * @param typeName
	 * @return
	 */
	public static String getOptionalTypeParameter(String typeName) {
		if (!isOptional(typeName)) {
			return null;
		}
		int start = typeName.indexOf('<');
		if (start == -1) {
			return null;
		}
		// the type name follows the signature java.util.Optional<MyType>
		// extract the enclosed type MyType.
		int end = typeName.lastIndexOf('>');
		return typeName.substring(start + 1, end);
	}

	public static IType getEnclosedType(IType type, String typeName, IJavaProject javaProject)
			throws JavaModelException {
		// type name is the string of the JDT type (which could be null if type is not
		// retrieved)
		String enclosedType = typeName;
		if (type == null) {
			// JDT type is null, in some case it's because type is optional (ex :
			// java.util.Optional<MyType>)
			// try to extract the enclosed type from the optional type (to get 'MyType' )
			enclosedType = getOptionalTypeParameter(typeName);
			if (enclosedType != null) {
				type = findType(javaProject, enclosedType);
			}
		}
		return type;
	}

	public static String[] getRawTypeParameters(String fieldTypeName) {
		int start = fieldTypeName.indexOf("<") + 1;
		int end = fieldTypeName.lastIndexOf(">");
		String keyValue = fieldTypeName.substring(start, end);
		int index = keyValue.indexOf(',');
		return new String[] { keyValue.substring(0, index), keyValue.substring(index + 1, keyValue.length()) };
	}

	public static boolean isPrimitiveType(String valueClass) {
		return valueClass.equals("java.lang.String") || valueClass.equals("java.lang.Boolean")
				|| valueClass.equals("java.lang.Integer") || valueClass.equals("java.lang.Long")
				|| valueClass.equals("java.lang.Double") || valueClass.equals("java.lang.Float");
	}

	public static boolean isMap(String mapValueClass) {
		return mapValueClass.startsWith("java.util.Map");
	}

	public static boolean isList(String valueClass) {
		return valueClass.startsWith("java.util.List");
	}

	public static boolean isNumber(String valueClass) {
		return NUMBER_TYPES.contains(valueClass);
	}

	public static boolean isPrimitiveBoolean(String valueClass) {
		return valueClass.equals("boolean");
	}

	public static IJarEntryResource findPropertiesResource(IPackageFragmentRoot packageRoot, String propertiesFileName)
			throws JavaModelException {
		Object[] resources = packageRoot.getNonJavaResources();
		if (resources != null) {
			for (Object object : resources) {
				if (object instanceof IJarEntryResource) {
					IJarEntryResource res = (IJarEntryResource) object;
					if ("META-INF".equals(res.getName())) {
						IJarEntryResource[] children = res.getChildren();
						if (children != null) {
							for (IJarEntryResource r : children) {
								if (propertiesFileName.equals(r.getName())) {
									return r;
								}
							}
						}
						return null;
					}
				}
			}
		}
		return null;
	}

	public static boolean isSimpleFieldType(IType type, String typeName) throws JavaModelException {
		return type == null || isPrimitiveType(typeName) || isList(typeName) || isMap(typeName) || isOptional(typeName)
				|| (type != null && type.isEnum());
	}

	public static boolean overlaps(ISourceRange typeRange, ISourceRange methodRange) {
		if (typeRange == null || methodRange == null) {
			return false;
		}
		// method range is overlapping if it appears before or actually overlaps the
		// type's range
		return methodRange.getOffset() < typeRange.getOffset() || methodRange.getOffset() >= typeRange.getOffset()
				&& methodRange.getOffset() <= (typeRange.getOffset() + typeRange.getLength());
	}
}
