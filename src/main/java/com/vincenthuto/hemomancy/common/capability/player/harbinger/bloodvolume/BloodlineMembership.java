package com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineSavedData.NoticeKind;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineSavedData.PendingNotice;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineSavedData.PetitionResult;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.BoundThrone;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.InitiatoryDegreeEvents;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointGainEvents;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedAccessRules;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncBloodlinePetitions;
import com.vincenthuto.hemomancy.common.util.HemoText;
import com.vincenthuto.hemomancy.common.util.ProgressionGates;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * Every way a player leaves or enters a bloodline: expel, leave, dissolve and the leader's answer to a petition, with
 * the login settlement for a player who was away when it happened. The kick and ledger payloads, the Unbinding, the
 * {@code /hemo} disband and the two Unstained exits all come here.
 */
public final class BloodlineMembership {

	/** How a line ends. Only the Hematic Unbinding pays the pool out (NQ-11-9). */
	public enum Dissolution {
		DISBAND(false), UNBINDING(true), COMMAND(false), SEVERED(false);

		private final boolean paysShares;

		Dissolution(boolean paysShares) {
			this.paysShares = paysShares;
		}
	}

	public record Dissolved(int players, int npcs, int onlineReset) {
	}

	private BloodlineMembership() {
	}

	// ── Expel ──

	/** The leader's kick from the ledger screen: every check, the removal and every notice. */
	public static void expel(ServerPlayer leader, UUID target) {
		BloodlineSavedData data = data(leader.server);
		Bloodline line = data.getBloodlineForPlayer(leader.getUUID());
		if (line == null || !line.isValid()) {
			leader.displayClientMessage(Component.translatable("hemomancy.bloodline.kick.no_bloodline")
					.withStyle(ChatFormatting.RED), false);
			return;
		}
		if (!line.canManage(leader.getUUID())) {
			leader.displayClientMessage(Component.translatable("hemomancy.bloodline.kick.not_leader")
					.withStyle(ChatFormatting.RED), false);
			return;
		}
		if (target.equals(line.getLeaderUUID())) {
			leader.displayClientMessage(Component.translatable("hemomancy.bloodline.kick.cannot_kick_leader")
					.withStyle(ChatFormatting.RED), false);
			return;
		}
		if (!line.hasMember(target)) {
			leader.displayClientMessage(Component.translatable("hemomancy.bloodline.kick.not_member")
					.withStyle(ChatFormatting.GRAY), false);
			return;
		}
		ServerPlayer kicked = leader.server.getPlayerList().getPlayer(target);
		depart(leader.server, line, target, kicked);
		if (kicked != null) {
			kicked.displayClientMessage(Component.translatable("hemomancy.bloodline.kick.you_were_removed",
					leader.getName().getString()).withStyle(ChatFormatting.DARK_RED), false);
			clearThroneBond(kicked, true);
			BloodlineDisbandHelper.crumbleRemovedLedgers(kicked);
		} else {
			data.addNotice(target, PendingNotice.of(NoticeKind.EXPELLED, line, leader.getName().getString(), 0, true));
		}
		leader.displayClientMessage(Component.translatable("hemomancy.bloodline.kick.success", nameOf(leader.server, target))
				.withStyle(ChatFormatting.DARK_RED), false);
	}

	// ── Leave ──

	/** A member leaves by choice (PT-36): no pool share (NQ-11-4); the leader cannot leave (NQ-11-6). */
	public static boolean leave(ServerPlayer member) {
		BloodlineSavedData data = data(member.server);
		Bloodline line = data.getBloodlineForPlayer(member.getUUID());
		if (line == null || !line.isValid()) {
			member.displayClientMessage(Component.translatable("hemomancy.bloodline.leave.no_bloodline")
					.withStyle(ChatFormatting.GRAY), false);
			return false;
		}
		if (line.canManage(member.getUUID())) {
			member.displayClientMessage(Component.translatable("hemomancy.bloodline.leave.leader")
					.withStyle(ChatFormatting.DARK_RED), false);
			return false;
		}
		depart(member.server, line, member.getUUID(), member);
		member.displayClientMessage(HemoText.translatable("hemomancy.bloodline.leave.success", line.displayName())
				.withStyle(ChatFormatting.DARK_RED), false);
		clearThroneBond(member, true);
		BloodlineDisbandHelper.crumbleRemovedLedgers(member);
		for (ServerPlayer other : onlineMembers(member.server, line)) {
			other.displayClientMessage(Component.translatable("hemomancy.bloodline.leave.member_notice",
					member.getName().getString()).withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), false);
		}
		return true;
	}

	/**
	 * A cleansed player (the Unstained cure, or clean blood found on a player still listed in a line) leaves it. A
	 * member departs quietly; a leader's line cannot stand without them and is dissolved.
	 */
	public static void releaseCleansed(ServerPlayer player) {
		BloodlineSavedData data = data(player.server);
		data.dropPetitionsOf(player.getUUID());
		Bloodline line = data.getBloodlineForPlayer(player.getUUID());
		if (line == null) return;
		if (line.canManage(player.getUUID())) {
			dissolve(player.server, line, Dissolution.SEVERED, player, member -> member == player ? null
					: Component.translatable("message.hemomancy.unstained_cardinal_rite_events.the_founder_s_covenant_has_been")
							.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
			return;
		}
		depart(player.server, line, player.getUUID(), player);
		clearThroneBond(player, false);
		BloodlineDisbandHelper.crumbleRemovedLedgers(player);
		for (ServerPlayer other : onlineMembers(player.server, line)) {
			other.displayClientMessage(Component.translatable("hemomancy.bloodline.leave.member_notice",
					player.getName().getString()).withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), false);
		}
	}

	/**
	 * Takes {@code playerId} out of the line, records the removal for the ledger rule and resyncs the rest.
	 * {@code departed} is that player when present, or null while they are away.
	 */
	private static void depart(MinecraftServer server, Bloodline line, UUID playerId, @Nullable ServerPlayer departed) {
		BloodlineSavedData data = data(server);
		data.removeMember(line.getBloodlineUUID(), playerId);
		data.recordRemoval(line.getBloodlineUUID(), playerId, server.overworld().getGameTime());
		if (departed != null) {
			IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(departed);
			volume.setBloodLine(Bloodline.NOBLOODLINE);
			BloodVolumeEvents.syncVolume(departed, volume);
		}
		for (ServerPlayer member : onlineMembers(server, line)) {
			IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(member);
			volume.setBloodLine(line);
			BloodVolumeEvents.syncVolume(member, volume);
		}
		BloodVolumeEvents.syncBloodlinePool(server.overworld(), line);
	}

	// ── Dissolve ──

	/**
	 * Ends {@code line}: its Fanes come down, every member is unbound and loses their ledgers of it and their Throne
	 * bond, and an Unbinding pays each player member {@code pool / members}, capped by room in their vessel (NQ-11-10).
	 * Members who are away are settled at their next login. {@code actor} (the player who ended it, if one did) counts as
	 * present. {@code notice} gives each present member's line, or null.
	 */
	public static Dissolved dissolve(MinecraftServer server, Bloodline line, Dissolution how,
			@Nullable ServerPlayer actor, @Nullable Function<ServerPlayer, Component> notice) {
		BloodlineSavedData data = data(server);
		List<UUID> members = new ArrayList<>(line.getPlayerUUIDS());
		int npcs = line.getNpcMemberCount();
		float share = how.paysShares && !members.isEmpty() ? line.getBloodVolume() / members.size() : 0.0F;
		BloodlineDisbandHelper.removeOwnedFanes(server, line);
		data.disbandBloodline(line.getBloodlineUUID());
		int online = 0;
		for (UUID memberId : members) {
			ServerPlayer member = actor != null && actor.getUUID().equals(memberId) ? actor
					: server.getPlayerList().getPlayer(memberId);
			if (member == null) {
				data.addNotice(memberId, PendingNotice.of(NoticeKind.DISSOLVED, line, "", share, true));
				continue;
			}
			IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(member);
			volume.setBloodLine(Bloodline.NOBLOODLINE);
			BloodVolumeEvents.syncVolume(member, volume);
			BloodlineDisbandHelper.burnBloodlineLedgers(member, line);
			if (notice != null) {
				Component said = notice.apply(member);
				if (said != null) member.displayClientMessage(said, false);
			}
			payShare(member, share, line.displayName());
			clearThroneBond(member, true);
			online++;
		}
		return new Dissolved(members.size(), npcs, online);
	}

	private static void payShare(ServerPlayer member, float share, Component lineName) {
		if (share <= 0.0F) return;
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(member);
		double before = volume.getBloodVolume();
		volume.fill(share);
		double paid = Math.max(0.0D, volume.getBloodVolume() - before);
		BloodFlowLedger.recordApplied(member, "bloodline_share", "message.hemomancy.blood_volume_events.bloodline_share",
				Category.BLOODLINE, share, paid, 1, false,
				paid + 0.000001D < share ? "message.hemomancy.blood_flow.pool_limited" : "");
		BloodVolumeEvents.syncVolume(member, volume);
		member.displayClientMessage(HemoText.translatable("hemomancy.bloodline.share_paid", (int) Math.round(paid), lineName)
				.withStyle(ChatFormatting.DARK_RED), false);
	}

	// ── The Throne bond ──

	/**
	 * Clears {@code player}'s Covenant Throne bond (NQ-11-12): {@code covenant_throne_bound} is taken back and the
	 * respawn point goes if it is still the Throne's. A completed Living Covenant is kept. Says so when {@code tell}.
	 */
	public static boolean clearThroneBond(ServerPlayer player, boolean tell) {
		var degree = HemoCapabilityAccess.getInitiatoryDegree(player);
		BoundThrone throne = degree.getBoundThrone();
		boolean had = HarbingerAdvancementGranter.revoke(player, HarbingerAdvancementGranter.ADV_COVENANT_THRONE_BOUND);
		if (throne != null) {
			if (throne.dimension().equals(player.getRespawnDimension()) && throne.respawn().equals(player.getRespawnPosition())) {
				player.setRespawnPosition(Level.OVERWORLD, null, 0.0F, false, false);
			}
			degree.setBoundThrone(null);
			InitiatoryDegreeEvents.syncDegree(player, degree);
			had = true;
		}
		if (had && tell) {
			player.displayClientMessage(Component.translatable("hemomancy.bloodline.throne_bond_cleared")
					.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), false);
		}
		return had;
	}

	// ── Petitions (PT-35) ──

	/** A signed ledger used by a non-member: the gates, then a petition that waits for the leader. */
	public static void petition(ServerPlayer applicant, Bloodline line) {
		if (!ProgressionGates.hasDegree(applicant, 1)) {
			applicant.displayClientMessage(Component.translatable("message.hemomancy.unsigned_ledger_item.needs_degree")
					.withStyle(ChatFormatting.DARK_RED), true);
			return;
		}
		if (!HemoCapabilityAccess.getBloodVolume(applicant).isActive()) {
			applicant.displayClientMessage(Component.translatable("message.hemomancy.unsigned_ledger_item.needs_active_blood")
					.withStyle(ChatFormatting.DARK_RED), true);
			return;
		}
		BloodlineSavedData data = data(applicant.server);
		PetitionResult result = data.addPetition(line.getBloodlineUUID(), applicant.getUUID(),
				applicant.getName().getString());
		String key = switch (result) {
			case ADDED -> "message.hemomancy.unsigned_ledger_item.petition_sent";
			case ALREADY_WAITING -> "message.hemomancy.unsigned_ledger_item.petition_waiting";
			case FULL -> "message.hemomancy.unsigned_ledger_item.petition_full";
		};
		applicant.displayClientMessage(HemoText.translatable(key, line.displayName()).withStyle(ChatFormatting.DARK_RED), true);
		if (result != PetitionResult.ADDED) return;
		ServerPlayer leader = applicant.server.getPlayerList().getPlayer(line.getLeaderUUID());
		if (leader != null) {
			leader.displayClientMessage(Component.translatable("hemomancy.bloodline.petition.arrived",
					applicant.getName().getString()).withStyle(ChatFormatting.DARK_RED), false);
			sendPetitions(leader);
		}
	}

	/** The leader's Admit or Refuse on the ledger screen (NQ-11-7). Admit re-checks the gates of an online applicant. */
	public static void answerPetition(ServerPlayer leader, UUID applicant, boolean admit) {
		BloodlineSavedData data = data(leader.server);
		Bloodline line = data.getBloodlineLedBy(leader.getUUID());
		if (line == null) {
			leader.displayClientMessage(Component.translatable("hemomancy.ledger.disband.not_leader")
					.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), false);
			return;
		}
		String name = data.petitions(line.getBloodlineUUID()).get(applicant);
		if (name == null || !data.removePetition(line.getBloodlineUUID(), applicant)) {
			leader.displayClientMessage(Component.translatable("hemomancy.bloodline.petition.gone")
					.withStyle(ChatFormatting.GRAY), false);
			sendPetitions(leader);
			return;
		}
		ServerPlayer joiner = leader.server.getPlayerList().getPlayer(applicant);
		if (!admit) {
			if (joiner != null) {
				joiner.displayClientMessage(HemoText.translatable("hemomancy.bloodline.petition.refused", line.displayName())
						.withStyle(ChatFormatting.DARK_RED), false);
			} else {
				data.addNotice(applicant, PendingNotice.of(NoticeKind.REFUSED, line, leader.getName().getString(), 0, false));
			}
			leader.displayClientMessage(Component.translatable("hemomancy.bloodline.petition.refused_leader", name)
					.withStyle(ChatFormatting.GRAY), false);
			sendPetitions(leader);
			return;
		}
		Bloodline other = data.getBloodlineForPlayer(applicant);
		boolean eligible = other == null && (joiner == null || canJoin(joiner));
		if (!eligible) {
			leader.displayClientMessage(Component.translatable("hemomancy.bloodline.petition.not_eligible", name)
					.withStyle(ChatFormatting.GRAY), false);
			sendPetitions(leader);
			return;
		}
		data.addMember(line.getBloodlineUUID(), applicant);
		data.dropPetitionsOf(applicant);
		if (joiner != null) {
			welcome(joiner, line);
		} else {
			data.addNotice(applicant, PendingNotice.of(NoticeKind.ADMITTED, line, leader.getName().getString(), 0, false));
		}
		List<ServerPlayer> present = onlineMembers(leader.server, line);
		if (!present.contains(leader)) present.add(leader);
		for (ServerPlayer member : present) {
			IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(member);
			volume.setBloodLine(line);
			BloodVolumeEvents.syncVolume(member, volume);
			if (!member.getUUID().equals(applicant)) {
				member.displayClientMessage(HemoText.translatable("message.hemomancy.unsigned_ledger_item.has_joined_your_bloodline",
						name).withStyle(ChatFormatting.DARK_RED), false);
			}
		}
		BloodVolumeEvents.syncBloodlinePool(leader.server.overworld(), line);
		sendPetitions(leader);
	}

	private static boolean canJoin(ServerPlayer player) {
		return ProgressionGates.hasDegree(player, 1) && HemoCapabilityAccess.getBloodVolume(player).isActive()
				&& !UnstainedAccessRules.blocksHarbingerProgress(HemoCapabilityAccess.getUnstainedProgress(player));
	}

	private static void welcome(ServerPlayer joiner, Bloodline line) {
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(joiner);
		volume.setBloodLine(line);
		BloodVolumeEvents.syncVolume(joiner, volume);
		joiner.displayClientMessage(HemoText.translatable("message.hemomancy.unsigned_ledger_item.you_have_joined",
				line.displayName()).withStyle(ChatFormatting.DARK_RED), false);
		SkillPointGainEvents.onBloodlineJoined(joiner);
	}

	/** Sends the leader their line's waiting petitions; an empty list to anyone who leads no line. */
	public static void sendPetitions(ServerPlayer player) {
		Bloodline line = data(player.server).getBloodlineLedBy(player.getUUID());
		Map<UUID, String> waiting = line == null ? Map.of() : data(player.server).petitions(line.getBloodlineUUID());
		PacketHandler.sendToPlayer(player, PacketSyncBloodlinePetitions.of(waiting));
	}

	// ── Login settlement ──

	/**
	 * Settles what happened while {@code player} was away (T1). A dead player waits for the respawn, as a pending
	 * delivery does. A leader is also told how many petitions wait.
	 */
	public static void settle(ServerPlayer player) {
		if (player.isDeadOrDying()) return;
		BloodlineSavedData data = data(player.server);
		for (PendingNotice notice : data.takeNotices(player.getUUID())) {
			switch (notice.kind()) {
				case EXPELLED -> player.displayClientMessage(HemoText.translatable("hemomancy.bloodline.settle.expelled",
						notice.lineName(), notice.actor()).withStyle(ChatFormatting.DARK_RED), false);
				case DISSOLVED -> {
					player.displayClientMessage(HemoText.translatable("hemomancy.bloodline.settle.dissolved",
							notice.lineName()).withStyle(ChatFormatting.DARK_RED), false);
					payShare(player, notice.bloodOwed(), notice.lineName());
				}
				case ADMITTED -> {
					Bloodline line = data.getBloodline(notice.lineId());
					if (line != null && line.hasMember(player.getUUID())) welcome(player, line);
				}
				case REFUSED -> player.displayClientMessage(HemoText.translatable("hemomancy.bloodline.petition.refused",
						notice.lineName()).withStyle(ChatFormatting.DARK_RED), false);
			}
			if (notice.clearThrone()) clearThroneBond(player, true);
			if (notice.kind() == NoticeKind.EXPELLED) BloodlineDisbandHelper.crumbleRemovedLedgers(player);
		}
		Bloodline led = data.getBloodlineLedBy(player.getUUID());
		if (led != null) {
			int waiting = data.petitions(led.getBloodlineUUID()).size();
			if (waiting > 0) {
				player.displayClientMessage(Component.translatable("hemomancy.bloodline.petition.waiting_count", waiting)
						.withStyle(ChatFormatting.DARK_RED), false);
			}
			sendPetitions(player);
		}
	}

	// ── Helpers ──

	private static BloodlineSavedData data(MinecraftServer server) {
		ServerLevel overworld = server.overworld();
		return BloodlineSavedData.get(overworld);
	}

	private static List<ServerPlayer> onlineMembers(MinecraftServer server, Bloodline line) {
		List<ServerPlayer> online = new ArrayList<>();
		for (UUID id : line.getPlayerUUIDS()) {
			ServerPlayer member = server.getPlayerList().getPlayer(id);
			if (member != null) online.add(member);
		}
		return online;
	}

	private static String nameOf(MinecraftServer server, UUID id) {
		ServerPlayer online = server.getPlayerList().getPlayer(id);
		if (online != null) return online.getName().getString();
		var cache = server.getProfileCache();
		return cache == null ? id.toString() : cache.get(id).map(GameProfile::getName).orElse(id.toString());
	}
}
