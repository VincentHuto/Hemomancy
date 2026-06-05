package com.vincenthuto.hemomancy.common.network.capa.harbinger.scars;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.ItemScarBinder;
import com.vincenthuto.hemomancy.common.menu.ScarBinderInventoryMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nullable;

public class PacketOpenScarBinder implements CustomPacketPayload {

	public static final Type<PacketOpenScarBinder> TYPE = new Type<>(Hemomancy.rloc("packet_open_scar_binder"));
	public static final StreamCodec<FriendlyByteBuf, PacketOpenScarBinder> STREAM_CODEC = StreamCodec.of(PacketOpenScarBinder::encode, PacketOpenScarBinder::decode);
	public static PacketOpenScarBinder decode(final FriendlyByteBuf buffer) {
		buffer.readByte();
		return new PacketOpenScarBinder();
	}

	public static void encode(final FriendlyByteBuf buffer, final PacketOpenScarBinder message) {
		buffer.writeByte(0);
	}

	public static void handle(final PacketOpenScarBinder message, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			if (!(ctx.player() instanceof ServerPlayer player)) {
				return;
			}
			if (!Hemomancy.findItemInPlayerInv(player, ItemScarBinder.class).isEmpty()) {
				player.openMenu(new MenuProvider() {
					@Override
					public Component getDisplayName() {
						return Hemomancy.findItemInPlayerInv(player, ItemScarBinder.class).getHoverName();
					}

					@Nullable
					@Override
					public AbstractContainerMenu createMenu(int p_createMenu_1_, Inventory p_createMenu_2_,
							Player p_createMenu_3_) {
						return new ScarBinderInventoryMenu(p_createMenu_1_, p_createMenu_3_.level(),
								p_createMenu_3_.blockPosition(), p_createMenu_2_, p_createMenu_3_);
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
