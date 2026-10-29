package com.vincenthuto.hemomancy.common.entity.mob.facsimile;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.animal.horse.Donkey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** The Gardens' copy of a donkey. It carries nothing: no chest, saddle, food or foal. */
public class FacsimileDonkeyEntity extends Donkey {
	public FacsimileDonkeyEntity(EntityType<? extends FacsimileDonkeyEntity> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder setAttributes() {
		return AbstractChestedHorse.createBaseChestedHorseAttributes();
	}

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand hand) {
		return InteractionResult.PASS;
	}

	@Override
	public boolean isFood(ItemStack stack) {
		return false;
	}

	@Override
	public boolean canMate(Animal other) {
		return false;
	}

	/** The Gardens' own fauna: it spawns in the Gardens' dark and off grass, unlike the animal it copies. */
	@Override
	public boolean checkSpawnRules(LevelAccessor level, MobSpawnType reason) {
		return true;
	}

	/** No foal, from breeding or a spawn egg: the hive copies bodies, it does not raise young. */
	@Override
	public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
		return null;
	}
}
