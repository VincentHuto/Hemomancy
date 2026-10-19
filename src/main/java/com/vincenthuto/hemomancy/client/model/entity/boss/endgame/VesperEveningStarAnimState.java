package com.vincenthuto.hemomancy.client.model.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperTheEveningStarEntity;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperWeaponAction;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Client-only motion memory for one Evening Star. The model is shared between entities and posed from synced state
 * that lands in uneven bursts, so the continuity lives here: replayed action and stance clocks, a distance-driven
 * gait that knows which way he is stepping, the floor under each boot, a smoothed render height that eases step-ups,
 * and the last rendered pose that every state change blends out of instead of snapping.
 */
public final class VesperEveningStarAnimState {
	private static final Map<VesperTheEveningStarEntity, VesperEveningStarAnimState> STATES = new WeakHashMap<>();

	/** Blocks travelled per full gait cycle (two steps) at the Evening Star's leg length. */
	private static final float STRIDE_BLOCKS = 2.2F;
	/** Below this speed (blocks per tick) the stride shrinks instead of the feet sliding. */
	private static final float FULL_STRIDE_SPEED = 0.07F;
	/** Faster than this is a dash or a shove, not walking: the cadence stops climbing. */
	private static final float MAX_GAIT_SPEED = 0.34F;
	private static final float BASE_POSE_TAU = 0.4F;
	private static final float TRANSITION_POSE_TAU = 1.7F;
	private static final float TRANSITION_TICKS = 6.0F;

	private final VesperAnimClock actionClock = new VesperAnimClock();
	private final VesperAnimClock stanceClock = new VesperAnimClock();

	private boolean started;
	private float lastAge;
	private int lastTick = Integer.MIN_VALUE;
	private float dt;

	private float actionTime;
	private float stanceTime;

	// Gait, eased so one lerp hitch never kicks the legs.
	private float targetRate;
	private float rate;
	private float phase;
	private float targetStride;
	private float stride;
	private float targetForward = 1.0F;
	private float targetLateral;
	private float forward = 1.0F;
	private float lateral;
	private float gaitWeight = 1.0F;

	// Terrain: ground under each boot (absolute Y), sampled a tick at a time and followed per frame.
	private final float[] groundTarget = new float[2];
	private final float[] ground = new float[2];
	private final float[] footX = { 3.1F, -3.1F };
	private final float[] footZ = new float[2];
	private float renderY;
	private float visualY;
	private int airTicks;
	private float groundWeight;

	// Pose inertia.
	private float[][] pose;
	private int signature = Integer.MIN_VALUE;
	private float transition;

	private VesperEveningStarAnimState() {
	}

	public static VesperEveningStarAnimState of(VesperTheEveningStarEntity entity) {
		return STATES.computeIfAbsent(entity, e -> new VesperEveningStarAnimState());
	}

	/** Advances everything to this frame. Safe to call more than once per frame: a zero step changes nothing. */
	public void update(VesperTheEveningStarEntity entity, float partial) {
		float age = entity.tickCount + partial;
		// The renderer and the model both update each frame; the second call must keep the frame's step, not zero it,
		// or the pose blend never moves off its first frame.
		if (started && age == lastAge) return;
		dt = started ? age - lastAge : 0.0F;
		// A paused game, a long unload or a reloaded entity: jump to the present instead of crawling there.
		boolean snap = !started || dt < 0.0F || dt > 6.0F;
		if (snap) dt = 0.0F;
		lastAge = age;
		int tick = entity.tickCount;
		VesperWeaponAction action = entity.getWeaponAction();
		actionTime = actionClock.sample(action.ordinal() * 4 + (entity.getActionVariant() & 3),
				entity.getActionTick(), tick, partial, 1);
		if (action != VesperWeaponAction.NONE) actionTime = Math.min(actionTime, action.durationTicks());
		stanceTime = stanceClock.sample(entity.getActiveTendency().ordinal(), entity.getStanceTick(), tick, partial, 1);
		renderY = (float) Mth.lerp(partial, entity.yOld, entity.getY());

		if (tick != lastTick) {
			lastTick = tick;
			airTicks = entity.onGround() ? 0 : airTicks + 1;
			sampleMotion(entity);
			sampleTerrain(entity);
		}
		if (snap) {
			rate = targetRate;
			stride = targetStride;
			forward = targetForward;
			lateral = targetLateral;
			System.arraycopy(groundTarget, 0, ground, 0, 2);
			visualY = renderY;
			groundWeight = groundTarget(entity);
			gaitWeight = gaitTarget(entity);
			started = true;
		}

		float gaitFollow = VesperMountMotionRules.follow(dt, 2.0F);
		rate = Mth.lerp(gaitFollow, rate, targetRate);
		stride = Mth.lerp(VesperMountMotionRules.follow(dt, targetStride > stride ? 1.6F : 3.0F), stride, targetStride);
		forward = Mth.lerp(gaitFollow, forward, targetForward);
		lateral = Mth.lerp(gaitFollow, lateral, targetLateral);
		gaitWeight = Mth.lerp(VesperMountMotionRules.follow(dt, 2.5F), gaitWeight, gaitTarget(entity));
		phase = frac(phase + rate * dt);

		float footFollow = VesperMountMotionRules.follow(dt, 1.5F);
		for (int i = 0; i < 2; i++) ground[i] = Mth.lerp(footFollow, ground[i], groundTarget[i]);
		groundWeight = Mth.lerp(VesperMountMotionRules.follow(dt, 2.5F), groundWeight, groundTarget(entity));

		// Vanilla steps a mob up a block in one tick and the client spreads it over three straight-line ticks; ease
		// the rendered height after it so a step up or down reads as a step, not a pop. Leaps and falls stay exact.
		float gap = renderY - visualY;
		if (airTicks > 2 || Math.abs(gap) > 1.6F) visualY = renderY;
		else visualY += gap * VesperMountMotionRules.follow(dt, 1.6F);

		int nextSignature = signature(entity, actionTime);
		if (nextSignature != signature) {
			if (signature != Integer.MIN_VALUE) transition = 1.0F;
			signature = nextSignature;
		}
		transition = Math.max(0.0F, transition - dt / TRANSITION_TICKS);
		if (snap) pose = null;
	}

	private void sampleMotion(VesperTheEveningStarEntity entity) {
		double vx = entity.getX() - entity.xo;
		double vz = entity.getZ() - entity.zo;
		float yaw = entity.yBodyRot * Mth.DEG_TO_RAD;
		float cos = Mth.cos(yaw);
		float sin = Mth.sin(yaw);
		// Into the model's frame: +x is the model's +x (his left), forward is -z (reported here as positive).
		float side = (float) (vx * cos + vz * sin);
		float ahead = (float) (-vx * sin + vz * cos);
		float speed = Mth.sqrt(side * side + ahead * ahead);
		// A teleport or a swap is not a step.
		if (speed > 1.5F) speed = 0.0F;
		float gaitSpeed = Math.min(speed, MAX_GAIT_SPEED);
		targetRate = gaitSpeed / STRIDE_BLOCKS;
		targetStride = Mth.clamp(gaitSpeed / FULL_STRIDE_SPEED, 0.0F, 1.0F);
		if (speed > 0.004F) {
			targetForward = ahead / speed;
			targetLateral = side / speed;
		}
	}

	/** Samples the floor under each boot, at the boot's last rendered position. */
	private void sampleTerrain(VesperTheEveningStarEntity entity) {
		Level level = entity.level();
		double baseY = entity.getY();
		float yaw = entity.yBodyRot * Mth.DEG_TO_RAD;
		float cos = Mth.cos(yaw);
		float sin = Mth.sin(yaw);
		float scale = renderScale(entity) / 16.0F;
		for (int i = 0; i < 2; i++) {
			// Model (x, z) -> entity-local (x, -z) -> world, turned by the body yaw.
			float lx = footX[i] * scale;
			float lz = -footZ[i] * scale;
			double wx = entity.getX() + lx * cos - lz * sin;
			double wz = entity.getZ() + lx * sin + lz * cos;
			groundTarget[i] = (float) (baseY + groundAt(level, wx, wz, baseY));
		}
	}

	private static float groundAt(Level level, double x, double z, double fromY) {
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		int top = Mth.floor(fromY + 1.2D);
		int bottom = Mth.floor(fromY - 2.0D);
		for (int y = top; y >= bottom; y--) {
			pos.set(Mth.floor(x), y, Mth.floor(z));
			BlockState state = level.getBlockState(pos);
			VoxelShape shape = state.getCollisionShape(level, pos);
			if (!shape.isEmpty()) {
				double surface = y + shape.max(Direction.Axis.Y);
				// Ignore a ceiling: only surfaces a boot could stand on from here count.
				if (surface <= fromY + 1.2D) return (float) (surface - fromY);
			}
		}
		return (float) (bottom - fromY);
	}

	private float groundTarget(VesperTheEveningStarEntity entity) {
		// A step off a ledge is a few ticks of falling; anything longer is a leap, and the legs are its to pose.
		if (entity.isAwakening() || entity.isAwaitingAbsorption() || airTicks > 3) return 0.0F;
		return VesperEveningStarPose.airbornePose(entity.getWeaponAction()) ? 0.0F : 1.0F;
	}

	private static float gaitTarget(VesperTheEveningStarEntity entity) {
		if (entity.isAwaitingAbsorption() || entity.isHoodRemovalActive() || entity.isAwakening()) return 0.0F;
		VesperWeaponAction action = entity.getWeaponAction();
		if (action == VesperWeaponAction.NONE) return 1.0F;
		return VesperEveningStarPose.airbornePose(action) ? 0.0F : 0.3F;
	}

	private static int signature(VesperTheEveningStarEntity entity, float actionTime) {
		VesperWeaponAction action = entity.getWeaponAction();
		int hash = action.ordinal();
		hash = hash * 31 + (entity.getActionVariant() & 1);
		hash = hash * 31 + VesperEveningStarPose.segment(action, actionTime);
		hash = hash * 31 + entity.getActiveTendency().ordinal();
		hash = hash * 31 + (entity.isRaging() ? 1 : 0);
		hash = hash * 31 + (entity.isHoodRemovalActive() ? 1 : 0);
		hash = hash * 31 + (entity.isAwaitingAbsorption() ? 1 : 0);
		return hash;
	}

	/**
	 * Eases the freshly computed pose out of the last rendered one. Normally the lag is under half a tick, which only
	 * hides one-frame pops; right after a state change it widens so a cut-off move, a new stance or the end of an
	 * authored clip crossfades. Rotations take the short way round, so a finished spin never unwinds.
	 */
	public void blend(List<ModelPart> parts) {
		if (pose == null || pose.length != parts.size()) {
			pose = new float[parts.size()][9];
			for (int i = 0; i < parts.size(); i++) store(parts.get(i), pose[i]);
			return;
		}
		float t = transition * transition * (3.0F - 2.0F * transition);
		float alpha = VesperMountMotionRules.follow(dt, Mth.lerp(t, BASE_POSE_TAU, TRANSITION_POSE_TAU));
		for (int i = 0; i < parts.size(); i++) {
			ModelPart part = parts.get(i);
			float[] last = pose[i];
			part.x = Mth.lerp(alpha, last[0], part.x);
			part.y = Mth.lerp(alpha, last[1], part.y);
			part.z = Mth.lerp(alpha, last[2], part.z);
			part.xRot = angleLerp(alpha, last[3], part.xRot);
			part.yRot = angleLerp(alpha, last[4], part.yRot);
			part.zRot = angleLerp(alpha, last[5], part.zRot);
			part.xScale = Mth.lerp(alpha, last[6], part.xScale);
			part.yScale = Mth.lerp(alpha, last[7], part.yScale);
			part.zScale = Mth.lerp(alpha, last[8], part.zScale);
			store(part, last);
		}
	}

	private static float angleLerp(float alpha, float from, float to) {
		return from + VesperEveningStarPose.wrap(to - from) * alpha;
	}

	private static void store(ModelPart part, float[] into) {
		into[0] = part.x;
		into[1] = part.y;
		into[2] = part.z;
		into[3] = part.xRot;
		into[4] = part.yRot;
		into[5] = part.zRot;
		into[6] = part.xScale;
		into[7] = part.yScale;
		into[8] = part.zScale;
	}

	/** Remembers where boot {@code foot} was drawn (model units), so the next tick samples the floor under it. */
	void setFoot(int foot, float x, float z) {
		footX[foot] = x;
		footZ[foot] = z;
	}

	static float renderScale(VesperTheEveningStarEntity entity) {
		return com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperPhaseTransitionRules.awakeningScale(
				entity.getAwakeningFrame(0.0F));
	}

	public float actionTime() { return actionTime; }
	public float stanceTime() { return stanceTime; }
	public float gaitPhase() { return phase; }
	public float stride() { return stride * gaitWeight; }
	public float driveForward() { return forward; }
	public float driveLateral() { return lateral; }
	public float groundWeight() { return groundWeight; }

	/** How far the rendered body sits from the entity's real height while a step eases out, in blocks. */
	public float visualOffset() {
		return visualY - renderY;
	}

	/** Height of the floor under boot {@code foot} above (+) or below (-) the drawn body, in blocks. */
	public float groundUnder(int foot) {
		return ground[foot] - visualY;
	}

	private static float frac(float value) {
		return value - Mth.floor(value);
	}
}
