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
				"com/vincenthuto/hemomancy/client/event/ClientEvents.java"));
		String restraint = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/entity/utility/ArmatureRestraintEntity.java"));
		String blockEntity = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/tile/crafting/HematicArmatureBlockEntity.java"));
		String block = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/block/harbinger/crafting/HematicArmatureBlock.java"));
		String rotationHelper = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/block/shared/HorizontalFacingRotationHelper.java"));
		String fillerBlock = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/block/shared/FillerBlock.java"));
		String renderer = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/render/tile/crafting/HematicArmatureRenderer.java"));
		String model = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/model/tile/crafting/HematicArmatureModel.java"));

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
		assertContains("armature places two-high filler boundaries around left back bowl stands", block,
				"new BlockPos(-2, 1, 0)");
		assertContains("armature places second left back bowl pillar one block farther outward", block,
				"new BlockPos(-3, 1, 0)");
		assertContains("armature places two-high filler boundaries around right back bowl stands", block,
				"new BlockPos(2, 1, 0)");
		assertContains("armature places second right back bowl pillar one block farther outward", block,
				"new BlockPos(3, 1, 0)");
		assertContains("armature places one-high filler boundaries around left front bowl stands", block,
				"new BlockPos(-2, 0, 1)");
		assertContains("armature places one-high filler boundaries around right front bowl stands", block,
				"new BlockPos(2, 0, 1)");
		assertNotContains("armature should not make left front bowl fillers two blocks tall", block,
				"new BlockPos(-2, 1, 1)");
		assertNotContains("armature should not make right front bowl fillers two blocks tall", block,
				"new BlockPos(2, 1, 1)");
		assertNotContains("armature should not place extra far-out left front bowl fillers", block,
				"new BlockPos(-3, 1, 1)");
		assertNotContains("armature should not place extra far-out right front bowl fillers", block,
				"new BlockPos(3, 1, 1)");
		assertContains("armature includes a top arch filler offset group", block,
				"TOP_ARCH_FILLER_OFFSETS");
		assertContains("armature blocks the left lower arch area", block,
				"new BlockPos(-2, 2, 0)");
		assertContains("armature blocks the top arch crossbar", block,
				"new BlockPos(0, 3, 0)");
		assertContains("armature blocks the right lower arch area", block,
				"new BlockPos(2, 2, 0)");
		assertContains("armature includes a reservoir heart filler offset group", block,
				"RESERVOIR_HEART_FILLER_OFFSETS");
		assertContains("armature blocks the reservoir heart", block,
				"new BlockPos(0, 4, 0)");
		assertNotContains("armature leaves access above the reservoir heart", block,
				"new BlockPos(0, 5, 0)");
		assertContains("armature placement checks full filler height", block,
				"pos.getY() + MAX_FILLER_Y_OFFSET");
		assertContains("armature rotates bowl stand filler boundaries with block facing", block,
				"rotatedFillerOffsets(facing)");
		assertContains("armature delegates filler boundary rotation to the shared helper", block,
				"HorizontalFacingRotationHelper.rotateSouthOffsets(FILLER_OFFSETS, facing)");
		assertContains("armature rotates east-facing filler boundaries to the correct side", rotationHelper,
				"case EAST -> new BlockPos(offset.getZ(), offset.getY(), -offset.getX());");
		assertContains("armature rotates west-facing filler boundaries to the correct side", rotationHelper,
				"case WEST -> new BlockPos(-offset.getZ(), offset.getY(), offset.getX());");
		assertContains("armature placement checks rotated bowl stand filler boundaries", block,
				"canPlaceMultiBlock(level, pos, facing)");
		assertContains("armature removal keeps the old facing so rotated fillers are cleaned up", block,
				"removeFillers(level, pos, state.getValue(FACING));");
		assertContains("armature has a facing-specific filler cleanup path", block,
				"private void removeFillers(Level level, BlockPos mainPos, Direction facing)");
		assertContains("armature cleanup removes stale linked fillers left by old footprints", block,
				"removeLinkedFillerBlocks(level, mainPos);");
		assertContains("armature cleanup only removes fillers linked to this armature", block,
				"mainPos.equals(filler.getMainBlockPos())");
		assertContains("armature bowl item rendering has a separate facing rotation", renderer,
				"float itemYRot = itemRotationFor(facing);");
		assertContains("armature bowl item renderer uses the item-specific yaw", renderer,
				"renderBowlItems(armature, poseStack, buffer, combinedLight, combinedOverlay, itemYRot);");
		assertContains("armature bowl item renderer fixes east-facing item locations", renderer,
				"case EAST -> 90.0F;");
		assertContains("armature bowl item renderer fixes west-facing item locations", renderer,
				"case WEST -> 270.0F;");
		assertContains("armature renderer passes partial ticks into model animation", renderer,
				"renderModel(armature, partialTicks, poseStack, buffer, combinedLight, yRot);");
		assertContains("armature model exposes banner animation setup", model,
				"public void setupBannerAnimation(float ageInTicks)");
		assertContains("armature banner animation resets the left banner pose", model,
				"bannerL.resetPose();");
		assertContains("armature banner animation resets the right banner pose", model,
				"bannerR.resetPose();");
		assertContains("armature banner animation sways the left banner", model,
				"bannerL.zRot = sway;");
		assertContains("armature banner animation sways the right banner opposite the left", model,
				"bannerR.zRot = -sway;");
		assertContains("armature banner animation keeps motion subtle", model,
				"Mth.sin(ageInTicks * 0.08F) * 0.045F");
		assertContains("armature filler keeps an outline shape so the visual bowls can be clicked", fillerBlock,
				"return Shapes.block();");
		assertContains("armature center filler keeps collision empty so players and camera can pass", fillerBlock,
				"return isHematicArmatureCenterFiller(level, pos) ? Shapes.empty() : Shapes.block();");
		assertContains("armature bowl stand fillers collide like blocks", fillerBlock,
				"relative.equals(new BlockPos(0, 1, 0)) || relative.equals(new BlockPos(0, 2, 0))");
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
