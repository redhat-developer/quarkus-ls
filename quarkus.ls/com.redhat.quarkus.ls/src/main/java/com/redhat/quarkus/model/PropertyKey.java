/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.quarkus.model;

/**
 * The property key node
 * 
 * @author Angelo ZERR
 *
 */
public class PropertyKey extends Node {

	@Override
	public NodeType getNodeType() {
		return NodeType.PROPERTY_KEY;
	}

	/**
	 * Returns the profile of the property key and null otherwise.
	 * 
	 * <ul>
	 * <li>'%dev.key' will return 'dev'.</li>
	 * <li>'key' will return null.</li>
	 * </ul>
	 * 
	 * @return the profile of the property key and null otherwise.
	 */
	public String getProfile() {
		int profileEndOffset = getEndProfileOffset();
		if (profileEndOffset != -1) {
			String fulltext = getOwnerModel().getText();
			return fulltext.substring(getStart() + 1, profileEndOffset);
		}
		return null;
	}

	/**
	 * Returns the property name without the profile of the property key and null
	 * otherwise.
	 * 
	 * <ul>
	 * <li>'%dev.' will return null.</li>
	 * <li>'%dev.key' will return 'key'.</li>
	 * <li>'key' will return 'key'.</li>
	 * </ul>
	 * 
	 * @return the property name without the profile of the property key and null
	 *         otherwise.
	 */
	public String getPropertyName() {
		int profileEndOffset = getEndProfileOffset();
		if (profileEndOffset != -1) {
			int end = getEnd();
			if (profileEndOffset < end) {
				String fulltext = getOwnerModel().getText();
				return fulltext.substring(profileEndOffset + 1, end);
			}
			return null;
		}
		return getText();
	}

	/**
	 * Returns true if the given offset is before the profile and false otherwise.
	 * 
	 * @param offset the offset
	 * @return true if the given offset is before the profile and false otherwise.
	 */
	public boolean isBeforeProfile(int offset) {
		int profileEndOffset = getEndProfileOffset();
		if (profileEndOffset == -1) {
			return false;
		}
		return profileEndOffset >= offset;
	}

	/**
	 * 
	 * @return
	 */
	private int getEndProfileOffset() {
		int start = getStart();
		int end = getEnd();
		if (start == -1 || end == -1) {
			return -1;
		}
		String fulltext = getOwnerModel().getText();
		if (start >= fulltext.length()) {
			return -1;
		}
		int i = start;
		if (fulltext.charAt(i) == '%') {
			while (i < end) {
				char c = fulltext.charAt(i);
				if (c == '.') {
					break;
				}
				i++;
			}
			return i;
		}
		return -1;
	}

}
