package com.vincenthuto.hemomancy.common.tile.harbinger.puzzle;

import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules;
import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.RingDoorBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.RingDoorGeometry;
import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.RingDoorRules;
import com.vincenthuto.hemomancy.common.block.shared.HorizontalFacingRotationHelper;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodAccounting;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.HarbingerPathPermissions;
import com.vincenthuto.hemomancy.common.entity.boss.saint.EnumSaintType;
import com.vincenthuto.hemomancy.common.event.ApotheosBodyEvents;
import com.vincenthuto.hemomancy.common.event.SanguineProjectionTargeting;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.tile.HemoBlockEntity;
import com.vincenthuto.hemomancy.common.tile.ProjectedBloodSink;
import com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.SaintSarcophagusBlockEntity.DoorMode;
import com.vincenthuto.hemomancy.common.util.BlockEntitySync;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The ring door's controller. Each completed Chapel lights a node and its ring turns to the seam. The door opens to
 * held Blood Projection ({@link ProjectedBloodSink}): each fed tick pays {@link RingDoorRules#BLOOD_PER_TICK} and adds a
 * tick of charge, and a door left alone bleeds its charge back off. A Silentium's blood spreads from where they aim and
 * turns to the monolith's black and red; full, the slab sinks into the floor. An Apotheos's hyphae spread and crack it;
 * full, it bursts. Either way the frame stays and the door stays open. The renderer reads {@link #phase()},
 * {@link #charge(float)}, {@link #path()}, {@link #origin()}, {@link #feeding()} and the nodes; the client advances its
 * own copy of the charge and the phase tick between syncs.
 */
public class RingDoorBlockEntity extends HemoBlockEntity implements ProjectedBloodSink {
	public enum Phase { IDLE, OPENING, SINKING, OPEN }

	/** The saved tag's keys; the structure generator writes {@link #TAG_SAINT} and {@link #TAG_FILLERS_PLACED}. */
	public static final String TAG_PHASE = "Phase", TAG_PHASE_TICK = "PhaseTick", TAG_NODES = "Nodes",
			TAG_TURN_START = "TurnStart", TAG_PROJECTOR = "Projector", TAG_SAINT = "Saint", TAG_OPENED_BY = "OpenedBy",
			TAG_SKIPPED = "Skipped", TAG_FILLERS_PLACED = "FillersPlaced", TAG_CHARGE = "Charge", TAG_PATH = "Path",
			TAG_ORIGIN = "Origin", TAG_FEEDING = "Feeding";

	private static final BloodAccounting.Source RING = BloodAccounting.Source.of("ring_door", Category.WORLD, true, false);
	private static final ParticleColor CRIMSON = new ParticleColor(168, 36, 54);
	private static final ParticleColor HYPHAE = new ParticleColor(236, 226, 186);
	private static final ParticleColor HYPHAE_GOLD = new ParticleColor(242, 214, 128);
	private static final ParticleColor BLOOD = new ParticleColor(132, 14, 26);
	/** The server resends a charging door this often, and the client runs its own charge in between. */
	private static final int SYNC_TICKS = 10;

	private Phase phase = Phase.IDLE;
	private int phaseTick;
	private int nodes;
	private final long[] turnStart = new long[RingDoorRules.RINGS];
	/** The ticks of held projection the door holds, 0 to {@link RingDoorRules#OPEN_TICKS}. */
	private int charge;
	/** The way the door is being opened while it charges: PROPER for a Silentium, FORCED for an Apotheos. */
	private DoorMode path = DoorMode.NONE;
	/** The last player to feed the door. */
	@Nullable
	private UUID projector;
	/** Where the projection last struck the door, in the world. */
	@Nullable
	private Vec3 origin;
	private boolean feeding;
	/** The game tick of the last feed. Not saved: a reloaded door counts from its load. */
	private long lastFed = Long.MIN_VALUE / 2;
	/** The client's running charge, set from each sync and advanced each client tick. */
	private float shownCharge;
	@Nullable
	private EnumSaintType saint;
	private DoorMode openedBy = DoorMode.NONE;
	/** The Chapels the burst told the tomb were skipped. */
	private final List<ChapelRules.Kind> skipped = new ArrayList<>();
	/** A structure places the door without a placer, so its fillers grow on its first tick. */
	private boolean fillersPlaced;

	public RingDoorBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.ring_door.get(), pos, state);
	}

	public Phase phase() {
		return phase;
	}

	/** Ticks into SINKING; in {@code OPEN}, ticks since the door opened, counting to the burst's end. */
	public int phaseTick() {
		return phaseTick;
	}

	public int nodes() {
		return nodes;
	}

	/** The game time {@code ring}'s node lit, or 0 for long ago. */
	public long turnStart(int ring) {
		return ring >= 0 && ring < turnStart.length ? turnStart[ring] : 0L;
	}

	/** The server's charge, in ticks of held projection. */
	public int charge() {
		return charge;
	}

	/** The client's charge between syncs, {@code partialTick} into the frame. */
	public float charge(float partialTick) {
		float step = feeding ? 1f : -1f / RingDoorRules.BLEED_TICKS;
		return Math.max(0f, Math.min(RingDoorRules.OPEN_TICKS, shownCharge + step * partialTick));
	}

	public DoorMode path() {
		return path;
	}

	@Nullable
	public Vec3 origin() {
		return origin;
	}

	public boolean feeding() {
		return feeding;
	}

	@Nullable
	public UUID projector() {
		return projector;
	}

	/** The Saint whose chamber the door closes, as its structure named it; null when none did. */
	@Nullable
	public EnumSaintType saint() {
		return saint;
	}

	public DoorMode openedBy() {
		return openedBy;
	}

	/** Where {@code ring} rests while its node is dark, in degrees from the seam. */
	public float restAngle(int ring) {
		return RingDoorRules.restAngle(ring, worldPosition.asLong());
	}

	public boolean fillersPlaced() {
		return fillersPlaced;
	}

	public void markFillersPlaced() {
		fillersPlaced = true;
		setChanged();
	}

	// ── Chapels ─────────────────────────────────────────────────────────────

	/** Every Chapel focus close enough to belong to this door's mausoleum. */
	public static List<ChapelFocusBlockEntity> chapelsNear(ServerLevel level, BlockPos pos) {
		List<ChapelFocusBlockEntity> found = new ArrayList<>();
		for (BlockEntity entity : blockEntitiesNear(level, pos)) {
			if (entity instanceof ChapelFocusBlockEntity focus) found.add(focus);
		}
		return found;
	}

	/** Called when a Chapel completes: lights the nodes of the ring door its wing belongs to. */
	public static void chapelCompleted(ServerLevel level, BlockPos chapel) {
		BlockPos door = doorNear(level, chapel);
		if (door != null && level.getBlockEntity(door) instanceof RingDoorBlockEntity controller) {
			controller.refresh(chapelsNear(level, door));
		}
	}

	/**
	 * Lights a node for each completed Chapel, outer ring first; returns how many are lit. A newly lit ring starts
	 * its turn to the seam now, grinding.
	 */
	public int refresh(List<ChapelFocusBlockEntity> chapels) {
		int lit = (int) Math.min(RingDoorRules.RINGS, chapels.stream().filter(ChapelFocusBlockEntity::complete).count());
		if (lit == nodes || level == null) return lit;
		long now = level.getGameTime();
		for (int ring = 0; ring < RingDoorRules.RINGS; ring++) {
			if (ring >= lit) turnStart[ring] = 0L;
			else if (ring >= nodes) turnStart[ring] = now;
		}
		if (lit > nodes) sound(SoundEvents.GRINDSTONE_USE, 1.0f, 0.5f);
		nodes = lit;
		BlockEntitySync.markAndSync(this);
		return lit;
	}

	// ── Opening ─────────────────────────────────────────────────────────────

	/** A hand on the door: it answers blood, not hands. */
	public void touch(ServerPlayer player) {
		if (phase != Phase.OPEN) tell(player, Component.translatable("message.hemomancy.ring_door.touch"));
	}

	/**
	 * Blood projected at the door ({@code hitPos} is the part aimed at). An Apotheos may open it at any time; a
	 * Silentium once every node is lit; anyone else is told why not. A fed tick pays its blood, adds a tick of charge
	 * and moves the spread's origin to where the projection struck. The door takes one feed a tick, whoever gives it.
	 * Returns the blood taken.
	 */
	@Override
	public double takeProjectedBlood(Player player, BlockPos hitPos, double rate, boolean livingStaff) {
		if (!(level instanceof ServerLevel world) || !(player instanceof ServerPlayer server)) return 0.0D;
		if (phase != Phase.IDLE && phase != Phase.OPENING) return 0.0D;
		DoorMode way = wayFor(server);
		if (way == DoorMode.NONE) {
			tell(server, Component.translatable("message.hemomancy.ring_door.refused"));
			return 0.0D;
		}
		if (phase == Phase.OPENING && way != path) {
			tell(server, Component.translatable("message.hemomancy.ring_door.contested"));
			return 0.0D;
		}
		if (way == DoorMode.PROPER) {
			int lit = phase == Phase.IDLE ? refresh(chapelsNear(world, worldPosition)) : nodes;
			if (!RingDoorRules.mayProject(true, lit)) {
				tell(server, Component.translatable("message.hemomancy.ring_door.nodes", lit, RingDoorRules.RINGS));
				return 0.0D;
			}
		}
		long now = world.getGameTime();
		if (lastFed == now) return 0.0D;
		var volume = HemoCapabilityAccess.getBloodVolume(server);
		if (!volume.isActive() || volume.getBloodVolume() < RingDoorRules.BLOOD_PER_TICK) {
			tell(server, Component.translatable("message.hemomancy.ring_door.blood", RingDoorRules.BLOOD_COST));
			return 0.0D;
		}
		BloodAccounting.pay(server, RingDoorRules.BLOOD_PER_TICK, RING);
		boolean sync = phase == Phase.IDLE || !feeding || !server.getUUID().equals(projector);
		if (phase == Phase.IDLE) {
			phase = Phase.OPENING;
			path = way;
			charge = 0;
		}
		lastFed = now;
		feeding = true;
		projector = server.getUUID();
		Vec3 struck = struckPoint(world, server, hitPos);
		if (origin == null || origin.distanceToSqr(struck) > 0.0625D) sync = true;
		origin = struck;
		charge = RingDoorRules.nextCharge(charge, true, 0);
		if (charge % 4 == 0) fedEffects(world, way);
		if (charge >= RingDoorRules.OPEN_TICKS) {
			complete(world);
		} else if (sync || charge % SYNC_TICKS == 0) {
			BlockEntitySync.markAndSync(this);
		} else {
			setChanged();
		}
		return RingDoorRules.BLOOD_PER_TICK;
	}

	private static DoorMode wayFor(ServerPlayer player) {
		if (RingDoorRules.mayShatter(ApotheosBodyEvents.isApotheos(player))) return DoorMode.FORCED;
		var degree = HemoCapabilityAccess.getInitiatoryDegree(player);
		return HarbingerPathPermissions.isSilentium(degree.getDegreeNumber(), degree.getArchonPath())
				? DoorMode.PROPER : DoorMode.NONE;
	}

	/** Where the projection meets the door: its trace's point when that lands on the part, else the part's centre. */
	private static Vec3 struckPoint(ServerLevel level, ServerPlayer player, BlockPos hitPos) {
		HitResult trace = SanguineProjectionTargeting.pick(level, player, SanguineProjectionTargeting.PROJECTION_REACH,
				true);
		if (trace instanceof BlockHitResult block && block.getBlockPos().equals(hitPos)) return block.getLocation();
		return Vec3.atCenterOf(hitPos);
	}

	/** The door full: a Silentium's slab starts to sink and the tomb learns it was opened properly; an Apotheos's bursts. */
	private void complete(ServerLevel level) {
		charge = RingDoorRules.OPEN_TICKS;
		feeding = false;
		if (path == DoorMode.PROPER) {
			phase = Phase.SINKING;
			phaseTick = 0;
			recordDoor(level, DoorMode.PROPER, List.of());
			for (float x = -22; x <= 38; x += 10) {
				particles(level, HemoParticleData.glow(CRIMSON), at(x, 2, 8), 3, 0.15, 0.02);
			}
			sound(SoundEvents.BEACON_POWER_SELECT, 1.2f, 0.5f);
			sound(SoundEvents.GRINDSTONE_USE, 1.0f, 0.4f);
			BlockEntitySync.markAndSync(this);
		} else {
			burst(level);
		}
	}

	/** Every few fed ticks: the blood or the hyphae at the strike, and the door answering. */
	private void fedEffects(ServerLevel level, DoorMode way) {
		if (origin == null) return;
		float progress = RingDoorRules.progress(charge);
		if (way == DoorMode.PROPER) {
			particles(level, HemoParticleData.bloodCell(BLOOD), origin, 2, 0.08, 0.02);
			particles(level, HemoParticleData.glow(CRIMSON), origin, 1, 0.1, 0.01);
			if (charge % 20 == 0) sound(SoundEvents.BUCKET_EMPTY_LAVA, 0.5f, 0.45f + 0.3f * progress);
		} else {
			particles(level, HemoParticleData.sporiticSpore(HYPHAE), origin, 3, 0.15, 0.015);
			particles(level, HemoParticleData.glow(HYPHAE_GOLD), origin, 1, 0.1, 0.01);
			if (charge % 12 == 0) sound(SoundEvents.SCULK_VEIN_BREAK, 0.6f + 0.4f * progress, 0.5f + 0.4f * progress);
			if (charge % 40 == 0) sound(SoundEvents.GRINDSTONE_USE, 0.5f + 0.5f * progress, 0.4f);
		}
	}

	// ── Ticking ─────────────────────────────────────────────────────────────

	public static void serverTick(Level level, BlockPos pos, BlockState state, RingDoorBlockEntity door) {
		if (!door.fillersPlaced && state.getBlock() instanceof RingDoorBlock block) {
			block.placeFillers(level, pos, state);
			door.markFillersPlaced();
		}
		if (!(level instanceof ServerLevel server)) return;
		switch (door.phase) {
			case OPENING -> door.tickOpening(server);
			case SINKING -> door.tickSinking(server);
			case OPEN -> {
				if (door.phaseTick < RingDoorRules.BURST_TICKS) door.phaseTick++;
			}
			default -> {
			}
		}
	}

	/** The client's own clocks, so the spread and the slab move smoothly between the server's syncs. */
	public static void clientTick(Level level, BlockPos pos, BlockState state, RingDoorBlockEntity door) {
		switch (door.phase) {
			case OPENING -> door.shownCharge = door.charge(1f);
			case SINKING -> door.phaseTick++;
			case OPEN -> {
				if (door.phaseTick < RingDoorRules.BURST_TICKS) door.phaseTick++;
			}
			default -> {
			}
		}
	}

	/** Unfed past the grace, the charge bleeds off; drained, the door is idle again. */
	private void tickOpening(ServerLevel level) {
		long since = level.getGameTime() - lastFed;
		if (RingDoorRules.fed(since)) return;
		boolean stopped = feeding;
		feeding = false;
		int before = charge;
		charge = RingDoorRules.nextCharge(charge, false, since);
		if (charge <= 0) {
			phase = Phase.IDLE;
			path = DoorMode.NONE;
			projector = null;
			origin = null;
			sound(SoundEvents.BEACON_DEACTIVATE, 0.8f, 0.5f);
			BlockEntitySync.markAndSync(this);
			return;
		}
		if (stopped || (charge != before && charge % SYNC_TICKS == 0)) BlockEntitySync.markAndSync(this);
		else if (charge != before) setChanged();
	}

	private void tickSinking(ServerLevel level) {
		phaseTick++;
		if (phaseTick >= RingDoorRules.SINK_TICKS) {
			for (BlockPos cell : interiorCells(true)) {
				level.sendParticles(HemoParticleData.glow(CRIMSON), cell.getX() + 0.5, cell.getY() + 0.3,
						cell.getZ() + 0.5, 4, 0.4, 0.2, 0.4, 0.02);
			}
			sound(SoundEvents.DEEPSLATE_BRICKS_PLACE, 1.2f, 0.5f);
			openDoor(level, DoorMode.PROPER);
		} else {
			setChanged();
		}
	}

	/** The cracked slab bursts: fragments and hyphae across the doorway, and the tomb learns what was skipped. */
	private void burst(ServerLevel level) {
		for (BlockPos cell : interiorCells(true)) {
			double x = cell.getX() + 0.5, y = cell.getY() + 0.5, z = cell.getZ() + 0.5;
			level.sendParticles(HemoParticleData.glow(HYPHAE_GOLD), x, y, z, 6, 0.4, 0.4, 0.4, 0.08);
			level.sendParticles(HemoParticleData.sporiticSpore(HYPHAE), x, y, z, 8, 0.4, 0.4, 0.4, 0.12);
			level.sendParticles(HemoParticleData.bloodCell(BLOOD), x, y, z, 2, 0.4, 0.4, 0.4, 0.06);
		}
		level.playSound(null, worldPosition, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 1.0f, 0.6f);
		skipped.clear();
		skipped.addAll(skippedKinds(chapelsNear(level, worldPosition)));
		openDoor(level, DoorMode.FORCED);
		recordDoor(level, DoorMode.FORCED, List.copyOf(skipped));
	}

	/** The interior falls away; the frame stands. */
	private void openDoor(ServerLevel level, DoorMode mode) {
		for (BlockPos cell : interiorCells(false)) RingDoorBlock.releaseFiller(level, cell, worldPosition);
		BlockState state = level.getBlockState(worldPosition);
		if (state.getBlock() instanceof RingDoorBlock && !state.getValue(RingDoorBlock.OPEN)) {
			level.setBlock(worldPosition, state.setValue(RingDoorBlock.OPEN, true), Block.UPDATE_ALL);
		}
		phase = Phase.OPEN;
		phaseTick = 0;
		openedBy = mode;
		path = DoorMode.NONE;
		projector = null;
		feeding = false;
		BlockEntitySync.markAndSync(this);
	}

	// ── Geometry ────────────────────────────────────────────────────────────

	private Direction facing() {
		BlockState state = getBlockState();
		return state.hasProperty(RingDoorBlock.FACING) ? state.getValue(RingDoorBlock.FACING) : Direction.NORTH;
	}

	/** A north-facing door pixel, turned to this door, in the world. */
	private Vec3 at(double x, double y, double z) {
		double[] point = RingDoorGeometry.point(facing(), x, y, z);
		return new Vec3(worldPosition.getX() + point[0], worldPosition.getY() + point[1], worldPosition.getZ() + point[2]);
	}

	/** The cells the slab fills (not the frame's), in the world; with or without the controller's own cell. */
	private List<BlockPos> interiorCells(boolean withController) {
		List<BlockPos> cells = new ArrayList<>();
		if (withController) cells.add(worldPosition);
		Direction facing = facing();
		for (BlockPos offset : RingDoorGeometry.FILLERS) {
			if (RingDoorGeometry.isFrame(offset)) continue;
			cells.add(worldPosition.offset(HorizontalFacingRotationHelper.rotateNorthOffset(offset, facing)));
		}
		return cells;
	}

	private static void particles(ServerLevel level, ParticleOptions particle, Vec3 at, int count, double spread,
			double speed) {
		level.sendParticles(particle, at.x, at.y, at.z, count, spread, spread, spread, speed);
	}

	private void sound(SoundEvent sound, float volume, float pitch) {
		if (level != null) level.playSound(null, worldPosition, sound, SoundSource.BLOCKS, volume, pitch);
	}

	private static void tell(Player player, Component message) {
		player.displayClientMessage(message.copy().withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), true);
	}

	public AABB getRenderBoundingBox() {
		AABB box = new AABB(worldPosition);
		for (BlockPos offset : HorizontalFacingRotationHelper.rotateNorthOffsets(RingDoorGeometry.FILLERS, facing())) {
			box = box.minmax(new AABB(worldPosition.offset(offset)));
		}
		return box.inflate(1.0D);
	}

	// ── The mausoleum around the door ───────────────────────────────────────

	private static List<ChapelRules.Kind> skippedKinds(List<ChapelFocusBlockEntity> chapels) {
		List<ChapelRules.Kind> kinds = new ArrayList<>();
		boolean[] completed = new boolean[chapels.size()];
		chapels.sort((a, b) -> Integer.compare(a.slot(), b.slot()));
		for (int i = 0; i < chapels.size(); i++) {
			kinds.add(chapels.get(i).kind());
			completed[i] = chapels.get(i).complete();
		}
		return RingDoorRules.skipped(kinds, completed);
	}

	private void recordDoor(ServerLevel level, DoorMode mode, List<ChapelRules.Kind> skippedChapels) {
		for (BlockEntity entity : blockEntitiesNear(level, worldPosition)) {
			if (entity instanceof SaintSarcophagusBlockEntity tomb) tomb.recordDoor(mode, skippedChapels);
		}
	}

	private static List<BlockEntity> blockEntitiesNear(ServerLevel level, BlockPos pos) {
		List<BlockEntity> found = new ArrayList<>();
		ChunkPos centre = new ChunkPos(pos);
		int reach = RingDoorRules.CHAPEL_SEARCH;
		int chunks = SectionPos.blockToSectionCoord(reach) + 1;
		for (int cx = centre.x - chunks; cx <= centre.x + chunks; cx++) {
			for (int cz = centre.z - chunks; cz <= centre.z + chunks; cz++) {
				if (!level.hasChunk(cx, cz)) continue;
				for (BlockEntity entity : level.getChunk(cx, cz).getBlockEntities().values()) {
					if (entity.getBlockPos().distSqr(pos) <= (double) reach * reach) found.add(entity);
				}
			}
		}
		return found;
	}

	/** The nearest ring door within a Chapel's search, looking only in the chunk sections that can hold one. */
	@Nullable
	private static BlockPos doorNear(ServerLevel level, BlockPos chapel) {
		int reach = RingDoorRules.CHAPEL_SEARCH;
		int height = RingDoorRules.CHAPEL_SEARCH_HEIGHT;
		BlockPos best = null;
		for (int cx = SectionPos.blockToSectionCoord(chapel.getX() - reach);
				cx <= SectionPos.blockToSectionCoord(chapel.getX() + reach); cx++) {
			for (int cz = SectionPos.blockToSectionCoord(chapel.getZ() - reach);
					cz <= SectionPos.blockToSectionCoord(chapel.getZ() + reach); cz++) {
				if (!level.hasChunk(cx, cz)) continue;
				LevelChunk chunk = level.getChunk(cx, cz);
				for (int sy = SectionPos.blockToSectionCoord(chapel.getY() - height);
						sy <= SectionPos.blockToSectionCoord(chapel.getY() + height); sy++) {
					int index = chunk.getSectionIndexFromSectionY(sy);
					if (index < 0 || index >= chunk.getSectionsCount()) continue;
					LevelChunkSection section = chunk.getSection(index);
					if (section.hasOnlyAir() || !section.maybeHas(state -> state.getBlock() instanceof RingDoorBlock)) {
						continue;
					}
					for (BlockPos pos : BlockPos.betweenClosed(cx << 4, sy << 4, cz << 4, (cx << 4) + 15, (sy << 4) + 15,
							(cz << 4) + 15)) {
						if (!(section.getBlockState(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15).getBlock()
								instanceof RingDoorBlock)) continue;
						if (best == null || pos.distSqr(chapel) < best.distSqr(chapel)) best = pos.immutable();
					}
				}
			}
		}
		return best != null && best.distSqr(chapel) <= (double) reach * reach ? best : null;
	}

	// ── Saving and syncing ──────────────────────────────────────────────────

	private void writeState(CompoundTag tag) {
		tag.putString(TAG_PHASE, phase.name());
		tag.putInt(TAG_PHASE_TICK, phaseTick);
		tag.putInt(TAG_NODES, nodes);
		tag.putLongArray(TAG_TURN_START, turnStart);
		if (projector != null) tag.putUUID(TAG_PROJECTOR, projector);
		if (saint != null) tag.putString(TAG_SAINT, saint.name());
		tag.putString(TAG_OPENED_BY, openedBy.name());
		tag.putInt(TAG_CHARGE, charge);
		tag.putString(TAG_PATH, path.name());
		tag.putBoolean(TAG_FEEDING, feeding);
		if (origin != null) tag.put(TAG_ORIGIN, newDoubleList(origin.x, origin.y, origin.z));
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
		super.saveAdditional(tag, provider);
		writeState(tag);
		if (!skipped.isEmpty()) {
			ListTag kinds = new ListTag();
			skipped.forEach(kind -> kinds.add(StringTag.valueOf(kind.name())));
			tag.put(TAG_SKIPPED, kinds);
		}
		tag.putBoolean(TAG_FILLERS_PLACED, fillersPlaced);
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
		super.loadAdditional(tag, provider);
		phase = parse(Phase.class, tag.getString(TAG_PHASE), Phase.IDLE);
		phaseTick = tag.getInt(TAG_PHASE_TICK);
		nodes = Math.max(0, Math.min(RingDoorRules.RINGS, tag.getInt(TAG_NODES)));
		long[] starts = tag.getLongArray(TAG_TURN_START);
		for (int ring = 0; ring < turnStart.length; ring++) turnStart[ring] = ring < starts.length ? starts[ring] : 0L;
		projector = tag.hasUUID(TAG_PROJECTOR) ? tag.getUUID(TAG_PROJECTOR) : null;
		saint = tag.contains(TAG_SAINT) ? parse(EnumSaintType.class, tag.getString(TAG_SAINT), null) : null;
		openedBy = parse(DoorMode.class, tag.getString(TAG_OPENED_BY), DoorMode.NONE);
		charge = Math.max(0, Math.min(RingDoorRules.OPEN_TICKS, tag.getInt(TAG_CHARGE)));
		shownCharge = charge;
		path = parse(DoorMode.class, tag.getString(TAG_PATH), DoorMode.NONE);
		feeding = tag.getBoolean(TAG_FEEDING);
		ListTag at = tag.getList(TAG_ORIGIN, Tag.TAG_DOUBLE);
		origin = at.size() == 3 ? new Vec3(at.getDouble(0), at.getDouble(1), at.getDouble(2)) : null;
		if (tag.contains(TAG_SKIPPED)) {
			skipped.clear();
			for (Tag kind : tag.getList(TAG_SKIPPED, Tag.TAG_STRING)) {
				ChapelRules.Kind parsed = parse(ChapelRules.Kind.class, kind.getAsString(), null);
				if (parsed != null) skipped.add(parsed);
			}
		}
		// a structure writes FillersPlaced false; a door saved before the key existed already stands on its fillers
		fillersPlaced = !tag.contains(TAG_FILLERS_PLACED) || tag.getBoolean(TAG_FILLERS_PLACED);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
		CompoundTag tag = super.getUpdateTag(provider);
		writeState(tag);
		return tag;
	}

	@Nullable
	private static <E extends Enum<E>> E parse(Class<E> type, String name, @Nullable E fallback) {
		if (name == null || name.isEmpty()) return fallback;
		try {
			return Enum.valueOf(type, name);
		} catch (IllegalArgumentException unknown) {
			return fallback;
		}
	}

	private static ListTag newDoubleList(double... values) {
		ListTag list = new ListTag();
		for (double value : values) list.add(DoubleTag.valueOf(value));
		return list;
	}
}
