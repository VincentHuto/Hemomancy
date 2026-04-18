package com.vincenthuto.hemomancy.common.tile.functional;

import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.tile.IMultiBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Block entity for the Sanguine Monolith. Tracks ambient state and
 * provides a render bounding box that covers the full 1×2×1 structure.
 * Also drives the emergence-from-ground rise animation on first placement.
 */
public class SanguineMonolithBlockEntity extends BlockEntity implements IMultiBlockEntity {

	/** Duration of the placement rise animation in ticks (~1.5 s). */
	public static final int RISE_DURATION_TICKS = 30;

	/** Ticks since the monolith was placed; used for ambient glow animation. */
	private int tickCount = 0;
	/** Number of tier-7+ interactions, used for instability/explosion behavior. */
	private int archonInteractions = 0;

	/**
	 * World game-time when this monolith was first placed, or {@code -1} if
	 * unknown (pre-existing block, loaded from save before this feature existed).
	 */
	private long placedGameTime = -1L;

	public SanguineMonolithBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.sanguine_monolith.get(), pos, state);
	}

	@Override
	public AABB getRenderBoundingBox() {
		return IMultiBlockEntity.computeMultiBlockAABB(this);
	}

	public int getTickCount() {
		return tickCount;
	}

	/** Increments and returns the count of archon-tier interactions. */
	public int incrementArchonInteractions() {
		archonInteractions++;
		setChanged();
		return archonInteractions;
	}

	/** Called server-side immediately after placement to record the rise start time. */
	public void startRise(long gameTime) {
		this.placedGameTime = gameTime;
		setChanged();
	}

	/**
	 * Returns a value in [0, 1] representing how far the rise animation has
	 * progressed. Returns 1.0 if the animation is complete or was never started.
	 */
	public float getRiseProgress(float partialTick) {
		if (level == null || placedGameTime < 0) return 1.0f;
		float ticks = (level.getGameTime() - placedGameTime) + partialTick;
		return Math.min(1.0f, ticks / RISE_DURATION_TICKS);
	}

	/** Server-side tick. */
	public void tick() {
		tickCount++;
		if (tickCount % 100 == 0) {
			setChanged();
		}
	}

	/** Client-side tick: spawns ground-eruption particles during the rise window. */
	public void clientTick() {
		Level lvl = getLevel();
		if (lvl == null || !lvl.isClientSide || placedGameTime < 0) return;

		long ticksAlive = lvl.getGameTime() - placedGameTime;
		if (ticksAlive < 0 || ticksAlive >= RISE_DURATION_TICKS) return;

		float progress = ticksAlive / (float) RISE_DURATION_TICKS;
		// More particles at the start; taper off as the monolith rises
		int inkCount = (int)(3 * (1.0f - progress)) + 1;

		for (int i = 0; i < inkCount; i++) {
			double px = worldPosition.getX() + 0.5 + (lvl.random.nextDouble() - 0.5) * 0.9;
			double py = worldPosition.getY() + lvl.random.nextDouble() * 0.12;
			double pz = worldPosition.getZ() + 0.5 + (lvl.random.nextDouble() - 0.5) * 0.45;
			double vx = (lvl.random.nextDouble() - 0.5) * 0.04;
			double vy = 0.05 + lvl.random.nextDouble() * 0.10;
			double vz = (lvl.random.nextDouble() - 0.5) * 0.04;
			lvl.addParticle(ParticleTypes.SQUID_INK, px, py, pz, vx, vy, vz);
		}
		if (lvl.random.nextFloat() < 0.35f) {
			double px = worldPosition.getX() + 0.5 + (lvl.random.nextDouble() - 0.5) * 0.7;
			double py = worldPosition.getY() + lvl.random.nextDouble() * 0.2;
			double pz = worldPosition.getZ() + 0.5 + (lvl.random.nextDouble() - 0.5) * 0.3;
			lvl.addParticle(ParticleTypes.LARGE_SMOKE, px, py, pz, 0, 0.03, 0);
		}
	}

	@Override
	public CompoundTag getUpdateTag() {
		CompoundTag tag = super.getUpdateTag();
		tag.putInt("TickCount", tickCount);
		tag.putInt("ArchonInteractions", archonInteractions);
		if (placedGameTime >= 0) tag.putLong("PlacedGameTime", placedGameTime);
		return tag;
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		tag.putInt("TickCount", tickCount);
		tag.putInt("ArchonInteractions", archonInteractions);
		if (placedGameTime >= 0) tag.putLong("PlacedGameTime", placedGameTime);
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		tickCount = tag.getInt("TickCount");
		archonInteractions = tag.getInt("ArchonInteractions");
		placedGameTime = tag.contains("PlacedGameTime") ? tag.getLong("PlacedGameTime") : -1L;
	}
}
