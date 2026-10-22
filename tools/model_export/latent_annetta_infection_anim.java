	/*
	 * How it moves. The body is a serpent standing on its own coils: the coil base never leaves the floor, and every
	 * motion is a curve sent up the chain from it. Each attack is written once, as channels on a Drive (lean, lift,
	 * twist, arm swings...), and each joint samples that drive a few ticks earlier or later than the torso: the coils
	 * lead, the head, arms, hands and whip follow. So a blow starts low in the body and arrives at the claws a beat
	 * later, overshoots and settles, instead of every joint turning at once like a person's.
	 */

	/** Coil weights, base to waist: how much of the body's lean, lift (rearing up), side bend and twist each takes. */
	private static final float[] COIL_LEAN = {0.08F, 0.16F, 0.22F, 0.24F, 0.22F};
	private static final float[] COIL_LIFT = {0.42F, 0.34F, 0.08F, -0.32F, -0.34F};
	private static final float[] COIL_SIDE = {0.12F, 0.2F, 0.24F, 0.22F, 0.18F};
	private static final float[] COIL_TWIST = {0.0F, 0.04F, 0.1F, 0.16F, 0.2F};
	/** Ticks each coil samples the drive ahead of the torso: the base moves first. */
	private static final float COIL_LEAD = 1.6F;
	private static final float LAG_HEAD = 1.2F;
	private static final float LAG_SHOULDER = 0.7F;
	private static final float LAG_FOREARM = 1.5F;
	private static final float LAG_HAND = 2.3F;

	/** Every channel one moment of an action writes; zero is the resting pose. Angles are radians. */
	private static final class Drive {
		float lean, lift, side, twist, slide;
		float headPitch, headYaw, headRoll;
		float upX, upZ, upFore, upHand, loX, loZ, loFore, loHand, curl, tremor;
		float whipRaise, whipSweep, whipOut, uncoil, whipWave, crack;
		float tailSwing, tailCurl, busy;

		void clear() {
			lean = lift = side = twist = slide = 0.0F;
			headPitch = headYaw = headRoll = 0.0F;
			upX = upZ = upFore = upHand = loX = loZ = loFore = loHand = curl = tremor = 0.0F;
			whipRaise = whipSweep = whipOut = crack = 0.0F;
			uncoil = 0.1F;
			whipWave = 1.0F;
			tailSwing = tailCurl = busy = 0.0F;
		}
	}

	private final Drive drive = new Drive();

	private static float ramp(float t, float from, float to) {
		float x = Mth.clamp((t - from) / (to - from), 0.0F, 1.0F);
		return x * x * (3.0F - 2.0F * x);
	}

	/** Rises over in..peak and falls over peak..out: one smooth beat of an attack. */
	private static float beat(float t, float in, float peak, float out) {
		return ramp(t, in, peak) * (1.0F - ramp(t, peak, out));
	}

	/** Strikes from 0 to 1 over from..to, fast out of the gate, carrying past 1 and settling back: a blow's snap. */
	private static float snap(float t, float from, float to) {
		float x = Mth.clamp((t - from) / (to - from), 0.0F, 1.0F) - 1.0F;
		return 1.0F + 2.9F * x * x * x + 1.9F * x * x;
	}

	/** A damped shiver that starts at start: the body ringing after a blow. */
	private static float wobble(float t, float start, float speed, float decay) {
		if (t < start) return 0.0F;
		float d = t - start;
		return Mth.sin(d * speed) * (float) Math.exp(-d * decay);
	}

	/** Now and then its head jerks, birdlike and wrong: a short spike every few seconds, otherwise still. */
	private static float twitch(float ageInTicks, float period, float offset) {
		float cycle = (ageInTicks + offset) % period;
		return cycle < 5.0F ? Mth.sin(cycle / 5.0F * Mth.PI) : 0.0F;
	}

	/**
	 * Fills the drive for one joint. lag shifts that joint's clock: negative leads the torso, positive follows it.
	 * a is the attack tick, emerge the emergence tick, hurt ticks since the last blow, dying 0..1 as it dies.
	 */
	private static void sample(Drive d, LatentAnnettaInfectionEntity entity, LatentCombatRules.Attack attack, float a,
			float emerge, float hurt, float dying, float t, float lag) {
		d.clear();
		a -= lag;
		t -= lag;
		if (entity.isEmerging()) {
			// Pulled up out of her, crumpled, and unfolding from the floor up: the coils straighten first, then the
			// body, then the arms, writhing until it is free.
			float rise = LatentCombatRules.rise(emerge - lag);
			float folded = 1.0F - rise;
			d.lift = -0.8F * folded;
			d.lean = 0.7F * folded;
			d.side = 0.35F * Mth.sin(t * 0.33F) * folded;
			d.twist = 0.3F * Mth.sin(t * 0.21F + 1.0F) * folded;
			d.headPitch = 0.7F * folded;
			d.headRoll = 0.4F * Mth.sin(t * 0.4F) * folded;
			d.upX = d.loX = 0.5F * folded;
			d.upFore = d.loFore = -0.8F * folded;
			d.curl = 0.7F * folded;
			d.uncoil = 0.0F;
			d.whipWave = 1.0F + 2.0F * folded;
			d.tailCurl = 0.35F * folded;
			d.busy = folded;
		} else if (attack != null) {
			d.busy = 1.0F - ramp(a, LatentCombatRules.duration(attack) - 6.0F, LatentCombatRules.duration(attack));
			switch (attack) {
				case WHIP_LASH -> {
					// It rears and winds away from its target with the lash cocked back, then the whole body unwinds
					// into the swing and the lash cracks across a beat behind it.
					float draw = ramp(a, 0.0F, 10.0F);
					float strike = a < 10.0F ? 0.0F : snap(a, 10.0F, 15.0F);
					float rest = 1.0F - ramp(a, 18.0F, 30.0F);
					float wind = draw * (1.0F - strike);
					d.lift = 0.45F * wind + 0.1F * strike * rest;
					d.lean = -0.15F * wind + 0.4F * strike * rest;
					d.twist = (-0.5F * wind + 0.6F * strike) * rest;
					d.side = (0.2F * wind - 0.22F * strike) * rest;
					d.headYaw = -0.55F * d.twist;
					d.headPitch = -0.15F * wind + 0.2F * strike * rest;
					d.whipRaise = Mth.lerp(strike, -0.7F, -1.1F) * ramp(a, 0.0F, 8.0F) * rest;
					d.whipSweep = (-0.9F * wind + 1.15F * strike) * rest;
					d.whipOut = -0.6F * wind * rest;
					d.uncoil = 0.1F + 0.9F * Math.max(0.4F * draw, strike) * rest;
					d.crack = beat(a, 11.0F, 14.0F, 24.0F);
					d.whipWave = 0.5F;
					d.upZ = d.loZ = 0.35F * d.twist;
					d.upX = d.loX = 0.2F * wind;
					d.tailSwing = -0.7F * d.twist;
				}
				case SPEAR_THRUST -> {
					// It draws up and back over its coils with the lance cocked, then the coils throw the whole body
					// down and forward behind the point; it hangs there a moment and pulls back up.
					float draw = ramp(a, 0.0F, 12.0F);
					float strike = a < 12.0F ? 0.0F : snap(a, 12.0F, 17.0F);
					float rest = 1.0F - ramp(a, 20.0F, 32.0F);
					float wind = draw * (1.0F - strike);
					d.lift = 0.7F * wind;
					d.lean = -0.12F * wind + 0.6F * strike * rest;
					d.twist = 0.32F * wind - 0.22F * strike * rest;
					d.slide = -2.5F * strike * rest;
					d.headPitch = -0.25F * wind + 0.15F * strike * rest;
					d.headYaw = -0.4F * d.twist;
					d.upX = d.loX = -0.1F * wind - 0.3F * strike * rest;
					d.upFore = d.loFore = 0.35F * wind - 0.2F * strike * rest;
					d.curl = 0.3F * wind;
					d.tailCurl = 0.18F * wind;
					d.tailSwing = 0.25F * strike * rest;
					d.whipWave = 1.5F;
					d.whipOut = -0.3F * wind;
				}
				case CLAW_RAKE -> {
					// Two rakes, one claw then the other: each rears overhead and comes down across the body, and the
					// body corkscrews into each cut while the tail swings the other way.
					float windOne = beat(a, 0.0F, 4.0F, 7.0F);
					float cutOne = ramp(a, 4.0F, 8.0F) * (1.0F - ramp(a, 9.0F, 14.0F));
					float windTwo = beat(a, 8.0F, 12.0F, 15.0F);
					float cutTwo = ramp(a, 12.0F, 16.0F) * (1.0F - ramp(a, 17.0F, 23.0F));
					d.loX = -1.1F * windOne + 1.0F * cutOne;
					d.loZ = 0.5F * windOne - 0.95F * cutOne;
					d.loFore = -0.5F * windOne + 0.5F * cutOne;
					d.loHand = 0.4F * cutOne;
					d.upX = -1.1F * windTwo + 1.0F * cutTwo;
					d.upZ = 0.5F * windTwo - 0.95F * cutTwo;
					d.upFore = -0.5F * windTwo + 0.5F * cutTwo;
					d.upHand = 0.4F * cutTwo;
					d.twist = 0.3F * windOne - 0.5F * cutOne - 0.3F * windTwo + 0.5F * cutTwo;
					d.side = 0.2F * cutOne - 0.2F * cutTwo;
					d.lean = 0.3F * (cutOne + cutTwo) - 0.1F * (windOne + windTwo);
					d.lift = 0.25F * (windOne + windTwo);
					d.headYaw = -0.4F * d.twist;
					d.curl = -0.6F * Math.max(cutOne, cutTwo);
					d.tailSwing = -0.6F * d.twist;
					d.whipOut = -0.4F * (cutOne + cutTwo);
				}
				case INFECTION_BLOOM -> {
					// It rears to its full height and arches back, shaking, arms flung wide, then throws its chest
					// forward and the infection out of it.
					float rear = ramp(a, 0.0F, 12.0F) * (1.0F - ramp(a, 13.0F, 16.0F));
					float release = a < 13.0F ? 0.0F : snap(a, 13.0F, 17.0F) * (1.0F - ramp(a, 20.0F, 28.0F));
					float spread = rear + 0.6F * release;
					d.lift = 0.9F * rear + 0.3F * release;
					d.lean = -0.28F * rear + 0.45F * release;
					d.headPitch = -0.45F * rear + 0.3F * release;
					d.upZ = 0.65F * spread;
					d.loZ = 0.55F * spread;
					d.upX = d.loX = -0.35F * spread;
					d.upFore = d.loFore = 0.45F * spread;
					d.curl = -0.65F * spread;
					d.tremor = rear;
					d.whipOut = -0.75F * spread;
					d.whipRaise = -0.4F * spread;
					d.uncoil = 0.1F + 0.6F * spread;
					d.whipWave = 2.0F;
					d.tailCurl = -0.12F * rear;
				}
				case ROOT_ERUPTION -> {
					// It sinks into its coils and dives, drives both claws into the floor and drags, shuddering, while
					// its tail thrashes; then hauls itself back up.
					float dig = ramp(a, 2.0F, 14.0F) * (1.0F - ramp(a, 26.0F, 36.0F));
					float drive = a < 16.0F ? 0.0F : snap(a, 16.0F, 22.0F) * (1.0F - ramp(a, 26.0F, 36.0F));
					d.lift = -0.4F * dig;
					d.lean = 0.45F * dig + 0.15F * drive;
					d.headPitch = 0.3F * dig;
					d.upX = d.loX = -0.2F * dig - 0.25F * drive;
					d.upFore = d.loFore = -0.4F * dig + 0.3F * drive;
					d.upHand = d.loHand = 0.5F * dig;
					d.curl = 0.6F * dig;
					d.tremor = beat(a, 14.0F, 22.0F, 32.0F);
					d.tailSwing = 0.3F * Mth.sin(t * 0.8F) * dig;
					d.whipOut = 0.2F * dig;
					d.whipWave = 1.8F;
				}
			}
		}
		hurt -= lag;
		if (hurt >= 0.0F && hurt < 14.0F) {
			// Struck: the blow knocks the upper body back and it rings, a shiver that runs out down the coils.
			float ring = wobble(hurt, 0.0F, 0.9F, 0.22F);
			d.lean -= 0.3F * ring;
			d.side += 0.12F * ring;
			d.headPitch -= 0.3F * ring;
			d.headRoll += 0.25F * ring;
			d.upZ += 0.25F * ring;
			d.loZ += 0.2F * ring;
			d.whipWave += 1.5F * Math.abs(ring);
		}
		if (dying > 0.0F) {
			// Dying, it does not topple like a man: the coils give way and it slumps forward onto them.
			float slump = ramp(dying, 0.0F, 1.0F);
			d.lift = Mth.lerp(slump, d.lift, -0.9F);
			d.lean = Mth.lerp(slump, d.lean, 0.9F);
			d.headPitch = Mth.lerp(slump, d.headPitch, 0.6F);
			d.upX = Mth.lerp(slump, d.upX, 0.3F);
			d.loX = Mth.lerp(slump, d.loX, 0.4F);
			d.curl = Mth.lerp(slump, d.curl, 0.8F);
			d.whipWave *= 1.0F - slump;
			d.busy = 1.0F;
		}
	}

	@Override
	public void setupAnim(LatentAnnettaInfectionEntity entity, float limbSwing, float limbSwingAmount,
			float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);
		float partial = ageInTicks - entity.tickCount;
		float t = ageInTicks;
		float move = Mth.clamp(limbSwingAmount, 0.0F, 1.0F);
		LatentCombatRules.Attack attack = entity.getAttack();
		float a = entity.getActionTick() + partial;
		float emerge = entity.getEmergeTick() + partial;
		float hurt = entity.hurtTime > 0 ? entity.hurtDuration - (entity.hurtTime - partial) : -100.0F;
		float dying = entity.deathTime > 0 ? Math.min(1.0F, (entity.deathTime + partial) / 20.0F) : 0.0F;
		Drive d = this.drive;
		float yaw = netHeadYaw * Mth.DEG_TO_RAD;
		float pitch = headPitch * Mth.DEG_TO_RAD;

		// ---- the coils. At rest it weaves like a cobra: a slow sideways S that climbs from the base to the waist,
		// and a breath that lifts and settles the body on them. Moving, the S quickens into a slither and it leans in.
		sample(d, entity, attack, a, emerge, hurt, dying, t, 0.0F);
		float calm = 1.0F - 0.65F * d.busy;
		float weaveSpeed = 0.055F;
		float slitherPhase = limbSwing * 0.42F;
		float breath = Mth.sin(t * 0.06F);
		for (int i = 0; i < this.coils.length; i++) {
			sample(d, entity, attack, a, emerge, hurt, dying, t, -COIL_LEAD * (this.coils.length - i) / this.coils.length);
			ModelPart coil = this.coils[i];
			float weave = Mth.sin(t * weaveSpeed - i * 0.62F) * 0.07F * calm;
			float slither = Mth.sin(slitherPhase - i * 0.75F) * 0.16F * move;
			coil.xRot += COIL_LEAN[i] * (d.lean + 0.25F * move) - COIL_LIFT[i] * (d.lift + 0.12F * breath * calm);
			coil.zRot += COIL_SIDE[i] * d.side + weave + slither;
			coil.yRot += COIL_TWIST[i] * (d.twist + yaw * 0.5F);
		}
		this.root.z += d.slide;

		// ---- the tail trails along the floor: the same S carries down it, growing toward the tip, and it swings
		// against the body's twists for balance. It never lifts off the floor.
		float tailSpeed = 0.05F + 0.08F * move;
		for (int i = 0; i < this.tail.length; i++) {
			sample(d, entity, attack, a, emerge, hurt, dying, t, i * 0.9F);
			float reach = (0.06F + 0.03F * i) * (calm + 1.4F * move);
			this.tail[i].yRot += Mth.sin(t * tailSpeed + slitherPhase - i * 0.8F) * reach
					+ d.tailSwing * (0.25F + 0.06F * i) + d.tailCurl * (i == 0 ? 0.0F : 1.0F);
		}

		// ---- waist, torso and hood ride the top of the coils.
		sample(d, entity, attack, a, emerge, hurt, dying, t, 0.0F);
		float sway = Mth.sin(t * weaveSpeed - this.coils.length * 0.62F);
		this.skirt.yRot += 0.1F * d.twist;
		this.torso.xRot += 0.3F * d.lean + 0.04F * breath * calm + 0.12F * move;
		this.torso.zRot += 0.3F * d.side - 0.05F * sway * calm;
		this.torso.yRot += 0.45F * d.twist + yaw * 0.25F;
		float shake = d.tremor * 0.05F * Mth.sin(t * 2.1F);
		this.torso.xRot += shake;
		this.torso.zRot += shake;
		float bodyLean = 0.3F * d.lean;
		float bodySide = 0.3F * d.side;
		this.leftPauldron.zRot += 0.06F * breath;
		this.rightPauldron.zRot -= 0.06F * breath;

		// The hood holds still against the sway, the way a snake's head does, and snaps round now and then.
		sample(d, entity, attack, a, emerge, hurt, dying, t, LAG_HEAD);
		float jerk = twitch(t, 83.0F, 0.0F);
		float jerk2 = twitch(t, 131.0F, 40.0F);
		this.headMass.yRot += yaw * 0.25F + d.headYaw + 0.25F * jerk - 0.18F * jerk2;
		this.headMass.xRot += pitch * 0.5F + d.headPitch - 0.6F * bodyLean + 0.12F * jerk2;
		this.headMass.zRot += d.headRoll - 0.7F * bodySide + 0.08F * sway * calm + 0.3F * jerk;
		this.crest.xRot += 0.04F * Mth.sin(t * 0.05F - 1.8F);
		this.crestTip.xRot += 0.06F * Mth.sin(t * 0.05F - 2.3F);
		this.crestHook.xRot += 0.08F * Mth.sin(t * 0.05F - 2.8F);

		// ---- the claw arms: shoulder, forearm and hand each a beat behind the last, so they swing like whips of
		// bone. Both share the same idle beat so the lance stays between their hands.
		float armWave = Mth.sin(t * 0.07F) * calm;
		sample(d, entity, attack, a, emerge, hurt, dying, t, LAG_SHOULDER);
		// What hangs from the shoulders stays near plumb as the body pitches, rather than swinging into the floor.
		float bodyPitch = 1.22F * d.lean - 0.18F * d.lift;
		this.upperClawArm.xRot += d.upX - 0.6F * bodyPitch + 0.07F * armWave;
		this.upperClawArm.zRot += d.upZ;
		this.lowerClawArm.xRot += d.loX - 0.6F * bodyPitch + 0.07F * armWave;
		this.lowerClawArm.zRot += d.loZ;
		sample(d, entity, attack, a, emerge, hurt, dying, t, LAG_FOREARM);
		float foreWave = Mth.sin(t * 0.07F - 0.8F) * calm;
		this.upperClawArmForearm.xRot += d.upFore + 0.09F * foreWave;
		this.lowerClawArmForearm.xRot += d.loFore + 0.09F * foreWave;
		sample(d, entity, attack, a, emerge, hurt, dying, t, LAG_HAND);
		float wristWave = Mth.sin(t * 0.07F - 1.6F) * calm;
		this.upperClawArmHand.xRot += d.upHand + 0.08F * wristWave;
		this.lowerClawArmHand.xRot += d.loHand + 0.08F * wristWave;
		flexClaws(t, d.curl, d.tremor * 0.16F * Mth.sin(t * 1.6F));

		// ---- the whip arm swings from the shoulder; the cord straightens as it uncoils and a crack runs out along
		// it to the tip, each segment later than the last.
		sample(d, entity, attack, a, emerge, hurt, dying, t, LAG_SHOULDER);
		float whipPitch = 1.22F * d.lean - 0.18F * d.lift;
		this.whipArm0.xRot += d.whipRaise - 0.85F * whipPitch + 0.05F * armWave;
		this.whipArm0.yRot += d.whipSweep;
		this.whipArm0.zRot += d.whipOut;
		// Low and leaning, it draws the cord up into a tighter hook.
		setWhipUncoil(d.uncoil - 0.5F * Math.max(0.0F, whipPitch) + 0.08F * Mth.sin(t * 0.1F));
		for (int i = 0; i < this.whipArm.length; i++) {
			sample(d, entity, attack, a, emerge, hurt, dying, t, LAG_SHOULDER + i * 0.55F);
			float grow = 1.0F + i * 0.15F;
			this.whipArm[i].zRot += Mth.sin(t * 0.12F - i * 0.7F) * 0.07F * grow * d.whipWave;
			this.whipArm[i].xRot += Mth.cos(t * 0.09F - i * 0.6F) * 0.05F * grow * d.whipWave;
			if (i > 0) this.whipArm[i].yRot += Mth.sin(i * 0.9F - (a - i * 0.55F) * 1.1F) * 0.3F * d.crack;
		}

		// ---- the hanging things trail the body: skirt strands and her hair swing against its lean and sway.
		for (int i = 0; i < this.skirtStrands.length; i++) {
			this.skirtStrands[i].xRot += Mth.sin(t * 0.08F + i * 0.9F) * 0.06F + 0.2F * move - 0.5F * bodyLean;
			this.skirtStrands[i].zRot += Mth.cos(t * 0.06F + i * 1.3F) * 0.06F - 0.6F * bodySide;
		}
		for (int i = 0; i < this.hair.length; i++) {
			this.hair[i].xRot += Mth.sin(t * 0.08F + i * 1.1F) * 0.06F - this.torso.xRot * 0.5F - this.headMass.xRot * 0.4F;
			this.hair[i].zRot += Mth.cos(t * 0.06F + i * 0.8F) * 0.05F - this.headMass.zRot * 0.5F;
		}
	}

	/**
	 * The hooked fingers flex in a slow ripple, first finger to last; curl adds a grip (positive) or a splay
	 * (negative), and tremor shakes them.
	 */
	private void flexClaws(float t, float curl, float tremor) {
		ModelPart[][] hands = {
				{this.upperClawArmFinger0, this.upperClawArmFinger1, this.upperClawArmFinger2},
				{this.lowerClawArmFinger0, this.lowerClawArmFinger1, this.lowerClawArmFinger2}};
		ModelPart[][] tips = {
				{this.upperClawArmFinger0Tip, this.upperClawArmFinger1Tip, this.upperClawArmFinger2Tip},
				{this.lowerClawArmFinger0Tip, this.lowerClawArmFinger1Tip, this.lowerClawArmFinger2Tip}};
		for (int h = 0; h < 2; h++) {
			for (int i = 0; i < 3; i++) {
				float ripple = 0.5F + 0.5F * Mth.sin(t * 0.11F - i * 0.6F - h * 1.7F);
				float shake = tremor * Mth.sin(t * 2.3F + i * 1.9F + h);
				hands[h][i].xRot += 0.14F * ripple + curl + shake;
				tips[h][i].xRot += 0.18F * ripple + curl * 0.8F + shake;
			}
		}
		this.upperClawArmThumb.xRot += 0.1F * Mth.sin(t * 0.11F - 2.0F) - curl * 0.5F;
		this.lowerClawArmThumb.xRot += 0.1F * Mth.sin(t * 0.11F - 3.7F) - curl * 0.5F;
	}

	/** 0 leaves the vessels in their hanging hook; 1 straightens them into a whip held out from the shoulder. */
	public void setWhipUncoil(float t) {
		float keep = 1.0F - Mth.clamp(t, -0.6F, 1.0F);
		for (int i = 1; i < this.whipArm.length; i++) {
			this.whipArm[i].xRot *= keep;
			this.whipArm[i].yRot *= keep;
			this.whipArm[i].zRot *= keep;
		}
	}

	/** The floor knot the coils rise from; the floor is at y 2 of this frame. */
	public void translateToCoilBase(PoseStack poseStack) {
		this.root.translateAndRotate(poseStack);
		this.coilBase.translateAndRotate(poseStack);
	}

	/** The waist's frame, at the top of the coils, where the skirt strands hang from. */
	public void translateToSkirt(PoseStack poseStack) {
		translateToCoilBase(poseStack);
		for (ModelPart coil : this.coils) {
			coil.translateAndRotate(poseStack);
		}
		this.skirt.translateAndRotate(poseStack);
	}

	/** Moves the pose into the torso's frame (model pixels), where the back and maw tendrils grow from. */
	public void translateToTorso(PoseStack poseStack) {
		translateToSkirt(poseStack);
		this.torso.translateAndRotate(poseStack);
	}

	/** The hood's frame, round the empty face. */
	public void translateToHead(PoseStack poseStack) {
		translateToTorso(poseStack);
		this.headMass.translateAndRotate(poseStack);
	}

	/** The last vessel segment of the whip arm; its end is at y 4.6. */
	public void translateToWhipTip(PoseStack poseStack) {
		translateToTorso(poseStack);
		for (ModelPart segment : this.whipArm) {
			segment.translateAndRotate(poseStack);
		}
	}
