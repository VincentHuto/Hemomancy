package com.vincenthuto.hemomancy.client.render.entity.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.projectile.VesperScuteProjectileEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/** A detached wedge of the Crowned mount's dark-red carapace, using its authored atlas. */
public final class VesperScuteProjectileRenderer extends EntityRenderer<VesperScuteProjectileEntity> {
	private static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/boss/endgame/vesper_crowned_refusal.png");

	public VesperScuteProjectileRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public void render(VesperScuteProjectileEntity entity, float yaw, float partialTick, PoseStack poseStack,
			MultiBufferSource buffers, int light) {
		poseStack.pushPose();
		poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTick, entity.yRotO, entity.getYRot()) - 90.0F));
		poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTick, entity.xRotO, entity.getXRot())));
		poseStack.mulPose(Axis.XP.rotationDegrees((entity.tickCount + partialTick) * 34.0F));
		poseStack.scale(0.075F, 0.075F, 0.075F);
		VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutout(TEXTURE));
		PoseStack.Pose pose = poseStack.last();
		face(pose, consumer, light, 10, 0, 0, -6, -4, -2, -6, 4, -2, 0, 0, -1);
		face(pose, consumer, light, 10, 0, 0, -6, 4, 2, -6, -4, 2, 0, 0, 1);
		face(pose, consumer, light, 10, 0, 0, -6, 4, -2, -6, 4, 2, 0, 1, 0);
		face(pose, consumer, light, 10, 0, 0, -6, -4, 2, -6, -4, -2, 0, -1, 0);
		poseStack.popPose();
		super.render(entity, yaw, partialTick, poseStack, buffers, light);
	}

	private static void face(PoseStack.Pose pose, VertexConsumer out, int light,
			int ax, int ay, int az, int bx, int by, int bz, int cx, int cy, int cz, int nx, int ny, int nz) {
		vertex(pose, out, light, ax, ay, az, 0.474F, 0.232F, nx, ny, nz);
		vertex(pose, out, light, bx, by, bz, 0.498F, 0.254F, nx, ny, nz);
		vertex(pose, out, light, cx, cy, cz, 0.452F, 0.254F, nx, ny, nz);
	}

	private static void vertex(PoseStack.Pose pose, VertexConsumer out, int light, int x, int y, int z,
			float u, float v, int nx, int ny, int nz) {
		Matrix4f matrix = pose.pose();
		Matrix3f normals = pose.normal();
		Vector3f normal = new Vector3f(nx, ny, nz).mul(normals).normalize();
		out.addVertex(matrix, x, y, z).setColor(255, 255, 255, 255).setUv(u, v)
				.setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(normal.x(), normal.y(), normal.z());
	}

	@Override
	public ResourceLocation getTextureLocation(VesperScuteProjectileEntity entity) {
		return TEXTURE;
	}
}
