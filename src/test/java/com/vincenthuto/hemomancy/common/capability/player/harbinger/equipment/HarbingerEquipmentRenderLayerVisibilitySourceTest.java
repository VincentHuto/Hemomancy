package com.vincenthuto.hemomancy.common.capability.player.harbinger.equipment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class HarbingerEquipmentRenderLayerVisibilitySourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");

	private HarbingerEquipmentRenderLayerVisibilitySourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String contract = read("com/vincenthuto/hemomancy/common/capability/player/harbinger/equipment/IHarbingerEquipmentItemHandler.java");
		String container = read("com/vincenthuto/hemomancy/common/capability/player/harbinger/equipment/HarbingerEquipmentContainer.java");

		assertContains("handler exposes visibility getter", contract,
				"boolean isRenderLayerVisible(int slot);");
		assertContains("handler exposes visibility setter", contract,
				"void setRenderLayerVisible(int slot, boolean visible);");
		assertContains("container stores render layer visibility", container,
				"private final boolean[] renderLayerVisible = new boolean[SCAR_SLOTS];");
		assertContains("new containers default visibility on", container,
				"Arrays.fill(renderLayerVisible, true);");
		assertContains("container serializes render layer visibility", container,
				"nbt.put(\"RenderLayerVisibility\", visibilityTag);");
		assertContains("container deserializes old saves as visible", container,
				"Arrays.fill(renderLayerVisible, true);");
		assertContains("container reads visibility nbt only when present", container,
				"if (nbt.contains(\"RenderLayerVisibility\"))");
		assertContains("container writes slot booleans", container,
				"visibilityTag.putBoolean(Integer.toString(i), renderLayerVisible[i]);");
		assertContains("container reads slot booleans", container,
				"renderLayerVisible[i] = visibilityTag.getBoolean(Integer.toString(i));");
		assertContains("container clamps invalid getter slots visible", container,
				"if (slot < 0 || slot >= renderLayerVisible.length) return true;");
	}

	private static String read(String path) throws IOException {
		Path fullPath = SOURCE_ROOT.resolve(path);
		if (!Files.exists(fullPath)) {
			throw new AssertionError("missing " + fullPath);
		}
		return Files.readString(fullPath).replace("\r\n", "\n");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}
}
