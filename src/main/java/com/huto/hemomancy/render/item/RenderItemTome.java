package com.huto.hemomancy.render.item;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.init.ItemInit;
import com.huto.hemomancy.item.ItemTome;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class RenderItemTome extends BlockEntityWithoutLevelRenderer {
	public RenderItemTome(BlockEntityRenderDispatcher p_172550_, EntityModelSet p_172551_) {
		super(p_172550_, p_172551_);
		// TODO Auto-generated constructor stub
	}

	public BookModel model;
	public static ResourceLocation liber_sanguinum = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/liber_sanguinum.png");
	public static ResourceLocation liber_inclinatio = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/liber_inclinatio.png");
	public static ResourceLocation liber_inclinatio_hidden = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/liber_inclinatio_hidden.png");
	public static ResourceLocation glowing_page = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/glow_page.png");

	float close = 1f;

	@Override
	public void renderByItem(ItemStack stack, ItemTransforms.TransformType transform, PoseStack ms,
			MultiBufferSource buffers, int light, int overlay) {
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;

		if (stack.getItem() instanceof ItemTome) {
			ItemTome item = (ItemTome) stack.getItem();
			ms.pushPose();
			ms.translate(0.5D, 0.50D, 0.5D);
			ms.mulPose(Vector3f.XP.rotationDegrees(45));
			ms.mulPose(Vector3f.ZP.rotationDegrees(15));
			ms.mulPose(Vector3f.YP.rotationDegrees(-60));

			float f = (float) item.ticks + 1;
			ms.translate(0.0D, 0.1F + Mth.sin(f * 0.1F) * 0.01F, 0.0D);

			float f1;
			for (f1 = item.nextPageAngle - item.pageAngle; f1 >= (float) Math.PI; f1 -= ((float) Math.PI * 2F)) {
			}

			while (f1 < -(float) Math.PI) {
				f1 += ((float) Math.PI * 2F);
			}
			float f2 = item.pageAngle + f1 * 2;
			ms.mulPose(Vector3f.YP.rotation(-f2));
			ms.mulPose(Vector3f.ZP.rotationDegrees(80.0F));
			float f3 = Mth.lerp(1, item.oFlip, item.flip);
			float f4 = Mth.frac(f3 + 0.25F) * 1.6F - 0.3F;
			float f5 = Mth.frac(f3 + 0.75F) * 1.6F - 0.3F;
			if (player.getMainHandItem() == stack || player.getOffhandItem() == stack) {
				if (close < 1) {
					close += 0.005f;
					this.model.setupAnim(f, Mth.clamp(f4, 0.0F, 1.0F), Mth.clamp(f5, 0.0F, 1.0F), close);
				}
			} else {
				if (close > 0) {
					close -= 0.005f;
					this.model.setupAnim(f, Mth.clamp(f4, 0.0F, 1.0F), Mth.clamp(f5, 0.0F, 1.0F), close);
				}

			}
			MultiBufferSource.BufferSource irendertypebuffer$impl = MultiBufferSource
					.immediate(Tesselator.getInstance().getBuilder());
			ResourceLocation location = stack.getItem() == ItemInit.liber_sanguinum.get() ? liber_sanguinum
					: stack.getItem() == ItemInit.liber_inclinatio.get() ? liber_inclinatio
							: stack.getItem() == ItemInit.liber_inclinatio_hidden.get() ? liber_inclinatio_hidden
									: liber_sanguinum;
			VertexConsumer ivertexbuilder = irendertypebuffer$impl.getBuffer(model.renderType(location));
			ms.scale(0.75f, 0.75f, 0.75f);
			if (transform == TransformType.GUI) {
				ms.translate(0.15, 0.03, 0);
				ms.scale(0.8f, 0.8f, 0.8f);
				ms.mulPose(new Quaternion(Vector3f.YP, -125, true));
				ms.mulPose(new Quaternion(Vector3f.XP, 35, true));
				ms.mulPose(new Quaternion(Vector3f.ZP, 45, true));

			}

			model.renderToBuffer(ms, ivertexbuilder, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

			irendertypebuffer$impl.endBatch();
			ms.popPose();
		}
	}

	public BookModel getModel() {
		return model;
	}
}