/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
	*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.services.completions;

import static com.redhat.qute.QuteAssert.c;
import static com.redhat.qute.QuteAssert.r;
import static com.redhat.qute.QuteAssert.testCompletionFor;

import org.junit.jupiter.api.Test;

/**
 * Tests for Qute completion in parameter declaration.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteCompletionInParameterDeclarationTest {

	@Test
	public void completionInParameterDeclarationForJavaClass() throws Exception {
		String template = "{@|}\r\n";

		// Without snippet
		testCompletionFor(template, //
				// Package completion
				c("org.acme", "org.acme", r(0, 2, 0, 2)), //
				// Class completion
				c("org.acme.Item", "org.acme.Item item", r(0, 2, 0, 2)), //
				c("org.acme.Review", "org.acme.Review review", r(0, 2, 0, 2)), //
				c("java.util.List<E>", "java.util.List<E> list", r(0, 2, 0, 2)), //
				c("java.util.Map<K,V>", "java.util.Map<K,V> map", r(0, 2, 0, 2)));

		// With snippet support
		testCompletionFor(template, //
				true, // snippet support
				// Package completion
				c("org.acme", "org.acme", r(0, 2, 0, 2)), //
				// Class completion
				c("org.acme.Item", "org.acme.Item ${1:item}$0", r(0, 2, 0, 2)), //
				c("org.acme.Review", "org.acme.Review ${1:review}$0", r(0, 2, 0, 2)), //
				c("java.util.List<E>", "java.util.List<${1:E}> ${2:list}$0", r(0, 2, 0, 2)), //
				c("java.util.Map<K,V>", "java.util.Map<${1:K},${2:V}> ${3:map}$0", r(0, 2, 0, 2)));

	}

	@Test
	public void completionInParameterDeclarationForJavaClassNotClosed() throws Exception {
		String template = "{@|\r\n";

		// Without snippet
		testCompletionFor(template, //
				// Package completion
				c("org.acme", "org.acme", r(0, 2, 0, 2)), //
				// Class completion
				c("org.acme.Item", "org.acme.Item item}", r(0, 2, 0, 2)), //
				c("org.acme.Review", "org.acme.Review review}", r(0, 2, 0, 2)), //
				c("java.util.List<E>", "java.util.List<E> list}", r(0, 2, 0, 2)), //
				c("java.util.Map<K,V>", "java.util.Map<K,V> map}", r(0, 2, 0, 2)));

		// With snippet support
		testCompletionFor(template, //
				true, // snippet support
				// Package completion
				c("org.acme", "org.acme", r(0, 2, 0, 2)), //
				// Class completion
				c("org.acme.Item", "org.acme.Item ${1:item}}$0", r(0, 2, 0, 2)), //
				c("org.acme.Review", "org.acme.Review ${1:review}}$0", r(0, 2, 0, 2)), //
				c("java.util.List<E>", "java.util.List<${1:E}> ${2:list}}$0", r(0, 2, 0, 2)), //
				c("java.util.Map<K,V>", "java.util.Map<${1:K},${2:V}> ${3:map}}$0", r(0, 2, 0, 2)));

	}

	@Test
	public void completionInParameterDeclarationForJavaClassNotClosedFollowedByExpression() throws Exception {
		String template = "{@|\r\n" + //
				"{abcd}";

		// Without snippet
		testCompletionFor(template, //
				// Package completion
				c("org.acme", "org.acme", r(0, 2, 0, 2)), //
				// Class completion
				c("org.acme.Item", "org.acme.Item item}", r(0, 2, 0, 2)), //
				c("org.acme.Review", "org.acme.Review review}", r(0, 2, 0, 2)), //
				c("java.util.List<E>", "java.util.List<E> list}", r(0, 2, 0, 2)), //
				c("java.util.Map<K,V>", "java.util.Map<K,V> map}", r(0, 2, 0, 2)));

		// With snippet support
		testCompletionFor(template, //
				true, // snippet support
				// Package completion
				c("org.acme", "org.acme", r(0, 2, 0, 2)), //
				// Class completion
				c("org.acme.Item", "org.acme.Item ${1:item}}$0", r(0, 2, 0, 2)), //
				c("org.acme.Review", "org.acme.Review ${1:review}}$0", r(0, 2, 0, 2)), //
				c("java.util.List<E>", "java.util.List<${1:E}> ${2:list}}$0", r(0, 2, 0, 2)), //
				c("java.util.Map<K,V>", "java.util.Map<${1:K},${2:V}> ${3:map}}$0", r(0, 2, 0, 2)));

		template = "{@I|t\r\n" + //
				"{abcd}";

		// Without snippet
		testCompletionFor(template, //
				false, 7,
				// Class completion
				c("java.lang.Integer", "java.lang.Integer integer}", r(0, 2, 0, 4)), //
				c("org.acme.Item", "org.acme.Item item}", r(0, 2, 0, 4)), //
				c("org.acme.ItemWithTemplateData", "org.acme.ItemWithTemplateData itemWithTemplateData}",
						r(0, 2, 0, 4)), //
				c("org.acme.ItemWithTemplateDataIgnoreSubClasses",
						"org.acme.ItemWithTemplateDataIgnoreSubClasses itemWithTemplateDataIgnoreSubClasses}",
						r(0, 2, 0, 4)), //
				c("org.acme.ItemWithRegisterForReflection",
						"org.acme.ItemWithRegisterForReflection itemWithRegisterForReflection}", r(0, 2, 0, 4)), //
				c("org.acme.ItemWithRegisterForReflectionNoFields",
						"org.acme.ItemWithRegisterForReflectionNoFields itemWithRegisterForReflectionNoFields}",
						r(0, 2, 0, 4)), //
				c("org.acme.ItemWithRegisterForReflectionNoMethods",
						"org.acme.ItemWithRegisterForReflectionNoMethods itemWithRegisterForReflectionNoMethods}",
						r(0, 2, 0, 4)));

		// With snippet support
		testCompletionFor(template, //
				true, // snippet support
				7,
				// Class completion
				c("java.lang.Integer", "java.lang.Integer ${1:integer}}$0", r(0, 2, 0, 4)), //
				c("org.acme.Item", "org.acme.Item ${1:item}}$0", r(0, 2, 0, 4)), //
				c("org.acme.ItemWithTemplateData", "org.acme.ItemWithTemplateData ${1:itemWithTemplateData}}$0",
						r(0, 2, 0, 4)), //
				c("org.acme.ItemWithTemplateDataIgnoreSubClasses",
						"org.acme.ItemWithTemplateDataIgnoreSubClasses ${1:itemWithTemplateDataIgnoreSubClasses}}$0",
						r(0, 2, 0, 4)), //
				c("org.acme.ItemWithRegisterForReflection",
						"org.acme.ItemWithRegisterForReflection ${1:itemWithRegisterForReflection}}$0", r(0, 2, 0, 4)), //
				c("org.acme.ItemWithRegisterForReflectionNoFields",
						"org.acme.ItemWithRegisterForReflectionNoFields ${1:itemWithRegisterForReflectionNoFields}}$0",
						r(0, 2, 0, 4)), //
				c("org.acme.ItemWithRegisterForReflectionNoMethods",
						"org.acme.ItemWithRegisterForReflectionNoMethods ${1:itemWithRegisterForReflectionNoMethods}}$0",
						r(0, 2, 0, 4)));
	}

	@Test
	public void completionInParameterDeclarationForJavaClassNotClosedWithPattern() throws Exception {
		String template = "{@I|t\r\n";

		// Without snippet
		testCompletionFor(template, //
				false, 7,
				// Class completion
				c("java.lang.Integer", "java.lang.Integer integer}", r(0, 2, 0, 4)), //
				c("org.acme.Item", "org.acme.Item item}", r(0, 2, 0, 4)), //
				c("org.acme.ItemWithTemplateData", "org.acme.ItemWithTemplateData itemWithTemplateData}",
						r(0, 2, 0, 4)), //
				c("org.acme.ItemWithTemplateDataIgnoreSubClasses",
						"org.acme.ItemWithTemplateDataIgnoreSubClasses itemWithTemplateDataIgnoreSubClasses}",
						r(0, 2, 0, 4)), //
				c("org.acme.ItemWithRegisterForReflection",
						"org.acme.ItemWithRegisterForReflection itemWithRegisterForReflection}", r(0, 2, 0, 4)), //
				c("org.acme.ItemWithRegisterForReflectionNoFields",
						"org.acme.ItemWithRegisterForReflectionNoFields itemWithRegisterForReflectionNoFields}",
						r(0, 2, 0, 4)), //
				c("org.acme.ItemWithRegisterForReflectionNoMethods",
						"org.acme.ItemWithRegisterForReflectionNoMethods itemWithRegisterForReflectionNoMethods}",
						r(0, 2, 0, 4)));

		// With snippet support
		testCompletionFor(template, //
				true, // snippet support
				7,
				// Class completion
				c("java.lang.Integer", "java.lang.Integer ${1:integer}}$0", r(0, 2, 0, 4)), //
				c("org.acme.Item", "org.acme.Item ${1:item}}$0", r(0, 2, 0, 4)), //
				c("org.acme.ItemWithTemplateData", "org.acme.ItemWithTemplateData ${1:itemWithTemplateData}}$0",
						r(0, 2, 0, 4)), //
				c("org.acme.ItemWithTemplateDataIgnoreSubClasses",
						"org.acme.ItemWithTemplateDataIgnoreSubClasses ${1:itemWithTemplateDataIgnoreSubClasses}}$0",
						r(0, 2, 0, 4)), //
				c("org.acme.ItemWithRegisterForReflection",
						"org.acme.ItemWithRegisterForReflection ${1:itemWithRegisterForReflection}}$0", r(0, 2, 0, 4)), //
				c("org.acme.ItemWithRegisterForReflectionNoFields",
						"org.acme.ItemWithRegisterForReflectionNoFields ${1:itemWithRegisterForReflectionNoFields}}$0",
						r(0, 2, 0, 4)), //
				c("org.acme.ItemWithRegisterForReflectionNoMethods",
						"org.acme.ItemWithRegisterForReflectionNoMethods ${1:itemWithRegisterForReflectionNoMethods}}$0",
						r(0, 2, 0, 4)));

		template = "{@It|\r\n";

		// Without snippet
		testCompletionFor(template, //
				false, 6,
				// Class completion
				c("org.acme.Item", "org.acme.Item item}", r(0, 2, 0, 4)), //
				c("org.acme.ItemWithTemplateData", "org.acme.ItemWithTemplateData itemWithTemplateData}",
						r(0, 2, 0, 4)), //
				c("org.acme.ItemWithTemplateDataIgnoreSubClasses",
						"org.acme.ItemWithTemplateDataIgnoreSubClasses itemWithTemplateDataIgnoreSubClasses}",
						r(0, 2, 0, 4)), //
				c("org.acme.ItemWithRegisterForReflection",
						"org.acme.ItemWithRegisterForReflection itemWithRegisterForReflection}", r(0, 2, 0, 4)), //
				c("org.acme.ItemWithRegisterForReflectionNoFields",
						"org.acme.ItemWithRegisterForReflectionNoFields itemWithRegisterForReflectionNoFields}",
						r(0, 2, 0, 4)), //
				c("org.acme.ItemWithRegisterForReflectionNoMethods",
						"org.acme.ItemWithRegisterForReflectionNoMethods itemWithRegisterForReflectionNoMethods}",
						r(0, 2, 0, 4)));

		// With snippet support
		testCompletionFor(template, //
				true, // snippet support
				6,
				// Class completion
				c("org.acme.Item", "org.acme.Item ${1:item}}$0", r(0, 2, 0, 4)), //
				c("org.acme.ItemWithTemplateData", "org.acme.ItemWithTemplateData ${1:itemWithTemplateData}}$0",
						r(0, 2, 0, 4)), //
				c("org.acme.ItemWithTemplateDataIgnoreSubClasses",
						"org.acme.ItemWithTemplateDataIgnoreSubClasses ${1:itemWithTemplateDataIgnoreSubClasses}}$0",
						r(0, 2, 0, 4)), //
				c("org.acme.ItemWithRegisterForReflection",
						"org.acme.ItemWithRegisterForReflection ${1:itemWithRegisterForReflection}}$0", r(0, 2, 0, 4)), //
				c("org.acme.ItemWithRegisterForReflectionNoFields",
						"org.acme.ItemWithRegisterForReflectionNoFields ${1:itemWithRegisterForReflectionNoFields}}$0",
						r(0, 2, 0, 4)), //
				c("org.acme.ItemWithRegisterForReflectionNoMethods",
						"org.acme.ItemWithRegisterForReflectionNoMethods ${1:itemWithRegisterForReflectionNoMethods}}$0",
						r(0, 2, 0, 4)));
	}

	@Test
	public void completionInParameterDeclarationWithAliasForJavaClass() throws Exception {
		String template = "{@| alias}\r\n";

		// Without snippet
		testCompletionFor(template, //
				// Package completion
				c("org.acme", "org.acme", r(0, 2, 0, 2)), //
				// Class completion
				c("org.acme.Item", "org.acme.Item", r(0, 2, 0, 2)), //
				c("org.acme.Review", "org.acme.Review", r(0, 2, 0, 2)), //
				c("java.util.List<E>", "java.util.List<E>", r(0, 2, 0, 2)), //
				c("java.util.Map<K,V>", "java.util.Map<K,V>", r(0, 2, 0, 2)));

		// With snippet support
		testCompletionFor(template, //
				true, // snippet support
				// Package completion
				c("org.acme", "org.acme", r(0, 2, 0, 2)), //
				// Class completion
				c("org.acme.Item", "org.acme.Item$0", r(0, 2, 0, 2)), //
				c("org.acme.Review", "org.acme.Review$0", r(0, 2, 0, 2)), //
				c("java.util.List<E>", "java.util.List<${1:E}>$0", r(0, 2, 0, 2)), //
				c("java.util.Map<K,V>", "java.util.Map<${1:K},${2:V}>$0", r(0, 2, 0, 2)));

	}

	@Test
	public void completionInTypeParameterClosed() throws Exception {
		String template = "{@java.util.List<|> alias}\r\n";

		// Without snippet
		testCompletionFor(template, //
				// Package completion
				c("org.acme", "org.acme", r(0, 17, 0, 17)), //
				// Class completion
				c("org.acme.Item", "org.acme.Item", r(0, 17, 0, 17)), //
				c("org.acme.Review", "org.acme.Review", r(0, 17, 0, 17)), //
				c("java.util.List<E>", "java.util.List<E>", r(0, 17, 0, 17)), //
				c("java.util.Map<K,V>", "java.util.Map<K,V>", r(0, 17, 0, 17)));

		// With snippet support
		testCompletionFor(template, //
				true, // snippet support
				// Package completion
				c("org.acme", "org.acme", r(0, 17, 0, 17)), //
				// Class completion
				c("org.acme.Item", "org.acme.Item$0", r(0, 17, 0, 17)), //
				c("org.acme.Review", "org.acme.Review$0", r(0, 17, 0, 17)), //
				c("java.util.List<E>", "java.util.List<${1:E}>$0", r(0, 17, 0, 17)), //
				c("java.util.Map<K,V>", "java.util.Map<${1:K},${2:V}>$0", r(0, 17, 0, 17)));

	}

	@Test
	public void completionInTypeParameterNotClosed() throws Exception {
		String template = "{@java.util.List<| alias}\r\n";

		// Without snippet
		testCompletionFor(template, //
				// Package completion
				c("org.acme", "org.acme>", r(0, 17, 0, 17)), //
				// Class completion
				c("org.acme.Item", "org.acme.Item>", r(0, 17, 0, 17)), //
				c("org.acme.Review", "org.acme.Review>", r(0, 17, 0, 17)), //
				c("java.util.List<E>", "java.util.List<E>", r(0, 17, 0, 17)), //
				c("java.util.Map<K,V>", "java.util.Map<K,V>", r(0, 17, 0, 17)));

		// With snippet support
		testCompletionFor(template, //
				true, // snippet support
				// Package completion
				c("org.acme", "org.acme>", r(0, 17, 0, 17)), //
				// Class completion
				c("org.acme.Item", "org.acme.Item>$0", r(0, 17, 0, 17)), //
				c("org.acme.Review", "org.acme.Review>$0", r(0, 17, 0, 17)), //
				c("java.util.List<E>", "java.util.List<${1:E}>$0", r(0, 17, 0, 17)), //
				c("java.util.Map<K,V>", "java.util.Map<${1:K},${2:V}>$0", r(0, 17, 0, 17)));

	}

	@Test
	public void completionInSecondTypeParameter() throws Exception {
		String template = "{@java.util.Map<java.lang.String,| alias}\r\n";

		// Without snippet
		testCompletionFor(template, //
				// Package completion
				c("org.acme", "org.acme>", r(0, 33, 0, 33)), //
				// Class completion
				c("org.acme.Item", "org.acme.Item>", r(0, 33, 0, 33)), //
				c("org.acme.Review", "org.acme.Review>", r(0, 33, 0, 33)), //
				c("java.util.List<E>", "java.util.List<E>", r(0, 33, 0, 33)), //
				c("java.util.Map<K,V>", "java.util.Map<K,V>", r(0, 33, 0, 33)));

		// With snippet support
		testCompletionFor(template, //
				true, // snippet support
				// Package completion
				c("org.acme", "org.acme>", r(0, 33, 0, 33)), //
				// Class completion
				c("org.acme.Item", "org.acme.Item>$0", r(0, 33, 0, 33)), //
				c("org.acme.Review", "org.acme.Review>$0", r(0, 33, 0, 33)), //
				c("java.util.List<E>", "java.util.List<${1:E}>$0", r(0, 33, 0, 33)), //
				c("java.util.Map<K,V>", "java.util.Map<${1:K},${2:V}>$0", r(0, 33, 0, 33)));

	}

}