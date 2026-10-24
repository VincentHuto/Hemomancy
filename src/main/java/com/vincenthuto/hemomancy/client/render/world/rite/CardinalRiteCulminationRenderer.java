package com.vincenthuto.hemomancy.client.render.world.rite;

import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.common.rite.harbinger.ActiveRiteData;
import static com.vincenthuto.hemomancy.client.render.world.rite.CardinalRiteVisualRules.*;
import static com.vincenthuto.hemomancy.client.render.world.rite.RiteSurface.*;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.data.ActiveRiteClientData;
import com.vincenthuto.hemomancy.client.render.world.AnimusMortemRenderTypes;
import com.vincenthuto.hemomancy.client.data.CardinalRiteEventClientState;
import com.vincenthuto.hemomancy.client.data.CardinalRiteEventClientState.RiteEvent;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteCulmination;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteEventKind;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteLightningPalette;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import com.vincenthuto.hutoslib.client.particle.BoltRenderer;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.common.lightning.LightningTestBoltFactory;
import com.vincenthuto.hutoslib.common.lightning.LightningTestConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * How a Cardinal Rite resolves. Each culmination style has its own beat, anticipated through the culmination phase
 * and released on completion:
 * <ul>
 * <li>Ascension: a blood pillar rises through the caster, breathing in segments, then frays upward.</li>
 * <li>Grounding: a shock ring races out past the floor's edge, vessels cracking out behind it.</li>
 * <li>Binding: vessels arc from the Focus to everyone nearby, pulse, then snap.</li>
 * <li>Forging: the daemon's blood condenses into a knot over the Focus that drops onto it with a flash.</li>
 * <li>Domain: a membrane dome swells out three chunks, then sinks into the ground.</li>
 * </ul>
 * A collapse or broken structure instead bursts the daemon into dark cells and black-violet lightning at the Focus.
 * Geometry carries every beat; particles and lightning are accents.
 */
public final class CardinalRiteCulminationRenderer {
	private static final double CULL_DISTANCE = 112.0D;
	private static final ParticleColor EMBER = new ParticleColor(235, 52, 38);
	private static final ParticleColor CELL = new ParticleColor(170, 14, 30);
	private static final ParticleColor DARK = new ParticleColor(14, 2, 10);
	private static final ParticleColor DARK_CELL = new ParticleColor(70, 6, 16);
	private static final int FIRST_VOLLEY = 1, SECOND_VOLLEY = 2, LANDING = 4;
	private static final int MAX_BOUND = 8;
	private static final CardinalRiteEventKind[] ENDINGS = { CardinalRiteEventKind.COMPLETED,
			CardinalRiteEventKind.COLLAPSED, CardinalRiteEventKind.BROKEN, CardinalRiteEventKind.CANCELLED };

	private static final View VIEW = new View();
	private static final Path PATH = new Path();
	private static final double[] P = new double[3];
	private static final double[] CTRL = new double[12];
	private static final double[] TARGETS = new double[3 * MAX_BOUND];
	private static final Map<RiteEvent, Integer> FIRED = new IdentityHashMap<>();
	private static long lastAccentTick = Long.MIN_VALUE;

	private CardinalRiteCulminationRenderer() {
	}

	public static void render(PoseStack poseStack, float partialTick) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) return;
		List<RiteEvent> events = CardinalRiteEventClientState.all();
		if (events.isEmpty()) {
			FIRED.clear();
			return;
		}
		var camera = mc.gameRenderer.getMainCamera();
		Vec3 cam = camera.getPosition();
		Quaternionf rotation = camera.rotation();
		double time = AnimationTime.gameTime(mc.level, partialTick);
		var buffers = mc.renderBuffers().bufferSource();
		AnimusMortemRenderTypes.begin(time);

		VertexConsumer blood = null;
		for (RiteEvent event : events) {
			if (!active(event, partialTick, cam)) continue;
			if (blood == null) blood = buffers.getBuffer(AnimusMortemRenderTypes.ANIMUS);
			drawBlood(mc, poseStack, blood, event, cam, rotation, time, partialTick);
		}
		if (blood == null) return;
		buffers.endBatch(AnimusMortemRenderTypes.ANIMUS);

		VertexConsumer decay = null;
		for (RiteEvent event : events) {
			if (!isCollapse(event.kind()) || !active(event, partialTick, cam)) continue;
			if (decay == null) decay = buffers.getBuffer(AnimusMortemRenderTypes.MORTEM);
			Vec3 origin = burstCentre(event.center());
			poseStack.pushPose();
			translate(poseStack, origin, cam, rotation);
			collapseDecay(poseStack.last().pose(), decay, event.age(partialTick), time, seed(event));
			poseStack.popPose();
		}
		if (decay != null) buffers.endBatch(AnimusMortemRenderTypes.MORTEM);

		long tick = mc.level.getGameTime();
		boolean accentTick = tick != lastAccentTick;
		lastAccentTick = tick;
		for (RiteEvent event : events) {
			if (active(event, partialTick, cam)) accents(mc, event, event.age(partialTick), accentTick, partialTick);
		}
		FIRED.keySet().removeIf(event -> event.age(0) > CardinalRiteEventClientState.EVENT_LIFETIME_TICKS);
	}

	private static boolean active(RiteEvent event, float partialTick, Vec3 cam) {
		float age = event.age(partialTick);
		float life = switch (event.kind()) {
			case CULMINATION -> superseded(event) ? -1 : CULMINATION_TICKS + 20;
			case COMPLETED -> releaseTicks(style(event));
			case COLLAPSED, BROKEN -> COLLAPSE_TICKS;
			default -> -1;
		};
		if (age < 0 || age >= life) return false;
		BlockPos c = event.center();
		double reach = CULL_DISTANCE + (style(event) == CardinalRiteCulmination.DOMAIN ? DOMAIN_RADIUS : 0);
		return cam.distanceToSqr(c.getX() + 0.5, c.getY() + 1.0, c.getZ() + 0.5) < reach * reach;
	}

	private static boolean isCollapse(CardinalRiteEventKind kind) {
		return kind == CardinalRiteEventKind.COLLAPSED || kind == CardinalRiteEventKind.BROKEN;
	}

	/** A culmination stops anticipating once the rite has ended, however it ended. */
	private static boolean superseded(RiteEvent culmination) {
		for (CardinalRiteEventKind kind : ENDINGS) {
			RiteEvent end = CardinalRiteEventClientState.latest(culmination.center(), kind);
			if (end != null && end.receivedTick() >= culmination.receivedTick()) return true;
		}
		return false;
	}

	static float releaseTicks(CardinalRiteCulmination style) {
		return switch (style) {
			case ASCENSION -> ASCENSION_TICKS;
			case GROUNDING -> GROUNDING_TICKS;
			case BINDING -> BINDING_TICKS;
			case FORGING -> FORGING_TICKS;
			case DOMAIN -> DOMAIN_TICKS;
		};
	}

	private static CardinalRiteCulmination style(RiteEvent event) {
		CardinalRiteCulmination style = CardinalRiteCulmination.byId(event.style());
		return style == null ? CardinalRiteCulmination.ASCENSION : style;
	}

	private static int seed(RiteEvent event) {
		return Math.floorMod(event.center().hashCode(), 113);
	}

	private static Vec3 focusTop(BlockPos center) {
		return new Vec3(center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 0.5);
	}

	private static Vec3 burstCentre(BlockPos center) {
		return focusTop(center).add(0, 2.2, 0);
	}

	private static void translate(PoseStack poseStack, Vec3 origin, Vec3 cam, Quaternionf rotation) {
		poseStack.translate(origin.x - cam.x, origin.y - cam.y, origin.z - cam.z);
		VIEW.set(cam.x, cam.y, cam.z, origin.x, origin.y, origin.z, rotation);
	}

	private static void drawBlood(Minecraft mc, PoseStack poseStack, VertexConsumer v, RiteEvent event, Vec3 cam,
			Quaternionf rotation, double time, float partialTick) {
		float age = event.age(partialTick);
		int seed = seed(event);
		CardinalRiteCulmination style = style(event);
		BlockPos center = event.center();
		poseStack.pushPose();
		if (isCollapse(event.kind())) {
			translate(poseStack, burstCentre(center), cam, rotation);
			collapseBlood(poseStack.last().pose(), v, age, time, seed);
			poseStack.popPose();
			return;
		}
		boolean release = event.kind() == CardinalRiteEventKind.COMPLETED;
		if (style == CardinalRiteCulmination.ASCENSION) {
			Player caster = caster(mc.level, center);
			Vec3 base = caster == null ? focusTop(center) : caster.getPosition(partialTick);
			translate(poseStack, base, cam, rotation);
			if (release) ascension(poseStack.last().pose(), v, age, time, seed);
			else ascensionThread(poseStack.last().pose(), v, (float) CardinalRiteVisualRules.ascensionThread(age),
					time, seed);
		} else {
			Vec3 origin = focusTop(center);
			translate(poseStack, origin, cam, rotation);
			Matrix4f m = poseStack.last().pose();
			switch (style) {
				case GROUNDING -> grounding(m, v, release, age, time, seed, floorRadius(center));
				case BINDING -> binding(m, v, release, age, time, seed, gatherBound(mc.level, origin, partialTick));
				case FORGING -> forging(m, v, release, age, time, seed);
				case DOMAIN -> {
					if (release) domain(m, v, age, seed);
				}
				default -> {
				}
			}
		}
		poseStack.popPose();
	}

	// ── Ascension ──

	private static void ascension(Matrix4f m, VertexConsumer v, float age, double time, int seed) {
		double height = ascensionHeight(age), fray = ascensionFray(age);
		if (height < 0.1) return;
		float a = (float) (1 - fray);
		double drift = fray * 3;
		// Shell and flame-like wisps.
		PATH.clear();
		for (int i = 0; i <= 14; i++) {
			double t = i / 14.0;
			PATH.add(0, t * height + drift * t, 0, (0.9 - 0.3 * t) * (1 + fray * 0.6));
		}
		ribbon(m, v, VIEW, PATH, RIBBON, seed, TINT, 0.26F * a);
		for (int i = 0; i < 10; i++) {
			double t = (i + 0.5) / 10;
			double flicker = 0.85 + 0.25 * Math.sin(time * 0.9 + i * 1.7 + seed);
			double size = 0.7 * flicker * (1 + fray);
			card(m, v, VIEW, Math.sin(i * 2.3) * 0.15, t * height + drift * t * 1.4, Math.cos(i * 2.3) * 0.15,
					size, size * 1.3, WISP, seed + 1 + i, TINT, 0.24F * a);
		}
		// Breathing core: segments swell and travel upward.
		PATH.clear();
		for (int i = 0; i <= 20; i++) {
			double t = i / 20.0;
			double y = t * height;
			double breath = 0.7 + 0.3 * Math.sin(time * 0.35 - y * 0.9);
			PATH.add(0, y + drift * t, 0, 0.3 * breath * (1 + fray * 0.5));
		}
		ribbon(m, v, VIEW, PATH, RIBBON, seed + 12, TINT, 0.95F * a);
		ribbon(m, v, VIEW, PATH, 0, PATH.size(), 0.18, RIBBON, seed + 13, CORE, 0.9F * a);
		// Sinew twisting up the column, loosening as it frays.
		for (int strand = 0; strand < 3; strand++) {
			PATH.clear();
			for (int i = 0; i <= 24; i++) {
				double t = i / 24.0, y = t * height;
				double angle = strand * Math.PI * 2 / 3 + y * 0.9 - time * 0.12;
				double radius = 0.38 * (1 + fray * 1.5);
				PATH.add(Math.cos(angle) * radius, y + drift * t, Math.sin(angle) * radius, 0.09 - 0.04 * t);
			}
			ribbon(m, v, VIEW, PATH, RIBBON, seed + 14 + strand, TINT, 0.8F * a);
		}
		// Knots riding up the column.
		for (int k = 0; k < 4; k++) {
			double t = (time * 0.03 + k / 4.0) % 1.0;
			card(m, v, VIEW, 0, t * height, 0, 0.22, 0.22, FOCUS, seed + 20 + k, CORE,
					a * 0.8F * (float) Math.sin(t * Math.PI));
		}
		// The crown bursting as the pillar tops out, and the floor rippling at its root.
		float crown = a * (float) (1 - HemoEasing.smoothstep((age - ASCENSION_RISE_TICKS) / 10.0));
		card(m, v, VIEW, 0, height, 0, 0.7, 0.7, FOCUS, seed + 25, TINT, crown);
		ground(m, v, 0, 0.05, 0, (1 + age * 0.15) / 0.76, RIPPLE, seed + 26, TINT,
				0.6F * (float) (1 - HemoEasing.smoothstep(age / 40.0)));
		ground(m, v, 0, 0.04, 0, 1.3, POOL, seed + 27, TINT, 0.5F * a);
	}

	private static void ascensionThread(Matrix4f m, VertexConsumer v, float thread, double time, int seed) {
		if (thread <= 0) return;
		double height = 8 * thread;
		PATH.clear();
		for (int i = 0; i <= 10; i++) {
			double t = i / 10.0;
			PATH.add(Math.sin(time * 0.1 + t * 5) * 0.05, t * height, 0, 0.06 + 0.06 * thread);
		}
		ribbon(m, v, VIEW, PATH, RIBBON, seed + 30, TINT, 0.7F * thread);
		card(m, v, VIEW, 0, 0.3, 0, 0.6, 0.6, WISP, seed + 31, TINT, 0.3F * thread);
	}

	// ── Grounding ──

	private static void grounding(Matrix4f m, VertexConsumer v, boolean release, float age, double time, int seed,
			double floorRadius) {
		if (!release) {
			// The floor heaves in time with the heart before it lets go.
			double phase = heartbeatPhase(time);
			if (phase < 0.5) {
				double r = Math.max(1, floorRadius) * phase / 0.5;
				ground(m, v, 0, 0.06, 0, r / 0.76, RIPPLE, seed + 40, TINT,
						0.4F * (float) ((1 - phase / 0.5) * HemoEasing.smoothstep(age / 20.0)));
			}
			return;
		}
		double r = groundingFront(age, floorRadius);
		float fade = (float) fadeOut(age, GROUNDING_TICKS, 24);
		ground(m, v, 0, 0.06, 0, r / 0.76, RIPPLE, seed + 41, TINT, 0.9F * fade);
		ground(m, v, 0, 0.05, 0, r * 0.8 / 0.76, RIPPLE, seed + 42, TINT, 0.4F * fade);
		// A bright broken rim riding the front.
		for (int arc = 0; arc < 6; arc++) {
			PATH.clear();
			double start = arc * Math.PI / 3 + seed;
			for (int i = 0; i <= 8; i++) {
				double angle = start + i * (Math.PI / 3) * 0.8 / 8;
				PATH.add(Math.cos(angle) * r, 0.12, Math.sin(angle) * r, 0.18);
			}
			ribbon(m, v, VIEW, PATH, RIBBON, seed + 43 + arc, TINT, 0.8F * fade);
		}
		// Vessels cracking outward through the ground behind the front.
		double inner = Math.min(r, Math.max(1, floorRadius) * 0.4);
		for (int k = 0; k < 16; k++) {
			double angle = k * Math.PI * 2 / 16 + hash(k + seed) * 0.3;
			PATH.clear();
			for (int i = 0; i <= 8; i++) {
				double t = i / 8.0;
				double d = HemoEasing.lerp(inner, r, t);
				double jitter = Math.sin(d * 1.7 + k * 3.1) * 0.25;
				double ax = Math.cos(angle), az = Math.sin(angle);
				PATH.add(ax * d - az * jitter, 0.07, az * d + ax * jitter, 0.08 - 0.03 * t);
			}
			ribbon(m, v, VIEW, PATH, RIBBON, seed + 50 + k, TINT, 0.7F * fade);
			ribbon(m, v, VIEW, PATH, 6, 9, 1.2, SLASH, seed + 66 + k, CLOT, 0.6F * fade);
		}
		card(m, v, VIEW, 0, 0.3, 0, 0.8, 0.8, FOCUS, seed + 82, TINT, (float) (1 - HemoEasing.smoothstep(age / 10.0)));
	}

	private static double floorRadius(BlockPos center) {
		ActiveRiteData.RiteEntry entry = entryAt(center);
		if (entry == null) return 4.0;
		return entry.getFootprintRadius() > 0 ? entry.getFootprintRadius() : entry.getRiteSize() * 2.0 + 3.0;
	}

	// ── Binding ──

	private static void binding(Matrix4f m, VertexConsumer v, boolean release, float age, double time, int seed,
			int bound) {
		for (int b = 0; b < bound; b++) {
			double tx = TARGETS[b * 3], ty = TARGETS[b * 3 + 1], tz = TARGETS[b * 3 + 2];
			CTRL[0] = 0;
			CTRL[1] = 0.2;
			CTRL[2] = 0;
			CTRL[3] = tx * 0.15;
			CTRL[4] = 2.6;
			CTRL[5] = tz * 0.15;
			CTRL[6] = tx * 0.75;
			CTRL[7] = ty + 2.0;
			CTRL[8] = tz * 0.75;
			CTRL[9] = tx;
			CTRL[10] = ty;
			CTRL[11] = tz;
			int s = seed + b * 7;
			if (!release) {
				// Faint threads feel for the participants while the daemon merges.
				double reach = HemoEasing.smoothstep((age - 20) / 60.0);
				if (reach > 0) vessel(m, v, 0, reach, time, s, 0.25F * (float) reach, true);
				continue;
			}
			double reach = bindingReach(age), snap = bindingSnap(age);
			float fade = (float) fadeOut(age, BINDING_TICKS, 16);
			if (snap <= 0) {
				vessel(m, v, 0, reach, time, s, fade, false);
				if (age >= BINDING_REACH_TICKS) {
					for (int k = 0; k < 3; k++) {
						double t = ((age - BINDING_REACH_TICKS) / 8.0 + k / 3.0) % 1.0;
						bezier(CTRL, t, P);
						card(m, v, VIEW, P[0], P[1], P[2], 0.16, 0.16, FOCUS, s + 3 + k, CORE,
								fade * (float) Math.sin(t * Math.PI));
					}
				}
			} else {
				vessel(m, v, 0, 0.55 * (1 - snap), time, s, fade, false);
				vessel(m, v, 0.55 + 0.45 * snap, 1, time, s + 1, fade, false);
				double flash = Math.sin(Math.min(1, snap * 1.4) * Math.PI);
				card(m, v, VIEW, tx, ty, tz, 0.55 * flash, 0.55 * flash, FOCUS, s + 6, TINT, fade * (float) flash);
				bezier(CTRL, 0.55, P);
				card(m, v, VIEW, P[0], P[1], P[2], 0.3 * flash, 0.3 * flash, WISP, s + 7, TINT, fade * (float) flash);
			}
		}
	}

	/** One stretch of a binding vessel: shell, two twisting strands and a pale core. */
	private static void vessel(Matrix4f m, VertexConsumer v, double from, double to, double time, int seed,
			float alpha, boolean threadOnly) {
		if (to - from < 0.02 || alpha <= 0.003F) return;
		PATH.clear();
		for (int i = 0; i <= 14; i++) {
			bezier(CTRL, HemoEasing.lerp(from, to, i / 14.0), P);
			PATH.add(P[0], P[1], P[2], threadOnly ? 0.05 : 0.14);
		}
		if (threadOnly) {
			ribbon(m, v, VIEW, PATH, RIBBON, seed, CORE, alpha);
			return;
		}
		ribbon(m, v, VIEW, PATH, RIBBON, seed, TINT, alpha * 0.3F);
		ribbon(m, v, VIEW, PATH, 0, PATH.size(), 0.3, RIBBON, seed + 1, CORE, alpha * 0.8F);
		for (int strand = 0; strand < 2; strand++) {
			PATH.clear();
			for (int i = 0; i <= 14; i++) {
				double t = HemoEasing.lerp(from, to, i / 14.0);
				bezier(CTRL, t, P);
				double angle = t * 16 + strand * Math.PI - time * 0.2;
				PATH.add(P[0] + Math.cos(angle) * 0.06, P[1] + Math.sin(angle) * 0.06, P[2] + Math.sin(angle) * 0.04,
						0.06);
			}
			ribbon(m, v, VIEW, PATH, RIBBON, seed + 2 + strand, TINT, alpha * 0.85F);
		}
	}

	/** Fills {@link #TARGETS} with nearby living players' chests relative to {@code origin}; returns the count. */
	private static int gatherBound(ClientLevel level, Vec3 origin, float partialTick) {
		int count = 0;
		double radiusSqr = BINDING_RADIUS * BINDING_RADIUS;
		for (Player player : level.players()) {
			if (count >= MAX_BOUND) break;
			if (player.isSpectator() || !player.isAlive()) continue;
			Vec3 at = player.getPosition(partialTick);
			if (at.distanceToSqr(origin) > radiusSqr) continue;
			TARGETS[count * 3] = at.x - origin.x;
			TARGETS[count * 3 + 1] = at.y + player.getBbHeight() * 0.6 - origin.y;
			TARGETS[count * 3 + 2] = at.z - origin.z;
			count++;
		}
		return count;
	}

	// ── Forging ──

	private static void forging(Matrix4f m, VertexConsumer v, boolean release, float age, double time, int seed) {
		float pulse = heartbeat(time);
		if (!release) {
			double condense = forgingCondense(age);
			float appear = (float) HemoEasing.smoothstep(age / 10.0);
			double y = FORGING_KNOT_HEIGHT;
			for (int k = 0; k < 6; k++) {
				PATH.clear();
				for (int i = 0; i <= 10; i++) {
					double t = i / 10.0;
					double radius = 2.2 * (1 - condense * 0.6) * (1 - t);
					double angle = k * Math.PI / 3 + t * 4 + time * 0.08;
					PATH.add(Math.cos(angle) * radius, y + (1 - t) * Math.sin(k * 1.7) * 0.6,
							Math.sin(angle) * radius, 0.07 - 0.04 * t);
				}
				ribbon(m, v, VIEW, PATH, RIBBON, seed + 90 + k, TINT, 0.6F * appear);
			}
			double knot = (0.15 + 0.3 * condense) * (1 + 0.3 * pulse);
			card(m, v, VIEW, 0, y, 0, 0.9 * condense + 0.2, 0.9 * condense + 0.2, WISP, seed + 96, TINT, 0.35F * appear);
			card(m, v, VIEW, 0, y, 0, knot, knot, FOCUS, seed + 97, TINT, appear);
			card(m, v, VIEW, 0, y, 0, knot * 0.45, knot * 0.45, FOCUS, seed + 98, CORE, appear * (0.6F + 0.4F * pulse));
			return;
		}
		double y = forgingKnotHeight(age);
		if (age < FORGING_DROP_TICKS + 1) {
			PATH.clear().add(0, y + 0.9, 0, 0.04).add(0, y + 0.4, 0, 0.1).add(0, y, 0, 0.16);
			ribbon(m, v, VIEW, PATH, RIBBON, seed + 99, TINT, 0.7F);
			card(m, v, VIEW, 0, y, 0, 0.45, 0.45, FOCUS, seed + 100, TINT, 1);
			card(m, v, VIEW, 0, y, 0, 0.2, 0.2, FOCUS, seed + 101, CORE, 1);
		}
		double flash = forgingFlash(age);
		if (flash > 0) {
			double since = age - FORGING_DROP_TICKS;
			card(m, v, VIEW, 0, 0.3, 0, 1.6 * flash, 1.6 * flash, FOCUS, seed + 102, TINT, (float) flash);
			card(m, v, VIEW, 0, 0.3, 0, 0.6 * flash, 0.6 * flash, FOCUS, seed + 103, CORE, (float) flash);
			ground(m, v, 0, 0.05, 0, (0.5 + since * 0.15) / 0.76, RIPPLE, seed + 104, TINT, 0.8F * (float) flash);
			double streak = Math.min(2.2, 0.3 + since * 0.14);
			for (int k = 0; k < 8; k++) {
				double angle = k * Math.PI / 4 + seed;
				double ax = Math.cos(angle), az = Math.sin(angle);
				PATH.clear().add(ax * 0.3, 0.06, az * 0.3, 0.09).add(ax * streak, 0.06, az * streak, 0.03);
				ribbon(m, v, VIEW, PATH, SLASH, seed + 105 + k, TINT, (float) flash);
			}
		}
		ground(m, v, 0, 0.04, 0, 0.8, POOL, seed + 113, TINT, 0.5F * (float) fadeOut(age, FORGING_TICKS, 20)
				* (float) HemoEasing.smoothstep((age - FORGING_DROP_TICKS) / 2.0));
	}

	// ── Domain ──

	private static void domain(Matrix4f m, VertexConsumer v, float age, int seed) {
		double radius = domainRadius(age), sink = domainSink(age);
		float fade = (float) fadeOut(age, DOMAIN_TICKS, 16);
		if (radius < 0.5) return;
		double cy = -radius * 0.95 * sink;
		dome(m, v, VIEW, 0, cy, 0, radius, 10, 40, seed + 120, TINT, 0.02F * fade, 0.28F * fade);
		double rim = Math.sqrt(Math.max(0, radius * radius - cy * cy));
		ground(m, v, 0, 0.1, 0, rim / 0.76, RIPPLE, seed + 121, TINT, 0.7F * fade);
	}

	// ── Collapse ──

	private static void collapseBlood(Matrix4f m, VertexConsumer v, float age, double time, int seed) {
		float torn = (float) (1 - HemoEasing.smoothstep(age / 30.0));
		for (int k = 0; k < 6; k++) {
			direction(k, 6, seed, P);
			double d = collapseReach(age, 0.7 + 0.5 * hash(k + seed));
			double drop = 0.006 * age * age;
			PATH.clear().add(P[0] * d * 0.6, P[1] * d * 0.6 - drop, P[2] * d * 0.6, 0.08)
					.add(P[0] * (d * 0.6 + 0.3), P[1] * (d * 0.6 + 0.3) - drop + 0.05, P[2] * (d * 0.6 + 0.3), 0.06)
					.add(P[0] * (d * 0.6 + 0.6), P[1] * (d * 0.6 + 0.6) - drop, P[2] * (d * 0.6 + 0.6), 0.02);
			ribbon(m, v, VIEW, PATH, RIBBON, seed + 130 + k, CLOT, torn);
			card(m, v, VIEW, P[0] * d, P[1] * d - drop, P[2] * d, 0.08, 0.08, FOCUS, seed + 136 + k, CLOT, torn);
		}
		card(m, v, VIEW, 0, 0, 0, 0.9, 0.9, FOCUS, seed + 142, CLOT, (float) (1 - HemoEasing.smoothstep(age / 6.0)));
	}

	private static void collapseDecay(Matrix4f m, VertexConsumer v, float age, double time, int seed) {
		double core = 0.6 + age * 0.1;
		card(m, v, VIEW, 0, 0, 0, core, core, FOCUS, seed + 150, TINT, (float) (1 - HemoEasing.smoothstep(age / 16.0)));
		float cells = (float) (1 - HemoEasing.smoothstep((age - 10) / 30.0));
		for (int k = 0; k < 18; k++) {
			direction(k, 18, seed, P);
			double d = collapseReach(age, 0.6 + 0.6 * hash(k * 3 + seed));
			double size = 0.14 * (1 - age / COLLAPSE_TICKS);
			card(m, v, VIEW, P[0] * d, P[1] * d - 0.006 * age * age, P[2] * d, size, size, FOCUS, seed + 151 + k,
					TINT, cells);
		}
		for (int k = 0; k < 4; k++) {
			double rise = age * 0.05 + k * 0.3;
			double size = 0.8 + age * 0.04;
			card(m, v, VIEW, Math.sin(k * 2.1 + seed) * 0.6, rise, Math.cos(k * 2.1 + seed) * 0.6, size, size * 1.2,
					WISP, seed + 170 + k, TINT, 0.4F * cells);
		}
	}

	/** Evenly spread unit direction {@code k} of {@code n}. */
	private static void direction(int k, int n, int seed, double[] out) {
		double y = 1 - 2 * (k + 0.5) / n, ring = Math.sqrt(1 - y * y), angle = k * 2.399963 + seed;
		out[0] = Math.cos(angle) * ring;
		out[1] = y;
		out[2] = Math.sin(angle) * ring;
	}

	// ── Accents ──

	private static void accents(Minecraft mc, RiteEvent event, float age, boolean accentTick, float partialTick) {
		ClientLevel level = mc.level;
		var random = level.random;
		int seed = seed(event);
		if (isCollapse(event.kind())) {
			Vec3 c = burstCentre(event.center());
			if (fire(event, FIRST_VOLLEY)) {
				lightning(c, 6, seed, 2.5, partialTick);
				for (int k = 0; k < 10; k++) {
					level.addParticle(HemoParticleData.darkGlow(DARK), c.x, c.y, c.z, (random.nextDouble() - 0.5) * 0.3,
							(random.nextDouble() - 0.4) * 0.3, (random.nextDouble() - 0.5) * 0.3);
					level.addParticle(HemoParticleData.bloodCell(DARK_CELL), c.x, c.y, c.z,
							(random.nextDouble() - 0.5) * 0.25, random.nextDouble() * 0.2,
							(random.nextDouble() - 0.5) * 0.25);
				}
			}
			if (age >= 8 && fire(event, SECOND_VOLLEY)) lightning(c, 4, seed + 7, 4.0, partialTick);
			return;
		}
		if (event.kind() != CardinalRiteEventKind.COMPLETED || !accentTick) return;
		Vec3 focus = focusTop(event.center());
		switch (style(event)) {
			case GROUNDING -> {
				if (age >= GROUNDING_FRONT_TICKS) return;
				double r = groundingFront(age, floorRadius(event.center()));
				for (int k = 0; k < 4; k++) {
					double angle = random.nextDouble() * Math.PI * 2;
					double x = focus.x + Math.cos(angle) * r, z = focus.z + Math.sin(angle) * r;
					level.addParticle(k % 2 == 0 ? HemoParticleData.bloodCell(CELL)
							: HemoParticleData.emberSpark(EMBER, 0.14F, 0.85F, 20), x, focus.y + 0.1, z,
							0.0, 0.12 + random.nextDouble() * 0.1, 0.0);
				}
			}
			case FORGING -> {
				if (age < FORGING_DROP_TICKS || !fire(event, LANDING)) return;
				for (int k = 0; k < 10; k++) {
					level.addParticle(HemoParticleData.emberSpark(EMBER, 0.12F, 0.9F, 22), focus.x, focus.y + 0.2,
							focus.z, (random.nextDouble() - 0.5) * 0.4, random.nextDouble() * 0.25,
							(random.nextDouble() - 0.5) * 0.4);
				}
				level.addParticle(HemoParticleData.glow(CELL), focus.x, focus.y + 0.3, focus.z, 0, 0.02, 0);
			}
			case ASCENSION -> {
				if (age >= ASCENSION_FRAY_START) return;
				Player caster = caster(level, event.center());
				Vec3 base = caster == null ? focus : caster.position();
				level.addParticle(HemoParticleData.bloodCell(CELL), base.x + (random.nextDouble() - 0.5) * 0.6,
						base.y + random.nextDouble() * ascensionHeight(age) * 0.5,
						base.z + (random.nextDouble() - 0.5) * 0.6, 0.0, 0.2, 0.0);
			}
			default -> {
			}
		}
	}

	private static boolean fire(RiteEvent event, int flag) {
		int fired = FIRED.getOrDefault(event, 0);
		if ((fired & flag) != 0) return false;
		FIRED.put(event, fired | flag);
		return true;
	}

	/** Black-violet HutosLib bolts flung out of the burst. */
	private static void lightning(Vec3 centre, int count, int seed, double reach, float partialTick) {
		for (int k = 0; k < count; k++) {
			direction(k, count, seed, P);
			double length = reach * (0.6 + 0.8 * hash(k + seed * 5));
			Vec3 end = centre.add(P[0] * length, P[1] * length * 0.6, P[2] * length);
			long boltSeed = seed * 131L + k;
			LightningTestConfig config = new LightningTestConfig(LightningTestConfig.Backend.BOLT, CardinalRiteLightningPalette.OUTER_BLACK,
					CardinalRiteLightningPalette.OUTER_BLACK, CardinalRiteLightningPalette.INNER_PURPLE, (float) (length + 4.0), 0.0F, 0.0F, 0.0F, 10.0F, 2.6F, 8, 6, 0.22F,
					0.04F, true, boltSeed, false, 20);
			BoltRenderer.INSTANCE.add(LightningTestBoltFactory.create(centre, end, boltSeed, config.outerColor(),
					config.size(), config), partialTick);
			BoltRenderer.INSTANCE.add(LightningTestBoltFactory.create(centre, end, boltSeed, config.innerColor(),
					Math.max(0.01F, config.size() * 0.45F), config), partialTick);
		}
	}

	// ── Rite lookup ──

	private static ActiveRiteData.RiteEntry entryAt(BlockPos center) {
		for (ActiveRiteData.RiteEntry rite : ActiveRiteClientData.getActiveRites()) {
			if (rite.getCenter().equals(center)) return rite;
		}
		CardinalRiteEventClientState.Ghost ghost = CardinalRiteEventClientState.ghostAt(center);
		return ghost == null ? null : ghost.entry();
	}

	private static Player caster(ClientLevel level, BlockPos center) {
		ActiveRiteData.RiteEntry entry = entryAt(center);
		UUID owner = entry == null ? null : entry.getOwner();
		return owner == null ? null : level.getPlayerByUUID(owner);
	}

	private static void bezier(double[] c, double t, double[] out) {
		double u = 1 - t;
		double c0 = u * u * u, c1 = 3 * u * u * t, c2 = 3 * u * t * t, c3 = t * t * t;
		for (int i = 0; i < 3; i++) out[i] = c[i] * c0 + c[3 + i] * c1 + c[6 + i] * c2 + c[9 + i] * c3;
	}
}
