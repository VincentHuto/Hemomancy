package com.vincenthuto.hemomancy.common.entity.boss.saint.velorum;

import com.vincenthuto.hemomancy.common.entity.SyncedOwner;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintArena;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintCompanion;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * One of Velorum's congregation, woken from its rime (design section 9). It is a frozen devotee block taken up off the
 * floor by the arena and put back by it: it thaws to standing in its block's pose, fights beside the challengers in a
 * proper fight (a two-handed blow on him every 30 ticks, the grip, the shelter) or goes for the hive in a forced one
 * (the grip and its blow), falls still where it stands at no health, and walks home to kneel when his body fails. It
 * runs no goals: its few actions are its own tick. One that loads with no live Velorum discards itself.
 */
public class VelorumDevoteeEntity extends PathfinderMob implements SaintCompanion {
	public enum State { THAWING, AWAKE, STILLED, RETURNING, KNEELING }

	private static final EntityDataAccessor<Integer> DATA_STATE =
			SynchedEntityData.defineId(VelorumDevoteeEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_STATE_START =
			SynchedEntityData.defineId(VelorumDevoteeEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_POSE =
			SynchedEntityData.defineId(VelorumDevoteeEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> DATA_HOSTILE =
			SynchedEntityData.defineId(VelorumDevoteeEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> DATA_GRIPPING =
			SynchedEntityData.defineId(VelorumDevoteeEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Integer> DATA_BLOW_START =
			SynchedEntityData.defineId(VelorumDevoteeEntity.class, EntityDataSerializers.INT);
	private static final SyncedOwner OWNER = SyncedOwner.define(VelorumDevoteeEntity.class);

	/** Ticks it waits for its Saint to load before it gives up. */
	private static final int OWNER_GRACE = 40;
	private static final double WALK_SPEED = 0.7D;
	private static final double REACH = 1.6D;
	private static final double HOME_REACH = 0.6D;
	/** The blow's wind-up, as its strike track times it. */
	public static final int BLOW_WINDUP = 12;

	@Nullable
	private BlockPos home;
	private Direction facing = Direction.NORTH;
	private int blowCooldown;
	private int blowAt = -1;
	private int gripTicks;
	@Nullable
	private UUID gripped;
	private int orphanTicks;

	public VelorumDevoteeEntity(EntityType<? extends VelorumDevoteeEntity> type, Level level) {
		super(type, level);
		this.setPersistenceRequired();
	}

	public static AttributeSupplier.Builder setAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, VelorumCongregationRules.HEALTH)
				.add(Attributes.ARMOR, VelorumCongregationRules.ARMOR)
				.add(Attributes.MOVEMENT_SPEED, 0.22D)
				.add(Attributes.ATTACK_DAMAGE, VelorumCongregationRules.BLOW)
				.add(Attributes.FOLLOW_RANGE, 32.0D)
				.add(Attributes.KNOCKBACK_RESISTANCE, 0.6D);
	}

	/** Takes a woken figure up off the floor: its Saint, side, place and pose, thawing from the first tick. */
	public static VelorumDevoteeEntity wake(ServerLevel level, VelorumEntity saint, BlockPos home, Direction facing,
			int pose, boolean hostile) {
		VelorumDevoteeEntity devotee = new VelorumDevoteeEntity(
				com.vincenthuto.hemomancy.common.init.EntityInit.velorum_devotee.get(), level);
		devotee.home = home.immutable();
		devotee.facing = facing;
		OWNER.set(devotee, saint.getUUID());
		devotee.entityData.set(DATA_POSE, pose);
		devotee.entityData.set(DATA_HOSTILE, hostile);
		float yaw = facing.toYRot();
		devotee.moveTo(home.getX() + 0.5D, home.getY(), home.getZ() + 0.5D, yaw, 0.0F);
		devotee.setYBodyRot(yaw);
		devotee.setYHeadRot(yaw);
		devotee.state(State.THAWING);
		level.addFreshEntity(devotee);
		return devotee;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_STATE, State.THAWING.ordinal());
		builder.define(DATA_STATE_START, 0);
		builder.define(DATA_POSE, 0);
		builder.define(DATA_HOSTILE, false);
		builder.define(DATA_GRIPPING, false);
		builder.define(DATA_BLOW_START, -1);
		OWNER.defineDefault(builder);
	}

	// ── what it is ──────────────────────────────────────────────────

	@Override
	public UUID saintId() {
		UUID id = OWNER.get(this);
		return id == null ? getUUID() : id;
	}

	@Override
	public Allegiance allegiance() {
		return hostile() ? Allegiance.SAINT : Allegiance.CHALLENGERS;
	}

	public boolean hostile() {
		return entityData.get(DATA_HOSTILE);
	}

	public State state() {
		int ordinal = entityData.get(DATA_STATE);
		State[] states = State.values();
		return states[Math.max(0, Math.min(states.length - 1, ordinal))];
	}

	/** The game time the state began, for the client's tracks. */
	public int stateStart() {
		return entityData.get(DATA_STATE_START);
	}

	/** The block pose it woke from: kneeling, bowed, reaching, prostrate, striding. */
	public int figurePose() {
		return entityData.get(DATA_POSE);
	}

	public boolean gripping() {
		return entityData.get(DATA_GRIPPING);
	}

	/** The game time its last blow began, -1 for none. */
	public int blowStart() {
		return entityData.get(DATA_BLOW_START);
	}

	public boolean awake() {
		return state() == State.AWAKE;
	}

	@Nullable
	public BlockPos home() {
		return home;
	}

	public Direction facing() {
		return facing;
	}

	private void state(State next) {
		entityData.set(DATA_STATE, next.ordinal());
		entityData.set(DATA_STATE_START, (int) level().getGameTime());
		if (next != State.AWAKE) entityData.set(DATA_GRIPPING, false);
	}

	/** At no health, or recalled: down into kneeling where it stands, under new rime. */
	public void fallStill() {
		if (state() == State.STILLED || level().isClientSide) return;
		state(State.STILLED);
		getNavigation().stop();
		setTarget(null);
		gripped = null;
		playSound(SoundInit.ENTITY_WILL_FALTER.get(), 1.0F, 0.7F);
	}

	/** Rewoken from where it fell still. */
	public void rewake() {
		if (state() != State.STILLED) return;
		setHealth(getMaxHealth());
		state(State.THAWING);
	}

	/** The ending: stop, turn toward him, then walk home. */
	public void sendHome() {
		if (state() == State.RETURNING || state() == State.KNEELING) return;
		state(State.RETURNING);
		setTarget(null);
		gripped = null;
	}

	public boolean isHome() {
		return state() == State.KNEELING;
	}

	// ── the tick ─────────────────────────────────────────────────────

	@Override
	protected void customServerAiStep() {
		super.customServerAiStep();
		if (!(level() instanceof ServerLevel server)) return;
		VelorumEntity saint = saint(server);
		if (saint == null) {
			if (++orphanTicks > OWNER_GRACE) discard();
			return;
		}
		orphanTicks = 0;
		if (blowCooldown > 0) blowCooldown--;
		long now = server.getGameTime();
		long age = now - stateStart();
		switch (state()) {
			case THAWING -> {
				getNavigation().stop();
				if (age >= VelorumCongregationRules.thawTicks(figurePose())) state(State.AWAKE);
			}
			case AWAKE -> fight(saint);
			case STILLED, KNEELING -> getNavigation().stop();
			case RETURNING -> walkHome(saint, age);
		}
	}

	private void fight(VelorumEntity saint) {
		if (saint.ending()) {
			getNavigation().stop();
			return;
		}
		if (hostile()) {
			hunt(saint);
		} else {
			serve(saint);
		}
	}

	/** An ally walks stiffly to Velorum and strikes him every 30 ticks. */
	private void serve(VelorumEntity saint) {
		double distance = distanceTo(saint);
		getLookControl().setLookAt(saint, 10.0F, 10.0F);
		if (distance > REACH) {
			getNavigation().moveTo(saint, WALK_SPEED);
		} else {
			getNavigation().stop();
		}
		strikeWhenReady(saint, distance);
	}

	/** A hostile goes for the nearest challenger, grips them, then strikes; faster and colder in the dark. */
	private void hunt(VelorumEntity saint) {
		Player quarry = saint.nearestChallenger(position());
		if (quarry == null) {
			getNavigation().stop();
			return;
		}
		setTarget(quarry);
		getLookControl().setLookAt(quarry, 10.0F, 10.0F);
		double distance = distanceTo(quarry);
		if (gripped != null) {
			if (!quarry.getUUID().equals(gripped) || distance > REACH * 1.5D) {
				releaseGrip();
			} else {
				getNavigation().stop();
				gripTicks++;
				VelorumMoves.slow(quarry, 5, VelorumMoves.SLOWNESS_II);
				saint.stillness().add(saint, quarry, VelorumCongregationRules.gripFill(saint.dark().inDark(quarry)));
				if (gripTicks >= VelorumCongregationRules.HOSTILE_GRIP_TICKS) {
					releaseGrip();
					strike(quarry);
				}
				return;
			}
		}
		if (distance > REACH) {
			double speed = saint.dark().darkAt(saint, position()) ? WALK_SPEED * VelorumCongregationRules.DARK_SPEED : WALK_SPEED;
			getNavigation().moveTo(quarry, speed);
		} else if (blowCooldown <= 0) {
			getNavigation().stop();
			gripped = quarry.getUUID();
			gripTicks = 0;
			entityData.set(DATA_GRIPPING, true);
		}
	}

	private void releaseGrip() {
		gripped = null;
		gripTicks = 0;
		blowCooldown = VelorumCongregationRules.BLOW_GAP;
		entityData.set(DATA_GRIPPING, false);
	}

	private void strikeWhenReady(LivingEntity target, double distance) {
		if (blowAt >= 0) {
			if (--blowAt <= 0) {
				blowAt = -1;
				if (distanceTo(target) <= REACH + 0.5D) strike(target);
			}
			return;
		}
		if (blowCooldown <= 0 && distance <= REACH) {
			blowAt = BLOW_WINDUP;
			entityData.set(DATA_BLOW_START, (int) level().getGameTime());
		}
	}

	private void strike(LivingEntity target) {
		blowCooldown = VelorumCongregationRules.BLOW_GAP;
		if (!ManipulationCombatHelper.canHarmFor(this, target)) return;
		entityData.set(DATA_BLOW_START, (int) level().getGameTime());
		DamageSource source = damageSources().source(DamageTypes.MOB_ATTACK, this);
		target.hurt(source, VelorumCongregationRules.BLOW);
		playSound(SoundEvents.GLASS_HIT, 0.8F, 0.6F);
	}

	private void walkHome(VelorumEntity saint, long age) {
		if (home == null) {
			discard();
			return;
		}
		if (age < VelorumEndingRules.WALK_HOME - VelorumEndingRules.DEVOTEES_TURN) {
			// the faithful stop where they stand and turn toward him
			getNavigation().stop();
			getLookControl().setLookAt(saint, 10.0F, 10.0F);
			return;
		}
		Vec3 target = Vec3.atBottomCenterOf(home);
		if (position().distanceTo(target) > HOME_REACH) {
			getNavigation().moveTo(target.x, target.y, target.z, WALK_SPEED);
			return;
		}
		getNavigation().stop();
		moveTo(target.x, target.y, target.z, facing.toYRot(), 0.0F);
		state(State.KNEELING);
		saint.congregation().arrived(saint, this);
	}

	@Nullable
	private VelorumEntity saint(ServerLevel server) {
		UUID id = OWNER.get(this);
		if (id == null) return null;
		Entity entity = server.getEntity(id);
		return entity instanceof VelorumEntity velorum && velorum.isAlive() && !velorum.isRemoved() ? velorum : null;
	}

	// ── damage ───────────────────────────────────────────────────────

	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (level().isClientSide) return false;
		State state = state();
		if (state == State.STILLED || state == State.RETURNING || state == State.KNEELING || state == State.THAWING) {
			return source.is(DamageTypes.GENERIC_KILL) && super.hurt(source, amount);
		}
		// the challengers never strike their own; the hostile let go when struck
		if (!hostile() && source.getEntity() instanceof Player) return false;
		if (hostile() && gripped != null) releaseGrip();
		return super.hurt(source, amount);
	}

	/** At no health it falls still where it stands instead of dying, and can be rewoken there. */
	@Override
	public void setHealth(float health) {
		if (health <= 0.0F && !level().isClientSide && state() != State.STILLED && isAddedToLevel()) {
			super.setHealth(1.0F);
			fallStill();
			return;
		}
		super.setHealth(health);
	}

	@Override
	public boolean isPushable() {
		return state() == State.AWAKE;
	}

	@Override
	public boolean removeWhenFarAway(double distance) {
		return false;
	}

	@Override
	public boolean canUsePortal(boolean allowPassengers) {
		return false;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.GLASS_HIT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.GLASS_BREAK;
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		OWNER.save(this, tag);
		if (home != null) tag.putLong("Home", home.asLong());
		tag.putInt("Facing", facing.get2DDataValue());
		tag.putInt("FigurePose", figurePose());
		tag.putBoolean("Hostile", hostile());
		tag.putInt("DevoteeState", state().ordinal());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		OWNER.load(this, tag);
		home = tag.contains("Home") ? BlockPos.of(tag.getLong("Home")) : null;
		facing = Direction.from2DDataValue(tag.getInt("Facing"));
		entityData.set(DATA_POSE, tag.getInt("FigurePose"));
		entityData.set(DATA_HOSTILE, tag.getBoolean("Hostile"));
		entityData.set(DATA_STATE, tag.getInt("DevoteeState"));
	}

	/** The arena it belongs to, through its Saint; null when the Saint is gone. */
	@Nullable
	public SaintArena arena() {
		return level() instanceof ServerLevel server && saint(server) != null ? saint(server).arena() : null;
	}
}
