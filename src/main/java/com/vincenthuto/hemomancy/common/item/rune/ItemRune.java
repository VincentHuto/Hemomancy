package com.vincenthuto.hemomancy.common.item.rune;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.vincenthuto.hemomancy.common.capability.player.kinship.BloodTendencyProvider;
import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.kinship.IBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.rune.IRune;
import com.vincenthuto.hemomancy.common.capability.player.rune.RuneType;
import com.vincenthuto.hemomancy.common.capability.player.rune.RunesCapabilities;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.BloodTendencyServerPacket;
import com.vincenthuto.hutoslib.client.HLTextUtils;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.PacketDistributor;

public class ItemRune extends Item implements IRune {

	EnumBloodTendency assignedTendency;
	float deepenAmount;
	int tier;

	private final List<RuneModifier> passiveModifiers = new ArrayList<>();
	private final List<RuneEffectEntry> passiveEffects = new ArrayList<>();

	public record RuneModifier(Attribute attribute, UUID uuid, String name, double amount,
			AttributeModifier.Operation operation) {
	}

	public record RuneEffectEntry(MobEffect effect, int amplifier) {
	}

	/** Legacy constructor for backward compatibility (tier defaults to 0). */
	public ItemRune(Properties properties, EnumBloodTendency tendencyIn, float deepenAmountIn) {
		this(properties, tendencyIn, deepenAmountIn, 0);
	}

	public ItemRune(Properties properties, EnumBloodTendency tendencyIn, float deepenAmountIn, int tier) {
		super(properties);
		this.assignedTendency = tendencyIn;
		this.deepenAmount = deepenAmountIn;
		this.tier = tier;
	}

	/** Adds a persistent attribute modifier applied on equip and removed on unequip. */
	public ItemRune withModifier(Attribute attribute, String name, double amount,
			AttributeModifier.Operation operation) {
		UUID uuid = UUID.nameUUIDFromBytes(("hemomancy:" + name).getBytes());
		this.passiveModifiers.add(new RuneModifier(attribute, uuid, name, amount, operation));
		return this;
	}

	/** Adds a persistent mob effect granted while the rune is equipped. */
	public ItemRune withEffect(MobEffect effect, int amplifier) {
		this.passiveEffects.add(new RuneEffectEntry(effect, amplifier));
		return this;
	}

	@Override
	public void onEquipped(LivingEntity player) {
		if (player instanceof Player) {
			if (!player.getCommandSenderWorld().isClientSide) {
				IBloodTendency coven = player.getCapability(BloodTendencyProvider.TENDENCY_CAPA)
						.orElseThrow(IllegalArgumentException::new);
				if (coven != null) {
					coven.setTendencyAlignment(getAssignedTendency(), getDeepenAmount());
					Player playerEnt = (Player) player;
					PacketHandler.CHANNELBLOODTENDENCY.send(
							PacketDistributor.PLAYER.with(() -> (ServerPlayer) playerEnt),
							new BloodTendencyServerPacket(coven.getTendency()));
				}

				for (RuneModifier mod : passiveModifiers) {
					AttributeInstance attr = player.getAttribute(mod.attribute());
					if (attr != null && attr.getModifier(mod.uuid()) == null) {
						attr.addPermanentModifier(
								new AttributeModifier(mod.uuid(), mod.name(), mod.amount(), mod.operation()));
					}
				}

				for (RuneEffectEntry eff : passiveEffects) {
					player.addEffect(new MobEffectInstance(eff.effect(), -1, eff.amplifier(), true, false));
				}
			}
		}
	}

	@Override
	public void onUnequipped(LivingEntity player) {
		if (player instanceof Player) {
			if (!player.getCommandSenderWorld().isClientSide) {
				IBloodTendency coven = player.getCapability(BloodTendencyProvider.TENDENCY_CAPA)
						.orElseThrow(IllegalArgumentException::new);
				if (coven != null) {
					coven.setTendencyAlignment(getAssignedTendency(), -getDeepenAmount());
					Player playerEnt = (Player) player;
					PacketHandler.CHANNELBLOODTENDENCY.send(
							PacketDistributor.PLAYER.with(() -> (ServerPlayer) playerEnt),
							new BloodTendencyServerPacket(coven.getTendency()));
				}

				for (RuneModifier mod : passiveModifiers) {
					AttributeInstance attr = player.getAttribute(mod.attribute());
					if (attr != null) {
						attr.removePermanentModifier(mod.uuid());
					}
				}

				for (RuneEffectEntry eff : passiveEffects) {
					if (!hasOtherRuneWithEffect(player, eff.effect())) {
						player.removeEffect(eff.effect());
					}
				}
			}
		}
	}

	private boolean hasOtherRuneWithEffect(LivingEntity player, MobEffect effect) {
		return player.getCapability(RunesCapabilities.RUNES).map(runes -> {
			for (int i = 0; i < runes.getSlots(); i++) {
				ItemStack stack = runes.getStackInSlot(i);
				if (stack.getItem() instanceof ItemRune otherRune) {
					for (RuneEffectEntry otherEff : otherRune.passiveEffects) {
						if (otherEff.effect() == effect)
							return true;
					}
				}
			}
			return false;
		}).orElse(false);
	}

	@Override
	public void onWornTick(LivingEntity entity) {
		if (!entity.level().isClientSide && tier >= 3) {
			applyTierThreeTickEffect(entity);
		}
	}

	protected void applyTierThreeTickEffect(LivingEntity entity) {
		switch (assignedTendency) {
		case CONGEATIO:
			if (entity.tickCount % 40 == 0) {
				AABB area = entity.getBoundingBox().inflate(5.0);
				entity.level().getEntitiesOfClass(Monster.class, area)
						.forEach(mob -> mob.addEffect(
								new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0)));
			}
			break;
		case TENEBRIS:
			if (entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) < 4) {
				entity.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 40, 0, true, false));
			}
			break;
		case ANIMUS:
			if (entity.getHealth() < entity.getMaxHealth() * 0.5f && entity.tickCount % 60 == 0) {
				entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 80, 0, true, false));
			}
			break;
		default:
			break;
		}
	}

	/**
	 * Called when a player wearing this rune attacks another entity.
	 */
	public void onPlayerAttack(Player player, LivingEntity target) {
		if (assignedTendency == EnumBloodTendency.MORTEM) {
			if (tier >= 3) {
				target.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 1));
			} else if (tier >= 2) {
				target.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0));
			}
		}
	}

	/**
	 * Called when a player wearing this rune is attacked by another entity.
	 */
	public void onPlayerDefend(Player player, LivingEntity attacker) {
		if (assignedTendency == EnumBloodTendency.FLAMMEUS && tier >= 3) {
			attacker.setSecondsOnFire(4);
		}
		if (assignedTendency == EnumBloodTendency.FERRIC && tier >= 1) {
			attacker.hurt(player.damageSources().thorns(player), tier);
		}
	}

	/**
	 * Called when a player wearing this rune kills another entity.
	 */
	public void onPlayerKill(Player player, LivingEntity killed) {
		if (assignedTendency == EnumBloodTendency.ANIMUS) {
			player.heal(tier);
		}
	}

	public float getDeepenAmount() {
		return deepenAmount;
	}

	public void setDeepenAmount(int deepenAmount) {
		this.deepenAmount = deepenAmount;
	}

	public EnumBloodTendency getAssignedTendency() {
		return assignedTendency;
	}

	public void setAssignedTendency(EnumBloodTendency assignedTendency) {
		this.assignedTendency = assignedTendency;
	}

	public int getTier() {
		return tier;
	}

	public List<RuneModifier> getPassiveModifiers() {
		return passiveModifiers;
	}

	public List<RuneEffectEntry> getPassiveEffects() {
		return passiveEffects;
	}

	@Override
	public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
	}

	@Override
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);

		if (tier > 0) {
			String tierStr = tier == 1 ? "I" : tier == 2 ? "II" : "III";
			tooltip.add(Component.literal("Tier " + tierStr).withStyle(ChatFormatting.DARK_PURPLE));
		}

		tooltip.add(Component
				.translatable(ChatFormatting.GOLD + "Tendency: " + HLTextUtils.toProperCase(assignedTendency.name())));
		tooltip.add(Component.translatable(ChatFormatting.GREEN + "Tendency Amount: " + deepenAmount));

		for (RuneModifier mod : passiveModifiers) {
			String sign = mod.amount() > 0 ? "+" : "";
			String valueStr;
			if (mod.operation() == AttributeModifier.Operation.MULTIPLY_TOTAL) {
				valueStr = sign + String.format("%.0f%%", mod.amount() * 100);
			} else {
				valueStr = sign + String.format("%.0f", mod.amount());
			}
			tooltip.add(Component.literal(valueStr + " ")
					.append(Component.translatable(mod.attribute().getDescriptionId()))
					.withStyle(ChatFormatting.BLUE));
		}

		for (RuneEffectEntry eff : passiveEffects) {
			tooltip.add(Component.translatable(eff.effect().getDescriptionId())
					.withStyle(ChatFormatting.AQUA));
		}

		if (assignedTendency == EnumBloodTendency.FERRIC && tier >= 1) {
			tooltip.add(Component.literal("Thorns: reflects " + tier + " damage")
					.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
		}
		if (assignedTendency == EnumBloodTendency.ANIMUS && tier >= 1) {
			tooltip.add(Component.literal("Heals " + tier + " on kill")
					.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
		}
		if (assignedTendency == EnumBloodTendency.MORTEM && tier >= 3) {
			tooltip.add(Component.literal("Withers struck foes")
					.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
		} else if (assignedTendency == EnumBloodTendency.MORTEM && tier >= 2) {
			tooltip.add(Component.literal("Poisons struck foes")
					.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
		}
		if (assignedTendency == EnumBloodTendency.FLAMMEUS && tier >= 3) {
			tooltip.add(Component.literal("Ignites attackers")
					.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
		}
		if (tier >= 3) {
			switch (assignedTendency) {
			case CONGEATIO:
				tooltip.add(Component.literal("Slows nearby foes")
						.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
				break;
			case TENEBRIS:
				tooltip.add(Component.literal("Grants invisibility in darkness")
						.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
				break;
			case ANIMUS:
				tooltip.add(Component.literal("Regenerates when wounded")
						.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
				break;
			default:
				break;
			}
		}
	}

	@Override
	public RuneType getRuneType() {
		return RuneType.RUNE;
	}

}
