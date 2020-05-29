/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
* 
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.snippets;

import java.io.IOException;
import java.util.Set;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.redhat.microprofile.ls.commons.snippets.ISnippetContext;

/**
 * A snippet context properties which matches dependency. You can write a vscode
 * snippet that declares a context like this:
 * 
 * <code>
 * 
 * "Add datasource properties": {
		"prefix": "qds",
		"body": [
			"quarkus.datasource.url=${1|jdbc:mariadb://localhost:3306/mydb,jdbc:mysql://localhost:3306/test,jdbc:h2:mem:mydb,jdbc:postgresql://localhost/mydb,jdbc:sqlserver://localhost:1433;databaseName=mydb|}",
			"quarkus.datasource.driver=${2|org.mariadb.jdbc.Driver,org.mysql.jdbc.Driver,org.h2.Driver,org.postgresql.Driver,com.microsoft.sqlserver.jdbc.SQLServerDriver|}",
			"quarkus.datasource.username=${3:developer}",
			"quarkus.datasource.password=${4:developer}",
			"quarkus.hibernate-orm.database.generation=${5|update,drop-and-create,create,drop,none|}"
		],
		"description": "Configure Quarkus datasource",
		"context": {
			"dependency": "quarkus-agroal"
		}
	}

 * </code>
 * 
 * This snippet will be available only if "quarkus-agroal" is present as
 * dependency in the project.
 * 
 * @author Angelo ZERR
 *
 */
public class SnippetContextForProperties implements ISnippetContext<Set<String>> {

	public static final TypeAdapter<SnippetContextForProperties> TYPE_ADAPTER = new SnippetContextForPropertiesAdapter();

	private String dependency;

	public SnippetContextForProperties(String dependency) {
		this.dependency = dependency;
	}

	@Override
	public boolean isMatch(Set<String> dependencies) {
		return dependencies != null && dependencies.contains(dependency);
	}

	private static class SnippetContextForPropertiesAdapter extends TypeAdapter<SnippetContextForProperties> {
		public SnippetContextForProperties read(JsonReader in) throws IOException {
			if (in.peek() == JsonToken.NULL) {
				in.nextNull();
				return null;
			}
			String dependency = null;
			in.beginObject();
			while (in.hasNext()) {
				String name = in.nextName();
				switch (name) {
				case "dependency":
					dependency = in.nextString();
					break;
				default:
					in.skipValue();
				}
			}
			in.endObject();
			return new SnippetContextForProperties(dependency);
		}

		public void write(JsonWriter writer, SnippetContextForProperties value) throws IOException {
			// Do nothing
		}
	}

}