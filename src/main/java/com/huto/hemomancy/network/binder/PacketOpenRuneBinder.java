package com.huto.hemomancy.network.binder;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.container.ContainerRuneBinder;
import com.huto.hemomancy.item.rune.ItemRuneBinder;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketOpenRuneBinder {
	public static PacketOpenRuneBinder decode(final PacketBuffer buffer) {
		buffer.readByte();
		return new PacketOpenRuneBinder();
	}

	public static void encode(final PacketOpenRuneBinder message, final PacketBuffer buffer) {
		buffer.writeByte(0);
	}

	public static void handle(final PacketOpenRuneBinder message, final Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayerEntity player = ctx.get().getSender();
			if (!Hemomancy.findItemInPlayerInv(player, ItemRuneBinder.class).isEmpty()) {
				player.openContainer(new INamedContainerProvider() {
					@Override
					public ITextComponent getDisplayName() {
						return Hemomancy.findItemInPlayerInv(player, ItemRuneBinder.class).getDisplayName();
					}

					@Nullable
					@Override
					public Container createMenu(int p_createMenu_1_, PlayerInventory p_createMenu_2_,
							PlayerEntity p_createMenu_3_) {
						return new ContainerRuneBinder(p_createMenu_1_, p_createMenu_3_.world,
								p_createMenu_3_.getPosition(), p_createMenu_2_, p_createMenu_3_);
					}
				});
			}
		});
		ctx.get().setPacketHandled(true);
	}
}