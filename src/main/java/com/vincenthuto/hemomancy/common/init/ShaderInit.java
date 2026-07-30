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

	public static ShaderHolder WILL_STATE_MONOLITH = new ShaderHolder(Hemomancy.rloc("item/will_state_monolith"),
			DefaultVertexFormat.NEW_ENTITY, "HemoTime", "ShardSeed", "Burden", "Attuned",
			"FractalScale", "WillMonolithColor");

	public static ShaderHolder HERMIT_FAREWELL_DISSOLVE = new ShaderHolder(Hemomancy.rloc("entity/hermit_farewell_dissolve"),
			DefaultVertexFormat.NEW_ENTITY, "HemoTime", "HermitDissolveProgress", "HermitDissolveSeed");

	public static ShaderHolder BLOOD_STRUCTURE_WARP = new ShaderHolder(Hemomancy.rloc("world/blood_structure_warp"),
			DefaultVertexFormat.BLOCK, "HemoTime", "Progress", "BlockSeed", "WiggleAmp");

	public static ShaderHolder LOOM_ORB = new ShaderHolder(Hemomancy.rloc("world/loom_orb"),
			DefaultVertexFormat.POSITION_COLOR, "HemoTime", "OrbSeed", "OrbCenter", "OrbRadius", "WritheStrength",
			"ThreadScale", "GlowLayer");

	public static ShaderHolder MYCELIAL_CRUCIBLE_BASIN = new ShaderHolder(Hemomancy.rloc("world/mycelial_crucible_basin"),
			DefaultVertexFormat.POSITION_TEX_COLOR, "HemoTime", "BasinSeed", "SwirlIntensity");

	public static ShaderHolder MNEMONIC_LOWTIDE_LAKE = new ShaderHolder(Hemomancy.rloc("world/mnemonic_lowtide_lake"),
			DefaultVertexFormat.POSITION_TEX_COLOR, "HemoTime", "LakeSeed", "WaveStrength", "WaveDetailScale",
			"NoiseScale", "GlossStrength", "EdgeFade");

	public static ShaderHolder MNEMONIC_LOWTIDE_SKYBOX_BASE = new ShaderHolder(Hemomancy.rloc("world/mnemonic_lowtide_skybox_base"),
			DefaultVertexFormat.POSITION_TEX_COLOR, "HemoTime", "FaceSeed", "CoverageBias", "NoduleScale",
			"VeinIntensity", "BaseIntensity");

	public static ShaderHolder MNEMONIC_LOWTIDE_SKYBOX = new ShaderHolder(Hemomancy.rloc("world/mnemonic_lowtide_skybox"),
			DefaultVertexFormat.POSITION_TEX_COLOR, "HemoTime", "FaceSeed", "CoverageBias", "TunnelScale",
			"BubbleScale", "TendrilIntensity");

	public static ShaderHolder MNEMONIC_LOWTIDE_PARCHMENT = new ShaderHolder(
			Hemomancy.rloc("world/mnemonic_lowtide_parchment"), DefaultVertexFormat.POSITION_TEX_COLOR,
			"HemoTime", "ParchmentSeed", "WindRippleStrength", "WindRippleScale", "WindDirection");

	public static ShaderHolder SILENT_ARCHON_FOG = new ShaderHolder(Hemomancy.rloc("world/silent_archon_fog"),
			DefaultVertexFormat.POSITION_TEX_COLOR, "HemoTime", "FogSeed", "FogLayer", "FogDensity");

	public static ShaderHolder CARDINAL_RITE_FOG = new ShaderHolder(Hemomancy.rloc("world/cardinal_rite_fog"),
			DefaultVertexFormat.POSITION_TEX_COLOR, "HemoTime", "FogSeed");

	public static ShaderHolder SILENT_ARCHON_STORM_CLOUD = new ShaderHolder(Hemomancy.rloc("world/silent_archon_storm_cloud"),
			DefaultVertexFormat.POSITION_TEX_COLOR, "HemoTime", "CloudSeed", "CloudDensity");

	public static ShaderHolder QLIPHOTH_BLACK_HOLE = new ShaderHolder(Hemomancy.rloc("world/qliphoth_black_hole"),
			DefaultVertexFormat.POSITION_TEX_COLOR, "HemoTime", "HoleSeed", "LensStrength", "RingIntensity",
			"FinalHole");

	public static ShaderHolder APOTHEOS_FLOOR_FUNNEL = new ShaderHolder(Hemomancy.rloc("world/apotheos_floor_funnel"),
			DefaultVertexFormat.POSITION_TEX_COLOR, "HemoTime", "FunnelSeed", "RingRise", "RingSpeed",
			"MeatNoiseScale", "HighlightIntensity", "CenterVoidRadius");

	public static ShaderHolder APOTHEOS_WALL_MEMBRANE = new ShaderHolder(
			Hemomancy.rloc("world/apotheos_wall_membrane"), DefaultVertexFormat.POSITION_TEX_COLOR,
			"HemoTime", "WallSeed", "FiberScale", "TraceIntensity", "RedGlowIntensity", "CeilingFadeStart",
			"CeilingFadeEnd");

	public static ShaderHolder APOTHEOS_CEILING_CORE = new ShaderHolder(
			Hemomancy.rloc("world/apotheos_ceiling_core"), DefaultVertexFormat.POSITION_TEX_COLOR,
			"HemoTime", "CeilingSeed", "CoreNoiseScale", "RotationSpeed", "YellowGlowIntensity",
			"GreenOrbIntensity", "CoreUndulationIntensity");

	public static ShaderHolder APOTHEOS_CEILING_ATMOSPHERE = new ShaderHolder(
			Hemomancy.rloc("world/apotheos_ceiling_atmosphere"), DefaultVertexFormat.POSITION_TEX_COLOR,
			"HemoTime", "CeilingSeed", "AtmosphereNoiseScale", "RotationSpeed", "StormIntensity",
			"AtmosphereOpacity");

	public static ShaderHolder APOTHEOS_WALL_TOP_RIM = new ShaderHolder(
			Hemomancy.rloc("world/apotheos_wall_top_rim"), DefaultVertexFormat.POSITION_TEX_COLOR,
			"HemoTime", "RimSeed", "RimPulseSpeed", "RimCoreIntensity", "RimGlowIntensity");

	public static ShaderHolder APOTHEOS_PORTAL_GLOW = new ShaderHolder(Hemomancy.rloc("world/apotheos_portal_glow"),
			DefaultVertexFormat.POSITION_TEX_COLOR, "HemoTime", "GlowSeed", "GlowIntensity", "GlowRadius",
			"CenterVoidRadius");

	public static ShaderHolder APOTHEOS_PORTAL_HAZE = new ShaderHolder(Hemomancy.rloc("world/apotheos_portal_haze"),
			DefaultVertexFormat.POSITION_TEX_COLOR, "HemoTime", "HazeSeed", "OutwardSpeed", "HazeIntensity",
			"CenterVoidRadius");

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
        registerShader(event, WILL_STATE_MONOLITH.createInstance(provider));
        registerShader(event, HERMIT_FAREWELL_DISSOLVE.createInstance(provider));
        registerShader(event, BLOOD_STRUCTURE_WARP.createInstance(provider));
        registerShader(event, LOOM_ORB.createInstance(provider));
        registerShader(event, MYCELIAL_CRUCIBLE_BASIN.createInstance(provider));
        registerShader(event, MNEMONIC_LOWTIDE_LAKE.createInstance(provider));
        registerShader(event, MNEMONIC_LOWTIDE_SKYBOX_BASE.createInstance(provider));
        registerShader(event, MNEMONIC_LOWTIDE_SKYBOX.createInstance(provider));
        registerShader(event, MNEMONIC_LOWTIDE_PARCHMENT.createInstance(provider));
        registerShader(event, SILENT_ARCHON_FOG.createInstance(provider));
        registerShader(event, CARDINAL_RITE_FOG.createInstance(provider));
        registerShader(event, SILENT_ARCHON_STORM_CLOUD.createInstance(provider));
        registerShader(event, QLIPHOTH_BLACK_HOLE.createInstance(provider));
        registerShader(event, APOTHEOS_FLOOR_FUNNEL.createInstance(provider));
        registerShader(event, APOTHEOS_WALL_MEMBRANE.createInstance(provider));
        registerShader(event, APOTHEOS_CEILING_CORE.createInstance(provider));
        registerShader(event, APOTHEOS_CEILING_ATMOSPHERE.createInstance(provider));
        registerShader(event, APOTHEOS_WALL_TOP_RIM.createInstance(provider));
        registerShader(event, APOTHEOS_PORTAL_GLOW.createInstance(provider));
        registerShader(event, APOTHEOS_PORTAL_HAZE.createInstance(provider));

	}
	   public static void registerShader(RegisterShadersEvent event, ExtendedShaderInstance extendedShaderInstance) {
	        event.registerShader(extendedShaderInstance, s -> {
	        });
	    }

}
