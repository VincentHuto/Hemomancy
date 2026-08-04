package com.vincenthuto.hemomancy.common.rite;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.IScars;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.ScarType;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.ScarInit;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.ItemScar;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.ItemScarPattern;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.ScarDefinition;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.scars.PacketSyncScarsState;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public final class ScarBrazierRite {
	public static final int REQUIRED_DEGREE = 4;
	public static final double LEARN_BLOOD_COST = 100.0D;
	public static final double LOADOUT_BLOOD_COST = 50.0D;

	private ScarBrazierRite() {
	}

	public static ScarBrazierInteractionRules.Burn selectBurn(boolean lit, boolean empty, boolean sneaking,
			ItemStack stack) {
		return ScarBrazierInteractionRules.select(lit, empty, sneaking,
				stack.getItem() instanceof ItemScar,
				stack.getItem() instanceof ItemScarPattern && ItemScarPattern.hasPreparedLoadout(stack),
				stack.is(ItemInit.runic_motif_paper.get()));
	}

	public static boolean burn(Level level, BlockPos pos, Player player, ItemStack stack,
			ScarBrazierInteractionRules.Burn burn) {
		return switch (burn) {
			case LEARN -> tryLearnScar(level, pos, player, stack);
			case COMMIT -> tryCommitLoadout(level, pos, player, stack);
			case CLEAR -> tryClearLoadout(level, pos, player, stack);
			case NONE -> false;
		};
	}

	public static int getMaxActiveScars(Player player) {
		return ScarBrazierInteractionRules.maxActiveScars(HemoCapabilityAccess.getPlayerDegreeNumber(player));
	}

	private static boolean tryLearnScar(Level level, BlockPos pos, Player player, ItemStack stack) {
		if (!(stack.getItem() instanceof ItemScar scarItem)) {
			return false;
		}
		ScarDefinition definition = scarItem.getScarDefinition();
		if (definition == null || definition.getScarType() != ScarType.CEREBRAL) {
			message(player, "Only cerebral scar items can be burned into memory.", ChatFormatting.RED);
			return true;
		}
		ResourceLocation scarId = ScarInit.SCARS_TYPE_REGISTRY.getKey(definition);
		if (scarId == null) {
			message(player, "This scar has no registered pattern to remember.", ChatFormatting.RED);
			return true;
		}
		if (!hasDegree(player)) {
			message(player, "The Iron Brazier answers this scar rite only at the Fourth Degree and above.", ChatFormatting.RED);
			return true;
		}

		IScars scars = HemoCapabilityAccess.getScarState(player).orElse(null);
		if (scars == null) {
			message(player, "Your scar memory is silent.", ChatFormatting.RED);
			return true;
		}
		if (scars.knowsCerebralScar(scarId)) {
			message(player, "You already know this scar.", ChatFormatting.GOLD);
			return true;
		}
		if (!spendBlood(player, LEARN_BLOOD_COST)) {
			message(player, "Not enough blood to sear the scar into memory.", ChatFormatting.RED);
			return true;
		}

		scars.addKnownCerebralScar(scarId);
		if (player instanceof ServerPlayer serverPlayer) {
			HarbingerAdvancementGranter.grantIfNotDone(serverPlayer,
					HarbingerAdvancementGranter.ADV_VEIN_MASON_FIRST_SCAR_LEARNED);
		}
		stack.shrink(1);
		syncScarState(player, scars);
		pulse(level, pos);
		message(player, "The scar burns cleanly into your cerebral map.", ChatFormatting.DARK_RED);
		return true;
	}

	private static boolean tryCommitLoadout(Level level, BlockPos pos, Player player, ItemStack stack) {
		if (!(stack.getItem() instanceof ItemScarPattern) || !ItemScarPattern.hasPreparedLoadout(stack)) {
			return false;
		}
		if (!hasDegree(player)) {
			message(player, "The Iron Brazier answers this scar rite only at the Fourth Degree and above.", ChatFormatting.RED);
			return true;
		}
		List<ResourceLocation> selected = new ArrayList<>(ItemScarPattern.getScarIds(stack));
		if (selected.isEmpty()) {
			message(player, "This scar pattern carries no loadout.", ChatFormatting.RED);
			return true;
		}
		if (selected.size() > getMaxActiveScars(player)) {
			message(player, "That pattern exceeds your current scar capacity.", ChatFormatting.RED);
			return true;
		}

		IScars scars = HemoCapabilityAccess.getScarState(player).orElse(null);
		if (scars == null) {
			message(player, "Your scar memory is silent.", ChatFormatting.RED);
			return true;
		}
		for (ResourceLocation id : selected) {
			ScarDefinition definition = ScarInit.getByName(id.toString());
			if (definition == null || definition.getScarType() != ScarType.CEREBRAL || !scars.knowsCerebralScar(id)) {
				message(player, "The pattern contains a scar you have not learned: " + id.getPath(), ChatFormatting.RED);
				return true;
			}
		}
		if (!spendBlood(player, LOADOUT_BLOOD_COST)) {
			message(player, "Not enough blood to seal the scar loadout.", ChatFormatting.RED);
			return true;
		}

		for (ResourceLocation active : List.copyOf(scars.getActiveCerebralScars())) {
			scars.deactivateCerebralScar(active);
		}
		for (ResourceLocation id : selected) {
			scars.activateCerebralScar(id);
		}
		if (player instanceof ServerPlayer serverPlayer) {
			HarbingerAdvancementGranter.grantIfNotDone(serverPlayer,
					HarbingerAdvancementGranter.ADV_VEIN_MASON_FIRST_EFFIGY_LOADOUT);
			if (!HarbingerAdvancementGranter.isVeinMasonRewardClaimed(serverPlayer)) {
				HarbingerAdvancementGranter.grantIfNotDone(serverPlayer,
						HarbingerAdvancementGranter.ADV_VEIN_MASON_CONTINUATION_READY);
			}
		}
		stack.shrink(1);
		syncScarState(player, scars);
		pulse(level, pos);
		message(player, "The pattern collapses into your active scar loadout.", ChatFormatting.DARK_RED);
		return true;
	}

	private static boolean tryClearLoadout(Level level, BlockPos pos, Player player, ItemStack stack) {
		if (!stack.is(ItemInit.runic_motif_paper.get())) {
			return false;
		}
		if (!hasDegree(player)) {
			message(player, "The Iron Brazier answers this scar rite only at the Fourth Degree and above.", ChatFormatting.RED);
			return true;
		}
		IScars scars = HemoCapabilityAccess.getScarState(player).orElse(null);
		if (scars == null) {
			message(player, "Your scar memory is silent.", ChatFormatting.RED);
			return true;
		}
		if (scars.getActiveCerebralScars().isEmpty()) {
			message(player, "No active cerebral scars answer the flame.", ChatFormatting.GOLD);
			return true;
		}
		if (!spendBlood(player, LOADOUT_BLOOD_COST)) {
			message(player, "Not enough blood to scour the active scar loadout.", ChatFormatting.RED);
			return true;
		}

		for (ResourceLocation active : List.copyOf(scars.getActiveCerebralScars())) {
			scars.deactivateCerebralScar(active);
		}
		stack.shrink(1);
		syncScarState(player, scars);
		pulse(level, pos);
		message(player, "The blank motif burns away your active cerebral scar loadout.", ChatFormatting.DARK_RED);
		return true;
	}

	private static boolean hasDegree(Player player) {
		return HemoCapabilityAccess.getPlayerDegreeNumber(player) >= REQUIRED_DEGREE;
	}

	private static boolean spendBlood(Player player, double cost) {
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player).orElse(null);
		if (volume == null || !volume.isActive() || volume.getBloodVolume() < cost) {
			return false;
		}
		volume.drain(cost);
		volume.addBloodSpend(cost);
		if (player instanceof ServerPlayer serverPlayer) {
			BloodVolumeEvents.syncVolume(serverPlayer, volume);
		}
		return true;
	}

	private static void syncScarState(Player player, IScars scars) {
		if (player instanceof ServerPlayer serverPlayer) {
			PacketHandler.sendToPlayer(serverPlayer, new PacketSyncScarsState(serverPlayer, scars));
		}
	}

	private static void pulse(Level level, BlockPos pos) {
		if (level instanceof ServerLevel serverLevel) {
			PacketHandler.sendSanguineOmenEffect(pos.getCenter(), 24.0D, serverLevel, 30, 0.35F);
		}
	}

	private static void message(Player player, String text, ChatFormatting color) {
		player.displayClientMessage(Component.literal(text).withStyle(color), true);
	}
}
