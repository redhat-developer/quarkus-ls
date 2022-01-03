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
package com.redhat.qute.project;

import com.redhat.qute.commons.JavaMemberInfo;

/**
 * The search result of Java member.
 * 
 * @author Angelo ZERR
 *
 */
public class JavaMemberResult {

	private JavaMemberInfo member;
	private boolean matchParameters;
	private boolean matchVirtualMethod;

	/**
	 * Returns the Java member which matches the part name and null otherwise.
	 * 
	 * @return the Java member which matches the part name and null otherwise.
	 */
	public JavaMemberInfo getMember() {
		return member;
	}

	/**
	 * Set the Java member which matches the part name and null otherwise.
	 * 
	 * @param member the Java member which matches the part name and null otherwise.
	 */
	public void setMember(JavaMemberInfo member) {
		this.member = member;
	}

	/**
	 * Returns true if the Java method matches the parameters types and false
	 * otherwise.
	 * 
	 * @return true if the Java method matches the parameters types and false
	 *         otherwise.
	 */
	public boolean isMatchParameters() {
		return matchParameters;
	}

	/**
	 * Set true if the Java method matches the parameters types and false otherwise.
	 * 
	 * @param matchParameters true if the Java method matches the parameters types
	 *                        and false otherwise.
	 */
	public void setMatchParameters(boolean matchParameters) {
		this.matchParameters = matchParameters;
	}

	/**
	 * Returns true if the first parameter type of the method matches the type of
	 * the base object type to search and false otherwise.
	 * 
	 * @return true if the first parameter type of the method matches the type of
	 *         the base object type to search and false otherwise.
	 */
	public boolean isMatchVirtualMethod() {
		return matchVirtualMethod;
	}

	/**
	 * Set true if the first parameter type of the method matches the type of the
	 * base object type to search and false otherwise.
	 * 
	 * @param matchVirtualMethod true if the first parameter type of the method
	 *                           matches the type of the base object type to search
	 *                           and false otherwise.
	 */
	public void setMatchVirtualMethod(boolean matchVirtualMethod) {
		this.matchVirtualMethod = matchVirtualMethod;
	}

}
