package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.data.BloodBindingTendrilClientState;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class BloodBindingTendrilRenderer {
	private BloodBindingTendrilRenderer() {
	}

	public static void render(PoseStack poseStack, float partialTick) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.level == null || minecraft.player == null) return;
		List<BloodBindingTendrilClientState.Snapshot> snapshots =
				BloodBindingTendrilClientState.snapshots(partialTick);
		if (snapshots.isEmpty()) return;

		MultiBufferSource.BufferSource buffer = minecraft.renderBuffers().bufferSource();
		Vec3 camera = minecraft.gameRenderer.getMainCamera().getPosition();
		float time = minecraft.level.getGameTime() + partialTick;
		renderPass(poseStack, buffer.getBuffer(RenderTypeInit.RITE_BOUNDARY_GLOW), snapshots,
				minecraft.level, minecraft.player, camera, time, true);
		buffer.endBatch(RenderTypeInit.RITE_BOUNDARY_GLOW);
		renderPass(poseStack, buffer.getBuffer(RenderTypeInit.RITE_BOUNDARY_CORE), snapshots,
				minecraft.level, minecraft.player, camera, time, false);
		buffer.endBatch(RenderTypeInit.RITE_BOUNDARY_CORE);
	}

	private static void renderPass(PoseStack poseStack, VertexConsumer consumer,
			List<BloodBindingTendrilClientState.Snapshot> snapshots, ClientLevel level,
			net.minecraft.world.entity.Entity viewer, Vec3 camera, float time, boolean glowPass) {
		for (BloodBindingTendrilClientState.Snapshot snapshot : snapshots) {
			double top = Math.max(snapshot.casterFeet().y, snapshot.targetFeet().y) + 3.0D;
			double bottom = Math.min(snapshot.casterFeet().y, snapshot.targetFeet().y) - 4.0D;
			var strands = BloodBindingTendrilGeometry.strands(snapshot.casterFeet(), snapshot.targetFeet(),
					snapshot.targetHeight(), snapshot.seed(), time, snapshot.ageTicks(),
					snapshot.retractionTicks(), (x, z) -> surfaceY(level, viewer, x, z, top, bottom));
			SanguineTendrilRibbonRenderer.render(poseStack, consumer, strands, camera, glowPass);
		}
	}

	private static double surfaceY(ClientLevel level, net.minecraft.world.entity.Entity viewer,
			double x, double z, double top, double bottom) {
		HitResult hit = level.clip(new ClipContext(new Vec3(x, top, z), new Vec3(x, bottom, z),
				ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, viewer));
		return hit.getType() == HitResult.Type.BLOCK ? hit.getLocation().y : Double.NaN;
	}
}
