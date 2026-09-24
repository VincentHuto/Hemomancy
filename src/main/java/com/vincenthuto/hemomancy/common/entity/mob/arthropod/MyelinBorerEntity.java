package com.vincenthuto.hemomancy.common.entity.mob.arthropod;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ServerLevelAccessor;

/**
 * The Cortical Drift's fiber crawler - a blind, segmented grub that grips the biome's nerve-bridge
 * network instead of walking on the islands. Left alone it MENDS the cables, closing gaps a severed
 * run has left behind. Provoked - by a player breaking network blocks nearby, striking one, or killing
 * one within earshot - it turns and BORES, chewing fibers out of the bridges and biting with Neural
 * Overload. It calms after {@link MyelinBorerMovementRules#AGITATION_TICKS} ticks and the survivors
 * repair what they tore. It never eats synaptic nodes, so the network's topology always survives.
 */
public class MyelinBorerEntity extends Monster {

	public static AttributeSupplier.Builder setAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 8.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.2D)
				.add(Attributes.ATTACK_DAMAGE, 2.0D);
	}

	public MyelinBorerEntity(EntityType<? extends MyelinBorerEntity> type, Level worldIn) {
		super(type, worldIn);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(2, new BoreFiberGoal(this, 1.0D, 12));
		this.goalSelector.addGoal(3, new MendFiberGoal(this, 1.0D, 12));
		// The cable controller below handles patrol and pursuit. Vanilla path goals leave the web.
		this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 6.0F));
		this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
		// Only a provoked Borer hunts; a calm one is busy mending and ignores players entirely.
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
				target -> this.isAgitated()));
	}

	public static final TagKey<Block> CRAWLABLE = BlockTags.create(Hemomancy.rloc("cortical_crawlable"));

	private static final EntityDataAccessor<Integer> AGITATION =
			SynchedEntityData.defineId(MyelinBorerEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> BLINK_PHASE =
			SynchedEntityData.defineId(MyelinBorerEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> CRAWLING =
			SynchedEntityData.defineId(MyelinBorerEntity.class, EntityDataSerializers.BOOLEAN);
	private static final int BLINK_RANGE = 6;
	private int blinkCooldown;
	private int blinkCharge;
	private int blinkArrival;
	private Vec3 blinkLanding;
	private BlockPos cableAnchor;
	private BlockPos previousAnchor;
	private BlockPos workTarget;
	private int biteCooldown;

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(AGITATION, 0);
		builder.define(BLINK_PHASE, 0);
		builder.define(CRAWLING, false);
	}

	public int getAgitation() {
		return this.entityData.get(AGITATION);
	}

	public void setAgitation(int ticks) {
		this.entityData.set(AGITATION, Math.max(0, ticks));
		if (ticks <= 0 && !this.level().isClientSide) {
			this.setTarget(null);
		}
	}

	/** A fresh provocation restarts the full timer. */
	public void provoke() {
		setAgitation(MyelinBorerMovementRules.raiseAgitation(getAgitation()));
	}

	public boolean isAgitated() {
		return MyelinBorerMovementRules.isAgitated(getAgitation());
	}

	public boolean isBlinking() {
		return this.entityData.get(BLINK_PHASE) > 0;
	}

	public boolean isCrawling() {
		return this.entityData.get(CRAWLING);
	}

	public void setWorkTarget(BlockPos target) {
		this.workTarget = target;
	}

	/** Starts a short bridge-to-bridge blink only when the cable run between positions is broken. */
	public boolean tryBlinkToward(BlockPos target) {
		if (!(level() instanceof ServerLevel server) || blinkCooldown > 0 || blinkCharge > 0
				|| blinkArrival > 0 || !isGripping()) {
			return false;
		}
		BlockPos anchor = cableAnchor != null ? cableAnchor : nearestCable(blockPosition());
		if (anchor == null) {
			return false;
		}
		Vec3 selected = null;
		double best = target.distSqr(blockPosition());
		for (BlockPos cable : BlockPos.betweenClosed(anchor.offset(-BLINK_RANGE, -2, -BLINK_RANGE),
				anchor.offset(BLINK_RANGE, 2, BLINK_RANGE))) {
			if (!server.hasChunkAt(cable) || !server.getBlockState(cable).is(CRAWLABLE)
					|| cable.distSqr(anchor) > BLINK_RANGE * BLINK_RANGE
					|| cable.distSqr(blockPosition()) > BLINK_RANGE * BLINK_RANGE
					|| cable.distSqr(anchor) < 4
					|| !hasCableGap(anchor, cable)) {
				continue;
			}
			Vec3 landing = cablePerch(cable);
			if (landing == null) {
				continue;
			}
			double distance = target.distSqr(cable);
			if (distance + 1.0D < best) {
				best = distance;
				selected = landing;
			}
		}
		if (selected == null) {
			return false;
		}
		blinkLanding = selected;
		blinkCharge = 4;
		blinkCooldown = 30;
		entityData.set(BLINK_PHASE, blinkCharge);
		getNavigation().stop();
		setDeltaMovement(Vec3.ZERO);
		return true;
	}

	private BlockPos nearestCable(BlockPos around) {
		BlockPos best = null;
		double distance = 10.0D;
		for (BlockPos pos : BlockPos.betweenClosed(around.offset(-2, -2, -2), around.offset(2, 2, 2))) {
			Vec3 perch = cablePerch(pos);
			if (perch != null && perch.distanceToSqr(position()) < distance) {
				best = pos.immutable();
				distance = perch.distanceToSqr(position());
			}
		}
		return best;
	}

	/** Pull borers left on the shell by older saves back onto the loaded interior web. */
	private void recoverStrandedBorer() {
		if (tickCount != 20 && tickCount % 200 != 0) {
			return;
		}
		BlockPos origin = blockPosition();
		BlockPos best = null;
		double distance = Double.POSITIVE_INFINITY;
		for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-16, -8, -16),
				origin.offset(16, 56, 16))) {
			Vec3 perch = cablePerch(pos);
			if (perch != null && perch.distanceToSqr(position()) < distance) {
				best = pos.immutable();
				distance = perch.distanceToSqr(position());
			}
		}
		if (best != null) {
			cableAnchor = best;
			Vec3 perch = cablePerch(best);
			teleportTo(perch.x, perch.y, perch.z);
			setDeltaMovement(Vec3.ZERO);
		}
	}

	private Vec3 cablePerch(BlockPos cable) {
		if (!level().hasChunkAt(cable) || !level().getBlockState(cable).is(CRAWLABLE)
				|| !level().getBlockState(cable.above()).isAir()) {
			return null;
		}
		Vec3 perch = new Vec3(cable.getX() + 0.5D, cable.getY() + 1.0D, cable.getZ() + 0.5D);
		return level().noCollision(this, getBoundingBox().move(perch.subtract(position()))) ? perch : null;
	}

	private boolean hasCableGap(BlockPos from, BlockPos to) {
		int steps = (int) Math.ceil(Math.sqrt(from.distSqr(to)) * 2.0D);
		for (int step = 1; step < steps; step++) {
			double t = (double) step / steps;
			BlockPos sample = BlockPos.containing(from.getX() + 0.5D + (to.getX() - from.getX()) * t,
					from.getY() + 0.5D + (to.getY() - from.getY()) * t,
					from.getZ() + 0.5D + (to.getZ() - from.getZ()) * t);
			if (!level().getBlockState(sample).is(CRAWLABLE)) {
				return true;
			}
		}
		return false;
	}

	/** True while touching any nerve-network block. */
	public boolean isGripping() {
		if (cableAnchor != null && cablePerch(cableAnchor) != null
				&& position().distanceToSqr(cablePerch(cableAnchor)) < 4.0D) {
			return true;
		}
		BlockPos pos = this.blockPosition();
		if (this.level().getBlockState(pos).is(CRAWLABLE)) {
			return true;
		}
		for (Direction direction : Direction.values()) {
			if (this.level().getBlockState(pos.relative(direction)).is(CRAWLABLE)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onClimbable() {
		return isGripping();
	}

	@Override
	public float maxUpStep() {
		return isGripping() ? MyelinBorerMovementRules.CLIMB_STEP_HEIGHT : super.maxUpStep();
	}

	private void crawlOnCable() {
		if (cableAnchor == null || cablePerch(cableAnchor) == null) {
			cableAnchor = nearestCable(blockPosition());
		}
		if (cableAnchor == null) {
			setNoGravity(false);
			entityData.set(CRAWLING, false);
			recoverStrandedBorer();
			return;
		}
		setNoGravity(true);
		Vec3 perch = cablePerch(cableAnchor);
		if (perch == null) {
			return;
		}
		BlockPos target = getTarget() != null && isAgitated() ? getTarget().blockPosition() : workTarget;
		if (position().distanceToSqr(perch) < 0.035D && !isBlinking()) {
			BlockPos next = chooseNextCable(target);
			if (next != null) {
				previousAnchor = cableAnchor;
				cableAnchor = next;
				perch = cablePerch(next);
			} else if (target != null) {
				tryBlinkToward(target);
			} else if (tickCount % 40 == 0) {
				blinkToNearbyRun();
			}
		}
		Vec3 offset = perch.subtract(position());
		if (offset.lengthSqr() > 0.0004D && !isBlinking()) {
			// A knock is retracted to the same cable before it can become a fall.
			Vec3 step = offset.lengthSqr() > 1.0D ? offset : offset.normalize().scale(0.085D);
			if (step.lengthSqr() > offset.lengthSqr()) {
				step = offset;
			}
			setPos(position().add(step));
			setYRot((float) (Math.atan2(-step.x, step.z) * 180.0D / Math.PI));
			entityData.set(CRAWLING, true);
		} else {
			entityData.set(CRAWLING, false);
		}
		setDeltaMovement(Vec3.ZERO);
	}

	private void blinkToNearbyRun() {
		BlockPos destination = null;
		int seen = 0;
		for (BlockPos pos : BlockPos.betweenClosed(cableAnchor.offset(-BLINK_RANGE, -2, -BLINK_RANGE),
				cableAnchor.offset(BLINK_RANGE, 2, BLINK_RANGE))) {
			if (pos.distSqr(cableAnchor) < 4 || cablePerch(pos) == null || !hasCableGap(cableAnchor, pos)) {
				continue;
			}
			if (random.nextInt(++seen) == 0) {
				destination = pos.immutable();
			}
		}
		if (destination != null) {
			tryBlinkToward(destination);
		}
	}

	private BlockPos chooseNextCable(BlockPos target) {
		if (target != null) {
			return nextStepToward(target);
		}
		BlockPos best = null;
		double bestScore = Double.POSITIVE_INFINITY;
		for (BlockPos nearby : BlockPos.betweenClosed(cableAnchor.offset(-1, -1, -1),
				cableAnchor.offset(1, 1, 1))) {
			if (nearby.equals(cableAnchor) || nearby.equals(previousAnchor) || cablePerch(nearby) == null) {
				continue;
			}
			double score = random.nextDouble();
			if (score < bestScore) {
				best = nearby.immutable();
				bestScore = score;
			}
		}
		if (best == null) {
			previousAnchor = null;
		}
		return best;
	}

	/** Search the local cable graph so bends can briefly lead away from a work site. */
	private BlockPos nextStepToward(BlockPos target) {
		ArrayDeque<BlockPos> queue = new ArrayDeque<>();
		Set<BlockPos> visited = new HashSet<>();
		Map<BlockPos, BlockPos> firstSteps = new HashMap<>();
		queue.add(cableAnchor);
		visited.add(cableAnchor);
		BlockPos best = null;
		double bestDistance = target.distSqr(cableAnchor);
		while (!queue.isEmpty() && visited.size() < 256) {
			BlockPos current = queue.removeFirst();
			for (BlockPos neighbor : BlockPos.betweenClosed(current.offset(-1, -1, -1),
					current.offset(1, 1, 1))) {
				BlockPos step = neighbor.immutable();
				if (step.distSqr(cableAnchor) > 144 || visited.contains(step)) {
					continue;
				}
				if (cablePerch(step) == null) {
					continue;
				}
				visited.add(step);
				BlockPos first = current.equals(cableAnchor) ? step : firstSteps.get(current);
				firstSteps.put(step, first);
				queue.addLast(step);
				double distance = step.distSqr(target);
				if (distance < bestDistance) {
					bestDistance = distance;
					best = first;
				}
			}
		}
		return best;
	}

	@Override
	public void tick() {
		super.tick();
		if (this.level().isClientSide) {
			return;
		}
		setAgitation(MyelinBorerMovementRules.decayAgitation(getAgitation()));
		if (biteCooldown > 0) {
			biteCooldown--;
		}
		if (getTarget() != null && isAgitated() && distanceToSqr(getTarget()) < 2.5D && biteCooldown == 0) {
			doHurtTarget(getTarget());
			biteCooldown = 20;
		}
		if (blinkCooldown > 0) {
			blinkCooldown--;
		}
		if (blinkCharge > 0) {
			MyelinBorerBlinkEffects.ball((ServerLevel) level(), position().add(0, getBbHeight() * 0.5D, 0));
			setDeltaMovement(Vec3.ZERO);
			if (--blinkCharge == 0 && blinkLanding != null) {
				Vec3 start = position().add(0, getBbHeight() * 0.5D, 0);
				teleportTo(blinkLanding.x, blinkLanding.y, blinkLanding.z);
				MyelinBorerBlinkEffects.zap((ServerLevel) level(), start,
						position().add(0, getBbHeight() * 0.5D, 0));
				blinkLanding = null;
				cableAnchor = blockPosition().below();
				previousAnchor = null;
				blinkArrival = 3;
			}
			entityData.set(BLINK_PHASE, blinkCharge + blinkArrival);
		} else if (blinkArrival > 0) {
			MyelinBorerBlinkEffects.ball((ServerLevel) level(), position().add(0, getBbHeight() * 0.5D, 0));
			entityData.set(BLINK_PHASE, --blinkArrival);
		}
		if (blinkCharge == 0 && blinkArrival == 0) {
			crawlOnCable();
		}
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		boolean hurt = super.hurt(source, amount);
		if (hurt && !this.level().isClientSide) {
			MyelinBorerAgitationEvents.provokeNearby((net.minecraft.server.level.ServerLevel) this.level(),
					this.getBoundingBox(), source.getEntity() instanceof Player player ? player : null);
		}
		return hurt;
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putInt("Agitation", getAgitation());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		setAgitation(tag.getInt("Agitation"));
	}

	@Override
	protected int calculateFallDamage(float distance, float damageMultiplier) {
		return 0;
	}

	@Override
	public boolean doHurtTarget(Entity target) {
		boolean flag = super.doHurtTarget(target);
		if (flag && target instanceof LivingEntity living) {
			// Stack neural_overload: if already present, bump amplifier
			MobEffectInstance existing = living.getEffect(EffectInit.neural_overload);
			int newAmp = existing != null ? Math.min(existing.getAmplifier() + 1, 4) : 0;
			living.addEffect(new MobEffectInstance(EffectInit.neural_overload, 200, newAmp));
		}
		return flag;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundInit.ENTITY_MYELIN_BORER_AMBIENT.get();
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundInit.ENTITY_MYELIN_BORER_DEATH.get();
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return SoundInit.ENTITY_MYELIN_BORER_HURT.get();
	}

	@Override
	protected float getSoundVolume() {
		return 0.3f;
	}

	public static boolean canSpawnHere(EntityType<? extends Monster> pType, ServerLevelAccessor pLevel,
			MobSpawnType pSpawnType, BlockPos pPos, RandomSource pRandom) {
		if (pLevel.getDifficulty() == Difficulty.PEACEFUL) {
			return false;
		}
		return pLevel.getBlockState(pPos.below()).is(CRAWLABLE)
				&& pLevel.getBlockState(pPos).isAir()
				&& pLevel.getBlockState(pPos.above()).isAir();
	}

}
