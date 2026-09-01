package com.vincenthuto.hemomancy.common.manipulation.ferric;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.PowerGuardrailState;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.manipulation.*;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class IronheartedManip extends BloodManipulation {
	public IronheartedManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	protected boolean canPerformAction(Player player, float chargeTicks) {
		if (chargeTicks < BodyIdiomRules.IRON_HEART_CHARGE_TICKS) {
			player.displayClientMessage(Component.translatable("message.hemomancy.ironhearted.charge")
					.withStyle(ChatFormatting.GRAY), true);
			return false;
		}
		float maxIronHeartHealth = BodyIdiomRules.maxIronHeartHealth(player);
		if (HemoCapabilityAccess.getPowerGuardrails(player).getIronHeartHealth()
				>= maxIronHeartHealth) {
			player.displayClientMessage(Component.translatable("message.hemomancy.ironhearted.full")
					.withStyle(ChatFormatting.DARK_GRAY), true);
			return false;
		}
		return true;
	}

	@Override
	public int getRequiredChargeTicks() {
		return BodyIdiomRules.IRON_HEART_CHARGE_TICKS;
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position,
			float chargeTicks) {
		if (!(player instanceof ServerPlayer serverPlayer) || !(world instanceof ServerLevel level)) return;
		PowerGuardrailState state = HemoCapabilityAccess.getPowerGuardrails(player);
		float strength = ManipulationCastingRules.chargeFraction(chargeTicks, getRequiredChargeTicks());
		state.setIronHeartHealth(net.minecraft.util.Mth.clamp(state.getIronHeartHealth()
				+ BodyIdiomRules.IRON_HEART_HEALTH_PER_CAST * strength,
				0.0F, BodyIdiomRules.maxIronHeartHealth(player)));
		state.setIronHeartExpiryTick(world.getGameTime() + BodyIdiomRules.IRON_HEART_DURATION_TICKS);
		BodyIdiomEvents.sync(serverPlayer);
		level.sendParticles(ParticleTypes.CRIMSON_SPORE, player.getX(), player.getY() + 1.0D, player.getZ(),
				22, 0.38D, 0.55D, 0.38D, 0.02D);
		level.playSound(null, player.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.55F, 1.35F);
	}
}
