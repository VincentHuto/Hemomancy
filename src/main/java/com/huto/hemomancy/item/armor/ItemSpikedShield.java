package com.huto.hemomancy.item.armor;

import java.util.List;

import javax.annotation.Nullable;

import com.huto.hemomancy.render.item.RenderItemSpikedShield;

import net.minecraft.block.DispenserBlock;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BannerItem;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemSpikedShield extends Item {
	public ItemSpikedShield(Item.Properties builder) {
		super(builder.maxDamage(1024).setISTER(() -> RenderItemSpikedShield::new));
		DispenserBlock.registerDispenseBehavior(this, ArmorItem.DISPENSER_BEHAVIOR);
	}

	@Override
	public boolean isShield(ItemStack stack, LivingEntity entity) {
		return true;
	}

	public String getTranslationKey(ItemStack stack) {
		return stack.getChildTag("BlockEntityTag") != null
				? this.getTranslationKey() + '.' + getColor(stack).getTranslationKey()
				: super.getTranslationKey(stack);
	}

	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip,
			ITooltipFlag flagIn) {
		BannerItem.appendHoverTextFromTileEntityTag(stack, tooltip);
	}

	public UseAction getUseAction(ItemStack stack) {
		return UseAction.BLOCK;
	}

	public int getUseDuration(ItemStack stack) {
		return 72000;
	}

	 @Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		 return false;
	}
	
	@Override
	public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
		if (entityIn instanceof PlayerEntity) {
			PlayerEntity player = (PlayerEntity) entityIn;
			if (player.getHeldItemOffhand() == stack || player.getHeldItemMainhand() == stack) {
				if (player.isActiveItemStackBlocking()) {
					List<Entity> mobs = worldIn.getEntitiesInAABBexcluding(player, player.getBoundingBox().grow(0.55),
							EntityPredicates.pushableBy(player));
					for (Entity ent : mobs) {
						ent.attackEntityFrom(DamageSource.GENERIC, 1.5f);
						stack.damageItem(1, player, (p_220017_1_) -> {
							p_220017_1_.sendBreakAnimation(player.getActiveHand());
						});
					}
				}
			}
		}

	}

	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
		ItemStack itemstack = playerIn.getHeldItem(handIn);
		playerIn.setActiveHand(handIn);
		return ActionResult.resultConsume(itemstack);
	}

	public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
		return ItemTags.PLANKS.contains(repair.getItem()) || super.getIsRepairable(toRepair, repair);
	}

	public static DyeColor getColor(ItemStack stack) {
		return DyeColor.byId(stack.getOrCreateChildTag("BlockEntityTag").getInt("Base"));
	}
}
