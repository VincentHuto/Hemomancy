package com.vincenthuto.hemomancy.common.item.harbinger;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.VascularSystemEvents;
import com.vincenthuto.hemomancy.common.mission.cicatrix_anchorite.VeinMasonAssignments;
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
		tooltip.add(Component.translatable("tooltip.hemomancy.sanguine_salve.repairs_the_most_damaged_vein_section")
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
		boolean bloodActive = HemoCapabilityAccess.getBloodVolume(player).isActive();
		if (!bloodActive) {
			return InteractionResultHolder.fail(stack);
		}

		var vasc = HemoCapabilityAccess.getVascularSystem(player);
		boolean needsHeal = false;
		for (EnumVeinSections s : EnumVeinSections.values()) {
			if (vasc.getHealthBySection(s) < 100f) {
				needsHeal = true;
				break;
			}
		}

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

		var vascular = HemoCapabilityAccess.getVascularSystem(player);
		var result = VascularPoulticeRules.healMostDamaged(vascular.getVascularSystem(), healAmount).orElse(null);
		if (result != null) {
			EnumVeinSections worst = result.section();
			float before = result.before();
			float after = result.after();
			vascular.setVascularSystem(vascular.getVascularSystem());

			// Sync to client
			VascularSystemEvents.syncVascular((ServerPlayer) player, vascular);
			VeinMasonAssignments.onTreatment((ServerPlayer) player);

			// Feedback
			String sectionName = HLTextUtils.toProperCase(worst.name());
			player.displayClientMessage(
					Component.literal(sectionName + " repaired: " + (int) before + "% → " + (int) after + "%")
							.withStyle(ChatFormatting.DARK_RED),
					true);

			level.playSound(null, player.getX(), player.getY(), player.getZ(),
					SoundEvents.HONEY_DRINK, SoundSource.PLAYERS, 0.8f, 0.9f);
		}

		// Consume the item
		if (!player.getAbilities().instabuild) {
			stack.shrink(1);
		}
		player.getCooldowns().addCooldown(this, 40); // 2-second cooldown

		return stack;
	}

	public float getHealAmount() {
		return healAmount;
	}
}
