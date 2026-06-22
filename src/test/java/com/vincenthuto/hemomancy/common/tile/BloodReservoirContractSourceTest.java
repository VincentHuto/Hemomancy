package com.vincenthuto.hemomancy.common.tile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class BloodReservoirContractSourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");

	private BloodReservoirContractSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		Path tileRoot = SOURCE_ROOT.resolve("com/vincenthuto/hemomancy/common/tile");
		String reservoir = read(tileRoot.resolve("IBloodReservoir.java"));
		String slotAccess = read(tileRoot.resolve("IBloodContainerSlotAccess.java"));
		String capabilityAccess = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/capability/HemoCapabilityAccess.java"));
		String blockInteractions = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/block/shared/BlockBloodInteractions.java"));
		String bloodTransfer = read(tileRoot.resolve("BloodContainerTransfer.java"));
		String sourceTree = readAllJava(SOURCE_ROOT.resolve("com/vincenthuto/hemomancy/common"));

		assertContains("reservoir exposes blood capability", reservoir, "IBloodVolume getBloodCapability()");
		assertContains("reservoir owns send updates", reservoir, "default void sendUpdates()");
		assertContains("reservoir owns receive helper", reservoir, "default double receiveBlood(double amount)");
		assertContains("reservoir owns drain helper", reservoir, "default double drainBlood(double amount)");
		assertNotContains("reservoir is not tied to inventories", reservoir, "extends Container");
		assertContains("slot access exposes input slot", slotAccess, "getBloodContainerInputSlot()");
		assertContains("slot access exposes output slot", slotAccess, "getEmptyBloodContainerOutputSlot()");
		assertNotContains("slot access is not a container", slotAccess, "extends Container");
		assertContains("slot processing composes reservoir and inventory", slotAccess,
				"processBloodContainerInputSlot(Container container, IBloodReservoir reservoir)");
		assertContains("capability lookup uses reservoir contract", capabilityAccess, "be instanceof IBloodReservoir");
		assertContains("capability lookup reads reservoir attachment directly", capabilityAccess,
				"be.getData(HemoAttachmentTypes.BLOCK_BLOOD_VOLUME)");
		assertNotContains("capability lookup does not recurse through reservoir method", capabilityAccess,
				"reservoir.getBloodCapability()");
		assertContains("block interactions sync reservoirs", blockInteractions, "be instanceof IBloodReservoir reservoir");
		assertContains("blood container transfer accepts reservoir", bloodTransfer, "IBloodReservoir reservoir");
		assertNotContains("old blood tile contract removed", sourceTree, "IBloodTile");
		assertNotContains("old reservoir-container contract removed", sourceTree, "IBloodReservoirContainer");
	}

	private static String read(Path path) throws IOException {
		if (!Files.exists(path)) {
			throw new AssertionError("missing " + path);
		}
		return Files.readString(path).replace("\r\n", "\n");
	}

	private static String readAllJava(Path root) throws IOException {
		StringBuilder combined = new StringBuilder();
		try (var paths = Files.walk(root)) {
			for (Path path : paths.filter(path -> path.toString().endsWith(".java")).toList()) {
				combined.append(read(path)).append('\n');
			}
		}
		return combined.toString();
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}

	private static void assertNotContains(String label, String text, String unexpected) {
		if (text.contains(unexpected)) {
			throw new AssertionError(label + ": contained " + unexpected);
		}
	}
}
