package com.vincenthuto.hemomancy.client.model.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperWeaponAction;
import net.minecraft.util.Mth;

/** Fractional-tick motion curves for Vesper's living-weapon poses. */
public final class VesperWeaponAnimationRules {
	private static final float ENTRY_TICKS = 6.0F;
	private static final float EXIT_TICKS = 7.0F;

	private VesperWeaponAnimationRules() { }

	public static float actionBlend(VesperWeaponAction action, float tick) {
		if (action == VesperWeaponAction.NONE || action.durationTicks() <= 0) return 0.0F;
		float entry = smootherstep(tick / ENTRY_TICKS);
		float exit = smootherstep((action.durationTicks() - tick) / EXIT_TICKS);
		return Math.min(entry, exit);
	}

	public static float swingArc(VesperWeaponAction action, float tick) {
		if (action == VesperWeaponAction.NONE || action.contactCount() == 0) return 0.0F;
		float contact = action.contactTick(0);
		float loaded = Math.max(1.0F, contact - 3.0F);
		float followThrough = Math.min(action.durationTicks(), contact + 1.0F);
		if (tick <= 0.0F || tick >= action.durationTicks()) return 0.0F;
		if (tick <= loaded) return -smootherstep(tick / loaded);
		if (tick <= followThrough) {
			return Mth.lerp(smootherstep((tick - loaded) / Math.max(1.0F, followThrough - loaded)), -1.0F, 1.0F);
		}
		return 1.0F - smootherstep((tick - followThrough)
				/ Math.max(1.0F, action.durationTicks() - followThrough));
	}

	public static float contactMotion(VesperWeaponAction action, float tick) {
		if (action == VesperWeaponAction.NONE || action.contactCount() == 0) return 0.0F;
		float first = action.contactTick(0);
		if (tick <= first) {
			float sign = 1.0F;
			float start = Math.max(0.0F, first - 6.0F);
			float loaded = Math.max(start + 1.0F, first - 3.0F);
			if (tick <= start) return 0.0F;
			if (tick <= loaded) {
				return Mth.lerp(smootherstep((tick - start) / Math.max(1.0F, loaded - start)), 0.0F, -0.32F * sign);
			}
			return Mth.lerp(smootherstep((tick - loaded) / Math.max(1.0F, first - loaded)), -0.32F * sign, sign);
		}
		for (int i = 0; i < action.contactCount() - 1; i++) {
			float fromTick = action.contactTick(i);
			float toTick = action.contactTick(i + 1);
			if (tick <= toTick) {
				float from = (i & 1) == 0 ? 1.0F : -1.0F;
				float to = -from;
				return Mth.lerp(smootherstep((tick - fromTick) / Math.max(1.0F, toTick - fromTick)), from, to);
			}
		}
		int lastIndex = action.contactCount() - 1;
		float lastTick = action.contactTick(lastIndex);
		float lastSign = (lastIndex & 1) == 0 ? 1.0F : -1.0F;
		return lastSign * (1.0F - smootherstep((tick - lastTick)
				/ Math.max(1.0F, action.durationTicks() - lastTick)));
	}

	public static float stanceBlend(float stanceTick) {
		return smoothstep(stanceTick / 30.0F);
	}

	public static float cycloneSpin(VesperWeaponAction action, float tick, float direction) {
		if (action == VesperWeaponAction.NONE || action.durationTicks() <= 0) return 0.0F;
		float contact = Math.max(1.0F, action.impactTick());
		if (tick <= contact) return -0.7F * smootherstep(tick / contact) * direction;
		float recovery = smootherstep((tick - contact)
				/ Math.max(1.0F, action.durationTicks() - contact));
		return Mth.lerp(recovery, -0.7F, Mth.TWO_PI * 2.0F) * direction;
	}

	public static float broodTramplePitch(float tick) {
		if (tick <= 22.0F) return Mth.lerp(smootherstep(tick / 22.0F), 0.0F, -0.16F);
		if (tick <= 32.0F) return -0.16F;
		if (tick <= 38.0F) return Mth.lerp(smootherstep((tick - 32.0F) / 6.0F), -0.16F, 0.22F);
		return Mth.lerp(smootherstep((tick - 38.0F) / 24.0F), 0.22F, 0.0F);
	}

	public static float stingerMotion(float tick) {
		if (tick <= 8.0F) return 0.0F;
		if (tick <= 16.0F) return smootherstep((tick - 8.0F) / 8.0F);
		float[] contacts = { 16.0F, 28.0F, 40.0F, 52.0F };
		for (int i = 0; i < contacts.length - 1; i++) {
			if (tick <= contacts[i + 1]) {
				float from = (i & 1) == 0 ? 1.0F : -1.0F;
				return Mth.lerp(smootherstep((tick - contacts[i]) / 12.0F), from, -from);
			}
		}
		return Mth.lerp(smootherstep((tick - 52.0F) / 16.0F), -1.0F, 0.0F);
	}

	public static float flailArmMotion(VesperWeaponAction action, float tick) {
		return switch (action) {
			case CHAIN_SWEEP -> curve(tick,
					0.0F, 0.0F, 6.0F, -0.25F, 12.0F, -0.78F,
					18.0F, 0.55F, 22.0F, 1.0F, 30.0F, 0.0F);
			case HOOK_AND_CRUSH -> curve(tick,
					0.0F, 0.0F, 8.0F, -0.55F, 16.0F, 0.9F, 22.0F, 0.35F,
					28.0F, -0.9F, 34.0F, -0.6F, 40.0F, 0.0F);
			default -> 0.0F;
		};
	}

	public static float flailFollowMotion(VesperWeaponAction action, float tick) {
		return switch (action) {
			case CHAIN_SWEEP -> curve(tick,
					0.0F, 0.0F, 8.0F, -0.25F, 14.0F, -0.72F,
					20.0F, 0.45F, 24.0F, 0.85F, 30.0F, 0.0F);
			case HOOK_AND_CRUSH -> curve(tick,
					0.0F, 0.0F, 10.0F, -0.5F, 18.0F, 0.75F, 24.0F, 0.3F,
					30.0F, -0.75F, 35.0F, -0.5F, 40.0F, 0.0F);
			default -> 0.0F;
		};
	}

	public static OffhandGrip twoHandedGrip(EnumBloodTendency tendency, float motion) {
		return switch (tendency) {
			case ANIMUS -> new OffhandGrip(-0.3840F, -0.2555F, 1.2013F,
					-0.4760F, 0.0605F, 0.6636F);
			case MORTEM -> new OffhandGrip(-0.4053F, -0.1370F, 1.3111F,
					-0.6113F, 0.2848F, 0.9744F);
			default -> new OffhandGrip(0.0F, 0.0F, 0.0F, -1.0472F, 0.0F, 0.0F);
		};
	}

	public static OffhandGrip twoHandedGrip(VesperWeaponAction action, float motion) {
		return switch (action) {
			case ICHIMONJI -> interpolateGrip(motion,
					new OffhandGrip(-0.4101F, 0.2573F, 2.1228F, -0.1665F, -0.0509F, 0.7013F),
					new OffhandGrip(-0.7057F, 0.0813F, 1.9849F, -0.2312F, -0.1239F, 0.3728F),
					new OffhandGrip(-0.8480F, -0.1614F, 1.7449F, -0.2750F, -0.1745F, 0.1648F),
					new OffhandGrip(-0.8195F, -0.5072F, 1.6567F, -0.4752F, -0.3165F, -0.1058F),
					new OffhandGrip(-0.7323F, -0.8924F, 1.5882F, -0.6309F, -0.4723F, -0.3922F));
			case CROSSCUT -> interpolateGrip(motion,
					new OffhandGrip(-0.4373F, -0.2679F, 1.2184F, -0.5963F, 0.1482F, 0.7487F),
					new OffhandGrip(-0.4710F, -0.1571F, 1.3451F, -0.5253F, 0.1316F, 0.7041F),
					new OffhandGrip(-0.4912F, -0.0822F, 1.4290F, -0.5412F, 0.1727F, 0.7829F),
					new OffhandGrip(-0.5040F, -0.0973F, 1.4258F, -0.6250F, 0.2430F, 0.8968F),
					new OffhandGrip(-0.5117F, -0.1344F, 1.4017F, -0.7072F, 0.3011F, 0.9921F));
			case LEAPING_CLEAVE -> interpolateGrip(motion,
					new OffhandGrip(-0.2018F, 0.0610F, 2.2429F, -0.0681F, -0.0910F, 0.6300F),
					new OffhandGrip(-0.5428F, -0.0292F, 2.1796F, -0.2281F, -0.1161F, 0.5507F),
					new OffhandGrip(-0.7856F, -0.1554F, 2.0346F, -0.3252F, -0.1682F, 0.3300F),
					new OffhandGrip(-0.9789F, -0.1849F, 1.7235F, 0.0139F, -0.2249F, -0.2053F),
					new OffhandGrip(-0.7289F, -0.5558F, 1.4817F, 0.0165F, -0.3217F, -0.2116F));
			case REAPER_SWEEP -> interpolateGrip(motion,
					new OffhandGrip(-0.8712F, 0.1671F, 1.6024F, 0.0121F, -0.1409F, -0.2003F),
					new OffhandGrip(-0.8374F, 0.2664F, 1.7487F, 0.0133F, -0.1874F, -0.2030F),
					new OffhandGrip(-1.0552F, 0.1358F, 1.6369F, 0.2553F, -0.2958F, 0.3549F),
					new OffhandGrip(-1.2404F, 0.0268F, 1.6516F, 0.2894F, -0.3757F, 0.5737F),
					new OffhandGrip(-1.4163F, -0.1123F, 1.6445F, 0.2972F, -0.4346F, 0.7260F));
			default -> twoHandedGrip(action.tendency() == null ? EnumBloodTendency.ANIMUS : action.tendency(), 0.0F);
		};
	}

	private static OffhandGrip interpolateGrip(float motion, OffhandGrip loaded, OffhandGrip loadedMid,
			OffhandGrip neutral, OffhandGrip followMid, OffhandGrip followThrough) {
		float clamped = Mth.clamp(motion, -1.0F, 1.0F);
		if (clamped <= -0.5F) return lerpGrip((clamped + 1.0F) * 2.0F, loaded, loadedMid);
		if (clamped <= 0.0F) return lerpGrip((clamped + 0.5F) * 2.0F, loadedMid, neutral);
		if (clamped <= 0.5F) return lerpGrip(clamped * 2.0F, neutral, followMid);
		return lerpGrip((clamped - 0.5F) * 2.0F, followMid, followThrough);
	}

	private static OffhandGrip lerpGrip(float amount, OffhandGrip from, OffhandGrip to) {
		return new OffhandGrip(
				Mth.lerp(amount, from.armX, to.armX), Mth.lerp(amount, from.armY, to.armY),
				Mth.lerp(amount, from.armZ, to.armZ), Mth.lerp(amount, from.elbowX, to.elbowX),
				Mth.lerp(amount, from.elbowY, to.elbowY), Mth.lerp(amount, from.elbowZ, to.elbowZ));
	}

	private static float curve(float tick, float... keys) {
		if (keys.length < 4 || tick <= keys[0]) return keys.length < 2 ? 0.0F : keys[1];
		for (int i = 2; i < keys.length; i += 2) {
			if (tick <= keys[i]) {
				float progress = (tick - keys[i - 2]) / Math.max(1.0F, keys[i] - keys[i - 2]);
				return Mth.lerp(smootherstep(progress), keys[i - 1], keys[i + 1]);
			}
		}
		return keys[keys.length - 1];
	}

	public record OffhandGrip(float armX, float armY, float armZ,
			float elbowX, float elbowY, float elbowZ) { }

	private static float smoothstep(float value) {
		float t = Mth.clamp(value, 0.0F, 1.0F);
		return t * t * (3.0F - 2.0F * t);
	}

	private static float smootherstep(float value) {
		float t = Mth.clamp(value, 0.0F, 1.0F);
		return t * t * t * (t * (t * 6.0F - 15.0F) + 10.0F);
	}
}
