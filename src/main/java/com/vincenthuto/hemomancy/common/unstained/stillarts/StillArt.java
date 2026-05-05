package com.vincenthuto.hemomancy.common.unstained.stillarts;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumClarityStage;
import com.vincenthuto.hemomancy.common.capability.player.unstained.IUnstainedProgress;
import com.vincenthuto.hemomancy.common.init.StillArtInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.manips.ManipCooldownPacket;
import com.vincenthuto.hutoslib.client.HLTextUtils;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class StillArt {
	public static final StillArt BLANK = new StillArt("no_selected", EnumClarityStage.AWAKENED, 0, StillArtAction.NOOP);

	private static final double TICKS_PER_SECOND = 20.0;
	private static final Map<UUID, Long> UNIVERSAL_COOLDOWN_MAP = new ConcurrentHashMap<>();

	private final String name;
	private final EnumClarityStage requiredStage;
	private final int cooldownTicks;
	private final StillArtAction action;

	public StillArt(String name, EnumClarityStage requiredStage, int cooldownTicks, StillArtAction action) {
		this.name = name;
		this.requiredStage = requiredStage;
		this.cooldownTicks = cooldownTicks;
		this.action = action;
	}

	public String getName() {
		return name;
	}

	public String getProperName() {
		return HLTextUtils.convertInitToLang(name);
	}

	public EnumClarityStage getRequiredStage() {
		return requiredStage;
	}

	public int getCooldownTicks() {
		return cooldownTicks;
	}

	public static StillArt byName(String name) {
		StillArt art = StillArtInit.getByName(name);
		return art != null ? art : BLANK;
	}

	public boolean isUnlockedFor(Player player) {
		IUnstainedProgress progress = HemoCapabilityAccess.getUnstainedProgress(player).orElse(null);
		if (progress == null || !progress.hasClarityUnlocked()) {
			return false;
		}
		EnumClarityStage current = EnumClarityStage.byClarity(progress.getClarity());
		return current.getLevel() >= requiredStage.getLevel();
	}

	public boolean isOnCooldown(Player player) {
		Long expiryTick = UNIVERSAL_COOLDOWN_MAP.get(player.getUUID());
		if (expiryTick == null) {
			return false;
		}
		if (player.level().getGameTime() >= expiryTick) {
			UNIVERSAL_COOLDOWN_MAP.remove(player.getUUID());
			return false;
		}
		return true;
	}

	public long getRemainingCooldownTicks(Player player) {
		Long expiryTick = UNIVERSAL_COOLDOWN_MAP.get(player.getUUID());
		if (expiryTick == null) {
			return 0;
		}
		return Math.max(0, expiryTick - player.level().getGameTime());
	}

	private void startCooldown(Player player) {
		if (cooldownTicks > 0) {
			UNIVERSAL_COOLDOWN_MAP.put(player.getUUID(), player.level().getGameTime() + cooldownTicks);
		}
	}

	public void performAction(Player player, Level level, ItemStack heldItem, net.minecraft.core.BlockPos position) {
		if (!(player instanceof ServerPlayer serverPlayer) || !(level instanceof ServerLevel serverLevel)) {
			return;
		}
		if (this == BLANK || "no_selected".equals(name)) {
			serverPlayer.displayClientMessage(Component.literal("No Still Art selected.")
					.withStyle(ChatFormatting.GRAY), true);
			return;
		}
		if (!HemoCapabilityAccess.getKnownStillArts(serverPlayer)
				.map(known -> known.isKnown(this))
				.orElse(false)) {
			serverPlayer.displayClientMessage(Component.literal("The Lady has not given you this Still Art.")
					.withStyle(ChatFormatting.GRAY), true);
			return;
		}
		if (!isUnlockedFor(serverPlayer)) {
			serverPlayer.displayClientMessage(Component.literal("Your clarity cannot hold that stillness yet.")
					.withStyle(ChatFormatting.GRAY), true);
			return;
		}
		if (isOnCooldown(serverPlayer)) {
			double seconds = getRemainingCooldownTicks(serverPlayer) / TICKS_PER_SECOND;
			serverPlayer.displayClientMessage(
					Component.literal(String.format("Still Arts are settling. (%.1fs)", seconds))
							.withStyle(ChatFormatting.AQUA),
					true);
			return;
		}

		if (action.cast(serverPlayer, serverLevel, heldItem, position, this)) {
			startCooldown(serverPlayer);
			PacketHandler.sendToPlayer(serverPlayer, new ManipCooldownPacket(cooldownTicks));
		}
	}
}
