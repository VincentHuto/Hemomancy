package com.vincenthuto.hemomancy.common.entity.boss.saint.velorum;

import com.vincenthuto.hemomancy.common.entity.boss.BossBands;
import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint;
import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint.Height;
import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.entity.boss.FootprintFrame;
import com.vincenthuto.hemomancy.common.entity.boss.saint.FollowCondition;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintFollowUp;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMove;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoveContext;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoveInputs;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoves;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintOpening;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintPickContext;
import com.vincenthuto.hemomancy.common.entity.boss.saint.TelegraphSpec;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Velorum's twenty-one moves (design section 7 as resolved on 2026-11-05, and the revision of 2026-11-06: Frozen Cruor
 * and liquid dark). Each declares its timeline, footprint and telegraph from synced inputs only (the client draws what
 * the server tests), and its bands, pressures, cooldown, strings, opening and effects for the server. Phase 3 cuts
 * every wind-up by 15% and the Repose deed every recovery by 30%, both through the kit's floors. Strings: A (Toll,
 * Sweep, the Release), B (Bid into the Release), C (the Still Floor into the Splinter Crown), D (Toll into the Hollow
 * Toll, Hollow into a second Hollow), E (the Shadow Step into the Release), F (Gloam Cuts into Cruor Javelins) and G
 * (Arrested Pulse into Gloam Cuts).
 * <p>
 * The moves whose fronts fly (the javelins, the cuts) keep the footprint as the drawn and tested line and strike only
 * at the flying head: {@link #head} is that window, so a body the shaft or cut has already passed is never struck.
 */
public enum VelorumMove implements SaintMove {
	STAFF_TOLL(1, Kind.MELEE, 0b111, 16, 3, 18, 0, 9.0F,
			VelorumSelectionRules.Cells.of(30, 24, 0, 0, 1.0F, 0.5F, 0.0F)) {
		private static final BossFootprint CONE = new BossFootprint.Cone(0.0D, 3.5D, 70.0D, 0.0D, Height.STANDING);

		@Override
		public BossFootprint footprint(SaintMoveInputs in) {
			return CONE;
		}

		@Override
		public List<SaintFollowUp> followUps(int phase) {
			if (phase >= 3) return List.of(new SaintFollowUp(HOLLOW_TOLL, 0.55F, 6, FollowCondition.ALWAYS));
			return List.of(new SaintFollowUp(LANTERN_SWEEP, 0.6F, 4,
					FollowCondition.bands(BossBands.Band.HUG, BossBands.Band.NEAR)));
		}

		@Override
		public void onTick(SaintMoveContext ctx) {
			if (ctx.tick() == 0) VelorumMoves.welling(ctx);
			if (ctx.committedNow()) VelorumEffects.toll(ctx);
		}

		@Override
		public void onHit(SaintMoveContext ctx, LivingEntity target) {
			if (VelorumMoves.hurt(ctx, target, baseDamage())) VelorumMoves.stillness(ctx, target, 12.0F);
		}
	},
	LANTERN_SWEEP(2, Kind.MELEE, 0b111, 14, 6, 16, 40, 8.0F,
			VelorumSelectionRules.Cells.of(10, 26, 8, 0, 1.0F, 1.0F, 0.0F)) {
		private static final BossFootprint SWEEP = new BossFootprint.Sweep(1.0D, 5.0D, 160.0D, 40.0D, true, 6,
				new Height(0.0D, 2.4D, false));

		@Override
		public BossFootprint footprint(SaintMoveInputs in) {
			return SWEEP;
		}

		@Override
		public List<SaintFollowUp> followUps(int phase) {
			if (phase < 2) return List.of();
			return List.of(new SaintFollowUp(CRUOR_RELEASE, 0.5F, 6, FollowCondition.bands(BossBands.Band.MID)));
		}

		@Override
		public void onTick(SaintMoveContext ctx) {
			if (ctx.tick() == 0) VelorumMoves.sound(ctx, SoundEvents.CHAIN_STEP, 1.0F, 0.6F);
			if (ctx.committedNow()) VelorumMoves.sound(ctx, SoundEvents.LANTERN_HIT, 1.0F, 0.7F);
		}

		@Override
		public void onHit(SaintMoveContext ctx, LivingEntity target) {
			if (!VelorumMoves.hurt(ctx, target, baseDamage())) return;
			VelorumMoves.slow(target, 40, VelorumMoves.SLOWNESS_I);
			VelorumEffects.frostSlash(ctx, target);
		}
	},
	HANGING_LANTERN(3, Kind.MELEE, 0b111, 14, 4, 16, 60, 6.0F,
			VelorumSelectionRules.Cells.of(30, 18, 0, 0, 0.0F, 0.0F, 1.0F)) {
		private static final BossFootprint REAR = new BossFootprint.Cone(0.0D, 3.5D, 120.0D, 180.0D,
				new Height(0.0D, 2.0D, false));
		private static final int FLANK_WEIGHT = 8;

		@Override
		public BossFootprint footprint(SaintMoveInputs in) {
			return REAR;
		}

		@Override
		public Aim aim() {
			return Aim.LOCK_AT_START;
		}

		@Override
		public int weight(SaintPickContext ctx) {
			if (ctx.sector() == BossBands.Sector.FLANK && cells.band(ctx.band()) > 0) return FLANK_WEIGHT;
			return super.weight(ctx);
		}

		@Override
		public void onTick(SaintMoveContext ctx) {
			if (ctx.tick() == 0) VelorumMoves.welling(ctx);
			if (ctx.committedNow()) VelorumMoves.sound(ctx, SoundEvents.CHAIN_PLACE, 1.0F, 0.7F);
		}

		@Override
		public void onHit(SaintMoveContext ctx, LivingEntity target) {
			if (!VelorumMoves.hurt(ctx, target, baseDamage())) return;
			VelorumMoves.stillness(ctx, target, 15.0F);
			VelorumMoves.slow(target, 30, VelorumMoves.SLOWNESS_I);
		}
	},
	/** The Release of Cruor (was Black-Ice Splinters): blades of frozen blood break out along five seam trenches. */
	CRUOR_RELEASE(4, Kind.AREA, 0b111, 24, 12, 22, 80, 10.0F,
			VelorumSelectionRules.Cells.of(0, 10, 26, 22, 1.0F, 0.0F, 0.0F)) {
		/** Resolution A-1: five narrow lanes fanned 15 degrees apart, the front at a block a tick. */
		private static final BossFootprint FAN = fan(12.0D, 1.0D);
		private static final BossFootprint FAN_LONG = fan(14.0D, 1.0D);
		private static final int FLANK_WEIGHT = 8;
		private static final float TOSS = 0.4F;

		private static BossFootprint fan(double length, double speed) {
			return new BossFootprint.Lane(length, 0.45D, 2, 0.0D, 15.0D, speed, 0, Height.STANDING);
		}

		@Override
		public BossFootprint footprint(SaintMoveInputs in) {
			return in.phase() >= 3 ? FAN_LONG : FAN;
		}

		@Override
		public int weight(SaintPickContext ctx) {
			if (ctx.sector() == BossBands.Sector.FLANK && cells.band(ctx.band()) > 0) return FLANK_WEIGHT;
			return super.weight(ctx);
		}

		@Override
		public void onTick(SaintMoveContext ctx) {
			if (ctx.tick() == 0) {
				VelorumMoves.sound(ctx, SoundEvents.AMETHYST_BLOCK_RESONATE, 1.0F, 0.5F);
				VelorumMoves.welling(ctx);
			}
			int active = VelorumMoves.activeTick(ctx);
			if (active >= 0 && active % 4 == 0) VelorumMoves.sound(ctx, SoundEvents.GLASS_BREAK, 0.8F, 0.5F);
			if (VelorumMoves.lastActive(ctx) && ctx.saint() instanceof VelorumEntity velorum) {
				// the filled trenches stand as the cruor field: the floor narrows for 60 ticks
				BossFootprint.Lane lane = (BossFootprint.Lane) footprint(ctx.inputs());
				velorum.standCruorField(new BossFootprint.Lane(lane.length(), lane.halfWidth(), lane.sideLanes(),
						lane.spacing(), lane.fanDegrees(), 0.0D, 0, lane.height()), ctx.frame());
			}
		}

		@Override
		public void onHit(SaintMoveContext ctx, LivingEntity target) {
			if (!VelorumMoves.hurt(ctx, target, baseDamage())) return;
			VelorumMoves.stillness(ctx, target, 20.0F);
			Vec3 away = target.position().subtract(ctx.frame().origin());
			target.push(0.0D, TOSS, 0.0D);
			if (away.horizontalDistanceSqr() > 1.0E-4D) target.push(away.x * 0.02D, 0.0D, away.z * 0.02D);
			target.hurtMarked = true;
		}
	},
	STILL_FLOOR(5, Kind.AREA, 0b111, 30, 3, 30, 140, 6.0F,
			VelorumSelectionRules.Cells.of(12, 18, 16, 4, 1.0F, 1.0F, 1.0F)) {
		private static final BossFootprint RING = new BossFootprint.Annulus(2.5D, 10.0D, 0, 0.0D, 0.0D, 0,
				new Height(0.0D, 2.5D, true));
		private static final int ROOT_TICKS = 20;

		@Override
		public BossFootprint footprint(SaintMoveInputs in) {
			return RING;
		}

		@Override
		public int weight(SaintPickContext ctx) {
			return VelorumSelectionRules.withPressure(super.weight(ctx), VelorumSelectionRules.stillness(ctx.targetMeter()));
		}

		@Override
		public List<SaintFollowUp> followUps(int phase) {
			if (phase < 2) return List.of();
			return List.of(new SaintFollowUp(SPLINTER_CROWN, 0.5F, 6, FollowCondition.onHit()));
		}

		@Override
		public void onTick(SaintMoveContext ctx) {
			if (ctx.tick() == 0) VelorumMoves.welling(ctx);
			if (ctx.tick() == 10) VelorumMoves.sound(ctx, SoundEvents.POWDER_SNOW_BREAK, 1.0F, 0.5F);
			if (ctx.committedNow()) {
				VelorumMoves.sound(ctx, SoundInit.RITE_STAFF_IMPACT.get(), 1.0F, 0.7F);
				VelorumEffects.stillFloor(ctx);
			}
		}

		/** Resolution A-3: the ring roots only the grounded and unmoving; a moving body passes through it. */
		@Override
		public void onHit(SaintMoveContext ctx, LivingEntity target) {
			if (VelorumMoves.moving(target)) return;
			if (!VelorumMoves.hurt(ctx, target, baseDamage())) return;
			VelorumMoves.stillness(ctx, target, 25.0F);
			if (target instanceof Player player) {
				com.vincenthuto.hemomancy.common.entity.boss.saint.SaintRoot.root(player, ROOT_TICKS, VelorumEntity.STILL_FLOOR_ROOT, true);
				VelorumMoves.sound(ctx.saint(), player.position(), SoundEvents.PLAYER_HURT_FREEZE, 1.0F, 0.6F);
			}
		}
	},
	BID_TO_KNEEL(6, Kind.AREA, 0b111, 28, 30, 16, 160, 0.0F,
			VelorumSelectionRules.Cells.of(0, 0, 14, 26, 1.0F, 1.0F, 0.0F)) {
		/** Resolution A-4: the raised lantern's cold cone, in sight; no thread is drawn. */
		private static final BossFootprint GAZE = new BossFootprint.Occluded(
				new BossFootprint.Cone(0.0D, 20.0D, 50.0D, 0.0D, Height.STANDING));
		private static final int KNEEL_TICKS = 30;
		private static final int GAZE_SLOW = 10;

		@Override
		public BossFootprint footprint(SaintMoveInputs in) {
			return GAZE;
		}

		@Override
		public int hitEvery() {
			return 1;
		}

		@Override
		public int weight(SaintPickContext ctx) {
			return VelorumSelectionRules.withPressure(super.weight(ctx), VelorumSelectionRules.kite(ctx.kiteTicks()));
		}

		@Override
		public List<SaintFollowUp> followUps(int phase) {
			return List.of(new SaintFollowUp(CRUOR_RELEASE, 0.8F, 4, FollowCondition.onHit()));
		}

		@Override
		public void onTick(SaintMoveContext ctx) {
			if (!(ctx.saint() instanceof VelorumEntity velorum)) return;
			if (ctx.tick() == 0) {
				VelorumMoves.sound(ctx, SoundInit.RITE_OFFERING_DRAWN.get(), 1.0F, 0.6F);
				// the kiting that earned the gaze no longer counts
				ctx.saint().controller().pressure().resetKite();
			}
			if (VelorumMoves.lastActive(ctx) && !velorum.overheldThisMove()) {
				VelorumMoves.sound(ctx, SoundEvents.AMETHYST_BLOCK_CHIME, 1.0F, 0.5F);
				for (Player player : velorum.gazed()) {
					if (velorum.stillness().value(velorum, player) >= VelorumStillnessRules.GAZE_KNEEL) {
						velorum.stillness().kneel(player, KNEEL_TICKS);
						ctx.saint().controller().markHit();
					}
				}
			}
		}

		@Override
		public void onHit(SaintMoveContext ctx, LivingEntity target) {
			if (!(ctx.saint() instanceof VelorumEntity velorum) || !(target instanceof Player player)) return;
			velorum.stillness().add(velorum, player, VelorumStillnessRules.GAZE_FILL);
			VelorumMoves.slow(player, GAZE_SLOW, VelorumMoves.SLOWNESS_I);
			velorum.gaze(player);
			if (VelorumDeedRules.litany(ctx.inputs().deedMask())) velorum.drawSilence(ctx, player);
		}
	},
	/** The Long Night: folds of liquid dark rise round the lantern and run out as four pools (revision R.3, R.4). */
	// one recovery tick: the kit opens a recovery opening on the recovery's first tick, and with none the Gutter never
	// opened, so the Long Night repeated every 42 ticks under the opening pressure (revision polish, 2026-11-06)
	LONG_NIGHT(7, Kind.AREA, 0b111, 32, 10, 1, VelorumDeedRules.LONG_NIGHT_SPACING, 2.0F,
			VelorumSelectionRules.Cells.of(1, 1, 1, 1, 1.0F, 1.0F, 1.0F)) {
		public static final int DARK_TICKS = 180;

		@Override
		public BossFootprint footprint(SaintMoveInputs in) {
			return BossFootprint.None.INSTANCE;
		}

		@Override
		public int weight(SaintPickContext ctx) {
			return VelorumDeedRules.longNightWeight(ctx.ticksSinceOpening(), VelorumDeedRules.litany(ctx.deedMask()));
		}

		@Override
		public int cooldown(SaintPickContext ctx) {
			return VelorumDeedRules.longNightSpacing(VelorumDeedRules.litany(ctx.deedMask()));
		}

		@Override
		public void onTick(SaintMoveContext ctx) {
			if (!(ctx.saint() instanceof VelorumEntity velorum)) return;
			if (ctx.tick() == 0) {
				VelorumMoves.sound(ctx, SoundInit.RITE_AMBIENT_DRONE.get(), 1.0F, 0.5F);
				// the folds rise through the wind-up and sink into the floor after the commit
				VelorumEffects.longNight(ctx);
			}
			if (!ctx.committedNow()) return;
			VelorumMoves.sound(ctx, SoundEvents.BEACON_DEACTIVATE, 1.0F, 0.5F);
			velorum.congregation().darkenPillars(velorum, DARK_TICKS);
			Vec3 lantern = velorum.lanternPoint();
			for (Player player : ctx.arena().challengers()) {
				if (!velorum.sees(lantern, player)) continue;
				SaintMoves.hurt(ctx, player, baseDamage());
				velorum.stillness().gloam(player, VelorumDarkRules.GLOAM_TICKS);
			}
			velorum.seedNightPools(ctx);
		}

		@Override
		public @Nullable SaintOpening.Spec recoveryOpening(SaintMoveContext ctx) {
			return VelorumOpeningRules.gutter(VelorumOpeningRules.gutterTicks(ctx.inputs().phase(), false));
		}
	},
	SET_IN_PLACE(8, Kind.AREA, 0b111, 20, 2, 24, 60, 10.0F,
			VelorumSelectionRules.Cells.of(0, 0, 0, 0, 1.0F, 1.0F, 1.0F)) {
		private static final BossFootprint DISC = new BossFootprint.Disc(BossFootprint.Anchor.TARGET, 1.2D, Height.STANDING);

		@Override
		public BossFootprint footprint(SaintMoveInputs in) {
			return DISC;
		}

		@Override
		public int weight(SaintPickContext ctx) {
			if (!(ctx.saint() instanceof VelorumEntity velorum) || !(ctx.target() instanceof Player player)) return 0;
			boolean held = velorum.stillness().preserved(player) || velorum.stillness().kneeling(player);
			return held && ctx.distance() <= VelorumSelectionRules.SET_IN_PLACE_REACH ? VelorumSelectionRules.SET_IN_PLACE_WEIGHT : 0;
		}

		@Override
		public void onHit(SaintMoveContext ctx, LivingEntity target) {
			if (!VelorumMoves.hurt(ctx, target, baseDamage())) return;
			if (ctx.saint() instanceof VelorumEntity velorum && target instanceof Player player) {
				velorum.stillness().release(velorum, player);
			}
			VelorumEffects.shatter(ctx, target);
		}
	},
	RECALL(9, Kind.AREA, 0b110, 20, 40, 18, 200, 0.0F,
			VelorumSelectionRules.Cells.of(1, 1, 1, 1, 1.0F, 1.0F, 1.0F)) {
		private static final int WEIGHT = 30;

		@Override
		public BossFootprint footprint(SaintMoveInputs in) {
			return BossFootprint.None.INSTANCE;
		}

		@Override
		public boolean eligible(SaintPickContext ctx) {
			return ctx.saint() instanceof VelorumEntity velorum && !velorum.forced()
					&& velorum.congregation().recallable(velorum) != null;
		}

		@Override
		public int weight(SaintPickContext ctx) {
			return WEIGHT;
		}

		@Override
		public void onTick(SaintMoveContext ctx) {
			if (!(ctx.saint() instanceof VelorumEntity velorum)) return;
			if (ctx.tick() == 0) {
				velorum.beginRecall();
				VelorumMoves.sound(ctx, SoundInit.RITE_OFFERING_DRAWN.get(), 1.0F, 0.6F);
			}
			if (VelorumMoves.activeTick(ctx) >= 0 && !velorum.holdRecall()) {
				ctx.saint().controller().skipToRecovery();
				return;
			}
			if (VelorumMoves.lastActive(ctx) && !velorum.overheldThisMove()) velorum.finishRecall();
		}
	},
	CALL_TO_PRAYER(10, Kind.AREA, 0b110, 24, 6, 20, 300, 0.0F,
			VelorumSelectionRules.Cells.of(1, 1, 1, 1, 1.0F, 1.0F, 1.0F)) {
		private static final int WEIGHT = 16;

		@Override
		public BossFootprint footprint(SaintMoveInputs in) {
			return BossFootprint.None.INSTANCE;
		}

		@Override
		public boolean eligible(SaintPickContext ctx) {
			return ctx.saint() instanceof VelorumEntity velorum && velorum.forced()
					&& velorum.congregation().canRaise(velorum);
		}

		@Override
		public int weight(SaintPickContext ctx) {
			return WEIGHT;
		}

		@Override
		public void onTick(SaintMoveContext ctx) {
			if (ctx.committedNow() && ctx.saint() instanceof VelorumEntity velorum) {
				velorum.congregation().raise(velorum, VelorumCongregationRules.CALL_RISES);
				VelorumMoves.sound(ctx, SoundEvents.BELL_RESONATE, 0.6F, 0.5F);
			}
		}
	},
	/** Three shapes of liquid dark rise out of a pool and walk a road, each leaving a strip of dark behind it. */
	PROCESSION(11, Kind.AREA, 0b110, 26, 44, 14, 240, 4.0F,
			VelorumSelectionRules.Cells.of(0, 0, 14, 20, 1.0F, 1.0F, 1.0F)) {
		/** Three shades 2 blocks apart walk the line at half a block a tick. */
		private static final double SHADE_SPEED = 0.5D;
		private static final BossFootprint PATH = new BossFootprint.Path(0.6D, SHADE_SPEED, 1.0D, 3, 2.0D, Height.STANDING);
		private static final double HALF_LENGTH = 9.0D;

		@Override
		public BossFootprint footprint(SaintMoveInputs in) {
			return PATH;
		}

		@Override
		public int hitEvery() {
			return 4;
		}

		@Override
		public int weight(SaintPickContext ctx) {
			return VelorumSelectionRules.withPressure(super.weight(ctx), VelorumSelectionRules.hiding(ctx.unseenTicks()));
		}

		@Override
		public void onTick(SaintMoveContext ctx) {
			if (ctx.tick() == 0 || ctx.committedNow()) {
				Vec3 target = ctx.frame().target(), origin = ctx.frame().origin();
				Vec3 view = new Vec3(target.x - origin.x, 0.0D, target.z - origin.z);
				Vec3 side = view.lengthSqr() < 1.0E-6D ? new Vec3(1.0D, 0.0D, 0.0D)
						: new Vec3(-view.z, 0.0D, view.x).normalize();
				Vec3 a = ctx.arena().clamp(target.add(side.scale(HALF_LENGTH)));
				Vec3 b = ctx.arena().clamp(target.subtract(side.scale(HALF_LENGTH)));
				ctx.saint().controller().setPoints(List.of(a, b));
			}
			if (!(ctx.saint() instanceof VelorumEntity velorum)) return;
			List<Vec3> road = ctx.saint().controller().frame() == null ? List.of() : ctx.saint().controller().frame().points();
			if (ctx.tick() == 0) {
				VelorumMoves.sound(ctx, SoundInit.ENTITY_WILL_MATERIALIZE.get(), 1.0F, 0.6F);
				// the shades rise out of a pool at the road's far end
				if (!road.isEmpty()) {
					int span = ctx.saint().controller().timeline().total();
					velorum.dark().pool(velorum, road.get(0), VelorumDarkRules.cap(VelorumDarkRules.Source.PROCESSION,
							ctx.inputs().phase()), span + VelorumDarkRules.life(VelorumDarkRules.Source.PROCESSION, ctx.inputs().phase()));
				}
			}
			if (ctx.committedNow() && road.size() >= 2) {
				int active = ctx.saint().controller().timeline().active();
				velorum.dark().strip(velorum, road.get(0), road.get(road.size() - 1), SHADE_SPEED,
						active + VelorumDarkRules.STRIP_LIFE);
			}
		}

		@Override
		public void onHit(SaintMoveContext ctx, LivingEntity target) {
			if (!VelorumMoves.hurt(ctx, target, baseDamage())) return;
			VelorumMoves.stillness(ctx, target, 30.0F);
			VelorumMoves.sound(ctx.saint(), target.position(), SoundInit.ENTITY_WILL_DISSOLVE.get(), 0.8F, 1.0F);
		}
	},
	/** Cruor splinters burst up out of the blood the challengers spilled where they stood still. */
	SPLINTER_CROWN(12, Kind.AREA, 0b110, 26, 4, 20, 160, 7.0F,
			VelorumSelectionRules.Cells.of(0, 10, 18, 12, 1.0F, 1.0F, 1.0F)) {
		private static final BossFootprint DISCS = new BossFootprint.Discs(1.5D, 0, Height.STANDING);
		private static final int LOCK_TICK = 14;
		private static final int SPILLED = 2;
		private static final int MAX_POINTS = 8;

		@Override
		public BossFootprint footprint(SaintMoveInputs in) {
			return DISCS;
		}

		@Override
		public void onTick(SaintMoveContext ctx) {
			if (!(ctx.saint() instanceof VelorumEntity velorum)) return;
			// resolution A-2 and R.5: each challenger's stillest spot, plus the two places his hits spilled most blood
			if (ctx.tick() == 0 || ctx.tick() == LOCK_TICK) {
				List<Vec3> points = new ArrayList<>();
				for (Player player : ctx.arena().challengers()) {
					if (points.size() >= MAX_POINTS - SPILLED) break;
					points.add(velorum.stillness().stillest(player));
				}
				points.addAll(velorum.crownSpills(SPILLED, ctx.tick() == 0));
				ctx.saint().controller().setPoints(points);
			}
			if (ctx.tick() == 0) VelorumMoves.sound(ctx, SoundEvents.CHAIN_BREAK, 1.0F, 0.8F);
			if (ctx.committedNow()) VelorumMoves.sound(ctx, SoundEvents.GLASS_BREAK, 1.0F, 0.6F);
		}

		@Override
		public void onHit(SaintMoveContext ctx, LivingEntity target) {
			if (VelorumMoves.hurt(ctx, target, baseDamage())) VelorumMoves.stillness(ctx, target, 10.0F);
		}
	},
	ENDLESS_HOUR(13, Kind.AREA, 0b100, 30, 200, 30, 0, 6.0F,
			VelorumSelectionRules.Cells.of(0, 0, 0, 0, 1.0F, 1.0F, 1.0F)) {
		@Override
		public BossFootprint footprint(SaintMoveInputs in) {
			return BossFootprint.None.INSTANCE;
		}

		/** Resolution D-7: started by the fight's state, never by weight or cooldown. */
		@Override
		public boolean picked() {
			return false;
		}

		@Override
		public void onTick(SaintMoveContext ctx) {
			if (!(ctx.saint() instanceof VelorumEntity velorum)) return;
			if (ctx.tick() == 0) VelorumMoves.sound(ctx, SoundEvents.CONDUIT_DEACTIVATE, 1.0F, 0.5F);
			if (ctx.committedNow()) velorum.beginHour();
			if (VelorumMoves.lastActive(ctx)) velorum.closeHour(ctx);
		}

		@Override
		public @Nullable SaintOpening.Spec recoveryOpening(SaintMoveContext ctx) {
			if (ctx.saint() instanceof VelorumEntity velorum && velorum.hourBroken()) return null;
			return VelorumOpeningRules.gutter(VelorumOpeningRules.gutterTicks(ctx.inputs().phase(), true));
		}
	},
	HOLLOW_TOLL(14, Kind.AREA, 0b100, 24, 20, 20, 60, 7.0F,
			VelorumSelectionRules.Cells.of(30, 22, 0, 0, 1.0F, 1.0F, 1.0F)) {
		/** Out to 9 blocks at 0.45 a tick; full height: keep moving and it passes through you (resolution A-3). */
		private static final BossFootprint RING = new BossFootprint.Ring(1.0D, 9.0D, 1.0D, 1, 0, 18, Height.STANDING);

		@Override
		public BossFootprint footprint(SaintMoveInputs in) {
			return RING;
		}

		@Override
		public Aim aim() {
			return Aim.LOCK_AT_START;
		}

		@Override
		public List<SaintFollowUp> followUps(int phase) {
			return List.of(new SaintFollowUp(HOLLOW_TOLL, 0.55F, 8, FollowCondition.ALWAYS));
		}

		@Override
		public void onTick(SaintMoveContext ctx) {
			if (ctx.tick() == 0) VelorumMoves.welling(ctx);
			if (ctx.committedNow()) VelorumEffects.toll(ctx);
		}

		@Override
		public void onHit(SaintMoveContext ctx, LivingEntity target) {
			if (VelorumMoves.moving(target)) return;
			if (VelorumMoves.hurt(ctx, target, baseDamage())) VelorumMoves.stillness(ctx, target, 15.0F);
		}
	},
	SILENCE_DRAW(15, Kind.AREA, 0b111, 24, 20, 18, 220, 1.0F,
			VelorumSelectionRules.Cells.of(0, 10, 12, 0, 1.0F, 1.0F, 1.0F)) {
		private static final BossFootprint DRAW = new BossFootprint.Occluded(
				new BossFootprint.Disc(BossFootprint.Anchor.ORIGIN, 10.0D, Height.STANDING));

		@Override
		public BossFootprint footprint(SaintMoveInputs in) {
			return DRAW;
		}

		@Override
		public int hitEvery() {
			return 1;
		}

		@Override
		public boolean eligible(SaintPickContext ctx) {
			return VelorumDeedRules.litany(ctx.deedMask());
		}

		@Override
		public void onTick(SaintMoveContext ctx) {
			if (ctx.tick() == 0) VelorumMoves.sound(ctx, SoundInit.RITE_OFFERING_DRAWN.get(), 1.0F, 0.6F);
		}

		@Override
		public void onHit(SaintMoveContext ctx, LivingEntity target) {
			if (ctx.saint() instanceof VelorumEntity velorum && target instanceof Player player) velorum.drawSilence(ctx, player);
		}
	},

	// ── revision 2026-11-06: Frozen Cruor and liquid dark ────────────

	/**
	 * Arrested Pulse: the claw closes as round a heart, and the target's blood slows and crystallises. No damage at the
	 * commit; the mark beats at 30, 60 and 90 ticks after it ({@link VelorumPulseRules}). The footprint is the beat's
	 * disc round the target, shown through the wind-up; it strikes nothing itself.
	 */
	ARRESTED_PULSE(16, Kind.AREA, 0b111, 22, 4, 16, 260, 0.0F,
			VelorumSelectionRules.Cells.of(0, 10, 14, 12, 1.0F, 1.0F, 1.0F)) {
		private static final BossFootprint DISC = new BossFootprint.Disc(BossFootprint.Anchor.TARGET,
				VelorumPulseRules.SPLASH, Height.STANDING);

		@Override
		public BossFootprint footprint(SaintMoveInputs in) {
			return DISC;
		}

		@Override
		public boolean eligible(SaintPickContext ctx) {
			return !(ctx.saint() instanceof VelorumEntity velorum && ctx.target() instanceof Player player
					&& velorum.stillness().marked(player));
		}

		@Override
		public int weight(SaintPickContext ctx) {
			return VelorumSelectionRules.withPressure(super.weight(ctx), VelorumPulseRules.coldPressure(ctx.targetMeter()));
		}

		@Override
		public List<SaintFollowUp> followUps(int phase) {
			if (phase < 3) return List.of();
			return List.of(new SaintFollowUp(GLOAM_CUTS, 0.45F, 8,
					FollowCondition.bands(BossBands.Band.NEAR, BossBands.Band.MID)));
		}

		@Override
		public void onTick(SaintMoveContext ctx) {
			if (ctx.tick() == 0) VelorumMoves.sound(ctx, SoundInit.RITE_OFFERING_DRAWN.get(), 0.8F, 0.5F);
			if (ctx.committedNow() && ctx.saint() instanceof VelorumEntity velorum) velorum.arrestPulse(ctx);
		}

		@Override
		public void onHit(SaintMoveContext ctx, LivingEntity target) {
		}
	},
	/**
	 * Cruor Javelins: cruor drawn up the staff into the lantern cage freezes into three shafts (five in phase 3), loosed
	 * four ticks apart at the target, its lead and its mirrored lead. The footprint is one {@code Tether} a shaft from his
	 * body to {@code anchor(i)}, the shaft's end cut at the first pillar, figure or wall, with the shaft's loose as the
	 * part's delay; the shaft strikes at its head as it flies at 1.5 a tick.
	 */
	CRUOR_JAVELINS(17, Kind.AREA, 0b110, 24, 20, 18, 180, 7.0F,
			VelorumSelectionRules.Cells.of(0, 6, 18, 24, 1.0F, 0.5F, 0.0F)) {
		private static final BossFootprint THREE = shafts(3);
		private static final BossFootprint FIVE = shafts(5);
		private static final int ACTIVE_PHASE_THREE = 28;

		private static BossFootprint shafts(int count) {
			List<BossFootprint.Union.Part> parts = new ArrayList<>();
			for (int i = 0; i < count; i++) {
				parts.add(new BossFootprint.Union.Part(new BossFootprint.Tether(BossFootprint.End.BODY,
						BossFootprint.End.ANCHOR, i, JAVELIN_HALF_WIDTH, 0.0D, Height.STANDING), i * JAVELIN_GAP));
			}
			return new BossFootprint.Union(parts);
		}

		@Override
		public BossMoveTimeline timeline(SaintMoveInputs in) {
			BossMoveTimeline base = super.timeline(in);
			return in.phase() >= 3 ? new BossMoveTimeline(base.windup(), ACTIVE_PHASE_THREE, base.recovery()) : base;
		}

		@Override
		public BossFootprint footprint(SaintMoveInputs in) {
			return in.phase() >= 3 ? FIVE : THREE;
		}

		@Override
		public int hitEvery() {
			return 1;
		}

		@Override
		public void onTick(SaintMoveContext ctx) {
			if (!(ctx.saint() instanceof VelorumEntity velorum)) return;
			int count = javelins(ctx.inputs());
			if (ctx.tick() == 0) {
				VelorumEffects.javelinDraw(ctx);
				VelorumMoves.sound(ctx, SoundInit.RITE_OFFERING_DRAWN.get(), 1.0F, 0.5F);
			}
			// the shafts freeze together at the cage one after another through the wind-up
			for (int i = 0; i < count; i++) {
				if (ctx.tick() == JAVELIN_FORM + i * (JAVELIN_FORMED - JAVELIN_FORM) / count) {
					VelorumMoves.sound(ctx, SoundEvents.GLASS_PLACE, 0.8F, 0.5F);
				}
			}
			// the aims track until the commit, then lock
			if (ctx.stage() == BossMoveTimeline.Stage.WINDUP && ctx.tick() % JAVELIN_AIM_EVERY == 0 || ctx.committedNow()) {
				velorum.aimJavelins(ctx, count);
			}
			int age = ctx.age();
			for (int i = 0; i < count; i++) {
				int since = age - i * JAVELIN_GAP;
				if (since == 0) VelorumMoves.sound(ctx, SoundEvents.TRIDENT_THROW.value(), 1.0F, 0.6F);
				if (since > 0 && since == velorum.javelinLands(ctx.frame(), i)) velorum.javelinLanded(ctx.frame(), i);
			}
		}

		@Override
		public void onHit(SaintMoveContext ctx, LivingEntity target) {
			if (!(ctx.saint() instanceof VelorumEntity velorum)) return;
			FootprintFrame frame = ctx.frame();
			for (int i = 0; i < Math.min(javelins(ctx.inputs()), frame.anchors().size()); i++) {
				double since = ctx.age() - i * JAVELIN_GAP;
				Vec3 end = frame.anchors().get(i);
				if (!head(frame.origin(), end, target.position(), since, JAVELIN_SPEED, JAVELIN_HALF_WIDTH, JAVELIN_LENGTH)) continue;
				if (!velorum.strikeOnce(target, i, JAVELIN_STRIKES)) continue;
				if (!VelorumMoves.hurt(ctx, target, baseDamage())) continue;
				VelorumMoves.stillness(ctx, target, 12.0F);
				VelorumMoves.rime(velorum, target, JAVELIN_RIME_TICKS, VelorumMoves.RIME_CAP);
				VelorumMoves.sound(velorum, target.position(), SoundEvents.GLASS_BREAK, 0.8F, 0.6F);
			}
		}
	},
	/**
	 * Veins of Frost: the frozen and stilled figures' veins flare, and frozen veins run along the floor's block seams
	 * from each toward the target (tracking until tick 18), meeting in a disc; at the commit cruor spines erupt along
	 * every vein. Footprint: a {@code Path} through {@code points} (the meeting point first, then each vein out to its
	 * figure and back) and a {@code Discs} of 2.0 that lands only at {@code points[0]}; {@code anchors} are the figures.
	 */
	VEINS_OF_FROST(18, Kind.AREA, 0b110, 30, 6, 22, 220, 10.0F,
			VelorumSelectionRules.Cells.of(0, 8, 16, 18, 1.0F, 1.0F, 1.0F)) {
		private static final BossFootprint VEINS = new BossFootprint.Union(List.of(
				new BossFootprint.Union.Part(new BossFootprint.Path(VEIN_HALF_WIDTH, 0.0D, 0.0D, 0, 0.0D,
						new Height(0.0D, 1.6D, false)), 0),
				// only points[0], the meeting point, ever lands: the rest are the veins' corners
				new BossFootprint.Union.Part(new BossFootprint.Discs(VEIN_DISC, NEVER_LANDS, Height.STANDING), 0)));
		private static final float VEIN_DAMAGE = 7.0F;

		@Override
		public BossFootprint footprint(SaintMoveInputs in) {
			return VEINS;
		}

		@Override
		public boolean eligible(SaintPickContext ctx) {
			if (!(ctx.saint() instanceof VelorumEntity velorum) || ctx.target() == null) return false;
			return velorum.congregation().feeding(velorum, ctx.target().position(), VelorumSelectionRules.VEIN_REACH).size()
					>= VelorumSelectionRules.VEIN_FEEDERS;
		}

		@Override
		public int weight(SaintPickContext ctx) {
			int feeding = ctx.saint() instanceof VelorumEntity velorum && ctx.target() != null
					? velorum.congregation().feeding(velorum, ctx.target().position(), VelorumSelectionRules.VEIN_REACH).size() : 0;
			return VelorumSelectionRules.withPressure(super.weight(ctx), VelorumSelectionRules.veinFeeders(feeding));
		}

		@Override
		public void onTick(SaintMoveContext ctx) {
			if (!(ctx.saint() instanceof VelorumEntity velorum)) return;
			if (ctx.tick() == 0) VelorumMoves.welling(ctx);
			if (ctx.tick() <= VEIN_LOCK && ctx.tick() % VEIN_EVERY == 0) velorum.layVeins(ctx, ctx.tick() == 0);
			if (ctx.committedNow()) {
				VelorumMoves.sound(ctx, SoundEvents.GLASS_BREAK, 1.0F, 0.45F);
				VelorumMoves.sound(ctx, SoundInit.RITE_STAFF_IMPACT.get(), 1.0F, 0.6F);
			}
		}

		/** One strike a challenger, the larger: 10 in the meeting disc, 7 on a vein. */
		@Override
		public void onHit(SaintMoveContext ctx, LivingEntity target) {
			if (!(ctx.saint() instanceof VelorumEntity velorum) || !velorum.strikeOnce(target, 0, 1)) return;
			List<Vec3> points = ctx.frame().points();
			boolean disc = !points.isEmpty() && BossFootprint.Shapes.planar(points.get(0), target.position()) <= VEIN_DISC;
			if (!VelorumMoves.hurt(ctx, target, disc ? baseDamage() : VEIN_DAMAGE)) return;
			VelorumMoves.stillness(ctx, target, 18.0F);
			VelorumMoves.slow(target, 30, VelorumMoves.SLOWNESS_II);
		}
	},
	/**
	 * The Shadow Step: he sinks straight down into the dark and rises among the congregation, 4 to 6 blocks from the
	 * target beside or behind them. He is moved on the tick before the commit, so the frame's origin at the commit is
	 * where he rose and the arrival {@code Ring} spreads from there; {@code points[0]} is the destination from tick 0, so
	 * it can be drawn before he moves. Untouchable, his presence off, from tick 14 to four past the commit.
	 */
	UMBRAL_STEP(19, Kind.AREA, 0b010, 24, 10, 18, 200, 6.0F,
			VelorumSelectionRules.Cells.of(10, 10, 0, 22, 1.0F, 1.0F, 1.0F)) {
		private static final BossFootprint ARRIVAL = new BossFootprint.Union(List.of(new BossFootprint.Union.Part(
				new BossFootprint.Ring(0.6D, 3.5D, 1.0D, 1, 0, 6, Height.JUMPABLE), 1)));

		@Override
		public BossFootprint footprint(SaintMoveInputs in) {
			return ARRIVAL;
		}

		@Override
		public boolean eligible(SaintPickContext ctx) {
			return !(ctx.saint() instanceof VelorumEntity velorum) || ctx.target() == null
					|| velorum.stepDestination(ctx.target()) != null;
		}

		/** FAR from any side; HUG and NEAR only from behind him; the FAR-60 and grip pressures on top. */
		@Override
		public int weight(SaintPickContext ctx) {
			int base = switch (ctx.band()) {
				case FAR -> cells.far();
				case HUG, NEAR -> ctx.sector() == BossBands.Sector.REAR ? cells.band(ctx.band()) : 0;
				case MID -> 0;
			};
			if (!(ctx.saint() instanceof VelorumEntity velorum)) return base;
			int pressure = (ctx.band() == BossBands.Band.FAR ? VelorumSelectionRules.stepFar(velorum.farTicks()) : 0)
					+ VelorumSelectionRules.stepGrip(velorum.gripped());
			return VelorumSelectionRules.withPressure(base, pressure);
		}

		@Override
		public List<SaintFollowUp> followUps(int phase) {
			return List.of(new SaintFollowUp(CRUOR_RELEASE, 0.6F, 6, FollowCondition.bands(BossBands.Band.NEAR, BossBands.Band.MID)),
					new SaintFollowUp(GLOAM_CUTS, 0.4F, 6, FollowCondition.ALWAYS));
		}

		@Override
		public void onTick(SaintMoveContext ctx) {
			if (ctx.saint() instanceof VelorumEntity velorum) velorum.tickStep(ctx);
		}

		@Override
		public void onHit(SaintMoveContext ctx, LivingEntity target) {
			if (!VelorumMoves.hurt(ctx, target, baseDamage())) return;
			VelorumMoves.stillness(ctx, target, 10.0F);
			if (ctx.saint() instanceof VelorumEntity velorum && target instanceof Player player) {
				velorum.stillness().gloam(player, VelorumDarkRules.GLOAM_TICKS);
			}
		}
	},
	/**
	 * Gloam Cuts: three smoky slashes of liquid dark from his raised claw, 10 long and 3.2 wide, flying at 0.8 a tick,
	 * loosed at +0, +6 and +12, angled -15, +15 and 0 degrees. The first two run low ({@code JUMPABLE}); the third flies
	 * at face height and strikes a body whose top reaches 1.55, so a sneak stoops under it. Each settles into a small
	 * pool where it ends. Footprint: one single-spoke {@code Spokes} a cut, the cut's loose as the part's delay.
	 */
	GLOAM_CUTS(20, Kind.AREA, 0b110, 22, 26, 16, 120, 6.0F,
			VelorumSelectionRules.Cells.of(0, 16, 20, 8, 1.0F, 0.0F, 0.0F)) {
		private static final BossFootprint CUTS = new BossFootprint.Union(List.of(
				new BossFootprint.Union.Part(cut(0), Cut.DELAYS[0]),
				new BossFootprint.Union.Part(cut(1), Cut.DELAYS[1]),
				new BossFootprint.Union.Part(cut(2), Cut.DELAYS[2])));
		private static final int FLANK_WEIGHT = 6;

		private static BossFootprint cut(int index) {
			Height height = Cut.HIGH[index] ? new Height(0.0D, HIGH_CUT_TOP, false) : Height.JUMPABLE;
			return new BossFootprint.Spokes(1, CUT_HALF_WIDTH, 0.0D, CUT_LENGTH, Cut.ANGLES[index], CUT_SPEED, height);
		}

		@Override
		public BossFootprint footprint(SaintMoveInputs in) {
			return CUTS;
		}

		@Override
		public int hitEvery() {
			return 1;
		}

		@Override
		public int weight(SaintPickContext ctx) {
			int base = ctx.sector() == BossBands.Sector.FLANK && cells.band(ctx.band()) > 0 ? FLANK_WEIGHT : super.weight(ctx);
			return VelorumSelectionRules.cuts(base, ctx.phase());
		}

		@Override
		public List<SaintFollowUp> followUps(int phase) {
			return List.of(new SaintFollowUp(CRUOR_JAVELINS, 0.55F, 6, FollowCondition.bands(BossBands.Band.MID, BossBands.Band.FAR)));
		}

		@Override
		public void onTick(SaintMoveContext ctx) {
			if (!(ctx.saint() instanceof VelorumEntity velorum)) return;
			int age = ctx.age();
			for (int i = 0; i < CUT_ANGLES.length; i++) {
				int since = age - CUT_DELAYS[i];
				if (since == 0) {
					VelorumEffects.gloamCut(ctx, i);
					VelorumMoves.sound(ctx, SoundEvents.PLAYER_ATTACK_SWEEP, 1.0F, CUT_HIGH[i] ? 0.8F : 0.5F);
				}
				if (since == CUT_FLIGHT) {
					Vec3 end = BossFootprint.Shapes.at(ctx.frame(), ctx.frame().origin(), CUT_ANGLES[i], CUT_LENGTH);
					int phase = ctx.inputs().phase();
					velorum.dark().pool(velorum, end, VelorumDarkRules.cap(VelorumDarkRules.Source.CUT, phase),
							VelorumDarkRules.life(VelorumDarkRules.Source.CUT, phase));
				}
			}
		}

		@Override
		public void onHit(SaintMoveContext ctx, LivingEntity target) {
			if (!(ctx.saint() instanceof VelorumEntity velorum)) return;
			FootprintFrame frame = ctx.frame();
			double feet = target.getY() - frame.floorY();
			for (int i = 0; i < CUT_ANGLES.length; i++) {
				double since = ctx.age() - CUT_DELAYS[i];
				Vec3 end = BossFootprint.Shapes.at(frame, frame.origin(), CUT_ANGLES[i], CUT_LENGTH);
				if (!head(frame.origin(), end, target.position(), since, CUT_SPEED, CUT_HALF_WIDTH, CUT_DEPTH)) continue;
				if (!cutStrikes(CUT_HIGH[i], feet, target.getBbHeight())) continue;
				if (!velorum.strikeOnce(target, i, CUT_STRIKES)) continue;
				if (!VelorumMoves.hurt(ctx, target, baseDamage())) continue;
				VelorumMoves.stillness(ctx, target, 8.0F);
				if (target instanceof Player player) velorum.stillness().gloam(player, VelorumDarkRules.GLOAM_TICKS);
			}
		}
	},
	/**
	 * The Eclipse: the claw closes over the cage, every lantern in the chamber goes out and the congregation stands up
	 * as torn umbral shapes that reach ({@link VelorumEclipseRules}). Started by state, never weight; relit by held
	 * Projection into a 60-tick Guttering Lantern, unbroken into a half one.
	 */
	ECLIPSE(21, Kind.AREA, 0b100, 40, 160, 30, 0, 0.0F,
			VelorumSelectionRules.Cells.of(0, 0, 0, 0, 1.0F, 1.0F, 1.0F)) {
		private static final int LIGHT_GOES = 30;
		private static final int HEARTBEAT = 40;

		@Override
		public BossFootprint footprint(SaintMoveInputs in) {
			return BossFootprint.None.INSTANCE;
		}

		@Override
		public boolean picked() {
			return false;
		}

		@Override
		public void onTick(SaintMoveContext ctx) {
			if (!(ctx.saint() instanceof VelorumEntity velorum)) return;
			if (ctx.tick() == 0) velorum.eclipseOnset(ctx);
			if (ctx.tick() == LIGHT_GOES) VelorumMoves.sound(ctx, SoundEvents.BEACON_DEACTIVATE, 1.0F, 0.35F);
			if (ctx.committedNow()) velorum.beginEclipse(ctx);
			int active = VelorumMoves.activeTick(ctx);
			if (active >= 0 && velorum.eclipseHolding()) {
				velorum.tickEclipse(active);
				if (active % HEARTBEAT == 0) VelorumMoves.sound(ctx, SoundInit.RITE_AMBIENT_HEARTBEAT.get(), 1.0F, 0.45F);
			}
			if (VelorumMoves.lastActive(ctx)) velorum.closeEclipse();
		}

		@Override
		public @Nullable SaintOpening.Spec recoveryOpening(SaintMoveContext ctx) {
			boolean relit = ctx.saint() instanceof VelorumEntity velorum && velorum.eclipseRelit();
			return VelorumOpeningRules.gutter(VelorumEclipseRules.gutterTicks(ctx.inputs().phase(), relit));
		}
	};

	/** The moves Velorum holds an invocation through; struck in their hold, they overhold. */
	public static final List<VelorumMove> INVOCATIONS = List.of(BID_TO_KNEEL, RECALL, SILENCE_DRAW);

	// ── the flying fronts' numbers ───────────────────────────────────

	/** Cruor Javelins: 18 long, 0.45 half-width, 1.5 a tick, loosed 4 apart, at most two a challenger. */
	public static final double JAVELIN_REACH = 18.0D;
	public static final double JAVELIN_HALF_WIDTH = 0.45D;
	public static final double JAVELIN_SPEED = 1.5D;
	/** The shaft's own length behind its point: a body this far behind the head is still struck. */
	public static final double JAVELIN_LENGTH = 1.4D;
	public static final int JAVELIN_GAP = 4;
	public static final int JAVELIN_STRIKES = 2;
	/** Two Rime levels for 120 ticks, the Cruor Javelin's own. */
	public static final int JAVELIN_RIME_TICKS = 120;
	/** The shafts freeze together at the cage from tick 8 to 22; the aims are re-read every 2 ticks. */
	public static final int JAVELIN_FORM = 8;
	public static final int JAVELIN_FORMED = 22;
	public static final int JAVELIN_AIM_EVERY = 2;
	/** The target's lead: where they will be in 10 ticks; the mirror is where they were 10 ago, the side shafts 2 off. */
	public static final int JAVELIN_LEAD = 10;
	public static final double JAVELIN_SIDE = 2.0D;

	/** Veins of Frost: 0.5 half-width veins, a meeting disc of 2.0, the veins re-laid every 3 ticks until tick 18. */
	public static final double VEIN_HALF_WIDTH = 0.5D;
	public static final double VEIN_DISC = 2.0D;
	public static final int VEIN_LOCK = 18;
	public static final int VEIN_EVERY = 3;
	/** A {@code Discs} landing gap no move outlives: only the first point lands. */
	static final int NEVER_LANDS = 100_000;

	/** Gloam Cuts: 10 long, 1.6 half-width, 0.8 a tick, at +0, +6 and +12, at -15, +15 and 0 degrees. */
	public static final double CUT_LENGTH = 10.0D;
	public static final double CUT_HALF_WIDTH = 1.6D;
	public static final double CUT_SPEED = 0.8D;
	/** A cut's crescent is this deep behind its leading edge. */
	public static final double CUT_DEPTH = 1.0D;
	public static final int[] CUT_DELAYS = Cut.DELAYS;
	public static final double[] CUT_ANGLES = Cut.ANGLES;
	public static final boolean[] CUT_HIGH = Cut.HIGH;
	/** A cut flies 12.5 ticks; its pool opens on the tick after it ends. */
	public static final int CUT_FLIGHT = 13;
	public static final int CUT_STRIKES = 2;
	/** The high cut: it strikes a body whose top reaches 1.55 and whose feet are under 2.6. */
	public static final double HIGH_CUT_BOTTOM = 1.55D;
	public static final double HIGH_CUT_TOP = 2.6D;

	/**
	 * The cuts' arrays, in a holder of their own: a constant's body builds its footprint while this enum's own statics are
	 * still unset, and a holder class is initialised on first use.
	 */
	static final class Cut {
		static final int[] DELAYS = {0, 6, 12};
		static final double[] ANGLES = {-15.0D, 15.0D, 0.0D};
		static final boolean[] HIGH = {false, false, true};

		private Cut() {
		}
	}

	private final int id;
	private final Kind kind;
	private final int phases;
	private final int windup;
	private final int active;
	private final int recovery;
	private final int cooldown;
	private final float damage;
	protected final VelorumSelectionRules.Cells cells;

	VelorumMove(int id, Kind kind, int phases, int windup, int active, int recovery, int cooldown, float damage,
			VelorumSelectionRules.Cells cells) {
		this.id = id;
		this.kind = kind;
		this.phases = phases;
		this.windup = windup;
		this.active = active;
		this.recovery = recovery;
		this.cooldown = cooldown;
		this.damage = damage;
		this.cells = cells;
	}

	@Override
	public int id() {
		return id;
	}

	@Override
	public String key() {
		return name().toLowerCase(java.util.Locale.ROOT);
	}

	@Override
	public Kind kind() {
		return kind;
	}

	/** Every move tracks until its commit, then locks, unless it says otherwise. */
	@Override
	public Aim aim() {
		return Aim.TRACK;
	}

	@Override
	public boolean inPhase(int phase) {
		return phase >= 1 && phase <= 3 && (phases & (1 << (phase - 1))) != 0;
	}

	/** The authored wind-up, active span and recovery, before the phase cut and the Repose deed. */
	public BossMoveTimeline authored() {
		return new BossMoveTimeline(windup, active, recovery);
	}

	@Override
	public BossMoveTimeline timeline(SaintMoveInputs in) {
		int wind = in.phase() >= 3 ? SaintMoves.cut(kind, windup, VelorumMoves.PHASE_THREE_CUT)
				: Math.max(windup, SaintMoves.revealFloor(kind));
		int rec = VelorumDeedRules.recovery(recovery, VelorumDeedRules.repose(in.deedMask()));
		return new BossMoveTimeline(wind, active, rec);
	}

	@Override
	public TelegraphSpec telegraph(SaintMoveInputs in) {
		return new TelegraphSpec(null, null, 0, id, true);
	}

	@Override
	public int weight(SaintPickContext ctx) {
		return cells.weight(ctx.band(), ctx.sector());
	}

	@Override
	public int cooldown(SaintPickContext ctx) {
		return cooldown;
	}

	@Override
	public float baseDamage() {
		return damage;
	}

	@Override
	public void onHit(SaintMoveContext ctx, LivingEntity target) {
		VelorumMoves.hurt(ctx, target, baseDamage());
	}

	public static @Nullable VelorumMove byId(int id) {
		for (VelorumMove move : values()) if (move.id == id) return move;
		return null;
	}

	/** How many shafts Cruor Javelins looses: three, five in phase 3. */
	public static int javelins(SaintMoveInputs in) {
		return in.phase() >= 3 ? 5 : 3;
	}

	/**
	 * Whether {@code point} is under a flying front's head {@code since} ticks after its loose: the front runs from
	 * {@code from} toward {@code to} at {@code speed}, is {@code halfWidth} wide each side, and strikes what lies from
	 * its edge back {@code depth} plus one tick's travel, never behind where it left. Once it has reached {@code to} it
	 * strikes nothing more.
	 */
	public static boolean head(Vec3 from, Vec3 to, Vec3 point, double since, double speed, double halfWidth, double depth) {
		if (since < 0.0D) return false;
		double dx = to.x - from.x, dz = to.z - from.z;
		double length = Math.sqrt(dx * dx + dz * dz);
		if (length < 1.0E-6D) return false;
		double before = speed * (since - 1.0D);
		if (before >= length) return false;
		double front = Math.min(length, speed * since);
		double along = ((point.x - from.x) * dx + (point.z - from.z) * dz) / length;
		double off = Math.abs(((point.x - from.x) * dz - (point.z - from.z) * dx) / length);
		return off <= halfWidth && along >= 0.0D && along <= front && along >= front - speed - depth;
	}

	/**
	 * Whether a Gloam Cut strikes a body with its feet {@code feet} above the floor and {@code height} tall: a low cut
	 * strikes feet up to 0.9 (a jump clears it); the high cut strikes a body whose top reaches 1.55 (a sneak, 1.5 tall,
	 * stoops under it; a standing body, 1.8, does not), with its feet under 2.6.
	 */
	public static boolean cutStrikes(boolean high, double feet, double height) {
		if (!high) return Height.JUMPABLE.admits(feet, true);
		return feet + height >= HIGH_CUT_BOTTOM - 1.0E-6D && feet <= HIGH_CUT_TOP + 1.0E-6D;
	}
}
