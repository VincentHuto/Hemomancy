package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodAccounting;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.util.HemoText;
import com.vincenthuto.hutoslib.client.HLTextUtils;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class LivingToolItem extends DiggerItem implements IDispellable, ITemperAlignedWeapon {
	/** Reserve-protected (F-15): a dormant volume pays nothing, and a short one loses the weapon without paying. */
	private static final BloodAccounting.Source BLOOD_PRICE = BloodAccounting.Source.of("living_tool", Category.TOOL, true);
	/** What a digging form charges for each block it breaks (Track 8); paid only in full. */
	public static final BloodAccounting.Source DIG = BloodAccounting.Source.of("living_dig", Category.TOOL, false);
	/** What a Living tool mines and gets drops from, unless a subclass names its own tag (the axe's). */
	public static final TagKey<Block> MINEABLE = TagKey.create(Registries.BLOCK, Hemomancy.rloc("living_tool_mineable"));
	private final float attackDamage;
	protected final float speed;
	private final TagKey<Block> mineable;
	@Nullable
	private final EnumBloodTemper weaponTemper;

	public LivingToolItem(float speedIn, float attackDamageIn, float attackSpeedIn, Tier tier, Properties builderIn) {
		this(speedIn, attackDamageIn, attackSpeedIn, null, tier, builderIn);
	}

	public LivingToolItem(float speedIn, float attackDamageIn, float attackSpeedIn,
			@Nullable EnumBloodTemper weaponTemper, Tier tier, Properties builderIn) {
		this(speedIn, attackDamageIn, attackSpeedIn, weaponTemper, MINEABLE, tier, builderIn);
	}

	/** {@code mineable} becomes the tool's data component rule: the blocks it mines fast and gets drops from. */
	protected LivingToolItem(float speedIn, float attackDamageIn, float attackSpeedIn,
			@Nullable EnumBloodTemper weaponTemper, TagKey<Block> mineable, Tier tier, Properties builderIn) {
		// A living form has no durability: blood is what it wears, and a form is remade whole at every swap (Track 8).
		super(tier, mineable, builderIn.attributes(DiggerItem.createAttributes(tier, attackDamageIn, attackSpeedIn))
				.component(DataComponents.UNBREAKABLE, new Unbreakable(false)));
		this.attackDamage = attackDamageIn;
		this.speed = speedIn;
		this.weaponTemper = weaponTemper;
		this.mineable = mineable;
	}

	protected float getLivingAttackDamage() {
		return this.attackDamage;
	}

	/**
	 * Whether this form digs (the axe and the pickaxe): it mines at the tier the Staff's rings have earned and pays
	 * blood for every block ({@link LivingDigRules}). The other forms only cut cobwebs and keep their old speed.
	 */
	public boolean digs() {
		return false;
	}

	/** The blocks this form mines fast and harvests. */
	public TagKey<Block> mineable() {
		return mineable;
	}

	/** The dig tier stamped on this form from its Staff's rings. */
	public static LivingStaffGrowthRules.DigTier digTier(ItemStack stack) {
		return LivingStaffGrowthRules.digTier(LivingStaffGrowth.stampedRing(stack));
	}

	@Override
	public float getDestroySpeed(ItemStack stack, BlockState state) {
		if (!digs()) return state.is(mineable) ? speed : 0.5f;
		return state.is(mineable) ? digTier(stack).speed() : LivingDigRules.HAND_SPEED;
	}

	@Override
	public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
		if (!digs()) return super.isCorrectToolForDrops(stack, state);
		// Iron ore at the bond, diamonds from ring 2, diamond-equal from ring 4, never netherite (CL-16, Track 8).
		return state.is(mineable) && !state.is(incorrectFor(digTier(stack).harvest()));
	}

	/** The vanilla tag of blocks a tool of {@code harvest} tier gets no drops from. */
	public static TagKey<Block> incorrectFor(LivingStaffGrowthRules.Harvest harvest) {
		return switch (harvest) {
			case STONE -> BlockTags.INCORRECT_FOR_STONE_TOOL;
			case IRON -> BlockTags.INCORRECT_FOR_IRON_TOOL;
			case DIAMOND -> BlockTags.INCORRECT_FOR_DIAMOND_TOOL;
		};
	}

	@Override
	public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miner) {
		if (digs() && !level.isClientSide && miner instanceof ServerPlayer player && state.is(mineable)) {
			LivingDigging.payForBlock(player, state.getDestroySpeed(level, pos));
		}
		// No durability to wear.
		return true;
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity holder, int slot, boolean selected) {
		super.inventoryTick(stack, level, holder, slot, selected);
		if (digs() && !level.isClientSide && holder instanceof Player player && level.getGameTime() % 20L == slot % 20L) {
			LivingStaffGrowth.refreshStamp(player, stack);
		}
	}

	// A living form takes no enchantment by any route: no table, no anvil, no book (CL-16). Fortune and Silk Touch
	// stay a vanilla pickaxe's reason to exist.

	@Override
	public boolean isEnchantable(ItemStack stack) {
		return false;
	}

	@Override
	public int getEnchantmentValue(ItemStack stack) {
		return 0;
	}

	@Override
	public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
		return false;
	}

	@Override
	public boolean isPrimaryItemFor(ItemStack stack, Holder<Enchantment> enchantment) {
		return false;
	}

	@Override
	public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
		return false;
	}

	/** The form crumbles when its wielder runs dry and it was not a Staff that can recoil back into shape. */
	public static void crumble(ItemStack stack, LivingEntity holder, EquipmentSlot slot) {
		if (stack.isEmpty()) return;
		holder.onEquippedItemBroken(stack.getItem(), slot);
		stack.shrink(1);
	}

	@Override
	public int getEntityLifespan(ItemStack itemStack, Level world) {
		return 0;
	}

	@Override
	public boolean canFitInsideContainerItems() {
		return false;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public Component getName(ItemStack stack) {
		return Component
				.literal(HLTextUtils.stringToBloody(
						HLTextUtils.convertInitToLang(HLTextUtils.getItemRegistryName(stack.getItem()))))
				.withStyle(ChatFormatting.DARK_RED);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, context, tooltip, flagIn);
		TemperWeaponHelper.appendTemperTooltip(stack, tooltip);
		if (getLivingAttackDamage() > 0.0F) {
			tooltip.add(HemoText.translatable("tooltip.hemomancy.living_tool.melee_blood_cost_50_chance_of",
					(int) (getLivingAttackDamage() * 25f)).withStyle(ChatFormatting.DARK_RED));
		}
		if (digs()) {
			LivingStaffGrowthRules.DigTier tier = digTier(stack);
			tooltip.add(Component.translatable("tooltip.hemomancy.living_tool.dig_tier."
					+ tier.harvest().name().toLowerCase(java.util.Locale.ROOT),
					LivingStaffGrowth.stampedRing(stack)).withStyle(ChatFormatting.GRAY));
			tooltip.add(Component.translatable("tooltip.hemomancy.living_tool.dig_cost").withStyle(ChatFormatting.DARK_RED));
		}
		if (this instanceof LivingBladeItem || this instanceof LivingAxeItem) {
			tooltip.add(HemoText.translatable("tooltip.hemomancy.living_tool.feral_hit_an_additional_ml",
					(int) (getLivingAttackDamage() * 75f)).withStyle(ChatFormatting.DARK_RED));
		}
		if (this instanceof LivingTorchItem) {
			tooltip.add(HemoText.translatable("tooltip.hemomancy.living_tool.breath_upkeep_ml_s",
					(int) (LivingTorchBreathRules.BLOOD_COST_PER_TICK * 20)).withStyle(ChatFormatting.DARK_RED));
		}
	}

	@Override
	@Nullable
	public EnumBloodTemper hemomancy$getWeaponTemper() {
		return weaponTemper;
	}

	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		if (target.level().random.nextBoolean()) {
			if (attacker instanceof Player) {
				if (!attacker.level().isClientSide) {
					Player playerIn = (Player) attacker;
					IBloodVolume playerVolume = HemoCapabilityAccess.getBloodVolume(playerIn)
							;
					float damageMod = this.getLivingAttackDamage() * 25f;
					if (!playerVolume.isActive()) {
						// A dormant volume owes the tool nothing.
					} else if (BloodAccounting.canPayInFull(playerIn, damageMod, BLOOD_PRICE)) {
						BloodAccounting.pay(playerIn, damageMod, BLOOD_PRICE);
					} else {
						boolean restoredStaffForm = LivingStaffWeaponFormHelper.restoreStaffAfterBloodFailure(stack, attacker);
						if (!restoredStaffForm) {
							crumble(stack, attacker, EquipmentSlot.MAINHAND);
						}
						Vec3 pos = playerIn.position();
						if (attacker.level() instanceof ServerLevel serverLevel) {
							PacketHandler.sendLivingToolBreakParticles(pos, ParticleColor.BLOOD, 64f, serverLevel);
						}
					}

				}
			}
		}

		if (target.level().random.nextDouble() > 0.75) {
			attacker.addEffect(new MobEffectInstance(EffectInit.blood_rush, 50, 2));
			target.addEffect(new MobEffectInstance(EffectInit.blood_loss, 50, 2));
		}
		return super.hurtEnemy(stack, target, attacker);
	}

}
