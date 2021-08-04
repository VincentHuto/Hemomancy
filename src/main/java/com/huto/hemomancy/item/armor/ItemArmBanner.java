package com.huto.hemomancy.item.armor;

import java.util.List;

import javax.annotation.Nullable;

import com.huto.hemomancy.capa.rune.IRune;
import com.huto.hemomancy.capa.rune.RuneType;
import com.huto.hemomancy.render.layer.IRenderRunes;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class ItemArmBanner extends Item implements IRune, IRenderRunes {
	public ArmorMaterial material;

	public ItemArmBanner(Properties prop, ArmorMaterial materialIn) {
		super(prop.stacksTo(1));
		this.material = materialIn;
	}
	
//	@Override
//	public void initializeClient(Consumer<IItemRenderProperties> consumer) {
//		consumer.accept(new IItemRenderProperties() {
//			final BlockEntityWithoutLevelRenderer myRenderer = new RenderArmBanner(null, null);
//
//			@Override
//			public BlockEntityWithoutLevelRenderer getItemStackRenderer() {
//				return myRenderer;
//			}
//		});
//	}


	@Override
	public String getDescriptionId(ItemStack stack) {
		return stack.getTagElement("BlockEntityTag") != null ? this.getDescriptionId() + '.' + getColor(stack).getName()
				: super.getDescriptionId(stack);
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		BannerItem.appendHoverTextFromBannerBlockEntityTag(stack, tooltip);
	}

	@Override

	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		ItemStack itemstack = playerIn.getItemInHand(handIn);
		playerIn.startUsingItem(handIn);
		return InteractionResultHolder.consume(itemstack);
	}

	@Override

	public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
		return ItemTags.PLANKS.contains(repair.getItem()) || super.isValidRepairItem(toRepair, repair);
	}

	public static DyeColor getColor(ItemStack stack) {
		return DyeColor.byId(stack.getOrCreateTagElement("BlockEntityTag").getInt("Base"));
	}

	@Override
	public void onPlayerRuneRender(PoseStack matrix, ItemStack stack, int packedLight,
			MultiBufferSource iRenderTypeBuffer, Player player, RenderType type, float partialTicks) {
		matrix.pushPose();
		if (stack.getItem() instanceof ItemArmBanner) {
			EntityRenderer<?> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(player);
			EntityModel<?> model = ((RenderLayerParent<?, ?>) renderer).getModel();
			if (model instanceof HumanoidModel<?>) {
				HumanoidModel<?> biModel = (HumanoidModel<?>) model;
				biModel.leftArm.translateAndRotate(matrix);
			}
			matrix.pushPose();
			if (stack.getItem() instanceof ItemArmBanner) {
				Lighting.setupFor3DItems();
				matrix.mulPose(Vector3f.XN.rotationDegrees(180f));
				matrix.mulPose(Vector3f.YN.rotationDegrees(90f));
				matrix.mulPose(Vector3f.ZN.rotationDegrees(-72.5f));
				matrix.scale(0.5f, 0.5f, 0.5f);
				matrix.translate(-0.35, 0, -0.35);
				Minecraft.getInstance().getItemRenderer().renderStatic(stack, TransformType.FIXED, packedLight,
						OverlayTexture.NO_OVERLAY, matrix, iRenderTypeBuffer, 0);
			}
			matrix.popPose();
		}
		matrix.popPose();

	}

	@Override
	public RuneType getRuneType() {
		return RuneType.BANNER;
	}

	@Override
	public void onPlayerRuneRender(PoseStack matrix, int packedLight, MultiBufferSource iRenderTypeBuffer,
			Player player, RenderType type, float partialTicks) {
	}

}
