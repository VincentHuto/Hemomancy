package com.huto.hemomancy.network.jar;

import java.util.function.Supplier;

import com.huto.hemomancy.item.morphlings.ItemMorphlingJar;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

public class PacketJarTogglePickup {
	public static PacketJarTogglePickup decode(final FriendlyByteBuf buffer) {
		buffer.readByte();
		return new PacketJarTogglePickup();
	}

	public static void encode(final PacketJarTogglePickup message, final FriendlyByteBuf buffer) {
		buffer.writeByte(0);
	}

	public static void handle(final PacketJarTogglePickup message, final Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			Player player = ctx.get().getSender();
			if (player == null)
				return;
			if (player.getMainHandItem().getItem() instanceof ItemMorphlingJar)
				((ItemMorphlingJar) player.getMainHandItem().getItem()).togglePickup(player, player.getMainHandItem());
			else if (player.getOffhandItem().getItem() instanceof ItemMorphlingJar)
				((ItemMorphlingJar) player.getOffhandItem().getItem()).togglePickup(player, player.getOffhandItem());
			else {
				// check hotbar
				for (int i = 0; i <= 8; i++) {
					ItemStack stack = player.getInventory().getItem(i);
					if (stack.getItem() instanceof ItemMorphlingJar) {
						((ItemMorphlingJar) stack.getItem()).togglePickup(player, stack);
						break;
					}
				}
			}
		});
		ctx.get().setPacketHandled(true);
	}
}