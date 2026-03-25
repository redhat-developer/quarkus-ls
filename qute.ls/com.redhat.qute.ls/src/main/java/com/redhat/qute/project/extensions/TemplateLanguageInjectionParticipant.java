/*******************************************************************************
* Copyright (c) 2026 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.project.extensions;

import java.nio.file.Path;
import java.util.Collection;

import com.redhat.qute.parser.injection.InjectionDetector;

public interface TemplateLanguageInjectionParticipant extends BaseParticpant {

	Collection<InjectionDetector> getInjectionDetectorsFor(Path path);

}
