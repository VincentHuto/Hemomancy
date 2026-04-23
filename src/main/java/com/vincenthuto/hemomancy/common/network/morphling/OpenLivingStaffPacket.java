package com.vincenthuto.hemomancy.common.network.morphling;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.item.tool.living.LivingStaffItem;
import com.vincenthuto.hemomancy.common.menu.LivingStaffMenu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class OpenLivingStaffPacket implements CustomPacketPayload {

	public static final Type<OpenLivingStaffPacket> TYPE = new Type<>(Hemomancy.rloc("open_living_staff_packet"));
	public static final StreamCodec<FriendlyByteBuf, OpenLivingStaffPacket> STREAM_CODEC = StreamCodec.of(OpenLivingStaffPacket::encode, OpenLivingStaffPacket::decode);
	public static OpenLivingStaffPacket decode(final FriendlyByteBuf buffer) {
		buffer.readByte();
		return new OpenLivingStaffPacket();
	}

	public static void encode(final FriendlyByteBuf buffer, final OpenLivingStaffPacket message) {
		buffer.writeByte(0);
	}

	public static void handle(final OpenLivingStaffPacket message, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			if (!(ctx.player() instanceof ServerPlayer player)) {
				return;
			}
			if (!Hemomancy.findItemInPlayerInv(player, LivingStaffItem.class).isEmpty()) {
				player.openMenu(new MenuProvider() {
					@Nullable
					@Override
					public AbstractContainerMenu createMenu(int p_createMenu_1_, Inventory p_createMenu_2_,
							Player p_createMenu_3_) {
						return new LivingStaffMenu(p_createMenu_1_, p_createMenu_3_.level(),
								p_createMenu_3_.blockPosition(), p_createMenu_2_, p_createMenu_3_);
					}

					@Override
					public Component getDisplayName() {
						return Hemomancy.findItemInPlayerInv(player, LivingStaffItem.class).getHoverName();
					}
				});
			}
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
