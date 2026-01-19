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
package com.redhat.qute.commons;

/**
 * Enum representing a project feature supported by the Qute Language Server.
 * 
 * <p>This enum is used to indicate which features or frameworks a Qute project
 * has enabled or is compatible with. Examples of features include:
 * <ul>
 *     <li>{@link #Renarde} - Support for Renarde templates</li>
 *     <li>{@link #Roq} - Support for Roq templates</li>
 * </ul>
 * 
 * <p>Features are typically used in {@link com.redhat.qute.commons.ProjectInfo}
 * to describe the capabilities of a Qute project.
 * 
 * <p>Example usage:
 * <pre>{@code
 * Set<ProjectFeature> features = EnumSet.of(ProjectFeature.Renarde, ProjectFeature.Roq);
 * projectInfo.setFeatures(features);
 * }</pre>
 * 
 * <p>Note: if integer values are used via {@link #getValue()} and {@link #forValue(int)},
 * they can be used for serialization or compact representation.
 * 
 * @author Angelo ZERR
 */
public enum ProjectFeature {

    /** Support for Renarde templates. */
    Renarde(0),

    /** Support for Roq templates. */
    Roq(1);

    /** Integer value of the feature, useful for serialization. */
    private final int value;

    /**
     * Constructor for the enum.
     * 
     * @param value integer value associated with the feature
     */
    ProjectFeature(int value) {
        this.value = value;
    }

    /**
     * Returns the integer value of this feature.
     * 
     * @return integer value of the feature
     */
    public int getValue() {
        return value;
    }

    /**
     * Returns the {@link ProjectFeature} corresponding to a given integer value.
     * 
     * @param value integer value of the feature (starting at 0)
     * @return corresponding ProjectFeature
     * @throws IllegalArgumentException if the value does not correspond to any feature
     */
    public static ProjectFeature forValue(int value) {
        ProjectFeature[] allValues = ProjectFeature.values();
        for (ProjectFeature f : allValues) {
            if (f.getValue() == value) {
                return f;
            }
        }
        throw new IllegalArgumentException("Illegal enum value: " + value);
    }
}
