package com.vincenthuto.hemomancy.common.capability.player.harbinger.spore;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.shared.cooldown.PlayerCooldowns;
import com.vincenthuto.hemomancy.common.event.ApotheosBodyEvents;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/** Burning spore load: an Apotheos's emergency heal. */
public final class SporeLoadService {
	private SporeLoadService() {}

	public static void burn(ServerPlayer player) {
		if (!player.isAlive() || !ApotheosBodyEvents.isApotheos(player)) return;
		SporeLoad spores = HemoCapabilityAccess.getSporeLoad(player);
		long now = player.level().getGameTime();
		PlayerCooldowns cooldowns = PlayerCooldowns.of(player);
		SporeLoadRules.BurnResult result = SporeLoadRules.burn(spores.load(), player.getHealth(),
				player.getMaxHealth(), cooldowns.until(PlayerCooldowns.SPORE_BURN) <= now);
		if (result != SporeLoadRules.BurnResult.OK) {
			String key = switch (result) {
				case WHOLE -> "message.hemomancy.spore_load.whole";
				case COOLING -> "message.hemomancy.spore_load.cooling";
				default -> "message.hemomancy.spore_load.too_few";
			};
			player.displayClientMessage(Component.translatable(key)
					.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), true);
			return;
		}
		spores.spend(SporeLoadRules.BURN_COST);
		player.heal(SporeLoadRules.BURN_HEAL);
		cooldowns.setUntil(PlayerCooldowns.SPORE_BURN, now + SporeLoadRules.BURN_COOLDOWN_TICKS);
		((ServerLevel) player.level()).sendParticles(HemoParticleData.glow(new ParticleColor(196, 120, 140)),
				player.getX(), player.getY() + 1.0, player.getZ(), 24, 0.4, 0.6, 0.4, 0.03);
	}
}
