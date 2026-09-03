package com.vincenthuto.hemomancy.common.entity.npc.circus;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public final class CircusRingmasterEntity extends PathfinderMob {
	public CircusRingmasterEntity(EntityType<? extends CircusRingmasterEntity> type, Level level) {
		super(type, level);
		setPersistenceRequired();
		setNoGravity(true);
		noPhysics = true;
		xpReward = 0;
	}

	public static AttributeSupplier.Builder setAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 80.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.0D).add(Attributes.ARMOR, 12.0D)
				.add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
	}

	@Override
	protected void registerGoals() {
	}

	@Override
	public void tick() {
		super.tick();
		setTarget(null);
		getNavigation().stop();
		setDeltaMovement(0.0D, 0.0D, 0.0D);
		setNoGravity(true);
		noPhysics = true;
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		boolean administrative = source.is(DamageTypes.GENERIC_KILL)
				|| source.getEntity() instanceof Player player && player.isCreative();
		return administrative && super.hurt(source, amount);
	}

	@Override
	protected InteractionResult mobInteract(Player player, InteractionHand hand) {
		return InteractionResult.PASS;
	}

	@Override public void push(double x, double y, double z) { }
	@Override public boolean isPushable() { return false; }
	@Override public boolean isPickable() { return false; }
	@Override public boolean isAttackable() { return false; }
	@Override public boolean removeWhenFarAway(double distanceToClosestPlayer) { return false; }

}
