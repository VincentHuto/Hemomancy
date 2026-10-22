package com.vincenthuto.hemomancy.client.render.world.floor;

import com.mojang.blaze3d.shaders.Uniform;
import com.vincenthuto.hemomancy.client.render.shader.ShaderInit;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;

/**
 * The floor membrane's render types with the rite's response loaded into the shader. Each wraps the plain membrane
 * type from {@code HemoRenderTypes}: it sets that type's state up as usual, then the rite uniforms for this Focus,
 * so every Focus's draw carries its own rite. Clearing the base state resets them to their quiet defaults.
 */
public final class CardinalFloorRenderTypes {
	private CardinalFloorRenderTypes() {
	}

	public static RenderType withRite(RenderType base, FloorRiteResponse rite) {
		// Copied now: the draw happens later in the frame, and the response is reused next frame.
		float[] rings = rite.rings.clone();
		float[] surge = rite.surge.clone();
		float[] draw = rite.draw.clone();
		float[] mood = rite.mood.clone();
		float[] swelling = rite.swelling.clone();
		float[] wounds = rite.wounds.clone();
		float[] woundStrengths = rite.woundStrengths.clone();
		float[] blood = rite.blood.clone();
		return new RenderType("hemomancy_cardinal_floor_rite", base.format(), base.mode(), base.bufferSize(),
				base.affectsCrumbling(), base.sortOnUpload(),
				() -> {
					base.setupRenderState();
					ShaderInstance shader = ShaderInit.CARDINAL_FLOOR_MEMBRANE.getInstance().get();
					if (shader == null) return;
					set(shader, "RiteRings", rings);
					set(shader, "RiteSurge", surge);
					set(shader, "RiteDraw", draw);
					set(shader, "RiteMood", mood);
					set(shader, "RiteSwell", swelling);
					set(shader, "WoundAngles", wounds);
					set(shader, "WoundLevels", woundStrengths);
					set(shader, "RiteBlood", blood);
				},
				base::clearRenderState) {
		};
	}

	private static void set(ShaderInstance shader, String name, float[] values) {
		Uniform uniform = shader.getUniform(name);
		if (uniform != null) uniform.set(values[0], values[1], values[2], values[3]);
	}
}
