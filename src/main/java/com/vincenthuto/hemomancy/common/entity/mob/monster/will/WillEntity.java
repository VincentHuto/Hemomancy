package com.vincenthuto.hemomancy.common.entity.mob.monster.will;

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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
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

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class WillEntity extends Monster implements BoundPuppeteerSummon {
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

	private int falterTicks;
	private int dissolveTicks;
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
		if (getPhase() == WillPhase.FALTERING) {
			if (--falterTicks <= 0) {
				setNoAi(false);
				setPhase(WillPhase.MATERIALIZED);
			}
			return;
		}
		if (getPhase() == WillPhase.DISSOLVING) {
			if (++dissolveTicks >= 40) {
				if (dropsOnDissolve) dropDissolveLoot();
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
		boolean result = super.hurt(source, amount);
		if (result && getOrigin() == WillOrigin.BROKEN && getPhase() == WillPhase.MATERIALIZED
				&& getHealth() <= getMaxHealth() * WillCombatRules.falterFraction()) {
			setPhase(WillPhase.FALTERING);
			falterTicks = safeConfig(HemoServerConfig.WILL_FALTER_WINDOW_TICKS, WillCombatRules.falterWindowTicks());
			setNoAi(true);
			playSound(SoundInit.ENTITY_WILL_FALTER.get(), 0.8F, 1.25F);
		}
		return result;
	}

	private void startDissolving(boolean drops) {
		dropsOnDissolve = drops;
		dissolveTicks = 0;
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

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand hand) {
		if (!(player instanceof ServerPlayer serverPlayer)) {
			return InteractionResult.SUCCESS;
		}
		if (!safeConfig(HemoServerConfig.WILL_BEND_ENABLED, true)) {
			return InteractionResult.PASS;
		}
		ItemStack stack = player.getItemInHand(hand);
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
			setNoAi(false);
			setPhase(WillPhase.MATERIALIZED);
			return InteractionResult.CONSUME;
		}
		HemoCapabilityAccess.getBloodTendency(serverPlayer)
				.ifPresent(tendency -> tendency.addTendencyAlignment(getSchool(), 3.0F));
		if (random.nextFloat() < 0.5F) spawnAtLocation(EnumBloodTendency.getRepEnzyme(getSchool()));
		if (random.nextFloat() < 0.15F) spawnAtLocation(ItemInit.faded_memory.get());
		startDissolving(false);
		return InteractionResult.CONSUME;
	}

	private static boolean safeConfig(net.neoforged.neoforge.common.ModConfigSpec.BooleanValue value, boolean fallback) {
		return value == null ? fallback : value.get();
	}

	private static int safeConfig(net.neoforged.neoforge.common.ModConfigSpec.IntValue value, int fallback) {
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
		tag.putInt("DissolveTicks", dissolveTicks);
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
		dissolveTicks = tag.getInt("DissolveTicks");
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
