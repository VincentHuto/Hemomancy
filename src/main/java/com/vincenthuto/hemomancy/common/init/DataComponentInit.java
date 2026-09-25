package com.vincenthuto.hemomancy.common.init;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.item.component.LivingWeaponGraftData;
import com.vincenthuto.hemomancy.common.item.component.TinctureDoseData;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.StructureScannerTooltipComponent;
import com.vincenthuto.hemomancy.common.item.shared.MnemonicBlueprintTarget;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class DataComponentInit {


	public static final DeferredRegister<DataComponentType<?>> COMPONENTS = DeferredRegister
			.create(Registries.DATA_COMPONENT_TYPE, Hemomancy.MOD_ID);
	public static final DeferredHolder<DataComponentType<?>, DataComponentType<com.vincenthuto.hemomancy.common.enchanting.ScriptoriumProvenance>> SCRIPTORIUM_PROVENANCE = COMPONENTS
			.register("scriptorium_provenance", () -> DataComponentType.<com.vincenthuto.hemomancy.common.enchanting.ScriptoriumProvenance>builder()
					.persistent(com.vincenthuto.hemomancy.common.enchanting.ScriptoriumProvenance.CODEC)
					.networkSynchronized(com.vincenthuto.hemomancy.common.enchanting.ScriptoriumProvenance.STREAM_CODEC).build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<com.vincenthuto.hemomancy.common.antecedent.AhaematicSample>> AHAEMATIC_SAMPLE = COMPONENTS.register("ahaematic_sample", () -> DataComponentType.<com.vincenthuto.hemomancy.common.antecedent.AhaematicSample>builder().persistent(com.vincenthuto.hemomancy.common.antecedent.AhaematicSample.CODEC).networkSynchronized(com.vincenthuto.hemomancy.common.antecedent.AhaematicSample.STREAM_CODEC).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> LISTENING_SCAR_STATE = COMPONENTS.register("listening_scar_state", () -> DataComponentType.<Integer>builder().persistent(com.mojang.serialization.Codec.INT).networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.VAR_INT).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<net.minecraft.world.item.component.ItemContainerContents>> CHARM_TALISMAN = COMPONENTS.register("charm_talisman", () -> DataComponentType.<net.minecraft.world.item.component.ItemContainerContents>builder().persistent(net.minecraft.world.item.component.ItemContainerContents.CODEC).networkSynchronized(net.minecraft.world.item.component.ItemContainerContents.STREAM_CODEC).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> ANCIENT_RECORDING = COMPONENTS.register("ancient_recording", () -> DataComponentType.<String>builder().persistent(com.mojang.serialization.Codec.STRING).networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.STRING_UTF8).build());

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<StructureScannerTooltipComponent>> STRUCTURE_SCANNER_TOOLTIP = COMPONENTS
			.register("structure_scanner_tooltip", () -> DataComponentType.<StructureScannerTooltipComponent>builder()
					.persistent(StructureScannerTooltipComponent.CODEC)
					.networkSynchronized(StructureScannerTooltipComponent.STREAM_CODEC)
					.cacheEncoding()
					.build());

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<LivingWeaponGraftData>> LIVING_WEAPON_GRAFT_DATA = COMPONENTS
			.register("living_weapon_graft_data", () -> DataComponentType.<LivingWeaponGraftData>builder()
					.persistent(LivingWeaponGraftData.CODEC)
					.networkSynchronized(LivingWeaponGraftData.STREAM_CODEC)
					.cacheEncoding()
					.build());

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<MnemonicBlueprintTarget>> MNEMONIC_BLUEPRINT_TARGET = COMPONENTS
			.register("mnemonic_blueprint_target", () -> DataComponentType.<MnemonicBlueprintTarget>builder()
					.persistent(MnemonicBlueprintTarget.CODEC)
					.networkSynchronized(MnemonicBlueprintTarget.STREAM_CODEC)
					.cacheEncoding()
					.build());

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<TinctureDoseData>> TINCTURE_DOSES = COMPONENTS
			.register("tincture_doses", () -> DataComponentType.<TinctureDoseData>builder()
					.persistent(TinctureDoseData.CODEC)
					.networkSynchronized(TinctureDoseData.STREAM_CODEC)
					.cacheEncoding()
					.build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<com.vincenthuto.hemomancy.common.item.component.ClairaudiographRecording>> CLAIRAUDIOGRAPH_RECORDING = COMPONENTS
            .register("clairaudiograph_recording", () -> DataComponentType.<com.vincenthuto.hemomancy.common.item.component.ClairaudiographRecording>builder()
                    .persistent(com.vincenthuto.hemomancy.common.item.component.ClairaudiographRecording.CODEC)
                    .networkSynchronized(com.vincenthuto.hemomancy.common.item.component.ClairaudiographRecording.STREAM_CODEC).build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> BLOOD_SAMPLE_IDENTIFIED = COMPONENTS
            .register("blood_sample_identified", () -> DataComponentType.<Boolean>builder()
                    .persistent(com.mojang.serialization.Codec.BOOL)
                    .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.BOOL).build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<com.vincenthuto.hemomancy.common.item.component.FieldCaseContents>> FIELD_CASE_CONTENTS = COMPONENTS
            .register("field_case_contents", () -> DataComponentType.<com.vincenthuto.hemomancy.common.item.component.FieldCaseContents>builder()
                    .persistent(com.vincenthuto.hemomancy.common.item.component.FieldCaseContents.CODEC)
                    .networkSynchronized(com.vincenthuto.hemomancy.common.item.component.FieldCaseContents.STREAM_CODEC).build());

	private DataComponentInit() {
	}
}

