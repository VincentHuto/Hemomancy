package com.vincenthuto.hemomancy.common.tile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class IronBrazierOfferingSourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");

	private IronBrazierOfferingSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String blockEntity = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/tile/IronBrazierBlockEntity.java"));
		String block = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/block/harbinger/BrazierBlock.java"));
		String renderer = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/render/tile/functional/IronBrazierRenderer.java"));
		String clientEvents = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/event/ClientEvents.java"));

		assertContains("brazier stores one offering item", blockEntity, "private ItemStack offering");
		assertContains("brazier exposes offering copy", blockEntity, "getOfferingDisplayStack()");
		assertContains("brazier can insert a single item", blockEntity, "insertOffering");
		assertContains("brazier can extract offering", blockEntity, "extractOffering");
		assertContains("brazier can consume offering for recipes", blockEntity, "consumeOffering");
		assertContains("brazier syncs offering NBT", blockEntity, "TAG_OFFERING");
		assertContains("brazier applies empty offering update tags", blockEntity, "handleUpdateTag");
		assertContains("brazier applies block entity data packets", blockEntity, "onDataPacket");
		assertNotContains("organ reagent state is disabled", blockEntity, "lockedOrgan");
		assertNotContains("organ echo acceptance is disabled", block, "tryAcceptEcho");
		assertNotContains("organ reagent acceptance is disabled", block, "tryAcceptReagent");
		assertContains("shift right-click extracts offering", block, "extractOffering");
		assertContains("normal right-click inserts offering", block, "insertOffering");
		assertContains("blood tools pass through to projection item use", block, "isBloodTool(stack)");
		assertContains("brazier is a block blood endpoint", block, "implements EntityBlock, SimpleWaterloggedBlock, BlockBloodEndpoint");
		assertContains("brazier controls whether absorption particles may draw", block,
				"canDrawAbsorptionParticles");
		assertContains("unlit brazier particle gating uses ritual phase only", block,
				"BrazierBloodInteractionRules.shouldDrawAbsorptionParticles(state.getValue(RITUAL_PHASE) > 0)");
		assertContains("brazier lights from projected blood", block, "projectBloodIntoBlock");
		assertContains("brazier lighting costs 50 blood", block, "BLOOD_TO_LIGHT = 50.0D");
		assertContains("brazier projection spends player blood", block, "volume.subtractBloodVolume(BLOOD_TO_LIGHT)");
		assertContains("brazier projection records blood spend", block, "volume.addBloodSpend(BLOOD_TO_LIGHT)");
		assertNotContains("empty hand no longer lights brazier", block, "player.hurt(player.damageSources().generic(), 1.5f)");
		assertNotContains("flint and steel no longer lights brazier", block, "Items.FLINT_AND_STEEL");
		assertContains("breaking drops offering", block, "dropOffering");
		assertContains("renderer draws stored item", renderer, "getOfferingDisplayStack");
		assertContains("renderer uses item renderer", renderer, "renderStatic");
		assertContains("iron brazier renderer registered", clientEvents, "IronBrazierRenderer::new");
	}

	private static String read(Path path) throws IOException {
		if (!Files.exists(path)) {
			throw new AssertionError("missing " + path);
		}
		return Files.readString(path).replace("\r\n", "\n");
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
