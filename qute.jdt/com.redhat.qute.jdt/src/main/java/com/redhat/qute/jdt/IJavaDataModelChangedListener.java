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
package com.redhat.qute.jdt;

import com.redhat.qute.commons.datamodel.JavaDataModelChangeEvent;

/**
 * A Java data model change listener.
 *
 * @author Angelo ZERR
 *
 */
@FunctionalInterface
public interface IJavaDataModelChangedListener {

	void dataModelChanged(JavaDataModelChangeEvent event);
}
