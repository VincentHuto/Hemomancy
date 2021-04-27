package com.huto.hemomancy.item.rune;

import java.util.List;

import javax.annotation.Nullable;

import com.huto.hemomancy.capa.rune.IRune;
import com.huto.hemomancy.capa.rune.RuneType;
import com.huto.hemomancy.init.ItemInit;
import com.huto.hemomancy.render.item.RenderArmBanner;
import com.huto.hemomancy.render.layer.IRenderRunes;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BannerItem;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

public class ItemArmBanner extends Item implements IRune, IRenderRunes {

	public ItemArmBanner(Properties prop) {
		super(prop.maxStackSize(1).setISTER(() -> RenderArmBanner::new));
	}

	@Override
	public String getTranslationKey(ItemStack stack) {
		return stack.getChildTag("BlockEntityTag") != null
				? this.getTranslationKey() + '.' + getColor(stack).getTranslationKey()
				: super.getTranslationKey(stack);
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip,
			ITooltipFlag flagIn) {
		BannerItem.appendHoverTextFromTileEntityTag(stack, tooltip);
	}

	@Override

	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
		ItemStack itemstack = playerIn.getHeldItem(handIn);
		playerIn.setActiveHand(handIn);
		return ActionResult.resultConsume(itemstack);
	}

	@Override

	public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
		return ItemTags.PLANKS.contains(repair.getItem()) || super.getIsRepairable(toRepair, repair);
	}

	public static DyeColor getColor(ItemStack stack) {
		return DyeColor.byId(stack.getOrCreateChildTag("BlockEntityTag").getInt("Base"));
	}

	@Override
	public void onPlayerRuneRender(MatrixStack matrix, ItemStack stack, int packedLight,
			IRenderTypeBuffer iRenderTypeBuffer, PlayerEntity player, RenderType type, float partialTicks) {
		matrix.push();
		if (stack.getItem() == ItemInit.arm_banner.get()) {
			EntityRenderer<?> renderer = Minecraft.getInstance().getRenderManager().getRenderer(player);
			EntityModel<?> model = ((IEntityRenderer<?, ?>) renderer).getEntityModel();
			if (model instanceof BipedModel<?>) {
				BipedModel<?> biModel = (BipedModel<?>) model;
				biModel.bipedLeftArm.translateRotate(matrix);
			}
			matrix.push();
			if (stack.getItem() instanceof ItemArmBanner) {
				RenderHelper.enableStandardItemLighting();
				matrix.rotate(Vector3f.XN.rotationDegrees(180f));
				matrix.rotate(Vector3f.YN.rotationDegrees(90f));
				matrix.rotate(Vector3f.ZN.rotationDegrees(-72.5f));
				matrix.scale(0.5f, 0.5f, 0.5f);
				matrix.translate(-0.35, 0, -0.35);
				Minecraft.getInstance().getItemRenderer().renderItem(stack, TransformType.FIXED, packedLight,
						OverlayTexture.NO_OVERLAY, matrix, iRenderTypeBuffer);
			}
			matrix.pop();
		}
		matrix.pop();

	}

	@Override
	public RuneType getRuneType() {
		return RuneType.BANNER;
	}

	@Override
	public void onPlayerRuneRender(MatrixStack matrix, int packedLight, IRenderTypeBuffer iRenderTypeBuffer,
			PlayerEntity player, RenderType type, float partialTicks) {
	}

}
