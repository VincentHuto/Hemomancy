package com.vincenthuto.hemomancy.client.render.world.circus;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.npc.CircusAcrobatModel;
import com.vincenthuto.hemomancy.client.model.entity.npc.CircusFireEaterModel;
import com.vincenthuto.hemomancy.client.model.entity.npc.CircusKnifeThrowerModel;
import com.vincenthuto.hemomancy.client.model.entity.npc.CircusThreadkeeperModel;
import com.vincenthuto.hemomancy.client.model.entity.npc.CircusUnderstudyModel;
import com.vincenthuto.hemomancy.client.render.world.circus.PhantomShowRules.Look;
import com.vincenthuto.hemomancy.client.render.world.circus.ShowMarks.Seat;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * The phantom audience and performers: the troupe's own jointed rigs, posed straight onto their humanoid parts with no
 * entity behind them, drawn translucent in bone with a crimson rim. A phantom is never opaque, fades as the viewer
 * walks up to it, and fades while it would cover a living creature; a Disturbed one hides from the crosshair.
 */
public final class PhantomFigures {
	private static final int BONE = 0xDCD8D2;
	private static final int RIM = 0x9A143E;
	private static final float PAINT = 0.3F;
	/** A show's act lasts four beats; applause follows each act's end. */
	public static final float ACT = StringLights.BEAT * 4.0F;
	private static final float APPLAUSE = 24.0F;
	private static final float EPOCH = 400.0F;
	private static final float GLIMPSE = 30.0F;
	private static final float FRAGMENT_EPOCH = 240.0F;
	private static final double ROPE = 2.0D;
	private static final double HANG = 2.05D;
	private static final float FADE_PER_TICK = 1.0F / 6.0F;
	private static final double CUE_RANGE = 28.0D;
	private static final int AUDIENCE_CAP = 60;
	private static final Map<Long, Float> overlapFades = new HashMap<>();
	private static EntityModelSet bakedFrom;
	private static Map<Body, HumanoidModel<?>> models = Map.of();
	private static float lastTime;

	private PhantomFigures() {
	}

	/** The rigs a phantom can wear, with the texture each was painted for. */
	public enum Body {
		UNDERSTUDY(CircusUnderstudyModel.LAYER_LOCATION, CircusUnderstudyModel::new, "circus_understudy"),
		THREADKEEPER(CircusThreadkeeperModel.LAYER_LOCATION, CircusThreadkeeperModel::new, "circus_threadkeeper"),
		ACROBAT(CircusAcrobatModel.LAYER_LOCATION, CircusAcrobatModel::new, "acrobat"),
		KNIFE_THROWER(CircusKnifeThrowerModel.LAYER_LOCATION, CircusKnifeThrowerModel::new, "knife_thrower"),
		FIRE_EATER(CircusFireEaterModel.LAYER_LOCATION, CircusFireEaterModel::new, "fire_eater");

		private final ModelLayerLocation layer;
		private final Function<ModelPart, HumanoidModel<?>> factory;
		private final ResourceLocation texture;

		Body(ModelLayerLocation layer, Function<ModelPart, HumanoidModel<?>> factory, String texture) {
			this.layer = layer;
			this.factory = factory;
			this.texture = Hemomancy.rloc("textures/entity/npc/harbinger/circus/" + texture + "_0.png");
		}
	}

	/**
	 * The phantom materials, built once on first draw (a holder, so the planning above loads without the renderer): a
	 * flat bone silhouette, with only a breath of each costume's paint showing through.
	 */
	private static final class Materials {
		static final RenderType SILHOUETTE = RenderType.entityTranslucentEmissive(
				ResourceLocation.withDefaultNamespace("textures/misc/white.png"));
		static final Map<Body, RenderType> PAINTED = new EnumMap<>(Body.class);

		static {
			for (Body body : Body.values()) PAINTED.put(body, RenderType.entityTranslucentEmissive(body.texture));
		}
	}

	/** How a figure hangs: on the ground, or from a trapeze pivot swinging along its facing. */
	public enum Kind {
		SEATED, STANDING, SWINGING
	}

	/**
	 * One phantom this frame: its rig, where its feet are (or its trapeze pivot, swinging), the way it faces (Minecraft
	 * yaw degrees), its pose, its seed, its alpha before the per-frame fades, and its swing angle.
	 */
	public record Figure(Body body, Kind kind, Vec3 at, float yaw, PhantomPose pose, long seed, float alpha,
			double swing, boolean hidesFromGaze) {
		/** About where the figure's chest is, for the fades and the spotlights. */
		public Vec3 centre() {
			if (kind != Kind.SWINGING) return at.add(0.0D, kind == Kind.SEATED ? 0.4D : 1.0D, 0.0D);
			double rad = Math.toRadians(yaw);
			double out = Math.sin(swing) * (ROPE + 1.0D);
			return at.add(-Math.sin(rad) * out, -Math.cos(swing) * (ROPE + 1.0D), Math.cos(rad) * out);
		}
	}

	/** The most seated phantoms a look draws at once: the nearest seats in the cue range fill first. */
	public static int cap(Look look) {
		return look.chase() ? AUDIENCE_CAP : AUDIENCE_CAP * 2 / 5;
	}

	/**
	 * Who sits and who performs this frame, every alpha scaled by {@code weight} (a stage change draws the old cast
	 * fading out and the new one fading in). Pure apart from reading each trapeze's facing through {@code states}.
	 */
	public static List<Figure> plan(ShowMarks marks, Look look, float time, Vec3 viewer,
			Function<BlockPos, BlockState> states, float weight) {
		List<Figure> cast = new ArrayList<>();
		float base = look.phantomAlpha() * Math.clamp(weight, 0.0F, 1.0F);
		if (base <= 0.0F) return cast;
		look = new Look(look.audienceShare(), look.glimpses(), look.performers(), look.bulbLit(), look.flicker(),
				look.chase(), look.spotlights(), look.house(), look.vignette(), base, look.peripheralOnly());
		boolean attuned = look.chase();
		float applause = attuned ? envelope(time % ACT, APPLAUSE, 6.0F) : 0.0F;
		List<Seat> seats = new ArrayList<>();
		for (Seat seat : marks.seats())
			if (Vec3.atBottomCenterOf(seat.pos()).distanceTo(viewer) <= CUE_RANGE) seats.add(seat);
		int sat = 0;
		int cap = cap(look);
		for (Seat seat : seats) {
			if (sat >= cap) break;
			long seed = seat.pos().asLong();
			float presence = presence(PhantomShowRules.unit(seed), look.audienceShare());
			if (presence <= 0.0F) continue;
			cast.add(seated(seat, seed, look, time, viewer, marks, applause, look.phantomAlpha() * presence, false));
			sat++;
		}
		for (int k = 0; k < look.glimpses() && !seats.isEmpty(); k++) {
			float shifted = time + k * 137.0F;
			long epoch = (long) Math.floor(shifted / FRAGMENT_EPOCH);
			float within = shifted - epoch * FRAGMENT_EPOCH;
			float envelope = envelope(within, FRAGMENT_EPOCH, 40.0F);
			if (envelope <= 0.0F) continue;
			Seat seat = seats.get((int) Math.floorMod(epoch * 31L + k * 7919L, (long) seats.size()));
			cast.add(seated(seat, seat.pos().asLong() ^ k, look, time, viewer, marks, 0.0F,
					look.phantomAlpha() * envelope, true));
		}
		if (look.performers() <= 0) return cast;
		List<BlockPos> trapezes = marks.trapezes();
		long epoch = (long) Math.floor(time / EPOCH);
		float within = time - epoch * EPOCH;
		if (attuned) {
			for (int i = 0; i < Math.min(look.performers(), trapezes.size()); i++)
				cast.add(swinger(trapezes.get(i), states, time, look.phantomAlpha(), false, true));
			if (marks.hasRing()) {
				int act = (int) Math.floorMod((long) Math.floor(time / ACT), 3L);
				float onStage = envelope(time % ACT, ACT, 30.0F);
				if (onStage > 0.0F) cast.add(ringAct(marks, act, time, look.phantomAlpha() * onStage, false));
			}
		} else if (look.peripheralOnly()) {
			float envelope = envelope(within, GLIMPSE, 6.0F);
			if (within < GLIMPSE && envelope > 0.0F) {
				boolean onTrapeze = (epoch & 1L) == 0L && !trapezes.isEmpty();
				if (onTrapeze) cast.add(swinger(trapezes.get(0), states, time, look.phantomAlpha() * envelope, true, false));
				else if (marks.hasRing()) cast.add(ringAct(marks, -1, time, look.phantomAlpha() * envelope, true));
			}
		} else {
			float alpha = look.phantomAlpha() * 0.75F * envelope(within, EPOCH, 30.0F);
			boolean onTrapeze = (epoch & 1L) == 0L && !trapezes.isEmpty();
			if (onTrapeze) cast.add(swinger(trapezes.get(0), states, time, alpha, false, true));
			else if (marks.hasRing()) cast.add(ringAct(marks, (int) Math.floorMod(epoch / 2L, 3L), time, alpha, false));
		}
		return cast;
	}

	/** The finale's audience: the dissolved seating's rows round {@code centre}, filled in the Ringmaster's dark. */
	private static final double[] FINALE_ROWS = {18.0D, 21.5D, 25.0D};
	private static final int FINALE_SEATS_PER_ROW = 26;

	/**
	 * Who sits in the Ringmaster's dark this frame (2026-11-18): rows round the copy's ring where its seating was, each
	 * row a step higher, most seats taken, everyone watching the ring and clapping on the act's beat. Pure.
	 */
	public static List<Figure> planFinale(Vec3 centre, float time, float alpha) {
		List<Figure> cast = new ArrayList<>();
		float applause = envelope(time % ACT, APPLAUSE, 6.0F);
		for (int row = 0; row < FINALE_ROWS.length; row++) {
			for (int i = 0; i < FINALE_SEATS_PER_ROW; i++) {
				long seed = row * 7919L + i * 104729L + 0x5DEECE66DL;
				if (PhantomShowRules.unit(seed) > 0.8F) continue;
				double angle = Math.PI * 2.0D * (i + 0.5D * row) / FINALE_SEATS_PER_ROW;
				Vec3 at = centre.add(Math.cos(angle) * FINALE_ROWS[row], 1.0D + row * 1.25D + 9.5D / 16.0D,
						Math.sin(angle) * FINALE_ROWS[row]);
				float yaw = (float) Math.toDegrees(Math.atan2(-(centre.x - at.x), centre.z - at.z));
				cast.add(new Figure(audienceBody(seed), Kind.SEATED, at, yaw, PhantomPose.seated(seed, time, 0.0F, applause),
						seed, alpha, 0.0D, false));
			}
		}
		return cast;
	}

	private static Figure seated(Seat seat, long seed, Look look, float time, Vec3 viewer, ShowMarks marks,
			float applause, float alpha, boolean hides) {
		Vec3 base = Vec3.atBottomCenterOf(seat.pos());
		float lookYaw = 0.0F;
		if (look.chase() && marks.hasRing()) lookYaw = relativeYaw(seat.yaw(), base, marks.ring().centre());
		else if (!look.peripheralOnly()) lookYaw = relativeYaw(seat.yaw(), base, viewer);
		Body body = audienceBody(seed);
		if (seat.deck())
			return new Figure(body, Kind.STANDING, base, seat.yaw(), leaning(seed, time, lookYaw, applause), seed, alpha,
					0.0D, hides);
		return new Figure(body, Kind.SEATED, base.add(0.0D, 9.5D / 16.0D, 0.0D), seat.yaw(),
				PhantomPose.seated(seed, time, lookYaw, applause), seed, alpha, 0.0D, hides);
	}

	/** A gallery phantom stands at the rail with its forearms on it. */
	private static PhantomPose leaning(long seed, float time, float lookYaw, float applause) {
		PhantomPose sat = PhantomPose.seated(seed, time, lookYaw, applause);
		return new PhantomPose(sat.headYaw(), sat.headPitch(), sat.bodyLean() * 0.6F,
				HemoEasing.lerp(-1.2F, sat.rightArmX(), applause), HemoEasing.lerp(-1.15F, sat.leftArmX(), applause),
				sat.rightArmZ(), sat.leftArmZ(), 0.0F);
	}

	private static Figure swinger(BlockPos trapeze, Function<BlockPos, BlockState> states, float time, float alpha,
			boolean still, boolean hasOwnSeed) {
		BlockState state = states.apply(trapeze);
		Direction facing = state.hasProperty(HorizontalDirectionalBlock.FACING)
				? state.getValue(HorizontalDirectionalBlock.FACING) : Direction.NORTH;
		long seed = trapeze.asLong();
		double swing = still ? 0.0D : PhantomPose.trapezeSwing(time, seed);
		PhantomPose pose = PhantomPose.swinging(still ? 0.0F : PhantomPose.swingPhase(time, seed));
		Vec3 pivot = Vec3.atBottomCenterOf(trapeze).add(0.0D, 3.5D / 16.0D + ROPE, 0.0D);
		return new Figure(Body.ACROBAT, Kind.SWINGING, pivot, facing.toYRot(), pose, seed, alpha, swing, still);
	}

	private static Figure ringAct(ShowMarks marks, int act, float time, float alpha, boolean glimpse) {
		var ring = marks.ring();
		double theta = time * 0.004D + Math.max(act, 0) * 2.1D;
		double dx = Math.cos(theta), dz = Math.sin(theta);
		Vec3 at = ring.centre().add(dx * ring.radius() * 0.55D, 0.0D, dz * ring.radius() * 0.55D);
		float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
		Body body = switch (Math.floorMod(act, 3)) {
			case 0 -> Body.ACROBAT;
			case 1 -> Body.KNIFE_THROWER;
			default -> Body.FIRE_EATER;
		};
		PhantomPose pose = act < 0 ? PhantomPose.STANDING : PhantomPose.ringAct(act, time);
		return new Figure(act < 0 ? Body.UNDERSTUDY : body, Kind.STANDING, at, yaw, pose, (long) act * 977L, alpha,
				0.0D, glimpse);
	}

	private static Body audienceBody(long seed) {
		float pick = PhantomShowRules.unit(seed ^ 0x2545F4914F6CDD1DL);
		if (pick < 0.6F) return Body.UNDERSTUDY;
		if (pick < 0.75F) return Body.THREADKEEPER;
		if (pick < 0.88F) return Body.ACROBAT;
		return Body.KNIFE_THROWER;
	}

	/** The turn, radians, from a figure facing {@code yaw} (Minecraft degrees) at {@code from} toward {@code to}. */
	private static float relativeYaw(float yaw, Vec3 from, Vec3 to) {
		double toward = Math.toDegrees(Math.atan2(-(to.x - from.x), to.z - from.z));
		return (float) Math.toRadians(net.minecraft.util.Mth.wrapDegrees(toward - yaw));
	}

	/** 0 to 1 and back over a window, easing {@code edge} ticks at each end. */
	private static float envelope(float within, float length, float edge) {
		if (within < 0.0F || within > length) return 0.0F;
		return Math.min(1.0F, Math.min(within / edge, (length - within) / edge));
	}

	/** Draws the cast, camera-relative, fading each by the viewer's gaze, nearness and the creatures in front. */
	public static void render(PoseStack poseStack, MultiBufferSource buffers, Vec3 camera, Vector3f look,
			List<Figure> cast, List<AABB> creatures, float time) {
		render(poseStack, buffers, camera, camera, look, cast, creatures, time);
	}

	/**
	 * As {@link #render}, under a pose stack already translated to {@code origin} (an entity renderer's), so each figure
	 * is placed from {@code origin} while the fades still read the camera.
	 */
	public static void render(PoseStack poseStack, MultiBufferSource buffers, Vec3 origin, Vec3 camera, Vector3f look,
			List<Figure> cast, List<AABB> creatures, float time) {
		Minecraft minecraft = Minecraft.getInstance();
		if (bakedFrom != minecraft.getEntityModels()) bake(minecraft.getEntityModels());
		float elapsed = Math.clamp(time - lastTime, 0.0F, 4.0F);
		lastTime = time;
		List<Figure> sorted = new ArrayList<>(cast);
		sorted.sort(java.util.Comparator.comparingInt(figure -> figure.body().ordinal()));
		for (Figure figure : sorted) {
			Vec3 centre = figure.centre();
			float alpha = figure.alpha() * PhantomShowRules.nearFade(centre.distanceTo(camera));
			if (figure.hidesFromGaze()) {
				Vec3 to = centre.subtract(camera).normalize();
				double cos = to.x * look.x() + to.y * look.y() + to.z * look.z();
				alpha *= PhantomShowRules.peripheralFade((float) Math.toDegrees(Math.acos(Math.clamp(cos, -1.0D, 1.0D))));
			}
			alpha *= overlapFade(figure.seed() ^ figure.body().ordinal(), covers(camera, centre, creatures), elapsed);
			if (alpha <= 0.01F) continue;
			draw(poseStack, buffers, origin, figure, alpha);
		}
	}

	private static boolean covers(Vec3 camera, Vec3 centre, List<AABB> creatures) {
		for (AABB box : creatures) if (PhantomShowRules.covers(camera, centre, box)) return true;
		return false;
	}

	private static float overlapFade(long key, boolean covering, float elapsed) {
		float current = overlapFades.getOrDefault(key, 1.0F);
		float next = Math.clamp(current + (covering ? -1.0F : 1.0F) * FADE_PER_TICK * elapsed, 0.0F, 1.0F);
		if (next >= 1.0F) overlapFades.remove(key);
		else overlapFades.put(key, next);
		return next;
	}

	public static void clear() {
		overlapFades.clear();
	}

	private static void draw(PoseStack poseStack, MultiBufferSource buffers, Vec3 origin, Figure figure, float alpha) {
		HumanoidModel<?> model = models.get(figure.body());
		if (model == null) return;
		pose(model, figure.pose());
		poseStack.pushPose();
		poseStack.translate(figure.at().x - origin.x, figure.at().y - origin.y, figure.at().z - origin.z);
		poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - figure.yaw()));
		if (figure.kind() == Kind.SWINGING) {
			poseStack.mulPose(Axis.XP.rotation((float) figure.swing()));
			poseStack.translate(0.0D, -ROPE - HANG, 0.0D);
		} else if (figure.kind() == Kind.SEATED) {
			poseStack.translate(0.0D, -(24.0F - model.rightLeg.getInitialPose().y) / 16.0F, 0.0D);
		}
		poseStack.scale(-1.0F, -1.0F, 1.0F);
		poseStack.translate(0.0F, -1.501F, 0.0F);
		int a = Math.clamp((int) (alpha * 255.0F), 0, 255);
		model.renderToBuffer(poseStack, buffers.getBuffer(Materials.SILHOUETTE), LightTexture.FULL_BRIGHT,
				OverlayTexture.NO_OVERLAY, a << 24 | BONE);
		model.renderToBuffer(poseStack, buffers.getBuffer(Materials.PAINTED.get(figure.body())), LightTexture.FULL_BRIGHT,
				OverlayTexture.NO_OVERLAY, (int) (a * PAINT) << 24 | BONE);
		poseStack.translate(0.0F, 0.75F, 0.0F);
		poseStack.scale(1.06F, 1.03F, 1.06F);
		poseStack.translate(0.0F, -0.75F, 0.0F);
		model.renderToBuffer(poseStack, buffers.getBuffer(Materials.SILHOUETTE), LightTexture.FULL_BRIGHT,
				OverlayTexture.NO_OVERLAY, (a / 10) << 24 | RIM);
		poseStack.popPose();
	}

	private static void pose(HumanoidModel<?> model, PhantomPose pose) {
		model.head.resetPose();
		model.hat.resetPose();
		model.body.resetPose();
		model.rightArm.resetPose();
		model.leftArm.resetPose();
		model.rightLeg.resetPose();
		model.leftLeg.resetPose();
		model.head.yRot = pose.headYaw();
		model.head.xRot = pose.headPitch();
		model.hat.copyFrom(model.head);
		model.body.xRot = pose.bodyLean();
		model.rightArm.xRot = pose.rightArmX();
		model.leftArm.xRot = pose.leftArmX();
		model.rightArm.zRot = pose.rightArmZ();
		model.leftArm.zRot = pose.leftArmZ();
		boolean sitting = pose.legX() < -1.0F;
		model.rightLeg.xRot = pose.legX();
		model.leftLeg.xRot = sitting ? pose.legX() : -pose.legX();
		if (sitting) {
			model.rightLeg.yRot = 0.3142F;
			model.rightLeg.zRot = 0.07F;
			model.leftLeg.yRot = -0.3142F;
			model.leftLeg.zRot = -0.07F;
		}
	}

	private static void bake(EntityModelSet set) {
		Map<Body, HumanoidModel<?>> baked = new EnumMap<>(Body.class);
		for (Body body : Body.values()) baked.put(body, body.factory.apply(set.bakeLayer(body.layer)));
		models = baked;
		bakedFrom = set;
	}

	/** How far a seat's own value sits inside the audience share, so a growing share fills seats in softly. */
	private static float presence(float seatValue, float share) {
		if (share <= 0.0F) return 0.0F;
		return Math.clamp((share - seatValue) / 0.06F, 0.0F, 1.0F);
	}
}
