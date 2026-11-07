package com.vincenthuto.hemomancy.common.entity.boss.saint.velorum;

import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoveContext;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * Velorum's server cues: every one goes to the client as a {@code VELORUM_RIME} cue whose count is the beat below, drawn
 * as world geometry by {@code VelorumRimeGeometry} in the RIME and UMBRA materials, and since the Frozen Cruor revision
 * (2026-11-06) by {@code VelorumTelegraphPass} in his cruor and liquid-dark materials. The only particles are a few fine
 * glow motes (frost light) and blood-cell motes (blood leaving a body) as accents; no vanilla particle and no hit glow.
 */
public final class VelorumEffects {
	public static final int TOLL = 1, FROST_SLASH = 2, STILL_FLOOR = 3, LONG_NIGHT = 4, SHATTER = 5, PRESERVED = 6,
			FREED = 7, THAW = 8, WAKING = 9, PINNING = 10, KNEEL_HOME = 11, BOW = 12, HOUR_BROKEN = 13, REST = 14,
			GUARDED = 15, OVERHELD = 16, GUTTER = 17, LITANY_TOLL = 18, BLACK_ICE = 19;
	/** The Release of Cruor's standing field (revision 2026-11-06): the beat {@code BLACK_ICE} kept its number. */
	public static final int CRUOR_RELEASE = BLACK_ICE;
	/**
	 * The Frozen Cruor and liquid dark revision's beats (design R.16 EFFECTS 3). Each rides a {@code VELORUM_RIME} cue
	 * whose count is {@link VelorumVisualRules#pack}ed with the entity it is about, so a mark or a grip follows its body.
	 */
	public static final int STAIN = 20, PULSE_MARK = 21, PULSE_BEAT = 22, PULSE_BURST = 23, PULSE_END = 24, JAVELIN_DRAW = 25,
			VEIN_FLARE = 26, STEP_SINK = 27, STEP_RISE = 28, GLOAM_CUT = 29, ECLIPSE_OUT = 30, ECLIPSE_RELIT = 31,
			SHAPE_REACH = 32;

	/** How long each beat's geometry lives, by beat. */
	public static int life(int kind) {
		return switch (kind) {
			case STAIN -> VelorumVisualRules.STAIN_LIFE;
			case PULSE_MARK -> VelorumVisualRules.MARK_LIFE;
			case PULSE_BEAT -> VelorumVisualRules.PASS + 4;
			case PULSE_BURST -> VelorumVisualRules.BURST + 6;
			case PULSE_END -> 12;
			case JAVELIN_DRAW -> VelorumVisualRules.FORM_END + 4;
			case VEIN_FLARE -> 40;
			case STEP_SINK, STEP_RISE -> 30;
			case GLOAM_CUT -> 10;
			case ECLIPSE_OUT -> 24;
			case ECLIPSE_RELIT -> VelorumVisualRules.RELIT_BURST + VelorumVisualRules.RELIT_TEAR + 8;
			case SHAPE_REACH -> VelorumVisualRules.REACH_TELL + VelorumVisualRules.GRIP;
			case LONG_NIGHT -> 42;
			case PRESERVED -> VelorumStillnessRules.PRESERVED_TICKS;
			case REST -> 60;
			case BOW -> 24;
			case WAKING, PINNING -> 6;
			case BLACK_ICE -> 60;
			case THAW -> 30;
			default -> 20;
		};
	}

	private static final ParticleColor FROST = new ParticleColor(196, 226, 248);
	private static final ParticleColor CORE = new ParticleColor(232, 244, 255);
	/** Dark oxblood: blood drawn out of a body to be frozen (R.2 {@code CRUOR_LIT}). */
	private static final ParticleColor BLOOD = new ParticleColor(106, 28, 38);

	private VelorumEffects() {
	}

	public static void cue(ServerLevel level, int kind, Vec3 from, Vec3 to, double radius) {
		ManipulationVisuals.burst(level, ManipulationVisuals.Form.VELORUM_RIME, from, to, radius, life(kind), kind);
	}

	/** A beat about one entity: its id rides the packed count, so the client draws it on that body wherever it goes. */
	public static void cueOn(ServerLevel level, int kind, Entity about, Vec3 from, Vec3 to, double radius) {
		ManipulationVisuals.burst(level, ManipulationVisuals.Form.VELORUM_RIME, from, to, radius, life(kind),
				VelorumVisualRules.pack(kind, about.getId()));
	}

	/** Blood leaving a body as it is drawn on: a few blood-cell motes, the accent of the cruor's source. */
	public static void bleed(ServerLevel level, Vec3 at, int count, double spread) {
		level.sendParticles(HemoParticleData.bloodCell(BLOOD), at.x, at.y, at.z, count, spread, spread * 0.6D, spread, 0.02D);
	}

	/** A few fine motes rising: an accent, never the effect. */
	public static void motes(ServerLevel level, Vec3 at, int count, double spread, boolean core) {
		level.sendParticles(HemoParticleData.glow(core ? CORE : FROST), at.x, at.y, at.z, count, spread, spread * 0.5D,
				spread, 0.01D);
	}

	public static void toll(SaintMoveContext ctx) {
		Vec3 at = ctx.frame().origin();
		cue(ctx.level(), TOLL, at, ctx.frame().aim(), 3.5D);
		VelorumMoves.sound(ctx, com.vincenthuto.hemomancy.common.init.SoundInit.RITE_STAFF_IMPACT.get(), 1.0F, 0.7F);
		VelorumMoves.sound(ctx, net.minecraft.sounds.SoundEvents.BELL_RESONATE, 0.5F, 0.5F);
	}

	public static void frostSlash(SaintMoveContext ctx, LivingEntity target) {
		Vec3 at = target.position().add(0.0D, target.getBbHeight() * 0.6D, 0.0D);
		cue(ctx.level(), FROST_SLASH, at, ctx.frame().origin().add(0.0D, 1.4D, 0.0D), 0.8D);
	}

	public static void stillFloor(SaintMoveContext ctx) {
		cue(ctx.level(), STILL_FLOOR, ctx.frame().origin(), ctx.frame().origin(), 10.0D);
	}

	public static void longNight(SaintMoveContext ctx) {
		cue(ctx.level(), LONG_NIGHT, ctx.frame().origin(), ctx.frame().origin(), 2.5D);
	}

	public static void blackIce(VelorumEntity velorum, Vec3 origin, Vec3 aim) {
		cue((ServerLevel) velorum.level(), BLACK_ICE, origin, aim, 12.0D);
	}

	public static void shatter(SaintMoveContext ctx, LivingEntity target) {
		cue(ctx.level(), SHATTER, target.position(), target.position(), 1.2D);
	}

	public static void preserved(VelorumEntity velorum, Player player) {
		cue((ServerLevel) velorum.level(), PRESERVED, player.position(), player.position(), player.getBbWidth());
		// the shell is the player's own blood: it leaves the chest as the panes set from the feet up
		bleed((ServerLevel) velorum.level(), player.position().add(0.0D, player.getBbHeight() * 0.6D, 0.0D), 6, 0.25D);
	}

	public static void freed(VelorumEntity velorum, Player player) {
		cue((ServerLevel) velorum.level(), FREED, player.position(), player.position(), player.getBbWidth());
	}

	public static void thaw(VelorumEntity velorum, BlockPos figure) {
		Vec3 at = Vec3.atBottomCenterOf(figure);
		cue((ServerLevel) velorum.level(), THAW, at, at, 0.6D);
	}

	/** A fed tick of warm blood into a figure: the cracks spread as {@code progress} runs 0 to 1. */
	public static void waking(VelorumEntity velorum, BlockPos figure, float progress) {
		if (velorum.level().getGameTime() % 4 != 0) return;
		Vec3 at = Vec3.atBottomCenterOf(figure);
		cue((ServerLevel) velorum.level(), WAKING, at, at, Math.max(0.05D, progress));
	}

	/** A fed tick of the Apotheos's hyphae threading into a figure's base. */
	public static void pinning(VelorumEntity velorum, BlockPos figure, Player player) {
		if (velorum.level().getGameTime() % 4 != 0) return;
		cue((ServerLevel) velorum.level(), PINNING, player.position().add(0.0D, 1.0D, 0.0D), Vec3.atBottomCenterOf(figure), 0.6D);
	}

	public static void kneelHome(VelorumEntity velorum, Vec3 at) {
		cue((ServerLevel) velorum.level(), KNEEL_HOME, at, at, 0.8D);
		motes((ServerLevel) velorum.level(), at.add(0.0D, 0.8D, 0.0D), 3, 0.3D, false);
	}

	public static void bow(VelorumEntity velorum) {
		Vec3 at = velorum.position().add(0.0D, 1.5D, 0.0D);
		cue((ServerLevel) velorum.level(), BOW, at, at, 1.0D);
	}

	public static void hourBroken(VelorumEntity velorum) {
		cue((ServerLevel) velorum.level(), HOUR_BROKEN, velorum.position(), velorum.position(), 6.0D);
	}

	public static void guarded(VelorumEntity velorum, Vec3 at) {
		cue((ServerLevel) velorum.level(), GUARDED, at, at, 0.6D);
	}

	public static void overheld(VelorumEntity velorum) {
		Vec3 at = velorum.position().add(0.0D, 1.8D, 0.0D);
		cue((ServerLevel) velorum.level(), OVERHELD, at, at, 1.0D);
	}

	public static void gutter(VelorumEntity velorum) {
		cue((ServerLevel) velorum.level(), GUTTER, velorum.position(), velorum.position(), 2.0D);
	}

	public static void litanyToll(VelorumEntity velorum) {
		cue((ServerLevel) velorum.level(), LITANY_TOLL, velorum.position(), velorum.position(), VelorumDeedRules.TOLL_RADIUS);
	}

	/** The frost falling out of the light: the rest burst as his body fails, motes through each lantern. */
	public static void rest(VelorumEntity velorum) {
		Vec3 at = velorum.position().add(0.0D, 1.5D, 0.0D);
		cue((ServerLevel) velorum.level(), REST, at, at, 1.5D);
		motes((ServerLevel) velorum.level(), at, 4, 0.5D, true);
	}

	// ── the Frozen Cruor and liquid dark revision (2026-11-06) ───────

	/** The Release of Cruor's field, under its new name; the old call stays for the server lane's rename. */
	public static void cruorRelease(VelorumEntity velorum, Vec3 origin, Vec3 aim) {
		blackIce(velorum, origin, aim);
	}

	/**
	 * A challenger's stillest spot or a place his hits drew blood, every 20 ticks (R.8): a dark stain that deepens with
	 * {@code depth} (0 fresh to 1 soaked), so the Splinter Crown's sites show before it comes.
	 */
	public static void stain(VelorumEntity velorum, Vec3 at, float depth) {
		cue((ServerLevel) velorum.level(), STAIN, at, at, Math.max(0.0D, Math.min(1.0D, depth)));
	}

	/** Arrested Pulse's mark at its commit: the client draws the veins, the wrist frost, the tightening ring and the beats. */
	public static void pulseMark(VelorumEntity velorum, Player marked) {
		cueOn((ServerLevel) velorum.level(), PULSE_MARK, marked, marked.position(), velorum.position(), 1.5D);
		// the black-red veins under the skin from the wrists up: the shared Congeatio skin, held for the mark's life
		ManipulationVisuals.attached(marked, ManipulationVisuals.Form.FROZEN_VEINS, marked.getBbWidth(), VelorumVisualRules.MARK_LIFE, 1);
	}

	/** A beat the marked walked through: the wrist frost cracks off in pale motes. {@code beat} is 0, 1 or 2. */
	public static void pulseBeat(VelorumEntity velorum, Player marked, int beat) {
		cueOn((ServerLevel) velorum.level(), PULSE_BEAT, marked, marked.position(), marked.position(), beat);
		motes((ServerLevel) velorum.level(), marked.position().add(0.0D, marked.getBbHeight() * 0.45D, 0.0D), 4, 0.25D, false);
	}

	/** A beat on a still or cold challenger: cruor spines burst out of their own blood. */
	public static void pulseBurst(VelorumEntity velorum, Player marked) {
		cueOn((ServerLevel) velorum.level(), PULSE_BURST, marked, marked.position(), marked.position(), 1.5D);
		bleed((ServerLevel) velorum.level(), marked.position().add(0.0D, marked.getBbHeight() * 0.55D, 0.0D), 5, 0.3D);
	}

	/** The mark ended early (Searing, a warming Projection, preservation): its veins and frost fade off the body. */
	public static void pulseEnd(VelorumEntity velorum, Player marked) {
		cueOn((ServerLevel) velorum.level(), PULSE_END, marked, marked.position(), marked.position(), 1.0D);
		ManipulationVisuals.attached(marked, ManipulationVisuals.Form.FROZEN_VEINS, 0.0D, 0, 0);
	}

	/** Cruor Javelins' draw: the cruor leaves the seams under his heel and climbs the staff into the cage. */
	public static void javelinDraw(SaintMoveContext ctx) {
		Vec3 at = ctx.frame().origin();
		cue(ctx.level(), JAVELIN_DRAW, at, ctx.frame().aim(), 1.0D);
	}

	/** A frozen or stilled figure's veins flaring black-red through its rime as he draws on them. */
	public static void veinFlare(VelorumEntity velorum, BlockPos figure) {
		Vec3 at = Vec3.atBottomCenterOf(figure);
		cue((ServerLevel) velorum.level(), VEIN_FLARE, at, velorum.position(), 0.6D);
	}

	/** The Shadow Step's departure: the dark wells up under him where he sinks. */
	public static void stepSink(VelorumEntity velorum, Vec3 at) {
		cue((ServerLevel) velorum.level(), STEP_SINK, at, at, 2.5D);
	}

	/** The Shadow Step's arrival: a pool opens and a torn column of dark rises where he comes up. */
	public static void stepRise(VelorumEntity velorum, Vec3 at) {
		cue((ServerLevel) velorum.level(), STEP_RISE, at, velorum.position(), 2.5D);
	}

	/** A gloam cut leaving his hand ({@code cut} 0, 1 or 2): smoke torn off his fingers. */
	public static void gloamCut(SaintMoveContext ctx, int cut) {
		Vec3 at = ctx.frame().origin().add(0.0D, 1.6D, 0.0D);
		cue(ctx.level(), GLOAM_CUT, at, ctx.frame().aim(), cut);
	}

	/** The Eclipse's light going out: a last cold flash at the cage as the claw closes. */
	public static void eclipseOut(VelorumEntity velorum) {
		Vec3 at = velorum.lanternPoint();
		cue((ServerLevel) velorum.level(), ECLIPSE_OUT, at, velorum.position(), 1.0D);
	}

	/** The Eclipse relit: light bursts through his fingers and the dark tears back to the walls. */
	public static void eclipseRelit(VelorumEntity velorum) {
		Vec3 at = velorum.lanternPoint();
		cue((ServerLevel) velorum.level(), ECLIPSE_RELIT, at, velorum.position(), 16.0D);
		motes((ServerLevel) velorum.level(), at, 6, 0.4D, true);
	}

	/** An Eclipse shape reaching for a challenger from its figure: a 16-tick tell, then the grip while they stay in 6. */
	public static void shapeReach(VelorumEntity velorum, BlockPos figure, Player target) {
		shapeReach(velorum, Vec3.atBottomCenterOf(figure), target);
	}

	/** The same from the shape's own foot. */
	public static void shapeReach(VelorumEntity velorum, Vec3 shape, Player target) {
		cueOn((ServerLevel) velorum.level(), SHAPE_REACH, target, shape, target.position(), 6.0D);
	}
}
