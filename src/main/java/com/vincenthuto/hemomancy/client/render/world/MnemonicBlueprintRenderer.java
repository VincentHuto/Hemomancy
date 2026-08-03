package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.item.shared.MnemonicBlueprintTarget;
import com.vincenthuto.hemomancy.common.recipe.BloodStructureRecipe;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hutoslib.math.MultiblockPattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.model.data.ModelData;

import java.util.ArrayList;
import java.util.List;

public final class MnemonicBlueprintRenderer {
	private static final float GHOST_ALPHA = 0.28F;
	private static final double MAX_DISTANCE_SQR = 4096.0D;
	private static final long MATERIAL_CYCLE_TICKS = 40L;

	private static MnemonicBlueprintTarget target;
	private static MnemonicBlueprintPattern pattern;
	private static BlockPos floorCenter;
	private static Direction facing = Direction.NORTH;
	private static MnemonicBlueprintProgress.Summary progress = MnemonicBlueprintProgress.complete();
	private static final MnemonicBlueprintCompletionNotice COMPLETION_NOTICE =
			new MnemonicBlueprintCompletionNotice();
	private static final MnemonicBlueprintProjectionSession<Level> SESSION =
			new MnemonicBlueprintProjectionSession<>();

	private MnemonicBlueprintRenderer() {
	}

	public static boolean anchor(MnemonicBlueprintTarget selected, BlockPos center, Direction direction) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.level == null || selected == null || center == null) return false;
		MultiblockPattern resolved = resolvePattern(minecraft.level, selected);
		if (resolved == null) return false;
		target = selected;
		pattern = MnemonicBlueprintPattern.from(resolved);
		floorCenter = center.immutable();
		facing = direction == null || direction.getAxis().isVertical() ? Direction.NORTH : direction;
		SESSION.activate(minecraft.level);
		COMPLETION_NOTICE.reset();
		boolean hasCells = !pattern.cells().isEmpty();
		updateProgress(minecraft.level);
		return hasCells;
	}

	public static boolean clear() {
		boolean wasActive = SESSION.clear();
		resetProjectionData();
		return wasActive;
	}

	public static boolean disconnect() {
		boolean wasActive = SESSION.disconnect();
		resetProjectionData();
		return wasActive;
	}

	private static void resetProjectionData() {
		target = null;
		pattern = null;
		floorCenter = null;
		facing = Direction.NORTH;
		progress = MnemonicBlueprintProgress.complete();
		COMPLETION_NOTICE.reset();
	}

	private static void finishProjectionData() {
		target = null;
		pattern = null;
		floorCenter = null;
		facing = Direction.NORTH;
	}

	public static boolean isActive() {
		return SESSION.isActive() && target != null && pattern != null && floorCenter != null;
	}

	public static void tick() {
		Minecraft minecraft = Minecraft.getInstance();
		COMPLETION_NOTICE.tick();
		if (SESSION.clearIfWorldChanged(minecraft.level)) {
			resetProjectionData();
		} else if (minecraft.level != null && isActive()) {
			updateProgress(minecraft.level);
		}
	}

	public static MnemonicBlueprintProgress.Summary progress() {
		return progress;
	}

	public static boolean shouldRenderProgressOverlay() {
		return isActive()
				? COMPLETION_NOTICE.shouldRender(progress.remaining())
				: progress.remaining() == 0 && COMPLETION_NOTICE.shouldRender(0);
	}

	private static void updateProgress(Level level) {
		if (level == null || pattern == null || floorCenter == null) {
			progress = MnemonicBlueprintProgress.complete();
			return;
		}
		List<String> missing = new ArrayList<>();
		for (MnemonicBlueprintPattern.Cell cell : pattern.cells()) {
			BlockPos worldPos = MnemonicBlueprintPlacement.worldPosition(
					floorCenter, cell.localPos(), pattern.bounds(), facing);
			if (!MnemonicBlueprintPattern.matches(cell.key(), level.getBlockState(worldPos))) {
				missing.add(BuiltInRegistries.BLOCK.getKey(cell.key().fallbackBlock()).toString());
			}
		}
		progress = MnemonicBlueprintProgress.summarize(missing);
		COMPLETION_NOTICE.updateRemaining(progress.remaining());
		if (SESSION.clearIfComplete(progress.remaining())) finishProjectionData();
	}

	public static void render(RenderLevelStageEvent event) {
		if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS || !isActive()) return;
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.level == null || minecraft.player == null) return;
		if (floorCenter.distToCenterSqr(minecraft.player.position()) > MAX_DISTANCE_SQR) return;

		PoseStack poses = event.getPoseStack();
		Vec3 camera = event.getCamera().getPosition();
		MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
		MultiBufferSource ghostBuffers = ignored -> new AlphaVertexConsumer(
				buffers.getBuffer(RenderType.translucentMovingBlock()), GHOST_ALPHA);
		long cycle = minecraft.level.getGameTime() / MATERIAL_CYCLE_TICKS;
		for (MnemonicBlueprintPattern.Cell cell : pattern.cells()) {
			BlockPos worldPos = MnemonicBlueprintPlacement.worldPosition(
					floorCenter, cell.localPos(), pattern.bounds(), facing);
			if (MnemonicBlueprintPattern.matches(cell.key(), minecraft.level.getBlockState(worldPos))) continue;
			Block display = cell.key().displayBlock(cycle);
			if (display == null || display == Blocks.AIR) continue;
			poses.pushPose();
			poses.translate(worldPos.getX() - camera.x, worldPos.getY() - camera.y, worldPos.getZ() - camera.z);
			try {
				minecraft.getBlockRenderer().renderSingleBlock(display.defaultBlockState(), poses, ghostBuffers,
						LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ModelData.EMPTY,
						RenderType.translucentMovingBlock());
			} catch (RuntimeException ignored) {
				// A single unusual block model must not suppress the rest of the projection.
			}
			poses.popPose();
		}
		buffers.endBatch(RenderType.translucentMovingBlock());
	}

	private static MultiblockPattern resolvePattern(Level level, MnemonicBlueprintTarget selected) {
		if (selected.type() == MnemonicBlueprintTarget.Type.CARDINAL_RITE) {
			CardinalRiteRecipe rite = CardinalRiteRecipe.getRiteByLocation(level, selected.recipeId());
			return rite == null ? null : rite.getPreviewPattern();
		}
		BloodStructureRecipe structure = BloodStructureRecipe.getStructureByLocation(level, selected.recipeId());
		return structure == null ? null : structure.getPattern();
	}

	private record AlphaVertexConsumer(VertexConsumer delegate, float alpha) implements VertexConsumer {
		@Override public VertexConsumer addVertex(float x, float y, float z) { delegate.addVertex(x, y, z); return this; }
		@Override public VertexConsumer setColor(int red, int green, int blue, int vertexAlpha) {
			delegate.setColor(red, green, blue, Math.round(vertexAlpha * alpha)); return this;
		}
		@Override public VertexConsumer setUv(float u, float v) { delegate.setUv(u, v); return this; }
		@Override public VertexConsumer setUv1(int u, int v) { delegate.setUv1(u, v); return this; }
		@Override public VertexConsumer setUv2(int u, int v) { delegate.setUv2(u, v); return this; }
		@Override public VertexConsumer setNormal(float x, float y, float z) { delegate.setNormal(x, y, z); return this; }
	}
}
