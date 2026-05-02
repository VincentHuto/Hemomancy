package com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath;

import net.neoforged.fml.common.EventBusSubscriber;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.entity.boss.goal.EmptyPulseGoal;
import com.vincenthuto.hemomancy.common.entity.boss.goal.HematicCollapseGoal;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
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
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerBossEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.bus.api.SubscribeEvent;

/**
 * Hollow Vessel (Saint Hemorath) â€” a body drained of blood but still animate.
 * <p>
 * The Vessel does not distinguish between wound and willful sacrifice: every
 * drop of blood lost by the player â€” whether to damage taken or to hemomantic
 * casting â€” accumulates on the player's blood_debt capability. Periodically,
 * the Vessel discharges that accumulated debt as {@link HematicCollapseGoal}
 * damage, then zeroes it.
 * <p>
 * Phase 1 (100%â†’50%): slow attacks, standard debt accumulation.
 * Phase 2 (50%â†’0%): faster attacks, shorter Hematic Collapse cadence, and
 * bloodvolume spent is amplified by 1.25Ã— via {@link #onPlayerBloodSpend}.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class HollowVesselEntity extends Monster {

	private static final EntityDataAccessor<Boolean> DATA_COLLAPSE_CHARGING =
			SynchedEntityData.defineId(HollowVesselEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Integer> DATA_PHASE =
			SynchedEntityData.defineId(HollowVesselEntity.class, EntityDataSerializers.INT);

	private static final float PHASE_2_HEALTH_FRACTION = 0.5F;
	private static final float SIPHON_HEAL_FRACTION = 0.35F;
	private static final float HEMOPHAGY_HEAL_MULTIPLIER = 0.25F;
	private static final double PHASE_2_BLOOD_SPEND_BONUS = 0.25D;
	private static final double PHASE_2_DETECTION_RADIUS = 48.0D;

	public final AnimationState idleAnimationState = new AnimationState();
	public final AnimationState collapseChargeAnimationState = new AnimationState();

	private final ServerBossEvent bossEvent = new ServerBossEvent(
			Component.translatable("entity.hemomancy.hollow_vessel"),
			BossEvent.BossBarColor.RED,
			BossEvent.BossBarOverlay.NOTCHED_10);

	public HollowVesselEntity(EntityType<? extends HollowVesselEntity> type, Level level) {
		super(type, level);
		this.setPersistenceRequired();
		this.xpReward = 80;
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

	@Override
	public boolean doHurtTarget(Entity target) {
		boolean dealt = super.doHurtTarget(target);
		if (dealt && target instanceof LivingEntity) {
			float steal = (float) (this.getAttributeValue(Attributes.ATTACK_DAMAGE) * SIPHON_HEAL_FRACTION);
			this.heal(steal);
			if (this.level() instanceof ServerLevel server) {
				server.sendParticles(ParticleTypes.DAMAGE_INDICATOR,
						this.getX(), this.getY() + 1.0, this.getZ(),
						4, 0.3, 0.3, 0.3, 0.1);
			}
		}
		return dealt;
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (!this.level().isClientSide) {
			int desiredPhase = this.getHealth() <= this.getMaxHealth() * PHASE_2_HEALTH_FRACTION ? 2 : 1;
			if (desiredPhase != getPhase()) {
				this.entityData.set(DATA_PHASE, desiredPhase);
				if (desiredPhase == 2) {
					onPhaseTransition();
				}
			}
			bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

			// Ambient soul-drain particles every 10 ticks â€” the vessel seeps residual life
			if (this.tickCount % 10 == 0 && this.level() instanceof ServerLevel server) {
				server.sendParticles(ParticleTypes.SOUL,
						this.getX(), this.getY() + 1.0, this.getZ(),
						2, 0.4, 0.5, 0.4, 0.01);
				if (isInPhase2()) {
					server.sendParticles(ParticleTypes.DAMAGE_INDICATOR,
							this.getX(), this.getY() + 0.5, this.getZ(),
							1, 0.3, 0.2, 0.3, 0.02);
				}
			}
		}
	}

	private void onPhaseTransition() {
		if (this.level() instanceof ServerLevel server) {
			server.playSound(null, this.getX(), this.getY(), this.getZ(),
					SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 2.0F, 0.7F);
			server.sendParticles(ParticleTypes.LARGE_SMOKE,
					this.getX(), this.getY() + 1.0, this.getZ(),
					50, 1.0, 1.0, 1.0, 0.1);
			server.sendParticles(ParticleTypes.SOUL,
					this.getX(), this.getY() + 1.0, this.getZ(),
					30, 0.8, 0.8, 0.8, 0.04);
		}
		// Boss bar signals the phase shift â€” turns purple and renames
		bossEvent.setColor(BossEvent.BossBarColor.PURPLE);
		bossEvent.setName(Component.literal("Saint Hemorath - The Vessel Empties")
				.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
		if (this.getTarget() instanceof Player player) {
			player.displayClientMessage(
					Component.literal("The Hollow Vessel empties further...")
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
		bossEvent.addPlayer(player);
		// Announce the encounter to the player who enters sight range
		player.displayClientMessage(
				Component.literal("The Hollow Vessel stirs. Blood will be spent.")
						.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
				false);
	}

	@Override
	public void stopSeenByPlayer(ServerPlayer player) {
		super.stopSeenByPlayer(player);
		bossEvent.removePlayer(player);
	}

	@Override
	public void die(DamageSource source) {
		if (!this.level().isClientSide && source.getEntity() instanceof Player player) {
			ItemStack drop = new ItemStack(ItemInit.hallowed_residuum_hemorath.get());
			if (!player.getInventory().add(drop)) {
				player.drop(drop, false);
			}
			player.displayClientMessage(
					Component.literal("The Vessel quiets. You carry the enzyme of Saint Hemorath.")
							.withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC),
					false);
			HemoCapabilityAccess.getBloodVolume(player)
					.ifPresent(IBloodVolume::resetBloodDebt);
		}
		super.die(source);
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
	 * engaged with a Hollow Vessel in Phase 2, the spend is amplified by 25%.
	 */
	public static void onPlayerBloodSpend(Player player, double amount) {
		if (player.level().isClientSide) return;
		for (HollowVesselEntity vessel : player.level().getEntitiesOfClass(
				HollowVesselEntity.class, player.getBoundingBox().inflate(PHASE_2_DETECTION_RADIUS))) {
			if (vessel.isAlive() && vessel.isInPhase2() && vessel.getTarget() == player) {
				HemoCapabilityAccess.getBloodVolume(player)
						.ifPresent(v -> v.addDamage(amount * PHASE_2_BLOOD_SPEND_BONUS));
				return;
			}
		}
	}

	/**
	 * Empty Pulse's Hemophagy debuff severely reduces incoming heals while active.
	 */
	@SubscribeEvent
	public static void onLivingHeal(LivingHealEvent event) {
		if (event.getEntity().hasEffect(EffectInit.hemophagy)) {
			event.setAmount(event.getAmount() * HEMOPHAGY_HEAL_MULTIPLIER);
		}
	}
}
