package com.vincenthuto.hemomancy.client.render.tile.harbinger.puzzle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.render.world.PyreFlames;
import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules;
import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.PulsePylonBlock;
import com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.PulsePylonBlockEntity;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * A pulse pylon's charge ring, about its head: it idles dim and slow, and as the Chapel focus winds the charge up
 * before a pulse it brightens, quickens and swells. The ring follows the pylon's style: crimson on the venous pylon,
 * white-gold on Seraphae's anchor, pale blue-white on Velorum's frost pillar. Putriciel's pyre has no ring: the
 * charge feeds a Flammeus fire on its ember bed that grows with it (hottest at full charge), and when the charge falls
 * from full to nothing, the pulse, a purge of fire sweeps out over the floor from the pylon's foot and burns out.
 * Models from tools/model_export/pulse_pylon.py.
 */
public class PulsePylonRenderer implements BlockEntityRenderer<PulsePylonBlockEntity> {
	public static final ModelResourceLocation RING =
			ModelResourceLocation.standalone(Hemomancy.rloc("block/pulse_pylon_ring"));
	public static final ModelResourceLocation RING_ANCHOR =
			ModelResourceLocation.standalone(Hemomancy.rloc("block/pulse_pylon_ring_anchor"));
	public static final ModelResourceLocation RING_FROST =
			ModelResourceLocation.standalone(Hemomancy.rloc("block/pulse_pylon_ring_frost"));
	private static final RenderType RING_TYPE = RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS);
	/** The ring's centre in upper-cell pixels (pulse_pylon.py RING_Y), the same for every ringed style. */
	private static final float RING_Y = 6f;
	/** Ticks the ring takes to ease from one charge step to the next. */
	private static final float EASE_TICKS = 5f;
	/** The pyre's ember bed in the upper cell (pulse_pylon.py PYRE_BED), where its fire stands. */
	private static final Vec3 PYRE_BED = new Vec3(0.5, 3.6 / 16.0, 0.5);
	private static final double PYRE_RING = 2.4 / 16.0;
	/** The pyre fire's height over the bed at rest and at full charge, in blocks. */
	private static final double IDLE_FIRE = 0.18, FULL_FIRE = 0.75;
	/** The floor under the pylon's foot, in the upper cell's space. */
	private static final Vec3 FLOOR = new Vec3(0.5, -2 + 0.02, 0.5);
	/** How long a purge takes to sweep out to the pulse's reach and burn out, in ticks. */
	private static final float PURGE_TICKS = 20f;
	/** The purge starts just outside the plinth (15 px across). */
	private static final double PURGE_START = 0.9;

	public PulsePylonRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public void render(PulsePylonBlockEntity pylon, float partialTick, PoseStack poseStack, MultiBufferSource buffers,
			int light, int overlay) {
		if (pylon.getLevel() == null) return;
		float time = AnimationTime.gameTime(pylon.getLevel(), partialTick);
		float flare = flare(pylon, time);
		if (pylon.getBlockState().getValue(PulsePylonBlock.STYLE) == PulsePylonBlock.Style.PYRE) {
			pyre(pylon, poseStack, buffers, flare, time);
			return;
		}
		int block = Mth.clamp(Math.round(4 + 11 * flare), LightTexture.block(light), 15);
		int glow = LightTexture.pack(block, LightTexture.sky(light));
		float tint = 0.6f + 0.4f * flare;
		float swell = 1f + 0.1f * flare;
		poseStack.pushPose();
		poseStack.translate(0.5, RING_Y / 16f, 0.5);
		poseStack.mulPose(Axis.YP.rotationDegrees(time * (0.6f + 6f * flare)));
		poseStack.scale(swell, 1f, swell);
		poseStack.translate(-0.5, -RING_Y / 16f, -0.5);
		Minecraft mc = Minecraft.getInstance();
		mc.getBlockRenderer().getModelRenderer().renderModel(poseStack.last(), buffers.getBuffer(RING_TYPE), null,
				mc.getModelManager().getModel(ring(pylon)), tint, tint, tint, glow, overlay);
		poseStack.popPose();
	}

	/** The pyre's fire, grown by the charge, and the purge of a pulse that has just fired. */
	private static void pyre(PulsePylonBlockEntity pylon, PoseStack poseStack, MultiBufferSource buffers, float flare,
			float time) {
		PyreFlames.render(poseStack, buffers, pylon.getBlockPos(), PYRE_BED, PYRE_RING,
				HemoEasing.lerp(IDLE_FIRE, FULL_FIRE, flare), pylon.charge() == PulsePylonBlock.MAX_CHARGE, time);
		if (pylon.changedAt() == Long.MIN_VALUE || pylon.charge() != 0
				|| pylon.previousCharge() != PulsePylonBlock.MAX_CHARGE) return;
		float progress = (time - pylon.changedAt()) / PURGE_TICKS;
		if (progress < 0 || progress >= 1) return;
		double radius = HemoEasing.lerp(PURGE_START, ChapelRules.PULSE_REACH, HemoEasing.easeOutCubic((double) progress));
		float fade = 1f - HemoEasing.smoothstep(0.55f, 1f, progress);
		PyreFlames.purge(poseStack, buffers, pylon.getBlockPos(), FLOOR, radius, fade, progress, time);
	}

	@Override
	public AABB getRenderBoundingBox(PulsePylonBlockEntity pylon) {
		if (pylon.getBlockState().getValue(PulsePylonBlock.STYLE) != PulsePylonBlock.Style.PYRE) {
			return new AABB(pylon.getBlockPos());
		}
		// The pyre's fire rises over its head, and a purge sweeps the floor out to the pulse's reach.
		double reach = ChapelRules.PULSE_REACH + 1;
		return new AABB(pylon.getBlockPos()).inflate(reach, 0, reach).expandTowards(0, 1.5, 0).expandTowards(0, -2, 0);
	}

	private static ModelResourceLocation ring(PulsePylonBlockEntity pylon) {
		return switch (pylon.getBlockState().getValue(PulsePylonBlock.STYLE)) {
			case ANCHOR -> RING_ANCHOR;
			case FROST -> RING_FROST;
			default -> RING;
		};
	}

	/** 0 at rest, 1 at full charge, eased across each step the focus winds. */
	private static float flare(PulsePylonBlockEntity pylon, float time) {
		float to = pylon.charge() / (float) PulsePylonBlock.MAX_CHARGE;
		if (pylon.changedAt() == Long.MIN_VALUE) return to;
		float from = pylon.previousCharge() / (float) PulsePylonBlock.MAX_CHARGE;
		float step = HemoEasing.smoothstep(HemoEasing.clamp01((time - pylon.changedAt()) / EASE_TICKS));
		return HemoEasing.lerp(from, to, step);
	}
}
