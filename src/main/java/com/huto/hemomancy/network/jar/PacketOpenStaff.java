package com.huto.hemomancy.network.jar;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.container.ContainerLivingStaff;
import com.huto.hemomancy.item.tool.living.ItemLivingStaff;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

public class PacketOpenStaff {
	public static PacketOpenStaff decode(final FriendlyByteBuf buffer) {
		buffer.readByte();
		return new PacketOpenStaff();
	}

	public static void encode(final PacketOpenStaff message, final FriendlyByteBuf buffer) {
		buffer.writeByte(0);
	}

	public static void handle(final PacketOpenStaff message, final Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayer player = ctx.get().getSender();
			if (!Hemomancy.findItemInPlayerInv(player, ItemLivingStaff.class).isEmpty()) {
				player.openContainer(new INamedContainerProvider() {
					@Override
					public ITextComponent getDisplayName() {
						return Hemomancy.findItemInPlayerInv(player, ItemLivingStaff.class).getDisplayName();
					}

					@Nullable
					@Override
					public Container createMenu(int p_createMenu_1_, PlayerInventory p_createMenu_2_,
							PlayerEntity p_createMenu_3_) {
						return new ContainerLivingStaff(p_createMenu_1_, p_createMenu_3_.world,
								p_createMenu_3_.getPosition(), p_createMenu_2_, p_createMenu_3_);
					}
				});
			}
		});
		ctx.get().setPacketHandled(true);
	}
}