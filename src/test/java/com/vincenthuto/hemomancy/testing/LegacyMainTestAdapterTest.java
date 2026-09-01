package com.vincenthuto.hemomancy.testing;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

final class LegacyMainTestAdapterTest {
	private static final Path TEST_SOURCE_ROOT = Path.of("src", "test", "java");

	@TestFactory
	Stream<DynamicTest> legacyMainTests() throws Exception {
		return discoverLegacyMainClasses().stream()
				.map(testClass -> DynamicTest.dynamicTest(testClass.getName(), () -> invokeMain(testClass)));
	}

	static List<Class<?>> discoverLegacyMainClasses() throws Exception {
		try (Stream<Path> files = Files.walk(TEST_SOURCE_ROOT)) {
			return files
					.filter(Files::isRegularFile)
					.filter(path -> path.getFileName().toString().endsWith("Test.java"))
					.map(LegacyMainTestAdapterTest::classNameFor)
					.sorted()
					.map(LegacyMainTestAdapterTest::loadWithoutInitializing)
					.filter(LegacyMainTestAdapterTest::hasPublicStaticMain)
					.sorted(Comparator.comparing(Class::getName))
					.toList();
		}
	}

	private static String classNameFor(Path sourceFile) {
		String relative = TEST_SOURCE_ROOT.relativize(sourceFile).toString();
		return relative.substring(0, relative.length() - ".java".length())
				.replace('/', '.')
				.replace('\\', '.');
	}

	private static Class<?> loadWithoutInitializing(String className) {
		try {
			return Class.forName(className, false, LegacyMainTestAdapterTest.class.getClassLoader());
		} catch (ClassNotFoundException exception) {
			throw new IllegalStateException("Compiled test class is missing for " + className, exception);
		}
	}

	private static boolean hasPublicStaticMain(Class<?> testClass) {
		try {
			Method main = testClass.getMethod("main", String[].class);
			return Modifier.isPublic(main.getModifiers())
					&& Modifier.isStatic(main.getModifiers())
					&& main.getReturnType() == void.class;
		} catch (NoSuchMethodException ignored) {
			return false;
		}
	}

	private static void invokeMain(Class<?> testClass) throws Throwable {
		try {
			Method main = testClass.getMethod("main", String[].class);
			if (!main.trySetAccessible()) {
				throw new IllegalAccessException("Cannot access legacy main method on " + testClass.getName());
			}
			main.invoke(null, (Object) new String[0]);
		} catch (InvocationTargetException exception) {
			throw exception.getCause();
		}
	}
}
