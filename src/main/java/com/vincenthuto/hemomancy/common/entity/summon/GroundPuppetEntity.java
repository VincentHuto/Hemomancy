package com.vincenthuto.hemomancy.common.entity.summon;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Shared lifecycle boundary for puppet bodies that reuse Zombie locomotion and animation.
 * Puppets are constructed bodies, not undead mobs, so Zombie-specific daylight, drowning,
 * growth, loot-pickup, and equipment behavior must never leak into them.
 */
public abstract class GroundPuppetEntity extends Zombie {
	protected GroundPuppetEntity(EntityType<? extends Zombie> type, Level level) {
		super(type, level);
		resetInheritedZombieState();
	}

	@Override
	protected boolean isSunSensitive() {
		return false;
	}

	@Override
	protected boolean convertsInWater() {
		return false;
	}

	@Override
	protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
		// Puppet shapes never roll Zombie weapons or armor.
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		resetInheritedZombieState();
	}

	private void resetInheritedZombieState() {
		setBaby(false);
		setCanPickUpLoot(false);
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			setItemSlot(slot, ItemStack.EMPTY);
			setDropChance(slot, 0.0F);
		}
	}
}
