package com.vincenthuto.hemomancy.common.init;

import com.mojang.serialization.Codec;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.item.component.AhaematicSample;
import com.vincenthuto.hemomancy.common.brewing.AdvancedBrewData;
import com.vincenthuto.hemomancy.common.enchanting.ResonantPattern;
import com.vincenthuto.hemomancy.common.enchanting.ScriptoriumProvenance;
import com.vincenthuto.hemomancy.common.item.component.ClairaudiographRecording;
import com.vincenthuto.hemomancy.common.item.component.FieldCaseContents;
import com.vincenthuto.hemomancy.common.item.component.LivingWeaponGraftData;
import com.vincenthuto.hemomancy.common.item.component.TinctureDoseData;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.StructureScannerTooltipComponent;
import com.vincenthuto.hemomancy.common.item.shared.MnemonicBlueprintTarget;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class DataComponentInit {

	public static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister
			.createDataComponents(Registries.DATA_COMPONENT_TYPE, Hemomancy.MOD_ID);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<AdvancedBrewData>> ADVANCED_BREW = COMPONENTS
			.registerComponentType("advanced_brew", builder -> builder
					.persistent(AdvancedBrewData.CODEC)
					.networkSynchronized(AdvancedBrewData.STREAM_CODEC));
	public static final DeferredHolder<DataComponentType<?>, DataComponentType<ScriptoriumProvenance>> SCRIPTORIUM_PROVENANCE = COMPONENTS
			.registerComponentType("scriptorium_provenance", builder -> builder
					.persistent(ScriptoriumProvenance.CODEC)
					.networkSynchronized(ScriptoriumProvenance.STREAM_CODEC));
	public static final DeferredHolder<DataComponentType<?>, DataComponentType<ResonantPattern>> RESONANT_PATTERN = COMPONENTS
			.registerComponentType("resonant_pattern", builder -> builder
					.persistent(ResonantPattern.CODEC)
					.networkSynchronized(ResonantPattern.STREAM_CODEC));

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<AhaematicSample>> AHAEMATIC_SAMPLE = COMPONENTS
			.registerComponentType("ahaematic_sample", builder -> builder
					.persistent(AhaematicSample.CODEC)
					.networkSynchronized(AhaematicSample.STREAM_CODEC));
	public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> LISTENING_SCAR_STATE = COMPONENTS
			.registerComponentType("listening_scar_state", builder -> builder
					.persistent(Codec.INT)
					.networkSynchronized(ByteBufCodecs.VAR_INT));
	public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemContainerContents>> CHARM_TALISMAN = COMPONENTS
			.registerComponentType("charm_talisman", builder -> builder
					.persistent(ItemContainerContents.CODEC)
					.networkSynchronized(ItemContainerContents.STREAM_CODEC));
	public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> ANCIENT_RECORDING = COMPONENTS
			.registerComponentType("ancient_recording", builder -> builder
					.persistent(Codec.STRING)
					.networkSynchronized(ByteBufCodecs.STRING_UTF8));

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<StructureScannerTooltipComponent>> STRUCTURE_SCANNER_TOOLTIP = COMPONENTS
			.registerComponentType("structure_scanner_tooltip", builder -> builder
					.persistent(StructureScannerTooltipComponent.CODEC)
					.networkSynchronized(StructureScannerTooltipComponent.STREAM_CODEC)
					.cacheEncoding());

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<LivingWeaponGraftData>> LIVING_WEAPON_GRAFT_DATA = COMPONENTS
			.registerComponentType("living_weapon_graft_data", builder -> builder
					.persistent(LivingWeaponGraftData.CODEC)
					.networkSynchronized(LivingWeaponGraftData.STREAM_CODEC)
					.cacheEncoding());

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<MnemonicBlueprintTarget>> MNEMONIC_BLUEPRINT_TARGET = COMPONENTS
			.registerComponentType("mnemonic_blueprint_target", builder -> builder
					.persistent(MnemonicBlueprintTarget.CODEC)
					.networkSynchronized(MnemonicBlueprintTarget.STREAM_CODEC)
					.cacheEncoding());

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<TinctureDoseData>> TINCTURE_DOSES = COMPONENTS
			.registerComponentType("tincture_doses", builder -> builder
					.persistent(TinctureDoseData.CODEC)
					.networkSynchronized(TinctureDoseData.STREAM_CODEC)
					.cacheEncoding());

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<ClairaudiographRecording>> CLAIRAUDIOGRAPH_RECORDING = COMPONENTS
			.registerComponentType("clairaudiograph_recording", builder -> builder
					.persistent(ClairaudiographRecording.CODEC)
					.networkSynchronized(ClairaudiographRecording.STREAM_CODEC));

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> BLOOD_SAMPLE_IDENTIFIED = COMPONENTS
			.registerComponentType("blood_sample_identified", builder -> builder
					.persistent(Codec.BOOL)
					.networkSynchronized(ByteBufCodecs.BOOL));

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<FieldCaseContents>> FIELD_CASE_CONTENTS = COMPONENTS
			.registerComponentType("field_case_contents", builder -> builder
					.persistent(FieldCaseContents.CODEC)
					.networkSynchronized(FieldCaseContents.STREAM_CODEC));

	private DataComponentInit() {
	}
}
