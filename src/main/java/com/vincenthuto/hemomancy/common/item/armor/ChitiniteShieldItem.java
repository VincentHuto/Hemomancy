package com.vincenthuto.hemomancy.common.item.armor;

import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.client.render.item.ChitiniteShieldItemRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.registries.ForgeRegistries;

public class ChitiniteShieldItem extends Item {
	public static DyeColor getColor(ItemStack stack) {
		return DyeColor.byId(stack.getOrCreateTagElement("BlockEntityTag").getInt("Base"));
	}

	public ChitiniteShieldItem(Item.Properties builder) {
		super(builder.durability(1024));
		DispenserBlock.registerBehavior(this, ArmorItem.DISPENSE_ITEM_BEHAVIOR);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		BannerItem.appendHoverTextFromBannerBlockEntityTag(stack, tooltip);
	}

	@Override
	public String getDescriptionId(ItemStack stack) {
		return stack.getTagElement("BlockEntityTag") != null ? this.getDescriptionId() + '.' + getColor(stack).getName()
				: super.getDescriptionId(stack);
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.BLOCK;
	}

	@Override
	public int getUseDuration(ItemStack stack) {
		return 72000;
	}

	@Override
	public void initializeClient(Consumer<IClientItemExtensions> consumer) {
		super.initializeClient(consumer);
		consumer.accept(RenderPropChitiniteShield.INSTANCE);

	}

	@Override
	public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
		if (entityIn instanceof Player) {
			Player player = (Player) entityIn;
			if (player.getOffhandItem() == stack || player.getMainHandItem() == stack) {
				if (player.isBlocking()) {
					List<Entity> mobs = worldIn.getEntities(player, player.getBoundingBox().inflate(0.55),
							EntitySelector.pushableBy(player));
					for (Entity ent : mobs) {
						ent.hurt(entityIn.damageSources().generic(), 1.5f);
						stack.hurtAndBreak(1, player, (p_220017_1_) -> {
							p_220017_1_.broadcastBreakEvent(player.getUsedItemHand());
						});
					}
				}
			}
		}

	}

	@Override
	public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {

		return ForgeRegistries.ITEMS.tags().getTag(ItemTags.PLANKS).contains(repair.getItem())
				|| super.isValidRepairItem(toRepair, repair);
	}
	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return false;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		ItemStack itemstack = playerIn.getItemInHand(handIn);
		playerIn.startUsingItem(handIn);
		return InteractionResultHolder.consume(itemstack);
	}
}

class RenderPropChitiniteShield implements IClientItemExtensions {

	public static RenderPropChitiniteShield INSTANCE = new RenderPropChitiniteShield();


	@Override
	public BlockEntityWithoutLevelRenderer getCustomRenderer() {
		return new ChitiniteShieldItemRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
				Minecraft.getInstance().getEntityModels());
	}
}