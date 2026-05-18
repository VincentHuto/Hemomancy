package com.vincenthuto.hemomancy.common.item.harbinger.tool;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.entity.npc.DrudgeEntity;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.BloodVolumeServerPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;
import java.util.UUID;

public class DSDItem extends Item {

	/** Blood recovered when dissolving a Drudge. */
	private static final double DISSOLUTION_REFUND = 1500.0;
	/** Radius in which the DSD searches for a Drudge to dissolve. */
	private static final double DSD_RANGE = 16.0;

	public DSDItem(Properties prop) {
		super(prop.stacksTo(1));
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, context, tooltip, flagIn);
		tooltip.add(Component.literal("Also known as a D.S.D. used to"));
		tooltip.add(Component.literal("commandeer Drudges to your will."));
		tooltip.add(Component.literal("§8Shift+right-click to dissolve the nearest bound Drudge."));
	}

	@Override
	public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
		if (target instanceof DrudgeEntity drudge) {
			if (!player.level().isClientSide && player.isShiftKeyDown()) {
				UUID ownerUUID = drudge.getOwnerUUID();
				boolean isOwner = ownerUUID != null && ownerUUID.equals(player.getUUID());
				boolean isCreative = player.getAbilities().instabuild;
				if (isOwner || isCreative) {
					dissolve(player, drudge);
					return InteractionResult.SUCCESS;
				} else {
					player.displayClientMessage(Component.literal("§cThis is not your construct."), true);
					return InteractionResult.FAIL;
				}
			}
		}
		return super.interactLivingEntity(stack, player, target, hand);
	}

	/**
	 * Dissolves (kills) a Drudge and refunds blood to the owning player.
	 */
	private void dissolve(Player player, DrudgeEntity drudge) {
		// Return blood
		IBloodVolume vol = HemoCapabilityAccess.getBloodVolume(player).orElse(null);
		if (vol != null && vol.isActive()) {
			vol.fill(DISSOLUTION_REFUND);
			if (player instanceof ServerPlayer sp) {
				PacketHandler.sendToPlayer(sp, new BloodVolumeServerPacket(vol));
			}
		}

		// Visual cue
		player.level().playSound(null, drudge.blockPosition(), SoundEvents.ZOMBIE_DEATH,
				SoundSource.PLAYERS, 0.5f, 0.8f);

		drudge.discard();

		player.displayClientMessage(Component.literal(
				"§7The construct unravels. §c+" + (int) DISSOLUTION_REFUND + " mL§7 returned to blood."),
				false);
	}
}

