package com.vincenthuto.hemomancy.gametest;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.PendingDelivery;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.equipment.HarbingerEquipmentEntityEventHandler;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.morphling.EquippedMorphlingEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.musclememory.MuscleMemory;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.musclememory.MuscleMemoryEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.IScars;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.ScarStateSyncEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.menu.HarbingerEquipmentMenu;
import com.vincenthuto.hemomancy.common.menu.ScryingDiagnosticsMenuProvider;
import com.vincenthuto.hemomancy.common.menu.TendencyViewMenuProvider;
import com.vincenthuto.hemomancy.common.menu.VascularViewMenuProvider;
import com.vincenthuto.hemomancy.common.mission.alchemist.FirstDrawsAssignment;
import com.vincenthuto.hemomancy.common.mission.alchemist.FirstSeparationAssignment;
import com.vincenthuto.hemomancy.common.encounter.ChamberVisitService;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Opt-in ({@code -Dhemomancy.mpVerify=true}) server commands for checking player-state sync with a dedicated server
 * and two real clients. {@link #digest} is shared with the client driver's {@code peers} action, so one client's view
 * of another player can be compared with the server's copy line for line.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class MultiplayerVerification {
	private static final int[] LAYER_SLOTS = { IScars.FUNGAL_SLOT, HarbingerEquipmentMenu.CHARM_SLOT_INDEX,
			HarbingerEquipmentMenu.GOURD_SLOT_INDEX, HarbingerEquipmentMenu.JAR_SLOT_INDEX };

	private MultiplayerVerification() {
	}

	@SubscribeEvent
	public static void commands(RegisterCommandsEvent event) {
		if (!Boolean.getBoolean("hemomancy.mpVerify")) return;
		event.getDispatcher().register(Commands.literal("hemoverify").requires(source -> source.hasPermission(2))
				.then(Commands.literal("dress").then(Commands.argument("player", EntityArgument.player())
						.executes(ctx -> dress(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
				.then(Commands.literal("undress").then(Commands.argument("player", EntityArgument.player())
						.executes(ctx -> undress(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
				.then(Commands.literal("layer").then(Commands.argument("player", EntityArgument.player())
						.then(Commands.argument("slot", IntegerArgumentType.integer(0, 8))
								.then(Commands.argument("visible", BoolArgumentType.bool())
										.executes(ctx -> layer(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"),
												IntegerArgumentType.getInteger(ctx, "slot"),
												BoolArgumentType.getBool(ctx, "visible")))))))
				.then(Commands.literal("state").then(Commands.argument("player", EntityArgument.player())
						.executes(ctx -> state(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
				.then(Commands.literal("gifts").then(Commands.argument("player", EntityArgument.player())
						.executes(ctx -> gifts(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
				.then(Commands.literal("visit").then(Commands.argument("player", EntityArgument.player())
						.executes(ctx -> visit(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
				.then(Commands.literal("report").then(Commands.argument("player", EntityArgument.player())
						.executes(ctx -> report(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
				.then(Commands.literal("menu").then(Commands.argument("name", StringArgumentType.word())
						.executes(ctx -> menu(ctx.getSource(), StringArgumentType.getString(ctx, "name"))))));
	}

	/** What the render layers read for {@code player}: fungal scar, morphling, muscle memory, equipment, visibility. */
	public static String digest(Player player) {
		var muscle = HemoCapabilityAccess.muscleMemory(player);
		var equipment = HemoCapabilityAccess.getEquipment(player);
		StringBuilder out = new StringBuilder();
		out.append("fungal=").append(id(HemoCapabilityAccess.getScarState(player).getFungalScar()));
		out.append(" morphling=").append(id(HemoCapabilityAccess.getEquippedMorphling(player).getEquippedMorphling()));
		out.append(" muscle=");
		for (EnumVeinSections section : EnumVeinSections.values())
			muscle.enabledMemory(section).ifPresent(memory -> out.append(section).append(':').append(memory).append(','));
		out.append(" equipment=");
		for (int slot = 0; slot < equipment.getSlots(); slot++)
			if (!equipment.getStackInSlot(slot).isEmpty())
				out.append(slot).append(':').append(id(equipment.getStackInSlot(slot))).append(',');
		out.append(" layers=");
		for (int slot : LAYER_SLOTS) out.append(slot).append(':').append(equipment.isRenderLayerVisible(slot) ? 1 : 0).append(',');
		return out.toString();
	}

	private static int dress(CommandSourceStack source, ServerPlayer player) {
		var equipment = HemoCapabilityAccess.getEquipment(player);
		equipment.setStackInSlot(HarbingerEquipmentMenu.JAR_SLOT_INDEX, new ItemStack(ItemInit.morphling_jar.get()));
		equipment.setStackInSlot(HarbingerEquipmentMenu.CHARM_SLOT_INDEX, new ItemStack(ItemInit.charm_of_vascularium.get()));
		equipment.setStackInSlot(HarbingerEquipmentMenu.GOURD_SLOT_INDEX, new ItemStack(ItemInit.blood_gourd_red.get()));
		HemoCapabilityAccess.getScarState(player).setFungalScar(new ItemStack(ItemInit.noctifly_agaric.get()));
		HemoCapabilityAccess.getEquippedMorphling(player).setEquippedMorphling(new ItemStack(ItemInit.morphling_gravecap.get()));
		var muscle = HemoCapabilityAccess.muscleMemory(player);
		for (MuscleMemory memory : List.of(MuscleMemory.SANGUINE_FISTS, MuscleMemory.COURSING_LEGS)) {
			muscle.learnAndAddReserve(memory, 1_000_000);
			muscle.activate(memory);
		}
		syncLooks(player);
		return state(source, player);
	}

	private static int undress(CommandSourceStack source, ServerPlayer player) {
		var equipment = HemoCapabilityAccess.getEquipment(player);
		for (int slot : LAYER_SLOTS) if (slot != IScars.FUNGAL_SLOT) equipment.setStackInSlot(slot, ItemStack.EMPTY);
		HemoCapabilityAccess.getScarState(player).setFungalScar(ItemStack.EMPTY);
		HemoCapabilityAccess.getEquippedMorphling(player).clearMorphling();
		var muscle = HemoCapabilityAccess.muscleMemory(player);
		for (MuscleMemory memory : MuscleMemory.values()) muscle.deactivate(memory);
		syncLooks(player);
		return state(source, player);
	}

	private static int layer(CommandSourceStack source, ServerPlayer player, int slot, boolean visible) {
		HemoCapabilityAccess.getEquipment(player).setRenderLayerVisible(slot, visible);
		HarbingerEquipmentEntityEventHandler.syncLayerVisibilityToClient(player);
		return state(source, player);
	}

	/** The senders gameplay uses after these changes; equipment slots go out on the container's own tick. */
	private static void syncLooks(ServerPlayer player) {
		ScarStateSyncEvents.syncToClient(player);
		EquippedMorphlingEvents.syncToClient(player);
		MuscleMemoryEvents.sync(player);
		HarbingerEquipmentEntityEventHandler.syncLayerVisibilityToClient(player);
	}

	private static int state(CommandSourceStack source, ServerPlayer player) {
		return say(source, "STATE " + player.getName().getString() + " " + digest(player));
	}

	/** The guided-visit loaded fixture's six pending gifts, recorded through the real assignment paths. */
	private static int gifts(CommandSourceStack source, ServerPlayer player) {
		HemoCapabilityAccess.getBloodVolume(player).setActive(true);
		var alchemist = EntityInit.harbinger_alchemist.get().create(player.serverLevel());
		alchemist.setPos(player.position());
		try {
			boolean draws = FirstDrawsAssignment.brief(player, alchemist);
			FirstSeparationAssignment.markBriefed(player);
			FirstSeparationAssignment.giveBriefingSupplies(player);
			HarbingerAdvancementGranter.grantIfNotDone(player, HarbingerAdvancementGranter.ADV_FIRST_SEPARATION_STARTED);
			HarbingerAdvancementGranter.grantIfNotDone(player, HarbingerAdvancementGranter.ADV_FIRST_SEPARATION_COMPLETE);
			boolean tools = FirstSeparationAssignment.claimRewards(player);
			HemoCapabilityAccess.getInitiatoryDegree(player).setQliphothCommunionDone(true);
			player.getPersistentData().putBoolean("hemomancy:vesper_memory_pending", true);
			var persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
			persisted.putBoolean("hemomancy:mycophant_tendril_pending", true);
			player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
			say(source, "GIFTS " + player.getName().getString() + " draws=" + draws + " tools=" + tools);
		} finally {
			alchemist.discard();
		}
		return report(source, player);
	}

	private static int visit(CommandSourceStack source, ServerPlayer player) {
		ChamberVisitService.access(player).setGuidedComplete(false);
		boolean started = ChamberVisitService.beginGuidedVisit(player);
		say(source, "VISIT " + player.getName().getString() + " started=" + started);
		return report(source, player);
	}

	private static int report(CommandSourceStack source, ServerPlayer player) {
		StringBuilder out = new StringBuilder("REPORT ").append(player.getName().getString())
				.append(" dim=").append(player.level().dimension().location())
				.append(" alive=").append(player.isAlive())
				.append(" visit=").append(ChamberVisitService.isActive(player) ? ChamberVisitService.mode(player) : "none")
				.append(" observational=").append(ChamberVisitService.isObservational(player))
				.append(" owed={");
		for (PendingDelivery.Claim claim : PendingDelivery.claims()) {
			List<ItemStack> owed = claim.owed().apply(player);
			if (!owed.isEmpty()) out.append(claim.name()).append('=').append(owed.stream()
					.map(stack -> id(stack) + "x" + stack.getCount()).collect(Collectors.joining("+"))).append(' ');
		}
		out.append("} carried={");
		for (Item item : List.of(ItemInit.bloody_vial.get(), ItemInit.living_syringe.get(), ItemInit.vial_rack.get(),
				ItemInit.fungal_spine.get(), ItemInit.memory_of_vesper.get(), ItemInit.mycophant_tendril.get(),
				Items.STONE, Items.DIAMOND)) {
			int count = player.getInventory().countItem(item);
			if (count > 0) out.append(BuiltInRegistries.ITEM.getKey(item).getPath()).append('=').append(count).append(' ');
		}
		return say(source, out.append('}').toString());
	}

	private static int menu(CommandSourceStack source, String name) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		ServerPlayer player = source.getPlayerOrException();
		switch (name) {
			case "scrying" -> player.openMenu(new ScryingDiagnosticsMenuProvider());
			case "tendency" -> player.openMenu(new TendencyViewMenuProvider());
			case "vascular" -> player.openMenu(new VascularViewMenuProvider());
			default -> {
				return say(source, "MENU unknown " + name);
			}
		}
		return say(source, "MENU " + name + " opened=" + (player.containerMenu != player.inventoryMenu));
	}

	private static int say(CommandSourceStack source, String message) {
		Hemomancy.LOGGER.info("HEMOVERIFY {}", message);
		source.sendSuccess(() -> Component.literal(message), false);
		return 1;
	}

	private static String id(ItemStack stack) {
		return stack.isEmpty() ? "-" : BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
	}
}
