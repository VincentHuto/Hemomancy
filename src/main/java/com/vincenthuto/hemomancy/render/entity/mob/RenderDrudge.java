package com.vincenthuto.hemomancy.render.entity.mob;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.entity.drudge.EntityDrudge;
import com.vincenthuto.hemomancy.event.ClientTickHandler;
import com.vincenthuto.hemomancy.model.entity.mob.ModelDrudge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

public class RenderDrudge extends MobRenderer<EntityDrudge, ModelDrudge<EntityDrudge>> {

	protected static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/drudge/model_drudge.png");

	public RenderDrudge(Context renderManagerIn) {
		super(renderManagerIn, new ModelDrudge<EntityDrudge>(renderManagerIn.bakeLayer(ModelDrudge.LAYER_LOCATION)),
				0.5F);

	}

	@Override
	public void render(EntityDrudge entityIn, float entityYaw, float partialTicks, PoseStack ms,
			MultiBufferSource bufferIn, int packedLightIn) {
		super.render(entityIn, entityYaw, partialTicks, ms, bufferIn, packedLightIn);
		Minecraft mc = Minecraft.getInstance();

		int rank = entityIn.getDrudgeRank();
		Font fontR = mc.font;
		ms.scale(0.05f, 0.05f, 0.05f);
		ms.translate(0, 21, 0);
		ms.pushPose();
		ms.mulPose(new Quaternion(Vector3f.XP, 180, true));
		ms.mulPose(new Quaternion(Vector3f.YP, entityIn.tickCount * 0.13f, false));
		fontR.draw(ms, String.valueOf(rank), 0, -10, 0);
		fontR.draw(ms, entityIn.getRoleTitle().name(), 0, -20, 0);
		ms.popPose();
		ms.pushPose();
		ms.mulPose(new Quaternion(Vector3f.XP, 180, true));
		ms.mulPose(new Quaternion(Vector3f.YN, entityIn.tickCount * 0.13f, false));
		fontR.draw(ms, String.valueOf(rank), 0, -10, 0);
		fontR.draw(ms, entityIn.getRoleTitle().name(), 0, -20, 0);
		ms.popPose();

		if (entityIn.getDrudgeInventory() != null) {
			ms.pushPose();
			SimpleContainer inv = entityIn.getDrudgeInventory();
			int items = 0;
			for (int i = 0; i < inv.getContainerSize(); i++) {
				if (inv.getItem(i).isEmpty()) {
					break;
				} else {
					items++;
				}
			}
			float[] angles = new float[inv.getContainerSize()];

			float anglePer = 360F / items;
			float totalAngle = 0F;
			for (int i = 0; i < angles.length; i++) {
				angles[i] = totalAngle += anglePer;
			}

			double time = ClientTickHandler.ticksInGame + partialTicks;

			for (int i = 0; i < inv.getContainerSize(); i++) {
				
				ms.pushPose();
				ms.translate(0.5F, 1.25F, 0.5F);
				ms.mulPose(Vector3f.YP.rotationDegrees(angles[i] + (float) time));
				ms.translate(1.125F, 0F, 0.25F);
				ms.mulPose(Vector3f.YP.rotationDegrees(90F));
				ms.translate(0D, 0.075 * Math.sin((time + i * 10) / 5D), 0F);
				ItemStack stack = inv.getItem(i);
				if (!stack.isEmpty()) {
					mc.getItemRenderer().renderStatic(stack, ItemTransforms.TransformType.FIXED, packedLightIn,
							OverlayTexture.NO_OVERLAY, ms, bufferIn, i);
				}
				ms.popPose();
			}

			ms.popPose();
		}
	}

	@Override
	public ResourceLocation getTextureLocation(EntityDrudge entity) {
		return entity.getDrudgeRoleReLoc();

	}

}
