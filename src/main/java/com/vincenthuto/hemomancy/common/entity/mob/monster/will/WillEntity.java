package com.vincenthuto.hemomancy.common.entity.mob.monster.will;

import com.vincenthuto.hemomancy.client.particle.factory.WillAbsorptionGlowParticleFactory;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.entity.summon.BoundPuppeteerSummon;
import com.vincenthuto.hemomancy.common.entity.summon.BoundSummonBehavior;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.MarionetteCrossbarItem;
import com.vincenthuto.hemomancy.common.manipulation.MobManipCaster;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonRules;
import com.vincenthuto.hemomancy.common.worldgen.FungalGardenTravelHelper;
import com.vincenthuto.hemomancy.config.HemoServerConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class WillEntity extends Monster implements BoundPuppeteerSummon {
	private static final int NORMAL_DISSOLVE_TICKS = 40;
	private static final int BLOOD_ABSORPTION_DISSOLVE_TICKS = 120;
	private static final EntityDataAccessor<Byte> DATA_ORIGIN =
			SynchedEntityData.defineId(WillEntity.class, EntityDataSerializers.BYTE);
	private static final EntityDataAccessor<Byte> DATA_SCHOOL =
			SynchedEntityData.defineId(WillEntity.class, EntityDataSerializers.BYTE);
	private static final EntityDataAccessor<Byte> DATA_TIER =
			SynchedEntityData.defineId(WillEntity.class, EntityDataSerializers.BYTE);
	private static final EntityDataAccessor<Byte> DATA_PHASE =
			SynchedEntityData.defineId(WillEntity.class, EntityDataSerializers.BYTE);
	private static final EntityDataAccessor<Optional<UUID>> DATA_TARGET_UUID =
			SynchedEntityData.defineId(WillEntity.class, EntityDataSerializers.OPTIONAL_UUID);
	private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER_UUID =
			SynchedEntityData.defineId(WillEntity.class, EntityDataSerializers.OPTIONAL_UUID);
	private static final EntityDataAccessor<Optional<UUID>> DATA_CROSSBAR_UUID =
			SynchedEntityData.defineId(WillEntity.class, EntityDataSerializers.OPTIONAL_UUID);
	private static final EntityDataAccessor<String> DATA_SUMMON_NAME =
			SynchedEntityData.defineId(WillEntity.class, EntityDataSerializers.STRING);
	private static final EntityDataAccessor<Integer> DATA_DISMISSAL_TICKS =
			SynchedEntityData.defineId(WillEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> DATA_TRIAL_SUMMON =
			SynchedEntityData.defineId(WillEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Optional<UUID>> DATA_TRIAL_CASTER_UUID =
			SynchedEntityData.defineId(WillEntity.class, EntityDataSerializers.OPTIONAL_UUID);
	private static final EntityDataAccessor<Boolean> DATA_ABSORPTION_DISSOLVE =
			SynchedEntityData.defineId(WillEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Integer> DATA_DISSOLVE_TICKS =
			SynchedEntityData.defineId(WillEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_DISSOLVE_DURATION =
			SynchedEntityData.defineId(WillEntity.class, EntityDataSerializers.INT);

	private int falterTicks;
	private float falterBurstDamage;
	private int falterBurstWindowTicks;
	private UUID falterBurstAttackerId;
	private int dissolveTicks;
	private int dissolveDurationTicks = NORMAL_DISSOLVE_TICKS;
	private int castCooldown;
	private int kitIndex;
	private UUID redirectedOwner;
	private UUID dissolveTargetId;
	private long redirectUntilGameTime;
	private boolean dropsOnDissolve = true;

	public WillEntity(EntityType<? extends Monster> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder setAttributes() {
		return Monster.createMonsterAttributes()
				.add(Attributes.MAX_HEALTH, 20.0D)
				.add(Attributes.ATTACK_DAMAGE, 3.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.26D)
				.add(Attributes.FOLLOW_RANGE, 32.0D);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.05D, false));
		goalSelector.addGoal(7, new RandomStrollGoal(this, 0.8D));
		goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
		targetSelector.addGoal(1, new HurtByTargetGoal(this));
		targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_ORIGIN, (byte) WillOrigin.BROKEN.ordinal());
		builder.define(DATA_SCHOOL, (byte) EnumBloodTendency.ANIMUS.ordinal());
		builder.define(DATA_TIER, (byte) 1);
		builder.define(DATA_PHASE, (byte) WillPhase.MATERIALIZED.ordinal());
		builder.define(DATA_TARGET_UUID, Optional.empty());
		builder.define(DATA_OWNER_UUID, Optional.empty());
		builder.define(DATA_CROSSBAR_UUID, Optional.empty());
		builder.define(DATA_SUMMON_NAME, "claimed_will");
		builder.define(DATA_DISMISSAL_TICKS, 0);
		builder.define(DATA_TRIAL_SUMMON, false);
		builder.define(DATA_TRIAL_CASTER_UUID, Optional.empty());
		builder.define(DATA_ABSORPTION_DISSOLVE, false);
		builder.define(DATA_DISSOLVE_TICKS, 0);
		builder.define(DATA_DISSOLVE_DURATION, NORMAL_DISSOLVE_TICKS);
	}

	@Nullable
	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
			MobSpawnType reason, @Nullable SpawnGroupData spawnData) {
		SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, spawnData);
		applyRuleStats(null);
		return data;
	}

	public void configure(WillOrigin origin, EnumBloodTendency school, int tier, @Nullable Player target,
			boolean drifting) {
		setOrigin(origin);
		setSchool(school);
		setTier(tier);
		if (target != null) {
			entityData.set(DATA_TARGET_UUID, Optional.of(target.getUUID()));
			setTarget(target);
		}
		setPhase(drifting ? WillPhase.DRIFTING : WillPhase.MATERIALIZED);
		clearFalterBurst();
		applyRuleStats(target);
	}

	private void applyRuleStats(@Nullable Player target) {
		WillCombatRules.Stats stats = getOrigin() == WillOrigin.SENT
				? WillCombatRules.sentStats(target == null ? 6 : HemoCapabilityAccess.getPlayerDegreeNumber(target),
						target == null ? 20.0D : target.getMaxHealth())
				: WillCombatRules.brokenStats(getTier());
		if (getAttribute(Attributes.MAX_HEALTH) != null) {
			getAttribute(Attributes.MAX_HEALTH).setBaseValue(stats.maxHealth());
			setHealth(getMaxHealth());
		}
		if (getAttribute(Attributes.ATTACK_DAMAGE) != null) {
			getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(stats.attackDamage());
		}
		if (getAttribute(Attributes.MOVEMENT_SPEED) != null) {
			getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(stats.movementSpeed());
		}
	}

	@Override
	public void tick() {
		super.tick();
		if (level().isClientSide) return;
		if (hemomancy$getOwnerUUID() != null) {
			if (BoundSummonBehavior.commonServerTick(this, this)) {
				tickCasting();
			}
			return;
		}
		if (redirectedOwner != null) {
			if (level().getGameTime() >= redirectUntilGameTime) {
				startDissolving(true);
				return;
			}
			if (getTarget() == null || !getTarget().isAlive() || getTarget() instanceof Player) {
				level().getEntitiesOfClass(Monster.class, getBoundingBox().inflate(24.0D),
						mob -> mob != this && !(mob instanceof BoundPuppeteerSummon))
						.stream().findFirst().ifPresent(this::setTarget);
			}
		}
		if (getPhase() == WillPhase.DRIFTING && tickCount % 40 == 0) {
			setPhase(WillPhase.MATERIALIZED);
			playSound(SoundInit.ENTITY_WILL_MATERIALIZE.get(), 0.9F, 0.9F + random.nextFloat() * 0.25F);
		}
		if (getPhase() == WillPhase.MATERIALIZED) {
			tickFalterBurstWindow();
		}
		if (getPhase() == WillPhase.FALTERING) {
			if (--falterTicks <= 0) {
				setNoAi(false);
				setPhase(WillPhase.MATERIALIZED);
				clearFalterBurst();
			}
			return;
		}
		if (getPhase() == WillPhase.DISSOLVING) {
			dissolveTicks++;
			entityData.set(DATA_DISSOLVE_TICKS, dissolveTicks);
			if (dissolveTicks >= dissolveDurationTicks) {
				if (dropsOnDissolve) dropDissolveLoot();
				if (entityData.get(DATA_ABSORPTION_DISSOLVE)) dropAbsorptionDissolveLoot();
				if (entityData.get(DATA_ABSORPTION_DISSOLVE)) spawnAbsorptionDissolvePulse();
				discard();
			}
			return;
		}
		tickCasting();
	}

	private void tickCasting() {
		if (getPhase() != WillPhase.MATERIALIZED) return;
		LivingEntity target = getTarget();
		if (target == null || !target.isAlive()) return;
		if (castCooldown-- > 0) return;
		castCooldown = WillCombatRules.castIntervalTicks(getTier());
		List<ResourceLocation> kit = WillCombatRules.schoolKit(getSchool(), getTier());
		ResourceLocation id = kit.get(kitIndex++ % kit.size());
		boolean fired = MobManipCaster.cast(this, id, HemoServerConfig.DRUDGE_WORK_RADIUS.get());
		if (!fired && distanceToSqr(target) < 24.0D * 24.0D) {
			target.hurt(damageSources().mobAttack(this), (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.75F);
		}
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (getPhase() == WillPhase.DISSOLVING) return false;
		if (amount >= getHealth()) {
			setHealth(1.0F);
			startDissolving(true);
			return true;
		}
		float healthBefore = getHealth();
		boolean result = super.hurt(source, amount);
		if (result) {
			float dealt = Math.max(0.0F, healthBefore - getHealth());
			trackFalterBurst(source, dealt);
		}
		return result;
	}

	private void trackFalterBurst(DamageSource source, float amount) {
		if (amount <= 0.0F) return;
		if (getOrigin() != WillOrigin.BROKEN) return;
		if (getPhase() != WillPhase.MATERIALIZED) return;
		if (!(source.getEntity() instanceof Player player)) return;
		if (falterBurstWindowTicks <= 0 || falterBurstAttackerId == null
				|| !player.getUUID().equals(falterBurstAttackerId)) {
			clearFalterBurst();
			falterBurstAttackerId = player.getUUID();
			falterBurstWindowTicks = safeConfig(HemoServerConfig.WILL_FALTER_BURST_WINDOW_TICKS,
					WillCombatRules.falterBurstWindowTicks());
		}
		falterBurstDamage += amount;
		double threshold = getMaxHealth() * safeConfig(HemoServerConfig.WILL_FALTER_BURST_FRACTION,
				WillCombatRules.falterBurstFraction());
		if (falterBurstDamage >= threshold) {
			enterFaltering();
		}
	}

	private void tickFalterBurstWindow() {
		if (falterBurstWindowTicks > 0 && --falterBurstWindowTicks <= 0) {
			clearFalterBurst();
		}
	}

	private void enterFaltering() {
		clearFalterBurst();
		setPhase(WillPhase.FALTERING);
		falterTicks = safeConfig(HemoServerConfig.WILL_FALTER_WINDOW_TICKS, WillCombatRules.falterWindowTicks());
		setNoAi(true);
		playSound(SoundInit.ENTITY_WILL_FALTER.get(), 0.8F, 1.25F);
	}

	private void clearFalterBurst() {
		falterBurstDamage = 0.0F;
		falterBurstWindowTicks = 0;
		falterBurstAttackerId = null;
	}

	private void startDissolving(boolean drops) {
		startDissolving(drops, NORMAL_DISSOLVE_TICKS, false);
	}

	private void startDissolving(boolean drops, int durationTicks) {
		startDissolving(drops, durationTicks, false);
	}

	private void startDissolving(boolean drops, int durationTicks, boolean absorptionDissolve) {
		clearFalterBurst();
		dropsOnDissolve = drops;
		dissolveTicks = 0;
		dissolveDurationTicks = Math.max(1, durationTicks);
		entityData.set(DATA_ABSORPTION_DISSOLVE, absorptionDissolve);
		entityData.set(DATA_DISSOLVE_TICKS, 0);
		entityData.set(DATA_DISSOLVE_DURATION, dissolveDurationTicks);
		if (getTarget() instanceof ServerPlayer player) {
			dissolveTargetId = player.getUUID();
		} else {
			dissolveTargetId = entityData.get(DATA_TARGET_UUID).orElse(null);
		}
		setNoAi(true);
		setTarget(null);
		setPhase(WillPhase.DISSOLVING);
		playSound(SoundInit.ENTITY_WILL_DISSOLVE.get(), 0.9F, 0.75F);
	}

	public boolean canBloodUtilityBend() {
		return isAlive() && getOrigin() == WillOrigin.BROKEN && getPhase() == WillPhase.FALTERING;
	}

	public boolean canBloodAbsorptionDrawParticles() {
		return isAlive() && getOrigin() == WillOrigin.BROKEN
				&& (getPhase() == WillPhase.FALTERING
						|| (getPhase() == WillPhase.DISSOLVING && entityData.get(DATA_ABSORPTION_DISSOLVE)));
	}

	public boolean isAbsorptionDissolving() {
		return getPhase() == WillPhase.DISSOLVING && entityData.get(DATA_ABSORPTION_DISSOLVE);
	}

	public float getDissolveProgress(float partialTicks) {
		if (getPhase() != WillPhase.DISSOLVING) {
			return 0.0F;
		}
		float ticks = entityData.get(DATA_DISSOLVE_TICKS) + partialTicks;
		return Mth.clamp(ticks / Math.max(1.0F, entityData.get(DATA_DISSOLVE_DURATION)), 0.0F, 1.0F);
	}

	public float absorbFalteringWithBlood(ServerPlayer player, float amount) {
		if (!canBloodUtilityBend()) {
			return 0.0F;
		}
		float absorbed = Math.max(0.0F, Math.min(getHealth(), amount));
		if (absorbed <= 0.0F) {
			return 0.0F;
		}
		float remainingHealth = getHealth() - absorbed;
		if (remainingHealth > 1.0F) {
			setHealth(remainingHealth);
			return absorbed;
		}
		setHealth(1.0F);
		HemoCapabilityAccess.getBloodTendency(player)
				.ifPresent(tendency -> tendency.addTendencyAlignment(getSchool(), 3.0F));
		startDissolving(false, BLOOD_ABSORPTION_DISSOLVE_TICKS, true);
		return absorbed;
	}

	public boolean projectBanishWithBlood(ServerPlayer player) {
		if (!canBloodUtilityBend()) {
			return false;
		}
		HemoCapabilityAccess.getBloodTendency(player)
				.ifPresent(tendency -> tendency.addTendencyAlignment(getSchool(), -3.0F));
		spawnProjectedBanishmentBurst();
		startDissolving(false);
		return true;
	}

	private void spawnProjectedBanishmentBurst() {
		if (level() instanceof ServerLevel serverLevel) {
			serverLevel.sendParticles(ParticleTypes.POOF, getX(), getY() + getBbHeight() * 0.55D, getZ(),
					24, getBbWidth() * 0.55D, getBbHeight() * 0.35D, getBbWidth() * 0.55D, 0.04D);
			serverLevel.sendParticles(ParticleTypes.CRIMSON_SPORE, getX(), getY() + getBbHeight() * 0.55D, getZ(),
					18, getBbWidth() * 0.65D, getBbHeight() * 0.4D, getBbWidth() * 0.65D, 0.02D);
		}
	}

	private void spawnAbsorptionDissolvePulse() {
		if (level() instanceof ServerLevel serverLevel) {
			Vec3 center = position().add(0.0D, getBbHeight() * 0.55D, 0.0D);
			for (int i = 0; i < 42; i++) {
				Vec3 direction = new Vec3(random.nextDouble() * 2.0D - 1.0D,
						random.nextDouble() * 1.6D - 0.55D,
						random.nextDouble() * 2.0D - 1.0D).normalize();
				double speed = 0.12D + random.nextDouble() * 0.16D;
				serverLevel.sendParticles(WillAbsorptionGlowParticleFactory.createPulseData(ParticleColor.BLACK),
						center.x, center.y, center.z, 0,
						direction.x * speed, direction.y * speed, direction.z * speed, 1.0D);
			}
		}
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundInit.ENTITY_WILL_AMBIENT.get();
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundInit.ENTITY_WILL_HURT.get();
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundInit.ENTITY_WILL_DISSOLVE.get();
	}

	private void dropDissolveLoot() {
		WillCombatRules.LootProfile loot = WillCombatRules.lootFor(getSchool(), getTier(), getOrigin());
		spawnAtLocation(EnumBloodTendency.getRepEnzyme(loot.school()));
		if (loot.canDropFadedMemory() && random.nextFloat() < 0.08F) {
			spawnAtLocation(ItemInit.faded_memory.get());
		}
		if (getOrigin() == WillOrigin.SENT && dissolveTargetId != null && level().getServer() != null) {
			ServerPlayer player = level().getServer().getPlayerList().getPlayer(dissolveTargetId);
			if (player != null) {
				HemoCapabilityAccess.getWillAmbushState(player).addHiveAttention(1);
			}
		}
	}

	private void dropAbsorptionDissolveLoot() {
		if (random.nextFloat() < 0.5F) spawnAtLocation(EnumBloodTendency.getRepEnzyme(getSchool()));
		if (random.nextFloat() < 0.15F) spawnAtLocation(ItemInit.faded_memory.get());
	}

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand hand) {
		if (!(player instanceof ServerPlayer serverPlayer)) {
			return InteractionResult.SUCCESS;
		}
		if (!safeConfig(HemoServerConfig.WILL_BEND_ENABLED, true)) {
			return InteractionResult.PASS;
		}
		ItemStack stack = player.getItemInHand(hand);
		if (!(stack.getItem() instanceof MarionetteCrossbarItem)) {
			return InteractionResult.PASS;
		}
		WillBendRules.HeldItemKind held = stack.getItem() instanceof MarionetteCrossbarItem
				? WillBendRules.HeldItemKind.MARIONETTE_CROSSBAR
				: WillBendRules.HeldItemKind.EMPTY_OR_STAFF;
		boolean silent = FungalGardenTravelHelper.ARCHON_CHOICE_SILENCE.equals(
				player.getPersistentData().getString(FungalGardenTravelHelper.ARCHON_CHOICE_KEY));
		int archonBonus = HemoServerConfig.WILL_CLAIMED_BONUS_CAP_SILENT_ARCHON == null
				? WillBendRules.silentArchonBonusCap()
				: HemoServerConfig.WILL_CLAIMED_BONUS_CAP_SILENT_ARCHON.get();
		int cap = PuppeteerSummonRules.activeSummonCap(0) + (silent ? archonBonus : 0);
		boolean capAvailable = MarionetteCrossbarItem.activeSummonsForOwner(player).size() < cap;
		WillBendRules.BendOutcome outcome = WillBendRules.resolve(getOrigin(), getPhase(),
				HemoCapabilityAccess.getPlayerDegreeNumber(player), held, player.isShiftKeyDown(), capAvailable, silent);
		if (outcome.backfire()) {
			serverPlayer.addEffect(new MobEffectInstance(EffectInit.blood_drunkenness, 1200, 1));
			serverPlayer.addEffect(new MobEffectInstance(EffectInit.hematic_strain, 600, 0));
			HemoCapabilityAccess.getWillAmbushState(serverPlayer).addHiveAttention(outcome.hiveAttentionGain());
			return InteractionResult.CONSUME;
		}
		if (outcome.verb() == WillBendRules.BendVerb.REDIRECT) {
			boolean paid = HemoCapabilityAccess.getBloodVolume(serverPlayer)
					.map(volume -> volume.isActive() && volume.drain(outcome.bloodCost()))
					.orElse(false);
			if (!paid) return InteractionResult.CONSUME;
			redirectedOwner = serverPlayer.getUUID();
			redirectUntilGameTime = level().getGameTime() + 1800L;
			clearFalterBurst();
			setNoAi(false);
			setPhase(WillPhase.MATERIALIZED);
			return InteractionResult.CONSUME;
		}
		if (outcome.verb() == WillBendRules.BendVerb.COMMANDEER) {
			if (!safeConfig(HemoServerConfig.WILL_COMMANDEER_ENABLED, true)) return InteractionResult.CONSUME;
			if (!MarionetteCrossbarItem.consumeThread(stack, outcome.threadCost())) return InteractionResult.CONSUME;
			hemomancy$setOwnerUUID(serverPlayer.getUUID());
			hemomancy$setCrossbarUUID(MarionetteCrossbarItem.ensureCrossbarId(stack));
			hemomancy$setSummonName("claimed_will");
			clearFalterBurst();
			setNoAi(false);
			setPhase(WillPhase.MATERIALIZED);
			return InteractionResult.CONSUME;
		}
		return InteractionResult.CONSUME;
	}

	private static boolean safeConfig(net.neoforged.neoforge.common.ModConfigSpec.BooleanValue value, boolean fallback) {
		return value == null ? fallback : value.get();
	}

	private static int safeConfig(net.neoforged.neoforge.common.ModConfigSpec.IntValue value, int fallback) {
		return value == null ? fallback : value.get();
	}

	private static double safeConfig(net.neoforged.neoforge.common.ModConfigSpec.DoubleValue value, double fallback) {
		return value == null ? fallback : value.get();
	}

	@Override
	public boolean canAttack(LivingEntity target) {
		if (hemomancy$getOwnerUUID() != null || redirectedOwner != null) {
			return BoundSummonBehavior.canAttack(this, this, target) && super.canAttack(target);
		}
		return super.canAttack(target);
	}

	public WillOrigin getOrigin() {
		return WillOrigin.values()[Math.max(0, Math.min(WillOrigin.values().length - 1, entityData.get(DATA_ORIGIN)))];
	}

	public void setOrigin(WillOrigin origin) {
		entityData.set(DATA_ORIGIN, (byte) origin.ordinal());
	}

	public EnumBloodTendency getSchool() {
		return EnumBloodTendency.values()[Math.max(0, Math.min(EnumBloodTendency.values().length - 1, entityData.get(DATA_SCHOOL)))];
	}

	public void setSchool(EnumBloodTendency school) {
		entityData.set(DATA_SCHOOL, (byte) school.ordinal());
	}

	public int getTier() {
		return Math.max(1, entityData.get(DATA_TIER));
	}

	public void setTier(int tier) {
		entityData.set(DATA_TIER, (byte) Math.max(1, Math.min(4, tier)));
	}

	public WillPhase getPhase() {
		return WillPhase.values()[Math.max(0, Math.min(WillPhase.values().length - 1, entityData.get(DATA_PHASE)))];
	}

	public void setPhase(WillPhase phase) {
		entityData.set(DATA_PHASE, (byte) phase.ordinal());
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putString("WillOrigin", getOrigin().name());
		tag.putString("WillSchool", getSchool().name());
		tag.putInt("WillTier", getTier());
		tag.putString("WillPhase", getPhase().name());
		tag.putInt("FalterTicks", falterTicks);
		tag.putFloat("FalterBurstDamage", falterBurstDamage);
		tag.putInt("FalterBurstWindowTicks", falterBurstWindowTicks);
		if (falterBurstAttackerId != null) tag.putUUID("FalterBurstAttacker", falterBurstAttackerId);
		tag.putInt("DissolveTicks", dissolveTicks);
		tag.putInt("DissolveDurationTicks", dissolveDurationTicks);
		tag.putBoolean("AbsorptionDissolve", entityData.get(DATA_ABSORPTION_DISSOLVE));
		if (redirectedOwner != null) tag.putUUID("RedirectedOwner", redirectedOwner);
		if (dissolveTargetId != null) tag.putUUID("DissolveTarget", dissolveTargetId);
		tag.putLong("RedirectUntilGameTime", redirectUntilGameTime);
		BoundSummonBehavior.save(this, tag);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		if (tag.contains("WillOrigin")) setOrigin(WillOrigin.valueOf(tag.getString("WillOrigin")));
		if (tag.contains("WillSchool")) setSchool(EnumBloodTendency.valueOf(tag.getString("WillSchool")));
		setTier(tag.getInt("WillTier"));
		if (tag.contains("WillPhase")) setPhase(WillPhase.valueOf(tag.getString("WillPhase")));
		falterTicks = tag.getInt("FalterTicks");
		falterBurstDamage = tag.getFloat("FalterBurstDamage");
		falterBurstWindowTicks = tag.getInt("FalterBurstWindowTicks");
		falterBurstAttackerId = tag.hasUUID("FalterBurstAttacker") ? tag.getUUID("FalterBurstAttacker") : null;
		dissolveTicks = tag.getInt("DissolveTicks");
		dissolveDurationTicks = tag.contains("DissolveDurationTicks")
				? Math.max(1, tag.getInt("DissolveDurationTicks"))
				: NORMAL_DISSOLVE_TICKS;
		entityData.set(DATA_ABSORPTION_DISSOLVE, tag.getBoolean("AbsorptionDissolve"));
		entityData.set(DATA_DISSOLVE_TICKS, dissolveTicks);
		entityData.set(DATA_DISSOLVE_DURATION, dissolveDurationTicks);
		if (tag.hasUUID("RedirectedOwner")) redirectedOwner = tag.getUUID("RedirectedOwner");
		if (tag.hasUUID("DissolveTarget")) dissolveTargetId = tag.getUUID("DissolveTarget");
		redirectUntilGameTime = tag.getLong("RedirectUntilGameTime");
		BoundSummonBehavior.load(this, tag);
	}

	@Override public UUID hemomancy$getOwnerUUID() { return entityData.get(DATA_OWNER_UUID).orElse(null); }
	@Override public void hemomancy$setOwnerUUID(UUID ownerUuid) { entityData.set(DATA_OWNER_UUID, Optional.ofNullable(ownerUuid)); }
	@Override public UUID hemomancy$getCrossbarUUID() { return entityData.get(DATA_CROSSBAR_UUID).orElse(null); }
	@Override public void hemomancy$setCrossbarUUID(UUID crossbarUuid) { entityData.set(DATA_CROSSBAR_UUID, Optional.ofNullable(crossbarUuid)); }
	@Override public String hemomancy$getSummonName() { return entityData.get(DATA_SUMMON_NAME); }
	@Override public void hemomancy$setSummonName(String summonName) { entityData.set(DATA_SUMMON_NAME, summonName == null ? "" : summonName); }
	@Override public int hemomancy$getDismissalTicks() { return entityData.get(DATA_DISMISSAL_TICKS); }
	@Override public void hemomancy$setDismissalTicks(int ticks) { entityData.set(DATA_DISMISSAL_TICKS, Math.max(0, ticks)); }
	@Override public boolean hemomancy$isTrialSummon() { return entityData.get(DATA_TRIAL_SUMMON); }
	@Override public void hemomancy$setTrialSummon(boolean trialSummon) { entityData.set(DATA_TRIAL_SUMMON, trialSummon); }
	@Override public UUID hemomancy$getTrialCasterUUID() { return entityData.get(DATA_TRIAL_CASTER_UUID).orElse(null); }
	@Override public void hemomancy$setTrialCasterUUID(UUID casterUuid) { entityData.set(DATA_TRIAL_CASTER_UUID, Optional.ofNullable(casterUuid)); }
}
