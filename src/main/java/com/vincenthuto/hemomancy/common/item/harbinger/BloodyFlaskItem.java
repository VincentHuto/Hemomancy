package com.vincenthuto.hemomancy.common.item.harbinger;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.init.DataComponentInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.tile.CoagulationRules;
import com.vincenthuto.hemomancy.common.tile.HeldFormations;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Blood in a clay vessel. A Flask or Jug is an inert container (core-loop Track 3): it is stamped when it is bottled, or
 * when a player first carries an unstamped one, and its blood clots by {@link CoagulationRules} from then. Drinking or
 * pouring it gives the fresh share as blood and the clotted share as Sanguine Formations. Blood Rock does not clot.
 */
public class BloodyFlaskItem extends Item {

	private final double amount;
	private final boolean clots;

	public BloodyFlaskItem(Properties prop, double amount) {
		this(prop, amount, false);
	}

	public BloodyFlaskItem(Properties prop, double amount, boolean clots) {
		super(prop.stacksTo(16));
		this.amount = amount;
		this.clots = clots;
	}

	public double getAmount() {
		return amount;
	}

	public boolean clots() {
		return clots;
	}

	/**
	 * Stamps the time {@code stack} was bottled, rounded down to the minute; a stack that already carries a stamp keeps
	 * the older one.
	 */
	public static void stampBottled(ItemStack stack, long gameTime) {
		if (stack.getItem() instanceof BloodyFlaskItem flask && flask.clots && !stack.has(DataComponentInit.BOTTLED_AT))
			stack.set(DataComponentInit.BOTTLED_AT.get(), CoagulationRules.bottledStamp(gameTime));
	}

	/** Share of one vessel's blood still fresh at {@code gameTime}: 1 for an unstamped or non-clotting vessel. */
	public static double freshShare(ItemStack stack, long gameTime) {
		Long bottled = stack.get(DataComponentInit.BOTTLED_AT);
		if (bottled == null || !(stack.getItem() instanceof BloodyFlaskItem flask) || !flask.clots) return 1.0D;
		return CoagulationRules.freshFraction(gameTime - bottled);
	}

	/** Display reading of a vessel: 1 when just bottled, 0 at the end of the fresh window. */
	public static double freshness(ItemStack stack, long gameTime) {
		Long bottled = stack.get(DataComponentInit.BOTTLED_AT);
		return bottled == null ? 1.0D : CoagulationRules.freshness(gameTime - bottled);
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
		// Loot and gifts start clotting once someone carries them.
		if (!level.isClientSide && entity instanceof Player) stampBottled(stack, level.getGameTime());
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		tooltip.add(Component.translatable("tooltip.hemomancy.bloody_flask.used_to_quickly_gain_s_ml", amount));
		tooltip.add(Component.translatable("tooltip.hemomancy.bloody_flask.repeated").withStyle(ChatFormatting.GRAY));
		Level level = context.level();
		if (!clots || level == null || !stack.has(DataComponentInit.BOTTLED_AT)) return;
		double fresh = freshShare(stack, level.getGameTime());
		if (fresh >= 1.0D) {
			tooltip.add(Component.translatable("tooltip.hemomancy.living_blood.fresh").withStyle(ChatFormatting.RED));
		} else if (fresh <= 0.0D) {
			tooltip.add(Component.translatable("tooltip.hemomancy.living_blood.clotted").withStyle(ChatFormatting.DARK_GRAY));
		} else {
			tooltip.add(Component.translatable("tooltip.hemomancy.living_blood.clotting",
					Math.round((1.0D - fresh) * 100.0D)).withStyle(ChatFormatting.GRAY));
		}
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		ItemStack stack = playerIn.getItemInHand(handIn);
		if (worldIn.isClientSide || !(playerIn instanceof ServerPlayer player))
			return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player);
		double unclotted = amount * freshShare(stack, worldIn.getGameTime());
		int formations = CoagulationRules.neglectFormations(amount - unclotted);
		// CL-6: repeated drinks give less; the lost share is gone, and the clot's Formations are not cut.
		double share = CheapBloodInfusionRules.yieldShare(CheapBloodInfusionHelper.getActiveAmplifier(player));
		double fresh = unclotted * share;
		if (fresh > 0.0D && volume.isFull()) {
			player.displayClientMessage(Component.translatable("message.hemomancy.bloody_flask_item.blood_volume_is_full"), true);
			return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
		}
		if (fresh > 0.0D) {
			volume.fill(fresh);
			for (int i = 0; i < 30; i++) {
				PacketHandler.sendBloodFlaskParticles(player.position(), ParticleColor.BLOOD, 64f, (ServerLevel) worldIn);
			}
			BloodVolumeEvents.syncVolume(player, volume);
			CheapBloodInfusionHelper.applySuccessfulInfusion(player, stack.getItem(), share);
		}
		if (formations > 0) {
			// The clotted share breaks out of the vessel as Formations; what does not fit falls at the player's feet.
			int given = HeldFormations.give(player, formations);
			if (given < formations)
				player.drop(new ItemStack(ItemInit.sanguine_formation.get(), formations - given), false);
			player.displayClientMessage(Component.translatable("message.hemomancy.living_blood.flask_clotted", formations), true);
		}
		stack.shrink(1);
		return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
	}
}
