package com.vincenthuto.hemomancy.common.capability.player.harbinger.scar;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.fungal.AfflictionDigestHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.fungal.ConserveStateHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.fungal.RootedStateHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.fungal.VeinMinerHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.bloodline.VasculariumCharmRules;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.fungal.SaprovittaVestigiumItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.level.BlockEvent.BreakEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Collection;
import java.util.EnumMap;
import java.util.List;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public class ScarsEntityEventHandler {

	private static final EnumMap<EnumBloodTendency, SynergyBonus> SYNERGY_BONUSES = new EnumMap<>(
			EnumBloodTendency.class);

	static {
		SYNERGY_BONUSES.put(EnumBloodTendency.ANIMUS, new SynergyBonus(Attributes.MAX_HEALTH,
				Hemomancy.rloc("synergy_animus"), 2.0, AttributeModifier.Operation.ADD_VALUE));
		SYNERGY_BONUSES.put(EnumBloodTendency.FLAMMEUS, new SynergyBonus(Attributes.ATTACK_DAMAGE,
				Hemomancy.rloc("synergy_flammeus"), 1.0, AttributeModifier.Operation.ADD_VALUE));
		SYNERGY_BONUSES.put(EnumBloodTendency.MORTEM, new SynergyBonus(Attributes.ATTACK_DAMAGE,
				Hemomancy.rloc("synergy_mortem"), 1.0, AttributeModifier.Operation.ADD_VALUE));
		SYNERGY_BONUSES.put(EnumBloodTendency.CONGEATIO, new SynergyBonus(Attributes.MOVEMENT_SPEED,
				Hemomancy.rloc("synergy_congeatio"), 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
		SYNERGY_BONUSES.put(EnumBloodTendency.DUCTILIS, new SynergyBonus(Attributes.ATTACK_SPEED,
				Hemomancy.rloc("synergy_ductilis"), 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
		SYNERGY_BONUSES.put(EnumBloodTendency.LUX, new SynergyBonus(Attributes.ARMOR_TOUGHNESS,
				Hemomancy.rloc("synergy_lux"), 1.0, AttributeModifier.Operation.ADD_VALUE));
		SYNERGY_BONUSES.put(EnumBloodTendency.FERRIC, new SynergyBonus(Attributes.ARMOR,
				Hemomancy.rloc("synergy_ferric"), 1.0, AttributeModifier.Operation.ADD_VALUE));
		SYNERGY_BONUSES.put(EnumBloodTendency.TENEBRIS, new SynergyBonus(Attributes.MOVEMENT_SPEED,
				Hemomancy.rloc("synergy_tenebris"), 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
	}

	private static void dropItemsAt(Player player, Collection<ItemEntity> drops) {
		HemoCapabilityAccess.getEquipment(player).ifPresent(equipment -> {
			for (int i = 0; i < equipment.getSlots(); ++i) {
				ItemStack stack = equipment.getStackInSlot(i);
				if (!stack.isEmpty() && VasculariumCharmRules.shouldDropEquippedSlot(
						stack.is(ItemInit.charm_of_vascularium.get()))) {
					ItemEntity ei = new ItemEntity(player.level(), player.getX(), player.getY() + player.getEyeHeight(),
							player.getZ(), stack.copy());
					ei.setPickUpDelay(40);
					drops.add(ei);
					equipment.setStackInSlot(i, ItemStack.EMPTY);
				}
			}
		});
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void playerTickScars(PlayerTickEvent.Post event) {
		Player player = event.getEntity();

		HemoCapabilityAccess.getScarState(player).ifPresent(IScars::tick);

		if (!player.level().isClientSide && player.tickCount % 20 == 0) {
			checkScarSynergy(player);
		}

		if (!player.level().isClientSide) {
			HemoCapabilityAccess.getScarState(player).ifPresent(scars -> {
				ItemStack fungalSlot = scars.getFungalScar();
				RootedStateHelper.tick(player, fungalSlot);
				AfflictionDigestHelper.tick(player, fungalSlot);
				ConserveStateHelper.tick(player, fungalSlot);
			});
		}

		if (!player.level().isClientSide && player.tickCount % SaprovittaVestigiumItem.TRAIL_INTERVAL_TICKS == 0) {
			HemoCapabilityAccess.getScarState(player).ifPresent(scars -> {
				ItemStack fungalSlot = scars.getFungalScar();
				if (fungalSlot.getItem() instanceof SaprovittaVestigiumItem) {
					double movSq = player.getDeltaMovement().horizontalDistanceSqr();
					if (movSq > SaprovittaVestigiumItem.MOVEMENT_THRESHOLD_SQ) {
						int symbiosis = SkillPointHelper.getFungalSymbiosisLevel(player);
						AABB area = player.getBoundingBox().inflate(SaprovittaVestigiumItem.TRAIL_RADIUS
								+ symbiosis * 0.4D);
						float damage = SaprovittaVestigiumItem.TRAIL_DAMAGE * (1.0F + symbiosis * 0.08F);
						List<Monster> mobs = player.level().getEntitiesOfClass(Monster.class, area);
						for (Monster mob : mobs) {
							mob.hurt(player.damageSources().magic(), damage);
						}
					}
				}
			});
		}
	}

	@SubscribeEvent
	public static void onLivingHurt(LivingDamageEvent.Pre event) {
		LivingEntity harmed = event.getEntity();
		Entity sourceEntity = event.getContainer().getSource().getEntity();

		if (sourceEntity instanceof Player player && !player.level().isClientSide) {
			HemoCapabilityAccess.getScarState(player).ifPresent(scars ->
					scars.forEachActiveCerebralScar(scar -> scar.onPlayerAttack(player, harmed)));
		}

		if (harmed instanceof Player player && !player.level().isClientSide && sourceEntity instanceof LivingEntity attacker) {
			HemoCapabilityAccess.getScarState(player).ifPresent(scars ->
					scars.forEachActiveCerebralScar(scar -> scar.onPlayerDefend(player, attacker)));
		}
	}

	@SubscribeEvent
	public static void onEntityKilledByPlayer(LivingDeathEvent event) {
		if (event.getSource().getEntity() instanceof Player player && !player.level().isClientSide) {
			LivingEntity killed = event.getEntity();
			HemoCapabilityAccess.getScarState(player).ifPresent(scars ->
					scars.forEachActiveCerebralScar(scar -> scar.onPlayerKill(player, killed)));
		}
	}

	private static void checkScarSynergy(Player player) {
		HemoCapabilityAccess.getScarState(player).ifPresent(scars -> {
			EnumMap<EnumBloodTendency, Integer> counts = new EnumMap<>(EnumBloodTendency.class);
			scars.forEachActiveCerebralScar(scar -> counts.merge(scar.getAssignedTendency(), 1, Integer::sum));

			for (EnumBloodTendency tendency : EnumBloodTendency.values()) {
				SynergyBonus bonus = SYNERGY_BONUSES.get(tendency);
				if (bonus == null) {
					continue;
				}

				AttributeInstance attr = player.getAttribute(bonus.attribute());
				if (attr == null) {
					continue;
				}

				boolean hasSynergy = counts.getOrDefault(tendency, 0) >= 2;
				boolean hasModifier = attr.getModifier(bonus.modifierId()) != null;
				double scaledAmount = bonus.amount() * SkillPointHelper.getScarAffinityMultiplier(player);

				if (hasSynergy) {
					if (hasModifier) {
						attr.removeModifier(bonus.modifierId());
					}
					attr.addPermanentModifier(new AttributeModifier(
							bonus.modifierId(), scaledAmount, bonus.operation()));
				} else if (hasModifier) {
					attr.removeModifier(bonus.modifierId());
				}
			}
		});
	}

	@SubscribeEvent
	public static void onBlockBreak(BreakEvent event) {
		HemoCapabilityAccess.getScarState(event.getPlayer()).ifPresent(scars -> {
			HemoCapabilityAccess.getKnownManipulations(event.getPlayer()).ifPresent(manips -> {
				if (scars.getFungalScar().getItem() == ItemInit.talaromyces_minus.get()
						&& event.getPlayer().isShiftKeyDown()) {
					if (manips.getLastVeinMineStart() == BlockPos.ZERO && event.getState().is(Tags.Blocks.ORES)) {
						VeinMinerHelper.tryVeinMine(event.getPlayer().getMainHandItem(), event.getPlayer(),
								event.getPos());
					}
				}
			});
		});
	}

	private record SynergyBonus(Holder<Attribute> attribute, ResourceLocation modifierId, double amount,
			AttributeModifier.Operation operation) {
	}
}
