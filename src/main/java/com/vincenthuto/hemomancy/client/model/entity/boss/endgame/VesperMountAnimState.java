package com.vincenthuto.hemomancy.client.model.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.entity.boss.BossReplayClock;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperBurrowRules;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperTheCrownedRefusalEntity;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperWingedFlightRules;
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
 * Client-only motion memory for one Crowned Refusal. The mount model is shared between entities and posed from
 * synced state that arrives in uneven bursts, so everything that needs continuity lives here: replayed animation
 * clocks, a distance-driven gait phase, the sampled ground under each foot, and the last rendered pose that state
 * changes blend out of instead of snapping.
 */
public final class VesperMountAnimState {
	private static final Map<VesperTheCrownedRefusalEntity, VesperMountAnimState> STATES = new WeakHashMap<>();

	/** Blocks the body travels per full tripod cycle: the stance arc of a coxa sweep at the feet's reach. */
	private static final float STRIDE_BLOCKS = 1.9F;
	/** How far out the feet sit from the body's centre, for turning on the spot. */
	private static final float TURN_RADIUS_BLOCKS = 2.9F;
	/** Speed (blocks per tick, including turning) at which the gait reaches full stride. */
	private static final float FULL_STRIDE_SPEED = 0.07F;
	private static final float BASE_POSE_TAU = 0.45F;
	private static final float TRANSITION_POSE_TAU = 3.2F;
	private static final float TRANSITION_TICKS = 9.0F;
	private static final float MAX_BODY_DROP = 1.4F;
	private static final float MAX_BODY_RISE = 0.6F;
	private static final float MAX_LEG_REACH = 1.25F;
	private static final float MAX_TILT = 0.38F;

	private final BossReplayClock attackClock = new BossReplayClock();
	private final BossReplayClock staggerClock = new BossReplayClock();
	private final BossReplayClock flightClock = new BossReplayClock();
	private final BossReplayClock burrowClock = new BossReplayClock();
	private final BossReplayClock transitionClock = new BossReplayClock();
	private final BossReplayClock arrivalClock = new BossReplayClock();

	private boolean started;
	private float lastAge;
	private int lastTick = Integer.MIN_VALUE;
	private float dt;

	private float attackTime;
	private float staggerTime;
	private float flightTime;
	private float burrowTime;
	private float transitionTime;
	private float arrivalTime = -1.0F;

	// Gait: what the body did last tick, eased so a single lerp hitch never kicks the legs.
	private float targetRate;
	private float rate;
	private float phase;
	private float stride;
	private float targetStride;
	private float driveForward = 1.0F;
	private float driveLateral;
	private float driveTurn;
	private float targetForward = 1.0F;
	private float targetLateral;
	private float targetTurn;

	// Terrain: absolute ground heights sampled once a tick, followed per frame.
	private final float[] groundTarget = new float[6];
	private final float[] ground = new float[6];
	private float bodyHeightTarget;
	private float bodyHeight;
	private float pitchTarget;
	private float pitch;
	private float rollTarget;
	private float roll;
	private float terrainWeight;
	private float renderY;
	private int airTicks;
	private final float[] legDrop = new float[6];

	// Smoothed flight attitude and rider weights; the synced values step once a tick.
	private float bank;
	private float flightPitch;
	private float wingSpread;
	private float riderGrip;
	private float riderRecoil;
	private float riderFlight;

	// Pose inertia.
	private float[][] pose;
	private int signature = Integer.MIN_VALUE;
	private float transition;

	private VesperMountAnimState() {
	}

	public static VesperMountAnimState of(VesperTheCrownedRefusalEntity entity) {
		return STATES.computeIfAbsent(entity, e -> new VesperMountAnimState());
	}

	/** Advances everything to this frame. Safe to call more than once per frame: a zero step changes nothing. */
	public void update(VesperTheCrownedRefusalEntity entity, float ageInTicks, float partial) {
		dt = started ? ageInTicks - lastAge : 0.0F;
		// A paused game, a long unload or a reloaded entity: jump to the present instead of crawling there.
		boolean snap = !started || dt < 0.0F || dt > 6.0F;
		if (snap) dt = 0.0F;
		lastAge = ageInTicks;
		int tick = entity.tickCount;
		attackTime = attackClock.sample(entity.getAttack().ordinal(), entity.getAttackTick(), tick, partial, 1);
		staggerTime = staggerClock.sample(0, entity.getStaggerTick(), tick, partial, -1);
		flightTime = flightClock.sample(entity.getFlightMode().ordinal(), entity.getFlightTick(), tick, partial, 1);
		burrowTime = burrowClock.sample(entity.getBurrowHuntTicks(), entity.getBurrowTick(), tick, partial, 1);
		transitionTime = transitionClock.sample(0, entity.getTransitionTick(), tick, partial, 1);
		arrivalTime = entity.getArrivalTick() < 0 ? -1.0F : arrivalClock.sample(0, entity.getArrivalTick(), tick, partial, 1);
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
			driveForward = targetForward;
			driveLateral = targetLateral;
			driveTurn = targetTurn;
			System.arraycopy(groundTarget, 0, ground, 0, 6);
			bodyHeight = bodyHeightTarget;
			pitch = pitchTarget;
			roll = rollTarget;
			bank = entity.getFlightBank();
			flightPitch = entity.getFlightPitch();
			terrainWeight = terrainTarget(entity, airTicks);
			started = true;
		}

		float gaitFollow = VesperMountMotionRules.follow(dt, 2.5F);
		rate = Mth.lerp(gaitFollow, rate, targetRate);
		stride = Mth.lerp(VesperMountMotionRules.follow(dt, targetStride > stride ? 2.0F : 4.0F), stride, targetStride);
		driveForward = Mth.lerp(gaitFollow, driveForward, targetForward);
		driveLateral = Mth.lerp(gaitFollow, driveLateral, targetLateral);
		driveTurn = Mth.lerp(gaitFollow, driveTurn, targetTurn);
		phase = frac(phase + rate * dt);

		float footFollow = VesperMountMotionRules.follow(dt, 1.6F);
		for (int i = 0; i < 6; i++) ground[i] = Mth.lerp(footFollow, ground[i], groundTarget[i]);
		float bodyFollow = VesperMountMotionRules.follow(dt, 2.6F);
		bodyHeight = Mth.lerp(bodyFollow, bodyHeight, bodyHeightTarget);
		pitch = Mth.lerp(bodyFollow, pitch, pitchTarget);
		roll = Mth.lerp(bodyFollow, roll, rollTarget);
		terrainWeight = Mth.lerp(VesperMountMotionRules.follow(dt, 3.0F), terrainWeight, terrainTarget(entity, airTicks));
		solveLegs();

		float attitude = VesperMountMotionRules.follow(dt, 2.0F);
		bank = Mth.lerp(attitude, bank, entity.getFlightBank());
		flightPitch = Mth.lerp(attitude, flightPitch, entity.getFlightPitch());
		float weights = VesperMountMotionRules.follow(dt, 3.0F);
		riderGrip = Mth.lerp(weights, riderGrip, entity.isBurrowing() ? 1.0F : 0.0F);
		riderRecoil = Mth.lerp(weights, riderRecoil, entity.getActiveAnchor() >= 0 ? 1.0F : 0.0F);
		riderFlight = Mth.lerp(weights, riderFlight, entity.getFlightMode().airborne() ? 1.0F : 0.0F);

		int nextSignature = signature(entity);
		if (nextSignature != signature) {
			if (signature != Integer.MIN_VALUE) transition = 1.0F;
			signature = nextSignature;
		}
		transition = Math.max(0.0F, transition - dt / TRANSITION_TICKS);
		if (snap) pose = null;
	}

	private void sampleMotion(VesperTheCrownedRefusalEntity entity) {
		double vx = entity.getX() - entity.xo;
		double vz = entity.getZ() - entity.zo;
		float yaw = entity.yBodyRot * Mth.DEG_TO_RAD;
		float cos = Mth.cos(yaw);
		float sin = Mth.sin(yaw);
		// Into the model's frame: +x is the model's +x side, forward is -z (reported here as positive).
		float lateral = (float) (vx * cos + vz * sin);
		float forward = (float) (-vx * sin + vz * cos);
		float turn = Mth.wrapDegrees(entity.yBodyRot - entity.yBodyRotO) * Mth.DEG_TO_RAD;
		boolean grounded = !entity.getFlightMode().airborne() && entity.getBurrowStage() == null
				|| entity.getBurrowStage() == VesperBurrowRules.Stage.RECOVERY;
		float linear = Mth.sqrt(lateral * lateral + forward * forward);
		float turning = Math.abs(turn) * TURN_RADIUS_BLOCKS;
		float total = grounded ? linear + turning : 0.0F;
		// A teleport or a burrow surfacing is not a step.
		if (total > 1.5F) total = 0.0F;
		targetRate = total / STRIDE_BLOCKS;
		targetStride = Mth.clamp(total / FULL_STRIDE_SPEED, 0.0F, 1.0F);
		if (total > 0.004F) {
			targetForward = forward / total;
			targetLateral = lateral / total;
			targetTurn = Math.signum(turn) * turning / total;
		}
	}

	/** Samples the floor under each rest foot and fits the plane the body should ride on. */
	private void sampleTerrain(VesperTheCrownedRefusalEntity entity) {
		Level level = entity.level();
		double baseY = entity.getY();
		float yaw = entity.yBodyRot * Mth.DEG_TO_RAD;
		float cos = Mth.cos(yaw);
		float sin = Mth.sin(yaw);
		float[] xs = new float[6];
		float[] zs = new float[6];
		float[] hs = new float[6];
		for (int i = 0; i < 6; i++) {
			float[] foot = VesperMountMotionRules.restFoot(i / 2, i % 2 == 1);
			xs[i] = foot[0] / 16.0F;
			zs[i] = foot[1] / 16.0F;
			// Model (x, z) -> entity-local (x, -z) -> world, turned by the body yaw.
			float lx = xs[i];
			float lz = -zs[i];
			double wx = entity.getX() + lx * cos - lz * sin;
			double wz = entity.getZ() + lx * sin + lz * cos;
			hs[i] = groundAt(level, wx, wz, baseY);
			groundTarget[i] = (float) baseY + hs[i];
		}
		float[] plane = VesperMountMotionRules.fitPlane(xs, zs, hs);
		float b = Mth.clamp(plane[1], -0.45F, 0.45F);
		float c = Mth.clamp(plane[2], -0.45F, 0.45F);
		// Re-centre the clamped plane on the feet so legs share the leftover evenly.
		float mean = 0.0F;
		for (int i = 0; i < 6; i++) mean += hs[i] - b * xs[i] - c * zs[i];
		mean /= 6.0F;
		bodyHeightTarget = (float) baseY + Mth.clamp(mean, -MAX_BODY_DROP, MAX_BODY_RISE);
		// Positive x-rotation dips the front (-z); positive z-rotation dips the +x side.
		pitchTarget = Mth.clamp((float) Math.atan(c), -MAX_TILT, MAX_TILT);
		rollTarget = Mth.clamp((float) -Math.atan(b), -MAX_TILT, MAX_TILT);
	}

	private void solveLegs() {
		float tanPitch = (float) Math.tan(pitch);
		float tanRoll = (float) -Math.tan(roll);
		for (int i = 0; i < 6; i++) {
			float[] foot = VesperMountMotionRules.restFoot(i / 2, i % 2 == 1);
			float x = foot[0] / 16.0F;
			float z = foot[1] / 16.0F;
			float footHeight = bodyHeight + tanRoll * x + tanPitch * z;
			legDrop[i] = Mth.clamp(footHeight - ground[i], -MAX_LEG_REACH, MAX_LEG_REACH);
		}
	}

	private static float groundAt(Level level, double x, double z, double fromY) {
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		int top = Mth.floor(fromY + 1.5D);
		int bottom = Mth.floor(fromY - 3.0D);
		for (int y = top; y >= bottom; y--) {
			pos.set(Mth.floor(x), y, Mth.floor(z));
			BlockState state = level.getBlockState(pos);
			VoxelShape shape = state.getCollisionShape(level, pos);
			if (!shape.isEmpty()) return (float) (y + shape.max(Direction.Axis.Y) - fromY);
		}
		return (float) (bottom - fromY);
	}

	private static float terrainTarget(VesperTheCrownedRefusalEntity entity, int airTicks) {
		// A step down a ledge is a few ticks of falling; anything longer is a leap, and the legs are its to pose.
		if (entity.getTransitionTick() > 0 || entity.getFlightMode().airborne() || airTicks > 3) return 0.0F;
		VesperBurrowRules.Stage burrow = entity.getBurrowStage();
		if (burrow != null && burrow != VesperBurrowRules.Stage.RECOVERY) return 0.0F;
		return 1.0F;
	}

	private static int signature(VesperTheCrownedRefusalEntity entity) {
		VesperBurrowRules.Stage burrow = entity.getBurrowStage();
		VesperWingedFlightRules.FlightMode flight = entity.getFlightMode();
		int hash = entity.getAttack().ordinal();
		hash = hash * 31 + flight.ordinal();
		hash = hash * 31 + (burrow == null ? -1 : burrow.ordinal());
		hash = hash * 31 + (entity.getStaggerTick() > 0 ? 1 : 0);
		hash = hash * 31 + (entity.getActiveAnchor() >= 0 ? 1 : 0);
		return hash;
	}

	/**
	 * Eases the freshly computed pose out of the last rendered one. Normally the lag is a fraction of a tick, which
	 * only hides one-frame pops; right after a state change it widens so a cut-off move or a new mode crossfades.
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
			part.xRot = Mth.lerp(alpha, last[3], part.xRot);
			part.yRot = Mth.lerp(alpha, last[4], part.yRot);
			part.zRot = Mth.lerp(alpha, last[5], part.zRot);
			part.xScale = Mth.lerp(alpha, last[6], part.xScale);
			part.yScale = Mth.lerp(alpha, last[7], part.yScale);
			part.zScale = Mth.lerp(alpha, last[8], part.zScale);
			store(part, last);
		}
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

	/** Eased wing spread, so takeoff unfolds and landing folds instead of popping between rest poses. */
	public float wingSpread(float target) {
		wingSpread = Mth.lerp(VesperMountMotionRules.follow(dt, 4.0F), wingSpread, target);
		return wingSpread;
	}

	public float attackTime() { return attackTime; }
	public float staggerTime() { return staggerTime; }
	public float flightTime() { return flightTime; }
	public float burrowTime() { return burrowTime; }
	public float transitionTime() { return transitionTime; }
	/** Ticks into his arrival, or -1 once he is fighting. */
	public float arrivalTime() { return arrivalTime; }
	public float gaitPhase() { return phase; }
	public float stride() { return stride; }
	public float driveForward() { return driveForward; }
	public float driveLateral() { return driveLateral; }
	public float driveTurn() { return driveTurn; }
	public float bank() { return bank; }
	public float flightPitch() { return flightPitch; }
	public float riderGrip() { return riderGrip; }
	public float riderRecoil() { return riderRecoil; }
	public float riderFlight() { return riderFlight; }
	public float terrainWeight() { return terrainWeight; }
	public float terrainPitch() { return pitch * terrainWeight; }
	public float terrainRoll() { return roll * terrainWeight; }

	/** How far the body rides above (+) or below (-) the entity's own position, in blocks. */
	public float bodyLift() {
		return Mth.clamp(bodyHeight - renderY, -MAX_BODY_DROP, MAX_BODY_RISE) * terrainWeight;
	}

	/** How far leg {@code leg} must reach below (+) or tuck above (-) its rest foot to meet the floor, in blocks. */
	public float legDrop(int leg) {
		return legDrop[leg] * terrainWeight;
	}

	private static float frac(float value) {
		return value - Mth.floor(value);
	}
}
