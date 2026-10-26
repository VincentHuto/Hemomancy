package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.entity.boss.BossTelegraphs;
import com.vincenthuto.hemomancy.common.entity.mob.animal.FunglingEntity;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * What the Spawn Mother leaves lying in the Nursery: a nectar pool where a glob burst, a spore cloud vented behind her,
 * a lattice net settling over the player, and the binding tendrils of the Last Brood. Each goes with her: a hazard
 * whose owner is gone, dead or yielding removes itself. The client draws every kind; this keeps time and applies it.
 */
public class MycophantHazardEntity extends Entity {
	public enum Kind { NECTAR_POOL, SPORE_CLOUD, LATTICE_NET, BINDING_TENDRIL }

	private static final EntityDataAccessor<Byte> KIND = SynchedEntityData.defineId(MycophantHazardEntity.class, EntityDataSerializers.BYTE);
	private static final EntityDataAccessor<Float> RADIUS = SynchedEntityData.defineId(MycophantHazardEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Integer> LIFE = SynchedEntityData.defineId(MycophantHazardEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Long> BORN = SynchedEntityData.defineId(MycophantHazardEntity.class, EntityDataSerializers.LONG);
	private static final EntityDataAccessor<Integer> OWNER_ID = SynchedEntityData.defineId(MycophantHazardEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> LAND_DELAY = SynchedEntityData.defineId(MycophantHazardEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Vector3f> END = SynchedEntityData.defineId(MycophantHazardEntity.class, EntityDataSerializers.VECTOR3);
	private static final EntityDataAccessor<Integer> HITS = SynchedEntityData.defineId(MycophantHazardEntity.class, EntityDataSerializers.INT);

	private final Set<UUID> caught = new HashSet<>();

	public MycophantHazardEntity(EntityType<? extends MycophantHazardEntity> type, Level level) {
		super(type, level);
		this.noPhysics = true;
	}

	// ------------------------------------------------------------------ making them

	/** A nectar pool where a glob burst: it slows and, in an owned fight, feeds the nectar pressure. */
	public static MycophantHazardEntity pool(ServerLevel level, Vec3 at, MycophantEntity owner) {
		return spawn(level, at, owner, Kind.NECTAR_POOL, MycophantHazardRules.POOL_RADIUS, MycophantHazardRules.POOL_LIFE, 0);
	}

	/** A spore cloud settling where her vents opened: a sting every second and the same pressure as a pool. */
	public static MycophantHazardEntity cloud(ServerLevel level, Vec3 at, MycophantEntity owner) {
		return spawn(level, at, owner, Kind.SPORE_CLOUD, MycophantHazardRules.CLOUD_RADIUS, MycophantHazardRules.CLOUD_LIFE, 0);
	}

	/** The lattice net: it comes down after {@code landDelay} ticks and holds whoever it covers. */
	public static MycophantHazardEntity net(ServerLevel level, Vec3 at, MycophantEntity owner, int landDelay) {
		int delay = Math.max(1, landDelay);
		return spawn(level, at, owner, Kind.LATTICE_NET, MycophantHazardRules.NET_RADIUS, delay + MycophantHazardRules.NET_HOLD, delay);
	}

	/** A binding tendril from her body to {@code anchor}; it lasts until it is cut or the Last Brood ends it. */
	public static MycophantHazardEntity tendril(ServerLevel level, MycophantEntity owner, Vec3 anchor) {
		MycophantHazardEntity tendril = create(level, owner, Kind.BINDING_TENDRIL, 0.0F, Integer.MAX_VALUE, 0);
		tendril.entityData.set(END, new Vector3f((float) anchor.x, (float) anchor.y, (float) anchor.z));
		tendril.entityData.set(HITS, MycophantLastBroodRules.HITS_PER_TENDRIL);
		tendril.moveTo(anchor.x, anchor.y, anchor.z, 0.0F, 0.0F);
		tendril.setBoundingBox(tendril.tendrilBox());
		level.addFreshEntity(tendril);
		return tendril;
	}

	private static MycophantHazardEntity spawn(ServerLevel level, Vec3 at, MycophantEntity owner, Kind kind, float radius,
			int life, int landDelay) {
		MycophantHazardEntity hazard = create(level, owner, kind, radius, life, landDelay);
		hazard.moveTo(at.x, at.y + 0.01D, at.z, level.random.nextFloat() * 360.0F, 0.0F);
		level.addFreshEntity(hazard);
		return hazard;
	}

	private static MycophantHazardEntity create(ServerLevel level, MycophantEntity owner, Kind kind, float radius, int life,
			int landDelay) {
		MycophantHazardEntity hazard = new MycophantHazardEntity(EntityInit.mycophant_hazard.get(), level);
		hazard.entityData.set(KIND, (byte) kind.ordinal());
		hazard.entityData.set(RADIUS, radius);
		hazard.entityData.set(LIFE, life);
		hazard.entityData.set(BORN, level.getGameTime());
		hazard.entityData.set(OWNER_ID, owner.getId());
		hazard.entityData.set(LAND_DELAY, landDelay);
		return hazard;
	}

	/** Every hazard of {@code kind} the owner has standing near her. */
	public static List<MycophantHazardEntity> of(ServerLevel level, MycophantEntity owner, Kind kind) {
		return level.getEntitiesOfClass(MycophantHazardEntity.class, owner.getBoundingBox().inflate(48.0D),
				hazard -> hazard.kind() == kind && hazard.getOwnerId() == owner.getId() && !hazard.isRemoved());
	}

	// ------------------------------------------------------------------ reading them

	public Kind kind() {
		return Kind.values()[Math.floorMod(this.entityData.get(KIND), Kind.values().length)];
	}

	public float radius() { return this.entityData.get(RADIUS); }
	public int life() { return this.entityData.get(LIFE); }
	public int landDelay() { return this.entityData.get(LAND_DELAY); }
	public int getOwnerId() { return this.entityData.get(OWNER_ID); }

	/** Ticks since it was made, from the synced birth time so a late-joining client agrees. */
	public int age() {
		return (int) Math.max(0L, this.level().getGameTime() - this.entityData.get(BORN));
	}

	/** The fractional age a renderer animates with, given the game time it already reads. */
	public float age(double gameTime) {
		return (float) Math.max(0.0D, gameTime - this.entityData.get(BORN));
	}

	/** A binding tendril's anchor on the floor. */
	public Vec3 end() {
		Vector3f end = this.entityData.get(END);
		return new Vec3(end.x, end.y, end.z);
	}

	/** Blows a binding tendril can still take; it is cut at none. */
	public int hitsLeft() {
		return this.entityData.get(HITS);
	}

	/** The net has come down. */
	public boolean landed() {
		return kind() == Kind.LATTICE_NET && MycophantHazardRules.landed(age(), landDelay());
	}

	/** The net came down over {@code entity} (server side; the client has no catch list). */
	public boolean caught(LivingEntity entity) {
		return kind() == Kind.LATTICE_NET && this.caught.contains(entity.getUUID());
	}

	/** True when {@code entity} stands inside a net that has come down. */
	public static boolean nets(Level level, LivingEntity entity) {
		double reach = MycophantHazardRules.NET_RADIUS + 1.0D;
		return !level.getEntitiesOfClass(MycophantHazardEntity.class, entity.getBoundingBox().inflate(reach, 3.0D, reach),
				net -> net.landed() && !net.isRemoved() && (net.caught(entity) || net.covers(entity))).isEmpty();
	}

	@Nullable
	public MycophantEntity owner() {
		return this.level().getEntity(getOwnerId()) instanceof MycophantEntity boss ? boss : null;
	}

	/** Where a binding tendril leaves her body, for her feet at {@code feet} facing {@code yawDegrees}. */
	public static Vec3 tendrilRoot(Vec3 feet, float yawDegrees) {
		float yaw = yawDegrees * Mth.DEG_TO_RAD;
		return feet.add(-Mth.sin(yaw) * MycophantHazardRules.TENDRIL_ROOT_FORWARD, MycophantHazardRules.TENDRIL_ROOT_UP,
				Mth.cos(yaw) * MycophantHazardRules.TENDRIL_ROOT_FORWARD);
	}

	/** The tendril's root on her body now, or its anchor when she cannot be found. */
	public Vec3 root() {
		MycophantEntity boss = owner();
		return boss == null ? end() : tendrilRoot(boss.position(), boss.yBodyRot);
	}

	private boolean covers(LivingEntity entity) {
		return MycophantHazardRules.standingIn(entity.getX() - getX(), entity.getY() - getY(), entity.getZ() - getZ(), radius());
	}

	/**
	 * Hazards answer only to survival players and to whatever she is fighting (a summoned Mycophant's quarry); never
	 * to her, her brood, creative or spectating players.
	 */
	static boolean takes(@Nullable MycophantEntity owner, LivingEntity entity) {
		if (!entity.isAlive() || entity instanceof MycophantEntity || entity instanceof FunglingEntity) return false;
		if (entity instanceof Player player) return MycophantStrikeRules.onlySurvival(player.isSpectator(), player.isCreative());
		return owner != null && (entity == owner.getTarget() || entity == owner.quarry());
	}

	private boolean ownerAlive(@Nullable MycophantEntity boss) {
		return boss != null && boss.isAlive() && !boss.isRemoved() && boss.getState() != MycophantState.YIELD;
	}

	// ------------------------------------------------------------------ ticking

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(KIND, (byte) 0);
		builder.define(RADIUS, 1.0F);
		builder.define(LIFE, 1);
		builder.define(BORN, 0L);
		builder.define(OWNER_ID, -1);
		builder.define(LAND_DELAY, 0);
		builder.define(END, new Vector3f());
		builder.define(HITS, 0);
	}

	@Override
	public void tick() {
		super.tick();
		if (kind() == Kind.BINDING_TENDRIL) setBoundingBox(tendrilBox());
		if (this.level().isClientSide) {
			tickClient();
			return;
		}
		ServerLevel level = (ServerLevel) this.level();
		MycophantEntity boss = owner();
		int age = age();
		if (MycophantHazardRules.shouldExpire(ownerAlive(boss), age, life())) {
			discard();
			return;
		}
		switch (kind()) {
			case NECTAR_POOL -> pool(boss);
			case SPORE_CLOUD -> cloud(boss, age);
			case LATTICE_NET -> net(level, boss, age);
			case BINDING_TENDRIL -> {
				if (age % 6 == 0) BossTelegraphs.spores(level, root().lerp(end(), this.random.nextDouble()), MycophantMoves.SPORE, 1, 0.1D, 0.01D);
			}
		}
	}

	private List<LivingEntity> standing(MycophantEntity boss) {
		float r = radius();
		return this.level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(r, MycophantHazardRules.FLOOR_ABOVE, r),
				entity -> takes(boss, entity) && covers(entity));
	}

	private void pool(MycophantEntity boss) {
		for (LivingEntity victim : standing(boss)) {
			victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, MycophantHazardRules.POOL_SLOW_TICKS, 0, false, true));
			press(boss, victim);
		}
	}

	private void cloud(MycophantEntity boss, int age) {
		boolean sting = age > 0 && age % MycophantHazardRules.CLOUD_DAMAGE_EVERY == 0;
		for (LivingEntity victim : standing(boss)) {
			if (sting) victim.hurt(boss.damageSources().mobAttack(boss), MycophantHazardRules.CLOUD_DAMAGE);
			press(boss, victim);
		}
	}

	/** Standing in her nectar feeds the claim: the owner's pressure climbs one point a second. */
	private static void press(MycophantEntity boss, LivingEntity victim) {
		UUID owner = boss.getEncounterOwner();
		if (owner != null && owner.equals(victim.getUUID())) boss.addPressure(MycophantHazardRules.PRESSURE_PER_TICK);
	}

	private void net(ServerLevel level, MycophantEntity boss, int age) {
		int delay = landDelay();
		if (age < delay) {
			// The circle has to read from across the Nursery before it comes down.
			if (age % 4 == 0) {
				BossTelegraphs.ring(level, position(), radius(), age % 8 == 0 ? MycophantMoves.YOLK : MycophantMoves.BONE, 36);
				BossTelegraphs.glow(level, position().add(0.0D, 2.5D - 2.0D * age / (double) delay, 0.0D), MycophantMoves.AMBER, 2, radius() * 0.5D);
			}
			return;
		}
		if (age != delay) return;
		for (LivingEntity victim : standing(boss)) {
			this.caught.add(victim.getUUID());
			victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, MycophantHazardRules.NET_HOLD,
					MycophantHazardRules.NET_SLOW_AMPLIFIER, false, true));
			victim.setDeltaMovement(victim.getDeltaMovement().multiply(0.0D, 1.0D, 0.0D));
			victim.hurtMarked = true;
		}
		BossTelegraphs.ring(level, position(), radius(), MycophantMoves.AMBER, 48);
		BossTelegraphs.spores(level, position().add(0.0D, 0.4D, 0.0D), MycophantMoves.SPORE, 24, radius() * 0.5D, 0.02D);
		level.playSound(null, blockPosition(), SoundEvents.COBWEB_PLACE, SoundSource.HOSTILE, 1.6F, 0.5F);
		level.playSound(null, blockPosition(), SoundEvents.ROOTED_DIRT_BREAK, SoundSource.HOSTILE, 1.2F, 0.7F);
	}

	private void tickClient() {
		Kind kind = kind();
		float r = radius();
		switch (kind) {
			case SPORE_CLOUD -> {
				// The cloud is its motes: slow spores drifting up through the whole radius.
				for (int i = 0; i < 2; i++) {
					double a = this.random.nextDouble() * Math.PI * 2.0D, d = Math.sqrt(this.random.nextDouble()) * r;
					this.level().addParticle(HemoParticleData.sporiticSpore(this.random.nextInt(3) == 0 ? MycophantMoves.YOLK : MycophantMoves.SPORE),
							getX() + Math.cos(a) * d, getY() + this.random.nextDouble() * 1.8D, getZ() + Math.sin(a) * d,
							0.0D, 0.008D, 0.0D);
				}
			}
			case NECTAR_POOL -> {
				if (this.random.nextInt(6) != 0) return;
				double a = this.random.nextDouble() * Math.PI * 2.0D, d = Math.sqrt(this.random.nextDouble()) * r * 0.85D;
				this.level().addParticle(HemoParticleData.glow(MycophantMoves.YOLK), getX() + Math.cos(a) * d, getY() + 0.06D,
						getZ() + Math.sin(a) * d, 0.0D, 0.012D, 0.0D);
			}
			default -> {
			}
		}
	}

	// ------------------------------------------------------------------ the binding tendrils

	private AABB tendrilBox() {
		double w = MycophantHazardRules.TENDRIL_HALF_WIDTH;
		return new AABB(root(), end()).inflate(w);
	}

	@Override
	public boolean isPickable() {
		return kind() == Kind.BINDING_TENDRIL && !isRemoved();
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (kind() != Kind.BINDING_TENDRIL || this.level().isClientSide || isRemoved() || isInvulnerableTo(source)) return false;
		if (!(source.getEntity() instanceof Player player) || !(this.level() instanceof ServerLevel level)) return false;
		// The hit box spans the tendril's whole run; a swing has to be aimed along it, and one aimed past it at her lands on her.
		boolean struck = source.getDirectEntity() instanceof Projectile projectile
				? projectile.position().distanceTo(nearestTo(projectile.position())) <= MycophantHazardRules.TENDRIL_HIT_SLACK
				: aimedAt(player);
		if (!struck) {
			// the box spans the whole run; a blow or a shot aimed past the tendril at her lands on her
			MycophantEntity boss = owner();
			if (boss != null) {
				boolean atHer = source.getDirectEntity() instanceof Projectile projectile
						? boss.getBoundingBox().inflate(1.0D).contains(projectile.position())
						: aimsAt(player, boss.getBoundingBox());
				if (atHer) boss.hurt(source, amount);
			}
			return false;
		}
		int left = hitsLeft() - 1;
		this.entityData.set(HITS, Math.max(0, left));
		Vec3 at = nearestTo(player.getEyePosition());
		BossTelegraphs.bloodCells(level, at, MycophantMoves.CRIMSON, 10, 0.2D, 0.08D);
		BossTelegraphs.spores(level, at, MycophantMoves.SPORE, 6, 0.25D, 0.03D);
		level.playSound(null, at.x, at.y, at.z, SoundEvents.SLIME_BLOCK_BREAK, SoundSource.HOSTILE, 1.0F, 0.6F);
		if (left <= 0) cut(level);
		return true;
	}

	/** Cut through: it bursts along its length and is gone. */
	private void cut(ServerLevel level) {
		Vec3 root = root(), end = end();
		for (int i = 0; i <= 6; i++) {
			Vec3 at = root.lerp(end, i / 6.0D);
			BossTelegraphs.bloodCells(level, at, MycophantMoves.CRIMSON, 4, 0.2D, 0.06D);
			BossTelegraphs.glow(level, at, MycophantMoves.AMBER, 2, 0.2D);
		}
		level.playSound(null, end.x, end.y, end.z, SoundEvents.ROOTED_DIRT_BREAK, SoundSource.HOSTILE, 1.6F, 0.5F);
		discard();
	}

	/** The Last Brood is over: a tendril still standing dries and falls apart. */
	public void wither() {
		if (this.level() instanceof ServerLevel level) {
			Vec3 root = root(), end = end();
			for (int i = 0; i <= 4; i++) BossTelegraphs.spores(level, root.lerp(end, i / 4.0D), MycophantMoves.BONE, 4, 0.2D, 0.02D);
		}
		discard();
	}

	private boolean aimedAt(Player player) {
		Vec3 eye = player.getEyePosition(), look = player.getLookAngle();
		Vec3 root = root(), end = end();
		double distance = MycophantHazardRules.aimDistance(new double[] { eye.x, eye.y, eye.z }, new double[] { look.x, look.y, look.z },
				player.entityInteractionRange() + 1.0D, new double[] { root.x, root.y, root.z }, new double[] { end.x, end.y, end.z });
		return distance <= MycophantHazardRules.TENDRIL_HIT_SLACK;
	}

	private static boolean aimsAt(Player player, AABB box) {
		Vec3 eye = player.getEyePosition();
		return box.clip(eye, eye.add(player.getLookAngle().scale(player.entityInteractionRange() + 1.0D))).isPresent();
	}

	private Vec3 nearestTo(Vec3 point) {
		Vec3 root = root(), segment = end().subtract(root);
		double t = segment.lengthSqr() < 1.0E-6D ? 0.0D : Mth.clamp(point.subtract(root).dot(segment) / segment.lengthSqr(), 0.0D, 1.0D);
		return root.add(segment.scale(t));
	}

	// ------------------------------------------------------------------ the rest

	@Override
	public AABB getBoundingBoxForCulling() {
		if (kind() == Kind.BINDING_TENDRIL) return tendrilBox().inflate(1.0D);
		float r = radius() + 0.5F;
		return getBoundingBox().inflate(r, 3.0D, r);
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	// Hazards are the residue of a fight in progress; they are never written to disk.
	@Override
	public boolean shouldBeSaved() {
		return false;
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
	}
}
