package com.vincenthuto.hemomancy.common.entity.mob.arthropod;

import com.vincenthuto.hemomancy.common.init.SoundInit;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/** A Chthonian worker. */
public class ChthonianEntity extends AbstractChthonianEntity {

	public ChthonianEntity(EntityType<? extends ChthonianEntity> type, Level worldIn) {
		super(type, worldIn);
	}

	@Override
	protected int woodenToolWear() {
		return 5;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundInit.ENTITY_CHTHONIAN_AMBIENT.get();
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundInit.ENTITY_CHTHONIAN_DEATH.get();
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return SoundInit.ENTITY_CHTHONIAN_HURT.get();
	}
}
