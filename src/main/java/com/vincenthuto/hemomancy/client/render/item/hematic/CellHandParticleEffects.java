package com.vincenthuto.hemomancy.client.render.item.hematic;

import com.vincenthuto.hemomancy.client.particle.AbsorbedBloodCellParticle;
import com.vincenthuto.hemomancy.client.particle.BloodCellParticle;
import com.vincenthuto.hemomancy.client.particle.factory.AbsrobedBloodCellParticleFactory;
import com.vincenthuto.hemomancy.client.particle.factory.BloodCellParticleFactory;
import com.vincenthuto.hemomancy.client.particle.util.EntityParticleUtils;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.BloodAbsorptionItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.ICellHand;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffItem;
import com.vincenthuto.hutoslib.client.HLClientUtils;
import com.vincenthuto.hutoslib.client.particle.util.HLParticleUtils;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.math.Vector3;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

public final class CellHandParticleEffects {
	private static final int GLOBAL_PARTICLE_COUNT = 20;

	private CellHandParticleEffects() {
	}

	public static void spawnThirdPersonParticlesFromOrigin(Vec3 origin, LivingEntity living, ItemStack activeStack) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.isPaused() || !living.isUsingItem() || living.getUseItemRemainingTicks() <= 0) {
			return;
		}

		Level world = living.level();
		Random rand = new Random();
		if (isAbsorptionMode(living, activeStack)) {
			List<Entity> targets = living.level().getEntities(living, living.getBoundingBox().inflate(5.0));
			for (Entity target : targets) {
				if (target instanceof LivingEntity livingTarget) {
					Vector3 targetVec = Vector3.fromEntityCenter(livingTarget);
					Vec3 finalPos = origin.subtract(targetVec.x, targetVec.y, targetVec.z).reverse();
					Predicate<Entity> targetPred = EntityParticleUtils.getEntityPredicate(target);
					ParticleColor targetColor = EntityParticleUtils.getColorFromPredicate(targetPred);
					Particle created = mc.particleEngine.createParticle(
							AbsrobedBloodCellParticleFactory.createData(targetColor),
							origin.x, origin.y, origin.z,
							(float) finalPos.x + rand.nextFloat() - 0.5D,
							(float) finalPos.y - rand.nextFloat(),
							(float) finalPos.z + rand.nextFloat() - 0.5D);
					if (created instanceof AbsorbedBloodCellParticle particle) {
						particle.setTargetYOffset(0.0D);
					}
				}
			}
		} else if (isProjectionMode(living, activeStack)) {
			HitResult trace = living.pick(5, HLClientUtils.getPartialTicks(), true);
			if (trace.getType() == HitResult.Type.BLOCK) {
				Vec3 projectionTarget = trace.getLocation().add(0.0D, 1.05D, 0.0D);
				Vec3 finalPos = projectionTarget.subtract(origin.x, origin.y, origin.z).reverse();
				world.addParticle(AbsrobedBloodCellParticleFactory.createData(ParticleColor.BLOOD),
						projectionTarget.x, projectionTarget.y, projectionTarget.z,
						(float) finalPos.x + rand.nextFloat() - 0.5D,
						(float) finalPos.y - rand.nextFloat() - 0.5F,
						(float) finalPos.z + rand.nextFloat() - 0.5D);
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

	public static void spawnFirstPersonParticlesForStack(ItemStack stack, HumanoidArm hand) {
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
		Vec3 anchor = calculateFirstPersonHandAnchor(hand);
		Vec3 origin = firstPersonAnchorToWorld(anchor);

		if (isAbsorptionMode(player, stack)) {
			List<Entity> targets = player.level().getEntities(player, player.getBoundingBox().inflate(5.0));
			for (Entity target : targets) {
				if (target instanceof LivingEntity livingTarget) {
					Vector3 targetVec = Vector3.fromEntityCenter(livingTarget);
					Predicate<Entity> targetPred = EntityParticleUtils.getEntityPredicate(target);
					ParticleColor targetColor = EntityParticleUtils.getColorFromPredicate(targetPred);
					Vec3 source = new Vec3(targetVec.x, targetVec.y, targetVec.z);
					Vec3 finalPos = source.subtract(origin);
					Particle created = mc.particleEngine.createParticle(
							AbsrobedBloodCellParticleFactory.createData(targetColor),
							origin.x, origin.y, origin.z,
							(float) finalPos.x + rand.nextFloat() - 0.5D,
							(float) finalPos.y - rand.nextFloat(),
							(float) finalPos.z + rand.nextFloat() - 0.5D);
					if (created instanceof AbsorbedBloodCellParticle particle) {
						particle.setFirstPersonTargetAnchor(anchor);
					}
				}
			}
		} else if (isProjectionMode(player, stack)) {
			HitResult trace = player.pick(5, HLClientUtils.getPartialTicks(), true);
			if (trace.getType() == HitResult.Type.BLOCK) {
				Vec3 projectionTarget = trace.getLocation().add(0.0, 1.05D, 0.0);
				Vec3 finalPos = projectionTarget.subtract(origin.x, origin.y, origin.z).reverse();
				mc.particleEngine.createParticle(AbsrobedBloodCellParticleFactory.createData(ParticleColor.BLOOD),
						projectionTarget.x, projectionTarget.y, projectionTarget.z,
						(float) finalPos.x + rand.nextFloat() - 0.5D,
						(float) finalPos.y - rand.nextFloat(),
						(float) finalPos.z + rand.nextFloat() - 0.5D);
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

	private static boolean isAbsorptionMode(LivingEntity living, ItemStack stack) {
		return stack.getItem() instanceof BloodAbsorptionItem
				|| LivingStaffItem.isLivingStaffAbsorptionUse(living, stack);
	}

	private static boolean isProjectionMode(LivingEntity living, ItemStack stack) {
		return stack.getItem() instanceof ICellHand && !(stack.getItem() instanceof BloodAbsorptionItem)
				|| LivingStaffItem.isLivingStaffProjectionUse(living, stack);
	}

	private static Vec3 calculateFirstPersonHandAnchor(HumanoidArm hand) {
		LocalPlayer player = Minecraft.getInstance().player;
		float useTicks = 0.0F;
		if (player != null && player.isUsingItem()) {
			ItemStack useStack = player.getUseItem();
			useTicks = useStack.getUseDuration(player) - player.getUseItemRemainingTicks()
					+ HLClientUtils.getPartialTicks();
		}

		float sideSign = hand == HumanoidArm.RIGHT ? 1.0F : -1.0F;
		float wave = net.minecraft.util.Mth.sin(useTicks * 0.35F);
		float pulse = net.minecraft.util.Mth.sin(useTicks * 0.18F);
		return new Vec3(sideSign * (0.32D + 0.035D * wave), -0.14D + 0.035D * pulse,
				0.66D - 0.03D * pulse);
	}

	private static Vec3 firstPersonAnchorToWorld(Vec3 localOffset) {
		Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
		Vec3 right = new Vec3(camera.getLeftVector()).scale(-localOffset.x);
		Vec3 up = new Vec3(camera.getUpVector()).scale(localOffset.y);
		Vec3 forward = new Vec3(camera.getLookVector()).scale(localOffset.z);
		return camera.getPosition().add(right).add(up).add(forward);
	}
}
