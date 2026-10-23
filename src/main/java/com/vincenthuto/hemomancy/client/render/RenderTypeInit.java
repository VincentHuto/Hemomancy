 package com.vincenthuto.hemomancy.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.shader.ShaderInit;

import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.systems.RenderSystem;
import org.joml.Matrix4f;

public class RenderTypeInit extends RenderType {

	public static final RenderType RADIANT_RENDER_TYPE = create("radiant", DefaultVertexFormat.POSITION_COLOR,
			VertexFormat.Mode.QUADS, 256, false, true,
			RenderType.CompositeState.builder().setShaderState(RENDERTYPE_LIGHTNING_SHADER)
					.setWriteMaskState(COLOR_WRITE).setTransparencyState(TRANSLUCENT_TRANSPARENCY)
					.setOutputState(PARTICLES_TARGET).setCullState(NO_CULL).setDepthTestState(LEQUAL_DEPTH_TEST)
					.createCompositeState(false));

	// Cardinal Rite boundary circle — renders inside the world, occluded by blocks
	public static final RenderType RITE_BOUNDARY_CORE = create("RiteBoundaryCore",
			DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 256, false, true,
			RenderType.CompositeState.builder()
					.setShaderState(RENDERTYPE_LIGHTNING_SHADER)
					.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
					.setDepthTestState(LEQUAL_DEPTH_TEST)
					.setWriteMaskState(COLOR_DEPTH_WRITE)
					.setCullState(NO_CULL)
					.setLightmapState(NO_LIGHTMAP)
					.createCompositeState(false));

	public static final RenderType RITE_BOUNDARY_GLOW = create("RiteBoundaryGlow",
			DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 256, false, true,
			RenderType.CompositeState.builder()
					.setShaderState(RENDERTYPE_LIGHTNING_SHADER)
					.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
					.setDepthTestState(LEQUAL_DEPTH_TEST)
					.setWriteMaskState(COLOR_WRITE)
					.setCullState(NO_CULL)
					.setLightmapState(NO_LIGHTMAP)
					.createCompositeState(false));

	/**
	 * Additive bloom over the rite boundary: heartbeat swell on the vessel rims and the bright beats (anchor
	 * pulses, seals, flushes). Colour-only, so it brightens what is behind it without hiding it.
	 */
	public static final RenderType RITE_BOUNDARY_BLOOM = create("RiteBoundaryBloom",
			DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 256, false, true,
			RenderType.CompositeState.builder()
					.setShaderState(RENDERTYPE_LIGHTNING_SHADER)
					.setTransparencyState(LIGHTNING_TRANSPARENCY)
					.setDepthTestState(LEQUAL_DEPTH_TEST)
					.setWriteMaskState(COLOR_WRITE)
					.setCullState(NO_CULL)
					.setLightmapState(NO_LIGHTMAP)
					.createCompositeState(false));

	// Unstained cardinal rite boundary: the same flat-colour material; the renderer supplies the white/ice palette.
	public static final RenderType UNSTAINED_RITE_BOUNDARY_CORE = RITE_BOUNDARY_CORE;

	public static final RenderType UNSTAINED_RITE_BOUNDARY_GLOW = RITE_BOUNDARY_GLOW;

	public static final RenderType MONOLITH_SHATTER_TRIANGLES = create("MonolithShatterTriangles",
			DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLES, 256, false, true,
			RenderType.CompositeState.builder()
					.setShaderState(RENDERTYPE_LIGHTNING_SHADER)
					.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
					.setDepthTestState(LEQUAL_DEPTH_TEST)
					.setWriteMaskState(COLOR_WRITE)
					.setCullState(NO_CULL)
					.setLightmapState(NO_LIGHTMAP)
					.createCompositeState(false));

	public static final RenderType CLAW_SLASH_GLOW = create("ClawSlashGlow",
			DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 512, false, true,
			RenderType.CompositeState.builder()
					.setShaderState(RENDERTYPE_LIGHTNING_SHADER)
					.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
					.setDepthTestState(LEQUAL_DEPTH_TEST)
					.setWriteMaskState(COLOR_WRITE)
					.setCullState(NO_CULL)
					.setLightmapState(NO_LIGHTMAP)
					.createCompositeState(false));

	public static final RenderType CLAW_SLASH_CORE = create("ClawSlashCore",
			DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 512, false, true,
			RenderType.CompositeState.builder()
					.setShaderState(RENDERTYPE_LIGHTNING_SHADER)
					.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
					.setDepthTestState(LEQUAL_DEPTH_TEST)
					.setWriteMaskState(COLOR_DEPTH_WRITE)
					.setCullState(NO_CULL)
					.setLightmapState(NO_LIGHTMAP)
					.createCompositeState(false));

	public static final RenderType LOOM_EFFECT = create("LoomEffect",
			DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 256, false, true,
			RenderType.CompositeState.builder()
					.setShaderState(RENDERTYPE_LIGHTNING_SHADER)
					.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
					.setDepthTestState(LEQUAL_DEPTH_TEST)
					.setWriteMaskState(COLOR_WRITE)
					.setCullState(NO_CULL)
					.setLightmapState(NO_LIGHTMAP)
					.createCompositeState(false));

	// Flask fluid level — flat colour, depth-tested against world but rendered before glass
	public static final RenderType FLASK_FLUID = create("FlaskFluid",
			DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 256, false, true,
			RenderType.CompositeState.builder()
					.setShaderState(RENDERTYPE_LIGHTNING_SHADER)
					.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
					.setDepthTestState(LEQUAL_DEPTH_TEST)
					.setWriteMaskState(COLOR_DEPTH_WRITE)
					.setCullState(NO_CULL)
					.setLightmapState(NO_LIGHTMAP)
					.createCompositeState(false));

	public static final RenderType INCUBATOR_FLUID = create("IncubatorFluid",
			DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 256, false, true,
			RenderType.CompositeState.builder()
					.setShaderState(RENDERTYPE_LIGHTNING_SHADER)
					.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
					.setDepthTestState(LEQUAL_DEPTH_TEST)
					.setWriteMaskState(COLOR_WRITE)
					.setCullState(NO_CULL)
					.setLightmapState(NO_LIGHTMAP)
					.createCompositeState(false));

    public static final RenderType SCRIPTORIUM_ARMOR_ENTITY_GLINT = create(
        "hemomancy_scriptorium_armor_entity_glint",
        DefaultVertexFormat.POSITION_TEX,
        VertexFormat.Mode.QUADS,
        1536,
        RenderType.CompositeState.builder()
            .setShaderState(RENDERTYPE_ARMOR_ENTITY_GLINT_SHADER)
            .setTextureState(new RenderStateShard.TextureStateShard(Hemomancy.rloc("textures/item/crimson_item_glint.png"), true, false))
            .setWriteMaskState(COLOR_WRITE)
            .setCullState(NO_CULL)
            .setDepthTestState(EQUAL_DEPTH_TEST)
            .setTransparencyState(GLINT_TRANSPARENCY)
            .setTexturingState(ENTITY_GLINT_TEXTURING)
            .setLayeringState(VIEW_OFFSET_Z_LAYERING)
            .createCompositeState(false)
    );
    public static final RenderType SCRIPTORIUM_GLINT_TRANSLUCENT = create(
        "hemomancy_scriptorium_glint_translucent",
        DefaultVertexFormat.POSITION_TEX,
        VertexFormat.Mode.QUADS,
        1536,
        RenderType.CompositeState.builder()
            .setShaderState(RENDERTYPE_GLINT_TRANSLUCENT_SHADER)
            .setTextureState(new RenderStateShard.TextureStateShard(Hemomancy.rloc("textures/item/crimson_item_glint.png"), true, false))
            .setWriteMaskState(COLOR_WRITE)
            .setCullState(NO_CULL)
            .setDepthTestState(EQUAL_DEPTH_TEST)
            .setTransparencyState(GLINT_TRANSPARENCY)
            .setTexturingState(GLINT_TEXTURING)
            .setOutputState(ITEM_ENTITY_TARGET)
            .createCompositeState(false)
    );
    public static final RenderType SCRIPTORIUM_GLINT = create(
        "hemomancy_scriptorium_glint",
        DefaultVertexFormat.POSITION_TEX,
        VertexFormat.Mode.QUADS,
        1536,
        RenderType.CompositeState.builder()
            .setShaderState(RENDERTYPE_GLINT_SHADER)
            .setTextureState(new RenderStateShard.TextureStateShard(Hemomancy.rloc("textures/item/crimson_item_glint.png"), true, false))
            .setWriteMaskState(COLOR_WRITE)
            .setCullState(NO_CULL)
            .setDepthTestState(EQUAL_DEPTH_TEST)
            .setTransparencyState(GLINT_TRANSPARENCY)
            .setTexturingState(GLINT_TEXTURING)
            .createCompositeState(false)
    );
    public static final RenderType SCRIPTORIUM_ENTITY_GLINT = create(
        "hemomancy_scriptorium_entity_glint",
        DefaultVertexFormat.POSITION_TEX,
        VertexFormat.Mode.QUADS,
        1536,
        RenderType.CompositeState.builder()
            .setShaderState(RENDERTYPE_ENTITY_GLINT_SHADER)
            .setTextureState(new RenderStateShard.TextureStateShard(Hemomancy.rloc("textures/item/crimson_item_glint.png"), true, false))
            .setWriteMaskState(COLOR_WRITE)
            .setCullState(NO_CULL)
            .setDepthTestState(EQUAL_DEPTH_TEST)
            .setTransparencyState(GLINT_TRANSPARENCY)
            .setOutputState(ITEM_ENTITY_TARGET)
            .setTexturingState(ENTITY_GLINT_TEXTURING)
            .createCompositeState(false)
    );
    public static final RenderType SCRIPTORIUM_ENTITY_GLINT_DIRECT = create(
        "hemomancy_scriptorium_entity_glint_direct",
        DefaultVertexFormat.POSITION_TEX,
        VertexFormat.Mode.QUADS,
        1536,
        RenderType.CompositeState.builder()
            .setShaderState(RENDERTYPE_ENTITY_GLINT_DIRECT_SHADER)
            .setTextureState(new RenderStateShard.TextureStateShard(Hemomancy.rloc("textures/item/crimson_item_glint.png"), true, false))
            .setWriteMaskState(COLOR_WRITE)
            .setCullState(NO_CULL)
            .setDepthTestState(EQUAL_DEPTH_TEST)
            .setTransparencyState(GLINT_TRANSPARENCY)
            .setTexturingState(ENTITY_GLINT_TEXTURING)
            .createCompositeState(false)
    );

    public static RenderType scriptoriumGlint(RenderType type) {
        if (type == RenderType.glint()) return SCRIPTORIUM_GLINT;
        if (type == RenderType.glintTranslucent()) return SCRIPTORIUM_GLINT_TRANSLUCENT;
        if (type == RenderType.entityGlint()) return SCRIPTORIUM_ENTITY_GLINT;
        if (type == RenderType.entityGlintDirect()) return SCRIPTORIUM_ENTITY_GLINT_DIRECT;
        if (type == RenderType.armorEntityGlint()) return SCRIPTORIUM_ARMOR_ENTITY_GLINT;
        return type;
    }

	private static final RenderType CRIMSON_GLINT = create("hemomancy_crimson_entity_glint", DefaultVertexFormat.NEW_ENTITY,
			VertexFormat.Mode.QUADS, 256, false, false,
			RenderType.CompositeState.builder().setShaderState(RENDERTYPE_ENERGY_SWIRL_SHADER)
					.setTextureState(new RenderStateShard.TextureStateShard(
							Hemomancy.rloc("textures/item/crimson_item_glint.png"), true, false))
					.setWriteMaskState(COLOR_WRITE).setCullState(NO_CULL).setDepthTestState(EQUAL_DEPTH_TEST)
					.setTransparencyState(GLINT_TRANSPARENCY).setTexturingState(ENTITY_GLINT_TEXTURING)
					.setLightmapState(LIGHTMAP).setOverlayState(OVERLAY)
					.createCompositeState(false));
	private static final RenderType LUX_GLINT = create("hemomancy_lux_entity_glint", DefaultVertexFormat.NEW_ENTITY,
			VertexFormat.Mode.QUADS, 256, false, false,
			RenderType.CompositeState.builder().setShaderState(ShaderInit.LUX_GLINT.getShard())
					.setWriteMaskState(COLOR_WRITE).setCullState(NO_CULL).setDepthTestState(EQUAL_DEPTH_TEST)
					.setTransparencyState(LIGHTNING_TRANSPARENCY).setTexturingState(ENTITY_GLINT_TEXTURING)
					.setLightmapState(LIGHTMAP).setOverlayState(OVERLAY)
					.createCompositeState(false));

	public static RenderType energySwirl(ResourceLocation pLocation, float pU, float pV) {
		return ENERGY_SWIRL_TYPES.get(pLocation, new SwirlOffset(pU, pV));
	}

	private record SwirlOffset(float u, float v) {
	}

	/** One swirl type per texture; its scroll offset is set the way {@code OffsetTexturingStateShard} sets it. */
	private static final SeededRenderTypes<ResourceLocation, SwirlOffset, RenderType> ENERGY_SWIRL_TYPES =
			new SeededRenderTypes<>(64, (pLocation, offset) -> create("energy_swirl", DefaultVertexFormat.NEW_ENTITY,
					VertexFormat.Mode.QUADS, 256, false, true,
					RenderType.CompositeState.builder().setShaderState(RENDERTYPE_ENERGY_SWIRL_SHADER)
							.setTextureState(new RenderStateShard.TextureStateShard(pLocation, false, false))
							.setTexturingState(new RenderStateShard.TexturingStateShard("offset_texturing",
									() -> RenderSystem.setTextureMatrix(
											new Matrix4f().translation(offset.get().u(), offset.get().v(), 0.0F)),
									RenderSystem::resetTextureMatrix))
							.setTransparencyState(ADDITIVE_TRANSPARENCY).setCullState(NO_CULL).setLightmapState(LIGHTMAP)
							.setOverlayState(OVERLAY).createCompositeState(false)));

	public static RenderType getCrimsonGlint() {
		return CRIMSON_GLINT;
	}

	public static RenderType getLuxGlint() {
		return LUX_GLINT;
	}

	public RenderTypeInit(String nameIn, VertexFormat formatIn, Mode drawModeIn, int bufferSizeIn,
			boolean useDelegateIn, boolean needsSortingIn, Runnable setupTaskIn, Runnable clearTaskIn) {
		super(nameIn, formatIn, drawModeIn, bufferSizeIn, useDelegateIn, needsSortingIn, setupTaskIn, clearTaskIn);
	}

}
