package com.vincenthuto.hemomancy.common.init;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumClarityStage;
import com.vincenthuto.hemomancy.common.manipulation.stillarts.StillArt;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Comparator;
import java.util.List;

public class StillArtInit {
	public static final ResourceKey<Registry<StillArt>> STILL_ART_KEY = ResourceKey
			.createRegistryKey(Hemomancy.rloc("still_arts"));

	public static final DeferredRegister<StillArt> STILL_ARTS = DeferredRegister.create(STILL_ART_KEY,
			Hemomancy.MOD_ID);

	public static final Registry<StillArt> STILL_ARTS_TYPE_REGISTRY = STILL_ARTS
			.makeRegistry(builder -> builder.maxId(Integer.MAX_VALUE - 1)
					.defaultKey(Hemomancy.rloc("silver_rebuke")));

	public static final DeferredHolder<StillArt, StillArt> silver_rebuke = STILL_ARTS.register("silver_rebuke",
			() -> new StillArt("silver_rebuke", EnumClarityStage.AWAKENED, 200, (player, level, heldItem, pos, art) -> {
				AABB area = player.getBoundingBox().inflate(5.0);
				Vec3 look = player.getLookAngle().normalize();
				List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
						target -> target != player && target.isAlive() && isInFront(player, look, target, 0.25));
				for (LivingEntity target : targets) {
					Vec3 away = target.position().subtract(player.position()).normalize();
					target.push(away.x * 0.8, 0.25, away.z * 0.8);
					target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 0, false, true));
				}
				level.playSound(null, player.blockPosition(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.0f, 1.45f);
				level.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0, player.getZ(),
						24, 1.1, 0.6, 1.1, 0.03);
				return true;
			}));

	public static final DeferredHolder<StillArt, StillArt> lethean_mute = STILL_ARTS.register("lethean_mute",
			() -> new StillArt("lethean_mute", EnumClarityStage.AWAKENED, 320, (player, level, heldItem, pos, art) -> {
				AABB area = player.getBoundingBox().inflate(6.0);
				List<Monster> targets = level.getEntitiesOfClass(Monster.class, area, Monster::isAlive);
				targets.forEach(target -> {
					target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 1, false, true));
					target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0, false, true));
				});
				player.displayClientMessage(Component.literal("The air forgets how to answer blood.")
						.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), true);
				level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0f, 0.7f);
				return true;
			}));

	public static final DeferredHolder<StillArt, StillArt> still_pulse = STILL_ARTS.register("still_pulse",
			() -> new StillArt("still_pulse", EnumClarityStage.DISCERNING, 260, (player, level, heldItem, pos, art) -> {
				level.getEntitiesOfClass(Monster.class, player.getBoundingBox().inflate(7.0), Monster::isAlive)
						.forEach(target -> target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2, false, true)));
				player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 0, false, true));
				level.sendParticles(ParticleTypes.SNOWFLAKE, player.getX(), player.getY() + 0.5, player.getZ(),
						40, 2.5, 0.25, 2.5, 0.02);
				return true;
			}));

	public static final DeferredHolder<StillArt, StillArt> pale_diagnosis = STILL_ARTS.register("pale_diagnosis",
			() -> new StillArt("pale_diagnosis", EnumClarityStage.DISCERNING, 240, (player, level, heldItem, pos, art) -> {
				List<LivingEntity> targets = level.getNearbyEntities(LivingEntity.class, TargetingConditions.DEFAULT,
						player, player.getBoundingBox().inflate(18.0));
				int marked = 0;
				for (LivingEntity target : targets) {
					if (target == player || !target.isAlive()) continue;
					boolean bloodActive = target instanceof Player other
							&& HemoCapabilityAccess.getBloodVolume(other).map(v -> v.isActive()).orElse(false);
					if (bloodActive || target instanceof Monster
							|| target.hasEffect(MobEffects.POISON)
							|| target.hasEffect(MobEffects.WITHER)) {
						target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 160, 0, false, true));
						marked++;
					}
				}
				player.displayClientMessage(Component.literal("Pale Diagnosis marks " + marked + " suspect body(s).")
						.withStyle(ChatFormatting.AQUA), true);
				return true;
			}));

	public static final DeferredHolder<StillArt, StillArt> memory_shear = STILL_ARTS.register("memory_shear",
			() -> new StillArt("memory_shear", EnumClarityStage.VIGILANT, 300, (player, level, heldItem, pos, art) -> {
				Monster target = level.getEntitiesOfClass(Monster.class, player.getBoundingBox().inflate(10.0), Monster::isAlive)
						.stream().min(Comparator.comparingDouble(player::distanceToSqr)).orElse(null);
				if (target == null) {
					player.displayClientMessage(Component.literal("There is no hostile memory near enough to shear.")
							.withStyle(ChatFormatting.GRAY), true);
					return false;
				}
				target.setTarget(null);
				target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 160, 1, false, true));
				target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 0, false, true));
				level.sendParticles(ParticleTypes.WAX_OFF, target.getX(), target.getY() + 1.0, target.getZ(),
						20, 0.4, 0.6, 0.4, 0.02);
				return true;
			}));

	public static final DeferredHolder<StillArt, StillArt> absolving_step = STILL_ARTS.register("absolving_step",
			() -> new StillArt("absolving_step", EnumClarityStage.VIGILANT, 220, (player, level, heldItem, pos, art) -> {
				Vec3 look = player.getLookAngle().normalize();
				player.setDeltaMovement(look.x * 1.6, Math.max(0.15, player.getDeltaMovement().y), look.z * 1.6);
				player.move(MoverType.SELF, player.getDeltaMovement());
				player.clearFire();
				player.removeEffect(MobEffects.POISON);
				player.removeEffect(MobEffects.WITHER);
				level.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 0.2, player.getZ(),
						18, 0.35, 0.15, 0.35, 0.04);
				return true;
			}));

	public static final DeferredHolder<StillArt, StillArt> quietus_bell = STILL_ARTS.register("quietus_bell",
			() -> new StillArt("quietus_bell", EnumClarityStage.RESOLUTE, 420, (player, level, heldItem, pos, art) -> {
				player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 160, 1, false, true));
				player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 120, 0, false, true));
				level.getEntitiesOfClass(Monster.class, player.getBoundingBox().inflate(8.0), Monster::isAlive)
						.forEach(target -> target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, 1, false, true)));
				level.playSound(null, player.blockPosition(), SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 1.2f, 1.8f);
				return true;
			}));

	public static final DeferredHolder<StillArt, StillArt> autoimmune_edge = STILL_ARTS.register("autoimmune_edge",
			() -> new StillArt("autoimmune_edge", EnumClarityStage.ENLIGHTENED, 600, (player, level, heldItem, pos, art) -> {
				List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(5.0),
						target -> target != player && target.isAlive());
				targets.forEach(target -> {
					target.hurt(player.damageSources().magic(), 4.0f);
					target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0, false, true));
				});
				player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0, false, true));
				player.displayClientMessage(Component.literal("The immune will cuts outward.")
						.withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC), true);
				level.sendParticles(ParticleTypes.WAX_OFF, player.getX(), player.getY() + 1.0, player.getZ(),
						60, 2.2, 1.0, 2.2, 0.04);
				return true;
			}));

	public static StillArt getByName(String name) {
		if (name == null || name.isEmpty()) {
			return StillArt.BLANK;
		}
		for (StillArt art : STILL_ARTS_TYPE_REGISTRY) {
			if (art != null && name.equals(art.getName())) {
				return art;
			}
		}
		return StillArt.BLANK;
	}

	private static boolean isInFront(Player player, Vec3 look, LivingEntity target, double threshold) {
		Vec3 toTarget = target.position().subtract(player.position()).normalize();
		return look.dot(toTarget) > threshold;
	}
}
