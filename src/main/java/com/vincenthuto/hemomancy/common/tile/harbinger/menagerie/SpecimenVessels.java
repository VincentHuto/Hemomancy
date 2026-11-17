package com.vincenthuto.hemomancy.common.tile.harbinger.menagerie;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.HematicAcclimationRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.entity.menagerie.MenagerieLeash;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodProfileData;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodSampleData;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodSamplingResult;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodVialItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tile.functional.SpecimenJarData;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.mission.alchemist.BloodResearch;
import com.vincenthuto.hemomancy.common.mission.alchemist.ClinicalBloodKnowledge;
import com.vincenthuto.hemomancy.common.tile.BlockBloodVolume;
import com.vincenthuto.hemomancy.common.tile.BloodDrawRights;
import com.vincenthuto.hemomancy.common.util.BlockEntitySync;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

/**
 * What the Specimen Jar and the Specimen Vat share as blocks (core-loop Track 5): their second's tick, held
 * Absorption drawing from the creature, Projection feeding the buffer, an empty hand reading the creature, taking out
 * a husk or putting a led creature in, and a vial drawing a sample. The creature's own state is {@link KeptSpecimen}.
 */
public final class SpecimenVessels {
	/** An Absorption tick that holds the channel on a vessel without pulling, so the stream does not fall through. */
	public static final double HELD = 0.01D;
	/** How far a led creature may stand from an empty vessel and still be put in. */
	public static final double LEAD_IN_RANGE = 6.0D;
	/** How far a Sanguine Vigil's tending reaches. */
	public static final int VIGIL_REACH = 8;

	private SpecimenVessels() {
	}

	/** Once a second: the creature settles, eats from the buffer, dies, rattles or breaks out. */
	public static void serverTick(Level level, BlockPos pos, SpecimenVessel vessel) {
		if (!(level instanceof ServerLevel server)) return;
		if (Math.floorMod(server.getGameTime() + pos.asLong(), VatVigorRules.FEED_INTERVAL) != 0L) return;
		vessel.buffer();
		BlockEntity blockEntity = (BlockEntity) vessel;
		boolean changed = vessel.kept().tick(server, pos, blockEntity);
		// The brood takes its share after the creature has eaten (P6); a vessel with no pairing does nothing here.
		changed |= SpecimenBrood.second(server, pos, vessel);
		if (changed) BlockEntitySync.markAndSync(blockEntity);
		else blockEntity.setChanged();
	}

	// ── Absorption and Projection ──

	/** Held Absorption on a vessel: a kept creature is drawn; an empty vessel or a husk lets the buffer be drunk. */
	public static double absorb(ServerLevel level, SpecimenVessel vessel, ServerPlayer player) {
		KeptSpecimen kept = vessel.kept();
		BlockEntity blockEntity = (BlockEntity) vessel;
		vessel.buffer();
		KeptSpecimen.DrawOutcome outcome = kept.draw(level, player, blockEntity);
		boolean speak = level.getGameTime() % 20L == 0L;
		return switch (outcome) {
			case PULLED -> {
				if (level.getGameTime() % 20L < VatVigorRules.DRAW_INTERVAL && !BloodResearch.knows(player, kept.typeId())) {
					player.displayClientMessage(Component.translatable("message.hemomancy.absorption.unread_weak")
							.withStyle(ChatFormatting.GRAY), true);
				}
				yield HELD;
			}
			case WAITING -> HELD;
			case EMPTY, HUSK, DORMANT -> 0.0D;
			case PRESERVED -> {
				if (speak) player.displayClientMessage(Component.translatable("message.hemomancy.menagerie.preserved"), true);
				yield HELD;
			}
			case NOT_YOURS -> {
				if (speak) player.displayClientMessage(Component.translatable("message.hemomancy.living_blood.not_yours"), true);
				yield HELD;
			}
			case SPENT -> {
				if (speak) player.displayClientMessage(Component.translatable("message.hemomancy.menagerie.spent",
						kept.specimen().isEmpty() ? Component.empty() : SpecimenJarData.getSpecimenName(kept.specimen()))
						.withStyle(ChatFormatting.GRAY), true);
				yield HELD;
			}
		};
	}

	/** Projection into a vessel fills its living buffer, which the creature eats as Vigor. */
	public static double project(SpecimenVessel vessel, ServerPlayer player, BlockPos pos, double rate) {
		vessel.buffer();
		return vessel.takeProjectedBlood(player, pos, rate, false);
	}

	// ── Use ──

	/**
	 * An empty hand on a vessel: takes out a husk, puts a led creature that fits into an empty vessel, or opens the
	 * vessel's screen on its creature (P6; it used to print {@link #readout} to chat). Returns false when there was
	 * nothing to do: an empty vessel with no led creature near, or a hand that holds something, which keeps its own use
	 * (a Living Syringe samples the creature through its own {@code useOn}).
	 */
	public static boolean useEmptyHand(ServerLevel level, BlockPos pos, SpecimenVessel vessel, ServerPlayer player,
			Predicate<Mob> fits) {
		KeptSpecimen kept = vessel.kept();
		BlockEntity blockEntity = (BlockEntity) vessel;
		if (kept.husk()) {
			if (!mayHandle(vessel, player)) return true;
			SpecimenBrood.sever(level, vessel);
			for (ItemStack leaving : kept.takeHusk()) {
				if (!player.addItem(leaving)) player.drop(leaving, false);
			}
			BlockEntitySync.markAndSync(blockEntity);
			player.displayClientMessage(Component.translatable("message.hemomancy.menagerie.husk_taken")
					.withStyle(ChatFormatting.GRAY), false);
			return true;
		}
		if (!kept.occupied()) return leadIn(level, pos, vessel, player, fits);
		if (!opensScreen(player)) return false;
		BloodResearch.recordMet(player, kept.type());
		player.openMenu(vessel, pos);
		return true;
	}

	/** Whether a use on a kept creature opens its screen: only an empty main hand does, and never while sneaking. */
	public static boolean opensScreen(Player player) {
		return player.getMainHandItem().isEmpty() && !player.isShiftKeyDown();
	}

	/** The data a vessel's screen shows {@code viewer}, worked out on the server. */
	public static VesselView viewData(SpecimenVessel vessel, ServerPlayer viewer) {
		return new VesselView(vessel, viewer);
	}

	/** Puts the nearest creature {@code player} leads, and that fits, into the empty vessel. */
	public static boolean leadIn(ServerLevel level, BlockPos pos, SpecimenVessel vessel, ServerPlayer player,
			Predicate<Mob> fits) {
		KeptSpecimen kept = vessel.kept();
		if (kept.occupied()) return false;
		List<Mob> leads = MenagerieLeash.ledBy(level, player.getUUID());
		if (leads.isEmpty()) return false;
		if (!mayHandle(vessel, player)) return true;
		Mob led = leads.stream()
				.filter(mob -> mob.distanceToSqr(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D) <= LEAD_IN_RANGE * LEAD_IN_RANGE)
				.filter(fits)
				.min(java.util.Comparator.comparingDouble(mob -> mob.distanceToSqr(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D)))
				.orElse(null);
		if (led == null) {
			// A led creature near enough but too big or strong for this vessel is told what it needs.
			Component why = leads.stream()
					.filter(mob -> mob.distanceToSqr(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D) <= LEAD_IN_RANGE * LEAD_IN_RANGE)
					.map(vessel::tooSmallFor).filter(java.util.Objects::nonNull).findFirst()
					.orElse(Component.translatable("message.hemomancy.menagerie.nothing_to_put_in"));
			player.displayClientMessage(why.copy().withStyle(ChatFormatting.GRAY), true);
			return false;
		}
		MenagerieLeash.release(led, false);
		if (led.isPassenger()) led.stopRiding();
		CompoundTag saved = SpecimenJarData.captureEntity(led);
		if (saved.isEmpty()) return false;
		float max = led.getMaxHealth();
		// A weakened creature starts as weak as it was taken; never under a fifth.
		double start = Math.max(20.0D, Math.min(VatVigorRules.CAPTURED, 100.0D * led.getHealth() / Math.max(1.0F, max)));
		kept.keep(saved, max, start, level.getGameTime());
		BlockEntity blockEntity = (BlockEntity) vessel;
		BlockBloodVolume buffer = vessel.buffer();
		if (buffer.owner() == null) buffer.setOwner(player.getUUID());
		led.discard();
		BloodResearch.recordMet(player, kept.type());
		HarbingerAdvancementGranter.grantIfNotDone(player, HarbingerAdvancementGranter.ADV_IN_GLASS);
		level.playSound(null, pos, SoundEvents.BOTTLE_FILL_DRAGONBREATH, SoundSource.BLOCKS, 0.8F, 0.7F);
		BlockEntitySync.markAndSync(blockEntity);
		player.displayClientMessage(Component.translatable("message.hemomancy.menagerie.put_in",
				SpecimenJarData.getSpecimenName(saved)), true);
		return true;
	}

	/**
	 * An empty vial on a vessel draws a sample from its creature, paid in Vigor. A kind whose hide only the Living
	 * Syringe pierces refuses a vial in glass as it would in the wild, at no cost (P6). Returns the filled vial, or
	 * empty.
	 */
	public static ItemStack sample(ServerLevel level, BlockPos pos, SpecimenVessel vessel, ServerPlayer player,
			ItemStack vial, InteractionHand hand) {
		if (!(vial.getItem() instanceof BloodVialItem) || BloodSampleData.isFilled(vial)) return ItemStack.EMPTY;
		KeptSpecimen kept = vessel.kept();
		if (BloodProfileData.profile(kept.type(), false).requiresLivingSyringe()) {
			player.displayClientMessage(Component.translatable(BloodSamplingResult.REQUIRES_LIVING_SYRINGE.translationKey(),
					SpecimenJarData.getSpecimenName(kept.specimen())), true);
			return ItemStack.EMPTY;
		}
		if (!maySample(vessel, player)) return ItemStack.EMPTY;
		ResourceLocation type = kept.sample(level, (BlockEntity) vessel, VatVigorRules.SAMPLE_COST);
		if (type == null) {
			player.displayClientMessage(Component.translatable("message.hemomancy.menagerie.cannot_sample")
					.withStyle(ChatFormatting.GRAY), true);
			return ItemStack.EMPTY;
		}
		ItemStack sample = vial.copyWithCount(1);
		CompoundTag tag = sample.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		tag.putString(BloodVialItem.TAG_ENTITY_TYPE, type.toString());
		tag.putBoolean(BloodVialItem.TAG_STATE, true);
		tag.putString(BloodSampleData.TAG_DONOR, donor(pos, kept, VatVigorRules.SAMPLE_COST));
		sample.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
		vial.shrink(1);
		if (vial.isEmpty()) player.setItemInHand(hand, sample);
		else if (!player.addItem(sample)) player.drop(sample, false);
		ClinicalBloodKnowledge.collected(player, sample);
		BloodResearch.recordMet(player, kept.type());
		level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
		return sample;
	}

	/**
	 * A Living Syringe draws a kept creature of any kind for {@link VatVigorRules#SYRINGE_SAMPLE_COST} Vigor, under the
	 * same keeper's rights and the same refusal of a preserved specimen. Returns the sampled kind and its donor tag, or
	 * null when nothing was drawn (and nothing paid). The syringe's own code stores the sample in its rack or vial.
	 */
	@Nullable
	public static Drawn syringeSample(ServerLevel level, BlockPos pos, SpecimenVessel vessel, ServerPlayer player) {
		KeptSpecimen kept = vessel.kept();
		if (!kept.living() || !maySample(vessel, player)) return null;
		ResourceLocation type = kept.sample(level, (BlockEntity) vessel, VatVigorRules.SYRINGE_SAMPLE_COST);
		if (type == null) {
			player.displayClientMessage(Component.translatable("message.hemomancy.menagerie.cannot_sample")
					.withStyle(ChatFormatting.GRAY), true);
			return null;
		}
		BloodResearch.recordMet(player, kept.type());
		level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 0.8F);
		player.displayClientMessage(Component.translatable("message.hemomancy.menagerie.syringe_sample",
				SpecimenJarData.getSpecimenName(kept.specimen()), Math.round(VatVigorRules.SYRINGE_SAMPLE_COST)), true);
		return new Drawn(type, donor(pos, kept, VatVigorRules.SYRINGE_SAMPLE_COST));
	}

	/** What a syringe drew from a vessel: the kind and the donor tag its sample carries. */
	public record Drawn(ResourceLocation type, String donor) {
	}

	/**
	 * Whether {@code player} may take a husk from, lead a creature into or pick up {@code vessel} (CQ-4): its owner and
	 * their bloodline, or anyone when nobody owns it. A refusal says why.
	 */
	public static boolean mayHandle(SpecimenVessel vessel, Player player) {
		if (BloodDrawRights.mayDraw(player, (BlockEntity) vessel)) return true;
		player.displayClientMessage(Component.translatable("message.hemomancy.living_blood.not_yours"), true);
		return false;
	}

	/** A preserved specimen gives nothing, and a kept one is sampled only by its keeper and their bloodline (CL-8). */
	private static boolean maySample(SpecimenVessel vessel, ServerPlayer player) {
		if (vessel.buffer().owner() == null) {
			player.displayClientMessage(Component.translatable("message.hemomancy.menagerie.preserved"), true);
			return false;
		}
		if (!BloodDrawRights.mayDraw(player, (BlockEntity) vessel)) {
			player.displayClientMessage(Component.translatable("message.hemomancy.living_blood.not_yours"), true);
			return false;
		}
		return true;
	}

	/**
	 * The donor a vessel sample records: the vessel and the Vigor band the creature was drawn at, so one kept creature
	 * at three Vigor levels counts as three individuals for second-level research.
	 */
	private static String donor(BlockPos pos, KeptSpecimen kept, double paid) {
		return "vessel:" + pos.toShortString().replace(" ", "") + ":" + VatVigorRules.band(kept.vigor() + paid);
	}

	// ── Reading ──

	/** What a vessel says to {@code viewer}: the creature, its Vigor and buffer, and its blood as far as they know it. */
	public static List<Component> readout(Player viewer, SpecimenVessel vessel) {
		KeptSpecimen kept = vessel.kept();
		List<Component> lines = new ArrayList<>();
		if (!kept.occupied()) {
			lines.add(Component.translatable("message.hemomancy.menagerie.readout.empty").withStyle(ChatFormatting.GRAY));
			return lines;
		}
		CompoundTag specimen = kept.specimen();
		lines.add(Component.translatable("message.hemomancy.menagerie.readout.header", SpecimenJarData.getSpecimenName(specimen))
				.withStyle(ChatFormatting.DARK_RED));
		if (kept.husk()) {
			lines.add(Component.translatable("message.hemomancy.menagerie.readout.husk").withStyle(ChatFormatting.GRAY));
			return lines;
		}
		BlockBloodVolume buffer = vessel.buffer();
		if (buffer.owner() == null) {
			lines.add(Component.translatable("message.hemomancy.menagerie.readout.preserved").withStyle(ChatFormatting.GRAY));
		} else {
			lines.add(Component.translatable("message.hemomancy.menagerie.readout.vigor", Math.round(kept.vigor()),
					Component.translatable("message.hemomancy.menagerie.vigor." + VatVigorRules.word(kept.vigor())))
					.withStyle(ChatFormatting.GRAY));
			lines.add(Component.translatable("message.hemomancy.menagerie.readout.buffer",
					Math.round(buffer.getBloodVolume()), Math.round(buffer.capacity())).withStyle(ChatFormatting.GRAY));
			if (kept.tended(((BlockEntity) vessel).getLevel() == null ? 0L : ((BlockEntity) vessel).getLevel().getGameTime())) {
				lines.add(Component.translatable("message.hemomancy.menagerie.readout.tended").withStyle(ChatFormatting.GRAY));
			}
		}
		ResourceLocation type = kept.typeId();
		if (!BloodResearch.knows(viewer, type)) {
			lines.add(Component.translatable("message.hemomancy.menagerie.readout.unread").withStyle(ChatFormatting.DARK_GRAY));
			return lines;
		}
		List<EnumBloodTemper> schools = kept.schools(viewer.level().isClientSide);
		lines.add(Component.translatable("message.hemomancy.menagerie.readout.schools", schoolList(schools))
				.withStyle(ChatFormatting.GRAY));
		if (BloodResearch.studied(viewer, type)) {
			long now = viewer.level().getGameTime();
			double acclimation = HematicAcclimationRules.multiplier(
					HemoCapabilityAccess.getPowerGuardrails(viewer).hematicExposure(String.valueOf(type), now));
			lines.add(Component.translatable("message.hemomancy.menagerie.readout.yield",
					String.format(Locale.ROOT, "%.1f", VatVigorRules.temperPerDraw(kept.vigor(), acclimation)),
					String.format(Locale.ROOT, "%.1f", VatVigorRules.bloodPerDraw(kept.vigor(), acclimation)))
					.withStyle(ChatFormatting.GRAY));
		} else {
			lines.add(Component.translatable("message.hemomancy.menagerie.readout.study",
					ClinicalStudy.remaining(viewer, type)).withStyle(ChatFormatting.DARK_GRAY));
		}
		return lines;
	}

	/** A creature's schools as one phrase, "Flammeus and Lux", for its readout and its screen. */
	public static Component schoolList(List<EnumBloodTemper> schools) {
		if (schools.isEmpty()) return Component.translatable("message.hemomancy.menagerie.readout.no_schools");
		MutableComponent list = Component.empty();
		for (int i = 0; i < schools.size(); i++) {
			if (i > 0) list.append(Component.translatable("message.hemomancy.menagerie.readout.and"));
			list.append(BloodManipulation.temperName(schools.get(i)));
		}
		return list;
	}

	/** How many more individuals of a kind a player must examine before its yield is known. */
	private static final class ClinicalStudy {
		static int remaining(Player viewer, ResourceLocation type) {
			return Math.max(0, com.vincenthuto.hemomancy.common.mission.alchemist.ClinicalBloodProgress.STUDY_INDIVIDUALS
					- BloodResearch.individuals(viewer, type));
		}
	}

	/**
	 * What is known of {@code type} for its quirks (P6): read from rules that already hold, with {@code inherited} the
	 * school one kept individual carries (null in the Bestiary, which speaks of the kind).
	 */
	public static KeptQuirks.Facts quirkFacts(@Nullable EntityType<?> type, float maxHealth, @Nullable EnumBloodTemper inherited,
			boolean clientSide) {
		boolean hostile = type != null && type.getCategory() == MobCategory.MONSTER;
		boolean jarKind = type != null && type.is(EntityInit.SPECIMEN_JAR_CAPTURABLE);
		return new KeptQuirks.Facts(hostile, maxHealth, BloodProfileData.profile(type, clientSide).requiresLivingSyringe(),
				jarKind, SpecimenBrood.breeds(type), inherited);
	}

	/** A kind's maximum health as it spawns, for the Bestiary's notes; 10 when the kind has no attributes. */
	public static float baseMaxHealth(@Nullable EntityType<?> type) {
		if (type == null) return 10.0F;
		try {
			@SuppressWarnings("unchecked")
			var supplier = DefaultAttributes.getSupplier((EntityType<? extends LivingEntity>) type);
			return supplier.hasAttribute(Attributes.MAX_HEALTH) ? (float) supplier.getBaseValue(Attributes.MAX_HEALTH) : 10.0F;
		} catch (RuntimeException notLiving) {
			return 10.0F;
		}
	}

	// ── Breaking and tending ──

	/** A vessel broken with a creature in it lets the creature go; a husk drops its leavings. */
	public static void releaseOnRemove(ServerLevel level, BlockPos pos, SpecimenVessel vessel, boolean suppressed) {
		KeptSpecimen kept = vessel.kept();
		// Broken or lifted, a vessel loses its brood on both sides; what was paid is gone (P6).
		SpecimenBrood.sever(level, vessel);
		if (suppressed || !kept.occupied()) return;
		if (kept.husk()) {
			kept.dropHusk(level, pos);
			return;
		}
		CompoundTag specimen = kept.specimen();
		if (SpecimenJarData.releaseSpecimen(level, pos, specimen).isEmpty()) {
			ItemStack stack = new ItemStack(((BlockEntity) vessel).getBlockState().getBlock());
			kept.saveToItem(stack);
			net.minecraft.world.level.block.Block.popResource(level, pos, stack);
		}
		kept.clear();
	}

	/** A tending Sanguine Vigil keeps every kept creature within its reach from fading until {@code until}. */
	public static int tendAround(ServerLevel level, BlockPos centre, long until) {
		int tended = 0;
		AABB box = new AABB(centre).inflate(VIGIL_REACH);
		int minX = ((int) Math.floor(box.minX)) >> 4, maxX = ((int) Math.floor(box.maxX)) >> 4;
		int minZ = ((int) Math.floor(box.minZ)) >> 4, maxZ = ((int) Math.floor(box.maxZ)) >> 4;
		for (int cx = minX; cx <= maxX; cx++) {
			for (int cz = minZ; cz <= maxZ; cz++) {
				LevelChunk chunk = level.getChunkSource().getChunkNow(cx, cz);
				if (chunk == null) continue;
				for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
					// A joined vat's other vats answer for the vat that runs it; only that one is tended.
					if (!(blockEntity instanceof SpecimenVessel vessel) || vessel.vessel() != vessel
							|| !box.contains(blockEntity.getBlockPos().getCenter()) || !vessel.kept().living()) continue;
					vessel.kept().settle(level.getGameTime(), vessel.buffer().owner() != null);
					vessel.kept().tendUntil(until);
					blockEntity.setChanged();
					tended++;
				}
			}
		}
		return tended;
	}
}
