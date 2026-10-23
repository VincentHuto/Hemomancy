package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodAccounting;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;
import com.vincenthuto.hemomancy.common.damage.HemoDamageTypes;
import com.vincenthuto.hemomancy.common.damage.SchoolDamage;
import com.vincenthuto.hemomancy.common.damage.SchoolDamageSource;
import com.vincenthuto.hemomancy.common.damage.SchoolHitContext;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.entity.mob.animal.BroodMother;
import com.vincenthuto.hemomancy.common.entity.mob.animal.FunglingEntity;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.worldgen.MycophantEncounterManager;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import com.vincenthuto.hemomancy.common.entity.boss.BossBarBinding;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * The Mycophant, the Spawn Mother: the hivemind's brood cultivator. She does not hunt the Apotheos candidate, she
 * retrieves it, and at half health she stops retrieving a child and tries to join with an equal. The player wins by
 * surviving every claim; at zero health she yields and names them heir.
 *
 * <p>Locomotion, facing and the move timeline belong to {@link MycophantController}; every number lives in the
 * Mycophant*Rules classes. State is synced for the generated model's animation.
 */
public class MycophantEntity extends Monster implements BroodMother {
	private static final BloodAccounting.Source CLAIM = BloodAccounting.Source.of("mycophant_claim", Category.HAZARD, false, false);
	private static final BloodAccounting.Source SIPHON = BloodAccounting.Source.of("mycophant_siphon", Category.HAZARD, false, false);
	private static final EntityDataAccessor<Integer> DATA_STATE = SynchedEntityData.defineId(MycophantEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_STATE_TICK = SynchedEntityData.defineId(MycophantEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_ATTACK = SynchedEntityData.defineId(MycophantEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_ATTACK_TICK = SynchedEntityData.defineId(MycophantEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_PHASE = SynchedEntityData.defineId(MycophantEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_SPLIT_TICK = SynchedEntityData.defineId(MycophantEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_INTRO_TICK = SynchedEntityData.defineId(MycophantEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> DATA_REMATCH_INTRO = SynchedEntityData.defineId(MycophantEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Integer> DATA_CLAIMED_ID = SynchedEntityData.defineId(MycophantEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_SIPHON_TARGET_ID = SynchedEntityData.defineId(MycophantEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Float> DATA_GLOW = SynchedEntityData.defineId(MycophantEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> DATA_CLAIM_METER = SynchedEntityData.defineId(MycophantEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Integer> DATA_YIELD_TICK = SynchedEntityData.defineId(MycophantEntity.class, EntityDataSerializers.INT);

	/** Review-client hook: a forced pose name, null in play. */
	public static volatile String reviewHoldPose;

	private final MycophantController controller = new MycophantController(this);
	private final BossBarBinding bossBar = new BossBarBinding("entity.hemomancy.mycophant", BossEvent.BossBarColor.YELLOW, BossEvent.BossBarOverlay.NOTCHED_12);

	@Nullable private UUID encounterOwner;
	private float nectarPressure;
	private boolean introDone;
	private boolean splitDone;
	private boolean bossBarShown;

	// the current hold (claim in phase 1, fusion in phase 2)
	private int holdHits;
	private boolean holdCastUsed;
	private int lastOwnerCastTick = -1000;
	// the siphon latch
	private int siphonDamageTaken;
	private int meterReleaseTick = -1;
	// a broken fusion's bonus wound, landed on the next tick so it never shares a hurt cooldown with the breaking blow
	private float pendingRefusal;
	@Nullable private UUID pendingRefusalFrom;

	public MycophantEntity(EntityType<? extends MycophantEntity> type, Level level) {
		super(type, level);
		this.setPersistenceRequired();
		this.xpReward = 180;
	}

	public static AttributeSupplier.Builder setAttributes() {
		return Monster.createMonsterAttributes()
				.add(Attributes.MAX_HEALTH, 720.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.18D)
				.add(Attributes.ATTACK_DAMAGE, 16.0D)
				.add(Attributes.FOLLOW_RANGE, 64.0D)
				.add(Attributes.KNOCKBACK_RESISTANCE, 1.15D)
				.add(Attributes.ARMOR, 20.0D)
				.add(Attributes.STEP_HEIGHT, 1.25D);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_STATE, MycophantState.IDLE.ordinal());
		builder.define(DATA_STATE_TICK, 0);
		builder.define(DATA_ATTACK, MycophantAttack.NONE.ordinal());
		builder.define(DATA_ATTACK_TICK, 0);
		builder.define(DATA_PHASE, 1);
		builder.define(DATA_SPLIT_TICK, 0);
		builder.define(DATA_INTRO_TICK, 0);
		builder.define(DATA_REMATCH_INTRO, false);
		builder.define(DATA_CLAIMED_ID, -1);
		builder.define(DATA_SIPHON_TARGET_ID, -1);
		builder.define(DATA_GLOW, 0.6F);
		builder.define(DATA_CLAIM_METER, 0.0F);
		builder.define(DATA_YIELD_TICK, 0);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
	}

	// ------------------------------------------------------------------ synced state (read by the model)

	public MycophantState getState() { return MycophantState.byOrdinal(this.entityData.get(DATA_STATE)); }
	public int getStateTick() { return this.entityData.get(DATA_STATE_TICK); }
	public MycophantAttack getAttack() { return MycophantAttack.byOrdinal(this.entityData.get(DATA_ATTACK)); }
	public int getAttackTick() { return this.entityData.get(DATA_ATTACK_TICK); }
	public int getPhase() { return this.entityData.get(DATA_PHASE); }
	public int getSplitTick() { return this.entityData.get(DATA_SPLIT_TICK); }
	public int getIntroTick() { return this.entityData.get(DATA_INTRO_TICK); }
	public boolean isRematchIntro() { return this.entityData.get(DATA_REMATCH_INTRO); }
	public int getClaimedId() { return this.entityData.get(DATA_CLAIMED_ID); }
	public int getSiphonTargetId() { return this.entityData.get(DATA_SIPHON_TARGET_ID); }
	public float getGlowLevel() { return this.entityData.get(DATA_GLOW); }
	public float getClaimMeter() { return this.entityData.get(DATA_CLAIM_METER); }
	public int getYieldTick() { return this.entityData.get(DATA_YIELD_TICK); }
	public boolean isSplitComplete() { return getPhase() >= 2; }
	public boolean isHolding() { return getClaimedId() >= 0; }

	void setState(MycophantState state) {
		this.entityData.set(DATA_STATE, state.ordinal());
		this.entityData.set(DATA_STATE_TICK, 0);
	}

	void tickStateClock() { this.entityData.set(DATA_STATE_TICK, getStateTick() + 1); }
	void setAttack(MycophantAttack attack) { this.entityData.set(DATA_ATTACK, attack.ordinal()); this.entityData.set(DATA_ATTACK_TICK, 0); }
	void setAttackTick(int tick) { this.entityData.set(DATA_ATTACK_TICK, tick); }
	void setPhase(int phase) { this.entityData.set(DATA_PHASE, phase); }
	void setSplitTick(int tick) { this.entityData.set(DATA_SPLIT_TICK, tick); }
	void setIntroTick(int tick) { this.entityData.set(DATA_INTRO_TICK, tick); }
	void setGlow(float glow) { this.entityData.set(DATA_GLOW, glow); }
	void setSiphonTargetId(int id) { this.entityData.set(DATA_SIPHON_TARGET_ID, id); }

	public void setRematchIntro(boolean rematch) { this.entityData.set(DATA_REMATCH_INTRO, rematch); }

	// ------------------------------------------------------------------ ownership and pressure

	@Nullable
	public UUID getEncounterOwner() { return this.encounterOwner; }

	public void setEncounterOwner(@Nullable UUID owner) {
		this.encounterOwner = owner;
		if (owner != null) this.xpReward = 0;
	}

	@Nullable
	ServerPlayer owner() {
		if (this.encounterOwner == null || !(this.level() instanceof ServerLevel level)) return null;
		ServerPlayer player = level.getServer().getPlayerList().getPlayer(this.encounterOwner);
		return player != null && player.level() == this.level() && player.isAlive() ? player : null;
	}

	/** The fight's quarry: the owner in the Nursery, else the vanilla target (a direct-summoned Mycophant). */
	@Nullable
	LivingEntity quarry() {
		if (this.encounterOwner != null) return owner();
		LivingEntity target = getTarget();
		return target != null && target.isAlive() ? target : null;
	}

	public float getNectarPressure() { return this.nectarPressure; }

	void addPressure(float amount) {
		if (this.encounterOwner != null) this.nectarPressure = Mth.clamp(this.nectarPressure + amount, 0.0F, 100.0F);
	}

	void setPressure(float pressure) {
		if (this.encounterOwner != null) this.nectarPressure = Mth.clamp(pressure, 0.0F, 100.0F);
	}

	boolean isIntroDone() { return this.introDone; }
	void finishIntro() { this.introDone = true; }
	boolean isSplitDone() { return this.splitDone; }
	void finishSplit() { this.splitDone = true; }

	// ------------------------------------------------------------------ geometry helpers

	/** Unit vector along her body yaw. */
	Vec3 forward() {
		float yaw = this.yBodyRot * Mth.DEG_TO_RAD;
		return new Vec3(-Mth.sin(yaw), 0.0D, Mth.cos(yaw));
	}

	/** Where a held player is kept: pressed against the front of the great sac. */
	Vec3 holdPoint() {
		return position().add(forward().scale(2.7D)).add(0.0D, 0.55D, 0.0D);
	}

	/** World position of a body point given in blocks: right, up, forward relative to her body yaw. */
	Vec3 bodyPoint(double right, double up, double forward) {
		Vec3 f = forward();
		Vec3 r = new Vec3(-f.z, 0.0D, f.x);
		return position().add(r.scale(right)).add(0.0D, up, 0.0D).add(f.scale(forward));
	}

	/** Degrees between where she faces and where {@code point} lies, 0..180. */
	double angleTo(Vec3 point) {
		Vec3 to = point.subtract(position()).multiply(1.0D, 0.0D, 1.0D);
		if (to.lengthSqr() < 1.0E-4D) return 0.0D;
		double dot = to.normalize().dot(forward());
		return Math.toDegrees(Math.acos(Mth.clamp(dot, -1.0D, 1.0D)));
	}

	// ------------------------------------------------------------------ ticking

	@Override
	public void aiStep() {
		super.aiStep();
		if (this.level().isClientSide) return;
		this.bossBar.trackHealth(this);
		if (!this.isAlive()) return;
		if (reviewHoldPose != null) return;
		applyPendingRefusal();
		controller.tick();
		tickHold();
		tickSiphon();
		tickPressure();
		refreshBossBar();
	}

	private void tickPressure() {
		ServerPlayer owner = owner();
		if (owner == null || getState() == MycophantState.INTRO || getState() == MycophantState.SPLIT) return;
		if (this.tickCount % 20 == 0) this.nectarPressure = MycophantCombatRules.pressureAfterSecond(this.nectarPressure, getPhase());
		switch (MycophantCombatRules.nectarHazard(this.nectarPressure)) {
			case SLOW -> owner.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 25, 0, false, false));
			case DEEP -> owner.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 25, 1, false, false));
			case ENGULFING -> {
				owner.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 25, 2, false, false));
				if (this.tickCount % 20 == 0) {
					float drown = 2.0F;
					if (MycophantClaimRules.drownClampActive(this.tickCount, this.meterReleaseTick))
						drown = MycophantClaimRules.clampNonlethal(owner.getHealth(), drown);
					if (drown > 0.0F) owner.hurt(this.damageSources().drown(), drown);
				}
			}
			default -> { }
		}
	}

	private void refreshBossBar() {
		boolean show = getState() != MycophantState.INTRO
				|| !MycophantIntroRules.invulnerable(getIntroTick(), isRematchIntro());
		if (show && !this.bossBarShown) {
			this.bossBarShown = true;
			for (ServerPlayer player : ((ServerLevel) level()).getPlayers(p -> p.distanceToSqr(this) < 96 * 96))
				this.bossBar.show(player);
		}
		Component name = Component.translatable(getPhase() >= 2 ? "entity.hemomancy.mycophant.joining" : "entity.hemomancy.mycophant");
		if (this.encounterOwner != null)
			name = name.copy().append(Component.literal("  " + Math.round(this.nectarPressure) + "%")
					.withStyle(ChatFormatting.GOLD));
		this.bossBar.setName(name);
	}

	/** True while the intro, split or yield are playing out: she cannot be harmed through them. */
	public boolean isUntouchable() {
		MycophantState state = getState();
		if (state == MycophantState.SPLIT || state == MycophantState.YIELD) return true;
		return state == MycophantState.INTRO && MycophantIntroRules.invulnerable(getIntroTick(), isRematchIntro());
	}

	// ------------------------------------------------------------------ holds: the claim and the fusion

	/** Takes {@code player} into her arms (phase 1 claim, phase 2 fusion). */
	void beginHold(ServerPlayer player) {
		this.entityData.set(DATA_CLAIMED_ID, player.getId());
		this.entityData.set(DATA_CLAIM_METER, 0.0F);
		this.holdHits = 0;
		this.holdCastUsed = false;
		player.stopUsingItem();
		this.level().playSound(null, this.blockPosition(), SoundInit.ENTITY_MYCOPHANT_HURT_OTHER.get(), SoundSource.HOSTILE, 1.4F, 0.55F);
		MycophantMoves.holdBegins(this, player);
	}

	@Nullable
	ServerPlayer heldPlayer() {
		int id = getClaimedId();
		if (id < 0) return null;
		Entity entity = this.level().getEntity(id);
		return entity instanceof ServerPlayer player && player.isAlive() ? player : null;
	}

	private void tickHold() {
		if (!isHolding()) return;
		ServerPlayer held = heldPlayer();
		if (held == null || getState() != MycophantState.ATTACK || !getAttack().isEmbrace()) {
			releaseHold();
			return;
		}
		Vec3 point = holdPoint();
		if (held.position().distanceToSqr(point) > 0.09D) held.teleportTo(point.x, point.y, point.z);
		held.setDeltaMovement(Vec3.ZERO);
		held.hurtMarked = true;
		// suspended at the hold point, the small drops between re-placements would otherwise add up to a lethal fall
		held.resetFallDistance();
		float meter = getClaimMeter() + MycophantClaimRules.meterPerTick(getPhase());
		this.entityData.set(DATA_CLAIM_METER, Math.min(1.0F, meter));
		setGlow(0.8F + meter * 0.7F);
		MycophantMoves.holdTick(this, held, meter);
		if (meter >= 1.0F) failHold(held);
	}

	/** The meter filled: it hurts, it takes blood and nectar rises, but it never kills. */
	private void failHold(ServerPlayer held) {
		int phase = getPhase();
		float damage = MycophantClaimRules.clampNonlethal(held.getHealth(), MycophantClaimRules.filledMeterDamage(phase));
		releaseHold();
		this.meterReleaseTick = this.tickCount;
		this.nectarPressure = MycophantCombatRules.pressureAfterFailedCocoon(this.nectarPressure);
		controller.skipToRecovery();
		if (damage > 0.0F) held.hurt(HemoDamageTypes.mycophantClaim(this.level(), this), damage);
		if (!held.isAlive() || held.level() != this.level()) return;
		var volume = HemoCapabilityAccess.getBloodVolume(held);
		if (volume.isActive()) {
			BloodAccounting.pay(held, volume.getMaxBloodVolume() * MycophantClaimRules.bloodDrainFraction(phase), CLAIM);
		}
		pushAway(held, 0.9D, 0.35D);
	}

	/** The player tore free: she reels, and a broken fusion costs her dearly. */
	private void breakHold(ServerPlayer held, boolean empowered) {
		boolean fusion = getAttack() == MycophantAttack.FUSION_EMBRACE;
		releaseHold();
		pushAway(held, 1.3D, 0.45D);
		this.nectarPressure = MycophantCombatRules.pressureAfterEscape(this.nectarPressure, empowered);
		this.level().playSound(null, this.blockPosition(), SoundInit.ENTITY_MYCOPHANT_HURT.get(), SoundSource.HOSTILE, 1.3F, 0.7F);
		MycophantMoves.holdBroken(this, held, empowered);
		controller.forceStagger(MycophantControllerRules.breakStaggerTicks(), null);
		if (fusion) {
			this.pendingRefusal = MycophantClaimRules.fusionBreakBonus(getMaxHealth());
			this.pendingRefusalFrom = held.getUUID();
		}
	}

	/**
	 * Lands a broken fusion's bonus wound. Its damage type bypasses the hurt cooldown, and the cooldown state is put
	 * back afterwards, so the ordinary blows of the stagger window still land at full rate.
	 */
	private void applyPendingRefusal() {
		if (this.pendingRefusal <= 0.0F || !(this.level() instanceof ServerLevel level)) return;
		float amount = this.pendingRefusal;
		ServerPlayer from = this.pendingRefusalFrom == null ? null : level.getServer().getPlayerList().getPlayer(this.pendingRefusalFrom);
		this.pendingRefusal = 0.0F;
		this.pendingRefusalFrom = null;
		int invulnerable = this.invulnerableTime;
		float last = this.lastHurt;
		this.hurt(HemoDamageTypes.mycophantRefusal(level, from != null ? from : this), amount);
		this.invulnerableTime = invulnerable;
		this.lastHurt = last;
	}

	/** Lets go without consequence (logout, phase change, the yield). */
	public void releaseHold() {
		// she carried them; whatever drops the hold left on their fall distance was never a fall
		ServerPlayer held = heldPlayer();
		if (held != null) held.resetFallDistance();
		this.entityData.set(DATA_CLAIMED_ID, -1);
		this.entityData.set(DATA_CLAIM_METER, 0.0F);
		setGlow(0.6F);
	}

	private void pushAway(ServerPlayer player, double horizontal, double up) {
		Vec3 away = player.position().subtract(position()).multiply(1.0D, 0.0D, 1.0D);
		away = away.lengthSqr() < 1.0E-4D ? forward() : away.normalize();
		player.setDeltaMovement(away.x * horizontal, up, away.z * horizontal);
		player.hurtMarked = true;
	}

	/**
	 * A blood manipulation or Primal Morphling ability from the owner. Cast while held, it tears the player free at once;
	 * otherwise it opens a short window in which landing a blow on her counts as an empowered break.
	 */
	public void onOwnerCast(ServerPlayer caster) {
		if (heldPlayer() == caster && MycophantClaimRules.castBreaks(true, false, this.holdCastUsed)) {
			this.holdCastUsed = true;
			breakHold(caster, true);
			return;
		}
		this.lastOwnerCastTick = this.tickCount;
	}

	private boolean withinCastWindow() {
		return this.tickCount - this.lastOwnerCastTick <= MycophantClaimRules.castWindowTicks();
	}

	// ------------------------------------------------------------------ the siphon

	void latchSiphon(ServerPlayer player) {
		setSiphonTargetId(player.getId());
		this.siphonDamageTaken = 0;
		this.level().playSound(null, player.blockPosition(), SoundInit.ENTITY_MYCOPHANT_HURT_OTHER.get(), SoundSource.HOSTILE, 1.0F, 1.4F);
	}

	void unlatchSiphon() {
		setSiphonTargetId(-1);
		this.siphonDamageTaken = 0;
	}

	@Nullable
	ServerPlayer siphonTarget() {
		int id = getSiphonTargetId();
		if (id < 0) return null;
		Entity entity = this.level().getEntity(id);
		return entity instanceof ServerPlayer player && player.isAlive() ? player : null;
	}

	private void tickSiphon() {
		if (getSiphonTargetId() < 0) return;
		ServerPlayer target = siphonTarget();
		if (target == null || getAttack() != MycophantAttack.SIPHON_FEED || getState() != MycophantState.ATTACK
				|| target.distanceTo(this) > MycophantClaimRules.siphonRange() + 2.0D || !this.hasLineOfSight(target)) {
			unlatchSiphon();
			return;
		}
		Vec3 reel = position().subtract(target.position()).multiply(1.0D, 0.0D, 1.0D);
		if (reel.lengthSqr() > 9.0D) {
			Vec3 pull = reel.normalize().scale(MycophantClaimRules.siphonReelPerTick());
			target.push(pull.x, 0.0D, pull.z);
			target.hurtMarked = true;
		}
		if (this.tickCount % 20 == 0) {
			int phase = getPhase();
			var volume = HemoCapabilityAccess.getBloodVolume(target);
			boolean drained = volume.isActive() && !volume.isEmpty();
			if (drained) {
				BloodAccounting.pay(target, volume.getMaxBloodVolume() * (phase >= 2 ? 0.025D : 0.015D), SIPHON);
			} else {
				float damage = MycophantClaimRules.clampNonlethal(target.getHealth(), MycophantClaimRules.siphonDrainPerSecond(phase));
				if (damage > 0.0F) target.hurt(HemoDamageTypes.mycophantClaim(this.level(), this), damage);
			}
			addPressure(1.0F);
		}
		MycophantMoves.siphonTick(this, target);
	}

	// ------------------------------------------------------------------ the brood

	@Override
	public void onBroodLost(FunglingEntity brood, Vec3 where) {
		if (!isAlive() || getState() == MycophantState.YIELD) return;
		controller.requestStagger(MycophantControllerRules.griefTicks(), where);
	}

	public void discardBrood() {
		if (!(this.level() instanceof ServerLevel level)) return;
		for (FunglingEntity fungling : level.getEntitiesOfClass(FunglingEntity.class, getBoundingBox().inflate(48.0D),
				f -> this.getUUID().equals(f.getBroodMother()))) {
			MycophantMoves.dissolve(level, fungling.position());
			fungling.discard();
		}
	}

	int livingBrood() {
		return this.level().getEntitiesOfClass(FunglingEntity.class, getBoundingBox().inflate(48.0D),
				f -> f.isAlive() && this.getUUID().equals(f.getBroodMother())).size();
	}

	// ------------------------------------------------------------------ damage

	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (this.level().isClientSide) return super.hurt(source, amount);
		if (isUntouchable()) return false;
		Entity attacker = source.getEntity();
		boolean fromOwner = attacker instanceof ServerPlayer player && this.encounterOwner != null
				&& this.encounterOwner.equals(player.getUUID());
		boolean fromQuarry = fromOwner || (this.encounterOwner == null && attacker instanceof Player);
		// a manipulation that lands on her (or any blow inside a cast's window) breaks a hold or the guard
		if (fromQuarry && (withinCastWindow() || isManipulationHit(source))) {
			if (isHolding() && MycophantClaimRules.castBreaks(false, true, this.holdCastUsed)) {
				ServerPlayer held = heldPlayer();
				this.holdCastUsed = true;
				if (held != null) breakHold(held, true);
			} else if (controller.isGuarding()) {
				controller.breakGuard();
			}
			this.lastOwnerCastTick = -1000;
		}
		if (controller.isGuarding() && attacker != null) amount = MycophantClaimRules.guardedDamage(amount, angleTo(attacker.position()));
		boolean hurt = super.hurt(source, amount);
		if (!hurt) return false;
		if (fromQuarry && isHolding()) {
			if (++this.holdHits >= MycophantClaimRules.hitsToBreak(getPhase())) {
				ServerPlayer held = heldPlayer();
				if (held != null) breakHold(held, false);
			}
		}
		if (fromQuarry && getSiphonTargetId() >= 0) {
			this.siphonDamageTaken += Math.round(amount);
			if (this.siphonDamageTaken >= 6) unlatchSiphon();
		}
		if (!this.splitDone && getPhase() < 2 && MycophantCombatRules.phase(getHealth(), getMaxHealth()) >= 2 && isAlive()) {
			releaseHold();
			unlatchSiphon();
			controller.skipToRecovery();
			controller.requestSplit();
		}
		return true;
	}

	/** A blood manipulation's own hit: its school context names a registered manipulation. */
	private static boolean isManipulationHit(DamageSource source) {
		SchoolHitContext hit = SchoolDamage.current();
		if (hit == null && source instanceof SchoolDamageSource school) hit = school.context();
		if (hit == null && source.getDirectEntity() != null) hit = SchoolDamage.projectileContext(source.getDirectEntity());
		return hit != null && ManipulationInit.MANIPS_TYPE_REGISTRY.containsKey(hit.ability());
	}

	/** The boss theme waits for her roar. */
	public boolean isMusicSilent() {
		return getState() == MycophantState.INTRO && !MycophantIntroRules.musicPlays(getIntroTick(), isRematchIntro());
	}

	@Override
	public boolean doHurtTarget(Entity target) {
		boolean hurt = super.doHurtTarget(target);
		if (hurt) {
			this.playSound(SoundInit.ENTITY_MYCOPHANT_HURT_OTHER.get(), 1.0F, 0.9F + this.random.nextFloat() * 0.25F);
			EndgameBossActions.disableShieldOnHit(this, target, 80);
		}
		return hurt;
	}

	@Override
	protected boolean shouldDropLoot() {
		// an owned Mycophant pays out through MycophantEncounterManager.completeVictory instead
		return this.encounterOwner == null && super.shouldDropLoot();
	}

	// ------------------------------------------------------------------ the yielding

	@Override
	public void die(DamageSource source) {
		if (!this.level().isClientSide) {
			releaseHold();
			unlatchSiphon();
			discardBrood();
			setState(MycophantState.YIELD);
			setAttack(MycophantAttack.NONE);
			this.entityData.set(DATA_YIELD_TICK, 0);
			this.level().playSound(null, this.blockPosition(), SoundInit.ENTITY_MYCOPHANT_DEATH.get(), SoundSource.HOSTILE, 1.4F, 0.6F);
		}
		super.die(source);
	}

	@Override
	protected void tickDeath() {
		// no flip and no explosion: she folds, bows, offers the Tendril and sinks into the nectar
		int t = getYieldTick() + 1;
		if (!this.level().isClientSide) {
			this.entityData.set(DATA_YIELD_TICK, t);
			MycophantMoves.yieldTick(this, t);
			if (t >= MycophantYieldRules.length()) {
				if (this.encounterOwner != null) MycophantEncounterManager.completeVictory(this);
				this.bossBar.hideAll();
				this.remove(RemovalReason.KILLED);
			}
		}
	}

	@Override
	public void remove(RemovalReason reason) {
		if (!this.level().isClientSide) this.bossBar.hideAll();
		super.remove(reason);
	}

	// ------------------------------------------------------------------ boss bar, sounds, persistence

	@Override
	public void startSeenByPlayer(ServerPlayer player) {
		super.startSeenByPlayer(player);
		if (this.bossBarShown) this.bossBar.show(player);
	}

	@Override
	public void stopSeenByPlayer(ServerPlayer player) {
		super.stopSeenByPlayer(player);
		this.bossBar.hide(player);
	}

	@Override
	public boolean removeWhenFarAway(double distanceToClosestPlayer) {
		return false;
	}

	@Override
	public boolean fireImmune() {
		return true;
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	protected float getSoundVolume() {
		return 0.65F;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundInit.ENTITY_MYCOPHANT_AMBIENT.get();
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundInit.ENTITY_MYCOPHANT_HURT.get();
	}

	@Override
	protected SoundEvent getDeathSound() {
		return null;
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		if (this.encounterOwner != null) tag.putUUID("MycophantOwner", this.encounterOwner);
		tag.putFloat("NectarPressure", this.nectarPressure);
		tag.putBoolean("EnteredPhaseTwo", this.splitDone);
		tag.putBoolean("IntroDone", this.introDone);
		tag.putBoolean("RematchIntro", isRematchIntro());
	}

	/** Old saves (cocoon keys, no intro flag) resume idle: phase from health, no hold, split done if past half. */
	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		this.encounterOwner = tag.hasUUID("MycophantOwner") ? tag.getUUID("MycophantOwner") : null;
		this.nectarPressure = tag.getFloat("NectarPressure");
		MycophantSaveRules.Resume resume = MycophantSaveRules.resume(tag.getBoolean("EnteredPhaseTwo"),
				tag.contains("IntroDone"), tag.getBoolean("IntroDone"), getHealth(), getMaxHealth());
		this.splitDone = resume.splitDone();
		this.introDone = resume.introDone();
		setRematchIntro(tag.getBoolean("RematchIntro"));
		setPhase(resume.phase());
		if (this.splitDone) setSplitTick(MycophantSplitRules.length());
		setState(this.introDone ? MycophantState.IDLE : MycophantState.INTRO);
		setAttack(MycophantAttack.NONE);
		releaseHold();
		unlatchSiphon();
		if (this.introDone) this.bossBarShown = false;
		if (this.encounterOwner != null) this.xpReward = 0;
	}

	MycophantController controller() {
		return this.controller;
	}

	AABB arenaBox() {
		return getBoundingBox().inflate(32.0D, 8.0D, 32.0D);
	}

	/**
	 * She wades through her nectar rather than floating in it: afloat in the shallow pool she never touches ground, so
	 * step-up never fires and the pool's rim would hold her in. On its bottom her claws haul her over the rim.
	 */
	@Override
	public boolean isAffectedByFluids() {
		return false;
	}

	@Override
	public boolean isPushedByFluid() {
		return false;
	}

	/** Her claws, crest and opened wings reach well past the hitbox; cull on the whole body so she never pops at screen edges. */
	@Override
	public AABB getBoundingBoxForCulling() {
		return getBoundingBox().inflate(6.0D, 0.0D, 6.0D).expandTowards(0.0D, 5.0D, 0.0D).expandTowards(0.0D, -1.5D, 0.0D);
	}
}
