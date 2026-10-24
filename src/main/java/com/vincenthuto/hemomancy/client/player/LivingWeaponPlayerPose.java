package com.vincenthuto.hemomancy.client.player;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingAxeItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingBaghnakhItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingBladeItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingFlailItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingSickleItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingSpearItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingTorchItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.SanguisLanceaItem;
import com.vincenthuto.hemomancy.common.item.unstained.tool.AnnettasSanguisLanceaItem;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

import static com.vincenthuto.hemomancy.client.player.SwingCurve.Ease.IN;
import static com.vincenthuto.hemomancy.client.player.SwingCurve.Ease.LINEAR;
import static com.vincenthuto.hemomancy.client.player.SwingCurve.Ease.OUT;
import static com.vincenthuto.hemomancy.client.player.SwingCurve.Ease.SHARP;
import static com.vincenthuto.hemomancy.client.player.SwingCurve.Ease.SMOOTH;

/**
 * Keyframed attack strokes for the Living Arsenal, authored for the right hand and mirrored for the left.
 *
 * <p>Third person: arm pitch is absolute (negative raises the arm forward); arm yaw is positive inward across
 * the body; arm roll is positive outward; torso twist is positive with the weapon shoulder drawn back, matching
 * vanilla's attack wind-up. First person follows vanilla's hand transform: a stroke's {@code plane} yaws the
 * frame in which pitch swings the weapon (0 chops straight down, about 45 degrees cuts diagonally like vanilla,
 * near 90 degrees sweeps across the screen); negative pitch drives the weapon forward and down.
 */
public final class LivingWeaponPlayerPose {
	private static final float HELD_X = -Mth.PI / 10.0F;
	private static final SwingCurve REST = SwingCurve.from(0).build();
	private static final SwingCurve HELD = SwingCurve.from(HELD_X).build();
	private static final SwingCurve WEIGHT = SwingCurve.from(0).to(.08F, 1, OUT).to(.88F, 1, LINEAR)
			.to(1, 0, SMOOTH).build();

	public enum Stroke {
		/** Ferric axe: lift, hang, an accelerating chop that bites, a wrench to pull it free. */
		AXE(new Motion.Builder()
				.armX(SwingCurve.from(HELD_X).to(.30F, -2.75F, SMOOTH).to(.36F, -2.88F, OUT).to(.46F, .45F, IN)
						.to(.54F, .36F, OUT).to(.64F, .05F, SMOOTH).to(1, HELD_X, SMOOTH).build())
				.armY(SwingCurve.from(0).to(.30F, -.16F, SMOOTH).to(.46F, .24F, IN).to(1, 0, SMOOTH).build())
				.armZ(SwingCurve.from(0).to(.30F, .22F, SMOOTH).to(.46F, .08F, IN).to(1, 0, SMOOTH).build())
				.twist(SwingCurve.from(0).to(.36F, .30F, SMOOTH).to(.46F, -.18F, IN).to(.54F, -.14F, OUT)
						.to(1, 0, SMOOTH).build())
				.offX(SwingCurve.from(0).to(.30F, -.50F, SMOOTH).to(.46F, .35F, IN).to(1, 0, SMOOTH).build())
				.plane(.20F)
				.y(SwingCurve.from(0).to(.30F, .26F, SMOOTH).to(.36F, .30F, OUT).to(.46F, -.30F, IN)
						.to(.54F, -.26F, OUT).to(.64F, -.16F, SMOOTH).to(1, 0, SMOOTH).build())
				.z(SwingCurve.from(0).to(.36F, .06F, SMOOTH).to(.46F, -.22F, IN).to(1, 0, SMOOTH).build())
				.x(SwingCurve.from(0).to(.36F, -.10F, SMOOTH).to(.46F, -.16F, IN).to(1, 0, SMOOTH).build())
				.pitch(SwingCurve.from(0).to(.30F, .50F, SMOOTH).to(.36F, .60F, OUT).to(.46F, -1.15F, IN)
						.to(.54F, -1.02F, OUT).to(.64F, -.80F, SMOOTH).to(1, 0, SMOOTH).build())
				.roll(SwingCurve.from(0).to(.36F, -.12F, SMOOTH).to(.46F, .18F, IN).to(1, 0, SMOOTH).build())
				.jolt(.46F).build(), .46F),

		/** Animus blade, first cut: cocked high outside, then a diagonal cut down across the body. */
		BLADE_FOREHAND(new Motion.Builder()
				.armX(SwingCurve.from(HELD_X).to(.20F, -2.30F, SMOOTH).to(.40F, -.35F, IN).to(.52F, -.15F, OUT)
						.to(1, HELD_X, SMOOTH).build())
				.armY(SwingCurve.from(0).to(.20F, -.45F, SMOOTH).to(.40F, .55F, IN).to(.52F, .60F, OUT)
						.to(1, 0, SMOOTH).build())
				.armZ(SwingCurve.from(0).to(.20F, .25F, SMOOTH).to(.40F, -.10F, IN).to(1, 0, SMOOTH).build())
				.twist(SwingCurve.from(0).to(.20F, .25F, SMOOTH).to(.40F, -.30F, IN).to(.52F, -.34F, OUT)
						.to(1, 0, SMOOTH).build())
				.offX(SwingCurve.from(0).to(.20F, -.35F, SMOOTH).to(.40F, .25F, IN).to(1, 0, SMOOTH).build())
				.plane(.80F)
				.pitch(SwingCurve.from(0).to(.20F, .55F, SMOOTH).to(.40F, -1.55F, IN).to(.52F, -1.70F, OUT)
						.to(1, 0, SMOOTH).build())
				.roll(SwingCurve.from(0).to(.20F, .15F, SMOOTH).to(.40F, -.35F, IN).to(1, 0, SMOOTH).build())
				.x(SwingCurve.from(0).to(.20F, .10F, SMOOTH).to(.40F, -.42F, IN).to(.52F, -.46F, OUT)
						.to(1, 0, SMOOTH).build())
				.y(SwingCurve.from(0).to(.20F, .14F, SMOOTH).to(.40F, -.12F, IN).to(1, 0, SMOOTH).build())
				.z(SwingCurve.from(0).to(.40F, -.22F, IN).to(1, 0, SMOOTH).build()).build(), .40F),

		/** Animus blade, answering cut: drawn low across the body, then a rising backhand to the outside. */
		BLADE_BACKHAND(new Motion.Builder()
				.armX(SwingCurve.from(HELD_X).to(.20F, -.25F, SMOOTH).to(.40F, -1.95F, IN).to(.52F, -2.10F, OUT)
						.to(1, HELD_X, SMOOTH).build())
				.armY(SwingCurve.from(0).to(.20F, .60F, SMOOTH).to(.40F, -.50F, IN).to(.52F, -.56F, OUT)
						.to(1, 0, SMOOTH).build())
				.armZ(SwingCurve.from(0).to(.20F, -.15F, SMOOTH).to(.40F, .30F, IN).to(1, 0, SMOOTH).build())
				.twist(SwingCurve.from(0).to(.20F, -.30F, SMOOTH).to(.40F, .25F, IN).to(.52F, .28F, OUT)
						.to(1, 0, SMOOTH).build())
				.offX(SwingCurve.from(0).to(.20F, .20F, SMOOTH).to(.40F, -.30F, IN).to(1, 0, SMOOTH).build())
				.plane(1.10F)
				.pitch(SwingCurve.from(0).to(.20F, -1.25F, SMOOTH).to(.40F, .55F, IN).to(.52F, .70F, OUT)
						.to(1, 0, SMOOTH).build())
				.roll(SwingCurve.from(0).to(.20F, -.30F, SMOOTH).to(.40F, .25F, IN).to(1, 0, SMOOTH).build())
				.x(SwingCurve.from(0).to(.20F, -.36F, SMOOTH).to(.40F, .12F, IN).to(.52F, .16F, OUT)
						.to(1, 0, SMOOTH).build())
				.y(SwingCurve.from(0).to(.20F, -.16F, SMOOTH).to(.40F, .12F, IN).to(1, 0, SMOOTH).build())
				.z(SwingCurve.from(0).to(.40F, -.20F, IN).to(1, 0, SMOOTH).build()).build(), .40F),

		/** Lux spear: drawn back along the shaft, driven straight out with a corkscrew, then reeled in. */
		SPEAR(new Motion.Builder()
				.armX(SwingCurve.from(HELD_X).to(.22F, -.25F, SMOOTH).to(.38F, -1.55F, IN).to(.50F, -1.50F, OUT)
						.to(1, HELD_X, SMOOTH).build())
				.armY(SwingCurve.from(0).to(.22F, -.10F, SMOOTH).to(.38F, .12F, IN).to(1, 0, SMOOTH).build())
				.twist(SwingCurve.from(0).to(.22F, .30F, SMOOTH).to(.38F, -.25F, IN).to(.50F, -.22F, OUT)
						.to(1, 0, SMOOTH).build())
				.offX(SwingCurve.from(0).to(.22F, -.90F, SMOOTH).to(.38F, -1.20F, IN).to(1, 0, SMOOTH).build())
				.offY(SwingCurve.from(0).to(.22F, .50F, SMOOTH).to(.38F, .60F, IN).to(1, 0, SMOOTH).build())
				.z(SwingCurve.from(0).to(.22F, .20F, SMOOTH).to(.38F, -.55F, IN).to(.50F, -.50F, OUT)
						.to(1, 0, SMOOTH).build())
				.y(SwingCurve.from(0).to(.22F, -.04F, SMOOTH).to(.38F, .06F, IN).to(1, 0, SMOOTH).build())
				.x(SwingCurve.from(0).to(.22F, .04F, SMOOTH).to(.38F, -.18F, IN).to(1, 0, SMOOTH).build())
				.pitch(SwingCurve.from(0).to(.22F, .12F, SMOOTH).to(.38F, -.15F, IN).to(1, 0, SMOOTH).build())
				.roll(SwingCurve.from(0).to(.22F, -.10F, SMOOTH).to(.38F, .25F, IN).to(.50F, .30F, OUT)
						.to(1, 0, SMOOTH).build()).build(), .38F),

		/** Sanguis Lancea: raised overhand, then a heavy downward-forward drive. */
		LANCEA(new Motion.Builder()
				.armX(SwingCurve.from(HELD_X).to(.24F, -2.60F, SMOOTH).to(.42F, -1.30F, IN).to(.54F, -1.20F, OUT)
						.to(1, HELD_X, SMOOTH).build())
				.armY(SwingCurve.from(0).to(.24F, -.12F, SMOOTH).to(.42F, .16F, IN).to(1, 0, SMOOTH).build())
				.twist(SwingCurve.from(0).to(.24F, .32F, SMOOTH).to(.42F, -.26F, IN).to(.54F, -.22F, OUT)
						.to(1, 0, SMOOTH).build())
				.offX(SwingCurve.from(0).to(.24F, -.60F, SMOOTH).to(.42F, .30F, IN).to(1, 0, SMOOTH).build())
				.y(SwingCurve.from(0).to(.24F, .30F, SMOOTH).to(.42F, -.04F, IN).to(1, 0, SMOOTH).build())
				.z(SwingCurve.from(0).to(.24F, .15F, SMOOTH).to(.42F, -.55F, IN).to(.54F, -.50F, OUT)
						.to(1, 0, SMOOTH).build())
				.x(SwingCurve.from(0).to(.42F, -.14F, IN).to(1, 0, SMOOTH).build())
				.pitch(SwingCurve.from(0).to(.24F, .30F, SMOOTH).to(.42F, -.35F, IN).to(.54F, -.30F, OUT)
						.to(1, 0, SMOOTH).build()).build(), .42F),

		/** Flammeus torch: a short draw, a lunging jab, and a flick of the crown as the flame flares. */
		TORCH(new Motion.Builder()
				.armX(SwingCurve.from(HELD_X).to(.20F, -.55F, SMOOTH).to(.36F, -1.55F, IN).to(.48F, -1.70F, OUT)
						.to(1, HELD_X, SMOOTH).build())
				.armY(SwingCurve.from(0).to(.20F, -.08F, SMOOTH).to(.36F, .10F, IN).to(1, 0, SMOOTH).build())
				.twist(SwingCurve.from(0).to(.20F, .18F, SMOOTH).to(.36F, -.14F, IN).to(1, 0, SMOOTH).build())
				.offX(SwingCurve.from(0).to(.20F, -.25F, SMOOTH).to(.36F, .20F, IN).to(1, 0, SMOOTH).build())
				.z(SwingCurve.from(0).to(.20F, .12F, SMOOTH).to(.36F, -.45F, IN).to(.48F, -.40F, OUT)
						.to(1, 0, SMOOTH).build())
				.y(SwingCurve.from(0).to(.20F, -.06F, SMOOTH).to(.36F, .04F, IN).to(.48F, .10F, OUT)
						.to(1, 0, SMOOTH).build())
				.x(SwingCurve.from(0).to(.36F, -.14F, IN).to(1, 0, SMOOTH).build())
				.pitch(SwingCurve.from(0).to(.20F, .15F, SMOOTH).to(.36F, -.25F, IN).to(.48F, .10F, OUT)
						.to(1, 0, SMOOTH).build()).build(), .36F),

		/** Tenebris claws, first rake: cocked high, then torn down and inward. */
		RAKE_INWARD(rake(1), .36F),
		/** Tenebris claws, answering rake: torn down and outward. */
		RAKE_OUTWARD(rake(-1), .36F),

		/** Congeatio flail: the handle stirs the head overhead, then lashes it down in front. */
		FLAIL(new Motion.Builder()
				.armX(SwingCurve.from(HELD_X).to(.30F, -2.70F, SMOOTH).to(.44F, -.20F, IN).to(.54F, -.05F, OUT)
						.to(1, HELD_X, SMOOTH).build())
				.armY(SwingCurve.from(0).to(.30F, -.10F, SMOOTH).to(.44F, .25F, IN).to(1, 0, SMOOTH).build())
				.armZ(SwingCurve.from(0).to(.10F, .25F, SMOOTH).to(.20F, -.15F, SMOOTH).to(.30F, .20F, SMOOTH)
						.to(.44F, 0, IN).to(1, 0, SMOOTH).build())
				.twist(SwingCurve.from(0).to(.30F, .25F, SMOOTH).to(.44F, -.22F, IN).to(1, 0, SMOOTH).build())
				.offX(SwingCurve.from(0).to(.30F, -.40F, SMOOTH).to(.44F, .30F, IN).to(1, 0, SMOOTH).build())
				.plane(.35F)
				.pitch(SwingCurve.from(0).to(.30F, .70F, SMOOTH).to(.44F, -1.25F, IN).to(.54F, -1.10F, OUT)
						.to(1, 0, SMOOTH).build())
				.roll(SwingCurve.from(0).to(.10F, .35F, SMOOTH).to(.20F, -.30F, SMOOTH).to(.30F, .25F, SMOOTH)
						.to(.44F, -.20F, IN).to(1, 0, SMOOTH).build())
				.y(SwingCurve.from(0).to(.30F, .28F, SMOOTH).to(.44F, -.24F, IN).to(.54F, -.20F, OUT)
						.to(1, 0, SMOOTH).build())
				.z(SwingCurve.from(0).to(.30F, .06F, SMOOTH).to(.44F, -.28F, IN).to(1, 0, SMOOTH).build())
				.x(SwingCurve.from(0).to(.15F, .06F, SMOOTH).to(.30F, -.04F, SMOOTH).to(.44F, -.14F, IN)
						.to(1, 0, SMOOTH).build())
				.jolt(.44F).build(), .44F),

		/**
		 * Mortem sickle: cocked high over the weapon shoulder, hooked down and across so the point bites, then drawn
		 * back through the cut to finish low before the far hip. The honed inner curve faces forward from the grip, so
		 * first person lays the blade over (roll) to keep the crescent in view as it reaches past the crosshair and is
		 * pulled back toward the reaper.
		 */
		SICKLE(new Motion.Builder()
				.armX(SwingCurve.from(HELD_X).to(.24F, -2.50F, SMOOTH).to(.40F, -1.05F, IN).to(.58F, -.40F, OUT)
						.to(1, HELD_X, SMOOTH).build())
				.armY(SwingCurve.from(0).to(.24F, -.55F, SMOOTH).to(.40F, .55F, IN).to(.58F, .80F, OUT)
						.to(1, 0, SMOOTH).build())
				.armZ(SwingCurve.from(0).to(.24F, .45F, SMOOTH).to(.40F, 0, IN).to(.58F, -.15F, OUT)
						.to(1, 0, SMOOTH).build())
				.twist(SwingCurve.from(0).to(.24F, .32F, SMOOTH).to(.40F, -.32F, IN).to(.58F, -.22F, OUT)
						.to(1, 0, SMOOTH).build())
				.offX(SwingCurve.from(0).to(.24F, -.45F, SMOOTH).to(.40F, .20F, IN).to(.58F, .30F, OUT)
						.to(1, 0, SMOOTH).build())
				.x(SwingCurve.from(0).to(.24F, -.22F, SMOOTH).to(.40F, -.32F, IN).to(.58F, -.40F, OUT)
						.to(1, 0, SMOOTH).build())
				.y(SwingCurve.from(0).to(.24F, .04F, SMOOTH).to(.40F, .08F, IN).to(.58F, -.10F, OUT)
						.to(1, 0, SMOOTH).build())
				.z(SwingCurve.from(0).to(.40F, -.14F, IN).to(.58F, .08F, OUT).to(1, 0, SMOOTH).build())
				.roll(SwingCurve.from(0).to(.24F, .12F, SMOOTH).to(.40F, .95F, IN).to(.58F, 1.00F, OUT)
						.to(1, 0, SMOOTH).build())
				.pitch(SwingCurve.from(0).to(.24F, .16F, SMOOTH).to(.40F, -.35F, IN).to(.58F, -.10F, OUT)
						.to(1, 0, SMOOTH).build())
				.jolt(.40F).build(), .40F);

		private final Motion motion;
		/** Progress at which the stroke visibly lands. */
		public final float contact;

		Stroke(Motion motion, float contact) {
			this.motion = motion;
			this.contact = contact;
		}
	}

	private LivingWeaponPlayerPose() { }

	/** The stroke for a Living Arsenal weapon, or null for items without one. */
	@Nullable
	public static Stroke stroke(Item item, int combo) {
		if (item instanceof LivingAxeItem) return Stroke.AXE;
		if (item instanceof LivingBladeItem) return (combo & 1) == 0 ? Stroke.BLADE_FOREHAND : Stroke.BLADE_BACKHAND;
		if (item instanceof SanguisLanceaItem || item instanceof AnnettasSanguisLanceaItem) return Stroke.LANCEA;
		if (item instanceof LivingSpearItem) return Stroke.SPEAR;
		if (item instanceof LivingTorchItem) return Stroke.TORCH;
		if (item instanceof LivingBaghnakhItem) return (combo & 1) == 0 ? Stroke.RAKE_INWARD : Stroke.RAKE_OUTWARD;
		if (item instanceof LivingFlailItem) return Stroke.FLAIL;
		if (item instanceof LivingSickleItem) return Stroke.SICKLE;
		return null;
	}

	/** First-person hand transform for a Living Arsenal weapon; false leaves any other item to vanilla. */
	public static boolean applyFirstPerson(PoseStack poses, LivingEntity holder, HumanoidArm arm, ItemStack stack,
			float equip, float swing) {
		Stroke stroke = stroke(stack.getItem(), SwingComboTracker.strike(holder, arm, swing));
		if (stroke == null) return false;
		firstPerson(poses, stroke, swing, equip, arm == HumanoidArm.RIGHT);
		return true;
	}

	public static UnstainedWeaponPlayerPose.ArmPose thirdPerson(Stroke stroke, float progress, boolean rightHanded) {
		float p = Mth.clamp(progress, 0, 1);
		Motion m = stroke.motion;
		float side = rightHanded ? 1 : -1;
		return new UnstainedWeaponPlayerPose.ArmPose(m.armX.at(p), -side * m.armY.at(p), side * m.armZ.at(p),
				side * m.twist.at(p), WEIGHT.at(p), m.offX.at(p), side * m.offY.at(p), 0);
	}

	public static void firstPerson(PoseStack poses, Stroke stroke, float progress, float equip, boolean rightHanded) {
		float p = Mth.clamp(progress, 0, 1);
		Motion m = stroke.motion;
		float side = rightHanded ? 1 : -1;
		float weight = WEIGHT.at(p);
		poses.translate(side * .56F, -.52F - equip * (1 - weight) * .6F, -.72F);
		poses.translate(side * m.x.at(p), m.y.at(p) + jolt(p, m.jolt), m.z.at(p));
		float plane = side * m.plane;
		poses.mulPose(Axis.YP.rotation(plane));
		poses.mulPose(Axis.ZP.rotation(side * m.roll.at(p)));
		poses.mulPose(Axis.XP.rotation(m.pitch.at(p)));
		poses.mulPose(Axis.YP.rotation(-plane));
	}

	/** A short damped shudder after a weapon bites, so heavy blows read as landing on something. */
	static float jolt(float progress, float contact) {
		if (contact < 0) return 0;
		float t = (progress - contact) / .18F;
		if (t <= 0 || t >= 1) return 0;
		return .022F * Mth.sin(t * Mth.TWO_PI * 2.0F) * (1 - t) * (1 - t);
	}

	private static Motion rake(float direction) {
		return new Motion.Builder()
				.armX(SwingCurve.from(HELD_X).to(.18F, -2.20F, SMOOTH).to(.36F, -.55F, IN).to(.48F, -.40F, OUT)
						.to(1, HELD_X, SMOOTH).build())
				.armY(SwingCurve.from(0).to(.18F, -.30F * direction, SMOOTH).to(.36F, .40F * direction, IN)
						.to(1, 0, SMOOTH).build())
				.armZ(SwingCurve.from(0).to(.18F, .15F, SMOOTH).to(.36F, -.05F, IN).to(1, 0, SMOOTH).build())
				.twist(SwingCurve.from(0).to(.18F, .20F * direction, SMOOTH).to(.36F, -.25F * direction, IN)
						.to(1, 0, SMOOTH).build())
				.offX(SwingCurve.from(0).to(.18F, -.45F, SMOOTH).to(.36F, .20F, IN).to(1, 0, SMOOTH).build())
				.plane(.60F * direction)
				.pitch(SwingCurve.from(0).to(.18F, .70F, SMOOTH).to(.36F, -1.20F, IN).to(.48F, -1.30F, OUT)
						.to(1, 0, SMOOTH).build())
				.roll(SwingCurve.from(0).to(.36F, -.30F * direction, IN).to(1, 0, SMOOTH).build())
				.x(SwingCurve.from(0).to(.18F, (direction > 0 ? .10F : -.25F), SMOOTH)
						.to(.36F, (direction > 0 ? -.30F : .12F), IN).to(1, 0, SMOOTH).build())
				.y(SwingCurve.from(0).to(.18F, .18F, SMOOTH).to(.36F, -.16F, IN).to(1, 0, SMOOTH).build())
				.z(SwingCurve.from(0).to(.36F, -.26F, IN).to(1, 0, SMOOTH).build())
				.build();
	}

	private record Motion(SwingCurve armX, SwingCurve armY, SwingCurve armZ, SwingCurve twist,
			SwingCurve offX, SwingCurve offY, float plane, SwingCurve x, SwingCurve y, SwingCurve z,
			SwingCurve roll, SwingCurve pitch, float jolt) {
		private static final class Builder {
			private SwingCurve armX = HELD, armY = REST, armZ = REST, twist = REST, offX = REST, offY = REST;
			private SwingCurve x = REST, y = REST, z = REST, roll = REST, pitch = REST;
			private float plane;
			private float jolt = -1;

			Builder armX(SwingCurve c) { armX = c; return this; }
			Builder armY(SwingCurve c) { armY = c; return this; }
			Builder armZ(SwingCurve c) { armZ = c; return this; }
			Builder twist(SwingCurve c) { twist = c; return this; }
			Builder offX(SwingCurve c) { offX = c; return this; }
			Builder offY(SwingCurve c) { offY = c; return this; }
			Builder plane(float radians) { plane = radians; return this; }
			Builder x(SwingCurve c) { x = c; return this; }
			Builder y(SwingCurve c) { y = c; return this; }
			Builder z(SwingCurve c) { z = c; return this; }
			Builder roll(SwingCurve c) { roll = c; return this; }
			Builder pitch(SwingCurve c) { pitch = c; return this; }
			Builder jolt(float contact) { jolt = contact; return this; }

			Motion build() {
				return new Motion(armX, armY, armZ, twist, offX, offY, plane, x, y, z, roll, pitch, jolt);
			}
		}
	}
}
