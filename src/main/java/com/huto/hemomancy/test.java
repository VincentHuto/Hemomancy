package com.huto.hemomancy;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class test extends EntityRenderer<FishingBobberEntity> {
	private static final ResourceLocation BOBBER = new ResourceLocation("textures/entity/fishing_hook.png");
	private static final RenderType field_229103_e_ = RenderType.getEntityCutout(BOBBER);

	public test(EntityRendererManager renderManagerIn) {
		super(renderManagerIn);
	}

	public void render(FishingBobberEntity entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn,
			IRenderTypeBuffer bufferIn, int packedLightIn) {
		PlayerEntity playerentity = entityIn.func_234606_i_();
		if (playerentity != null) {
			matrixStackIn.push();
			matrixStackIn.push();
			matrixStackIn.scale(0.5F, 0.5F, 0.5F);
			matrixStackIn.rotate(this.renderManager.getCameraOrientation());
			matrixStackIn.rotate(Vector3f.YP.rotationDegrees(180.0F));
			MatrixStack.Entry matrixstack$entry = matrixStackIn.getLast();
			Matrix4f matrix4f = matrixstack$entry.getMatrix();
			Matrix3f matrix3f = matrixstack$entry.getNormal();
			IVertexBuilder ivertexbuilder = bufferIn.getBuffer(field_229103_e_);
			vertex(ivertexbuilder, matrix4f, matrix3f, packedLightIn, 0.0F, 0, 0, 1);
			vertex(ivertexbuilder, matrix4f, matrix3f, packedLightIn, 1.0F, 0, 1, 1);
			vertex(ivertexbuilder, matrix4f, matrix3f, packedLightIn, 1.0F, 1, 1, 0);
			vertex(ivertexbuilder, matrix4f, matrix3f, packedLightIn, 0.0F, 1, 0, 0);
			matrixStackIn.pop();
			int i = playerentity.getPrimaryHand() == HandSide.RIGHT ? 1 : -1;
			ItemStack itemstack = playerentity.getHeldItemMainhand();
			if (!(itemstack.getItem() instanceof net.minecraft.item.FishingRodItem)) {
				i = -i;
			}

			float f = playerentity.getSwingProgress(partialTicks);
			float f1 = MathHelper.sin(MathHelper.sqrt(f) * (float) Math.PI);
			float f2 = MathHelper.lerp(partialTicks, playerentity.prevRenderYawOffset, playerentity.renderYawOffset)
					* ((float) Math.PI / 180F);
			double d0 = (double) MathHelper.sin(f2);
			double d1 = (double) MathHelper.cos(f2);
			double d2 = (double) i * 0.35D;
			double d4;
			double d5;
			double d6;
			float f3;
			if ((this.renderManager.options == null || this.renderManager.options.getPointOfView().func_243192_a())
					&& playerentity == Minecraft.getInstance().player) {
				double d7 = this.renderManager.options.fov;
				d7 = d7 / 100.0D;
				Vector3d vector3d = new Vector3d((double) i * -0.36D * d7, -0.045D * d7, 0.4D);
				vector3d = vector3d.rotatePitch(
						-MathHelper.lerp(partialTicks, playerentity.prevRotationPitch, playerentity.rotationPitch)
								* ((float) Math.PI / 180F));
				vector3d = vector3d.rotateYaw(
						-MathHelper.lerp(partialTicks, playerentity.prevRotationYaw, playerentity.rotationYaw)
								* ((float) Math.PI / 180F));
				vector3d = vector3d.rotateYaw(f1 * 0.5F);
				vector3d = vector3d.rotatePitch(-f1 * 0.7F);
				d4 = MathHelper.lerp((double) partialTicks, playerentity.prevPosX, playerentity.getPosX()) + vector3d.x;
				d5 = MathHelper.lerp((double) partialTicks, playerentity.prevPosY, playerentity.getPosY()) + vector3d.y;
				d6 = MathHelper.lerp((double) partialTicks, playerentity.prevPosZ, playerentity.getPosZ()) + vector3d.z;
				f3 = playerentity.getEyeHeight();
			} else {
				d4 = MathHelper.lerp((double) partialTicks, playerentity.prevPosX, playerentity.getPosX()) - d1 * d2
						- d0 * 0.8D;
				d5 = playerentity.prevPosY + (double) playerentity.getEyeHeight()
						+ (playerentity.getPosY() - playerentity.prevPosY) * (double) partialTicks - 0.45D;
				d6 = MathHelper.lerp((double) partialTicks, playerentity.prevPosZ, playerentity.getPosZ()) - d0 * d2
						+ d1 * 0.8D;
				f3 = playerentity.isCrouching() ? -0.1875F : 0.0F;
			}

			double d9 = MathHelper.lerp((double) partialTicks, entityIn.prevPosX, entityIn.getPosX());
			double d10 = MathHelper.lerp((double) partialTicks, entityIn.prevPosY, entityIn.getPosY()) + 0.25D;
			double d8 = MathHelper.lerp((double) partialTicks, entityIn.prevPosZ, entityIn.getPosZ());
			float f4 = (float) (d4 - d9);
			float f5 = (float) (d5 - d10) + f3;
			float f6 = (float) (d6 - d8);
			IVertexBuilder ivertexbuilder1 = bufferIn.getBuffer(RenderType.getLines());
			Matrix4f matrix4f1 = matrixStackIn.getLast().getMatrix();

			for (int k = 0; k < 16; ++k) {
				stringVertex(f4, f5, f6, ivertexbuilder1, matrix4f1, fraction(k, 16));
				stringVertex(f4, f5, f6, ivertexbuilder1, matrix4f1, fraction(k + 1, 16));
			}

			matrixStackIn.pop();
			super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
		}
	}

	private static float fraction(int i, int j) {
		return (float) i / (float) j;
	}

	private static void vertex(IVertexBuilder vb, Matrix4f matrix4f, Matrix3f matrix3f, int light, float x, int y,
			int u, int v) {
		vb.pos(matrix4f, x - 0.5F, (float) y - 0.5F, 0.0F).color(255, 255, 255, 255).tex((float) u, (float) v)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(light).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
	}

	private static void stringVertex(float x, float y, float z, IVertexBuilder vb, Matrix4f matrixIn, float grav) {
		vb.pos(matrixIn, x * grav, y * (grav * grav + grav) * 0.5F + 0.25F, z * grav).color(0, 0, 0, 255).endVertex();
	}

	/**
	 * Returns the location of an entity's texture.
	 */
	public ResourceLocation getEntityTexture(FishingBobberEntity entity) {
		return BOBBER;
	}
}
