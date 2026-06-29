package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.armor.ability.ArmorSetAbilityRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ActivateArmorSetAbilityC2SPacket(ResourceLocation abilityId) implements CustomPacketPayload {
	public static final Type<ActivateArmorSetAbilityC2SPacket> TYPE =
			new Type<>(Hemomancy.rloc("activate_armor_set_ability"));
	public static final StreamCodec<FriendlyByteBuf, ActivateArmorSetAbilityC2SPacket> STREAM_CODEC =
			StreamCodec.of(ActivateArmorSetAbilityC2SPacket::encode, ActivateArmorSetAbilityC2SPacket::decode);

	public static void encode(FriendlyByteBuf buffer, ActivateArmorSetAbilityC2SPacket packet) {
		buffer.writeResourceLocation(packet.abilityId);
	}

	public static ActivateArmorSetAbilityC2SPacket decode(FriendlyByteBuf buffer) {
		return new ActivateArmorSetAbilityC2SPacket(buffer.readResourceLocation());
	}

	public static void handle(ActivateArmorSetAbilityC2SPacket packet, IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			if (ctx.player() instanceof ServerPlayer player) {
				ArmorSetAbilityRegistry.tryActivate(player, packet.abilityId);
			}
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
