package com.vincenthuto.hemomancy.compat.mna.spell;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MnAPluginManipulationInit {

	public static final DeferredRegister<BloodManipulation> MNA_MANIPS = DeferredRegister
			.create(ManipulationInit.MANIP_KEY, Hemomancy.MOD_ID);

	public static final DeferredHolder<BloodManipulation, BloodManipulation> mana_memory_sanguine_transfusion = MNA_MANIPS.register(
			"mana_memory_sanguine_transfusion",
			() -> new SanguineTransfusionManip("mana_memory_sanguine_transfusion", 200, 1, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.DUCTILIS, EnumVeinSections.BODY)
					.setCooldownTicks(20));
}
