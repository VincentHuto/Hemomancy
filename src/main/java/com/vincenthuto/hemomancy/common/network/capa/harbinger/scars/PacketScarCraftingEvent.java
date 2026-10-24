package com.vincenthuto.hemomancy.common.network.capa.harbinger.scars;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.network.ServerboundGuards;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.init.SkillPointInit;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.ItemScar;
import com.vincenthuto.hemomancy.common.menu.tile.crafting.ScarStationMenu;
import com.vincenthuto.hemomancy.common.recipe.ScarRecipe;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.ScarStationBlockEntity;
import com.vincenthuto.hemomancy.common.util.HemoText;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PacketScarCraftingEvent implements CustomPacketPayload {

	public static final Type<PacketScarCraftingEvent> TYPE = new Type<>(Hemomancy.rloc("packet_scar_crafting_event"));
	public static final StreamCodec<FriendlyByteBuf, PacketScarCraftingEvent> STREAM_CODEC = StreamCodec.of(PacketScarCraftingEvent::encode, PacketScarCraftingEvent::decode);

	/** Minimum initiatory degree required to use the Cerebral Scar Station. */
	private static final int REQUIRED_DEGREE = 4;

	public PacketScarCraftingEvent() {
	}

	public static void encode(FriendlyByteBuf buf, PacketScarCraftingEvent msg) {
	}

	public static PacketScarCraftingEvent decode(FriendlyByteBuf buf) {
		return new PacketScarCraftingEvent();
	}

	public static void handle(final PacketScarCraftingEvent msg, final IPayloadContext ctx) {
		Handler.handle(msg, ctx);
	}

	public static class Handler {

		public static void handle(final PacketScarCraftingEvent msg, final IPayloadContext ctx) {
			ctx.enqueueWork(() -> {
				Player packetPlayer = ctx.player();
				if (!(packetPlayer instanceof ServerPlayer player)) return;
				ScarStationMenu menu = ServerboundGuards.menu(player, ScarStationMenu.class);
				if (menu != null) {
					tryCraft(player, menu.getTe());
				}
			});
		}
	}

	public static void tryCraft(ServerPlayer player, ScarStationBlockEntity station) {
		int playerDegree = HemoCapabilityAccess.getPlayerDegreeNumber(player);
		if (playerDegree < REQUIRED_DEGREE) {
			player.displayClientMessage(
					Component.translatable("message.hemomancy.packet_scar_crafting_event.the_chisel_resists_your_hand",
							Component.translatable("message.hemomancy.packet_scar_crafting_event.adept").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD),
							HemoText.translatable("message.hemomancy.packet_scar_crafting_event.rank_degree_is_required_to_carve",
		REQUIRED_DEGREE).withStyle(ChatFormatting.RED)).withStyle(ChatFormatting.RED),
					false);
			return;
		}
		ScarRecipe recipe = station.getCurrentRecipe();
		if (recipe != null && recipe.getTier() >= 3
				&& !SkillPointHelper.isUnlocked(player, SkillPointInit.skill_deep_inscription)) {
			player.displayClientMessage(
					Component.translatable("message.hemomancy.packet_scar_crafting_event.the_third_inscription_tier_refuses_a").withStyle(ChatFormatting.DARK_RED), false);
			return;
		}
		if (!com.vincenthuto.hemomancy.common.event.MachineAccessEvents.hasPersonalAccess(player, station.getBlockState().getBlock())
				&& recipe != null && !recipe.getResultItem().is(
						com.vincenthuto.hemomancy.common.entity.npc.dialogue.VeinMasonScarLesson.forPlayer(player).scar().get())) {
			player.displayClientMessage(Component.translatable("message.hemomancy.packet_scar_crafting_event.supervised_access_is_for_your_first"), false);
			return;
		}
		Component failure = station.craftingFailure();
		if (failure != null) {
			player.displayClientMessage(failure.copy().withStyle(ChatFormatting.RED), false);
			return;
		}
		boolean outputWasEmpty = station.getItem(2).isEmpty();
		station.craftEvent();
		if (outputWasEmpty && station.getItem(2).getItem() instanceof ItemScar) {
			HarbingerAdvancementGranter.grantIfNotDone(player,
					HarbingerAdvancementGranter.ADV_VEIN_MASON_FIRST_SCAR_CARVED);
		}
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
