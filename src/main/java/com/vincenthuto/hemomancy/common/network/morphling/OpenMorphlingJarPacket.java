package com.vincenthuto.hemomancy.common.network.morphling;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.ItemMorphlingJar;
import com.vincenthuto.hemomancy.common.menu.MorphlingJarMenu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class OpenMorphlingJarPacket implements CustomPacketPayload {

	public static final Type<OpenMorphlingJarPacket> TYPE = new Type<>(Hemomancy.rloc("open_morphling_jar_packet"));
	public static final StreamCodec<FriendlyByteBuf, OpenMorphlingJarPacket> STREAM_CODEC = StreamCodec.of(OpenMorphlingJarPacket::encode, OpenMorphlingJarPacket::decode);
	public static OpenMorphlingJarPacket decode(final FriendlyByteBuf buffer) {
		buffer.readByte();
		return new OpenMorphlingJarPacket();
	}

	public static void encode(final FriendlyByteBuf buffer, final OpenMorphlingJarPacket message) {
		buffer.writeByte(0);
	}

	public static void handle(final OpenMorphlingJarPacket message, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			if (!(ctx.player() instanceof ServerPlayer player)) return;

			// Find jar in inventory first, then check scar slot 7
			ItemStack jarStack = Hemomancy.findItemInPlayerInv(player, ItemMorphlingJar.class);
			if (jarStack.isEmpty()) {
				jarStack = HemoCapabilityAccess.getScars(player)
						.map(r -> r.getStackInSlot(7))
						.filter(s -> s.getItem() instanceof ItemMorphlingJar)
						.orElse(ItemStack.EMPTY);
			}
			if (!jarStack.isEmpty()) {
				final ItemStack finalJar = jarStack;
				player.openMenu(new MenuProvider() {
					@Nullable
					@Override
					public AbstractContainerMenu createMenu(int p_createMenu_1_, Inventory p_createMenu_2_,
							Player p_createMenu_3_) {
						return new MorphlingJarMenu(p_createMenu_1_, p_createMenu_3_.level(),
								p_createMenu_3_.blockPosition(), p_createMenu_2_, p_createMenu_3_);
					}

					@Override
					public Component getDisplayName() {
						return finalJar.getHoverName();
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
