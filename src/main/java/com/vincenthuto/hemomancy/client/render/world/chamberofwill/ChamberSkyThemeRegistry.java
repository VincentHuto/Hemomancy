package com.vincenthuto.hemomancy.client.render.world.chamberofwill;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.data.ChamberOfWillClientData;
import com.vincenthuto.hemomancy.client.data.VesperFightClientData;
import com.vincenthuto.hemomancy.client.data.MycophantFightClientData;
import com.vincenthuto.hemomancy.common.worldgen.ChamberOfWillManager;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ChamberSkyThemeRegistry {
	public static final ResourceLocation DEFAULT_SKY =
			Hemomancy.rloc("textures/environment/chamber_of_will_sky.png");
	public static final ResourceLocation DEFAULT_CLOUDS =
			Hemomancy.rloc("textures/environment/chamber_of_will_clouds.png");
	public static final ResourceLocation DEFAULT_WISP =
			Hemomancy.rloc("textures/environment/single_cloud.png");
	public static final ResourceLocation DEFAULT_NOISE =
			Hemomancy.rloc("textures/environment/noise_tile.png");
	public static final ResourceLocation SILENT_ARCHON_SKY =
			Hemomancy.rloc("textures/environment/silent_archon_sky.png");
	public static final ResourceLocation SILENT_ARCHON_CLOUDS =
			Hemomancy.rloc("textures/environment/silent_archon_clouds.png");
	public static final ResourceLocation QLIPHOTH_COMMUNION_SKY =
			Hemomancy.rloc("textures/environment/qliphoth_communion_sky.png");
	public static final ResourceLocation QLIPHOTH_COMMUNION_CLOUDS =
			Hemomancy.rloc("textures/environment/qliphoth_communion_clouds.png");

	private static final Map<ResourceLocation, ChamberSkyTheme> THEMES = new LinkedHashMap<>();
	private static final Map<ResourceLocation, ChamberThemeEffects> EFFECTS = new LinkedHashMap<>();
	public static final ChamberSkyTheme DEFAULT = ChamberSkyTheme.builder(ChamberOfWillManager.THEME_WILL_DEFAULT)
			.skybox(0xFF454545, 0xFF808080)
			.nebula(0x362E00, 0x801A00, 0x2E2600)
			.tints(0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF)
			.pulse(1.0F)
			.layers(1, 2, 2, 3)
			.build();

	static {
		register(DEFAULT, new WillDefaultChamberEffects(DEFAULT));
		ChamberSkyTheme mnemonicLowtide = ChamberSkyTheme.builder(ChamberOfWillManager.THEME_MNEMONIC_LOWTIDE)
				.skybox(0xFF1B0606, 0xFF1B0606)
				.nebula(0x000000, 0x000000, 0x000000)
				.tints(0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF)
				.pulse(0.0F)
				.motion(1.0F)
				.layers(0, 0, 0, 0)
				.toggles(true, false, false, false)
				.build();
		register(mnemonicLowtide, new MnemonicLowtideChamberEffects(mnemonicLowtide));
		ChamberSkyTheme archonRevelation = ChamberSkyTheme.builder(ChamberOfWillManager.THEME_ARCHON_REVELATION)
				.skybox(0xFF3D3338, 0xFF8A6E72)
				.nebula(0x3E2606, 0x8C1E16, 0x5B3A10)
				.tints(0xFFE8D8, 0xFFD5EE, 0xC9D7FF, 0xFFE3A8)
				.pulse(1.15F)
				.motion(1.08F)
				.layers(2, 2, 2, 3)
				.build();
		register(archonRevelation, new ArchonRevelationChamberEffects(archonRevelation));
		ChamberSkyTheme qliphothCommunion = ChamberSkyTheme.builder(ChamberOfWillManager.THEME_QLIPHOTH_COMMUNION)
				.textures(QLIPHOTH_COMMUNION_SKY, QLIPHOTH_COMMUNION_CLOUDS, DEFAULT_WISP, DEFAULT_NOISE)
				.skybox(0xFF2C2635, 0xFF6D5F86)
				.nebula(0x271238, 0x4C1138, 0x64510D)
				.tints(0xD8B6FF, 0xE39DFF, 0xB8C7FF, 0xFFE88D)
				.pulse(1.35F)
				.motion(0.92F)
				.layers(2, 1, 3, 0)
				.build();
		register(qliphothCommunion, new QliphothCommunionChamberEffects(qliphothCommunion));
		ChamberSkyTheme silentArchon = ChamberSkyTheme.builder(ChamberOfWillManager.THEME_SILENT_ARCHON)
				.textures(SILENT_ARCHON_SKY, SILENT_ARCHON_CLOUDS, SILENT_ARCHON_CLOUDS, DEFAULT_NOISE)
				.skybox(0xFF5F7472, 0xFF8CA6A2)
				.nebula(0x223031, 0x33484A, 0x55706D)
				.tints(0xAEB8B8, 0x6E3337, 0x798992, 0xA8AAA0)
				.pulse(0.18F)
				.motion(0.36F)
				.layers(0, 0, 0, 0)
				.monolithPillars(16)
				.toggles(true, false, true, false)
				.build();
		register(silentArchon, new SilentArchonChamberEffects(silentArchon));
		ChamberSkyTheme vesperFight = ChamberSkyTheme.builder(ChamberOfWillManager.THEME_VESPER_FIGHT)
				.skybox(0xFF000000, 0xFF000000)
				.nebula(0x000000, 0x000000, 0x000000)
				.tints(0x000000, 0x000000, 0x000000, 0x000000)
				.pulse(0.0F)
				.motion(0.0F)
				.layers(0, 0, 0, 0)
				.toggles(true, false, false, false)
				.build();
		register(vesperFight, new VesperFightChamberEffects(vesperFight));
		ChamberSkyTheme mycophantNursery = ChamberSkyTheme.builder(ChamberOfWillManager.THEME_MYCOPHANT_NURSERY)
				.textures(QLIPHOTH_COMMUNION_SKY, QLIPHOTH_COMMUNION_CLOUDS, DEFAULT_WISP, DEFAULT_NOISE)
				.skybox(0xFF071109, 0xFF172817).nebula(0x102515, 0x24452A, 0x5A272D)
				.tints(0x6B9365, 0x71383C, 0x95805A, 0x345A42).pulse(0.72F).motion(0.2F)
				.layers(2, 2, 1, 1).monolithPillars(0).toggles(true, true, true, false).build();
		register(mycophantNursery, new MycophantNurseryChamberEffects(mycophantNursery));
		ChamberSkyTheme apotheos = ChamberSkyTheme.builder(ChamberOfWillManager.THEME_APOTHEOS)
				.skybox(0xFF000000, 0xFF000000)
				.nebula(0x000000, 0x000000, 0x000000)
				.tints(0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF)
				.pulse(0.0F)
				.motion(1.0F)
				.layers(0, 0, 0, 0)
				.toggles(true, false, false, false)
				.build();
		register(apotheos, new ApotheosChamberEffects(apotheos));
	}

	private ChamberSkyThemeRegistry() {
	}

	private static ChamberSkyTheme register(ChamberSkyTheme theme, ChamberThemeEffects effects) {
		THEMES.put(theme.id(), theme);
		EFFECTS.put(theme.id(), effects);
		return theme;
	}

	public static ChamberSkyTheme byId(ResourceLocation id) {
		return THEMES.getOrDefault(id, DEFAULT);
	}

	public static ChamberSkyTheme activeTheme() {
		return byId(MycophantFightClientData.isActive() ? ChamberOfWillManager.THEME_MYCOPHANT_NURSERY
				: VesperFightClientData.isActive() ? ChamberOfWillManager.THEME_VESPER_FIGHT
				: ChamberOfWillClientData.skyTheme());
	}

	public static ChamberThemeEffects effectsById(ResourceLocation id) {
		return EFFECTS.getOrDefault(id, EFFECTS.get(DEFAULT.id()));
	}

	public static ChamberThemeEffects activeEffects() {
		return effectsById(activeTheme().id());
	}
}
