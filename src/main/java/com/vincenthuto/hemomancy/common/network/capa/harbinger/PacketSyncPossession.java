package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.possession.Possession;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.possession.PossessionAction;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.possession.SurrogateForm;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Server to the possessed player and everyone tracking them: the form they are drawn as, and (for the owner's HUD and
 * possession wheel) Concentration, the wheel's toggles, the return hold and each gift's ready time. {@code form} is -1
 * when the player is not possessing. {@code toggles} packs Still, Climb and Hover as bits 0, 1 and 2.
 */
public record PacketSyncPossession(int entityId, int form, float concentration, float max, int shell, int toggles,
		long returnAt, long[] readyAt) implements CustomPacketPayload {
	public static final Type<PacketSyncPossession> TYPE = new Type<>(Hemomancy.rloc("sync_possession"));
	public static final StreamCodec<FriendlyByteBuf, PacketSyncPossession> STREAM_CODEC = StreamCodec.of(
			(buffer, packet) -> {
				buffer.writeVarInt(packet.entityId());
				buffer.writeVarInt(packet.form() + 1);
				buffer.writeFloat(packet.concentration());
				buffer.writeFloat(packet.max());
				buffer.writeVarInt(packet.shell());
				buffer.writeByte(packet.toggles());
				buffer.writeVarLong(packet.returnAt());
				buffer.writeVarInt(packet.readyAt().length);
				for (long ready : packet.readyAt()) buffer.writeVarLong(ready);
			},
			buffer -> {
				int entityId = buffer.readVarInt();
				int form = buffer.readVarInt() - 1;
				float concentration = buffer.readFloat();
				float max = buffer.readFloat();
				int shell = buffer.readVarInt();
				int toggles = buffer.readUnsignedByte();
				long returnAt = buffer.readVarLong();
				long[] readyAt = new long[Math.min(buffer.readVarInt(), PossessionAction.values().length)];
				for (int i = 0; i < readyAt.length; i++) readyAt[i] = buffer.readVarLong();
				return new PacketSyncPossession(entityId, form, concentration, max, shell, toggles, returnAt, readyAt);
			});

	public static PacketSyncPossession of(int entityId, Possession possession) {
		SurrogateForm form = possession.form();
		int toggles = (possession.toggled(PossessionAction.STILL) ? 1 : 0)
				| (possession.toggled(PossessionAction.CLIMB) ? 2 : 0)
				| (possession.toggled(PossessionAction.HOVER) ? 4 : 0);
		long[] readyAt = new long[PossessionAction.values().length];
		for (PossessionAction action : PossessionAction.values()) readyAt[action.ordinal()] = possession.readyAt(action);
		return new PacketSyncPossession(entityId, form == null ? -1 : form.ordinal(), possession.concentration(),
				possession.max(), possession.shell(), toggles, possession.returnAt(), readyAt);
	}

	public static PacketSyncPossession none(int entityId) {
		return new PacketSyncPossession(entityId, -1, 0f, 0f, 0, 0, 0L, new long[0]);
	}

	public static void handle(PacketSyncPossession packet, IPayloadContext context) {
		context.enqueueWork(() -> {
			if (!(context.player().level().getEntity(packet.entityId()) instanceof Player player)) return;
			SurrogateForm[] forms = SurrogateForm.values();
			SurrogateForm form = packet.form() >= 0 && packet.form() < forms.length ? forms[packet.form()] : null;
			Possession possession = HemoCapabilityAccess.getPossession(player);
			SurrogateForm before = possession.form();
			possession.setFromSync(form, packet.concentration(), packet.max(),
					packet.shell(), (packet.toggles() & 1) != 0, (packet.toggles() & 2) != 0, (packet.toggles() & 4) != 0,
					packet.returnAt(), packet.readyAt());
			if (before != form) player.refreshDimensions();
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
