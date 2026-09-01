package com.vincenthuto.hemomancy.client.render.item.harbinger;

import com.vincenthuto.hemomancy.client.data.ActiveRiteClientData;
import com.vincenthuto.hemomancy.client.particle.*;
import com.vincenthuto.hemomancy.client.particle.factory.AbsorbedBloodCellParticleFactory;
import com.vincenthuto.hemomancy.client.particle.factory.BloodCellParticleFactory;
import com.vincenthuto.hemomancy.client.particle.factory.WillAbsorptionGlowParticleFactory;
import com.vincenthuto.hemomancy.client.particle.util.EntityParticleUtils;
import com.vincenthuto.hemomancy.common.block.harbinger.BlockBloodInteractions;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.livingstaff.ILivingStaffProgress;
import com.vincenthuto.hemomancy.common.entity.mob.monster.will.WillEntity;
import com.vincenthuto.hemomancy.common.event.SanguineProjectionTargeting;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.component.LivingWeaponGraftData;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.*;
import com.vincenthuto.hutoslib.client.HLClientUtils;
import com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.HLParticleUtils;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.math.Vector3;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;

public final class CellHandParticleEffects {
	private static final int GLOBAL_PARTICLE_COUNT = 20;
	private static final int ABSORBED_ITEM_PARTICLES_PER_TICK = 2;
	private static final float ABSORBED_ITEM_PARTICLE_SCALE = 0.55F;
	private static final ParticleColor WILL_ABSORPTION_GLOW = ParticleColor.BLACK;
	private static final ProjectionParticleEmissionGate PROJECTION_EMISSION_GATE =
			new ProjectionParticleEmissionGate();

	private CellHandParticleEffects() {
	}

	public static void spawnThirdPersonParticlesFromOrigin(Vec3 origin, LivingEntity living, ItemStack activeStack) {
		Minecraft mc = Minecraft.getInstance();
		if (!ProjectionParticlePerspective.allowsThirdPersonEmission(
				living == mc.player, mc.options.getCameraType().isFirstPerson())) {
			return;
		}
		if (mc.isPaused() || !living.isUsingItem() || living.getUseItemRemainingTicks() <= 0) {
			return;
		}

		Level world = living.level();
		Random rand = new Random();
		if (isAbsorptionMode(living, activeStack)) {
			Optional<Vec3> blockSource = findCardinalRiteCancellationSource(living)
					.or(() -> BlockBloodInteractions.findLookedAtBloodBlockSource(world, living));
			if (blockSource.isPresent()) {
				spawnAbsorbedBloodParticle(mc, origin, blockSource.get(), ParticleColor.BLOOD, rand, Optional.empty());
			} else {
				Optional<WillEntity> willTarget = getFalteringWillAbsorptionParticleTarget(living, activeStack);
				if (willTarget.isPresent()) {
					WillEntity will = willTarget.get();
					spawnWillAbsorptionGlowParticle(mc, origin,
							will.position().add(0.0D, will.getBbHeight() * 0.55D, 0.0D), rand, Optional.empty());
					spawnWillAbsorptionTendencySpiralParticle(mc, origin, will, rand, Optional.empty());
				} else {
					for (LivingEntity livingTarget : getAbsorptionParticleTargets(living, activeStack)) {
						Vector3 targetVec = Vector3.fromEntityCenter(livingTarget);
						Predicate<Entity> targetPred = EntityParticleUtils.getEntityPredicate(livingTarget);
						ParticleColor targetColor = EntityParticleUtils.getColorFromPredicate(targetPred);
						spawnAbsorbedBloodParticle(mc, origin, new Vec3(targetVec.x, targetVec.y, targetVec.z),
								targetColor, rand, Optional.empty());
					}
				}
			}
		} else if (isProjectionMode(living, activeStack)) {
			if (PROJECTION_EMISSION_GATE.tryAcquire(living.getId(), world.getGameTime())) {
				HitResult trace = SanguineProjectionTargeting.pick(world, living,
						SanguineProjectionTargeting.PROJECTION_REACH, true);
				if (trace.getType() == HitResult.Type.BLOCK) {
					spawnProjectionParticle(mc, origin, trace, rand);
				}
			}
		}

		Vec3[] inversedSphere = HLParticleUtils.inversedSphere(GLOBAL_PARTICLE_COUNT,
				-world.getGameTime() * 0.01, 0.15, false);
		for (int i = 0; i < GLOBAL_PARTICLE_COUNT; i++) {
			world.addParticle(BloodCellParticleFactory.createData(new ParticleColor(255, 0, 0)),
					origin.x() + inversedSphere[i].x,
					origin.y() + inversedSphere[i].y,
					origin.z() + inversedSphere[i].z,
					0, 0.00, 0);
		}
	}

	public static void spawnFirstPersonParticlesForStack(ItemStack stack, HumanoidArm hand, Vec3 origin) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.isPaused() || !mc.options.getCameraType().isFirstPerson()) {
			return;
		}
		LocalPlayer player = mc.player;
		if (player == null || !player.isUsingItem() || player.getUseItemRemainingTicks() <= 0) {
			return;
		}
		if (!isAbsorptionMode(player, stack) && !isProjectionMode(player, stack)) {
			return;
		}

		InteractionHand activeHand = player.getUsedItemHand();
		HumanoidArm activeArm = activeHand == InteractionHand.MAIN_HAND
				? player.getMainArm()
				: player.getMainArm().getOpposite();
		if (activeArm != hand) {
			return;
		}

		Level world = player.level();
		Random rand = new Random();
		Vec3 anchor = worldToFirstPersonAnchor(origin);

		if (isAbsorptionMode(player, stack)) {
			Optional<Vec3> blockSource = findCardinalRiteCancellationSource(player)
					.or(() -> BlockBloodInteractions.findLookedAtBloodBlockSource(world, player));
			if (blockSource.isPresent()) {
				spawnAbsorbedBloodParticle(mc, origin, blockSource.get(), ParticleColor.BLOOD, rand,
						Optional.of(anchor));
			} else {
				Optional<WillEntity> willTarget = getFalteringWillAbsorptionParticleTarget(player, stack);
				if (willTarget.isPresent()) {
					WillEntity will = willTarget.get();
					spawnWillAbsorptionGlowParticle(mc, origin,
							will.position().add(0.0D, will.getBbHeight() * 0.55D, 0.0D), rand, Optional.of(anchor));
					spawnWillAbsorptionTendencySpiralParticle(mc, origin, will, rand, Optional.of(anchor));
				} else {
					for (LivingEntity livingTarget : getAbsorptionParticleTargets(player, stack)) {
						Vector3 targetVec = Vector3.fromEntityCenter(livingTarget);
						Predicate<Entity> targetPred = EntityParticleUtils.getEntityPredicate(livingTarget);
						ParticleColor targetColor = EntityParticleUtils.getColorFromPredicate(targetPred);
						spawnAbsorbedBloodParticle(mc, origin, new Vec3(targetVec.x, targetVec.y, targetVec.z),
								targetColor, rand, Optional.of(anchor));
					}
				}
			}
		} else if (isProjectionMode(player, stack)) {
			if (PROJECTION_EMISSION_GATE.tryAcquire(player.getId(), world.getGameTime())) {
				HitResult trace = SanguineProjectionTargeting.pick(world, player,
						SanguineProjectionTargeting.PROJECTION_REACH, true);
				if (trace.getType() == HitResult.Type.BLOCK) {
					spawnProjectionParticle(mc, origin, trace, rand);
				}
			}
		}

		Vec3[] inversedSphere = HLParticleUtils.inversedSphere(GLOBAL_PARTICLE_COUNT,
				-world.getGameTime() * 0.016, 0.15, false);
		for (int i = 0; i < GLOBAL_PARTICLE_COUNT; i++) {
			Vec3 localOffset = anchor.add(inversedSphere[i]);
			Vec3 particleOrigin = firstPersonAnchorToWorld(localOffset);
			Particle created = mc.particleEngine.createParticle(
					BloodCellParticleFactory.createData(new ParticleColor(255, 0, 0)),
					particleOrigin.x(), particleOrigin.y(), particleOrigin.z(), 0, 0.00, 0);
			if (created instanceof BloodCellParticle particle) {
				particle.setFirstPersonAnchor(localOffset);
			}
		}
	}

	public static void spawnFirstPersonParticlesForStack(ItemStack stack, HumanoidArm hand) {
		spawnFirstPersonParticlesForStack(stack, hand, fallbackFirstPersonHandOrigin(hand));
	}

	private static Optional<Vec3> findCardinalRiteCancellationSource(LivingEntity caster) {
		return ActiveRiteClientData.getActiveRites().stream()
				.filter(rite -> caster.getUUID().equals(rite.getOwner()))
				.filter(rite -> rite.getCancellationTicks() > 0)
				.findFirst()
				.map(rite -> Vec3.atCenterOf(rite.getCenter()).add(0.0D, 0.45D, 0.0D));
	}

	public static void spawnBrazierItemAbsorptionParticles(ItemStack offeringStack, Vec3 source) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.isPaused() || offeringStack.isEmpty()) {
			return;
		}
		LocalPlayer player = mc.player;
		if (player == null || !player.isUsingItem() || player.getUseItemRemainingTicks() <= 0) {
			return;
		}
		ItemStack activeStack = player.getUseItem();
		if (!isAbsorptionMode(player, activeStack)) {
			return;
		}

		HumanoidArm activeArm = player.getUsedItemHand() == InteractionHand.MAIN_HAND
				? player.getMainArm()
				: player.getMainArm().getOpposite();
		Vec3 origin = mc.options.getCameraType().isFirstPerson()
				? fallbackFirstPersonHandOrigin(activeArm)
				: calculateThirdPersonHandOrigin(player, activeArm);
		spawnItemParticlesAlongAbsorptionPath(mc, offeringStack, origin, source);
	}

	private static void spawnItemParticlesAlongAbsorptionPath(Minecraft mc, ItemStack offeringStack, Vec3 origin,
			Vec3 source) {
		if (mc.level == null) {
			return;
		}
		Vec3 path = source.subtract(origin);
		if (path.lengthSqr() < 0.0001D) {
			return;
		}
		Random rand = new Random();
		Vec3 inwardVelocity = origin.subtract(source).normalize().scale(0.035D);
		ParticleColor absorptionItemColor = absorptionItemColor(offeringStack);
		for (int i = 0; i < ABSORBED_ITEM_PARTICLES_PER_TICK; i++) {
			double pathOffset = 0.18D + rand.nextDouble() * 0.72D;
			Vec3 side = path.cross(new Vec3(0.0D, 1.0D, 0.0D));
			if (side.lengthSqr() < 0.0001D) {
				side = new Vec3(1.0D, 0.0D, 0.0D);
			}
			side = side.normalize().scale((rand.nextDouble() - 0.5D) * 0.08D);
			Vec3 point = origin.add(path.scale(pathOffset)).add(side);
			double velocityX = inwardVelocity.x + (rand.nextDouble() - 0.5D) * 0.025D;
			double velocityY = inwardVelocity.y + (rand.nextDouble() - 0.5D) * 0.025D;
			double velocityZ = inwardVelocity.z + (rand.nextDouble() - 0.5D) * 0.025D;
			Particle itemParticle = mc.particleEngine.createParticle(
					new ItemParticleOption(ParticleTypes.ITEM, offeringStack),
					point.x, point.y, point.z,
					velocityX, velocityY, velocityZ);
			if (itemParticle != null) {
				itemParticle.scale(ABSORBED_ITEM_PARTICLE_SCALE);
			}
			mc.level.addParticle(GlowParticleFactory.createData(absorptionItemColor),
					point.x + side.x * 0.35D, point.y + side.y * 0.35D, point.z + side.z * 0.35D,
					velocityX * 0.75D, velocityY * 0.75D, velocityZ * 0.75D);
		}
	}

	private static ParticleColor absorptionItemColor(ItemStack offeringStack) {
		if (offeringStack.is(ItemInit.memory_of_vesper.get())) {
			return new ParticleColor(180, 10, 30);
		}
		return LivingWeaponGraftData.fromStack(offeringStack)
				.map(LivingWeaponGraftData::form)
				.map(form -> form.manipulationHolder().get().getTend().getColor())
				.orElse(ParticleColor.BLOOD);
	}

	private static void spawnWillAbsorptionTendencySpiralParticle(Minecraft mc, Vec3 origin, WillEntity will,
			Random rand, Optional<Vec3> firstPersonTargetAnchor) {
		double age = will.tickCount + HLClientUtils.getPartialTicks();
		double spiralAngle = age * 0.42D + rand.nextDouble() * Math.PI * 0.45D;
		double heightPhase = (age * 0.075D + rand.nextDouble()) % 1.0D;
		double radius = 0.38D + 0.08D * Math.sin(age * 0.17D);
		Vec3 source = will.position().add(
				Math.cos(spiralAngle) * radius,
				will.getBbHeight() * (0.18D + 0.68D * heightPhase),
				Math.sin(spiralAngle) * radius);
		Vec3 finalPos = source.subtract(origin);
		Particle created = mc.particleEngine.createParticle(
				WillAbsorptionGlowParticleFactory.createData(will.getSchool().getColor()),
				origin.x, origin.y, origin.z,
				finalPos.x + rand.nextDouble() * 0.08D - 0.04D,
				finalPos.y + rand.nextDouble() * 0.08D - 0.04D,
				finalPos.z + rand.nextDouble() * 0.08D - 0.04D);
		if (created instanceof WillAbsorptionGlowParticle particle) {
			if (firstPersonTargetAnchor.isPresent()) {
				particle.setFirstPersonTargetAnchor(firstPersonTargetAnchor.get());
			} else {
				particle.setTargetYOffset(0.0D);
			}
		}
	}

	private static void spawnWillAbsorptionGlowParticle(Minecraft mc, Vec3 origin, Vec3 source, Random rand,
			Optional<Vec3> firstPersonTargetAnchor) {
		Vec3 finalPos = source.subtract(origin);
		Particle created = mc.particleEngine.createParticle(
				WillAbsorptionGlowParticleFactory.createData(WILL_ABSORPTION_GLOW),
				origin.x, origin.y, origin.z,
				finalPos.x + rand.nextDouble() * 0.22D - 0.11D,
				finalPos.y + rand.nextDouble() * 0.32D - 0.16D,
				finalPos.z + rand.nextDouble() * 0.22D - 0.11D);
		if (created instanceof WillAbsorptionGlowParticle particle) {
			if (firstPersonTargetAnchor.isPresent()) {
				particle.setFirstPersonTargetAnchor(firstPersonTargetAnchor.get());
			} else {
				particle.setTargetYOffset(0.0D);
			}
		}
	}

	private static void spawnAbsorbedBloodParticle(Minecraft mc, Vec3 origin, Vec3 source,
			ParticleColor targetColor, Random rand, Optional<Vec3> firstPersonTargetAnchor) {
		Vec3 finalPos = source.subtract(origin);
		Particle created = mc.particleEngine.createParticle(
				AbsorbedBloodCellParticleFactory.createData(targetColor),
				origin.x, origin.y, origin.z,
				(float) finalPos.x + rand.nextFloat() - 0.5D,
				(float) finalPos.y - rand.nextFloat(),
				(float) finalPos.z + rand.nextFloat() - 0.5D);
		if (created instanceof AbsorbedBloodCellParticle particle) {
			if (firstPersonTargetAnchor.isPresent()) {
				Vec3 anchor = firstPersonTargetAnchor.get();
				particle.setFirstPersonTargetAnchor(anchor);
			} else {
				particle.setTargetYOffset(0.0D);
			}
		}
	}

	private static void spawnProjectionParticle(Minecraft mc, Vec3 origin, HitResult trace, Random rand) {
		Particle created = mc.particleEngine.createParticle(
				AbsorbedBloodCellParticleFactory.createData(ParticleColor.BLOOD),
				origin.x, origin.y, origin.z, 0.0D, 0.0D, 0.0D);
		if (created instanceof AbsorbedBloodCellParticle particle) {
			particle.setProjectionPath(origin, trace.getLocation(),
					projectionDeviation(origin, trace.getLocation(), rand));
		}
	}

	private static Vec3 projectionDeviation(Vec3 source, Vec3 target, Random rand) {
		return BloodProjectionParticlePath.arcDeviation(source, target,
				rand.nextDouble() * 2.0D - 1.0D,
				rand.nextDouble() * 2.0D - 1.0D);
	}

	private static boolean isAbsorptionMode(LivingEntity living, ItemStack stack) {
		return stack.getItem() instanceof BloodAbsorptionItem
				|| LivingStaffItem.isLivingStaffAbsorptionUse(living, stack);
	}

	private static boolean isProjectionMode(LivingEntity living, ItemStack stack) {
		return stack.getItem() instanceof ICellHand && !(stack.getItem() instanceof BloodAbsorptionItem)
				|| LivingStaffItem.isLivingStaffProjectionUse(living, stack);
	}

	private static List<LivingEntity> getAbsorptionParticleTargets(LivingEntity living, ItemStack stack) {
		if (stack.getItem() instanceof BloodAbsorptionItem) {
			return BloodAbsorptionItem.findBareAbsorptionTarget(living, LivingStaffFocusRules.bareAbsorptionRange())
					.stream()
					.toList();
		}
		if (!(living instanceof Player player) || !LivingStaffItem.isLivingStaffAbsorptionUse(player, stack)) {
			return List.of();
		}
		ILivingStaffProgress progress = HemoCapabilityAccess.getLivingStaffProgress(player).orElse(null);
		LivingStaffFocusProfile focus = LivingStaffFocusProfile.fromPlayer(player, progress);
		return player.level().getEntitiesOfClass(LivingEntity.class,
						player.getBoundingBox().inflate(LivingStaffFocusRules.absorptionRange(focus)),
						target -> BloodAbsorptionItem.isValidAbsorptionTarget(player, target))
				.stream()
				.sorted(Comparator.comparingDouble(player::distanceToSqr))
				.limit(LivingStaffFocusRules.absorptionTargetCap(true, focus))
				.toList();
	}

	private static Optional<WillEntity> getFalteringWillAbsorptionParticleTarget(LivingEntity living, ItemStack stack) {
		double range;
		if (stack.getItem() instanceof BloodAbsorptionItem) {
			range = LivingStaffFocusRules.bareAbsorptionRange();
		} else if (living instanceof Player player && LivingStaffItem.isLivingStaffAbsorptionUse(player, stack)) {
			ILivingStaffProgress progress = HemoCapabilityAccess.getLivingStaffProgress(player).orElse(null);
			LivingStaffFocusProfile focus = LivingStaffFocusProfile.fromPlayer(player, progress);
			range = LivingStaffFocusRules.absorptionRange(focus);
		} else {
			return Optional.empty();
		}
		return living.level().getEntitiesOfClass(WillEntity.class, living.getBoundingBox().inflate(range),
						WillEntity::canBloodAbsorptionDrawParticles)
				.stream()
				.min(Comparator.comparingDouble(living::distanceToSqr));
	}

	private static Vec3 firstPersonAnchorToWorld(Vec3 localOffset) {
		Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
		Vec3 right = new Vec3(camera.getLeftVector()).scale(-localOffset.x);
		Vec3 up = new Vec3(camera.getUpVector()).scale(localOffset.y);
		Vec3 forward = new Vec3(camera.getLookVector()).scale(localOffset.z);
		return camera.getPosition().add(right).add(up).add(forward);
	}

	private static Vec3 fallbackFirstPersonHandOrigin(HumanoidArm hand) {
		double side = hand == HumanoidArm.RIGHT ? 0.32D : -0.32D;
		return firstPersonAnchorToWorld(new Vec3(side, -0.14D, 0.66D));
	}

	private static Vec3 worldToFirstPersonAnchor(Vec3 worldPosition) {
		Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
		Vec3 delta = worldPosition.subtract(camera.getPosition());
		Vec3 right = new Vec3(camera.getLeftVector()).scale(-1.0D);
		Vec3 up = new Vec3(camera.getUpVector());
		Vec3 forward = new Vec3(camera.getLookVector());
		return new Vec3(delta.dot(right), delta.dot(up), delta.dot(forward));
	}

	private static Vec3 calculateThirdPersonHandOrigin(LivingEntity living, HumanoidArm side) {
		double bodyYaw = Math.toRadians(living.yBodyRot);
		Vec3 forward = new Vec3(-Math.sin(bodyYaw), 0.0D, Math.cos(bodyYaw));
		Vec3 right = new Vec3(-forward.z, 0.0D, forward.x);
		double sideOffset = side == HumanoidArm.RIGHT ? 0.36D : -0.36D;

		return living.position()
				.add(0.0D, living.getBbHeight() * 0.72D, 0.0D)
				.add(forward.scale(0.46D))
				.add(right.scale(sideOffset));
	}
}
