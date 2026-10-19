package com.vincenthuto.hemomancy.client.model.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperWeaponAction;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * The Evening Star's stances and moves, keyed by hand in the manner of Annetta's: every move is a run of poses on
 * the action's own clock, timed to the server's contact ticks and joined by Catmull-Rom curves, so a wind-up flows
 * into its strike and the strike into its recovery without stopping on any pose. The whole body works each cut:
 * the torso twists away and the elbow cocks on the wind-up, then hips, shoulder and forearm uncoil through the hit
 * while the front foot drives in.
 * <p>
 * A pose is a row of channels. The weapon is in the right hand; arms are posed against the torso they hang from.
 * Arm x: -pi/2 holds the arm level ahead, below that raises it overhead, above zero swings it behind. Arm y swings a
 * raised arm out to its own side (+ for the right arm, - for the left), arm z lifts it out sideways the same way.
 * Forearm x bends the elbow (negative brings the hand up and in). Torso x leans forward, y turns him to his right.
 */
final class VesperEveningStarMoves {
	static final int AX = 0, AY = 1, AZ = 2, AF = 3, OX = 4, OY = 5, OZ = 6, OF = 7, TX = 8, TY = 9, TZ = 10;
	static final int LUNGE = 11, CROUCH = 12, SPIN = 13, KICK = 14, HEAD = 15, CHANNELS = 16;

	private VesperEveningStarMoves() {
	}

	/** The parts a pose drives. */
	record Rig(ModelPart whole, ModelPart body, ModelPart head, ModelPart rightArm, ModelPart rightFore,
			ModelPart leftArm, ModelPart leftFore, ModelPart rightLeg, ModelPart rightShin, ModelPart leftLeg,
			ModelPart leftShin) {
	}

	// ------------------------------------------------------------------------------------------------ stances

	/**
	 * The guard each weapon is carried in between moves. Every stance keeps a bend in both elbows and the knees
	 * soft, turns the weapon side back, and breathes: nothing here moves faster than a breath, so it reads as tense
	 * rather than twitchy. A fresh stance flourishes the new weapon up into view.
	 */
	static float[] stance(EnumBloodTendency tendency, boolean raging, float age, float stanceTime) {
		float[] p = new float[CHANNELS];
		if (raging) {
			set(p, -0.8F, 0.3F, 0.5F, -1.15F, -0.8F, -0.3F, -0.5F, -1.15F, 0.3F, 0.0F, 0.0F, 0.2F, 0.55F);
		} else {
			switch (tendency) {
				case ANIMUS -> grip(set(p, -0.85F, -0.2F, 0.08F, -0.75F, 0, 0, 0, 0, 0.1F, 0.3F, 0.0F, 0.35F, 0.25F));
				case MORTEM -> grip(set(p, -0.5F, -0.25F, 0.12F, -1.05F, 0, 0, 0, 0, 0.16F, 0.35F, 0.0F, 0.3F, 0.35F));
				case LUX -> set(p, -0.45F, 0.15F, 0.18F, -1.05F, -1.0F, 0.25F, -0.05F, -0.55F, 0.08F, 0.4F, 0.0F, 0.4F, 0.25F);
				case TENEBRIS -> set(p, -0.65F, 0.2F, 0.3F, -1.45F, -0.65F, -0.2F, -0.3F, -1.45F, 0.32F, 0.0F, 0.0F, 0.2F, 0.6F);
				case DUCTILIS -> set(p, -1.4F, 0.3F, 0.02F, -0.3F, -1.3F, -0.35F, -0.05F, -0.55F, 0.04F, 0.25F, 0.0F, 0.3F, 0.15F);
				case FLAMMEUS -> set(p, -0.9F, 0.1F, 0.18F, -0.95F, -0.3F, -0.1F, -0.22F, -0.45F, 0.08F, 0.2F, 0.0F, 0.3F, 0.25F);
				case CONGEATIO -> set(p, -0.45F, 0.1F, 0.3F, -0.75F, -0.3F, -0.1F, -0.22F, -0.5F, 0.1F, 0.15F, 0.0F, 0.25F, 0.3F);
				case FERRIC -> set(p, -0.6F, -0.1F, 0.12F, -1.05F, -0.8F, 0.4F, -0.08F, -0.9F, 0.08F, 0.25F, 0.0F, 0.3F, 0.25F);
			}
		}
		float heave = Mth.sin(age * 0.16F);
		float sway = Mth.sin(age * 0.05F) * Mth.sin(age * 0.11F + 1.3F);
		p[TX] += heave * 0.025F;
		p[TY] += sway * 0.04F;
		p[AF] += heave * 0.04F;
		p[OF] -= heave * 0.03F;
		p[AZ] -= heave * 0.02F;
		p[OZ] += heave * 0.02F;
		if (!raging && stanceTime < 30.0F) {
			// The weapon has just grown into his hand: he lifts it to look at it, then settles into guard.
			float flourish = window(stanceTime, 0.0F, 7.0F, 16.0F, 30.0F);
			p[AX] -= 0.85F * flourish;
			p[AF] -= 0.55F * flourish;
			p[TX] -= 0.1F * flourish;
			p[HEAD] += 0.3F * flourish;
		}
		return p;
	}

	private static float[] set(float[] p, float ax, float ay, float az, float af, float ox, float oy, float oz,
			float of, float tx, float ty, float tz, float lunge, float crouch) {
		p[AX] = ax; p[AY] = ay; p[AZ] = az; p[AF] = af;
		p[OX] = ox; p[OY] = oy; p[OZ] = oz; p[OF] = of;
		p[TX] = tx; p[TY] = ty; p[TZ] = tz;
		p[LUNGE] = lunge; p[CROUCH] = crouch;
		return p;
	}

	/** Two hands on one haft: the off hand reaches across to just below the weapon hand. */
	private static float[] grip(float[] p) {
		p[OX] = p[AX] - 0.05F;
		p[OY] = p[AY] + 0.6F;
		p[OZ] = -0.12F;
		p[OF] = p[AF] - 0.3F;
		return p;
	}

	// ------------------------------------------------------------------------------------------------ moves

	/** Lays {@code action} at {@code t} over the stance pose {@code base}, in place. */
	static void act(VesperWeaponAction action, int variant, float t, float[] base) {
		if (action == VesperWeaponAction.NONE) return;
		Keys k = new Keys(base, (variant & 1) == 0 ? 1.0F : -1.0F);
		build(action, k, (variant & 1) != 0);
		float[] target = k.sample(t);
		float d = action.durationTicks();
		float w = window(t, 0.0F, Math.min(2.0F, action.contactCount() > 0 ? action.contactTick(0) * 0.3F : 2.0F),
				d - 3.0F, d);
		for (int i = 0; i < CHANNELS; i++) {
			// Whole turns land facing front again, so a finished spin fades out without unwinding.
			if (i == SPIN) base[i] = (target[i] - Math.round(target[i])) * w;
			else base[i] = Mth.lerp(w, base[i], target[i]);
		}
	}

	private static void build(VesperWeaponAction action, Keys k, boolean alternate) {
		float d = action.durationTicks();
		switch (action) {
			// ---------------------------------------------------------------- Animus: the two-handed blade
			case ICHIMONJI -> {
				// One straight draw-cut down the centre line: raised over the crown, hung, then dropped through.
				k.twoHanded();
				k.at(9).arm(-2.55F, 0.05F, 0.12F).fore(-1.25F).torso(-0.12F, 0.12F, 0.0F).lunge(0.15F).crouch(0.15F);
				k.at(15).arm(-2.85F, 0.0F, 0.1F).fore(-1.5F).torso(-0.2F, 0.05F, 0.0F).head(-0.1F);
				k.at(18).arm(-0.95F, 0.0F, 0.05F).fore(-0.2F).torso(0.45F, -0.05F, 0.0F).lunge(1.25F).crouch(0.7F).head(0.15F);
				k.at(21).arm(-0.55F, 0.0F, 0.05F).fore(-0.35F).torso(0.55F, -0.08F, 0.0F).lunge(1.3F);
				k.at(d).stance();
			}
			case CROSSCUT -> {
				// An X: down from his right shoulder to his left hip, then back up the other diagonal.
				k.twoHanded().mirrored();
				k.at(9).arm(-1.75F, 1.0F, 0.35F).fore(-1.5F).torso(-0.02F, 0.7F, 0.05F).lunge(0.2F);
				k.at(15).arm(-1.05F, -0.85F, 0.05F).fore(-0.25F).torso(0.35F, -0.55F, -0.05F).lunge(1.0F).crouch(0.5F);
				k.at(18).arm(-0.85F, -1.2F, 0.0F).fore(-0.6F).torso(0.3F, -0.75F, -0.05F);
				k.at(22).arm(-1.8F, -0.9F, -0.05F).fore(-1.45F).torso(0.05F, -0.6F, 0.0F).lunge(0.6F);
				k.at(26).arm(-1.0F, 1.0F, 0.3F).fore(-0.25F).torso(0.3F, 0.6F, 0.05F).lunge(1.1F).crouch(0.55F);
				k.at(29).arm(-0.8F, 1.25F, 0.4F).fore(-0.5F).torso(0.25F, 0.75F, 0.05F);
				k.at(d).stance();
			}
			case BLADE_RISING_CUT -> {
				// From the low line: the blade dropped behind his hip, then whipped up through the target.
				k.twoHanded().mirrored();
				k.at(6).arm(0.35F, 0.3F, 0.3F).fore(-0.5F).torso(0.45F, 0.4F, 0.0F).crouch(0.8F).lunge(0.3F);
				k.at(10).arm(-2.25F, -0.2F, 0.1F).fore(-0.3F).torso(-0.1F, -0.35F, 0.0F).lunge(0.9F).crouch(0.3F).head(-0.15F);
				k.at(13).arm(-2.6F, -0.25F, 0.1F).fore(-0.65F).torso(-0.15F, -0.4F, 0.0F);
				k.at(d).stance();
			}
			case BLADE_DRAWING_STEP -> {
				// Sheathed at the left hip, drawn in one step through the target, then a turning cut back.
				k.twoHanded().mirrored();
				k.at(8).arm(-0.3F, -0.9F, 0.0F).fore(-1.6F).torso(0.25F, -0.5F, 0.0F).crouch(0.75F).lunge(0.2F);
				k.at(16).arm(-1.3F, 1.0F, 0.3F).fore(-0.15F).torso(0.3F, 0.6F, 0.05F).lunge(1.3F).crouch(0.6F);
				k.at(20).arm(-1.75F, 1.1F, 0.4F).fore(-1.4F).torso(0.15F, 0.75F, 0.05F).lunge(0.8F);
				k.at(24).arm(-1.05F, -1.0F, 0.0F).fore(-0.3F).torso(0.35F, -0.6F, -0.05F).lunge(1.2F).crouch(0.6F);
				k.at(27).arm(-0.85F, -1.2F, 0.0F).fore(-0.55F).torso(0.3F, -0.75F, -0.05F);
				k.at(d).stance();
			}
			// ---------------------------------------------------------------- Mortem: the axe
			case LEAPING_CLEAVE -> {
				// Crouch, spring with the axe swung up behind his head, hang at the top, and split the floor.
				k.twoHanded();
				k.at(6).arm(0.35F, -0.25F, 0.2F).fore(-0.8F).torso(0.45F, 0.1F, 0.0F).crouch(1.0F).lunge(0.0F);
				k.at(11).arm(-2.85F, 0.0F, 0.12F).fore(-1.0F).torso(-0.25F, 0.0F, 0.0F).crouch(0.0F).kick(0.35F).head(-0.15F);
				k.at(18).arm(-3.0F, 0.0F, 0.1F).fore(-1.45F).torso(-0.32F, 0.0F, 0.0F).kick(0.55F);
				k.at(22).arm(-0.55F, 0.0F, 0.05F).fore(-0.2F).torso(0.75F, 0.0F, 0.0F).kick(0.0F).crouch(1.0F).lunge(1.0F).head(0.2F);
				k.at(26).arm(-0.45F, 0.0F, 0.05F).fore(-0.3F).torso(0.65F, 0.0F, 0.0F).crouch(0.9F);
				k.at(d).stance();
			}
			case REAPER_SWEEP -> {
				// A wide, low reaping sweep from his right side across to his left, his whole weight turning with it.
				k.twoHanded().mirrored();
				k.at(10).arm(-1.15F, 1.2F, 0.5F).fore(-1.0F).torso(0.15F, 0.85F, 0.05F).crouch(0.45F).lunge(0.2F);
				k.at(15).arm(-1.05F, 0.55F, 0.4F).fore(-0.55F).torso(0.25F, 0.45F, 0.0F);
				k.at(18).arm(-0.9F, -0.85F, 0.2F).fore(-0.25F).torso(0.3F, -0.7F, -0.05F).lunge(1.0F).crouch(0.65F);
				k.at(22).arm(-0.7F, -1.3F, 0.1F).fore(-0.5F).torso(0.25F, -1.0F, -0.05F);
				k.at(d).stance();
			}
			case AXE_HAFT_SHOVE -> {
				k.twoHanded();
				k.at(6).arm(-1.0F, 0.2F, 0.2F).fore(-1.8F).torso(0.05F, 0.3F, 0.0F).crouch(0.55F);
				k.at(10).arm(-1.45F, 0.0F, 0.1F).fore(-0.25F).torso(0.35F, -0.1F, 0.0F).lunge(1.1F).crouch(0.4F);
				k.at(13).arm(-1.4F, 0.0F, 0.1F).fore(-0.35F).torso(0.3F, -0.1F, 0.0F);
				k.at(d).stance();
			}
			case AXE_GRAVE_SPLITTER -> {
				// Raised high over the crown and hung there, brought down to split the floor, then wrenched free.
				k.twoHanded();
				k.at(12).arm(-2.9F, 0.05F, 0.1F).fore(-1.35F).torso(-0.22F, 0.1F, 0.0F).crouch(0.1F).head(-0.15F);
				k.at(20).arm(-3.05F, 0.0F, 0.1F).fore(-1.6F).torso(-0.3F, 0.05F, 0.0F);
				k.at(24).arm(-0.5F, 0.0F, 0.05F).fore(-0.15F).torso(0.62F, 0.0F, 0.0F).crouch(0.85F).lunge(1.0F).head(0.25F);
				k.at(27).arm(-0.55F, 0.0F, 0.05F).fore(-0.4F).torso(0.7F, 0.0F, 0.0F);
				k.at(30).arm(-1.45F, 0.15F, 0.1F).fore(-0.9F).torso(0.15F, 0.2F, 0.0F).crouch(0.4F).lunge(0.5F);
				k.at(d).stance();
			}
			// ---------------------------------------------------------------- Lux: the spear
			case SKY_LANCE -> {
				// Crouch with the spear trailed low, spring, and drive it down point first from the top of the leap.
				k.at(8).arm(0.3F, 0.2F, 0.2F).fore(-1.4F).off(0.2F, 0.1F, -0.2F, -1.0F).torso(0.4F, 0.2F, 0.0F).crouch(1.0F);
				k.at(13).arm(-2.75F, 0.0F, 0.1F).fore(-0.55F).off(-2.4F, 0.2F, -0.1F, -0.9F).torso(-0.25F, 0.0F, 0.0F).crouch(0.0F).kick(0.5F);
				k.at(17).arm(-1.0F, 0.0F, 0.0F).fore(-0.1F).off(-1.1F, 0.3F, -0.05F, -0.4F).torso(0.9F, 0.0F, 0.0F).kick(0.0F).crouch(0.8F).head(0.3F);
				k.at(25).arm(-0.85F, 0.0F, 0.0F).fore(-0.2F).torso(0.8F, 0.0F, 0.0F).crouch(0.9F).lunge(0.8F);
				k.at(d).stance();
			}
			case LANCE_FLURRY -> thrusts(k, action, true);
			case SPEAR_PINNING_THRUST -> {
				// A long lunge: back leg straight, front knee deep, the spear driven to its full length and held.
				k.at(12).arm(0.0F, 0.35F, 0.3F).fore(-1.55F).off(-0.9F, 0.4F, -0.05F, -0.9F).torso(0.0F, 0.55F, 0.0F).crouch(0.6F).lunge(0.3F);
				k.at(18).arm(-1.55F, -0.05F, 0.0F).fore(-0.02F).off(-1.45F, 0.25F, -0.05F, -0.3F).torso(0.55F, -0.3F, 0.0F).lunge(1.7F).crouch(0.6F);
				k.at(24).arm(-1.5F, -0.05F, 0.0F).fore(-0.1F).torso(0.5F, -0.25F, 0.0F).lunge(1.6F);
				k.at(d).stance();
			}
			case SPEAR_VAULT_KICK -> {
				// He plants the spear and vaults on it, swinging both legs up through the kick.
				k.at(8).arm(-1.0F, 0.0F, 0.2F).fore(-0.8F).off(-1.1F, 0.3F, -0.1F, -0.7F).torso(0.3F, 0.0F, 0.0F).crouch(0.85F);
				k.at(12).arm(-2.55F, 0.0F, 0.1F).fore(-0.3F).off(-2.4F, 0.2F, -0.1F, -0.4F).torso(-0.5F, 0.0F, 0.0F).crouch(0.0F).kick(1.0F);
				k.at(15).kick(0.6F).torso(-0.3F, 0.0F, 0.0F);
				k.at(18).kick(1.0F).torso(-0.55F, 0.0F, 0.0F);
				k.at(23).arm(-1.0F, 0.0F, 0.2F).fore(-0.8F).torso(0.35F, 0.0F, 0.0F).kick(0.0F).crouch(0.8F);
				k.at(d).stance();
			}
			// ---------------------------------------------------------------- Tenebris: the claws
			case TWIN_REND, CLAW_EVISCERATE -> {
				rakes(k.mirrored(), action);
				if (action == VesperWeaponAction.CLAW_EVISCERATE) {
					// Both hands hook in on the last rake and rip upward.
					float last = action.contactTick(action.contactCount() - 1);
					k.at(last + 3.0F).arm(-2.4F, 0.1F, 0.3F).fore(-0.9F).off(-2.4F, -0.1F, -0.3F, -0.9F).torso(-0.15F, 0.0F, 0.0F).crouch(0.2F).head(-0.2F);
				}
				k.at(d).stance();
			}
			case PREDATOR_POUNCE -> {
				// Coiled low as a cat, then a spring with both claws high and a raking landing.
				k.at(12).arm(0.25F, 0.3F, 0.5F).fore(-1.4F).off(0.25F, -0.3F, -0.5F, -1.4F).torso(0.65F, 0.0F, 0.0F).crouch(1.0F).head(-0.35F);
				k.at(16).arm(-2.6F, 0.4F, 0.6F).fore(-1.0F).off(-2.6F, -0.4F, -0.6F, -1.0F).torso(-0.1F, 0.0F, 0.0F).crouch(0.0F).kick(0.5F);
				k.at(20).arm(-0.75F, -0.4F, 0.15F).fore(-0.3F).off(-0.75F, 0.4F, -0.15F, -0.3F).torso(0.7F, 0.0F, 0.0F).kick(0.0F).crouch(0.9F).lunge(0.8F);
				k.at(24).arm(-0.5F, -0.6F, 0.1F).fore(-0.6F).off(-0.5F, 0.6F, -0.1F, -0.6F).torso(0.6F, 0.0F, 0.0F);
				k.at(d).stance();
			}
			case CLAW_SHADOW_FEINT -> {
				// A shoulder dip one way, and the rake comes from the other.
				k.mirrored();
				k.at(6).arm(-1.2F, -0.4F, 0.1F).fore(-1.6F).torso(0.3F, -0.45F, -0.3F).crouch(0.75F).lunge(0.3F);
				k.at(10).arm(-2.1F, 0.8F, 0.6F).fore(-1.4F).torso(0.15F, 0.5F, 0.15F);
				k.at(14).arm(-0.85F, -0.7F, 0.15F).fore(-0.25F).torso(0.4F, -0.45F, -0.1F).lunge(1.0F).crouch(0.6F);
				k.at(17).arm(-0.65F, -1.0F, 0.1F).fore(-0.5F).torso(0.35F, -0.6F, -0.1F);
				k.at(d).stance();
			}
			// ---------------------------------------------------------------- Ductilis: the crossbow
			case CONDUCTIVE_VOLLEY, STORM_LOCK, CROSSBOW_BOLT_RAIN, CROSSBOW_SNAP_SHOT -> shots(k, action);
			// ---------------------------------------------------------------- Flammeus: the torch
			case BRANDING_THRUSTS -> thrusts(k, action, false);
			case UPDRAFT_IMPALEMENT -> {
				// Driven in low, then heaved upward on the second beat, lifting whatever is on the end.
				k.at(11).arm(-0.6F, 0.3F, 0.3F).fore(-1.7F).torso(0.05F, 0.5F, 0.0F).crouch(0.7F).lunge(0.3F);
				k.at(16).arm(-1.35F, -0.05F, 0.05F).fore(-0.1F).torso(0.45F, -0.25F, 0.0F).lunge(1.2F).crouch(0.7F);
				k.at(20).arm(-1.25F, 0.0F, 0.05F).fore(-0.35F).torso(0.4F, -0.2F, 0.0F).crouch(0.9F);
				k.at(24).arm(-2.5F, 0.0F, 0.05F).fore(-0.25F).torso(-0.25F, -0.1F, 0.0F).lunge(0.9F).crouch(0.1F).head(-0.25F);
				k.at(28).arm(-2.6F, 0.0F, 0.05F).fore(-0.5F).torso(-0.2F, -0.1F, 0.0F);
				k.at(d).stance();
			}
			case TORCH_CINDER_SWEEP -> {
				k.mirrored();
				k.at(8).arm(-1.1F, 1.15F, 0.5F).fore(-1.2F).torso(0.1F, 0.75F, 0.05F).crouch(0.4F);
				k.at(14).arm(-1.0F, -0.9F, 0.2F).fore(-0.25F).torso(0.25F, -0.6F, -0.05F).lunge(1.0F).crouch(0.5F);
				k.at(18).arm(-0.85F, -1.25F, 0.1F).fore(-0.5F).torso(0.2F, -0.8F, -0.05F);
				k.at(d).stance();
			}
			case FLAMMEUS_CONCENTRATION -> {
				// The torch drawn to his mouth; he breathes the fire out over it in pulses.
				k.at(8).arm(-1.75F, -0.45F, 0.15F).fore(-1.55F).torso(-0.08F, 0.1F, 0.0F).crouch(0.3F).head(-0.15F);
				k.at(12).arm(-1.6F, -0.4F, 0.15F).fore(-1.3F).torso(0.2F, 0.0F, 0.0F).lunge(0.6F).head(0.1F);
				for (int beat = 16; beat <= 44; beat += 4) {
					float pulse = (beat / 4) % 2 == 0 ? 0.06F : -0.04F;
					k.at(beat).torso(0.2F + pulse, 0.0F, 0.0F).head(0.1F + pulse);
				}
				k.at(50).arm(-1.55F, -0.35F, 0.15F).fore(-1.2F).torso(0.15F, 0.0F, 0.0F);
				k.at(d).stance();
			}
			// ---------------------------------------------------------------- Congeatio: the flail
			case CHAIN_SWEEP -> {
				// The head swung once round over his crown for speed, then dragged flat through the target.
				k.mirrored();
				k.at(5).arm(-2.3F, -0.5F, 0.4F).fore(-0.5F).torso(-0.05F, -0.2F, 0.0F);
				k.at(10).arm(-2.5F, 0.7F, 0.6F).fore(-0.4F).torso(0.0F, 0.55F, 0.05F).crouch(0.3F);
				k.at(15).arm(-1.4F, 0.9F, 0.5F).fore(-0.3F).torso(0.15F, 0.6F, 0.05F);
				k.at(18).arm(-1.0F, -0.95F, 0.2F).fore(-0.15F).torso(0.3F, -0.7F, -0.05F).lunge(1.0F).crouch(0.5F);
				k.at(22).arm(-0.75F, -1.3F, 0.1F).fore(-0.4F).torso(0.25F, -0.95F, -0.05F);
				k.at(d).stance();
			}
			case HOOK_AND_CRUSH -> {
				// A flat hooking sweep, then the flail brought over the top onto whatever it caught.
				k.mirrored();
				k.at(9).arm(-1.4F, 1.0F, 0.5F).fore(-0.6F).torso(0.1F, 0.7F, 0.05F).crouch(0.3F);
				k.at(14).arm(-1.0F, -0.9F, 0.2F).fore(-0.2F).torso(0.3F, -0.6F, -0.05F).lunge(0.9F).crouch(0.5F);
				k.at(18).arm(-1.0F, -0.8F, 0.2F).fore(-0.6F).torso(0.25F, -0.5F, 0.0F);
				k.at(24).arm(-2.95F, 0.1F, 0.25F).fore(-0.5F).torso(-0.25F, 0.1F, 0.0F).lunge(0.4F).crouch(0.1F).head(-0.15F);
				k.at(28).arm(-0.6F, 0.0F, 0.1F).fore(-0.15F).torso(0.7F, 0.0F, 0.0F).lunge(1.1F).crouch(0.9F).head(0.2F);
				k.at(32).arm(-0.55F, 0.0F, 0.1F).fore(-0.35F).torso(0.6F, 0.0F, 0.0F);
				k.at(d).stance();
			}
			case FLAIL_ORBIT -> {
				// The flail swung round and round over his head, his whole body turning under it.
				k.at(4).arm(-2.4F, 0.0F, 0.35F).fore(-0.45F).torso(0.0F, 0.0F, 0.0F).crouch(0.3F);
				for (int i = 0; i <= 6; i++) {
					float time = 6.0F + i * 4.0F;
					float angle = i * Mth.HALF_PI;
					k.at(time).arm(-2.65F, 0.55F * Mth.sin(angle), 0.35F + 0.4F * Mth.cos(angle)).fore(-0.35F)
							.torso(0.05F, 0.3F * Mth.sin(angle), 0.05F * Mth.cos(angle));
				}
				k.at(d).stance();
			}
			case FLAIL_SHATTER_SLAM -> {
				k.at(10).arm(-2.4F, 0.4F, 0.4F).fore(-0.6F).torso(-0.1F, 0.3F, 0.0F).crouch(0.2F);
				k.at(17).arm(-3.0F, 0.0F, 0.15F).fore(-0.45F).torso(-0.3F, 0.1F, 0.0F).head(-0.15F);
				k.at(22).arm(-0.55F, 0.0F, 0.05F).fore(-0.15F).torso(0.6F, 0.0F, 0.0F).lunge(1.1F).crouch(0.85F).head(0.25F);
				k.at(26).arm(-0.5F, 0.0F, 0.05F).fore(-0.35F).torso(0.52F, 0.0F, 0.0F);
				k.at(d).stance();
			}
			// ---------------------------------------------------------------- Ferric: the staff
			case MAGNETIC_AXIS -> {
				// Both hands lift the staff overhead to draw the iron in, then drive its foot into the floor.
				k.at(8).arm(-2.3F, -0.1F, 0.1F).fore(-0.7F).off(-2.2F, 0.35F, -0.1F, -0.8F).torso(-0.18F, 0.1F, 0.0F).head(-0.2F);
				k.at(14).arm(-2.5F, -0.05F, 0.1F).fore(-0.8F).off(-2.4F, 0.3F, -0.1F, -0.9F).torso(-0.25F, 0.05F, 0.0F);
				k.at(18).arm(-1.15F, 0.0F, 0.05F).fore(-0.15F).off(-1.1F, 0.4F, -0.1F, -0.35F).torso(0.5F, 0.0F, 0.0F).crouch(0.8F).lunge(0.6F).head(0.15F);
				k.at(22).arm(-1.1F, 0.0F, 0.05F).fore(-0.3F).torso(0.42F, 0.0F, 0.0F).crouch(0.7F);
				k.at(d).stance();
			}
			case IRON_RETORT -> {
				// The staff held level across his body to brace, then pushed out; he holds the brace while it lasts.
				k.at(8).arm(-1.3F, 0.3F, 0.25F).fore(-1.2F).off(-1.3F, -0.3F, -0.25F, -1.2F).torso(0.15F, 0.0F, 0.0F).crouch(0.65F).lunge(0.5F);
				k.at(16).arm(-1.4F, 0.2F, 0.2F).fore(-0.45F).off(-1.4F, -0.2F, -0.2F, -0.45F).torso(0.3F, 0.0F, 0.0F).crouch(0.6F).lunge(0.9F);
				k.at(24).fore(-0.6F).torso(0.25F, 0.0F, 0.0F);
				k.at(36).fore(-0.55F).torso(0.28F, 0.0F, 0.0F);
				k.at(d).stance();
			}
			case STAFF_LODESTONE_SWEEP -> {
				k.mirrored();
				k.at(8).arm(-0.75F, 1.1F, 0.4F).fore(-0.9F).off(-0.85F, 1.4F, 0.1F, -1.1F).torso(0.3F, 0.8F, 0.05F).crouch(0.75F);
				k.at(14).arm(-0.7F, -0.95F, 0.1F).fore(-0.3F).off(-0.8F, -0.5F, -0.2F, -0.6F).torso(0.4F, -0.75F, -0.05F).lunge(1.0F).crouch(0.8F);
				k.at(18).arm(-0.6F, -1.25F, 0.05F).fore(-0.5F).off(-0.7F, -0.8F, -0.25F, -0.8F).torso(0.35F, -0.95F, -0.05F);
				k.at(d).stance();
			}
			case STAFF_PILLAR_VAULT -> {
				k.at(8).arm(-1.0F, 0.0F, 0.2F).fore(-0.9F).off(-1.1F, 0.3F, -0.1F, -0.8F).torso(0.35F, 0.0F, 0.0F).crouch(0.9F);
				k.at(14).arm(-2.6F, 0.0F, 0.1F).fore(-0.5F).off(-2.5F, 0.25F, -0.1F, -0.6F).torso(-0.35F, 0.0F, 0.0F).crouch(0.0F).kick(0.8F);
				k.at(18).arm(-2.9F, 0.0F, 0.1F).fore(-0.9F).torso(-0.25F, 0.0F, 0.0F).kick(0.4F);
				k.at(22).arm(-0.7F, 0.0F, 0.05F).fore(-0.2F).off(-0.75F, 0.35F, -0.1F, -0.4F).torso(0.75F, 0.0F, 0.0F).kick(0.0F).crouch(1.0F).lunge(0.7F);
				k.at(26).arm(-0.65F, 0.0F, 0.05F).fore(-0.35F).torso(0.65F, 0.0F, 0.0F);
				k.at(d).stance();
			}
			// ---------------------------------------------------------------- the dual sickles
			case SICKLE_CYCLONE, SICKLE_REEL_CYCLONE, SICKLE_TRAVELLING_CYCLONE, SICKLE_REVERSE_CYCLONE -> {
				float turns = switch (action) {
					case SICKLE_REVERSE_CYCLONE -> -2.0F;
					case SICKLE_TRAVELLING_CYCLONE -> 3.0F;
					default -> 2.0F;
				};
				spin(k.mirrored(), action, turns);
			}
			case SICKLE_GALLOWS_WHEEL -> {
				// One sickle up, one down, the pair wheeling round him like a gallows' arm.
				k.mirrored();
				k.at(10).arm(-2.7F, 0.2F, 0.6F).fore(-0.5F).off(0.3F, -0.2F, -0.6F, -0.4F).torso(0.1F, 0.4F, 0.15F).crouch(0.5F);
				k.at(16).spin(0.15F);
				k.at(40).spin(1.0F).arm(-2.6F, 0.2F, 0.7F).off(0.25F, -0.2F, -0.7F, -0.4F);
				k.at(44).spin(1.0F).arm(-1.0F, 0.3F, 0.5F).fore(-1.0F).off(-1.0F, -0.3F, -0.5F, -1.0F).torso(0.3F, 0.0F, 0.0F);
				k.at(d).stance();
			}
			case SICKLE_FEINT_CYCLONE -> {
				// The spin starts, checks a quarter of the way round, and snaps back to cut the other way.
				k.mirrored();
				k.at(5).arm(-1.2F, 0.3F, 1.1F).fore(-0.5F).off(-1.2F, -0.3F, -1.1F, -0.5F).torso(0.2F, -0.3F, 0.0F).crouch(0.6F);
				k.at(8).spin(0.22F).torso(0.25F, 0.2F, 0.0F);
				k.at(10).spin(0.05F).arm(-0.9F, -0.8F, 0.3F).fore(-0.25F).off(-1.1F, -0.6F, -0.5F, -0.8F).torso(0.35F, -0.5F, 0.0F).lunge(0.9F);
				k.at(13).spin(0.0F).arm(-0.7F, -1.1F, 0.2F).fore(-0.5F);
				k.at(d).stance();
			}
			case SICKLE_POUNCE, SICKLE_POUNCE_FEINT -> {
				boolean feint = action == VesperWeaponAction.SICKLE_POUNCE_FEINT;
				float c = action.contactTick(0);
				k.at(c - 8).arm(0.3F, 0.3F, 0.5F).fore(-1.3F).off(0.3F, -0.3F, -0.5F, -1.3F).torso(0.6F, 0.0F, 0.0F).crouch(1.0F).head(-0.3F);
				k.at(c - 4).arm(-2.7F, 0.4F, 0.5F).fore(-0.9F).off(-2.7F, -0.4F, -0.5F, -0.9F).torso(-0.15F, 0.0F, 0.0F).crouch(0.0F).kick(0.5F);
				if (feint) {
					// Lands short with the hook arm already drawn back for what comes next.
					k.at(c).arm(-2.4F, 0.2F, 0.3F).fore(-1.6F).off(-0.9F, -0.4F, -0.4F, -0.8F).torso(0.4F, 0.3F, 0.0F).kick(0.0F).crouch(0.9F);
				} else {
					k.at(c).arm(-0.85F, -0.5F, 0.1F).fore(-0.2F).off(-0.85F, 0.5F, -0.1F, -0.2F).torso(0.75F, 0.0F, 0.0F).kick(0.0F).crouch(1.0F).lunge(0.8F);
					k.at(c + 4).arm(-0.6F, -0.7F, 0.1F).fore(-0.4F).off(-0.6F, 0.7F, -0.1F, -0.4F).torso(0.65F, 0.0F, 0.0F);
				}
				k.at(d).stance();
			}
			case SICKLE_CROSS_REND, SICKLE_REAP_L, SICKLE_REAP_R -> {
				if (action == VesperWeaponAction.SICKLE_CROSS_REND) {
					rakes(k.mirrored(), action);
				} else {
					// One sickle reaches wide and hooks inward across his body; the other cocks back for the next.
					boolean left = action == VesperWeaponAction.SICKLE_REAP_L;
					float s = left ? -1.0F : 1.0F;
					Keys at = k.at(5);
					lead(at, left, -1.45F, 1.0F * s, 0.7F * s, -0.6F);
					trail(at, left, -0.5F, -0.6F);
					at.torso(0.2F, 0.55F * s, 0.1F * s).crouch(0.55F);
					at = k.at(10);
					lead(at, left, -1.1F, -0.75F * s, 0.15F * s, -0.9F);
					at.torso(0.35F, -0.5F * s, -0.05F * s).lunge(0.9F);
					at = k.at(13);
					lead(at, left, -0.85F, -0.95F * s, 0.1F * s, -1.2F);
				}
				k.at(d).stance();
			}
			case SICKLE_REAPERS_RISE -> {
				k.at(7).arm(0.4F, -0.5F, 0.2F).fore(-0.6F).off(0.4F, 0.5F, -0.2F, -0.6F).torso(0.55F, 0.0F, 0.0F).crouch(1.0F).head(-0.2F);
				k.at(12).arm(-2.7F, 0.2F, 0.4F).fore(-0.3F).off(-2.7F, -0.2F, -0.4F, -0.3F).torso(-0.3F, 0.0F, 0.0F).crouch(0.1F).lunge(0.7F).head(-0.3F);
				k.at(15).arm(-2.85F, 0.3F, 0.5F).fore(-0.6F).off(-2.85F, -0.3F, -0.5F, -0.6F).torso(-0.35F, 0.0F, 0.0F);
				k.at(d).stance();
			}
			case SICKLE_PASS_THROUGH -> {
				// Low and forward through the target, then a half turn to cut back at it from behind.
				k.mirrored();
				k.at(6).arm(0.35F, 0.3F, 0.4F).fore(-0.8F).off(0.35F, -0.3F, -0.4F, -0.8F).torso(0.6F, 0.0F, 0.0F).crouch(0.9F);
				k.at(10).arm(-1.2F, -0.5F, 0.2F).fore(-0.2F).off(-1.2F, 0.5F, -0.2F, -0.2F).torso(0.55F, 0.0F, 0.0F).lunge(1.2F).crouch(0.8F);
				k.at(13).spin(0.0F);
				k.at(16).spin(0.5F).arm(-1.6F, 1.0F, 0.6F).fore(-1.2F).torso(0.3F, 0.5F, 0.0F);
				k.at(18).spin(0.5F).arm(-1.0F, -0.8F, 0.2F).fore(-0.25F).torso(0.4F, -0.5F, 0.0F).lunge(1.0F);
				k.at(d).spin(1.0F).stance();
			}
			case SICKLE_HOOK, SICKLE_HOOK_REEL, SICKLE_ANCHOR_HOOK -> hookThrow(k, action);
			case SANGUINE_CRESCENTS, SICKLE_CRESCENT_FAN, SICKLE_CRESCENT_STAGGER, SICKLE_CRESCENT_SKIM, SICKLE_CRESCENT_RETURN -> {
				crescents(k.mirrored(), action);
				k.at(d).stance();
			}
			case SICKLE_HARVEST -> {
				// The whole last stand in one take: throw, reel, spin, cross, volley, and the final crash.
				k.at(9).arm(-2.75F, 0.2F, 0.2F).fore(-1.7F).torso(-0.1F, 0.55F, 0.0F).crouch(0.3F);
				k.at(14).arm(-1.25F, -0.2F, 0.1F).fore(-0.1F).torso(0.35F, -0.4F, 0.0F).lunge(1.0F);
				k.at(18).arm(-0.9F, 0.1F, 0.2F).fore(-1.4F).off(-0.9F, -0.2F, -0.2F, -0.6F).torso(-0.2F, 0.1F, 0.0F).lunge(0.3F);
				k.at(22).arm(-1.0F, 0.1F, 0.2F).fore(-0.6F).off(-0.8F, -0.1F, -0.2F, -1.4F).torso(-0.25F, -0.1F, 0.0F);
				k.at(26).arm(-0.8F, 0.1F, 0.2F).fore(-1.5F).off(-1.0F, -0.1F, -0.2F, -0.5F).torso(-0.2F, 0.1F, 0.0F);
				k.at(30).arm(-1.15F, 0.3F, 1.15F).fore(-0.45F).off(-1.15F, -0.3F, -1.15F, -0.45F).torso(0.2F, 0.0F, 0.0F).spin(0.0F).crouch(0.5F);
				k.at(46).spin(2.0F);
				k.at(50).arm(-1.6F, -0.6F, 0.1F).fore(-1.2F).off(-1.6F, 0.6F, -0.1F, -1.2F).torso(0.1F, 0.0F, 0.0F);
				k.at(54).arm(-1.0F, 0.7F, 0.5F).fore(-0.2F).off(-1.0F, -0.7F, -0.5F, -0.2F).torso(0.4F, 0.0F, 0.0F).lunge(1.0F);
				k.at(58).arm(-2.5F, 0.2F, 0.3F).fore(-1.6F).off(-1.0F, -0.2F, -0.3F, -0.5F).torso(0.0F, 0.4F, 0.0F);
				k.at(62).arm(-1.2F, -0.2F, 0.1F).fore(-0.1F).off(-2.5F, -0.2F, -0.3F, -1.6F).torso(0.3F, -0.4F, 0.0F);
				k.at(66).arm(-3.0F, 0.1F, 0.3F).fore(-1.0F).off(-3.0F, -0.1F, -0.3F, -1.0F).torso(-0.35F, 0.0F, 0.0F).crouch(0.1F).head(-0.2F);
				k.at(70).arm(-0.6F, -0.2F, 0.1F).fore(-0.2F).off(-0.6F, 0.2F, -0.1F, -0.2F).torso(0.8F, 0.0F, 0.0F).crouch(1.0F).lunge(1.1F).head(0.25F);
				k.at(75).arm(-0.5F, -0.3F, 0.1F).fore(-0.4F).off(-0.5F, 0.3F, -0.1F, -0.4F).torso(0.7F, 0.0F, 0.0F);
				k.at(d).stance();
			}
			// ---------------------------------------------------------------- borrowed attacks
			case BORROWED_HULK_YANK, BORROWED_MNEMONIC_LASH -> {
				// The Crossbar hand raised to pull the strings taut, then yanked down and back on each beat.
				for (int i = 0; i < action.contactCount(); i++) {
					float c = action.contactTick(i);
					k.at(c - 6).off(-2.7F, -0.2F, -0.3F, -0.5F).torso(-0.15F, -0.2F, 0.0F).head(-0.2F);
					k.at(c).off(-0.4F, -0.1F, -0.2F, -1.4F).torso(0.25F, 0.3F, 0.0F).lunge(0.2F).crouch(0.5F).head(0.1F);
					k.at(c + 3).off(-0.5F, -0.1F, -0.2F, -1.2F).torso(0.2F, 0.25F, 0.0F);
				}
				k.at(d).stance();
			}
			case BORROWED_MARROW_VOLLEY, BORROWED_HOUND_CURS -> {
				// Conducting: the Crossbar hand sweeps out over the arena on each beat.
				k.at(Math.max(4.0F, action.contactTick(0) - 6.0F)).off(-1.9F, 0.4F, -0.2F, -0.9F).torso(0.0F, 0.3F, 0.0F);
				for (int i = 0; i < action.contactCount(); i++) {
					float c = action.contactTick(i);
					float s = (i & 1) == 0 ? 1.0F : -1.0F;
					k.at(c).off(-1.55F, -0.6F * s, -0.5F, -0.25F).torso(0.1F, -0.25F * s, 0.0F).lunge(0.5F);
				}
				k.at(d).stance();
			}
			case BORROWED_VULTURE_DIVE -> {
				k.at(8).arm(0.35F, -0.1F, 1.0F).fore(-0.3F).off(0.35F, 0.1F, -1.0F, -0.3F).torso(0.5F, 0.0F, 0.0F).crouch(1.0F);
				k.at(13).arm(0.6F, -0.1F, 1.3F).fore(-0.2F).off(0.6F, 0.1F, -1.3F, -0.2F).torso(-0.3F, 0.0F, 0.0F).crouch(0.0F).kick(0.4F);
				k.at(18).arm(-2.8F, 0.0F, 0.1F).fore(-0.2F).off(-2.8F, 0.0F, -0.1F, -0.2F).torso(0.7F, 0.0F, 0.0F).kick(0.0F).crouch(0.9F);
				k.at(22).arm(-0.6F, -0.2F, 0.1F).fore(-0.3F).off(-0.6F, 0.2F, -0.1F, -0.3F).torso(0.7F, 0.0F, 0.0F).lunge(0.8F);
				k.at(d).stance();
			}
			case BORROWED_MUMMER_DECOYS -> {
				// A mummer's bow, arms opened wide, as the afterimages peel away to either side.
				k.at(8).arm(-0.4F, 0.0F, 1.2F).fore(-0.4F).off(-0.4F, 0.0F, -1.2F, -0.4F).torso(-0.1F, 0.0F, 0.0F);
				k.at(12).arm(-0.3F, 0.0F, 1.25F).off(-0.3F, 0.0F, -1.25F, -0.4F).torso(0.55F, 0.0F, 0.0F).head(0.35F).lunge(0.6F);
				k.at(36).torso(0.5F, 0.0F, 0.0F).head(0.3F);
				k.at(40).arm(-1.4F, 0.0F, 0.6F).fore(-0.8F).off(-1.4F, 0.0F, -0.6F, -0.8F).torso(0.0F, 0.0F, 0.0F).head(0.0F);
				k.at(d).stance();
			}
			case BORROWED_BELLOWS_BREATH -> {
				k.at(8).arm(-0.5F, 0.2F, 0.5F).fore(-0.7F).off(-0.5F, -0.2F, -0.5F, -0.7F).torso(-0.25F, 0.0F, 0.0F).head(-0.6F);
				k.at(14).arm(-0.6F, 0.2F, 0.4F).off(-0.6F, -0.2F, -0.4F, -0.7F).torso(0.3F, 0.0F, 0.0F).head(0.35F).lunge(0.8F);
				k.at(26).torso(0.25F, 0.0F, 0.0F).head(0.3F);
				k.at(d).stance();
			}
			default -> k.at(d).stance();
		}
	}

	/** Alternating thrusts: each one coiled back and twisted away, then driven out with the front foot. */
	private static void thrusts(Keys k, VesperWeaponAction action, boolean twoHands) {
		for (int i = 0; i < action.contactCount(); i++) {
			float c = action.contactTick(i);
			// The elbow draws back with the forearm kept level, so the point stays on the target through the coil.
			Keys coil = k.at(c - 4.0F).arm(0.05F, 0.25F, 0.25F).fore(-1.6F).torso(0.0F, 0.45F, 0.0F).lunge(0.25F).crouch(0.45F);
			Keys hit = k.at(c).arm(-1.5F, -0.05F, 0.05F).fore(-0.05F).torso(0.32F, -0.25F, 0.0F).lunge(1.15F).crouch(0.5F);
			Keys back = k.at(c + 2.0F).arm(-1.35F, 0.05F, 0.1F).fore(-0.45F).torso(0.25F, -0.1F, 0.0F).lunge(0.85F);
			if (twoHands) {
				coil.off(-0.95F, 0.45F, -0.05F, -1.1F);
				hit.off(-1.45F, 0.25F, -0.05F, -0.35F);
				back.off(-1.3F, 0.3F, -0.05F, -0.6F);
			}
		}
		k.at(action.durationTicks()).stance();
	}

	/**
	 * Rakes that trade hands: the leading hand comes down from high outside across the body while the other draws
	 * back for the next, the torso turning with each.
	 */
	private static void rakes(Keys k, VesperWeaponAction action) {
		for (int i = 0; i < action.contactCount(); i++) {
			float c = action.contactTick(i);
			boolean left = (i & 1) == 1;
			float s = left ? -1.0F : 1.0F;
			float gap = i == 0 ? 5.0F : Math.min(3.0F, (c - action.contactTick(i - 1)) * 0.5F);
			Keys raise = k.at(c - gap);
			lead(raise, left, -2.2F, 0.7F * s, 0.55F * s, -1.5F);
			trail(raise, left, -0.9F, -1.3F);
			raise.torso(0.2F, 0.45F * s, 0.1F * s).crouch(0.6F);
			Keys hit = k.at(c);
			lead(hit, left, -0.85F, -0.6F * s, 0.1F * s, -0.3F);
			trail(hit, left, -1.2F, -1.5F);
			hit.torso(0.4F, -0.45F * s, -0.08F * s).lunge(0.85F).crouch(0.65F);
		}
	}

	/** Throwing cuts that loose the blood crescents, alternating hands; skimmers are thrown low. */
	private static void crescents(Keys k, VesperWeaponAction action) {
		boolean low = action == VesperWeaponAction.SICKLE_CRESCENT_SKIM;
		boolean fan = action == VesperWeaponAction.SICKLE_CRESCENT_FAN;
		for (int i = 0; i < action.contactCount(); i++) {
			float c = action.contactTick(i);
			boolean left = (i & 1) == 1;
			float s = left ? -1.0F : 1.0F;
			float gap = i == 0 ? 6.0F : Math.min(3.0F, (c - action.contactTick(i - 1)) * 0.5F);
			Keys cock = k.at(c - gap);
			lead(cock, left, low ? -0.2F : -2.5F, 0.6F * s, 0.5F * s, -1.7F);
			cock.torso(low ? 0.4F : -0.05F, 0.6F * s, 0.0F).crouch(low ? 0.9F : 0.4F);
			if (fan) trail(cock, left, -2.5F, -1.7F);
			Keys loose = k.at(c);
			lead(loose, left, low ? -1.0F : -1.25F, -0.55F * s, 0.1F * s, -0.1F);
			loose.torso(low ? 0.55F : 0.3F, -0.5F * s, 0.0F).lunge(0.9F);
			if (fan) trail(loose, left, -1.25F, -0.1F);
		}
	}

	/** Arms flung out, the body turning under them; the spin speeds up into the cut and bleeds out of it. */
	private static void spin(Keys k, VesperWeaponAction action, float turns) {
		float first = action.contactTick(0);
		float last = action.contactTick(action.contactCount() - 1);
		k.at(Math.max(2.0F, first - 6.0F)).arm(-1.25F, -0.3F, 0.5F).fore(-1.0F).off(-1.25F, 0.3F, -0.5F, -1.0F)
				.torso(0.25F, -0.45F, 0.0F).crouch(0.7F).spin(-0.06F * Math.signum(turns));
		k.at(first).arm(-1.15F, 0.2F, 1.2F).fore(-0.35F).off(-1.15F, -0.2F, -1.2F, -0.35F).torso(0.2F, 0.0F, 0.0F)
				.crouch(0.45F).spin(turns * 0.2F);
		k.at(last).arm(-1.1F, 0.25F, 1.25F).off(-1.1F, -0.25F, -1.25F, -0.35F).spin(turns * 0.95F);
		k.at(last + 4.0F).arm(-1.0F, 0.3F, 0.7F).fore(-0.9F).off(-1.0F, -0.3F, -0.7F, -0.9F).torso(0.35F, 0.0F, 0.0F)
				.crouch(0.6F).spin(turns);
		k.at(action.durationTicks()).spin(turns).stance();
	}

	/** The hook thrown overhand, then hauled back hand over hand along its tether. */
	private static void hookThrow(Keys k, VesperWeaponAction action) {
		float c = action.contactTick(0);
		boolean anchor = action == VesperWeaponAction.SICKLE_ANCHOR_HOOK;
		float side = anchor ? 0.9F : 0.2F;
		k.at(c - 5).arm(-2.75F, side, anchor ? 0.9F : 0.25F).fore(-1.7F).torso(-0.1F, 0.6F, anchor ? 0.2F : 0.0F).crouch(0.3F);
		k.at(c).arm(-1.4F, -0.2F, 0.1F).fore(-0.05F).torso(0.35F, -0.35F, 0.0F).lunge(1.0F).crouch(0.4F);
		float end = action.lastImpactTick() > c ? action.lastImpactTick() : action.durationTicks() - 6.0F;
		// Hand over hand: each haul draws one arm back while the other reaches.
		int hauls = Math.max(1, (int) ((end - c - 2.0F) / 4.0F));
		for (int i = 0; i < hauls; i++) {
			float time = c + 3.0F + i * 4.0F;
			boolean right = (i & 1) == 0;
			k.at(time).arm(right ? -0.7F : -1.3F, 0.0F, 0.15F).fore(right ? -1.6F : -0.5F)
					.off(right ? -1.3F : -0.7F, 0.0F, -0.15F, right ? -0.5F : -1.6F)
					.torso(-0.15F, right ? 0.15F : -0.15F, 0.0F).lunge(0.2F).crouch(0.55F);
		}
		k.at(action.durationTicks()).stance();
	}

	/** The crossbow shouldered and aimed; each bolt kicks it up and rocks him back. Rain is shot high. */
	private static void shots(Keys k, VesperWeaponAction action) {
		boolean rain = action == VesperWeaponAction.CROSSBOW_BOLT_RAIN;
		boolean snap = action == VesperWeaponAction.CROSSBOW_SNAP_SHOT;
		float aimX = rain ? -2.35F : -1.55F;
		float first = action.contactTick(0);
		k.at(Math.min(first - 2.0F, snap ? 4.0F : 10.0F)).arm(aimX, 0.05F, 0.0F).fore(-0.2F)
				.off(aimX + 0.1F, -0.45F, -0.05F, -0.5F).torso(snap ? -0.1F : 0.05F, 0.15F, 0.0F)
				.head(rain ? -0.5F : 0.0F).crouch(snap ? 0.6F : 0.3F);
		for (int i = 0; i < action.contactCount(); i++) {
			float c = action.contactTick(i);
			k.at(c - 1.0F).arm(aimX, 0.05F, 0.0F).fore(-0.2F).torso(snap ? -0.1F : 0.05F, 0.15F, 0.0F);
			k.at(c + 1.0F).arm(aimX - 0.35F, 0.05F, 0.0F).fore(-0.45F).off(aimX - 0.25F, -0.45F, -0.05F, -0.7F)
					.torso(snap ? -0.35F : -0.12F, 0.2F, 0.0F).kick(snap ? 0.35F : 0.0F);
			k.at(c + 4.0F).arm(aimX, 0.05F, 0.0F).fore(-0.25F).off(aimX + 0.1F, -0.45F, -0.05F, -0.5F)
					.torso(snap ? -0.05F : 0.05F, 0.15F, 0.0F).kick(0.0F);
		}
		k.at(action.durationTicks()).stance();
	}

	private static void lead(Keys k, boolean left, float x, float y, float z, float fore) {
		if (left) k.off(x, y, z, fore);
		else k.arm(x, y, z).fore(fore);
	}

	private static void trail(Keys k, boolean leadIsLeft, float x, float fore) {
		float s = leadIsLeft ? 1.0F : -1.0F;
		if (leadIsLeft) k.arm(x, 0.35F * s, 0.4F * s).fore(fore);
		else k.off(x, 0.35F * s, 0.4F * s, fore);
	}

	// ------------------------------------------------------------------------------------------------ applying

	/** Writes a pose onto the rig, over a freshly reset model. */
	static void apply(Rig r, float[] p) {
		rot(r.rightArm(), p[AX], p[AY], p[AZ]);
		r.rightFore().xRot = p[AF];
		rot(r.leftArm(), p[OX], p[OY], p[OZ]);
		r.leftFore().xRot = p[OF];
		rot(r.body(), p[TX], p[TY], p[TZ]);
		r.head().xRot = p[HEAD];
		r.whole().yRot = p[SPIN] * Mth.TWO_PI;
		float lunge = p[LUNGE];
		float crouch = p[CROUCH];
		float kick = p[KICK];
		// Left foot leads; the right drives back. Crouching folds both knees, and the hips settle onto the boots.
		r.leftLeg().xRot = -0.45F * lunge - 0.38F * crouch - 1.2F * kick;
		r.leftShin().xRot = 0.5F * lunge + 0.8F * crouch + 0.35F * kick;
		r.rightLeg().xRot = 0.32F * lunge - 0.38F * crouch - 1.0F * kick;
		r.rightShin().xRot = 0.15F * lunge + 0.8F * crouch + 0.5F * kick;
		r.leftLeg().yRot = -0.1F * lunge;
		r.rightLeg().yRot = 0.16F * lunge;
	}

	private static void rot(ModelPart part, float x, float y, float z) {
		part.xRot = x;
		part.yRot = y;
		part.zRot = z;
	}

	// ------------------------------------------------------------------------------------------------ timing

	static float ramp(float t, float from, float to) {
		float x = Mth.clamp((t - from) / (to - from), 0.0F, 1.0F);
		return x * x * (3.0F - 2.0F * x);
	}

	/** In, held, out: 0 before a, 1 from b to c, 0 after d. */
	static float window(float t, float a, float b, float c, float d) {
		return ramp(t, a, b) * (1.0F - ramp(t, c, d));
	}

	/** A move's poses on its own clock, joined by Catmull-Rom curves. */
	static final class Keys {
		private final float[] base;
		private final float sign;
		private float[] times = new float[24];
		private float[][] poses = new float[24][];
		private int count;
		private boolean twoHanded;
		private boolean mirrored;
		private float[] current;

		Keys(float[] base, float sign) {
			this.base = base;
			this.sign = sign;
			add(0.0F, base.clone());
		}

		/** Both hands on the haft for every key. */
		Keys twoHanded() {
			this.twoHanded = true;
			return this;
		}

		/** The move cuts the other way on alternate variants. */
		Keys mirrored() {
			this.mirrored = true;
			return this;
		}

		/** Starts a key at {@code time}, carrying every channel over from the key before it. */
		Keys at(float time) {
			if (time <= times[count - 1]) {
				// At or before the last key: keep editing it rather than running the clock backwards.
				current = poses[count - 1];
				return this;
			}
			add(time, poses[count - 1].clone());
			return this;
		}

		private void add(float time, float[] pose) {
			if (count == times.length) {
				times = java.util.Arrays.copyOf(times, count * 2);
				poses = java.util.Arrays.copyOf(poses, count * 2);
			}
			times[count] = time;
			poses[count] = pose;
			current = pose;
			count++;
		}

		Keys arm(float x, float y, float z) {
			current[AX] = x; current[AY] = y; current[AZ] = z;
			return this;
		}

		Keys fore(float x) {
			current[AF] = x;
			return this;
		}

		Keys off(float x, float y, float z, float fore) {
			current[OX] = x; current[OY] = y; current[OZ] = z; current[OF] = fore;
			return this;
		}

		Keys torso(float x, float y, float z) {
			current[TX] = x; current[TY] = y; current[TZ] = z;
			return this;
		}

		Keys lunge(float v) { current[LUNGE] = v; return this; }
		Keys crouch(float v) { current[CROUCH] = v; return this; }
		Keys kick(float v) { current[KICK] = v; return this; }
		Keys head(float v) { current[HEAD] = v; return this; }
		Keys spin(float turns) { current[SPIN] = turns; return this; }

		/** Back to the guard the move started from, keeping however far he has spun. */
		Keys stance() {
			float spin = current[SPIN];
			System.arraycopy(base, 0, current, 0, CHANNELS);
			current[SPIN] = spin;
			return this;
		}

		float[] sample(float t) {
			float[] out = new float[CHANNELS];
			for (int ch = 0; ch < CHANNELS; ch++) out[ch] = curve(t, ch);
			if (mirrored && sign < 0.0F) {
				// Cut the other way across his centre line. The ends of the move still meet the stance, because
				// act() fades the keyed pose back into it.
				out[AY] = -out[AY];
				out[OY] = -out[OY];
				out[TY] = -out[TY];
				out[TZ] = -out[TZ];
				out[SPIN] = -out[SPIN];
			}
			if (twoHanded) grip(out);
			return out;
		}

		/**
		 * Catmull-Rom through the keys, held flat before the first and after the last. Velocity carries through each
		 * key, so a wind-up flows into its strike instead of stopping on every pose.
		 */
		private float curve(float t, int ch) {
			if (t <= times[0]) return poses[0][ch];
			if (t >= times[count - 1]) return poses[count - 1][ch];
			int i = 0;
			while (i < count - 2 && t > times[i + 1]) i++;
			float t1 = times[i], t2 = times[i + 1];
			float p1 = poses[i][ch], p2 = poses[i + 1][ch];
			float p0 = i > 0 ? poses[i - 1][ch] : p1;
			float t0 = i > 0 ? times[i - 1] : t1 - (t2 - t1);
			float p3 = i + 2 < count ? poses[i + 2][ch] : p2;
			float t3 = i + 2 < count ? times[i + 2] : t2 + (t2 - t1);
			float s = (t - t1) / (t2 - t1);
			// Tangents scaled to each segment's length so uneven key spacing does not overshoot wildly.
			float m1 = (p2 - p0) / (t2 - t0) * (t2 - t1);
			float m2 = (p3 - p1) / (t3 - t1) * (t2 - t1);
			float s2 = s * s, s3 = s2 * s;
			return (2 * s3 - 3 * s2 + 1) * p1 + (s3 - 2 * s2 + s) * m1 + (-2 * s3 + 3 * s2) * p2 + (s3 - s2) * m2;
		}
	}
}
