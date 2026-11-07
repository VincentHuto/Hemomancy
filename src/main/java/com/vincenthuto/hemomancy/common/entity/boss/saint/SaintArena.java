package com.vincenthuto.hemomancy.common.entity.boss.saint;

import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.SaintSarcophagusBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * A Saint's chamber, resolved from the tomb's position and facing at the rise: the chamber and boss-floor boxes, the
 * local frame of {@link SaintChamberLayout}, the set pieces found by one scan per key, the challengers inside, and the
 * leash. Every block the fight changes goes through here: the original state (and block entity) is recorded the first
 * time a position changes and restored by the contract in the Kit API (ending, removal, leash reset, load, server
 * stop). Entities the fight spawns are owned here and go with the trial.
 */
public final class SaintArena {
	public enum RestoreOrder { IMMEDIATE, OUTERMOST_FIRST, INNERMOST_FIRST }

	/** One change per cell per 10 ticks. */
	public static final int MIN_CELL_INTERVAL = 10;
	public static final int EMPTY_RESET_TICKS = 600;
	/** Restores write originals back without drops and without neighbour updates that would break a two-tall piece. */
	public static final int RESTORE_FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_SUPPRESS_DROPS;

	/** A position's state and block entity as they stood before the fight changed it. */
	public record Original(BlockState state, @Nullable CompoundTag blockEntity) {
	}

	/** An edit budget handle: see {@link #budget}. */
	public static final class EditBudget {
		private final SaintArenaRecord.Budget budget;

		private EditBudget(SaintArenaRecord.Budget budget) {
			this.budget = budget;
		}

		public String key() {
			return budget.key();
		}

		public int standing() {
			return budget.standing();
		}
	}

	private final SaintTrialEntity saint;
	private final ServerLevel level;
	private final boolean chamberFound;
	private final BlockPos tomb;
	private final Direction facing;
	private final BlockPos origin;
	private final Direction xAxis;
	private final Direction zAxis;
	private final AABB chamber;
	private final AABB bossFloor;
	private final SaintArenaRecord<Original> record = new SaintArenaRecord<>(MIN_CELL_INTERVAL);
	private final Map<String, List<BlockPos>> found = new HashMap<>();
	private final Map<String, EditBudget> budgets = new HashMap<>();
	private final Map<UUID, BlockPos> owned = new LinkedHashMap<>();
	private final World world = new World();
	private List<Player> challengers = List.of();
	private long challengersAt = Long.MIN_VALUE;
	private int challengerCount;
	private int emptyTicks;

	private SaintArena(SaintTrialEntity saint, ServerLevel level, boolean chamberFound, BlockPos tomb, Direction facing) {
		this.saint = saint;
		this.level = level;
		this.chamberFound = chamberFound;
		this.tomb = tomb;
		this.facing = facing;
		this.xAxis = facing.getOpposite();
		this.zAxis = xAxis.getClockWise();
		if (chamberFound) {
			this.origin = tomb.relative(xAxis, -SaintChamberLayout.TOMB_X).below(SaintChamberLayout.TOMB_Y)
					.relative(zAxis, -SaintChamberLayout.TOMB_Z);
			this.chamber = box(0, 0, 0, SaintChamberLayout.LENGTH - 1, SaintChamberLayout.HEIGHT - 1,
					SaintChamberLayout.WIDTH - 1);
			this.bossFloor = box(SaintChamberLayout.CLEAR_MIN_X, SaintChamberLayout.FLOOR_Y,
					SaintChamberLayout.BOSS_FLOOR_MIN_Z, SaintChamberLayout.CLEAR_MAX_X, SaintChamberLayout.HEIGHT - 1,
					SaintChamberLayout.BOSS_FLOOR_MAX_Z);
		} else {
			// no tomb: a square round the Saint, with the boss floor's centre where it stands
			this.origin = tomb.offset(-16, -SaintChamberLayout.FLOOR_Y, -SaintChamberLayout.ZC);
			this.chamber = box(0, 0, 0, SaintChamberLayout.LENGTH - 1, SaintChamberLayout.HEIGHT - 1,
					SaintChamberLayout.WIDTH - 1);
			this.bossFloor = box(16 - SaintChamberLayout.CLEAR_HALF, SaintChamberLayout.FLOOR_Y,
					SaintChamberLayout.BOSS_FLOOR_MIN_Z, 16 + SaintChamberLayout.CLEAR_HALF, SaintChamberLayout.HEIGHT - 1,
					SaintChamberLayout.BOSS_FLOOR_MAX_Z);
		}
	}

	/** The tomb position and facing, else a fallback square round the Saint. */
	public static SaintArena resolve(SaintTrialEntity saint) {
		ServerLevel level = (ServerLevel) saint.level();
		BlockPos tomb = saint.getTombPos();
		BlockState state = saint.getTombState();
		if (tomb != null && state != null && state.hasProperty(SaintSarcophagusBlock.FACING)) {
			return new SaintArena(saint, level, true, tomb, state.getValue(SaintSarcophagusBlock.FACING));
		}
		return new SaintArena(saint, level, false, saint.blockPosition(), Direction.WEST);
	}

	public boolean chamberFound() {
		return chamberFound;
	}

	public AABB chamber() {
		return chamber;
	}

	public AABB bossFloor() {
		return bossFloor;
	}

	public BlockPos tomb() {
		return tomb;
	}

	public Direction facing() {
		return facing;
	}

	/** The world y of the course a player stands on over the boss floor. */
	public double floorY() {
		return bossFloor.minY;
	}

	public BlockPos toWorld(int x, int y, int z) {
		return origin.relative(xAxis, x).above(y).relative(zAxis, z);
	}

	/** A local point to the world: local (x + 0.5, y, z + 0.5) is the bottom centre of local block (x, y, z). */
	public Vec3 toWorld(double x, double y, double z) {
		Vec3 centre = Vec3.atBottomCenterOf(origin);
		double dx = x - 0.5D, dz = z - 0.5D;
		return new Vec3(centre.x + xAxis.getStepX() * dx + zAxis.getStepX() * dz, centre.y + y,
				centre.z + xAxis.getStepZ() * dx + zAxis.getStepZ() * dz);
	}

	public Vec3 toLocal(Vec3 world) {
		Vec3 centre = Vec3.atBottomCenterOf(origin);
		double wx = world.x - centre.x, wz = world.z - centre.z;
		double x = wx * xAxis.getStepX() + wz * xAxis.getStepZ();
		double z = wx * zAxis.getStepX() + wz * zAxis.getStepZ();
		return new Vec3(x + 0.5D, world.y - centre.y, z + 0.5D);
	}

	/** One scan of the chamber box per key, cached: basins, gutters, anchors, pyres, figures, lanterns. */
	public List<BlockPos> find(String key, Predicate<BlockState> test) {
		return found.computeIfAbsent(key, ignored -> {
			List<BlockPos> hits = new ArrayList<>();
			for (BlockPos pos : BlockPos.betweenClosed(Mth.floor(chamber.minX), Mth.floor(chamber.minY),
					Mth.floor(chamber.minZ), Mth.floor(chamber.maxX) - 1, Mth.floor(chamber.maxY) - 1,
					Mth.floor(chamber.maxZ) - 1)) {
				if (level.isLoaded(pos) && test.test(level.getBlockState(pos))) hits.add(pos.immutable());
			}
			return Collections.unmodifiableList(hits);
		});
	}

	public boolean contains(Vec3 point) {
		return chamber.contains(point);
	}

	/** A point pulled inside the chamber box, half a block in from its walls: the leash. */
	public Vec3 clamp(Vec3 point) {
		return new Vec3(Mth.clamp(point.x, chamber.minX + 0.5D, chamber.maxX - 0.5D),
				Mth.clamp(point.y, chamber.minY, chamber.maxY - 1.0D),
				Mth.clamp(point.z, chamber.minZ + 0.5D, chamber.maxZ - 0.5D));
	}

	/** Alive, not spectating, inside the chamber box; read once per tick. */
	public List<Player> challengers() {
		long now = level.getGameTime();
		if (challengersAt != now) {
			challengersAt = now;
			challengers = List.copyOf(level.getEntitiesOfClass(Player.class, chamber,
					player -> player.isAlive() && !player.isSpectator()));
		}
		return challengers;
	}

	/** Counted from the rise's end; rises, never falls; at least 1. */
	public int challengerCount() {
		return Math.max(1, challengerCount);
	}

	/** The kit counts the roster once the rise has ended. */
	void countChallengers() {
		challengerCount = Math.max(challengerCount, challengers().size());
	}

	// ── block edits ──────────────────────────────────────────────────

	public boolean set(BlockPos pos, BlockState state) {
		return set(pos, state, Block.UPDATE_ALL);
	}

	public boolean set(BlockPos pos, BlockState state, int flags) {
		world.flags = flags;
		return record.set(world, pos.asLong(), new Original(state, null), now());
	}

	/** A travelling front: the edit lands after {@code delayTicks}. */
	public void schedule(BlockPos pos, BlockState state, int delayTicks) {
		record.schedule(pos.asLong(), new Original(state, null), delayTicks, now());
	}

	/** Timed: the state stands {@code ticks}, then the original returns. */
	public void swapFor(BlockPos pos, BlockState state, int ticks) {
		world.flags = Block.UPDATE_ALL;
		record.swapFor(world, pos.asLong(), new Original(state, null), ticks, now());
	}

	public boolean revert(BlockPos pos) {
		return record.revert(world, pos.asLong());
	}

	/** Clears a two-tall (or taller) set piece, upper storey first, without drops or neighbour updates. */
	public boolean clearColumn(BlockPos base, int storeys) {
		boolean any = false;
		for (int y = storeys - 1; y >= 0; y--) {
			any |= set(base.above(y), Blocks.AIR.defaultBlockState(), RESTORE_FLAGS);
		}
		return any;
	}

	public EditBudget budget(String key, int perWindow, int windowTicks, int maxRecorded) {
		return budgets.computeIfAbsent(key,
				ignored -> new EditBudget(record.budget(key, perWindow, windowTicks, maxRecorded)));
	}

	/** Past maxRecorded the oldest edit of that budget reverts first. */
	public boolean edit(EditBudget budget, BlockPos pos, BlockState state) {
		world.flags = Block.UPDATE_ALL;
		return record.edit(world, budget.budget, pos.asLong(), new Original(state, null), now());
	}

	public boolean changed(BlockPos pos) {
		return record.changed(pos.asLong());
	}

	// ── owned entities ───────────────────────────────────────────────

	/** The entity goes with the trial; {@code home} is where it belongs (a devotee's place). */
	public <E extends Entity> E own(E entity, @Nullable BlockPos home) {
		owned.put(entity.getUUID(), home);
		return entity;
	}

	public void disown(Entity entity) {
		owned.remove(entity.getUUID());
	}

	@Nullable
	public BlockPos home(Entity entity) {
		return owned.get(entity.getUUID());
	}

	/** The owned entities loaded now. */
	public List<Entity> owned() {
		List<Entity> loaded = new ArrayList<>();
		for (UUID id : owned.keySet()) {
			Entity entity = level.getEntity(id);
			if (entity != null && !entity.isRemoved()) loaded.add(entity);
		}
		return loaded;
	}

	/** Discards every loaded owned entity; one not loaded discards itself when it loads with no live Saint. */
	public void discardOwned() {
		for (Entity entity : owned()) entity.discard();
		owned.clear();
	}

	// ── restore ──────────────────────────────────────────────────────

	/** Restores every record in {@code order} over {@code overTicks} (IMMEDIATE or 0: now). Owned entities stay. */
	public void restore(RestoreOrder order, int overTicks) {
		Vec3 centre = chamber.getCenter();
		record.restore(world, order, overTicks, pos -> {
			BlockPos block = BlockPos.of(pos);
			double dx = block.getX() + 0.5D - centre.x, dz = block.getZ() + 0.5D - centre.z;
			return Math.sqrt(dx * dx + dz * dz);
		});
	}

	/** Everything back now and every owned entity discarded: removal, the leash, a load, the server stopping. */
	public void restoreNow() {
		record.restoreNow(world);
		discardOwned();
	}

	public boolean restoring() {
		return record.restoring();
	}

	/** No records and no owned entities. */
	public boolean clean() {
		return record.clean() && owned.isEmpty();
	}

	/** Lands schedules, ends swaps, runs a spread restore and counts the leash; true when the empty chamber resets. */
	public boolean tick(boolean fighting) {
		record.tick(world, now());
		owned.keySet().removeIf(id -> {
			Entity entity = level.getEntity(id);
			return entity != null && entity.isRemoved() && entity.getRemovalReason() != null
					&& entity.getRemovalReason().shouldDestroy();
		});
		if (!fighting || !chamberFound) {
			emptyTicks = 0;
			return false;
		}
		emptyTicks = challengers().isEmpty() ? emptyTicks + 1 : 0;
		return emptyTicks >= EMPTY_RESET_TICKS;
	}

	public int emptyTicks() {
		return emptyTicks;
	}

	public void save(CompoundTag tag) {
		CompoundTag arena = new CompoundTag();
		record.save(arena, original -> {
			CompoundTag entry = new CompoundTag();
			entry.put("block", NbtUtils.writeBlockState(original.state()));
			if (original.blockEntity() != null) entry.put("entity", original.blockEntity());
			return entry;
		});
		ListTag ownedList = new ListTag();
		owned.forEach((id, home) -> {
			CompoundTag entry = new CompoundTag();
			entry.putUUID("id", id);
			if (home != null) entry.putLong("home", home.asLong());
			ownedList.add(entry);
		});
		arena.put("owned", ownedList);
		arena.putInt("challengers", challengerCount);
		arena.putInt("empty", emptyTicks);
		tag.put("SaintArena", arena);
	}

	public void load(CompoundTag tag) {
		CompoundTag arena = tag.getCompound("SaintArena");
		var blocks = level.holderLookup(Registries.BLOCK);
		record.load(arena, entry -> new Original(NbtUtils.readBlockState(blocks, entry.getCompound("block")),
				entry.contains("entity") ? entry.getCompound("entity") : null));
		owned.clear();
		ListTag ownedList = arena.getList("owned", Tag.TAG_COMPOUND);
		for (int i = 0; i < ownedList.size(); i++) {
			CompoundTag entry = ownedList.getCompound(i);
			if (entry.hasUUID("id")) owned.put(entry.getUUID("id"), entry.contains("home") ? BlockPos.of(entry.getLong("home")) : null);
		}
		challengerCount = arena.getInt("challengers");
		emptyTicks = arena.getInt("empty");
	}

	/** Whether a saved arena compound holds records or owned entities to restore. */
	public static boolean hasRecords(CompoundTag tag) {
		CompoundTag arena = tag.getCompound("SaintArena");
		return !arena.getList("records", Tag.TAG_COMPOUND).isEmpty() || !arena.getList("owned", Tag.TAG_COMPOUND).isEmpty()
				|| !arena.getList("scheduled", Tag.TAG_COMPOUND).isEmpty();
	}

	public SaintTrialEntity saint() {
		return saint;
	}

	private long now() {
		return level.getGameTime();
	}

	private AABB box(int x0, int y0, int z0, int x1, int y1, int z1) {
		BlockPos a = toWorld(x0, y0, z0), b = toWorld(x1, y1, z1);
		return new AABB(Math.min(a.getX(), b.getX()), Math.min(a.getY(), b.getY()), Math.min(a.getZ(), b.getZ()),
				Math.max(a.getX(), b.getX()) + 1, Math.max(a.getY(), b.getY()) + 1, Math.max(a.getZ(), b.getZ()) + 1);
	}

	/** Reads and writes the level for the record; a first change keeps the block entity's data with its state. */
	private final class World implements SaintArenaRecord.World<Original> {
		private int flags = Block.UPDATE_ALL;

		@Override
		public Original read(long pos) {
			BlockPos block = BlockPos.of(pos);
			BlockEntity entity = level.getBlockEntity(block);
			return new Original(level.getBlockState(block),
					entity == null ? null : entity.saveWithFullMetadata(level.registryAccess()));
		}

		@Override
		public void write(long pos, Original state, boolean restoring) {
			BlockPos block = BlockPos.of(pos);
			level.setBlock(block, state.state(), restoring ? RESTORE_FLAGS : flags);
			if (restoring && state.blockEntity() != null) {
				BlockEntity entity = level.getBlockEntity(block);
				if (entity != null) {
					entity.loadWithComponents(state.blockEntity(), level.registryAccess());
					entity.setChanged();
				}
			}
		}
	}
}
