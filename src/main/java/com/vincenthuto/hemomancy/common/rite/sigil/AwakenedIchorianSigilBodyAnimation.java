package com.vincenthuto.hemomancy.common.rite.sigil;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class AwakenedIchorianSigilBodyAnimation {
	private AwakenedIchorianSigilBodyAnimation() {
	}

	public static BodyPose pose(ResourceLocation sigilId, float ageTicks, float movementSpeed) {
		String path = sigilId == null ? "" : sigilId.getPath();
		Profile profile = profile(path);
		float phase = Math.floorMod(path.hashCode(), 6283) / 1000.0F;
		float activity = 1.0F + Mth.clamp(movementSpeed * 8.0F, 0.0F, 0.65F);
		float primary = (float) Math.sin(ageTicks * profile.frequency() + phase);
		float secondary = (float) Math.sin(
				ageTicks * profile.frequency() * 1.73F + phase * 0.61F + 1.4F);
		float breath = (float) Math.sin(
				ageTicks * profile.frequency() * 0.72F + phase * 1.31F);

		float offsetX = primary * profile.shift() * activity;
		float offsetY = secondary * profile.shift() * 0.65F
				+ Math.abs(breath) * profile.shift() * 0.22F;
		float offsetZ = breath * profile.shift() * 0.42F;
		float yaw = secondary * profile.flexDegrees() * 0.55F * activity;
		float pitch = primary * profile.flexDegrees() * activity;
		float roll = breath * profile.flexDegrees() * 0.72F * activity;
		float pulse = breath * profile.pulse() * (0.80F + activity * 0.20F);
		return new BodyPose(offsetX, offsetY, offsetZ, yaw, pitch, roll,
				1.0F + pulse,
				1.0F - pulse * 0.82F,
				1.0F + pulse * 0.45F);
	}

	private static Profile profile(String path) {
		return switch (path) {
			case "reservoir" -> new Profile(0.052F, 0.026F, 3.0F, 0.060F);
			case "bastion" -> new Profile(0.071F, 0.030F, 4.2F, 0.028F);
			case "hematic_lattice" -> new Profile(0.088F, 0.038F, 5.5F, 0.038F);
			case "mnemonic" -> new Profile(0.064F, 0.047F, 7.0F, 0.032F);
			case "suture" -> new Profile(0.112F, 0.052F, 8.5F, 0.045F);
			case "shunt" -> new Profile(0.138F, 0.060F, 10.0F, 0.040F);
			case "seal" -> new Profile(0.043F, 0.020F, 2.2F, 0.026F);
			case "cage" -> new Profile(0.079F, 0.034F, 5.2F, 0.058F);
			case "lens" -> new Profile(0.067F, 0.044F, 7.8F, 0.030F);
			default -> new Profile(0.075F, 0.035F, 5.0F, 0.035F);
		};
	}

	public record BodyPose(float offsetX, float offsetY, float offsetZ,
			float yawDegrees, float pitchDegrees, float rollDegrees,
			float scaleX, float scaleY, float scaleZ) {
	}

	private record Profile(float frequency, float shift, float flexDegrees, float pulse) {
	}
}
