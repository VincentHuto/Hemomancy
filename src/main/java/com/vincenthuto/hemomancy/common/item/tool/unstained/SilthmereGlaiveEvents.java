package com.vincenthuto.hemomancy.common.item.tool.unstained;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumPurityStage;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedProgressEvents;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

/**
 * Event handlers for the {@link SilthmereGlaiveItem}.
 * <p>
 * <ul>
 *   <li>Every 40 ticks: removes Glowing from any player who holds the glaive
 *       in their mainhand, reducing mob target-acquisition against them.</li>
 *   <li>On mob kill: if the killer holds the glaive and has ABSOLVED+ purity,
 *       grants +0.5 purity.</li>
 * </ul>
 */
@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.GAME)
public class SilthmereGlaiveEvents {

	/** How often (in ticks) the Glowing suppression pass runs. */
	private static final int GLOWING_CHECK_INTERVAL = 40;

	/** Purity rewarded on kill at ABSOLVED+ purity. */
	private static final float KILL_PURITY_REWARD = 0.5f;

	// ── Player tick: Glowing suppression ─────────────────────────────────────

	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (event.phase != TickEvent.Phase.END) return;
		Player player = event.player;
		if (player.level().isClientSide) return;
		if (player.level().getGameTime() % GLOWING_CHECK_INTERVAL != 0) return;

		ItemStack mainhand = player.getItemBySlot(EquipmentSlot.MAINHAND);
		if (!(mainhand.getItem() instanceof SilthmereGlaiveItem)) return;

		if (player.hasEffect(MobEffects.GLOWING)) {
			player.removeEffect(MobEffects.GLOWING);
		}
	}

	// ── Living death: purity reward on kill ──────────────────────────────────

	@SubscribeEvent
	public static void onLivingDeath(LivingDeathEvent event) {
		if (!(event.getSource().getEntity() instanceof ServerPlayer killer)) return;
		LivingEntity victim = event.getEntity();
		// Don't reward player kills
		if (victim instanceof Player) return;

		ItemStack mainhand = killer.getItemBySlot(EquipmentSlot.MAINHAND);
		if (!(mainhand.getItem() instanceof SilthmereGlaiveItem)) return;

		HemoCapabilityAccess.getUnstainedProgress(killer).ifPresent(progress -> {
			if (!progress.hasBegunPurification() || progress.isPurified()) return;
			if (EnumPurityStage.byPurity(progress.getPurity()).getLevel()
					< EnumPurityStage.ABSOLVED.getLevel()) return;

			progress.addPurity(KILL_PURITY_REWARD);
			UnstainedProgressEvents.syncProgress(killer, progress);
			killer.displayClientMessage(
					Component.literal("Silthmere's memory stirs — purity grows.")
							.withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC),
					true);
		});
	}
}
