package com.vincenthuto.hemomancy.common.network.capa;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;

import com.vincenthuto.hemomancy.common.capability.player.degree.EnumInitiatoryDegree;
import com.vincenthuto.hemomancy.common.capability.player.skill.EnumSkillStates;
import com.vincenthuto.hemomancy.common.capability.player.skill.SkillPoint;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.init.SkillPointInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;

import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Client → Server packet: player requests to unlock or level-up a skill.
 * The server validates prerequisites, checks blood cost, drains blood,
 * applies the change, and syncs back to the client.
 */
public class PacketUnlockSkill implements CustomPacketPayload {

	public static final Type<PacketUnlockSkill> TYPE = new Type<>(Hemomancy.rloc("packet_unlock_skill"));
	public static final StreamCodec<FriendlyByteBuf, PacketUnlockSkill> STREAM_CODEC = StreamCodec.of(PacketUnlockSkill::encode, PacketUnlockSkill::decode);

	private final int skillId;

	public PacketUnlockSkill(int skillId) {
		this.skillId = skillId;
	}

	public static void encode(PacketUnlockSkill msg, FriendlyByteBuf buf) {
		buf.writeInt(msg.skillId);
	}

	public static PacketUnlockSkill decode(FriendlyByteBuf buf) {
		return new PacketUnlockSkill(buf.readInt());
	}

	public static void handle(final PacketUnlockSkill msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			ServerPlayer player = ctx.player();
			if (player == null) return;

			SkillPoint skill = SkillPointInit.getById(msg.skillId);
			if (skill == null) return;

			// ── Degree gate: block if player hasn't reached the required initiation tier ──
			if (skill.getRequiredDegree() > 0) {
				int playerDegree = HemoCapabilityAccess.getPlayerDegreeNumber(player);
				if (playerDegree < skill.getRequiredDegree()) {
					EnumInitiatoryDegree needed = EnumInitiatoryDegree.byNumber(skill.getRequiredDegree());
					String degreeName = needed != null ? needed.getTitle() : ("Degree " + skill.getRequiredDegree());
					player.displayClientMessage(
							Component.literal("Requires initiation: " + degreeName)
									.withStyle(ChatFormatting.RED), true);
					return;
				}
			}

			// ── Case 1: Skill is LOCKED → unlock it (first purchase) ──
			if (skill.getState() == EnumSkillStates.LOCKED) {
				// Check prerequisite parent is unlocked
				if (skill.getParent() != null && skill.getParent().getState() != EnumSkillStates.UNLOCKED) {
					player.displayClientMessage(
							Component.literal("Prerequisite skill not yet unlocked!")
									.withStyle(ChatFormatting.RED), true);
					return;
				}
				double cost = skill.getLevelUpCost();
				int spCost = skill.getSkillPointCost();
				if (!tryDrainSkillPoints(player, spCost)) return;
				if (!tryDrainBlood(player, cost)) {
					// Refund skill points if blood drain failed
					SkillPointInit.skillPoints += spCost;
					return;
				}

				skill.setState(EnumSkillStates.UNLOCKED);
				skill.setCurrentLevel(1);
				player.displayClientMessage(
						Component.literal("Unlocked: " + skill.getName().replace("_", " "))
								.withStyle(ChatFormatting.DARK_RED), true);

				// Sync updated skills back to client
				syncSkills(player);
				return;
			}

			// ── Case 2: Skill is UNLOCKED → level it up ──
			if (skill.getState() == EnumSkillStates.UNLOCKED) {
				if (skill.isMaxed()) {
					player.displayClientMessage(
							Component.literal("Skill already at max level!")
									.withStyle(ChatFormatting.GRAY), true);
					return;
				}
				double cost = skill.getLevelUpCost();
				int spCost = skill.getSkillPointCost();
				if (!tryDrainSkillPoints(player, spCost)) return;
				if (!tryDrainBlood(player, cost)) {
					// Refund skill points if blood drain failed
					SkillPointInit.skillPoints += spCost;
					return;
				}

				skill.tryLevelUp();
				player.displayClientMessage(
						Component.literal(skill.getName().replace("_", " ")
								+ " → Level " + skill.getCurrentLevel())
								.withStyle(ChatFormatting.DARK_RED), true);

				syncSkills(player);
			}
		});
	}

	/**
	 * Check and consume skill points. Returns true on success.
	 */
	private static boolean tryDrainSkillPoints(ServerPlayer player, int cost) {
		if (cost <= 0) return true;
		if (SkillPointInit.skillPoints < cost) {
			player.displayClientMessage(
					Component.literal("Not enough skill points! Need " + cost
							+ " (have " + SkillPointInit.skillPoints + ")")
							.withStyle(ChatFormatting.RED), true);
			return false;
		}
		SkillPointInit.skillPoints -= cost;
		return true;
	}

	/**
	 * Attempt to drain the given blood cost from the player.
	 * @return true if the drain succeeded; false (with message) otherwise.
	 */
	private static boolean tryDrainBlood(ServerPlayer player, double cost) {
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player).orElse(null);
		if (volume == null || !volume.isActive()) {
			player.displayClientMessage(
					Component.literal("Blood system is not active!")
							.withStyle(ChatFormatting.RED), true);
			return false;
		}
		if (volume.getBloodVolume() < cost) {
			player.displayClientMessage(
					Component.literal("Not enough blood! Need "
							+ (int) cost + " mL")
							.withStyle(ChatFormatting.RED), true);
			return false;
		}
		volume.drain(cost);
		BloodVolumeEvents.syncVolume(player, volume);
		return true;
	}

	/**
	 * Sends the full skill tree state back to the client via
	 * {@link PacketSyncSkills}.
	 */
	private static void syncSkills(ServerPlayer player) {
		PacketHandler.sendToPlayer(player, new PacketSyncSkills(SkillPointInit.serializeAll()));
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
