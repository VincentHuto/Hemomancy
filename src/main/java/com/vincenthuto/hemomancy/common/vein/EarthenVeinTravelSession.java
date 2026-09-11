package com.vincenthuto.hemomancy.common.vein;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.UUID;

public final class EarthenVeinTravelSession {
	private final UUID nonce = UUID.randomUUID();
	private final UUID owner;
	private final ResourceLocation sourceDimension;
	private final BlockPos sourcePos;
	private final boolean temporarySource;
	@Nullable private final UUID sourceVeinId;
	private final long createdAt;
	private EarthenVeinTravelPhase phase = EarthenVeinTravelPhase.SELECTING;
	@Nullable private EarthenVeinDestination destination;
	private double fedBlood;
	private long phaseStartedAt;
	private long lastFedAt;

	private EarthenVeinTravelSession(UUID owner, ResourceLocation sourceDimension, BlockPos sourcePos,
			boolean temporarySource, @Nullable UUID sourceVeinId, long createdAt) {
		this.owner = owner;
		this.sourceDimension = sourceDimension;
		this.sourcePos = sourcePos.immutable();
		this.temporarySource = temporarySource;
		this.sourceVeinId = sourceVeinId;
		this.createdAt = createdAt;
		this.phaseStartedAt = createdAt;
		this.lastFedAt = createdAt;
	}

	public static EarthenVeinTravelSession selecting(UUID owner, ResourceLocation sourceDimension,
			BlockPos sourcePos, boolean temporarySource, long gameTime) {
		return selecting(owner, sourceDimension, sourcePos, temporarySource, null, gameTime);
	}

	public static EarthenVeinTravelSession selecting(UUID owner, ResourceLocation sourceDimension,
			BlockPos sourcePos, boolean temporarySource, @Nullable UUID sourceVeinId, long gameTime) {
		return new EarthenVeinTravelSession(owner, sourceDimension, sourcePos, temporarySource, sourceVeinId, gameTime);
	}

	public boolean select(UUID player, EarthenVeinDestination selected, long gameTime) {
		if (phase != EarthenVeinTravelPhase.SELECTING || !owner.equals(player)) return false;
		destination = selected;
		phase = EarthenVeinTravelPhase.FEEDING;
		phaseStartedAt = gameTime;
		lastFedAt = gameTime;
		return true;
	}

	public double feed(UUID player, double offered, double available, long gameTime) {
		if (phase != EarthenVeinTravelPhase.FEEDING || !owner.equals(player) || destination == null) return 0.0D;
		double accepted = EarthenVeinTravelRules.acceptedBlood(fedBlood, destination.cost(), offered, available);
		if (accepted <= 0.0D) return 0.0D;
		fedBlood += accepted;
		lastFedAt = gameTime;
		if (fedBlood >= destination.cost()) {
			phase = EarthenVeinTravelPhase.READY;
			phaseStartedAt = gameTime;
		}
		return accepted;
	}

	public boolean beginSwallow(UUID player, long gameTime) {
		if (phase != EarthenVeinTravelPhase.READY || !owner.equals(player)) return false;
		phase = EarthenVeinTravelPhase.SWALLOWING;
		phaseStartedAt = gameTime;
		return true;
	}

	public void beginEjection(long gameTime) {
		phase = EarthenVeinTravelPhase.EJECTING;
		phaseStartedAt = gameTime;
	}

	public boolean swallowComplete(long gameTime) {
		return phase == EarthenVeinTravelPhase.SWALLOWING
				&& gameTime - phaseStartedAt >= EarthenVeinTravelRules.SWALLOW_TICKS;
	}

	public boolean ejectionComplete(long gameTime) {
		return phase == EarthenVeinTravelPhase.EJECTING
				&& gameTime - phaseStartedAt >= EarthenVeinTravelRules.EJECTION_TICKS;
	}

	public UUID nonce() { return nonce; }
	public UUID owner() { return owner; }
	public ResourceLocation sourceDimension() { return sourceDimension; }
	public BlockPos sourcePos() { return sourcePos; }
	public boolean temporarySource() { return temporarySource; }
	@Nullable public UUID sourceVeinId() { return sourceVeinId; }
	public long createdAt() { return createdAt; }
	public EarthenVeinTravelPhase phase() { return phase; }
	@Nullable public EarthenVeinDestination destination() { return destination; }
	public double fedBlood() { return fedBlood; }
	public long phaseStartedAt() { return phaseStartedAt; }
	public long lastFedAt() { return lastFedAt; }
}
