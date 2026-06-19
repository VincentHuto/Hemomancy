package com.vincenthuto.hemomancy.common.network.capa.harbinger.scars;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.IScars;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.ScarsContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.LinkedHashSet;
import java.util.Set;

public class PacketSyncScarsState implements CustomPacketPayload {
	public static final Type<PacketSyncScarsState> TYPE = new Type<>(Hemomancy.rloc("packet_sync_scars_state"));
	public static final StreamCodec<FriendlyByteBuf, PacketSyncScarsState> STREAM_CODEC =
			StreamCodec.of(PacketSyncScarsState::encode, PacketSyncScarsState::decode);

	private final int playerId;
	private final Set<ResourceLocation> known;
	private final Set<ResourceLocation> active;
	private final ItemStack fungal;

	public PacketSyncScarsState(Player player, IScars scars) {
		this(player.getId(), scars.getKnownCerebralScars(), scars.getActiveCerebralScars(), scars.getFungalScar());
	}

	private PacketSyncScarsState(int playerId, Set<ResourceLocation> known, Set<ResourceLocation> active,
			ItemStack fungal) {
		this.playerId = playerId;
		this.known = new LinkedHashSet<>(known);
		this.active = new LinkedHashSet<>(active);
		this.fungal = fungal.copy();
	}

	public static void encode(FriendlyByteBuf buf, PacketSyncScarsState msg) {
		buf.writeInt(msg.playerId);
		writeIds(buf, msg.known);
		writeIds(buf, msg.active);
		ItemStack.OPTIONAL_STREAM_CODEC.encode((RegistryFriendlyByteBuf) buf, msg.fungal);
	}

	public static PacketSyncScarsState decode(FriendlyByteBuf buf) {
		int playerId = buf.readInt();
		Set<ResourceLocation> known = readIds(buf);
		Set<ResourceLocation> active = readIds(buf);
		ItemStack fungal = ItemStack.OPTIONAL_STREAM_CODEC.decode((RegistryFriendlyByteBuf) buf);
		return new PacketSyncScarsState(playerId, known, active, fungal);
	}

	public static void handle(PacketSyncScarsState msg, IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			Minecraft mc = Minecraft.getInstance();
			if (mc.level == null) {
				return;
			}
			Entity entity = mc.level.getEntity(msg.playerId);
			if (entity instanceof Player player) {
				HemoCapabilityAccess.getScarState(player).ifPresent(scars -> {
					if (scars instanceof ScarsContainer container) {
						container.loadStateFromSync(msg.known, msg.active, msg.fungal);
					}
				});
			}
		});
	}

	private static void writeIds(FriendlyByteBuf buf, Set<ResourceLocation> ids) {
		buf.writeInt(ids.size());
		for (ResourceLocation id : ids) {
			buf.writeResourceLocation(id);
		}
	}

	private static Set<ResourceLocation> readIds(FriendlyByteBuf buf) {
		int size = buf.readInt();
		Set<ResourceLocation> ids = new LinkedHashSet<>();
		for (int i = 0; i < size; i++) {
			ids.add(buf.readResourceLocation());
		}
		return ids;
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
