package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.rite.IchorianKnowledge;
import com.vincenthuto.hemomancy.common.rite.sigil.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.*;

public record PacketSyncIchorianKnowledge(Map<ResourceLocation, BitSet> partial, Set<ResourceLocation> known,
		Map<ResourceLocation, IchorianSigilDefinition> definitions)
		implements CustomPacketPayload {
	public static final Type<PacketSyncIchorianKnowledge> TYPE =
			new Type<>(ResourceLocation.fromNamespaceAndPath(
					"hemomancy", "packet_sync_ichorian_knowledge"));
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
			writeDefinition(buffer, definition);
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
			IchorianSigilDefinition definition = readDefinition(buffer);
			definitions.put(definition.id(), definition);
		}
		return new PacketSyncIchorianKnowledge(partial, known, definitions);
	}

	static void writeDefinition(FriendlyByteBuf buffer, IchorianSigilDefinition definition) {
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
		buffer.writeVarInt(definition.connections().size());
		for (IchorianSigilDefinition.Connection connection : definition.connections()) {
			buffer.writeVarInt(connection.from());
			buffer.writeVarInt(connection.to());
		}
		buffer.writeBoolean(definition.awakenedForm().isPresent());
		definition.awakenedForm().ifPresent(form -> writeAnatomy(buffer, form));
	}

	static IchorianSigilDefinition readDefinition(FriendlyByteBuf buffer) {
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
		List<IchorianSigilDefinition.Connection> connections = new ArrayList<>();
		for (int edge = 0, edgeCount = buffer.readVarInt(); edge < edgeCount; edge++) {
			connections.add(new IchorianSigilDefinition.Connection(
					buffer.readVarInt(), buffer.readVarInt()));
		}
		Optional<IchorianSigilAnatomy> anatomy = Optional.empty();
		if (buffer.readBoolean()) {
			anatomy = IchorianSigilAnatomyValidator.validate(nodes.size(), readAnatomy(buffer)).form();
		}
		return new IchorianSigilDefinition(id, kind, tier, color, name, purpose,
				stability, capacity, nodes, connections, anatomy);
	}

	private static void writeAnatomy(FriendlyByteBuf buffer, IchorianSigilAnatomy form) {
		writeVec3(buffer, form.forward());
		buffer.writeEnum(form.animation().style());
		buffer.writeFloat(form.animation().pulse());
		buffer.writeFloat(form.animation().flex());
		buffer.writeFloat(form.animation().lag());
		buffer.writeVarInt(form.landmarks().size());
		for (IchorianSigilAnatomy.Landmark landmark : form.landmarks()) {
			buffer.writeVarInt(landmark.source());
			writeVec3(buffer, landmark.position());
			buffer.writeEnum(landmark.role());
			buffer.writeFloat(landmark.radius());
		}
		buffer.writeVarInt(form.vessels().size());
		for (IchorianSigilAnatomy.Vessel vessel : form.vessels()) {
			buffer.writeVarInt(vessel.from());
			buffer.writeVarInt(vessel.to());
			buffer.writeFloat(vessel.thickness());
		}
		buffer.writeVarInt(form.membranes().size());
		for (IchorianSigilAnatomy.Membrane membrane : form.membranes()) {
			buffer.writeVarInt(membrane.a());
			buffer.writeVarInt(membrane.b());
			buffer.writeVarInt(membrane.c());
		}
	}

	private static IchorianSigilAnatomy readAnatomy(FriendlyByteBuf buffer) {
		Vec3 forward = readVec3(buffer);
		var animation = new IchorianSigilAnatomy.Animation(
				buffer.readEnum(IchorianSigilAnatomy.Style.class),
				buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
		List<IchorianSigilAnatomy.Landmark> landmarks = new ArrayList<>();
		for (int index = 0, count = buffer.readVarInt(); index < count; index++) {
			landmarks.add(new IchorianSigilAnatomy.Landmark(
					buffer.readVarInt(), readVec3(buffer),
					buffer.readEnum(IchorianSigilAnatomy.Role.class), buffer.readFloat()));
		}
		List<IchorianSigilAnatomy.Vessel> vessels = new ArrayList<>();
		for (int index = 0, count = buffer.readVarInt(); index < count; index++) {
			vessels.add(new IchorianSigilAnatomy.Vessel(
					buffer.readVarInt(), buffer.readVarInt(), buffer.readFloat()));
		}
		List<IchorianSigilAnatomy.Membrane> membranes = new ArrayList<>();
		for (int index = 0, count = buffer.readVarInt(); index < count; index++) {
			membranes.add(new IchorianSigilAnatomy.Membrane(
					buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt()));
		}
		return new IchorianSigilAnatomy(forward, animation, landmarks, vessels, membranes);
	}

	private static void writeVec3(FriendlyByteBuf buffer, Vec3 vector) {
		buffer.writeDouble(vector.x);
		buffer.writeDouble(vector.y);
		buffer.writeDouble(vector.z);
	}

	private static Vec3 readVec3(FriendlyByteBuf buffer) {
		return new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
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
