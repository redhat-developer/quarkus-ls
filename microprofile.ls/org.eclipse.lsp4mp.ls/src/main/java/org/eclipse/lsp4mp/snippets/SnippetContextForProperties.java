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
package org.eclipse.lsp4mp.snippets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.lsp4mp.commons.MicroProfileProjectInfo;
import org.eclipse.lsp4mp.commons.metadata.ItemMetadata;
import org.eclipse.lsp4mp.ls.commons.snippets.ISnippetContext;
import org.eclipse.lsp4mp.utils.MicroProfilePropertiesUtils;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * A snippet context properties which matches properties. You can write a vscode
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
			"properties": ["quarkus.datasource.url", "quarkus.hibernate-orm.database.generation"]
		}
	}

 * </code>
 * 
 * This snippet will be available only if
 * "quarkus.hibernate-orm.database.generation" is present for the project.
 * 
 * @author Angelo ZERR
 *
 */
public class SnippetContextForProperties implements ISnippetContext<MicroProfileProjectInfo> {

	public static final TypeAdapter<SnippetContextForProperties> TYPE_ADAPTER = new SnippetContextForPropertiesAdapter();

	private List<String> properties;

	public SnippetContextForProperties(List<String> properties) {
		this.properties = properties;
	}

	@Override
	public boolean isMatch(MicroProfileProjectInfo projectInfo) {
		for (String propertyName : this.properties) {
			ItemMetadata metadata = MicroProfilePropertiesUtils.getProperty(propertyName, projectInfo);
			if (metadata == null) {
				return false;
			}
		}
		return true;
	}

	private static class SnippetContextForPropertiesAdapter extends TypeAdapter<SnippetContextForProperties> {
		public SnippetContextForProperties read(JsonReader in) throws IOException {
			if (in.peek() == JsonToken.NULL) {
				in.nextNull();
				return null;
			}
			List<String> properties = new ArrayList<>();
			in.beginObject();
			while (in.hasNext()) {
				String name = in.nextName();
				switch (name) {
				case "properties":
					if (in.peek() == JsonToken.BEGIN_ARRAY) {
						in.beginArray();
						while (in.peek() != JsonToken.END_ARRAY) {
							properties.add(in.nextString());
						}
						in.endArray();
					} else {
						properties.add(in.nextString());
					}
					break;
				default:
					in.skipValue();
				}
			}
			in.endObject();
			return new SnippetContextForProperties(properties);
		}

		public void write(JsonWriter writer, SnippetContextForProperties value) throws IOException {
			// Do nothing
		}
	}

}