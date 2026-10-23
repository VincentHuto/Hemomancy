package com.vincenthuto.hemomancy.common.entity.mob.arthropod;

import com.vincenthuto.hemomancy.common.init.SoundInit;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/** The crystal-grown Fervent Chitinite. */
public class FerventChitiniteEntity extends AbstractChitiniteEntity {

	public FerventChitiniteEntity(EntityType<? extends FerventChitiniteEntity> type, Level worldIn) {
		super(type, worldIn);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundInit.ENTITY_FERVENT_CHITINITE_AMBIENT.get();
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundInit.ENTITY_FERVENT_CHITINITE_DEATH.get();
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return SoundInit.ENTITY_FERVENT_CHITINITE_HURT.get();
	}
}
