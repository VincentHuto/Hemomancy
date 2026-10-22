package com.vincenthuto.hemomancy.support;

import java.nio.file.Path;

/** Repository locations for tests. Gradle runs tests with the project root as the working directory. */
public final class RepoPaths {
	public static final Path ROOT = Path.of("").toAbsolutePath();
	public static final Path MAIN_JAVA = ROOT.resolve("src/main/java/com/vincenthuto/hemomancy");
	public static final Path MAIN_RESOURCES = ROOT.resolve("src/main/resources");
	public static final Path ASSETS = MAIN_RESOURCES.resolve("assets/hemomancy");
	public static final Path DATA = MAIN_RESOURCES.resolve("data/hemomancy");
	public static final Path TEST_JAVA = ROOT.resolve("src/test/java/com/vincenthuto/hemomancy");
	public static final Path GAMETEST_JAVA = ROOT.resolve("src/gameTest/java/com/vincenthuto/hemomancy");
	public static final Path TOOLS = ROOT.resolve("tools");
	public static final Path DOCS = ROOT.resolve("docs");

	private RepoPaths() {
	}

	/** Resolves a path written relative to the repository root, such as {@code "docs/TESTING.md"}. */
	public static Path resolve(String repoRelative) {
		return ROOT.resolve(repoRelative);
	}

	/** Resolves a class under the mod package, such as {@code "common/network/PacketHandler.java"}. */
	public static Path mainJava(String packageRelative) {
		return MAIN_JAVA.resolve(packageRelative);
	}

	/** Shows a path relative to the repository root, with forward slashes, for assertion messages. */
	public static String display(Path path) {
		Path absolute = path.toAbsolutePath();
		Path shown = absolute.startsWith(ROOT) ? ROOT.relativize(absolute) : absolute;
		return shown.toString().replace('\\', '/');
	}
}
