package com.vincenthuto.hemomancy.common.rite.sigil;

/**
 * The timing and per-style character of an awakened sigil's living body, kept free of Minecraft types so it can be
 * tested on its own. The body is drawn by {@code client.render.world.IchorianSigilFormGeometry}: living-blood flesh
 * (a translucent shell, twisting sinew, glossy knots, sheet membranes) with the sigil's own ichor running through it.
 */
public final class IchorianSigilFormRules {
	/** Ichor boluses ride a vessel from end to end once every this many ticks at a flourish's speed of 1. */
	public static final float BOLUS_PERIOD_TICKS = 46.0F;

	private IchorianSigilFormRules() {
	}

	/** 0..1, sharp: the body's shared heartbeat, about one beat a second, shaped as Riposte's sinew pulses. */
	public static float heartbeat(float time) {
		float wave = (float) Math.max(0.0D, Math.sin(time * 0.3D));
		return wave * wave * wave * wave;
	}

	/** 0.55..1, so repeated parts of the body never pulse in lockstep. */
	public static float flicker(float time, int index, long seed) {
		double phase = index * 1.7D + (seed & 1023L) * 0.013D;
		return (float) (0.775D + 0.225D * Math.sin(time * 0.09D + phase));
	}

	/** 0..1 down a vessel: where its bolus number {@code bolus} of {@code count} is now. */
	public static float bolusPosition(float time, int vessel, int bolus, int count, float speed) {
		double travel = time * speed / BOLUS_PERIOD_TICKS + vessel * 0.37D + bolus / (double) Math.max(1, count);
		return (float) (travel - Math.floor(travel));
	}

	/** 0..1: a bolus swells where it travels, fading in and out at the vessel's ends. */
	public static float bolusPresence(float position) {
		return (float) Math.sin(Math.PI * Math.max(0.0F, Math.min(1.0F, position)));
	}

	/**
	 * How far a membrane is pushed out of its own plane at its middle, as a share of its size: a slow breath, and for
	 * a contractile body a hard pull on the heartbeat. A shutter body closes its membranes in toward their middles
	 * instead (see {@link #shutter}).
	 */
	public static float membraneBillow(Flourish flourish, float time, int membrane) {
		float breath = (float) (0.06D + 0.04D * Math.sin(time * 0.05D + membrane * 1.3D));
		return flourish.membraneBeat() ? breath - 0.09F * heartbeat(time) : breath;
	}

	/** 0..1: how far a shutter body's membranes have drawn closed; 0 for any other body. */
	public static float shutter(Flourish flourish, float time) {
		if (!flourish.shutter()) return 0.0F;
		double cycle = Math.sin(time * 0.022D);
		return (float) Math.max(0.0D, cycle) * 0.55F;
	}

	/** Where a dripping body's droplet number {@code drop} is in its fall, 0..1, or -1 while it gathers. */
	public static float drip(float time, int drop) {
		double cycle = time / 70.0D + drop / 3.0D;
		double phase = cycle - Math.floor(cycle);
		return phase < 0.45D ? -1.0F : (float) ((phase - 0.45D) / 0.55D);
	}

	public static Flourish flourish(IchorianSigilAnatomy.Style style) {
		return switch (style) {
			case PENDULOUS_AMPULLA -> Flourish.DRIP;
			case NEEDLE_THREAD -> Flourish.THREAD;
			case CONTRACTILE_SHIELD -> Flourish.CONTRACT;
			case ARTERIAL_FORK -> Flourish.RACE;
			case RECALL_RIBBON -> Flourish.ECHO;
			case FIVE_LIPPED_SHUTTER -> Flourish.SHUTTER;
			case WALKING_RIB_TOWER -> Flourish.CAGE;
			case VASCULAR_ARBOR -> Flourish.ARBOR;
			case OPTIC_STALK_VEIL -> Flourish.GAZE;
		};
	}

	/** Each body style's one signature over the shared flesh. */
	public enum Flourish {
		/** The Reservoir's swollen organ drips ichor from its lowest point. */
		DRIP(1, 0.7F),
		/** The Suture's hook trails a long thread of ichor behind it. */
		THREAD(1, 1.0F),
		/** The Bastion's membranes pull taut on every heartbeat. */
		CONTRACT(1, 0.9F),
		/** The Shunt's ichor races its forks, three boluses to a vessel. */
		RACE(3, 2.4F),
		/** The Mnemonic drags a fading echo of where its spine just was. */
		ECHO(1, 0.8F),
		/** The Seal's five lips draw closed and open again. */
		SHUTTER(1, 0.5F),
		/** The Cage's ribs are doubled into bars. */
		CAGE(1, 0.8F),
		/** The Lattice's vessels put out fine ichor twigs. */
		ARBOR(2, 1.1F),
		/** The Lens's eyes are ringed with a second, wider iris. */
		GAZE(1, 0.8F);

		private final int boluses;
		private final float bolusSpeed;

		Flourish(int boluses, float bolusSpeed) {
			this.boluses = boluses;
			this.bolusSpeed = bolusSpeed;
		}

		public int boluses() {
			return boluses;
		}

		public float bolusSpeed() {
			return bolusSpeed;
		}

		public boolean membraneBeat() {
			return this == CONTRACT;
		}

		public boolean shutter() {
			return this == SHUTTER;
		}
	}
}
