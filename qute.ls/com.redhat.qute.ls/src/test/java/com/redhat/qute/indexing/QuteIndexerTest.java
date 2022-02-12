package com.redhat.qute.indexing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.redhat.qute.commons.ProjectInfo;
import com.redhat.qute.project.MockQuteProjectRegistry;
import com.redhat.qute.project.QuteProject;
import com.redhat.qute.project.indexing.QuteIndex;
import com.redhat.qute.project.indexing.QuteIndexer;

public class QuteIndexerTest {

	@Test
	public void definition() {
		long start = System.currentTimeMillis();
		QuteIndexer indexer = new QuteIndexer(createProject());
		indexer.scan();
		long end = System.currentTimeMillis();
		System.err.println((end - start) + "ms");

		// base.qute.html -->
		// <title>{#insert title}Default Title{/}</title>

		// BookPage/book.qute.html -->
		// {#include base.qute.html}
		// {#ti|tle}A Book{/title}

		// get definition of title parameter of insert, declared in'base.qute.html'
		List<QuteIndex> indexes = indexer.find("base.qute.html", "insert", "title");
		assertNotNull(indexes);
		assertEquals(1, indexes.size());
		QuteIndex index = indexes.get(0);
		assertEquals("QuteIndex [\n" + //
				"  tag = \"insert\"\n" + //
				"  parameter = \"title\"\n" + //
				"  position = Position [\n" + //
				"    line = 3\n" + //
				"    character = 18\n" + //
				"  ]\n" + //
				"  kind = INSERT\n" + //
				"  templateId = \"base.qute.html\"\n" + //
				"]", index.toString());
	}

	private static QuteProject createProject() {
		ProjectInfo projectInfo = new ProjectInfo("test-qute", "src/test/resources/templates");
		MockQuteProjectRegistry registry = new MockQuteProjectRegistry();
		return new QuteProject(projectInfo, registry, registry);
	}

	@Test
	public void completion() {
		long start = System.currentTimeMillis();
		QuteIndexer indexer = new QuteIndexer(createProject());
		indexer.scan();
		long end = System.currentTimeMillis();
		System.err.println((end - start) + "ms");

		// base.qute.html -->
		// <title>{#insert title}Default Title{/}</title>
		// ...
		// {#insert body}No body!{/}

		// BookPage/book.qute.html -->
		// {#include base.qute.html}
		// {#|
		List<QuteIndex> indexes = indexer.find("base.qute.html", "insert", null);
		assertNotNull(indexes);
		assertEquals(2, indexes.size());
		assertEquals("[QuteIndex [\n" + //
				"  tag = \"insert\"\n" + //
				"  parameter = \"title\"\n" + //
				"  position = Position [\n" + //
				"    line = 3\n" + //
				"    character = 18\n" + //
				"  ]\n" + //
				"  kind = INSERT\n" + //
				"  templateId = \"base.qute.html\"\n" + //
				"], QuteIndex [\n" + //
				"  tag = \"insert\"\n" + //
				"  parameter = \"body\"\n" + //
				"  position = Position [\n" + //
				"    line = 8\n" + //
				"    character = 9\n" + //
				"  ]\n" + //
				"  kind = INSERT\n" + //
				"  templateId = \"base.qute.html\"\n" + //
				"]]", indexes.toString());
	}

	@Test
	public void referencesOfIncludedFile() {
		long start = System.currentTimeMillis();
		QuteIndexer indexer = new QuteIndexer(createProject());
		indexer.scan();
		long end = System.currentTimeMillis();
		System.err.println((end - start) + "ms");

		// base.qute.html -->
		// <title>{#insert title}Default Title{/}</title>
		// ...
		// {#insert body}No body!{/}

		// 1. reference
		// BookPage/book.qute.html -->
		// {#include base}
		// {#title}A Book{/title}

		// 2. reference
		// BookPage/books.qute.html -->
		// {#include base}
		// {#title}Books{/title}

		List<QuteIndex> indexes = indexer.find(null, "include", "base");
		assertNotNull(indexes);
		assertEquals(2, indexes.size());
		assertEquals("[QuteIndex [\n" + //
				"  tag = \"include\"\n" + //
				"  parameter = \"base\"\n" + //
				"  position = Position [\n" + //
				"    line = 0\n" + //
				"    character = 10\n" + //
				"  ]\n" + //
				"  kind = INCLUDE\n" + //
				"  templateId = \"BookPage/books.qute.html\"\n" + //
				"], QuteIndex [\n" + //
				"  tag = \"include\"\n" + //
				"  parameter = \"base\"\n" + //
				"  position = Position [\n" + //
				"    line = 0\n" + //
				"    character = 10\n" + //
				"  ]\n" + //
				"  kind = INCLUDE\n" + //
				"  templateId = \"BookPage/book.qute.html\"\n" + //
				"]]", indexes.toString());
	}

	@Test
	public void referencesOfIncludedTag() {
		long start = System.currentTimeMillis();
		QuteIndexer indexer = new QuteIndexer(createProject());
		indexer.scan();
		long end = System.currentTimeMillis();
		System.err.println((end - start) + "ms");

		// base.qute.html -->
		// <title>{#insert title}Default Title{/}</title>
		// ...
		// {#insert body}No body!{/}

		// 1. reference
		// BookPage/book.qute.html -->
		// {#include base}
		// {#title}A Book{/title}

		// 2. reference
		// BookPage/books.qute.html -->
		// {#include base}
		// {#title}Books{/title}

		List<QuteIndex> indexes = indexer.find(null, "body", null);
		assertNotNull(indexes);
		assertEquals(2, indexes.size());
		assertEquals("[QuteIndex [\n" + //
				"  tag = \"body\"\n" + //
				"  parameter = null\n" + //
				"  position = Position [\n" + //
				"    line = 2\n" + //
				"    character = 2\n" + //
				"  ]\n" + //
				"  kind = CUSTOM\n" + //
				"  templateId = \"BookPage/books.qute.html\"\n" + //
				"], QuteIndex [\n" + //
				"  tag = \"body\"\n" + //
				"  parameter = null\n" + //
				"  position = Position [\n" + //
				"    line = 2\n" + //
				"    character = 2\n" + //
				"  ]\n" + //
				"  kind = CUSTOM\n" + //
				"  templateId = \"BookPage/book.qute.html\"\n" + //
				"]]", indexes.toString());
	}

}
