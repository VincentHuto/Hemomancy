package com.vincenthuto.hemomancy.client.render;

import com.vincenthuto.hemomancy.client.render.world.rite.DaemonHaze;
import com.vincenthuto.hemomancy.client.render.world.rite.DaemonHazeQueue;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.shader.ExtendedShaderInstance;
import com.vincenthuto.hemomancy.client.render.shader.ShaderInit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

/**
 * Client-local render types used by animated item renderers.
 */
public final class HemoRenderTypes {

	private HemoRenderTypes() {
	}

	/** Seeds held per render type family; each seed keeps at most two types (see {@link SeededRenderTypes}). */
	private static final int SEEDED_TYPE_LIMIT = 256;
	/**
	 * Rite render types are held per rite seed: repeated requests within a frame (the floor asks for its membrane
	 * several times) share one type, so they batch together instead of each allocating a type and flushing the last.
	 */
	private static final int RITE_TYPE_CACHE_LIMIT = 32;

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

	/** The Abocipher font atlas, which the Dominion floor's seal spells its runes from. */
	public static final ResourceLocation ABOCIPHER_FONT_TEXTURE = Hemomancy.rloc("textures/font/abocipher.png");
	/** The engram runes packed a to z (tools/assets/build_engram_atlas.py), filtered smoothly for the Communion floor's inscriptions. */
	public static final ResourceLocation ENGRAM_ATLAS_TEXTURE = Hemomancy.rloc("textures/world/rite_floor/engram_atlas.png");

	public static final ResourceLocation MNEMONIC_LOWTIDE_PARCHMENT_TEXTURE = Hemomancy.rloc(
			"textures/world/lowtide_parchment/parchment_atlas.png");

	public static RenderType mnemonicLowtideParchment(float gameTime, float parchmentSeed, float windRippleStrength,
			float windRippleScale, float windDirectionX, float windDirectionY) {
		return MNEMONIC_LOWTIDE_PARCHMENT_TYPES.get(parchmentSeed, new MnemonicLowtideParchmentUniforms(gameTime,
			windRippleStrength, windRippleScale, windDirectionX, windDirectionY));
	}

	private record MnemonicLowtideParchmentUniforms(float gameTime, float windRippleStrength,
			float windRippleScale, float windDirectionX, float windDirectionY) {
	}

	private static final SeededRenderTypes<Float, MnemonicLowtideParchmentUniforms, RenderType> MNEMONIC_LOWTIDE_PARCHMENT_TYPES =
			new SeededRenderTypes<>(SEEDED_TYPE_LIMIT, HemoRenderTypes::createMnemonicLowtideParchment);

	private static RenderType createMnemonicLowtideParchment(Float key, Supplier<MnemonicLowtideParchmentUniforms> values) {
		float parchmentSeed = key;
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"mnemonic_lowtide_parchment_uniforms",
				() -> {
					MnemonicLowtideParchmentUniforms u = values.get();
					float gameTime = u.gameTime();
					float windRippleStrength = u.windRippleStrength();
					float windRippleScale = u.windRippleScale();
					float windDirectionX = u.windDirectionX();
					float windDirectionY = u.windDirectionY();
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
		return LOOM_ORB_SHELL_TYPES.get(new LoomOrbShellKey(orbSeed, glowLayer, writeDepth),
			new LoomOrbShellUniforms(gameTime, centerX, centerY, centerZ, orbRadius, writheStrength, threadScale));
	}

	private record LoomOrbShellUniforms(float gameTime, float centerX, float centerY, float centerZ,
			float orbRadius, float writheStrength, float threadScale) {
	}

	private record LoomOrbShellKey(float orbSeed, boolean glowLayer, boolean writeDepth) {
	}

	private static final SeededRenderTypes<LoomOrbShellKey, LoomOrbShellUniforms, RenderType> LOOM_ORB_SHELL_TYPES =
			new SeededRenderTypes<>(SEEDED_TYPE_LIMIT, HemoRenderTypes::createLoomOrbShell);

	private static RenderType createLoomOrbShell(LoomOrbShellKey key, Supplier<LoomOrbShellUniforms> values) {
		float orbSeed = key.orbSeed();
		boolean glowLayer = key.glowLayer();
		boolean writeDepth = key.writeDepth();
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"loom_orb_uniforms",
				() -> {
					LoomOrbShellUniforms u = values.get();
					float gameTime = u.gameTime();
					float centerX = u.centerX();
					float centerY = u.centerY();
					float centerZ = u.centerZ();
					float orbRadius = u.orbRadius();
					float writheStrength = u.writheStrength();
					float threadScale = u.threadScale();
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
		return MONOLITH_FRAGMENT_TYPES.get(shardSeed, new MonolithFragmentUniforms(gameTime, burden, attuned,
			guiClamp, vertexWarp));
	}

	private record MonolithFragmentUniforms(float gameTime, float burden, float attuned, float guiClamp,
			float vertexWarp) {
	}

	private static final SeededRenderTypes<Float, MonolithFragmentUniforms, RenderType> MONOLITH_FRAGMENT_TYPES =
			new SeededRenderTypes<>(SEEDED_TYPE_LIMIT, HemoRenderTypes::createMonolithFragment);

	private static RenderType createMonolithFragment(Float key, Supplier<MonolithFragmentUniforms> values) {
		float shardSeed = key;
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"monolith_fragment_uniforms",
				() -> {
					MonolithFragmentUniforms u = values.get();
					float gameTime = u.gameTime();
					float burden = u.burden();
					float attuned = u.attuned();
					float vertexWarp = u.vertexWarp();
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
		return SILENT_ARCHON_ARMOR_OVERLAY_TYPES.get(shardSeed, new SilentArchonArmorOverlayUniforms(gameTime,
			burden, attuned, guiClamp));
	}

	private record SilentArchonArmorOverlayUniforms(float gameTime, float burden, float attuned, float guiClamp) {
	}

	private static final SeededRenderTypes<Float, SilentArchonArmorOverlayUniforms, RenderType> SILENT_ARCHON_ARMOR_OVERLAY_TYPES =
			new SeededRenderTypes<>(SEEDED_TYPE_LIMIT, HemoRenderTypes::createSilentArchonArmorOverlay);

	private static RenderType createSilentArchonArmorOverlay(Float key, Supplier<SilentArchonArmorOverlayUniforms> values) {
		float shardSeed = key;
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"silent_archon_armor_overlay_uniforms",
				() -> {
					SilentArchonArmorOverlayUniforms u = values.get();
					float gameTime = u.gameTime();
					float burden = u.burden();
					float attuned = u.attuned();
					float guiClamp = u.guiClamp();
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
		return MONOLITHIC_DISLOCATION_SHELL_TYPES.get(shardSeed, new MonolithicDislocationShellUniforms(gameTime));
	}

	private record MonolithicDislocationShellUniforms(float gameTime) {
	}

	private static final SeededRenderTypes<Float, MonolithicDislocationShellUniforms, RenderType> MONOLITHIC_DISLOCATION_SHELL_TYPES =
			new SeededRenderTypes<>(SEEDED_TYPE_LIMIT, HemoRenderTypes::createMonolithicDislocationShell);

	private static RenderType createMonolithicDislocationShell(Float key, Supplier<MonolithicDislocationShellUniforms> values) {
		float shardSeed = key;
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"monolithic_dislocation_shell_uniforms",
				() -> {
					MonolithicDislocationShellUniforms u = values.get();
					float gameTime = u.gameTime();
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
		return WILL_STATE_MONOLITH_SHELL_TYPES.get(shardSeed, new WillStateMonolithShellUniforms(gameTime, red,
			green, blue, dissolveProgress, flickerAlpha));
	}

	private record WillStateMonolithShellUniforms(float gameTime, float red, float green, float blue,
			float dissolveProgress, float flickerAlpha) {
	}

	private static final SeededRenderTypes<Float, WillStateMonolithShellUniforms, RenderType> WILL_STATE_MONOLITH_SHELL_TYPES =
			new SeededRenderTypes<>(SEEDED_TYPE_LIMIT, HemoRenderTypes::createWillStateMonolithShell);

	private static RenderType createWillStateMonolithShell(Float key, Supplier<WillStateMonolithShellUniforms> values) {
		float shardSeed = key;
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"will_state_monolith_shell_uniforms",
				() -> {
					WillStateMonolithShellUniforms u = values.get();
					float gameTime = u.gameTime();
					float red = u.red();
					float green = u.green();
					float blue = u.blue();
					float dissolveProgress = u.dissolveProgress();
					float flickerAlpha = u.flickerAlpha();
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
		return MONOLITH_ENTITY_SURFACE_TYPES.get(shardSeed, new MonolithEntitySurfaceUniforms(gameTime, burden,
			attuned, fractalScale));
	}

	private record MonolithEntitySurfaceUniforms(float gameTime, float burden, float attuned, float fractalScale) {
	}

	private static final SeededRenderTypes<Float, MonolithEntitySurfaceUniforms, RenderType> MONOLITH_ENTITY_SURFACE_TYPES =
			new SeededRenderTypes<>(SEEDED_TYPE_LIMIT, HemoRenderTypes::createMonolithEntitySurface);

	private static RenderType createMonolithEntitySurface(Float key, Supplier<MonolithEntitySurfaceUniforms> values) {
		float shardSeed = key;
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"monolith_entity_surface_uniforms",
				() -> {
					MonolithEntitySurfaceUniforms u = values.get();
					float gameTime = u.gameTime();
					float burden = u.burden();
					float attuned = u.attuned();
					float fractalScale = u.fractalScale();
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
		return HERMIT_FAREWELL_DISSOLVE_TYPES.get(new HermitFarewellDissolveKey(texture, seed),
			new HermitFarewellDissolveUniforms(gameTime, progress));
	}

	private record HermitFarewellDissolveUniforms(float gameTime, float progress) {
	}

	private record HermitFarewellDissolveKey(ResourceLocation texture, float seed) {
	}

	private static final SeededRenderTypes<HermitFarewellDissolveKey, HermitFarewellDissolveUniforms, RenderType> HERMIT_FAREWELL_DISSOLVE_TYPES =
			new SeededRenderTypes<>(SEEDED_TYPE_LIMIT, HemoRenderTypes::createHermitFarewellDissolve);

	private static RenderType createHermitFarewellDissolve(HermitFarewellDissolveKey key, Supplier<HermitFarewellDissolveUniforms> values) {
		ResourceLocation texture = key.texture();
		float seed = key.seed();
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"hermit_farewell_dissolve_uniforms",
				() -> {
					HermitFarewellDissolveUniforms u = values.get();
					float gameTime = u.gameTime();
					float progress = u.progress();
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

	/** Vesper's open-wound tell: an additive blood skin whose vessels beat toward the wound (camera-relative centre). */
	public static RenderType vesperWoundDraw(ResourceLocation texture, float gameTime, float woundX, float woundY,
			float woundZ, float bodyYawRadians, float drainProgress, float flash, float seed) {
		return VESPER_WOUND_DRAW_TYPES.get(new VesperWoundDrawKey(texture, seed),
			new VesperWoundDrawUniforms(gameTime, woundX, woundY, woundZ, bodyYawRadians, drainProgress, flash));
	}

	private record VesperWoundDrawUniforms(float gameTime, float woundX, float woundY, float woundZ,
			float bodyYawRadians, float drainProgress, float flash) {
	}

	private record VesperWoundDrawKey(ResourceLocation texture, float seed) {
	}

	private static final SeededRenderTypes<VesperWoundDrawKey, VesperWoundDrawUniforms, RenderType> VESPER_WOUND_DRAW_TYPES =
			new SeededRenderTypes<>(SEEDED_TYPE_LIMIT, HemoRenderTypes::createVesperWoundDraw);

	private static RenderType createVesperWoundDraw(VesperWoundDrawKey key, Supplier<VesperWoundDrawUniforms> values) {
		ResourceLocation texture = key.texture();
		float seed = key.seed();
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"vesper_wound_draw_uniforms",
				() -> {
					VesperWoundDrawUniforms u = values.get();
					float gameTime = u.gameTime();
					float woundX = u.woundX();
					float woundY = u.woundY();
					float woundZ = u.woundZ();
					float bodyYawRadians = u.bodyYawRadians();
					float drainProgress = u.drainProgress();
					float flash = u.flash();
					ShaderInstance shader = ShaderInit.VESPER_WOUND_DRAW.getInstance().get();
					setUniform(shader, "HemoTime", gameTime);
					setUniform(shader, "WoundCenter", woundX, woundY, woundZ);
					setUniform(shader, "BodyYaw", bodyYawRadians);
					setUniform(shader, "DrainProgress", drainProgress);
					setUniform(shader, "DrainFlash", flash);
					setUniform(shader, "DrawSeed", seed);
				},
				() -> {
					ShaderInstance shader = ShaderInit.VESPER_WOUND_DRAW.getInstance().get();
					if (shader instanceof ExtendedShaderInstance extended) {
						extended.setUniformDefaults();
					}
				});
		return RenderType.create("vesper_wound_draw",
				DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 1536, true, true,
				RenderType.CompositeState.builder()
						.setShaderState(ShaderInit.VESPER_WOUND_DRAW.getShard())
						.setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
						.setTexturingState(uniforms)
						.setTransparencyState(RenderType.LIGHTNING_TRANSPARENCY)
						.setDepthTestState(RenderType.LEQUAL_DEPTH_TEST)
						.setWriteMaskState(RenderType.COLOR_WRITE)
						.setLayeringState(RenderType.VIEW_OFFSET_Z_LAYERING)
						.setCullState(RenderType.NO_CULL)
						.setLightmapState(RenderType.NO_LIGHTMAP)
						.setOverlayState(RenderType.NO_OVERLAY)
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
		return bloodStructureWarp(gameTime, progress, blockSeed, wiggleAmp, centerX, centerY, centerZ,
				finalizeProgress, meltGroundY, meltHeight, fillLine, 0.0F, 0.0F);
	}

	/**
	 * The structure warp shell as a single-block infusion times it: absent while the veins spread, it swallows
	 * the block over the end of the first {@code crackShare} of the finalize and melts only after the break.
	 */
	public static RenderType bloodInfusionWarp(float gameTime, float progress, float blockSeed, float wiggleAmp,
			float centerX, float centerY, float centerZ, float finalizeProgress, float meltGroundY, float meltHeight,
			float crackShare) {
		return bloodStructureWarp(gameTime, progress, blockSeed, wiggleAmp, centerX, centerY, centerZ,
				finalizeProgress, meltGroundY, meltHeight, NO_FILL_LINE, 1.0F, crackShare);
	}

	private static RenderType bloodStructureWarp(float gameTime, float progress, float blockSeed, float wiggleAmp,
			float centerX, float centerY, float centerZ, float finalizeProgress, float meltGroundY, float meltHeight,
			float fillLine, float infusionTiming, float crackShare) {
		return BLOOD_STRUCTURE_WARP_TYPES.get(blockSeed, new BloodStructureWarpUniforms(gameTime, progress,
			wiggleAmp, centerX, centerY, centerZ, finalizeProgress, meltGroundY, meltHeight, fillLine, infusionTiming,
			crackShare));
	}

	private record BloodStructureWarpUniforms(float gameTime, float progress, float wiggleAmp, float centerX,
			float centerY, float centerZ, float finalizeProgress, float meltGroundY, float meltHeight, float fillLine,
			float infusionTiming, float crackShare) {
	}

	private static final SeededRenderTypes<Float, BloodStructureWarpUniforms, RenderType> BLOOD_STRUCTURE_WARP_TYPES =
			new SeededRenderTypes<>(SEEDED_TYPE_LIMIT, HemoRenderTypes::createBloodStructureWarp);

	private static RenderType createBloodStructureWarp(Float key, Supplier<BloodStructureWarpUniforms> values) {
		float blockSeed = key;
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"blood_structure_warp_uniforms",
				() -> {
					BloodStructureWarpUniforms u = values.get();
					float gameTime = u.gameTime();
					float progress = u.progress();
					float wiggleAmp = u.wiggleAmp();
					float centerX = u.centerX();
					float centerY = u.centerY();
					float centerZ = u.centerZ();
					float finalizeProgress = u.finalizeProgress();
					float meltGroundY = u.meltGroundY();
					float meltHeight = u.meltHeight();
					float fillLine = u.fillLine();
					float infusionTiming = u.infusionTiming();
					float crackShare = u.crackShare();
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
					setUniform(shader, "InfusionTiming", infusionTiming);
					setUniform(shader, "CrackShare", crackShare);
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

	/**
	 * Veins on the faces of a block being infused, spreading out from the camera-relative {@code origin} (where the
	 * projection is aimed) on the face whose outward normal is {@code normal}. The web itself is fixed to the block
	 * through the camera-relative {@code anchor}, its minimum corner. The veins darken over the first
	 * {@code crackShare} of the finalize.
	 */
	public static RenderType bloodInfusionVeins(float gameTime, float progress, float blockSeed,
			float finalizeProgress, float crackShare, float originX, float originY, float originZ,
			float normalX, float normalY, float normalZ, float anchorX, float anchorY, float anchorZ) {
		return BLOOD_INFUSION_VEINS_TYPES.get(blockSeed, new BloodInfusionVeinsUniforms(gameTime, progress,
			finalizeProgress, crackShare, originX, originY, originZ, normalX, normalY, normalZ, anchorX, anchorY,
			anchorZ));
	}

	private record BloodInfusionVeinsUniforms(float gameTime, float progress, float finalizeProgress,
			float crackShare, float originX, float originY, float originZ, float normalX, float normalY, float normalZ,
			float anchorX, float anchorY, float anchorZ) {
	}

	private static final SeededRenderTypes<Float, BloodInfusionVeinsUniforms, RenderType> BLOOD_INFUSION_VEINS_TYPES =
			new SeededRenderTypes<>(SEEDED_TYPE_LIMIT, HemoRenderTypes::createBloodInfusionVeins);

	private static RenderType createBloodInfusionVeins(Float key, Supplier<BloodInfusionVeinsUniforms> values) {
		float blockSeed = key;
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"blood_infusion_veins_uniforms",
				() -> {
					BloodInfusionVeinsUniforms u = values.get();
					float gameTime = u.gameTime();
					float progress = u.progress();
					float finalizeProgress = u.finalizeProgress();
					float crackShare = u.crackShare();
					float originX = u.originX();
					float originY = u.originY();
					float originZ = u.originZ();
					float normalX = u.normalX();
					float normalY = u.normalY();
					float normalZ = u.normalZ();
					float anchorX = u.anchorX();
					float anchorY = u.anchorY();
					float anchorZ = u.anchorZ();
					ShaderInstance shader = ShaderInit.BLOOD_INFUSION_VEINS.getInstance().get();
					setUniform(shader, "HemoTime", gameTime);
					setUniform(shader, "Progress", progress);
					setUniform(shader, "BlockSeed", blockSeed);
					setUniform(shader, "FinalizeProgress", finalizeProgress);
					setUniform(shader, "CrackShare", crackShare);
					setUniform(shader, "VeinOrigin", originX, originY, originZ);
					setUniform(shader, "VeinNormal", normalX, normalY, normalZ);
					setUniform(shader, "VeinAnchor", anchorX, anchorY, anchorZ);
				},
				() -> {
				});
		return RenderType.create("blood_infusion_veins",
				DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 256, false, true,
				RenderType.CompositeState.builder()
						.setShaderState(ShaderInit.BLOOD_INFUSION_VEINS.getShard())
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
		return MYCELIAL_CRUCIBLE_BASIN_TYPES.get(basinSeed, new MycelialCrucibleBasinUniforms(gameTime,
			swirlIntensity));
	}

	private record MycelialCrucibleBasinUniforms(float gameTime, float swirlIntensity) {
	}

	private static final SeededRenderTypes<Float, MycelialCrucibleBasinUniforms, RenderType> MYCELIAL_CRUCIBLE_BASIN_TYPES =
			new SeededRenderTypes<>(SEEDED_TYPE_LIMIT, HemoRenderTypes::createMycelialCrucibleBasin);

	private static RenderType createMycelialCrucibleBasin(Float key, Supplier<MycelialCrucibleBasinUniforms> values) {
		float basinSeed = key;
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"mycelial_crucible_basin_uniforms",
				() -> {
					MycelialCrucibleBasinUniforms u = values.get();
					float gameTime = u.gameTime();
					float swirlIntensity = u.swirlIntensity();
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
	/** The Latent Infection's skin shader, drawn over its model; samples the skin texture to stay on its flesh. */
	public static RenderType latentInfectionSkin(ResourceLocation skin, float gameTime) {
		return LATENT_INFECTION_SKIN_TYPES.get(skin, new LatentInfectionSkinUniforms(gameTime));
	}

	private record LatentInfectionSkinUniforms(float gameTime) {
	}

	private static final SeededRenderTypes<ResourceLocation, LatentInfectionSkinUniforms, RenderType> LATENT_INFECTION_SKIN_TYPES =
			new SeededRenderTypes<>(SEEDED_TYPE_LIMIT, HemoRenderTypes::createLatentInfectionSkin);

	private static RenderType createLatentInfectionSkin(ResourceLocation key, Supplier<LatentInfectionSkinUniforms> values) {
		ResourceLocation skin = key;
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"latent_infection_skin_uniforms",
				() -> {
					LatentInfectionSkinUniforms u = values.get();
					float gameTime = u.gameTime();
					setUniform(ShaderInit.LATENT_INFECTION_SKIN.getInstance().get(), "HemoTime", gameTime);
				},
				() -> {
					ShaderInstance shader = ShaderInit.LATENT_INFECTION_SKIN.getInstance().get();
					if (shader instanceof ExtendedShaderInstance extended) {
						extended.setUniformDefaults();
					}
				});
		return RenderType.create("latent_infection_skin",
				DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 1536, false, true,
				RenderType.CompositeState.builder()
						.setShaderState(ShaderInit.LATENT_INFECTION_SKIN.getShard())
						.setTextureState(new RenderStateShard.TextureStateShard(skin, false, false))
						.setTexturingState(uniforms)
						.setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
						.setDepthTestState(RenderType.LEQUAL_DEPTH_TEST)
						.setWriteMaskState(RenderType.COLOR_WRITE)
						.setCullState(RenderType.CULL)
						.setLightmapState(RenderType.NO_LIGHTMAP)
						.createCompositeState(false));
	}

	/** Spilled infected blood on a floor, in the infected pool shader; same vertex layout as {@link #bloodPool}. */
	public static RenderType infectedPool(float gameTime, float poolSeed) {
		return INFECTED_POOL_TYPES.get(poolSeed, new InfectedPoolUniforms(gameTime));
	}

	private record InfectedPoolUniforms(float gameTime) {
	}

	private static final SeededRenderTypes<Float, InfectedPoolUniforms, RenderType> INFECTED_POOL_TYPES =
			new SeededRenderTypes<>(SEEDED_TYPE_LIMIT, HemoRenderTypes::createInfectedPool);

	private static RenderType createInfectedPool(Float key, Supplier<InfectedPoolUniforms> values) {
		float poolSeed = key;
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"infected_pool_uniforms",
				() -> {
					InfectedPoolUniforms u = values.get();
					float gameTime = u.gameTime();
					ShaderInstance shader = ShaderInit.INFECTED_POOL.getInstance().get();
					setUniform(shader, "HemoTime", gameTime);
					setUniform(shader, "PoolSeed", poolSeed);
				},
				() -> {
					ShaderInstance shader = ShaderInit.INFECTED_POOL.getInstance().get();
					if (shader instanceof ExtendedShaderInstance extended) {
						extended.setUniformDefaults();
					}
				});
		return RenderType.create("infected_pool",
				DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS, 256, false, true,
				RenderType.CompositeState.builder()
						.setShaderState(ShaderInit.INFECTED_POOL.getShard())
						.setTexturingState(uniforms)
						.setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
						.setDepthTestState(RenderType.LEQUAL_DEPTH_TEST)
						.setWriteMaskState(RenderType.COLOR_WRITE)
						.setCullState(RenderType.NO_CULL)
						.setLightmapState(RenderType.NO_LIGHTMAP)
						.createCompositeState(false));
	}

	public static RenderType bloodPool(float gameTime, float poolSeed) {
		return bloodPool(gameTime, poolSeed, 0F);
	}

	/** A pool of loose blood on the ground: {@code ragged} above 0 lobes its edge instead of a vessel's round rim. */
	public static RenderType bloodPool(float gameTime, float poolSeed, float ragged) {
		return bloodPool(gameTime, poolSeed, ragged, 0F);
	}

	/** Blood filling a square vessel's mouth, such as a cauldron's; draw it with the mouth's half-width as radius. */
	public static RenderType squareBloodPool(float gameTime, float poolSeed) {
		return bloodPool(gameTime, poolSeed, 0F, 1F);
	}

	private static RenderType bloodPool(float gameTime, float poolSeed, float ragged, float square) {
		return BLOOD_POOL_TYPES.get(poolSeed, new BloodPoolUniforms(gameTime, ragged, square));
	}

	private record BloodPoolUniforms(float gameTime, float ragged, float square) {
	}

	private static final SeededRenderTypes<Float, BloodPoolUniforms, RenderType> BLOOD_POOL_TYPES =
			new SeededRenderTypes<>(SEEDED_TYPE_LIMIT, HemoRenderTypes::createBloodPool);

	private static RenderType createBloodPool(Float key, Supplier<BloodPoolUniforms> values) {
		float poolSeed = key;
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"blood_pool_uniforms",
				() -> {
					BloodPoolUniforms u = values.get();
					float gameTime = u.gameTime();
					float ragged = u.ragged();
					float square = u.square();
					ShaderInstance shader = ShaderInit.BLOOD_POOL.getInstance().get();
					setUniform(shader, "HemoTime", gameTime);
					setUniform(shader, "PoolSeed", poolSeed);
					setUniform(shader, "Ragged", ragged);
					setUniform(shader, "Square", square);
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
	 * star, 1 its lanes of ichor, 2 its node caps. One type per layer and seed, so each loom's draw in a frame keeps
	 * its own engram.
	 */
	public static RenderType engramSigil(EngramSigilParams params) {
		return ENGRAM_SIGIL_TYPES.get(new EngramSigilKey(params.layer(), params.seed()), params);
	}

	/** One engram draw's uniforms. Layer: 0 skin, 1 lanes, 2 caps. AimZ above one half means the staff points at it. */
	public record EngramSigilParams(int layer, float gameTime, float seed, float radius, float unfurl, float withdraw,
			float beat, float pressure, boolean keyed, int rotation, int turns, float dim, float light, float aimX,
			float aimY, float aimZ, float dissolve) {
	}

	private record EngramSigilKey(int layer, float seed) {
	}

	private static final SeededRenderTypes<EngramSigilKey, EngramSigilParams, RenderType> ENGRAM_SIGIL_TYPES =
			new SeededRenderTypes<>(RITE_TYPE_CACHE_LIMIT, HemoRenderTypes::createEngramSigil);

	private static RenderType createEngramSigil(EngramSigilKey key, Supplier<EngramSigilParams> values) {
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"engram_sigil_uniforms",
				() -> {
					EngramSigilParams p = values.get();
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
		return RenderType.create("engram_sigil_" + key.layer(),
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

	private record CardinalFloorKey(String name, RenderStateShard.WriteMaskStateShard writeMask, float seed) {
	}

	private record CardinalFloorUniforms(float gameTime, int style, int floorHalf, float spread, float front,
			float beat, float floorLight, int baseColor, int veinColor, int glowColor, float focusX, float focusZ) {
	}

	private static final SeededRenderTypes<CardinalFloorKey, CardinalFloorUniforms, RenderType> CARDINAL_FLOOR_TYPES =
			new SeededRenderTypes<>(RITE_TYPE_CACHE_LIMIT, HemoRenderTypes::createCardinalFloor);

	private static RenderType cardinalFloor(String name, RenderStateShard.WriteMaskStateShard writeMask,
			float gameTime, float seed, int style, int floorHalf, float spread, float front, float beat,
			float floorLight, int baseColor, int veinColor, int glowColor, float focusX, float focusZ) {
		return CARDINAL_FLOOR_TYPES.get(new CardinalFloorKey(name, writeMask, seed), new CardinalFloorUniforms(
				gameTime, style, floorHalf, spread, front, beat, floorLight, baseColor, veinColor, glowColor, focusX,
				focusZ));
	}

	private static RenderType createCardinalFloor(CardinalFloorKey key, Supplier<CardinalFloorUniforms> values) {
		String name = key.name();
		RenderStateShard.WriteMaskStateShard writeMask = key.writeMask();
		float seed = key.seed();
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"cardinal_floor_membrane_uniforms",
				() -> {
					CardinalFloorUniforms u = values.get();
					float gameTime = u.gameTime();
					int style = u.style();
					int floorHalf = u.floorHalf();
					float spread = u.spread();
					float front = u.front();
					float beat = u.beat();
					float floorLight = u.floorLight();
					int baseColor = u.baseColor();
					int veinColor = u.veinColor();
					int glowColor = u.glowColor();
					float focusX = u.focusX();
					float focusZ = u.focusZ();
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
						.setTextureState(RenderStateShard.MultiTextureStateShard.builder()
								.add(ABOCIPHER_FONT_TEXTURE, false, false)
								.add(ENGRAM_ATLAS_TEXTURE, true, false)
								.build())
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
		return MNEMONIC_LOWTIDE_LAKE_TYPES.get(lakeSeed, new MnemonicLowtideLakeUniforms(gameTime, waveStrength,
			waveDetailScale, noiseScale, glossStrength, edgeFade));
	}

	private record MnemonicLowtideLakeUniforms(float gameTime, float waveStrength, float waveDetailScale,
			float noiseScale, float glossStrength, float edgeFade) {
	}

	private static final SeededRenderTypes<Float, MnemonicLowtideLakeUniforms, RenderType> MNEMONIC_LOWTIDE_LAKE_TYPES =
			new SeededRenderTypes<>(SEEDED_TYPE_LIMIT, HemoRenderTypes::createMnemonicLowtideLake);

	private static RenderType createMnemonicLowtideLake(Float key, Supplier<MnemonicLowtideLakeUniforms> values) {
		float lakeSeed = key;
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"mnemonic_lowtide_lake_uniforms",
				() -> {
					MnemonicLowtideLakeUniforms u = values.get();
					float gameTime = u.gameTime();
					float waveStrength = u.waveStrength();
					float waveDetailScale = u.waveDetailScale();
					float noiseScale = u.noiseScale();
					float glossStrength = u.glossStrength();
					float edgeFade = u.edgeFade();
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
		return APOTHEOS_FLOOR_FUNNEL_TYPES.get(funnelSeed, new ApotheosFloorFunnelUniforms(gameTime, ringRise,
			ringSpeed, meatNoiseScale, highlightIntensity, centerVoidRadius));
	}

	private record ApotheosFloorFunnelUniforms(float gameTime, float ringRise, float ringSpeed,
			float meatNoiseScale, float highlightIntensity, float centerVoidRadius) {
	}

	private static final SeededRenderTypes<Float, ApotheosFloorFunnelUniforms, RenderType> APOTHEOS_FLOOR_FUNNEL_TYPES =
			new SeededRenderTypes<>(SEEDED_TYPE_LIMIT, HemoRenderTypes::createApotheosFloorFunnel);

	private static RenderType createApotheosFloorFunnel(Float key, Supplier<ApotheosFloorFunnelUniforms> values) {
		float funnelSeed = key;
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"apotheos_floor_funnel_uniforms",
				() -> {
					ApotheosFloorFunnelUniforms u = values.get();
					float gameTime = u.gameTime();
					float ringRise = u.ringRise();
					float ringSpeed = u.ringSpeed();
					float meatNoiseScale = u.meatNoiseScale();
					float highlightIntensity = u.highlightIntensity();
					float centerVoidRadius = u.centerVoidRadius();
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
		return APOTHEOS_WALL_MEMBRANE_TYPES.get(wallSeed, new ApotheosWallMembraneUniforms(gameTime, fiberScale,
			traceIntensity, redGlowIntensity, ceilingFadeStart, ceilingFadeEnd));
	}

	private record ApotheosWallMembraneUniforms(float gameTime, float fiberScale, float traceIntensity,
			float redGlowIntensity, float ceilingFadeStart, float ceilingFadeEnd) {
	}

	private static final SeededRenderTypes<Float, ApotheosWallMembraneUniforms, RenderType> APOTHEOS_WALL_MEMBRANE_TYPES =
			new SeededRenderTypes<>(SEEDED_TYPE_LIMIT, HemoRenderTypes::createApotheosWallMembrane);

	private static RenderType createApotheosWallMembrane(Float key, Supplier<ApotheosWallMembraneUniforms> values) {
		float wallSeed = key;
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"apotheos_wall_membrane_uniforms",
				() -> {
					ApotheosWallMembraneUniforms u = values.get();
					float gameTime = u.gameTime();
					float fiberScale = u.fiberScale();
					float traceIntensity = u.traceIntensity();
					float redGlowIntensity = u.redGlowIntensity();
					float ceilingFadeStart = u.ceilingFadeStart();
					float ceilingFadeEnd = u.ceilingFadeEnd();
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
		return CARDINAL_STAFF_MELT_TYPES.get(blockSeed, new CardinalStaffMeltUniforms(gameTime, wiggleAmp, centerX,
				centerY, centerZ, finalizeProgress, meltGroundY, meltHeight));
	}

	private record CardinalStaffMeltUniforms(float gameTime, float wiggleAmp, float centerX, float centerY,
			float centerZ, float finalizeProgress, float meltGroundY, float meltHeight) {
	}

	private static final SeededRenderTypes<Float, CardinalStaffMeltUniforms, RenderType> CARDINAL_STAFF_MELT_TYPES =
			new SeededRenderTypes<>(RITE_TYPE_CACHE_LIMIT, HemoRenderTypes::createCardinalStaffBloodMelt);

	private static RenderType createCardinalStaffBloodMelt(Float key, Supplier<CardinalStaffMeltUniforms> values) {
		float blockSeed = key;
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"cardinal_staff_blood_melt_uniforms",
				() -> {
					CardinalStaffMeltUniforms u = values.get();
					float gameTime = u.gameTime();
					float wiggleAmp = u.wiggleAmp();
					float centerX = u.centerX();
					float centerY = u.centerY();
					float centerZ = u.centerZ();
					float finalizeProgress = u.finalizeProgress();
					float meltGroundY = u.meltGroundY();
					float meltHeight = u.meltHeight();
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
		return APOTHEOS_CEILING_CORE_TYPES.get(ceilingSeed, new ApotheosCeilingCoreUniforms(gameTime, coreNoiseScale,
			rotationSpeed, yellowGlowIntensity, greenOrbIntensity, coreUndulationIntensity));
	}

	private record ApotheosCeilingCoreUniforms(float gameTime, float coreNoiseScale, float rotationSpeed,
			float yellowGlowIntensity, float greenOrbIntensity, float coreUndulationIntensity) {
	}

	private static final SeededRenderTypes<Float, ApotheosCeilingCoreUniforms, RenderType> APOTHEOS_CEILING_CORE_TYPES =
			new SeededRenderTypes<>(SEEDED_TYPE_LIMIT, HemoRenderTypes::createApotheosCeilingCore);

	private static RenderType createApotheosCeilingCore(Float key, Supplier<ApotheosCeilingCoreUniforms> values) {
		float ceilingSeed = key;
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"apotheos_ceiling_core_uniforms",
				() -> {
					ApotheosCeilingCoreUniforms u = values.get();
					float gameTime = u.gameTime();
					float coreNoiseScale = u.coreNoiseScale();
					float rotationSpeed = u.rotationSpeed();
					float yellowGlowIntensity = u.yellowGlowIntensity();
					float greenOrbIntensity = u.greenOrbIntensity();
					float coreUndulationIntensity = u.coreUndulationIntensity();
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
		return APOTHEOS_CEILING_ATMOSPHERE_TYPES.get(ceilingSeed, new ApotheosCeilingAtmosphereUniforms(gameTime,
			atmosphereNoiseScale, rotationSpeed, stormIntensity, atmosphereOpacity));
	}

	private record ApotheosCeilingAtmosphereUniforms(float gameTime, float atmosphereNoiseScale,
			float rotationSpeed, float stormIntensity, float atmosphereOpacity) {
	}

	private static final SeededRenderTypes<Float, ApotheosCeilingAtmosphereUniforms, RenderType> APOTHEOS_CEILING_ATMOSPHERE_TYPES =
			new SeededRenderTypes<>(SEEDED_TYPE_LIMIT, HemoRenderTypes::createApotheosCeilingAtmosphere);

	private static RenderType createApotheosCeilingAtmosphere(Float key, Supplier<ApotheosCeilingAtmosphereUniforms> values) {
		float ceilingSeed = key;
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"apotheos_ceiling_atmosphere_uniforms",
				() -> {
					ApotheosCeilingAtmosphereUniforms u = values.get();
					float gameTime = u.gameTime();
					float atmosphereNoiseScale = u.atmosphereNoiseScale();
					float rotationSpeed = u.rotationSpeed();
					float stormIntensity = u.stormIntensity();
					float atmosphereOpacity = u.atmosphereOpacity();
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
		return APOTHEOS_WALL_TOP_RIM_TYPES.get(rimSeed, new ApotheosWallTopRimUniforms(gameTime, rimPulseSpeed,
			rimCoreIntensity, rimGlowIntensity));
	}

	private record ApotheosWallTopRimUniforms(float gameTime, float rimPulseSpeed, float rimCoreIntensity,
			float rimGlowIntensity) {
	}

	private static final SeededRenderTypes<Float, ApotheosWallTopRimUniforms, RenderType> APOTHEOS_WALL_TOP_RIM_TYPES =
			new SeededRenderTypes<>(SEEDED_TYPE_LIMIT, HemoRenderTypes::createApotheosWallTopRim);

	private static RenderType createApotheosWallTopRim(Float key, Supplier<ApotheosWallTopRimUniforms> values) {
		float rimSeed = key;
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"apotheos_wall_top_rim_uniforms",
				() -> {
					ApotheosWallTopRimUniforms u = values.get();
					float gameTime = u.gameTime();
					float rimPulseSpeed = u.rimPulseSpeed();
					float rimCoreIntensity = u.rimCoreIntensity();
					float rimGlowIntensity = u.rimGlowIntensity();
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
		return APOTHEOS_PORTAL_GLOW_TYPES.get(glowSeed, new ApotheosPortalGlowUniforms(gameTime, glowIntensity,
			glowRadius, centerVoidRadius));
	}

	private record ApotheosPortalGlowUniforms(float gameTime, float glowIntensity, float glowRadius,
			float centerVoidRadius) {
	}

	private static final SeededRenderTypes<Float, ApotheosPortalGlowUniforms, RenderType> APOTHEOS_PORTAL_GLOW_TYPES =
			new SeededRenderTypes<>(SEEDED_TYPE_LIMIT, HemoRenderTypes::createApotheosPortalGlow);

	private static RenderType createApotheosPortalGlow(Float key, Supplier<ApotheosPortalGlowUniforms> values) {
		float glowSeed = key;
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"apotheos_portal_glow_uniforms",
				() -> {
					ApotheosPortalGlowUniforms u = values.get();
					float gameTime = u.gameTime();
					float glowIntensity = u.glowIntensity();
					float glowRadius = u.glowRadius();
					float centerVoidRadius = u.centerVoidRadius();
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
		return APOTHEOS_PORTAL_HAZE_TYPES.get(hazeSeed, new ApotheosPortalHazeUniforms(gameTime, outwardSpeed,
			hazeIntensity, centerVoidRadius));
	}

	private record ApotheosPortalHazeUniforms(float gameTime, float outwardSpeed, float hazeIntensity,
			float centerVoidRadius) {
	}

	private static final SeededRenderTypes<Float, ApotheosPortalHazeUniforms, RenderType> APOTHEOS_PORTAL_HAZE_TYPES =
			new SeededRenderTypes<>(SEEDED_TYPE_LIMIT, HemoRenderTypes::createApotheosPortalHaze);

	private static RenderType createApotheosPortalHaze(Float key, Supplier<ApotheosPortalHazeUniforms> values) {
		float hazeSeed = key;
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"apotheos_portal_haze_uniforms",
				() -> {
					ApotheosPortalHazeUniforms u = values.get();
					float gameTime = u.gameTime();
					float outwardSpeed = u.outwardSpeed();
					float hazeIntensity = u.hazeIntensity();
					float centerVoidRadius = u.centerVoidRadius();
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
		return MNEMONIC_LOWTIDE_SKYBOX_TYPES.get(faceSeed, new MnemonicLowtideSkyboxUniforms(gameTime, coverageBias,
			tunnelScale, bubbleScale, tendrilIntensity));
	}

	private record MnemonicLowtideSkyboxUniforms(float gameTime, float coverageBias, float tunnelScale,
			float bubbleScale, float tendrilIntensity) {
	}

	private static final SeededRenderTypes<Float, MnemonicLowtideSkyboxUniforms, RenderType> MNEMONIC_LOWTIDE_SKYBOX_TYPES =
			new SeededRenderTypes<>(SEEDED_TYPE_LIMIT, HemoRenderTypes::createMnemonicLowtideSkybox);

	private static RenderType createMnemonicLowtideSkybox(Float key, Supplier<MnemonicLowtideSkyboxUniforms> values) {
		float faceSeed = key;
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"mnemonic_lowtide_skybox_uniforms",
				() -> {
					MnemonicLowtideSkyboxUniforms u = values.get();
					float gameTime = u.gameTime();
					float coverageBias = u.coverageBias();
					float tunnelScale = u.tunnelScale();
					float bubbleScale = u.bubbleScale();
					float tendrilIntensity = u.tendrilIntensity();
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
		return MNEMONIC_LOWTIDE_SKYBOX_BASE_TYPES.get(faceSeed, new MnemonicLowtideSkyboxBaseUniforms(gameTime,
			coverageBias, noduleScale, veinIntensity, baseIntensity));
	}

	private record MnemonicLowtideSkyboxBaseUniforms(float gameTime, float coverageBias, float noduleScale,
			float veinIntensity, float baseIntensity) {
	}

	private static final SeededRenderTypes<Float, MnemonicLowtideSkyboxBaseUniforms, RenderType> MNEMONIC_LOWTIDE_SKYBOX_BASE_TYPES =
			new SeededRenderTypes<>(SEEDED_TYPE_LIMIT, HemoRenderTypes::createMnemonicLowtideSkyboxBase);

	private static RenderType createMnemonicLowtideSkyboxBase(Float key, Supplier<MnemonicLowtideSkyboxBaseUniforms> values) {
		float faceSeed = key;
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"mnemonic_lowtide_skybox_base_uniforms",
				() -> {
					MnemonicLowtideSkyboxBaseUniforms u = values.get();
					float gameTime = u.gameTime();
					float coverageBias = u.coverageBias();
					float noduleScale = u.noduleScale();
					float veinIntensity = u.veinIntensity();
					float baseIntensity = u.baseIntensity();
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
		return QLIPHOTH_BLACK_HOLE_TYPES.get(new QliphothBlackHoleKey(fallbackTexture, holeSeed, finalHole,
			treeApex), new QliphothBlackHoleUniforms(sceneTextureId, gameTime, lensStrength, ringIntensity));
	}

	private record QliphothBlackHoleUniforms(int sceneTextureId, float gameTime, float lensStrength,
			float ringIntensity) {
	}

	private record QliphothBlackHoleKey(ResourceLocation fallbackTexture, float holeSeed, boolean finalHole,
			boolean treeApex) {
	}

	private static final SeededRenderTypes<QliphothBlackHoleKey, QliphothBlackHoleUniforms, RenderType> QLIPHOTH_BLACK_HOLE_TYPES =
			new SeededRenderTypes<>(SEEDED_TYPE_LIMIT, HemoRenderTypes::createQliphothBlackHole);

	private static RenderType createQliphothBlackHole(QliphothBlackHoleKey key, Supplier<QliphothBlackHoleUniforms> values) {
		ResourceLocation fallbackTexture = key.fallbackTexture();
		float holeSeed = key.holeSeed();
		boolean finalHole = key.finalHole();
		boolean treeApex = key.treeApex();
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"qliphoth_black_hole_uniforms",
				() -> {
					QliphothBlackHoleUniforms u = values.get();
					int sceneTextureId = u.sceneTextureId();
					float gameTime = u.gameTime();
					float lensStrength = u.lensStrength();
					float ringIntensity = u.ringIntensity();
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
		return SILENT_ARCHON_STORM_CLOUD_TYPES.get(cloudSeed, new SilentArchonStormCloudUniforms(gameTime,
			cloudDensity));
	}

	private record SilentArchonStormCloudUniforms(float gameTime, float cloudDensity) {
	}

	private static final SeededRenderTypes<Float, SilentArchonStormCloudUniforms, RenderType> SILENT_ARCHON_STORM_CLOUD_TYPES =
			new SeededRenderTypes<>(SEEDED_TYPE_LIMIT, HemoRenderTypes::createSilentArchonStormCloud);

	private static RenderType createSilentArchonStormCloud(Float key, Supplier<SilentArchonStormCloudUniforms> values) {
		float cloudSeed = key;
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"silent_archon_storm_cloud_uniforms",
				() -> {
					SilentArchonStormCloudUniforms u = values.get();
					float gameTime = u.gameTime();
					float cloudDensity = u.cloudDensity();
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

	/** The fog type for one rite, held per seed. */
	public static RenderType cardinalRiteFog(float gameTime, float fogSeed) {
		return CARDINAL_RITE_FOG_TYPES.get(fogSeed, gameTime);
	}

	private static final SeededRenderTypes<Float, Float, RenderType> CARDINAL_RITE_FOG_TYPES =
			new SeededRenderTypes<>(RITE_TYPE_CACHE_LIMIT, HemoRenderTypes::createCardinalRiteFog);

	private static RenderType createCardinalRiteFog(Float fogSeed, Supplier<Float> gameTime) {
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"cardinal_rite_fog_uniforms",
				() -> {
					ShaderInstance shader = ShaderInit.CARDINAL_RITE_FOG.getInstance().get();
					setUniform(shader, "HemoTime", gameTime.get());
					setUniform(shader, "FogSeed", fogSeed);
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
	 * One sky-wound type per seed. It writes depth (its shader discards empty sky), so Fabulous graphics composite it
	 * in front of the clouds behind it.
	 */
	private record CardinalSkyWoundUniforms(float gameTime, float grow, float open, float flash, float strain,
			int baseColor, int veinColor, int glowColor) {
	}

	private static final SeededRenderTypes<Float, CardinalSkyWoundUniforms, RenderType> CARDINAL_SKY_WOUND_TYPES =
			new SeededRenderTypes<>(RITE_TYPE_CACHE_LIMIT, HemoRenderTypes::createCardinalSkyWound);

	public static RenderType cardinalRiteSkyWound(float gameTime, float seed, float grow, float open, float flash,
			float strain, int baseColor, int veinColor, int glowColor) {
		return CARDINAL_SKY_WOUND_TYPES.get(seed, new CardinalSkyWoundUniforms(gameTime, grow, open, flash, strain,
				baseColor, veinColor, glowColor));
	}

	private static RenderType createCardinalSkyWound(Float seed, Supplier<CardinalSkyWoundUniforms> values) {
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"cardinal_rite_sky_wound_uniforms",
				() -> {
					CardinalSkyWoundUniforms wound = values.get();
					ShaderInstance shader = ShaderInit.CARDINAL_RITE_SKY_WOUND.getInstance().get();
					setUniform(shader, "HemoTime", wound.gameTime());
					setUniform(shader, "WoundSeed", seed);
					setUniform(shader, "WoundShape", wound.grow(), wound.open(), wound.flash(), wound.strain());
					setRgbUniform(shader, "BaseColor", wound.baseColor());
					setRgbUniform(shader, "VeinColor", wound.veinColor());
					setRgbUniform(shader, "GlowColor", wound.glowColor());
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

	/**
	 * One haze type per daemon seed, and one more for when the camera is inside the box: then whatever shares the box
	 * must not hide the smoke in front of it.
	 */
	private record HazeKey(float seed, boolean inside) {
	}

	private record DaemonHazeUniforms(DaemonHazeQueue.Draw draw, float cameraX, float cameraY, float cameraZ) {
	}

	private static final SeededRenderTypes<HazeKey, DaemonHazeUniforms, RenderType> CARDINAL_DAEMON_HAZE_TYPES =
			new SeededRenderTypes<>(RITE_TYPE_CACHE_LIMIT, HemoRenderTypes::createCardinalDaemonHaze);

	/** The daemon's smoke volume; the camera position is relative to the daemon's centre. */
	public static RenderType cardinalDaemonHaze(DaemonHazeQueue.Draw draw, float cameraX, float cameraY,
			float cameraZ) {
		boolean inside = DaemonHaze.cameraInside(cameraX, cameraY, cameraZ, draw.halfWidth(), draw.halfHeight());
		return CARDINAL_DAEMON_HAZE_TYPES.get(new HazeKey(draw.seed(), inside),
				new DaemonHazeUniforms(draw, cameraX, cameraY, cameraZ));
	}

	private static RenderType createCardinalDaemonHaze(HazeKey key, Supplier<DaemonHazeUniforms> values) {
		boolean inside = key.inside();
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"cardinal_daemon_haze_uniforms",
				() -> {
					ShaderInstance shader = ShaderInit.CARDINAL_DAEMON_HAZE.getInstance().get();
					DaemonHazeUniforms haze = values.get();
					DaemonHazeQueue.Draw draw = haze.draw();
					setUniform(shader, "HemoTime", draw.time());
					setUniform(shader, "HazeSeed", draw.seed());
					setUniform(shader, "HazeState", draw.emergence(), draw.strength(), draw.swallow(), draw.eyes());
					setUniform(shader, "HazeTint", ((draw.tint() >> 16) & 0xFF) / 255.0F,
							((draw.tint() >> 8) & 0xFF) / 255.0F, (draw.tint() & 0xFF) / 255.0F, draw.flare());
					setUniform(shader, "HazeLean", draw.trailX(), draw.trailY(), draw.trailZ(), draw.recall());
					setUniform(shader, "HazeCamera", haze.cameraX(), haze.cameraY(), haze.cameraZ());
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


	/** One staff haze type per seed, with an inside variant. */
	private record StaffHazeUniforms(DaemonHazeQueue.StaffDraw draw, float cameraX, float cameraY, float cameraZ) {
	}

	private static final SeededRenderTypes<HazeKey, StaffHazeUniforms, RenderType> CARDINAL_STAFF_HAZE_TYPES =
			new SeededRenderTypes<>(RITE_TYPE_CACHE_LIMIT, HemoRenderTypes::createCardinalStaffHaze);

	/** The haze curling off a planted staff; the camera position is relative to the haze box's centre. */
	public static RenderType cardinalStaffHaze(DaemonHazeQueue.StaffDraw draw, float cameraX, float cameraY,
			float cameraZ) {
		boolean inside = DaemonHaze.cameraInside(cameraX, cameraY, cameraZ, DaemonHaze.STAFF_HAZE_HALF_WIDTH,
				DaemonHaze.STAFF_HAZE_HALF_HEIGHT);
		return CARDINAL_STAFF_HAZE_TYPES.get(new HazeKey(draw.seed(), inside),
				new StaffHazeUniforms(draw, cameraX, cameraY, cameraZ));
	}

	private static RenderType createCardinalStaffHaze(HazeKey key, Supplier<StaffHazeUniforms> values) {
		boolean inside = key.inside();
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"cardinal_staff_haze_uniforms",
				() -> {
					ShaderInstance shader = ShaderInit.CARDINAL_STAFF_HAZE.getInstance().get();
					StaffHazeUniforms haze = values.get();
					DaemonHazeQueue.StaffDraw draw = haze.draw();
					setUniform(shader, "HemoTime", draw.time());
					setUniform(shader, "HazeSeed", draw.seed());
					setUniform(shader, "HazeState", draw.emergence(), draw.strength(), 0.0F, 0.0F);
					setUniform(shader, "HazeCamera", haze.cameraX(), haze.cameraY(), haze.cameraZ());
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
		return SILENT_ARCHON_VOLUMETRIC_FOG_TYPES.get(new SilentArchonVolumetricFogKey(texture, fogSeed),
			new SilentArchonVolumetricFogUniforms(gameTime, fogLayer, fogDensity));
	}

	private record SilentArchonVolumetricFogUniforms(float gameTime, float fogLayer, float fogDensity) {
	}

	private record SilentArchonVolumetricFogKey(ResourceLocation texture, float fogSeed) {
	}

	private static final SeededRenderTypes<SilentArchonVolumetricFogKey, SilentArchonVolumetricFogUniforms, RenderType> SILENT_ARCHON_VOLUMETRIC_FOG_TYPES =
			new SeededRenderTypes<>(SEEDED_TYPE_LIMIT, HemoRenderTypes::createSilentArchonVolumetricFog);

	private static RenderType createSilentArchonVolumetricFog(SilentArchonVolumetricFogKey key, Supplier<SilentArchonVolumetricFogUniforms> values) {
		ResourceLocation texture = key.texture();
		float fogSeed = key.fogSeed();
		RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
				"silent_archon_fog_uniforms",
				() -> {
					SilentArchonVolumetricFogUniforms u = values.get();
					float gameTime = u.gameTime();
					float fogLayer = u.fogLayer();
					float fogDensity = u.fogDensity();
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

