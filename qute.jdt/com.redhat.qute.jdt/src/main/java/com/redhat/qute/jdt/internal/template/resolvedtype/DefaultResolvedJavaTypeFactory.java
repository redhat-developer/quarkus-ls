/*******************************************************************************
* Copyright (c) 2023 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.jdt.internal.template.resolvedtype;

import static com.redhat.qute.jdt.internal.QuteJavaConstants.JAVA_LANG_OBJECT_TYPE;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import com.redhat.qute.commons.InvalidMethodReason;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.commons.datamodel.resolvers.ValueResolverKind;

/**
 * The default {@link ResolvedJavaTypeInfo} factory.
 * 
 * @author Angelo ZERR
 *
 */
public class DefaultResolvedJavaTypeFactory extends AbstractResolvedJavaTypeFactory {

	private static final Logger LOGGER = Logger.getLogger(DefaultResolvedJavaTypeFactory.class.getName());

	private static final IResolvedJavaTypeFactory INSTANCE = new DefaultResolvedJavaTypeFactory();

	public static IResolvedJavaTypeFactory getInstance() {
		return INSTANCE;
	}

	@Override
	public boolean isAdaptedFor(ValueResolverKind kind) {
		return true;
	}

	@Override
	protected boolean isValidField(IField field, IType type) throws JavaModelException {
		if (type.isEnum()) {
			return true;
		}
		return Flags.isPublic(field.getFlags());
	}

	@Override
	protected boolean isValidRecordField(IField field, IType type) {
		return true;
	}

	/**
	 * Returns the reason
	 *
	 * @param method
	 * @param type
	 *
	 * @return
	 *
	 * @see https://github.com/quarkusio/quarkus/blob/ce19ff75e9f732ff731bb30c2141b44b42c66050/independent-projects/qute/core/src/main/java/io/quarkus/qute/ReflectionValueResolver.java#L176
	 */
	@Override
	protected InvalidMethodReason getValidMethodForQute(IMethod method, String typeName) {
		if (JAVA_LANG_OBJECT_TYPE.equals(typeName)) {
			return InvalidMethodReason.FromObject;
		}
		try {
			if ("V".equals(method.getReturnType())) {
				return InvalidMethodReason.VoidReturn;
			}
			if (Flags.isStatic(method.getFlags())) {
				return InvalidMethodReason.Static;
			}
		} catch (JavaModelException e) {
			LOGGER.log(Level.SEVERE, "Error while checking if '" + method.getElementName() + "' is valid.", e);
		}
		return null;
	}
}
