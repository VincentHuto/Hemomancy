package com.vincenthuto.hemomancy.client.render;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.shader.ExtendedShaderInstance;
import com.vincenthuto.hemomancy.common.init.ShaderInit;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.resources.ResourceLocation;

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

	public static final RenderType BLOODWELL_FOUNTAIN = RenderType.create("bloodwell_fountain",
			DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 1024, false, true,
			RenderType.CompositeState.builder()
					.setShaderState(RenderType.RENDERTYPE_LIGHTNING_SHADER)
					.setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
					.setDepthTestState(RenderType.LEQUAL_DEPTH_TEST)
					.setWriteMaskState(RenderType.COLOR_DEPTH_WRITE)
					.setCullState(RenderType.NO_CULL)
					.setLightmapState(RenderType.NO_LIGHTMAP)
					.createCompositeState(false));

	public static final RenderType MNEMONIC_LOWTIDE_WATERY_FOG = RenderType.create("mnemonic_lowtide_watery_fog",
			DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 4096, false, true,
			RenderType.CompositeState.builder()
					.setShaderState(RenderType.RENDERTYPE_LIGHTNING_SHADER)
					.setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
					.setDepthTestState(RenderType.NO_DEPTH_TEST)
					.setWriteMaskState(RenderType.COLOR_WRITE)
					.setCullState(RenderType.NO_CULL)
					.setLightmapState(RenderType.NO_LIGHTMAP)
					.createCompositeState(false));

	public static final RenderType MNEMONIC_LOWTIDE_CEILING_ROOTS = RenderType.create(
			"mnemonic_lowtide_ceiling_roots",
			DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 8192, false, true,
			RenderType.CompositeState.builder()
					.setShaderState(RenderType.RENDERTYPE_LIGHTNING_SHADER)
					.setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
					.setDepthTestState(RenderType.NO_DEPTH_TEST)
					.setWriteMaskState(RenderType.COLOR_WRITE)
					.setCullState(RenderType.NO_CULL)
					.setLightmapState(RenderType.NO_LIGHTMAP)
					.createCompositeState(false));

	public static final ResourceLocation MNEMONIC_LOWTIDE_PARCHMENT_TEXTURE = Hemomancy.rloc(
			"textures/world/lowtide_parchment/parchment_atlas.png");

	public static RenderType mnemonicLowtideParchment(float gameTime, float parchmentSeed, float windRippleStrength,
			float windRippleScale, float windDirectionX, float windDirectionY) {
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"mnemonic_lowtide_parchment_uniforms",
				() -> {
					ShaderInstance shader = ShaderInit.MNEMONIC_LOWTIDE_PARCHMENT.getInstance().get();
					setUniform(shader, "HemoTime", gameTime);
					setUniform(shader, "ParchmentSeed", parchmentSeed);
					setUniform(shader, "WindRippleStrength", windRippleStrength);
					setUniform(shader, "WindRippleScale", windRippleScale);
					setUniform(shader, "WindDirection", windDirectionX, windDirectionY, 0.0F);
				},
				() -> {
					ShaderInstance shader = ShaderInit.MNEMONIC_LOWTIDE_PARCHMENT.getInstance().get();
					if (shader instanceof ExtendedShaderInstance extended) {
						extended.setUniformDefaults();
					}
				});
		return RenderType.create("mnemonic_lowtide_parchment",
				DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS, 8192, false, true,
				RenderType.CompositeState.builder()
						.setShaderState(ShaderInit.MNEMONIC_LOWTIDE_PARCHMENT.getShard())
						.setTextureState(new RenderStateShard.TextureStateShard(MNEMONIC_LOWTIDE_PARCHMENT_TEXTURE,
								false, false))
						.setTexturingState(uniforms)
						.setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
						.setDepthTestState(RenderType.NO_DEPTH_TEST)
						.setWriteMaskState(RenderType.COLOR_WRITE)
						.setCullState(RenderType.NO_CULL)
						.setLightmapState(RenderType.NO_LIGHTMAP)
						.createCompositeState(false));
	}

	public static RenderType loomOrbShell(float gameTime, float orbSeed, float centerX, float centerY, float centerZ,
			float orbRadius, float writheStrength, float threadScale, boolean glowLayer) {
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"loom_orb_uniforms",
				() -> {
					ShaderInstance shader = ShaderInit.LOOM_ORB.getInstance().get();
					setUniform(shader, "HemoTime", gameTime);
					setUniform(shader, "OrbSeed", orbSeed);
					setUniform(shader, "OrbCenter", centerX, centerY, centerZ);
					setUniform(shader, "OrbRadius", orbRadius);
					setUniform(shader, "WritheStrength", writheStrength);
					setUniform(shader, "ThreadScale", threadScale);
					setUniform(shader, "GlowLayer", glowLayer ? 1.0f : 0.0f);
				},
				() -> {
					ShaderInstance shader = ShaderInit.LOOM_ORB.getInstance().get();
					if (shader instanceof ExtendedShaderInstance extended) {
						extended.setUniformDefaults();
					}
				});
		return RenderType.create(glowLayer ? "loom_orb_shell_glow" : "loom_orb_shell_core",
				DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 2048, false, true,
				RenderType.CompositeState.builder()
						.setShaderState(ShaderInit.LOOM_ORB.getShard())
						.setTexturingState(uniforms)
						.setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
						.setDepthTestState(RenderType.LEQUAL_DEPTH_TEST)
						.setWriteMaskState(glowLayer ? RenderType.COLOR_WRITE : RenderType.COLOR_DEPTH_WRITE)
						.setCullState(RenderType.NO_CULL)
						.setLightmapState(RenderType.NO_LIGHTMAP)
						.createCompositeState(false));
	}

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
					setUniform(shader, "FractalScale", burden > 0.5f ? 13.0f : 8.0f);
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

	public static RenderType silentArchonArmorOverlay(float gameTime, float shardSeed, float burden, float attuned,
			float guiClamp) {
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"silent_archon_armor_overlay_uniforms",
				() -> {
					ShaderInstance shader = ShaderInit.MONOLITH_FRAGMENT_ENTITY.getInstance().get();
					setUniform(shader, "HemoTime", gameTime);
					setUniform(shader, "ShardSeed", shardSeed);
					setUniform(shader, "Burden", burden);
					setUniform(shader, "Attuned", attuned);
					setUniform(shader, "FractalScale", guiClamp > 0.5f ? 9.0f : 11.0f);
				},
				() -> {
					ShaderInstance shader = ShaderInit.MONOLITH_FRAGMENT_ENTITY.getInstance().get();
					if (shader instanceof ExtendedShaderInstance extended) {
						extended.setUniformDefaults();
					}
				});
		return RenderType.create("silent_archon_armor_overlay",
				DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 1536, true, true,
				RenderType.CompositeState.builder()
						.setShaderState(ShaderInit.MONOLITH_FRAGMENT_ENTITY.getShard())
						.setTexturingState(uniforms)
						.setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
						.setDepthTestState(RenderType.LEQUAL_DEPTH_TEST)
						.setWriteMaskState(RenderType.COLOR_WRITE)
						.setCullState(RenderType.NO_CULL)
						.setLightmapState(RenderType.NO_LIGHTMAP)
						.setOverlayState(RenderType.OVERLAY)
						.createCompositeState(false));
	}

	public static RenderType monolithicDislocationShell(float gameTime, float shardSeed) {
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"monolithic_dislocation_shell_uniforms",
				() -> {
					ShaderInstance shader = ShaderInit.MONOLITH_FRAGMENT_ENTITY.getInstance().get();
					setUniform(shader, "HemoTime", gameTime);
					setUniform(shader, "ShardSeed", shardSeed);
					setUniform(shader, "Burden", 0.32f);
					setUniform(shader, "Attuned", 1.0f);
					setUniform(shader, "FractalScale", 12.5f);
				},
				() -> {
					ShaderInstance shader = ShaderInit.MONOLITH_FRAGMENT_ENTITY.getInstance().get();
					if (shader instanceof ExtendedShaderInstance extended) {
						extended.setUniformDefaults();
					}
				});
		return RenderType.create("monolithic_dislocation_shell",
				DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 1536, true, true,
				RenderType.CompositeState.builder()
						.setShaderState(ShaderInit.MONOLITH_FRAGMENT_ENTITY.getShard())
						.setTexturingState(uniforms)
						.setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
						.setDepthTestState(RenderType.LEQUAL_DEPTH_TEST)
						.setWriteMaskState(RenderType.COLOR_WRITE)
						.setCullState(RenderType.NO_CULL)
						.setLightmapState(RenderType.NO_LIGHTMAP)
						.setOverlayState(RenderType.OVERLAY)
						.createCompositeState(false));
	}

	public static RenderType monolithEntitySurface(float gameTime, float shardSeed, float burden, float attuned,
			float fractalScale) {
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"monolith_entity_surface_uniforms",
				() -> {
					ShaderInstance shader = ShaderInit.MONOLITH_FRAGMENT_ENTITY.getInstance().get();
					setUniform(shader, "HemoTime", gameTime);
					setUniform(shader, "ShardSeed", shardSeed);
					setUniform(shader, "Burden", burden);
					setUniform(shader, "Attuned", attuned);
					setUniform(shader, "FractalScale", fractalScale);
				},
				() -> {
					ShaderInstance shader = ShaderInit.MONOLITH_FRAGMENT_ENTITY.getInstance().get();
					if (shader instanceof ExtendedShaderInstance extended) {
						extended.setUniformDefaults();
					}
				});
		return RenderType.create("monolith_entity_surface",
				DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 1536, true, true,
				RenderType.CompositeState.builder()
						.setShaderState(ShaderInit.MONOLITH_FRAGMENT_ENTITY.getShard())
						.setTexturingState(uniforms)
						.setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
						.setDepthTestState(RenderType.LEQUAL_DEPTH_TEST)
						.setWriteMaskState(RenderType.COLOR_DEPTH_WRITE)
						.setCullState(RenderType.NO_CULL)
						.setLightmapState(RenderType.NO_LIGHTMAP)
						.setOverlayState(RenderType.OVERLAY)
						.createCompositeState(false));
	}

	public static RenderType hermitFarewellDissolve(ResourceLocation texture, float gameTime, float progress,
			float seed) {
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"hermit_farewell_dissolve_uniforms",
				() -> {
					ShaderInstance shader = ShaderInit.HERMIT_FAREWELL_DISSOLVE.getInstance().get();
					setUniform(shader, "HemoTime", gameTime);
					setUniform(shader, "HermitDissolveProgress", progress);
					setUniform(shader, "HermitDissolveSeed", seed);
				},
				() -> {
					ShaderInstance shader = ShaderInit.HERMIT_FAREWELL_DISSOLVE.getInstance().get();
					if (shader instanceof ExtendedShaderInstance extended) {
						extended.setUniformDefaults();
					}
				});
		return RenderType.create("hermit_farewell_dissolve",
				DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 1536, true, true,
				RenderType.CompositeState.builder()
						.setShaderState(ShaderInit.HERMIT_FAREWELL_DISSOLVE.getShard())
						.setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
						.setTexturingState(uniforms)
						.setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
						.setDepthTestState(RenderType.LEQUAL_DEPTH_TEST)
						.setWriteMaskState(RenderType.COLOR_DEPTH_WRITE)
						.setCullState(RenderType.NO_CULL)
						.setLightmapState(RenderType.LIGHTMAP)
						.setOverlayState(RenderType.OVERLAY)
						.createCompositeState(false));
	}

	public static RenderType bloodStructureWarp(float gameTime, float progress, float blockSeed, float wiggleAmp,
			float centerX, float centerY, float centerZ, float finalizeProgress, float meltGroundY, float meltHeight) {
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"blood_structure_warp_uniforms",
				() -> {
					ShaderInstance shader = ShaderInit.BLOOD_STRUCTURE_WARP.getInstance().get();
					setUniform(shader, "HemoTime", gameTime);
					setUniform(shader, "Progress", progress);
					setUniform(shader, "BlockSeed", blockSeed);
					setUniform(shader, "WiggleAmp", wiggleAmp);
					setUniform(shader, "WarpCenter", centerX, centerY, centerZ);
					setUniform(shader, "FinalizeProgress", finalizeProgress);
					setUniform(shader, "MeltGroundY", meltGroundY);
					setUniform(shader, "MeltHeight", meltHeight);
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

	public static RenderType mycelialCrucibleBasin(float gameTime, float basinSeed, float swirlIntensity) {
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"mycelial_crucible_basin_uniforms",
				() -> {
					ShaderInstance shader = ShaderInit.MYCELIAL_CRUCIBLE_BASIN.getInstance().get();
					setUniform(shader, "HemoTime", gameTime);
					setUniform(shader, "BasinSeed", basinSeed);
					setUniform(shader, "SwirlIntensity", swirlIntensity);
				},
				() -> {
					ShaderInstance shader = ShaderInit.MYCELIAL_CRUCIBLE_BASIN.getInstance().get();
					if (shader instanceof ExtendedShaderInstance extended) {
						extended.setUniformDefaults();
					}
				});
		return RenderType.create("mycelial_crucible_basin",
				DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS, 4096, false, true,
				RenderType.CompositeState.builder()
						.setShaderState(ShaderInit.MYCELIAL_CRUCIBLE_BASIN.getShard())
						.setTexturingState(uniforms)
						.setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
						.setDepthTestState(RenderType.LEQUAL_DEPTH_TEST)
						.setWriteMaskState(RenderType.COLOR_WRITE)
						.setCullState(RenderType.NO_CULL)
						.setLightmapState(RenderType.NO_LIGHTMAP)
						.createCompositeState(false));
	}

	public static RenderType mnemonicLowtideLake(float gameTime, float lakeSeed, float waveStrength,
			float waveDetailScale, float noiseScale, float glossStrength, float edgeFade) {
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"mnemonic_lowtide_lake_uniforms",
				() -> {
					ShaderInstance shader = ShaderInit.MNEMONIC_LOWTIDE_LAKE.getInstance().get();
					setUniform(shader, "HemoTime", gameTime);
					setUniform(shader, "LakeSeed", lakeSeed);
					setUniform(shader, "WaveStrength", waveStrength);
					setUniform(shader, "WaveDetailScale", waveDetailScale);
					setUniform(shader, "NoiseScale", noiseScale);
					setUniform(shader, "GlossStrength", glossStrength);
					setUniform(shader, "EdgeFade", edgeFade);
				},
				() -> {
					ShaderInstance shader = ShaderInit.MNEMONIC_LOWTIDE_LAKE.getInstance().get();
					if (shader instanceof ExtendedShaderInstance extended) {
						extended.setUniformDefaults();
					}
				});
		return RenderType.create("mnemonic_lowtide_lake",
				DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS, 16384, false, true,
				RenderType.CompositeState.builder()
						.setShaderState(ShaderInit.MNEMONIC_LOWTIDE_LAKE.getShard())
						.setTexturingState(uniforms)
						.setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
						.setDepthTestState(RenderType.NO_DEPTH_TEST)
						.setWriteMaskState(RenderType.COLOR_WRITE)
						.setCullState(RenderType.NO_CULL)
						.setLightmapState(RenderType.NO_LIGHTMAP)
						.createCompositeState(false));
	}

	public static RenderType mnemonicLowtideSkybox(float gameTime, float faceSeed, float coverageBias,
			float tunnelScale, float bubbleScale, float tendrilIntensity) {
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"mnemonic_lowtide_skybox_uniforms",
				() -> {
					ShaderInstance shader = ShaderInit.MNEMONIC_LOWTIDE_SKYBOX.getInstance().get();
					setUniform(shader, "HemoTime", gameTime);
					setUniform(shader, "FaceSeed", faceSeed);
					setUniform(shader, "CoverageBias", coverageBias);
					setUniform(shader, "TunnelScale", tunnelScale);
					setUniform(shader, "BubbleScale", bubbleScale);
					setUniform(shader, "TendrilIntensity", tendrilIntensity);
				},
				() -> {
					ShaderInstance shader = ShaderInit.MNEMONIC_LOWTIDE_SKYBOX.getInstance().get();
					if (shader instanceof ExtendedShaderInstance extended) {
						extended.setUniformDefaults();
					}
				});
		return RenderType.create("mnemonic_lowtide_skybox",
				DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS, 1536, false, true,
				RenderType.CompositeState.builder()
						.setShaderState(ShaderInit.MNEMONIC_LOWTIDE_SKYBOX.getShard())
						.setTexturingState(uniforms)
						.setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
						.setDepthTestState(RenderType.NO_DEPTH_TEST)
						.setWriteMaskState(RenderType.COLOR_WRITE)
						.setCullState(RenderType.NO_CULL)
						.setLightmapState(RenderType.NO_LIGHTMAP)
						.createCompositeState(false));
	}

	public static RenderType mnemonicLowtideSkyboxBase(float gameTime, float faceSeed, float coverageBias,
			float noduleScale, float veinIntensity, float baseIntensity) {
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"mnemonic_lowtide_skybox_base_uniforms",
				() -> {
					ShaderInstance shader = ShaderInit.MNEMONIC_LOWTIDE_SKYBOX_BASE.getInstance().get();
					setUniform(shader, "HemoTime", gameTime);
					setUniform(shader, "FaceSeed", faceSeed);
					setUniform(shader, "CoverageBias", coverageBias);
					setUniform(shader, "NoduleScale", noduleScale);
					setUniform(shader, "VeinIntensity", veinIntensity);
					setUniform(shader, "BaseIntensity", baseIntensity);
				},
				() -> {
					ShaderInstance shader = ShaderInit.MNEMONIC_LOWTIDE_SKYBOX_BASE.getInstance().get();
					if (shader instanceof ExtendedShaderInstance extended) {
						extended.setUniformDefaults();
					}
				});
		return RenderType.create("mnemonic_lowtide_skybox_base",
				DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS, 1536, false, true,
				RenderType.CompositeState.builder()
						.setShaderState(ShaderInit.MNEMONIC_LOWTIDE_SKYBOX_BASE.getShard())
						.setTexturingState(uniforms)
						.setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
						.setDepthTestState(RenderType.NO_DEPTH_TEST)
						.setWriteMaskState(RenderType.COLOR_WRITE)
						.setCullState(RenderType.NO_CULL)
						.setLightmapState(RenderType.NO_LIGHTMAP)
						.createCompositeState(false));
	}

	public static RenderType qliphothBlackHole(ResourceLocation texture, float gameTime, float holeSeed,
			float lensStrength, float ringIntensity, boolean finalHole) {
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"qliphoth_black_hole_uniforms",
				() -> {
					ShaderInstance shader = ShaderInit.QLIPHOTH_BLACK_HOLE.getInstance().get();
					setUniform(shader, "HemoTime", gameTime);
					setUniform(shader, "HoleSeed", holeSeed);
					setUniform(shader, "LensStrength", lensStrength);
					setUniform(shader, "RingIntensity", ringIntensity);
					setUniform(shader, "FinalHole", finalHole ? 1.0f : 0.0f);
				},
				() -> {
					ShaderInstance shader = ShaderInit.QLIPHOTH_BLACK_HOLE.getInstance().get();
					if (shader instanceof ExtendedShaderInstance extended) {
						extended.setUniformDefaults();
					}
				});
		return RenderType.create(finalHole ? "qliphoth_black_hole_zenith" : "qliphoth_black_hole",
				DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS, 2048, false, true,
				RenderType.CompositeState.builder()
						.setShaderState(ShaderInit.QLIPHOTH_BLACK_HOLE.getShard())
						.setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
						.setTexturingState(uniforms)
						.setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
						.setDepthTestState(RenderType.NO_DEPTH_TEST)
						.setWriteMaskState(RenderType.COLOR_WRITE)
						.setCullState(RenderType.NO_CULL)
						.setLightmapState(RenderType.NO_LIGHTMAP)
						.createCompositeState(false));
	}

	public static RenderType silentArchonStormCloud(float gameTime, float cloudSeed, float cloudDensity) {
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"silent_archon_storm_cloud_uniforms",
				() -> {
					ShaderInstance shader = ShaderInit.SILENT_ARCHON_STORM_CLOUD.getInstance().get();
					setUniform(shader, "HemoTime", gameTime);
					setUniform(shader, "CloudSeed", cloudSeed);
					setUniform(shader, "CloudDensity", cloudDensity);
				},
				() -> {
					ShaderInstance shader = ShaderInit.SILENT_ARCHON_STORM_CLOUD.getInstance().get();
					if (shader instanceof ExtendedShaderInstance extended) {
						extended.setUniformDefaults();
					}
				});
		return RenderType.create("silent_archon_storm_cloud",
				DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS, 4096, false, true,
				RenderType.CompositeState.builder()
						.setShaderState(ShaderInit.SILENT_ARCHON_STORM_CLOUD.getShard())
						.setTexturingState(uniforms)
						.setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
						.setDepthTestState(RenderType.NO_DEPTH_TEST)
						.setWriteMaskState(RenderType.COLOR_WRITE)
						.setCullState(RenderType.NO_CULL)
						.setLightmapState(RenderType.NO_LIGHTMAP)
						.createCompositeState(false));
	}

	public static RenderType silentArchonVolumetricFog(ResourceLocation texture, float gameTime, float fogSeed,
			float fogLayer, float fogDensity) {
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"silent_archon_fog_uniforms",
				() -> {
					ShaderInstance shader = ShaderInit.SILENT_ARCHON_FOG.getInstance().get();
					setUniform(shader, "HemoTime", gameTime);
					setUniform(shader, "FogSeed", fogSeed);
					setUniform(shader, "FogLayer", fogLayer);
					setUniform(shader, "FogDensity", fogDensity);
				},
				() -> {
					ShaderInstance shader = ShaderInit.SILENT_ARCHON_FOG.getInstance().get();
					if (shader instanceof ExtendedShaderInstance extended) {
						extended.setUniformDefaults();
					}
				});
		return RenderType.create("silent_archon_volumetric_fog",
				DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS, 4096, false, true,
				RenderType.CompositeState.builder()
						.setShaderState(ShaderInit.SILENT_ARCHON_FOG.getShard())
						.setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
						.setTexturingState(uniforms)
						.setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
						.setDepthTestState(RenderType.NO_DEPTH_TEST)
						.setWriteMaskState(RenderType.COLOR_WRITE)
						.setCullState(RenderType.NO_CULL)
						.setLightmapState(RenderType.NO_LIGHTMAP)
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

