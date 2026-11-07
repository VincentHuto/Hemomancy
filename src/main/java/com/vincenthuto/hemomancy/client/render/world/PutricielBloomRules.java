package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielSludgeRules;
import com.vincenthuto.hemomancy.common.util.HemoEasing;

import java.util.ArrayList;
import java.util.List;

/**
 * Saint Putriciel's effects as pure numbers (visual pass 2, 2026-11-06): the life of a rot telegraph (the colony
 * spreading over the floor from the reveal, fruiting toward the commit, bursting at it, then drying and fading) and of
 * a fire telegraph (the char eating out, heating to white at the commit, cooling to ash); the spreading front each
 * vertex reads; the bracket rosettes, sacs and roots the rot pushes up and their unfurl, quiver and wither; the clods
 * and spore puffs they throw; the reach of a lashing palm and the wake of a swung censer; the fire's flare. No
 * Minecraft types, so JVM tests cover it; visual numbers never decide hits.
 */
public final class PutricielBloomRules {
	// ── the life of a rot telegraph ──────────────────────────────────

	/** The commit's flash: the colony fruits and heaves for this many ticks. */
	public static final float FLASH = 4.0F;
	/** After the active span the colony dries and fades over this many ticks. */
	public static final float LINGER = 16.0F;
	/** The colony has spread over its whole footprint by this much of the reveal. */
	public static final float SPREAD_LEAD = 0.65F;

	/**
	 * A rot telegraph at one moment: how far the colony has spread ({@code grow}, 0 to 1 of its footprint), how much
	 * rot has pooled into the floor ({@code wet}), how far it has fruited ({@code spore}), the commit's heave, how far
	 * a trench has opened ({@code width}) and the whole thing's strength.
	 */
	public record Look(float grow, float wet, float spore, float heave, float width, float alpha) {
		public static final Look NONE = new Look(0, 0, 0, 0, 0, 0);

		public boolean visible() {
			return alpha > 0.003F;
		}

		/** The same look, its strength scaled. */
		public Look faded(float by) {
			return new Look(grow, wet, spore, heave, width, alpha * by);
		}
	}

	/**
	 * The colony at {@code moveTime} ticks into a move that reveals at {@code revealTick} and commits at the end of its
	 * {@code windup}: it emerges and spreads through the wind-up while rot pools and the spores ripen, fruits and heaves
	 * at the commit, stands through the active span, then dries and fades over {@link #LINGER}.
	 */
	public static Look look(float moveTime, int revealTick, int windup, int active) {
		if (moveTime < revealTick) return Look.NONE;
		if (moveTime < windup) {
			float r = build(moveTime, revealTick, windup);
			float grow = HemoEasing.easeOutCubic(HemoEasing.clamp01(r / SPREAD_LEAD));
			float wet = 0.8F * HemoEasing.ramp(r, 0.25F, 1.0F);
			float spore = 0.15F + 0.45F * r * r;
			float width = 0.3F + 0.7F * HemoEasing.easeOutCubic(r);
			return new Look(grow, wet, spore, 0.0F, width, HemoEasing.ramp(r, 0.0F, 0.15F));
		}
		float since = moveTime - windup;
		if (since < FLASH) {
			float f = 1.0F - since / FLASH;
			return new Look(1.0F, 0.8F + 0.2F * (1.0F - f), 0.6F + 0.4F * f, f, 1.0F + 0.25F * f, 1.0F);
		}
		if (since < active) {
			float settle = HemoEasing.clamp01((since - FLASH) / Math.max(1.0F, active - FLASH));
			return new Look(1.0F, 1.0F, 0.6F - 0.15F * settle, 0.0F, 1.0F, 1.0F);
		}
		float l = (since - Math.max(active, FLASH)) / LINGER;
		if (l >= 1.0F) return Look.NONE;
		return new Look(1.0F, 1.0F - HemoEasing.ramp(l, 0.0F, 0.8F), 0.45F * (1.0F - l), 0.0F,
				1.0F - 0.4F * HemoEasing.ramp(l, 0.3F, 1.0F), 1.0F - HemoEasing.ramp(l, 0.55F, 1.0F));
	}

	/** 0 at the reveal to 1 at the commit. */
	public static float build(float moveTime, int revealTick, int windup) {
		return windup <= revealTick ? 1.0F : HemoEasing.clamp01((moveTime - revealTick) / (float) (windup - revealTick));
	}

	/** The last tick a rot telegraph shows. */
	public static float lastTick(int windup, int active) {
		return windup + Math.max(active, FLASH) + LINGER;
	}

	// ── the life of a fire telegraph ─────────────────────────────────

	/** The fire's linger: the embers cool to ash over this many ticks after the active span. */
	public static final float COOL = 18.0F;
	/** The char has eaten its whole footprint by this much of the reveal. */
	public static final float CHAR_LEAD = 0.7F;
	/** Heat at and over this reads white-hot. */
	public static final float WHITE_HOT = 0.9F;

	/** A fire telegraph at one moment: how far the char has eaten, its heat, its ash, a furrow's opening, strength. */
	public record Burn(float grow, float heat, float ash, float width, float alpha) {
		public static final Burn NONE = new Burn(0, 0, 0, 0, 0);

		public boolean visible() {
			return alpha > 0.003F;
		}

		public Burn faded(float by) {
			return new Burn(grow, heat, ash, width, alpha * by);
		}
	}

	/**
	 * The scorch at {@code moveTime}: the char eats out and the heat climbs through the wind-up, runs white-hot over
	 * the commit's flash, burns through the active span, then cools to ash over {@link #COOL}.
	 */
	public static Burn burn(float moveTime, int revealTick, int windup, int active) {
		if (moveTime < revealTick) return Burn.NONE;
		if (moveTime < windup) {
			float r = build(moveTime, revealTick, windup);
			return new Burn(HemoEasing.easeOutCubic(HemoEasing.clamp01(r / CHAR_LEAD)), 0.15F + 0.6F * r * r, 0.2F * r,
					0.4F + 0.6F * HemoEasing.easeOutCubic(r), HemoEasing.ramp(r, 0.0F, 0.12F));
		}
		float since = moveTime - windup;
		if (since < FLASH) {
			float f = 1.0F - since / FLASH;
			return new Burn(1.0F, 0.88F + 0.12F * f, 0.25F, 1.0F + 0.2F * f, 1.0F);
		}
		if (since < active) {
			float settle = HemoEasing.clamp01((since - FLASH) / Math.max(1.0F, active - FLASH));
			return new Burn(1.0F, 0.88F - 0.2F * settle, 0.3F, 1.0F, 1.0F);
		}
		float l = (since - Math.max(active, FLASH)) / COOL;
		if (l >= 1.0F) return Burn.NONE;
		float cool = 1.0F - l;
		return new Burn(1.0F, 0.62F * cool * cool, 0.3F + 0.6F * l, 1.0F, 1.0F - HemoEasing.ramp(l, 0.6F, 1.0F));
	}

	// ── the spreading front ──────────────────────────────────────────

	/**
	 * How far the colony (or the char) has taken a point {@code distance} blocks from where it started, when its front
	 * stands at {@code reach}: 1 well behind the front, 0.5 at it, 0 by {@code feather} ahead of it. The shaders turn
	 * this into blotches that take the floor unevenly, so the front reads ragged.
	 */
	public static float front(double distance, double reach, double feather) {
		if (reach <= 0.0D) return 0.0F;
		double f = Math.max(1.0E-3D, feather);
		return (float) HemoEasing.clamp01((reach + f - distance) / (2.0D * f));
	}

	// ── growths ──────────────────────────────────────────────────────

	/** A cap unfurls past full size over UNFURL ticks and settles back over SETTLE. */
	public static final float UNFURL = 4.0F, SETTLE = 4.0F, OVERSHOOT = 1.12F;
	/** A withering growth droops, shrinks and crumbles over this many ticks. */
	public static final float WITHER = 12.0F;
	/** A standing cap quivers this many radians after it unfurls, dying away over QUIVER_DECAY ticks. */
	public static final double QUIVER = 0.09D;
	public static final float QUIVER_DECAY = 8.0F;

	/**
	 * One bracket cap of a rosette: its root offset from the rosette's foot, its lift off the floor, radius, the way
	 * it faces (radians), how far it tilts up (radians), its fan (radians), its thickness, its tier and how many ticks
	 * after the rosette's first cap it unfurls.
	 */
	public record Cap(double dx, double dz, double lift, double radius, double yaw, double tilt, double span,
			double thickness, int tier, float delay) {
	}

	/**
	 * A rosette of {@code count} bracket caps about {@code scale} blocks across: they fan out from a knot in up to
	 * three tiers, the lowest widest, each tier unfurling a beat after the one under it, no two alike.
	 */
	public static List<Cap> rosette(int seed, double scale, int count) {
		List<Cap> out = new ArrayList<>(count);
		int tiers = count >= 5 ? 3 : count >= 3 ? 2 : 1;
		for (int i = 0; i < count; i++) {
			double u = unit(seed, 100 + i), v = unit(seed, 200 + i), w = unit(seed, 300 + i);
			int tier = i * tiers / Math.max(1, count);
			double yaw = i * 2.39996D + u * 0.7D + seed * 0.13D;
			double radius = scale * (0.42D + 0.22D * v) * (1.0D - 0.18D * tier);
			double reach = scale * (0.06D + 0.08D * w);
			out.add(new Cap(Math.cos(yaw) * reach, Math.sin(yaw) * reach, 0.03D + tier * scale * 0.2D + w * scale * 0.05D, radius,
					yaw, 0.22D + 0.22D * u + tier * 0.08D, 1.9D + 1.0D * v, radius * (0.2D + 0.08D * w), tier,
					tier * 1.2F + (float) (u * 0.8D)));
		}
		return out;
	}

	/** The size of a cap {@code age} ticks after it began to unfurl: a soft swell past full size, then settling. */
	public static float unfurl(float age) {
		if (age <= 0.0F) return 0.0F;
		if (age < UNFURL) return OVERSHOOT * HemoEasing.easeOutCubic(age / UNFURL);
		return HemoEasing.lerp(OVERSHOOT, 1.0F, HemoEasing.ramp(age, UNFURL, UNFURL + SETTLE));
	}

	/** A cap's size, unfurled at 0 and withering from {@code witherAt}: it shrinks by half as it goes. */
	public static float size(float age, float witherAt) {
		return unfurl(age) * (1.0F - 0.5F * withered(age, witherAt));
	}

	/** How far a growth has withered, 0 to 1, from {@code witherAt}. */
	public static float withered(float age, float witherAt) {
		return HemoEasing.ramp(age, witherAt, witherAt + WITHER);
	}

	/** The material's formation: a cap grows out from its root as it unfurls and crumbles back from its rim as it withers. */
	public static float formation(float age, float witherAt) {
		if (age <= 0.0F) return 0.0F;
		float up = HemoEasing.lerp(0.3F, 1.0F, HemoEasing.ramp(age, 0.0F, UNFURL * 0.9F));
		return up * (1.0F - HemoEasing.ramp(age, witherAt + WITHER * 0.3F, witherAt + WITHER));
	}

	/** How far a standing cap quivers, radians: nothing while it unfurls, then dying away. */
	public static double quiver(float age, double time, int seed) {
		if (age < UNFURL) return 0.0D;
		return QUIVER * Math.exp(-(age - UNFURL) / QUIVER_DECAY) * Math.sin(time * 1.9D + seed * 1.7D);
	}

	/** A sac swells over this many ticks, bursts in BURST and shrivels over SHRIVEL. */
	public static final float SWELL = 10.0F, BURST = 3.0F, SHRIVEL = 10.0F;

	/** A sac's swelling {@code age} ticks after it began, breathing a little once full. */
	public static float swell(float age, double time, int seed) {
		if (age <= 0.0F) return 0.0F;
		float s = HemoEasing.smoothstep(HemoEasing.clamp01(age / SWELL));
		return s * (float) (1.0D + 0.05D * Math.sin(time * 0.4D + seed));
	}

	/** A burst sac's formation: whole until it bursts, torn open from the top over BURST, then shrivelling. */
	public static float tear(float sinceBurst) {
		if (sinceBurst <= 0.0F) return 1.0F;
		if (sinceBurst < BURST) return HemoEasing.lerp(1.0F, 0.45F, sinceBurst / BURST);
		return 0.45F * (1.0F - HemoEasing.ramp(sinceBurst, BURST, BURST + SHRIVEL));
	}

	/** A root breaching the floor shows this much of its length {@code age} ticks after it broke out. */
	public static final float BREACH = 3.0F;

	public static float breach(float age) {
		if (age <= 0.0F) return 0.0F;
		return HemoEasing.easeOutCubic(HemoEasing.clamp01(age / BREACH));
	}

	// ── debris ───────────────────────────────────────────────────────

	/** Clods thrown up by a breach fly and settle over this many ticks. */
	public static final float CLOD_LIFE = 14.0F;
	public static final double CLOD_GRAVITY = 0.075D;

	/**
	 * Clod {@code i} of a burst, {@code age} ticks after it: {x, y, z} from the burst point, its tumble and its fade (1
	 * to 0), or null once it has settled and gone. {@code reach} scales how far clods fly.
	 */
	public static double[] clod(int seed, int i, float age, double reach) {
		if (age < 0.0F || age >= CLOD_LIFE) return null;
		double a = unit(seed, 400 + i) * Math.PI * 2.0D;
		double speed = reach * (0.04D + 0.06D * unit(seed, 420 + i));
		double up = 0.2D + 0.18D * unit(seed, 440 + i);
		double flight = 2.0D * up / CLOD_GRAVITY;
		double t = Math.min(age, flight);
		double y = Math.max(0.0D, up * t - CLOD_GRAVITY * t * t * 0.5D);
		double out = speed * (t + Math.max(0.0D, age - flight) * 0.08D);
		double fade = 1.0D - HemoEasing.ramp(age, CLOD_LIFE * 0.6F, CLOD_LIFE);
		return new double[]{Math.cos(a) * out, y, Math.sin(a) * out, age * (0.5D + unit(seed, 460 + i)), fade};
	}

	/** A puff of spores thrown up by a burst rolls and spreads over this many ticks. */
	public static final float PUFF_LIFE = 18.0F;

	/**
	 * Puff {@code i} of a burst, {@code age} ticks after it: {x, y, z} from the burst point, its size and how far it has
	 * thinned (0 dense to 1 gone), or null once it has gone. {@code reach} scales the burst.
	 */
	public static double[] puff(int seed, int i, float age, double reach) {
		if (age < 0.0F || age >= PUFF_LIFE) return null;
		double t = age / PUFF_LIFE;
		double a = unit(seed, 500 + i) * Math.PI * 2.0D;
		double roll = HemoEasing.easeOutCubic(Math.min(1.0D, t * 1.6D));
		double out = reach * (0.25D + 0.6D * unit(seed, 520 + i)) * roll;
		double y = reach * (0.15D + 0.45D * unit(seed, 540 + i)) * roll + 0.25D * t;
		double size = reach * (0.22D + 0.3D * roll) * (0.75D + 0.5D * unit(seed, 560 + i));
		return new double[]{Math.cos(a) * out, y, Math.sin(a) * out, size, HemoEasing.ramp((float) t, 0.35F, 1.0F)};
	}

	// ── reaches, wakes and throws ────────────────────────────────────

	/** A palm's reach lashes out over REACH_OUT ticks from the commit and snaps back over REACH_BACK after the active span. */
	public static final float REACH_OUT = 3.0F, REACH_BACK = 4.0F;

	/** How far the reach's head has run along its lane, 0 to 1, {@code age} ticks after the commit; -1 when not drawn. */
	public static float reachHead(float age, int active) {
		if (age < 0.0F) return -1.0F;
		if (age < REACH_OUT) return HemoEasing.easeOutCubic(age / REACH_OUT);
		if (age < active) return 1.0F;
		float back = (age - active) / REACH_BACK;
		if (back >= 1.0F) return -1.0F;
		return 1.0F - HemoEasing.easeInCubic(back);
	}

	/** A censer's wake starts this many ticks before the commit and frays over WAKE_LINGER after the active span. */
	public static final float WAKE_LEAD = 3.0F, WAKE_LINGER = 9.0F;

	/** The wake's strength {@code age} ticks after the commit of a move active for {@code active}; 0 when not drawn. */
	public static float wake(float age, int active) {
		if (age < -WAKE_LEAD) return 0.0F;
		if (age < 0.0F) return HemoEasing.smoothstep(1.0F + age / WAKE_LEAD);
		if (age < active) return 1.0F;
		return 1.0F - HemoEasing.clamp01((age - active) / WAKE_LINGER);
	}

	/** How far the wake has frayed: 0 while the censer swings, 1 as it goes, spreading and lifting as it thins. */
	public static float fray(float age, int active) {
		return age < active ? 0.0F : HemoEasing.clamp01((age - active) / WAKE_LINGER);
	}

	/** A thrown shelf flies this many ticks before it lands. */
	public static final float THROW = 10.0F;

	/** The height of a throw's arc at {@code t} of its flight, peaking at {@code lift}. */
	public static double arc(double t, double lift) {
		double c = HemoEasing.clamp01(t);
		return 4.0D * lift * c * (1.0D - c);
	}

	// ── fire ─────────────────────────────────────────────────────────

	/** A tongue erupts past its height over FLARE ticks and settles over another FLARE. */
	public static final float FLARE = 2.0F, FLARE_OVERSHOOT = 1.3F;

	/** A tongue's height factor {@code age} ticks after it erupted: a leap, then burning steadily. */
	public static float flare(float age) {
		if (age <= 0.0F) return 0.0F;
		if (age < FLARE) return FLARE_OVERSHOOT * HemoEasing.easeOutCubic(age / FLARE);
		return HemoEasing.lerp(FLARE_OVERSHOOT, 1.0F, HemoEasing.ramp(age, FLARE, FLARE * 2.0F));
	}

	/** The flicker on tongue {@code i}: a slow lick and a quicker shiver, about 1. */
	public static double flicker(double time, int seed, int i) {
		return 1.0D + 0.14D * Math.sin(time * 0.55D + i * 1.9D + seed) + 0.06D * Math.sin(time * 1.7D + i * 0.7D);
	}

	/** A slam's shock rolls out over this many ticks. */
	public static final float SHOCK = 8.0F;

	/** The shock's radius {@code age} ticks after the slam, reaching {@code reach}; negative when not drawn. */
	public static double shockRadius(float age, double reach) {
		if (age < 0.0F || age > SHOCK) return -1.0D;
		return 0.3D + (reach - 0.3D) * HemoEasing.easeOutCubic(HemoEasing.clamp01(age / (SHOCK * 0.8F)));
	}

	// ── rot sludge (revision 2026-11-06, R2.4 and R2.5) ──────────────

	/** The pool's beats, from the server's rules so both sides agree on the burn. */
	public static final float SLUDGE_FORM = (float) PutricielSludgeRules.FORM, SLUDGE_SETTLE = (float) PutricielSludgeRules.SETTLE;
	public static final float SLUDGE_CATCH = (float) PutricielSludgeRules.CATCH, SLUDGE_BOIL_END = (float) PutricielSludgeRules.BOIL_END;
	public static final float SLUDGE_ASH_AT = (float) PutricielSludgeRules.ASH_AT, SLUDGE_BURN = (float) PutricielSludgeRules.BURN;
	public static final float SLUDGE_COOL = (float) PutricielSludgeRules.COOL, SLUDGE_ENDING_BURN = (float) PutricielSludgeRules.ENDING_BURN;
	public static final double RUNNER_SPEED = (double) PutricielSludgeRules.RUNNER_SPEED;
	/** A pool is this thick at its centre (3 px), with a meniscus lip this high just inside its rim. */
	public static final double SLUDGE_DEPTH = 0.18D, SLUDGE_LIP = 0.06D;
	/** How thin the body runs just inside the lip. */
	private static final double SHALLOW = 0.035D;
	/** The lobed rim varies between these fractions of the radius. */
	public static final double LOBE_MIN = 0.85D, LOBE_MAX = 1.1D;
	/** Its thickness overshoots to this as it forms. */
	public static final float SLUDGE_OVERSHOOT = 1.15F;
	/** Settling thins it to this fraction of itself; the boil thins it from 0.18 to 0.08. */
	public static final float SETTLED_THIN = 0.2F, BOILED_THIN = 0.08F / 0.18F;
	/** The burning front crosses the surface at a sixth of the radius a tick. */
	public static final double FRONT_PER_RADIUS = 1.0D / 6.0D;
	/** The boil chars the sludge to this before the crust closes. */
	public static final float BOIL_CHAR = 0.7F;
	/** A rot pool's bubble: one slot rises every 25 to 40 ticks, swelling over {@link #BUBBLE_SWELL} and bursting. */
	public static final float BUBBLE_SWELL = 9.0F, RIPPLE = 8.0F;
	/** In the boil a bubble bursts in each of {@link #BOIL_SLOTS} slots every {@link #BOIL_PERIOD} ticks: two a tick. */
	public static final int BOIL_SLOTS = 8;
	public static final float BOIL_PERIOD = 4.0F;

	/**
	 * A pool's look at one moment: how far it has spread out to its rim (0 to 1), its thickness against the full 0.18,
	 * its gloss, the char behind the burning front (0 living, 0.7 charred, 1 cold crust), how far the rim's lobes have
	 * curled up, how far it has sunk (blocks), the wet stain round it and the cream scum on it. The vertex colour is
	 * this state, never a tint.
	 */
	public record Sludge(float spread, float thick, float gloss, float charred, float curl, float sink, float stain, float scum) {
		public static final Sludge NONE = new Sludge(0, 0, 0, 0, 0, 0, 0, 0);

		public boolean visible() {
			return spread > 0.005F && thick > 0.005F;
		}
	}

	/** A pool in the rot, {@code age} ticks after it was laid: it spreads to its rim over 8 ticks, its body overshooting. */
	public static Sludge sludge(float age) {
		if (age < 0.0F) return Sludge.NONE;
		float spread = HemoEasing.easeOutCubic(HemoEasing.clamp01(age / SLUDGE_FORM));
		float rise = HemoEasing.easeOutCubic(HemoEasing.clamp01(age / (SLUDGE_FORM * 0.7F)));
		float thick = HemoEasing.lerp(0.3F, SLUDGE_OVERSHOOT, rise)
				- (SLUDGE_OVERSHOOT - 1.0F) * HemoEasing.ramp(age, SLUDGE_FORM * 0.7F, SLUDGE_FORM * 1.6F);
		return new Sludge(spread, thick, 1.0F, 0.0F, 0.0F, 0.0F, spread, 0.65F);
	}

	/**
	 * A pool settling into the moss (the cap or its safety life; never fire), {@code since} ticks into a settle of
	 * {@code ticks}: the gloss dies, it dulls to a matte crust, thins to a fifth and sinks, the stain fading with it.
	 */
	public static Sludge settling(float since, float ticks) {
		float s = HemoEasing.clamp01(since / Math.max(1.0F, ticks));
		if (s >= 1.0F) return Sludge.NONE;
		float gloss = 1.0F - HemoEasing.smoothstep(Math.min(1.0F, s * 1.6F));
		return new Sludge(1.0F, HemoEasing.lerp(1.0F, SETTLED_THIN, s), gloss, 0.0F, 0.0F, 0.025F * s, 1.0F - s, 0.65F * (1.0F - s));
	}

	/**
	 * A burning pool {@code b} ticks after it caught, everywhere the front has passed: it boils and thins, its lobes
	 * curl, the char closes to a crust over 30 to 44 and it crumbles and sinks onto the ash by 50. The char of one point
	 * also needs {@link #sludgeChar}, which reads how long ago the front passed it.
	 */
	public static Sludge sludgeBurn(float b) {
		if (b < 0.0F) return sludge(100.0F);
		if (b >= SLUDGE_BURN) return Sludge.NONE;
		float thin = HemoEasing.lerp(1.0F, BOILED_THIN, HemoEasing.ramp(b, SLUDGE_CATCH, SLUDGE_BOIL_END));
		float crumble = HemoEasing.ramp(b, SLUDGE_ASH_AT, SLUDGE_BURN);
		float heave = boil(b);
		float curl = HemoEasing.ramp(b, SLUDGE_CATCH + 4.0F, SLUDGE_BOIL_END);
		return new Sludge(1.0F, thin * (1.0F - crumble) * (1.0F + 0.12F * heave), 1.0F - HemoEasing.ramp(b, 0.0F, SLUDGE_CATCH * 2.0F),
				crust(b), curl, 0.03F * crumble, 1.0F - HemoEasing.ramp(b, SLUDGE_BOIL_END, SLUDGE_ASH_AT), 0.65F * (1.0F - HemoEasing.ramp(b, 0.0F, SLUDGE_CATCH)));
	}

	/** The crust over the whole pool once the boil is done: 0.7 at 30, closing to 1 (cold) by the ash at 44. */
	public static float crust(float b) {
		if (b < SLUDGE_BOIL_END) return 0.0F;
		return BOIL_CHAR + (1.0F - BOIL_CHAR) * HemoEasing.ramp(b, SLUDGE_BOIL_END, SLUDGE_ASH_AT);
	}

	/** The char of a point the burning front passed {@code since} ticks ago, {@code b} ticks into the burn. */
	public static float sludgeChar(float since, float b) {
		if (since < 0.0F) return 0.0F;
		return Math.max(BOIL_CHAR * HemoEasing.ramp(since, 0.0F, SLUDGE_BOIL_END - SLUDGE_CATCH), crust(b));
	}

	/** How far the burning front has run across the pool from the catch point, blocks. */
	public static double catchFront(float b, double radius) {
		return b <= 0.0F ? 0.0D : radius * FRONT_PER_RADIUS * b;
	}

	/** The boil's heave: from the catch's end to the boil's, eased in and out. */
	public static float boil(float b) {
		return HemoEasing.window(b, SLUDGE_CATCH, SLUDGE_CATCH + 3.0F, SLUDGE_BOIL_END - 4.0F, SLUDGE_BOIL_END);
	}

	/**
	 * The fire over a burning pool: the low tongues rising along the front through the catch (0.3 to 0.5), the tall
	 * ones over the whole pool through the boil (0.6 to 1.1) dying back to embers through the char (to 0.2) and out at
	 * the crumble. A factor on 1.1 blocks.
	 */
	public static float sludgeFire(float b) {
		if (b < 0.0F || b >= SLUDGE_BURN) return 0.0F;
		float rise = HemoEasing.lerp(0.45F, 1.0F, HemoEasing.ramp(b, SLUDGE_CATCH - 2.0F, SLUDGE_CATCH + 4.0F));
		float die = HemoEasing.lerp(1.0F, 0.2F / 1.1F, HemoEasing.ramp(b, SLUDGE_BOIL_END, SLUDGE_ASH_AT));
		return rise * die * (1.0F - HemoEasing.ramp(b, SLUDGE_ASH_AT, SLUDGE_ASH_AT + 4.0F));
	}

	/** The scorch on the ash after the crumble: the seams' heat going dark (0.6 to 0) and the plates fading by 110. */
	public static float coolingHeat(float b) {
		if (b < SLUDGE_ASH_AT) return 0.85F;
		return 0.6F * (1.0F - HemoEasing.ramp(b, SLUDGE_BURN, SLUDGE_BURN + SLUDGE_COOL * 0.5F));
	}

	public static float coolingAlpha(float b) {
		return 1.0F - HemoEasing.ramp(b, SLUDGE_BURN + SLUDGE_COOL * 0.7F, SLUDGE_BURN + SLUDGE_COOL);
	}

	/**
	 * The pool's section: its height over the floor at radial place {@code rho} (0 at the centre, 1 at the rim), at full
	 * thickness. 0.18 at the centre falling to a meniscus lip 0.06 high just inside the rim, then a rounded edge to the
	 * floor.
	 */
	public static double sludgeProfile(double rho) {
		double r = Math.abs(rho);
		if (r >= 1.0D) return 0.0D;
		// the body thins from the centre to a shallow just inside the rim, where the meniscus stands up as a lip
		double inner = Math.min(1.0D, r / 0.84D);
		double base = SHALLOW + (SLUDGE_DEPTH - SHALLOW) * (1.0D - inner * inner);
		double ridge = SLUDGE_LIP * Math.exp(-Math.pow((r - 0.88D) / 0.04D, 2.0D));
		double edge = r < 0.9D ? 1.0D : Math.sqrt(Math.max(0.0D, 1.0D - Math.pow((r - 0.9D) / 0.1D, 2.0D)));
		return Math.max(base, ridge) * edge;
	}

	/** The lobed rim at bearing {@code angle} (radians): five to seven seeded lobes, 0.85 to 1.1 of the radius. */
	public static double lobe(int seed, double angle) {
		int lobes = 5 + (int) Math.floor(unit(seed, 900) * 3.0D);
		double phase = unit(seed, 901) * Math.PI * 2.0D;
		double wave = 0.65D * Math.sin(lobes * angle + phase) + 0.35D * Math.sin((lobes + 2) * angle + phase * 1.7D);
		double mid = (LOBE_MIN + LOBE_MAX) / 2.0D, half = (LOBE_MAX - LOBE_MIN) / 2.0D;
		return mid + half * Math.max(-1.0D, Math.min(1.0D, wave));
	}

	/**
	 * A rot pool's bubble in slot {@code slot}, {@code age} ticks into the pool's life: {x, z} its place across the pool
	 * (-1 to 1 of the radius), how far it has swelled (0 to 1), and ticks since it burst (negative before), or null
	 * between bubbles. One slot rises every 25 to 40 ticks.
	 */
	public static double[] bubble(int seed, int slot, float age) {
		double period = 25.0D + 15.0D * unit(seed, 910 + slot);
		double shifted = age + period * unit(seed, 920 + slot);
		int cycle = (int) Math.floor(shifted / period);
		double t = shifted - cycle * period;
		if (t > BUBBLE_SWELL + RIPPLE + 4.0D) return null;
		int key = seed * 31 + cycle * 7 + slot;
		double a = unit(key, 1) * Math.PI * 2.0D, d = 0.55D * Math.sqrt(unit(key, 2));
		double swell = t < BUBBLE_SWELL ? HemoEasing.smoothstep(t / BUBBLE_SWELL) : 1.0D;
		return new double[]{Math.cos(a) * d, Math.sin(a) * d, swell, t - BUBBLE_SWELL};
	}

	/** The boil's bubble in slot {@code slot}, {@code b} ticks into the burn: as {@link #bubble}, every four ticks. */
	public static double[] boilBubble(int seed, int slot, float b) {
		if (boil(b) <= 0.0F) return null;
		double shifted = b + BOIL_PERIOD * slot / (double) BOIL_SLOTS;
		int cycle = (int) Math.floor(shifted / BOIL_PERIOD);
		double t = shifted - cycle * BOIL_PERIOD;
		int key = seed * 17 + cycle * 13 + slot;
		double a = unit(key, 3) * Math.PI * 2.0D, d = 0.8D * Math.sqrt(unit(key, 4));
		double swell = HemoEasing.smoothstep(Math.min(1.0D, t / (BOIL_PERIOD - 1.0D)));
		return new double[]{Math.cos(a) * d, Math.sin(a) * d, swell, t - (BOIL_PERIOD - 1.0D), key};
	}

	/** How far a fire runner's head has crawled along its lane of {@code length}, {@code age} ticks after it left. */
	public static double runnerHead(float age, double length) {
		return age <= 0.0F ? 0.0D : Math.min(length, RUNNER_SPEED * age);
	}

	/** A runner's fire: burning at its head while it crawls, dying over 10 ticks once it arrives. */
	public static float runnerFire(float age, double length) {
		if (age < 0.0F) return 0.0F;
		float arrive = (float) (length / Math.max(1.0E-3D, RUNNER_SPEED));
		return HemoEasing.ramp(age, 0.0F, 2.0F) * (1.0F - HemoEasing.ramp(age, arrive, arrive + 10.0F));
	}

	// ── the body's shed (revision 2026-11-06, R1.5) ──────────────────

	/** A burning fragment smoulders on the floor this many ticks after it comes to rest. */
	public static final float SMOULDER = 20.0F;
	public static final double FRAGMENT_GRAVITY = 0.045D, BOUNCE = 0.35D;

	/**
	 * Burning fragment {@code i} broken off the back {@code drop} blocks over the floor, {@code age} ticks after it
	 * broke: {x, y, z} from where it broke (y down to -drop at the floor), its tumble and ticks since it came to rest
	 * (negative while it flies), or null once it has smouldered out. It falls, bounces once and lies.
	 */
	public static double[] fragment(int seed, int i, float age, double drop) {
		if (age < 0.0F) return null;
		double a = unit(seed, 960 + i) * Math.PI * 2.0D;
		double speed = 0.06D + 0.06D * unit(seed, 961 + i);
		double up = 0.08D + 0.08D * unit(seed, 962 + i);
		double g = FRAGMENT_GRAVITY;
		// first flight: up and out, down to the floor at -drop
		double first = (up + Math.sqrt(up * up + 2.0D * g * Math.max(0.0D, drop))) / g;
		double second = 2.0D * (Math.sqrt(up * up + 2.0D * g * Math.max(0.0D, drop)) * BOUNCE) / g;
		double rest = first + second;
		if (age > rest + SMOULDER) return null;
		double y, out;
		if (age < first) {
			y = up * age - 0.5D * g * age * age;
			out = speed * age;
		} else if (age < rest) {
			double t = age - first, vy = Math.sqrt(up * up + 2.0D * g * Math.max(0.0D, drop)) * BOUNCE;
			y = -drop + vy * t - 0.5D * g * t * t;
			out = speed * first + speed * 0.5D * t;
		} else {
			y = -drop;
			out = speed * first + speed * 0.5D * second;
		}
		double tumble = Math.min(age, rest) * (0.4D + 0.5D * unit(seed, 963 + i));
		return new double[]{Math.cos(a) * out, Math.max(-drop, y), Math.sin(a) * out, tumble, age >= rest ? age - rest : -1.0D};
	}

	/** An ash flake drifts down and out over this many ticks. */
	public static final float FLAKE_LIFE = 30.0F;

	/**
	 * Ash flake {@code i} of a stream shed at {@code rate} a second, {@code age} ticks into the fire: the tick it was
	 * shed and how far through its life it is (0 to 1), or null when this slot holds none now.
	 */
	public static double[] flake(int seed, int i, float age, float rate) {
		if (rate <= 0.0F) return null;
		double every = 20.0D / rate;
		double born = (Math.floor(age / every) - i) * every;
		double life = (age - born) / FLAKE_LIFE;
		if (born < 0.0D || life < 0.0D || life >= 1.0D) return null;
		return new double[]{born, life};
	}

	// ── seeds ────────────────────────────────────────────────────────

	/** A stable value in [0, 1) for seed and index. */
	public static double unit(int seed, int index) {
		int h = seed * 0x7FEB352D ^ index * 0x846CA68B;
		h ^= h >>> 16;
		h *= 0x7FEB352D;
		h ^= h >>> 15;
		h *= 0x846CA68B;
		h ^= h >>> 16;
		return (h >>> 8) / (double) (1 << 24);
	}

	private PutricielBloomRules() {
	}
}
