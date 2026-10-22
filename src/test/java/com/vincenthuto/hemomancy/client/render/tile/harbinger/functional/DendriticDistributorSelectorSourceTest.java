package com.vincenthuto.hemomancy.client.render.tile.harbinger.functional;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DendriticDistributorSelectorSourceTest {
	@Test
	void selectorIsSyncedAndRenderedFromTheBlockEntity() throws IOException {
		String entity = read("src/main/java/com/vincenthuto/hemomancy/common/tile/harbinger/functional/DendriticDistributorBlockEntity.java");
		String packet = read("src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/manips/SynapticLoadoutActionPacket.java");
		String renderer = read("src/main/java/com/vincenthuto/hemomancy/client/render/tile/harbinger/functional/DendriticDistributorRenderer.java");
		String events = read("src/main/java/com/vincenthuto/hemomancy/client/registry/ModelRegistrar.java");
		String screen = read("src/main/java/com/vincenthuto/hemomancy/client/screen/tile/functional/SynapticLoadoutScreen.java");
		String block = read("src/main/resources/assets/hemomancy/models/block/dendritic_distributor.json");

		assertTrue(entity.contains("tag.putInt(SELECTED_SLOT_TAG, selectedSlot)"), "selected slot persists");
		assertTrue(entity.contains("ClientboundBlockEntityDataPacket.create(this)"), "selected slot reaches clients");
		assertTrue(entity.contains("te.tickSelector();"), "client tick animates the selector every tick");
		assertTrue(packet.contains("if (apply(player, known, msg.slotIndex)) markSelected(player, msg);"),
				"only a successful Apply moves the selector");
		assertTrue(packet.contains("if (saveOrOverwrite(player, known, volume, msg.slotIndex, msg.name)) markSelected(player, msg);"),
				"only a successful Save moves the selector");
		assertTrue(packet.contains("case RENAME -> rename(player, known, msg.slotIndex, msg.name);"),
				"Rename leaves the selector alone");
		assertTrue(renderer.contains("te.selectorDegrees(partialTicks)"), "selector turns with interpolation");
		assertTrue(renderer.contains("if (te.selectorSettled())"), "bouton lights only after the sweep lands");
		assertTrue(renderer.contains("LightTexture.FULL_BRIGHT"), "lit bouton and impulse are full-bright");
		assertTrue(events.contains("DendriticDistributorRenderer.PART_MODELS"), "part models are registered");
		assertTrue(screen.contains("distributor.getSelectedSlot()"), "screen opens on the selected pattern");
		assertFalse(block.contains("bouton_lit") || block.contains("selector_"),
				"the static block model must not bake a selection");
	}

	private static String read(String path) throws IOException {
		return Files.readString(Path.of(path)).replace("\r\n", "\n");
	}
}
