package com.vincenthuto.hemomancy.common.block.harbinger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class HematicArmatureInteractionSourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");

	private HematicArmatureInteractionSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String clientEvents = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/event/ArmatureCameraEvents.java"));
		String restraint = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/entity/utility/ArmatureRestraintEntity.java"));
		String blockEntity = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/tile/harbinger/crafting/HematicArmatureBlockEntity.java"));
		String block = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/block/harbinger/crafting/HematicArmatureBlock.java"));
		String geometry = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/block/harbinger/crafting/ArmatureGeometry.java"));
		String fillerBlock = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/block/shared/FillerBlock.java"));
		String renderer = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/render/tile/harbinger/crafting/HematicArmatureRenderer.java"));

		assertContains("armature uses front-facing third person camera", clientEvents,
				"CameraType.THIRD_PERSON_FRONT");
		assertContains("restraint observes shift dismount input", restraint,
				"player.isShiftKeyDown()");
		assertContains("restraint releases rider on shift", restraint,
				"player.stopRiding();");
		assertContains("restraint dismounts outside the armature footprint", restraint,
				"dismountLocation(armaturePos");
		assertContains("bowl insertion delegates single item capacity to rules", blockEntity,
				"ArmatureUpgradeRules.insertedCountForBowl(getItem(slot).isEmpty(), held.getCount())");
		assertContains("bowl insertion chooses one next empty bowl", blockEntity,
				"ArmatureUpgradeRules.nextInsertableBowlSlot(occupiedBowlSlots())");
		assertNotContains("bowl insertion must not merge into an occupied stack", blockEntity,
				"bowlStack.grow(");
		assertContains("armature footprint comes from the generated frame geometry", block,
				"return ArmatureGeometry.fillers(facing);");
		assertContains("armature footprint reaches the outer bowl stands two cells out", geometry,
				"{-2, 0, 0}, {-2, 1, 0}");
		assertContains("armature footprint keeps the low front bowl stands", geometry,
				"{-1, 0, -1}");
		assertNotContains("armature footprint is one cell slimmer on each side than the old frame", geometry,
				"{-3,");
		assertContains("armature footprint blocks the top crossbeam and reservoir heart", geometry,
				"{0, 3, 0}, {0, 4, 0}");
		assertContains("armature placement checks full filler height", block,
				"pos.getY() + MAX_FILLER_Y_OFFSET");
		assertContains("armature placement checks rotated filler cells", block,
				"canPlaceMultiBlock(level, pos, facing)");
		assertContains("armature removal keeps the old facing so rotated fillers are cleaned up", block,
				"removeFillers(level, pos, state.getValue(FACING));");
		assertContains("armature has a facing-specific filler cleanup path", block,
				"private void removeFillers(Level level, BlockPos mainPos, Direction facing)");
		assertContains("armature cleanup removes stale linked fillers left by old footprints", block,
				"removeLinkedFillerBlocks(level, mainPos);");
		assertContains("armature cleanup only removes fillers linked to this armature", block,
				"mainPos.equals(filler.getMainBlockPos())");
		assertContains("armature seats the restrained subject on its dais", block,
				"RESTRAINT_Y_OFFSET = ArmatureGeometry.DAIS_TOP / 16.0D");
		assertContains("armature bowl items turn with the same south-frame rotation as the particles", renderer,
				"renderBowlItems(armature, mc, time, pose, buffers, light, overlay, itemRotationFor(facing));");
		assertContains("armature bowl item renderer fixes east-facing item locations", renderer,
				"case EAST -> 90.0F;");
		assertContains("armature bowl item renderer fixes west-facing item locations", renderer,
				"case WEST -> 270.0F;");
		assertContains("armature particles and bowl items share generated anchors", blockEntity,
				"BOWL_PARTICLE_OFFSETS = ArmatureGeometry.BOWLS;");
		assertContains("armature banners sway opposite each other", renderer,
				"Axis.ZP.rotationDegrees(k == 0 ? sway : -sway)");
		assertContains("armature banner animation keeps motion subtle", renderer,
				"Mth.sin(time * .08F) * 2.6F");
		assertContains("armature fillers outline and collide with their own frame cell", fillerBlock,
				"HematicArmatureBlock.partShape(controller, offset)");
		assertContains("armature subject column keeps an outline but no collision", fillerBlock,
				"if (ArmatureGeometry.holdsSubject(offset)) return collision ? Shapes.empty() : Shapes.block();");
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
			throw new AssertionError(label + ": still contains " + unexpected);
		}
	}
}
