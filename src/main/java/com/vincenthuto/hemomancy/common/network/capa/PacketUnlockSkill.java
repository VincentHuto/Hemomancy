package com.vincenthuto.hemomancy.common.network.capa;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.degree.EnumInitiatoryDegree;
import com.vincenthuto.hemomancy.common.capability.player.skill.EnumSkillStates;
import com.vincenthuto.hemomancy.common.capability.player.skill.SkillPoint;
import com.vincenthuto.hemomancy.common.capability.player.skill.SkillProgress;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.init.SkillPointInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client -> Server packet: player requests to unlock or level-up a skill.
 * The server validates prerequisites against that player's SkillProgress.
 */
public class PacketUnlockSkill implements CustomPacketPayload {

	public static final Type<PacketUnlockSkill> TYPE = new Type<>(Hemomancy.rloc("packet_unlock_skill"));
	public static final StreamCodec<FriendlyByteBuf, PacketUnlockSkill> STREAM_CODEC = StreamCodec.of(PacketUnlockSkill::encode, PacketUnlockSkill::decode);

	private final int skillId;

	public PacketUnlockSkill(int skillId) {
		this.skillId = skillId;
	}

	public static void encode(FriendlyByteBuf buf, PacketUnlockSkill msg) {
		buf.writeInt(msg.skillId);
	}

	public static PacketUnlockSkill decode(FriendlyByteBuf buf) {
		return new PacketUnlockSkill(buf.readInt());
	}

	public static void handle(final PacketUnlockSkill msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			Player packetPlayer = ctx.player();
			if (!(packetPlayer instanceof ServerPlayer player)) return;

			SkillPoint skill = SkillPointInit.getById(msg.skillId);
			if (skill == null) return;
			SkillProgress progress = HemoCapabilityAccess.requireSkillProgress(player);

			if (skill.getRequiredDegree() > 0) {
				int playerDegree = HemoCapabilityAccess.getPlayerDegreeNumber(player);
				if (playerDegree < skill.getRequiredDegree()) {
					EnumInitiatoryDegree needed = EnumInitiatoryDegree.byNumber(skill.getRequiredDegree());
					String degreeName = needed != null ? needed.getTitle() : ("Degree " + skill.getRequiredDegree());
					player.displayClientMessage(Component.literal("Requires initiation: " + degreeName)
							.withStyle(ChatFormatting.RED), true);
					return;
				}
			}

			EnumSkillStates state = progress.getState(skill);
			if (state == EnumSkillStates.LOCKED) {
				if (skill.getParent() != null && progress.getState(skill.getParent()) != EnumSkillStates.UNLOCKED) {
					player.displayClientMessage(Component.literal("Prerequisite skill not yet unlocked!")
							.withStyle(ChatFormatting.RED), true);
					return;
				}
				double bloodCost = progress.getLevelUpCost(skill);
				int spCost = skill.getSkillPointCost();
				if (!tryDrainSkillPoints(player, progress, spCost)) return;
				if (!tryDrainBlood(player, bloodCost)) {
					progress.refundSkillPoints(spCost);
					syncSkills(player);
					return;
				}
				progress.setSkill(skill, EnumSkillStates.UNLOCKED, 1);
				player.displayClientMessage(Component.literal("Unlocked: " + skill.getName().replace("_", " "))
						.withStyle(ChatFormatting.DARK_RED), true);
				syncSkills(player);
				return;
			}

			if (state == EnumSkillStates.UNLOCKED) {
				if (progress.isMaxed(skill)) {
					player.displayClientMessage(Component.literal("Skill already at max level!")
							.withStyle(ChatFormatting.GRAY), true);
					return;
				}
				double bloodCost = progress.getLevelUpCost(skill);
				int spCost = skill.getSkillPointCost();
				if (!tryDrainSkillPoints(player, progress, spCost)) return;
				if (!tryDrainBlood(player, bloodCost)) {
					progress.refundSkillPoints(spCost);
					syncSkills(player);
					return;
				}
				progress.unlockOrLevel(skill);
				player.displayClientMessage(Component.literal(skill.getName().replace("_", " ")
								+ " -> Level " + progress.getLevel(skill))
						.withStyle(ChatFormatting.DARK_RED), true);
				syncSkills(player);
			}
		});
	}

	private static boolean tryDrainSkillPoints(ServerPlayer player, SkillProgress progress, int cost) {
		if (cost <= 0) return true;
		if (progress.getSkillPoints() < cost) {
			player.displayClientMessage(Component.literal("Not enough skill points! Need " + cost
							+ " (have " + progress.getSkillPoints() + ")")
					.withStyle(ChatFormatting.RED), true);
			return false;
		}
		progress.spendSkillPoints(cost);
		return true;
	}

	private static boolean tryDrainBlood(ServerPlayer player, double cost) {
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player).orElse(null);
		if (volume == null || !volume.isActive()) {
			player.displayClientMessage(Component.literal("Blood system is not active!")
					.withStyle(ChatFormatting.RED), true);
			return false;
		}
		if (volume.getBloodVolume() < cost) {
			player.displayClientMessage(Component.literal("Not enough blood! Need " + (int) cost + " mL")
					.withStyle(ChatFormatting.RED), true);
			return false;
		}
		volume.drain(cost);
		BloodVolumeEvents.syncVolume(player, volume);
		return true;
	}

	private static void syncSkills(ServerPlayer player) {
		PacketHandler.sendToPlayer(player,
				new PacketSyncSkills(HemoCapabilityAccess.requireSkillProgress(player).toSyncTag()));
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
