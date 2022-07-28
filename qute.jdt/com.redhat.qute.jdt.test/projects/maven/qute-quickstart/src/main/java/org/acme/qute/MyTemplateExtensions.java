package org.acme.qute;

import io.quarkus.qute.TemplateExtension;

@TemplateExtension
public class MyTemplateExtensions {

	public static String lengthMinusTwo(String str) {
		return Integer.toString(str.length() - 2);
	}

}
