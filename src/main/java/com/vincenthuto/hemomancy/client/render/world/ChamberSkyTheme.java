package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.resources.ResourceLocation;

public record ChamberSkyTheme(
		ResourceLocation id,
		ResourceLocation skyTexture,
		ResourceLocation cloudTexture,
		ResourceLocation wispTexture,
		ResourceLocation noiseTexture,
		int skyboxColor,
		int cloudColor,
		int nebulaPrimary,
		int nebulaSecondary,
		int nebulaAccent,
		int capillaryTint,
		int bloodTint,
		int veinTint,
		int neuralTint,
		float pulseStrength,
		float motionMultiplier,
		int capillaryLayers,
		int blueVeinLayers,
		int bloodVesselLayers,
		int neuralLayers,
		boolean renderBaseSkybox,
		boolean renderCloudLayers,
		boolean renderNebulaLayers,
		boolean renderMembranePulse
) {
	public static Builder builder(ResourceLocation id) {
		return new Builder(id);
	}

	public static final class Builder {
		private final ResourceLocation id;
		private ResourceLocation skyTexture = Hemomancy.rloc("textures/environment/chamber_of_will_sky.png");
		private ResourceLocation cloudTexture = Hemomancy.rloc("textures/environment/chamber_of_will_clouds.png");
		private ResourceLocation wispTexture = Hemomancy.rloc("textures/environment/single_cloud.png");
		private ResourceLocation noiseTexture = Hemomancy.rloc("textures/environment/noise_tile.png");
		private int skyboxColor = 0xFF454545;
		private int cloudColor = 0xFF808080;
		private int nebulaPrimary = 0x362E00;
		private int nebulaSecondary = 0x801A00;
		private int nebulaAccent = 0x2E2600;
		private int capillaryTint = 0xFFFFFF;
		private int bloodTint = 0xFFFFFF;
		private int veinTint = 0xFFFFFF;
		private int neuralTint = 0xFFFFFF;
		private float pulseStrength = 1.0F;
		private float motionMultiplier = 1.0F;
		private int capillaryLayers = 1;
		private int blueVeinLayers = 2;
		private int bloodVesselLayers = 2;
		private int neuralLayers = 3;
		private boolean renderBaseSkybox = true;
		private boolean renderCloudLayers = true;
		private boolean renderNebulaLayers = true;
		private boolean renderMembranePulse = true;

		private Builder(ResourceLocation id) {
			this.id = id;
		}

		public Builder textures(ResourceLocation sky, ResourceLocation clouds, ResourceLocation wisp, ResourceLocation noise) {
			this.skyTexture = sky;
			this.cloudTexture = clouds;
			this.wispTexture = wisp;
			this.noiseTexture = noise;
			return this;
		}

		public Builder skybox(int skyboxColor, int cloudColor) {
			this.skyboxColor = skyboxColor;
			this.cloudColor = cloudColor;
			return this;
		}

		public Builder nebula(int primary, int secondary, int accent) {
			this.nebulaPrimary = primary;
			this.nebulaSecondary = secondary;
			this.nebulaAccent = accent;
			return this;
		}

		public Builder tints(int capillary, int blood, int vein, int neural) {
			this.capillaryTint = capillary;
			this.bloodTint = blood;
			this.veinTint = vein;
			this.neuralTint = neural;
			return this;
		}

		public Builder pulse(float pulseStrength) {
			this.pulseStrength = pulseStrength;
			return this;
		}

		public Builder motion(float motionMultiplier) {
			this.motionMultiplier = motionMultiplier;
			return this;
		}

		public Builder layers(int capillary, int blueVein, int bloodVessel, int neural) {
			this.capillaryLayers = capillary;
			this.blueVeinLayers = blueVein;
			this.bloodVesselLayers = bloodVessel;
			this.neuralLayers = neural;
			return this;
		}

		public Builder toggles(boolean baseSkybox, boolean clouds, boolean nebula, boolean membranePulse) {
			this.renderBaseSkybox = baseSkybox;
			this.renderCloudLayers = clouds;
			this.renderNebulaLayers = nebula;
			this.renderMembranePulse = membranePulse;
			return this;
		}

		public ChamberSkyTheme build() {
			return new ChamberSkyTheme(id, skyTexture, cloudTexture, wispTexture, noiseTexture, skyboxColor,
					cloudColor, nebulaPrimary, nebulaSecondary, nebulaAccent, capillaryTint, bloodTint, veinTint,
					neuralTint, pulseStrength, motionMultiplier, capillaryLayers, blueVeinLayers, bloodVesselLayers,
					neuralLayers, renderBaseSkybox, renderCloudLayers, renderNebulaLayers, renderMembranePulse);
		}
	}
}
