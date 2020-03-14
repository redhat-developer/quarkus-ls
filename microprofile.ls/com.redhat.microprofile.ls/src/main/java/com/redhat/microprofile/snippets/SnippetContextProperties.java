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

import java.util.Set;

import com.redhat.microprofile.ls.commons.snippets.ISnippetContext;

/**
 * A snippet context properties which matches extension. You can write a vscode
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
			"extension": "quarkus-agroal"
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
public class SnippetContextProperties implements ISnippetContext<Set<String>> {

	private String extension;

	public SnippetContextProperties(String extension) {
		this.extension = extension;
	}

	@Override
	public boolean isMatch(Set<String> extensions) {
		return extensions.contains(extension);
	}

}
