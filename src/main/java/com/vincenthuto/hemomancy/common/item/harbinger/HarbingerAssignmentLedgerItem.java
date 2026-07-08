package com.vincenthuto.hemomancy.common.item.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bestiary.SpecimenBestiaryDefinitions;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.mission.HarbingerArtificerAssignmentHelper;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.mission.OpenHarbingerAssignmentLedgerPacket;
import com.vincenthuto.hutoslib.common.item.ItemGuideBook;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class HarbingerAssignmentLedgerItem extends ItemGuideBook {
	public HarbingerAssignmentLedgerItem(Properties properties, ResourceLocation texture) {
		super(properties, texture);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
			boolean artificerArmaturePlaced = HarbingerAdvancementGranter.isArtificerArmaturePlaced(serverPlayer);
			boolean artificerFirstHematicUpgrade =
					HarbingerAdvancementGranter.isArtificerFirstHematicUpgrade(serverPlayer);
			boolean artificerHematicIronFitting =
					HarbingerAdvancementGranter.isArtificerHematicIronFitting(serverPlayer);
			boolean artificerFirstForkUpgrade =
					HarbingerAdvancementGranter.isArtificerFirstForkUpgrade(serverPlayer);
			boolean artificerForkFitting = HarbingerAdvancementGranter.isArtificerForkFitting(serverPlayer);
			boolean artificerFrameConsecrated =
					HarbingerAdvancementGranter.isArtificerFrameConsecrated(serverPlayer);
			boolean artificerFirstBloodLustUpgrade =
					HarbingerAdvancementGranter.isArtificerFirstBloodLustUpgrade(serverPlayer);
			boolean artificerBloodLustFitting =
					HarbingerAdvancementGranter.isArtificerBloodLustFitting(serverPlayer);
			boolean artificerMonolithicFrame =
					HarbingerAdvancementGranter.isArtificerMonolithicFrame(serverPlayer);
			boolean artificerFirstD7Upgrade =
					HarbingerAdvancementGranter.isArtificerFirstD7Upgrade(serverPlayer);
			boolean artificerD7Fitting = HarbingerAdvancementGranter.isArtificerD7Fitting(serverPlayer);
			boolean artificerFirstLivingGraft =
					HarbingerAdvancementGranter.isArtificerFirstLivingGraft(serverPlayer);
			int artificerLivingWeaponFormCount =
					HarbingerArtificerAssignmentHelper.knownLivingWeaponFormCount(serverPlayer);
			boolean artificerLivingArsenalFitting =
					HarbingerAdvancementGranter.isArtificerLivingArsenalFitting(serverPlayer);
			PacketHandler.sendToPlayer(serverPlayer, new OpenHarbingerAssignmentLedgerPacket(
					HemoCapabilityAccess.getPlayerDegreeNumber(serverPlayer),
					HarbingerAdvancementGranter.hasAdvancement(serverPlayer,
							Hemomancy.rloc("hemomancy/the_first_awakening")),
					HarbingerAdvancementGranter.hasAdvancement(serverPlayer,
							HarbingerAdvancementGranter.ADV_DEGREE_1_NEOPHYTE),
					HarbingerAdvancementGranter.isVesselFilled(serverPlayer),
					HarbingerAdvancementGranter.isLiberSanguinumCrafted(serverPlayer),
					HarbingerAdvancementGranter.isHematicIronBlockCrafted(serverPlayer),
					HarbingerAdvancementGranter.hasAdvancement(serverPlayer,
							HarbingerAdvancementGranter.ADV_HERMIT_ROAD_FIRST_REMNANT),
					HarbingerAdvancementGranter.hasAdvancement(serverPlayer,
							HarbingerAdvancementGranter.ADV_HERMIT_ROAD_LEDGER_GRANTED),
					hasVialCentrifuge(serverPlayer),
					hasSampledBloodVial(serverPlayer),
					HarbingerAdvancementGranter.isFirstSeparationStarted(serverPlayer),
					hasAnyEnzyme(serverPlayer),
					HarbingerAdvancementGranter.getRedTaxonomySpecimenCount(serverPlayer),
					HarbingerAdvancementGranter.isRedTaxonomyComplete(serverPlayer),
					HarbingerAdvancementGranter.getEnzymeMasteryCount(serverPlayer),
					HarbingerAdvancementGranter.isEnzymeMasteryComplete(serverPlayer),
					HemoCapabilityAccess.getSpecimenBestiary(serverPlayer)
							.map(progress -> progress.recordedSpecimenCount()).orElse(0),
					SpecimenBestiaryDefinitions.totalResearchSpecimens(),
					HemoCapabilityAccess.getSpecimenBestiary(serverPlayer)
							.map(progress -> progress.recordedMorphlingLayerCount()).orElse(0),
					hasBlankHematicMemory(serverPlayer),
					HarbingerAdvancementGranter.isMnemonistWovenVesselComplete(serverPlayer),
					HarbingerAdvancementGranter.isMnemonistFirstWeaveComplete(serverPlayer),
					HarbingerAdvancementGranter.isVicarMasonsRespiteDirective(serverPlayer),
					HarbingerAdvancementGranter.isVeinMasonFirstLesson(serverPlayer),
					HarbingerAdvancementGranter.isVeinMasonFirstScarCarved(serverPlayer),
					HarbingerAdvancementGranter.isVeinMasonFirstScarLearned(serverPlayer),
					HarbingerAdvancementGranter.isVeinMasonFirstEffigyPattern(serverPlayer),
					HarbingerAdvancementGranter.isVeinMasonFirstEffigyLoadout(serverPlayer),
					artificerArmaturePlaced, artificerFirstHematicUpgrade, artificerHematicIronFitting,
					artificerFirstForkUpgrade, artificerForkFitting, artificerFrameConsecrated,
					artificerFirstBloodLustUpgrade, artificerBloodLustFitting, artificerMonolithicFrame,
					artificerFirstD7Upgrade, artificerD7Fitting, artificerFirstLivingGraft,
					artificerLivingWeaponFormCount, artificerLivingArsenalFitting));
		}
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
	}

	private static boolean hasBlankHematicMemory(ServerPlayer player) {
		return player.getInventory().items.stream()
				.anyMatch(stack -> stack.is(ItemInit.hematic_memory.get()));
	}

	private static boolean hasVialCentrifuge(ServerPlayer player) {
		return player.getInventory().items.stream()
				.anyMatch(stack -> stack.is(BlockInit.vial_centrifuge.get().asItem()));
	}

	private static boolean hasSampledBloodVial(ServerPlayer player) {
		return player.getInventory().items.stream()
				.anyMatch(stack -> stack.getItem() instanceof BloodVialItem
						&& BloodVialItem.getEntityType(stack) != null);
	}

	private static boolean hasAnyEnzyme(ServerPlayer player) {
		return player.getInventory().items.stream().anyMatch(HarbingerAssignmentLedgerItem::isEnzyme);
	}

	private static boolean isEnzyme(ItemStack stack) {
		return stack.is(ItemInit.vivacious_enzyme.get())
				|| stack.is(ItemInit.fervent_enzyme.get())
				|| stack.is(ItemInit.neurotic_enzyme.get())
				|| stack.is(ItemInit.incandescent_enzyme.get())
				|| stack.is(ItemInit.ruinous_enzyme.get())
				|| stack.is(ItemInit.frigid_enzyme.get())
				|| stack.is(ItemInit.ferric_enzyme.get())
				|| stack.is(ItemInit.umbral_enzyme.get());
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, context, tooltip, flag);
		tooltip.add(Component.translatable("item.hemomancy.harbinger_assignment_ledger.tooltip")
				.withStyle(ChatFormatting.GRAY));
		tooltip.add(Component.translatable("item.hemomancy.harbinger_assignment_ledger.tooltip.use")
				.withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
	}
}
