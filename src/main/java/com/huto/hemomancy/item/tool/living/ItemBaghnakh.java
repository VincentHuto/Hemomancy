package com.huto.hemomancy.item.tool.living;

import java.util.Set;

import com.google.common.collect.Sets;
import com.huto.hemomancy.init.PotionInit;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.IVanishable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.IItemTier;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.potion.EffectInstance;

public class ItemBaghnakh extends ToolItem implements IVanishable {
	private static final Set<Block> EFFECTIVE_ON = Sets.newHashSet(Blocks.COBWEB);
	private float speed;

	public ItemBaghnakh(float speedIn, float attackDamageIn, float attackSpeedIn, IItemTier tier,
			Properties builderIn) {
		super(attackDamageIn, -2.8f, tier, EFFECTIVE_ON, builderIn);
	}

	
	@Override
	public boolean hitEntity(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		if (target.world.rand.nextDouble() > 0.35) {
			target.addPotionEffect(new EffectInstance(PotionInit.blood_loss.get(), 100, 2));
		}
		return super.hitEntity(stack, target, attacker);
	}

	@Override
	public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
		return super.onEntitySwing(stack, entity);
	}

	@Override
	public boolean canHarvestBlock(BlockState blockIn) {
		if (EFFECTIVE_ON.contains(blockIn.getBlock())) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public float getDestroySpeed(ItemStack stack, BlockState state) {
		if (EFFECTIVE_ON.contains(state.getBlock())) {
			return speed;
		} else {
			return 0.5f;
		}
	}
}
