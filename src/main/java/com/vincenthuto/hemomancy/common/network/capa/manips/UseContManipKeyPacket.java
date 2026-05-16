package com.vincenthuto.hemomancy.common.network.capa.manips;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class UseContManipKeyPacket implements CustomPacketPayload {

	public static final Type<UseContManipKeyPacket> TYPE = new Type<>(Hemomancy.rloc("use_cont_manip_key_packet"));
	public static final StreamCodec<FriendlyByteBuf, UseContManipKeyPacket> STREAM_CODEC = StreamCodec.of(UseContManipKeyPacket::encode, UseContManipKeyPacket::decode);

	public static UseContManipKeyPacket decode(final FriendlyByteBuf buffer) {
		buffer.readByte();
		return new UseContManipKeyPacket(buffer.readFloat());
	}

	public static void encode(final FriendlyByteBuf buffer, final UseContManipKeyPacket message) {
		buffer.writeByte(0);
		buffer.writeFloat(message.parTick);
	}

	@SuppressWarnings("unused")
	public static void handle(final UseContManipKeyPacket message, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			Player player = ctx.player();
			if (player == null)
				return;
			if (!player.level().isClientSide) {
				float pTic = message.parTick;
				IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(player)
						.orElseThrow(NullPointerException::new);
				IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player)
						.orElseThrow(NullPointerException::new);
				if (volume.isActive()) {
					if (known.getSelectedManip() != null) {
						BloodManipulation selectedManip = known.getSelectedManip();

						if (selectedManip != null) {
							// Check manipulation is equipped
							if (!known.isManipEquipped(selectedManip)) {
								player.displayClientMessage(
										Component.literal("That manipulation is not equipped!")
												.withStyle(ChatFormatting.RED), true);
								return;
							}
							// Continuous and Charged
							if (selectedManip.getType() == EnumManipulationType.CONTINUOUS
									|| selectedManip.getType() == EnumManipulationType.CHARGED) {
								selectedManip.performAction(player, player.level(),
										player.getMainHandItem(), player.blockPosition());
							} else {
								player.displayClientMessage(Component.literal(
										"Selected Manipulation is not a Continuous or Charged MobEffect")
												.withStyle(ChatFormatting.RED),
										true);
							}
						}
					}

				} else {
					player.displayClientMessage(Component.literal("You lack the skill to manifest this power!")
							.withStyle(ChatFormatting.RED), true);
				}
			}
		});
	}

	float parTick;

	public UseContManipKeyPacket() {
	}

	public UseContManipKeyPacket(float par) {
		this.parTick = par;
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
