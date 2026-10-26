package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.entity.boss.BossTelegraphs;
import com.vincenthuto.hemomancy.common.entity.mob.animal.FunglingEntity;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

import javax.annotation.Nullable;

/**
 * What the Spawn Mother lobs: a glob of nectar from the siphon that leaves a pool where it bursts, or an egg sac from
 * the abdomen that bursts in spores, or, in an owned fight, lands whole and roots as a brood tumor. It flies with no
 * drag, moving by its velocity and then losing {@link MycophantHazardRules#GLOB_GRAVITY} each tick, so it lands on the
 * point {@link MycophantStrikeRules#lobVelocity} aimed it at.
 */
public class MycophantGlobEntity extends Projectile {
	public enum Kind { GLOB, EGG }

	private static final EntityDataAccessor<Byte> KIND = SynchedEntityData.defineId(MycophantGlobEntity.class, EntityDataSerializers.BYTE);
	private static final EntityDataAccessor<Boolean> TUMOR = SynchedEntityData.defineId(MycophantGlobEntity.class, EntityDataSerializers.BOOLEAN);

	/** Where an egg will come down (server only, never saved); its landing ring is redrawn there in flight. */
	@Nullable private Vec3 landing;

	public MycophantGlobEntity(EntityType<? extends MycophantGlobEntity> type, Level level) {
		super(type, level);
	}

	/** Lobs a glob or egg from {@code from} so it comes down on {@code target} after {@code flightTicks}. */
	public static void launch(ServerLevel level, MycophantEntity owner, Vec3 from, Vec3 target, Kind kind, boolean tumor,
			int flightTicks) {
		MycophantGlobEntity glob = new MycophantGlobEntity(EntityInit.mycophant_glob.get(), level);
		glob.setOwner(owner);
		glob.entityData.set(KIND, (byte) kind.ordinal());
		glob.entityData.set(TUMOR, tumor && kind == Kind.EGG);
		if (kind == Kind.EGG) glob.landing = target;
		glob.setPos(from.x, from.y, from.z);
		Vec3 to = target.subtract(from);
		double[] v = MycophantStrikeRules.lobVelocity(to.x, to.y, to.z, flightTicks, MycophantHazardRules.GLOB_GRAVITY);
		glob.setDeltaMovement(v[0], v[1], v[2]);
		glob.setYRot(level.random.nextFloat() * 360.0F);
		level.addFreshEntity(glob);
	}

	public Kind kind() {
		return Kind.values()[Math.floorMod(this.entityData.get(KIND), Kind.values().length)];
	}

	/** An egg meant to land whole and root as a brood tumor. */
	public boolean isTumor() {
		return this.entityData.get(TUMOR);
	}

	@Nullable
	private MycophantEntity boss() {
		return getOwner() instanceof MycophantEntity boss ? boss : null;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(KIND, (byte) 0);
		builder.define(TUMOR, false);
	}

	@Override
	public void tick() {
		super.tick();
		HitResult hit = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
		if (hit.getType() != HitResult.Type.MISS && !net.neoforged.neoforge.event.EventHooks.onProjectileImpact(this, hit)) {
			hitTargetOrDeflectSelf(hit);
		}
		if (isRemoved()) return;
		checkInsideBlocks();
		Vec3 v = getDeltaMovement();
		double[] position = { getX(), getY(), getZ() };
		double[] velocity = { v.x, v.y, v.z };
		MycophantHazardRules.step(position, velocity, MycophantHazardRules.GLOB_GRAVITY);
		updateRotation();
		setDeltaMovement(velocity[0], velocity[1], velocity[2]);
		setPos(position[0], position[1], position[2]);
		if (this.level().isClientSide) {
			trail();
		} else if (this.tickCount > MycophantHazardRules.GLOB_MAX_AGE) {
			discard();
		} else if (this.landing != null && this.tickCount % 3 == 0 && this.level() instanceof ServerLevel server) {
			// the ring stays up until the egg comes down, however long the shot outlasts the volley
			BossTelegraphs.ring(server, this.landing, MycophantHazardRules.EGG_RADIUS, MycophantMoves.YOLK, 26);
		}
	}

	private void trail() {
		if (kind() == Kind.GLOB) {
			this.level().addParticle(HemoParticleData.glow(this.random.nextBoolean() ? MycophantMoves.YOLK : MycophantMoves.AMBER),
					getX(), getY() + 0.1D, getZ(), 0.0D, 0.0D, 0.0D);
		} else if (this.tickCount % 2 == 0) {
			this.level().addParticle(HemoParticleData.sporiticSpore(MycophantMoves.SPORE), getX(), getY() + 0.2D, getZ(),
					0.0D, 0.01D, 0.0D);
		}
	}

	@Override
	protected double getDefaultGravity() {
		return MycophantHazardRules.GLOB_GRAVITY;
	}

	@Override
	protected boolean canHitEntity(Entity entity) {
		return super.canHitEntity(entity) && entity instanceof LivingEntity living && MycophantHazardEntity.takes(boss(), living);
	}

	@Override
	protected void onHitEntity(EntityHitResult result) {
		super.onHitEntity(result);
		MycophantEntity boss = boss();
		if (this.level().isClientSide || boss == null || kind() != Kind.GLOB) return;
		if (result.getEntity() instanceof LivingEntity target)
			target.hurt(boss.damageSources().mobAttack(boss), MycophantHazardRules.GLOB_DAMAGE);
	}

	@Override
	protected void onHit(HitResult result) {
		super.onHit(result);
		if (!(this.level() instanceof ServerLevel level)) return;
		MycophantEntity boss = boss();
		Vec3 landing = floor(level, result.getLocation());
		if (boss != null && boss.isAlive()) {
			if (kind() == Kind.GLOB) splash(level, boss, landing);
			else if (!(isTumor() && root(level, boss, landing, result))) burst(level, boss, landing);
		}
		discard();
	}

	/** The glob bursts into a pool of her nectar. */
	private void splash(ServerLevel level, MycophantEntity boss, Vec3 landing) {
		MycophantHazardEntity.pool(level, landing, boss);
		BossTelegraphs.bloodCells(level, landing.add(0.0D, 0.2D, 0.0D), MycophantMoves.AMBER, 10, 0.3D, 0.06D);
		BossTelegraphs.glow(level, landing.add(0.0D, 0.15D, 0.0D), MycophantMoves.YOLK, 4, 0.4D);
		level.playSound(null, landing.x, landing.y, landing.z, SoundEvents.HONEY_BLOCK_FALL, SoundSource.HOSTILE, 1.2F, 0.6F);
	}

	/** The egg breaks open in a burst of spores. */
	private void burst(ServerLevel level, MycophantEntity boss, Vec3 landing) {
		double r = MycophantHazardRules.EGG_RADIUS;
		for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, new net.minecraft.world.phys.AABB(landing, landing).inflate(r),
				entity -> MycophantHazardEntity.takes(boss, entity) && entity.position().distanceToSqr(landing) <= r * r)) {
			victim.hurt(boss.damageSources().mobAttack(boss), MycophantHazardRules.EGG_DAMAGE);
		}
		BossTelegraphs.spores(level, landing.add(0.0D, 0.4D, 0.0D), MycophantMoves.SPORE, 30, 1.0D, 0.06D);
		BossTelegraphs.ring(level, landing, r, MycophantMoves.YOLK, 24);
		BossTelegraphs.bloodCells(level, landing.add(0.0D, 0.3D, 0.0D), MycophantMoves.CRIMSON, 8, 0.4D, 0.08D);
		level.playSound(null, landing.x, landing.y, landing.z, SoundEvents.FUNGUS_BREAK, SoundSource.HOSTILE, 1.4F, 0.5F);
		level.playSound(null, landing.x, landing.y, landing.z, SoundEvents.SLIME_SQUISH, SoundSource.HOSTILE, 1.0F, 0.6F);
	}

	/** The egg lands whole and roots; false when it cannot (no room, a full nursery), so it bursts instead. */
	private boolean root(ServerLevel level, MycophantEntity boss, Vec3 landing, HitResult result) {
		BlockPos at = result instanceof BlockHitResult block ? block.getBlockPos().relative(block.getDirection()) : BlockPos.containing(landing);
		return boss.broodTumors().plant(level, at);
	}

	/** The floor under where it struck, so a pool lies flat even when the glob hit a body. */
	private static Vec3 floor(ServerLevel level, Vec3 at) {
		HitResult hit = level.clip(new ClipContext(at.add(0.0D, 0.5D, 0.0D), at.subtract(0.0D, 4.0D, 0.0D), ClipContext.Block.COLLIDER,
				ClipContext.Fluid.NONE, CollisionContext.empty()));
		return hit.getType() == HitResult.Type.MISS ? at : hit.getLocation();
	}

	@Override
	public boolean isPickable() {
		return false;
	}

	@Override
	public boolean shouldBeSaved() {
		return false;
	}
}
