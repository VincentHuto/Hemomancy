package com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.spore.SporeLoad;
import com.vincenthuto.hemomancy.common.capability.player.shared.cooldown.PlayerCooldowns;
import com.vincenthuto.hemomancy.common.event.ApotheosBodyEvents;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.world.FungalGardensPlaces;
import com.vincenthuto.hemomancy.common.world.HeartNodeSavedData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

/** Molting at the heart-node: reads the player's grown tendency and sets the new shape ({@link MoltingRules}). */
public final class MoltingService {
	private static final ParticleColor SHED_SKIN = new ParticleColor(226, 206, 200);

	private MoltingService() {
	}

	/** The grown alignment per tendency, in {@link EnumBloodTendency#values()} order. */
	public static float[] baseAlignment(ServerPlayer player) {
		IBloodTendency tendency = HemoCapabilityAccess.getBloodTendency(player);
		EnumBloodTendency[] tendencies = EnumBloodTendency.values();
		float[] base = new float[tendencies.length];
		for (int i = 0; i < tendencies.length; i++) base[i] = tendency.getBaseAlignment(tendencies[i]);
		return base;
	}

	public static long softUntil(ServerPlayer player) {
		return PlayerCooldowns.of(player).until(PlayerCooldowns.MOLTING);
	}

	/**
	 * Molts {@code player} into {@code allocation} at their own heart-node {@code nodePos}. Every check is made again
	 * here, so the screen's own state is never trusted. Returns what refused it, or {@link MoltingRules.Refusal#NONE}.
	 */
	public static MoltingRules.Refusal molt(ServerPlayer player, BlockPos nodePos, int[] allocation) {
		MoltingRules.Refusal refusal = check(player, nodePos, allocation);
		player.displayClientMessage(Component.translatable(MoltingRules.messageKey(refusal))
				.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), true);
		if (refusal != MoltingRules.Refusal.NONE) return refusal;

		IBloodTendency tendency = HemoCapabilityAccess.getBloodTendency(player);
		EnumBloodTendency[] tendencies = EnumBloodTendency.values();
		for (int i = 0; i < tendencies.length; i++) tendency.setTendencyAlignment(tendencies[i], allocation[i]);
		BloodTendencyEvents.syncTendency(player, tendency);
		HemoCapabilityAccess.getSporeLoad(player).spend(MoltingRules.SPORE_COST);
		long now = player.level().getGameTime();
		PlayerCooldowns.of(player).setUntil(PlayerCooldowns.MOLTING, now + MoltingRules.COOLDOWN_TICKS);

		player.serverLevel().sendParticles(HemoParticleData.sporiticSpore(SHED_SKIN), player.getX(),
				player.getY() + 1.0D, player.getZ(), 40, 0.45D, 0.9D, 0.45D, 0.02D);
		player.serverLevel().sendParticles(HemoParticleData.glow(tendency.getAvgBloodColor()), player.getX(),
				player.getY() + 1.0D, player.getZ(), 24, 0.35D, 0.7D, 0.35D, 0.04D);
		player.level().playSound(null, player.blockPosition(), SoundEvents.ARMADILLO_SCUTE_DROP, SoundSource.PLAYERS,
				1.0F, 0.6F);
		player.level().playSound(null, player.blockPosition(), SoundEvents.FUNGUS_PLACE, SoundSource.PLAYERS, 0.8F,
				0.5F);
		return refusal;
	}

	private static MoltingRules.Refusal check(ServerPlayer player, BlockPos nodePos, int[] allocation) {
		boolean apotheos = ApotheosBodyEvents.isApotheos(player);
		if (!apotheos) return MoltingRules.Refusal.NOT_APOTHEOS;
		BlockPos own = HeartNodeSavedData.get(player.serverLevel()).nodeOf(player.getUUID());
		boolean atOwnNode = own != null && own.equals(nodePos);
		SporeLoad spores = HemoCapabilityAccess.getSporeLoad(player);
		int pool = MoltingRules.pool(baseAlignment(player));
		MoltingRules.Refusal refusal = MoltingRules.refusal(true,
				atOwnNode && FungalGardensPlaces.isInGardens(player.level(), player.blockPosition()), spores.load(),
				player.level().getGameTime(), softUntil(player), pool);
		if (refusal != MoltingRules.Refusal.NONE) return refusal;
		return MoltingRules.placed(allocation, pool) ? MoltingRules.Refusal.NONE : MoltingRules.Refusal.UNPLACED;
	}
}
