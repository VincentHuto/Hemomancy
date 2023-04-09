package com.vincenthuto.hemomancy.item;

import java.util.List;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hutoslib.common.item.ItemGuideBook;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class BloodyBookItem extends ItemGuideBook {

	public BloodyBookItem(Properties prop, ResourceLocation loc) {
		super(prop, loc);
	}

	@Override
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
		tooltip.add(Component.literal(ChatFormatting.GOLD + "A guide to your blood and its power."));
	}

//	@Override
//	public boolean hurtEnemy(ItemStack stack, LivingEntity entity, @Nonnull LivingEntity attacker) {
//		if (!(entity instanceof Player) && entity != null) {
//			double range = 8;
//			List<LivingEntity> alreadyTargetedEntities = new ArrayList<>();
//			int dmg = 5;
//			long lightningSeed = entity.level.random.nextLong();
//
//			Predicate<Entity> selector = e -> e instanceof LivingEntity && !(e instanceof Player)
//					&& !alreadyTargetedEntities.contains(e);
//
//			Random rand = new Random(lightningSeed);
//			LivingEntity lightningSource = entity;
//			int hops = entity.level.isThundering() ? 10 : 4;
//			for (int i = 0; i < hops; i++) {
//				List<Entity> entities = entity.level.getEntities(lightningSource,
//						new AABB(lightningSource.getX() - range, lightningSource.getY() - range,
//								lightningSource.getZ() - range, lightningSource.getX() + range,
//								lightningSource.getY() + range, lightningSource.getZ() + range),
//						selector);
//				if (entities.isEmpty()) {
//					break;
//				}
//
//				LivingEntity target = (LivingEntity) entities.get(rand.nextInt(entities.size()));
//				if (attacker instanceof Player player) {
//					target.hurt(this.damageSources().playerAttack(player), dmg);
//				} else {
//					target.hurt(this.damageSources().mobAttack(attacker), dmg);
//				}
//
//				HutosLib.proxy.lightningFX(VecHelper.fromEntityCenter(lightningSource),
//						VecHelper.fromEntityCenter(target), 1, 0x0179C4, 0xAADFFF);
//
//				alreadyTargetedEntities.add(target);
//				lightningSource = target;
//				dmg--;
//			}
//		}
//
//		return super.hurtEnemy(stack, entity, attacker);
//	}

	@Override
	public Rarity getRarity(ItemStack par1ItemStack) {
		return Rarity.UNCOMMON;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level lvl, Player p_41433_, InteractionHand p_41434_) {
		if (lvl.isClientSide()) {
			Hemomancy.proxy.openGuideGui();
		}
		return super.use(lvl, p_41433_, p_41434_);
	}

}
