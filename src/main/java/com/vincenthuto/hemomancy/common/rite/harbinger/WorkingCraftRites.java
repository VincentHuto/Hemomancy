package com.vincenthuto.hemomancy.common.rite.harbinger;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.livingstaff.LivingStaffBondHelper;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberKnowledgeHelper;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointGainEvents;
import com.vincenthuto.hemomancy.common.event.MachineAccessEvents;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.mission.vicar.FirstBloodcraftAssignment;
import com.vincenthuto.hemomancy.common.rite.structure.BloodCraftingPatternBlockRules;
import com.vincenthuto.hemomancy.common.rite.structure.BloodStructureCraftingHelper;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.recipe.RecipeDegreeGates;
import com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteEventKind;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteMediumRules;
import com.vincenthuto.hemomancy.common.rite.CardinalRitePhase;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.CardinalFocusBlockEntity;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The Working craft rite variant: a built subject beside the Cardinal Focus is
 * fed, sealed, swallowed by the Working floor, and forged into its result. Its
 * brazier offerings are held in {@link RiteOfferingEscrow} like a station
 * upgrade's, so a recalled or broken craft returns them with the staff.
 */
public final class WorkingCraftRites {
	private static final int HINT_INTERVAL_TICKS = 40;
	private static final Map<UUID, Hint> LAST_HINT = new HashMap<>();

	private record Hint(BlockPos pos, long gameTime, boolean subject) {
	}

	private WorkingCraftRites() {
	}

	public static boolean isRite(ActiveCardinalRite rite) {
		return rite.isWorkingCraft();
	}

	/**
	 * Records the subject and escrows the itinerary's offerings. A false result
	 * refuses activation; anything already taken returns with the staff.
	 */
	public static boolean prepare(ServerLevel level, ActiveCardinalRite rite,
			WorkingCraftSubjectMatcher.SubjectMatch subject) {
		if (subject == null || !RiteOfferingEscrow.itineraryPresent(level, rite.getOfferingItinerary())) return false;
		CompoundTag state = rite.craft().tag();
		state.putString("Side", subject.side().getSerializedName());
		ListTag cells = new ListTag();
		for (int i = 0; i < subject.positions().size(); i++) {
			CompoundTag cell = new CompoundTag();
			cell.putLong("Pos", subject.positions().get(i).asLong());
			cell.put("State", NbtUtils.writeBlockState(subject.states().get(i)));
			cells.add(cell);
		}
		state.put("Subject", cells);
		RiteOfferingEscrow.open(state);
		if (!RiteOfferingEscrow.take(level, state, rite.getOfferingItinerary())) {
			RiteOfferingEscrow.returnEscrow(level, rite, state);
			return false;
		}
		return true;
	}

	public static List<BlockPos> subjectPositions(ActiveCardinalRite rite) {
		ListTag cells = rite.craft().tag().getList("Subject", Tag.TAG_COMPOUND);
		List<BlockPos> positions = new ArrayList<>(cells.size());
		for (int i = 0; i < cells.size(); i++) positions.add(BlockPos.of(cells.getCompound(i).getLong("Pos")));
		return positions;
	}

	/** The middle of the subject's blocks, where the daemon dives in. */
	public static net.minecraft.world.phys.Vec3 subjectHeart(ActiveCardinalRite rite) {
		List<BlockPos> positions = subjectPositions(rite);
		if (positions.isEmpty()) return net.minecraft.world.phys.Vec3.atCenterOf(rite.getCenterPos().above());
		double x = 0.0D, y = 0.0D, z = 0.0D;
		for (BlockPos pos : positions) {
			x += pos.getX() + 0.5D;
			y += pos.getY() + 0.5D;
			z += pos.getZ() + 0.5D;
		}
		return new net.minecraft.world.phys.Vec3(x / positions.size(), y / positions.size(), z / positions.size());
	}

	/** The subject's block states as captured when the rite began. */
	public static List<BlockState> subjectStates(Level level, ActiveCardinalRite rite) {
		ListTag cells = rite.craft().tag().getList("Subject", Tag.TAG_COMPOUND);
		List<BlockState> states = new ArrayList<>(cells.size());
		var blocks = level.holderLookup(Registries.BLOCK);
		for (int i = 0; i < cells.size(); i++) {
			states.add(NbtUtils.readBlockState(blocks, cells.getCompound(i).getCompound("State")));
		}
		return states;
	}

	/**
	 * Whether every subject block is still the block it was. Only the block is
	 * compared, so connecting blocks such as ash trails may change shape. An
	 * unloaded subject counts as intact; the rite waits for it.
	 */
	public static boolean subjectIntact(ServerLevel level, ActiveCardinalRite rite) {
		List<BlockPos> positions = subjectPositions(rite);
		List<BlockState> states = subjectStates(level, rite);
		if (positions.isEmpty()) return false;
		for (int i = 0; i < positions.size(); i++) {
			BlockPos pos = positions.get(i);
			if (!level.hasChunkAt(pos)) continue;
			if (!level.getBlockState(pos).is(states.get(i).getBlock())) return false;
		}
		return true;
	}

	/** Sound for blood rising up the subject, and the seal once it is full. */
	public static void onFed(ServerLevel level, ServerPlayer feeder, ActiveCardinalRite rite) {
		BlockPos center = rite.getCenterPos();
		if (rite.getPhase() == CardinalRitePhase.SUBJECT_FEEDING) {
			if (level.getGameTime() % 20 == 0) {
				CardinalRiteBeats.sound(level, center, SoundInit.RITE_SUBJECT_RISE, 0.7F, 0.8F + 0.4F * rite.subjectFill());
			}
			return;
		}
		CardinalRiteBeats.sound(level, center, SoundInit.RITE_SEAL, 1.0F, 1.0F);
		CardinalRiteBeats.send(level, rite, CardinalRiteEventKind.SEALED);
		ServerPlayer caster = level.getServer().getPlayerList().getPlayer(rite.getPlayerUUID());
		CardinalRiteRecipe recipe = CardinalRiteRecipe.getRiteByLocation(level, rite.getRecipeId());
		if (caster != null && recipe != null) HarbingerCardinalRiteEvents.enterFinale(level, caster, rite, recipe);
		feeder.displayClientMessage(Component.literal("The Working floor has drunk its fill. The offerings are drawn in.")
				.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), true);
	}

	/**
	 * Settles a finished craft once: consumes the medium and escrow, removes the
	 * subject into the floor and condenses the result on the Focus. A craft that
	 * already settled (after a restart) reports success without repeating it.
	 *
	 * @return false when the offerings, subject or medium no longer answer; nothing was taken
	 */
	public static boolean complete(ServerLevel level, ServerPlayer caster, ActiveCardinalRite rite,
			CardinalRiteRecipe recipe) {
		CompoundTag state = rite.craft().tag();
		if (RiteOfferingEscrow.isConsumed(state)) return true;
		// Offerings already sent home (or never taken) cannot pay for the result.
		if (!RiteOfferingEscrow.isEscrowed(state)) return false;
		if (settledByFocus(level, rite)) {
			// Rite data older than its chunk: the Focus already took this structure and made its result.
			RiteOfferingEscrow.markConsumed(state);
			CardinalRiteSavedData.get(level).setDirty();
			return true;
		}
		if (!subjectIntact(level, rite)) {
			caster.displayClientMessage(Component.literal("The rite falls silent: its structure was disturbed.")
					.withStyle(ChatFormatting.DARK_RED), false);
			return false;
		}
		BlockPos center = rite.getCenterPos();
		if (!(level.getBlockEntity(center) instanceof CardinalFocusBlockEntity focus)
				|| !CardinalRiteMediumRules.finish(focus, recipe.getMedium(), recipe.shouldConsumeMediumOnSuccess())) {
			caster.displayClientMessage(Component.literal(
					"The rite falls silent: its medium is no longer seated in the Cardinal Focus.")
					.withStyle(ChatFormatting.DARK_RED), false);
			return false;
		}
		// Witnessed in the chunk that loses the structure, so stale rite data cannot settle it again.
		focus.markCraftSettled(riteIdentity(rite));
		RiteOfferingEscrow.markConsumed(state);
		CardinalRiteSavedData.get(level).setDirty();

		var cell = HemoParticleData.bloodCell(new ParticleColor(150, 6, 16));
		for (BlockPos pos : subjectPositions(rite)) {
			if (level.getBlockState(pos).isAir()) continue;
			level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
			level.sendParticles(cell, pos.getX() + 0.5D, pos.getY() + 0.3D, pos.getZ() + 0.5D,
					6, 0.3D, 0.15D, 0.3D, 0.02D);
		}

		ItemStack result = recipe.getResult().copy();
		if (!result.isEmpty()) {
			ItemEntity drop = new ItemEntity(level, center.getX() + 0.5D, center.getY() + 1.5D,
					center.getZ() + 0.5D, result.copy());
			drop.setDeltaMovement(0.0D, 0.05D, 0.0D);
			if (!level.addFreshEntity(drop)) caster.drop(result.copy(), false);
			MachineAccessEvents.awardMachineCrafted(caster, result);
			FirstBloodcraftAssignment.recordStructure(caster, result);
			LivingStaffBondHelper.unlockFromBloodStructureCraft(caster, result);
		}
		CardinalRiteBeats.sound(level, center, SoundInit.RITE_SUBJECT_SINK, 1.0F, 0.9F);
		CardinalRiteBeats.sound(level, center, SoundInit.RITE_COMPLETE, 0.8F, 1.0F);
		caster.displayClientMessage(Component.literal("The Working floor takes the structure. ")
				.append(result.isEmpty() ? Component.literal("The rite is complete.") : result.getHoverName().copy()
						.append(Component.literal(" condenses on the Focus.")))
				.withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
		SkillPointGainEvents.onRiteCompleted(caster);
		LiberKnowledgeHelper.unlockForRite(caster, rite.getRecipeId().getPath());
		return true;
	}

	/**
	 * Returns the escrow of a craft that ends without completing. A craft its
	 * Focus already settled (rite data reloaded from before the settling) spent
	 * its offerings; only its staff comes home, through the rite's ending.
	 */
	public static void recover(ServerLevel level, ActiveCardinalRite rite) {
		if (!isRite(rite) || !RiteOfferingEscrow.isEscrowed(rite.craft().tag())) return;
		if (settledByFocus(level, rite)) {
			RiteOfferingEscrow.markConsumed(rite.craft().tag());
			CardinalRiteSavedData.get(level).setDirty();
			return;
		}
		RiteOfferingEscrow.returnEscrow(level, rite, rite.craft().tag());
	}

	private static UUID riteIdentity(ActiveCardinalRite rite) {
		return rite.craft().tag().hasUUID("RiteIdentity") ? rite.craft().tag().getUUID("RiteIdentity") : null;
	}

	private static boolean settledByFocus(ServerLevel level, ActiveCardinalRite rite) {
		return level.getBlockEntity(rite.getCenterPos()) instanceof CardinalFocusBlockEntity focus
				&& focus.settledCraft(riteIdentity(rite));
	}

	/**
	 * Projection into a Working craft's built structure outside a rite explains
	 * how to perform it and takes the projection, so the block does not also
	 * condense a Sanguine Formation.
	 */
	public static boolean hintAt(ServerPlayer player, ServerLevel level, BlockPos pos) {
		// A structure already on a Working floor's rite is not a frame to start; take the projection quietly.
		for (ActiveCardinalRite rite : CardinalRiteSavedData.get(level).getActiveRites().values()) {
			if (isRite(rite) && !rite.isEnded() && subjectPositions(rite).contains(pos)) {
				player.displayClientMessage(Component.literal("A Working floor's rite already holds this structure.")
						.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), true);
				return true;
			}
		}
		long gameTime = level.getGameTime();
		Hint last = LAST_HINT.get(player.getUUID());
		if (last != null && last.pos().equals(pos) && gameTime - last.gameTime() < HINT_INTERVAL_TICKS) {
			return last.subject();
		}
		BlockState hit = level.getBlockState(pos);
		CardinalRiteRecipe found = null;
		for (CardinalRiteRecipe recipe : CardinalRiteRecipe.getAllRecipes(level)) {
			if (!recipe.isWorkingCraft()
					|| !BloodCraftingPatternBlockRules.patternMayContainBlock(recipe.getSubject(), hit)) continue;
			if (BloodStructureCraftingHelper.findPatternAtHit(recipe.getSubject().getBlockPattern(), level, pos) != null) {
				found = recipe;
				break;
			}
		}
		LAST_HINT.put(player.getUUID(), new Hint(pos.immutable(), gameTime, found != null));
		if (found == null) return false;
		player.displayClientMessage(hint(found), true);
		return true;
	}

	static Component hint(CardinalRiteRecipe recipe) {
		ItemStack[] media = recipe.getMedium().getItems();
		String floor = recipe.getFloorId() == null ? "Working" : recipe.getFloorId().getPath();
		String tier = floor.substring(floor.lastIndexOf('_') + 1);
		return Component.literal("A frame for the ")
				.append(recipe.getResult().getHoverName().copy().withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" (" + RecipeDegreeGates.requirementLabel(recipe) + ")"))
				.append(Component.literal(". Raise it beside a Cardinal Focus on a " + tier + " Working floor, seat "))
				.append(media.length == 0 ? Component.literal("its medium") : media[0].getHoverName().copy())
				.append(Component.literal(" in the Focus, light its braziers and plant your Living Staff."))
				.withStyle(ChatFormatting.DARK_RED);
	}

	public static void clear() {
		LAST_HINT.clear();
	}
}
