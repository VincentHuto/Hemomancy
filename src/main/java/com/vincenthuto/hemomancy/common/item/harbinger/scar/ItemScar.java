package com.vincenthuto.hemomancy.common.item.harbinger.scar;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.IBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.IScar;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.ScarType;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodTendencyServerPacket;
import com.vincenthuto.hutoslib.client.HLTextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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

import java.util.ArrayList;
import java.util.List;

public class ItemScar extends Item implements IScar {

	EnumBloodTendency assignedTendency;
	float deepenAmount;
	int tier;

	private final List<ScarModifier> passiveModifiers = new ArrayList<>();
	private final List<ScarEffectEntry> passiveEffects = new ArrayList<>();

	/** Passive blood drained per worn tick (server-side). Zero means no upkeep. */
	private double bloodUpkeep = 0.0;

	/** Delta applied to the player's max blood volume on equip (negative = smaller pool). */
	private double maxBloodModifier = 0.0;

	public record ScarModifier(Holder<Attribute> attribute, ResourceLocation id, double amount,
			AttributeModifier.Operation operation) {
	}

	public record ScarEffectEntry(Holder<MobEffect> effect, int amplifier) {
	}

	/** Legacy constructor for backward compatibility (tier defaults to 0). */
	public ItemScar(Properties properties, EnumBloodTendency tendencyIn, float deepenAmountIn) {
		this(properties, tendencyIn, deepenAmountIn, 0);
	}

	public ItemScar(Properties properties, EnumBloodTendency tendencyIn, float deepenAmountIn, int tier) {
		super(properties);
		this.assignedTendency = tendencyIn;
		this.deepenAmount = deepenAmountIn;
		this.tier = tier;
	}

	/** Adds a persistent attribute modifier applied on equip and removed on unequip. */
	public ItemScar withModifier(Holder<Attribute> attribute, String name, double amount,
			AttributeModifier.Operation operation) {
		this.passiveModifiers.add(new ScarModifier(attribute, Hemomancy.rloc(name), amount, operation));
		return this;
	}

	/** Adds a persistent mob effect granted while the scar is equipped. */
	public ItemScar withEffect(Holder<MobEffect> effect, int amplifier) {
		this.passiveEffects.add(new ScarEffectEntry(effect, amplifier));
		return this;
	}

	/**
	 * Sets the passive blood drained from the player's blood pool each worn tick.
	 * Represents the physiological cost of maintaining the scar's power (e.g. veins
	 * running hot, the eyes always watching). Only drains when blood is active.
	 */
	public ItemScar withBloodUpkeep(double drainPerTick) {
		this.bloodUpkeep = drainPerTick;
		return this;
	}

	/**
	 * Adds a delta to the player's maximum blood volume while the scar is equipped.
	 * Positive values expand the pool; negative values constrict it, representing
	 * the scar's claim on the circulatory system.
	 */
	public ItemScar withMaxBloodModifier(double delta) {
		this.maxBloodModifier = delta;
		return this;
	}

	@Override
	public void onEquipped(LivingEntity player) {
		if (player instanceof Player) {
			if (!player.getCommandSenderWorld().isClientSide) {
				IBloodTendency coven = HemoCapabilityAccess.getBloodTendency(player)
						.orElseThrow(IllegalArgumentException::new);
				if (coven != null) {
					coven.setTendencyAlignment(getAssignedTendency(), getDeepenAmount());
					Player playerEnt = (Player) player;
					PacketHandler.sendToPlayer((ServerPlayer) playerEnt, new BloodTendencyServerPacket(coven.getTendency()));
				}

				for (ScarModifier mod : passiveModifiers) {
					AttributeInstance attr = player.getAttribute(mod.attribute());
					if (attr != null && attr.getModifier(mod.id()) == null) {
						attr.addPermanentModifier(
								new AttributeModifier(mod.id(), mod.amount(), mod.operation()));
					}
				}

				for (ScarEffectEntry eff : passiveEffects) {
					player.addEffect(new MobEffectInstance(eff.effect(), -1, eff.amplifier(), true, false));
				}

				if (maxBloodModifier != 0.0) {
					HemoCapabilityAccess.getBloodVolume(player).ifPresent(v -> {
						if (v.isActive()) {
							double absAmount = Math.abs(maxBloodModifier);
							if (maxBloodModifier > 0) {
								v.addMaxBloodVolume(absAmount);
							} else {
								v.subtractMaxBloodVolume(absAmount);
							}
						}
					});
				}
			}
		}
	}

	@Override
	public void onUnequipped(LivingEntity player) {
		if (player instanceof Player) {
			if (!player.getCommandSenderWorld().isClientSide) {
				IBloodTendency coven = HemoCapabilityAccess.getBloodTendency(player)
						.orElseThrow(IllegalArgumentException::new);
				if (coven != null) {
					coven.setTendencyAlignment(getAssignedTendency(), -getDeepenAmount());
					Player playerEnt = (Player) player;
					PacketHandler.sendToPlayer((ServerPlayer) playerEnt, new BloodTendencyServerPacket(coven.getTendency()));
				}

				for (ScarModifier mod : passiveModifiers) {
					AttributeInstance attr = player.getAttribute(mod.attribute());
					if (attr != null) {
						attr.removeModifier(mod.id());
					}
				}

				for (ScarEffectEntry eff : passiveEffects) {
					if (!hasotherScarWithEffect(player, eff.effect())) {
						player.removeEffect(eff.effect());
					}
				}

				if (maxBloodModifier != 0.0) {
					HemoCapabilityAccess.getBloodVolume(player).ifPresent(v -> {
						if (v.isActive()) {
							// Reverse the equip operation
							double absAmount = Math.abs(maxBloodModifier);
							if (maxBloodModifier > 0) {
								v.subtractMaxBloodVolume(absAmount);
							} else {
								v.addMaxBloodVolume(absAmount);
							}
						}
					});
				}
			}
		}
	}

	private boolean hasotherScarWithEffect(LivingEntity player, Holder<MobEffect> effect) {
		if (!(player instanceof net.minecraft.world.entity.player.Player p)) return false;
		return HemoCapabilityAccess.getScars(p).map(scars -> {
			for (int i = 0; i < scars.getSlots(); i++) {
				ItemStack stack = scars.getStackInSlot(i);
				if (stack.getItem() instanceof ItemScar otherScar) {
					for (ScarEffectEntry otherEff : otherScar.passiveEffects) {
						if (otherEff.effect().equals(effect))
							return true;
					}
				}
			}
			return false;
		}).orElse(false);
	}

	@Override
	public void onWornTick(LivingEntity entity) {
		if (entity == null) {
			return;
		}
		if (!entity.level().isClientSide) {
			if (bloodUpkeep > 0) {
				HemoCapabilityAccess.getBloodVolume(entity).ifPresent(v -> {
					if (v.isActive()) v.drain(bloodUpkeep);
				});
			}
			applyTierThreeTickEffect(entity);
		}
	}

	protected void applyTierThreeTickEffect(LivingEntity entity) {
		double masteryMult = (entity instanceof Player player) ? scarEffectDurationMultiplier(player) : 1.0;
		switch (assignedTendency) {
		case CONGEATIO:
			// T2+ Glacier: slow nearby monsters every 2 seconds
			if (tier >= 2 && entity.tickCount % 40 == 0) {
				int dur = (int)(60 * masteryMult);
				AABB area = entity.getBoundingBox().inflate(5.0);
				entity.level().getEntitiesOfClass(Monster.class, area)
						.forEach(mob -> mob.addEffect(
								new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, dur, 0)));
			}
			// T3 Descendence: passive slow-fall
			if (tier >= 3 && entity.tickCount % 40 == 0) {
				entity.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING,
						(int)(45 * masteryMult), 0, true, false));
			}
			break;
		case TENEBRIS:
			// All TENEBRIS tiers: invisibility while standing in darkness
			if (entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) < 4) {
				entity.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, (int)(40 * masteryMult), 0, true, false));
			}
			break;
		case ANIMUS:
			// T3 Phoenix: emergency regeneration when badly wounded
			if (tier >= 3 && entity.getHealth() < entity.getMaxHealth() * 0.5f && entity.tickCount % 60 == 0) {
				entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, (int)(80 * masteryMult), 0, true, false));
			}
			break;
		case LUX:
			// T3 Transcendence: damage resistance in bright sunlight
			if (tier >= 3 && entity.tickCount % 40 == 0
					&& entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) >= 12) {
				entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,
						(int)(60 * masteryMult), 0, true, false));
			}
			break;
		default:
			break;
		}
	}

	/**
	 * Called when a player wearing this scar attacks another entity.
	 */
	public void onPlayerAttack(Player player, LivingEntity target) {
		double masteryMult = scarEffectDurationMultiplier(player);
		if (assignedTendency == EnumBloodTendency.MORTEM) {
			if (tier >= 3) {
				target.addEffect(new MobEffectInstance(MobEffects.WITHER, (int)(80 * masteryMult), 1));
			} else if (tier >= 2) {
				target.addEffect(new MobEffectInstance(MobEffects.POISON, (int)(60 * masteryMult), 0));
			}
		}
		if (assignedTendency == EnumBloodTendency.CONGEATIO) {
			target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
					(int)(30 * tier * masteryMult), 0));
		}
	}

	/**
	 * Called when a player wearing this scar is attacked by another entity.
	 */
	public void onPlayerDefend(Player player, LivingEntity attacker) {
		double masteryMult = scarEffectDurationMultiplier(player);
		if (assignedTendency == EnumBloodTendency.FLAMMEUS && tier >= 2) {
			attacker.igniteForSeconds(tier >= 3 ? 4.0F : 2.0F);
		}
		if (assignedTendency == EnumBloodTendency.FERRIC && tier >= 1) {
			attacker.hurt(player.damageSources().thorns(player), tier);
		}
		if (assignedTendency == EnumBloodTendency.LUX) {
			int blindDur = (int)((tier >= 2 ? 60 : 40) * masteryMult);
			attacker.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, blindDur, 0));
			if (tier >= 2) {
				attacker.addEffect(new MobEffectInstance(MobEffects.GLOWING,
						(int)(200 * masteryMult), 0));
			}
		}
		if (assignedTendency == EnumBloodTendency.TENEBRIS) {
			int light = player.level().getBrightness(LightLayer.BLOCK, player.blockPosition());
			// T3 Eye: always grants invisibility when struck; T2 Moon: only in darkness
			if (tier >= 3 || (tier >= 2 && light < 7)) {
				int dur = (int)((tier >= 3 ? 80 : 60) * masteryMult);
				player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, dur, 0, true, false));
			}
		}
	}

	/**
	 * Called when a player wearing this scar kills another entity.
	 */
	public void onPlayerKill(Player player, LivingEntity killed) {
		double masteryMult = scarEffectDurationMultiplier(player);
		// ANIMUS: T2+ Marrow/Phoenix heal on kill
		if (assignedTendency == EnumBloodTendency.ANIMUS && tier >= 2) {
			player.heal(tier);
		}
		// MORTEM T1 Blight: the toxin backtracks — brief self-poison after a kill
		if (assignedTendency == EnumBloodTendency.MORTEM && tier == 1) {
			player.addEffect(new MobEffectInstance(MobEffects.POISON, (int)(30 * masteryMult), 0));
		}
		// DUCTILIS: T2+ Flux/Chimera kill chain
		if (assignedTendency == EnumBloodTendency.DUCTILIS && tier >= 2) {
			int dur = (int)(80 * tier * masteryMult);
			player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, dur, 0, true, true));
			if (tier >= 3) {
				player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,
						(int)(60 * tier * masteryMult), 0, true, true));
				player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST,
						(int)(60 * tier * masteryMult), 0, true, true));
			}
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

	private double scarEffectDurationMultiplier(Player player) {
		double multiplier = SkillPointHelper.getScarMasteryDurationMultiplier(player);
		if (tier >= 3) {
			multiplier *= 1.0 + SkillPointHelper.getDeepInscriptionLevel(player) * 0.05;
		}
		return multiplier;
	}

	public List<ScarModifier> getPassiveModifiers() {
		return passiveModifiers;
	}

	public List<ScarEffectEntry> getPassiveEffects() {
		return passiveEffects;
	}

	@Override
	public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, context, tooltip, flagIn);

		if (tier > 0) {
			String tierStr = tier == 1 ? "I" : tier == 2 ? "II" : "III";
			tooltip.add(Component.literal("Tier " + tierStr).withStyle(ChatFormatting.DARK_PURPLE));
		}

		tooltip.add(Component
				.translatable(ChatFormatting.GOLD + "Tendency: " + HLTextUtils.toProperCase(assignedTendency.name())));
		tooltip.add(Component.translatable(ChatFormatting.GREEN + "Tendency Amount: " + deepenAmount));

		// ── Upsides (positive modifiers and effects) ──
		for (ScarModifier mod : passiveModifiers) {
			if (mod.amount() <= 0) continue;
			String sign = "+";
			String valueStr;
			if (mod.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
				valueStr = sign + String.format("%.0f%%", mod.amount() * 100);
			} else {
				valueStr = sign + String.format("%.0f", mod.amount());
			}
			tooltip.add(Component.literal("✦ " + valueStr + " ")
					.append(Component.translatable(mod.attribute().value().getDescriptionId()))
					.withStyle(ChatFormatting.DARK_GREEN));
		}

		for (ScarEffectEntry eff : passiveEffects) {
			tooltip.add(Component.literal("✦ ").append(
					Component.translatable(eff.effect().value().getDescriptionId()))
					.withStyle(ChatFormatting.DARK_GREEN));
		}

		// Behavioral upside hints
		if (assignedTendency == EnumBloodTendency.ANIMUS && tier >= 2) {
			tooltip.add(Component.literal("✦ Heals " + tier + " on kill")
					.withStyle(ChatFormatting.DARK_GREEN));
		}
		if (assignedTendency == EnumBloodTendency.MORTEM && tier >= 3) {
			tooltip.add(Component.literal("✦ Withers struck foes")
					.withStyle(ChatFormatting.DARK_GREEN));
		} else if (assignedTendency == EnumBloodTendency.MORTEM && tier >= 2) {
			tooltip.add(Component.literal("✦ Poisons struck foes")
					.withStyle(ChatFormatting.DARK_GREEN));
		}
		if (assignedTendency == EnumBloodTendency.FLAMMEUS && tier >= 2) {
			tooltip.add(Component.literal(tier >= 3 ? "✦ Ignites attackers" : "✦ Briefly ignites attackers")
					.withStyle(ChatFormatting.DARK_GREEN));
		}
		if (assignedTendency == EnumBloodTendency.FERRIC && tier >= 1) {
			tooltip.add(Component.literal("✦ Thorns: reflects " + tier + " damage")
					.withStyle(ChatFormatting.DARK_GREEN));
		}
		if (assignedTendency == EnumBloodTendency.LUX) {
			tooltip.add(Component.literal("✦ Blinds attackers")
					.withStyle(ChatFormatting.DARK_GREEN));
			if (tier >= 2) {
				tooltip.add(Component.literal("✦ Marks attackers with Glowing")
						.withStyle(ChatFormatting.DARK_GREEN));
			}
		}
		if (assignedTendency == EnumBloodTendency.CONGEATIO) {
			tooltip.add(Component.literal("✦ Slows struck foes")
					.withStyle(ChatFormatting.DARK_GREEN));
			if (tier >= 2) {
				tooltip.add(Component.literal("✦ Slows nearby foes")
						.withStyle(ChatFormatting.DARK_GREEN));
			}
			if (tier >= 3) {
				tooltip.add(Component.literal("✦ Slow fall")
						.withStyle(ChatFormatting.DARK_GREEN));
			}
		}
		if (assignedTendency == EnumBloodTendency.TENEBRIS) {
			tooltip.add(Component.literal("✦ Grants invisibility in darkness")
					.withStyle(ChatFormatting.DARK_GREEN));
			if (tier >= 2) {
				tooltip.add(Component.literal("✦ Grants invisibility when struck in darkness")
						.withStyle(ChatFormatting.DARK_GREEN));
			}
			if (tier >= 3) {
				tooltip.add(Component.literal("✦ Grants invisibility when struck")
						.withStyle(ChatFormatting.DARK_GREEN));
			}
		}
		if (assignedTendency == EnumBloodTendency.DUCTILIS && tier >= 2) {
			tooltip.add(Component.literal("✦ Grants Haste on kill")
					.withStyle(ChatFormatting.DARK_GREEN));
			if (tier >= 3) {
				tooltip.add(Component.literal("✦ Grants Speed and Strength on kill")
						.withStyle(ChatFormatting.DARK_GREEN));
			}
		}
		if (assignedTendency == EnumBloodTendency.ANIMUS && tier >= 3) {
			tooltip.add(Component.literal("✦ Regenerates when gravely wounded")
					.withStyle(ChatFormatting.DARK_GREEN));
		}
		if (assignedTendency == EnumBloodTendency.LUX && tier >= 3) {
			tooltip.add(Component.literal("✦ Grants Resistance in bright light")
					.withStyle(ChatFormatting.DARK_GREEN));
		}

		// ── Downsides (negative modifiers and costs) ──
		for (ScarModifier mod : passiveModifiers) {
			if (mod.amount() >= 0) continue;
			String valueStr;
			if (mod.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
				valueStr = String.format("%.0f%%", mod.amount() * 100);
			} else {
				valueStr = String.format("%.0f", mod.amount());
			}
			tooltip.add(Component.literal("✦ " + valueStr + " ")
					.append(Component.translatable(mod.attribute().value().getDescriptionId()))
					.withStyle(ChatFormatting.RED));
		}

		if (maxBloodModifier != 0.0) {
			String sign = maxBloodModifier > 0 ? "+" : "";
			String line = sign + String.format("%.0f", maxBloodModifier) + " Max Blood";
			ChatFormatting color = maxBloodModifier > 0 ? ChatFormatting.DARK_GREEN : ChatFormatting.RED;
			tooltip.add(Component.literal("✦ " + line).withStyle(color));
		}

		if (bloodUpkeep > 0) {
			tooltip.add(Component.literal("✦ Blood Upkeep: " + String.format("%.1f", bloodUpkeep) + "/tick")
					.withStyle(ChatFormatting.RED));
		}

		if (assignedTendency == EnumBloodTendency.MORTEM && tier == 1) {
			tooltip.add(Component.literal("✦ Poison backtracks on kill")
					.withStyle(ChatFormatting.RED));
		}
	}

	@Override
	public ScarType getScarType() {
		return ScarType.SCAR;
	}

}
