package com.vincenthuto.hemomancy.client.render.tile.harbinger.functional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.FruitingBodyCacheBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

/**
 * Draws a closed fruiting-body cache's two cap halves, breathing slowly over what they hold. The body below is the
 * block model; the split when its owner opens it is the moment ghost's (HeartNodeMomentRenderer), since the block
 * goes at once.
 */
public class FruitingBodyCacheRenderer implements BlockEntityRenderer<FruitingBodyCacheBlockEntity> {
	/** Ticks of one breath, and how much the cap swells at the top of it. */
	private static final float BREATH_TICKS = 90.0F;
	private static final float BREATH_RISE = 0.035F;
	private static final float BREATH_SPREAD = 0.015F;

	public FruitingBodyCacheRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public void render(FruitingBodyCacheBlockEntity cache, float partialTick, PoseStack pose,
			MultiBufferSource buffers, int light, int overlay) {
		Level level = cache.getLevel();
		if (level == null) return;
		// Caches don't breathe in step: each starts its breath at its own point.
		float offset = Math.floorMod(cache.getBlockPos().asLong(), (long) BREATH_TICKS);
		float time = AnimationTime.gameTime(level, partialTick) + offset;
		float breath = 0.5F + 0.5F * Mth.sin(time * Mth.TWO_PI / BREATH_TICKS);
		pose.pushPose();
		HeartNodeParts.scaleAbout(pose, 0.5F, HeartNodeParts.LID_HINGE_Y, 0.5F, 1.0F + BREATH_SPREAD * breath,
				1.0F + BREATH_RISE * breath, 1.0F + BREATH_SPREAD * breath);
		HeartNodeParts.draw(pose, buffers, HeartNodeParts.LID_WEST, light);
		HeartNodeParts.draw(pose, buffers, HeartNodeParts.LID_EAST, light);
		pose.popPose();
	}
}
