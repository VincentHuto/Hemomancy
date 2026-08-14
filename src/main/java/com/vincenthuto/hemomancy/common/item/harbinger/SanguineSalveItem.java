package com.vincenthuto.hemomancy.common.item.harbinger;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.IVascularSystem;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.VascularSystemEvents;
import com.vincenthuto.hutoslib.client.HLTextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Map;

/**
 * A consumable salve that repairs the player's most-damaged vascular section.
 * <p>
 * On use the item heals the worst section by a flat amount (default 25 HP out
 * of 100) and plays a squelchy sound. If the blood system isn't active, or
 * every section is already at 100, the item won't be consumed.
 * <p>
 * Intended craft: dried leech + recycled enzyme + blood-stained stone (or similar).
 */
public class SanguineSalveItem extends Item {

	private final float healAmount;

	/**
	 * @param properties  standard item properties
	 * @param healAmount  how many HP (0-100 scale) a single use restores
	 */
	public SanguineSalveItem(Properties properties, float healAmount) {
		super(properties.stacksTo(16));
		this.healAmount = healAmount;
	}

	public SanguineSalveItem(Properties properties) {
		this(properties, 25f);
	}

	// ── Tooltip ──

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, context, tooltip, flag);
		tooltip.add(Component.literal("Repairs the most damaged vein section")
				.withStyle(ChatFormatting.GRAY));
		tooltip.add(Component.literal("Restores +" + (int) healAmount + " vascular health")
				.withStyle(ChatFormatting.DARK_RED));
	}

	// ── Use behaviour (hold-to-apply, like eating) ──

	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.BOW;
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity entity) {
		return 30; // 1.5 seconds
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);

		// Only usable when the blood system is active
		boolean bloodActive = HemoCapabilityAccess.getBloodVolume(player)
				.map(vol -> vol.isActive()).orElse(false);
		if (!bloodActive) {
			return InteractionResultHolder.fail(stack);
		}

		boolean needsHeal = HemoCapabilityAccess.getVascularSystem(player)
				.map(vasc -> {
					for (EnumVeinSections s : EnumVeinSections.values()) {
						if (vasc.getHealthBySection(s) < 100f) return true;
					}
					return false;
				}).orElse(false);

		if (!needsHeal) {
			player.displayClientMessage(
					Component.literal("All vein sections are healthy").withStyle(ChatFormatting.GREEN), true);
			return InteractionResultHolder.fail(stack);
		}

		player.startUsingItem(hand);
		return InteractionResultHolder.consume(stack);
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
		if (!(entity instanceof Player player)) return stack;
		if (level.isClientSide) return stack;

		HemoCapabilityAccess.getVascularSystem(player).ifPresent(vascular -> {
			// Find the most damaged section
			EnumVeinSections worst = findMostDamagedSection(vascular);
			if (worst == null) return; // all at 100

			float before = vascular.getHealthBySection(worst);
			float after = Math.min(100f, before + healAmount);

			// Apply the heal
			Map<EnumVeinSections, Float> sys = vascular.getVascularSystem();
			sys.put(worst, after);
			vascular.setVascularSystem(sys);

			// Sync to client
			VascularSystemEvents.syncVascular((ServerPlayer) player, vascular);

			// Feedback
			String sectionName = HLTextUtils.toProperCase(worst.name());
			player.displayClientMessage(
					Component.literal(sectionName + " repaired: " + (int) before + "% → " + (int) after + "%")
							.withStyle(ChatFormatting.DARK_RED),
					true);

			level.playSound(null, player.getX(), player.getY(), player.getZ(),
					SoundEvents.HONEY_DRINK, SoundSource.PLAYERS, 0.8f, 0.9f);
		});

		// Consume the item
		if (!player.getAbilities().instabuild) {
			stack.shrink(1);
		}
		player.getCooldowns().addCooldown(this, 40); // 2-second cooldown

		return stack;
	}

	/**
	 * Returns the section with the lowest health, or null if all sections are at 100.
	 */
	private EnumVeinSections findMostDamagedSection(IVascularSystem vascular) {
		EnumVeinSections worst = null;
		float worstHealth = 100f;

		for (EnumVeinSections section : EnumVeinSections.values()) {
			float h = vascular.getHealthBySection(section);
			if (h < worstHealth) {
				worstHealth = h;
				worst = section;
			}
		}

		return (worstHealth >= 100f) ? null : worst;
	}

	public float getHealAmount() {
		return healAmount;
	}
}
