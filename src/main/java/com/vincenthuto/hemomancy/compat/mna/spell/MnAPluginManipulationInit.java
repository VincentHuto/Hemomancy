package com.vincenthuto.hemomancy.compat.mna.spell;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;

import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import com.vincenthuto.hemomancy.common.init.ManipulationInit;

public class MnAPluginManipulationInit {

	public static final DeferredRegister<BloodManipulation> MNA_MANIPS = DeferredRegister
			.create(ManipulationInit.MANIP_KEY, Hemomancy.MOD_ID);

	public static final RegistryObject<BloodManipulation> mana_memory_sanguine_transfusion = MNA_MANIPS.register(
			"mana_memory_sanguine_transfusion",
			() -> new SanguineTransfusionManip("mana_memory_sanguine_transfusion", 200, 1, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.DUCTILIS, EnumVeinSections.BODY));
}
