package com.vincenthuto.hemomancy.common.item.harbinger.scar;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.IScarItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.List;

public class ItemScar extends Item implements IScarItem {
	private final DeferredHolder<ScarDefinition, ScarDefinition> scarDefinition;

	public ItemScar(Properties properties, DeferredHolder<ScarDefinition, ScarDefinition> scarDefinition) {
		super(properties);
		this.scarDefinition = scarDefinition;
	}

	@Override
	public ScarDefinition getScarDefinition() {
		return scarDefinition.get();
	}

	public void onEquipped(LivingEntity player) {
		getScarDefinition().onEquipped(player, ItemStack.EMPTY);
	}

	public void onUnequipped(LivingEntity player) {
		getScarDefinition().onUnequipped(player, ItemStack.EMPTY);
	}

	public void onWornTick(LivingEntity entity) {
		getScarDefinition().onWornTick(entity, ItemStack.EMPTY);
	}

	public void onPlayerAttack(Player player, LivingEntity target) {
		getScarDefinition().onPlayerAttack(player, target);
	}

	public void onPlayerDefend(Player player, LivingEntity attacker) {
		getScarDefinition().onPlayerDefend(player, attacker);
	}

	public void onPlayerKill(Player player, LivingEntity killed) {
		getScarDefinition().onPlayerKill(player, killed);
	}

	public float getDeepenAmount() {
		return getScarDefinition().getDeepenAmount();
	}

	public com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency getAssignedTendency() {
		return getScarDefinition().getAssignedTendency();
	}

	public int getTier() {
		return getScarDefinition().getTier();
	}

	@Override
	public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, context, tooltip, flagIn);
		getScarDefinition().appendHoverText(tooltip);
	}
}
