package com.vincenthuto.hemomancy.common.network.capa.runes;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.vincenthuto.hemomancy.common.item.rune.ItemRuneBinder;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

public class PacketBinderTogglePickup {
	public static PacketBinderTogglePickup decode(final FriendlyByteBuf buffer) {
		buffer.readByte();
		return new PacketBinderTogglePickup();
	}

	public static void encode(final PacketBinderTogglePickup message, final FriendlyByteBuf buffer) {
		buffer.writeByte(0);
	}

	public static void handle(final PacketBinderTogglePickup message, final Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			@Nullable ServerPlayer player = ctx.get().getSender();
			if (player == null)
				return;
			if (player.getMainHandItem().getItem() instanceof ItemRuneBinder)
				((ItemRuneBinder) player.getMainHandItem().getItem()).togglePickup(player, player.getMainHandItem());
			else if (player.getOffhandItem().getItem() instanceof ItemRuneBinder)
				((ItemRuneBinder) player.getOffhandItem().getItem()).togglePickup(player, player.getOffhandItem());
			else {
				// check hotbar
				for (int i = 0; i <= 8; i++) {
					ItemStack stack = player.getInventory().getItem(i);
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