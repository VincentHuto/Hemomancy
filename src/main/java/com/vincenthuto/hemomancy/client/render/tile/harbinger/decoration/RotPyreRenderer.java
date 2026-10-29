package com.vincenthuto.hemomancy.client.render.tile.harbinger.decoration;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.render.world.PyreFlames;
import com.vincenthuto.hemomancy.common.block.harbinger.decoration.RotPyreBlock;
import com.vincenthuto.hemomancy.common.tile.harbinger.decoration.RotPyreBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** A lit Rot Pyre's Flammeus fire on its ember bed; an unlit pyre draws nothing over its cold char. */
public class RotPyreRenderer implements BlockEntityRenderer<RotPyreBlockEntity> {
	private static final Vec3 BED = new Vec3(0.5, RotPyreBlock.BED / 16.0, 0.5);
	/** The ring of smaller tongues stands this far out on the bed (it is 7 px across). */
	private static final double RING = 2.6 / 16.0;

	public RotPyreRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public void render(RotPyreBlockEntity pyre, float partialTick, PoseStack poseStack, MultiBufferSource buffers,
			int light, int overlay) {
		if (pyre.getLevel() == null || !pyre.getBlockState().getValue(RotPyreBlock.LIT)) return;
		double time = AnimationTime.gameTime(pyre.getLevel(), (double) partialTick);
		PyreFlames.render(poseStack, buffers, pyre.getBlockPos(), BED, RING,
				(RotPyreBlock.FLAME_TIP - RotPyreBlock.BED) / 16.0, false, time);
	}

	@Override
	public AABB getRenderBoundingBox(RotPyreBlockEntity pyre) {
		// The tongues, smoke and licks rise above the bones.
		return new AABB(pyre.getBlockPos()).expandTowards(0.0, 1.0, 0.0);
	}
}
