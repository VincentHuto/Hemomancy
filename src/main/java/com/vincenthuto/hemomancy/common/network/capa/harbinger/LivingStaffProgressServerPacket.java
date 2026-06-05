package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.livingstaff.ILivingStaffProgress;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class LivingStaffProgressServerPacket implements CustomPacketPayload {
	public static final Type<LivingStaffProgressServerPacket> TYPE =
			new Type<>(Hemomancy.rloc("living_staff_progress_server_packet"));
	public static final StreamCodec<FriendlyByteBuf, LivingStaffProgressServerPacket> STREAM_CODEC =
			StreamCodec.of(LivingStaffProgressServerPacket::encode, LivingStaffProgressServerPacket::decode);

	private final boolean hasLivingStaffBond;
	private final double bloodHandled;
	private final boolean vesperMemoryAwakened;

	public LivingStaffProgressServerPacket(ILivingStaffProgress progress) {
		this(progress.hasLivingStaffBond(), progress.getBloodHandled(), progress.isVesperMemoryAwakened());
	}

	public LivingStaffProgressServerPacket(boolean hasLivingStaffBond, double bloodHandled,
			boolean vesperMemoryAwakened) {
		this.hasLivingStaffBond = hasLivingStaffBond;
		this.bloodHandled = bloodHandled;
		this.vesperMemoryAwakened = vesperMemoryAwakened;
	}

	public static LivingStaffProgressServerPacket decode(FriendlyByteBuf buffer) {
		return new LivingStaffProgressServerPacket(buffer.readBoolean(), buffer.readDouble(), buffer.readBoolean());
	}

	public static void encode(FriendlyByteBuf buffer, LivingStaffProgressServerPacket packet) {
		buffer.writeBoolean(packet.hasLivingStaffBond);
		buffer.writeDouble(packet.bloodHandled);
		buffer.writeBoolean(packet.vesperMemoryAwakened);
	}

	public static void handle(LivingStaffProgressServerPacket packet, IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			Player player = ctx.player();
			if (player == null) {
				return;
			}
			HemoCapabilityAccess.getLivingStaffProgress(player).ifPresent(progress -> {
				progress.setLivingStaffBond(packet.hasLivingStaffBond);
				progress.setBloodHandled(packet.bloodHandled);
				progress.setVesperMemoryAwakened(packet.vesperMemoryAwakened);
			});
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
