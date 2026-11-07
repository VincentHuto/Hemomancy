package com.vincenthuto.hemomancy.client.model.entity.boss.velorum;

import com.vincenthuto.hemomancy.client.model.entity.boss.hemorath.HemorathPoseBlend;
import com.vincenthuto.hemomancy.client.model.entity.boss.hemorath.HemorathSpring;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumDevoteeEntity;
import net.minecraft.util.Mth;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * One woken devotee's client animation state, kept per body: the inertial blend out of the last shown pose whenever its
 * state, its grip or its blow changes ({@link HemorathPoseBlend}, shared with the Saints), the springs that swing its
 * hood, cord and sleeves and hold its head a beat behind its body ({@link HemorathSpring}), and its measured ground
 * speed and turn. Its clocks are the entity's synced state and blow start, read on the level's game time.
 *
 * <p>The Eclipse's shapes of liquid dark are not entities: each has a state of its own here, keyed by its figure
 * ({@link #shape}), stepped on the level's game time and forgotten once it has gone unused for a while.
 */
public final class VelorumDevoteeAnimState {
	/** A changed state blends from the last pose over this many ticks; a blow's own blend is shorter, so it snaps. */
	public static final float BLEND = 6.0F, BLOW_BLEND = 3.0F;
	public static final int SPRINGS = 12;

	private static final Map<VelorumDevoteeEntity, VelorumDevoteeAnimState> STATES = new WeakHashMap<>();
	private static final Map<Long, VelorumDevoteeAnimState> SHAPES = new HashMap<>();
	/** A shape's state unused this many ticks is forgotten. */
	private static final float SHAPE_FORGET = 200.0F;

	private final HemorathPoseBlend blend = new HemorathPoseBlend();
	private final HemorathSpring[] springs = new HemorathSpring[SPRINGS];
	private final HemorathSpring walkSpring = new HemorathSpring();
	private final HemorathSpring turnSpring = new HemorathSpring();
	private float lastAge = Float.NaN;
	private float frameDt;
	private float bodyYaw = Float.NaN;
	private float yawTravel;

	private VelorumDevoteeAnimState() {
		for (int i = 0; i < SPRINGS; i++) springs[i] = new HemorathSpring();
	}

	public static VelorumDevoteeAnimState of(VelorumDevoteeEntity devotee) {
		return STATES.computeIfAbsent(devotee, ignored -> new VelorumDevoteeAnimState());
	}

	static void clear() {
		STATES.clear();
		SHAPES.clear();
	}

	/** The state of the Eclipse shape standing out of the figure {@code key} (its base, as a long), at game time {@code now}. */
	public static VelorumDevoteeAnimState shape(long key, float now) {
		if (SHAPES.size() > 64) SHAPES.values().removeIf(state -> !(state.lastAge >= now - SHAPE_FORGET));
		return SHAPES.computeIfAbsent(key, ignored -> new VelorumDevoteeAnimState());
	}

	/** Steps a shape's clock once a frame; {@code age} is the level's game time with the partial tick. */
	public void step(float age) {
		frameDt = Float.isNaN(lastAge) ? 0.0F : Math.max(0.0F, Math.min(5.0F, age - lastAge));
		lastAge = age;
	}

	/** Steps the measured motion once a frame; {@code age} is the body's age with the partial tick. */
	public void update(VelorumDevoteeEntity devotee, float partial, float age, float walking) {
		frameDt = Float.isNaN(lastAge) ? 0.0F : Math.max(0.0F, Math.min(5.0F, age - lastAge));
		lastAge = age;
		walkSpring.step(Math.min(1.0F, walking * 2.5F), frameDt, 0.35F, 1.0F);
		float yawRate = Mth.wrapDegrees(devotee.yBodyRot - devotee.yBodyRotO);
		turnSpring.step(Mth.clamp(yawRate / 10.0F, -1.0F, 1.0F), frameDt, 0.35F, 0.85F);
		float yaw = Mth.rotLerp(partial, devotee.yBodyRotO, devotee.yBodyRot);
		if (!Float.isNaN(bodyYaw)) yawTravel += Mth.wrapDegrees(yaw - bodyYaw) * Mth.DEG_TO_RAD;
		bodyYaw = yaw;
	}

	/** Blends {@code pose} out of the last shown pose when {@code key} changes; a blow blends in shorter. */
	public void blend(float[] pose, long key, float age, boolean blow) {
		blend.apply(pose, key, age, blow ? BLOW_BLEND : BLEND);
	}

	public float lag(int slot, float driver, float frequency, float damping) {
		return springs[slot].lag(driver, frameDt, frequency, damping);
	}

	public float follow(int slot, float target, float frequency, float damping) {
		return springs[slot].step(target, frameDt, frequency, damping);
	}

	/** 0 to 1: how far it is walking, smoothed so a stop settles. */
	public float walking() {
		return Math.max(0.0F, Math.min(1.0F, walkSpring.value()));
	}

	/** -1 to 1: how hard it is turning, smoothed. */
	public float turn() {
		return turnSpring.value();
	}

	public float yawTravel() {
		return yawTravel;
	}
}
