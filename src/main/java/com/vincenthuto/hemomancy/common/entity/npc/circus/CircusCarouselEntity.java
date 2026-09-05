package com.vincenthuto.hemomancy.common.entity.npc.circus;

import com.vincenthuto.hemomancy.common.entity.mob.monster.EnthralledDollEntity;
import com.vincenthuto.hemomancy.common.entity.mob.monster.BloodDrunkPuppeteerEntity;
import com.vincenthuto.hemomancy.common.circus.CircusCarouselEncounterRules;
import com.vincenthuto.hemomancy.common.circus.CircusPavilionSavedData;
import com.vincenthuto.hemomancy.common.circus.CircusPavilionStateRules;
import com.vincenthuto.hemomancy.common.circus.CircusRouteRules;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
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

import java.util.List;

public final class CircusCarouselEntity extends Entity {
	private static final EntityDataAccessor<Boolean> ACTIVE = SynchedEntityData.defineId(
			CircusCarouselEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Integer> SEVERED = SynchedEntityData.defineId(
			CircusCarouselEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> BROKEN = SynchedEntityData.defineId(
			CircusCarouselEntity.class, EntityDataSerializers.INT);
	private static final double TROUPE_RANGE = 16.0D;
	private float rotationSpeed = CircusCarouselRules.targetSpeed(false);
	private int activeTicks;
	private BlockPos encounterOrigin;
	private int missingOwnerTicks;

	public CircusCarouselEntity(EntityType<? extends CircusCarouselEntity> type, Level level) {
		super(type, level);
		noPhysics = true;
		setNoGravity(true);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(ACTIVE, false);
		builder.define(SEVERED, 0);
		builder.define(BROKEN, 0);
	}

	@Override
	public void tick() {
		super.tick();
		noPhysics = true;
		setNoGravity(true);
		setDeltaMovement(0.0D, 0.0D, 0.0D);
		if (level().isClientSide) {
			rotationSpeed = CircusCarouselRules.nextSpeed(rotationSpeed, isActive());
			setYRot(Mth.wrapDegrees(getYRot() + rotationSpeed));
			return;
		}
		if (encounterOrigin == null) encounterOrigin = blockPosition();
		syncEncounterState((ServerLevel) level());
		ensureCaptives((ServerLevel) level());

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
			if (isRiderSevered(horse) || captive(horse) != null) continue;
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
		double riderRadius = 1.0D + 0.55D / CircusCarouselRules.HORSE_RADIUS;
		callback.accept(passenger, getX() + pose.x() * riderRadius,
				getY() + 2.55D + pose.bob(), getZ() + pose.z() * riderRadius);
		float yaw = pose.angleDegrees() + 90.0F;
		captive.setYRot(yaw);
		captive.setYBodyRot(yaw);
		captive.setYHeadRot(yaw);
	}

	private void syncEncounterState(ServerLevel level) {
		CircusPavilionSavedData data = CircusPavilionSavedData.get(level);
		CircusPavilionSavedData.Site site = data.site(level, encounterOrigin);
		entityData.set(SEVERED, site.severedMask());
		entityData.set(BROKEN, site.brokenMask());
		if (site.activeOwner() == null) {
			missingOwnerTicks = 0;
			return;
		}
		Player owner = level.getPlayerByUUID(site.activeOwner());
		if (owner != null && owner.isAlive() && owner.level() == level && owner.distanceToSqr(this) <= 64.0D * 64.0D) {
			missingOwnerTicks = 0;
		} else if (++missingOwnerTicks >= 200) {
			data.reset(level, encounterOrigin, site.activeOwner());
			resetEncounterVisuals();
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
			level.sendParticles(ParticleTypes.CRIMSON_SPORE, getX() + pose.x(),
					getY() + 2.0D + pose.bob(), getZ() + pose.z(), 2, 0.35D, 0.5D, 0.35D, 0.01D);
		}
	}

	public boolean isActive() {
		return entityData.get(ACTIVE);
	}

	public BlockPos encounterOrigin() {
		return encounterOrigin == null ? blockPosition() : encounterOrigin;
	}

	public boolean isRiderSevered(int rider) {
		return (entityData.get(SEVERED) & (1 << Math.floorMod(rider, 3))) != 0;
	}

	public boolean isAnchorBroken(int anchor) {
		return (entityData.get(BROKEN) & (1 << Math.floorMod(anchor, 3))) != 0;
	}

	public boolean isDestroyed() {
		return CircusCarouselEncounterRules.allAnchorsBroken(entityData.get(BROKEN));
	}

	public boolean severCaptive(Player player) {
		return severCaptive(player, nearestHorse(player, entityData.get(SEVERED), false));
	}

	public boolean severCaptive(Player player, int rider) {
		if (!(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)
				|| !(level() instanceof ServerLevel server) || rider < 0 || rider > 2 || isRiderSevered(rider)) return false;
		CircusPavilionSavedData data = CircusPavilionSavedData.get(server);
		CircusPavilionSavedData.Site site = data.site(server, encounterOrigin());
		if (!CircusPavilionStateRules.canAct(site.activeOwner(), player.getUUID())
				|| site.route() != CircusRouteRules.Route.LIBERATION) return false;
		int severed = CircusCarouselEncounterRules.sever(site.severedMask(), rider);
		data.setCarouselProgress(server, encounterOrigin(), severed, site.brokenMask());
		entityData.set(SEVERED, severed);
		BloodDrunkPuppeteerEntity captive = captive(rider);
		if (captive != null) {
			captive.stopRiding();
			captive.discard();
		}
		CircusCarouselRules.HorsePose pose = CircusCarouselRules.horsePose(getYRot(), rider);
		server.sendParticles(ParticleTypes.CRIMSON_SPORE, getX() + pose.x(), getY() + 2.6D + pose.bob(),
				getZ() + pose.z(), 35, 0.35D, 0.75D, 0.35D, 0.04D);
		server.playSound(null, blockPosition(), SoundEvents.CHAIN_BREAK, SoundSource.PLAYERS, 1.0F, 0.8F);
		serverPlayer.displayClientMessage(net.minecraft.network.chat.Component.translatable(
				"hemomancy.circus.captive.severed", rider + 1), false);
		return true;
	}

	private int nearestHorse(Player player, int mask, boolean requireSevered) {
		int best = -1;
		double bestScore = 0.82D;
		for (int horse = 0; horse < 3; horse++) {
			boolean selected = (mask & (1 << horse)) != 0;
			if (selected != requireSevered || requireSevered && isAnchorBroken(horse)) continue;
			CircusCarouselRules.HorsePose pose = CircusCarouselRules.horsePose(getYRot(), horse);
			net.minecraft.world.phys.Vec3 target = new net.minecraft.world.phys.Vec3(
					getX() + pose.x(), getY() + (requireSevered ? 1.1D : 2.6D) + pose.bob(), getZ() + pose.z());
			net.minecraft.world.phys.Vec3 delta = target.subtract(player.getEyePosition());
			double score = delta.normalize().dot(player.getLookAngle());
			if (score > bestScore) { bestScore = score; best = horse; }
		}
		return best;
	}

	private void resetEncounterVisuals() {
		entityData.set(SEVERED, 0);
		entityData.set(BROKEN, 0);
		missingOwnerTicks = 0;
	}

	public AABB getCarouselRenderBounds() {
		return getBoundingBox().inflate(3.5D, 0.5D, 3.5D);
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		entityData.set(ACTIVE, false);
		activeTicks = 0;
		rotationSpeed = CircusCarouselRules.targetSpeed(false);
		encounterOrigin = tag.contains("EncounterOrigin") ? BlockPos.of(tag.getLong("EncounterOrigin")) : blockPosition();
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		tag.putLong("EncounterOrigin", encounterOrigin().asLong());
	}

	@Override
	public boolean isPickable() {
		return entityData.get(SEVERED) != 0;
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	public boolean isAttackable() {
		return entityData.get(SEVERED) != 0 && !isDestroyed();
	}

	@Override
	public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
		if (!(source.getEntity() instanceof Player player) || !(level() instanceof ServerLevel server)) return false;
		CircusPavilionSavedData data = CircusPavilionSavedData.get(server);
		CircusPavilionSavedData.Site site = data.site(server, encounterOrigin());
		if (!CircusPavilionStateRules.canAct(site.activeOwner(), player.getUUID())
				|| site.route() != CircusRouteRules.Route.LIBERATION) return false;
		int anchor = nearestHorse(player, site.severedMask(), true);
		if (anchor < 0 || !CircusCarouselEncounterRules.canBreakAnchor(site.severedMask(), anchor)) return false;
		int broken = CircusCarouselEncounterRules.breakAnchor(site.brokenMask(), site.severedMask(), anchor);
		data.setCarouselProgress(server, encounterOrigin(), site.severedMask(), broken);
		entityData.set(BROKEN, broken);
		server.playSound(null, blockPosition(), SoundEvents.ANVIL_DESTROY, SoundSource.PLAYERS, 1.1F, 0.65F);
		if (CircusCarouselEncounterRules.allAnchorsBroken(broken)) {
			data.setPhase(server, encounterOrigin(), CircusPavilionStateRules.Phase.DESCENT);
			server.sendParticles(ParticleTypes.EXPLOSION, getX(), getY() + 1.0D, getZ(), 12,
					2.5D, 0.4D, 2.5D, 0.05D);
		}
		return true;
	}
}
