package com.vincenthuto.hemomancy.client.render.world;

import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraGeometry.*;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.common.entity.utility.AwakenedIchorianSigilEntity;
import com.vincenthuto.hemomancy.common.rite.sigil.AwakenedIchorianSigilBodyAnimation;
import com.vincenthuto.hemomancy.common.rite.sigil.AwakenedIchorianSigilFacing;
import com.vincenthuto.hemomancy.common.rite.sigil.AwakenedIchorianSigilPose;
import com.vincenthuto.hemomancy.common.rite.sigil.AwakenedIchorianSigilPoseCalculator;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilAnatomy;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilDefinition;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilFormRules;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilFormRules.Flourish;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilRegistry;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * An awakened Ichorian Sigil's living body, drawn to the Hematic Riposte standard on the living-blood material: each
 * vessel is a translucent shell around three twisting strands of sinew with the sigil's ichor running down its core
 * in boluses, shedding wisps; each landmark is a knot of glossy blood shaped by its role (lobed organ, lidded eye,
 * bound joint, barred rib, clawed tip); each membrane is a breathing sheet veined with ichor. The sigil's authored
 * colour is the ichor, carried on the light material, so the body is crimson flesh lit from within by the same
 * colour that fills its gouges. Each body style adds one signature ({@link Flourish}).
 */
final class IchorianSigilFormGeometry {
	private static final int TINT = 0xFFFFFF, CORE = 0xFFD2C4, CLOT = 0x7A1020, PUPIL = 0x1A0306;
	/** Forms further than this from the camera are not drawn. */
	private static final double DRAW_DISTANCE = 96.0D;
	private static final int SPINE_SAMPLES = 10;
	private static final Vec3 UP = new Vec3(0.0D, 1.0D, 0.0D);

	private IchorianSigilFormGeometry() {
	}

	static void collect(ClientLevel world, PoseStack poses, LuxUmbraBatch batch, Vec3 camera, Vec3 right, Vec3 up,
			float partial) {
		for (Entity entity : world.entitiesForRendering()) {
			if (!(entity instanceof AwakenedIchorianSigilEntity form)) continue;
			Vec3 at = form.getPosition(partial);
			if (at.distanceToSqr(camera) > DRAW_DISTANCE * DRAW_DISTANCE) continue;
			IchorianSigilDefinition sigil = form.getSigilId() == null ? null : IchorianSigilRegistry.get(form.getSigilId());
			if (sigil == null || sigil.awakenedForm().isEmpty()) continue;
			poses.pushPose();
			poses.translate(at.x - camera.x, at.y - camera.y, at.z - camera.z);
			draw(poses, batch.vertices(LuxUmbraBatch.Material.ANIMUS), batch.vertices(LuxUmbraBatch.Material.LUX),
					sigil, form, camera.subtract(at), right, up, AnimationTime.age(form, partial), partial);
			poses.popPose();
		}
	}

	private static void draw(PoseStack p, VertexConsumer flesh, VertexConsumer ichor, IchorianSigilDefinition sigil,
			AwakenedIchorianSigilEntity form, Vec3 camera, Vec3 right, Vec3 up, float time, float partial) {
		IchorianSigilAnatomy anatomy = sigil.awakenedForm().orElseThrow();
		Body body = new Body(p, flesh, ichor, camera, right, up, time, sigil.id().hashCode(), sigil.color() & 0xFFFFFF,
				IchorianSigilFormRules.flourish(anatomy.animation().style()));
		Matrix4f frame = frame(sigil, anatomy, form, time, partial);
		AwakenedIchorianSigilPose pose = AwakenedIchorianSigilPoseCalculator.calculate(sigil, time);
		List<Vec3> points = place(pose, frame);
		body.scale = Math.max(0.2F, pose.scale()) * frame.getScale(new Vector3f()).y;

		for (int index = 0; index < pose.membranes().size(); index++) body.membrane(points, pose.membranes().get(index), index);
		if (body.flourish == Flourish.ECHO) {
			List<Vec3> earlier = place(AwakenedIchorianSigilPoseCalculator.calculate(sigil, time - 9.0F), frame);
			for (var vessel : pose.primaryVessels()) body.echo(earlier, vessel);
		}
		int vesselIndex = 0;
		for (var vessel : pose.primaryVessels()) body.vessel(points, vessel, vesselIndex++, true);
		for (var vessel : pose.secondaryVessels()) body.vessel(points, vessel, vesselIndex++, false);
		for (var landmark : pose.landmarks()) body.knot(points, pose, landmark);
		if (body.flourish == Flourish.DRIP) body.drip(points, pose);
	}

	/** The same turn, authored-forward correction and body sway the entity renderer applied to the old body. */
	private static Matrix4f frame(IchorianSigilDefinition sigil, IchorianSigilAnatomy anatomy,
			AwakenedIchorianSigilEntity form, float time, float partial) {
		AwakenedIchorianSigilBodyAnimation.BodyPose sway = AwakenedIchorianSigilBodyAnimation.pose(sigil.id(), time);
		return new Matrix4f()
				.rotateY((float) Math.toRadians(AwakenedIchorianSigilFacing.renderYaw(form.getRenderFacingYaw(partial))))
				.rotateY((float) Math.toRadians(AwakenedIchorianSigilFacing.authoredForwardCorrection(
						anatomy.forward().x, anatomy.forward().z)))
				.translate(sway.offsetX(), sway.offsetY(), sway.offsetZ())
				.rotateY((float) Math.toRadians(sway.yawDegrees()))
				.rotateX((float) Math.toRadians(sway.pitchDegrees()))
				.rotateZ((float) Math.toRadians(sway.rollDegrees()))
				.scale(sway.scaleX(), sway.scaleY(), sway.scaleZ());
	}

	private static List<Vec3> place(AwakenedIchorianSigilPose pose, Matrix4f frame) {
		List<Vec3> points = new ArrayList<>(pose.landmarks().size());
		for (var landmark : pose.landmarks()) {
			Vector3f at = frame.transformPosition(new Vector3f((float) landmark.position().x,
					(float) landmark.position().y, (float) landmark.position().z));
			points.add(new Vec3(at.x, at.y, at.z));
		}
		return points;
	}

	/** One form's drawing state: its buffers, view and the numbers every part shares. */
	private static final class Body {
		final PoseStack p;
		final VertexConsumer flesh;
		final VertexConsumer ichor;
		final Vec3 camera, right, up;
		final float time;
		final int seed;
		final int color;
		final Flourish flourish;
		final float beat;
		float scale = 1.0F;

		Body(PoseStack p, VertexConsumer flesh, VertexConsumer ichor, Vec3 camera, Vec3 right, Vec3 up, float time,
				int seed, int color, Flourish flourish) {
			this.p = p;
			this.flesh = flesh;
			this.ichor = ichor;
			this.camera = camera;
			this.right = right;
			this.up = up;
			this.time = time;
			this.seed = seed;
			this.color = color;
			this.flourish = flourish;
			this.beat = IchorianSigilFormRules.heartbeat(time);
		}

		// ── Vessels ──

		void vessel(List<Vec3> points, AwakenedIchorianSigilPose.Vessel vessel, int index, boolean primary) {
			float grown = vessel.growth();
			if (grown <= 0.001F) return;
			Vec3 from = points.get(vessel.from()), to = points.get(vessel.to());
			double length = from.distanceTo(to);
			if (length < 1.0E-4D) return;
			double thick = Math.max(0.018D, vessel.thickness()) * grown;
			List<Vec3> spine = spine(from, to, index, thick);
			float flicker = IchorianSigilFormRules.flicker(time, index, seed);

			// Shell: a dark clotted underlayer, a broad translucent sheath over it, then wisps shed against it.
			ribbonPath(p, flesh, spine, camera, thick * 2.1D, RIBBON, seed + index + 50, CLOT, 0.55F * grown);
			ribbonPath(p, flesh, spine, camera, thick * 3.4D * (1.0D + 0.15D * beat), RIBBON, seed + index, TINT,
					0.32F * grown * flicker);
			for (int w = 0; w < 2; w++) {
				double along = 0.28D + 0.44D * w;
				double drift = (time * 0.012D + w * 0.5D + index * 0.31D) % 1.0D;
				Vec3 wispAt = sample(spine, along).add(0.0D, drift * 0.16D * scale, 0.0D);
				card(p, flesh, wispAt, right, up, thick * 2.2D, thick * 3.0D, WISP, seed + index * 7 + w, TINT,
						0.24F * grown * (1.0F - (float) drift) * IchorianSigilFormRules.flicker(time, index * 3 + w, seed));
			}

			// Sinew: three strands twisting round the spine, tapering in three steps from the thicker end.
			Vec3 axis = to.subtract(from).normalize();
			Vec3 side = facingSide(axis, camera.subtract(from.lerp(to, 0.5D)));
			Vec3 lift = axis.cross(side).normalize();
			double twist = primary ? 0.75D : 1.0D;
			for (int strand = 0; strand < 3; strand++) {
				List<Vec3> helix = new ArrayList<>(spine.size());
				for (int i = 0; i < spine.size(); i++) {
					double t = i / (spine.size() - 1.0D);
					double angle = t * Math.PI * 2.0D * twist + strand * Math.PI * 2.0D / 3.0D + time * 0.05D + index;
					double radius = thick * 0.42D * Math.sin(Math.PI * (0.1D + 0.8D * t));
					helix.add(spine.get(i).add(side.scale(Math.cos(angle) * radius)).add(lift.scale(Math.sin(angle) * radius)));
				}
				double[] widths = new double[helix.size()];
				for (int i = 0; i < widths.length; i++) {
					double t = i / (widths.length - 1.0D);
					double step = t < 1.0D / 3.0D ? 0.07D : t < 2.0D / 3.0D ? 0.05D : 0.035D;
					widths[i] = thick * step / 0.07D * 0.62D * (1.0D + 0.25D * beat);
				}
				ribbonPath(p, flesh, helix, camera, widths, RIBBON, seed + index * 11 + strand, TINT,
						0.86F * grown);
			}

			// Ichor: a lit filament down the core and boluses riding it in the sigil's colour.
			ribbonPath(p, ichor, spine, camera, thick * 1.1D, RIBBON, seed + index * 5, color,
					(0.62F + 0.33F * beat) * grown);
			int boluses = flourish.boluses();
			for (int b = 0; b < boluses; b++) {
				float position = IchorianSigilFormRules.bolusPosition(time, index, b, boluses, flourish.bolusSpeed());
				float presence = IchorianSigilFormRules.bolusPresence(position);
				Vec3 at = sample(spine, position);
				card(p, ichor, at, right, up, thick * 1.7D, thick * 1.7D, FOCUS, seed + index * 13 + b, color,
						0.85F * presence * grown);
				card(p, flesh, at, right, up, thick * 1.35D * presence, thick * 1.35D * presence, FOCUS,
						seed + index * 17 + b, TINT, 0.6F * presence * grown);
			}
			if (flourish == Flourish.ARBOR) twigs(spine, side, lift, thick, index, grown);
		}

		/** A gently bowed, writhing line from one landmark to the next. */
		List<Vec3> spine(Vec3 from, Vec3 to, int index, double thick) {
			Vec3 axis = to.subtract(from);
			Vec3 bend = axis.cross(UP);
			if (bend.lengthSqr() < 1.0E-8D) bend = axis.cross(new Vec3(1.0D, 0.0D, 0.0D));
			bend = bend.normalize();
			double bow = Math.min(axis.length() * 0.12D, 0.08D * scale) * (((seed + index) & 1) == 0 ? 1.0D : -1.0D);
			List<Vec3> spine = new ArrayList<>(SPINE_SAMPLES + 1);
			for (int i = 0; i <= SPINE_SAMPLES; i++) {
				double t = i / (double) SPINE_SAMPLES;
				double envelope = Math.sin(Math.PI * t);
				double writhe = Math.sin(time * 0.08D + t * Math.PI * 3.0D + index) * envelope * thick * 0.35D;
				spine.add(from.lerp(to, t).add(bend.scale(envelope * bow + writhe))
						.add(0.0D, Math.cos(time * 0.06D + t * 4.0D + index) * envelope * thick * 0.25D, 0.0D));
			}
			return spine;
		}

		/** The Mnemonic's fading echo of where its spine was nine ticks ago. */
		void echo(List<Vec3> earlier, AwakenedIchorianSigilPose.Vessel vessel) {
			if (vessel.growth() <= 0.001F) return;
			Vec3 from = earlier.get(vessel.from()), to = earlier.get(vessel.to());
			if (from.distanceToSqr(to) < 1.0E-8D) return;
			double thick = Math.max(0.018D, vessel.thickness()) * vessel.growth();
			ribbonPath(p, flesh, spine(from, to, vessel.from() + 40, thick), camera, thick * 3.2D, RIBBON,
					seed + 300 + vessel.from(), TINT, 0.13F * vessel.growth());
			ribbonPath(p, ichor, spine(from, to, vessel.from() + 40, thick), camera, thick * 0.9D, RIBBON,
					seed + 330 + vessel.from(), color, 0.16F * vessel.growth());
		}

		/** The Lattice's fine ichor twigs off each vessel. */
		void twigs(List<Vec3> spine, Vec3 side, Vec3 lift, double thick, int index, float grown) {
			for (int twig = 0; twig < 2; twig++) {
				double along = 0.32D + 0.36D * twig;
				Vec3 root = sample(spine, along);
				double angle = (seed + index * 3 + twig) * 1.31D + Math.sin(time * 0.04D + twig) * 0.3D;
				Vec3 out = side.scale(Math.cos(angle)).add(lift.scale(Math.sin(angle))).add(0.0D, 0.35D, 0.0D).normalize();
				double reach = 0.16D * scale * grown;
				Vec3 bend = root.add(out.scale(reach * 0.55D)).add(lift.scale(reach * 0.15D));
				Vec3 tip = root.add(out.scale(reach));
				ribbonPath(p, flesh, List.of(root, bend, tip), camera, thick * 0.55D, 0.0D, RIBBON,
						seed + 500 + index * 4 + twig, TINT, 0.7F * grown);
				ribbonPath(p, ichor, List.of(root, bend, tip), camera, thick * 0.3D, 0.0D, RIBBON,
						seed + 520 + index * 4 + twig, color, 0.55F * grown);
			}
		}

		// ── Knots ──

		void knot(List<Vec3> points, AwakenedIchorianSigilPose pose, AwakenedIchorianSigilPose.Landmark landmark) {
			Vec3 at = points.get(landmark.source());
			double r = landmark.radius() * scale;
			float alive = Math.max(0.35F, landmark.activation());
			float flicker = IchorianSigilFormRules.flicker(time, landmark.source() + 20, seed);
			Vec3 toward = camera.subtract(at).normalize();
			int s = seed + landmark.source() * 29;
			switch (landmark.role()) {
				case ORGAN -> {
					// Three overlapping lobes of glossy blood around an ichor core that brightens on the beat.
					card(p, flesh, at.add(toward.scale(-r * 0.2D)), right, up, r * 1.15D, r * 1.25D, FOCUS, s, CLOT, alive);
					for (int lobe = 0; lobe < 3; lobe++) {
						double angle = lobe * Math.PI * 2.0D / 3.0D + s * 0.1D + Math.sin(time * 0.04D + lobe) * 0.2D;
						Vec3 offset = right.scale(Math.cos(angle) * r * 0.42D).add(up.scale(Math.sin(angle) * r * 0.42D));
						double lobeSize = r * (0.72D + 0.12D * lobe) * (1.0D + 0.12D * beat);
						card(p, flesh, at.add(offset), right, up, lobeSize, lobeSize, FOCUS, s + lobe + 1, TINT, alive);
					}
					card(p, ichor, at.add(toward.scale(r * 0.25D)), right, up, r * (0.7D + 0.35D * beat),
							r * (0.7D + 0.35D * beat), FOCUS, s + 5, color, (0.55F + 0.4F * beat) * alive * flicker);
				}
				case EYE -> {
					// A lidded eye: blood lid, ichor iris, dark pupil and a pale glint, stacked toward the viewer.
					card(p, flesh, at, right, up, r * 1.45D, r * 1.2D, FOCUS, s, TINT, alive);
					card(p, ichor, at.add(toward.scale(r * 0.15D)), right, up, r * 0.95D, r * 0.95D, RIPPLE, s + 1, color,
							0.95F * alive);
					card(p, ichor, at.add(toward.scale(r * 0.18D)), right, up, r * 0.62D, r * 0.62D, FOCUS, s + 2, color,
							0.8F * alive * flicker);
					card(p, flesh, at.add(toward.scale(r * 0.24D)), right, up, r * 0.3D, r * 0.36D, ANIMUS_FOCUS, s + 3,
							PUPIL, alive);
					card(p, flesh, at.add(toward.scale(r * 0.3D)).add(right.scale(-r * 0.2D)).add(up.scale(r * 0.22D)),
							right, up, r * 0.11D, r * 0.11D, ANIMUS_FOCUS, s + 4, CORE, alive);
					if (flourish == Flourish.GAZE) {
						card(p, ichor, at, right, up, r * 2.1D, r * 2.1D, RIPPLE, s + 5, color, 0.55F * alive * flicker);
					}
				}
				case RIB -> {
					int bars = flourish == Flourish.CAGE ? 2 : 1;
					for (int bar = 0; bar < bars; bar++) rib(points, at, r * (1.0D + 0.35D * bar), s + bar * 3, alive);
					card(p, flesh, at, right, up, r * 0.75D, r * 0.75D, FOCUS, s + 9, TINT, alive);
				}
				case LIMB_TIP, HOOK, MEMBRANE_TIP -> {
					claw(points, pose, landmark, at, r, s, alive);
				}
				default -> {
					// Joint, valve, ganglion: a bound knot with a ring of sinew round it and an ichor spark.
					card(p, flesh, at, right, up, r * 0.95D * (1.0D + 0.1D * beat), r * 0.95D * (1.0D + 0.1D * beat),
							FOCUS, s, TINT, alive);
					card(p, flesh, at.add(toward.scale(r * 0.1D)), right, up, r * 1.25D, r * 1.25D, RIPPLE, s + 1, TINT,
							0.8F * alive);
					card(p, ichor, at.add(toward.scale(r * 0.2D)), right, up, r * 0.42D, r * 0.42D, FOCUS, s + 2, color,
							(0.5F + 0.4F * beat) * alive * flicker);
				}
			}
		}

		/** A curved bar of rib round a rib landmark, drawn as a claw is: thick base, clotted ends, pale core line. */
		void rib(List<Vec3> points, Vec3 at, double r, int s, float alive) {
			Vec3 out = at.subtract(centroid(points));
			out = out.lengthSqr() < 1.0E-8D ? new Vec3(1.0D, 0.0D, 0.0D) : out.normalize();
			Vec3 along = out.cross(UP);
			if (along.lengthSqr() < 1.0E-8D) along = new Vec3(0.0D, 0.0D, 1.0D);
			along = along.normalize();
			double half = r * 2.2D;
			List<Vec3> arc = new ArrayList<>(7);
			for (int i = 0; i <= 6; i++) {
				double t = i / 6.0D * 2.0D - 1.0D;
				double flex = Math.sin(time * 0.05D + s) * 0.08D;
				arc.add(at.add(along.scale(t * half)).subtract(out.scale((t * t) * half * (0.55D + flex))));
			}
			ribbonPath(p, flesh, arc, camera, r * 0.42D, SLASH, s, TINT, alive);
			ribbonPath(p, flesh, arc.subList(0, 3), camera, r * 0.26D, SLASH, s + 1, CLOT, alive);
			ribbonPath(p, flesh, arc.subList(4, 7), camera, r * 0.26D, SLASH, s + 2, CLOT, alive);
			ribbonPath(p, flesh, arc, camera, r * 0.1D, RIBBON, s + 3, CORE, 0.85F * alive);
		}

		/** A limb tip, hook or membrane tip as a three-pass claw curling away from the body. */
		void claw(List<Vec3> points, AwakenedIchorianSigilPose pose, AwakenedIchorianSigilPose.Landmark landmark,
				Vec3 at, double r, int s, float alive) {
			Vec3 from = neighbour(points, pose, landmark.source());
			Vec3 out = at.subtract(from);
			out = out.lengthSqr() < 1.0E-8D ? new Vec3(0.0D, -1.0D, 0.0D) : out.normalize();
			Vec3 curlAxis = out.cross(UP);
			if (curlAxis.lengthSqr() < 1.0E-8D) curlAxis = new Vec3(1.0D, 0.0D, 0.0D);
			curlAxis = curlAxis.normalize();
			Vec3 curlTo = curlAxis.cross(out).normalize();
			boolean hook = landmark.role() == IchorianSigilAnatomy.Role.HOOK;
			boolean fan = landmark.role() == IchorianSigilAnatomy.Role.MEMBRANE_TIP;
			int talons = fan ? 2 : 1;
			double reach = r * (hook ? 3.4D : fan ? 2.2D : 2.8D);
			double curl = (hook ? 0.9D : 0.35D) + 0.12D * Math.sin(time * 0.06D + s);
			card(p, flesh, at, right, up, r * 0.8D, r * 0.8D, FOCUS, s, TINT, alive);
			for (int talon = 0; talon < talons; talon++) {
				Vec3 spread = fan ? curlAxis.scale((talon - 0.5D) * 0.7D) : Vec3.ZERO;
				List<Vec3> path = new ArrayList<>(6);
				for (int i = 0; i <= 5; i++) {
					double t = i / 5.0D;
					path.add(at.add(out.add(spread).normalize().scale(t * reach)).add(curlTo.scale(t * t * reach * curl)));
				}
				ribbonPath(p, flesh, path.subList(0, 4), camera, r * 0.42D, r * 0.26D, SLASH, s + talon * 4 + 1, TINT, alive);
				ribbonPath(p, flesh, path.subList(3, 6), camera, r * 0.26D, 0.0D, SLASH, s + talon * 4 + 2, CLOT, alive);
				ribbonPath(p, flesh, path.subList(0, 5), camera, r * 0.1D, 0.0D, RIBBON, s + talon * 4 + 3, CORE,
						0.85F * alive);
				if (hook && flourish == Flourish.THREAD) thread(path.get(5), out, curlTo, s, alive);
			}
		}

		/** The Suture's thread: a long, looping strand of ichor trailing back from its hook. */
		void thread(Vec3 tip, Vec3 out, Vec3 curlTo, int s, float alive) {
			List<Vec3> loop = new ArrayList<>(14);
			for (int i = 0; i <= 13; i++) {
				double t = i / 13.0D;
				double wave = Math.sin(t * Math.PI * 2.0D - time * 0.09D);
				loop.add(tip.subtract(out.scale(t * 0.9D * scale)).add(curlTo.scale(wave * 0.12D * scale * t))
						.add(0.0D, -t * t * 0.25D * scale, 0.0D));
			}
			ribbonPath(p, ichor, loop, camera, 0.016D * scale, 0.0D, RIBBON, s + 40, color, 0.8F * alive);
		}

		/** The Reservoir's drip: droplets of ichor gathering under its lowest landmark and falling away. */
		void drip(List<Vec3> points, AwakenedIchorianSigilPose pose) {
			Vec3 lowest = points.get(0);
			for (Vec3 point : points) if (point.y < lowest.y) lowest = point;
			double r = 0.05D * scale;
			for (int drop = 0; drop < 3; drop++) {
				float fall = IchorianSigilFormRules.drip(time, drop);
				int s = seed + 700 + drop;
				if (fall < 0.0F) {
					card(p, ichor, lowest.add(0.0D, -r, 0.0D), right, up, r * 0.6D, r * 0.8D, FOCUS, s, color,
							0.6F * pose.quickening());
					continue;
				}
				Vec3 at = lowest.add(0.0D, -r - fall * fall * 1.1D * scale, 0.0D);
				card(p, flesh, at, right, up, r * 0.7D, r * 0.9D, FOCUS, s, TINT, (1.0F - fall) * pose.quickening());
				card(p, ichor, at, right, up, r * 0.9D, r * 0.9D, FOCUS, s + 9, color,
						0.7F * (1.0F - fall) * pose.quickening());
			}
		}

		// ── Membranes ──

		/** A breathing sheet between three landmarks, billowed out of its plane and veined with ichor. */
		void membrane(List<Vec3> points, AwakenedIchorianSigilPose.Membrane membrane, int index) {
			float inflation = membrane.inflation();
			if (inflation <= 0.001F) return;
			Vec3 a = points.get(membrane.a()), b = points.get(membrane.b()), c = points.get(membrane.c());
			Vec3 middle = a.add(b).add(c).scale(1.0D / 3.0D);
			float close = IchorianSigilFormRules.shutter(flourish, time);
			if (close > 0.0F) {
				a = a.lerp(middle, close);
				b = b.lerp(middle, close);
				c = c.lerp(middle, close);
			}
			Vec3 normal = b.subtract(a).cross(c.subtract(a));
			if (normal.lengthSqr() < 1.0E-10D) return;
			normal = normal.normalize();
			double size = Math.max(a.distanceTo(middle), Math.max(b.distanceTo(middle), c.distanceTo(middle)));
			Vec3 belly = middle.add(normal.scale(size * IchorianSigilFormRules.membraneBillow(flourish, time, index)
					* inflation * 2.0D));
			Vec3 ab = billowed(a, b, normal, size, 0.45D), bc = billowed(b, c, normal, size, 0.45D),
					ca = billowed(c, a, normal, size, 0.45D);
			float sheet = (flourish == Flourish.GAZE ? 0.16F : 0.24F) * inflation
					* IchorianSigilFormRules.flicker(time, index + 60, seed);
			int s = seed + 900 + index * 7;
			// A thin dark skin, lit through with the ichor's colour so it reads as the same blood as the gouges.
			surface(p, flesh, a, ab, belly, ca, LUX_SHELL, s, CLOT, sheet);
			surface(p, flesh, b, bc, belly, ab, LUX_SHELL, s + 1, CLOT, sheet);
			surface(p, flesh, c, ca, belly, bc, LUX_SHELL, s + 2, CLOT, sheet);
			float glow = 0.2F * inflation * (0.7F + 0.3F * beat);
			surface(p, ichor, a, ab, belly, ca, LUX_SHELL, s + 4, color, glow);
			surface(p, ichor, b, bc, belly, ab, LUX_SHELL, s + 5, color, glow);
			surface(p, ichor, c, ca, belly, bc, LUX_SHELL, s + 6, color, glow);
			for (Vec3 corner : List.of(a, b, c)) {
				Vec3 mid = corner.lerp(belly, 0.5D).add(normal.scale(size * 0.04D));
				ribbonPath(p, ichor, List.of(corner, mid, belly), camera, 0.02D * scale, 0.006D * scale, RIBBON,
						s + 3, color, 0.7F * inflation);
			}
		}

		Vec3 billowed(Vec3 from, Vec3 to, Vec3 normal, double size, double share) {
			return from.lerp(to, 0.5D).add(normal.scale(size * IchorianSigilFormRules.membraneBillow(flourish, time, 0)
					* share));
		}

		// ── Helpers ──

		Vec3 neighbour(List<Vec3> points, AwakenedIchorianSigilPose pose, int source) {
			for (var vessel : pose.primaryVessels()) {
				if (vessel.to() == source) return points.get(vessel.from());
				if (vessel.from() == source) return points.get(vessel.to());
			}
			return centroid(points);
		}
	}

	private static Vec3 centroid(List<Vec3> points) {
		Vec3 sum = Vec3.ZERO;
		for (Vec3 point : points) sum = sum.add(point);
		return points.isEmpty() ? sum : sum.scale(1.0D / points.size());
	}

	/** A point {@code t} (0..1) of the way along a sampled path. */
	private static Vec3 sample(List<Vec3> path, double t) {
		double at = Math.max(0.0D, Math.min(1.0D, t)) * (path.size() - 1);
		int index = Math.min(path.size() - 2, (int) Math.floor(at));
		return path.get(index).lerp(path.get(index + 1), at - index);
	}
}
