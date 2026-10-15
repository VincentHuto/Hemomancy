package com.vincenthuto.hemomancy.client.render;

import com.vincenthuto.hemomancy.client.render.world.rite.DaemonHaze;
import com.vincenthuto.hemomancy.client.render.world.rite.DaemonHazeQueue;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.shader.ExtendedShaderInstance;
import com.vincenthuto.hemomancy.common.init.ShaderInit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Client-local render types used by animated item renderers.
 */
public final class HemoRenderTypes {

	private HemoRenderTypes() {
	}

    public static final RenderType MANIPULATION_LOCATOR = RenderType.create("manipulation_locator",
            DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 1024, false, true,
            RenderType.CompositeState.builder().setShaderState(RenderType.RENDERTYPE_LIGHTNING_SHADER)
                    .setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(RenderType.NO_DEPTH_TEST).setWriteMaskState(RenderType.COLOR_WRITE)
                    .setCullState(RenderType.NO_CULL).setLightmapState(RenderType.NO_LIGHTMAP).createCompositeState(false));

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

	public static final RenderType APOTHEOS_WALL_FRAME = RenderType.create("apotheos_wall_frame",
			DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 8192, false, true,
			RenderType.CompositeState.builder()
					.setShaderState(RenderType.RENDERTYPE_LIGHTNING_SHADER)
					.setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
					.setDepthTestState(RenderType.NO_DEPTH_TEST)
					.setWriteMaskState(RenderType.COLOR_WRITE)
					.setCullState(RenderType.NO_CULL)
					.setLightmapState(RenderType.NO_LIGHTMAP)
					.createCompositeState(false));

	public static final RenderType APOTHEOS_CEILING_PRIMITIVES = RenderType.create("apotheos_ceiling_primitives",
			DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 24576, false, true,
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

	public static RenderType faneBoundaryShell(float gameTime, float orbSeed,
			float centerX, float centerY, float centerZ, float orbRadius,
			float writheStrength, float threadScale, boolean glowLayer) {
		return loomOrbShell(gameTime, orbSeed, centerX, centerY, centerZ,
				orbRadius, writheStrength, threadScale, glowLayer, false);
	}

	private static RenderType loomOrbShell(float gameTime, float orbSeed,
			float centerX, float centerY, float centerZ, float orbRadius,
			float writheStrength, float threadScale, boolean glowLayer, boolean writeDepth) {
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
		RenderStateShard.WriteMaskStateShard writeMask = writeDepth
				? (glowLayer ? RenderType.COLOR_WRITE : RenderType.COLOR_DEPTH_WRITE)
				: RenderType.COLOR_WRITE;
		return RenderType.create(glowLayer ? "loom_orb_shell_glow" : "loom_orb_shell_core",
				DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 2048, false, true,
				RenderType.CompositeState.builder()
						.setShaderState(ShaderInit.LOOM_ORB.getShard())
						.setTexturingState(uniforms)
						.setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
						.setDepthTestState(RenderType.LEQUAL_DEPTH_TEST)
						.setWriteMaskState(writeMask)
						.setCullState(RenderType.NO_CULL)
						.setLightmapState(RenderType.NO_LIGHTMAP)
						.createCompositeState(false));
	}

	public static RenderType monolithFragment(float gameTime, float shardSeed, float burden, float attuned,
			float guiClamp) {
		return monolithFragment(gameTime, shardSeed, burden, attuned, guiClamp, 1.0F);
	}

	public static RenderType monolithFragmentRigid(float gameTime, float shardSeed, float burden, float attuned,
			float guiClamp) {
		return monolithFragment(gameTime, shardSeed, burden, attuned, guiClamp, 0.0F);
	}

	private static RenderType monolithFragment(float gameTime, float shardSeed, float burden, float attuned,
			float guiClamp, float vertexWarp) {
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"monolith_fragment_uniforms",
				() -> {
					ShaderInstance shader = ShaderInit.MONOLITH_FRAGMENT.getInstance().get();
					setUniform(shader, "HemoTime", gameTime);
					setUniform(shader, "ShardSeed", shardSeed);
					setUniform(shader, "Burden", burden);
					setUniform(shader, "Attuned", attuned);
					setUniform(shader, "FractalScale", burden > 0.5f ? 13.0f : 8.0f);
					setUniform(shader, "VertexWarp", vertexWarp);
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

	public static RenderType willStateMonolithShell(float gameTime, float shardSeed, float red, float green,
			float blue, float dissolveProgress, float flickerAlpha) {
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"will_state_monolith_shell_uniforms",
				() -> {
					ShaderInstance shader = ShaderInit.WILL_STATE_MONOLITH.getInstance().get();
					setUniform(shader, "HemoTime", gameTime);
					setUniform(shader, "ShardSeed", shardSeed);
					setUniform(shader, "Burden", 0.32f);
					setUniform(shader, "Attuned", 1.0f);
					setUniform(shader, "FractalScale", 12.5f);
					setUniform(shader, "WillMonolithColor", red, green, blue);
					setUniform(shader, "WillDissolveProgress", dissolveProgress);
					setUniform(shader, "WillDissolveFlicker", flickerAlpha);
				},
				() -> {
					ShaderInstance shader = ShaderInit.WILL_STATE_MONOLITH.getInstance().get();
					if (shader instanceof ExtendedShaderInstance extended) {
						extended.setUniformDefaults();
					}
				});
		return RenderType.create("will_state_monolith_shell",
				DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 1536, true, true,
				RenderType.CompositeState.builder()
						.setShaderState(ShaderInit.WILL_STATE_MONOLITH.getShard())
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

	/** No fill line: the whole structure shell shows. */
	public static final float NO_FILL_LINE = 100000.0F;

	public static RenderType bloodStructureWarp(float gameTime, float progress, float blockSeed, float wiggleAmp,
			float centerX, float centerY, float centerZ, float finalizeProgress, float meltGroundY, float meltHeight) {
		return bloodStructureWarp(gameTime, progress, blockSeed, wiggleAmp, centerX, centerY, centerZ,
				finalizeProgress, meltGroundY, meltHeight, NO_FILL_LINE);
	}

	/**
	 * The structure warp shell cut at {@code fillLine} (camera-relative height), with a bright wet meniscus
	 * there. A Working floor uses it to climb the structure it is about to swallow.
	 */
	public static RenderType bloodStructureWarp(float gameTime, float progress, float blockSeed, float wiggleAmp,
			float centerX, float centerY, float centerZ, float finalizeProgress, float meltGroundY, float meltHeight,
			float fillLine) {
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
					setUniform(shader, "FillLine", fillLine);
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

	/**
	 * A pool of blood standing in a vessel (the Iron Brazier's bowl, the Consecrated Bloodwell's basin). Vertex red is a
	 * jet falling into it, green the heat simmering it, blue a fire over it, and alpha its opacity.
	 */
	public static RenderType bloodPool(float gameTime, float poolSeed) {
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"blood_pool_uniforms",
				() -> {
					ShaderInstance shader = ShaderInit.BLOOD_POOL.getInstance().get();
					setUniform(shader, "HemoTime", gameTime);
					setUniform(shader, "PoolSeed", poolSeed);
				},
				() -> {
					ShaderInstance shader = ShaderInit.BLOOD_POOL.getInstance().get();
					if (shader instanceof ExtendedShaderInstance extended) {
						extended.setUniformDefaults();
					}
				});
		return RenderType.create("blood_pool",
				DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS, 256, false, true,
				RenderType.CompositeState.builder()
						.setShaderState(ShaderInit.BLOOD_POOL.getShard())
						.setTexturingState(uniforms)
						.setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
						.setDepthTestState(RenderType.LEQUAL_DEPTH_TEST)
						.setWriteMaskState(RenderType.COLOR_DEPTH_WRITE)
						.setCullState(RenderType.NO_CULL)
						.setLightmapState(RenderType.NO_LIGHTMAP)
						.createCompositeState(false));
	}

	/**
	 * The engram a Somatic Loom unfurls while it weaves (shaders/core/world/engram_sigil): layer 0 its skin and empty
	 * star, 1 its lanes of ichor, 2 its node caps. Cached by every uniform value, as the Cardinal floor's types are,
	 * so each loom's draw in a frame keeps its own engram.
	 */
	public static RenderType engramSigil(EngramSigilParams params) {
		RenderType cached = ENGRAM_SIGIL_TYPES.get(params);
		if (cached == null) {
			cached = createEngramSigil(params);
			ENGRAM_SIGIL_TYPES.put(params, cached);
		}
		return cached;
	}

	/** One engram draw's uniforms. Layer: 0 skin, 1 lanes, 2 caps. AimZ above one half means the staff points at it. */
	public record EngramSigilParams(int layer, float gameTime, float seed, float radius, float unfurl, float withdraw,
			float beat, float pressure, boolean keyed, int rotation, int turns, float dim, float light, float aimX,
			float aimY, float aimZ, float dissolve) {
	}

	private static final Map<EngramSigilParams, RenderType> ENGRAM_SIGIL_TYPES = riteTypeCache();

	private static RenderType createEngramSigil(EngramSigilParams p) {
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"engram_sigil_uniforms",
				() -> {
					ShaderInstance shader = ShaderInit.ENGRAM_SIGIL.getInstance().get();
					setUniform(shader, "HemoTime", p.gameTime());
					setUniform(shader, "SigilSeed", p.seed());
					setUniform(shader, "SigilLayer", p.layer());
					setUniform(shader, "SigilRadius", p.radius());
					setUniform(shader, "Unfurl", p.unfurl());
					setUniform(shader, "Withdraw", p.withdraw());
					setUniform(shader, "Beat", p.beat());
					setUniform(shader, "Pressure", p.pressure());
					setUniform(shader, "Keyed", p.keyed() ? 1.0F : 0.0F);
					setUniform(shader, "Rotation", p.rotation());
					setUniform(shader, "Turns", p.turns());
					setUniform(shader, "Dim", p.dim());
					setUniform(shader, "FloorLight", p.light());
					setUniform(shader, "AimPoint", p.aimX(), p.aimY(), p.aimZ());
					setUniform(shader, "Dissolve", p.dissolve());
				},
				() -> {
					ShaderInstance shader = ShaderInit.ENGRAM_SIGIL.getInstance().get();
					if (shader instanceof ExtendedShaderInstance extended) {
						extended.setUniformDefaults();
					}
				});
		// The skin and lanes lie on the ground without writing depth, so the loom, the weaver and the echo stand in them.
		return RenderType.create("engram_sigil_" + p.layer(),
				DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS, 8192, false, false,
				RenderType.CompositeState.builder()
						.setShaderState(ShaderInit.ENGRAM_SIGIL.getShard())
						.setTexturingState(uniforms)
						.setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
						.setDepthTestState(RenderType.LEQUAL_DEPTH_TEST)
						.setWriteMaskState(RenderType.COLOR_WRITE)
						.setCullState(RenderType.NO_CULL)
						.setLightmapState(RenderType.NO_LIGHTMAP)
						.createCompositeState(false));
	}

	/**
	 * @param focusX the Focus centre's x relative to the camera; with {@code focusZ}, it lets the shader rebuild
	 *               ground positions from camera-relative ones for the brazier trench
	 */
	public static RenderType cardinalFloorMembrane(float gameTime, float seed, int style, int floorHalf,
			float spread, float front, float beat, float floorLight, int baseColor, int veinColor, int glowColor,
			float focusX, float focusZ) {
		return cardinalFloor("cardinal_floor_membrane", RenderType.COLOR_WRITE, gameTime, seed, style, floorHalf,
				spread, front, beat, floorLight, baseColor, veinColor, glowColor, focusX, focusZ);
	}

	/** The raised mound around the Focus: the same membrane, but writing depth so what sinks into it is hidden. */
	public static RenderType cardinalFloorMound(float gameTime, float seed, int style, int floorHalf,
			float spread, float front, float beat, float floorLight, int baseColor, int veinColor, int glowColor,
			float focusX, float focusZ) {
		return cardinalFloor("cardinal_floor_mound", RenderType.COLOR_DEPTH_WRITE, gameTime, seed, style, floorHalf,
				spread, front, beat, floorLight, baseColor, veinColor, glowColor, focusX, focusZ);
	}

	/**
	 * Rite render types are cached by their exact uniform values: repeated requests within a frame (the floor asks
	 * for its membrane several times) share one type, so they batch together instead of each allocating a type and
	 * flushing the last. Different values still get distinct types, so every batch keeps its own uniforms.
	 */
	private static final int RITE_TYPE_CACHE_LIMIT = 32;
	private static final Map<CardinalFloorKey, RenderType> CARDINAL_FLOOR_TYPES = riteTypeCache();
	private static final Map<CardinalStaffMeltKey, RenderType> CARDINAL_STAFF_MELT_TYPES = riteTypeCache();
	/** One fog type per rite seed; its time uniform is updated in place because the fog ends its batch per rite. */
	private static final Map<Float, CardinalRiteFogType> CARDINAL_RITE_FOG_TYPES = riteTypeCache();

	private static <K, V> Map<K, V> riteTypeCache() {
		return new LinkedHashMap<>(16, 0.75F, true) {
			@Override
			protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
				return size() > RITE_TYPE_CACHE_LIMIT;
			}
		};
	}

	private record CardinalFloorKey(String name, float gameTime, float seed, int style, int floorHalf, float spread,
			float front, float beat, float floorLight, int baseColor, int veinColor, int glowColor, float focusX,
			float focusZ) {
	}

	private record CardinalStaffMeltKey(float gameTime, float blockSeed, float wiggleAmp, float centerX,
			float centerY, float centerZ, float finalizeProgress, float meltGroundY, float meltHeight) {
	}

	private static final class CardinalRiteFogType {
		private final float seed;
		private float gameTime;
		private final RenderType type;

		private CardinalRiteFogType(float seed) {
			this.seed = seed;
			this.type = createCardinalRiteFog(this);
		}
	}

	private static RenderType cardinalFloor(String name, RenderStateShard.WriteMaskStateShard writeMask,
			float gameTime, float seed, int style, int floorHalf, float spread, float front, float beat,
			float floorLight, int baseColor, int veinColor, int glowColor, float focusX, float focusZ) {
		CardinalFloorKey key = new CardinalFloorKey(name, gameTime, seed, style, floorHalf, spread, front, beat,
				floorLight, baseColor, veinColor, glowColor, focusX, focusZ);
		RenderType cached = CARDINAL_FLOOR_TYPES.get(key);
		if (cached == null) {
			cached = createCardinalFloor(name, writeMask, gameTime, seed, style, floorHalf, spread, front, beat,
					floorLight, baseColor, veinColor, glowColor, focusX, focusZ);
			CARDINAL_FLOOR_TYPES.put(key, cached);
		}
		return cached;
	}

	private static RenderType createCardinalFloor(String name, RenderStateShard.WriteMaskStateShard writeMask,
			float gameTime, float seed, int style, int floorHalf, float spread, float front, float beat,
			float floorLight, int baseColor, int veinColor, int glowColor, float focusX, float focusZ) {
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"cardinal_floor_membrane_uniforms",
				() -> {
					ShaderInstance shader = ShaderInit.CARDINAL_FLOOR_MEMBRANE.getInstance().get();
					setUniform(shader, "HemoTime", gameTime);
					setUniform(shader, "FloorSeed", seed);
					setUniform(shader, "FloorStyle", style);
					setUniform(shader, "FloorHalf", floorHalf);
					setUniform(shader, "Spread", spread);
					setUniform(shader, "Front", front);
					setUniform(shader, "Beat", beat);
					setUniform(shader, "FloorLight", floorLight);
					setUniform(shader, "FocusOffset", focusX, focusZ);
					setRgbUniform(shader, "BaseColor", baseColor);
					setRgbUniform(shader, "VeinColor", veinColor);
					setRgbUniform(shader, "GlowColor", glowColor);
				},
				() -> {
					ShaderInstance shader = ShaderInit.CARDINAL_FLOOR_MEMBRANE.getInstance().get();
					if (shader instanceof ExtendedShaderInstance extended) {
						extended.setUniformDefaults();
					}
				});
		// Unsorted: the floor quads never overlap, so submission order keeps the Focus collar on top.
		return RenderType.create(name,
				DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS, 8192, false, false,
				RenderType.CompositeState.builder()
						.setShaderState(ShaderInit.CARDINAL_FLOOR_MEMBRANE.getShard())
						.setTexturingState(uniforms)
						.setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
						.setDepthTestState(RenderType.LEQUAL_DEPTH_TEST)
						.setWriteMaskState(writeMask)
						.setCullState(RenderType.NO_CULL)
						.setLightmapState(RenderType.NO_LIGHTMAP)
						.createCompositeState(false));
	}

	private static void setRgbUniform(ShaderInstance shader, String name, int rgb) {
		setUniform(shader, name, ((rgb >> 16) & 0xFF) / 255.0F, ((rgb >> 8) & 0xFF) / 255.0F, (rgb & 0xFF) / 255.0F);
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

	public static RenderType apotheosFloorFunnel(float gameTime, float funnelSeed, float ringRise, float ringSpeed,
			float meatNoiseScale, float highlightIntensity, float centerVoidRadius) {
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"apotheos_floor_funnel_uniforms",
				() -> {
					ShaderInstance shader = ShaderInit.APOTHEOS_FLOOR_FUNNEL.getInstance().get();
					setUniform(shader, "HemoTime", gameTime);
					setUniform(shader, "FunnelSeed", funnelSeed);
					setUniform(shader, "RingRise", ringRise);
					setUniform(shader, "RingSpeed", ringSpeed);
					setUniform(shader, "MeatNoiseScale", meatNoiseScale);
					setUniform(shader, "HighlightIntensity", highlightIntensity);
					setUniform(shader, "CenterVoidRadius", centerVoidRadius);
				},
				() -> {
					ShaderInstance shader = ShaderInit.APOTHEOS_FLOOR_FUNNEL.getInstance().get();
					if (shader instanceof ExtendedShaderInstance extended) {
						extended.setUniformDefaults();
					}
				});
		return RenderType.create("apotheos_floor_funnel",
				DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS, 32768, false, true,
				RenderType.CompositeState.builder()
						.setShaderState(ShaderInit.APOTHEOS_FLOOR_FUNNEL.getShard())
						.setTexturingState(uniforms)
						.setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
						.setDepthTestState(RenderType.NO_DEPTH_TEST)
						.setWriteMaskState(RenderType.COLOR_WRITE)
						.setCullState(RenderType.NO_CULL)
						.setLightmapState(RenderType.NO_LIGHTMAP)
						.createCompositeState(false));
	}

	public static RenderType apotheosWallMembrane(float gameTime, float wallSeed, float fiberScale,
			float traceIntensity, float redGlowIntensity, float ceilingFadeStart, float ceilingFadeEnd) {
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"apotheos_wall_membrane_uniforms",
				() -> {
					ShaderInstance shader = ShaderInit.APOTHEOS_WALL_MEMBRANE.getInstance().get();
					setUniform(shader, "HemoTime", gameTime);
					setUniform(shader, "WallSeed", wallSeed);
					setUniform(shader, "FiberScale", fiberScale);
					setUniform(shader, "TraceIntensity", traceIntensity);
					setUniform(shader, "RedGlowIntensity", redGlowIntensity);
					setUniform(shader, "CeilingFadeStart", ceilingFadeStart);
					setUniform(shader, "CeilingFadeEnd", ceilingFadeEnd);
				},
				() -> {
					ShaderInstance shader = ShaderInit.APOTHEOS_WALL_MEMBRANE.getInstance().get();
					if (shader instanceof ExtendedShaderInstance extended) {
						extended.setUniformDefaults();
					}
				});
		return RenderType.create("apotheos_wall_membrane",
				DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS, 32768, false, true,
				RenderType.CompositeState.builder()
						.setShaderState(ShaderInit.APOTHEOS_WALL_MEMBRANE.getShard())
						.setTexturingState(uniforms)
						.setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
						.setDepthTestState(RenderType.NO_DEPTH_TEST)
						.setWriteMaskState(RenderType.COLOR_WRITE)
						.setCullState(RenderType.NO_CULL)
						.setLightmapState(RenderType.NO_LIGHTMAP)
						.createCompositeState(false));
	}

	public static RenderType cardinalStaffBloodMelt(float gameTime, float blockSeed, float wiggleAmp,
			float centerX, float centerY, float centerZ, float finalizeProgress,
			float meltGroundY, float meltHeight) {
		CardinalStaffMeltKey key = new CardinalStaffMeltKey(gameTime, blockSeed, wiggleAmp, centerX, centerY,
				centerZ, finalizeProgress, meltGroundY, meltHeight);
		RenderType cached = CARDINAL_STAFF_MELT_TYPES.get(key);
		if (cached == null) {
			cached = createCardinalStaffBloodMelt(gameTime, blockSeed, wiggleAmp, centerX, centerY, centerZ,
					finalizeProgress, meltGroundY, meltHeight);
			CARDINAL_STAFF_MELT_TYPES.put(key, cached);
		}
		return cached;
	}

	private static RenderType createCardinalStaffBloodMelt(float gameTime, float blockSeed, float wiggleAmp,
			float centerX, float centerY, float centerZ, float finalizeProgress,
			float meltGroundY, float meltHeight) {
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"cardinal_staff_blood_melt_uniforms",
				() -> {
					ShaderInstance shader = ShaderInit.BLOOD_STRUCTURE_WARP_ENTITY.getInstance().get();
					setUniform(shader, "HemoTime", gameTime);
					setUniform(shader, "Progress", 1.0F);
					setUniform(shader, "BlockSeed", blockSeed);
					setUniform(shader, "WiggleAmp", wiggleAmp);
					setUniform(shader, "WarpCenter", centerX, centerY, centerZ);
					setUniform(shader, "FinalizeProgress", finalizeProgress);
					setUniform(shader, "MeltGroundY", meltGroundY);
					setUniform(shader, "MeltHeight", meltHeight);
				},
				() -> {
					ShaderInstance shader = ShaderInit.BLOOD_STRUCTURE_WARP_ENTITY.getInstance().get();
					if (shader instanceof ExtendedShaderInstance extended) {
						extended.setUniformDefaults();
					}
				});
		return RenderType.create("cardinal_staff_blood_melt",
				DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 1536, true, true,
				RenderType.CompositeState.builder()
						.setShaderState(ShaderInit.BLOOD_STRUCTURE_WARP_ENTITY.getShard())
						.setTexturingState(uniforms)
						.setLayeringState(RenderType.VIEW_OFFSET_Z_LAYERING)
						.setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
						.setDepthTestState(RenderType.LEQUAL_DEPTH_TEST)
						.setWriteMaskState(RenderType.COLOR_WRITE)
						.setCullState(RenderType.NO_CULL)
						.setLightmapState(RenderType.LIGHTMAP)
						.setOverlayState(RenderType.OVERLAY)
						.createCompositeState(false));
	}

	public static RenderType apotheosCeilingCore(float gameTime, float ceilingSeed, float coreNoiseScale,
			float rotationSpeed, float yellowGlowIntensity, float greenOrbIntensity, float coreUndulationIntensity) {
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"apotheos_ceiling_core_uniforms",
				() -> {
					ShaderInstance shader = ShaderInit.APOTHEOS_CEILING_CORE.getInstance().get();
					setUniform(shader, "HemoTime", gameTime);
					setUniform(shader, "CeilingSeed", ceilingSeed);
					setUniform(shader, "CoreNoiseScale", coreNoiseScale);
					setUniform(shader, "RotationSpeed", rotationSpeed);
					setUniform(shader, "YellowGlowIntensity", yellowGlowIntensity);
					setUniform(shader, "GreenOrbIntensity", greenOrbIntensity);
					setUniform(shader, "CoreUndulationIntensity", coreUndulationIntensity);
				},
				() -> {
					ShaderInstance shader = ShaderInit.APOTHEOS_CEILING_CORE.getInstance().get();
					if (shader instanceof ExtendedShaderInstance extended) {
						extended.setUniformDefaults();
					}
				});
		return RenderType.create("apotheos_ceiling_core",
				DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS, 49152, false, true,
				RenderType.CompositeState.builder()
						.setShaderState(ShaderInit.APOTHEOS_CEILING_CORE.getShard())
						.setTexturingState(uniforms)
						.setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
						.setDepthTestState(RenderType.NO_DEPTH_TEST)
						.setWriteMaskState(RenderType.COLOR_WRITE)
						.setCullState(RenderType.NO_CULL)
						.setLightmapState(RenderType.NO_LIGHTMAP)
						.createCompositeState(false));
	}

	public static RenderType apotheosCeilingAtmosphere(float gameTime, float ceilingSeed, float atmosphereNoiseScale,
			float rotationSpeed, float stormIntensity, float atmosphereOpacity) {
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"apotheos_ceiling_atmosphere_uniforms",
				() -> {
					ShaderInstance shader = ShaderInit.APOTHEOS_CEILING_ATMOSPHERE.getInstance().get();
					setUniform(shader, "HemoTime", gameTime);
					setUniform(shader, "CeilingSeed", ceilingSeed);
					setUniform(shader, "AtmosphereNoiseScale", atmosphereNoiseScale);
					setUniform(shader, "RotationSpeed", rotationSpeed);
					setUniform(shader, "StormIntensity", stormIntensity);
					setUniform(shader, "AtmosphereOpacity", atmosphereOpacity);
				},
				() -> {
					ShaderInstance shader = ShaderInit.APOTHEOS_CEILING_ATMOSPHERE.getInstance().get();
					if (shader instanceof ExtendedShaderInstance extended) {
						extended.setUniformDefaults();
					}
				});
		return RenderType.create("apotheos_ceiling_atmosphere",
				DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS, 49152, false, true,
				RenderType.CompositeState.builder()
						.setShaderState(ShaderInit.APOTHEOS_CEILING_ATMOSPHERE.getShard())
						.setTexturingState(uniforms)
						.setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
						.setDepthTestState(RenderType.NO_DEPTH_TEST)
						.setWriteMaskState(RenderType.COLOR_WRITE)
						.setCullState(RenderType.NO_CULL)
						.setLightmapState(RenderType.NO_LIGHTMAP)
						.createCompositeState(false));
	}

	public static RenderType apotheosWallTopRim(float gameTime, float rimSeed, float rimPulseSpeed,
			float rimCoreIntensity, float rimGlowIntensity) {
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"apotheos_wall_top_rim_uniforms",
				() -> {
					ShaderInstance shader = ShaderInit.APOTHEOS_WALL_TOP_RIM.getInstance().get();
					setUniform(shader, "HemoTime", gameTime);
					setUniform(shader, "RimSeed", rimSeed);
					setUniform(shader, "RimPulseSpeed", rimPulseSpeed);
					setUniform(shader, "RimCoreIntensity", rimCoreIntensity);
					setUniform(shader, "RimGlowIntensity", rimGlowIntensity);
				},
				() -> {
					ShaderInstance shader = ShaderInit.APOTHEOS_WALL_TOP_RIM.getInstance().get();
					if (shader instanceof ExtendedShaderInstance extended) {
						extended.setUniformDefaults();
					}
				});
		return RenderType.create("apotheos_wall_top_rim",
				DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS, 8192, false, true,
				RenderType.CompositeState.builder()
						.setShaderState(ShaderInit.APOTHEOS_WALL_TOP_RIM.getShard())
						.setTexturingState(uniforms)
						.setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
						.setDepthTestState(RenderType.NO_DEPTH_TEST)
						.setWriteMaskState(RenderType.COLOR_WRITE)
						.setCullState(RenderType.NO_CULL)
						.setLightmapState(RenderType.NO_LIGHTMAP)
						.createCompositeState(false));
	}

	public static RenderType apotheosPortalGlow(float gameTime, float glowSeed, float glowIntensity, float glowRadius,
			float centerVoidRadius) {
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"apotheos_portal_glow_uniforms",
				() -> {
					ShaderInstance shader = ShaderInit.APOTHEOS_PORTAL_GLOW.getInstance().get();
					setUniform(shader, "HemoTime", gameTime);
					setUniform(shader, "GlowSeed", glowSeed);
					setUniform(shader, "GlowIntensity", glowIntensity);
					setUniform(shader, "GlowRadius", glowRadius);
					setUniform(shader, "CenterVoidRadius", centerVoidRadius);
				},
				() -> {
					ShaderInstance shader = ShaderInit.APOTHEOS_PORTAL_GLOW.getInstance().get();
					if (shader instanceof ExtendedShaderInstance extended) {
						extended.setUniformDefaults();
					}
				});
		return RenderType.create("apotheos_portal_glow",
				DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS, 32768, false, true,
				RenderType.CompositeState.builder()
						.setShaderState(ShaderInit.APOTHEOS_PORTAL_GLOW.getShard())
						.setTexturingState(uniforms)
						.setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
						.setDepthTestState(RenderType.NO_DEPTH_TEST)
						.setWriteMaskState(RenderType.COLOR_WRITE)
						.setCullState(RenderType.NO_CULL)
						.setLightmapState(RenderType.NO_LIGHTMAP)
						.createCompositeState(false));
	}

	public static RenderType apotheosPortalHaze(float gameTime, float hazeSeed, float outwardSpeed,
			float hazeIntensity, float centerVoidRadius) {
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"apotheos_portal_haze_uniforms",
				() -> {
					ShaderInstance shader = ShaderInit.APOTHEOS_PORTAL_HAZE.getInstance().get();
					setUniform(shader, "HemoTime", gameTime);
					setUniform(shader, "HazeSeed", hazeSeed);
					setUniform(shader, "OutwardSpeed", outwardSpeed);
					setUniform(shader, "HazeIntensity", hazeIntensity);
					setUniform(shader, "CenterVoidRadius", centerVoidRadius);
				},
				() -> {
					ShaderInstance shader = ShaderInit.APOTHEOS_PORTAL_HAZE.getInstance().get();
					if (shader instanceof ExtendedShaderInstance extended) {
						extended.setUniformDefaults();
					}
				});
		return RenderType.create("apotheos_portal_haze",
				DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS, 32768, false, true,
				RenderType.CompositeState.builder()
						.setShaderState(ShaderInit.APOTHEOS_PORTAL_HAZE.getShard())
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
		return qliphothBlackHole(texture, -1, gameTime, holeSeed, lensStrength, ringIntensity, finalHole);
	}

	public static RenderType qliphothBlackHole(ResourceLocation fallbackTexture, int sceneTextureId,
			float gameTime, float holeSeed, float lensStrength, float ringIntensity, boolean finalHole) {
		return qliphothBlackHole(fallbackTexture, sceneTextureId, gameTime, holeSeed, lensStrength, ringIntensity,
				finalHole, finalHole);
	}

	public static RenderType qliphothBlackHole(ResourceLocation fallbackTexture, int sceneTextureId,
			float gameTime, float holeSeed, float lensStrength, float ringIntensity, boolean finalHole,
			boolean treeApex) {
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"qliphoth_black_hole_uniforms",
				() -> {
					ShaderInstance shader = ShaderInit.QLIPHOTH_BLACK_HOLE.getInstance().get();
					if (sceneTextureId >= 0) RenderSystem.setShaderTexture(0, sceneTextureId);
					setUniform(shader, "HemoTime", gameTime);
					setUniform(shader, "HoleSeed", holeSeed);
					setUniform(shader, "LensStrength", lensStrength);
					setUniform(shader, "RingIntensity", ringIntensity);
					setUniform(shader, "FinalHole", finalHole ? 1.0f : 0.0f);
					setUniform(shader, "TreeApex", treeApex ? 1.0f : 0.0f);
					setUniform(shader, "ScreenSize", (float)Minecraft.getInstance().getWindow().getWidth(),
							(float)Minecraft.getInstance().getWindow().getHeight());
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
						.setTextureState(new RenderStateShard.TextureStateShard(fallbackTexture, false, false))
						.setTexturingState(uniforms)
						.setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
						.setDepthTestState(treeApex ? RenderType.LEQUAL_DEPTH_TEST : RenderType.NO_DEPTH_TEST)
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

	/**
	 * The fog type for one rite. It is cached per seed and its time is updated in place, so callers must end the
	 * batch (as {@code CardinalRiteFogRenderer} does per rite) before asking for the same seed at a new time.
	 */
	public static RenderType cardinalRiteFog(float gameTime, float fogSeed) {
		CardinalRiteFogType fog = CARDINAL_RITE_FOG_TYPES.get(fogSeed);
		if (fog == null) {
			fog = new CardinalRiteFogType(fogSeed);
			CARDINAL_RITE_FOG_TYPES.put(fogSeed, fog);
		}
		fog.gameTime = gameTime;
		return fog.type;
	}

	private static RenderType createCardinalRiteFog(CardinalRiteFogType fog) {
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"cardinal_rite_fog_uniforms",
				() -> {
					ShaderInstance shader = ShaderInit.CARDINAL_RITE_FOG.getInstance().get();
					setUniform(shader, "HemoTime", fog.gameTime);
					setUniform(shader, "FogSeed", fog.seed);
					setUniform(shader, "ColorModulator", 1.0F, 1.0F, 1.0F, 1.0F);
				},
				() -> {
					ShaderInstance shader = ShaderInit.CARDINAL_RITE_FOG.getInstance().get();
					if (shader instanceof ExtendedShaderInstance extended) {
						extended.setUniformDefaults();
					}
				});
		return RenderType.create("cardinal_rite_fog",
				DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS, 16384, false, true,
				RenderType.CompositeState.builder()
						.setShaderState(ShaderInit.CARDINAL_RITE_FOG.getShard())
						.setTexturingState(uniforms)
						.setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
						.setDepthTestState(RenderType.LEQUAL_DEPTH_TEST)
						.setWriteMaskState(RenderType.COLOR_WRITE)
						.setCullState(RenderType.NO_CULL)
						.setLightmapState(RenderType.NO_LIGHTMAP)
						.createCompositeState(false));
	}

	/**
	 * One sky-wound type per seed; its uniforms are updated in place, so callers end the batch per wound. It writes
	 * depth (its shader discards empty sky), so Fabulous graphics composite it in front of the clouds behind it.
	 */
	private static final Map<Float, CardinalSkyWoundType> CARDINAL_SKY_WOUND_TYPES = riteTypeCache();

	private static final class CardinalSkyWoundType {
		private final float seed;
		private float gameTime;
		private float grow;
		private float open;
		private float flash;
		private float strain;
		private int baseColor;
		private int veinColor;
		private int glowColor;
		private final RenderType type;

		private CardinalSkyWoundType(float seed) {
			this.seed = seed;
			this.type = createCardinalSkyWound(this);
		}
	}

	public static RenderType cardinalRiteSkyWound(float gameTime, float seed, float grow, float open, float flash,
			float strain, int baseColor, int veinColor, int glowColor) {
		CardinalSkyWoundType wound = CARDINAL_SKY_WOUND_TYPES.get(seed);
		if (wound == null) {
			wound = new CardinalSkyWoundType(seed);
			CARDINAL_SKY_WOUND_TYPES.put(seed, wound);
		}
		wound.gameTime = gameTime;
		wound.grow = grow;
		wound.open = open;
		wound.flash = flash;
		wound.strain = strain;
		wound.baseColor = baseColor;
		wound.veinColor = veinColor;
		wound.glowColor = glowColor;
		return wound.type;
	}

	private static RenderType createCardinalSkyWound(CardinalSkyWoundType wound) {
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"cardinal_rite_sky_wound_uniforms",
				() -> {
					ShaderInstance shader = ShaderInit.CARDINAL_RITE_SKY_WOUND.getInstance().get();
					setUniform(shader, "HemoTime", wound.gameTime);
					setUniform(shader, "WoundSeed", wound.seed);
					setUniform(shader, "WoundShape", wound.grow, wound.open, wound.flash, wound.strain);
					setRgbUniform(shader, "BaseColor", wound.baseColor);
					setRgbUniform(shader, "VeinColor", wound.veinColor);
					setRgbUniform(shader, "GlowColor", wound.glowColor);
					setUniform(shader, "ColorModulator", 1.0F, 1.0F, 1.0F, 1.0F);
				},
				() -> {
					ShaderInstance shader = ShaderInit.CARDINAL_RITE_SKY_WOUND.getInstance().get();
					if (shader instanceof ExtendedShaderInstance extended) {
						extended.setUniformDefaults();
					}
				});
		return RenderType.create("cardinal_rite_sky_wound",
				DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS, 256, false, false,
				RenderType.CompositeState.builder()
						.setShaderState(ShaderInit.CARDINAL_RITE_SKY_WOUND.getShard())
						.setTexturingState(uniforms)
						.setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
						.setDepthTestState(RenderType.LEQUAL_DEPTH_TEST)
						.setWriteMaskState(RenderType.COLOR_DEPTH_WRITE)
						.setCullState(RenderType.NO_CULL)
						.setLightmapState(RenderType.NO_LIGHTMAP)
						.createCompositeState(false));
	}

	/** One haze type per daemon seed; its uniforms change in place, so the queue ends its batch per daemon. */
	private static final Map<Float, CardinalDaemonHazeType> CARDINAL_DAEMON_HAZE_TYPES = riteTypeCache();

	private static final class CardinalDaemonHazeType {
		private DaemonHazeQueue.Draw draw;
		private float cameraX;
		private float cameraY;
		private float cameraZ;
		private final RenderType type;
		/** With the camera inside the box, whatever shares the box must not hide the smoke in front of it. */
		private final RenderType insideType;

		private CardinalDaemonHazeType() {
			this.type = createCardinalDaemonHaze(this, false);
			this.insideType = createCardinalDaemonHaze(this, true);
		}
	}

	/** The daemon's smoke volume; the camera position is relative to the daemon's centre. */
	public static RenderType cardinalDaemonHaze(DaemonHazeQueue.Draw draw, float cameraX, float cameraY,
			float cameraZ) {
		CardinalDaemonHazeType haze = CARDINAL_DAEMON_HAZE_TYPES.get(draw.seed());
		if (haze == null) {
			haze = new CardinalDaemonHazeType();
			CARDINAL_DAEMON_HAZE_TYPES.put(draw.seed(), haze);
		}
		haze.draw = draw;
		haze.cameraX = cameraX;
		haze.cameraY = cameraY;
		haze.cameraZ = cameraZ;
		return DaemonHaze.cameraInside(cameraX, cameraY, cameraZ, draw.halfWidth(), draw.halfHeight())
				? haze.insideType : haze.type;
	}

	private static RenderType createCardinalDaemonHaze(CardinalDaemonHazeType haze, boolean inside) {
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"cardinal_daemon_haze_uniforms",
				() -> {
					ShaderInstance shader = ShaderInit.CARDINAL_DAEMON_HAZE.getInstance().get();
					DaemonHazeQueue.Draw draw = haze.draw;
					if (draw == null) return;
					setUniform(shader, "HemoTime", draw.time());
					setUniform(shader, "HazeSeed", draw.seed());
					setUniform(shader, "HazeState", draw.emergence(), draw.strength(), draw.swallow(), draw.eyes());
					setUniform(shader, "HazeTint", ((draw.tint() >> 16) & 0xFF) / 255.0F,
							((draw.tint() >> 8) & 0xFF) / 255.0F, (draw.tint() & 0xFF) / 255.0F, draw.flare());
					setUniform(shader, "HazeLean", draw.trailX(), draw.trailY(), draw.trailZ(), draw.recall());
					setUniform(shader, "HazeCamera", haze.cameraX, haze.cameraY, haze.cameraZ);
					setUniform(shader, "HazeShape", draw.height(), draw.yaw(), draw.halfWidth(), draw.halfHeight());
					setUniform(shader, "HazeHead", draw.headYaw());
					setUniform(shader, "HazeTilt", draw.tiltX(), 0.0F, draw.tiltZ(), draw.tiltAngle());
					setUniform(shader, "HazeFlow", draw.flow(), draw.stream());
					setUniform(shader, "ColorModulator", 1.0F, 1.0F, 1.0F, 1.0F);
				},
				() -> {
					ShaderInstance shader = ShaderInit.CARDINAL_DAEMON_HAZE.getInstance().get();
					if (shader instanceof ExtendedShaderInstance extended) {
						extended.setUniformDefaults();
					}
				});
		return RenderType.create(inside ? "cardinal_daemon_haze_inside" : "cardinal_daemon_haze",
				DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS, 256, false, false,
				RenderType.CompositeState.builder()
						.setShaderState(ShaderInit.CARDINAL_DAEMON_HAZE.getShard())
						.setTexturingState(uniforms)
						.setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
						.setDepthTestState(inside ? RenderType.NO_DEPTH_TEST : RenderType.LEQUAL_DEPTH_TEST)
						.setWriteMaskState(RenderType.COLOR_WRITE)
						.setCullState(RenderType.NO_CULL)
						.setLightmapState(RenderType.NO_LIGHTMAP)
						.createCompositeState(false));
	}


	/** One staff haze type per seed, with an inside variant; uniforms change in place, so each ends its batch. */
	private static final Map<Float, CardinalStaffHazeType> CARDINAL_STAFF_HAZE_TYPES = riteTypeCache();

	private static final class CardinalStaffHazeType {
		private DaemonHazeQueue.StaffDraw draw;
		private float cameraX;
		private float cameraY;
		private float cameraZ;
		private final RenderType type;
		private final RenderType insideType;

		private CardinalStaffHazeType() {
			this.type = createCardinalStaffHaze(this, false);
			this.insideType = createCardinalStaffHaze(this, true);
		}
	}

	/** The haze curling off a planted staff; the camera position is relative to the haze box's centre. */
	public static RenderType cardinalStaffHaze(DaemonHazeQueue.StaffDraw draw, float cameraX, float cameraY,
			float cameraZ) {
		CardinalStaffHazeType haze = CARDINAL_STAFF_HAZE_TYPES.get(draw.seed());
		if (haze == null) {
			haze = new CardinalStaffHazeType();
			CARDINAL_STAFF_HAZE_TYPES.put(draw.seed(), haze);
		}
		haze.draw = draw;
		haze.cameraX = cameraX;
		haze.cameraY = cameraY;
		haze.cameraZ = cameraZ;
		return DaemonHaze.cameraInside(cameraX, cameraY, cameraZ, DaemonHaze.STAFF_HAZE_HALF_WIDTH,
				DaemonHaze.STAFF_HAZE_HALF_HEIGHT) ? haze.insideType : haze.type;
	}

	private static RenderType createCardinalStaffHaze(CardinalStaffHazeType haze, boolean inside) {
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"cardinal_staff_haze_uniforms",
				() -> {
					ShaderInstance shader = ShaderInit.CARDINAL_STAFF_HAZE.getInstance().get();
					DaemonHazeQueue.StaffDraw draw = haze.draw;
					if (draw == null) return;
					setUniform(shader, "HemoTime", draw.time());
					setUniform(shader, "HazeSeed", draw.seed());
					setUniform(shader, "HazeState", draw.emergence(), draw.strength(), 0.0F, 0.0F);
					setUniform(shader, "HazeCamera", haze.cameraX, haze.cameraY, haze.cameraZ);
					setUniform(shader, "HazeShape", 0.0F, 0.0F, (float) DaemonHaze.STAFF_HAZE_HALF_WIDTH,
							(float) DaemonHaze.STAFF_HAZE_HALF_HEIGHT);
					setUniform(shader, "ColorModulator", 1.0F, 1.0F, 1.0F, 1.0F);
				},
				() -> {
					ShaderInstance shader = ShaderInit.CARDINAL_STAFF_HAZE.getInstance().get();
					if (shader instanceof ExtendedShaderInstance extended) {
						extended.setUniformDefaults();
					}
				});
		return RenderType.create(inside ? "cardinal_staff_haze_inside" : "cardinal_staff_haze",
				DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS, 256, false, false,
				RenderType.CompositeState.builder()
						.setShaderState(ShaderInit.CARDINAL_STAFF_HAZE.getShard())
						.setTexturingState(uniforms)
						.setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
						.setDepthTestState(inside ? RenderType.NO_DEPTH_TEST : RenderType.LEQUAL_DEPTH_TEST)
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

	private static void setUniform(ShaderInstance shader, String name, float x, float y) {
		if (shader == null) {
			return;
		}
		Uniform uniform = shader.getUniform(name);
		if (uniform != null) {
			uniform.set(x, y);
		}
	}

	private static void setUniform(ShaderInstance shader, String name,
			float x, float y, float z, float w) {
		if (shader == null) {
			return;
		}
		Uniform uniform = shader.getUniform(name);
		if (uniform != null) {
			uniform.set(x, y, z, w);
		}
	}
}

