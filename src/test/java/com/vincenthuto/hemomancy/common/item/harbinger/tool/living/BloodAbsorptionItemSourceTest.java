package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class BloodAbsorptionItemSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	private BloodAbsorptionItemSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String source = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/living/BloodAbsorptionItem.java");
		String onUseTick = source.substring(source.indexOf("public void onUseTick"),
				source.indexOf("public static Optional<LivingEntity> findBareAbsorptionTarget"));
		String absorbFromTarget = source.substring(source.indexOf("public static double absorbFromTarget"),
				source.indexOf("public static boolean isValidAbsorptionTarget"));

		assertBefore("bare absorption skips client drain ticks",
				onUseTick, "if (worldIn.isClientSide)", "absorbFromTarget");
		assertBefore("absorbing from a target only hurts entities on the server",
				absorbFromTarget, "if (level.isClientSide)", "target.hurt");
	}

	private static String read(String path) throws IOException {
		return Files.readString(ROOT.resolve(path)).replace("\r\n", "\n");
	}

	private static void assertBefore(String label, String text, String first, String second) {
		int firstIndex = text.indexOf(first);
		int secondIndex = text.indexOf(second);
		if (firstIndex < 0 || secondIndex < 0 || firstIndex > secondIndex) {
			throw new AssertionError(label + " (expected '" + first + "' before '" + second + "')");
		}
	}
}
