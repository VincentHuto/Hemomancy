package com.vincenthuto.hemomancy.common.manipulation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class BloodManipulationSourceTest {
	private static final Path BLOOD_MANIPULATION = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/manipulation/BloodManipulation.java");

	private BloodManipulationSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		borrowedBloodCoverHappensAfterAlignmentGate();
	}

	private static void borrowedBloodCoverHappensAfterAlignmentGate() throws IOException {
		String source = Files.readString(BLOOD_MANIPULATION).replace("\r\n", "\n");
		String body = methodBody(source,
				"private boolean tryPerformAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position,\n"
						+ "\t\t\tfloat chargeTicks, boolean applyCooldown, boolean enforceCooldown)");
		int alignmentGate = body.indexOf("tendency.getAlignmentByTendency(tend) >= alignLevel");
		int borrowedCover = body.indexOf("BorrowedBloodReserve.drainToCover");

		if (alignmentGate < 0) {
			throw new AssertionError("missing alignment gate");
		}
		if (borrowedCover < 0) {
			throw new AssertionError("missing borrowed-blood cover");
		}
		if (borrowedCover < alignmentGate) {
			throw new AssertionError("borrowed-blood cover must happen after alignment passes");
		}
	}

	private static String methodBody(String source, String signature) {
		int start = source.indexOf(signature);
		if (start < 0) {
			throw new AssertionError("missing method " + signature);
		}
		int brace = source.indexOf('{', start);
		if (brace < 0) {
			throw new AssertionError("missing method body for " + signature);
		}
		int depth = 0;
		for (int i = brace; i < source.length(); i++) {
			char ch = source.charAt(i);
			if (ch == '{') {
				depth++;
			} else if (ch == '}') {
				depth--;
				if (depth == 0) {
					return source.substring(brace, i + 1);
				}
			}
		}
		throw new AssertionError("unterminated method body for " + signature);
	}
}
