package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.entity.boss.BossTelegraphs;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

import javax.annotation.Nullable;

/**
 * A Hyphae Lance: where the Spawn Mother's wing spars stab the floor, a spot further along the line cracks amber for
 * {@link MycophantStrikeRules#LANCE_WARN_TICKS}, then a cluster of hyphae spears straight up through it and tosses
 * whoever stands there. It waits {@code delay} ticks before it starts cracking, so one line erupts in sequence. The
 * client draws the crack and the spears; this keeps time and hurts.
 */
public class MycophantLanceEntity extends Entity {
	private static final EntityDataAccessor<Integer> DELAY = SynchedEntityData.defineId(MycophantLanceEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> OWNER_ID = SynchedEntityData.defineId(MycophantLanceEntity.class, EntityDataSerializers.INT);

	public MycophantLanceEntity(EntityType<? extends MycophantLanceEntity> type, Level level) {
		super(type, level);
		this.noPhysics = true;
	}

	/** Sets a lance in the floor under {@code at}: it starts cracking after {@code delay} ticks, then erupts. */
	public static void erupt(ServerLevel level, Vec3 at, MycophantEntity owner, int delay) {
		Vec3 floor = floor(level, at);
		if (floor == null) return;
		MycophantLanceEntity lance = new MycophantLanceEntity(EntityInit.mycophant_lance.get(), level);
		lance.setPos(floor.x, floor.y, floor.z);
		lance.setYRot(level.random.nextFloat() * 360.0F);
		lance.entityData.set(DELAY, Math.max(0, delay));
		lance.entityData.set(OWNER_ID, owner.getId());
		level.addFreshEntity(lance);
	}

	/** The first floor within a couple of blocks of the point, or null over a drop. */
	@Nullable
	private static Vec3 floor(ServerLevel level, Vec3 at) {
		HitResult hit = level.clip(new ClipContext(at.add(0.0D, 1.5D, 0.0D), at.subtract(0.0D, 3.0D, 0.0D), ClipContext.Block.COLLIDER,
				ClipContext.Fluid.NONE, CollisionContext.empty()));
		return hit.getType() == HitResult.Type.MISS ? null : hit.getLocation();
	}

	/** Ticks before it starts cracking. */
	public int delay() {
		return this.entityData.get(DELAY);
	}

	/** The tick it erupts on: its wait, then the crack. */
	public int burstTick() {
		return delay() + MycophantStrikeRules.LANCE_WARN_TICKS;
	}

	@Nullable
	private MycophantEntity owner() {
		return this.level().getEntity(this.entityData.get(OWNER_ID)) instanceof MycophantEntity boss ? boss : null;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(DELAY, 0);
		builder.define(OWNER_ID, -1);
	}

	@Override
	public void tick() {
		super.tick();
		if (!(this.level() instanceof ServerLevel server)) return;
		MycophantEntity boss = owner();
		boolean alive = boss != null && boss.isAlive() && boss.getState() != MycophantState.YIELD;
		int burst = burstTick();
		if (MycophantHazardRules.shouldExpire(alive, this.tickCount, burst + MycophantHazardRules.LANCE_LIFE)) {
			discard();
			return;
		}
		if (this.tickCount >= delay() && this.tickCount < burst && this.tickCount % 3 == 0) {
			server.sendParticles(HemoParticleData.darkGlow(MycophantMoves.AMBER), getX(), getY() + 0.05D, getZ(), 2, 0.35D, 0.0D, 0.35D, 0.0D);
			server.sendParticles(HemoParticleData.sporiticSpore(MycophantMoves.SPORE), getX(), getY() + 0.1D, getZ(), 1, 0.3D, 0.0D, 0.3D, 0.01D);
		}
		if (this.tickCount == burst) burst(server, boss);
	}

	private void burst(ServerLevel server, MycophantEntity boss) {
		double r = MycophantHazardRules.LANCE_RADIUS;
		for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class, new AABB(position(), position()).inflate(r, 2.6D, r),
				entity -> MycophantHazardEntity.takes(boss, entity))) {
			double dx = victim.getX() - getX(), dz = victim.getZ() - getZ();
			if (dx * dx + dz * dz > r * r || victim.getY() > getY() + 2.6D || victim.getY() < getY() - 0.6D) continue;
			if (!victim.hurt(boss.damageSources().mobAttack(boss), MycophantHazardRules.LANCE_DAMAGE)) continue;
			if (!victim.isAlive() || victim.level() != server) continue;
			Vec3 out = new Vec3(dx, 0.0D, dz);
			Vec3 push = out.lengthSqr() < 1.0E-4D ? Vec3.ZERO : out.normalize().scale(0.2D);
			victim.push(push.x, MycophantHazardRules.LANCE_TOSS, push.z);
			victim.hurtMarked = true;
		}
		BossTelegraphs.bloodCells(server, position().add(0.0D, 0.6D, 0.0D), MycophantMoves.AMBER, 12, 0.3D, 0.14D);
		BossTelegraphs.spores(server, position().add(0.0D, 0.8D, 0.0D), MycophantMoves.SPORE, 10, 0.3D, 0.06D);
		BossTelegraphs.glow(server, position().add(0.0D, 0.3D, 0.0D), MycophantMoves.YOLK, 2, 0.2D);
		server.playSound(null, blockPosition(), SoundEvents.POINTED_DRIPSTONE_LAND, SoundSource.HOSTILE, 1.1F, 0.6F + this.random.nextFloat() * 0.2F);
		server.playSound(null, blockPosition(), SoundEvents.ROOTED_DIRT_BREAK, SoundSource.HOSTILE, 1.0F, 0.6F);
	}

	@Override
	public AABB getBoundingBoxForCulling() {
		return getBoundingBox().inflate(1.5D, 3.0D, 1.5D);
	}

	@Override
	public boolean isPickable() {
		return false;
	}

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
