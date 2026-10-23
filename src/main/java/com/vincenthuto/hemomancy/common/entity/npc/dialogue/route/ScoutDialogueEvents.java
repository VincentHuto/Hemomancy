package com.vincenthuto.hemomancy.common.entity.npc.dialogue.route;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.npc.unstained.UnstainedScoutEntity;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.shared.PreWrittenMemoItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.*;

/**
 * The Unstained Scout's dialogue event: giving up its notes.
 */
final class ScoutDialogueEvents {
	private ScoutDialogueEvents() {
	}

	/** Handles the event when it is one of this NPC's; false lets the next route try. */
	public static boolean handle(DialogueEvent event) {
		ServerPlayer player = event.getPlayer();
		switch (event.getEventId()) {
		case "scout_give_notes" -> {
			handleScoutGiveNotes(player, event.getEntityId());
		}
		default -> {
			return false;
		}
		}
		return true;
	}

	/**
	 * Drops the scout's field notes at the entity's position, then kills the
	 * scout so the player understands she has truly expired. The notes contain
	 * memos for both Harbinger and Unstained paths so that any player can
	 * eventually dictate the correct version into their book.
	 */
	private static void handleScoutGiveNotes(ServerPlayer player, int entityId) {
		ItemStack notes = PreWrittenMemoItem.create(
				ItemInit.scout_field_notes.get(),
				ResourceLocation.tryBuild(Hemomancy.MOD_ID, "annetta_insect_observation"),
				ResourceLocation.tryBuild(Hemomancy.MOD_ID, "annetta_insect_observation_immaculatus"));

		Entity entity = player.level().getEntity(entityId);
		DialogueRewards.giveOrDropAtEntity(player, entityId, notes);
		if (entity instanceof UnstainedScoutEntity scout) {
			scout.setInvulnerable(false);
			scout.kill();
		}

		player.displayClientMessage(
				Component.translatable("hemomancy.dialogue.event.scout_gives_notes")
						.withStyle(ChatFormatting.AQUA),
				false);
	}
}
