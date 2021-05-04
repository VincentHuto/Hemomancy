package com.huto.hemomancy.item.tool;

import java.util.List;

import com.huto.hemomancy.entity.drudge.EntityDrudge;
import com.huto.hemomancy.init.ItemInit;
import com.hutoslib.client.particle.ParticleUtil;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class ItemDrudgeElectrode extends Item {

	public static String TAG_MODE = "mode";

	public ItemDrudgeElectrode(Properties properties) {
		super(properties);
	}

	@Override
	public boolean hitEntity(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		if (target instanceof EntityDrudge) {
			if (!target.world.isRemote) {
				ParticleUtil.spawnPoof((ServerWorld) target.world, target.getPosition());
			}
			ItemEntity itemEnt = new ItemEntity(target.world, target.getPosX(), target.getPosY(), target.getPosZ(),
					new ItemStack(ItemInit.living_will.get(), 1));
			target.world.addEntity(itemEnt);
			target.remove();
			return false;
		} else {
			return super.hitEntity(stack, target, attacker);
		}
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
		ItemStack stack = playerIn.getHeldItemMainhand();
		if (stack.getItem() instanceof ItemDrudgeElectrode) {
			CompoundNBT compound = stack.getOrCreateTag();
			if (!compound.getBoolean(TAG_MODE)) {
				playerIn.playSound(SoundEvents.BLOCK_BEACON_ACTIVATE, 0.40f, 1F);
				compound.putBoolean(TAG_MODE, !compound.getBoolean(TAG_MODE));
			} else {
				playerIn.playSound(SoundEvents.BLOCK_BEACON_DEACTIVATE, 0.40f, 1F);
				compound.putBoolean(TAG_MODE, !compound.getBoolean(TAG_MODE));
			}
			stack.setTag(compound);
		}
		return super.onItemRightClick(worldIn, playerIn, handIn);
	}

	@Override
	public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);

	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		tooltip.add(new StringTextComponent("Used to give and reflect commands to drudges"));
		if (stack.hasTag()) {
			if (stack.getTag().getBoolean(TAG_MODE)) {
				tooltip.add(new TranslationTextComponent("State: On").mergeStyle(TextFormatting.RED));
			} else {
				tooltip.add(new TranslationTextComponent("State: Off").mergeStyle(TextFormatting.GRAY));
			}
		}
		super.addInformation(stack, worldIn, tooltip, flagIn);

	}
}
