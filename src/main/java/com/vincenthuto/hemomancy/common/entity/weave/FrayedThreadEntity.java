package com.vincenthuto.hemomancy.common.entity.weave;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffItem;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.SomaticLoomBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.SomaticWeaveRules;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * A thread torn loose from a Somatic Loom's echo. It hunts the weaver and attacks with its tendency's kit; each
 * attack and each staff hit strains it until it goes slack, when a Living Staff can hook it home. It never dies:
 * damage only strains it. Without its loom, or once its loom no longer counts it loose, it unravels.
 */
public class FrayedThreadEntity extends PathfinderMob {
	private static final EntityDataAccessor<Byte> TENDENCY = SynchedEntityData.defineId(FrayedThreadEntity.class, EntityDataSerializers.BYTE);
	private static final EntityDataAccessor<Byte> RANK = SynchedEntityData.defineId(FrayedThreadEntity.class, EntityDataSerializers.BYTE);
	private static final EntityDataAccessor<Float> STRAIN = SynchedEntityData.defineId(FrayedThreadEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Integer> WINDUP = SynchedEntityData.defineId(FrayedThreadEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> FADED = SynchedEntityData.defineId(FrayedThreadEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<BlockPos> LOOM = SynchedEntityData.defineId(FrayedThreadEntity.class, EntityDataSerializers.BLOCK_POS);
	private int cooldown;
	private int strikeCooldown;

	public FrayedThreadEntity(EntityType<? extends FrayedThreadEntity> type, Level level) {
		super(type, level);
		setNoGravity(true);
		noPhysics = true;
	}

	public static AttributeSupplier.Builder setAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 20.0D).add(Attributes.FOLLOW_RANGE, 24.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.3D);
	}

	public static FrayedThreadEntity spawn(ServerLevel level, SomaticLoomBlockEntity loom, EnumBloodTendency tendency,
			EnumManipulationRank rank) {
		FrayedThreadEntity thread = new FrayedThreadEntity(EntityInit.frayed_thread.get(), level);
		thread.entityData.set(TENDENCY, (byte) tendency.ordinal());
		thread.entityData.set(RANK, (byte) rank.ordinal());
		thread.entityData.set(LOOM, loom.getBlockPos());
		Vec3 at = loom.echoAnchor();
		thread.moveTo(at.x, at.y + 0.5D, at.z, level.random.nextFloat() * 360F, 0F);
		thread.cooldown = SomaticWeaveRules.attackInterval(rank) / 2;
		level.addFreshEntity(thread);
		level.sendParticles(HemoParticleData.glow(tendency.getColor()), at.x, at.y + 0.5D, at.z, 14, 0.15D, 0.15D, 0.15D, 0.04D);
		level.playSound(null, at.x, at.y, at.z, SoundEvents.WOOL_BREAK, SoundSource.BLOCKS, 0.9F, 0.6F);
		return thread;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(TENDENCY, (byte) 0);
		builder.define(RANK, (byte) 0);
		builder.define(STRAIN, 0.0F);
		builder.define(WINDUP, 0);
		builder.define(FADED, 0);
		builder.define(LOOM, BlockPos.ZERO);
	}

	public EnumBloodTendency tendency() { return EnumBloodTendency.values()[entityData.get(TENDENCY)]; }
	public EnumManipulationRank rank() { return EnumManipulationRank.values()[entityData.get(RANK)]; }
	public BlockPos loomPos() { return entityData.get(LOOM); }
	public float strain() { return entityData.get(STRAIN); }
	public boolean isSlack() { return strain() >= 1.0F; }
	public int windup() { return entityData.get(WINDUP); }
	public boolean isFaded() { return entityData.get(FADED) > 0; }
	protected void setWindup(int ticks) { entityData.set(WINDUP, ticks); }
	public void fade(int ticks) { entityData.set(FADED, ticks); }

	public void addStrain(float amount) {
		if (isSlack()) return;
		entityData.set(STRAIN, Math.min(1.0F, strain() + amount));
		if (isSlack()) {
			setWindup(0);
			if (level() instanceof ServerLevel server) {
				server.sendParticles(HemoParticleData.darkGlow(tendency().getColor()), getX(), getY(), getZ(), 10, 0.2D, 0.2D, 0.2D, 0.02D);
				server.playSound(null, getX(), getY(), getZ(), SoundEvents.WOOL_PLACE, SoundSource.HOSTILE, 0.8F, 0.5F);
			}
		}
	}

	/** Weaves a slack thread home through the loom; a taut one refuses. */
	public boolean tryHook(Player by) {
		if (!isSlack() || !(level() instanceof ServerLevel server)) return false;
		return server.getBlockEntity(loomPos()) instanceof SomaticLoomBlockEntity loom && loom.weaveHome(this, by);
	}

	/** Whether this thread's loom is woven by `entity`: threads hunt and harm only their weaver. */
	public boolean hunts(net.minecraft.world.entity.Entity entity) {
		return entity != null && level().getBlockEntity(loomPos()) instanceof SomaticLoomBlockEntity loom
				&& entity.getUUID().equals(loom.getWeaver());
	}

	/**
	 * The slack thread `player` is looking at within its loom's hook reach (longer on a Ganglionic loom), if any.
	 * Works on both sides: the client uses it to keep the staff from starting a channel at a thread.
	 */
	@Nullable
	public static FrayedThreadEntity lookedAtSlackThread(Player player) {
		double max = SomaticWeaveRules.hookReach(2);
		Vec3 eye = player.getEyePosition();
		Vec3 end = eye.add(player.getViewVector(1.0F).scale(max));
		var hit = net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(player.level(), player, eye, end,
				player.getBoundingBox().expandTowards(end.subtract(eye)).inflate(1.0D),
				entity -> entity instanceof FrayedThreadEntity thread && thread.isSlack() && !thread.isRemoved(), 0.3F);
		if (hit == null || !(hit.getEntity() instanceof FrayedThreadEntity thread)) return null;
		int stage = player.level().getBlockEntity(thread.loomPos()) instanceof SomaticLoomBlockEntity loom ? loom.upgradeTier() : 0;
		double reach = SomaticWeaveRules.hookReach(stage);
		return eye.distanceToSqr(hit.getLocation()) <= reach * reach ? thread : null;
	}

	@Nullable
	private SomaticLoomBlockEntity loom(ServerLevel server) {
		return server.isLoaded(loomPos()) && server.getBlockEntity(loomPos()) instanceof SomaticLoomBlockEntity loom
				&& loom.ownsLooseThread(getUUID()) ? loom : null;
	}

	@Override
	public void tick() {
		noPhysics = true;
		super.tick();
		if (!(level() instanceof ServerLevel server)) return;
		SomaticLoomBlockEntity loom = loom(server);
		if (loom == null) {
			discard();
			return;
		}
		if (entityData.get(FADED) > 0) entityData.set(FADED, entityData.get(FADED) - 1);
		if (strikeCooldown > 0) strikeCooldown--;
		Vec3 home = loom.echoAnchor();
		if (isSlack()) {
			// Spent: it sinks toward the loom and waits to be hooked.
			steer(home.add(0, 0.6D, 0), 0.04D);
			return;
		}
		Player target = loom.weaverPlayer(server);
		if (target == null) {
			steer(home.add(0, 1.0D, 0), 0.06D);
			return;
		}
		ThreadAttacks.Profile profile = ThreadAttacks.profile(tendency());
		double angle = tickCount * profile.orbit() + getId();
		// Winding up, it dives within staff reach: the tell is also the opening to strike it.
		boolean winding = windup() > 0;
		double distance = SomaticWeaveRules.holdDistance(profile.distance(), winding);
		double height = winding ? Math.min(profile.height(), 1.4D) : profile.height();
		Vec3 desired = target.position().add(Math.cos(angle) * distance, height, Math.sin(angle) * distance);
		Vec3 fromLoom = desired.subtract(home);
		if (fromLoom.lengthSqr() > SomaticWeaveRules.RITUAL_RADIUS * SomaticWeaveRules.RITUAL_RADIUS)
			desired = home.add(fromLoom.normalize().scale(SomaticWeaveRules.RITUAL_RADIUS));
		steer(desired, profile.speed());
		getLookControl().setLookAt(target);
		tickAttack(target);
	}

	private void steer(Vec3 to, double speed) {
		Vec3 delta = to.subtract(position());
		Vec3 push = delta.lengthSqr() < 1.0E-4D ? Vec3.ZERO : delta.normalize().scale(Math.min(delta.length(), speed));
		setDeltaMovement(getDeltaMovement().scale(0.8D).add(push));
	}

	private void tickAttack(Player target) {
		int windup = windup();
		if (windup > 0) {
			setWindup(windup - 1);
			if (windup - 1 == 0) {
				ThreadAttacks.perform(this, target);
				addStrain(SomaticWeaveRules.strainPerAttack(rank()));
				cooldown = SomaticWeaveRules.attackInterval(rank());
			}
			return;
		}
		if (--cooldown <= 0 && hasLineOfSight(target) && distanceToSqr(target) < 14 * 14) {
			setWindup(SomaticWeaveRules.WINDUP_TICKS);
			level().playSound(null, getX(), getY(), getZ(), SoundEvents.WOOL_HIT, SoundSource.HOSTILE, 1.0F, 1.6F);
		}
	}

	/**
	 * Threads take no harm: a Living Staff blow in hand strains them, once per recovery, and anything else
	 * (shots, area harm) passes through.
	 */
	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (level().isClientSide || isFaded() || isSlack() || strikeCooldown > 0) return false;
		if (source.getDirectEntity() instanceof Player player && source.getEntity() == player
				&& source.is(net.minecraft.world.damagesource.DamageTypes.PLAYER_ATTACK)
				&& player.getMainHandItem().getItem() instanceof LivingStaffItem) {
			strikeCooldown = SomaticWeaveRules.STRIKE_COOLDOWN_TICKS;
			addStrain(SomaticWeaveRules.strainPerHit(rank()));
			if (level() instanceof ServerLevel server)
				server.sendParticles(HemoParticleData.ember(tendency().getColor(), 0.08F, 0.8F, 14), getX(), getY(), getZ(),
						8, 0.15D, 0.15D, 0.15D, 0.05D);
			setDeltaMovement(getDeltaMovement().add(position().subtract(player.position()).normalize().scale(0.4D)));
		}
		return false;
	}

	@Override
	protected InteractionResult mobInteract(Player player, InteractionHand hand) {
		if (player.getItemInHand(hand).getItem() instanceof LivingStaffItem && isSlack()) {
			return tryHook(player) ? InteractionResult.SUCCESS : InteractionResult.PASS;
		}
		return InteractionResult.PASS;
	}

	@Override public boolean isPushable() { return false; }
	@Override public boolean removeWhenFarAway(double distance) { return false; }
	@Override protected void registerGoals() {}
	@Override public boolean causeFallDamage(float distance, float multiplier, DamageSource source) { return false; }

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putByte("Tendency", entityData.get(TENDENCY));
		tag.putByte("Rank", entityData.get(RANK));
		tag.putFloat("Strain", strain());
		tag.put("Loom", NbtUtils.writeBlockPos(loomPos()));
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		entityData.set(TENDENCY, tag.getByte("Tendency"));
		entityData.set(RANK, tag.getByte("Rank"));
		entityData.set(STRAIN, tag.getFloat("Strain"));
		NbtUtils.readBlockPos(tag, "Loom").ifPresent(pos -> entityData.set(LOOM, pos));
	}
}
