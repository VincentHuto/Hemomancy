package com.huto.hemomancy.network.binder;

import java.util.function.Supplier;

import com.huto.hemomancy.item.rune.ItemRuneBinder;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketBinderTogglePickup {
	public static PacketBinderTogglePickup decode(final PacketBuffer buffer) {
		buffer.readByte();
		return new PacketBinderTogglePickup();
	}

	public static void encode(final PacketBinderTogglePickup message, final PacketBuffer buffer) {
		buffer.writeByte(0);
	}

	public static void handle(final PacketBinderTogglePickup message, final Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			PlayerEntity player = ctx.get().getSender();
			if (player == null)
				return;
			if (player.getHeldItemMainhand().getItem() instanceof ItemRuneBinder)
				((ItemRuneBinder) player.getHeldItemMainhand().getItem()).togglePickup(player,
						player.getHeldItemMainhand());
			else if (player.getHeldItemOffhand().getItem() instanceof ItemRuneBinder)
				((ItemRuneBinder) player.getHeldItemOffhand().getItem()).togglePickup(player,
						player.getHeldItemOffhand());
			else {
				// check hotbar
				for (int i = 0; i <= 8; i++) {
					ItemStack stack = player.inventory.getStackInSlot(i);
					if (stack.getItem() instanceof ItemRuneBinder) {
						((ItemRuneBinder) stack.getItem()).togglePickup(player, stack);
						break;
					}
				}
			}
		});
		ctx.get().setPacketHandled(true);
	}
}