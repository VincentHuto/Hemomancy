package com.vincenthuto.hemomancy.common.capability.player.harbinger.court;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.entity.mob.aquatic.BrinedVotaryEntity;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingItem;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncCourtProgress;
import com.vincenthuto.hemomancy.common.util.ProgressionGates;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import javax.annotation.Nullable;

/**
 * The one server-authoritative "Court known" state and the three routes into it (Brined Court Track 5): a Voyager's
 * explanation, a Brined Votary's letter, the Alchemist's lead, and finding a Court structure by sailing past it.
 * Whichever route arrives first records itself; later routes only acknowledge it.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class CourtDiscoveryProgress {
	/** Both Seats and every inner-house outpost. */
	public static final TagKey<Structure> COURT = TagKey.create(Registries.STRUCTURE, Hemomancy.rloc("brined_court"));
	public static final int DISCOVERY_INTERVAL_TICKS = 20;

	private CourtDiscoveryProgress() {
	}

	public static CourtProgress progress(Player player) {
		return HemoCapabilityAccess.courtProgress(player);
	}

	public static boolean isKnown(Player player) {
		return progress(player).known();
	}

	@Nullable
	public static CourtProgress.Route knownBy(Player player) {
		return progress(player).knownBy();
	}

	/**
	 * Records that {@code player} knows the Court through {@code route}. Only the first route is kept and announced;
	 * a later one is acknowledged by whoever delivered it.
	 */
	public static CourtIntroductionRules.Arrival markKnown(ServerPlayer player, CourtProgress.Route route) {
		CourtProgress progress = progress(player);
		CourtIntroductionRules.Arrival arrival = CourtIntroductionRules.arrive(progress.known());
		if (arrival == CourtIntroductionRules.Arrival.ACKNOWLEDGED) return arrival;
		progress.setKnown(route);
		sync(player);
		player.displayClientMessage(Component.translatable(route == CourtProgress.Route.FOUND
				? "hemomancy.court.discovery" : "hemomancy.court.known").withStyle(ChatFormatting.DARK_AQUA), false);
		return arrival;
	}

	public static void sync(ServerPlayer player) {
		PacketHandler.sendToPlayer(player, new PacketSyncCourtProgress(progress(player).serializeNBT(player.registryAccess())));
	}

	public static boolean carries(Player player, Item item) {
		return player.getInventory().contains(stack -> stack.is(item));
	}

	public static boolean carriesChart(Player player) {
		return carries(player, ItemInit.relay_chart.get());
	}

	public static boolean carriesLetter(Player player) {
		return carries(player, ItemInit.brine_sealed_letter.get());
	}

	/** True while a dropped letter never reached an NPC and the player still doesn't know the Court. */
	public static boolean letterLost(Player player) {
		CourtProgress progress = progress(player);
		return CourtIntroductionRules.letterLost(progress.known(), progress.letterDropped(), carriesLetter(player));
	}

	/** The highest maturity of any strain the player wears or carries, or -1 with none. */
	public static int highestStrainMaturity(Player player) {
		int highest = -1;
		ItemStack worn = HemoCapabilityAccess.getEquippedMorphling(player).getEquippedMorphling();
		if (worn.getItem() instanceof MorphlingItem) highest = MorphlingItem.getMaturityLevel(worn);
		for (ItemStack stack : player.getInventory().items) {
			if (stack.getItem() instanceof MorphlingItem) highest = Math.max(highest, MorphlingItem.getMaturityLevel(stack));
		}
		return highest;
	}

	/** A Vicar, Alchemist or Voyager reads the letter the player carries, or hears about the one they lost. */
	public static boolean recordLetter(ServerPlayer player) {
		if (!carriesLetter(player) && !progress(player).letterDropped()) return false;
		return markKnown(player, CourtProgress.Route.LETTER) == CourtIntroductionRules.Arrival.FIRST;
	}

	/** A Voyager explains the Court to a fifth-Degree player. */
	public static boolean recordVoyager(ServerPlayer player) {
		if (!ProgressionGates.hasDegree(player, CourtIntroductionRules.MINIMUM_DEGREE)) return false;
		return markKnown(player, CourtProgress.Route.VOYAGER) == CourtIntroductionRules.Arrival.FIRST;
	}

	/** The Alchemist's lead, for a fifth-Degree player with a strain at Mature or higher. */
	public static boolean recordAlchemist(ServerPlayer player) {
		if (!ProgressionGates.hasDegree(player, CourtIntroductionRules.MINIMUM_DEGREE)
				|| highestStrainMaturity(player) < CourtIntroductionRules.MATURE) return false;
		return markKnown(player, CourtProgress.Route.ALCHEMIST) == CourtIntroductionRules.Arrival.FIRST;
	}

	/**
	 * A fresh Relay Chart for a player who knows the Court and isn't carrying one, recording that a Voyager issued it;
	 * otherwise empty.
	 */
	public static ItemStack takeChart(ServerPlayer player) {
		CourtProgress progress = progress(player);
		if (!CourtIntroductionRules.canIssueChart(progress.known(), carriesChart(player))) return ItemStack.EMPTY;
		if (!progress.chartIssued()) {
			progress.setChartIssued(true);
			sync(player);
		}
		return new ItemStack(ItemInit.relay_chart.get());
	}

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % DISCOVERY_INTERVAL_TICKS != 0) return;
		if (!mayDiscover(player)) return;
		if (insideCourt(player)) markKnown(player, CourtProgress.Route.FOUND);
	}

	/** Only a living, non-spectator player who doesn't know the Court yet can find it, as with the Circus. */
	public static boolean mayDiscover(ServerPlayer player) {
		return player.isAlive() && !player.isSpectator() && !isKnown(player);
	}

	/** Inside the bounds of any piece of a Seat or outpost. */
	public static boolean insideCourt(ServerPlayer player) {
		return player.serverLevel().structureManager().getStructureWithPieceAt(player.blockPosition(), COURT).isValid();
	}

	/** A Votary carries one letter per player, for a killer who doesn't know the Court yet: never a loot table. */
	@SubscribeEvent
	public static void onLivingDrops(LivingDropsEvent event) {
		if (!(event.getEntity() instanceof BrinedVotaryEntity votary)
				|| !(event.getSource().getEntity() instanceof ServerPlayer player)) return;
		CourtProgress progress = progress(player);
		if (!CourtIntroductionRules.letterDrops(progress.known(), progress.letterDropped())) return;
		progress.setLetterDropped(true);
		sync(player);
		event.getDrops().add(new ItemEntity(votary.level(), votary.getX(), votary.getY() + 0.5D, votary.getZ(),
				new ItemStack(ItemInit.brine_sealed_letter.get())));
	}
}
