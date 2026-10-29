package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.possession.Possession;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.possession.SurrogateForm;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Server to the possessed player and everyone tracking them: the form they are drawn as, and (for the owner's HUD)
 * Concentration. {@code form} is -1 when the player is not possessing.
 */
public record PacketSyncPossession(int entityId, int form, float concentration, float max, int shell)
		implements CustomPacketPayload {
	public static final Type<PacketSyncPossession> TYPE = new Type<>(Hemomancy.rloc("sync_possession"));
	public static final StreamCodec<FriendlyByteBuf, PacketSyncPossession> STREAM_CODEC = StreamCodec.of(
			(buffer, packet) -> {
				buffer.writeVarInt(packet.entityId());
				buffer.writeVarInt(packet.form() + 1);
				buffer.writeFloat(packet.concentration());
				buffer.writeFloat(packet.max());
				buffer.writeVarInt(packet.shell());
			},
			buffer -> new PacketSyncPossession(buffer.readVarInt(), buffer.readVarInt() - 1, buffer.readFloat(),
					buffer.readFloat(), buffer.readVarInt()));

	public static PacketSyncPossession of(int entityId, Possession possession) {
		SurrogateForm form = possession.form();
		return new PacketSyncPossession(entityId, form == null ? -1 : form.ordinal(), possession.concentration(),
				possession.max(), possession.shell());
	}

	public static PacketSyncPossession none(int entityId) {
		return new PacketSyncPossession(entityId, -1, 0f, 0f, 0);
	}

	public static void handle(PacketSyncPossession packet, IPayloadContext context) {
		context.enqueueWork(() -> {
			if (!(context.player().level().getEntity(packet.entityId()) instanceof Player player)) return;
			SurrogateForm[] forms = SurrogateForm.values();
			SurrogateForm form = packet.form() >= 0 && packet.form() < forms.length ? forms[packet.form()] : null;
			HemoCapabilityAccess.getPossession(player).setFromSync(form, packet.concentration(), packet.max(),
					packet.shell());
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
