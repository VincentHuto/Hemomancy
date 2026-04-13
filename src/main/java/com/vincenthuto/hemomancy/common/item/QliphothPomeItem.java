package com.vincenthuto.hemomancy.common.item;

import java.util.List;
import java.util.function.Consumer;

import com.vincenthuto.hemomancy.client.render.item.QliphothPomeItemRenderer;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeProvider;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

/**
 * Edible reagent fruit dropped by Qliphoth Bloom trees. Renders as a dark
 * glowing orb matching the void-like spheres embedded in the tree's trunk.
 *
 * <p>Eating the pome grants a portable echo of the Bloom's aura:
 * <ul>
 *   <li>+300 blood volume (instant burst if blood system is active)</li>
 *   <li>Regeneration II for 12 seconds</li>
 *   <li>Darkness for 7 seconds (Qliphothic energy dims the senses)</li>
 *   <li>25 % manipulation cost reduction for 3 minutes</li>
 * </ul>
 */
public class QliphothPomeItem extends Item {

	/**
	 * NBT key stored on the player's persistent data ({@link net.minecraft.world.entity.Entity#getPersistentData()})
	 * holding the world game-time tick at which the Pome Empowerment discount expires.
	 * A value of 0 (or absent) means no active empowerment.
	 */
	public static final String POME_EMPOWERMENT_KEY = "hemomancy:pome_empowerment_expiry";

	/** Amount of blood (in mod units) added instantly on consume. */
	private static final double BLOOD_BURST = 300.0;

	/** Duration of the Regeneration II effect in ticks (12 seconds). */
	private static final int REGEN_DURATION_TICKS = 240;

	/** Duration of the Darkness side-effect in ticks (7 seconds). */
	private static final int DARKNESS_DURATION_TICKS = 140;

	/** Duration of the 25 % manipulation discount in ticks (3 minutes). */
	private static final long EMPOWERMENT_DURATION_TICKS = 3600L;

	public QliphothPomeItem(Properties properties) {
		super(properties);
	}

	// ── Tooltip ──

	@Override
	public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, world, tooltip, flag);
		tooltip.add(Component.literal("A dark fruit of the Qliphoth, heavy with void-blood")
				.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
		tooltip.add(Component.literal("+300 Blood Volume")
				.withStyle(ChatFormatting.DARK_RED));
		tooltip.add(Component.literal("Regeneration II (12s)")
				.withStyle(ChatFormatting.RED));
		tooltip.add(Component.literal("Manipulation Cost -25% (3 min)")
				.withStyle(ChatFormatting.GOLD));
		tooltip.add(Component.literal("Darkness (7s)")
				.withStyle(ChatFormatting.DARK_GRAY));
	}

	// ── On-eat effects ──

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
		// Let vanilla handle hunger/saturation consumption
		ItemStack result = super.finishUsingItem(stack, level, entity);

		if (!level.isClientSide && entity instanceof Player player) {
			// ── Blood burst ──
			player.getCapability(BloodVolumeProvider.VOLUME_CAPA).ifPresent(volume -> {
				if (volume.isActive()) {
					volume.fill(BLOOD_BURST);
					BloodVolumeEvents.syncVolume((ServerPlayer) player, volume);
				}
			});

			// ── Regeneration II burst ──
			player.addEffect(new MobEffectInstance(MobEffects.REGENERATION,
					REGEN_DURATION_TICKS, 1, false, true, true));

			// ── Darkness side-effect ──
			player.addEffect(new MobEffectInstance(MobEffects.DARKNESS,
					DARKNESS_DURATION_TICKS, 0, false, true, true));

			// ── Timed manipulation discount ──
			long expiryTick = level.getGameTime() + EMPOWERMENT_DURATION_TICKS;
			player.getPersistentData().putLong(POME_EMPOWERMENT_KEY, expiryTick);

			player.displayClientMessage(
					Component.literal("The void-fruit's power floods your veins...")
							.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC),
					true);
		}

		return result;
	}

	// ── Renderer ──

	@Override
	public void initializeClient(Consumer<IClientItemExtensions> consumer) {
		consumer.accept(new IClientItemExtensions() {
			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return new QliphothPomeItemRenderer(
						Minecraft.getInstance().getBlockEntityRenderDispatcher(),
						Minecraft.getInstance().getEntityModels());
			}
		});
	}
}
