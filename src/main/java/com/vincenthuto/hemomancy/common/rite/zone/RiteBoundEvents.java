package com.vincenthuto.hemomancy.common.rite.zone;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.rite.CardinalRitePurpose;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/**
 * Keeps rite-bound things to their rite spaces: a player cannot set a rite-bound block down outside one, nothing
 * rite-bound answers a right-click outside one, and a rite-bound creature that strays out unravels.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class RiteBoundEvents {
	private static final String OUTSIDE_TAG = "hemomancy:rite_bound_outside";
	private static final int ENTITY_CHECK_INTERVAL = 20;
	private static final ParticleColor UNRAVEL_COLOR = new ParticleColor(150, 18, 30);

	private RiteBoundEvents() {}

	/** Whether placing {@code state} at {@code pos} by {@code placer} must be refused; the GameTests call this too. */
	public static boolean refusesPlacement(Level level, BlockPos pos, BlockState state, Entity placer) {
		RiteBoundRules.Requirement requirement = RiteBound.of(state);
		if (!requirement.bound()) return false;
		return RiteBoundRules.refusesPlacement(requirement, placer instanceof Player, purposeAt(level, pos));
	}

	@SubscribeEvent
	public static void onPlace(BlockEvent.EntityPlaceEvent event) {
		if (!(event.getLevel() instanceof Level level)) return;
		BlockState placed = event.getPlacedBlock();
		if (!refusesPlacement(level, event.getPos(), placed, event.getEntity())) return;
		event.setCanceled(true);
		if (event.getEntity() instanceof Player player) refuse(player, RiteBound.of(placed));
	}

	@SubscribeEvent
	public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		Level level = event.getLevel();
		RiteBoundRules.Requirement block = RiteBound.of(level.getBlockState(event.getPos()));
		if (!RiteBoundRules.admits(block, purposeAt(level, event.getPos()))) {
			event.setUseBlock(TriState.FALSE);
			refuse(event.getEntity(), block);
		}
		RiteBoundRules.Requirement item = RiteBound.ofUse(event.getItemStack());
		if (!RiteBoundRules.admits(item, purposeAt(level, event.getEntity().blockPosition()))) {
			event.setUseItem(TriState.FALSE);
			refuse(event.getEntity(), item);
		}
	}

	@SubscribeEvent
	public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
		if (refusesUse(event.getEntity(), event.getItemStack())) {
			event.setCanceled(true);
			event.setCancellationResult(InteractionResult.FAIL);
		}
	}

	@SubscribeEvent
	public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
		if (refusesUse(event.getEntity(), event.getItemStack())) {
			event.setCanceled(true);
			event.setCancellationResult(InteractionResult.FAIL);
		}
	}

	@SubscribeEvent
	public static void onEntityTick(EntityTickEvent.Post event) {
		Entity entity = event.getEntity();
		if (entity instanceof Player || entity.tickCount % ENTITY_CHECK_INTERVAL != 0
				|| !(entity.level() instanceof ServerLevel level)) return;
		RiteBoundRules.Requirement requirement = RiteBound.of(entity.getType());
		if (!requirement.bound()) return;
		var data = entity.getPersistentData();
		if (RiteBoundRules.admits(requirement, purposeAt(level, entity.blockPosition()))) {
			data.remove(OUTSIDE_TAG);
			return;
		}
		int outside = data.getInt(OUTSIDE_TAG) + ENTITY_CHECK_INTERVAL;
		data.putInt(OUTSIDE_TAG, outside);
		if (!RiteBoundRules.unravels(outside)) return;
		level.sendParticles(HemoParticleData.bloodCell(UNRAVEL_COLOR), entity.getX(), entity.getY(0.5D), entity.getZ(),
				18, entity.getBbWidth() * 0.4D, entity.getBbHeight() * 0.35D, entity.getBbWidth() * 0.4D, 0.02D);
		entity.discard();
	}

	private static boolean refusesUse(Player player, ItemStack stack) {
		RiteBoundRules.Requirement requirement = RiteBound.ofUse(stack);
		if (RiteBoundRules.admits(requirement, purposeAt(player.level(), player.blockPosition()))) return false;
		refuse(player, requirement);
		return true;
	}

	private static CardinalRitePurpose purposeAt(Level level, BlockPos pos) {
		return RiteSpaces.purposeAt(level, pos).orElse(null);
	}

	private static void refuse(Player player, RiteBoundRules.Requirement requirement) {
		if (player.level().isClientSide()) return;
		player.displayClientMessage(Component.translatable("hemomancy.rite_bound.refused",
				RiteBoundRules.describe(requirement)).withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), true);
	}
}
