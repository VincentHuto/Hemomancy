package com.vincenthuto.hemomancy.common.entity.mob.arthropod;

import com.vincenthuto.hemomancy.common.init.SoundInit;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/** The Chthonian Queen; her bites wear wooden tools faster. */
public class ChthonianQueenEntity extends AbstractChthonianEntity {

	public ChthonianQueenEntity(EntityType<? extends ChthonianQueenEntity> type, Level worldIn) {
		super(type, worldIn);
	}

	@Override
	protected int woodenToolWear() {
		return 8;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundInit.ENTITY_CHTHONIAN_QUEEN_AMBIENT.get();
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundInit.ENTITY_CHTHONIAN_QUEEN_DEATH.get();
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return SoundInit.ENTITY_CHTHONIAN_QUEEN_HURT.get();
	}
}
