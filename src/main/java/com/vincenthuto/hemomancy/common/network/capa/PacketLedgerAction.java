package com.vincenthuto.hemomancy.common.network.capa;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineDisbandHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineSavedData;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.rite.harbinger.CrimsonLodgeEvents;
import com.vincenthuto.hemomancy.common.rite.harbinger.CrimsonLodgeSavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

/**
 * Client → Server: The ledger GUI sends an action request.
 * <p>
 * Actions:
 * <ul>
 *   <li>0 = Summon recruited NPC Harbingers (must be inside lodge)</li>
 *   <li>1 = Recall to lodge recall point (from anywhere)</li>
 *   <li>2 = Set recall point to current position (leader only, must be inside lodge)</li>
 * </ul>
 */
public class PacketLedgerAction implements CustomPacketPayload {

	public static final Type<PacketLedgerAction> TYPE = new Type<>(Hemomancy.rloc("packet_ledger_action"));
	public static final StreamCodec<FriendlyByteBuf, PacketLedgerAction> STREAM_CODEC = StreamCodec.of(PacketLedgerAction::encode, PacketLedgerAction::decode);

	public static final int ACTION_SUMMON_NPCS = 0;
	public static final int ACTION_RECALL_TO_LODGE = 1;
	public static final int ACTION_SET_RECALL_POINT = 2;
	public static final int ACTION_DISBAND_BLOODLINE = 3;

	private final int action;

	public PacketLedgerAction(int action) {
		this.action = action;
	}

	public static void encode(FriendlyByteBuf buf, PacketLedgerAction msg) {
		buf.writeInt(msg.action);
	}

	public static PacketLedgerAction decode(FriendlyByteBuf buf) {
		return new PacketLedgerAction(buf.readInt());
	}

	public static void handle(final PacketLedgerAction msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			Player packetPlayer = ctx.player();
			if (!(packetPlayer instanceof ServerPlayer player)) return;

			HemoCapabilityAccess.getBloodVolume(player).ifPresent(volume -> {
				switch (msg.action) {
					case ACTION_SUMMON_NPCS -> handleNpcSummon(player, volume);
					case ACTION_RECALL_TO_LODGE -> handleLodgeRecall(player, volume);
					case ACTION_SET_RECALL_POINT -> handleSetRecallPoint(player, volume);
					case ACTION_DISBAND_BLOODLINE -> handleDisbandBloodline(player, volume);
				}
			});
		});
	}

	// ── Action Handlers (moved from UnsignedLedgerItem) ──

	private static void handleNpcSummon(ServerPlayer player, IBloodVolume volume) {
		Bloodline bloodline = volume.getBloodLine();

		if (!bloodline.isValid()) {
			player.displayClientMessage(
					Component.translatable("hemomancy.ledger.summon.no_bloodline")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);
			return;
		}

		if (bloodline.getNpcMemberCount() == 0) {
			player.displayClientMessage(
					Component.translatable("hemomancy.ledger.summon.no_npcs")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);
			return;
		}

		if (!CrimsonLodgeEvents.isInCrimsonLodge(player)) {
			player.displayClientMessage(
					Component.translatable("hemomancy.ledger.summon.not_in_lodge")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);
			return;
		}

		ServerLevel sLevel = (ServerLevel) player.level();
		int summoned = 0;

		for (UUID npcUUID : bloodline.getNpcMemberUUIDs()) {
			Entity existing = sLevel.getEntity(npcUUID);
			if (existing != null && existing.distanceTo(player) < 16.0) {
				continue;
			}

			if (teleportOrSpawnNpc(sLevel, player, npcUUID)) {
				summoned++;
			}
		}

		if (summoned > 0) {
			sLevel.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT,
					SoundSource.PLAYERS, 1.0f, 0.7f);
			player.displayClientMessage(
					Component.translatable("hemomancy.ledger.summon.success", summoned)
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
					false);
		} else {
			player.displayClientMessage(
					Component.translatable("hemomancy.ledger.summon.already_near")
							.withStyle(ChatFormatting.GRAY),
					false);
		}
	}

	private static void handleLodgeRecall(ServerPlayer player, IBloodVolume volume) {
		Bloodline bloodline = volume.getBloodLine();

		if (!bloodline.isValid()) {
			player.displayClientMessage(
					Component.translatable("hemomancy.ledger.recall.no_bloodline")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);
			return;
		}

		ServerLevel overworld = player.server.overworld();
		CrimsonLodgeSavedData data = CrimsonLodgeSavedData.get(overworld);
		CrimsonLodgeSavedData.LodgeEntry lodge = data.getLodgeForOwner(bloodline.getLeaderUUID());

		if (lodge == null) {
			player.displayClientMessage(
					Component.translatable("hemomancy.ledger.recall.no_lodge")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);
			return;
		}

		BlockPos recall = lodge.recallPoint();
		ServerLevel targetLevel = null;

		for (ServerLevel dim : player.server.getAllLevels()) {
			if (dim.dimension().location().toString().equals(lodge.dimension())) {
				targetLevel = dim;
				break;
			}
		}
		if (targetLevel == null) {
			targetLevel = player.server.overworld();
		}

		player.teleportTo(targetLevel,
				recall.getX() + 0.5, recall.getY() + 1.0, recall.getZ() + 0.5,
				player.getYRot(), player.getXRot());

		targetLevel.playSound(null, recall, SoundEvents.ENDERMAN_TELEPORT,
				SoundSource.PLAYERS, 1.0f, 0.7f);
		player.displayClientMessage(
				Component.translatable("hemomancy.ledger.recall.success")
						.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
				false);
	}

	private static void handleSetRecallPoint(ServerPlayer player, IBloodVolume volume) {
		Bloodline bloodline = volume.getBloodLine();

		if (!bloodline.isValid()) {
			player.displayClientMessage(
					Component.translatable("hemomancy.ledger.recall.no_bloodline")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);
			return;
		}

		if (!bloodline.getLeaderUUID().equals(player.getUUID())) {
			player.displayClientMessage(
					Component.translatable("hemomancy.ledger.recall.not_leader")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);
			return;
		}

		ServerLevel overworld = player.server.overworld();
		CrimsonLodgeSavedData data = CrimsonLodgeSavedData.get(overworld);

		if (data.setRecallPoint(player.getUUID(), player.blockPosition())) {
			player.level().playSound(null, player.blockPosition(), SoundEvents.RESPAWN_ANCHOR_SET_SPAWN,
					SoundSource.PLAYERS, 1.0f, 1.0f);
			player.displayClientMessage(
					Component.translatable("hemomancy.ledger.recall.point_set")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
					false);
		} else {
			player.displayClientMessage(
					Component.translatable("hemomancy.ledger.recall.out_of_range")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);
		}
	}

	private static void handleDisbandBloodline(ServerPlayer player, IBloodVolume volume) {
		Bloodline bloodline = volume.getBloodLine();

		if (!bloodline.isValid()) {
			player.displayClientMessage(
					Component.translatable("hemomancy.ledger.disband.no_bloodline")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);
			return;
		}

		if (!bloodline.getLeaderUUID().equals(player.getUUID())) {
			player.displayClientMessage(
					Component.translatable("hemomancy.ledger.disband.not_leader")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);
			return;
		}

		ServerLevel overworld = player.server.overworld();
		BloodlineSavedData savedData = BloodlineSavedData.get(overworld);
		Bloodline globalLine = savedData.getBloodline(bloodline.getBloodlineUUID());
		if (globalLine == null) {
			BloodlineDisbandHelper.removeOwnedSanctums(player.server, bloodline);
			volume.setBloodLine(Bloodline.NOBLOODLINE);
			BloodVolumeEvents.syncVolume(player, volume);
			player.displayClientMessage(
					Component.translatable("hemomancy.ledger.disband.missing")
							.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),
					false);
			return;
		}

		int playerCount = globalLine.getPlayerUUIDS().size();
		int npcCount = globalLine.getNpcMemberCount();
		BloodlineDisbandHelper.removeOwnedSanctums(player.server, globalLine);
		savedData.disbandBloodline(globalLine.getBloodlineUUID());

		BloodlineDisbandHelper.resetOnlineMembers(player.server, globalLine, member ->
				member.getUUID().equals(player.getUUID()) ? null
						: Component.translatable("hemomancy.ledger.disband.member_notice", player.getName().getString())
								.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));

		player.displayClientMessage(
				Component.translatable("hemomancy.ledger.disband.success", playerCount, npcCount)
						.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
				false);
	}

	private static boolean teleportOrSpawnNpc(ServerLevel level, ServerPlayer player, UUID npcUUID) {
		double targetX = player.getX() + (player.getRandom().nextDouble() - 0.5) * 3.0;
		double targetY = player.getY();
		double targetZ = player.getZ() + (player.getRandom().nextDouble() - 0.5) * 3.0;

		for (ServerLevel dim : level.getServer().getAllLevels()) {
			Entity found = dim.getEntity(npcUUID);
			if (found != null) {
				found.teleportTo(targetX, targetY, targetZ);
				return true;
			}
		}

		EntityType<?>[] harbingerTypes = {
			EntityInit.harbinger_vicar.get(),
			EntityInit.harbinger_alchemist.get()
		};
		EntityType<?> type = harbingerTypes[(npcUUID.hashCode() & Integer.MAX_VALUE) % harbingerTypes.length];

		Entity spawned = type.create(level);
		if (spawned != null) {
			spawned.setUUID(npcUUID);
			spawned.setPos(targetX, targetY, targetZ);
			level.addFreshEntity(spawned);
			return true;
		}
		return false;
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
