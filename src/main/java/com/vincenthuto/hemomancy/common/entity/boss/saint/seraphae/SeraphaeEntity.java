package com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae;

import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Saint Seraphae — The Bound Radiance.
 * <p>
 * A self-fracturing entity that cannot be killed through conventional damage.
 * Victory is achieved by raising containment integrity to 100 through
 * containment actions while managing fragment pressure.
 * <p>
 * The boss bar displays containment integrity (not health). Health is set very
 * high and the boss is effectively damage-immune during normal play — the win
 * condition is solely integrity-based.
 */
public class SeraphaeEntity extends Monster {

	// ── synched data ──────────────────────────────────────────────────
	private static final EntityDataAccessor<Integer> DATA_STATE =
			SynchedEntityData.defineId(SeraphaeEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Float> DATA_INTEGRITY =
			SynchedEntityData.defineId(SeraphaeEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Integer> DATA_STATE_TIMER =
			SynchedEntityData.defineId(SeraphaeEntity.class, EntityDataSerializers.INT);

	// ── containment integrity ─────────────────────────────────────────
	private static final float INITIAL_INTEGRITY = 50.0F;
	private static final float MAX_INTEGRITY = 100.0F;
	private static final float MIN_INTEGRITY = 0.0F;

	/** Passive integrity drain per tick. */
	private static final float BASE_DECAY_PER_TICK = 0.01F;
	/** Extra decay per living fragment. */
	private static final float FRAGMENT_DECAY_PER_TICK = 0.008F;
	/** Integrity gained when a fragment is contained (direct kill). */
	private static final float FRAGMENT_CONTAIN_GAIN = 3.0F;
	/** Integrity gained when a fragment is contained via an anchor. */
	private static final float ANCHOR_CONTAIN_GAIN = 5.0F;
	/** Large integrity burst during successful CONDENSING window. */
	private static final float CONDENSE_BURST_GAIN = 20.0F;
	/** Integrity gained from direct hits while the Chain Saint is condensing. */
	private static final float CONDENSING_HIT_GAIN = 4.0F;

	// ── state timing ──────────────────────────────────────────────────
	/** Ticks Seraphae stays STABLE before fracturing. */
	private static final int STABLE_DURATION = 200;
	/** Ticks FRACTURING lasts before going DISPERSED. */
	private static final int FRACTURING_DURATION = 100;
	/** Ticks DISPERSED lasts before CONDENSING. */
	private static final int DISPERSED_DURATION = 160;
	/** Ticks CONDENSING window lasts. */
	private static final int CONDENSING_DURATION = 80;

	/** Number of fragments spawned per FRACTURING cycle. */
	private static final int MIN_FRAGMENTS = 3;
	private static final int MAX_FRAGMENTS = 6;
	/** Maximum fragments when escalation is active. */
	private static final int MAX_ESCALATED_FRAGMENTS = 8;

	/** Number of containment anchors placed on encounter start. */
	private static final int ANCHOR_COUNT = 4;
	/** Radius for anchor placement around the boss spawn. */
	private static final double ANCHOR_RADIUS = 10.0;

	/** Arena radius for fragment/anchor detection. */
	private static final double ARENA_RADIUS = 32.0;

	// ── escalation thresholds ─────────────────────────────────────────
	/** Below this integrity, spawn rate & hazards increase. */
	private static final float ESCALATION_THRESHOLD = 25.0F;
	/** Chance per player per tick-scan of receiving wisp damage in DISPERSED state. */
	private static final float DISPERSED_DAMAGE_CHANCE = 0.3F;

	// ── animation states ──────────────────────────────────────────────
	public final AnimationState idleAnimationState = new AnimationState();
	public final AnimationState fracturingAnimationState = new AnimationState();
	public final AnimationState condensingAnimationState = new AnimationState();

	// ── server-side fields (not synched beyond DATA_*) ────────────────
	private int stateTimer = STABLE_DURATION;
	private boolean anchorsPlaced;
	private boolean defeated;

	private final ServerBossEvent bossEvent = new ServerBossEvent(
			Component.translatable("entity.hemomancy.seraphae"),
			BossEvent.BossBarColor.YELLOW,
			BossEvent.BossBarOverlay.NOTCHED_10);

	// ── constructor ───────────────────────────────────────────────────

	public SeraphaeEntity(EntityType<? extends SeraphaeEntity> type, Level level) {
		super(type, level);
		this.setPersistenceRequired();
		this.xpReward = 120;
	}

	// ── attributes ────────────────────────────────────────────────────

	public static AttributeSupplier.Builder setAttributes() {
		return Monster.createMonsterAttributes()
				.add(Attributes.MAX_HEALTH, 500.0D) // damage is cosmetic only - win condition is integrity-based
				.add(Attributes.MOVEMENT_SPEED, 0.22D)
				.add(Attributes.ATTACK_DAMAGE, 4.0D)
				.add(Attributes.FOLLOW_RANGE, 48.0D)
				.add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
				.add(Attributes.ARMOR, 20.0D);
	}

	// ── goals ─────────────────────────────────────────────────────────

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.6D));
		this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 16.0F));
		this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
	}

	// ── synched data ──────────────────────────────────────────────────

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_STATE, SeraphaeState.STABLE.ordinal());
		builder.define(DATA_INTEGRITY, INITIAL_INTEGRITY);
		builder.define(DATA_STATE_TIMER, STABLE_DURATION);
	}

	public SeraphaeState getSeraphaeState() {
		return SeraphaeState.fromId(this.entityData.get(DATA_STATE));
	}

	private void setSeraphaeState(SeraphaeState state) {
		this.entityData.set(DATA_STATE, state.ordinal());
	}

	public int getStateTimer() {
		return this.entityData.get(DATA_STATE_TIMER);
	}

	private void setStateTimer(int ticks) {
		stateTimer = ticks;
		this.entityData.set(DATA_STATE_TIMER, ticks);
	}

	public float getContainmentIntegrity() {
		return this.entityData.get(DATA_INTEGRITY);
	}

	private void setContainmentIntegrity(float value) {
		this.entityData.set(DATA_INTEGRITY, Math.max(MIN_INTEGRITY, Math.min(MAX_INTEGRITY, value)));
	}

	// ── damage immunity ───────────────────────────────────────────────

	@Override
	public boolean hurt(DamageSource source, float amount) {
		// Seraphae cannot be conventionally killed — absorb damage but don't
		// let health reach 0. Fragments are the mechanic, not DPS.
		boolean condensingPlayerHit = !this.level().isClientSide && getSeraphaeState() == SeraphaeState.CONDENSING
				&& source.getEntity() instanceof Player;
		if (!this.level().isClientSide && this.getHealth() - amount <= 1.0F) {
			if (condensingPlayerHit) {
				recordCondensingHit();
			}
			this.setHealth(1.0F);
			return false;
		}
		boolean hurt = super.hurt(source, amount);
		if (hurt && condensingPlayerHit) {
			recordCondensingHit();
		}
		return hurt;
	}

	private void recordCondensingHit() {
		setContainmentIntegrity(getContainmentIntegrity() + CONDENSING_HIT_GAIN);
		if (this.level() instanceof ServerLevel server) {
			server.sendParticles(ParticleTypes.END_ROD,
					this.getX(), this.getY() + 1.5, this.getZ(),
					8, 0.5, 0.8, 0.5, 0.03);
		}
	}

	// ── main tick ─────────────────────────────────────────────────────

	@Override
	public void aiStep() {
		super.aiStep();

		if (this.level().isClientSide || defeated) return;

		ServerLevel server = (ServerLevel) this.level();

		// One-time anchor placement
		if (!anchorsPlaced) {
			placeAnchors(server);
			anchorsPlaced = true;
		}

		// ── containment decay ──
		float integrity = getContainmentIntegrity();
		float decay = BASE_DECAY_PER_TICK;

		// Extra decay per living fragment
		int livingFragments = countLivingFragments(server);
		decay += livingFragments * FRAGMENT_DECAY_PER_TICK;

		integrity -= decay;
		setContainmentIntegrity(integrity);

		// ── win condition ──
		if (getContainmentIntegrity() >= MAX_INTEGRITY) {
			onContainmentComplete(server);
			return;
		}

		// ── failure escalation ──
		if (getContainmentIntegrity() <= MIN_INTEGRITY) {
			onArenaOverrun(server);
		}

		// ── state machine ──
		stateTimer--;
		this.entityData.set(DATA_STATE_TIMER, stateTimer);
		if (stateTimer <= 0) {
			advanceState(server);
		}

		// Per-state behaviour
		tickState(server);

		// Boss bar shows integrity progress
		bossEvent.setProgress(getContainmentIntegrity() / MAX_INTEGRITY);
		updateBossBarColor();
	}

	// ── state machine ─────────────────────────────────────────────────

	private void advanceState(ServerLevel server) {
		SeraphaeState current = getSeraphaeState();
		switch (current) {
			case STABLE -> enterFracturing(server);
			case FRACTURING -> enterDispersed(server);
			case DISPERSED -> enterCondensing(server);
			case CONDENSING -> enterStable(server);
		}
	}

	private void enterStable(ServerLevel server) {
		// Coming from CONDENSING — award the burst integrity gain
		if (getSeraphaeState() == SeraphaeState.CONDENSING) {
			setContainmentIntegrity(getContainmentIntegrity() + CONDENSE_BURST_GAIN);
			server.sendParticles(ParticleTypes.FLASH,
					this.getX(), this.getY() + 1.5, this.getZ(),
					3, 0.0, 0.0, 0.0, 0.0);
			broadcastMessage("Condensing complete — containment strengthened!",
					ChatFormatting.GREEN);
		}

		setSeraphaeState(SeraphaeState.STABLE);
		setStateTimer(STABLE_DURATION);
		this.setInvisible(false);

		server.playSound(null, this.getX(), this.getY(), this.getZ(),
				SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 2.0F, 1.2F);
		broadcastMessage("Seraphae stabilizes... briefly.", ChatFormatting.GOLD);
	}

	private void enterFracturing(ServerLevel server) {
		setSeraphaeState(SeraphaeState.FRACTURING);
		setStateTimer(FRACTURING_DURATION);

		int count = MIN_FRAGMENTS + server.random.nextInt(MAX_FRAGMENTS - MIN_FRAGMENTS + 1);
		// Escalation: more fragments when integrity is low
		if (getContainmentIntegrity() < ESCALATION_THRESHOLD) {
			count = Math.min(count + 2, MAX_ESCALATED_FRAGMENTS);
		}
		spawnFragments(server, count);

		server.playSound(null, this.getX(), this.getY(), this.getZ(),
				SoundEvents.GLASS_BREAK, SoundSource.HOSTILE, 2.0F, 0.6F);
		server.sendParticles(ParticleTypes.END_ROD,
				this.getX(), this.getY() + 1.0, this.getZ(),
				40, 1.0, 1.2, 1.0, 0.15);

		broadcastMessage("Seraphae, the Chain Saint, fractures! Bind the fragments!", ChatFormatting.RED);
	}

	private void enterDispersed(ServerLevel server) {
		setSeraphaeState(SeraphaeState.DISPERSED);
		setStateTimer(DISPERSED_DURATION);
		this.setInvisible(true); // untargetable

		server.sendParticles(ParticleTypes.FLASH,
				this.getX(), this.getY() + 1.0, this.getZ(),
				3, 0.0, 0.0, 0.0, 0.0);

		broadcastMessage("Seraphae disperses into radiant wisps...", ChatFormatting.YELLOW);
	}

	private void enterCondensing(ServerLevel server) {
		setSeraphaeState(SeraphaeState.CONDENSING);
		setStateTimer(CONDENSING_DURATION);
		this.setInvisible(false);

		server.playSound(null, this.getX(), this.getY(), this.getZ(),
				SoundEvents.BEACON_DEACTIVATE, SoundSource.HOSTILE, 2.0F, 0.8F);

		broadcastMessage("The chains draw Seraphae inward - strike now for containment!", ChatFormatting.GREEN);
	}

	// ── per-state tick behaviour ──────────────────────────────────────

	private void tickState(ServerLevel server) {
		switch (getSeraphaeState()) {
			case STABLE -> tickStable(server);
			case FRACTURING -> tickFracturing(server);
			case DISPERSED -> tickDispersed(server);
			case CONDENSING -> tickCondensing(server);
		}
	}

	private void tickStable(ServerLevel server) {
		// Gentle ambient particles
		if (this.tickCount % 15 == 0) {
			server.sendParticles(ParticleTypes.END_ROD,
					this.getX(), this.getY() + 1.5, this.getZ(),
					3, 0.3, 0.4, 0.3, 0.01);
		}
	}

	private void tickFracturing(ServerLevel server) {
		// Pulsing disruption particles
		if (this.tickCount % 8 == 0) {
			float urgency = 1.0F - Math.max(0.0F, Math.min(1.0F, stateTimer / (float) FRACTURING_DURATION));
			server.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
					this.getX(), this.getY() + 1.0, this.getZ(),
					8 + (int) (8 * urgency), 0.6 + urgency * 0.4, 0.8, 0.6 + urgency * 0.4, 0.04);
		}
	}

	private void tickDispersed(ServerLevel server) {
		// Environmental hazard wisps — deal minor damage to nearby players
		if (this.tickCount % 30 == 0) {
			for (Player player : server.getEntitiesOfClass(Player.class,
					this.getBoundingBox().inflate(ARENA_RADIUS))) {
				// Random wisp damage from the dispersed radiance
				if (server.random.nextFloat() < DISPERSED_DAMAGE_CHANCE) {
					player.hurt(this.damageSources().magic(), 1.5F);
					server.sendParticles(ParticleTypes.END_ROD,
							player.getX(), player.getY() + 1.0, player.getZ(),
							6, 0.3, 0.5, 0.3, 0.05);
				}
			}
		}
		// Floating wisp particles near the boss location
		if (this.tickCount % 5 == 0) {
			double angle = (this.tickCount * 0.1) % (2 * Math.PI);
			double wispX = this.getX() + Math.cos(angle) * 5.0;
			double wispZ = this.getZ() + Math.sin(angle) * 5.0;
			server.sendParticles(ParticleTypes.END_ROD,
					wispX, this.getY() + 1.5, wispZ,
					2, 0.1, 0.2, 0.1, 0.01);
		}
	}

	private void tickCondensing(ServerLevel server) {
		// Converging particles toward the boss
		if (this.tickCount % 4 == 0) {
			for (int i = 0; i < 6; i++) {
				double angle = server.random.nextDouble() * 2 * Math.PI;
				double dist = 3.0 + server.random.nextDouble() * 4.0;
				double px = this.getX() + Math.cos(angle) * dist;
				double pz = this.getZ() + Math.sin(angle) * dist;
				server.sendParticles(ParticleTypes.END_ROD,
						px, this.getY() + 0.5 + server.random.nextDouble() * 2.0, pz,
						1, 0.0, 0.0, 0.0, 0.0);
			}
			for (ContainmentAnchorEntity anchor : server.getEntitiesOfClass(
					ContainmentAnchorEntity.class,
					new AABB(this.blockPosition()).inflate(ARENA_RADIUS),
					a -> this.getUUID().equals(a.getParentId()))) {
				sendContainmentLine(server, anchor.position().add(0.0, 0.8, 0.0), this.position().add(0.0, 1.3, 0.0));
			}
		}

		// Award condense burst when window closes (handled in advanceState)
		// Player can deal damage during this window; each hit adds small integrity
	}

	private void sendContainmentLine(ServerLevel server, Vec3 from, Vec3 to) {
		Vec3 delta = to.subtract(from);
		for (int i = 1; i <= 8; i++) {
			Vec3 point = from.add(delta.scale(i / 9.0D));
			server.sendParticles(ParticleTypes.END_ROD,
					point.x, point.y, point.z,
					1, 0.025, 0.025, 0.025, 0.0);
		}
	}

	@Override
	public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
		// Seraphae doesn't melee in DISPERSED state
		if (getSeraphaeState() == SeraphaeState.DISPERSED) return false;
		return super.doHurtTarget(target);
	}

	// ── fragment management ───────────────────────────────────────────

	private void spawnFragments(ServerLevel server, int count) {
		for (int i = 0; i < count; i++) {
			SeraphaeFragmentEntity fragment = EntityInit.seraphae_fragment.get().create(server);
			if (fragment == null) continue;

			double angle = (i * 2.0 * Math.PI / count) + server.random.nextFloat() * 0.5;
			double spawnX = this.getX() + Math.cos(angle) * 4.0;
			double spawnZ = this.getZ() + Math.sin(angle) * 4.0;

			fragment.moveTo(spawnX, this.getY(), spawnZ,
					server.random.nextFloat() * 360.0F, 0.0F);
			fragment.finalizeSpawn((ServerLevelAccessor) server,
					server.getCurrentDifficultyAt(this.blockPosition()),
					MobSpawnType.TRIGGERED, null);
			fragment.setParentId(this.getUUID());
			fragment.setPersistenceRequired();

			// Target nearest player
			Player nearest = server.getNearestPlayer(this, ARENA_RADIUS);
			if (nearest != null) {
				fragment.setTarget(nearest);
			}

			server.addFreshEntity(fragment);
		}
	}

	private int countLivingFragments(ServerLevel server) {
		return server.getEntitiesOfClass(SeraphaeFragmentEntity.class,
				new AABB(this.blockPosition()).inflate(ARENA_RADIUS),
				f -> f.isAlive() && !f.isContained()
						&& this.getUUID().equals(f.getParentId())).size();
	}

	/**
	 * Called when a fragment belonging to this Seraphae is contained.
	 *
	 * @param anchorAssisted true if captured by a ContainmentAnchor (bonus gain)
	 */
	public void onFragmentContained(boolean anchorAssisted) {
		float gain = anchorAssisted ? ANCHOR_CONTAIN_GAIN : FRAGMENT_CONTAIN_GAIN;
		setContainmentIntegrity(getContainmentIntegrity() + gain);

		if (this.level() instanceof ServerLevel server) {
			server.sendParticles(ParticleTypes.HAPPY_VILLAGER,
					this.getX(), this.getY() + 2.0, this.getZ(),
					10, 0.5, 0.5, 0.5, 0.02);
		}
	}

	// ── anchor placement ──────────────────────────────────────────────

	private void placeAnchors(ServerLevel server) {
		for (int i = 0; i < ANCHOR_COUNT; i++) {
			ContainmentAnchorEntity anchor = EntityInit.containment_anchor.get().create(server);
			if (anchor == null) continue;

			double angle = i * 2.0 * Math.PI / ANCHOR_COUNT;
			double ax = this.getX() + Math.cos(angle) * ANCHOR_RADIUS;
			double az = this.getZ() + Math.sin(angle) * ANCHOR_RADIUS;

			anchor.moveTo(ax, this.getY(), az, 0, 0);
			anchor.setParentId(this.getUUID());
			server.addFreshEntity(anchor);
		}
	}

	// ── win / lose conditions ─────────────────────────────────────────

	private void onContainmentComplete(ServerLevel server) {
		defeated = true;

		server.playSound(null, this.getX(), this.getY(), this.getZ(),
				SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.HOSTILE, 2.0F, 1.0F);
		server.sendParticles(ParticleTypes.FLASH,
				this.getX(), this.getY() + 1.5, this.getZ(),
				5, 0.0, 0.0, 0.0, 0.0);
		server.sendParticles(ParticleTypes.END_ROD,
				this.getX(), this.getY() + 1.0, this.getZ(),
				80, 2.0, 2.0, 2.0, 0.2);

		broadcastMessage("Seraphae is chained once more. The radiance is contained.",
				ChatFormatting.GOLD);

		// Drop reward to nearest player
		Player nearest = server.getNearestPlayer(this, ARENA_RADIUS);
		if (nearest != null) {
			ItemStack drop = new ItemStack(ItemInit.hallowed_residuum_seraphae.get());
			if (!nearest.getInventory().add(drop)) {
				nearest.drop(drop, false);
			}
		}

		// Clean up anchors
		for (ContainmentAnchorEntity anchor : server.getEntitiesOfClass(
				ContainmentAnchorEntity.class,
				new AABB(this.blockPosition()).inflate(ARENA_RADIUS),
				a -> this.getUUID().equals(a.getParentId()))) {
			anchor.discard();
		}
		// Clean up remaining fragments
		for (SeraphaeFragmentEntity frag : server.getEntitiesOfClass(
				SeraphaeFragmentEntity.class,
				new AABB(this.blockPosition()).inflate(ARENA_RADIUS),
				f -> this.getUUID().equals(f.getParentId()))) {
			frag.discard();
		}

		this.discard();
	}

	private void onArenaOverrun(ServerLevel server) {
		// Soft fail — massive pressure, but not instant death
		broadcastMessage("Containment failing! Radiance overflows!",
				ChatFormatting.DARK_RED);

		// Deal area damage
		for (Player player : server.getEntitiesOfClass(Player.class,
				this.getBoundingBox().inflate(ARENA_RADIUS))) {
			player.hurt(this.damageSources().magic(), 4.0F);
		}

		// Reset integrity to a small value to allow recovery
		setContainmentIntegrity(5.0F);
	}

	// ── boss bar ──────────────────────────────────────────────────────

	private void updateBossBarColor() {
		float integrity = getContainmentIntegrity();
		if (integrity >= 75.0F) {
			bossEvent.setColor(BossEvent.BossBarColor.GREEN);
		} else if (integrity >= 40.0F) {
			bossEvent.setColor(BossEvent.BossBarColor.YELLOW);
		} else {
			bossEvent.setColor(BossEvent.BossBarColor.RED);
		}
	}

	// ── client tick ───────────────────────────────────────────────────

	@Override
	public void tick() {
		super.tick();
		if (this.level().isClientSide) {
			this.idleAnimationState.startIfStopped(this.tickCount);
			SeraphaeState state = getSeraphaeState();
			if (state == SeraphaeState.FRACTURING) {
				this.fracturingAnimationState.startIfStopped(this.tickCount);
			} else {
				this.fracturingAnimationState.stop();
			}
			if (state == SeraphaeState.CONDENSING) {
				this.condensingAnimationState.startIfStopped(this.tickCount);
			} else {
				this.condensingAnimationState.stop();
			}
		}
	}

	// ── boss bar tracking ─────────────────────────────────────────────

	@Override
	public void startSeenByPlayer(ServerPlayer player) {
		super.startSeenByPlayer(player);
		bossEvent.addPlayer(player);
		player.displayClientMessage(
				Component.literal("Seraphae, the Chain Saint, stirs. Maintain containment.")
						.withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC),
				false);
	}

	@Override
	public void stopSeenByPlayer(ServerPlayer player) {
		super.stopSeenByPlayer(player);
		bossEvent.removePlayer(player);
	}

	// ── save / load ───────────────────────────────────────────────────

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putFloat("ContainmentIntegrity", getContainmentIntegrity());
		tag.putInt("SeraphaeState", getSeraphaeState().ordinal());
		tag.putInt("StateTimer", stateTimer);
		tag.putBoolean("AnchorsPlaced", anchorsPlaced);
		tag.putBoolean("Defeated", defeated);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		if (tag.contains("ContainmentIntegrity")) {
			setContainmentIntegrity(tag.getFloat("ContainmentIntegrity"));
		}
		if (tag.contains("SeraphaeState")) {
			setSeraphaeState(SeraphaeState.fromId(tag.getInt("SeraphaeState")));
		}
		if (tag.contains("StateTimer")) {
			setStateTimer(tag.getInt("StateTimer"));
		}
		anchorsPlaced = tag.getBoolean("AnchorsPlaced");
		defeated = tag.getBoolean("Defeated");
	}

	// ── misc overrides ────────────────────────────────────────────────

	@Override
	public boolean removeWhenFarAway(double dist) {
		return false;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.AMETHYST_BLOCK_CHIME;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.AMETHYST_CLUSTER_BREAK;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.BEACON_DEACTIVATE;
	}

	// ── utility ───────────────────────────────────────────────────────

	private void broadcastMessage(String text, ChatFormatting color) {
		if (!(this.level() instanceof ServerLevel server)) return;
		for (Player player : server.getEntitiesOfClass(Player.class,
				this.getBoundingBox().inflate(ARENA_RADIUS))) {
			player.displayClientMessage(
					Component.literal(text).withStyle(color, ChatFormatting.BOLD),
					false);
		}
	}
}
