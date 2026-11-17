package com.vincenthuto.hemomancy.common.manipulation.verb.strand;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * How a strand's visual cue packs into a visual packet's {@code count}, and where the strand leaves the body. Shared by
 * the server, which sends one keyed cue when a strand opens and refreshes it each pulse, and every client, which runs
 * the same rule class from the caster's look to draw its own copy (core-loop Track 12).
 */
public final class StrandCue {
	/** A strand cue lives this long unless refreshed: a little over one pulse. */
	public static final int LIFE = StrandSpec.CADENCE + 6;
	private static final EnumBloodTemper[] SCHOOLS = EnumBloodTemper.values();
	private static final StrandForm[] FORMS = StrandForm.values();
	private static final LashRules.Phase[] PHASES = LashRules.Phase.values();

	/** The accents a strand fires at a server point: each is one burst. */
	public enum Accent { CRACK, PIERCE, DRENCH, ERUPTION, BRAND, TELEGRAPH, TAP, GRASP, JAVELIN, DOME, BEAM, VOLLEY }

	/**
	 * A strand cue as a client reads it. {@code skill} is the caster's skill level the form's motion depends on (Taut Line
	 * for a lash or jet, Deep Brand for a brand, 0 otherwise), so every client runs the caster's rules, not its own;
	 * {@code twin} says the off hand pours a second strand (Second Strand, CL-27). {@code phase} is a lash's phase as
	 * the server has it (a lash's {@code param} is its charge); other forms carry {@code CHARGING} and never read it.
	 */
	public record Decoded(StrandForm form, EnumBloodTemper primary, @Nullable EnumBloodTemper secondary, boolean leftArm,
			double param, int skill, boolean twin, LashRules.Phase phase) {
	}

	public record DecodedAccent(Accent accent, EnumBloodTemper primary, @Nullable EnumBloodTemper secondary) {
	}

	private StrandCue() {
	}

	/** {@link #encode(StrandForm, EnumBloodTemper, EnumBloodTemper, boolean, double, int, boolean)} with no skill or twin. */
	public static int encode(StrandForm form, EnumBloodTemper primary, @Nullable EnumBloodTemper secondary, boolean leftArm,
			double param) {
		return encode(form, primary, secondary, leftArm, param, 0, false);
	}

	/** {@link #encode(StrandForm, EnumBloodTemper, EnumBloodTemper, boolean, double, int, boolean, LashRules.Phase)} for a form with no phase. */
	public static int encode(StrandForm form, EnumBloodTemper primary, @Nullable EnumBloodTemper secondary, boolean leftArm,
			double param, int skill, boolean twin) {
		return encode(form, primary, secondary, leftArm, param, skill, twin, LashRules.Phase.CHARGING);
	}

	/**
	 * Bits 0-2 form, 3-6 primary school, 7-10 secondary + 1 (0 for none), 11 left arm, 12-18 the form's state in
	 * hundredths (jet pressure, plant fill, brand progress, a lash's charge); bit 19 keeps the count positive and
	 * non-zero; bits 20-21 the skill level the form's motion reads (0 to 3); bit 22 the twin; bits 23-24 a lash's phase.
	 */
	public static int encode(StrandForm form, EnumBloodTemper primary, @Nullable EnumBloodTemper secondary, boolean leftArm,
			double param, int skill, boolean twin, LashRules.Phase phase) {
		int hundredths = (int) Math.round(Math.max(0.0D, Math.min(1.0D, param)) * 100.0D);
		return phase.ordinal() << 23 | (twin ? 1 : 0) << 22 | Math.max(0, Math.min(3, skill)) << 20 | 1 << 19 | hundredths << 12 | (leftArm ? 1 : 0) << 11
				| (secondary == null ? 0 : secondary.ordinal() + 1) << 7 | primary.ordinal() << 3 | form.ordinal();
	}

	public static Decoded decode(int count) {
		int secondary = count >> 7 & 15;
		return new Decoded(FORMS[Math.min(count & 7, FORMS.length - 1)], SCHOOLS[Math.min(count >> 3 & 15, SCHOOLS.length - 1)],
				secondary == 0 || secondary > SCHOOLS.length ? null : SCHOOLS[secondary - 1], (count >> 11 & 1) == 1,
				Math.min(100, count >> 12 & 127) / 100.0D, count >> 20 & 3, (count >> 22 & 1) == 1, PHASES[count >> 23 & 3]);
	}

	/** Bits 0-2 and 13 accent (its low three bits, then its fourth), 3-6 primary, 7-10 secondary + 1; bit 12 keeps it positive. */
	public static int encodeAccent(Accent accent, EnumBloodTemper primary, @Nullable EnumBloodTemper secondary) {
		return (accent.ordinal() >> 3 & 1) << 13 | 1 << 12 | (secondary == null ? 0 : secondary.ordinal() + 1) << 7
				| primary.ordinal() << 3 | accent.ordinal() & 7;
	}

	public static DecodedAccent decodeAccent(int count) {
		Accent[] accents = Accent.values();
		int secondary = count >> 7 & 15;
		return new DecodedAccent(accents[Math.min(count & 7 | (count >> 13 & 1) << 3, accents.length - 1)],
				SCHOOLS[Math.min(count >> 3 & 15, SCHOOLS.length - 1)],
				secondary == 0 || secondary > SCHOOLS.length ? null : SCHOOLS[secondary - 1]);
	}

	/**
	 * Where the strand leaves the body: a little in front of and below the eye, to the side of the arm that holds it.
	 * {@code yaw} in degrees, as entities carry it.
	 */
	public static Vec3 hand(Vec3 eye, Vec3 look, float yaw, boolean leftArm) {
		double radians = Math.toRadians(yaw);
		Vec3 right = new Vec3(-Math.cos(radians), 0.0D, -Math.sin(radians));
		return eye.add(look.scale(0.35D)).add(0.0D, -0.4D, 0.0D).add(right.scale(leftArm ? -0.32D : 0.32D));
	}
}
