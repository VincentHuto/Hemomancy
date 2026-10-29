package com.vincenthuto.hemomancy.common.entity.boss.saint;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.SaintSarcophagusBlock;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberDiscoveryEvents;
import com.vincenthuto.hemomancy.common.entity.boss.presentation.BossIntroWindow;
import com.vincenthuto.hemomancy.common.entity.boss.BossReplayClock;
import com.vincenthuto.hemomancy.common.entity.boss.BossBarBinding;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

/**
 * O-1: the four entombed Saints fought in a trial (Hemorath, Seraphae, Putriciel, Velorum). They share a persistent
 * body, a boss bar, the rise out of the sarcophagus, a synced visual state that the renderer reads (a state id and the
 * ticks it has left), the floor-ring telegraph and the chat lines sent to everyone in the fight. Each Saint keeps its
 * own mechanics, goals and bar progress: Seraphae's bar is her containment integrity, not her health.
 * <p>
 * A Saint cannot be killed. Every ending (Hemorath's overload or fall, Putriciel's fifth absolution, Seraphae's
 * containment, Velorum's fall) goes through {@link #endTrial} once: it rolls the Saint's loot table
 * {@code entities/<id>} and closes the Saint back into the sarcophagus it rose from.
 */
public abstract class SaintTrialEntity extends Monster implements RisingSaint {
	private static final EntityDataAccessor<Integer> DATA_RISE_TICK =
			SynchedEntityData.defineId(SaintTrialEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_VISUAL_STATE =
			SynchedEntityData.defineId(SaintTrialEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_VISUAL_TICKS =
			SynchedEntityData.defineId(SaintTrialEntity.class, EntityDataSerializers.INT);

	public static final int VISUAL_NONE = 0;

	/** How far from the Saint a player can stand and still carry its reward when no one struck the last blow. */
	private static final double REWARD_RANGE = 32.0D;
	private static final String TAG_TOMB_POS = "SaintTombPos";
	private static final String TAG_TOMB_STATE = "SaintTombState";
	private static final String TAG_TRIAL_ENDED = "SaintTrialEnded";

	protected final BossBarBinding bossBar;
	private final BossIntroWindow rise = new BossIntroWindow(SaintRise.TICKS);
	private final BossReplayClock riseClock = new BossReplayClock();
	@Nullable
	private BlockPos tombPos;
	@Nullable
	private BlockState tombState;
	private boolean trialEnded;
	private static final EntityDataAccessor<Integer> DATA_SECONDARY_MOVE = SynchedEntityData.defineId(SaintTrialEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_SECONDARY_TICKS = SynchedEntityData.defineId(SaintTrialEntity.class, EntityDataSerializers.INT);
	private final SaintSecondaryAttacks secondary = new SaintSecondaryAttacks();
	private final BossReplayClock secondaryClock = new BossReplayClock();

	protected SaintTrialEntity(EntityType<? extends SaintTrialEntity> type, Level level, int xpReward,
			BossBarBinding bossBar) {
		super(type, level);
		this.bossBar = bossBar;
		this.setPersistenceRequired();
		this.xpReward = xpReward;
	}

	/** Which Saint this body is: the one its sarcophagus holds again when the trial ends. */
	public abstract EnumSaintType saintType();

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_VISUAL_STATE, VISUAL_NONE);
		builder.define(DATA_VISUAL_TICKS, 0);
		builder.define(DATA_RISE_TICK, -1);
		builder.define(DATA_SECONDARY_MOVE, 0);
		builder.define(DATA_SECONDARY_TICKS, 0);
	}

	void secondaryState(int move,int ticks) {
		entityData.set(DATA_SECONDARY_MOVE,move);entityData.set(DATA_SECONDARY_TICKS,Math.max(0,ticks));
	}
	public int getSecondaryMove(){return entityData.get(DATA_SECONDARY_MOVE);}
	public int getSecondaryTicks(){return entityData.get(DATA_SECONDARY_TICKS);}
	public float secondaryTime(float partial) {
		return secondaryClock.sample(getSecondaryMove(),getSecondaryTicks(),tickCount,partial,-1);
	}
	protected final void tickSecondaryAttacks(boolean allowed) {secondary.tick(this,allowed && !rising());}

	@Override
	public void beginRise() {
		secondary.cancel(this);
		rise.begin();
		this.entityData.set(DATA_RISE_TICK, 0);
		bossBar.setVisible(false);
	}

	@Override
	public float riseTime(float partialTick) {
		return SaintRise.time(riseClock, this.entityData.get(DATA_RISE_TICK), this.tickCount, partialTick);
	}

	/**
	 * Called first in a Saint's server {@code aiStep}: true while it is still rising out of its sarcophagus, so it
	 * skips its attacks. Shows the bar on the tick the rise ends.
	 */
	protected boolean tickRise() {
		if (!rise.active()) return false;
		if (this.level() instanceof ServerLevel server) SaintRise.tickEffects(server, this, rise.elapsed());
		boolean ended = rise.tick();
		this.entityData.set(DATA_RISE_TICK, rise.elapsed());
		if (ended) bossBar.setVisible(true);
		return true;
	}

	/** Still coming out of the sarcophagus: a Saint's own {@code hurt} rules wait for it. */
	protected boolean rising() {
		return rise.active();
	}

	/** Rising, it neither moves nor runs its goals, but it still settles onto the floor. */
	@Override
	protected boolean isImmobile() {
		return super.isImmobile() || rise.active();
	}

	@Override
	public boolean isInvulnerableTo(DamageSource source) {
		return rise.active() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) || super.isInvulnerableTo(source);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		rise.save(tag, "SaintRise");
		tag.putBoolean(TAG_TRIAL_ENDED, trialEnded);
		tag.putString("SaintAwakening", awakening);
		tag.putString("SaintTombRecord", tombRecord);
		net.minecraft.nbt.ListTag skipped = new net.minecraft.nbt.ListTag();
		skippedChapels.forEach(kind -> skipped.add(net.minecraft.nbt.StringTag.valueOf(kind)));
		tag.put("SaintSkippedChapels", skipped);
		if (tombPos != null && tombState != null) {
			tag.putLong(TAG_TOMB_POS, tombPos.asLong());
			tag.put(TAG_TOMB_STATE, NbtUtils.writeBlockState(tombState));
		}
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		rise.load(tag, "SaintRise");
		this.entityData.set(DATA_RISE_TICK, rise.elapsed());
		if (rise.active()) bossBar.setVisible(false);
		trialEnded = tag.getBoolean(TAG_TRIAL_ENDED);
		// The enrage's modifiers and effects were saved with the entity; only the record is read back.
		if (tag.contains("SaintAwakening")) awakening = tag.getString("SaintAwakening");
		if (tag.contains("SaintTombRecord")) tombRecord = tag.getString("SaintTombRecord");
		skippedChapels.clear();
		for (net.minecraft.nbt.Tag kind : tag.getList("SaintSkippedChapels", net.minecraft.nbt.Tag.TAG_STRING)) {
			skippedChapels.add(kind.getAsString());
		}
		if (tag.contains(TAG_TOMB_POS) && tag.contains(TAG_TOMB_STATE)) {
			tombPos = BlockPos.of(tag.getLong(TAG_TOMB_POS));
			tombState = NbtUtils.readBlockState(this.level().holderLookup(Registries.BLOCK),
					tag.getCompound(TAG_TOMB_STATE));
		}
	}

	/** The melee trial goals; only the stroll speed differs between Saints that use them. */
	protected void registerTrialGoals(double strollSpeed) {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, false));
		this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, strollSpeed));
		this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 24.0F));
		this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
	}

	public int getVisualState() {
		return this.entityData.get(DATA_VISUAL_STATE);
	}

	public int getVisualTicks() {
		return this.entityData.get(DATA_VISUAL_TICKS);
	}

	public void setVisualState(int visualState, int ticks) {
		this.entityData.set(DATA_VISUAL_STATE, visualState);
		this.entityData.set(DATA_VISUAL_TICKS, Math.max(0, ticks));
	}

	/** Counts the visual state down and clears it on its last tick, unless {@link #holdsVisualState} keeps it. */
	protected void tickVisualState() {
		int ticks = getVisualTicks();
		if (ticks > 0) {
			this.entityData.set(DATA_VISUAL_TICKS, ticks - 1);
			if (ticks == 1 && !holdsVisualState(getVisualState())) {
				this.entityData.set(DATA_VISUAL_STATE, VISUAL_NONE);
			}
		}
	}

	/** A state that stays after its ticks run out (Hemorath's overload). */
	protected boolean holdsVisualState(int visualState) {
		return false;
	}

	@Override
	public void startSeenByPlayer(ServerPlayer player) {
		super.startSeenByPlayer(player);
		bossBar.show(player);
	}

	@Override
	public void stopSeenByPlayer(ServerPlayer player) {
		super.stopSeenByPlayer(player);
		bossBar.hide(player);
	}

	/** A ring of single particles on the floor around the Saint: the telegraph of an area attack. */
	protected void sendFloorRing(ServerLevel server, double radius, SimpleParticleType particle, int points, double y) {
		for (int i = 0; i < points; i++) {
			double angle = i * Math.PI * 2.0D / points;
			double x = this.getX() + Math.cos(angle) * radius;
			double z = this.getZ() + Math.sin(angle) * radius;
			server.sendParticles(particle, x, y, z, 1, 0.02D, 0.02D, 0.02D, 0.0D);
		}
	}

	/** Sends a translated chat line to everyone in the fight. */
	protected void broadcastMessage(String key, ChatFormatting color) {
		broadcast(Component.translatable(key).withStyle(color));
	}

	/** Sends a chat line to everyone in the fight: by default, whoever sees the bar. */
	protected void broadcast(Component message) {
		bossBar.broadcast(message);
	}

	// ── the ending ────────────────────────────────────────────────────

	/**
	 * Remembers the sarcophagus this Saint rose from (its position and block state, before the encounter clears it),
	 * so that the ending can close the Saint back into it.
	 */
	/** How this Saint was woken: through a ring door opened properly, shattered (enraged), or neither. */
	private String awakening = "NONE";
	/** The tomb's own door record when the Saint rose (the awakening may be inferred from who woke it). */
	private String tombRecord = "NONE";
	private final java.util.List<String> skippedChapels = new java.util.ArrayList<>();

	public String awakening() {
		return awakening;
	}

	/**
	 * Wakes the Saint as its ring door was opened. A forced awakening enrages it once: harder and faster, with the
	 * deed of every skipped Chapel.
	 */
	public void setAwakening(String mode, java.util.List<String> skipped) {
		setAwakening(mode, skipped, mode);
	}

	public void setAwakening(String mode, java.util.List<String> skipped, String rawRecord) {
		awakening = mode;
		tombRecord = rawRecord;
		skippedChapels.clear();
		skippedChapels.addAll(skipped);
		if (!SaintEnrageRules.enraged(mode)) return;
		java.util.List<com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules.Kind> kinds = new java.util.ArrayList<>();
		for (String name : skipped) {
			try {
				kinds.add(com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules.Kind.valueOf(name));
			} catch (IllegalArgumentException ignored) {
				// an unknown Chapel lends nothing
			}
		}
		var add = net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;
		com.vincenthuto.hemomancy.common.util.AttributeModifiers.syncPermanent(getAttribute(
				net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE), Hemomancy.rloc("saint_enrage_damage"),
				SaintEnrageRules.damageBonus(), add, true);
		com.vincenthuto.hemomancy.common.util.AttributeModifiers.syncPermanent(getAttribute(
				net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED), Hemomancy.rloc("saint_enrage_speed"),
				SaintEnrageRules.speedBonus(kinds), add, true);
		double health = SaintEnrageRules.healthBonus(kinds);
		com.vincenthuto.hemomancy.common.util.AttributeModifiers.syncPermanent(getAttribute(
				net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH), Hemomancy.rloc("saint_enrage_fall"),
				health, add, health > 0);
		setHealth(getMaxHealth());
		if (SaintEnrageRules.resists(kinds)) {
			addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE,
					net.minecraft.world.effect.MobEffectInstance.INFINITE_DURATION, 0, false, false));
		}
		if (SaintEnrageRules.regenerates(kinds)) {
			addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.REGENERATION,
					net.minecraft.world.effect.MobEffectInstance.INFINITE_DURATION, 0, false, false));
		}
	}

	/** Puts back the enrage's lasting effects after a Saint's own mechanic clears them (Velorum's stillness break). */
	protected void reapplyEnrageEffects() {
		if (!SaintEnrageRules.enraged(awakening)) return;
		if (skippedChapels.contains("BEHOLDING")) {
			addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE,
					net.minecraft.world.effect.MobEffectInstance.INFINITE_DURATION, 0, false, false));
		}
		if (skippedChapels.contains("LITANY")) {
			addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.REGENERATION,
					net.minecraft.world.effect.MobEffectInstance.INFINITE_DURATION, 0, false, false));
		}
	}

	public void bindTomb(BlockPos pos, BlockState sarcophagus) {
		this.tombPos = pos.immutable();
		this.tombState = sarcophagus;
	}

	@Nullable
	public BlockPos getTombPos() {
		return tombPos;
	}

	/** The sarcophagus the Saint rose from, as it stood (its facing turns the chamber round it); {@code null} with no tomb. */
	@Nullable
	public BlockState getTombState() {
		return tombState;
	}

	/** True once {@link #endTrial} has run: the rewards are given and the tomb is closed. */
	public boolean hasTrialEnded() {
		return trialEnded;
	}

	/**
	 * Ends the trial, once per encounter: rolls the Saint's loot table for {@code victor} (or the nearest living player
	 * when no one is named), lets the Saint have its say, then restores its sarcophagus where it stood. Later calls,
	 * and calls on the client, do nothing. A removed or unloaded Saint never calls it.
	 */
	protected final void endTrial(@Nullable Player victor, @Nullable DamageSource cause) {
		if (trialEnded || !(this.level() instanceof ServerLevel server)) return;
		trialEnded = true;
		secondary.cancel(this);
		Player recipient = victor != null && victor.isAlive() ? victor
				: server.getNearestPlayer(this.getX(), this.getY(), this.getZ(), REWARD_RANGE,
						entity -> entity.isAlive() && !entity.isSpectator());
		rollTrialRewards(server, recipient, cause);
		announceTrialEnd(recipient);
		returnToTomb(server);
	}

	/** The Saint's own word on its ending, said before the tomb closes; {@code victor} carries the reward. */
	protected void announceTrialEnd(@Nullable Player victor) {
	}

	/** True when the rewards go into the victor's inventory (overflow drops at their feet), not onto the floor. */
	protected boolean rewardsGoToVictor() {
		return false;
	}

	/** An HP death is an ending too; a {@code /kill} only sends the Saint back to its tomb ({@link #remove}). */
	@Override
	public void die(DamageSource source) {
		boolean dying = !this.isRemoved() && !this.dead;
		super.die(source);
		if (dying && this.dead && !source.is(DamageTypes.GENERIC_KILL)) {
			endTrial(source.getEntity() instanceof Player killer ? killer : null, source);
		}
	}

	/** The loot table rolls once, in {@link #endTrial}, whatever the ending; never again as vanilla death loot. */
	@Override
	protected void dropFromLootTable(DamageSource damageSource, boolean hitByPlayer) {
	}

	private void rollTrialRewards(ServerLevel server, @Nullable Player recipient, @Nullable DamageSource cause) {
		DamageSource source = cause != null ? cause
				: recipient != null ? this.damageSources().playerAttack(recipient) : this.damageSources().generic();
		LootParams.Builder params = new LootParams.Builder(server)
				.withParameter(LootContextParams.THIS_ENTITY, this)
				.withParameter(LootContextParams.ORIGIN, this.position())
				.withParameter(LootContextParams.DAMAGE_SOURCE, source)
				.withOptionalParameter(LootContextParams.ATTACKING_ENTITY, source.getEntity())
				.withOptionalParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, source.getDirectEntity());
		if (recipient != null) {
			params = params.withParameter(LootContextParams.LAST_DAMAGE_PLAYER, recipient).withLuck(recipient.getLuck());
		}
		LootTable table = server.getServer().reloadableRegistries().getLootTable(this.getType().getDefaultLootTable());
		for (ItemStack rolled : table.getRandomItems(params.create(LootContextParamSets.ENTITY))) {
			ItemStack reward = SaintEnrageRules.fragmentOnly(awakening) ? fragmentOf(rolled) : rolled;
			// a reward handed into the inventory is never picked up, so it unlocks its Liber pages here
			if (recipient instanceof ServerPlayer player) LiberDiscoveryEvents.unlockForItem(player, reward);
			if (recipient != null && rewardsGoToVictor()) {
				if (!recipient.getInventory().add(reward)) {
					recipient.drop(reward, false);
				}
			} else {
				this.spawnAtLocation(reward);
			}
		}
	}

	/** A Saint taken out of the world without an ending (a {@code /kill}, a discard) still goes back into its tomb, unrewarded. */
	@Override
	public void remove(RemovalReason reason) {
		if (!trialEnded && reason.shouldDestroy() && this.isAddedToLevel() && this.level() instanceof ServerLevel server) {
			trialEnded = true;
			returnToTomb(server);
		}
		super.remove(reason);
	}

	/** Peaceful never clears a Saint away: its tomb is already open. */
	@Override
	public boolean shouldDespawnInPeaceful() {
		return false;
	}

	/** The tomb is remembered by position alone, so the Saint stays in its own dimension. */
	@Override
	public boolean canUsePortal(boolean allowPassengers) {
		return false;
	}

	/** A forced fight leaves only a fragment where a whole Hallowed Residuum would have been. */
	private static ItemStack fragmentOf(ItemStack rolled) {
		var id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(rolled.getItem());
		if (!id.getNamespace().equals(Hemomancy.MOD_ID) || !id.getPath().startsWith("hallowed_residuum_")
				|| id.getPath().startsWith("hallowed_residuum_fragment_")) return rolled;
		var fragment = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(Hemomancy.rloc(
				id.getPath().replace("hallowed_residuum_", "hallowed_residuum_fragment_")));
		return fragment == net.minecraft.world.item.Items.AIR ? rolled : new ItemStack(fragment, rolled.getCount());
	}

	private void returnToTomb(ServerLevel server) {
		if (tombPos == null || tombState == null) return;
		if (!SaintSarcophagusBlock.restoreDormant(server, tombPos, tombState, saintType())) {
			Hemomancy.LOGGER.warn("Saint {} could not return to its tomb at {}: the space is occupied", saintType(), tombPos);
			return;
		}
		// The door stays as it was opened: a tomb behind a shattered door wakes enraged again.
		if (server.getBlockEntity(tombPos) instanceof com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.SaintSarcophagusBlockEntity tomb
				&& SaintEnrageRules.recordToKeep(tombRecord) != null) {
			java.util.List<com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules.Kind> kinds = new java.util.ArrayList<>();
			for (String name : skippedChapels) {
				try {
					kinds.add(com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules.Kind.valueOf(name));
				} catch (IllegalArgumentException ignored) {
					// unknown names are dropped
				}
			}
			try {
				tomb.recordDoor(com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.SaintSarcophagusBlockEntity.DoorMode.valueOf(tombRecord), kinds);
			} catch (IllegalArgumentException unknown) {
				// an unreadable record is not written back
			}
		}
		broadcast(Component.translatable("message.hemomancy.saint.returns_to_tomb", saintType().getDisplayName())
				.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
	}
}
