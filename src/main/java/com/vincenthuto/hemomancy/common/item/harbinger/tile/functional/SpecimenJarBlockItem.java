package com.vincenthuto.hemomancy.common.item.harbinger.tile.functional;

import com.vincenthuto.hemomancy.common.entity.menagerie.MenagerieLeash;
import com.vincenthuto.hemomancy.common.entity.menagerie.MenagerieRules;
import com.vincenthuto.hemomancy.common.entity.summon.BoundPuppeteerSummon;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.entity.HemoEntityPredicates;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.UUID;

public class SpecimenJarBlockItem extends BlockItem {

	public SpecimenJarBlockItem(Block block, Properties properties) {
		super(block, properties);
	}

	@Override
	public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target,
			InteractionHand hand) {
		if (SpecimenJarData.hasSpecimen(stack)) {
			return InteractionResult.PASS;
		}
		// One rule for keeping a creature at all, shared with the leash and the vat (core-loop Track 5).
		MenagerieRules.Verdict kept = MenagerieLeash.verdict(target);
		if (!kept.keeps()) {
			if (!player.level().isClientSide && kept.messageKey() != null) {
				player.displayClientMessage(Component.translatable(kept.messageKey(), target.getDisplayName())
						.withStyle(ChatFormatting.GRAY), true);
			}
			return InteractionResult.PASS;
		}
		SpecimenCaptureRules.Verdict verdict = SpecimenCaptureRules.judge(candidate(target, player));
		if (verdict != SpecimenCaptureRules.Verdict.CAPTURE) {
			if (!player.level().isClientSide && verdict.messageKey() != null) {
				player.displayClientMessage(Component.translatable(verdict.messageKey(), target.getDisplayName())
						.withStyle(ChatFormatting.GRAY), true);
			}
			return InteractionResult.PASS;
		}
		if (player.level().isClientSide) {
			return InteractionResult.SUCCESS;
		}

		// A creature riding something is lifted off first: a passenger does not save, and would be deleted unsaved.
		if (target.isPassenger()) target.stopRiding();
		if (target instanceof net.minecraft.world.entity.Mob led) MenagerieLeash.release(led, false);
		CompoundTag specimen = SpecimenJarData.captureEntity(target);
		if (specimen.isEmpty()) {
			return InteractionResult.PASS;
		}
		ItemStack filled = new ItemStack(this);
		SpecimenJarData.setSpecimen(filled, specimen);
		SpecimenJarData.setKeptState(filled, com.vincenthuto.hemomancy.common.tile.harbinger.menagerie.VatVigorRules.CAPTURED,
				false, target.getMaxHealth());
		if (!player.getAbilities().instabuild && stack.getCount() <= 1) {
			player.setItemInHand(hand, filled);
		} else if (!player.getAbilities().instabuild) {
			stack.shrink(1);
			if (!player.addItem(filled)) {
				player.drop(filled, false);
			}
		} else if (!player.addItem(filled)) {
			player.drop(filled, false);
		}
		com.vincenthuto.hemomancy.common.mission.alchemist.BloodResearch.recordMet(player, target.getType());
		target.discard();
		return InteractionResult.CONSUME;
	}

	/** What the jar sees of {@code target}, held out by {@code player}. */
	public static SpecimenCaptureRules.Candidate candidate(LivingEntity target, Player player) {
		return new SpecimenCaptureRules.Candidate(target.getType().is(EntityInit.SPECIMEN_JAR_CAPTURABLE),
				target.isAlive(), target.getBbWidth(), target.getBbHeight(), target.getMaxHealth(),
				HemoEntityPredicates.isBoss(target), ownedByAnother(target, player.getUUID()),
				target.isVehicle());
	}

	private static boolean ownedByAnother(LivingEntity target, UUID player) {
		UUID owner = target instanceof OwnableEntity ownable ? ownable.getOwnerUUID()
				: target instanceof BoundPuppeteerSummon bound ? bound.hemomancy$getOwnerUUID() : null;
		return owner != null && !owner.equals(player);
	}

	/** A filled jar holds one creature and stacks no further (core-loop Track 5). */
	@Override
	public int getMaxStackSize(ItemStack stack) {
		return SpecimenJarData.hasSpecimen(stack) ? 1 : super.getMaxStackSize(stack);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		appendKept(stack, tooltip);
	}

	/** What a jar or vat item says of the creature it carries: its name, then its Vigor or that it is a husk. */
	public static void appendKept(ItemStack stack, List<Component> tooltip) {
		if (!SpecimenJarData.hasSpecimen(stack)) {
			tooltip.add(Component.translatable("tooltip.hemomancy.specimen_jar.empty"));
			return;
		}
		tooltip.add(Component.translatable("tooltip.hemomancy.specimen_jar.contains",
				SpecimenJarData.getSpecimenName(SpecimenJarData.getSpecimen(stack))));
		CompoundTag kept = SpecimenJarData.getKeptState(stack);
		if (kept.getBoolean("Husk")) {
			tooltip.add(Component.translatable("tooltip.hemomancy.specimen_jar.husk").withStyle(ChatFormatting.DARK_GRAY));
		} else if (kept.contains("Vigor")) {
			tooltip.add(Component.translatable("tooltip.hemomancy.specimen_jar.vigor", Math.round(kept.getDouble("Vigor")))
					.withStyle(ChatFormatting.GRAY));
		}
	}

}
