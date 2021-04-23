package com.huto.hemomancy.render.item;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.init.ItemInit;
import com.huto.hemomancy.item.ItemTome;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.model.BookModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;

public class RenderItemTome extends ItemStackTileEntityRenderer {
	public final BookModel model = new BookModel();
	public static ResourceLocation liber_sanguinum = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/liber_sanguinum.png");
	public static ResourceLocation liber_inclinatio = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/liber_inclinatio.png");
	public static ResourceLocation liber_inclinatio_hidden = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/liber_inclinatio_hidden.png");
	public static ResourceLocation glowing_page = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/glow_page.png");

	public int ticks;
	public float field_195523_f;
	public float field_195524_g;
	public float field_195525_h;
	public float field_195526_i;
	public float nextPageTurningSpeed;
	public float pageTurningSpeed;
	public float nextPageAngle;
	public float pageAngle;
	public float field_195531_n;

	public RenderItemTome() {
	}

	float close = 1f;

	@Override
	public void func_239207_a_(ItemStack stack, ItemCameraTransforms.TransformType transform, MatrixStack ms,
			IRenderTypeBuffer buffers, int light, int overlay) {
		Minecraft mc = Minecraft.getInstance();
		ClientPlayerEntity player = mc.player;

		if (stack.getItem() instanceof ItemTome) {
			ItemTome item = (ItemTome) stack.getItem();
			ms.push();
			ms.translate(0.5D, 0.50D, 0.5D);
			ms.rotate(Vector3f.XP.rotationDegrees(45));
			ms.rotate(Vector3f.ZP.rotationDegrees(15));
			ms.rotate(Vector3f.YP.rotationDegrees(-60));

			float f = (float) item.ticks + 1;
			ms.translate(0.0D, (double) (0.1F + MathHelper.sin(f * 0.1F) * 0.01F), 0.0D);

			float f1;
			for (f1 = item.nextPageAngle - item.pageAngle; f1 >= (float) Math.PI; f1 -= ((float) Math.PI * 2F)) {
			}

			while (f1 < -(float) Math.PI) {
				f1 += ((float) Math.PI * 2F);
			}
			float f2 = item.pageAngle + f1 * 2;
			ms.rotate(Vector3f.YP.rotation(-f2));
			ms.rotate(Vector3f.ZP.rotationDegrees(80.0F));
			float f3 = MathHelper.lerp(1, item.field_195524_g, item.field_195523_f);
			float f4 = MathHelper.frac(f3 + 0.25F) * 1.6F - 0.3F;
			float f5 = MathHelper.frac(f3 + 0.75F) * 1.6F - 0.3F;
			if (player.getHeldItemMainhand() == stack || player.getHeldItemOffhand() == stack) {
				if (close < 1) {
					close += 0.03f;
					this.model.setBookState(f, MathHelper.clamp(f4, 0.0F, 1.0F), MathHelper.clamp(f5, 0.0F, 1.0F),
							close);
				}
			} else {
				if (close > 0) {
					close -= 0.03f;
					this.model.setBookState(f, MathHelper.clamp(f4, 0.0F, 1.0F), MathHelper.clamp(f5, 0.0F, 1.0F),
							close);
				}

			}
			IRenderTypeBuffer.Impl irendertypebuffer$impl = IRenderTypeBuffer
					.getImpl(Tessellator.getInstance().getBuffer());
			ResourceLocation location = stack.getItem() == ItemInit.liber_sanguinum.get() ? liber_sanguinum
					: stack.getItem() == ItemInit.liber_inclinatio.get() ? liber_inclinatio
							: stack.getItem() == ItemInit.liber_inclinatio_hidden.get() ? liber_inclinatio_hidden
									: liber_sanguinum;
			IVertexBuilder ivertexbuilder = irendertypebuffer$impl.getBuffer(model.getRenderType(location));
			ms.scale(0.75f, 0.75f, 0.75f);
			if (transform == TransformType.GUI) {
				ms.translate(0.15, 0.03, 0);
				ms.scale(0.8f, 0.8f, 0.8f);
				ms.rotate(new Quaternion(Vector3f.YP, -125, true));
				ms.rotate(new Quaternion(Vector3f.XP, 35, true));
				ms.rotate(new Quaternion(Vector3f.ZP, 45, true));

			}

			model.render(ms, ivertexbuilder, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

			irendertypebuffer$impl.finish();
			ms.pop();
		}
	}

	public BookModel getModel() {
		return model;
	}
}