package com.vincenthuto.hemomancy.common.manipulation.flammeus;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.event.BloodStructureFeedRules;
import com.vincenthuto.hemomancy.common.init.RecipeInit;
import com.vincenthuto.hemomancy.common.manipulation.BloodFlowVisuals;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingPulse;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbSculpting;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTarget;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTargetKind;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketBloodStructureFeed;
import com.vincenthuto.hemomancy.common.recipe.BloodInfusionRecipe;
import com.vincenthuto.hemomancy.common.util.PlayerBlockEdits;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Pyretic Forge as a Sculpting of Projection. Held on items lying on the ground it smelts them where they lie, a few
 * each pulse, by the vanilla smelting recipes (no recipe authored twice), with the furnace's experience. Held on a
 * block that a Pyretic Forge infusion recipe names ({@code "sculpting": "hemomancy:pyretic_forge"}) it heats the block
 * until the recipe's blood is fed, then converts it in place ({@code "in_place": true}) or lifts it out as an item.
 * At a lit Iron Brazier the offering is worked by the brazier's transmutation rite under held Absorption, not here.
 *
 * <p>Block progress is a session map keyed by player UUID, cleared through {@code TransientServerStateEvents}.
 */
public final class PyreticForgeSculpting implements VerbSculpting {
	public static final ResourceLocation ID = Hemomancy.rloc("pyretic_forge");
	private static final double SYNC_RANGE = 64.0D;
	/** The Forge cue draws its molten heart this far above the point it is sent from, at full size. */
	private static final double FORGE_HEART = 0.95D;
	/**
	 * The size the Forge cue is sent at over a heap of items: a small bed of coals sunk into the ground under them, so
	 * the ore and what it becomes stay in view.
	 */
	public static final double ITEM_FORGE_SCALE = 0.45D;
	/** How far below the items the bed's heart sits, so only its glowing top and its flames show round them. */
	public static final double ITEM_FORGE_SINK = 0.14D;
	/** The size the Forge cue is sent at when a heated block converts. */
	public static final double BLOCK_FORGE_SCALE = 0.8D;
	private static final Map<UUID, Heating> HEATING = new HashMap<>();

	/** Blood fed into one block so far. */
	private record Heating(BlockPos pos, ResourceLocation recipe, double fed) {
	}

	@Override
	public Set<VerbTargetKind> wants() {
		return Set.of(VerbTargetKind.ITEMS, VerbTargetKind.BLOCK);
	}

	@Override
	public boolean claims(Player player, VerbTarget target) {
		Level level = player.level();
		if (target instanceof VerbTarget.Items items) {
			return items.items().stream().anyMatch(item -> smelting(level, item.getItem()).isPresent());
		}
		return target instanceof VerbTarget.Block block && blockRecipe(level, block.pos()).isPresent();
	}

	@Override
	public double bloodPerPulse() {
		return PyreticForgeRules.BLOOD_PER_PULSE;
	}

	/**
	 * Items are always admitted. The conversion breaks the old block, so a player who may not break it is refused before
	 * any blood is paid, and again on every pulse, so a heating refused mid-channel stops unpaid (CN-13). The final
	 * {@link #convert} can still be cancelled by another mod's event.
	 */
	@Override
	public boolean admits(ServerPlayer player, VerbTarget target) {
		if (target instanceof VerbTarget.Items) return true;
		if (!(target instanceof VerbTarget.Block block)) return false;
		BlockPos pos = block.pos();
		if (ManipulationCombatHelper.mayAlter(player, pos) && PlayerBlockEdits.mayBreak(player, pos)) return true;
		player.displayClientMessage(Component.translatable("message.hemomancy.player_block_edits.refused")
				.withStyle(ChatFormatting.DARK_RED), true);
		return false;
	}

	@Override
	public void pulse(ServerPlayer player, VerbTarget target, SculptingPulse pulse) {
		if (!(player.level() instanceof ServerLevel level)) return;
		if (target instanceof VerbTarget.Items items) {
			smeltItems(level, player, items, pulse);
		} else if (target instanceof VerbTarget.Block block) {
			heatBlock(level, player, block, pulse);
		}
		heatStream(player, target.point());
	}

	/**
	 * The Flammeus outpouring column round the caster would stand between the eye and the ore it works, so this Sculpting
	 * plays its own look instead: the heat stream and the small forge bed under the work.
	 */
	@Override
	public boolean presentsCast() {
		return false;
	}

	@Override
	public void end(ServerPlayer player, boolean released) {
		Heating heating = HEATING.remove(player.getUUID());
		if (heating != null && player.level() instanceof ServerLevel level) clearOverlay(level, heating.pos());
	}

	private static void smeltItems(ServerLevel level, ServerPlayer player, VerbTarget.Items items, SculptingPulse pulse) {
		int budget = PyreticForgeRules.itemsPerPulse(pulse.workScale(), SkillPointHelper.getCrimsonMasteryMultiplier(player));
		float experience = 0.0F;
		int smelted = 0;
		for (ItemEntity item : items.items()) {
			if (budget <= 0) break;
			if (!item.isAlive()) continue;
			ItemStack stack = item.getItem();
			Optional<RecipeHolder<SmeltingRecipe>> recipe = smelting(level, stack);
			if (recipe.isEmpty()) continue;
			ItemStack result = recipe.get().value().getResultItem(level.registryAccess());
			int count = Math.min(budget, stack.getCount());
			budget -= count;
			smelted += count;
			experience += recipe.get().value().getExperience() * count;
			ItemStack remainder = stack.copy();
			remainder.shrink(count);
			if (remainder.isEmpty()) item.discard();
			else item.setItem(remainder);
			drop(level, item.position(), result.copyWithCount(result.getCount() * count));
		}
		if (smelted == 0) return;
		awardExperience(level, items.focus(), experience);
		level.playSound(null, BlockPos.containing(items.focus()), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.45F, 1.25F);
		// A small molten bed sunk under the heap, its flames licking up round the items it works and its embers thrown
		// off the rim: the ore and the ingots stay in view.
		ManipulationVisuals.burst(level, ManipulationVisuals.Form.FORGE,
				items.focus().add(0.0D, -ITEM_FORGE_SINK - FORGE_HEART * ITEM_FORGE_SCALE, 0.0D), items.focus(),
				ITEM_FORGE_SCALE, 14);
	}

	/**
	 * A thin heat stream from the hand into the work each pulse, beside the hand's own blood stream: the outpouring
	 * column is not played for this Sculpting ({@link #presentsCast}), so this is what reaches out.
	 */
	private static void heatStream(ServerPlayer player, Vec3 work) {
		BloodFlowVisuals.toward(player, work, player.getId() * 7919L + player.level().getGameTime(),
				com.vincenthuto.hemomancy.common.network.particle.BloodFlowPacket.Style.LIVING);
	}

	private static void heatBlock(ServerLevel level, ServerPlayer player, VerbTarget.Block block, SculptingPulse pulse) {
		BlockPos pos = block.pos();
		Optional<RecipeHolder<BloodInfusionRecipe>> holder = blockRecipe(level, pos);
		if (holder.isEmpty()) return;
		BloodInfusionRecipe recipe = holder.get().value();
		Heating heating = HEATING.get(player.getUUID());
		if (heating == null || !heating.pos().equals(pos) || !heating.recipe().equals(holder.get().id())) {
			if (heating != null) clearOverlay(level, heating.pos());
			heating = new Heating(pos.immutable(), holder.get().id(), 0.0D);
		}
		double fed = heating.fed() + pulse.paid() * Math.max(0.0D, pulse.workScale());
		HEATING.put(player.getUUID(), new Heating(heating.pos(), heating.recipe(), fed));
		level.playSound(null, pos, SoundEvents.FIRE_AMBIENT, SoundSource.BLOCKS, 0.5F, 1.4F);
		if (fed < recipe.bloodCost()) {
			// The infusion veins show the heat; a full feed is never sent, since the client plays infusion's
			// lift-and-melt collapse at full progress and this block changes where it stands.
			float progress = (float) Math.min(0.95D, fed / recipe.bloodCost());
			PacketHandler.sendNear(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, SYNC_RANGE,
					PacketBloodStructureFeed.infusion(pos, block.point(), progress, BloodStructureFeedRules.PROGRESS_TIMEOUT_TICKS, false));
			return;
		}
		HEATING.remove(player.getUUID());
		clearOverlay(level, pos);
		convert(level, player, pos, recipe);
	}

	/** The heated block becomes the recipe's result: in place, or lifted out and dropped as an item. */
	static boolean convert(ServerLevel level, ServerPlayer player, BlockPos pos, BloodInfusionRecipe recipe) {
		BlockState result = recipe.result().defaultBlockState();
		boolean done;
		if (recipe.inPlace()) {
			done = PlayerBlockEdits.replace(player, pos, result);
		} else {
			done = PlayerBlockEdits.lift(player, pos) != null;
			if (done) drop(level, Vec3.atCenterOf(pos), recipe.resultStack());
		}
		if (!done) {
			player.displayClientMessage(Component.translatable("message.hemomancy.player_block_edits.refused")
					.withStyle(ChatFormatting.DARK_RED), true);
			return false;
		}
		level.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 0.7F, 0.85F);
		// The molten heart rests on the fired block's top with its flames round it.
		ManipulationVisuals.burst(level, ManipulationVisuals.Form.FORGE,
				Vec3.atBottomCenterOf(pos).add(0.0D, 1.05D - FORGE_HEART * BLOCK_FORGE_SCALE, 0.0D), Vec3.atCenterOf(pos),
				BLOCK_FORGE_SCALE, 20);
		return true;
	}

	/** The vanilla smelting recipe for {@code stack}, if it makes something. Safe on both sides. */
	public static Optional<RecipeHolder<SmeltingRecipe>> smelting(Level level, ItemStack stack) {
		if (stack.isEmpty()) return Optional.empty();
		return level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(stack), level)
				.filter(recipe -> !recipe.value().getResultItem(level.registryAccess()).isEmpty());
	}

	/** The Pyretic Forge infusion recipe for the block at {@code pos}, if one names it. Safe on both sides. */
	public static Optional<RecipeHolder<BloodInfusionRecipe>> blockRecipe(Level level, BlockPos pos) {
		if (!level.isLoaded(pos)) return Optional.empty();
		BlockState state = level.getBlockState(pos);
		boolean hasBlockEntity = level.getBlockEntity(pos) != null;
		return level.getRecipeManager().getAllRecipesFor(RecipeInit.blood_infusion_type.get()).stream()
				.filter(holder -> holder.value().isSculptedBy(ID) && holder.value().matches(state, hasBlockEntity))
				.min(Comparator.comparing(holder -> holder.id().toString()));
	}

	private static void drop(ServerLevel level, Vec3 at, ItemStack stack) {
		while (!stack.isEmpty()) {
			ItemStack part = stack.split(stack.getMaxStackSize());
			ItemEntity entity = new ItemEntity(level, at.x, at.y, at.z, part);
			entity.setDeltaMovement(Vec3.ZERO);
			level.addFreshEntity(entity);
		}
	}

	private static void awardExperience(ServerLevel level, Vec3 at, float experience) {
		int whole = Mth.floor(experience);
		if (level.random.nextFloat() < experience - whole) whole++;
		if (whole > 0) ExperienceOrb.award(level, at, whole);
	}

	private static void clearOverlay(ServerLevel level, BlockPos pos) {
		PacketHandler.sendNear(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, SYNC_RANGE,
				PacketBloodStructureFeed.infusion(pos, Vec3.atCenterOf(pos), 0.0F, BloodStructureFeedRules.PROGRESS_TIMEOUT_TICKS, true));
	}

	/** The blood fed into the block the player is heating, or 0; for tests. */
	public static double fedInto(Player player, BlockPos pos) {
		Heating heating = HEATING.get(player.getUUID());
		return heating != null && heating.pos().equals(pos) ? heating.fed() : 0.0D;
	}

	public static void clearSessionState() {
		HEATING.clear();
	}

	public static void logout(Player player) {
		HEATING.remove(player.getUUID());
	}
}
