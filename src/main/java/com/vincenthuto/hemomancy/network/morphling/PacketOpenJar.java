package com.vincenthuto.hemomancy.network.morphling;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.container.MenuMorphlingJar;
import com.vincenthuto.hemomancy.item.morphlings.ItemMorphlingJar;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;

public class PacketOpenJar {
	public static PacketOpenJar decode(final FriendlyByteBuf buffer) {
		buffer.readByte();
		return new PacketOpenJar();
	}

	public static void encode(final PacketOpenJar message, final FriendlyByteBuf buffer) {
		buffer.writeByte(0);
	}

	public static void handle(final PacketOpenJar message, final Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayer player = ctx.get().getSender();
			if (!Hemomancy.findItemInPlayerInv(player, ItemMorphlingJar.class).isEmpty()) {
				player.openMenu(new MenuProvider() {
					@Override
					public Component getDisplayName() {
						return Hemomancy.findItemInPlayerInv(player, ItemMorphlingJar.class).getHoverName();
					}

					@Nullable
					@Override
					public AbstractContainerMenu createMenu(int p_createMenu_1_, Inventory p_createMenu_2_,
							Player p_createMenu_3_) {
						return new MenuMorphlingJar(p_createMenu_1_, p_createMenu_3_.level,
								p_createMenu_3_.blockPosition(), p_createMenu_2_, p_createMenu_3_);
					}
				});
			}
		});
		ctx.get().setPacketHandled(true);
	}
}