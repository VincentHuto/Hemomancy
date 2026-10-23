package com.vincenthuto.hemomancy.client.render.world.floor;

import com.mojang.blaze3d.shaders.Uniform;
import com.vincenthuto.hemomancy.client.render.shader.ShaderInit;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import com.vincenthuto.hemomancy.client.render.SeededRenderTypes;
import java.util.Arrays;
import java.util.function.Supplier;

/**
 * The floor membrane's render types with the rite's response loaded into the shader. Each wraps the plain membrane
 * type from {@code HemoRenderTypes}: it sets that type's state up as usual, then the rite uniforms for this Focus,
 * so every Focus's draw carries its own rite. Clearing the base state resets them to their quiet defaults. One
 * wrapper is held per membrane type (see {@link SeededRenderTypes}).
 */
public final class CardinalFloorRenderTypes {
	private CardinalFloorRenderTypes() {
	}

	/** One Focus's rite response, copied: the draw happens later in the frame, and the response is reused next frame. */
	private record RiteValues(float[] rings, float[] surge, float[] draw, float[] mood, float[] swelling,
			float[] wounds, float[] woundStrengths, float[] blood) {
		@Override
		public boolean equals(Object other) {
			return other instanceof RiteValues o && Arrays.equals(rings, o.rings) && Arrays.equals(surge, o.surge)
					&& Arrays.equals(draw, o.draw) && Arrays.equals(mood, o.mood)
					&& Arrays.equals(swelling, o.swelling) && Arrays.equals(wounds, o.wounds)
					&& Arrays.equals(woundStrengths, o.woundStrengths) && Arrays.equals(blood, o.blood);
		}

		@Override
		public int hashCode() {
			return Arrays.hashCode(rings) * 31 + Arrays.hashCode(blood);
		}
	}

	private static final SeededRenderTypes<RenderType, RiteValues, RenderType> TYPES =
			new SeededRenderTypes<>(32, CardinalFloorRenderTypes::create);

	public static RenderType withRite(RenderType base, FloorRiteResponse rite) {
		return TYPES.get(base, new RiteValues(rite.rings.clone(), rite.surge.clone(), rite.draw.clone(),
				rite.mood.clone(), rite.swelling.clone(), rite.wounds.clone(), rite.woundStrengths.clone(),
				rite.blood.clone()));
	}

	private static RenderType create(RenderType base, Supplier<RiteValues> values) {
		return new RenderType("hemomancy_cardinal_floor_rite", base.format(), base.mode(), base.bufferSize(),
				base.affectsCrumbling(), base.sortOnUpload(),
				() -> {
					base.setupRenderState();
					ShaderInstance shader = ShaderInit.CARDINAL_FLOOR_MEMBRANE.getInstance().get();
					if (shader == null) return;
					RiteValues rite = values.get();
					float[] rings = rite.rings();
					float[] surge = rite.surge();
					float[] draw = rite.draw();
					float[] mood = rite.mood();
					float[] swelling = rite.swelling();
					float[] wounds = rite.wounds();
					float[] woundStrengths = rite.woundStrengths();
					float[] blood = rite.blood();
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
