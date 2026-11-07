package com.vincenthuto.hemomancy.client.render.world;

import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraGeometry.*;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.render.world.SeraphaeOvergrowthRules.Kind;
import com.vincenthuto.hemomancy.client.render.world.SeraphaeOvergrowthRules.Seam;
import com.vincenthuto.hemomancy.client.render.world.SeraphaeTissueGeometry.Ground;
import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint.Shapes;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

/**
 * Saint Seraphae's light with body (visual pass 2), on the Riposte and outpouring recipe: white light is the Lux
 * material (with the hot core and the raw clot as its two accents and a pale nerve sheath as its body); gold is a nerve
 * firing and is only ever the Ductilis material, on the {@code gold} consumer. Streams of light that swell with
 * travelling knots inside a sheath; lashes and reaches laid along the animated limb's own trail; standing sheets of
 * light that roll out from a strike at the height a move hits; bursts of rays with body out of a struck point; and the
 * gold nerve fibres that fire across her floor. Points are relative to the pose; {@code camera} is the eye in the same
 * space.
 */
final class SeraphaeLightGeometry {
	static final int TINT = 0xFFFFFF, CORE = 0xFFD2C4, CLOT = 0x7A1020;
	/** Her nerve sheath and swollen tissue in light: a pale ivory body for streams and lashes. */
	static final int SHEATH = 0xEADBD2;
	/** Raw nerve flesh: the body of a lash, dark enough to read against her pale stone. */
	static final int SINEW = 0xB04A58;
	/** Nerve gold, drawn only on the Ductilis material. */
	static final int NERVE = 0xFFD678;
	/** Ductilis shapes: a line with a travelling impulse, a glow, a crackle, a spark, a flowing band. */
	static final int LINE = 0, GLOW = 1, CRACKLE = 2, SPARK = 3, BAND = 4;
	private static final Vec3 UP = new Vec3(0, 1, 0);
	private static final int SAMPLES = 16;
	/** Gold fibres lie this far over the floor, above her tissue. */
	private static final double NERVE_LIFT = 0.032D;

	private SeraphaeLightGeometry() {
	}

	/**
	 * A stream of light from {@code from} to {@code to}, arching {@code lift} high, reached {@code reach} of the way,
	 * {@code width} its half-width at rest: a soft shell with wisps trailing back, a pale sheath of a body that swells
	 * with knots travelling toward {@code to} at {@code speed} blocks a tick, a white core, one raw sinew twisting through
	 * it, and the knots' hearts lit.
	 */
	static void stream(PoseStack p, VertexConsumer lux, Vec3 from, Vec3 to, Vec3 camera, Vec3 right, Vec3 up, double time,
			int seed, double reach, double lift, double width, double speed, float alpha) {
		if (alpha <= 0.0F || reach <= 0.0D || from.distanceToSqr(to) < 1e-4) return;
		double length = from.distanceTo(to);
		List<Vec3> spine = new ArrayList<>(SAMPLES + 1);
		double[] body = new double[SAMPLES + 1], shell = new double[SAMPLES + 1], core = new double[SAMPLES + 1];
		double[] swell = new double[SAMPLES + 1];
		for (int i = 0; i <= SAMPLES; i++) {
			double t = reach * i / SAMPLES;
			spine.add(from.lerp(to, t).add(0.0D, Math.sin(t * Math.PI) * lift, 0.0D));
			double k = SeraphaeOvergrowthRules.knot(t * length, time, speed, 1.4D);
			swell[i] = k;
			double ends = Math.min(1.0D, 0.45D + t * 3.0D) * Math.min(1.0D, 0.35D + (reach - t) * length / 0.6D);
			body[i] = width * (0.75D + 0.55D * k) * ends;
			shell[i] = body[i] * 2.6D;
			core[i] = body[i] * 0.42D;
		}
		Vec3 axis = to.subtract(from).normalize();
		Vec3 side = axis.cross(UP);
		side = side.lengthSqr() < 1e-6 ? new Vec3(1, 0, 0) : side.normalize();
		Vec3 bend = axis.cross(side).normalize();
		ribbonPath(p, lux, spine, camera, shell, RIBBON, seed, TINT, alpha * 0.35F);
		for (int i = 2; i < spine.size(); i += 3) {
			double w = shell[i] * (0.95D + 0.25D * Math.sin(time * 0.8D + i * 1.7D + seed));
			card(p, lux, spine.get(i).subtract(axis.scale(0.12D)), right, up, w, w * 1.2D, WISP, seed + i, TINT, alpha * 0.25F);
		}
		// the body fills its width (the Lux shell's flowing fill, not a lens or a filament), so it has mass at range
		double[] fill = new double[body.length];
		for (int i = 0; i < body.length; i++) fill[i] = body[i] * 0.8D;
		ribbonPath(p, lux, spine, camera, fill, LUX_SHELL, seed + 1, SHEATH, alpha * 0.7F);
		ribbonPath(p, lux, spine, camera, core, WHITE_BEAM, seed + 2, TINT, alpha);
		List<Vec3> sinew = new ArrayList<>(spine.size());
		double[] sinewWidth = new double[spine.size()];
		for (int i = 0; i < spine.size(); i++) {
			double angle = i * 0.9D - time * 0.22D + seed;
			double r = body[i] * 0.4D;
			sinew.add(spine.get(i).add(side.scale(Math.cos(angle) * r)).add(bend.scale(Math.sin(angle) * r)));
			sinewWidth[i] = body[i] * 0.3D;
		}
		ribbonPath(p, lux, sinew, camera, sinewWidth, RIBBON, seed + 3, CLOT, alpha * 0.8F);
		for (int i = 1; i < spine.size(); i++) {
			if (swell[i] > 0.82D && swell[i] >= swell[i - 1]) {
				card(p, lux, spine.get(i), right, up, body[i] * 1.5D, body[i] * 1.5D, FOCUS, seed + 20 + i, CORE, alpha * 0.85F);
			}
		}
		if (reach < 1.0D) {
			Vec3 head = spine.get(spine.size() - 1);
			card(p, lux, head, right, up, width * 1.6D, width * 1.6D, FOCUS, seed + 40, CORE, alpha);
		}
	}

	/**
	 * A lash laid along {@code trail}, the animated wing tip's own path (oldest first): a pale sheath of a stroke
	 * tapering from its tail to just behind its head, a raw edge on its leading half, a white core, and gold nerve-fire
	 * crackling down its middle with the impulse racing to the head, where dendrites fork out. Through the sweep it runs
	 * with the limb; then it widens, lifts and frays, shedding motes of light.
	 */
	static void lash(PoseStack p, VertexConsumer lux, VertexConsumer gold, List<Vec3> trail, Vec3 camera, Vec3 right, Vec3 up,
			double scale, float head, float fade, float fray, double time, int seed) {
		int count = trail.size();
		if (count < 3 || fade <= 0.0F) return;
		List<Vec3> path = new ArrayList<>(count);
		double[] body = new double[count], edge = new double[count], core = new double[count], fire = new double[count];
		for (int i = 0; i < count; i++) {
			double f = i / (double) (count - 1);
			path.add(trail.get(i).add(0.0D, fray * 0.35D * f, 0.0D));
			double w = (0.05D + 0.27D * Math.pow(f, 1.1D) * Math.min(1.0D, (1.0D - f) * 5.0D + 0.25D)) * scale * (1.0D + fray * 0.8D);
			body[i] = w;
			edge[i] = f < 0.15D ? 0.0D : w * 0.55D;
			core[i] = w * 0.3D * (1.0D - fray);
			fire[i] = w * 1.1D * (1.0D - fray * 0.6D);
		}
		float flicker = (float) (0.88D + 0.12D * Math.sin(time * 0.9D + seed));
		double[] halo = new double[count];
		for (int i = 0; i < count; i++) halo[i] = body[i] * 1.8D;
		ribbonPath(p, lux, path, camera, halo, SLASH, seed + 7, SHEATH, 0.45F * fade * flicker);
		// the body fills its width (a lens or a filament reads as a line at range): raw nerve flesh, its leading half
		// edged in clot
		ribbonPath(p, lux, path, camera, body, LUX_SHELL, seed, SINEW, 0.9F * fade * flicker);
		ribbonPath(p, lux, path, camera, edge, RIBBON, seed + 1, CLOT, 0.9F * fade);
		ribbonPath(p, lux, path, camera, core, WHITE_BEAM, seed + 2, TINT, fade);
		ribbonPath(p, gold, path, camera, fire, LINE, seed + 3, NERVE, fade);
		double[] crackle = new double[count];
		for (int i = 0; i < count; i++) crackle[i] = body[i] * 0.5D;
		ribbonPath(p, gold, path, camera, crackle, CRACKLE, seed + 4, NERVE, 0.85F * fade * (1.0F - fray));
		Vec3 tip = path.get(count - 1);
		if (head < 1.0F || fray < 0.3F) {
			card(p, lux, tip, right, up, 0.12D * scale, 0.12D * scale, FOCUS, seed + 5, CORE, 0.9F * fade);
			Vec3 forward = tip.subtract(path.get(count - 2));
			dendrites(p, gold, tip, forward, camera, 0.45D * scale, seed + 6, fade);
		}
		if (fray > 0.0F) {
			for (int k = 0; k < 4; k++) {
				Vec3 at = path.get(Math.min(count - 1, 1 + k * count / 4)).add(0.0D, fray * (0.3D + 0.2D * k), 0.0D);
				card(p, lux, at, right, up, 0.05D, 0.05D, FOCUS, seed + 10 + k, TINT, fade * 0.8F);
			}
		}
	}

	/**
	 * The reach's trail behind one hand: a long tongue of light laid along the hand's own path, fullest just behind the
	 * palm, a raw thread under it, a white core, and the palm itself a hot knot of light with gold running down to it.
	 */
	static void reach(PoseStack p, VertexConsumer lux, VertexConsumer gold, List<Vec3> trail, Vec3 camera, Vec3 right, Vec3 up,
			float fade, double time, int seed) {
		int count = trail.size();
		if (count < 3 || fade <= 0.0F) return;
		double[] body = new double[count], shell = new double[count], core = new double[count], under = new double[count];
		for (int i = 0; i < count; i++) {
			double f = i / (double) (count - 1);
			double w = 0.03D + 0.24D * Math.pow(f, 1.4D) * Math.min(1.0D, (1.0D - f) * 8.0D + 0.4D);
			body[i] = w;
			shell[i] = w * 2.4D;
			core[i] = w * 0.35D;
			under[i] = w * 0.55D;
		}
		ribbonPath(p, lux, trail, camera, shell, RIBBON, seed, TINT, 0.35F * fade);
		ribbonPath(p, lux, trail, camera, body, SLASH, seed + 1, SHEATH, 0.9F * fade);
		ribbonPath(p, lux, trail, camera, under, RIBBON, seed + 2, CLOT, 0.75F * fade);
		ribbonPath(p, lux, trail, camera, core, WHITE_BEAM, seed + 3, TINT, fade);
		double[] nerve = new double[count];
		for (int i = 0; i < count; i++) nerve[i] = body[i] * 0.9D;
		ribbonPath(p, gold, trail, camera, nerve, LINE, seed + 4, NERVE, 0.8F * fade);
		Vec3 palm = trail.get(count - 1);
		float beat = 0.8F + 0.2F * (float) Math.sin(time * 0.6D + seed);
		card(p, lux, palm, right, up, 0.22D, 0.22D, FOCUS, seed + 5, CORE, fade * beat);
		card(p, lux, palm, right, up, 0.42D, 0.42D, WISP, seed + 6, TINT, 0.4F * fade);
	}

	/** Forked gold dendrites out of a point along {@code forward}: three branches, each forking once. */
	static void dendrites(PoseStack p, VertexConsumer gold, Vec3 at, Vec3 forward, Vec3 camera, double size, int seed, float alpha) {
		if (alpha <= 0.0F || forward.lengthSqr() < 1e-8) return;
		Vec3 f = forward.normalize();
		Vec3 side = f.cross(UP);
		side = side.lengthSqr() < 1e-6 ? new Vec3(1, 0, 0) : side.normalize();
		Vec3 lift = side.cross(f).normalize();
		for (int b = 0; b < 3; b++) {
			double a = (b - 1) * 0.55D + (SeraphaeOvergrowthRules.unit(seed, b) - 0.5D) * 0.3D;
			Vec3 dir = f.scale(Math.cos(a)).add(side.scale(Math.sin(a))).add(lift.scale((SeraphaeOvergrowthRules.unit(seed, 10 + b) - 0.5D) * 0.6D))
					.normalize();
			Vec3 mid = at.add(dir.scale(size * 0.55D));
			ribbonPath(p, gold, List.of(at, mid), camera, 0.05D, 0.03D, LINE, seed + b, NERVE, alpha);
			for (int k = -1; k <= 1; k += 2) {
				Vec3 twig = dir.add(side.scale(k * 0.5D)).add(lift.scale(k * 0.25D)).normalize();
				ribbonPath(p, gold, List.of(mid, mid.add(twig.scale(size * 0.4D))), camera, 0.035D, 0.015D, LINE, seed + b * 3 + k,
						NERVE, alpha * 0.85F);
			}
		}
		ribbonPath(p, gold, List.of(at, at.add(f.scale(size * 0.2D))), camera, 0.12D, SPARK, seed + 9, NERVE, alpha);
	}

	/** One gold spark: a nerve's bead of fire at a point. */
	static void spark(PoseStack p, VertexConsumer gold, Vec3 at, Vec3 right, Vec3 up, double size, int seed, float alpha) {
		card(p, gold, at, right, up, size, size, SPARK, seed, NERVE, alpha);
		card(p, gold, at, right, up, size * 2.4D, size * 2.4D, GLOW, seed + 1, NERVE, alpha * 0.6F);
	}

	/** A nerve firing along {@code path}: a bold gold line with its impulse racing to the end, a crackle round it. */
	static void nerveLine(PoseStack p, VertexConsumer gold, List<Vec3> path, Vec3 camera, double width, int seed, float alpha) {
		if (path.size() < 2 || alpha <= 0.0F) return;
		ribbonPath(p, gold, path, camera, width, LINE, seed, NERVE, alpha);
		ribbonPath(p, gold, path, camera, width * 0.45D, CRACKLE, seed + 1, NERVE, alpha * 0.8F);
		ribbonPath(p, gold, path, camera, width * 0.3D, BAND, seed + 2, NERVE, alpha * 0.5F);
	}

	/**
	 * A crack of light opening down from {@code top} to {@code bottom} ({@code open} 0 shut to 1 wide): a pale lip of a
	 * body round a white core, gold sparking in it; the Fracture's crack down her chest.
	 */
	static void crack(PoseStack p, VertexConsumer lux, VertexConsumer gold, Vec3 top, Vec3 bottom, Vec3 camera, Vec3 right, Vec3 up,
			float open, int seed) {
		if (open <= 0.01F) return;
		Vec3 mid = top.lerp(bottom, 0.45D).add(right.scale(0.02D));
		List<Vec3> path = List.of(top, top.lerp(mid, 0.5D), mid, mid.lerp(bottom, 0.5D), bottom);
		double[] lip = {0.004D, 0.05D * open, 0.075D * open, 0.05D * open, 0.004D};
		double[] core = {0.002D, 0.02D * open, 0.03D * open, 0.02D * open, 0.002D};
		ribbonPath(p, lux, path, camera, lip, SLASH, seed, SHEATH, Math.min(1.0F, open * 1.4F));
		ribbonPath(p, lux, path, camera, core, WHITE_BEAM, seed + 1, TINT, 1.0F);
		card(p, lux, mid, right, up, 0.18D + 0.3D * open, 0.3D + 0.4D * open, WISP, seed + 2, TINT, 0.45F * open);
		ribbonPath(p, gold, path, camera, 0.05D * open, CRACKLE, seed + 3, NERVE, open);
		spark(p, gold, top.lerp(bottom, 0.3D), right, up, 0.04D + 0.03D * open, seed + 4, open);
	}

	/** Gold impulses travelling from {@code from} to {@code to} along a nerve or a thread, {@code speed} of it a tick. */
	static void beads(PoseStack p, VertexConsumer gold, Vec3 from, Vec3 to, Vec3 right, Vec3 up, double time, int count, double speed,
			double size, int seed, float alpha) {
		if (alpha <= 0.0F || count <= 0) return;
		for (int i = 0; i < count; i++) {
			double t = ((time * speed + i / (double) count) % 1.0D + 1.0D) % 1.0D;
			card(p, gold, from.lerp(to, t), right, up, size, size, SPARK, seed + i, NERVE, alpha * (float) Math.sin(Math.PI * t));
		}
	}

	/**
	 * A standing sheet of light on an arc round {@code centre} (pose-relative), {@code radius} out, bearings
	 * {@code fromDeg} to {@code toDeg} about {@code forward}, from {@code low} to {@code high} above the floor at
	 * {@code centre.y}: a continuous shell of light with a lit crest and wisps, its gold edge (the line to jump, or to
	 * duck under) on the Ductilis material when asked for.
	 */
	static void curtain(PoseStack p, VertexConsumer lux, VertexConsumer gold, Vec3 centre, double[] forward, double radius,
			double fromDeg, double toDeg, double low, double high, Vec3 camera, double time, int seed, float alpha,
			boolean goldLow, boolean goldHigh) {
		if (radius <= 0.15D || alpha <= 0.0F || toDeg <= fromDeg || high <= low) return;
		int segments = (int) Math.max(6, Math.min(72, Math.toRadians(toDeg - fromDeg) * radius / 0.45D));
		List<Vec3> feet = new ArrayList<>(segments + 1);
		for (int i = 0; i <= segments; i++) {
			double[] d = Shapes.rotate(forward, fromDeg + (toDeg - fromDeg) * i / segments);
			feet.add(centre.add(d[0] * radius, 0.0D, d[1] * radius));
		}
		wall(p, lux, gold, feet, low, high, camera, time, seed, alpha, goldLow, goldHigh);
	}

	/**
	 * A standing sheet of light along {@code feet} (floor points, pose-relative) from {@code low} to {@code high} above
	 * them: the shell, its lit crest and wisps, and its gold edge when asked for.
	 */
	static void wall(PoseStack p, VertexConsumer lux, VertexConsumer gold, List<Vec3> feet, double low, double high, Vec3 camera,
			double time, int seed, float alpha, boolean goldLow, boolean goldHigh) {
		int segments = feet.size() - 1;
		if (segments < 1 || alpha <= 0.0F || high <= low) return;
		List<Vec3> crest = new ArrayList<>(segments + 1), base = new ArrayList<>(segments + 1);
		for (int i = 0; i <= segments; i++) {
			double wave = 0.9D + 0.1D * Math.sin(i * 0.9D + time * 0.4D + seed);
			base.add(feet.get(i).add(0.0D, low, 0.0D));
			crest.add(feet.get(i).add(0.0D, low + (high - low) * wave, 0.0D));
		}
		for (int i = 0; i < segments; i++) {
			surface(p, lux, base.get(i), base.get(i + 1), crest.get(i + 1), crest.get(i), LUX_SHELL, seed + i, CORE, alpha * 0.3F);
		}
		// body: light pouring up out of the floor in shafts, each a sheath round a white core, rising and guttering on
		// its own beat, and the foot of the sheet lit where it leaves the floor
		double height = high - low;
		for (int i = 0; i <= segments; i += 2) {
			double beat = 0.5D + 0.5D * Math.sin(time * 0.55D + i * 2.3D + seed);
			double top = height * (0.55D + 0.5D * beat);
			Vec3 root = base.get(i);
			List<Vec3> shaft = List.of(root, root.add(0.0D, top * 0.5D, 0.0D), root.add(0.0D, top, 0.0D));
			double w = 0.05D + 0.05D * SeraphaeOvergrowthRules.unit(seed, 50 + i);
			ribbonPath(p, lux, shaft, camera, new double[]{w * 3.0D, w * 2.4D, w * 0.6D}, RIBBON, seed + 60 + i, SHEATH, alpha * 0.85F);
			ribbonPath(p, lux, shaft, camera, new double[]{w * 0.45D, w * 0.35D, 0.0D}, WHITE_BEAM, seed + 90 + i, TINT, alpha * 0.8F);
		}
		// the floor tears raw where the sheet leaves it
		ribbonPath(p, lux, base, camera, 0.1D, SLASH, seed + 9, CLOT, alpha * 0.7F);
		ribbonPath(p, lux, base, camera, 0.14D, SLASH, seed + 7, SHEATH, alpha * 0.75F);
		ribbonPath(p, lux, base, camera, 0.05D, WHITE_BEAM, seed + 8, CORE, alpha * 0.9F);
		ribbonPath(p, lux, crest, camera, 0.05D, RIBBON, seed + 1, CORE, alpha * 0.9F);
		ribbonPath(p, lux, crest, camera, 0.16D, WISP, seed + 2, TINT, alpha * 0.35F);
		if (goldHigh) {
			ribbonPath(p, gold, crest, camera, 0.07D, LINE, seed + 3, NERVE, alpha);
			ribbonPath(p, gold, crest, camera, 0.035D, BAND, seed + 4, NERVE, alpha * 0.6F);
		}
		if (goldLow) {
			ribbonPath(p, gold, base, camera, 0.07D, LINE, seed + 5, NERVE, alpha);
			ribbonPath(p, gold, base, camera, 0.035D, BAND, seed + 6, NERVE, alpha * 0.6F);
		}
	}

	/**
	 * A burst of light out of a struck point, on the Riposte strike's anatomy: a hot flash, a swelling shell, seven rays
	 * with body (a pale sheath round a white core, tapering to points) thrown out along {@code away}, and, for a nerve's
	 * strike, gold crackling out between them. {@code age} is ticks since the strike.
	 */
	static void burst(PoseStack p, VertexConsumer lux, VertexConsumer gold, Vec3 at, Vec3 away, Vec3 camera, Vec3 right, Vec3 up,
			float age, double size, int seed, boolean nerve) {
		if (age < 0.0F || age >= SeraphaeOvergrowthRules.IMPACT) return;
		float flash = 1.0F - HemoEasing.ramp(age, 0.0F, 3.5F);
		double r = size * (1.0D - 0.6D * HemoEasing.clamp01(age / 3.5F));
		card(p, lux, at, right, up, r, r, FOCUS, seed, CORE, flash);
		float shell = (1.0F - HemoEasing.ramp(age, 1.0F, SeraphaeOvergrowthRules.IMPACT)) * 0.5F;
		double w = size * (1.3D + age * 0.14D);
		card(p, lux, at, right, up, w, w * 1.15D, WISP, seed + 1, TINT, shell);
		float fade = 1.0F - HemoEasing.ramp(age, 2.0F, SeraphaeOvergrowthRules.IMPACT);
		double reach = size * (1.2D + age * 0.32D);
		Vec3 dir = away.lengthSqr() < 1e-6 ? new Vec3(1, 0, 0) : away.normalize();
		Vec3 side = dir.cross(UP);
		side = side.lengthSqr() < 1e-6 ? new Vec3(1, 0, 0) : side.normalize();
		for (int i = 0; i < 7; i++) {
			double a = i * 2.39996D + seed;
			Vec3 ray = dir.add(side.scale(Math.cos(a) * 0.8D)).add(0.0D, Math.sin(a) * 0.65D, 0.0D).normalize();
			Vec3 root = at.add(ray.scale(size * 0.25D));
			Vec3 tip = at.add(ray.scale(reach * (0.75D + 0.45D * SeraphaeOvergrowthRules.unit(seed, i))));
			List<Vec3> path = List.of(root, root.lerp(tip, 0.5D), tip);
			ribbonPath(p, lux, path, camera, new double[]{0.07D * size / 0.35D, 0.05D * size / 0.35D, 0.0D}, SLASH, seed + i, SHEATH, fade);
			ribbonPath(p, lux, path, camera, new double[]{0.025D, 0.018D, 0.0D}, WHITE_BEAM, seed + 20 + i, TINT, fade);
			if (nerve && i % 2 == 0) {
				Vec3 kink = root.lerp(tip, 0.55D).add(side.scale((SeraphaeOvergrowthRules.unit(seed, 30 + i) - 0.5D) * 0.3D));
				ribbonPath(p, gold, List.of(root, kink, tip), camera, 0.06D, 0.02D, CRACKLE, seed + 40 + i, NERVE, fade);
			}
		}
	}

	/**
	 * The gold nerve fibres of a network laid flat on the floor, each as far as {@code grow} has run it: a flowing
	 * band of nerve light at {@code fire} strength, and its bright line with impulses racing outward lit as far as
	 * {@code front} (0 to 1 of each fibre). Furrows and vessels are skipped; they are tissue.
	 */
	static void nerves(PoseStack p, VertexConsumer gold, Ground g, List<Seam> seams, float grow, float fire, float front, int seed) {
		if (fire <= 0.01F) return;
		for (int i = 0; i < seams.size(); i++) {
			Seam seam = seams.get(i);
			if (seam.kind() != Kind.FIBRE) continue;
			double shown = SeraphaeOvergrowthRules.shown(grow, seam.start(), seam.span());
			if (shown <= 0.0D) continue;
			floorStrip(p, gold, g, seam, shown, seam.width() * 1.8D, BAND, seed + i * 3, NERVE, fire * 0.6F);
			double lit = Math.min(shown, front);
			if (lit > 0.0D) floorStrip(p, gold, g, seam, lit, seam.width() * 4.0D, LINE, seed + i * 3 + 1, NERVE, fire);
		}
	}

	/** A seam laid flat over the floor as one strip, {@code half} wide, U running 0 to 1 along what is drawn. */
	static void floorStrip(PoseStack p, VertexConsumer v, Ground g, Seam seam, double shown, double half, int shape, int seed,
			int color, float alpha) {
		int n = seam.size();
		if (n < 2 || alpha <= 0.0F) return;
		double visible = seam.length() * Mth.clamp(shown, 0.0D, 1.0D);
		if (visible < 0.05D) return;
		Matrix4f pose = p.last().pose();
		int tile = MaterialQuads.seedTile(seed);
		double y = g.y() + NERVE_LIFT;
		Vec3 c = g.camera();
		double[] xs = seam.x(), zs = seam.y();
		double s = 0.0D, previousS = 0.0D;
		double[] previous = null;
		for (int i = 0; i < n; i++) {
			double x = xs[i], z = zs[i];
			boolean last = false;
			if (i > 0) {
				double step = Math.hypot(x - xs[i - 1], z - zs[i - 1]);
				if (s + step >= visible) {
					double t = step < 1e-6 ? 0.0D : (visible - s) / step;
					x = xs[i - 1] + (x - xs[i - 1]) * t;
					z = zs[i - 1] + (z - zs[i - 1]) * t;
					step = visible - s;
					last = true;
				}
				s += step;
			}
			double tx = xs[Math.min(n - 1, i + 1)] - xs[Math.max(0, i - 1)];
			double tz = zs[Math.min(n - 1, i + 1)] - zs[Math.max(0, i - 1)];
			double length = Math.hypot(tx, tz);
			if (length > 1e-6) {
				double na = -tz / length * half, nr = tx / length * half;
				double[] here = {g.wx(x - na, z - nr), g.wz(x - na, z - nr), g.wx(x + na, z + nr), g.wz(x + na, z + nr)};
				if (previous != null) {
					MaterialQuads.quad(v, pose, previous[0] - c.x, y - c.y, previous[1] - c.z, here[0] - c.x, y - c.y, here[1] - c.z,
							here[2] - c.x, y - c.y, here[3] - c.z, previous[2] - c.x, y - c.y, previous[3] - c.z, shape, tile, color,
							alpha, previousS / visible, s / visible, false);
				}
				previous = here;
				previousS = s;
			}
			if (last) break;
		}
	}
}
