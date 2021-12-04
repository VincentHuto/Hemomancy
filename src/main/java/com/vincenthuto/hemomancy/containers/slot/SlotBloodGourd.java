package com.vincenthuto.hemomancy.containers.slot;

import com.vincenthuto.hemomancy.capa.player.rune.IRunesItemHandler;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.items.SlotItemHandler;

public class SlotBloodGourd extends SlotItemHandler {
	int runeSlot;
	Player player;

	public SlotBloodGourd(Player player, IRunesItemHandler itemHandler, int slot, int par4, int par5) {
		super(itemHandler, slot, par4, par5);
		this.runeSlot = slot;
		this.player = player;
	}
}
