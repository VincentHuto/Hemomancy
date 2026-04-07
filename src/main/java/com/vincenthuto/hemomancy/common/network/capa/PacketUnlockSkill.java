package com.vincenthuto.hemomancy.common.network.capa;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.common.capability.player.skill.EnumSkillStates;
import com.vincenthuto.hemomancy.common.capability.player.skill.SkillPoint;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.init.SkillPointInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;

import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

/**
 * Client → Server packet: player requests to unlock or level-up a skill.
 * The server validates prerequisites, checks blood cost, drains blood,
 * applies the change, and syncs back to the client.
 */
public class PacketUnlockSkill {

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

	public static void handle(PacketUnlockSkill msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayer player = ctx.get().getSender();
			if (player == null) return;

			SkillPoint skill = SkillPointInit.getById(msg.skillId);
			if (skill == null) return;

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
		ctx.get().setPacketHandled(true);
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
		IBloodVolume volume = player.getCapability(BloodVolumeProvider.VOLUME_CAPA).orElse(null);
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
		PacketHandler.CHANNELBLOODVOLUME.send(
				PacketDistributor.PLAYER.with(() -> player),
				new PacketSyncSkills(SkillPointInit.serializeAll()));
	}
}
