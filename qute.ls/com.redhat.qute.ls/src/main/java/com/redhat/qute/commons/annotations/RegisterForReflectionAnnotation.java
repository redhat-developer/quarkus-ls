/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.commons.annotations;

import java.util.List;

import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;

/**
 * '@io.quarkus.runtime.annotations.RegisterForReflection' annotation
 * 
 * This annotation that can be used to force a class to be registered for
 * reflection in native image mode
 */
public class RegisterForReflectionAnnotation {

	/**
	 * If the methods should be registered
	 */
	private Boolean methods;

	/**
	 * If the fields should be registered
	 */
	private Boolean fields;

	/**
	 * Alternative classes that should actually be registered for reflection instead
	 * of the current class.
	 *
	 * This allows for classes in 3rd party libraries to be registered without
	 * modification or writing an extension. If this is set then the class it is
	 * placed on is not registered for reflection, so this should generally just be
	 * placed on an empty class that is not otherwise used.
	 */
	private List<String> targets;

	public boolean isMethods() {
		if (methods == null) {
			return true;
		}
		return methods.booleanValue();
	}

	public void setMethods(Boolean methods) {
		this.methods = methods;
	}

	public boolean isFields() {
		if (fields == null) {
			return true;
		}
		return fields.booleanValue();
	}

	public void setFields(Boolean fields) {
		this.fields = fields;
	}

	public List<String> getTargets() {
		return targets;
	}

	public void setTargets(List<String> targets) {
		this.targets = targets;
	}

	@Override
	public String toString() {
		ToStringBuilder b = new ToStringBuilder(this);
		b.add("fields", this.isFields());
		b.add("methods", this.isMethods());
		b.add("targets", this.getTargets());
		return b.toString();
	}

}
