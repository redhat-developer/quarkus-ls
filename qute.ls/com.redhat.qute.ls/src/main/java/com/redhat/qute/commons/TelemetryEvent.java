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
package com.redhat.qute.commons;

public class TelemetryEvent {
    private String name;
    private Object properties;

    public TelemetryEvent(String name, Object properties) {
        this.name = name;
        this.properties = properties;
    }

    /**
     * @return the name for the telemetry action performed
     */
    public String getName() {
        return name;
    }

    /**
     * @return the optional properties associated with this event
     */
    public Object getProperties() {
        return properties;
    }
}
