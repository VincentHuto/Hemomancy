package com.vincenthuto.hemomancy.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

import javax.annotation.Nonnull;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.IProxy;
import com.vincenthuto.hemomancy.VecHelper;
import com.vincenthuto.hemomancy.gui.guide.HemoTitlePage;
import com.vincenthuto.hutoslib.common.item.ItemGuideBook;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class ItemBloodyBook extends ItemGuideBook {

	public ItemBloodyBook(Properties prop, ResourceLocation loc) {
		super(prop, loc);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level lvl, Player p_41433_, InteractionHand p_41434_) {
		if (lvl.isClientSide()) {
			Hemomancy.proxy.openGui(new HemoTitlePage());
		}
		return super.use(lvl, p_41433_, p_41434_);
	}

	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity entity, @Nonnull LivingEntity attacker) {
		if (!(entity instanceof Player) && entity != null) {
			double range = 8;
			List<LivingEntity> alreadyTargetedEntities = new ArrayList<>();
			int dmg = 5;
			long lightningSeed = entity.level.random.nextLong();

			Predicate<Entity> selector = e -> e instanceof LivingEntity && !(e instanceof Player)
					&& !alreadyTargetedEntities.contains(e);

			Random rand = new Random(lightningSeed);
			LivingEntity lightningSource = entity;
			int hops = entity.level.isThundering() ? 10 : 4;
			for (int i = 0; i < hops; i++) {
				List<Entity> entities = entity.level.getEntities(lightningSource,
						new AABB(lightningSource.getX() - range, lightningSource.getY() - range,
								lightningSource.getZ() - range, lightningSource.getX() + range,
								lightningSource.getY() + range, lightningSource.getZ() + range),
						selector);
				if (entities.isEmpty()) {
					break;
				}

				LivingEntity target = (LivingEntity) entities.get(rand.nextInt(entities.size()));
				if (attacker instanceof Player player) {
					target.hurt(DamageSource.playerAttack(player), dmg);
				} else {
					target.hurt(DamageSource.mobAttack(attacker), dmg);
				}

				Hemomancy.proxy.lightningFX(VecHelper.fromEntityCenter(lightningSource),
						VecHelper.fromEntityCenter(target), 1, 0x0179C4, 0xAADFFF);

				alreadyTargetedEntities.add(target);
				lightningSource = target;
				dmg--;
			}
		}

		return super.hurtEnemy(stack, entity, attacker);
	}

	@Override
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
		tooltip.add(new TextComponent(ChatFormatting.GOLD + "A guide to your blood and its power."));
	}

	@Override
	public Rarity getRarity(ItemStack par1ItemStack) {
		return Rarity.UNCOMMON;
	}

}
