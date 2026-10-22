package com.vincenthuto.hemomancy.common.rite;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodAccounting;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.IScars;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.ScarType;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.ScarInit;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.ItemScar;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.ItemScarPattern;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.ScarDefinition;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.BloodAbsorptionItem;
import com.vincenthuto.hemomancy.common.mission.cicatrix_anchorite.VeinMasonAssignments;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.scars.PacketSyncScarsState;
import com.vincenthuto.hemomancy.common.rite.brazier.BrazierRite;
import com.vincenthuto.hemomancy.common.rite.brazier.BrazierRites;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Scar rites at the Iron Brazier: learning a cerebral scar, committing a prepared Effigy loadout, or scouring the
 * active loadout with a blank motif. Eligibility is checked once by {@link #preflight(Player, ItemStack,
 * ScarBrazierInteractionRules.Burn)}; the rite branches act on that result instead of validating again.
 */
public final class ScarBrazierRite implements BrazierRite<ScarBrazierRite.ScarPlan> {
	private static final BloodAccounting.Source SCAR_RITE = BloodAccounting.Source.of("scar_brazier_rite", Category.RITE, true, false);
	public static final int REQUIRED_DEGREE = 4;
	public static final double LEARN_BLOOD_COST = 100.0D;
	public static final double LOADOUT_BLOOD_COST = 50.0D;
	public static final ScarBrazierRite INSTANCE = new ScarBrazierRite();

	private ScarBrazierRite() {
	}

	public static ScarBrazierInteractionRules.Burn selectOffering(boolean lit, ItemStack stack) {
		return ScarBrazierInteractionRules.selectOffering(lit,
				stack.getItem() instanceof ItemScar,
				stack.getItem() instanceof ItemScarPattern && ItemScarPattern.hasPreparedLoadout(stack),
				stack.is(ItemInit.runic_motif_paper.get()));
	}

	public static double tryAbsorb(ServerLevel level, BlockPos pos, BlockState state, ServerPlayer player,
			double maxAmount) {
		return BrazierRites.run(INSTANCE, level, pos, state, player, maxAmount);
	}

	@Override
	public boolean accepts(ItemStack offering) {
		return selectOffering(true, offering) != ScarBrazierInteractionRules.Burn.NONE;
	}

	@Override
	public Gate<ScarPlan> preflight(Channel channel) {
		ServerPlayer player = channel.player();
		ItemStack offering = channel.offering();
		ScarBrazierInteractionRules.Burn burn = selectOffering(channel.lit(), offering);
		if (burn == ScarBrazierInteractionRules.Burn.NONE) {
			return Gate.refuse(false, false);
		}
		if (!ScarBrazierInteractionRules.canAbsorb(BloodAbsorptionItem.isChannelingBloodAbsorption(player),
				channel.maxAmount())) {
			return Gate.refuse(true, false);
		}
		Preflight preflight = preflight(player, offering, burn);
		if (!preflight.allowed()) {
			BrazierRites.messageEverySecond(channel.level(), player, preflight.message(), preflight.color());
			return Gate.refuse(true, true);
		}
		return Gate.pass("scar:" + burn.name(), new ScarPlan(burn, preflight));
	}

	@Override
	public Completion complete(Channel channel, ScarPlan plan) {
		ItemStack ritualStack = channel.offering().copy();
		int countBefore = ritualStack.getCount();
		perform(channel.level(), channel.pos(), channel.player(), ritualStack, plan.burn(), plan.preflight());
		return ritualStack.getCount() < countBefore ? Completion.CONSUMED : Completion.kept(true);
	}

	/**
	 * Performs the scar rite {@code burn} names on {@code stack} at once, without a channel. Returns false only when
	 * there is no scar rite to perform.
	 */
	public static boolean burn(Level level, BlockPos pos, Player player, ItemStack stack,
			ScarBrazierInteractionRules.Burn burn) {
		Preflight preflight = preflight(player, stack, burn);
		if (!preflight.allowed()) {
			message(player, preflight.message(), preflight.color());
			return burn != ScarBrazierInteractionRules.Burn.NONE;
		}
		perform(level, pos, player, stack, burn, preflight);
		return true;
	}

	public static int getMaxActiveScars(Player player) {
		int base = ScarBrazierInteractionRules.maxActiveScars(HemoCapabilityAccess.getPlayerDegreeNumber(player));
		return base == 0 ? 0 : base + com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper
				.getScarResonanceSlots(player);
	}

	/** The validated burn a passing preflight hands to completion. */
	public record ScarPlan(ScarBrazierInteractionRules.Burn burn, Preflight preflight) {
	}

	/**
	 * One scar-rite check. When allowed it carries what the rite branch acts on: the caster's scar state, the scar to
	 * learn (LEARN) and the loadout to activate (COMMIT).
	 */
	public record Preflight(boolean allowed, String message, ChatFormatting color, IScars scars,
			ResourceLocation scarId, List<ResourceLocation> selected) {
		private static Preflight accepted(IScars scars, ResourceLocation scarId, List<ResourceLocation> selected) {
			return new Preflight(true, "", ChatFormatting.WHITE, scars, scarId, selected);
		}

		private static Preflight denied(String message, ChatFormatting color) {
			return new Preflight(false, message, color, null, null, List.of());
		}
	}

	private static Preflight preflight(Player player, ItemStack stack, ScarBrazierInteractionRules.Burn burn) {
		if (burn == ScarBrazierInteractionRules.Burn.NONE) {
			return Preflight.denied("The brazier finds no scar rite in this offering.", ChatFormatting.RED);
		}
		if (!hasDegree(player)) {
			return Preflight.denied(
					"The Iron Brazier answers this scar rite only at the Fourth Degree and above.",
					ChatFormatting.RED);
		}
		IScars scars = HemoCapabilityAccess.getScarState(player);
		if (scars == null) {
			return Preflight.denied("Your scar memory is silent.", ChatFormatting.RED);
		}
		if (burn == ScarBrazierInteractionRules.Burn.LEARN) {
			if (!(stack.getItem() instanceof ItemScar scarItem)) {
				return Preflight.denied("Only scar items can be burned into memory.", ChatFormatting.RED);
			}
			ScarDefinition definition = scarItem.getScarDefinition();
			if (definition == null || definition.getScarType() != ScarType.CEREBRAL) {
				return Preflight.denied("Only cerebral scar items can be burned into memory.", ChatFormatting.RED);
			}
			ResourceLocation scarId = ScarInit.SCARS_TYPE_REGISTRY.getKey(definition);
			if (scarId == null) {
				return Preflight.denied("This scar has no registered pattern to remember.", ChatFormatting.RED);
			}
			if (scars.knowsCerebralScar(scarId)) {
				return Preflight.denied("You already know this scar.", ChatFormatting.GOLD);
			}
			return hasBlood(player, LEARN_BLOOD_COST)
					? Preflight.accepted(scars, scarId, List.of())
					: Preflight.denied("Not enough blood to sear the scar into memory.", ChatFormatting.RED);
		}
		if (burn == ScarBrazierInteractionRules.Burn.COMMIT) {
			if (!(stack.getItem() instanceof ItemScarPattern) || !ItemScarPattern.hasPreparedLoadout(stack)) {
				return Preflight.denied("This scar pattern carries no prepared loadout.", ChatFormatting.RED);
			}
			List<ResourceLocation> selected = ItemScarPattern.getScarIds(stack);
			if (selected.isEmpty()) {
				return Preflight.denied("This scar pattern carries no loadout.", ChatFormatting.RED);
			}
			if (selected.size() > getMaxActiveScars(player)) {
				return Preflight.denied("That pattern exceeds your current scar capacity.", ChatFormatting.RED);
			}
			for (ResourceLocation id : selected) {
				ScarDefinition definition = ScarInit.getByName(id.toString());
				if (definition == null || definition.getScarType() != ScarType.CEREBRAL
						|| !scars.knowsCerebralScar(id)) {
					return Preflight.denied(
							"The pattern contains a scar you have not learned: " + id.getPath(), ChatFormatting.RED);
				}
			}
			return hasBlood(player, LOADOUT_BLOOD_COST)
					? Preflight.accepted(scars, null, List.copyOf(selected))
					: Preflight.denied("Not enough blood to seal the scar loadout.", ChatFormatting.RED);
		}
		if (!stack.is(ItemInit.runic_motif_paper.get())) {
			return Preflight.denied("The brazier requires a blank runic motif to clear scars.", ChatFormatting.RED);
		}
		if (scars.getActiveCerebralScars().isEmpty()) {
			return Preflight.denied("No active cerebral scars answer the flame.", ChatFormatting.GOLD);
		}
		return hasBlood(player, LOADOUT_BLOOD_COST)
				? Preflight.accepted(scars, null, List.of())
				: Preflight.denied("Not enough blood to scour the active scar loadout.", ChatFormatting.RED);
	}

	private static boolean hasBlood(Player player, double cost) {
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player);
		return volume != null && volume.isActive() && volume.getBloodVolume() >= cost;
	}

	private static void perform(Level level, BlockPos pos, Player player, ItemStack stack,
			ScarBrazierInteractionRules.Burn burn, Preflight preflight) {
		switch (burn) {
			case LEARN -> tryLearnScar(level, pos, player, stack, preflight);
			case COMMIT -> tryCommitLoadout(level, pos, player, stack, preflight);
			case CLEAR -> tryClearLoadout(level, pos, player, stack, preflight);
			case NONE -> {
			}
		}
	}

	private static void tryLearnScar(Level level, BlockPos pos, Player player, ItemStack stack, Preflight preflight) {
		IScars scars = preflight.scars();
		spendBlood(player, LEARN_BLOOD_COST);
		scars.addKnownCerebralScar(preflight.scarId());
		if (player instanceof ServerPlayer serverPlayer) {
			HarbingerAdvancementGranter.grantIfNotDone(serverPlayer,
					HarbingerAdvancementGranter.ADV_VEIN_MASON_FIRST_SCAR_LEARNED);
		}
		stack.shrink(1);
		syncScarState(player, scars);
		pulse(level, pos);
		message(player, "The scar burns cleanly into your cerebral map.", ChatFormatting.DARK_RED);
	}

	private static void tryCommitLoadout(Level level, BlockPos pos, Player player, ItemStack stack,
			Preflight preflight) {
		IScars scars = preflight.scars();
		List<ResourceLocation> selected = preflight.selected();
		spendBlood(player, LOADOUT_BLOOD_COST);
		List<ResourceLocation> previous = List.copyOf(scars.getActiveCerebralScars());

		for (ResourceLocation active : List.copyOf(scars.getActiveCerebralScars())) {
			scars.deactivateCerebralScar(active);
		}
		for (ResourceLocation id : selected) {
			scars.activateCerebralScar(id);
		}
		if (player instanceof ServerPlayer serverPlayer) {
			if (HarbingerAdvancementGranter.isVeinMasonFirstEffigyPattern(serverPlayer)) {
				HarbingerAdvancementGranter.grantIfNotDone(serverPlayer,
						HarbingerAdvancementGranter.ADV_VEIN_MASON_FIRST_EFFIGY_LOADOUT);
			} else if (!HarbingerAdvancementGranter.isVeinMasonFirstEffigyLoadout(serverPlayer)) {
				message(player, "The loadout takes, but the Vein-Mason still requires your own Effigy preparation.",
						ChatFormatting.GOLD);
			}
			if (HarbingerAdvancementGranter.isVeinMasonFirstEffigyLoadout(serverPlayer)
					&& !HarbingerAdvancementGranter.isVeinMasonRewardClaimed(serverPlayer)) {
				HarbingerAdvancementGranter.grantIfNotDone(serverPlayer,
						HarbingerAdvancementGranter.ADV_VEIN_MASON_CONTINUATION_READY);
			}
			VeinMasonAssignments.onChangedLoadout(serverPlayer, previous, selected);
		}
		stack.shrink(1);
		syncScarState(player, scars);
		pulse(level, pos);
		message(player, "The pattern collapses into your active scar loadout.", ChatFormatting.DARK_RED);
	}

	private static void tryClearLoadout(Level level, BlockPos pos, Player player, ItemStack stack,
			Preflight preflight) {
		IScars scars = preflight.scars();
		spendBlood(player, LOADOUT_BLOOD_COST);

		for (ResourceLocation active : List.copyOf(scars.getActiveCerebralScars())) {
			scars.deactivateCerebralScar(active);
		}
		stack.shrink(1);
		syncScarState(player, scars);
		pulse(level, pos);
		message(player, "The blank motif burns away your active cerebral scar loadout.", ChatFormatting.DARK_RED);
	}

	private static boolean hasDegree(Player player) {
		return HemoCapabilityAccess.getPlayerDegreeNumber(player) >= REQUIRED_DEGREE;
	}

	/** Spends blood a passing preflight has already confirmed through {@link #hasBlood}. */
	private static void spendBlood(Player player, double cost) {
		BloodAccounting.pay(player, cost, SCAR_RITE);
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
