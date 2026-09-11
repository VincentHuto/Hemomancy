package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.client.vein.EarthenVeinTravelVisuals;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;
import com.vincenthuto.hemomancy.common.vein.EarthenVeinFeedingVortexProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public final class EarthenVeinFeedingTendrilRenderer {
	private EarthenVeinFeedingTendrilRenderer() {
	}

	public static void render(PoseStack poseStack, MultiBufferSource buffers, BlockPos pos,
			EarthenVeinTravelVisuals.Visual visual, float time) {
		var profile = EarthenVeinFeedingVortexProfile.forPhase(visual.phase(), visual.progress());
		if (!profile.graspingTendrils()) return;
		var strands = EarthenVeinFeedingTendrilGeometry.strands(visual.phase(), visual.progress(), time);
		Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition()
				.subtract(pos.getX(), pos.getY(), pos.getZ());
		SanguineTendrilRibbonRenderer.renderLocal(poseStack,
				buffers.getBuffer(RenderTypeInit.RITE_BOUNDARY_GLOW), strands, camera, true);
		SanguineTendrilRibbonRenderer.renderLocal(poseStack,
				buffers.getBuffer(RenderTypeInit.RITE_BOUNDARY_CORE), strands, camera, false);
	}
}
