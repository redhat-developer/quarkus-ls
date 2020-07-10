/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.jdt.core;

import org.eclipse.lsp4mp.commons.MicroProfilePropertiesChangeEvent;

/**
 * A MicroProfile properties change listener.
 * 
 * @author Angelo ZERR
 *
 */
@FunctionalInterface
public interface IMicroProfilePropertiesChangedListener {

	void propertiesChanged(MicroProfilePropertiesChangeEvent event);
}
