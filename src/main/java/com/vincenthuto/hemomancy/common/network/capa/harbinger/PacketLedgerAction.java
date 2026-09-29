package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.*;
import com.vincenthuto.hemomancy.common.event.worldevent.FoundingFaneSavedData;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerRecruitmentRules;
import com.vincenthuto.hemomancy.common.succession.ProfessionalHarbingerEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Client → Server: The ledger GUI sends an action request.
 * <p>
 * Actions:
 * <ul>
	 *   <li>0 = Summon recruited NPC Harbingers (must be inside the bloodline's Founding Fane)</li>
	 *   <li>1 = Recall to the fane recall point (from anywhere)</li>
	 *   <li>2 = Set fane recall point to current position (leader only, must be inside the fane)</li>
 * </ul>
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public class PacketLedgerAction implements CustomPacketPayload {
	private static final TicketType<UUID> RECRUIT_LOOKUP_TICKET =
			TicketType.create("hemomancy_ledger_recruit", UUID::compareTo, 200);
	private static final Map<UUID, PendingSummon> PENDING_SUMMONS = new HashMap<>();
	private record PendingSummon(UUID bloodlineId, ResourceKey<Level> dimension,
			long expiresAt, Set<UUID> recruits) {}
	private enum SummonResult { MOVED, PENDING, UNAVAILABLE }

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

	private static UUID getFaneOwner(Bloodline bloodline, ServerPlayer player) {
		return bloodline.isValid() ? bloodline.getLeaderUUID() : player.getUUID();
	}

	private static boolean isInsideOwnedFane(ServerPlayer player, UUID ownerUuid) {
		if (!(player.level() instanceof ServerLevel currentLevel)) {
			return false;
		}
		return FoundingFaneSavedData.get(currentLevel).isWithinFane(ownerUuid, player.blockPosition());
	}

	private static ServerLevel findPreferredFaneLevel(ServerPlayer player, UUID ownerUuid) {
		if (player.level() instanceof ServerLevel currentLevel
				&& FoundingFaneSavedData.get(currentLevel).hasFane(ownerUuid)) {
			return currentLevel;
		}

		ServerLevel overworld = player.server.overworld();
		if (FoundingFaneSavedData.get(overworld).hasFane(ownerUuid)) {
			return overworld;
		}

		for (ServerLevel level : player.server.getAllLevels()) {
			if (FoundingFaneSavedData.get(level).hasFane(ownerUuid)) {
				return level;
			}
		}
		return null;
	}

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

		UUID faneOwner = getFaneOwner(bloodline, player);
		if (!isInsideOwnedFane(player, faneOwner)) {
			player.displayClientMessage(
					Component.translatable("hemomancy.ledger.summon.not_in_lodge")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);
			return;
		}

		ServerLevel sLevel = (ServerLevel) player.level();
		int summoned = 0;
		int unavailable = 0;
		Set<UUID> pending = new LinkedHashSet<>();

		for (UUID npcUUID : bloodline.getNpcMemberUUIDs()) {
			Entity existing = sLevel.getEntity(npcUUID);
			if (existing != null && existing.distanceTo(player) < 16.0) {
				continue;
			}

			switch (teleportNpc(sLevel, player, bloodline, npcUUID)) {
				case MOVED -> summoned++;
				case PENDING -> pending.add(npcUUID);
				case UNAVAILABLE -> unavailable++;
			}
		}
		if (pending.isEmpty()) PENDING_SUMMONS.remove(player.getUUID());
		else PENDING_SUMMONS.put(player.getUUID(), new PendingSummon(
				bloodline.getBloodlineUUID(), sLevel.dimension(),
				player.server.getTickCount() + 100L, pending));

		if (summoned > 0) {
			sLevel.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT,
					SoundSource.PLAYERS, 1.0f, 0.7f);
			player.displayClientMessage(
					Component.translatable("hemomancy.ledger.summon.success", summoned)
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
					false);
		} else if (unavailable == 0 && pending.isEmpty()) {
			player.displayClientMessage(
					Component.translatable("hemomancy.ledger.summon.already_near")
							.withStyle(ChatFormatting.GRAY),
					false);
		}
		if (unavailable > 0) {
			player.displayClientMessage(Component.translatable(
					"hemomancy.ledger.summon.unavailable")
					.withStyle(ChatFormatting.DARK_RED), false);
		}
		if (!pending.isEmpty()) {
			player.displayClientMessage(Component.translatable(
					"hemomancy.ledger.summon.searching")
					.withStyle(ChatFormatting.GRAY), false);
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

		UUID faneOwner = getFaneOwner(bloodline, player);
		ServerLevel targetLevel = findPreferredFaneLevel(player, faneOwner);
		if (targetLevel == null) {
			player.displayClientMessage(
					Component.translatable("hemomancy.ledger.recall.no_lodge")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);
			return;
		}

		FoundingFaneSavedData data = FoundingFaneSavedData.get(targetLevel);
		BlockPos recall = data.getRecallPoint(faneOwner);
		if (recall == null) {
			player.displayClientMessage(
					Component.translatable("hemomancy.ledger.recall.no_lodge")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);
			return;
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

		if (!bloodline.canManage(player.getUUID())) {
			player.displayClientMessage(
					Component.translatable("hemomancy.ledger.recall.not_leader")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);
			return;
		}

		UUID faneOwner = getFaneOwner(bloodline, player);
		if (!(player.level() instanceof ServerLevel currentLevel)) {
			return;
		}
		FoundingFaneSavedData data = FoundingFaneSavedData.get(currentLevel);

		if (data.setRecallPoint(faneOwner, player.blockPosition())) {
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

		if (!bloodline.canManage(player.getUUID())) {
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
			BloodlineDisbandHelper.removeOwnedFanes(player.server, bloodline);
			volume.setBloodLine(Bloodline.NOBLOODLINE);
			BloodVolumeEvents.syncVolume(player, volume);
			BloodlineDisbandHelper.burnBloodlineLedgers(player, bloodline);
			player.displayClientMessage(
					Component.translatable("hemomancy.ledger.disband.missing")
							.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),
					false);
			return;
		}

		int playerCount = globalLine.getPlayerUUIDS().size();
		int npcCount = globalLine.getNpcMemberCount();
		BloodlineDisbandHelper.removeOwnedFanes(player.server, globalLine);
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

	private static SummonResult teleportNpc(ServerLevel level, ServerPlayer player, Bloodline bloodline, UUID npcUUID) {
		if (findLoadedRecruit(level, npcUUID) != null) {
			return moveLoadedRecruit(level, player, npcUUID) ? SummonResult.MOVED : SummonResult.UNAVAILABLE;
		}
		if (!loadRecordedOutpost(level, bloodline, npcUUID)) return SummonResult.UNAVAILABLE;
		return moveLoadedRecruit(level, player, npcUUID) ? SummonResult.MOVED : SummonResult.PENDING;
	}

	private static boolean moveLoadedRecruit(ServerLevel level, ServerPlayer player, UUID npcUUID) {
		double targetX = player.getX() + (player.getRandom().nextDouble() - 0.5) * 3.0;
		double targetY = player.getY();
		double targetZ = player.getZ() + (player.getRandom().nextDouble() - 0.5) * 3.0;
		Entity found = findLoadedRecruit(level, npcUUID);
		if (!(found instanceof ProfessionalHarbingerEntity npc) || !npc.isAlive()) return false;
		if (npc.level() == level) {
			npc.teleportTo(targetX, targetY, targetZ);
			return true;
		}
		Entity moved = npc.changeDimension(new DimensionTransition(level,
				new Vec3(targetX, targetY, targetZ), Vec3.ZERO,
				npc.getYRot(), npc.getXRot(), DimensionTransition.DO_NOTHING));
		return moved != null && moved.level() == level && moved.getUUID().equals(npcUUID);
	}

	private static Entity findLoadedRecruit(ServerLevel level, UUID npcUUID) {
		for (ServerLevel dim : level.getServer().getAllLevels()) {
			Entity found = dim.getEntity(npcUUID);
			if (found != null) return found;
		}
		return null;
	}

	private static boolean loadRecordedOutpost(ServerLevel level, Bloodline bloodline, UUID npcUUID) {
		int index = bloodline.getNpcMemberUUIDs().indexOf(npcUUID);
		if (index < 0 || index >= bloodline.getNpcMemberOutposts().size()) return false;
		var origin = HarbingerRecruitmentRules.outpostOrigin(
				bloodline.getNpcMemberOutposts().get(index));
		if (origin.isEmpty()) return false;
		var site = origin.get();
		ServerLevel source = level.getServer().getLevel(
				ResourceKey.create(Registries.DIMENSION, site.dimension()));
		if (source == null) return false;
		source.resetEmptyTime();
		// Chunk data can load before its entities enter the UUID lookup; the pending call retries on later ticks.
		for (int x = site.minChunkX(); x <= site.maxChunkX(); x++) {
			for (int z = site.minChunkZ(); z <= site.maxChunkZ(); z++) {
				ChunkPos chunk = new ChunkPos(x, z);
				source.getChunkSource().addRegionTicket(RECRUIT_LOOKUP_TICKET, chunk, 2, npcUUID, true);
				source.getChunkAt(new BlockPos(x * 16, 0, z * 16));
				if (source.getEntity(npcUUID) != null) return true;
			}
		}
		return true;
	}

	@SubscribeEvent
	public static void onServerTick(ServerTickEvent.Post event) {
		var server = event.getServer();
		Iterator<Map.Entry<UUID, PendingSummon>> requests = PENDING_SUMMONS.entrySet().iterator();
		while (requests.hasNext()) {
			var request = requests.next();
			var pending = request.getValue();
			ServerPlayer player = server.getPlayerList().getPlayer(request.getKey());
			if (player == null || !player.level().dimension().equals(pending.dimension())) {
				requests.remove();
				continue;
			}
			Bloodline bloodline = HemoCapabilityAccess.getBloodVolume(player)
					.map(IBloodVolume::getBloodLine).orElse(Bloodline.NOBLOODLINE);
			if (!bloodline.isValid() || !bloodline.getBloodlineUUID().equals(pending.bloodlineId())
					|| !isInsideOwnedFane(player, getFaneOwner(bloodline, player))) {
				requests.remove();
				continue;
			}
			int summoned = 0;
			Iterator<UUID> recruits = pending.recruits().iterator();
			while (recruits.hasNext()) {
				UUID npcId = recruits.next();
				if (!bloodline.hasNpcMember(npcId)) {
					recruits.remove();
				} else if (findLoadedRecruit(player.serverLevel(), npcId) != null) {
					if (moveLoadedRecruit(player.serverLevel(), player, npcId)) summoned++;
					recruits.remove();
				}
			}
			if (summoned > 0) {
				player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT,
						SoundSource.PLAYERS, 1.0f, 0.7f);
				player.displayClientMessage(Component.translatable(
						"hemomancy.ledger.summon.success", summoned)
						.withStyle(ChatFormatting.DARK_RED), false);
			}
			if (pending.recruits().isEmpty()) requests.remove();
			else if (server.getTickCount() >= pending.expiresAt()) {
				player.displayClientMessage(Component.translatable(
						"hemomancy.ledger.summon.unavailable")
						.withStyle(ChatFormatting.DARK_RED), false);
				requests.remove();
			}
		}
	}

	@SubscribeEvent
	public static void onServerStopped(ServerStoppedEvent event) {
		PENDING_SUMMONS.clear();
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
