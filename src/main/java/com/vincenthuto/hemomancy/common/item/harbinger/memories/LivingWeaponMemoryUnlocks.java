package com.vincenthuto.hemomancy.common.item.harbinger.memories;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulationGrantHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulationGrantHelper.MemoryGrantResult;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.item.component.LivingWeaponForm;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class LivingWeaponMemoryUnlocks {
	private LivingWeaponMemoryUnlocks() {
	}

	public static MemoryGrantResult checkFormMemory(ServerPlayer player, LivingWeaponForm form) {
		DeferredHolder<BloodManipulation, BloodManipulation> holder = manipulationHolder(form);
		return holder != null && holder.isBound()
				? KnownManipulationGrantHelper.checkMemoryGrant(player, holder.get())
				: new MemoryGrantResult(KnownManipulationGrantHelper.MemoryGrantStatus.INVALID, null, 0);
	}

	public static MemoryGrantResult grantFormMemory(ServerPlayer player, LivingWeaponForm form) {
		DeferredHolder<BloodManipulation, BloodManipulation> holder = manipulationHolder(form);
		return holder != null && holder.isBound()
				? KnownManipulationGrantHelper.grantMemory(player, holder.get())
				: new MemoryGrantResult(KnownManipulationGrantHelper.MemoryGrantStatus.INVALID, null, 0);
	}

	private static DeferredHolder<BloodManipulation, BloodManipulation> manipulationHolder(LivingWeaponForm form) {
		return switch (form) {
		case BLADE -> ManipulationInit.conjure_blade;
		case AXE -> ManipulationInit.conjure_axe;
		case SPEAR -> ManipulationInit.conjure_spear;
		case CLAWS -> ManipulationInit.conjure_claws;
		case CROSSBOW -> ManipulationInit.conjure_crossbow;
		case TORCH -> ManipulationInit.conjure_torch;
		case FLAIL -> ManipulationInit.conjure_flail;
		};
	}
}
