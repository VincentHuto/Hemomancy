package com.huto.hemomancy.render.entity.mob;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.entity.drudge.EntityDrudge;
import com.huto.hemomancy.event.ClientTickHandler;
import com.huto.hemomancy.model.entity.mob.ModelDrudge;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class RenderDrudge extends MobRenderer<EntityDrudge, ModelDrudge> {

	protected static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/drudge/model_drudge.png");

	public RenderDrudge(EntityRenderDispatcher renderManagerIn) {
		super(renderManagerIn, new ModelDrudge(), 0.25f);

	}

	@Override
	public void render(EntityDrudge entityIn, float entityYaw, float partialTicks, PoseStack ms,
			MultiBufferSource bufferIn, int packedLightIn) {
		super.render(entityIn, entityYaw, partialTicks, ms, bufferIn, packedLightIn);

		int rank = entityIn.getDrudgeRank();
		Minecraft mc = Minecraft.getInstance();
		Font fontR = mc.fontRenderer;
		ms.scale(0.05f, 0.05f, 0.05f);
		ms.translate(0, 21, 0);
		ms.push();
		ms.rotate(new Quaternion(Vector3f.XP, 180, true));
		ms.rotate(new Quaternion(Vector3f.YP, entityIn.ticksExisted * 0.13f, false));
		fontR.drawString(ms, String.valueOf(rank), 0, -10, 0);
		fontR.drawString(ms, entityIn.getRoleTitle().name(), 0, -20, 0);
		ms.pop();
		ms.push();
		ms.rotate(new Quaternion(Vector3f.XP, 180, true));
		ms.rotate(new Quaternion(Vector3f.YN, entityIn.ticksExisted * 0.13f, false));
		fontR.drawString(ms, String.valueOf(rank), 0, -10, 0);
		fontR.drawString(ms, entityIn.getRoleTitle().name(), 0, -20, 0);
		ms.pop();

		if (entityIn.getDrudgeInventory() != null) {
			ms.push();
			Inventory inv = entityIn.getDrudgeInventory();
			int items = 0;
			for (int i = 0; i < inv.getSizeInventory(); i++) {
				if (inv.getStackInSlot(i).isEmpty()) {
					break;
				} else {
					items++;
				}
			}
			float[] angles = new float[inv.getSizeInventory()];

			float anglePer = 360F / items;
			float totalAngle = 0F;
			for (int i = 0; i < angles.length; i++) {
				angles[i] = totalAngle += anglePer;
			}

			double time = ClientTickHandler.ticksInGame + partialTicks;

			for (int i = 0; i < inv.getSizeInventory(); i++) {

				ms.push();
				ms.translate(0.5F, 1.25F, 0.5F);
				ms.rotate(Vector3f.YP.rotationDegrees(angles[i] + (float) time));
				ms.translate(1.125F, 0F, 0.25F);
				ms.rotate(Vector3f.YP.rotationDegrees(90F));
				ms.translate(0D, 0.075 * Math.sin((time + i * 10) / 5D), 0F);
				ItemStack stack = inv.getStackInSlot(i);
				if (!stack.isEmpty()) {
					mc.getItemRenderer().renderItem(stack, ItemCameraTransforms.TransformType.GROUND, packedLightIn,
							OverlayTexture.NO_OVERLAY, ms, bufferIn);
				}
				ms.pop();
			}

			ms.pop();
		}
	}

	@Override
	public ResourceLocation getEntityTexture(EntityDrudge entity) {
		return entity.getDrudgeRoleReLoc();

	}

}
