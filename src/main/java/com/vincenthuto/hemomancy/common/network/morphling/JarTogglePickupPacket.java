package com.vincenthuto.hemomancy.common.network.morphling;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.common.item.morphlings.ItemMorphlingJar;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

public class JarTogglePickupPacket {
	public static JarTogglePickupPacket decode(final FriendlyByteBuf buffer) {
		buffer.readByte();
		return new JarTogglePickupPacket();
	}

	public static void encode(final JarTogglePickupPacket message, final FriendlyByteBuf buffer) {
		buffer.writeByte(0);
	}

	public static void handle(final JarTogglePickupPacket message, final Supplier<NetworkEvent.Context> ctx) {
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