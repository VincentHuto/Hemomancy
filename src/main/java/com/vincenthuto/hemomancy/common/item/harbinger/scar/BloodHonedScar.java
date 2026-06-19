package com.vincenthuto.hemomancy.common.item.harbinger.scar;

import com.vincenthuto.hemomancy.common.init.ScarInit;

/**
 * Compatibility item wrapper for the Blood-Honed scar. Behavior now lives in
 * {@link ScarInit#scar_blood_honed}.
 */
public class BloodHonedScar extends ItemScar {
	public BloodHonedScar(Properties properties) {
		super(properties, ScarInit.scar_blood_honed);
	}
}
