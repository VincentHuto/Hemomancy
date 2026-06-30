package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.armor.ability.ArmorSetAbilityRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncArmorSetAbilityCooldownS2CPacket(ResourceLocation abilityId, int remainingTicks)
		implements CustomPacketPayload {
	public static final Type<SyncArmorSetAbilityCooldownS2CPacket> TYPE =
			new Type<>(Hemomancy.rloc("sync_armor_set_ability_cooldown"));
	public static final StreamCodec<FriendlyByteBuf, SyncArmorSetAbilityCooldownS2CPacket> STREAM_CODEC =
			StreamCodec.of(SyncArmorSetAbilityCooldownS2CPacket::encode,
					SyncArmorSetAbilityCooldownS2CPacket::decode);

	public static void encode(FriendlyByteBuf buffer, SyncArmorSetAbilityCooldownS2CPacket packet) {
		buffer.writeResourceLocation(packet.abilityId);
		buffer.writeVarInt(packet.remainingTicks);
	}

	public static SyncArmorSetAbilityCooldownS2CPacket decode(FriendlyByteBuf buffer) {
		return new SyncArmorSetAbilityCooldownS2CPacket(buffer.readResourceLocation(), buffer.readVarInt());
	}

	public static void handle(SyncArmorSetAbilityCooldownS2CPacket packet, IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			Player player = ctx.player();
			if (player != null) {
				ArmorSetAbilityRegistry.handleClientCooldownSync(player, packet.abilityId, packet.remainingTicks);
			}
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
