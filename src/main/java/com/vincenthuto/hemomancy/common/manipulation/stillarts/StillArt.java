package com.vincenthuto.hemomancy.common.manipulation.stillarts;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumClarityStage;
import com.vincenthuto.hemomancy.common.capability.player.unstained.IUnstainedProgress;
import com.vincenthuto.hemomancy.common.init.StillArtInit;
import com.vincenthuto.hemomancy.common.manipulation.animation.CastingAnimationManager;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.unstained.StillArtCooldownPacket;
import com.vincenthuto.hemomancy.common.util.HemoText;
import com.vincenthuto.hutoslib.client.HLTextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class StillArt {
	public static final StillArt BLANK = new StillArt("no_selected", EnumClarityStage.AWAKENED, 0, StillArtEnding.STILLNESS, StillArtAction.NOOP);

	private static final double TICKS_PER_SECOND = 20.0;
	private static final Map<UUID, Long> UNIVERSAL_COOLDOWN_MAP = new ConcurrentHashMap<>();

	public static void clearSessionState() {
		UNIVERSAL_COOLDOWN_MAP.clear();
	}

	private final String name;
	private final EnumClarityStage requiredStage;
	private final int cooldownTicks;
	private final StillArtEnding ending;
	private final StillArtAction action;

	public StillArt(String name, EnumClarityStage requiredStage, int cooldownTicks, StillArtEnding ending, StillArtAction action) {
		this.name = name;
		this.requiredStage = requiredStage;
		this.cooldownTicks = cooldownTicks;
		this.ending = ending;
		this.action = action;
	}

	public String getName() {
		return name;
	}

	public String getProperName() {
		return HLTextUtils.convertInitToLang(name);
	}

	/** Player-facing name from {@code hemomancy.still_art.<name>}, or the proper-cased id for an art without one. */
	public MutableComponent getDisplayName() {
		return Component.translatableWithFallback("hemomancy.still_art." + name, getProperName());
	}

	public EnumClarityStage getRequiredStage() {
		return requiredStage;
	}

	public int getCooldownTicks() {
		return cooldownTicks;
	}

	/** Presentation only: the closing gesture of the cast. Never affects gameplay or saved data. */
	public StillArtEnding getEnding() {
		return ending;
	}

	public static StillArt byName(String name) {
		StillArt art = StillArtInit.getByName(name);
		return art != null ? art : BLANK;
	}

	public boolean isUnlockedFor(Player player) {
		IUnstainedProgress progress = HemoCapabilityAccess.getUnstainedProgress(player);
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
        if (com.vincenthuto.hemomancy.common.manipulation.ductilis.Paralysis.blocksActions(player)) return;
		if (this == BLANK || "no_selected".equals(name)) {
			serverPlayer.displayClientMessage(Component.translatable("message.hemomancy.still_art.no_still_art_selected").withStyle(ChatFormatting.GRAY), true);
			return;
		}
		if (!HemoCapabilityAccess.getKnownStillArts(serverPlayer).isKnown(this)) {
			serverPlayer.displayClientMessage(Component.translatable("message.hemomancy.still_art.the_lady_has_not_granted_you").withStyle(ChatFormatting.GRAY), true);
			return;
		}
		if (!isUnlockedFor(serverPlayer)) {
			serverPlayer.displayClientMessage(Component.translatable("message.hemomancy.still_art.your_clarity_is_too_weak_for").withStyle(ChatFormatting.GRAY), true);
			return;
		}
		if (isOnCooldown(serverPlayer)) {
			double seconds = getRemainingCooldownTicks(serverPlayer) / TICKS_PER_SECOND;
			serverPlayer.displayClientMessage(
					HemoText.translatable("message.hemomancy.still_art.still_arts_are_settling_s", String.format("%.1f", seconds)).withStyle(ChatFormatting.AQUA),
					true);
			return;
		}

		if (action.cast(serverPlayer, serverLevel, heldItem, position, this)) {
			startCooldown(serverPlayer);
			PacketHandler.sendToPlayer(serverPlayer, new StillArtCooldownPacket(cooldownTicks));
			CastingAnimationManager.releaseStillArt(serverPlayer, this);
		}
	}
}
