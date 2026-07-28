package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.rite.IchorianKnowledge;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilDefinition;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilRegistry;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilSyncData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;

public record PacketSyncIchorianKnowledge(Map<ResourceLocation, BitSet> partial, Set<ResourceLocation> known,
		Map<ResourceLocation, IchorianSigilDefinition> definitions)
		implements CustomPacketPayload {
	public static final Type<PacketSyncIchorianKnowledge> TYPE =
			new Type<>(Hemomancy.rloc("packet_sync_ichorian_knowledge"));
	public static final StreamCodec<FriendlyByteBuf, PacketSyncIchorianKnowledge> STREAM_CODEC =
			StreamCodec.of(PacketSyncIchorianKnowledge::encode, PacketSyncIchorianKnowledge::decode);

	public PacketSyncIchorianKnowledge(IchorianKnowledge knowledge) {
		this(knowledge.partialKnowledge(), knowledge.knownSigils(), IchorianSigilSyncData.capture().definitions());
	}

	private static void encode(FriendlyByteBuf buffer, PacketSyncIchorianKnowledge packet) {
		buffer.writeVarInt(packet.partial.size());
		packet.partial.forEach((id, nodes) -> {
			buffer.writeResourceLocation(id);
			long[] words = nodes.toLongArray();
			buffer.writeVarInt(words.length);
			for (long word : words) buffer.writeLong(word);
		});
		buffer.writeVarInt(packet.known.size());
		for (ResourceLocation id : packet.known) buffer.writeResourceLocation(id);
		buffer.writeVarInt(packet.definitions.size());
		for (IchorianSigilDefinition definition : packet.definitions.values()) {
			buffer.writeResourceLocation(definition.id());
			buffer.writeEnum(definition.kind());
			buffer.writeVarInt(definition.tier());
			buffer.writeInt(definition.color());
			buffer.writeUtf(definition.name());
			buffer.writeUtf(definition.purpose());
			buffer.writeVarInt(definition.stability());
			buffer.writeVarInt(definition.capacityMl());
			buffer.writeVarInt(definition.nodes().size());
			for (IchorianSigilDefinition.Node node : definition.nodes()) {
				buffer.writeDouble(node.x());
				buffer.writeDouble(node.z());
			}
		}
	}

	private static PacketSyncIchorianKnowledge decode(FriendlyByteBuf buffer) {
		Map<ResourceLocation, BitSet> partial = new HashMap<>();
		for (int i = 0, count = buffer.readVarInt(); i < count; i++) {
			ResourceLocation id = buffer.readResourceLocation();
			long[] words = new long[buffer.readVarInt()];
			for (int word = 0; word < words.length; word++) words[word] = buffer.readLong();
			partial.put(id, BitSet.valueOf(words));
		}
		Set<ResourceLocation> known = new HashSet<>();
		for (int i = 0, count = buffer.readVarInt(); i < count; i++) known.add(buffer.readResourceLocation());
		Map<ResourceLocation, IchorianSigilDefinition> definitions = new HashMap<>();
		for (int i = 0, count = buffer.readVarInt(); i < count; i++) {
			ResourceLocation id = buffer.readResourceLocation();
			IchorianSigilDefinition.Kind kind = buffer.readEnum(IchorianSigilDefinition.Kind.class);
			int tier = buffer.readVarInt();
			int color = buffer.readInt();
			String name = buffer.readUtf();
			String purpose = buffer.readUtf();
			int stability = buffer.readVarInt();
			int capacity = buffer.readVarInt();
			List<IchorianSigilDefinition.Node> nodes = new ArrayList<>();
			for (int node = 0, nodeCount = buffer.readVarInt(); node < nodeCount; node++) {
				nodes.add(new IchorianSigilDefinition.Node(buffer.readDouble(), buffer.readDouble()));
			}
			definitions.put(id, new IchorianSigilDefinition(id, kind, tier, color, name, purpose,
					stability, capacity, nodes));
		}
		return new PacketSyncIchorianKnowledge(partial, known, definitions);
	}

	public static void handle(PacketSyncIchorianKnowledge packet, IPayloadContext context) {
		context.enqueueWork(() -> {
			IchorianSigilRegistry.reload(packet.definitions);
			HemoCapabilityAccess.getIchorianKnowledge(context.player())
					.ifPresent(knowledge -> knowledge.replaceFrom(packet.partial, packet.known));
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
