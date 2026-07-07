package com.vincenthuto.hemomancy.common.init;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.item.component.LivingWeaponGraftData;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.StructureScannerTooltipComponent;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class DataComponentInit {

	public static final DeferredRegister<DataComponentType<?>> COMPONENTS = DeferredRegister
			.create(Registries.DATA_COMPONENT_TYPE, Hemomancy.MOD_ID);

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

	private DataComponentInit() {
	}
}

