package com.vincenthuto.hemomancy.common.item.rune;

import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.runes.PacketOpenRunesInv;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemSelfReflectionMirror extends Item {

	public ItemSelfReflectionMirror(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		if (worldIn.isClientSide) {
			PacketHandler.CHANNELRUNES.sendToServer(new PacketOpenRunesInv());
		}
		return super.use(worldIn, playerIn, handIn);
	}

}
