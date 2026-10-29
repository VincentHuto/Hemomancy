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
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.tile.HemoBlockEntity;
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
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The ring door's controller. Each completed Chapel lights a node and its ring turns to the seam. A Silentium
 * projects into the lit door and it fills ring by ring, a ring of their blood at the start of each, pulses, and sinks
 * into the floor; an Apotheos cracks it with hyphae and it bursts. Either way the frame stays and the door stays open.
 * The renderer reads {@link #phase()}, {@link #phaseTick()}, {@link #nodes()} and {@link #turnStart(int)}; the
 * client advances its own copy of the phase tick between syncs.
 */
public class RingDoorBlockEntity extends HemoBlockEntity {
	public enum Phase { IDLE, PROJECTING, SHATTERING, OPEN }

	/** The saved tag's keys; the structure generator writes {@link #TAG_SAINT} and {@link #TAG_FILLERS_PLACED}. */
	public static final String TAG_PHASE = "Phase", TAG_PHASE_TICK = "PhaseTick", TAG_NODES = "Nodes",
			TAG_TURN_START = "TurnStart", TAG_PROJECTOR = "Projector", TAG_SAINT = "Saint", TAG_OPENED_BY = "OpenedBy",
			TAG_SKIPPED = "Skipped", TAG_FILLERS_PLACED = "FillersPlaced";

	private static final BloodAccounting.Source RING = BloodAccounting.Source.of("ring_door", Category.WORLD, true, false);
	private static final ParticleColor CRIMSON = new ParticleColor(168, 36, 54);
	private static final ParticleColor HYPHAE = new ParticleColor(214, 196, 150);
	private static final ParticleColor BLOOD = new ParticleColor(132, 14, 26);

	private Phase phase = Phase.IDLE;
	private int phaseTick;
	private int nodes;
	private final long[] turnStart = new long[RingDoorRules.RINGS];
	@Nullable
	private UUID projector;
	/** The projecting player as clicked, for a projector the server's player list does not hold. Not saved. */
	private WeakReference<ServerPlayer> projectorHeld = new WeakReference<>(null);
	@Nullable
	private EnumSaintType saint;
	private DoorMode openedBy = DoorMode.NONE;
	/** The Chapels a shattering will tell the tomb were skipped, held until the burst. */
	private final List<ChapelRules.Kind> skipped = new ArrayList<>();
	/** A structure places the door without a placer, so its fillers grow on its first tick. */
	private boolean fillersPlaced;

	public RingDoorBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.ring_door.get(), pos, state);
	}

	public Phase phase() {
		return phase;
	}

	/** Ticks into the current phase; in {@code OPEN}, ticks since the door opened, counting to the burst's end. */
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

	// ── The click ───────────────────────────────────────────────────────────

	/**
	 * A player touches the door: an Apotheos shatters it; a Silentium with every node lit and three rings of blood
	 * starts the projection; anyone else is told why not. A door already moving or open ignores the touch.
	 */
	public void use(ServerPlayer player) {
		if (!(level instanceof ServerLevel world) || phase != Phase.IDLE) return;
		List<ChapelFocusBlockEntity> chapels = chapelsNear(world, worldPosition);
		int lit = refresh(chapels);
		if (RingDoorRules.mayShatter(ApotheosBodyEvents.isApotheos(player))) {
			shatter(skippedKinds(chapels));
			return;
		}
		var degree = HemoCapabilityAccess.getInitiatoryDegree(player);
		if (!HarbingerPathPermissions.isSilentium(degree.getDegreeNumber(), degree.getArchonPath())) {
			tell(player, Component.translatable("message.hemomancy.ring_door.refused"));
			return;
		}
		if (!RingDoorRules.mayProject(true, lit)) {
			tell(player, Component.translatable("message.hemomancy.ring_door.nodes", lit, RingDoorRules.RINGS));
			return;
		}
		var volume = HemoCapabilityAccess.getBloodVolume(player);
		if (!volume.isActive() || volume.getBloodVolume() < RingDoorRules.BLOOD_PER_RING * RingDoorRules.RINGS) {
			tell(player, Component.translatable("message.hemomancy.ring_door.blood",
					RingDoorRules.BLOOD_PER_RING * RingDoorRules.RINGS));
			return;
		}
		startProjection(player);
	}

	/**
	 * The Silentium's way. The door takes {@code player}'s blood a ring at a time, from the next tick; it does not
	 * ask again whether they may. False when the door is already moving or open.
	 */
	public boolean startProjection(ServerPlayer player) {
		if (phase != Phase.IDLE) return false;
		phase = Phase.PROJECTING;
		phaseTick = 0;
		projector = player.getUUID();
		projectorHeld = new WeakReference<>(player);
		BlockEntitySync.markAndSync(this);
		return true;
	}

	/** The Apotheos's way: hyphae crack the slab, and after {@link RingDoorRules#SHATTER_TICKS} it bursts. */
	public boolean shatter(List<ChapelRules.Kind> skippedChapels) {
		if (phase == Phase.SHATTERING || phase == Phase.OPEN) return false;
		phase = Phase.SHATTERING;
		phaseTick = 0;
		clearProjector();
		skipped.clear();
		skipped.addAll(skippedChapels);
		sound(SoundEvents.SCULK_BLOCK_SPREAD, 1.4f, 0.5f);
		sound(SoundEvents.GRINDSTONE_USE, 0.8f, 0.4f);
		BlockEntitySync.markAndSync(this);
		return true;
	}

	// ── Ticking ─────────────────────────────────────────────────────────────

	public static void serverTick(Level level, BlockPos pos, BlockState state, RingDoorBlockEntity door) {
		if (!door.fillersPlaced && state.getBlock() instanceof RingDoorBlock block) {
			block.placeFillers(level, pos, state);
			door.markFillersPlaced();
		}
		if (!(level instanceof ServerLevel server)) return;
		switch (door.phase) {
			case PROJECTING -> door.tickProjection(server);
			case SHATTERING -> door.tickShatter(server);
			case OPEN -> {
				if (door.phaseTick < RingDoorRules.BURST_TICKS) door.phaseTick++;
			}
			default -> {
			}
		}
	}

	/** The client's own phase clock, so the rings move smoothly between the server's syncs. */
	public static void clientTick(Level level, BlockPos pos, BlockState state, RingDoorBlockEntity door) {
		switch (door.phase) {
			case PROJECTING, SHATTERING -> door.phaseTick++;
			case OPEN -> {
				if (door.phaseTick < RingDoorRules.BURST_TICKS) door.phaseTick++;
			}
			default -> {
			}
		}
	}

	private void tickProjection(ServerLevel level) {
		int ring = RingDoorRules.paysAt(phaseTick);
		if (ring >= 0) {
			ServerPlayer player = projectorPlayer(level);
			if (!mayPay(level, player)) {
				breakProjection(player);
				return;
			}
			BloodAccounting.pay(player, RingDoorRules.stageCost(), RING);
			Vec3 node = at(RingDoorGeometry.RING_X, RingDoorGeometry.RING_Y + RingDoorGeometry.CHANNEL_RADII[ring],
					RingDoorGeometry.RING_FRONT_Z);
			particles(level, HemoParticleData.glow(CRIMSON), node, 10, 0.12, 0.02);
			particles(level, HemoParticleData.bloodCell(BLOOD), node, 6, 0.1, 0.03);
			sound(SoundEvents.BUCKET_EMPTY_LAVA, 0.8f, 0.5f + 0.15f * ring);
			BlockEntitySync.markAndSync(this);
		}
		if (phaseTick == RingDoorRules.pulseStart()) {
			for (float y = 4; y <= 72; y += 6) {
				particles(level, HemoParticleData.glow(CRIMSON), at(RingDoorGeometry.RING_X, y, RingDoorGeometry.RING_FRONT_Z),
						2, 0.04, 0.01);
			}
			sound(SoundEvents.BEACON_ACTIVATE, 1.0f, 0.6f);
			BlockEntitySync.markAndSync(this);
		}
		if (phaseTick == RingDoorRules.sinkStart()) {
			recordDoor(level, DoorMode.PROPER, List.of());
			for (float x = -22; x <= 38; x += 10) {
				particles(level, HemoParticleData.glow(CRIMSON), at(x, 2, 8), 3, 0.15, 0.02);
			}
			sound(SoundEvents.BEACON_POWER_SELECT, 1.2f, 0.5f);
			sound(SoundEvents.GRINDSTONE_USE, 1.0f, 0.4f);
			BlockEntitySync.markAndSync(this);
		}
		phaseTick++;
		if (phaseTick >= RingDoorRules.projectionLength()) {
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

	private void tickShatter(ServerLevel level) {
		if (phaseTick % 6 == 0) {
			float[] slab = RingDoorGeometry.SLAB;
			Vec3 centre = at((slab[0] + slab[3]) / 2, (slab[1] + slab[4]) / 2, slab[2]);
			float spread = 0.3f + 0.9f * RingDoorRules.crack(phaseTick);
			sendAcross(level, HemoParticleData.sporiticSpore(HYPHAE), centre, 6, spread * 1.6, spread * 1.6, 0.05, 0.02);
			sound(SoundEvents.SCULK_VEIN_BREAK, 0.8f, 0.6f);
		}
		phaseTick++;
		if (phaseTick >= RingDoorRules.SHATTER_TICKS) burst(level);
		else setChanged();
	}

	/** The cracked slab bursts: fragments and hyphae across the doorway, and the tomb learns what was skipped. */
	private void burst(ServerLevel level) {
		for (BlockPos cell : interiorCells(true)) {
			double x = cell.getX() + 0.5, y = cell.getY() + 0.5, z = cell.getZ() + 0.5;
			level.sendParticles(HemoParticleData.glow(CRIMSON), x, y, z, 6, 0.4, 0.4, 0.4, 0.08);
			level.sendParticles(HemoParticleData.sporiticSpore(HYPHAE), x, y, z, 6, 0.4, 0.4, 0.4, 0.1);
			level.sendParticles(HemoParticleData.bloodCell(BLOOD), x, y, z, 4, 0.4, 0.4, 0.4, 0.06);
		}
		level.playSound(null, worldPosition, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 1.0f, 0.6f);
		List<ChapelRules.Kind> told = List.copyOf(skipped);
		skipped.clear();
		openDoor(level, DoorMode.FORCED);
		recordDoor(level, DoorMode.FORCED, told);
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
		clearProjector();
		BlockEntitySync.markAndSync(this);
	}

	/** The projection fails: the rings drain back, and nothing is recorded. The blood already given is spent. */
	private void breakProjection(@Nullable ServerPlayer player) {
		phase = Phase.IDLE;
		phaseTick = 0;
		clearProjector();
		sound(SoundEvents.BEACON_DEACTIVATE, 1.0f, 0.5f);
		if (player != null) tell(player, Component.translatable("message.hemomancy.ring_door.broken"));
		BlockEntitySync.markAndSync(this);
	}

	private void clearProjector() {
		projector = null;
		projectorHeld = new WeakReference<>(null);
	}

	@Nullable
	private ServerPlayer projectorPlayer(ServerLevel level) {
		if (projector == null) return null;
		ServerPlayer online = level.getServer().getPlayerList().getPlayer(projector);
		if (online != null) return online;
		ServerPlayer held = projectorHeld.get();
		return held != null && projector.equals(held.getUUID()) && !held.hasDisconnected() ? held : null;
	}

	/** Whether the projector is still here, alive, near enough and holding a ring of blood. */
	private boolean mayPay(ServerLevel level, @Nullable ServerPlayer player) {
		if (player == null || player.isRemoved() || !player.isAlive() || player.level() != level) return false;
		Vec3 centre = at(RingDoorGeometry.RING_X, RingDoorGeometry.RING_Y, 8);
		if (player.position().distanceToSqr(centre) > RingDoorRules.PROJECTOR_REACH * RingDoorRules.PROJECTOR_REACH) {
			return false;
		}
		var volume = HemoCapabilityAccess.getBloodVolume(player);
		return volume.isActive() && volume.getBloodVolume() >= RingDoorRules.stageCost();
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

	/** Particles spread across the door's face ({@code across} along it, {@code depth} through it). */
	private void sendAcross(ServerLevel level, ParticleOptions particle, Vec3 at, int count, double across, double up,
			double depth, double speed) {
		boolean alongX = facing().getAxis() == Direction.Axis.Z;
		level.sendParticles(particle, at.x, at.y, at.z, count, alongX ? across : depth, up, alongX ? depth : across,
				speed);
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
}
