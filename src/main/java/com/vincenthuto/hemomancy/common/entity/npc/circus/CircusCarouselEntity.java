package com.vincenthuto.hemomancy.common.entity.npc.circus;

import com.vincenthuto.hemomancy.common.entity.mob.monster.EnthralledDollEntity;
import com.vincenthuto.hemomancy.common.entity.mob.monster.BloodDrunkPuppeteerEntity;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import net.minecraft.core.BlockPos;
import com.vincenthuto.hemomancy.common.entity.boss.BossReplayClock;
import com.vincenthuto.hemomancy.common.encounter.ordeal.finale.CarouselAlive;
import com.vincenthuto.hemomancy.common.encounter.ordeal.finale.RingmasterFinaleGeometry;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

public final class CircusCarouselEntity extends Entity {
	private static final EntityDataAccessor<Boolean> ACTIVE = SynchedEntityData.defineId(
			CircusCarouselEntity.class, EntityDataSerializers.BOOLEAN);
	/**
	 * The copy carousel on his stage (2026-11-18) comes alive: whether this is the copy, the move it runs
	 * ({@link CarouselAlive.Move} ordinal) and its tick (idle ticks between moves), the turn the move started from and
	 * the turn it aims to. Its turn, reach and ride up the pole are worked out from these on both sides.
	 */
	private static final EntityDataAccessor<Boolean> COPY = SynchedEntityData.defineId(
			CircusCarouselEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Integer> ALIVE_MOVE = SynchedEntityData.defineId(
			CircusCarouselEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> ALIVE_TICK = SynchedEntityData.defineId(
			CircusCarouselEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Float> ALIVE_FROM = SynchedEntityData.defineId(
			CircusCarouselEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> ALIVE_FIRE = SynchedEntityData.defineId(
			CircusCarouselEntity.class, EntityDataSerializers.FLOAT);
	private final BossReplayClock aliveClock = new BossReplayClock(0);
	private static final double TROUPE_RANGE = 16.0D;
	private float rotationSpeed = CircusCarouselRules.targetSpeed(false);
	private int activeTicks;
	private BlockPos encounterOrigin;
	/** The carousel on the finale's stage in his Chamber (2026-11-18): scenery and his weapon, never a real site's. */
	private boolean stageCopy;
	private int missingOwnerTicks;
	/** Server only: whether this real carousel's captives went free with a kill at the kneel (its site lies in ruin). */
	private boolean captivesFreed;

	public CircusCarouselEntity(EntityType<? extends CircusCarouselEntity> type, Level level) {
		super(type, level);
		noPhysics = true;
		setNoGravity(true);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(ACTIVE, false);
		builder.define(COPY, false);
		builder.define(ALIVE_MOVE, 0);
		builder.define(ALIVE_TICK, 0);
		builder.define(ALIVE_FROM, 0.0F);
		builder.define(ALIVE_FIRE, 0.0F);
	}

	@Override
	public void tick() {
		super.tick();
		noPhysics = true;
		setNoGravity(true);
		setDeltaMovement(0.0D, 0.0D, 0.0D);
		if (level().isClientSide) {
			if (stageCopy()) {
				aliveClock.sync(entityData.get(ALIVE_MOVE), entityData.get(ALIVE_TICK), tickCount, 1);
				setYRot(Mth.wrapDegrees(aliveYaw(aliveTime(0.0F))));
				return;
			}
			rotationSpeed = CircusCarouselRules.nextSpeed(rotationSpeed, isActive());
			setYRot(Mth.wrapDegrees(getYRot() + rotationSpeed));
			return;
		}
		if (encounterOrigin == null) encounterOrigin = blockPosition();
		if (stageCopy) {
			// A copy no finale owns (a crash's, loaded after its stage was cleared) removes itself with its riders.
			if (!com.vincenthuto.hemomancy.common.encounter.ordeal.finale.RingmasterFinaleDirector.ownsCopy(this)) {
				java.util.List.copyOf(getPassengers()).forEach(net.minecraft.world.entity.Entity::discard);
				discard();
				return;
			}
			// Between moves it turns idly on its own clock; in a move the Ringmaster's moves set its tick.
			if (aliveMove() == CarouselAlive.Move.NONE) entityData.set(ALIVE_TICK, entityData.get(ALIVE_TICK) + 1);
			setYRot(Mth.wrapDegrees(aliveYaw(entityData.get(ALIVE_TICK))));
			ensureCaptives((ServerLevel) level());
			return;
		}
		// While the finale runs in his Chamber the real carousel stands as it is: nothing of the site moves.
		if (com.vincenthuto.hemomancy.common.encounter.ordeal.finale.RingmasterFinaleDirector.holds(level().dimension(), encounterOrigin))
			return;
		syncEncounterState((ServerLevel) level());
		if (captivesFreed) List.copyOf(getPassengers()).forEach(Entity::discard);
		else ensureCaptives((ServerLevel) level());

		List<CircusPerformerEntity> troupe = level().getEntitiesOfClass(CircusPerformerEntity.class,
				getBoundingBox().inflate(TROUPE_RANGE));
		boolean active = CircusCarouselRules.shouldActivate((int) troupe.stream()
				.filter(performer -> performer.getActState() == CircusPerformerEntity.ActState.ALERT)
				.count());
		if (active != isActive()) {
			entityData.set(ACTIVE, active);
			activeTicks = 0;
			level().playSound(null, blockPosition(), SoundEvents.CHAIN_HIT, SoundSource.HOSTILE,
					active ? 1.2F : 0.7F, active ? 0.65F : 1.0F);
		}

		rotationSpeed = CircusCarouselRules.nextSpeed(rotationSpeed, active);
		setYRot(Mth.wrapDegrees(getYRot() + rotationSpeed));
		if (active) {
			activeTicks++;
			if (activeTicks % 5 == 0) spawnHorseParticles((ServerLevel) level());
			if (CircusCarouselRules.canStrike(activeTicks)) strikeThreats(troupe);
		} else if (tickCount % 120 == 0) {
			level().playSound(null, blockPosition(), SoundInit.ENTITY_ENTHRALLED_DOLL_AMBIENT.get(),
					SoundSource.NEUTRAL, 0.45F, 0.7F);
		}
	}

	private void ensureCaptives(ServerLevel level) {
		for (int horse = 0; horse < 3; horse++) {
			if (captive(horse) != null) continue;
			BloodDrunkPuppeteerEntity captive = EntityInit.blood_drunk_puppeteer.get().create(level);
			if (captive == null) continue;
			captive.bindToCarousel(horse);
			captive.setPos(position());
			level.addFreshEntity(captive);
			if (!captive.startRiding(this, true)) captive.discard();
		}
	}

	private BloodDrunkPuppeteerEntity captive(int horse) {
		return getPassengers().stream()
				.filter(BloodDrunkPuppeteerEntity.class::isInstance)
				.map(BloodDrunkPuppeteerEntity.class::cast)
				.filter(captive -> captive.getCarouselHorse() == horse)
				.findFirst().orElse(null);
	}

	@Override
	protected void positionRider(Entity passenger, Entity.MoveFunction callback) {
		if (!(passenger instanceof BloodDrunkPuppeteerEntity captive) || !hasPassenger(passenger)) {
			super.positionRider(passenger, callback);
			return;
		}
		CircusCarouselRules.HorsePose pose = CircusCarouselRules.horsePose(getYRot(), captive.getCarouselHorse());
		// A living carousel flings its horses out, and the captives ride them.
		float t = aliveTime(0.0F);
		double reach = CarouselAlive.reach(aliveMove(), t);
		double riderRadius = 1.0D + 0.55D / CircusCarouselRules.HORSE_RADIUS
				+ (reach - CircusCarouselRules.HORSE_RADIUS) / CircusCarouselRules.HORSE_RADIUS;
		double rise = CarouselAlive.lift(aliveMove(), t) - CarouselAlive.drop(reach);
		callback.accept(passenger, getX() + pose.x() * riderRadius,
				getY() + 2.55D + pose.bob() + rise, getZ() + pose.z() * riderRadius);
		float yaw = pose.angleDegrees() + 90.0F;
		captive.setYRot(yaw);
		captive.setYBodyRot(yaw);
		captive.setYHeadRot(yaw);
	}

	/**
	 * The site's state as it bears on the carousel: a ruined site's captives are free (the kill at the kneel), and an act
	 * whose holder has left the ring for longer than the arena's grace is released.
	 */
	private void syncEncounterState(ServerLevel level) {
		CircusPavilionSavedData data = CircusPavilionSavedData.get(level);
		CircusPavilionSavedData.Site site = data.site(level, encounterOrigin);
		captivesFreed = site.outcome() == CircusPavilionStateRules.Outcome.RUIN;
		if (site.activeOwner() == null) {
			missingOwnerTicks = 0;
			return;
		}
		CircusArena arena = CircusArena.of(encounterOrigin);
		Player owner = level.getPlayerByUUID(site.activeOwner());
		if (owner != null && owner.isAlive() && owner.level() == level && arena.holds(owner.position())) {
			missingOwnerTicks = 0;
		} else if (++missingOwnerTicks >= arena.graceTicks()) {
			data.reset(level, encounterOrigin, site.activeOwner());
			missingOwnerTicks = 0;
		}
	}

	private void strikeThreats(List<CircusPerformerEntity> troupe) {
		for (CircusPerformerEntity performer : troupe) {
			LivingEntity target = performer.getTarget();
			if (target == null || !target.isAlive() || target instanceof CircusPerformerEntity
					|| target instanceof EnthralledDollEntity doll && doll.isOwnedByCircusPerformer()) continue;
			for (int horse = 0; horse < 3; horse++) {
				CircusCarouselRules.HorsePose pose = CircusCarouselRules.horsePose(getYRot(), horse);
				double x = getX() + pose.x();
				double y = getY() + 1.1D + pose.bob();
				double z = getZ() + pose.z();
				if (target.getBoundingBox().intersects(new AABB(x - 0.9D, y, z - 0.9D,
						x + 0.9D, y + 2.4D, z + 0.9D))
						&& target.hurt(damageSources().mobAttack(performer), 4.0F)) {
					target.knockback(0.9D, getX() - target.getX(), getZ() - target.getZ());
					level().playSound(null, target.blockPosition(), SoundEvents.IRON_GOLEM_ATTACK,
							SoundSource.HOSTILE, 0.8F, 1.25F);
				}
			}
		}
	}

	private void spawnHorseParticles(ServerLevel level) {
		for (int horse = 0; horse < 3; horse++) {
			CircusCarouselRules.HorsePose pose = CircusCarouselRules.horsePose(getYRot(), horse);
			// Audit E: the spore sprite read as large pink crystals at this size; fine crimson glow motes instead.
			level.sendParticles(HemoParticleData.glow(CircusEffects.CRIMSON), getX() + pose.x(),
					getY() + 2.0D + pose.bob(), getZ() + pose.z(), 2, 0.35D, 0.5D, 0.35D, 0.01D);
		}
	}

	public boolean isActive() {
		return entityData.get(ACTIVE);
	}

	public void markStageCopy() {
		stageCopy = true;
		entityData.set(COPY, true);
		entityData.set(ALIVE_FROM, getYRot());
	}

	public boolean stageCopy() {
		return level().isClientSide ? entityData.get(COPY) : stageCopy;
	}

	/** The move the living copy runs; NONE between moves and on every real carousel. */
	public CarouselAlive.Move aliveMove() {
		return CarouselAlive.Move.byId(entityData.get(ALIVE_MOVE));
	}

	/** Ticks into the running move (or the idle ticks): synced on the server, replayed on the client clock. */
	public float aliveTime(float partialTick) {
		return level().isClientSide ? aliveClock.at(tickCount, partialTick) : entityData.get(ALIVE_TICK);
	}

	public float aliveFromYaw() {
		return entityData.get(ALIVE_FROM);
	}

	public float aliveFireYaw() {
		return entityData.get(ALIVE_FIRE);
	}

	/** Its turn {@code t} ticks into the running move (or idle). */
	public float aliveYaw(float t) {
		return CarouselAlive.yaw(aliveMove(), t, aliveFromYaw(), aliveFireYaw());
	}

	/** Starts {@code move} from where it stands now, aiming its horses at {@code fire} (a turn). */
	public void beginAlive(CarouselAlive.Move move, float fire) {
		entityData.set(ALIVE_FROM, aliveYaw(entityData.get(ALIVE_TICK)));
		entityData.set(ALIVE_FIRE, fire);
		entityData.set(ALIVE_MOVE, move.ordinal());
		entityData.set(ALIVE_TICK, 0);
	}

	/** The running move's tick, set by the Ringmaster's moves each tick. */
	public void tickAlive(int tick) {
		entityData.set(ALIVE_TICK, tick);
	}

	/** The move is over: it goes back to turning idly from where it stopped. */
	public void endAlive() {
		if (aliveMove() == CarouselAlive.Move.NONE) return;
		entityData.set(ALIVE_FROM, Mth.wrapDegrees(aliveYaw(entityData.get(ALIVE_TICK))));
		entityData.set(ALIVE_MOVE, CarouselAlive.Move.NONE.ordinal());
		entityData.set(ALIVE_TICK, 0);
	}

	public BlockPos encounterOrigin() {
		return encounterOrigin == null ? blockPosition() : encounterOrigin;
	}

	public AABB getCarouselRenderBounds() {
		// Flung out, its horses run to the curb.
		if (stageCopy()) return getBoundingBox().inflate(RingmasterFinaleGeometry.SPIRAL_TO + 2.0D, 4.0D, RingmasterFinaleGeometry.SPIRAL_TO + 2.0D);
		return getBoundingBox().inflate(3.5D, 0.5D, 3.5D);
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		entityData.set(ACTIVE, false);
		activeTicks = 0;
		rotationSpeed = CircusCarouselRules.targetSpeed(false);
		encounterOrigin = tag.contains("EncounterOrigin") ? BlockPos.of(tag.getLong("EncounterOrigin")) : blockPosition();
		stageCopy = tag.getBoolean("StageCopy");
		entityData.set(COPY, stageCopy);
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		tag.putLong("EncounterOrigin", encounterOrigin().asLong());
		if (stageCopy) tag.putBoolean("StageCopy", true);
	}

	/** Nothing of the Procession is struck: its captives went with the old finale's severing (2026-11-18). */
	@Override
	public boolean isPickable() {
		return false;
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	public boolean isAttackable() {
		return false;
	}

	@Override
	public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
		return false;
	}
}
