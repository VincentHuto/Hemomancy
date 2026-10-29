package com.vincenthuto.hemomancy.common.item.harbinger;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.possession.PossessionAbilities;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.possession.PossessionRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.possession.PossessionService;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.possession.SurrogateForm;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

/**
 * The thread a possessing Silentium's will hangs by. Used, it calls on the facsimile's gift; held while kneeling
 * (sneaking), it draws the will home cleanly. It exists only during a possession and cannot be kept.
 */
public class ThreadOfWillItem extends Item {
	public ThreadOfWillItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		SurrogateForm form = HemoCapabilityAccess.possessionForm(player);
		if (form == null) return InteractionResultHolder.fail(stack);
		if (player.isShiftKeyDown()) {
			player.startUsingItem(hand);
			return InteractionResultHolder.consume(stack);
		}
		if (form.abilityCooldown() <= 0 || player.getCooldowns().isOnCooldown(this)) return InteractionResultHolder.pass(stack);
		if (!level.isClientSide && player instanceof ServerPlayer server && PossessionAbilities.use(server, form)) {
			player.getCooldowns().addCooldown(this, form.abilityCooldown());
		}
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity entity) {
		return PossessionRules.RETURN_HOLD_TICKS;
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.BOW;
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
		if (!level.isClientSide && entity instanceof ServerPlayer player && player.isShiftKeyDown()) {
			PossessionService.returnClean(player);
		}
		return stack;
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
		if (!level.isClientSide && entity instanceof Player player && !HemoCapabilityAccess.isPossessing(player)) {
			stack.setCount(0);
		}
	}

	/** A dropped thread snaps back to the will it belongs to, or simply unravels. */
	@Override
	public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
		if (entity.level().isClientSide) return false;
		if (entity.getOwner() instanceof Player owner && HemoCapabilityAccess.isPossessing(owner)) {
			owner.getInventory().add(stack.copy());
		}
		entity.discard();
		return true;
	}
}
