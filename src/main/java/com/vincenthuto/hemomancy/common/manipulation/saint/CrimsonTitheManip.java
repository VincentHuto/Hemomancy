package com.vincenthuto.hemomancy.common.manipulation.saint;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BorrowedBloodReserve;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodVolumeServerPacket;
import com.vincenthuto.hemomancy.config.HemoServerConfig;
import com.vincenthuto.hutoslib.client.particle.data.ColorParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Crimson Tithe — Canon Memory of Saint Hemorath.
 * Doctrine: Judgment / Silence
 *
 * Issues blood through the shared borrowed reserve. Unspent blood is reclaimed
 * at the deadline; spent blood is collected at twice its value.
 *
 * Imprinted, not learned. The player uses it uncomfortably.
 */
public class CrimsonTitheManip extends BloodManipulation {

	private static final String TITHE_EXPIRY_KEY = "hemomancy:crimson_tithe_expiry";
	private static final String TITHE_STORED_KEY = "hemomancy:crimson_tithe_stored";
	private static final double BLOOD_STORE_AMOUNT = 500.0;
	private static final int REPAYMENT_WINDOW_TICKS = 600; // 30 seconds

	public CrimsonTitheManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	protected boolean canPerformAction(Player player, float chargeTicks) {
		if (!super.canPerformAction(player, chargeTicks)) return false;
		if (player instanceof ServerPlayer serverPlayer) tickDebt(serverPlayer);
		if (player.getPersistentData().contains(TITHE_EXPIRY_KEY)) {
			player.displayClientMessage(Component.literal("The current tithe has not come due.")
					.withStyle(ChatFormatting.DARK_RED), true);
			return false;
		}
		if (!HemoServerConfig.BORROWED_BLOOD_ENABLED.get()
				|| BorrowedBloodReserve.get(player) >= HemoServerConfig.BORROWED_BLOOD_CAP.get()) {
			player.displayClientMessage(Component.literal("There is no room for borrowed blood.")
					.withStyle(ChatFormatting.DARK_RED), true);
			return false;
		}
		return true;
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		double borrowed = BorrowedBloodReserve.deposit(player, BLOOD_STORE_AMOUNT);
		if (borrowed <= 0.0D) return;

		player.getPersistentData().putLong(TITHE_EXPIRY_KEY, world.getGameTime() + REPAYMENT_WINDOW_TICKS);
		player.getPersistentData().putDouble(TITHE_STORED_KEY, borrowed);

		player.displayClientMessage(
				Component.literal((int) borrowed + " borrowed blood granted. The tithe comes due in 30 seconds.")
						.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
				true);

		world.playSound(null, player.blockPosition(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 0.8f, 1.2f);

		if (world instanceof ServerLevel sLevel) {
			sLevel.sendParticles(
					new ColorParticleData(new ParticleColor(180, 0, 0)),
					player.getX(), player.getY() + 1.0, player.getZ(),
					20, 0.3, 0.5, 0.3, 0.02);
		}
	}

	public static void tickDebt(ServerPlayer player) {
		long expiry = player.getPersistentData().getLong(TITHE_EXPIRY_KEY);
		if (expiry <= 0L || player.level().getGameTime() < expiry) return;

		double issued = Math.max(0.0D, player.getPersistentData().getDouble(TITHE_STORED_KEY));
		player.getPersistentData().remove(TITHE_EXPIRY_KEY);
		player.getPersistentData().remove(TITHE_STORED_KEY);
		double returned = BorrowedBloodReserve.drainToCover(player, issued);
		double spent = Math.max(0.0D, issued - returned);
		double due = spent * 2.0D;
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player).orElse(null);
		if (volume != null && due > 0.0D) {
			volume.drain(Math.min(due, Math.max(0.0D, volume.getBloodVolume())));
			PacketHandler.sendToPlayer(player, new BloodVolumeServerPacket(volume));
		}

		if (spent > 0.0D) {
			player.hurt(player.damageSources().magic(), 6.0F);
			player.displayClientMessage(Component.literal("The tithe was spent. Hemorath collects double.")
					.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), false);
			player.level().playSound(null, player.blockPosition(), SoundEvents.WARDEN_ROAR,
					SoundSource.PLAYERS, 0.5F, 0.5F);
		} else {
			player.displayClientMessage(Component.literal("The unused tithe returns to Hemorath.")
					.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), true);
		}
	}
}
