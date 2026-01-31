package com.redhat.qute.parser.injection.scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Collection;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.redhat.qute.parser.injection.InjectionDetector;
import com.redhat.qute.parser.template.scanner.ScannerState;
import com.redhat.qute.parser.template.scanner.TemplateScanner;
import com.redhat.qute.parser.template.scanner.TokenType;
import com.redhat.qute.project.extensions.roq.frontmatter.YamlFrontMatterDetector;

/**
 * Tests for template scanner with YAML front matter injection.
 */
public class YamlFrontMatterScannerTest {

	private ScannerWithInjection<TokenType, ScannerState> scanner;

	// ========== Basic Front Matter Tests ==========

	@Test
	public void testYamlFrontMatterMinimal() {
		String content = "---\r\n" + //
				"\r\n" + //
				"---\r\n";
		
		Collection<InjectionDetector> injectors = Collections.singletonList(new YamlFrontMatterDetector());
		scanner = TemplateScanner.createScanner(content, injectors);
		
		assertOffsetAndToken(0, TokenType.LanguageInjectionStart, "---\r\n");
		assertNotNull(scanner.getInjectionMetadata());
		assertEquals("yaml-frontmatter", scanner.getInjectionMetadata().getLanguageId());
		
		assertOffsetAndToken(5, TokenType.LanguageInjectionContent, "\r\n");
		assertOffsetAndToken(7, TokenType.LanguageInjectionEnd, "---\r\n");
		assertOffsetAndToken(12, TokenType.EOS, "");
	}

	@Test
	public void testYamlFrontMatterSingleLine() {
		String content = "---\r\n" + //
				"title: Test\r\n" + //
				"---\r\n";
		
		Collection<InjectionDetector> injectors = Collections.singletonList(new YamlFrontMatterDetector());
		scanner = TemplateScanner.createScanner(content, injectors);
		
		assertOffsetAndToken(0, TokenType.LanguageInjectionStart, "---\r\n");
		assertEquals("yaml-frontmatter", scanner.getInjectionMetadata().getLanguageId());
		
		assertOffsetAndToken(5, TokenType.LanguageInjectionContent, "title: Test\r\n");
		assertOffsetAndToken(18, TokenType.LanguageInjectionEnd, "---\r\n");
		assertOffsetAndToken(23, TokenType.EOS, "");
	}

	@Test
	public void testYamlFrontMatterMultipleLines() {
		String content = "---\r\n" + //
				"layout: page\r\n" + //
				"paginate: true\r\n" + //
				"tagging: posts\r\n" + //
				"---\r\n";
		
		Collection<InjectionDetector> injectors = Collections.singletonList(new YamlFrontMatterDetector());
		scanner = TemplateScanner.createScanner(content, injectors);
		
		assertOffsetAndToken(0, TokenType.LanguageInjectionStart, "---\r\n");
		assertOffsetAndToken(5, TokenType.LanguageInjectionContent, 
				"layout: page\r\npaginate: true\r\ntagging: posts\r\n");
		assertOffsetAndToken(51, TokenType.LanguageInjectionEnd, "---\r\n");
		assertOffsetAndToken(56, TokenType.EOS, "");
	}

	@Test
	public void testYamlFrontMatterWithUnixLineEndings() {
		String content = "---\n" + //
				"title: Test\n" + //
				"---\n";
		
		Collection<InjectionDetector> injectors = Collections.singletonList(new YamlFrontMatterDetector());
		scanner = TemplateScanner.createScanner(content, injectors);
		
		assertOffsetAndToken(0, TokenType.LanguageInjectionStart, "---\n");
		assertOffsetAndToken(4, TokenType.LanguageInjectionContent, "title: Test\n");
		assertOffsetAndToken(16, TokenType.LanguageInjectionEnd, "---\n");
		assertOffsetAndToken(20, TokenType.EOS, "");
	}

	@Test
	public void testYamlFrontMatterWithComplexYaml() {
		String content = "---\r\n" + //
				"title: My Page\r\n" + //
				"author:\r\n" + //
				"  name: John Doe\r\n" + //
				"  email: john@example.com\r\n" + //
				"tags:\r\n" + //
				"  - java\r\n" + //
				"  - qute\r\n" + //
				"---\r\n";
		
		Collection<InjectionDetector> injectors = Collections.singletonList(new YamlFrontMatterDetector());
		scanner = TemplateScanner.createScanner(content, injectors);
		
		assertOffsetAndToken(0, TokenType.LanguageInjectionStart, "---\r\n");
		assertOffsetAndToken(5, TokenType.LanguageInjectionContent, 
				"title: My Page\r\nauthor:\r\n  name: John Doe\r\n  email: john@example.com\r\ntags:\r\n  - java\r\n  - qute\r\n");
		assertOffsetAndToken(102, TokenType.LanguageInjectionEnd, "---\r\n");
		assertOffsetAndToken(107, TokenType.EOS, "");
	}

	// ========== Front Matter + Qute Content Tests ==========

	@Test
	public void testYamlFrontMatterWithSimpleText() {
		String content = "---\r\n" + //
				"title: Test\r\n" + //
				"---\r\n" + //
				"Hello World";
		
		Collection<InjectionDetector> injectors = Collections.singletonList(new YamlFrontMatterDetector());
		scanner = TemplateScanner.createScanner(content, injectors);
		
		assertOffsetAndToken(0, TokenType.LanguageInjectionStart, "---\r\n");
		assertOffsetAndToken(5, TokenType.LanguageInjectionContent, "title: Test\r\n");
		assertOffsetAndToken(18, TokenType.LanguageInjectionEnd, "---\r\n");
		assertOffsetAndToken(23, TokenType.Content, "Hello World");
		assertOffsetAndToken(34, TokenType.EOS, "");
	}

	@Test
	public void testYamlFrontMatterWithExpression() {
		String content = "---\r\n" + //
				"title: Test\r\n" + //
				"---\r\n" + //
				"{item.name}";
		
		Collection<InjectionDetector> injectors = Collections.singletonList(new YamlFrontMatterDetector());
		scanner = TemplateScanner.createScanner(content, injectors);
		
		assertOffsetAndToken(0, TokenType.LanguageInjectionStart, "---\r\n");
		assertOffsetAndToken(5, TokenType.LanguageInjectionContent, "title: Test\r\n");
		assertOffsetAndToken(18, TokenType.LanguageInjectionEnd, "---\r\n");
		assertOffsetAndToken(23, TokenType.StartExpression, "{");
		assertOffsetAndToken(33, TokenType.EndExpression, "}");
		assertOffsetAndToken(34, TokenType.EOS, "");
	}

	@Test
	public void testYamlFrontMatterWithSection() {
		String content = "---\r\n" + //
				"layout: page\r\n" + //
				"---\r\n" + //
				"{#let name=value}\r\n" + //
				"    \r\n" + //
				"{/let}";
		
		Collection<InjectionDetector> injectors = Collections.singletonList(new YamlFrontMatterDetector());
		scanner = TemplateScanner.createScanner(content, injectors);
		
		assertOffsetAndToken(0, TokenType.LanguageInjectionStart, "---\r\n");
		assertOffsetAndToken(5, TokenType.LanguageInjectionContent, "layout: page\r\n");
		assertOffsetAndToken(19, TokenType.LanguageInjectionEnd, "---\r\n");
		
		assertOffsetAndToken(24, TokenType.StartTagOpen, "{#");
		assertOffsetAndToken(26, TokenType.StartTag, "let");
		assertOffsetAndToken(29, TokenType.Whitespace, " ");
		assertOffsetAndToken(30, TokenType.ParameterTag, "name=value");
		assertOffsetAndToken(40, TokenType.StartTagClose, "}");
		assertOffsetAndToken(41, TokenType.Content, "\r\n    \r\n");
		assertOffsetAndToken(49, TokenType.EndTagOpen, "{/");
		assertOffsetAndToken(51, TokenType.EndTag, "let");
		assertOffsetAndToken(54, TokenType.EndTagClose, "}");
		assertOffsetAndToken(55, TokenType.EOS, "");
	}

	@Test
	public void testYamlFrontMatterWithMultipleSections() {
		String content = "---\r\n" + //
				"title: Test\r\n" + //
				"---\r\n" + //
				"{#if user}\r\n" + //
				"  Hello {user.name}\r\n" + //
				"{/if}";
		
		Collection<InjectionDetector> injectors = Collections.singletonList(new YamlFrontMatterDetector());
		scanner = TemplateScanner.createScanner(content, injectors);
		
		assertOffsetAndToken(0, TokenType.LanguageInjectionStart, "---\r\n");
		assertOffsetAndToken(5, TokenType.LanguageInjectionContent, "title: Test\r\n");
		assertOffsetAndToken(18, TokenType.LanguageInjectionEnd, "---\r\n");
		assertOffsetAndToken(23, TokenType.StartTagOpen, "{#");
		assertOffsetAndToken(25, TokenType.StartTag, "if");
		assertOffsetAndToken(27, TokenType.Whitespace, " ");
		assertOffsetAndToken(28, TokenType.ParameterTag, "user");
		assertOffsetAndToken(32, TokenType.StartTagClose, "}");
		assertOffsetAndToken(33, TokenType.Content, "\r\n  Hello ");
		assertOffsetAndToken(43, TokenType.StartExpression, "{");
		assertOffsetAndToken(53, TokenType.EndExpression, "}");
		assertOffsetAndToken(54, TokenType.Content, "\r\n");
		assertOffsetAndToken(56, TokenType.EndTagOpen, "{/");
		assertOffsetAndToken(58, TokenType.EndTag, "if");
		assertOffsetAndToken(60, TokenType.EndTagClose, "}");
		assertOffsetAndToken(61, TokenType.EOS, "");
	}

	@Test
	public void testYamlFrontMatterWithComment() {
		String content = "---\r\n" + //
				"title: Test\r\n" + //
				"---\r\n" + //
				"{! This is a comment !}\r\n" + //
				"Content";
		
		Collection<InjectionDetector> injectors = Collections.singletonList(new YamlFrontMatterDetector());
		scanner = TemplateScanner.createScanner(content, injectors);
		
		assertOffsetAndToken(0, TokenType.LanguageInjectionStart, "---\r\n");
		assertOffsetAndToken(5, TokenType.LanguageInjectionContent, "title: Test\r\n");
		assertOffsetAndToken(18, TokenType.LanguageInjectionEnd, "---\r\n");
		assertOffsetAndToken(23, TokenType.StartComment, "{!");
		assertOffsetAndToken(25, TokenType.Comment, " This is a comment ");
		assertOffsetAndToken(44, TokenType.EndComment, "!}");
		assertOffsetAndToken(46, TokenType.Content, "\r\nContent");
		assertOffsetAndToken(55, TokenType.EOS, "");
	}

	// ========== Edge Cases ==========

	@Test
	public void testNoYamlFrontMatterWhenNotAtStart() {
		String content = "Some text\r\n" + //
				"---\r\n" + //
				"layout: page\r\n" + //
				"---\r\n";
		
		Collection<InjectionDetector> injectors = Collections.singletonList(new YamlFrontMatterDetector());
		scanner = TemplateScanner.createScanner(content, injectors);
		
		assertOffsetAndToken(0, TokenType.Content, "Some text\r\n---\r\nlayout: page\r\n---\r\n");
		assertOffsetAndToken(35, TokenType.EOS, "");
	}

	@Test
	public void testNoYamlFrontMatterWithSpaceBeforeDelimiter() {
		String content = " ---\r\n" + //
				"title: Test\r\n" + //
				"---\r\n";
		
		Collection<InjectionDetector> injectors = Collections.singletonList(new YamlFrontMatterDetector());
		scanner = TemplateScanner.createScanner(content, injectors);
		
		assertOffsetAndToken(0, TokenType.Content, " ---\r\ntitle: Test\r\n---\r\n");
		assertOffsetAndToken(24, TokenType.EOS, "");
	}

	@Test
	public void testYamlFrontMatterWithoutClosingDelimiter() {
		String content = "---\r\n" + //
				"title: Test\r\n" + //
				"This continues";
		
		Collection<InjectionDetector> injectors = Collections.singletonList(new YamlFrontMatterDetector());
		scanner = TemplateScanner.createScanner(content, injectors);
		
		assertOffsetAndToken(0, TokenType.LanguageInjectionStart, "---\r\n");
		assertOffsetAndToken(5, TokenType.LanguageInjectionContent, "title: Test\r\nThis continues");
		assertOffsetAndToken(32, TokenType.EOS, "");
	}

	@Test
	public void testYamlFrontMatterWithDashesInContent() {
		String content = "---\r\n" + //
				"title: Test---More\r\n" + //
				"---\r\n";
		
		Collection<InjectionDetector> injectors = Collections.singletonList(new YamlFrontMatterDetector());
		scanner = TemplateScanner.createScanner(content, injectors);
		
		assertOffsetAndToken(0, TokenType.LanguageInjectionStart, "---\r\n");
		assertOffsetAndToken(5, TokenType.LanguageInjectionContent, "title: Test---More\r\n");
		assertOffsetAndToken(25, TokenType.LanguageInjectionEnd, "---\r\n");
		assertOffsetAndToken(30, TokenType.EOS, "");
	}

	@Test
	public void testYamlFrontMatterWithEmptyLines() {
		String content = "---\r\n" + //
				"title: Test\r\n" + //
				"\r\n" + //
				"author: John\r\n" + //
				"---\r\n";
		
		Collection<InjectionDetector> injectors = Collections.singletonList(new YamlFrontMatterDetector());
		scanner = TemplateScanner.createScanner(content, injectors);
		
		assertOffsetAndToken(0, TokenType.LanguageInjectionStart, "---\r\n");
		assertOffsetAndToken(5, TokenType.LanguageInjectionContent, "title: Test\r\n\r\nauthor: John\r\n");
		assertOffsetAndToken(34, TokenType.LanguageInjectionEnd, "---\r\n");
		assertOffsetAndToken(39, TokenType.EOS, "");
	}

	// ========== Real-world Examples ==========

	@Test
	public void testRealWorldBlogPost() {
		String content = "---\r\n" + //
				"layout: post\r\n" + //
				"title: My First Blog Post\r\n" + //
				"date: 2024-01-15\r\n" + //
				"author: John Doe\r\n" + //
				"tags:\r\n" + //
				"  - tutorial\r\n" + //
				"  - qute\r\n" + //
				"---\r\n" + //
				"# {post.title}\r\n" + //
				"\r\n" + //
				"Written by {post.author} on {post.date}\r\n" + //
				"\r\n" + //
				"{#for tag in post.tags}\r\n" + //
				"  - {tag}\r\n" + //
				"{/for}";
		
		Collection<InjectionDetector> injectors = Collections.singletonList(new YamlFrontMatterDetector());
		scanner = TemplateScanner.createScanner(content, injectors);
		
		assertOffsetAndToken(0, TokenType.LanguageInjectionStart);
		TokenType token = scanner.scan();
		assertEquals(TokenType.LanguageInjectionContent, token);
		token = scanner.scan();
		assertEquals(TokenType.LanguageInjectionEnd, token);
		
		token = scanner.scan();
		assertEquals(TokenType.Content, token);
		token = scanner.scan();
		assertEquals(TokenType.StartExpression, token);
	}

	@Test
	public void testRealWorldDocumentationPage() {
		String content = "---\r\n" + //
				"title: Documentation\r\n" + //
				"sidebar: true\r\n" + //
				"toc: true\r\n" + //
				"---\r\n" + //
				"{#if sidebar}\r\n" + //
				"  {#include _sidebar.html /}\r\n" + //
				"{/if}\r\n" + //
				"\r\n" + //
				"{#if toc}\r\n" + //
				"  {#include _toc.html /}\r\n" + //
				"{/if}";
		
		Collection<InjectionDetector> injectors = Collections.singletonList(new YamlFrontMatterDetector());
		scanner = TemplateScanner.createScanner(content, injectors);
		
		assertOffsetAndToken(0, TokenType.LanguageInjectionStart);
		TokenType token = scanner.scan();
		assertEquals(TokenType.LanguageInjectionContent, token);
		token = scanner.scan();
		assertEquals(TokenType.LanguageInjectionEnd, token);
		
		token = scanner.scan();
		assertEquals(TokenType.StartTagOpen, token);
	}

	// ========== Helper Methods ==========

	public void assertOffsetAndToken(int tokenOffset, TokenType tokenType) {
		TokenType token = scanner.scan();
		assertEquals(tokenType, token, 
				"Expected " + tokenType + " at offset " + tokenOffset + " but got " + token);
		assertEquals(tokenOffset, scanner.getTokenOffset(),
				"Expected token at offset " + tokenOffset + " but got " + scanner.getTokenOffset());
	}

	public void assertOffsetAndToken(int tokenOffset, TokenType tokenType, String tokenText) {
		TokenType token = scanner.scan();
		assertEquals(tokenType, token,
				"Expected " + tokenType + " at offset " + tokenOffset + " but got " + token);
		assertEquals(tokenOffset, scanner.getTokenOffset(),
				"Expected token at offset " + tokenOffset + " but got " + scanner.getTokenOffset());
		assertEquals(tokenText, scanner.getTokenText(),
				"Expected token text '" + tokenText.replace("\r", "\\r").replace("\n", "\\n") + 
				"' but got '" + scanner.getTokenText().replace("\r", "\\r").replace("\n", "\\n") + "'");
	}
}