package com.vincenthuto.hemomancy.common.entity.projectile;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.damage.SchoolDamage;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.manipulation.congeatio.CruorJavelinThrowRules;
import com.vincenthuto.hemomancy.common.manipulation.verb.strand.StrandCue;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * A thrown Cruor Javelin: a shaft of frozen cruor that flies in an arc. It strikes only what its thrower may harm, as a
 * Congeatio hit that drives Rime in, and breaks on the body. In a block it sticks for a while, where its thrower's
 * Absorption can take part of its blood back, and then shatters. It is drawn in the shared world pass from a cue keyed
 * to it, not by an entity renderer.
 */
public class CruorJavelinEntity extends AbstractArrow {
	private static final EntityDataAccessor<Byte> STAGE = SynchedEntityData.defineId(CruorJavelinEntity.class, EntityDataSerializers.BYTE);
	/** How long the keyed cue lives if nothing retires it: the javelin's whole life. */
	private static final int CUE_TICKS = CruorJavelinThrowRules.STUCK_TICKS + 200;
	private double paid;
	private int stuckTicks;

	public CruorJavelinEntity(EntityType<? extends CruorJavelinEntity> type, Level level) {
		super(type, level);
		this.pickup = Pickup.DISALLOWED;
	}

	/** A javelin of {@code stage} leaving {@code thrower}'s hand at {@code from}, with {@code paid} blood drawn into it. */
	public static CruorJavelinEntity thrown(ServerLevel level, ServerPlayer thrower, Vec3 from, int stage, double paid) {
		CruorJavelinEntity javelin = new CruorJavelinEntity(EntityInit.cruor_javelin.get(), level);
		javelin.setOwner(thrower);
		javelin.setPos(from);
		javelin.entityData.set(STAGE, (byte) stage);
		javelin.paid = paid;
		Vec3 look = thrower.getLookAngle();
		javelin.shoot(look.x, look.y, look.z, (float) CruorJavelinThrowRules.speed(stage), 0.0F);
		level.addFreshEntity(javelin);
		javelin.show(CUE_TICKS);
		return javelin;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(STAGE, (byte) 1);
	}

	public int stage() {
		return entityData.get(STAGE);
	}

	/** Blood drawn into this javelin. */
	public double paid() {
		return paid;
	}

	/** Whether it is stuck in a block, where Absorption can take it. */
	public boolean stuck() {
		return inGround;
	}

	private void show(int ticks) {
		ManipulationVisuals.attached(this, ManipulationVisuals.Form.STRAND_ACCENT, position(), CruorJavelinThrowRules.length(stage()), ticks,
				StrandCue.encodeAccent(StrandCue.Accent.JAVELIN, EnumBloodTemper.CONGEATIO, EnumBloodTemper.FERRIC));
	}

	@Override
	protected boolean canHitEntity(Entity target) {
		return super.canHitEntity(target) && target instanceof LivingEntity living && getOwner() instanceof Player thrower
				&& ManipulationCombatHelper.canHarm(thrower, living);
	}

	@Override
	protected void onHitEntity(EntityHitResult hit) {
		if (!(level() instanceof ServerLevel level) || !(hit.getEntity() instanceof LivingEntity target)
				|| !(getOwner() instanceof Player thrower)) return;
		BloodManipulation javelin = ManipulationInit.getByName("cruor_javelin");
		int stage = stage();
		float damage = (float) (CruorJavelinThrowRules.damage(stage) * SkillPointHelper.getCrimsonMasteryMultiplier(thrower));
		try (var cast = SchoolDamage.cast(javelin, thrower, 1)) {
			if (ManipulationCombatHelper.hurt(javelin, thrower, target, level, damage, CruorJavelinThrowRules.RIME_TICKS,
					CruorJavelinThrowRules.rimeLevels(stage, target.isInWaterOrRain()))) {
				ManipulationVisuals.attached(target, ManipulationVisuals.Form.FROZEN_VEINS, target.getBbWidth(), CruorJavelinThrowRules.RIME_TICKS, 1);
			}
		}
		shatter(level, hit.getLocation());
	}

	@Override
	protected void tickDespawn() {
		if (++stuckTicks >= CruorJavelinThrowRules.STUCK_TICKS && level() instanceof ServerLevel level) shatter(level, position());
	}

	/** The javelin breaks: frost bursts where it was and it is gone. */
	public void shatter(ServerLevel level, Vec3 at) {
		Vec3 along = getDeltaMovement().lengthSqr() < 1.0E-6D ? new Vec3(0.0D, 1.0D, 0.0D) : getDeltaMovement().normalize();
		ManipulationVisuals.burst(level, ManipulationVisuals.Form.CRUOR_BREAK, at, at.add(along.scale(0.5D)), 0.8D, 18);
		level.playSound(null, blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.8F, 1.4F);
		discard();
	}

	@Override
	protected SoundEvent getDefaultHitGroundSoundEvent() {
		return SoundEvents.GLASS_HIT;
	}

	@Override
	protected ItemStack getDefaultPickupItem() {
		return new ItemStack(Items.ARROW);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putByte("Stage", (byte) stage());
		tag.putDouble("Paid", paid);
		tag.putInt("StuckTicks", stuckTicks);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		entityData.set(STAGE, tag.getByte("Stage"));
		paid = tag.getDouble("Paid");
		stuckTicks = tag.getInt("StuckTicks");
	}
}
