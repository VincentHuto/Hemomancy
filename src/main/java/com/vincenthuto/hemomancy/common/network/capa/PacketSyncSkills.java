package com.vincenthuto.hemomancy.common.network.capa;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.SkillPointInit;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Server → Client packet: synchronises the full skill tree state
 * (every skill's {@code state} and {@code currentLevel}).
 */
public class PacketSyncSkills implements CustomPacketPayload {

	public static final Type<PacketSyncSkills> TYPE = new Type<>(Hemomancy.rloc("packet_sync_skills"));
	public static final StreamCodec<FriendlyByteBuf, PacketSyncSkills> STREAM_CODEC = StreamCodec.of(PacketSyncSkills::encode, PacketSyncSkills::decode);

	private final ListTag data;

	public PacketSyncSkills(ListTag data) {
		this.data = data;
	}

	public static void encode(FriendlyByteBuf buf, PacketSyncSkills msg) {
		CompoundTag wrapper = new CompoundTag();
		wrapper.put("skills", msg.data);
		buf.writeNbt(wrapper);
	}

	public static PacketSyncSkills decode(FriendlyByteBuf buf) {
		CompoundTag wrapper = buf.readNbt();
		ListTag list = (wrapper != null) ? wrapper.getList("skills", 10) : new ListTag();
		return new PacketSyncSkills(list);
	}

	public static void handle(final PacketSyncSkills msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			SkillPointInit.deserializeAll(msg.data);
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
