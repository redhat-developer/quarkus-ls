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
package com.redhat.qute.project.datamodel.resolvers;

/**
 * Value resolver for Type-safe Message Bundles support. Those information comes
 * from:
 * 
 * <ul>
 * <li>Java by using @Message:
 * 
 * <code>
 * @MessageBundle
public interface AppMessages {

    &#64;Message("Hello {name ?: 'Qute'}")
    String hello_name(String name);
  </code>
 * 
 * </li>
 * <li>Properties files</li> msg.properties defines
 * 
 * <code>
 * hello_name=Hello {name ?: 'Qute'}
 * </code>
 * </ul>
 * 
 * @author Angelo ZERR
 *
 */
public class MessageMethodValueResolver extends MethodValueResolver implements MessageValueResolver {

	private String locale;

	private String message;

	/**
	 * Returns the locale of the message and null otherwise.
	 * 
	 * @return the locale of the message and null otherwise.
	 */
	public String getLocale() {
		return locale;
	}

	/**
	 * Set the locale.
	 * 
	 * @param locale the locale.
	 */
	public void setLocale(String locale) {
		this.locale = locale;
	}

	/**
	 * Returns the message content and null otherwise.
	 * 
	 * @return the message content and null otherwise.
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Set the message content.
	 * 
	 * @param message the message content.
	 */
	public void setMessage(String message) {
		this.message = message;
	}
}
