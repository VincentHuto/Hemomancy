package com.vincenthuto.hemomancy.common.entity.manipulation;

import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.manipulation.animus.LivingLandTerrainRules;
import com.vincenthuto.hemomancy.common.world.livingland.LivingLandLedger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * One Living Land strike (core-loop Track 9): a short column of real ground, held by blood, that runs out of its
 * patch to an enemy, lands once and goes back. The entity is the head; the blocks follow it along a curve from their
 * socket ({@link LivingLandTerrainRules#blockCentre}). The blocks themselves are recorded in the level's
 * {@link LivingLandLedger}, which the strike touches every tick: if the strike unloads, is removed, or the server
 * stops, the ledger's sweep puts the blocks back. A conjured strike (block lifting turned off in the server config)
 * carries copies and has no record.
 */
public final class LivingLandStrikeEntity extends Entity {
	private static final int MAX_BLOCKS = LivingLandTerrainRules.MAX_LENGTH;
	private static final EntityDataAccessor<Vector3f> ROOT =
			SynchedEntityData.defineId(LivingLandStrikeEntity.class, EntityDataSerializers.VECTOR3);
	private static final EntityDataAccessor<Byte> FACE =
			SynchedEntityData.defineId(LivingLandStrikeEntity.class, EntityDataSerializers.BYTE);
	private static final EntityDataAccessor<Integer> LENGTH =
			SynchedEntityData.defineId(LivingLandStrikeEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> RETURNING =
			SynchedEntityData.defineId(LivingLandStrikeEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> CONJURED =
			SynchedEntityData.defineId(LivingLandStrikeEntity.class, EntityDataSerializers.BOOLEAN);
	/** A raise's marker: it carries nothing and stands at its patch, so the blood holding the raised ground is drawn. */
	private static final EntityDataAccessor<Boolean> RAISE =
			SynchedEntityData.defineId(LivingLandStrikeEntity.class, EntityDataSerializers.BOOLEAN);
	private static final List<EntityDataAccessor<Integer>> STATES = List.of(
			SynchedEntityData.defineId(LivingLandStrikeEntity.class, EntityDataSerializers.INT),
			SynchedEntityData.defineId(LivingLandStrikeEntity.class, EntityDataSerializers.INT),
			SynchedEntityData.defineId(LivingLandStrikeEntity.class, EntityDataSerializers.INT),
			SynchedEntityData.defineId(LivingLandStrikeEntity.class, EntityDataSerializers.INT),
			SynchedEntityData.defineId(LivingLandStrikeEntity.class, EntityDataSerializers.INT));

	@Nullable private UUID owner;
	@Nullable private UUID record;
	@Nullable private UUID target;
	private float damage = 6.0F;
	private int age;
	private boolean landed;
	private int lingered;
	private final List<BlockState> carried = new ArrayList<>();

	public LivingLandStrikeEntity(EntityType<? extends LivingLandStrikeEntity> type, Level level) {
		super(type, level);
		noPhysics = true;
		setNoGravity(true);
	}

	/**
	 * Sets the strike out: {@code blocks} in column order (the surface block first), lifted already and recorded under
	 * {@code record} (null for a conjured strike), leaving from {@code root} on {@code face} for {@code target}.
	 */
	public void launch(ServerPlayer owner, @Nullable UUID record, List<BlockState> blocks, Vec3 root, Direction face,
			LivingEntity target, float damage) {
		this.owner = owner.getUUID();
		this.record = record;
		this.target = target.getUUID();
		this.damage = damage;
		carried.clear();
		carried.addAll(blocks.subList(0, Math.min(blocks.size(), MAX_BLOCKS)));
		entityData.set(ROOT, root.toVector3f());
		entityData.set(FACE, (byte) face.get3DDataValue());
		entityData.set(LENGTH, carried.size());
		entityData.set(CONJURED, record == null);
		for (int i = 0; i < carried.size(); i++) entityData.set(STATES.get(i), Block.getId(carried.get(i)));
		setPos(root);
	}

	/**
	 * Stands this entity at a raise as its marker: no blocks, no record, never saved. {@code root} is the middle of the
	 * patch's face; the raised cells stack out of it along {@code face}.
	 */
	public void markRaise(ServerPlayer owner, Vec3 root, Direction face) {
		this.owner = owner.getUUID();
		entityData.set(RAISE, true);
		entityData.set(CONJURED, true);
		entityData.set(ROOT, root.toVector3f());
		entityData.set(FACE, (byte) face.get3DDataValue());
		entityData.set(LENGTH, 0);
		setPos(root);
	}

	/** How many blocks the marked raise stands. */
	public void setRaised(int count) {
		entityData.set(LENGTH, Math.max(0, Math.min(count, MAX_BLOCKS)));
	}

	public boolean isRaise() {
		return entityData.get(RAISE);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(ROOT, new Vector3f());
		builder.define(FACE, (byte) Direction.UP.get3DDataValue());
		builder.define(LENGTH, 0);
		builder.define(RETURNING, false);
		builder.define(CONJURED, false);
		builder.define(RAISE, false);
		int air = Block.getId(Blocks.AIR.defaultBlockState());
		for (EntityDataAccessor<Integer> state : STATES) builder.define(state, air);
	}

	@Override
	public void tick() {
		super.tick();
		if (!(level() instanceof ServerLevel level)) return;
		age++;
		if (isRaise()) {
			// A marker lives exactly as long as its player's patch.
			ServerPlayer holder = owner == null ? null : level.getServer().getPlayerList().getPlayer(owner);
			if (holder == null || !com.vincenthuto.hemomancy.common.manipulation.animus.LivingLandSculpting.hasPatch(holder)) discard();
			return;
		}
		LivingLandLedger ledger = LivingLandLedger.get(level);
		if (record != null) {
			// Settled by the sweep while this strike was away (unloaded, or the server restarted): nothing to carry.
			if (!ledger.holds(record)) {
				discard();
				return;
			}
			ledger.touch(record, level.getGameTime());
		}
		ServerPlayer caster = owner == null ? null : level.getServer().getPlayerList().getPlayer(owner);
		if (age > LivingLandTerrainRules.SETTLE_DEADLINE_TICKS) {
			settleAndDiscard(level, caster);
			return;
		}
		if (!isReturning()) {
			if (landed) {
				// It holds a beat in the enemy before it goes home.
				if (++lingered >= LivingLandTerrainRules.LINGER_TICKS) entityData.set(RETURNING, true);
				return;
			}
			Entity found = target == null ? null : level.getEntity(target);
			LivingEntity enemy = found instanceof LivingEntity living && living.isAlive() ? living : null;
			if (caster == null || !caster.isAlive() || enemy == null || !ManipulationCombatHelper.canHarm(caster, enemy)
					|| age > LivingLandTerrainRules.REACH_TICKS) {
				entityData.set(RETURNING, true);
			} else {
				Vec3 aim = enemy.getBoundingBox().getCenter();
				advance(aim, LivingLandTerrainRules.OUT_SPEED);
				if (enemy.getBoundingBox().inflate(LivingLandTerrainRules.LAND_MARGIN).contains(position())) land(level, caster, enemy);
			}
			return;
		}
		Vec3 root = root();
		advance(root, LivingLandTerrainRules.BACK_SPEED);
		if (position().distanceToSqr(root) < 0.09D) settleAndDiscard(level, caster);
	}

	/** The channel let go: turn for home without landing. */
	public void recall() {
		entityData.set(RETURNING, true);
	}

	private void advance(Vec3 toward, double speed) {
		Vec3 step = toward.subtract(position());
		double length = step.length();
		Vec3 next = length <= speed ? toward : position().add(step.scale(speed / length));
		setDeltaMovement(next.subtract(position()));
		setPos(next);
	}

	/** One landing: school damage through the caster's harm rules, a shove by mode, and the turn for home. */
	private void land(ServerLevel level, ServerPlayer caster, LivingEntity enemy) {
		landed = true;
		setDeltaMovement(Vec3.ZERO);
		Direction face = face();
		if (ManipulationCombatHelper.hurt(ManipulationInit.living_land.get(), caster, enemy, level, damage)) {
			Vec3 shove = switch (LivingLandTerrainRules.Mode.forFace(face)) {
				case FLOOR_TEETH -> new Vec3(0.0D, 0.65D, 0.0D);
				case CEILING_CRUSH -> new Vec3(0.0D, -0.5D, 0.0D);
				case WALL_LANCES -> Vec3.atLowerCornerOf(face.getNormal()).scale(0.7D).add(0.0D, 0.18D, 0.0D);
			};
			enemy.setDeltaMovement(enemy.getDeltaMovement().add(shove));
			enemy.hurtMarked = true;
			HarbingerAdvancementGranter.grantIfNotDone(caster, HarbingerAdvancementGranter.ADV_LAND_STRIKE);
		}
		Vec3 at = position();
		ManipulationVisuals.burst(level, ManipulationVisuals.Form.ANIMUS_IMPACT, at, at.add(getDeltaMovement()), 0.9D, 12);
		BlockState head = carried.isEmpty() ? Blocks.STONE.defaultBlockState() : carried.getFirst();
		level.playSound(null, at.x, at.y, at.z, head.getSoundType().getBreakSound(), SoundSource.PLAYERS, 1.1F, 0.6F);
	}

	private void settleAndDiscard(ServerLevel level, @Nullable ServerPlayer caster) {
		if (record == null || LivingLandLedger.get(level).settle(level, record, caster)) {
			Vec3 at = root();
			BlockState head = carried.isEmpty() ? Blocks.STONE.defaultBlockState() : carried.getFirst();
			level.playSound(null, at.x, at.y, at.z, head.getSoundType().getPlaceSound(), SoundSource.PLAYERS, 0.9F, 0.7F);
			record = null;
			discard();
		}
	}

	/**
	 * Removed for good (killed or discarded by anything but its own settle): the blocks go back now. An unload keeps
	 * the record, and the ledger's sweep returns the blocks if the strike does not come back to carry them.
	 */
	@Override
	public void remove(RemovalReason reason) {
		if (record != null && reason.shouldDestroy() && level() instanceof ServerLevel level) {
			LivingLandLedger.get(level).settle(level, record,
					owner == null ? null : level.getServer().getPlayerList().getPlayer(owner));
			record = null;
		}
		super.remove(reason);
	}

	@Override
	public boolean canChangeDimensions(Level from, Level to) {
		return false;
	}

	@Override
	public boolean isPickable() {
		return false;
	}

	@Override
	public boolean shouldBeSaved() {
		return record != null && super.shouldBeSaved();
	}

	// ── What both sides read ──

	public Vec3 root() {
		return new Vec3(entityData.get(ROOT));
	}

	public Direction face() {
		return Direction.from3DDataValue(entityData.get(FACE));
	}

	public int length() {
		return Math.max(0, Math.min(entityData.get(LENGTH), MAX_BLOCKS));
	}

	public BlockState carried(int index) {
		return Block.stateById(entityData.get(STATES.get(Math.max(0, Math.min(index, MAX_BLOCKS - 1)))));
	}

	public boolean isReturning() {
		return entityData.get(RETURNING);
	}

	public boolean isConjured() {
		return entityData.get(CONJURED);
	}

	/** Whether this strike has landed on its enemy; server side. */
	public boolean hasLanded() {
		return landed;
	}

	@Nullable
	public UUID owner() {
		return owner;
	}

	@Nullable
	public UUID record() {
		return record;
	}

	/** Where block {@code index} stands now (0 is the head). */
	public Vec3 blockCentre(int index, Vec3 head) {
		return LivingLandTerrainRules.blockCentre(root(), face(), head, index);
	}

	// ── Save ──

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		owner = tag.hasUUID("owner") ? tag.getUUID("owner") : null;
		record = tag.hasUUID("record") ? tag.getUUID("record") : null;
		target = tag.hasUUID("target") ? tag.getUUID("target") : null;
		damage = tag.getFloat("damage");
		age = tag.getInt("age");
		landed = tag.getBoolean("landed");
		lingered = tag.getInt("lingered");
		carried.clear();
		ListTag states = tag.getList("blocks", Tag.TAG_COMPOUND);
		for (int i = 0; i < Math.min(states.size(), MAX_BLOCKS); i++) {
			carried.add(NbtUtils.readBlockState(level().holderLookup(net.minecraft.core.registries.Registries.BLOCK),
					states.getCompound(i)));
		}
		entityData.set(ROOT, new Vector3f(tag.getFloat("root_x"), tag.getFloat("root_y"), tag.getFloat("root_z")));
		entityData.set(FACE, tag.getByte("face"));
		entityData.set(LENGTH, carried.size());
		entityData.set(RETURNING, tag.getBoolean("returning"));
		entityData.set(CONJURED, record == null);
		for (int i = 0; i < carried.size(); i++) entityData.set(STATES.get(i), Block.getId(carried.get(i)));
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		if (owner != null) tag.putUUID("owner", owner);
		if (record != null) tag.putUUID("record", record);
		if (target != null) tag.putUUID("target", target);
		tag.putFloat("damage", damage);
		tag.putInt("age", age);
		tag.putBoolean("landed", landed);
		tag.putInt("lingered", lingered);
		ListTag states = new ListTag();
		for (BlockState state : carried) states.add(NbtUtils.writeBlockState(state));
		tag.put("blocks", states);
		Vector3f root = entityData.get(ROOT);
		tag.putFloat("root_x", root.x);
		tag.putFloat("root_y", root.y);
		tag.putFloat("root_z", root.z);
		tag.putByte("face", entityData.get(FACE));
		tag.putBoolean("returning", isReturning());
	}

	/** The cells this strike's blocks occupy, for tests and the renderer's light. */
	public BlockPos headCell() {
		return BlockPos.containing(position());
	}

	@Override
	public AABB getBoundingBoxForCulling() {
		return new AABB(root(), position()).inflate(1.5D);
	}
}
