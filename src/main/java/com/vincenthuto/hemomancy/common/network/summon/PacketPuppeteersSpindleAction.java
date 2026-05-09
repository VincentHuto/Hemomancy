package com.vincenthuto.hemomancy.common.network.summon;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.MarionetteCrossbarItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Optional;

public record PacketPuppeteersSpindleAction(Action action, String summonName) implements CustomPacketPayload {
	public static final Type<PacketPuppeteersSpindleAction> TYPE =
			new Type<>(Hemomancy.rloc("puppeteers_spindle_action"));
	public static final StreamCodec<FriendlyByteBuf, PacketPuppeteersSpindleAction> STREAM_CODEC =
			StreamCodec.of(PacketPuppeteersSpindleAction::encode, PacketPuppeteersSpindleAction::decode);

	public enum Action {
		SELECT,
		BIND,
		WIND,
		UNLOCK,
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
		Optional<ItemStack> crossbar = MarionetteCrossbarItem.findFirstCrossbar(player);
		if (crossbar.isEmpty()) {
			player.displayClientMessage(Component.translatable("hemomancy.summon.refill.no_crossbar")
					.withStyle(ChatFormatting.GRAY), true);
			return;
		}

		switch (msg.action) {
			case SELECT -> MarionetteCrossbarItem.selectSummon(crossbar.get(), player, msg.summonName);
			case BIND -> MarionetteCrossbarItem.bindCrossbar(crossbar.get(), player);
			case WIND -> MarionetteCrossbarItem.findFirstItem(player, ItemInit.puppeteering_thread.get())
					.ifPresentOrElse(
							thread -> MarionetteCrossbarItem.windCrossbar(crossbar.get(), thread, player),
							() -> player.displayClientMessage(Component.translatable("hemomancy.summon.refill.no_thread")
									.withStyle(ChatFormatting.GRAY), true));
			case UNLOCK -> MarionetteCrossbarItem.findFirstItem(player, ItemInit.sanguine_quintessence.get())
					.ifPresentOrElse(
							quintessence -> MarionetteCrossbarItem.unlockNextSummon(quintessence, player),
							() -> player.displayClientMessage(Component.translatable("hemomancy.summon.unlock.no_quintessence")
									.withStyle(ChatFormatting.GRAY), true));
			case CALL_OR_RECALL -> MarionetteCrossbarItem.callOrRecallSelectedSummon(crossbar.get(), player);
		}
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
