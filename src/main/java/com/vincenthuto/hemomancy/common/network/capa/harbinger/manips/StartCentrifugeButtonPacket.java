package com.vincenthuto.hemomancy.common.network.capa.harbinger.manips;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.menu.tile.crafting.VialCentrifugeMenu;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.VialCentrifugeBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.VialCentrifugeStartupResult;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class StartCentrifugeButtonPacket implements CustomPacketPayload {

	public static final Type<StartCentrifugeButtonPacket> TYPE = new Type<>(Hemomancy.rloc("start_centrifuge_button_packet"));
	public static final StreamCodec<FriendlyByteBuf, StartCentrifugeButtonPacket> STREAM_CODEC = StreamCodec.of(StartCentrifugeButtonPacket::encode, StartCentrifugeButtonPacket::decode);

	public static StartCentrifugeButtonPacket decode(FriendlyByteBuf buf) {
		return new StartCentrifugeButtonPacket();
	}

	public static void encode(FriendlyByteBuf buf, StartCentrifugeButtonPacket msg) {
	}

	public static void handle(final StartCentrifugeButtonPacket msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			if (ctx.player() instanceof ServerPlayer serverPlayer
					&& serverPlayer.containerMenu instanceof VialCentrifugeMenu menu
					&& menu.stillValid(serverPlayer)) {
				start(serverPlayer, menu.getTe());
			}
		});
	}

	public static VialCentrifugeStartupResult start(ServerPlayer player, VialCentrifugeBlockEntity station) {
		VialCentrifugeStartupResult result = station.attemptStartup(player);
		player.displayClientMessage(Component.translatable(result.translationKey()), true);
		if (result == VialCentrifugeStartupResult.SUCCESS
				&& HemoCapabilityAccess.getPlayerDegreeNumber(player) >= 1) {
			HarbingerAdvancementGranter.grantIfNotDone(player,
					HarbingerAdvancementGranter.ADV_FIRST_SEPARATION_STARTED);
		}
		return result;
	}

	public StartCentrifugeButtonPacket() {
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
