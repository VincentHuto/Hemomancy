package com.vincenthuto.hemomancy.common.entity.boss;

import net.minecraft.world.entity.Entity;

/** A boss that can hold a player against its body through a {@link BossHold}. */
public interface BossHoldCarrier {
	BossHold bossHold();

	Entity holdBody();
}
