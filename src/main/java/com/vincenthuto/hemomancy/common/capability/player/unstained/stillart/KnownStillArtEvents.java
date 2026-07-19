package com.vincenthuto.hemomancy.common.capability.player.unstained.stillart;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumClarityStage;
import com.vincenthuto.hemomancy.common.capability.player.unstained.IUnstainedProgress;
import com.vincenthuto.hemomancy.common.init.StillArtInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.unstained.KnownStillArtsServerPacket;
import com.vincenthuto.hemomancy.common.manipulation.stillarts.StillArt;
import com.vincenthuto.hemomancy.common.manipulation.stillarts.StillArtRewardTable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public class KnownStillArtEvents {
	public static void sync(ServerPlayer player, IKnownStillArts known) {
		PacketHandler.sendToPlayer(player, new KnownStillArtsServerPacket(known));
	}

	public static boolean grantArt(ServerPlayer player, StillArt art) {
		if (!art.isUnlockedFor(player)) {
			return false;
		}
		return HemoCapabilityAccess.getKnownStillArts(player)
				.map(known -> {
					boolean learned = known.learnArt(art);
					if (learned) {
						sync(player, known);
					}
					return learned;
				})
				.orElse(false);
	}

	public static boolean grantEligibleArts(ServerPlayer player, IUnstainedProgress progress) {
		if (!progress.hasClarityUnlocked()) {
			return false;
		}
		EnumClarityStage stage = EnumClarityStage.byClarity(progress.getClarity());
		return grantArtsForStage(player, stage, true);
	}

	public static boolean grantArtsForStage(ServerPlayer player, EnumClarityStage stage, boolean notify) {
		boolean grantedAny = false;
		for (String artName : StillArtRewardTable.eligibleArtNames(stage)) {
			StillArt art = StillArtInit.getByName(artName);
			if (art != StillArt.BLANK && grantArt(player, art)) {
				grantedAny = true;
				if (notify) {
					player.displayClientMessage(Component.translatable("hemomancy.still_art.gifted",
									Component.translatable("hemomancy.still_art." + art.getName()))
							.withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC), false);
				}
			}
		}
		return grantedAny;
	}

	@SubscribeEvent
	public static void onAdvancementEarned(AdvancementEvent.AdvancementEarnEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		StillArtRewardTable.stageForAdvancement(event.getAdvancement().id())
				.ifPresent(stage -> grantArtsForStage(player, stage, true));
	}

	@SubscribeEvent
	public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		ServerPlayer player = (ServerPlayer) event.getEntity();
		HemoCapabilityAccess.getUnstainedProgress(player).ifPresent(progress -> grantEligibleArts(player, progress));
		HemoCapabilityAccess.getKnownStillArts(player).ifPresent(known -> sync(player, known));
	}

	@SubscribeEvent
	public static void onDimensionChange(PlayerChangedDimensionEvent event) {
		ServerPlayer player = (ServerPlayer) event.getEntity();
		HemoCapabilityAccess.getKnownStillArts(player).ifPresent(known -> sync(player, known));
	}

	@SubscribeEvent
	public static void playerRespawn(PlayerRespawnEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			HemoCapabilityAccess.getKnownStillArts(player).ifPresent(known -> sync(player, known));
		}
	}
}
