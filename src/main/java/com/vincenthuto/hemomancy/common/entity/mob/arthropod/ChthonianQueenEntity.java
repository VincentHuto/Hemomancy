package com.vincenthuto.hemomancy.common.entity.mob.arthropod;

import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.Mob;
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

	/** The mound's one Queen: a boss tier above her workers, and she hunts any player who enters. */
	public static AttributeSupplier.Builder setAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 60.0D).add(Attributes.MOVEMENT_SPEED, 0.26D)
				.add(Attributes.ATTACK_DAMAGE, 7.0D).add(Attributes.ARMOR, 6.0D).add(Attributes.FOLLOW_RANGE, 24.0D)
				.add(Attributes.KNOCKBACK_RESISTANCE, 0.6D);
	}

	@Override
	protected boolean huntsAnyPlayer() {
		return true;
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
