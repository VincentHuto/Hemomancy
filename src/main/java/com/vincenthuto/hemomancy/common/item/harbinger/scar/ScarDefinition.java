package com.vincenthuto.hemomancy.common.item.harbinger.scar;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.ScarType;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.IBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.init.SkillPointInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodTendencyServerPacket;
import com.vincenthuto.hemomancy.common.util.CrimsonFireHelper;
import com.vincenthuto.hutoslib.client.HLTextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

public class ScarDefinition {
	private final ScarType scarType;
	private EnumBloodTendency assignedTendency;
	private float deepenAmount;
	private final int tier;
	private final List<ScarModifier> passiveModifiers = new ArrayList<>();
	private final List<ScarEffectEntry> passiveEffects = new ArrayList<>();
	private double bloodUpkeep = 0.0;
	private double maxBloodModifier = 0.0;
	private int ironHeartCapacityBonus;

	public record ScarModifier(Holder<Attribute> attribute, ResourceLocation id, double amount,
			AttributeModifier.Operation operation) {
	}

	public record ScarEffectEntry(Holder<MobEffect> effect, int amplifier) {
	}

	public ScarDefinition(ScarType scarType, EnumBloodTendency tendencyIn, float deepenAmountIn, int tier) {
		this.scarType = scarType;
		this.assignedTendency = tendencyIn;
		this.deepenAmount = deepenAmountIn;
		this.tier = tier;
	}

	public ScarDefinition withModifier(Holder<Attribute> attribute, String name, double amount,
	                                   AttributeModifier.Operation operation) {
		this.passiveModifiers.add(new ScarModifier(attribute, Hemomancy.rloc(name), amount, operation));
		return this;
	}

	public ScarDefinition withEffect(Holder<MobEffect> effect, int amplifier) {
		this.passiveEffects.add(new ScarEffectEntry(effect, amplifier));
		return this;
	}

	public ScarDefinition withBloodUpkeep(double drainPerTick) {
		this.bloodUpkeep = drainPerTick;
		return this;
	}

	public ScarDefinition withMaxBloodModifier(double delta) {
		this.maxBloodModifier = delta;
		return this;
	}

	public ScarDefinition withIronHeartCapacityBonus(int hearts) {
		this.ironHeartCapacityBonus = Math.max(0, hearts);
		return this;
	}

	public void onEquipped(LivingEntity player, ItemStack stack) {
		if (player instanceof Player && !player.getCommandSenderWorld().isClientSide) {
			IBloodTendency coven = HemoCapabilityAccess.getBloodTendency(player)
					.orElseThrow(IllegalArgumentException::new);
			if (coven != null) {
				coven.addTransientAlignment(getAssignedTendency(), getDeepenAmount());
				PacketHandler.sendToPlayer((ServerPlayer) player, new BloodTendencyServerPacket(coven.getTendency()));
			}

			for (ScarModifier mod : passiveModifiers) {
				AttributeInstance attr = player.getAttribute(mod.attribute());
				if (attr != null && attr.getModifier(mod.id()) == null) {
					attr.addPermanentModifier(new AttributeModifier(mod.id(), mod.amount(), mod.operation()));
				}
			}

			for (ScarEffectEntry eff : passiveEffects) {
				player.addEffect(new MobEffectInstance(eff.effect(), -1, eff.amplifier(), true, false));
			}

		}
	}

	public void onUnequipped(LivingEntity player, ItemStack stack) {
		if (player instanceof Player && !player.getCommandSenderWorld().isClientSide) {
			IBloodTendency coven = HemoCapabilityAccess.getBloodTendency(player)
					.orElseThrow(IllegalArgumentException::new);
			if (coven != null) {
				coven.addTransientAlignment(getAssignedTendency(), -getDeepenAmount());
				PacketHandler.sendToPlayer((ServerPlayer) player, new BloodTendencyServerPacket(coven.getTendency()));
			}

			for (ScarModifier mod : passiveModifiers) {
				AttributeInstance attr = player.getAttribute(mod.attribute());
				if (attr != null) {
					attr.removeModifier(mod.id());
				}
			}

			for (ScarEffectEntry eff : passiveEffects) {
				if (!hasOtherScarWithEffect(player, eff.effect())) {
					player.removeEffect(eff.effect());
				}
			}

		}
	}

	private boolean hasOtherScarWithEffect(LivingEntity player, Holder<MobEffect> effect) {
		if (!(player instanceof Player p)) {
			return false;
		}
		return HemoCapabilityAccess.getScarState(p).map(scars -> {
			final boolean[] found = { false };
			scars.forEachActiveCerebralScar(otherScar -> {
				if (otherScar != this && otherScar.hasEffect(effect)) {
					found[0] = true;
				}
			});
			ItemStack fungal = scars.getFungalScar();
			if (!found[0] && fungal.getItem() instanceof ItemScar scar && scar.getScarDefinition() != this
					&& scar.getScarDefinition().hasEffect(effect)) {
				found[0] = true;
			}
			return found[0];
		}).orElse(false);
	}

	private boolean hasEffect(Holder<MobEffect> effect) {
		for (ScarEffectEntry entry : passiveEffects) {
			if (entry.effect().equals(effect)) {
				return true;
			}
		}
		return false;
	}

	public void onWornTick(LivingEntity entity, ItemStack stack) {
		if (entity == null || entity.level().isClientSide) {
			return;
		}
		if (bloodUpkeep > 0) {
			double effectiveUpkeep = bloodUpkeep * (entity instanceof Player player
					&& SkillPointHelper.isTechniqueEnabled(player, SkillPointInit.skill_deep_scar_resonance)
					? 1.5D : 1.0D);
			HemoCapabilityAccess.getBloodVolume(entity).ifPresent(v -> {
				if (v.isActive()) {
					v.drain(effectiveUpkeep);
				}
			});
		}
		applyTierThreeTickEffect(entity);
	}

	protected void applyTierThreeTickEffect(LivingEntity entity) {
		double masteryMult = entity instanceof Player player ? scarEffectDurationMultiplier(player) : 1.0;
		switch (assignedTendency) {
		case CONGEATIO:
			if (tier >= 2 && entity.tickCount % 40 == 0) {
				int dur = (int) (60 * masteryMult);
				AABB area = entity.getBoundingBox().inflate(5.0);
				entity.level().getEntitiesOfClass(Monster.class, area)
						.forEach(mob -> mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, dur, 0)));
			}
			if (tier >= 3 && entity.tickCount % 40 == 0) {
				entity.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING,
						(int) (45 * masteryMult), 0, true, false));
			}
			break;
		case TENEBRIS:
			if (entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) < 4) {
				entity.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY,
						(int) (40 * masteryMult), 0, true, false));
			}
			break;
		case ANIMUS:
			if (tier >= 3 && entity.getHealth() < entity.getMaxHealth() * 0.5f && entity.tickCount % 60 == 0) {
				entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION,
						(int) (80 * masteryMult), 0, true, false));
			}
			break;
		case LUX:
			if (tier >= 3 && entity.tickCount % 40 == 0
					&& entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) >= 12) {
				entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,
						(int) (60 * masteryMult), 0, true, false));
			}
			break;
		default:
			break;
		}
	}

	public void onPlayerAttack(Player player, LivingEntity target) {
		double masteryMult = scarEffectDurationMultiplier(player);
		if (assignedTendency == EnumBloodTendency.MORTEM) {
			if (tier >= 3) {
				target.addEffect(new MobEffectInstance(MobEffects.WITHER, (int) (80 * masteryMult), 1));
			} else if (tier >= 2) {
				target.addEffect(new MobEffectInstance(MobEffects.POISON, (int) (60 * masteryMult), 0));
			}
		}
		if (assignedTendency == EnumBloodTendency.CONGEATIO) {
			target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
					(int) (30 * tier * masteryMult), 0));
		}
	}

	public void onPlayerDefend(Player player, LivingEntity attacker) {
		double masteryMult = scarEffectDurationMultiplier(player);
		if (assignedTendency == EnumBloodTendency.FLAMMEUS && tier >= 2) {
			CrimsonFireHelper.igniteCrimson(attacker, tier >= 3 ? 4.0F : 2.0F);
		}
		if (assignedTendency == EnumBloodTendency.FERRIC && tier >= 1) {
			attacker.hurt(player.damageSources().thorns(player), tier);
		}
		if (assignedTendency == EnumBloodTendency.LUX) {
			int blindDur = (int) ((tier >= 2 ? 60 : 40) * masteryMult);
			attacker.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, blindDur, 0));
			if (tier >= 2) {
				attacker.addEffect(new MobEffectInstance(MobEffects.GLOWING, (int) (200 * masteryMult), 0));
			}
		}
		if (assignedTendency == EnumBloodTendency.TENEBRIS) {
			int light = player.level().getBrightness(LightLayer.BLOCK, player.blockPosition());
			if (tier >= 3 || (tier >= 2 && light < 7)) {
				int dur = (int) ((tier >= 3 ? 80 : 60) * masteryMult);
				player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, dur, 0, true, false));
			}
		}
	}

	public void onPlayerKill(Player player, LivingEntity killed) {
		double masteryMult = scarEffectDurationMultiplier(player);
		if (assignedTendency == EnumBloodTendency.ANIMUS && tier >= 2) {
			player.heal(tier);
		}
		if (assignedTendency == EnumBloodTendency.MORTEM && tier == 1) {
			player.addEffect(new MobEffectInstance(MobEffects.POISON, (int) (30 * masteryMult), 0));
		}
		if (assignedTendency == EnumBloodTendency.DUCTILIS && tier >= 2) {
			int dur = (int) (80 * tier * masteryMult);
			player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, dur, 0, true, true));
			if (tier >= 3) {
				player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,
						(int) (60 * tier * masteryMult), 0, true, true));
				player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST,
						(int) (60 * tier * masteryMult), 0, true, true));
			}
		}
	}

	public void appendHoverText(List<Component> tooltip) {
		if (tier > 0) {
			String tierStr = tier == 1 ? "I" : tier == 2 ? "II" : "III";
			tooltip.add(Component.literal("Tier " + tierStr).withStyle(ChatFormatting.DARK_PURPLE));
		}

		tooltip.add(Component.translatable(ChatFormatting.GOLD + "Tendency: "
				+ HLTextUtils.toProperCase(assignedTendency.name())));
		tooltip.add(Component.translatable(ChatFormatting.GREEN + "Tendency Amount: " + deepenAmount));

		for (ScarModifier mod : passiveModifiers) {
			if (mod.amount() <= 0) continue;
			String valueStr = mod.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
					? "+" + String.format("%.0f%%", mod.amount() * 100)
					: "+" + String.format("%.0f", mod.amount());
			tooltip.add(Component.literal("* " + valueStr + " ")
					.append(Component.translatable(mod.attribute().value().getDescriptionId()))
					.withStyle(ChatFormatting.DARK_GREEN));
		}

		for (ScarEffectEntry eff : passiveEffects) {
			tooltip.add(Component.literal("* ").append(Component.translatable(eff.effect().value().getDescriptionId()))
					.withStyle(ChatFormatting.DARK_GREEN));
		}

		addBehaviorTooltip(tooltip);

		for (ScarModifier mod : passiveModifiers) {
			if (mod.amount() >= 0) continue;
			String valueStr = mod.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
					? String.format("%.0f%%", mod.amount() * 100)
					: String.format("%.0f", mod.amount());
			tooltip.add(Component.literal("* " + valueStr + " ")
					.append(Component.translatable(mod.attribute().value().getDescriptionId()))
					.withStyle(ChatFormatting.RED));
		}

		if (maxBloodModifier != 0.0) {
			String sign = maxBloodModifier > 0 ? "+" : "";
			ChatFormatting color = maxBloodModifier > 0 ? ChatFormatting.DARK_GREEN : ChatFormatting.RED;
			tooltip.add(Component.literal("* " + sign + String.format("%.0f", maxBloodModifier) + " Max Blood")
					.withStyle(color));
		}

		if (bloodUpkeep > 0) {
			tooltip.add(Component.literal("* Blood Upkeep: " + String.format("%.1f", bloodUpkeep) + "/tick")
					.withStyle(ChatFormatting.RED));
		}

		if (assignedTendency == EnumBloodTendency.MORTEM && tier == 1) {
			tooltip.add(Component.literal("* Poison backtracks on kill").withStyle(ChatFormatting.RED));
		}
	}

	private void addBehaviorTooltip(List<Component> tooltip) {
		if (ironHeartCapacityBonus > 0) {
			tooltip.add(Component.literal("* +" + ironHeartCapacityBonus + " Iron Heart capacity")
					.withStyle(ChatFormatting.DARK_GREEN));
		}
		if (assignedTendency == EnumBloodTendency.ANIMUS && tier >= 2) {
			tooltip.add(Component.literal("* Heals " + tier + " on kill").withStyle(ChatFormatting.DARK_GREEN));
		}
		if (assignedTendency == EnumBloodTendency.MORTEM && tier >= 3) {
			tooltip.add(Component.literal("* Withers struck foes").withStyle(ChatFormatting.DARK_GREEN));
		} else if (assignedTendency == EnumBloodTendency.MORTEM && tier >= 2) {
			tooltip.add(Component.literal("* Poisons struck foes").withStyle(ChatFormatting.DARK_GREEN));
		}
		if (assignedTendency == EnumBloodTendency.FLAMMEUS && tier >= 2) {
			tooltip.add(Component.literal(tier >= 3 ? "* Ignites attackers" : "* Briefly ignites attackers")
					.withStyle(ChatFormatting.DARK_GREEN));
		}
		if (assignedTendency == EnumBloodTendency.FERRIC && tier >= 1) {
			tooltip.add(Component.literal("* Thorns: reflects " + tier + " damage")
					.withStyle(ChatFormatting.DARK_GREEN));
		}
		if (assignedTendency == EnumBloodTendency.LUX) {
			tooltip.add(Component.literal("* Blinds attackers").withStyle(ChatFormatting.DARK_GREEN));
			if (tier >= 2) {
				tooltip.add(Component.literal("* Marks attackers with Glowing").withStyle(ChatFormatting.DARK_GREEN));
			}
		}
		if (assignedTendency == EnumBloodTendency.CONGEATIO) {
			tooltip.add(Component.literal("* Slows struck foes").withStyle(ChatFormatting.DARK_GREEN));
			if (tier >= 2) tooltip.add(Component.literal("* Slows nearby foes").withStyle(ChatFormatting.DARK_GREEN));
			if (tier >= 3) tooltip.add(Component.literal("* Slow fall").withStyle(ChatFormatting.DARK_GREEN));
		}
		if (assignedTendency == EnumBloodTendency.TENEBRIS) {
			tooltip.add(Component.literal("* Grants invisibility in darkness").withStyle(ChatFormatting.DARK_GREEN));
			if (tier >= 2) {
				tooltip.add(Component.literal("* Grants invisibility when struck in darkness")
						.withStyle(ChatFormatting.DARK_GREEN));
			}
			if (tier >= 3) {
				tooltip.add(Component.literal("* Grants invisibility when struck").withStyle(ChatFormatting.DARK_GREEN));
			}
		}
		if (assignedTendency == EnumBloodTendency.DUCTILIS && tier >= 2) {
			tooltip.add(Component.literal("* Grants Haste on kill").withStyle(ChatFormatting.DARK_GREEN));
			if (tier >= 3) {
				tooltip.add(Component.literal("* Grants Speed and Strength on kill")
						.withStyle(ChatFormatting.DARK_GREEN));
			}
		}
		if (assignedTendency == EnumBloodTendency.ANIMUS && tier >= 3) {
			tooltip.add(Component.literal("* Regenerates when gravely wounded").withStyle(ChatFormatting.DARK_GREEN));
		}
		if (assignedTendency == EnumBloodTendency.LUX && tier >= 3) {
			tooltip.add(Component.literal("* Grants Resistance in bright light").withStyle(ChatFormatting.DARK_GREEN));
		}
	}

	public ScarType getScarType() {
		return scarType;
	}

	public EnumBloodTendency getAssignedTendency() {
		return assignedTendency;
	}

	public void setAssignedTendency(EnumBloodTendency assignedTendency) {
		this.assignedTendency = assignedTendency;
	}

	public float getDeepenAmount() {
		return deepenAmount;
	}

	public void setDeepenAmount(float deepenAmount) {
		this.deepenAmount = deepenAmount;
	}

	public int getTier() {
		return tier;
	}

	public double getMaxBloodModifier() {
		return maxBloodModifier;
	}

	public int getIronHeartCapacityBonus() {
		return ironHeartCapacityBonus;
	}

	public List<ScarModifier> getPassiveModifiers() {
		return passiveModifiers;
	}

	public List<ScarEffectEntry> getPassiveEffects() {
		return passiveEffects;
	}

	protected double scarEffectDurationMultiplier(Player player) {
		double multiplier = SkillPointHelper.getScarMasteryDurationMultiplier(player);
		if (tier >= 3) {
			multiplier *= 1.0 + SkillPointHelper.getDeepInscriptionLevel(player) * 0.05;
		}
		return multiplier;
	}
}
