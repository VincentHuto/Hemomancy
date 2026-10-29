package com.vincenthuto.hemomancy.client.render.shader;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.vincenthuto.hemomancy.Hemomancy;
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

	public static ShaderHolder SANGUINE_OMEN_WORLD = new ShaderHolder(Hemomancy.rloc("screen/sanguine_omen_overlay"),
			DefaultVertexFormat.POSITION_TEX_COLOR, "HemoTime", "Progress", "Intensity", "Seed", "EyeMarker");

	public static ShaderHolder SANGUINE_OMEN_SCREEN_OVERLAY = new ShaderHolder(Hemomancy.rloc("screen/sanguine_omen_screen_overlay"),
			DefaultVertexFormat.POSITION_TEX_COLOR, "HemoTime", "Progress", "Intensity", "Seed");

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

	public static ShaderHolder LUX_GLINT = new ShaderHolder(Hemomancy.rloc("item/lux_glint"),
			DefaultVertexFormat.NEW_ENTITY);

	public static ShaderHolder BLOOD_STRUCTURE_WARP = new ShaderHolder(Hemomancy.rloc("world/blood_structure_warp"),
			DefaultVertexFormat.BLOCK, "HemoTime", "Progress", "BlockSeed", "WiggleAmp", "FillLine");

	public static ShaderHolder BLOOD_INFUSION_VEINS = new ShaderHolder(Hemomancy.rloc("world/blood_infusion_veins"),
			DefaultVertexFormat.BLOCK, "HemoTime", "Progress", "BlockSeed", "FinalizeProgress", "CrackShare",
			"VeinOrigin", "VeinNormal", "VeinAnchor");

	/** The consecrated ward over a Saint's mausoleum: runes on the stone, the threshold sheet, strikes. */
	public static ShaderHolder MAUSOLEUM_WARD = new ShaderHolder(Hemomancy.rloc("world/mausoleum_ward"),
			DefaultVertexFormat.BLOCK, "HemoTime", "WardColor", "WardRadius", "PlayerOffset", "FocusOffset",
			"Strike0", "Strike1", "Strike2", "Strike3", "StrikePower", "StrikeKind", "WardBody");

	/** The same ward burning into an Apotheos on hallowed ground, drawn over the player model. */
	public static ShaderHolder MAUSOLEUM_WARD_BODY = new ShaderHolder(Hemomancy.rloc("world/mausoleum_ward"),
			DefaultVertexFormat.NEW_ENTITY, "HemoTime", "WardBody");

	public static ShaderHolder BLOOD_STRUCTURE_WARP_ENTITY = new ShaderHolder(
			Hemomancy.rloc("world/blood_structure_warp"),
			DefaultVertexFormat.NEW_ENTITY, "HemoTime", "Progress", "BlockSeed", "WiggleAmp",
			"WarpCenter", "FinalizeProgress", "MeltGroundY", "MeltHeight", "FillLine");

	public static ShaderHolder VESPER_WOUND_DRAW = new ShaderHolder(Hemomancy.rloc("entity/vesper_wound_draw"),
			DefaultVertexFormat.NEW_ENTITY, "HemoTime", "WoundCenter", "BodyYaw", "DrainProgress", "DrainFlash",
			"DrawSeed");

	public static ShaderHolder LOOM_ORB = new ShaderHolder(Hemomancy.rloc("world/loom_orb"),
			DefaultVertexFormat.POSITION_COLOR, "HemoTime", "OrbSeed", "OrbCenter", "OrbRadius", "WritheStrength",
			"ThreadScale", "GlowLayer");

	public static ShaderHolder MYCELIAL_CRUCIBLE_BASIN = new ShaderHolder(Hemomancy.rloc("world/mycelial_crucible_basin"),
			DefaultVertexFormat.POSITION_TEX_COLOR, "HemoTime", "BasinSeed", "SwirlIntensity");

	public static ShaderHolder BLOOD_POOL = new ShaderHolder(Hemomancy.rloc("world/blood_pool"),
			DefaultVertexFormat.POSITION_TEX_COLOR, "HemoTime", "PoolSeed", "Ragged", "Square");

	/** Annetta's spilled infected blood: the blood pool's surface with ichor veins through it and a ragged splash edge. */
	public static ShaderHolder INFECTED_POOL = new ShaderHolder(Hemomancy.rloc("world/infected_pool"),
			DefaultVertexFormat.POSITION_TEX_COLOR, "HemoTime", "PoolSeed");

	/** The Latent Infection's living skin: crawling, heart-pumped ichor veins and a wet rim over its flesh. */
	public static ShaderHolder LATENT_INFECTION_SKIN = new ShaderHolder(Hemomancy.rloc("entity/latent_infection_skin"),
			DefaultVertexFormat.NEW_ENTITY, "HemoTime");

	public static ShaderHolder ENGRAM_SIGIL = new ShaderHolder(Hemomancy.rloc("world/engram_sigil"),
			DefaultVertexFormat.POSITION_TEX_COLOR, "HemoTime", "SigilSeed", "SigilLayer", "SigilRadius", "Unfurl",
			"Withdraw", "Beat", "Pressure", "Keyed", "Rotation", "Turns", "Dim", "FloorLight", "AimPoint");

	public static ShaderHolder CARDINAL_FLOOR_MEMBRANE = new ShaderHolder(Hemomancy.rloc("world/cardinal_floor_membrane"),
			DefaultVertexFormat.POSITION_TEX_COLOR, "HemoTime", "FloorSeed", "FloorStyle", "FloorHalf", "Spread",
			"Front", "Beat", "FloorLight", "FocusOffset", "BaseColor", "VeinColor", "GlowColor",
			"RiteRings", "RiteSurge", "RiteDraw", "RiteMood", "RiteSwell", "WoundAngles", "WoundLevels", "RiteBlood");

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

	public static ShaderHolder MANIPULATION_LUX = new ShaderHolder(Hemomancy.rloc("world/manipulation_lux"),
			DefaultVertexFormat.POSITION_TEX_COLOR, "HemoTime", "ColorModulator");

	public static ShaderHolder MANIPULATION_UMBRA = new ShaderHolder(Hemomancy.rloc("world/manipulation_umbra"),
			DefaultVertexFormat.POSITION_TEX_COLOR, "HemoTime", "ColorModulator");

    public static ShaderHolder AXONAL_TRAVEL_VIEW = new ShaderHolder(Hemomancy.rloc("screen/axonal_travel_view"),
            DefaultVertexFormat.POSITION_TEX_COLOR, "HemoTime", "ColorModulator");
    public static ShaderHolder MANIPULATION_ANIMUS = thermal("animus");
    public static ShaderHolder MANIPULATION_ANIMUS_MODEL = new ShaderHolder(Hemomancy.rloc("world/manipulation_animus_model"),
            DefaultVertexFormat.NEW_ENTITY, "HemoTime", "ColorModulator");
    public static ShaderHolder MANIPULATION_MORTEM = thermal("mortem");
    public static ShaderHolder MANIPULATION_PUTRICIEL_ROT = thermal("putriciel_rot");
    public static ShaderHolder MANIPULATION_INFECTION = new ShaderHolder(Hemomancy.rloc("world/manipulation_infection"),
            DefaultVertexFormat.NEW_ENTITY, "HemoTime", "ColorModulator");
    public static ShaderHolder MANIPULATION_FERRIC = thermal("ferric");
    public static ShaderHolder MANIPULATION_DUCTILIS = thermal("ductilis");
    public static ShaderHolder MANIPULATION_FLAME = thermal("flame");
    public static ShaderHolder MANIPULATION_MOLTEN = thermal("molten");
    public static ShaderHolder MANIPULATION_GLASS = thermal("glass");
    public static ShaderHolder MANIPULATION_CRUOR = thermal("cruor");
    public static ShaderHolder MANIPULATION_RIME = thermal("rime");
    public static ShaderHolder MANIPULATION_SKIN = new ShaderHolder(Hemomancy.rloc("world/manipulation_skin"),
            DefaultVertexFormat.NEW_ENTITY, "ColorModulator");

    /** Cast outpouring veins on the player model; the body atlas rides Sampler0. */
    public static ShaderHolder CAST_VEINS = new ShaderHolder(Hemomancy.rloc("entity/cast_veins"),
            DefaultVertexFormat.NEW_ENTITY, "HemoTime", "ColorModulator");

    /** Depth-reading full-screen passes: Lux outpouring light and Tenebris outpouring darkness. */
    public static ShaderHolder CAST_LUMEN = new ShaderHolder(Hemomancy.rloc("screen/cast_lumen"),
            DefaultVertexFormat.POSITION_TEX, "HemoTime", "InvProj", "InvView", "LightsA", "LightsB", "Count");
    public static ShaderHolder CAST_UMBRA = new ShaderHolder(Hemomancy.rloc("screen/cast_umbra"),
            DefaultVertexFormat.POSITION_TEX, "HemoTime", "InvProj", "InvView", "LightsA", "LightsB", "Count", "SelfShade",
            "ShadeInfo");

    private static ShaderHolder thermal(String material) {
        return new ShaderHolder(Hemomancy.rloc("world/manipulation_" + material),
                DefaultVertexFormat.POSITION_TEX_COLOR, material.equals("glass")?
                        new String[]{"ColorModulator"}:new String[]{"HemoTime", "ColorModulator"});
    }

	public static ShaderHolder CARDINAL_RITE_FOG = new ShaderHolder(Hemomancy.rloc("world/cardinal_rite_fog"),
			DefaultVertexFormat.POSITION_TEX_COLOR, "HemoTime", "FogSeed");
	public static ShaderHolder CARDINAL_RITE_SKY_WOUND = new ShaderHolder(Hemomancy.rloc("world/cardinal_rite_sky_wound"),
			DefaultVertexFormat.POSITION_TEX_COLOR, "HemoTime", "WoundSeed", "WoundShape", "BaseColor", "VeinColor",
			"GlowColor");
	public static ShaderHolder CARDINAL_DAEMON_HAZE = new ShaderHolder(Hemomancy.rloc("world/cardinal_daemon_haze"),
			DefaultVertexFormat.POSITION_TEX_COLOR, "HemoTime", "HazeSeed", "HazeState", "HazeTint", "HazeLean", "HazeCamera", "HazeShape", "HazeHead", "HazeTilt", "HazeFlow");
	public static ShaderHolder CARDINAL_STAFF_HAZE = new ShaderHolder(Hemomancy.rloc("world/cardinal_staff_haze"),
			DefaultVertexFormat.POSITION_TEX_COLOR, "HemoTime", "HazeSeed", "HazeState", "HazeCamera", "HazeShape");

	public static ShaderHolder SILENT_ARCHON_STORM_CLOUD = new ShaderHolder(Hemomancy.rloc("world/silent_archon_storm_cloud"),
			DefaultVertexFormat.POSITION_TEX_COLOR, "HemoTime", "CloudSeed", "CloudDensity");

	public static ShaderHolder QLIPHOTH_BLACK_HOLE = new ShaderHolder(Hemomancy.rloc("world/qliphoth_black_hole"),
			DefaultVertexFormat.POSITION_TEX_COLOR, "HemoTime", "HoleSeed", "LensStrength", "RingIntensity",
			"FinalHole", "ScreenSize");

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
        registerShader(event, SANGUINE_OMEN_WORLD.createInstance(provider));
        registerShader(event, AXONAL_TRAVEL_VIEW.createInstance(provider));
        registerShader(event, SANGUINE_OMEN_SCREEN_OVERLAY.createInstance(provider));
        registerShader(event, MONOLITH_FRAGMENT.createInstance(provider));
        registerShader(event, MONOLITH_FRAGMENT_ENTITY.createInstance(provider));
        registerShader(event, WILL_STATE_MONOLITH.createInstance(provider));
        registerShader(event, HERMIT_FAREWELL_DISSOLVE.createInstance(provider));
        registerShader(event, LUX_GLINT.createInstance(provider));
        registerShader(event, BLOOD_STRUCTURE_WARP.createInstance(provider));
        registerShader(event, BLOOD_STRUCTURE_WARP_ENTITY.createInstance(provider));
        registerShader(event, BLOOD_INFUSION_VEINS.createInstance(provider));
        registerShader(event, MAUSOLEUM_WARD.createInstance(provider));
        registerShader(event, MAUSOLEUM_WARD_BODY.createInstance(provider));
        registerShader(event, VESPER_WOUND_DRAW.createInstance(provider));
        registerShader(event, LOOM_ORB.createInstance(provider));
        registerShader(event, MYCELIAL_CRUCIBLE_BASIN.createInstance(provider));
        registerShader(event, BLOOD_POOL.createInstance(provider));
        registerShader(event, INFECTED_POOL.createInstance(provider));
        registerShader(event, LATENT_INFECTION_SKIN.createInstance(provider));
        registerShader(event, CARDINAL_FLOOR_MEMBRANE.createInstance(provider));
        registerShader(event, ENGRAM_SIGIL.createInstance(provider));
        registerShader(event, MNEMONIC_LOWTIDE_LAKE.createInstance(provider));
        registerShader(event, MNEMONIC_LOWTIDE_SKYBOX_BASE.createInstance(provider));
        registerShader(event, MNEMONIC_LOWTIDE_SKYBOX.createInstance(provider));
        registerShader(event, MNEMONIC_LOWTIDE_PARCHMENT.createInstance(provider));
        registerShader(event, SILENT_ARCHON_FOG.createInstance(provider));
        registerShader(event, CARDINAL_RITE_FOG.createInstance(provider));
        registerShader(event, CARDINAL_RITE_SKY_WOUND.createInstance(provider));
        registerShader(event, CARDINAL_DAEMON_HAZE.createInstance(provider));
        registerShader(event, CARDINAL_STAFF_HAZE.createInstance(provider));
        registerShader(event, MANIPULATION_LUX.createInstance(provider));
        registerShader(event, MANIPULATION_ANIMUS.createInstance(provider));
        registerShader(event, MANIPULATION_ANIMUS_MODEL.createInstance(provider));
        registerShader(event, MANIPULATION_MORTEM.createInstance(provider));
        registerShader(event, MANIPULATION_PUTRICIEL_ROT.createInstance(provider));
        registerShader(event, MANIPULATION_INFECTION.createInstance(provider));
        registerShader(event, MANIPULATION_FERRIC.createInstance(provider));
        registerShader(event, MANIPULATION_DUCTILIS.createInstance(provider));
        registerShader(event, MANIPULATION_UMBRA.createInstance(provider));
        registerShader(event, MANIPULATION_FLAME.createInstance(provider));
        registerShader(event, MANIPULATION_MOLTEN.createInstance(provider));
        registerShader(event, MANIPULATION_GLASS.createInstance(provider));
        registerShader(event, MANIPULATION_CRUOR.createInstance(provider));
        registerShader(event, MANIPULATION_RIME.createInstance(provider));
        registerShader(event, MANIPULATION_SKIN.createInstance(provider));
        registerShader(event, CAST_VEINS.createInstance(provider));
        registerShader(event, CAST_LUMEN.createInstance(provider));
        registerShader(event, CAST_UMBRA.createInstance(provider));
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
