package com.vincenthuto.hemomancy.client.model.entity.boss.endgame;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantAttack;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantControllerRules;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantEntity;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantIntroRules;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantMotionRules;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantSplitRules;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantState;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantYieldRules;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import java.util.List;

/** Replays the encounter poses on the joints baked from the Spawn Mother Blockbench project. */
public final class MycophantModel extends HierarchicalModel<MycophantEntity> {
	// LivingEntityRenderer raises the model-space origin by this much above the entity's feet.
	private static final float RENDER_ORIGIN_Y = 1.501F;

	private final ModelPart root;
	private final List<ModelPart> parts;
	private final ModelPart body;
	private final ModelPart thorax;
	private final ModelPart neck;
	private final ModelPart head;
	private final ModelPart abdomen;
	private final ModelPart sac;
	private final ModelPart collar;
	private final ModelPart skullL;
	private final ModelPart skullR;
	private final ModelPart maw;
	private final ModelPart pulp;
	private final ModelPart wingL;
	private final ModelPart wingR;
	private final ModelPart broodSocket;
	private final ModelPart siphonMouth;
	private final ModelPart gazeEye;
	private final ModelPart[] ventPetals;
	private final ModelPart[] jaws = new ModelPart[4];
	private final ModelPart[][] tails = new ModelPart[2][];
	private final ModelPart[][] frill = new ModelPart[2][8];
	private final ModelPart[][] feelers = new ModelPart[8][];
	private final ModelPart[][] fingers = new ModelPart[3][];
	private final ModelPart[] thumb;
	private final ModelPart[] siphon;
	private final ModelPart[][] pulpPetals = new ModelPart[7][];
	private final MycophantLimb[] claws = new MycophantLimb[4];
	private final MycophantLimb grasping;
	private final MycophantLimb feeding;
	private boolean cradleBrood;

	public MycophantModel() {
		this.root = MycophantMesh.createRoot();
		this.parts = root.getAllParts().toList();
		this.body = root.getChild("body");
		this.thorax = body.getChild("thorax");
		this.neck = thorax.getChild("neck");
		this.head = neck.getChild("head");
		this.abdomen = body.getChild("abdomen");
		this.sac = body.getChild("sac");
		this.collar = head.getChild("collar");
		ModelPart skull = head.getChild("skull");
		this.skullL = skull.getChild("skull_l");
		this.skullR = skull.getChild("skull_r");
		this.maw = skull.getChild("maw");
		this.pulp = head.getChild("pulp");
		this.gazeEye = pulp.getChild("gaze_eye");
		ModelPart wings = thorax.getChild("wings");
		this.wingL = wings.getChild("wing_l");
		this.wingR = wings.getChild("wing_r");
		this.ventPetals = new ModelPart[5];
		for (int i = 0; i < ventPetals.length; i++) ventPetals[i] = abdomen.getChild("rear_vent").getChild("vent_" + i);
		for (int side = 0; side < 2; side++) {
			String suffix = side == 0 ? "l" : "r";
			this.tails[side] = chain(abdomen, "tail_" + suffix, 3);
			for (int i = 0; i < frill[side].length; i++) frill[side][i] = collar.getChild("frill_" + suffix + "_" + i);
			for (int i = 0; i < 4; i++) {
				String name = "feeler_" + suffix + "_" + i;
				this.feelers[side * 4 + i] = chain(head.getChild(name), name + "_link", 3);
			}
			for (int pair = 0; pair < 2; pair++) {
				int i = pair * 2 + side;
				String name = (pair == 0 ? "fore_" : "hind_") + suffix;
				ModelPart upper = body.getChild("claw_" + name);
				ModelPart elbow = upper.getChild("elbow_" + name);
				ModelPart pincer = elbow.getChild("pincer_" + name);
				this.claws[i] = new MycophantLimb(upper, elbow, pincer);
				this.jaws[i] = pincer.getChild("jaw_" + name);
			}
		}
		this.grasping = upperLimb("hand");
		this.feeding = upperLimb("feeding");
		for (int i = 0; i < fingers.length; i++)
			fingers[i] = chain(grasping.wrist.getChild("finger_" + i), "finger_" + i + "_link", 3);
		this.thumb = chain(grasping.wrist, "thumb", 3);
		this.siphon = chain(feeding.wrist, "siphon", 6);
		this.siphonMouth = siphon[siphon.length - 1].getChild("siphon_mouth");
		this.broodSocket = grasping.wrist.getChild("brood_socket");
		for (int i = 0; i < pulpPetals.length; i++)
			pulpPetals[i] = chain(pulp.getChild("pulp_petal_" + i), "pulp_" + i + "_link", 2);
		applySplit(0.0F, 0.0F, 0.0F);
	}

	private MycophantLimb upperLimb(String name) {
		ModelPart upper = thorax.getChild(name);
		ModelPart elbow = upper.getChild(name + "_elbow");
		return new MycophantLimb(upper, elbow, elbow.getChild(name + "_wrist"));
	}

	private static ModelPart[] chain(ModelPart parent, String prefix, int count) {
		ModelPart[] links = new ModelPart[count];
		for (int i = 0; i < count; i++) {
			parent = parent.getChild(prefix + "_" + i);
			links[i] = parent;
		}
		return links;
	}

	@Override
	public ModelPart root() {
		return root;
	}


	/**
	 * Review-client hook: a pose name, with "_p2" for the opened head. The rebuild's poses are "stalk", "tending",
	 * "notice", "roar", "embrace", "hold", "spawn", "guard", "sweep", "siphon", "pluck", "bloom", "stagger", "yield" and
	 * "drag"; the moveset's are "rake_l", "rake_r", "slam", "settle_rise", "settle_drop", "pin", "tail_coil",
	 * "tail_lash", "vents_open", "hand_wind", "hand_sweep", "spit", "mortar", "nurse", "lances", "net", "gaze",
	 * "last_brood_rear", "last_brood_pull" and "last_brood_collapse" (the phase-2 ones always show the opened head).
	 */
	public static volatile String reviewPose;

	private static final class Pose {
		/** Scalars first, then the three-float targets, in {@link #write} order. */
		private static final int SCALARS = 19;
		private static final int VECTORS = 9;
		/** The pose as a flat array, for the blend memory each entity keeps in its MycophantAnimState. */
		static final int SIZE = SCALARS + VECTORS * 3;
		private static final float[] SCRATCH_A = new float[SIZE];
		private static final float[] SCRATCH_B = new float[SIZE];
		private static final float[] SCRATCH_OUT = new float[SIZE];

		float thorax, neck, head, frill, maw, tongue, tentFwd, height, vents;
		final float[] wing = new float[3];
		final float[] hand = new float[3];
		final float[] handPole = new float[3];
		float grip;
		final float[] siphonT = new float[3];
		final float[] siphonPole = new float[3];
		float curl;
		final float[] front = new float[3];
		final float[] second = new float[3];
		/** Offsets (blockout pixels, +x toward her right) added to the left and right front claws' footholds. */
		final float[] reachL = new float[3];
		final float[] reachR = new float[3];
		/** The tail tendrils' coil (+ curls them up over the abdomen) and swing (+ toward her right). */
		float tailCurl, tailSwing;
		/** The abdomen's lift (radians, + raises its end) and swing (radians, + toward her right). */
		float abdLift, abdYaw;
		/** The sac's swell: 1 is the Last Brood's opening (1.3 times its size, pushed forward); below 0 it sags. */
		float sac;
		/** Blockout pixels the pulp pushes forward out of the opened skull. */
		float pulpFwd;
		/** Radians the head turns toward her right (toward a larger yaw), shared down the neck and thorax. */
		float turn;
		/** How far the wing spars hook their tips down; below 0 they open flat. */
		float spar;

		Pose set(float thorax, float neck, float head, float frill, float maw, float tongue, float tentFwd, float height,
				float vents) {
			this.thorax = thorax; this.neck = neck; this.head = head; this.frill = frill; this.maw = maw;
			this.tongue = tongue; this.tentFwd = tentFwd; this.height = height; this.vents = vents;
			return this;
		}

		Pose wing(float x, float y, float z) { wing[0] = x; wing[1] = y; wing[2] = z; return this; }

		Pose hand(float x, float y, float z, float px, float py, float pz, float grip) {
			hand[0] = x; hand[1] = y; hand[2] = z; handPole[0] = px; handPole[1] = py; handPole[2] = pz; this.grip = grip;
			return this;
		}

		Pose siphon(float x, float y, float z, float px, float py, float pz, float curl) {
			siphonT[0] = x; siphonT[1] = y; siphonT[2] = z; siphonPole[0] = px; siphonPole[1] = py; siphonPole[2] = pz;
			this.curl = curl;
			return this;
		}

		Pose drag(float fx, float fy, float fz, float sx, float sy, float sz) {
			front[0] = fx; front[1] = fy; front[2] = fz; second[0] = sx; second[1] = sy; second[2] = sz;
			return this;
		}

		Pose reach(float lx, float ly, float lz, float rx, float ry, float rz) {
			reachL[0] = lx; reachL[1] = ly; reachL[2] = lz; reachR[0] = rx; reachR[1] = ry; reachR[2] = rz;
			return this;
		}

		Pose tail(float curl, float swing, float lift, float yaw) {
			tailCurl = curl; tailSwing = swing; abdLift = lift; abdYaw = yaw;
			return this;
		}

		Pose swell(float sac) { this.sac = sac; return this; }

		Pose pulp(float forward) { pulpFwd = forward; return this; }

		Pose turn(float turn) { this.turn = turn; return this; }

		Pose spar(float spar) { this.spar = spar; return this; }

		private float[][] vectors() {
			return new float[][] { wing, hand, handPole, siphonT, siphonPole, front, second, reachL, reachR };
		}

		void write(float[] out) {
			out[0] = thorax; out[1] = neck; out[2] = head; out[3] = frill; out[4] = maw; out[5] = tongue;
			out[6] = tentFwd; out[7] = height; out[8] = vents; out[9] = grip; out[10] = curl; out[11] = tailCurl;
			out[12] = tailSwing; out[13] = abdLift; out[14] = abdYaw; out[15] = sac; out[16] = pulpFwd; out[17] = turn;
			out[18] = spar;
			float[][] vectors = vectors();
			for (int a = 0; a < VECTORS; a++) System.arraycopy(vectors[a], 0, out, SCALARS + a * 3, 3);
		}

		Pose read(float[] in) {
			set(in[0], in[1], in[2], in[3], in[4], in[5], in[6], in[7], in[8]);
			grip = in[9]; curl = in[10]; tailCurl = in[11]; tailSwing = in[12]; abdLift = in[13]; abdYaw = in[14];
			sac = in[15]; pulpFwd = in[16]; turn = in[17]; spar = in[18];
			float[][] vectors = vectors();
			for (int a = 0; a < VECTORS; a++) System.arraycopy(in, SCALARS + a * 3, vectors[a], 0, 3);
			return this;
		}

		Pose copy(Pose o) {
			o.write(SCRATCH_OUT);
			return read(SCRATCH_OUT);
		}

		/** this = mix(a, b, t). Either may be this pose. */
		Pose mix(Pose a, Pose b, float t) {
			a.write(SCRATCH_A);
			b.write(SCRATCH_B);
			for (int i = 0; i < SIZE; i++) SCRATCH_OUT[i] = Mth.lerp(t, SCRATCH_A[i], SCRATCH_B[i]);
			return read(SCRATCH_OUT);
		}

		/** this += (b - a) * t: layers part of the move from a to b over what is already posed, so swings can overlap. */
		Pose add(Pose a, Pose b, float t) {
			a.write(SCRATCH_A);
			b.write(SCRATCH_B);
			write(SCRATCH_OUT);
			for (int i = 0; i < SIZE; i++) SCRATCH_OUT[i] += (SCRATCH_B[i] - SCRATCH_A[i]) * t;
			return read(SCRATCH_OUT);
		}
	}

	private static final Pose STALK = new Pose().set(0.0F, 0.0F, 0.0F, 0.55F, 0.15F, 0.3F, 0.4F, 0.0F, 0.0F).wing(0.0F, -0.5F, 0.08F)
			.hand(-26, 40, -50, -1, -0.3F, 0.3F, 0.3F).siphon(24, 44, -38, 1, -0.4F, 0.3F, 0.25F).drag(46, 0, -60, 60, 0, -26);
	private static final Pose TENDING = new Pose().set(-0.25F, -0.2F, -0.35F, 0.0F, 0.05F, 0.5F, 0.8F, -2.0F, 0.0F).wing(-0.15F, 0.95F, -0.35F)
			.hand(-6, 38, -58, -1, -0.6F, 0.1F, 0.75F).siphon(14, 52, -56, 1, -0.3F, 0.2F, 0.55F).drag(42, 0, -54, 58, 0, -22);
	private static final Pose NOTICE = new Pose().set(-0.05F, 0.15F, 0.25F, 0.5F, 0.2F, 0.3F, 0.3F, -1.0F, 0.0F).wing(0.05F, 0.75F, -0.1F)
			.hand(-8, 40, -66, -1, -0.6F, 0.1F, 0.7F).siphon(18, 54, -60, 1, -0.3F, 0.2F, 0.2F).drag(44, 0, -56, 58, 0, -22);
	private static final Pose ROAR = new Pose().set(0.4F, -0.05F, -0.12F, 1.0F, 1.0F, 0.0F, -0.4F, 2.0F, 0.4F).wing(0.1F, 0.12F, 0.35F)
			.hand(-52, 92, -24, -1, -0.5F, 0.5F, 0.0F).siphon(52, 90, -20, 1, -0.5F, 0.5F, -0.3F).drag(54, 58, -50, 64, 18, -32);
	private static final Pose EMBRACE = new Pose().set(-0.08F, -0.05F, 0.1F, 0.75F, 0.8F, 0.1F, 1.0F, 1.0F, 0.0F).wing(0.05F, 1.0F, 0.2F)
			.hand(-30, 50, -96, -1, 0.2F, 0.2F, 0.0F).siphon(30, 52, -92, 1, 0.2F, 0.2F, 0.0F).drag(46, 14, -98, 62, 4, -62);
	private static final Pose HOLD = new Pose().set(-0.12F, -0.1F, -0.15F, 0.9F, 0.6F, 0.2F, 1.0F, 0.0F, 0.2F).wing(0.1F, 1.3F, -0.2F)
			.hand(-8, 34, -58, -1, -0.4F, 0.2F, 0.85F).siphon(10, 40, -58, 1, -0.3F, 0.2F, 0.6F).drag(40, 0, -66, 56, 0, -28);
	private static final Pose SPAWN = new Pose().set(-0.12F, -0.1F, -0.1F, 0.4F, 0.3F, 0.4F, 0.5F, -3.0F, 1.0F).wing(0.0F, -0.5F, 0.08F)
			.hand(-30, 34, -40, -1, -0.3F, 0.3F, 0.4F).siphon(30, 36, -34, 1, -0.3F, 0.3F, 0.4F).drag(50, 0, -56, 64, 0, -20);
	private static final Pose GUARD = new Pose().set(-0.15F, -0.2F, -0.2F, 0.3F, 0.1F, 0.4F, 0.6F, -2.0F, 0.0F).wing(0.15F, 1.3F, -0.25F)
			.hand(-14, 40, -40, -1, -0.3F, 0.3F, 0.6F).siphon(14, 42, -38, 1, -0.3F, 0.3F, 0.5F).drag(44, 0, -52, 58, 0, -20);
	private static final Pose SWEEP_WIND = new Pose().set(0.2F, 0.0F, 0.0F, 0.9F, 0.6F, 0.1F, 0.0F, 1.0F, 0.1F).wing(0.3F, -0.9F, 0.6F)
			.hand(-48, 66, -10, -1, -0.3F, 0.3F, 0.1F).siphon(48, 66, -10, 1, -0.3F, 0.3F, -0.2F).drag(50, 0, -56, 62, 0, -24);
	private static final Pose SWEEP = new Pose().set(0.15F, 0.0F, 0.0F, 1.0F, 0.9F, 0.1F, 0.2F, 1.0F, 0.2F).wing(0.25F, 0.45F, 0.65F)
			.hand(-44, 60, -40, -1, -0.3F, 0.3F, 0.0F).siphon(44, 60, -36, 1, -0.3F, 0.3F, -0.2F).drag(48, 0, -60, 60, 0, -24);
	private static final Pose SIPHON_AIM = new Pose().set(0.1F, 0.1F, 0.15F, 0.7F, 0.4F, 0.2F, 0.4F, 0.5F, 0.0F).wing(0.0F, -0.5F, 0.08F)
			.hand(-26, 40, -50, -1, -0.3F, 0.3F, 0.3F).siphon(34, 78, -26, 1, -0.6F, 0.4F, -0.4F).drag(46, 0, -60, 60, 0, -26);
	private static final Pose PLUCK_WIND = new Pose().set(0.12F, 0.05F, 0.1F, 0.7F, 0.3F, 0.2F, 0.4F, 1.0F, 0.0F).wing(0.0F, -0.5F, 0.08F)
			.hand(-34, 74, -36, -1, -0.5F, 0.4F, 0.0F).siphon(24, 44, -38, 1, -0.4F, 0.3F, 0.25F).drag(46, 0, -60, 60, 0, -26);
	private static final Pose PLUCK = new Pose().set(-0.1F, -0.05F, 0.0F, 0.6F, 0.4F, 0.2F, 0.6F, 0.0F, 0.0F).wing(0.0F, -0.5F, 0.08F)
			.hand(-12, 30, -84, -1, 0.3F, 0.2F, 0.95F).siphon(24, 44, -38, 1, -0.4F, 0.3F, 0.25F).drag(46, 0, -64, 60, 0, -26);
	private static final Pose BLOOM_WIND = new Pose().set(-0.15F, -0.25F, -0.2F, 1.0F, 0.5F, 0.0F, -0.3F, -1.0F, 0.3F).wing(0.1F, 0.4F, 0.3F)
			.hand(-30, 40, -44, -1, -0.3F, 0.3F, 0.2F).siphon(30, 42, -40, 1, -0.3F, 0.3F, 0.1F).drag(48, 0, -58, 60, 0, -24);
	private static final Pose BLOOM = new Pose().set(0.25F, 0.05F, 0.05F, 1.0F, 1.0F, 0.0F, -0.6F, 2.0F, 0.6F).wing(0.15F, 0.2F, 0.5F)
			.hand(-40, 70, -30, -1, -0.5F, 0.5F, 0.0F).siphon(40, 70, -28, 1, -0.5F, 0.5F, -0.3F).drag(50, 10, -56, 62, 4, -26);
	private static final Pose STAGGER = new Pose().set(0.28F, -0.15F, 0.1F, 0.2F, 0.7F, 0.2F, -0.2F, 2.0F, 0.2F).wing(0.0F, -0.7F, 0.1F)
			.hand(-38, 52, -30, -1, -0.3F, 0.4F, 0.1F).siphon(38, 54, -26, 1, -0.3F, 0.4F, -0.1F).drag(52, 0, -52, 64, 0, -18);
	/** The split's strain (hunched, head bowed, the hand clutching toward the cracking face) and its wing display. */
	private static final Pose STRAIN = new Pose().set(-0.12F, -0.3F, -0.4F, 0.0F, 0.35F, 0.4F, 0.3F, -2.5F, 0.2F).wing(0.0F, -0.5F, 0.08F)
			.hand(-20, 64, -52, -1, -0.4F, 0.3F, 0.95F).siphon(22, 66, -50, 1, -0.4F, 0.3F, 0.7F).drag(46, 0, -58, 60, 0, -24);
	private static final Pose SPLIT_DISPLAY = new Pose().set(0.2F, 0.05F, 0.05F, 0.8F, 0.4F, 0.2F, 0.0F, 1.0F, 0.3F).wing(0.15F, 0.35F, 0.55F)
			.hand(-46, 70, -30, -1, -0.5F, 0.5F, 0.2F).siphon(46, 70, -28, 1, -0.5F, 0.5F, -0.2F).drag(50, 20, -54, 62, 4, -26);
	private static final Pose YIELDED = new Pose().set(-0.35F, -0.45F, -0.55F, -0.2F, 0.0F, 0.6F, 0.9F, -12.0F, 0.0F).wing(-0.3F, -1.2F, -0.4F)
			.hand(-30, 12, -54, -1, -0.5F, 0.2F, 0.2F).siphon(16, 26, -74, 1, 0.2F, 0.2F, -0.1F).drag(56, 0, -46, 66, 0, -14);

	// the moveset's poses (2026-10-24 expansion). Rakes: a claw lifts and draws back outward, then sweeps in across the
	// front, low; the slam cocks both front claws overhead in a crouch and brings them down together ahead of her
	private static final Pose RAKE_L_UP = new Pose().set(0.1F, 0.05F, 0.05F, 0.8F, 0.35F, 0.2F, 0.5F, 1.0F, 0.0F).wing(0.0F, -0.5F, 0.08F)
			.hand(-16, 56, -44, -1, -0.3F, 0.3F, 0.3F).siphon(24, 44, -38, 1, -0.4F, 0.3F, 0.25F).drag(46, 0, -60, 60, 0, -26)
			.reach(-14, 44, 6, 0, 0, 0).tail(0.0F, 0.0F, 0.0F, 0.05F);
	private static final Pose RAKE_L = new Pose().set(-0.12F, 0.0F, 0.05F, 0.9F, 0.6F, 0.1F, 0.8F, 0.0F, 0.0F).wing(0.05F, -0.4F, 0.1F)
			.hand(-14, 50, -52, -1, -0.3F, 0.3F, 0.2F).siphon(24, 44, -38, 1, -0.4F, 0.3F, 0.25F).drag(46, 0, -60, 60, 0, -26)
			.reach(32, 12, -34, 0, 0, 0).tail(0.0F, 0.0F, 0.0F, -0.06F).turn(0.06F);
	private static final Pose RAKE_R_UP = new Pose().set(0.1F, 0.05F, 0.05F, 0.8F, 0.35F, 0.2F, 0.5F, 1.0F, 0.0F).wing(0.0F, -0.5F, 0.08F)
			.hand(-26, 40, -50, -1, -0.3F, 0.3F, 0.3F).siphon(16, 58, -36, 1, -0.4F, 0.3F, 0.25F).drag(46, 0, -60, 60, 0, -26)
			.reach(0, 0, 0, 14, 44, 6).tail(0.0F, 0.0F, 0.0F, -0.05F);
	private static final Pose RAKE_R = new Pose().set(-0.12F, 0.0F, 0.05F, 0.9F, 0.6F, 0.1F, 0.8F, 0.0F, 0.0F).wing(0.05F, -0.4F, 0.1F)
			.hand(-26, 40, -50, -1, -0.3F, 0.3F, 0.3F).siphon(16, 52, -42, 1, -0.4F, 0.3F, 0.2F).drag(46, 0, -60, 60, 0, -26)
			.reach(0, 0, 0, -32, 12, -34).tail(0.0F, 0.0F, 0.0F, 0.06F).turn(-0.06F);
	private static final Pose SLAM_COCK = new Pose().set(0.05F, -0.1F, -0.1F, 1.0F, 0.3F, 0.1F, 0.7F, -3.0F, 0.2F).wing(0.1F, -0.2F, 0.4F)
			.hand(-24, 66, -40, -1, -0.4F, 0.3F, 0.2F).siphon(24, 68, -38, 1, -0.4F, 0.3F, 0.0F).drag(50, 0, -56, 64, 0, -22)
			.reach(14, 76, 14, -14, 76, 14);
	private static final Pose SLAM = new Pose().set(-0.25F, -0.1F, -0.1F, 1.0F, 1.0F, 0.0F, 1.0F, -2.0F, 0.3F).wing(0.2F, 0.3F, -0.2F)
			.hand(-30, 52, -50, -1, -0.3F, 0.3F, 0.1F).siphon(30, 54, -48, 1, -0.3F, 0.3F, -0.2F).drag(50, 0, -58, 64, 0, -24)
			.reach(32, 4, -26, -32, 4, -26);
	// the Brood Settle: she rears up on all four claws spread wide, then drops her belly to the floor and lies there
	private static final Pose SETTLE_RISE = new Pose().set(0.15F, 0.05F, 0.05F, 0.8F, 0.6F, 0.1F, 0.2F, 10.0F, 0.5F).wing(0.1F, 0.0F, 0.5F)
			.hand(-50, 74, -34, -1, -0.5F, 0.5F, 0.1F).siphon(50, 74, -32, 1, -0.5F, 0.5F, -0.2F).drag(64, 0, -64, 78, 0, -22)
			.swell(0.35F);
	private static final Pose SETTLE_DROP = new Pose().set(-0.18F, -0.2F, -0.15F, 0.4F, 0.5F, 0.3F, 0.7F, -11.0F, 0.3F).wing(0.0F, -0.8F, -0.1F)
			.hand(-40, 16, -62, -1, 0.3F, 0.3F, 0.3F).siphon(38, 18, -60, 1, 0.3F, 0.3F, 0.1F).drag(68, 0, -62, 82, 0, -20)
			.tail(-0.2F, 0.0F, -0.06F, 0.0F).swell(0.1F);
	// the Claw Pin: the right front claw rises and points, then stabs down onto the pin spot (MycophantHoldKind.PIN:
	// 3.6 blocks ahead and 1.6 to her right, on the floor, so blockout (25.6, 0, -57.6)) and presses there
	private static final Pose PIN_WIND = new Pose().set(0.12F, 0.05F, 0.1F, 0.75F, 0.3F, 0.2F, 0.5F, 1.5F, 0.0F).wing(0.0F, -0.5F, 0.08F)
			.hand(-26, 44, -50, -1, -0.3F, 0.3F, 0.3F).siphon(16, 60, -36, 1, -0.4F, 0.3F, 0.2F).drag(46, 0, -60, 60, 0, -26)
			.reach(0, 0, 0, -4, 42, -6);
	private static final Pose PIN = new Pose().set(-0.16F, -0.05F, -0.12F, 0.85F, 0.5F, 0.2F, 0.9F, -1.0F, 0.0F).wing(0.05F, -0.3F, 0.0F)
			.hand(-12, 30, -66, -1, 0.2F, 0.2F, 0.35F).siphon(14, 54, -40, 1, -0.4F, 0.3F, 0.3F).drag(46, 0, -60, 60, 0, -26)
			.reach(0, 0, 0, -20.4F, 0, 2.4F);
	// the Tail Lash: the tendrils coil up and to her left with the abdomen tilted, then lash across to her right (and back)
	private static final Pose TAIL_COIL = new Pose().set(0.05F, 0.0F, 0.05F, 0.6F, 0.2F, 0.3F, 0.4F, 1.0F, 0.2F).wing(0.05F, -0.6F, 0.15F)
			.hand(-26, 40, -50, -1, -0.3F, 0.3F, 0.3F).siphon(24, 44, -38, 1, -0.4F, 0.3F, 0.25F).drag(46, 0, -58, 64, 0, -22)
			.tail(1.0F, -0.8F, 0.2F, -0.15F).turn(0.3F);
	private static final Pose TAIL_LASH = new Pose().set(-0.02F, 0.0F, 0.0F, 0.8F, 0.5F, 0.2F, 0.3F, 0.0F, 0.2F).wing(0.05F, -0.5F, 0.1F)
			.hand(-26, 40, -50, -1, -0.3F, 0.3F, 0.3F).siphon(24, 44, -38, 1, -0.4F, 0.3F, 0.25F).drag(48, 0, -58, 64, 0, -22)
			.tail(0.15F, 1.0F, 0.1F, 0.25F).turn(0.15F);
	private static final Pose TAIL_LASH_BACK = new Pose().copy(TAIL_LASH).tail(0.15F, -1.0F, 0.1F, -0.25F).turn(-0.15F);
	// the Vent Bloom: the petal vents open wide with the abdomen raised and the tendrils lifted clear
	private static final Pose VENTS_OPEN = new Pose().set(-0.05F, -0.05F, 0.0F, 0.5F, 0.2F, 0.3F, 0.4F, 1.0F, 1.5F).wing(0.0F, -0.5F, 0.08F)
			.hand(-26, 40, -50, -1, -0.3F, 0.3F, 0.3F).siphon(24, 44, -38, 1, -0.4F, 0.3F, 0.25F).drag(46, 0, -60, 60, 0, -26)
			.tail(0.5F, 0.0F, 0.25F, 0.0F);
	// the Hand Sweep: the grasping hand swings out wide and low to her left, then sweeps across the front at the floor
	private static final Pose HAND_WIND = new Pose().set(0.06F, 0.0F, 0.0F, 0.8F, 0.4F, 0.2F, 0.4F, 0.5F, 0.0F).wing(0.05F, -0.6F, 0.1F)
			.hand(-76, 30, -16, -1, 0.4F, 0.3F, 0.05F).siphon(26, 46, -34, 1, -0.4F, 0.3F, 0.25F).drag(48, 0, -60, 60, 0, -26)
			.tail(0.0F, 0.0F, 0.0F, 0.08F).turn(-0.2F);
	private static final Pose HAND_SWEEP = new Pose().set(-0.1F, 0.0F, 0.0F, 0.9F, 0.7F, 0.1F, 0.6F, 0.0F, 0.0F).wing(0.05F, -0.4F, 0.1F)
			.hand(28, 24, -70, -0.5F, 0.6F, 0.2F, 0.15F).siphon(34, 56, -30, 1, -0.4F, 0.3F, 0.1F).drag(46, 0, -60, 60, 0, -26)
			.tail(0.0F, 0.0F, 0.0F, -0.08F).turn(0.2F);
	// the Siphon Spit: the siphon arches up high over her right shoulder, pulsing forward at each glob
	private static final Pose SPIT = new Pose().set(0.12F, 0.08F, 0.12F, 0.6F, 0.25F, 0.2F, 0.2F, 1.0F, 0.0F).wing(0.0F, -0.5F, 0.08F)
			.hand(-26, 44, -48, -1, -0.3F, 0.3F, 0.3F).siphon(34, 96, -44, 1, -0.6F, 0.4F, -0.6F).drag(46, 0, -60, 60, 0, -26);
	// the Spore Mortar: the abdomen lifts, the vents open and it pumps the eggs up and out
	private static final Pose MORTAR = new Pose().set(-0.1F, -0.05F, 0.0F, 0.4F, 0.2F, 0.3F, 0.5F, -1.0F, 1.2F).wing(0.0F, -0.5F, 0.08F)
			.hand(-28, 36, -48, -1, -0.3F, 0.3F, 0.3F).siphon(26, 40, -36, 1, -0.4F, 0.3F, 0.25F).drag(48, 0, -58, 66, 0, -20)
			.tail(0.4F, 0.0F, 0.35F, 0.0F).swell(0.15F);
	// Nursing: bowed low over the fungling as she tends it (the siphon itself swings onto it through the latch)
	private static final Pose NURSE = new Pose().set(-0.22F, -0.25F, -0.3F, 0.05F, 0.05F, 0.5F, 0.8F, -2.0F, 0.0F).wing(-0.12F, 0.8F, -0.3F)
			.hand(-8, 34, -62, -1, -0.6F, 0.1F, 0.6F).siphon(16, 46, -62, 1, -0.3F, 0.2F, 0.5F).drag(42, 0, -56, 58, 0, -22);
	// the Hyphae Lances: the wings rise with the spars pointing down, then stab them into the floor
	private static final Pose LANCES_RISE = new Pose().set(0.22F, 0.05F, 0.1F, 0.9F, 0.6F, 0.1F, -0.2F, 2.0F, 0.2F).wing(0.15F, 0.15F, 1.05F)
			.hand(-44, 74, -30, -1, -0.5F, 0.5F, 0.1F).siphon(44, 74, -28, 1, -0.5F, 0.5F, -0.2F).drag(50, 0, -58, 62, 0, -24)
			.spar(-0.3F);
	private static final Pose LANCES = new Pose().set(-0.12F, -0.1F, -0.15F, 1.0F, 0.85F, 0.0F, 0.6F, -1.5F, 0.3F).wing(-0.2F, 0.55F, -0.6F)
			.hand(-34, 40, -56, -1, -0.3F, 0.3F, 0.2F).siphon(34, 42, -52, 1, -0.3F, 0.3F, 0.0F).drag(52, 0, -62, 64, 0, -24)
			.spar(0.8F);
	// the Lattice Net: the wings spread forward and open flat, the arms flung wide
	private static final Pose NET = new Pose().set(0.08F, 0.05F, 0.05F, 1.0F, 0.5F, 0.1F, 0.3F, 1.0F, 0.1F).wing(0.1F, 0.95F, 0.4F)
			.hand(-50, 66, -44, -1, -0.4F, 0.5F, 0.0F).siphon(50, 66, -42, 1, -0.4F, 0.5F, -0.3F).drag(46, 0, -60, 60, 0, -26)
			.spar(-0.35F);
	// the Gaze: the collar flared to frame the pulp, which pushes forward with the tentacles drawn back; the head turns
	// after the beam (added in attackPose)
	private static final Pose GAZE = new Pose().set(0.05F, 0.02F, -0.04F, 1.0F, 0.9F, 0.0F, -0.5F, 1.0F, 0.2F).wing(0.12F, 0.3F, 0.5F)
			.hand(-40, 56, -40, -1, -0.4F, 0.4F, 0.2F).siphon(40, 56, -38, 1, -0.4F, 0.4F, 0.0F).drag(48, 0, -60, 62, 0, -24)
			.pulp(4.0F);
	// the Last Brood: she rears and roars with the collar over-flared, then squats anchored with the sac swollen open
	// toward the player and the arms spread to receive them; cut free, she collapses with the sac torn and slack
	private static final Pose LAST_BROOD_REAR = new Pose().set(0.45F, -0.05F, -0.1F, 1.2F, 1.0F, 0.0F, -0.5F, 4.0F, 0.8F).wing(0.15F, 0.1F, 0.7F)
			.hand(-56, 96, -20, -1, -0.5F, 0.5F, 0.0F).siphon(56, 94, -18, 1, -0.5F, 0.5F, -0.3F).drag(54, 58, -50, 64, 18, -32)
			.tail(0.3F, 0.0F, 0.1F, 0.0F).swell(0.5F);
	private static final Pose LAST_BROOD_PULL = new Pose().set(0.1F, -0.1F, -0.15F, 1.0F, 0.7F, 0.2F, 0.9F, -3.0F, 0.6F).wing(0.2F, 0.7F, 0.3F)
			.hand(-60, 56, -54, -1, -0.2F, 0.4F, 0.0F).siphon(60, 56, -52, 1, -0.2F, 0.4F, -0.2F).drag(64, 0, -70, 76, 0, -18)
			.tail(-0.1F, 0.0F, -0.04F, 0.0F).swell(1.0F);
	private static final Pose LAST_BROOD_COLLAPSE = new Pose().set(-0.32F, -0.4F, -0.45F, -0.2F, 0.4F, 0.7F, 0.9F, -12.0F, 0.1F).wing(-0.3F, -1.1F, -0.4F)
			.hand(-34, 10, -56, -1, -0.5F, 0.2F, 0.15F).siphon(20, 22, -72, 1, 0.2F, 0.2F, -0.1F).drag(60, 0, -50, 72, 0, -14)
			.tail(-0.3F, 0.0F, -0.08F, 0.0F).swell(-0.2F).spar(0.6F);
	/** Ticks each rake or slam takes to strike (arriving on its hit tick) and to ease back out after it. */
	private static final float SWING_TICKS = 4.0F;

	private final Pose pose = new Pose();
	private final Pose poseA = new Pose();
	private final Pose poseB = new Pose();
	private final Vector3f siphonAim = new Vector3f();
	/** 0..1: how far the siphon has swung onto its latched target (0 when there is none). */
	private float siphonLatch;
	/** Intro accents: how still the facial tentacles hold (0..1), the roar's tremble (0..1), the dropped fungling's fall (-1 = none). */
	private float tentacleCalm;
	private float roarTremble;
	/** The split's strain before the skull gives (0..1): the head shudders on its neck. */
	private float splitShudder;
	private float fungDrop = -1.0F;
	@Override
	public void setupAnim(MycophantEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
			float headPitch) {
		parts.forEach(ModelPart::resetPose);
		float partial = ageInTicks - entity.tickCount;
		float time = ageInTicks / 20.0F;
		// every synced counter is replayed on the client clock, so a burst or a gap in the server's updates never stalls
		// or skips a beat
		MycophantAnimState motion = MycophantAnimState.of(entity);
		motion.update(entity, partial);
		MycophantState state = entity.getState();
		float gait = motion.gait(partial);
		float split = entity.getPhase() >= 2 ? 1.0F : 0.0F;
		float splitT = state == MycophantState.SPLIT ? motion.splitTime() : (split > 0 ? 999.0F : 0.0F);
		// the siphon swings onto its target and, once it lets go, back off it along the last line it fed on
		float latch = motion.latch(partial);
		boolean aimed = entity.getSiphonTargetId() >= 0 && aimSiphon(entity, partial);
		if (aimed) motion.rememberAim(this.siphonAim);
		this.siphonLatch = latch > 0.0F && (aimed || motion.recallAim(this.siphonAim)) ? latch : 0.0F;
		this.tentacleCalm = 0.0F;
		this.roarTremble = 0.0F;
		this.splitShudder = 0.0F;
		this.fungDrop = -1.0F;
		boolean cradle = false;

		String review = reviewPose;
		if (review != null) {
			boolean opened = review.endsWith("_p2");
			String name = opened ? review.substring(0, review.length() - 3) : review;
			this.pose.copy(switch (name) {
				case "tending" -> TENDING;
				case "notice" -> NOTICE;
				case "roar" -> ROAR;
				case "embrace" -> EMBRACE;
				case "hold" -> HOLD;
				case "spawn" -> SPAWN;
				case "guard" -> GUARD;
				case "sweep" -> SWEEP;
				case "siphon" -> SIPHON_AIM;
				case "pluck" -> PLUCK;
				case "bloom" -> BLOOM;
				case "stagger" -> STAGGER;
				case "yield" -> YIELDED;
				case "rake_l" -> RAKE_L;
				case "rake_r" -> RAKE_R;
				case "slam" -> SLAM;
				case "settle_rise" -> SETTLE_RISE;
				case "settle_drop" -> SETTLE_DROP;
				case "pin" -> PIN;
				case "tail_coil" -> TAIL_COIL;
				case "tail_lash" -> TAIL_LASH;
				case "vents_open" -> VENTS_OPEN;
				case "hand_wind" -> HAND_WIND;
				case "hand_sweep" -> HAND_SWEEP;
				case "spit" -> SPIT;
				case "mortar" -> MORTAR;
				case "nurse" -> NURSE;
				case "lances" -> LANCES;
				case "net" -> NET;
				case "gaze" -> GAZE;
				case "last_brood_rear" -> LAST_BROOD_REAR;
				case "last_brood_pull" -> LAST_BROOD_PULL;
				case "last_brood_collapse" -> LAST_BROOD_COLLAPSE;
				default -> STALK;
			});
			gait = name.equals("drag") ? 1.0F : 0.0F;
			cradle = name.equals("tending");
			// the wing and pulp moves only exist with the head opened
			boolean phaseTwo = switch (name) {
				case "lances", "net", "gaze", "last_brood_rear", "last_brood_pull", "last_brood_collapse" -> true;
				default -> false;
			};
			splitT = opened || phaseTwo ? 999.0F : 0.0F;
		} else {
			switch (state) {
				case INTRO -> cradle = introPose(entity, motion.introTime());
				case ATTACK -> attackPose(entity, motion, partial, ageInTicks);
				case STAGGER -> {
					float s = motion.stateTime();
					if (motion.staggeredOutOfLastBrood()) {
						lastBroodCollapse(s, motion.lastBroodCutFree());
					} else {
						float rise = HemoEasing.smoothstep(s / 6.0F) * (1.0F - HemoEasing.smoothstep((s - 10.0F) / 20.0F));
						this.pose.mix(STALK, STAGGER, rise);
					}
				}
				case SPLIT -> splitPose(splitT);
				case YIELD -> {
					float y = motion.yieldTime();
					this.pose.mix(STALK, YIELDED, HemoEasing.smoothstep(MycophantYieldRules.lower(y) * 0.6F + MycophantYieldRules.fold(y) * 0.4F));
					this.pose.frill = Mth.lerp(MycophantYieldRules.fold(y), STALK.frill, -0.3F);
				}
				default -> this.pose.copy(STALK);
			}
			blendFromLast(entity, motion, ageInTicks);
		}
		apply(entity, time, gait, ageInTicks, splitT, cradle);
	}

	/**
	 * A new state, attack or hold starts from wherever she was drawn last, not from the authored start pose: a stagger
	 * landing mid-swing, a catch mid-lunge or a release mid-squeeze all ease over instead of popping.
	 */
	private void blendFromLast(MycophantEntity entity, MycophantAnimState motion, float age) {
		MycophantState state = entity.getState();
		int attack = state == MycophantState.ATTACK ? entity.getAttack().ordinal() + 1 : 0;
		motion.begin((state.ordinal() * 64 + attack) * 2 + (entity.isHolding() ? 1 : 0), age, Pose.SIZE);
		float blend = motion.blend(age);
		if (blend < 1.0F) this.pose.mix(this.poseA.read(motion.from()), this.pose, blend);
		this.pose.write(motion.last());
	}

	/**
	 * The waking, beat by beat: she tends the fungling with her back turned, perks up and goes still, pivots round on
	 * her claws, lets the fungling fall, draws back low, rears into the roar, then settles forward into the rush's
	 * starting stance so the Rushing Embrace's wind-up picks up without a pop. Returns true while she still cradles it.
	 */
	private boolean introPose(MycophantEntity entity, float t) {
		boolean rematch = entity.isRematchIntro();
		float notice = MycophantIntroRules.tickOf(MycophantIntroRules.Beat.NOTICE, rematch);
		float roar = MycophantIntroRules.tickOf(MycophantIntroRules.Beat.ROAR, rematch);
		float rush = MycophantIntroRules.tickOf(MycophantIntroRules.Beat.RUSH, rematch);
		float drop = rematch ? -1.0F : MycophantIntroRules.tickOf(MycophantIntroRules.Beat.DROP, false);
		float settle = rush - 18.0F;
		if (t >= roar - 10.0F) {
			roarPose(t - roar, settle - roar, rush - settle);
			return false;
		}
		if (!rematch && t < notice) {
			// tending: rocking over the fungling, the siphon feeding it in slow swallows, the hand stroking
			this.pose.copy(TENDING);
			float rock = Mth.sin(t * 0.09F);
			this.pose.head += rock * 0.07F;
			this.pose.neck += rock * 0.03F;
			this.pose.curl += Mth.sin(t * 0.21F) * 0.18F;
			this.pose.grip += Mth.sin(t * 0.13F) * 0.08F;
			return true;
		}
		// the notice: the head snaps up a little past its mark and the tentacles freeze, listening
		float perk = HemoEasing.smoothstep((t - notice) / 7.0F);
		float overshoot = Mth.sin(Mth.clamp((t - notice) / 14.0F, 0.0F, 1.0F) * Mth.PI) * 0.12F;
		if (rematch) this.pose.copy(NOTICE);
		else this.pose.mix(TENDING, NOTICE, perk);
		this.pose.head += overshoot;
		this.tentacleCalm = perk;
		if (rematch || t < drop) return !rematch;
		// the drop: the hand opens and lowers, and the fungling falls away into the nectar
		float release = HemoEasing.smoothstep((t - drop) / 6.0F);
		this.pose.mix(this.poseA.copy(this.pose), STALK, HemoEasing.smoothstep((t - drop) / 16.0F));
		this.pose.grip = Mth.lerp(release, NOTICE.grip, 0.0F);
		this.fungDrop = Mth.clamp((t - drop) / 9.0F, 0.0F, 1.0F);
		return t < drop + 9.0F;
	}

	/** The roar from ten ticks before it (r = ticks since the roar): draw back low, rear and bellow, then settle forward. */
	private void roarPose(float r, float hold, float settleTicks) {
		Pose crouch = this.poseB.copy(STALK);
		crouch.thorax = -0.18F;
		crouch.neck = -0.2F;
		crouch.head = -0.15F;
		crouch.height = -2.5F;
		crouch.frill = 0.2F;
		crouch.maw = 0.1F;
		if (r < 0.0F) {
			this.pose.mix(STALK, crouch, HemoEasing.smoothstep((r + 10.0F) / 10.0F));
			this.tentacleCalm = 1.0F - HemoEasing.smoothstep((r + 10.0F) / 10.0F);
		} else if (r < hold) {
			// a fast rear with a small overshoot, then the bellow held with a tremble
			float rear = HemoEasing.smoothstep(r / 6.0F);
			this.pose.mix(crouch, ROAR, rear);
			this.pose.thorax += Mth.sin(Mth.clamp(r / 12.0F, 0.0F, 1.0F) * Mth.PI) * 0.08F;
			this.roarTremble = rear * (1.0F - HemoEasing.smoothstep((r - hold + 8.0F) / 8.0F));
			this.pose.maw = ROAR.maw * (0.85F + Mth.sin(r * 1.7F) * 0.15F * this.roarTremble);
		} else {
			// settle low and forward: the stance the Rushing Embrace's wind-up starts from
			this.pose.mix(ROAR, STALK, HemoEasing.smoothstep((r - hold) / settleTicks));
		}
	}

	/**
	 * The split, by the controller's tick: she hunches and strains with her head bowed while the seam opens, rears into a
	 * bellow as the halves snap apart, throbs forward at the push, lifts into a display as the wings unfurl, then lowers
	 * into her ordinary stance so phase 2 picks up without a pop.
	 */
	private void splitPose(float s) {
		float snap = MycophantSplitRules.snapTick();
		if (s < snap) {
			this.pose.mix(STALK, STRAIN, HemoEasing.smoothstep(s / 8.0F));
			this.splitShudder = MycophantSplitRules.crack(s);
			return;
		}
		float r = s - snap;
		float rear = HemoEasing.smoothstep(r / 5.0F);
		float wings = MycophantSplitRules.wings(s);
		this.pose.mix(STRAIN, this.poseB.mix(ROAR, SPLIT_DISPLAY, wings), rear);
		this.pose.thorax += Mth.sin(Mth.clamp(r / 10.0F, 0.0F, 1.0F) * Mth.PI) * 0.1F;
		// the push: a forward throb as the pulp blooms and the shove lands
		float throb = Mth.sin(Mth.clamp((s - MycophantSplitRules.pushTick() + 3.0F) / 10.0F, 0.0F, 1.0F) * Mth.PI);
		this.pose.thorax -= throb * 0.12F;
		this.pose.head -= throb * 0.1F;
		this.pose.tentFwd += throb * 0.6F;
		// the bellow trembles until the wings take over
		this.roarTremble = rear * (1.0F - HemoEasing.smoothstep((s - 46.0F) / 12.0F));
		this.pose.maw *= 0.85F + Mth.sin(r * 1.7F) * 0.15F * this.roarTremble;
		// the collar stays folded through the snap so the halves swing clear in silhouette, then flares as the pulp blooms
		this.pose.frill = Mth.lerp(HemoEasing.smoothstep((s - 24.0F) / 14.0F), STRAIN.frill, this.pose.frill);
		float settle = MycophantSplitRules.settle(s);
		if (settle > 0.0F) this.pose.mix(this.poseA.copy(this.pose), STALK, settle);
	}

	private void attackPose(MycophantEntity entity, MycophantAnimState motion, float partial, float ageInTicks) {
		MycophantAttack attack = entity.getAttack();
		int phase = entity.getPhase();
		int variant = entity.getAttackVariant();
		BossMoveTimeline timeline = MycophantControllerRules.timeline(attack, phase, variant);
		float t = motion.attackTime();
		float w = Math.max(1, timeline.windup());
		float a = Math.max(1, timeline.active());
		float r = Math.max(1, timeline.recovery());
		float wind = HemoEasing.smoothstep(t / w);
		float act = HemoEasing.smoothstep((t - w) / a);
		float rec = HemoEasing.smoothstep((t - w - a) / r);
		switch (attack) {
			case RUSHING_EMBRACE, FUSION_EMBRACE -> {
				if (entity.isHolding()) {
					// the attack clock stands still through a hold, so the slow head work runs on the entity's age
					this.pose.copy(HOLD);
					float squeeze = motion.claimMeter(partial);
					this.pose.grip = Mth.lerp(squeeze, 0.7F, 1.0F);
					this.pose.thorax -= squeeze * 0.08F;
					this.pose.head += Mth.sin(ageInTicks * 0.2F) * 0.05F;
				} else if (t < w) {
					this.pose.mix(STALK, ROAR, wind);
				} else if (t < w + a) {
					this.pose.mix(ROAR, EMBRACE, HemoEasing.smoothstep((t - w) / (a * 0.5F)));
				} else {
					this.pose.mix(EMBRACE, STALK, rec);
				}
				if (attack == MycophantAttack.FUSION_EMBRACE && !entity.isHolding()) this.pose.wing[1] = Mth.lerp(act, 0.4F, 1.3F);
			}
			case SIPHON_FEED -> {
				if (t < w) this.pose.mix(STALK, SIPHON_AIM, wind);
				else if (t < w + a) this.pose.copy(SIPHON_AIM);
				else this.pose.mix(SIPHON_AIM, STALK, rec);
			}
			case PLUCK -> {
				if (t < w) this.pose.mix(STALK, PLUCK_WIND, wind);
				else if (t < w + a) this.pose.mix(PLUCK_WIND, PLUCK, act);
				else this.pose.mix(PLUCK, STALK, rec);
			}
			case RAKE_STRING -> rakeString(variant, phase, t, w, w + a + r);
			case BROOD_CALL, NECTAR_SURGE -> {
				if (t < w) this.pose.mix(STALK, SPAWN, wind);
				else if (t < w + a) this.pose.copy(SPAWN);
				else this.pose.mix(SPAWN, STALK, rec);
				if (attack == MycophantAttack.NECTAR_SURGE) this.pose.frill = Math.max(this.pose.frill, wind * 0.9F);
			}
			case WING_GUARD -> {
				if (t < w) this.pose.mix(STALK, GUARD, wind);
				else if (t < w + a) this.pose.copy(GUARD);
				else this.pose.mix(GUARD, STALK, rec);
			}
			case WING_SWEEP -> {
				if (t < w) this.pose.mix(STALK, SWEEP_WIND, wind);
				else if (t < w + a) this.pose.mix(SWEEP_WIND, SWEEP, HemoEasing.smoothstep((t - w) / (a * 0.4F)));
				else this.pose.mix(SWEEP, STALK, rec);
			}
			case PULP_BLOOM -> {
				if (t < w) this.pose.mix(STALK, BLOOM_WIND, wind);
				else if (t < w + a) this.pose.mix(BLOOM_WIND, BLOOM, HemoEasing.smoothstep((t - w) / (a * 0.5F)));
				else this.pose.mix(BLOOM, STALK, rec);
			}
			case BROOD_SETTLE -> {
				if (t < w) {
					this.pose.mix(STALK, SETTLE_RISE, wind);
				} else if (t < w + a) {
					// the belly comes down fast, squashing a little as it lands
					this.pose.mix(SETTLE_RISE, SETTLE_DROP, HemoEasing.smoothstep((t - w) / 3.0F));
				} else {
					// she lies low and heaves through the punish window, getting up only at its end
					float lie = t - w - a;
					float up = HemoEasing.smoothstep((lie - (r - 14.0F)) / 14.0F);
					this.pose.mix(SETTLE_DROP, STALK, up);
					this.pose.height += Mth.sin(lie * 0.25F) * 0.5F * (1.0F - up);
				}
				this.pose.height -= bump((t - w - 2.0F) / 4.0F) * 1.5F;
			}
			case CLAW_PIN -> {
				if (entity.isHolding()) {
					// pressing down on the pinned player; the attack clock stands still through the pin
					this.pose.copy(PIN);
					this.pose.thorax += Mth.sin(ageInTicks * 0.35F) * 0.02F;
					this.pose.reachR[1] -= Math.abs(Mth.sin(ageInTicks * 0.3F)) * 1.5F;
				} else if (t < w) {
					this.pose.mix(STALK, PIN_WIND, wind);
				} else if (t < w + a) {
					this.pose.mix(PIN_WIND, PIN, act);
				} else {
					this.pose.mix(PIN, STALK, rec);
				}
			}
			case TAIL_LASH -> {
				// coil through the wind-up, then lash in the swing ticks up to each hit, across and (variant 1) back
				int[] hits = com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantStrikeRules.tailHits(variant);
				this.pose.mix(STALK, TAIL_COIL, HemoEasing.smoothstep(t / Math.max(1.0F, w + hits[0] - SWING_TICKS)));
				for (int i = 0; i < hits.length; i++) {
					float lash = HemoEasing.smoothstep((t - (w + hits[i] - SWING_TICKS)) / SWING_TICKS);
					if (lash > 0.0F) this.pose.mix(this.poseA.copy(this.pose), i % 2 == 0 ? TAIL_LASH : TAIL_LASH_BACK, lash);
				}
				if (rec > 0.0F) this.pose.mix(this.poseA.copy(this.pose), STALK, rec);
			}
			case VENT_BLOOM -> {
				if (t < w) this.pose.mix(STALK, VENTS_OPEN, wind);
				else if (t < w + a) this.pose.copy(VENTS_OPEN);
				else this.pose.mix(VENTS_OPEN, STALK, rec);
				// the cloud puffs out as the move commits
				float puff = bump((t - w) / a);
				this.pose.vents += puff * 0.4F;
				this.pose.abdLift += puff * 0.08F;
			}
			case HAND_SWEEP -> {
				if (t < w) {
					this.pose.mix(STALK, HAND_WIND, wind);
				} else if (t < w + a) {
					// the hand swings out ahead of her on its way across, staying low
					float s = HemoEasing.smoothstep((t - w) / a);
					this.pose.mix(HAND_WIND, HAND_SWEEP, s);
					float arc = Mth.sin(s * Mth.PI);
					this.pose.hand[2] -= arc * 24.0F;
					this.pose.hand[1] -= arc * 4.0F;
				} else {
					this.pose.mix(HAND_SWEEP, STALK, rec);
				}
			}
			case SIPHON_SPIT -> {
				if (t < w) this.pose.mix(STALK, SPIT, wind);
				else if (t < w + a) this.pose.copy(SPIT);
				else this.pose.mix(SPIT, STALK, rec);
				// each glob swells the siphon and spits forward, peaking on its release tick
				float spit = 0.0F;
				int[] globs = com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantStrikeRules.globTicks(phase);
				for (int g : globs) spit += bump((t - w - g + 2.0F) / 4.0F);
				this.pose.siphonT[1] -= spit * 8.0F;
				this.pose.siphonT[2] -= spit * 14.0F;
				this.pose.curl += spit * 0.6F;
				this.pose.thorax += spit * 0.04F;
				this.pose.maw += spit * 0.2F;
			}
			case SPORE_MORTAR -> {
				if (t < w) this.pose.mix(STALK, MORTAR, wind);
				else if (t < w + a) this.pose.copy(MORTAR);
				else this.pose.mix(MORTAR, STALK, rec);
				// the vents pump twice through the tell, then three times as the eggs go up
				float pump = t < w ? 0.4F * sinSquared(t / w * 2.0F) : (t < w + a ? sinSquared((t - w) / a * 3.0F) : 0.0F);
				this.pose.abdLift += pump * 0.14F;
				this.pose.vents += pump * 0.35F;
				this.pose.height += pump * 0.8F;
			}
			case NURSING -> {
				if (t < w) this.pose.mix(STALK, NURSE, wind);
				else if (t < w + a) this.pose.copy(NURSE);
				else this.pose.mix(NURSE, STALK, rec);
				// rocking over the fungling in slow swallows, on the entity's age so a broken feeding never jumps
				float tend = HemoEasing.smoothstep((t - w) / 6.0F) * (1.0F - rec);
				float rock = Mth.sin(ageInTicks * 0.09F);
				this.pose.head += rock * 0.07F * tend;
				this.pose.neck += rock * 0.03F * tend;
				this.pose.curl += Mth.sin(ageInTicks * 0.21F) * 0.18F * tend;
				this.pose.grip += Mth.sin(ageInTicks * 0.13F) * 0.08F * tend;
			}
			case HYPHAE_LANCES -> {
				if (t < w) {
					this.pose.mix(STALK, LANCES_RISE, wind);
				} else if (t < w + a) {
					this.pose.mix(LANCES_RISE, LANCES, HemoEasing.smoothstep((t - w) / SWING_TICKS));
				} else {
					this.pose.mix(LANCES, STALK, rec);
				}
				// the spars stay driven in, straining, while the line erupts
				this.pose.thorax += Mth.sin(ageInTicks * 1.7F) * 0.012F * act * (1.0F - rec);
			}
			case LATTICE_NET -> {
				if (t < w) this.pose.mix(STALK, NET, wind);
				else if (t < w + a) this.pose.copy(NET);
				else this.pose.mix(NET, STALK, rec);
				// the cast: the wings throw forward as the net goes out
				float cast = bump((t - w) / a);
				this.pose.wing[1] += cast * 0.3F;
				this.pose.thorax -= cast * 0.1F;
				this.pose.spar -= cast * 0.2F;
			}
			case GAZE -> {
				if (t < w) this.pose.mix(STALK, GAZE, wind);
				else if (t < w + a) this.pose.copy(GAZE);
				else this.pose.mix(GAZE, STALK, rec);
				// once the beam sweeps, the head follows it (relative to her body, as far as the neck allows)
				float follow = HemoEasing.smoothstep((t - w) / SWING_TICKS) * (1.0F - rec);
				float bodyYaw = Mth.rotLerp(partial, entity.yBodyRotO, entity.yBodyRot);
				float relative = Mth.clamp(Mth.wrapDegrees(motion.gazeYaw(partial) - bodyYaw), -70.0F, 70.0F);
				this.pose.turn += relative * Mth.DEG_TO_RAD * follow;
				this.pose.pulpFwd += Mth.sin(ageInTicks * 0.9F) * 0.4F * follow;
			}
			case LAST_BROOD -> lastBrood(entity, t, w, ageInTicks);
			case NONE -> this.pose.copy(STALK);
		}
	}

	/**
	 * The Rake String: left, right (and left again in phase 2's long form), then for variants 1 and 2 a crouched
	 * hesitation with both claws cocked overhead and the slam. Each blow strikes in the {@link #SWING_TICKS} up to its
	 * hit and eases back out over as many after it (the last one over the recovery); the next claw lifts while the last
	 * one returns, so the string flows without a pause between blows.
	 */
	private void rakeString(int variant, int phase, float t, float w, float end) {
		int[] hits = com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantStrikeRules.stringHits(variant, phase);
		this.pose.copy(STALK);
		float previous = 0.0F;
		for (int i = 0; i < hits.length; i++) {
			float hit = w + hits[i];
			boolean slam = com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantStrikeRules.isSlam(variant, i, hits.length);
			boolean last = i == hits.length - 1;
			boolean left = i % 2 == 0;
			Pose up = slam ? SLAM_COCK : (left ? RAKE_L_UP : RAKE_R_UP);
			Pose struck = slam ? SLAM : (left ? RAKE_L : RAKE_R);
			float strikeStart = hit - SWING_TICKS;
			// the slam cocks quickly, then holds the hesitation until it drops
			float raiseTicks = Math.max(1.0F, slam ? Math.min(6.0F, strikeStart - previous) : strikeStart - previous);
			float raise = HemoEasing.smoothstep((t - previous) / raiseTicks);
			float strike = HemoEasing.smoothstep((t - strikeStart) / SWING_TICKS);
			float back = last ? HemoEasing.smoothstep((t - hit - 3.0F) / Math.max(SWING_TICKS, (end - hit - 3.0F) * 0.7F))
					: HemoEasing.smoothstep((t - hit) / SWING_TICKS);
			this.pose.add(STALK, up, raise * (1.0F - strike));
			this.pose.add(STALK, struck, strike * (1.0F - back));
			if (slam) {
				// the hesitation creeps: she sinks a little lower and the claws lift a little higher before they drop
				float creep = HemoEasing.smoothstep((t - previous - raiseTicks) / Math.max(1.0F, strikeStart - previous - raiseTicks))
						* (1.0F - strike);
				this.pose.height -= creep * 1.2F;
				this.pose.thorax += creep * 0.04F;
				this.pose.reachL[1] += creep * 6.0F;
				this.pose.reachR[1] += creep * 6.0F;
			}
			previous = hit;
		}
	}

	/**
	 * The Last Brood: she rears and roars through the tell, sinks into the anchored pull with the sac swollen open as it
	 * runs out, and strains there; a cocooned player is held against the opened sac.
	 */
	private void lastBrood(MycophantEntity entity, float t, float w, float ageInTicks) {
		if (entity.isHolding()) {
			this.pose.copy(HOLD);
			this.pose.sac = 1.0F;
			this.pose.frill = 1.0F;
			this.pose.grip = 0.9F;
			this.pose.thorax -= 0.06F;
			this.pose.head += Mth.sin(ageInTicks * 0.2F) * 0.05F;
			return;
		}
		if (t < w) {
			float rear = HemoEasing.smoothstep(t / 8.0F);
			this.pose.mix(STALK, LAST_BROOD_REAR, rear);
			this.roarTremble = rear * (1.0F - HemoEasing.smoothstep((t - (w - 14.0F)) / 6.0F));
			this.pose.maw *= 0.85F + Mth.sin(t * 1.7F) * 0.15F * this.roarTremble;
			this.pose.mix(this.poseA.copy(this.pose), LAST_BROOD_PULL, HemoEasing.smoothstep((t - (w - 8.0F)) / 8.0F));
			return;
		}
		this.pose.copy(LAST_BROOD_PULL);
		float strain = HemoEasing.smoothstep((t - w) / 10.0F);
		this.pose.thorax += Mth.sin(ageInTicks * 1.9F) * 0.02F * strain;
		this.pose.head += Mth.sin(ageInTicks * 1.3F) * 0.03F * strain;
		this.pose.sac += Mth.sin(ageInTicks * 0.45F) * 0.06F * strain;
	}

	/**
	 * The stagger after the Last Brood: cut free, she falls from the pull onto her belly with the sac torn and lies
	 * there through the collapse, rising only at its end; spent, she sags halfway for the shorter stagger.
	 */
	private void lastBroodCollapse(float s, boolean cutFree) {
		float duration = cutFree ? com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantLastBroodRules.COLLAPSE
				: com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantLastBroodRules.SPENT;
		Pose low = cutFree ? LAST_BROOD_COLLAPSE : this.poseB.mix(STALK, LAST_BROOD_COLLAPSE, 0.55F);
		this.pose.mix(LAST_BROOD_PULL, low, HemoEasing.smoothstep(s / 10.0F));
		this.pose.height -= bump((s - 7.0F) / 5.0F) * (cutFree ? 1.5F : 0.8F);
		this.pose.mix(this.poseA.copy(this.pose), STALK, HemoEasing.smoothstep((s - (duration - 16.0F)) / 16.0F));
	}

	/** 0 outside 0..1, rising to 1 at the middle and back with no jolt at either end. */
	private static float bump(float x) {
		return x <= 0.0F || x >= 1.0F ? 0.0F : sinSquared(x);
	}

	/** sin squared of x half-turns: x whole beats from 0 to 1 and back, smooth at every end. */
	private static float sinSquared(float x) {
		float s = Mth.sin(x * Mth.PI);
		return s * s;
	}

	/** Points the siphon at its latched target; returns false when the target is gone. */
	private boolean aimSiphon(MycophantEntity entity, float partial) {
		net.minecraft.world.entity.Entity target = entity.level().getEntity(entity.getSiphonTargetId());
		if (target == null) return false;
		double dx = Mth.lerp(partial, target.xo, target.getX()) - Mth.lerp(partial, entity.xo, entity.getX());
		double dy = Mth.lerp(partial, target.yo, target.getY()) + target.getBbHeight() * 0.6D - Mth.lerp(partial, entity.yo, entity.getY());
		double dz = Mth.lerp(partial, target.zo, target.getZ()) - Mth.lerp(partial, entity.zo, entity.getZ());
		float yaw = Mth.rotLerp(partial, entity.yBodyRotO, entity.yBodyRot) * Mth.DEG_TO_RAD;
		double fwd = dx * -Mth.sin(yaw) + dz * Mth.cos(yaw);
		double right = dx * -Mth.cos(yaw) + dz * -Mth.sin(yaw);
		// model space relative to the ground part: right is +X, up is -Y, forward is -Z; 1 block = 16 units
		this.siphonAim.set((float) (right * 16.0D), (float) (-dy * 16.0D), (float) (-fwd * 16.0D));
		return true;
	}


	private void apply(MycophantEntity entity, float time, float drag, float age, float splitTime, boolean cradle) {
		Pose p = this.pose;
		float breath = Mth.sin(time * 1.3F);
		float gait = MycophantMotionRules.dragPhase(age);
		float haul = Mth.sin(gait * Mth.TWO_PI * 2.0F) * drag;
		float rise = p.height + breath * 0.6F + Math.max(0.0F, haul) * 1.2F;
		body.y -= rise;
		body.z -= haul * 2.5F;
		body.zRot = Mth.sin(gait * Mth.TWO_PI) * 0.03F * drag;
		body.yRot = -body.zRot;
		thorax.xRot = -p.thorax - breath * 0.015F;
		thorax.yRot = -p.turn * 0.15F;
		neck.xRot = -p.neck;
		neck.yRot = -p.turn * 0.3F;
		head.xRot = -p.head + Mth.sin(age * 4.1F) * 0.03F * splitShudder;
		head.yRot = -p.turn * 0.55F - Mth.sin(time * 0.6F) * 0.06F * (1.0F - tentacleCalm)
				+ Mth.sin(age * 2.3F) * 0.05F * roarTremble + Mth.sin(age * 3.3F) * 0.05F * splitShudder;
		head.zRot = Mth.sin(time * 0.8F) * 0.04F + Mth.sin(age * 1.9F) * 0.06F * roarTremble
				+ Mth.sin(age * 2.6F + 2.0F) * 0.06F * splitShudder;

		abdomen.xRot = p.abdLift + 0.016F * breath;
		abdomen.yRot = p.abdYaw - body.zRot;
		abdomen.xScale = abdomen.yScale = 1.0F + p.vents * 0.03F * Mth.sin(time * 5.0F);
		for (ModelPart petal : ventPetals) {
			petal.yRot = -petal.x * p.vents * 0.10F;
			petal.xRot = petal.y * p.vents * 0.12F;
		}
		for (int side = 0; side < tails.length; side++) {
			for (int i = 0; i < tails[side].length; i++) {
				ModelPart link = tails[side][i];
				link.xRot = p.tailCurl * (i == 0 ? 0.48F : 0.32F) + Mth.sin(time * 1.8F - i * 0.6F + side) * 0.06F;
				link.yRot = p.tailSwing * (i == 0 ? 0.68F : 0.22F);
			}
		}

		float sacPulse = 1.0F + Mth.sin(time * 3.1F) * 0.02F + p.sac * 0.30F;
		sac.xScale = sac.yScale = sac.zScale = sacPulse;
		sac.z -= Math.max(0.0F, p.sac) * 6.0F;

		float flare = p.frill - STALK.frill;
		collar.xScale = collar.yScale = 1.0F + flare * 0.18F;
		for (int side = 0; side < frill.length; side++) {
			float sign = side == 0 ? -1.0F : 1.0F;
			for (int i = 0; i < frill[side].length; i++) {
				ModelPart lobe = frill[side][i];
				lobe.yRot = sign * (flare * 0.65F + Mth.sin(time * 2.0F + i * 1.7F) * 0.024F);
				lobe.xRot = -flare * (0.15F - i * 0.06F);
			}
		}
		maw.yScale = 0.85F + p.maw * 0.4F;
		maw.zScale = 0.9F + p.tongue * 0.25F;
		maw.y += (p.maw - STALK.maw) * 5.0F;
		maw.z -= (p.maw - STALK.maw) * 3.0F;
		for (int i = 0; i < feelers.length; i++) {
			for (int k = 0; k < feelers[i].length; k++) {
				ModelPart link = feelers[i][k];
				float wave = (1.0F - tentacleCalm * 0.85F) * (k == 0 ? 0.075F : 0.12F);
				link.xRot = -(p.tentFwd - STALK.tentFwd) * (k == 0 ? 0.4F : 0.09F)
						+ Mth.sin(time * 2.3F + i * 1.37F - k * 0.55F) * wave;
				link.yRot = Mth.cos(time * 1.6F + i - k * 0.5F) * wave;
			}
		}

		applySplit(splitTime, time, age);
		poseClaws(gait, drag, rise, haul);
		poseUpperArms(time, cradle);
		this.cradleBrood = cradle;

		MycophantAnimState state = MycophantAnimState.of(entity);
		Vec3 mouth = entity.getSiphonTargetId() < 0 ? null : modelOffset(siphonPose());
		Vec3 eye = entity.getAttack() == MycophantAttack.GAZE
				? modelOffset(transform(root, body, thorax, neck, head, pulp, gazeEye)) : null;
		state.focalPoints(entity.tickCount, mouth, eye);
	}

	void applySplit(float tick, float time, float age) {
		boolean open = tick >= 999.0F;
		float crack = open ? 1.0F : MycophantSplitRules.crack(tick);
		float hinge = open ? 1.0F : MycophantSplitRules.hinge(tick);
		float droop = open ? 1.0F : MycophantSplitRules.droop(tick);
		float bloom = open ? 1.0F : MycophantSplitRules.pulp(tick);
		float growth = open ? 1.0F : MycophantSplitRules.wings(tick);
		float seam = hinge > 0.0F ? 0.0F : crack * (0.025F + 0.035F * Math.abs(Mth.sin(age * 2.7F)));
		float swing = hinge * (55.0F * Mth.DEG_TO_RAD) + seam;
		float roll = droop * (13.0F * Mth.DEG_TO_RAD);
		skullL.xRot = skullR.xRot = droop * (6.0F * Mth.DEG_TO_RAD);
		skullL.yRot = swing;
		skullR.yRot = -swing;
		skullL.zRot = roll;
		skullR.zRot = -roll;
		maw.visible = hinge < 0.42F;
		pulp.visible = bloom > 0.0F;
		float push = Mth.sin(Mth.clamp((tick - MycophantSplitRules.pushTick() + 2.0F) / 10.0F, 0.0F, 1.0F) * Mth.PI);
		pulp.xScale = pulp.yScale = pulp.zScale = Mth.lerp(bloom, 0.35F, 1.0F) + push * 0.08F;
		pulp.z = pulp.getInitialPose().z + (1.0F - bloom) * 4.0F - pose.pulpFwd;
		for (int i = 0; i < pulpPetals.length; i++) {
			for (int k = 0; k < pulpPetals[i].length; k++) {
				ModelPart link = pulpPetals[i][k];
				link.xRot = Mth.sin(time * 3.0F + i + k * 0.8F) * 0.15F * bloom;
				link.zRot = Mth.cos(time * 2.4F + i * 2.0F + k * 0.6F) * 0.13F * bloom;
			}
		}

		float fan = HemoEasing.smoothstep(growth);
		float shiver = open ? 0.0F : bump((tick - 62.0F) / 16.0F);
		float wingScale = Mth.lerp(1.0F - (1.0F - growth) * (1.0F - growth), 0.08F, 1.0F);
		poseWing(wingL, -1.0F, fan, wingScale, time, shiver);
		poseWing(wingR, 1.0F, fan, wingScale, time, shiver);
	}

	private void poseWing(ModelPart wing, float side, float fan, float scale, float time, float shiver) {
		wing.visible = fan > 0.0F;
		wing.xScale = wing.yScale = wing.zScale = scale;
		wing.xRot = -pose.wing[0] + pose.spar * 0.3F;
		wing.yRot = side * (pose.wing[1] - STALK.wing[1] - (1.0F - fan) * 0.95F);
		wing.zRot = side * (-(pose.wing[2] - STALK.wing[2]) + (1.0F - fan) * 0.4F
				+ Mth.sin(time * 1.1F) * 0.035F + Mth.sin(time * 9.0F) * 0.08F * shiver);
	}

	private void poseClaws(float gait, float drag, float rise, float haul) {
		for (int i = 0; i < claws.length; i++) {
			boolean front = i < 2;
			float side = i % 2 == 0 ? -1.0F : 1.0F;
			float[] target = front ? pose.front : pose.second;
			float[] rest = front ? STALK.front : STALK.second;
			float[] reach = i % 2 == 0 ? pose.reachL : pose.reachR;
			float x = (target[0] - rest[0]) * side;
			float y = target[1] - rest[1];
			float z = target[2] - rest[2];
			float phase = (gait + ((front == (side > 0)) ? 0.5F : 0.0F)) % 1.0F;
			if (phase < 0.6F) {
				z += 22.0F * (phase / 0.6F - 0.5F) * drag;
			} else {
				float recovery = (phase - 0.6F) / 0.4F;
				z += 22.0F * (0.5F - recovery) * drag;
				y += Mth.sin(recovery * Mth.PI) * 16.0F * drag;
			}
			if (front) {
				x += reach[0];
				y += reach[1];
				z += reach[2];
			}
			MycophantLimb claw = claws[i];
			claw.pose(claw.target(x, rise - y, z + haul * 2.5F), true);
			claw.wrist.xRot -= Mth.clamp(y / 60.0F, 0.0F, 0.75F);
			float open = Mth.clamp((y - 8.0F) / 32.0F, 0.0F, 1.0F);
			jaws[i].yRot = side * (0.08F * drag + open * 0.58F);
		}
	}

	private void poseUpperArms(float time, boolean cradle) {
		Pose p = pose;
		float handReach = p.hand[2] - STALK.hand[2];
		grasping.pose(grasping.target((p.hand[0] - STALK.hand[0]) * 1.15F,
				STALK.hand[1] - p.hand[1], handReach), false, elbowGuide(p.handPole, STALK.handPole));
		grasping.wrist.xRot = (p.grip - STALK.grip) * 0.23F;
		for (int i = 0; i < fingers.length; i++) {
			fingers[i][0].yRot = (i - 1) * (1.0F - p.grip) * 0.22F;
			for (int k = 0; k < fingers[i].length; k++) fingers[i][k].xRot = (p.grip - STALK.grip) * (0.58F - k * 0.08F);
		}
		for (ModelPart link : thumb) link.yRot = -(p.grip - STALK.grip) * 0.32F;

		Vector3f feedTarget = feeding.target(p.siphonT[0] - STALK.siphonT[0], STALK.siphonT[1] - p.siphonT[1],
				p.siphonT[2] - STALK.siphonT[2]);
		Vector3f target = null;
		float aimWeight = siphonLatch;
		if (siphonLatch > 0.0F) {
			target = transform(root, body, thorax).last().pose().invert()
					.transformPosition(new Vector3f(siphonAim).div(16.0F).add(0.0F, RENDER_ORIGIN_Y, 0.0F)).mul(16.0F);
			feedTarget.lerp(feeding.fromShoulder(new Vector3f(target), 38.0F), siphonLatch);
		} else if (cradle) {
			PoseStack held = new PoseStack();
			grasping.transformWrist(held);
			broodSocket.translateAndRotate(held);
			target = held.last().pose().getTranslation(new Vector3f()).mul(16.0F).add(0.0F, -6.0F, 0.0F);
			aimWeight = 1.0F - HemoEasing.smoothstep(fungDrop);
			feedTarget.lerp(feeding.fromShoulder(new Vector3f(target), 22.0F), aimWeight);
		}
		feeding.pose(feedTarget, false, elbowGuide(p.siphonPole, STALK.siphonPole));
		if (target != null) feeding.aimWrist(target, new Vector3f(-0.28F, -0.22F, -0.93F).normalize(), aimWeight);
		for (int i = 0; i < siphon.length; i++) {
			float rest = (p.curl - STALK.curl) * 0.17F + Mth.sin(time * 2.2F - i * 0.55F) * 0.065F;
			siphon[i].xRot = Mth.lerp(aimWeight, rest, Mth.sin(time * 4.0F - i * 0.7F) * 0.042F);
			siphon[i].yRot = Mth.cos(time * 1.6F - i * 0.5F) * 0.05F * (1.0F - aimWeight);
		}
	}

	private static Vector3f elbowGuide(float[] pole, float[] rest) {
		return new Vector3f(pole[0] - rest[0], rest[1] - pole[1], pole[2] - rest[2]).mul(0.65F);
	}

	private PoseStack siphonPose() {
		PoseStack poses = transform(root, body, thorax);
		feeding.transformWrist(poses);
		for (ModelPart link : siphon) link.translateAndRotate(poses);
		siphonMouth.translateAndRotate(poses);
		return poses;
	}

	private static PoseStack transform(ModelPart... parts) {
		PoseStack poses = new PoseStack();
		for (ModelPart part : parts) part.translateAndRotate(poses);
		return poses;
	}

	private static Vec3 modelOffset(PoseStack poses) {
		Vector3f point = poses.last().pose().getTranslation(new Vector3f());
		return new Vec3(point.x, RENDER_ORIGIN_Y - point.y, -point.z);
	}

	public void renderBrood(PoseStack poses, ModelPart fungling, VertexConsumer buffer, int light, int overlay) {
		if (!cradleBrood) return;
		poses.pushPose();
		root.translateAndRotate(poses);
		body.translateAndRotate(poses);
		thorax.translateAndRotate(poses);
		grasping.transformWrist(poses);
		broodSocket.translateAndRotate(poses);
		float drop = Math.max(0.0F, fungDrop);
		float fall = drop * drop;
		poses.translate(0.0F, fall * 3.25F, -drop * 0.5F);
		poses.mulPose(Axis.XP.rotation(fall * 2.4F));
		float scale = 0.92F - fall * 0.4F;
		poses.scale(scale, scale, scale);
		poses.translate(0.0F, -1.5F, 0.0F);
		fungling.render(poses, buffer, light, overlay);
		poses.popPose();
	}
}
