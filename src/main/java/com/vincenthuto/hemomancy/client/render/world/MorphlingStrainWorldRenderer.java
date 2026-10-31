package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.render.world.OutpouringBrushes.Mat;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.morphling.IEquippedMorphling;
import com.vincenthuto.hemomancy.common.entity.npc.court.CourtMemberEntity;
import com.vincenthuto.hemomancy.common.entity.utility.MorphlingFieldEntity;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.CourtHouse;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingActiveState;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingItem;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingStrain;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingStrains;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.PrimalMorphlingRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * The house strains' world-space pieces, drawn on the shared toolkit (the LuxUmbra, Ductilis and Mortem materials
 * through {@link OutpouringBrushes}): Dead Man's Rope's strands to partners, block anchors and the Lifeline point,
 * with a light pulse running along each and beads along the line; its Primal train of strands; the Bloom and Whale
 * Fall field patches; Holdfast's grip strands, Sunburst's Arbor roots and Marrowplume's Whale Fall stalks spreading
 * into the ground; Ink Cap's dissolve and Hushbell's frost seal; and the strands that tie the Woven Current's members
 * to each other at the Line Camp. Called from {@link WorldEffectDispatcher}.
 */
public final class MorphlingStrainWorldRenderer {
	static final int VIOLET = 0x9C88FF, VIOLET_CORE = 0xE0D8FF, GOLD = 0xF6C45A, GOLD_DEEP = 0xD9822B;
	static final int STONE = 0xB49A8A, BONE = 0xEFE4D2, MARROW_RED = 0xD8343C, FROST = 0xCFE6F6, COBALT = 0x5878E0;
	private static final double CHEST = 0.62;
	/** How far a Woven Current member reaches to tie in to the nearest other member. */
	public static final double COURT_LINK_RANGE = 12.0;

	private MorphlingStrainWorldRenderer() {
	}

	private record View(PoseStack p, Vec3 camera, Vec3 right, Vec3 up, double time, float partial) {
		Vec3 local(Vec3 at) {
			return at.subtract(camera);
		}
	}

	public static void render(RenderLevelStageEvent event) {
		if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
		Minecraft mc = Minecraft.getInstance();
		ClientLevel level = mc.level;
		if (level == null) return;
		List<Player> wearers = new ArrayList<>();
		for (Player player : level.players()) {
			// Deliquesce makes the host invisible; its ink dissolve is the one piece still drawn for an invisible host.
			if (HemoCapabilityAccess.getEquippedMorphling(player).hasMorphling() && (!player.isInvisible() || deliquescing(player))) {
				wearers.add(player);
			}
		}
		List<MorphlingFieldEntity> fields = new ArrayList<>();
		List<CourtMemberEntity> woven = new ArrayList<>();
		for (Entity entity : level.entitiesForRendering()) {
			if (entity instanceof MorphlingFieldEntity field) fields.add(field);
			if (entity instanceof CourtMemberEntity member && member.role().house() == CourtHouse.WOVEN_CURRENT
					&& !member.isInvisible()) woven.add(member);
		}
		if (wearers.isEmpty() && fields.isEmpty() && woven.size() < 2) return;

		float partial = event.getPartialTick().getGameTimeDeltaPartialTick(true);
		Quaternionf rotation = event.getCamera().rotation();
		View view = new View(new PoseStack(), event.getCamera().getPosition(), new Vec3(new Vector3f(1, 0, 0).rotate(rotation)),
				new Vec3(new Vector3f(0, 1, 0).rotate(rotation)), AnimationTime.gameTime(level, (double) partial), partial);
		OutpouringBrushes.begin(view.time);
		for (MorphlingFieldEntity field : fields) field(view, field);
		courtLinks(view, woven);
		boolean firstPerson = mc.options.getCameraType().isFirstPerson();
		for (Player player : wearers) {
			boolean self = firstPerson && mc.getCameraEntity() == player;
			wearer(view, level, player, self);
		}
		OutpouringBrushes.flush(mc.renderBuffers().bufferSource());
	}

	/** An Ink Cap host mid-Deliquesce: invisible, but its dissolve and ink trail still show. */
	static boolean deliquescing(Player player) {
		IEquippedMorphling equipped = HemoCapabilityAccess.getEquippedMorphling(player);
		return MorphlingActiveState.has(equipped.getActiveFlags(), MorphlingActiveState.DELIQUESCE)
				&& MorphlingStrains.of(equipped.getEquippedMorphling()).orElse(null) == MorphlingStrains.INK_CAP;
	}

	private static void wearer(View view, ClientLevel level, Player player, boolean self) {
		if (player.isInvisible()) {
			if (!self) dissolve(view, player, player.getPosition(view.partial), player.getId() * 31);
			return;
		}
		IEquippedMorphling equipped = HemoCapabilityAccess.getEquippedMorphling(player);
		MorphlingStrain strain = MorphlingStrains.of(equipped.getEquippedMorphling()).orElse(null);
		int flags = equipped.getActiveFlags();
		int maturity = MorphlingItem.getMaturityLevel(equipped.getEquippedMorphling());
		Vec3 feet = player.getPosition(view.partial);
		Vec3 chest = feet.add(0, player.getBbHeight() * CHEST, 0);
		int seed = player.getId() * 31;

		// Strands are drawn for whatever the host holds, so a strand still shows the frame its strain changes.
		for (int id : equipped.getLinkedEntityIds()) {
			Entity other = level.getEntity(id);
			if (other == null) continue;
			Vec3 end = other.getPosition(view.partial).add(0, other.getBbHeight() * CHEST, 0);
			strand(view, chest, end, seed + id, MorphlingActiveState.has(flags, MorphlingActiveState.REELING), self);
		}
		for (long anchor : equipped.getLinkedAnchors()) {
			Vec3 end = Vec3.atCenterOf(BlockPos.of(anchor));
			strand(view, chest, end, seed + (int) anchor, MorphlingActiveState.has(flags, MorphlingActiveState.REELING), self);
			knot(view, end, seed + (int) anchor);
		}
		if (strain == null) return;
		if (MorphlingActiveState.has(flags, MorphlingActiveState.LIFELINE)) {
			Vec3 anchor = MorphlingStrainTrails.lifelineAnchor(player);
			if (anchor != null) {
				strand(view, chest, anchor.add(0, 0.1, 0), seed + 7, false, self);
				knot(view, anchor.add(0, 0.1, 0), seed + 7);
			}
		}
		boolean primal = maturity >= PrimalMorphlingRules.PRIMAL_LEVEL;
		if (strain == MorphlingStrains.DEAD_MANS_ROPE && primal) train(view, player, seed);
		if (strain == MorphlingStrains.MERMAIDS_PURSE && MorphlingActiveState.has(flags, MorphlingActiveState.SAIL_RAISED)) wake(view, player, seed);
		if (strain == MorphlingStrains.HOLDFAST && MorphlingActiveState.has(flags, MorphlingActiveState.ANCHORED
				| MorphlingActiveState.BREAKWATER | MorphlingActiveState.HOLDING | MorphlingActiveState.BRACE)) {
			groundRoots(view, feet, seed, MorphlingActiveState.has(flags, MorphlingActiveState.BREAKWATER) ? 9 : 6,
					MorphlingActiveState.has(flags, MorphlingActiveState.BREAKWATER) ? 1.6 : 1.0, Mat.MORTEM, STONE);
		}
		if (strain == MorphlingStrains.SUNBURST && MorphlingActiveState.has(flags, MorphlingActiveState.ARBOR)) {
			groundRoots(view, feet, seed, 8, 1.5, Mat.LUX, GOLD_DEEP);
		}
		if (strain == MorphlingStrains.MARROWPLUME && MorphlingActiveState.has(flags, MorphlingActiveState.WHALE_FALL_ROOTED)) {
			groundRoots(view, feet, seed, 7, 1.2, Mat.MORTEM, MARROW_RED);
		}
		if (strain == MorphlingStrains.INK_CAP && MorphlingActiveState.has(flags, MorphlingActiveState.DELIQUESCE) && !self) {
			dissolve(view, player, feet, seed);
		}
		if (strain == MorphlingStrains.HUSHBELL && MorphlingActiveState.has(flags, MorphlingActiveState.FOLD_SHUT
				| MorphlingActiveState.CRYPTOBIOSIS)) {
			float breath = 0.6F + 0.4F * (float) Math.sin(view.time * 0.05);
			for (int i = 0; i < 6; i++) {
				double a = i * Math.PI / 3 + view.time * 0.004;
				Vec3 at = feet.add(Math.cos(a) * 0.55, 0.15 + 0.05 * Math.sin(view.time * 0.05 + i), Math.sin(a) * 0.55);
				OutpouringBrushes.card(view.p, OutpouringBrushes.flow(Mat.LUX), view.local(at), view.right, view.up,
						0.35, 0.22, OutpouringBrushes.WISP, seed + i, FROST, 0.45F * breath);
			}
		}
	}

	/**
	 * A Dead Man's Rope strand: a sagging translucent violet line (wider while reeling) with beads along it and a
	 * bright pulse that runs from the host to the far end, as light-making bacteria stream along a slime-mould vein.
	 */
	private static void strand(View view, Vec3 from, Vec3 to, int seed, boolean reeling, boolean self) {
		double length = from.distanceTo(to);
		if (length < 0.2) return;
		int segments = Mth.clamp((int) (length * 4), 6, 64);
		List<Vec3> points = new ArrayList<>(segments + 1);
		for (int i = 0; i <= segments; i++) {
			double t = i / (double) segments;
			double sag = Math.sin(t * Math.PI) * Math.min(0.9, length * 0.06) * (reeling ? 0.25 : 1.0);
			double drift = Math.sin(view.time * 0.07 + t * 7 + seed) * 0.04 * Math.sin(t * Math.PI);
			points.add(view.local(from.lerp(to, t).add(drift, -sag, drift * 0.6)));
		}
		if (self) points = points.subList(Math.min(points.size() - 2, segments / 8 + 1), points.size());
		double width = reeling ? 0.1 : 0.07;
		OutpouringBrushes.ribbonPath(view.p, OutpouringBrushes.flow(Mat.LUX), points, width * 3.0, width * 2.4,
				OutpouringBrushes.WISP, seed, VIOLET, 0.4F);
		OutpouringBrushes.ribbonPath(view.p, OutpouringBrushes.flow(Mat.DUCTILIS), points, width * 0.7, width * 0.55,
				OutpouringBrushes.RIBBON, seed + 2, VIOLET, 0.85F);
		OutpouringBrushes.ribbonPath(view.p, OutpouringBrushes.flow(Mat.LUX), points, width, width * 0.8,
				OutpouringBrushes.RIBBON, seed + 1, VIOLET_CORE, 0.9F);
		int beads = Math.max(2, (int) (length * 1.6));
		for (int b = 1; b < beads; b++) {
			Vec3 at = points.get(Math.min(points.size() - 1, b * (points.size() - 1) / beads));
			OutpouringBrushes.card(view.p, OutpouringBrushes.flow(Mat.LUX), at, view.right, view.up, 0.14, 0.14,
					OutpouringBrushes.FOCUS, seed + b, VIOLET, 0.9F);
		}
		double pulse = (view.time * 0.045 + (seed & 7) * 0.13) % 1.0;
		Vec3 glow = points.get((int) Math.round(pulse * (points.size() - 1)));
		OutpouringBrushes.card(view.p, OutpouringBrushes.flow(Mat.LUX), glow, view.right, view.up, 0.4, 0.4,
				OutpouringBrushes.FOCUS, seed + 99, VIOLET_CORE, 1.0F);
	}

	/**
	 * The Woven Current's members tied in to each other: each reaches a strand to the nearest other member within
	 * {@link #COURT_LINK_RANGE}, and a pair shares one strand. A Lamp-colony lets go of the network while it speaks.
	 */
	private static void courtLinks(View view, List<CourtMemberEntity> members) {
		if (members.size() < 2) return;
		java.util.Set<Long> drawn = new java.util.HashSet<>();
		for (CourtMemberEntity member : members) {
			if (letGo(member, view.partial)) continue;
			CourtMemberEntity nearest = null;
			double best = COURT_LINK_RANGE * COURT_LINK_RANGE;
			for (CourtMemberEntity other : members) {
				if (other == member || letGo(other, view.partial)) continue;
				double d = other.distanceToSqr(member);
				if (d < best) {
					best = d;
					nearest = other;
				}
			}
			if (nearest == null) continue;
			int a = Math.min(member.getId(), nearest.getId()), b = Math.max(member.getId(), nearest.getId());
			if (!drawn.add(((long) a << 32) | (b & 0xFFFFFFFFL))) continue;
			Vec3 from = member.getPosition(view.partial).add(0, member.getBbHeight() * CHEST, 0);
			Vec3 to = nearest.getPosition(view.partial).add(0, nearest.getBbHeight() * CHEST, 0);
			strand(view, from, to, a * 31 + b, false, false);
		}
	}

	private static boolean letGo(CourtMemberEntity member, float partial) {
		return member.talkAmount(partial) > 0.5F || member.isWithdrawn();
	}

	/** Where a strand holds a block: a small knot of strands and a soft glow. */
	private static void knot(View view, Vec3 at, int seed) {
		Vec3 local = view.local(at);
		OutpouringBrushes.card(view.p, OutpouringBrushes.flow(Mat.LUX), local, view.right, view.up, 0.42, 0.42,
				OutpouringBrushes.POOL, seed, VIOLET, 0.35F);
		for (int i = 0; i < 4; i++) {
			double a = i * Math.PI / 2 + seed;
			List<Vec3> tendril = List.of(local, local.add(Math.cos(a) * 0.22, -0.12, Math.sin(a) * 0.22),
					local.add(Math.cos(a) * 0.32, -0.3, Math.sin(a) * 0.32));
			OutpouringBrushes.ribbonPath(view.p, OutpouringBrushes.flow(Mat.LUX), tendril, 0.035, 0.01,
					OutpouringBrushes.RIBBON, seed + i, VIOLET, 0.6F);
		}
	}

	/** Dead Man's Rope at Primal: a train of beaded strands several blocks long, laid along where the host walked. */
	private static void train(View view, Player player, int seed) {
		List<Vec3> history = new ArrayList<>(MorphlingStrainTrails.history(player));
		Vec3 now = player.getPosition(view.partial);
		if (history.size() < 3 || history.get(history.size() - 1).distanceTo(now) < 2.5) {
			// Standing still the train lies out behind the host, three blocks along the ground.
			double yaw = Math.toRadians(player.getPreciseBodyRotation(view.partial));
			Vec3 back = new Vec3(Math.sin(yaw), 0, -Math.cos(yaw));
			history.clear();
			for (int i = 0; i < 30; i++) history.add(now.add(back.scale(0.12 * i)));
		}
		for (int s = 0; s < 3; s++) {
			List<Vec3> points = new ArrayList<>();
			double side = (s - 1) * 0.16;
			for (int i = 0; i < history.size(); i += 2) {
				Vec3 at = i == 0 ? now : history.get(i);
				double sway = Math.sin(view.time * 0.06 + i * 0.35 + s * 2.1) * 0.08;
				points.add(view.local(at.add(side + sway, 0.55 - Math.min(0.5, i * 0.02), sway * 0.5)));
			}
			if (points.size() < 2) continue;
			OutpouringBrushes.ribbonPath(view.p, OutpouringBrushes.flow(Mat.LUX), points, 0.16, 0.04,
					OutpouringBrushes.WISP, seed + s + 5, VIOLET, 0.4F);
			OutpouringBrushes.ribbonPath(view.p, OutpouringBrushes.flow(Mat.LUX), points, 0.08, 0.02,
					OutpouringBrushes.RIBBON, seed + s, VIOLET_CORE, 0.8F);
			for (int b = 2; b < points.size(); b += 3) {
				OutpouringBrushes.card(view.p, OutpouringBrushes.flow(Mat.LUX), points.get(b), view.right, view.up, 0.12, 0.12,
						OutpouringBrushes.FOCUS, seed + b, VIOLET_CORE, 0.85F * (1 - b / (float) points.size()));
			}
		}
	}

	/** Mermaid's Purse sprint-swimming with the sail up: a short cobalt wake behind the host. */
	private static void wake(View view, Player player, int seed) {
		List<Vec3> history = MorphlingStrainTrails.history(player);
		if (history.size() < 3) return;
		List<Vec3> points = new ArrayList<>();
		Vec3 now = player.getPosition(view.partial);
		for (int i = 0; i < Math.min(history.size(), 16); i++) {
			points.add(view.local((i == 0 ? now : history.get(i)).add(0, player.getBbHeight() * 0.4, 0)));
		}
		OutpouringBrushes.ribbonPath(view.p, OutpouringBrushes.flow(Mat.LUX), points, 0.3, 0.02,
				OutpouringBrushes.WISP, seed, COBALT, 0.35F);
	}

	/** Strands, roots or stalks spreading from the feet into the ground, rising in as the state settles. */
	private static void groundRoots(View view, Vec3 feet, int seed, int count, double reach, Mat material, int colour) {
		for (int i = 0; i < count; i++) {
			double angle = i * Math.PI * 2 / count + (seed % 13) * 0.2;
			double len = reach * (0.7 + 0.3 * Math.sin(seed + i * 1.7));
			List<Vec3> path = new ArrayList<>();
			for (int k = 0; k <= 5; k++) {
				double t = k / 5.0;
				double r = 0.18 + len * t;
				double wobble = Math.sin(i * 2.3 + t * 5 + view.time * 0.02) * 0.06;
				path.add(view.local(feet.add(Math.cos(angle + wobble) * r, 0.04 - t * t * 0.18, Math.sin(angle + wobble) * r)));
			}
			OutpouringBrushes.ribbonPath(view.p, OutpouringBrushes.flow(material), path, 0.07, 0.015,
					OutpouringBrushes.RIBBON, seed + i, colour, 0.85F);
		}
		OutpouringBrushes.ground(view.p, OutpouringBrushes.flow(material), view.local(feet.add(0, 0.02, 0)), reach * 0.7,
				OutpouringBrushes.POOL, seed, colour, 0.25F);
	}

	/** Ink Cap's Deliquesce: the host thins into black ink, cards peeling off and sinking. */
	private static void dissolve(View view, Player player, Vec3 feet, int seed) {
		double height = player.getBbHeight();
		for (int i = 0; i < 10; i++) {
			double cycle = (view.time * 0.03 + i * 0.1) % 1.0;
			double angle = i * 2.4 + seed;
			Vec3 at = feet.add(Math.cos(angle) * 0.35, height * (0.9 - cycle * 0.85), Math.sin(angle) * 0.35);
			OutpouringBrushes.card(view.p, OutpouringBrushes.flow(Mat.UMBRA), view.local(at), view.right, view.up,
					0.32 + cycle * 0.2, 0.42 + cycle * 0.3, OutpouringBrushes.WISP, seed + i, 0xFFFFFF, (float) (0.65 * (1 - cycle)));
		}
		OutpouringBrushes.ground(view.p, OutpouringBrushes.flow(Mat.UMBRA), view.local(feet.add(0, 0.02, 0)), 0.9,
				OutpouringBrushes.POOL, seed, 0xFFFFFF, 0.6F);
	}

	/**
	 * The field patches: Sunburst's Bloom, a gold mat floating just above the ground with fronds turning on it; and
	 * Marrowplume's Whale Fall, a pale bone stain beaded red with plumes swaying out of it. Both fade in and out.
	 */
	private static void field(View view, MorphlingFieldEntity field) {
		double age = AnimationTime.age(field, (double) view.partial);
		int life = Math.max(1, field.lifetime());
		float fade = (float) Math.min(Math.min(1.0, age / 10.0), Math.max(0.0, (life - age) / 20.0));
		if (fade <= 0.01F) return;
		Vec3 centre = field.getPosition(view.partial);
		double radius = field.radius();
		int seed = field.getId() * 17;
		if (field.kind() == MorphlingFieldEntity.Kind.BLOOM) {
			double bob = Math.sin(view.time * 0.05) * 0.03;
			OutpouringBrushes.ground(view.p, OutpouringBrushes.flow(Mat.LUX), view.local(centre.add(0, 0.12 + bob, 0)),
					radius, OutpouringBrushes.POOL, seed, GOLD, 0.75F * fade);
			OutpouringBrushes.ground(view.p, OutpouringBrushes.flow(Mat.LUX), view.local(centre.add(0, 0.14 + bob, 0)),
					radius * 0.55, OutpouringBrushes.RIPPLE, seed + 1, GOLD_DEEP, 0.8F * fade);
			int fronds = (int) (radius * 5);
			for (int i = 0; i < fronds; i++) {
				double a = i * 2.39996 + seed;
				double r = radius * Math.sqrt((i + 0.5) / fronds);
				Vec3 root = centre.add(Math.cos(a) * r, 0.15 + bob, Math.sin(a) * r);
				double open = 0.5 + 0.5 * Math.sin(view.time * 0.04 + i);
				List<Vec3> frond = List.of(view.local(root), view.local(root.add(Math.cos(a) * 0.12 * open, 0.18, Math.sin(a) * 0.12 * open)),
						view.local(root.add(Math.cos(a) * 0.3 * open, 0.3, Math.sin(a) * 0.3 * open)));
				OutpouringBrushes.ribbonPath(view.p, OutpouringBrushes.flow(Mat.LUX), frond, 0.24, 0.1,
						OutpouringBrushes.WISP, seed + i, GOLD, fade);
				OutpouringBrushes.ribbonPath(view.p, OutpouringBrushes.flow(Mat.LUX), frond, 0.06, 0.03,
						OutpouringBrushes.RIBBON, seed + i + 50, GOLD_DEEP, fade);
			}
		} else {
			OutpouringBrushes.ground(view.p, OutpouringBrushes.flow(Mat.MORTEM), view.local(centre.add(0, 0.03, 0)),
					radius, OutpouringBrushes.POOL, seed, BONE, 0.55F * fade);
			OutpouringBrushes.ground(view.p, OutpouringBrushes.flow(Mat.MORTEM), view.local(centre.add(0, 0.04, 0)),
					radius * 0.6, OutpouringBrushes.RIPPLE, seed + 1, MARROW_RED, 0.5F * fade);
			int plumes = (int) (radius * 3);
			for (int i = 0; i < plumes; i++) {
				double a = i * 2.39996 + seed;
				double r = radius * 0.9 * Math.sqrt((i + 0.5) / plumes);
				Vec3 root = centre.add(Math.cos(a) * r, 0.03, Math.sin(a) * r);
				double sway = Math.sin(view.time * 0.05 + i * 1.3) * 0.12;
				double h = 0.5 + 0.25 * Math.sin(i * 3.7);
				List<Vec3> stalk = List.of(view.local(root), view.local(root.add(sway * 0.4, h * 0.5, sway * 0.2)),
						view.local(root.add(sway, h, sway * 0.5)));
				OutpouringBrushes.ribbonPath(view.p, OutpouringBrushes.flow(Mat.MORTEM), stalk, 0.05, 0.03,
						OutpouringBrushes.RIBBON, seed + i, MARROW_RED, 0.9F * fade);
				OutpouringBrushes.card(view.p, OutpouringBrushes.flow(Mat.MORTEM), stalk.get(2), view.right, view.up, 0.22, 0.22,
						OutpouringBrushes.WISP, seed + i, BONE, 0.7F * fade);
			}
		}
	}
}
