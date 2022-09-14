/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.jdt.internal.resolver;

import org.eclipse.jdt.core.IClassFile;

/**
 * Class file type resolver implementation.
 * 
 * @author Angelo ZERR
 *
 */
public class ClassFileTypeResolver extends AbstractTypeResolver {

	public ClassFileTypeResolver(IClassFile classFile) {
		super(classFile.findPrimaryType());
	}

	@Override
	protected String resolveSimpleType(String typeSignature) {
		return typeSignature;
	}

}
