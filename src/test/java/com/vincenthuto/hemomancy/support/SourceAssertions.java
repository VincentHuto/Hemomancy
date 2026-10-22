package com.vincenthuto.hemomancy.support;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Shared source-text reads and assertions. New source-text assertions are for architecture guardrails only
 * (standards section 11); behaviour belongs in unit, data or GameTests.
 *
 * <p>Failures throw {@link AssertionError}, so these work in JUnit tests and in legacy {@code main()} tests alike.
 */
public final class SourceAssertions {
	private SourceAssertions() {
	}

	/** Reads a repository file as strict UTF-8 with {@code \n} line endings. A missing file fails the test. */
	public static String read(String repoRelative) {
		return read(RepoPaths.resolve(repoRelative));
	}

	/** Reads a mod class, such as {@code "common/network/PacketHandler.java"}. */
	public static String readMainJava(String packageRelative) {
		return read(RepoPaths.mainJava(packageRelative));
	}

	public static String read(Path path) {
		if (!Files.isRegularFile(path)) {
			throw new AssertionError("missing " + RepoPaths.display(path));
		}
		try {
			return Files.readString(path).replace("\r\n", "\n");
		} catch (IOException e) {
			throw new UncheckedIOException("could not read " + RepoPaths.display(path), e);
		}
	}

	public static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing '" + expected + "'");
		}
	}

	public static void assertNotContains(String label, String text, String unexpected) {
		if (text.contains(unexpected)) {
			throw new AssertionError(label + ": unexpected '" + unexpected + "'");
		}
	}

	/** Text between the first {@code start} and the next {@code end} after it, both excluded. */
	public static String section(String text, String start, String end) {
		int from = text.indexOf(start);
		if (from < 0) {
			throw new AssertionError("missing section start '" + start + "'");
		}
		from += start.length();
		int to = text.indexOf(end, from);
		if (to < 0) {
			throw new AssertionError("missing section end '" + end + "' after '" + start + "'");
		}
		return text.substring(from, to);
	}

	/**
	 * The brace-matched body of the first method whose declaration contains {@code signatureStart}, from its opening
	 * brace to its closing brace inclusive. Braces in strings and comments are not special-cased.
	 */
	public static String methodBody(String text, String signatureStart) {
		int start = text.indexOf(signatureStart);
		if (start < 0) {
			throw new AssertionError("missing method '" + signatureStart + "'");
		}
		int brace = text.indexOf('{', start);
		if (brace < 0) {
			throw new AssertionError("missing body for method '" + signatureStart + "'");
		}
		int depth = 0;
		for (int i = brace; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c == '{') {
				depth++;
			} else if (c == '}' && --depth == 0) {
				return text.substring(brace, i + 1);
			}
		}
		throw new AssertionError("unbalanced body for method '" + signatureStart + "'");
	}
}
