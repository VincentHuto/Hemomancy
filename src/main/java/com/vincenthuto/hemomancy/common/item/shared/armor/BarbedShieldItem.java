package com.vincenthuto.hemomancy.common.item.shared.armor;

import com.vincenthuto.hemomancy.client.item.HemoClientItemExtensionsProvider;
import java.util.List;
import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.client.render.item.hematic.BarbedShieldItemRenderer;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BarbedShieldItem extends Item implements HemoClientItemExtensionsProvider {
	@Nullable
	private static CompoundTag getBlockEntityTag(ItemStack stack) {
		CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
		if (customData == null) {
			return null;
		}
		CompoundTag root = customData.copyTag();
		return root.contains("BlockEntityTag", Tag.TAG_COMPOUND) ? root.getCompound("BlockEntityTag") : null;
	}

	public static DyeColor getColor(ItemStack stack) {
		CompoundTag blockEntityTag = getBlockEntityTag(stack);
		return DyeColor.byId(blockEntityTag != null ? blockEntityTag.getInt("Base") : 0);
	}

	public BarbedShieldItem(Item.Properties builder) {
		super(builder.durability(1024));
		DispenserBlock.registerBehavior(this, ArmorItem.DISPENSE_ITEM_BEHAVIOR);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		BannerItem.appendHoverTextFromBannerBlockEntityTag(stack, tooltip);
	}

	public String getDescriptionId(ItemStack stack) {
		return getBlockEntityTag(stack) != null ? this.getDescriptionId() + '.' + getColor(stack).getName()
				: super.getDescriptionId(stack);
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.BLOCK;
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity entity) {
		return 72000;
	}

	@Override
	public IClientItemExtensions hemomancy$getClientItemExtensions() {
		return RenderPropBarbedShield.INSTANCE;
	}

	@Override
	public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
		if (entityIn instanceof Player player) {
			if (player.getOffhandItem() == stack || player.getMainHandItem() == stack) {
				if (player.isBlocking()) {
					List<Entity> mobs = worldIn.getEntities(player, player.getBoundingBox().inflate(0.55),
							EntitySelector.pushableBy(player));
					for (Entity ent : mobs) {
						ent.hurt(entityIn.damageSources().generic(), 1.5f);
						stack.hurtAndBreak(1, player,
								player.getUsedItemHand() == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND
										: EquipmentSlot.OFFHAND);
					}
				}
			}
		}
	}

	@Override
	public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
		return repair.is(ItemTags.PLANKS)
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

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
class RenderPropBarbedShield implements IClientItemExtensions {

	public static RenderPropBarbedShield INSTANCE = new RenderPropBarbedShield();

	@Override
	public BlockEntityWithoutLevelRenderer getCustomRenderer() {
		return new BarbedShieldItemRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
				Minecraft.getInstance().getEntityModels());
	}
}
