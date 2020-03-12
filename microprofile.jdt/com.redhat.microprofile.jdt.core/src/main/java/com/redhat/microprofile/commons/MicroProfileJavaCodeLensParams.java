/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.commons;

/**
 * MicroProfile Java codelens parameters.
 * 
 * @author Angelo ZERR
 *
 */
public class MicroProfileJavaCodeLensParams {

	private String uri;
	private boolean urlCodeLensEnabled;
	private Integer localServerPort;
	private String openURICommand;
	private boolean checkServerAvailable;

	private transient String localBaseURL;

	public MicroProfileJavaCodeLensParams() {

	}

	public MicroProfileJavaCodeLensParams(String uri) {
		this();
		setUri(uri);
	}

	/**
	 * Returns the java file uri.
	 * 
	 * @return the java file uri.
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * Set the java file uri.
	 * 
	 * @param uri the java file uri.
	 */
	public void setUri(String uri) {
		this.uri = uri;
	}

	/**
	 * Returns true if url codelens is enabled and false otherwise.
	 * 
	 * @return true if url codelens is enabled and false otherwise.
	 */
	public boolean isUrlCodeLensEnabled() {
		return urlCodeLensEnabled;
	}

	/**
	 * Set true if url codelens is enabled and false otherwise.
	 * 
	 * @param urlCodeLensEnabled the url codelens enabled.
	 */
	public void setUrlCodeLensEnabled(boolean urlCodeLensEnabled) {
		this.urlCodeLensEnabled = urlCodeLensEnabled;
	}

	/**
	 * Returns the local server port.
	 * 
	 * @return the local server port.
	 */
	public Integer getLocalServerPort() {
		return localServerPort;
	}

	/**
	 * Set the local server port
	 * 
	 * @param localServerPort the local server port
	 */
	public void setLocalServerPort(Integer localServerPort) {
		this.localServerPort = localServerPort;
		this.localBaseURL = null;
	}

	/**
	 * Returns the local base URL.
	 * 
	 * @return the local base URL.
	 */
	public String getLocalBaseURL() {
		if (localBaseURL == null) {
			localBaseURL = new StringBuilder("http://localhost:").append(getLocalServerPort()).toString();
		}
		return localBaseURL;
	}

	/**
	 * Returns the open URI command ID and null if it is not supported on client
	 * side.
	 * 
	 * @return the open URI command ID and null if it is not supported on client
	 *         side.
	 */
	public String getOpenURICommand() {
		return openURICommand;
	}

	/**
	 * Set the open URI command ID and null if it is not supported on client side.
	 * 
	 * @param openURICommand the open URI command ID
	 */
	public void setOpenURICommand(String openURICommand) {
		this.openURICommand = openURICommand;
	}

	/**
	 * Returns true if check of server available to show URL codelens must be done
	 * and false otherwise.
	 * 
	 * @return true if check of server available to show URL codelens must be done
	 *         and false otherwise.
	 */
	public boolean isCheckServerAvailable() {
		return checkServerAvailable;
	}

	/**
	 * Sets true if check of server available to show URL codelens must be done and
	 * false otherwise.
	 * 
	 * @param checkServerAvailable true if check of server available to show URL
	 *                             codelens must be done and false otherwise.
	 */
	public void setCheckServerAvailable(boolean checkServerAvailable) {
		this.checkServerAvailable = checkServerAvailable;
	}
}
