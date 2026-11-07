package com.vincenthuto.hemomancy.common.entity.boss.saint;

import com.vincenthuto.hemomancy.common.entity.boss.BossReplayClock;
import com.vincenthuto.hemomancy.common.entity.boss.FootprintFrame;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * The client's view of a kit Saint's synced move, opening and ending, with no client classes: the replay clocks
 * ({@link BossReplayClock}) for the move, the opening and the ending, the decoded footprint frame, and the inputs a
 * move's timeline and footprint read. Each Saint's {@code AnimState} and the telegraph pass read it.
 */
public final class SaintMoveView {
	private final SaintTrialEntity saint;
	private final BossReplayClock moveClock = new BossReplayClock();
	private final BossReplayClock openingClock = new BossReplayClock();
	private final BossReplayClock endingClock = new BossReplayClock();
	@Nullable
	private CompoundTag decodedTag;
	@Nullable
	private FootprintFrame decoded;

	SaintMoveView(SaintTrialEntity saint) {
		this.saint = saint;
	}

	/** The running move, or null. */
	@Nullable
	public SaintMove move() {
		int id = saint.syncedMoveId();
		return id == 0 ? null : saint.move(id);
	}

	public int moveId() {
		return saint.syncedMoveId();
	}

	/** Ticks since the move started, smooth across frames; -1 with no move. */
	public float moveTime(float partial) {
		if (saint.syncedMoveId() == 0) return -1.0F;
		return moveClock.sample(saint.syncedMoveSerial(), saint.syncedMoveTick(), saint.tickCount, partial, 1);
	}

	/** Ticks since the opening opened; -1 when closed. */
	public float openingTime(float partial) {
		int id = saint.syncedOpening();
		if (id == 0) return -1.0F;
		return openingClock.sample(id, saint.syncedOpeningTick(), saint.tickCount, partial, 1);
	}

	public int openingId() {
		return saint.syncedOpening();
	}

	public int openingLength() {
		return saint.syncedOpeningLength();
	}

	/** Ticks into the ending; -1 while none runs. */
	public float endingTime(float partial) {
		int tick = saint.syncedEndingTick();
		if (tick < 0) return -1.0F;
		return endingClock.sample(1, tick, saint.tickCount, partial, 1);
	}

	public boolean committed() {
		return (saint.syncedMoveFlags() & SaintTrialEntity.MOVE_COMMITTED) != 0;
	}

	public boolean aimLocked() {
		return (saint.syncedMoveFlags() & SaintTrialEntity.MOVE_AIM_LOCKED) != 0;
	}

	public SaintMoveInputs inputs() {
		return new SaintMoveInputs(saint.syncedPhase(), saint.syncedMoveVariant(), saint.syncedDeeds(),
				saint.syncedChallengerCount());
	}

	/** The move's frame as the server tests it: origin and aim live, the target re-read, the rest from the move's tag. */
	@Nullable
	public FootprintFrame frame() {
		if (saint.syncedMoveId() == 0) return null;
		CompoundTag tag = saint.syncedMoveFrame();
		if (tag != decodedTag) {
			decodedTag = tag;
			decoded = tag.contains("origin") ? FootprintFrame.load(tag) : null;
		}
		if (decoded == null) return null;
		Vec3 target = decoded.target();
		int targetId = saint.syncedMoveTarget();
		Entity entity = targetId < 0 ? null : saint.level().getEntity(targetId);
		if (entity != null) target = entity.position();
		return decoded.withOrigin(saint.syncedMoveOrigin()).withAim(saint.syncedMoveAim()).withTarget(target);
	}
}
