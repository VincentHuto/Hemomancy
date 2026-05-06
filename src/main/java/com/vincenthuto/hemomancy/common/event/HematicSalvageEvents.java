package com.vincenthuto.hemomancy.common.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.config.HemoServerConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class HematicSalvageEvents {
	private static final ResourceLocation WOUNDED_IRON_ADVANCEMENT = Hemomancy.rloc("hemomancy/wounded_iron");
	private static final Map<UUID, Long> LAST_DROP_TICK = new HashMap<>();

	private HematicSalvageEvents() {
	}

	@SubscribeEvent
	public static void onPlayerDamaged(LivingDamageEvent.Post event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		if (player.isCreative() || player.isSpectator()) return;
		if (!HemoServerConfig.HEMATIC_SALVAGE_ENABLED.get()) return;

		int damagedIronPieces = countDamagedVanillaIronArmor(player);
		long gameTime = player.level().getGameTime();
		long lastDropTick = LAST_DROP_TICK.getOrDefault(player.getUUID(), -1L);
		int degree = HemoCapabilityAccess.getPlayerDegreeNumber(player);

		if (!HematicSalvageRules.isEligible(
				event.getNewDamage(),
				HemoServerConfig.HEMATIC_SALVAGE_MIN_DAMAGE.get(),
				degree,
				HemoServerConfig.HEMATIC_SALVAGE_MAX_DEGREE.get(),
				gameTime,
				lastDropTick,
				damagedIronPieces,
				HemoServerConfig.HEMATIC_SALVAGE_COOLDOWN_TICKS.get())) {
			return;
		}

		double chance = HematicSalvageRules.calculateChance(
				damagedIronPieces,
				HemoServerConfig.HEMATIC_SALVAGE_BASE_CHANCE.get(),
				HemoServerConfig.HEMATIC_SALVAGE_CHANCE_PER_PIECE.get(),
				HemoServerConfig.HEMATIC_SALVAGE_MAX_CHANCE.get());
		if (player.level().getRandom().nextDouble() >= chance) return;

		LAST_DROP_TICK.put(player.getUUID(), gameTime);
		dropScrap(player);
		boolean firstDiscovery = !hasAdvancement(player, WOUNDED_IRON_ADVANCEMENT);
		HarbingerAdvancementGranter.grantIfNotDone(player, WOUNDED_IRON_ADVANCEMENT);
		if (firstDiscovery) {
			player.displayClientMessage(
					Component.translatable("hemomancy.hematic_salvage.first_drop")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);
		}
	}

	private static int countDamagedVanillaIronArmor(ServerPlayer player) {
		int count = 0;
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			if (slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) continue;
			ItemStack stack = player.getItemBySlot(slot);
			if (isDamagedVanillaIronArmor(stack)) {
				count++;
			}
		}
		return count;
	}

	private static boolean isDamagedVanillaIronArmor(ItemStack stack) {
		if (stack.isEmpty() || !stack.isDamaged()) return false;
		return stack.is(Items.IRON_HELMET)
				|| stack.is(Items.IRON_CHESTPLATE)
				|| stack.is(Items.IRON_LEGGINGS)
				|| stack.is(Items.IRON_BOOTS);
	}

	private static void dropScrap(ServerPlayer player) {
		ItemEntity drop = new ItemEntity(player.level(), player.getX(), player.getY() + 0.4, player.getZ(),
				new ItemStack(ItemInit.hematic_iron_scrap.get()));
		drop.setDefaultPickUpDelay();
		player.level().addFreshEntity(drop);

		if (player.level() instanceof ServerLevel serverLevel) {
			serverLevel.sendParticles(ParticleTypes.DAMAGE_INDICATOR,
					player.getX(), player.getY() + 1.0, player.getZ(),
					4, 0.25, 0.35, 0.25, 0.02);
			serverLevel.playSound(null, player.blockPosition(), SoundEvents.IRON_GOLEM_DAMAGE,
					SoundSource.PLAYERS, 0.35f, 1.45f);
		}
	}

	private static boolean hasAdvancement(ServerPlayer player, ResourceLocation id) {
		AdvancementHolder advancement = player.server.getAdvancements().get(id);
		return advancement != null && player.getAdvancements().getOrStartProgress(advancement).isDone();
	}
}
