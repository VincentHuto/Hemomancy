package com.vincenthuto.hemomancy.client.render.entity.boss.endgame;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.render.entity.boss.annetta.TendrilTubeBrush;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantGlobEntity;
import com.vincenthuto.hutoslib.client.HLRenderTypeInit;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * What she throws, in flight. A nectar glob is a wobbling drop of glossy amber stretched along its flight under a warm
 * glow; a mortar egg is a tumbling sac of the same skin as the brood tumors it can root as, its yolk glowing through.
 * Their trails (nectar beads, shed spores) are particles the entity emits.
 */
public class MycophantGlobRenderer extends EntityRenderer<MycophantGlobEntity> {
	private static final ResourceLocation NECTAR = Hemomancy.rloc("textures/entity/boss/endgame/mycophant_nectar.png");
	private static final ResourceLocation EGG = Hemomancy.rloc("textures/block/brood_tumor_side.png");
	private static final RenderType NECTAR_SKIN = RenderType.entityTranslucentEmissive(NECTAR);
	private static final RenderType EGG_SKIN = RenderType.entityCutoutNoCull(EGG);
	private static final int SLICES = 10;
	private static final int STACKS = 7;
	private static final float PIXELS = 16.0F;

	public MycophantGlobRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.shadowRadius = 0.0F;
	}

	@Override
	public void render(MycophantGlobEntity glob, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
			int light) {
		float age = AnimationTime.age(glob, partialTick);
		boolean egg = glob.kind() == MycophantGlobEntity.Kind.EGG;
		Vec3 motion = glob.getDeltaMovement();
		poseStack.pushPose();
		poseStack.translate(0.0F, 0.25F, 0.0F);

		// The glow first, facing the camera.
		poseStack.pushPose();
		poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
		float beat = 0.5F + 0.5F * Mth.sin(age * 0.5F);
		float glowSize = (egg ? 1.4F : 1.0F) * PIXELS;
		TendrilTubeBrush.halo(poseStack.last(), buffer.getBuffer(HLRenderTypeInit.TENDRIL_GLOW), 0.0F, 0.0F, 0.0F, glowSize, glowSize,
				1.0F, egg ? 0.66F : 0.81F, egg ? 0.16F : 0.31F, 0.35F + 0.15F * beat);
		poseStack.popPose();

		// Then the body, turned along its flight.
		double horizontal = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
		float heading = (float) Math.toDegrees(Math.atan2(motion.x, motion.z));
		float pitch = (float) Math.toDegrees(Math.atan2(motion.y, horizontal));
		poseStack.mulPose(Axis.YP.rotationDegrees(heading));
		poseStack.mulPose(Axis.XP.rotationDegrees(-pitch));
		if (egg) {
			// The egg tumbles end over end.
			poseStack.mulPose(Axis.XP.rotationDegrees(age * 14.0F));
			poseStack.mulPose(Axis.ZP.rotationDegrees(glob.getYRot()));
			ellipsoid(poseStack.last(), buffer.getBuffer(EGG_SKIN), 0.3F, 0.38F, 0.3F, light, 1.0F, 0.0F);
		} else {
			// The drop stretches along its flight and wobbles.
			float speed = (float) Math.min(1.0D, motion.length() * 2.0D);
			float wobble = 0.08F * Mth.sin(age * 1.3F);
			ellipsoid(poseStack.last(), buffer.getBuffer(NECTAR_SKIN), 0.2F * (1.0F + wobble), 0.2F * (1.0F - wobble),
					0.2F * (1.0F + 0.45F * speed), 0xF000F0, 0.9F, age * 0.02F);
		}
		poseStack.popPose();
		super.render(glob, yaw, partialTick, poseStack, buffer, light);
	}

	/** A lit latitude-longitude ellipsoid with semi-axes x, y, z in blocks, its texture wrapped once round it. */
	private static void ellipsoid(PoseStack.Pose pose, VertexConsumer out, float rx, float ry, float rz, int light, float alpha,
			float scroll) {
		for (int stack = 0; stack < STACKS; stack++) {
			float v0 = stack / (float) STACKS, v1 = (stack + 1) / (float) STACKS;
			for (int slice = 0; slice < SLICES; slice++) {
				float u0 = slice / (float) SLICES, u1 = (slice + 1) / (float) SLICES;
				vertex(pose, out, rx, ry, rz, u0, v0, light, alpha, scroll);
				vertex(pose, out, rx, ry, rz, u0, v1, light, alpha, scroll);
				vertex(pose, out, rx, ry, rz, u1, v1, light, alpha, scroll);
				vertex(pose, out, rx, ry, rz, u1, v0, light, alpha, scroll);
			}
		}
	}

	private static void vertex(PoseStack.Pose pose, VertexConsumer out, float rx, float ry, float rz, float u, float v, int light,
			float alpha, float scroll) {
		float theta = u * Mth.TWO_PI, phi = v * Mth.PI;
		float nx = Mth.sin(phi) * Mth.cos(theta), ny = Mth.cos(phi), nz = Mth.sin(phi) * Mth.sin(theta);
		out.addVertex(pose, nx * rx, ny * ry, nz * rz)
				.setColor(1.0F, 1.0F, 1.0F, alpha)
				.setUv(u, v + scroll)
				.setOverlay(OverlayTexture.NO_OVERLAY)
				.setLight(light)
				.setNormal(pose, nx, ny, nz);
	}

	@Override
	public ResourceLocation getTextureLocation(MycophantGlobEntity entity) {
		return entity.kind() == MycophantGlobEntity.Kind.EGG ? EGG : NECTAR;
	}
}
