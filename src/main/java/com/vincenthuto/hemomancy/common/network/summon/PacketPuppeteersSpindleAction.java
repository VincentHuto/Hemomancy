package com.vincenthuto.hemomancy.common.network.summon;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.MarionetteCrossbarItem;
import com.vincenthuto.hemomancy.common.menu.PuppeteersSpindleMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketPuppeteersSpindleAction(Action action, String summonName) implements CustomPacketPayload {
	public static final Type<PacketPuppeteersSpindleAction> TYPE =
			new Type<>(Hemomancy.rloc("puppeteers_spindle_action"));
	public static final StreamCodec<FriendlyByteBuf, PacketPuppeteersSpindleAction> STREAM_CODEC =
			StreamCodec.of(PacketPuppeteersSpindleAction::encode, PacketPuppeteersSpindleAction::decode);

	public enum Action {
		SELECT,
		BIND,
		CALL_OR_RECALL
	}

	public static void encode(FriendlyByteBuf buf, PacketPuppeteersSpindleAction msg) {
		buf.writeEnum(msg.action);
		buf.writeUtf(msg.summonName == null ? "" : msg.summonName);
	}

	public static PacketPuppeteersSpindleAction decode(FriendlyByteBuf buf) {
		return new PacketPuppeteersSpindleAction(buf.readEnum(Action.class), buf.readUtf());
	}

	public static void handle(final PacketPuppeteersSpindleAction msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			if (ctx.player() instanceof ServerPlayer player) {
				handleServer(msg, player);
			}
		});
	}

	private static void handleServer(PacketPuppeteersSpindleAction msg, ServerPlayer player) {
		if (!(player.containerMenu instanceof PuppeteersSpindleMenu menu)) {
			return;
		}
		ItemStack crossbar = menu.getCrossbarStack();
		if (!(crossbar.getItem() instanceof MarionetteCrossbarItem)) {
			player.displayClientMessage(Component.translatable("hemomancy.summon.spindle.no_crossbar")
					.withStyle(ChatFormatting.GRAY), true);
			return;
		}

		switch (msg.action) {
			case SELECT -> MarionetteCrossbarItem.selectSummon(crossbar, player, msg.summonName);
			case BIND -> MarionetteCrossbarItem.bindCrossbar(crossbar, player);
			case CALL_OR_RECALL -> MarionetteCrossbarItem.callOrRecallSelectedSummon(crossbar, player);
		}
		menu.getSpindle().sendUpdates();
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
