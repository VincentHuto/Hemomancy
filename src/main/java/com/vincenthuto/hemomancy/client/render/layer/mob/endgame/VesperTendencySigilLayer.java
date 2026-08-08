package com.vincenthuto.hemomancy.client.render.layer.mob.endgame;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.model.entity.boss.endgame.VesperTheEveningStarModel;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperTheEveningStarEntity;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperCombatRules;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.phys.Vec3;

public final class VesperTendencySigilLayer
		extends RenderLayer<VesperTheEveningStarEntity, VesperTheEveningStarModel> {
	private static final int[][] COLORS = {
			{255, 0, 0}, {255, 100, 0}, {255, 255, 0}, {255, 255, 255},
			{0, 58, 0}, {0, 100, 255}, {53, 53, 53}, {70, 0, 110}
	};

	public VesperTendencySigilLayer(RenderLayerParent<VesperTheEveningStarEntity, VesperTheEveningStarModel> parent) {
		super(parent);
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
			VesperTheEveningStarEntity entity, float limbSwing, float limbSwingAmount,
			float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
		if (entity.isInvisible()) return;
		float dissolve = entity.isAwaitingAbsorption()
				? VesperCombatRules.sigilDissolveAlpha((int) (entity.getDownedTicks() + partialTick))
				: 1.0F;
		if (dissolve <= 0.0F) return;
		poseStack.pushPose();
		poseStack.translate(0.0D, -2.25D, 0.72D);
		VertexConsumer consumer = buffer.getBuffer(RenderType.lines());
		PoseStack.Pose pose = poseStack.last();
		int active = entity.getActiveTendency().ordinal();
		boolean constellationPulse = entity.getHealth() <= entity.getMaxHealth() * 0.25F
				&& entity.getStanceTick() < 30;
		for (int i = 0; i < EnumBloodTendency.values().length; i++) {
			double angle = Math.PI * 2.0D * i / EnumBloodTendency.values().length - Math.PI / 2.0D;
			double cx = Math.cos(angle) * 1.75D;
			double cy = Math.sin(angle) * 1.75D;
			double size = i == active ? 0.34D : 0.27D;
			int alpha = (int) ((i == active ? 255 : constellationPulse ? 150 : 55) * dissolve);
			int[] color = COLORS[i];
			diamond(pose, consumer, cx, cy, size, i, color[0], color[1], color[2], alpha);
		}
		poseStack.popPose();
	}

	private static void diamond(PoseStack.Pose pose, VertexConsumer consumer, double x, double y, double size, int motif,
			int red, int green, int blue, int alpha) {
		Vec3 top = new Vec3(x, y - size, 0.0D);
		Vec3 right = new Vec3(x + size, y, 0.0D);
		Vec3 bottom = new Vec3(x, y + size, 0.0D);
		Vec3 left = new Vec3(x - size, y, 0.0D);
		line(pose, consumer, top, right, red, green, blue, alpha);
		line(pose, consumer, right, bottom, red, green, blue, alpha);
		line(pose, consumer, bottom, left, red, green, blue, alpha);
		line(pose, consumer, left, top, red, green, blue, alpha);
		switch (motif) {
			case 0 -> line(pose, consumer, top, bottom, red, green, blue, alpha);
			case 1 -> line(pose, consumer, left, right, red, green, blue, alpha);
			case 2 -> {
				line(pose, consumer, top, bottom, red, green, blue, alpha);
				line(pose, consumer, left, right, red, green, blue, alpha);
			}
			case 3 -> {
				line(pose, consumer, top, new Vec3(x, y, 0.0D), red, green, blue, alpha);
				line(pose, consumer, left, bottom, red, green, blue, alpha);
			}
			case 4 -> diamond(pose, consumer, x, y, size * 0.48D, -1, red, green, blue, alpha);
			case 5 -> {
				line(pose, consumer, top, bottom, red, green, blue, alpha);
				diamond(pose, consumer, x, y, size * 0.42D, -1, red, green, blue, alpha);
			}
			case 6 -> {
				line(pose, consumer, top, bottom, red, green, blue, alpha);
				line(pose, consumer, left, right, red, green, blue, alpha);
				diamond(pose, consumer, x, y, size * 0.34D, -1, red, green, blue, alpha);
			}
			case 7 -> {
				line(pose, consumer, top, right, red, green, blue, alpha);
				line(pose, consumer, left, bottom, red, green, blue, alpha);
				diamond(pose, consumer, x, y, size * 0.58D, -1, red, green, blue, alpha);
			}
			default -> { }
		}
	}

	private static void line(PoseStack.Pose pose, VertexConsumer consumer, Vec3 from, Vec3 to,
			int red, int green, int blue, int alpha) {
		Vec3 normal = to.subtract(from).normalize();
		consumer.addVertex(pose.pose(), (float) from.x, (float) from.y, (float) from.z)
				.setColor(red, green, blue, alpha)
				.setNormal(pose, (float) normal.x, (float) normal.y, (float) normal.z);
		consumer.addVertex(pose.pose(), (float) to.x, (float) to.y, (float) to.z)
				.setColor(red, green, blue, alpha)
				.setNormal(pose, (float) normal.x, (float) normal.y, (float) normal.z);
	}
}
