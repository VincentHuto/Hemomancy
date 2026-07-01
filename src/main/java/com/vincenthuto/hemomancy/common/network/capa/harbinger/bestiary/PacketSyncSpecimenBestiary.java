package com.vincenthuto.hemomancy.common.network.capa.harbinger.bestiary;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bestiary.SpecimenBestiaryProgress;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PacketSyncSpecimenBestiary implements CustomPacketPayload {
	public static final Type<PacketSyncSpecimenBestiary> TYPE =
			new Type<>(Hemomancy.rloc("sync_specimen_bestiary"));
	public static final StreamCodec<FriendlyByteBuf, PacketSyncSpecimenBestiary> STREAM_CODEC =
			StreamCodec.of(PacketSyncSpecimenBestiary::encode, PacketSyncSpecimenBestiary::decode);

	private final List<String> recordedSpecimens;
	private final List<String> surrenderedSpecimens;
	private final List<String> recordedMorphlingLayers;

	public PacketSyncSpecimenBestiary(SpecimenBestiaryProgress progress) {
		this(progress.recordedSpecimens(), progress.surrenderedSpecimens(), progress.recordedMorphlingLayers());
	}

	private PacketSyncSpecimenBestiary(Collection<String> recordedSpecimens, Collection<String> surrenderedSpecimens,
			Collection<String> recordedMorphlingLayers) {
		this.recordedSpecimens = new ArrayList<>(recordedSpecimens);
		this.surrenderedSpecimens = new ArrayList<>(surrenderedSpecimens);
		this.recordedMorphlingLayers = new ArrayList<>(recordedMorphlingLayers);
	}

	public static void encode(FriendlyByteBuf buf, PacketSyncSpecimenBestiary msg) {
		writeStringList(buf, msg.recordedSpecimens);
		writeStringList(buf, msg.surrenderedSpecimens);
		writeStringList(buf, msg.recordedMorphlingLayers);
	}

	public static PacketSyncSpecimenBestiary decode(FriendlyByteBuf buf) {
		return new PacketSyncSpecimenBestiary(readStringList(buf), readStringList(buf), readStringList(buf));
	}

	public static void handle(PacketSyncSpecimenBestiary msg, IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			Player player = ctx.player();
			if (player != null) {
				HemoCapabilityAccess.getSpecimenBestiary(player)
						.ifPresent(progress -> progress.replaceFromSync(msg.recordedSpecimens,
								msg.surrenderedSpecimens, msg.recordedMorphlingLayers));
			}
		});
	}

	private static void writeStringList(FriendlyByteBuf buf, List<String> values) {
		buf.writeInt(values.size());
		for (String value : values) {
			buf.writeUtf(value);
		}
	}

	private static List<String> readStringList(FriendlyByteBuf buf) {
		int count = buf.readInt();
		List<String> values = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			values.add(buf.readUtf());
		}
		return values;
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
