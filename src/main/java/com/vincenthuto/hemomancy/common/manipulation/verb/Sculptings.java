package com.vincenthuto.hemomancy.common.manipulation.verb;

import com.vincenthuto.hemomancy.common.manipulation.animus.BloodBindingManip;
import com.vincenthuto.hemomancy.common.manipulation.animus.BloodBindingSculpting;
import com.vincenthuto.hemomancy.common.manipulation.animus.LivingLandSculpting;
import com.vincenthuto.hemomancy.common.manipulation.animus.TendonLineSculpting;
import com.vincenthuto.hemomancy.common.manipulation.animus.VenousClingSculpting;
import com.vincenthuto.hemomancy.common.manipulation.animus.VitalEffusionSculpting;
import com.vincenthuto.hemomancy.common.manipulation.congeatio.GlacialFreezeSculpting;
import com.vincenthuto.hemomancy.common.manipulation.congeatio.GlacialRampartSculpting;
import com.vincenthuto.hemomancy.common.manipulation.congeatio.GlacialThawSculpting;
import com.vincenthuto.hemomancy.common.manipulation.ferric.FerricRampartSculpting;
import com.vincenthuto.hemomancy.common.manipulation.ferric.GougeCuttingSculpting;
import com.vincenthuto.hemomancy.common.manipulation.ferric.HewingSculpting;
import com.vincenthuto.hemomancy.common.manipulation.ferric.LodeDrawSculpting;
import com.vincenthuto.hemomancy.common.manipulation.ferric.ItemDrawSculpting;
import com.vincenthuto.hemomancy.common.manipulation.ferric.SanguineMendingSculpting;
import com.vincenthuto.hemomancy.common.manipulation.flammeus.FlameKindleSculpting;
import com.vincenthuto.hemomancy.common.manipulation.flammeus.FurnaceVeinsSculpting;
import com.vincenthuto.hemomancy.common.manipulation.flammeus.PyreticForgeSculpting;
import com.vincenthuto.hemomancy.common.manipulation.lux.LuxTraceSculpting;
import com.vincenthuto.hemomancy.common.manipulation.mortem.CarrionDrawSculpting;
import com.vincenthuto.hemomancy.common.manipulation.mortem.LignumMortisManip;
import com.vincenthuto.hemomancy.common.manipulation.mortem.LignumMortisSculpting;
import com.vincenthuto.hemomancy.common.manipulation.tenebris.EclipseSnuffSculpting;
import com.vincenthuto.hemomancy.common.manipulation.verb.strand.LashProfile;
import com.vincenthuto.hemomancy.common.manipulation.verb.strand.LashStyle;
import com.vincenthuto.hemomancy.common.manipulation.verb.strand.StrandForm;
import com.vincenthuto.hemomancy.common.manipulation.verb.strand.StrandSpec;
import com.vincenthuto.hemomancy.common.manipulation.verb.strand.Strands;

/**
 * Every Sculpting, one factory per manipulation it is attached to in {@code ManipulationInit}
 * ({@code setProjectionSculpting(Sculptings::x)} / {@code setAbsorptionSculpting(Sculptings::x)}, or a narrow one tried ahead of
 * the main with {@code addProjectionSculpting(Sculptings::x)}). Players only: no entity
 * cast or Drudge action is ever routed through a Sculpting ({@code SculptingGuardrailTest}).
 */
public final class Sculptings {
	private Sculptings() {
	}

	// ── Lignum Mortis: Projection on wood ──

	public static VerbSculpting lignumMortis() {
		return new LignumMortisSculpting("lignum_mortis", LignumMortisManip.Mode.STANDARD);
	}

	public static VerbSculpting canopyMortis() {
		return new LignumMortisSculpting("canopy_mortis", LignumMortisManip.Mode.CANOPY);
	}

	public static VerbSculpting workedLignum() {
		return new LignumMortisSculpting("worked_lignum", LignumMortisManip.Mode.WORKED);
	}

	// ── Pyretic Forge: Projection on items and blocks ──

	public static VerbSculpting pyreticForge() {
		return new PyreticForgeSculpting();
	}

	// ── Tendon Line: Projection throws, Absorption hauls ──

	public static VerbSculpting tendonLineThrow() {
		return new TendonLineSculpting.Throw();
	}

	public static VerbSculpting tendonLineHaul() {
		return new TendonLineSculpting.Haul();
	}

	// ── Blood Binding: Projection on a creature ──

	public static VerbSculpting bloodBinding() {
		return new BloodBindingSculpting(BloodBindingManip.Mode.BASELINE);
	}

	public static VerbSculpting lingeringBloodBinding() {
		return new BloodBindingSculpting(BloodBindingManip.Mode.LINGERING);
	}

	public static VerbSculpting chainBloodBinding() {
		return new BloodBindingSculpting(BloodBindingManip.Mode.CHAIN);
	}

	public static VerbSculpting bloodLattice() {
		return new BloodBindingSculpting(BloodBindingManip.Mode.LATTICE);
	}

	// ── The Staff's projection mining (core-loop Track 8) ──

	/** Lode Draw: Projection crawls an ore lode and breaks what it reached on release. */
	public static VerbSculpting lodeDraw() {
		return new LodeDrawSculpting();
	}

	/** Hematic Hewing: Projection cuts a tunnel, a face or a stair out of the struck face. */
	public static VerbSculpting hematicHewing() {
		return new HewingSculpting();
	}

	// ── The ground network (core-loop Track 6) ──

	/** Vascular Dowsing: Projection cuts gouges into the faces it sweeps. */
	public static VerbSculpting gougeCutting() {
		return new GougeCuttingSculpting();
	}

	/** Hematic Beacon: Projection spreads fading lines of light across a surface. */
	public static VerbSculpting luxTrace() {
		return new LuxTraceSculpting();
	}

	/** Blood Eclipse: Absorption draws the light out of what it can put out. */
	public static VerbSculpting eclipseSnuff() {
		return new EclipseSnuffSculpting();
	}

	/** Sanguine Magnetism: Absorption draws dropped items to the player. */
	public static VerbSculpting itemDraw() {
		return new ItemDrawSculpting();
	}

	// ── Farming (core-loop Track 7) ──

	/** Vital Effusion: Projection soaks a plant's soil and grows it as bone meal would. */
	public static VerbSculpting vitalEffusion() {
		return new VitalEffusionSculpting();
	}

	// ── Living Land and cling (core-loop Track 9) ──

	/** Living Land: held Projection wakes a patch of ground that strikes nearby enemies, or rises into a step. */
	public static VerbSculpting livingLand() {
		return new LivingLandSculpting();
	}

	/** Venous Cling: Projection into a wall or ceiling the player touches roots them to it. */
	public static VerbSculpting venousCling() {
		return new VenousClingSculpting();
	}

	// ── The second wave (core-loop round two P4) ──

	/** Sanguine Mending: Projection mends a dropped tool, an injured golem, or a worn anvil. */
	public static VerbSculpting sanguineMending() {
		return new SanguineMendingSculpting();
	}

	/** Crimson Flame Conjuration: Projection kindles campfires and candles near the aim, and primes TNT. */
	public static VerbSculpting flameKindle() {
		return new FlameKindleSculpting();
	}

	/** Furnace Veins: Projection burns as fuel in a bank of furnaces for as long as it is held. */
	public static VerbSculpting furnaceVeins() {
		return new FurnaceVeinsSculpting();
	}

	/** Glacial Grasp, narrow, ahead of its plant: Projection freezes open water under the stream. */
	public static VerbSculpting glacialFreeze() {
		return new GlacialFreezeSculpting();
	}

	/** Glacial Grasp: Absorption thaws Frozen Cruor and ice. */
	public static VerbSculpting glacialThaw() {
		return new GlacialThawSculpting();
	}

	/** Glacial Rampart: Projection raises an ice wall along the swept path. */
	public static VerbSculpting glacialRampart() {
		return new GlacialRampartSculpting();
	}

	/** Ferric Rampart: Projection raises an iron wall, a segment a cell, along the swept path. */
	public static VerbSculpting ferricRampart() {
		return new FerricRampartSculpting();
	}

	/** Carrion Communion: Absorption draws Mortem Temper and health, never blood, from flesh, bone and skulls. */
	public static VerbSculpting carrionDraw() {
		return new CarrionDrawSculpting();
	}

	// ── Strands: held Projection as a swung weapon (core-loop Track 12). Blood per pulse, two pulses a second ──

	/**
	 * Blood Lash: the tether, the first strand (CL-23). A light, forgiving rope that lingers longest; a charged crack
	 * holds what it strikes in place, and a sweep that lands gives a little blood back. 15 mL/s.
	 */
	public static VerbSculpting bloodLash() {
		return Strands.lash(7.5D, 0.6F, new LashProfile(4.5F, 8.0D, 12, 0, 7.5D, 20, 1.4F)
				.with(LashStyle.PLAIN.rope(1.0D, 1.25D, 0.97D, 1.6D).root(40).sweepReturn(4.0D)));
	}

	/**
	 * Searing Lash: the heavy whip. A long rope that lags the hand and overshoots; a charge widens the crack and sets
	 * what it strikes alight, each crack in a row hits harder until the whip rests, and its tap is one slow scald.
	 * 60 mL/s.
	 */
	public static VerbSculpting searingLashStrand() {
		return Strands.lash(30.0D, 1.0F, new LashProfile(7.0F, 24.0D, 20, 80, 30.0D, 40, 1.75F)
				.with(LashStyle.PLAIN.rope(1.25D, 0.7D, 1.04D, 1.0D).burst(1.0D, 80).heat(0.15F, 4, 60)));
	}

	/** Hematic Flare: a searchlight that Illuminates and outlines what it passes over, and flashes on the attack button. 40 mL/s. */
	public static VerbSculpting hematicFlareBeam() {
		return new com.vincenthuto.hemomancy.common.manipulation.lux.FlareBeamSculpting();
	}

	/** Blood Eclipse: a dome of dark the caster carries, ended in a flash by the attack button. 60 mL/s. */
	public static VerbSculpting bloodEclipseDome() {
		return new com.vincenthuto.hemomancy.common.manipulation.tenebris.EclipseDomeSculpting();
	}

	/** Glacial Grasp: a hand of ice that forms round a creature, holds it, and closes on the attack button. 40 mL/s. */
	public static VerbSculpting glacialGraspHold() {
		return new com.vincenthuto.hemomancy.common.manipulation.congeatio.GlacialGraspSculpting();
	}

	// The held forms of Blood Needle Fan, Prismatic Reproof, Hemorrhage, Conductive Mark, Insatiable Hunger and Grave
	// Debt were cut on 2026-11-12: each only repeated its own R cast in a shared shape. They cast on R as before.

	// ── The second wave of strands (core-loop round two P5) ──

	/**
	 * Gloam Laceration: the razor, a cat of nine tails. A short, taut rope ending in three tips, gone the moment the
	 * hand stops; every crack and tap lands three quick cuts, harder from behind or in the dark. A
	 * charged throw cuts through its whole arc, the third tap in a row is a small crack that lays Obscured, and other
	 * players barely see it. 40 mL/s.
	 */
	public static VerbSculpting gloamLacerationLash() {
		return Strands.lash(20.0D, 0.85F, new LashProfile(4.5F, 12.0D, 6, 60, 20.0D, 30, 1.25F)
				.with(LashStyle.PLAIN.rope(0.8D, 2.2D, 0.85D, 0.15D).tails(3, 0.4F).pierce(0.6D).ambush(1.75F).tapChain(3).hidden()));
	}

	/** Cruor Javelin: drawn into the hand through three stages and thrown on release. 50 mL/s while drawing. */
	public static VerbSculpting cruorJavelinThrow() {
		return new com.vincenthuto.hemomancy.common.manipulation.congeatio.CruorJavelinThrowSculpting();
	}

	/** Cruor Javelin: Absorption takes a stuck javelin of the caster's own back, for half its blood. */
	public static VerbSculpting cruorJavelinRecall() {
		return new com.vincenthuto.hemomancy.common.manipulation.congeatio.CruorJavelinRecallSculpting();
	}

	/** Blood Needle Lance: needles gather round the caster, loosed together on release or one at a time on the attack button. 35 mL/s. */
	public static VerbSculpting needleLanceVolley() {
		return new com.vincenthuto.hemomancy.common.manipulation.ferric.NeedleVolleySculpting();
	}

	/** Synaptic Jolt: keeps the conductor in the aim live, or jolts the creature in it. 40 mL/s. */
	public static VerbSculpting synapticJoltLive() {
		return new com.vincenthuto.hemomancy.common.manipulation.ductilis.SynapticLiveSculpting();
	}

	/** Crimson Flame Conjuration: paints a line of flame along the aim, flared by the attack button (its kindling claims unlit lights first). 35 mL/s. */
	public static VerbSculpting crimsonFlamePaint() {
		return new com.vincenthuto.hemomancy.common.manipulation.flammeus.FlamePaintSculpting();
	}

	/** Ferric Spikes: a run of blades steered along the ground, erupting into standing blades on the attack button. 50 mL/s. */
	public static VerbSculpting ferricSpikesRun() {
		return new com.vincenthuto.hemomancy.common.manipulation.ferric.FerricRunSculpting();
	}

}
