package com.vincenthuto.hemomancy.common.entity.boss.saint;

import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint;
import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.IntUnaryOperator;

/**
 * One Saint move. Each Saint has an enum of its moves implementing this. Methods the client calls ({@link #timeline},
 * {@link #footprint}, {@link #telegraph}) read only synced inputs; the rest run on the server.
 */
public interface SaintMove {
	/** MELEE and AREA set the wind-up and reveal floors, 12 and 20 ticks ({@link SaintMoves#kindFor}). */
	enum Kind { MELEE, AREA }

	/** TRACK follows the target until the commit tick; LOCK_AT_START fixes at the start; LEAD aims ahead of it. */
	enum Aim { TRACK, LOCK_AT_START, LEAD }

	/** 1..255, unique within the Saint; 0 means no move. */
	int id();

	/** lower_snake: lang, review stops, tests. */
	String key();

	Kind kind();

	Aim aim();

	default int leadTicks() {
		return 0;
	}

	/** False for moves only the Saint starts (Ignition, the Last Thirst, the Radiance Flood). */
	default boolean picked() {
		return true;
	}

	boolean inPhase(int phase);

	// read by both sides, from synced inputs only

	BossMoveTimeline timeline(SaintMoveInputs in);

	BossFootprint footprint(SaintMoveInputs in);

	default TelegraphSpec telegraph(SaintMoveInputs in) {
		return TelegraphSpec.DEFAULT;
	}

	// server only

	/** 0 = not here. */
	int weight(SaintPickContext ctx);

	default boolean eligible(SaintPickContext ctx) {
		return true;
	}

	int cooldown(SaintPickContext ctx);

	/** The variant the move starts with, synced as {@link SaintMoveInputs#variant}. */
	default int rollVariant(SaintPickContext ctx, IntUnaryOperator roll) {
		return 0;
	}

	default List<SaintFollowUp> followUps(int phase) {
		return List.of();
	}

	/** Opens at the recovery's first tick; the recovery lasts max(recovery, opening ticks). */
	default @Nullable SaintOpening.Spec recoveryOpening(SaintMoveContext ctx) {
		return null;
	}

	/** 0: each part strikes a target once per move; n: at most once per n ticks. */
	default int hitEvery() {
		return 0;
	}

	default float baseDamage() {
		return 0.0F;
	}

	/** Null uses the Saint's damage type (Putriciel's fire moves name the cleansing type). */
	default @Nullable ResourceKey<DamageType> damageType() {
		return null;
	}

	default void onTick(SaintMoveContext ctx) {
	}

	default void onHit(SaintMoveContext ctx, LivingEntity target) {
		SaintMoves.hurt(ctx, target, baseDamage());
	}
}
