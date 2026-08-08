package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.client.data.VesperFightClientData;
import com.vincenthuto.hemomancy.client.render.world.chamberofwill.VesperFightFloorRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketSyncVesperFightScene(boolean active, BlockPos center) implements CustomPacketPayload {
	public static final Type<PacketSyncVesperFightScene> TYPE = new Type<>(
			ResourceLocation.fromNamespaceAndPath("hemomancy", "sync_vesper_fight_scene"));
	public static final StreamCodec<FriendlyByteBuf, PacketSyncVesperFightScene> STREAM_CODEC =
			StreamCodec.of(PacketSyncVesperFightScene::encode, PacketSyncVesperFightScene::decode);

	public PacketSyncVesperFightScene {
		center = active ? center.immutable() : BlockPos.ZERO;
	}

	public static PacketSyncVesperFightScene activate(BlockPos center) {
		return new PacketSyncVesperFightScene(true, center);
	}

	public static PacketSyncVesperFightScene clearScene() {
		return new PacketSyncVesperFightScene(false, BlockPos.ZERO);
	}

	public static void encode(FriendlyByteBuf buffer, PacketSyncVesperFightScene packet) {
		buffer.writeBoolean(packet.active);
		buffer.writeBlockPos(packet.center);
	}

	public static PacketSyncVesperFightScene decode(FriendlyByteBuf buffer) {
		return new PacketSyncVesperFightScene(buffer.readBoolean(), buffer.readBlockPos());
	}

	public static void handle(PacketSyncVesperFightScene packet, IPayloadContext context) {
		context.enqueueWork(() -> {
			if (packet.active) {
				if (!packet.center.equals(VesperFightClientData.center())) {
					VesperFightFloorRenderer.clear();
				}
				VesperFightClientData.activate(packet.center);
			} else {
				VesperFightClientData.clear();
				VesperFightFloorRenderer.clear();
			}
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
