package com.vincenthuto.hemomancy.common.network.capa;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.vascular.EnumVeinSections;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;

public class VascularSystemServerPacket implements CustomPacketPayload {

	public static final Type<VascularSystemServerPacket> TYPE = new Type<>(Hemomancy.rloc("vascular_system_server_packet"));
	public static final StreamCodec<FriendlyByteBuf, VascularSystemServerPacket> STREAM_CODEC = StreamCodec.of(VascularSystemServerPacket::encode, VascularSystemServerPacket::decode);
	public static VascularSystemServerPacket decode(final FriendlyByteBuf packetBuffer) {
		Map<EnumVeinSections, Float> devo = new HashMap<>();
		for (EnumVeinSections key : EnumVeinSections.values()) {
			devo.put(key, packetBuffer.readNbt().getFloat(key.toString()));
		}
		return new VascularSystemServerPacket(devo);
	}

	public static void encode(final FriendlyByteBuf packetBuffer, final VascularSystemServerPacket msg) {
		CompoundTag covenTag = new CompoundTag();
		for (EnumVeinSections key : EnumVeinSections.values()) {
			if (msg.vascularSystem.get(key) != null) {
				covenTag.putFloat(key.toString(), msg.vascularSystem.get(key));
				packetBuffer.writeNbt(covenTag);
			} else {
				covenTag.putFloat(key.toString(), 100f);
				packetBuffer.writeNbt(covenTag);

			}
		}
	}

	// This code only runs on the client
	@SuppressWarnings("unused")
	public static void handle(final VascularSystemServerPacket msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			Player player = ctx.player();
			if (player == null) {
				return;
			}

			HemoCapabilityAccess.getVascularSystem(player)
					.orElseThrow(IllegalStateException::new).setVascularSystem(msg.vascularSystem);

		});
	}

	private Map<EnumVeinSections, Float> vascularSystem = new HashMap<>();

	public VascularSystemServerPacket(Map<EnumVeinSections, Float> vascularSystemIn) {
		this.vascularSystem = vascularSystemIn;
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
