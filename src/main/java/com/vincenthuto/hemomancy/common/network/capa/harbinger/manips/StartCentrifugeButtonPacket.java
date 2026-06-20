package com.vincenthuto.hemomancy.common.network.capa.harbinger.manips;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.menu.tile.crafting.VialCentrifugeMenu;
import com.vincenthuto.hemomancy.common.tile.crafting.VialCentrifugeBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
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
			AbstractContainerMenu container = ctx.player().containerMenu;
			if (container instanceof VialCentrifugeMenu) {
				VialCentrifugeBlockEntity station = ((VialCentrifugeMenu) container).getTe();
				if (station.attemptStartup() && ctx.player() instanceof ServerPlayer serverPlayer
						&& HemoCapabilityAccess.getPlayerDegreeNumber(serverPlayer) >= 2) {
					HarbingerAdvancementGranter.grantIfNotDone(serverPlayer,
							HarbingerAdvancementGranter.ADV_FIRST_SEPARATION_STARTED);
				}
			}
		});
	}

	public StartCentrifugeButtonPacket() {
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
