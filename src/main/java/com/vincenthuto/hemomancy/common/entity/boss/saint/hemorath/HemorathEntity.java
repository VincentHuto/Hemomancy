package com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath;

import java.util.UUID;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodAccounting;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import net.minecraft.nbt.CompoundTag;
import com.vincenthuto.hemomancy.common.entity.boss.presentation.BossScenes;
import com.vincenthuto.hemomancy.common.entity.boss.presentation.BossPresentationIds;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.entity.boss.goal.EmptyPulseGoal;
import com.vincenthuto.hemomancy.common.entity.boss.goal.HematicCollapseGoal;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import com.vincenthuto.hemomancy.common.entity.boss.BossBarBinding;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import com.vincenthuto.hemomancy.common.entity.boss.saint.EnumSaintType;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintTrialEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Saint Hemorath, the First Saint: a body drained of blood but still animate.
 * <p>
 * Hemorath does not distinguish between wound and willful sacrifice: every
 * drop of blood lost by the player whether to damage taken or to hemomantic
 * casting accumulates on the player's blood_debt capability. Periodically,
 * Hemorath discharges that accumulated debt as {@link HematicCollapseGoal}
 * damage, then zeroes it.
 * <p>
 * Phase 1 (100%50%): slow attacks, standard debt accumulation.
 * Phase 2 (50%0%): faster attacks, shorter Hematic Collapse cadence, and
 * bloodvolume spent is amplified by 1.25Ã— via {@link #onPlayerBloodSpend}.
 * <p>
 * He cannot be killed. Overloaded or felled, he is exsanguinated again and returns to his tomb
 * ({@link SaintTrialEntity#endTrial}); the marrow never stops making blood, so he reforms in time.
 */
public class HemorathEntity extends SaintTrialEntity {

	private static final EntityDataAccessor<Boolean> DATA_COLLAPSE_CHARGING =
			SynchedEntityData.defineId(HemorathEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Integer> DATA_PHASE =
			SynchedEntityData.defineId(HemorathEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Float> DATA_ABSORBED_BLOOD =
			SynchedEntityData.defineId(HemorathEntity.class, EntityDataSerializers.FLOAT);

	public static final int VISUAL_COLLAPSE_WINDUP = 1;
	public static final int VISUAL_COLLAPSE_IMPACT = 2;
	public static final int VISUAL_EMPTY_PULSE = 3;
	public static final int VISUAL_EMPTY_PULSE_IMPACT = 4;
	public static final int VISUAL_OVERLOAD = 5;

	private static final float PHASE_2_HEALTH_FRACTION = 0.5F;
	private static final float SIPHON_HEAL_FRACTION = 0.35F;
	private static final double PHASE_2_BLOOD_SPEND_BONUS = 0.25D;
	private static final double PHASE_2_DETECTION_RADIUS = 48.0D;
	public static final float OVERLOAD_THRESHOLD = 3000.0F;
	private static final float OVERLOAD_PHASE_2_MULTIPLIER = 1.35F;

	private static final EntityDataAccessor<Integer> DATA_VISUAL_TARGET =
			SynchedEntityData.defineId(HemorathEntity.class, EntityDataSerializers.INT);

	private boolean overloaded;
	private static final BloodAccounting.Source MARROW_SIPHON = BloodAccounting.Source.of("hemorath_marrow_siphon", Category.HAZARD, false, false);
	private static final EntityDataAccessor<Integer> DATA_SIPHON_UNTIL = SynchedEntityData.defineId(HemorathEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_SIPHON_RELEASE = SynchedEntityData.defineId(HemorathEntity.class, EntityDataSerializers.INT);
	private int siphonAge;
	private int overloadTicks;
	/** Who burst him: they carry the reward when the overload ends. */
	private Player overloadVictor;
	/** The victor's id, kept so a reload mid-burst can still find them. */
	private UUID overloadVictorId;

	public final AnimationState idleAnimationState = new AnimationState();
	public final AnimationState collapseChargeAnimationState = new AnimationState();

	public HemorathEntity(EntityType<? extends HemorathEntity> type, Level level) {
		super(type, level, 80, new BossBarBinding("entity.hemomancy.hollow_vessel", BossPresentationIds.FRAME_HEMORATH, BossEvent.BossBarColor.RED,
				BossEvent.BossBarOverlay.NOTCHED_10));
	}

	@Override
	public EnumSaintType saintType() {
		return EnumSaintType.HEMORATH;
	}

	public static AttributeSupplier.Builder setAttributes() {
		return Monster.createMonsterAttributes()
				.add(Attributes.MAX_HEALTH, 220.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.28D)
				.add(Attributes.ATTACK_DAMAGE, 8.0D)
				.add(Attributes.ATTACK_KNOCKBACK, 1.0D)
				.add(Attributes.FOLLOW_RANGE, 48.0D)
				.add(Attributes.KNOCKBACK_RESISTANCE, 0.8D)
				.add(Attributes.ARMOR, 6.0D);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new HematicCollapseGoal(this));
		this.goalSelector.addGoal(2, new EmptyPulseGoal(this));
		this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0D, true));
		this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.8D));
		this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 16.0F));
		this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_COLLAPSE_CHARGING, false);
		builder.define(DATA_PHASE, 1);
		builder.define(DATA_ABSORBED_BLOOD, 0.0F);
		builder.define(DATA_VISUAL_TARGET, -1);
		builder.define(DATA_SIPHON_UNTIL, HemorathSiphonRules.CYCLE);
		builder.define(DATA_SIPHON_RELEASE, 0);
	}

	public int getSiphonUntil() { return entityData.get(DATA_SIPHON_UNTIL); }
	public int getSiphonRelease() { return entityData.get(DATA_SIPHON_RELEASE); }

	private void tickMarrowSiphon(ServerLevel server) {
		entityData.set(DATA_ABSORBED_BLOOD, HemorathSiphonRules.leak(getAbsorbedBlood()));
		int until = HemorathSiphonRules.CYCLE - ++siphonAge % HemorathSiphonRules.CYCLE;
		entityData.set(DATA_SIPHON_UNTIL, until);
		entityData.set(DATA_SIPHON_RELEASE, Math.max(0, getSiphonRelease() - 1));
		boolean pulse = until == HemorathSiphonRules.CYCLE;
		if (!pulse && siphonAge % HemorathSiphonRules.AURA_INTERVAL != 0) return;
		double radius = pulse ? HemorathSiphonRules.SIPHON_RADIUS : HemorathSiphonRules.AURA_RADIUS;
		if (pulse) {
			entityData.set(DATA_SIPHON_RELEASE, HemorathSiphonRules.RELEASE);
			HemorathEffects.burst(server, position(), HemorathVisualRules.SIPHON, (float) radius);
		}
		for (Player player : server.getEntitiesOfClass(Player.class, getBoundingBox().inflate(radius),
				p -> ManipulationCombatHelper.canHarmFor(this, p) && distanceToSqr(p) <= radius * radius && hasLineOfSight(p))) {
			var volume = HemoCapabilityAccess.getBloodVolume(player);
			if (volume.isActive()) {
				var payment = BloodAccounting.payUpkeep(player, pulse ? HemorathSiphonRules.SIPHON_BLOOD : HemorathSiphonRules.AURA_BLOOD,
						MARROW_SIPHON, pulse ? HemorathSiphonRules.CYCLE : HemorathSiphonRules.AURA_INTERVAL, false);
				if (payment.paid() > 0) addAbsorbedBlood(player, payment.paid());
				else player.hurt(damageSources().magic(), pulse ? 2 : 1);
			} else {
				player.hurt(damageSources().magic(), pulse ? 2 : 1);
				HemorathEffects.intake(server, player.position().add(0, 1, 0), position().add(0, 2.6, 0));
			}
			if (overloaded) break;
		}
	}

	public boolean isCollapseCharging() {
		return this.entityData.get(DATA_COLLAPSE_CHARGING);
	}

	public void setCollapseCharging(boolean charging) {
		this.entityData.set(DATA_COLLAPSE_CHARGING, charging);
	}

	public int getPhase() {
		return this.entityData.get(DATA_PHASE);
	}

	public boolean isInPhase2() {
		return getPhase() >= 2;
	}

	public float getAbsorbedBlood() {
		return this.entityData.get(DATA_ABSORBED_BLOOD);
	}

	public int getVisualTargetId() { return this.entityData.get(DATA_VISUAL_TARGET); }

	public void setVisualTarget(LivingEntity target) {
		if (!this.level().isClientSide) this.entityData.set(DATA_VISUAL_TARGET, target == null ? -1 : target.getId());
	}

	public void addAbsorbedBlood(Player source, double amount) {
		if (this.level().isClientSide || overloaded || amount <= 0) {
			return;
		}
		float gained = (float) (amount * (isInPhase2() ? OVERLOAD_PHASE_2_MULTIPLIER : 1.0F));
		float absorbed = Math.min(OVERLOAD_THRESHOLD, getAbsorbedBlood() + gained);
		this.entityData.set(DATA_ABSORBED_BLOOD, absorbed);
		if (source != null && this.level() instanceof ServerLevel server) {
			HemorathEffects.intake(server, source.position().add(0, 1, 0), this.position().add(0, 2.6, 0));
		}
		if (absorbed >= OVERLOAD_THRESHOLD) {
			triggerOverload(source);
		} else if (source instanceof ServerPlayer serverPlayer && absorbed >= OVERLOAD_THRESHOLD * 0.75F) {
			serverPlayer.displayClientMessage(
					Component.translatable("message.hemomancy.hemorath.hemorath_s_hollow_frame_strains_with")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					true);
		}
	}

	@Override
	public boolean doHurtTarget(Entity target) {
		if (overloaded) {
			return false;
		}
		boolean dealt = super.doHurtTarget(target);
		if (dealt && target instanceof LivingEntity) {
			float steal = (float) (this.getAttributeValue(Attributes.ATTACK_DAMAGE) * SIPHON_HEAL_FRACTION);
			this.heal(steal);
			if (this.level() instanceof ServerLevel server) {
				HemorathEffects.intake(server, target.position().add(0, target.getBbHeight() * .6, 0), this.position().add(0, 2.6, 0));
				HemorathEffects.burst(server, target.position().add(0, target.getBbHeight() * .6, 0), HemorathVisualRules.MELEE, .7F);
			}
		}
		return dealt;
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (overloaded) {
			return false;
		}
		return super.hurt(source, amount);
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (!this.level().isClientSide) {
			if (tickRise()) return;
			if (!isAlive()) return;
			tickVisualState();
			if (overloaded) {
				tickSecondaryAttacks(false);
				tickOverloadDeath();
				return;
			}
			tickMarrowSiphon((ServerLevel) level());
			if (overloaded) return;
			tickSecondaryAttacks(true);
			int desiredPhase = this.getHealth() <= this.getMaxHealth() * PHASE_2_HEALTH_FRACTION ? 2 : 1;
			if (desiredPhase != getPhase()) {
				this.entityData.set(DATA_PHASE, desiredPhase);
				if (desiredPhase == 2) {
					onPhaseTransition();
				}
			}
			bossBar.trackHealth(this);


		}
	}

	@Override
	protected boolean holdsVisualState(int visualState) {
		return visualState == VISUAL_OVERLOAD || visualState == VISUAL_COLLAPSE_WINDUP
				|| visualState == VISUAL_EMPTY_PULSE;
	}

	// the overload is saved so a reload mid-burst still ends the trial instead of leaving a still body
	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		if (!overloaded) return;
		tag.putInt("OverloadTicks", overloadTicks);
		if (overloadVictorId != null) tag.putUUID("OverloadVictor", overloadVictorId);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		if (!tag.contains("OverloadTicks")) return;
		overloaded = true;
		overloadTicks = tag.getInt("OverloadTicks");
		overloadVictorId = tag.hasUUID("OverloadVictor") ? tag.getUUID("OverloadVictor") : null;
		this.setVisualState(VISUAL_OVERLOAD, overloadTicks);
	}

	private void tickOverloadDeath() {

		if (--overloadTicks <= 0) {
			if (this.level() instanceof ServerLevel server)
				HemorathEffects.burst(server, this.position().add(0, .6, 0), HemorathVisualRules.EXSANGUINATE, 3.5F);
			if (overloadVictor == null && overloadVictorId != null) overloadVictor = this.level().getPlayerByUUID(overloadVictorId);
			endTrial(overloadVictor, null);
			this.discard();
		}
	}

	private void onPhaseTransition() {
		BossScenes.play(this, BossPresentationIds.SCENE_HEMORATH_EMPTIES_CARD);
		if (this.level() instanceof ServerLevel server) {
			server.playSound(null, this.getX(), this.getY(), this.getZ(),
					SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 2.0F, 0.7F);
			HemorathEffects.burst(server, this.position().add(0, 2.6, 0), HemorathVisualRules.PHASE_BREAK, 2.5F);
		}
		// Boss bar signals the phase shift turns purple and renames
		bossBar.setColor(BossEvent.BossBarColor.PURPLE);
		bossBar.setName(Component.translatable("message.hemomancy.hemorath.saint_hemorath_the_vessel_empties")
				.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
		if (this.getTarget() instanceof Player player) {
			player.displayClientMessage(
					Component.translatable("message.hemomancy.hemorath.the_hollow_vessel_empties_further")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
					false);
		}
		AttributeInstance speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
		if (speed != null && speed.getBaseValue() < 0.32D) {
			speed.setBaseValue(0.32D);
		}
	}

	@Override
	public void tick() {
		super.tick();
		if (this.level().isClientSide) {
			this.idleAnimationState.startIfStopped(this.tickCount);
			if (isCollapseCharging()) {
				this.collapseChargeAnimationState.startIfStopped(this.tickCount);
			} else {
				this.collapseChargeAnimationState.stop();
			}
		}
	}

	@Override
	public void startSeenByPlayer(ServerPlayer player) {
		super.startSeenByPlayer(player);
		// Announce the encounter to the player who enters sight range
		player.displayClientMessage(
				Component.translatable("message.hemomancy.hemorath.the_hollow_vessel_stirs_blood_will")
						.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
				false);
	}

	@Override
	public void die(DamageSource source) {
		if (this.level() instanceof ServerLevel server)
			HemorathEffects.burst(server, this.position().add(0, 2, 0), HemorathVisualRules.EXSANGUINATE, 2.5F);
		super.die(source);
	}

	@Override
	protected void tickDeath() {
		if (++this.deathTime >= 20 && !this.level().isClientSide && !this.isRemoved())
			this.remove(RemovalReason.KILLED);
	}

	@Override
	public boolean removeWhenFarAway(double distanceToClosestPlayer) {
		return false;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.WITHER_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.WITHER_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.WITHER_DEATH;
	}

	/**
	 * Hook called from {@code BloodManipulation.performAction} after a cast's
	 * blood cost has been added to the player's debt. If the player is currently
	 * engaged with Hemorath in Phase 2, the spend is amplified by 25%.
	 */
	public static void onPlayerBloodSpend(Player player, double amount) {
		if (player.level().isClientSide) return;
		for (HemorathEntity vessel : player.level().getEntitiesOfClass(
				HemorathEntity.class, player.getBoundingBox().inflate(PHASE_2_DETECTION_RADIUS))) {
			if (vessel.isAlive() && vessel.getTarget() == player) {
				vessel.addAbsorbedBlood(player, amount);
				if (vessel.isInPhase2()) {
					HemoCapabilityAccess.getBloodVolume(player).addDamage(amount * PHASE_2_BLOOD_SPEND_BONUS);
				}
				return;
			}
		}
	}

	private void triggerOverload(Player source) {
		tickSecondaryAttacks(false);
		overloaded = true;
		overloadTicks = HemorathVisualRules.OVERLOAD_TICKS;
		this.setNoAi(true);
		this.goalSelector.getAvailableGoals().stream().filter(goal -> goal.isRunning()).forEach(goal -> goal.stop());
		this.targetSelector.getAvailableGoals().stream().filter(goal -> goal.isRunning()).forEach(goal -> goal.stop());
		this.setCollapseCharging(false);
		this.setVisualState(VISUAL_OVERLOAD, overloadTicks);
		this.setTarget(null);
		setVisualTarget(null);
		this.getNavigation().stop();
		if (this.level() instanceof ServerLevel server) {
			server.playSound(null, this.getX(), this.getY(), this.getZ(),
					SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 2.0F, 0.6F);
			HemorathEffects.burst(server, this.position().add(0, 2.6, 0), HemorathVisualRules.PHASE_BREAK, 1.7F);
		}
		if (source != null) {
			source.displayClientMessage(
					Component.translatable("message.hemomancy.hemorath.hemorath_bursts_exsanguinated_by_the_blood")
							.withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
					false);
		}
		overloadVictor = source;
		overloadVictorId = source == null ? null : source.getUUID();
	}

	/** His residuum goes straight into the victor's hands. */
	@Override
	protected boolean rewardsGoToVictor() {
		return true;
	}

	@Override
	protected void announceTrialEnd(Player victor) {
		if (victor == null) return;
		victor.displayClientMessage(
				Component.translatable("message.hemomancy.hemorath.the_vessel_quiets_you_carry_the")
						.withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC),
				false);
		HemoCapabilityAccess.getBloodVolume(victor).resetBloodDebt();
	}

}
