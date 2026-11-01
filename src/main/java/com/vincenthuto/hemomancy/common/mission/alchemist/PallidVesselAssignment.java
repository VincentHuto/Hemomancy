package com.vincenthuto.hemomancy.common.mission.alchemist;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.PendingDelivery;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.inquiry.ItemInquiryContext;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerAlchemistEntity;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.menu.HarbingerEquipmentMenu;
import com.vincenthuto.hemomancy.common.mission.vicar.EarlyInitiation;
import com.vincenthuto.hemomancy.common.util.ProgressionGates;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * The Alchemist's Pallid Vessel Vocation (Degree 3, optional). The rite step is recorded from the caster's own
 * completed Rite of the Pallid Vessel; the fill step from a carried or equipped White Blood Gourd holding blood after
 * that rite; the report at a nearby Alchemist. Saves from before this assignment have no record of who performed the
 * rite, so nothing is credited to them: they perform it again.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class PallidVesselAssignment {
	public static final ResourceLocation ADV_COMPLETE = Hemomancy.rloc("hemomancy/pallid_vessel_complete");
	public static final String RITE_PATH = "cardinal_rite/pallid_vessel_rite";
	public static final int REWARD_GOURDS = 2;
	public static final int REWARD_SHARDS = 1;
	private static final String DATA = "hemomancy:pallid_vessel";
	private static final String BRIEFED = "Briefed";
	private static final String RITE = "RiteDone";
	private static final String FILLED = "Filled";
	private static final String REPORTED = "Reported";
	private static final String GOURDS_PENDING = "GourdsPending";
	private static final String SHARDS_PENDING = "ShardsPending";

	/** The report's Dried Gourds and Amethyst Shard, owed until they really land. */
	public static final PendingDelivery.Claim REWARD = new PendingDelivery.Claim("pallid_vessel_reward",
			player -> {
				CompoundTag data = data(player);
				List<ItemStack> owed = new ArrayList<>();
				if (data.getInt(GOURDS_PENDING) > 0)
					owed.add(new ItemStack(ItemInit.dried_gourd.get(), data.getInt(GOURDS_PENDING)));
				if (data.getInt(SHARDS_PENDING) > 0)
					owed.add(new ItemStack(Items.AMETHYST_SHARD, data.getInt(SHARDS_PENDING)));
				return owed;
			},
			PallidVesselAssignment::eligible,
			(player, left) -> {
				CompoundTag data = data(player);
				data.putInt(GOURDS_PENDING, count(left, ItemInit.dried_gourd.get()));
				data.putInt(SHARDS_PENDING, count(left, Items.AMETHYST_SHARD));
				save(player, data);
			});

	private PallidVesselAssignment() {}

	public static boolean eligible(ServerPlayer player) {
		ItemInquiryContext context = ItemInquiryContext.from(player);
		return ProgressionGates.hasDegree(player, 3) && context.activeBlood() && !context.purifying()
				&& !context.clarityUnlocked();
	}

	public static PallidVesselProgress progress(ServerPlayer player) {
		CompoundTag data = data(player);
		return new PallidVesselProgress(data.getBoolean(BRIEFED), data.getBoolean(RITE), data.getBoolean(FILLED),
				data.getBoolean(REPORTED));
	}

	public static boolean brief(ServerPlayer player, Entity teacher) {
		if (!atAlchemist(player, teacher) || progress(player).briefed()) return false;
		write(player, progress(player).withBriefed());
		player.displayClientMessage(Component.translatable("hemomancy.alchemist.pallid_vessel.recorded.briefed")
				.withStyle(ChatFormatting.DARK_RED), false);
		return true;
	}

	/** Called when this player's own Rite of the Pallid Vessel resolves. */
	public static void recordRite(ServerPlayer player) {
		PallidVesselProgress before = progress(player);
		if (before.riteDone() || before.reported() || !eligible(player)) return;
		write(player, before.withRite());
		if (before.briefed()) player.displayClientMessage(Component.translatable(
				"hemomancy.alchemist.pallid_vessel.recorded.rite").withStyle(ChatFormatting.DARK_RED), false);
	}

	/** Records the fill once the player carries or wears a White Blood Gourd that holds blood. */
	public static boolean recordFill(ServerPlayer player) {
		PallidVesselProgress before = progress(player);
		if (!before.acceptsFill() || !eligible(player) || !carriesFilledVessel(player)) return false;
		write(player, before.withFill());
		if (before.briefed()) player.displayClientMessage(Component.translatable(
				"hemomancy.alchemist.pallid_vessel.recorded.filled").withStyle(ChatFormatting.DARK_RED), false);
		return true;
	}

	public static boolean report(ServerPlayer player, Entity teacher) {
		if (!atAlchemist(player, teacher)) return false;
		recordFill(player);
		PallidVesselProgress before = progress(player);
		if (!before.canReport()) return false;
		CompoundTag data = data(player);
		data.putBoolean(REPORTED, true);
		data.putInt(GOURDS_PENDING, REWARD_GOURDS);
		data.putInt(SHARDS_PENDING, REWARD_SHARDS);
		save(player, data);
		HarbingerAdvancementGranter.grantIfNotDone(player, ADV_COMPLETE);
		player.displayClientMessage(Component.translatable("hemomancy.alchemist.pallid_vessel.recorded.complete")
				.withStyle(ChatFormatting.DARK_RED), false);
		PendingDelivery.deliver(player, REWARD);
		return true;
	}

	static boolean isFilledVessel(ItemStack stack) {
		return stack.is(ItemInit.blood_gourd_white.get())
				&& HemoCapabilityAccess.getBloodVolume(stack).map(volume -> volume.getBloodVolume() > 0).orElse(false);
	}

	private static boolean carriesFilledVessel(ServerPlayer player) {
		for (ItemStack stack : player.getInventory().items) if (isFilledVessel(stack)) return true;
		if (isFilledVessel(player.getOffhandItem())) return true;
		return isFilledVessel(HemoCapabilityAccess.getEquipment(player)
				.getStackInSlot(HarbingerEquipmentMenu.GOURD_SLOT_INDEX));
	}

	private static boolean atAlchemist(ServerPlayer player, Entity teacher) {
		return teacher instanceof HarbingerAlchemistEntity && EarlyInitiation.near(player, teacher)
				&& player.isAlive() && eligible(player);
	}

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		if (event.getEntity() instanceof ServerPlayer player && player.tickCount % 20 == 10
				&& progress(player).acceptsFill()) recordFill(player);
	}

	private static int count(List<ItemStack> stacks, Item item) {
		return stacks.stream().filter(stack -> stack.is(item)).mapToInt(ItemStack::getCount).sum();
	}

	private static void write(ServerPlayer player, PallidVesselProgress progress) {
		CompoundTag data = data(player);
		data.putBoolean(BRIEFED, progress.briefed());
		data.putBoolean(RITE, progress.riteDone());
		data.putBoolean(FILLED, progress.filled());
		data.putBoolean(REPORTED, progress.reported());
		save(player, data);
	}

	private static CompoundTag data(ServerPlayer player) {
		return player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getCompound(DATA);
	}

	private static void save(ServerPlayer player, CompoundTag data) {
		CompoundTag root = player.getPersistentData();
		CompoundTag persisted = root.getCompound(Player.PERSISTED_NBT_TAG);
		persisted.put(DATA, data);
		root.put(Player.PERSISTED_NBT_TAG, persisted);
	}
}
