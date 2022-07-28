/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.commons;

import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;

/**
 * Parameters required for generating a field, getter, or template extension in
 * a Java type.
 *
 * @author datho7561
 */
public class GenerateMissingJavaMemberParams {

	public enum MemberType {

		Field(1), Getter(2), AppendTemplateExtension(3), CreateTemplateExtension(4);

		private final int value;

		MemberType(int value) {
			this.value = value;
		}

		public int getValue() {
			return this.value;
		}

		public static MemberType forValue(int value) {
			MemberType[] allValues = MemberType.values();
			if (value < 1 || value > allValues.length)
				throw new IllegalArgumentException("Illegal enum value: " + value);
			return allValues[value - 1];
		}

	}

	private MemberType memberType;
	private String missingProperty;
	private String javaType;
	private String projectUri;

	public GenerateMissingJavaMemberParams() {
		this(null, null, null, null);
	}

	/**
	 * Returns the parameters for generate a missing java member that is referenced
	 * in a template but doesn't exist.
	 * 
	 * @param memberType      the type of member to generate
	 * @param missingProperty the name of the java member that's missing
	 * @param javaType        the java type to generate the missing member in
	 * @param projectUri      the uri of the project
	 */
	public GenerateMissingJavaMemberParams(MemberType memberType, String missingProperty, String javaType,
			String projectUri) {
		this.memberType = memberType;
		this.missingProperty = missingProperty;
		this.javaType = javaType;
		this.projectUri = projectUri;
	}

	public MemberType getMemberType() {
		return this.memberType;
	}

	public void setMemberType(MemberType memberType) {
		this.memberType = memberType;
	}

	public String getMissingProperty() {
		return this.missingProperty;
	}

	public void setMissingProperty(String missingProperty) {
		this.missingProperty = missingProperty;
	}

	public String getJavaType() {
		return this.javaType;
	}

	public void setJavaType(String javaType) {
		this.javaType = javaType;
	}

	public String getProjectUri() {
		return this.projectUri;
	}

	public void setProjectUri(String projectUri) {
		this.projectUri = projectUri;
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.add("memberType", memberType);
		builder.add("missingProperty", missingProperty);
		builder.add("javaType", javaType);
		builder.add("projectUri", projectUri);
		return builder.toString();
	}

	@Override
	public boolean equals(Object other) {
		if (other == null || !(other instanceof GenerateMissingJavaMemberParams)) {
			return false;
		}
		GenerateMissingJavaMemberParams that = (GenerateMissingJavaMemberParams) other;
		return (this.memberType == that.memberType) //
				&& ((this.missingProperty == null && that.missingProperty == null)
						|| this.missingProperty.equals(that.missingProperty)) //
				&& ((this.javaType == null && that.javaType == null) || this.javaType.equals(that.javaType)) //
				&& ((this.projectUri == null && that.projectUri == null) || this.projectUri.equals(that.projectUri));
	}

}
