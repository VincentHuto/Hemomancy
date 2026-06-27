package com.vincenthuto.hemomancy.common.init;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.shader.ExtendedShaderInstance;
import com.vincenthuto.hemomancy.client.render.shader.ShaderHolder;
import com.vincenthuto.hemomancy.client.render.tile.RadiantPortalRendertype;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;

import java.io.IOException;

@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)

public class ShaderInit {

	public static ShaderHolder DISTORTED_TEXTURE = new ShaderHolder(Hemomancy.rloc("screen/distorted_texture"),
			DefaultVertexFormat.POSITION_TEX_COLOR, "Speed", "TimeOffset", "Intensity", "XFrequency", "YFrequency",
			"UVCoordinates");

	public static ShaderHolder SANGUINE_OMEN_WORLD = new ShaderHolder(Hemomancy.rloc("screen/sanguine_omen_overlay"),
			DefaultVertexFormat.POSITION_TEX_COLOR, "HemoTime", "Progress", "Intensity", "Seed");

	public static ShaderHolder SANGUINE_OMEN_SCREEN_OVERLAY = new ShaderHolder(Hemomancy.rloc("screen/sanguine_omen_screen_overlay"),
			DefaultVertexFormat.POSITION_TEX_COLOR, "HemoTime", "Progress", "Intensity", "Seed");

	public static ShaderHolder SCAR_GLOW = new ShaderHolder(Hemomancy.rloc("screen/scar_glow"),
			DefaultVertexFormat.POSITION_TEX_COLOR, "Speed", "Intensity", "BorderWidth");

	public static ShaderHolder MONOLITH_FRAGMENT = new ShaderHolder(Hemomancy.rloc("item/monolith_fragment"),
			DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, "HemoTime", "ShardSeed", "Burden", "Attuned",
			"FractalScale");

	public static ShaderHolder MONOLITH_FRAGMENT_ENTITY = new ShaderHolder(Hemomancy.rloc("item/monolith_fragment"),
			DefaultVertexFormat.NEW_ENTITY, "HemoTime", "ShardSeed", "Burden", "Attuned",
			"FractalScale");

	public static ShaderHolder HERMIT_FAREWELL_DISSOLVE = new ShaderHolder(Hemomancy.rloc("entity/hermit_farewell_dissolve"),
			DefaultVertexFormat.NEW_ENTITY, "HemoTime", "HermitDissolveProgress", "HermitDissolveSeed");

	public static ShaderHolder BLOOD_STRUCTURE_WARP = new ShaderHolder(Hemomancy.rloc("world/blood_structure_warp"),
			DefaultVertexFormat.BLOCK, "HemoTime", "Progress", "BlockSeed", "WiggleAmp");

	public static ShaderHolder LOOM_ORB = new ShaderHolder(Hemomancy.rloc("world/loom_orb"),
			DefaultVertexFormat.POSITION_COLOR, "HemoTime", "OrbSeed", "OrbCenter", "OrbRadius", "WritheStrength",
			"ThreadScale", "GlowLayer");

	public static ShaderHolder MYCELIAL_CRUCIBLE_BASIN = new ShaderHolder(Hemomancy.rloc("world/mycelial_crucible_basin"),
			DefaultVertexFormat.POSITION_TEX_COLOR, "HemoTime", "BasinSeed", "SwirlIntensity");

	public static ShaderHolder SILENT_ARCHON_FOG = new ShaderHolder(Hemomancy.rloc("world/silent_archon_fog"),
			DefaultVertexFormat.POSITION_TEX_COLOR, "HemoTime", "FogSeed", "FogLayer", "FogDensity");

	public static ShaderHolder SILENT_ARCHON_STORM_CLOUD = new ShaderHolder(Hemomancy.rloc("world/silent_archon_storm_cloud"),
			DefaultVertexFormat.POSITION_TEX_COLOR, "HemoTime", "CloudSeed", "CloudDensity");

	public static ShaderHolder QLIPHOTH_BLACK_HOLE = new ShaderHolder(Hemomancy.rloc("world/qliphoth_black_hole"),
			DefaultVertexFormat.POSITION_TEX_COLOR, "HemoTime", "HoleSeed", "LensStrength", "RingIntensity",
			"FinalHole");

	@SubscribeEvent
	public static void register(RegisterShadersEvent event) throws IOException {
        ResourceProvider provider = event.getResourceProvider();

		event.registerShader(new ShaderInstance(event.getResourceProvider(), Hemomancy.rloc("water"),
				DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP), (instance) -> {
					RadiantPortalRendertype.WATER_SHADER = instance;
				});
		event.registerShader(new ShaderInstance(event.getResourceProvider(), Hemomancy.rloc("ray"),
				DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP), (instance) -> {
					RadiantPortalRendertype.RAY_SHADER = instance;
				});
        registerShader(event, DISTORTED_TEXTURE.createInstance(provider));
        registerShader(event, SANGUINE_OMEN_WORLD.createInstance(provider));
        registerShader(event, SANGUINE_OMEN_SCREEN_OVERLAY.createInstance(provider));
        registerShader(event, SCAR_GLOW.createInstance(provider));
        registerShader(event, MONOLITH_FRAGMENT.createInstance(provider));
        registerShader(event, MONOLITH_FRAGMENT_ENTITY.createInstance(provider));
        registerShader(event, HERMIT_FAREWELL_DISSOLVE.createInstance(provider));
        registerShader(event, BLOOD_STRUCTURE_WARP.createInstance(provider));
        registerShader(event, LOOM_ORB.createInstance(provider));
        registerShader(event, MYCELIAL_CRUCIBLE_BASIN.createInstance(provider));
        registerShader(event, SILENT_ARCHON_FOG.createInstance(provider));
        registerShader(event, SILENT_ARCHON_STORM_CLOUD.createInstance(provider));
        registerShader(event, QLIPHOTH_BLACK_HOLE.createInstance(provider));

	}
	   public static void registerShader(RegisterShadersEvent event, ExtendedShaderInstance extendedShaderInstance) {
	        event.registerShader(extendedShaderInstance, s -> {
	        });
	    }

}
