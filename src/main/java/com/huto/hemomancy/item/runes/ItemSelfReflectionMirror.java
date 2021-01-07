package com.huto.hemomancy.item.runes;

import com.huto.hemomancy.network.PacketHandler;
import com.huto.hemomancy.network.capa.OpenRunesInvPacket;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class ItemSelfReflectionMirror extends Item {

	public ItemSelfReflectionMirror(Properties properties) {
		super(properties);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
		if (worldIn.isRemote) {
			PacketHandler.INSTANCE.sendToServer(new OpenRunesInvPacket());
		}
		return super.onItemRightClick(worldIn, playerIn, handIn);
	}

}
