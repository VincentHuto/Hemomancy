package com.vincenthuto.hemomancy.client.render;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.vincenthuto.hemomancy.client.render.shader.ExtendedShaderInstance;
import com.vincenthuto.hemomancy.common.init.ShaderInit;

import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;

/**
 * Client-local render types used by animated item renderers.
 */
public final class HemoRenderTypes {

	private HemoRenderTypes() {
	}

	public static final RenderType QLIPHOTH_CORE = RenderType.create("qliphoth_core",
			DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 256, false, true,
			RenderType.CompositeState.builder()
					.setShaderState(RenderType.RENDERTYPE_LIGHTNING_SHADER)
					.setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
					.setDepthTestState(RenderType.LEQUAL_DEPTH_TEST)
					.setWriteMaskState(RenderType.COLOR_DEPTH_WRITE)
					.setCullState(RenderType.NO_CULL)
					.setLightmapState(RenderType.NO_LIGHTMAP)
					.createCompositeState(false));

	public static final RenderType QLIPHOTH_GLOW = RenderType.create("qliphoth_glow",
			DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 256, false, true,
			RenderType.CompositeState.builder()
					.setShaderState(RenderType.RENDERTYPE_LIGHTNING_SHADER)
					.setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
					.setDepthTestState(RenderType.LEQUAL_DEPTH_TEST)
					.setWriteMaskState(RenderType.COLOR_WRITE)
					.setCullState(RenderType.NO_CULL)
					.setLightmapState(RenderType.NO_LIGHTMAP)
					.createCompositeState(false));

	public static RenderType monolithFragment(float gameTime, float shardSeed, float burden, float attuned,
			float guiClamp) {
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"monolith_fragment_uniforms",
				() -> {
					ShaderInstance shader = ShaderInit.MONOLITH_FRAGMENT.getInstance().get();
					setUniform(shader, "HemoTime", gameTime);
					setUniform(shader, "ShardSeed", shardSeed);
					setUniform(shader, "Burden", burden);
					setUniform(shader, "Attuned", attuned);
					setUniform(shader, "GuiClamp", guiClamp);
					setUniform(shader, "FractalScale", burden > 0.5f ? 13.0f : 8.0f);
					setUniform(shader, "Snap", burden > 0.5f ? 1.35f : 0.85f);
				},
				() -> {
					ShaderInstance shader = ShaderInit.MONOLITH_FRAGMENT.getInstance().get();
					if (shader instanceof ExtendedShaderInstance extended) {
						extended.setUniformDefaults();
					}
				});
		return RenderType.create("monolith_fragment_fractal",
				DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.TRIANGLES, 512, false, true,
				RenderType.CompositeState.builder()
						.setShaderState(ShaderInit.MONOLITH_FRAGMENT.getShard())
						.setTexturingState(uniforms)
						.setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
						.setDepthTestState(RenderType.LEQUAL_DEPTH_TEST)
						.setWriteMaskState(RenderType.COLOR_DEPTH_WRITE)
						.setCullState(RenderType.NO_CULL)
						.setLightmapState(RenderType.NO_LIGHTMAP)
						.createCompositeState(false));
	}

	public static RenderType bloodStructureWarp(float gameTime, float progress, float blockSeed, float wiggleAmp,
			float centerX, float centerY, float centerZ) {
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"blood_structure_warp_uniforms",
				() -> {
					ShaderInstance shader = ShaderInit.BLOOD_STRUCTURE_WARP.getInstance().get();
					setUniform(shader, "HemoTime", gameTime);
					setUniform(shader, "Progress", progress);
					setUniform(shader, "BlockSeed", blockSeed);
					setUniform(shader, "WiggleAmp", wiggleAmp);
					setUniform(shader, "WarpCenter", centerX, centerY, centerZ);
				},
				() -> {
					ShaderInstance shader = ShaderInit.BLOOD_STRUCTURE_WARP.getInstance().get();
					if (shader instanceof ExtendedShaderInstance extended) {
						extended.setUniformDefaults();
					}
				});
		return RenderType.create("blood_structure_warp",
				DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 2048, false, true,
				RenderType.CompositeState.builder()
						.setShaderState(ShaderInit.BLOOD_STRUCTURE_WARP.getShard())
						.setTexturingState(uniforms)
						.setLayeringState(RenderType.VIEW_OFFSET_Z_LAYERING)
						.setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
						.setDepthTestState(RenderType.LEQUAL_DEPTH_TEST)
						.setWriteMaskState(RenderType.COLOR_WRITE)
						.setCullState(RenderType.NO_CULL)
						.setLightmapState(RenderType.LIGHTMAP)
						.createCompositeState(false));
	}

	private static void setUniform(ShaderInstance shader, String name, float value) {
		if (shader == null) {
			return;
		}
		Uniform uniform = shader.getUniform(name);
		if (uniform != null) {
			uniform.set(value);
		}
	}

	private static void setUniform(ShaderInstance shader, String name, float x, float y, float z) {
		if (shader == null) {
			return;
		}
		Uniform uniform = shader.getUniform(name);
		if (uniform != null) {
			uniform.set(x, y, z);
		}
	}
}

