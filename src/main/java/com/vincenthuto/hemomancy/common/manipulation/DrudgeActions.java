package com.vincenthuto.hemomancy.common.manipulation;

import com.vincenthuto.hemomancy.common.damage.SchoolDamage;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.damage.SchoolState;
import com.vincenthuto.hemomancy.common.damage.SchoolStates;
import com.vincenthuto.hemomancy.common.entity.npc.DrudgeEntity;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper.Reach;
import com.vincenthuto.hemomancy.common.manipulation.congeatio.*;
import com.vincenthuto.hemomancy.common.manipulation.ductilis.*;
import com.vincenthuto.hemomancy.common.manipulation.ferric.*;
import com.vincenthuto.hemomancy.common.manipulation.tenebris.*;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * The work each manipulation does when a Drudge casts it, moved verbatim from {@code ManipulationInit} (step 9).
 * {@link BloodManipulation#setDrudgeAction} wraps each one in the Paralysis check and the school scope.
 */
public final class DrudgeActions {
	private DrudgeActions() {
	}

	/**
	 * The monsters in a Drudge's work area. A Drudge reaches every monster there, its owner's puppets included, as it
	 * did before step 9 (G-2b would spare them).
	 */
	static List<Monster> monsters(PathfinderMob drudge, Level world, AABB area) {
		return world.getEntitiesOfClass(Monster.class, area,
				monster -> ManipulationCombatHelper.canHarm(drudge, monster, Reach.MONSTER_FILTER_ANY_STATE));
	}

	/** The work area's monster nearest the Drudge, or null. */
	static Monster nearestMonster(PathfinderMob drudge, Level world, AABB area) {
		return monsters(drudge, world, area).stream()
				.min(Comparator.comparingDouble(drudge::distanceToSqr)).orElse(null);
	}

	/** blood_shot */
	public static boolean bloodShot(PathfinderMob drudge, Level world, BlockPos centre, int radius) {
		AABB area = new AABB(centre).inflate(radius);
		Monster target = nearestMonster(drudge, world, area);
		if (target == null) return false;
		target.hurt(world.damageSources().mobAttack(drudge),
				(float) drudge.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.5f);
		return true;
	}

	/** deadly_gaze */
	public static boolean deadlyGaze(PathfinderMob drudge, Level world, BlockPos centre, int radius) {
		AABB area = new AABB(centre).inflate(radius);
		Monster target = nearestMonster(drudge, world, area);
		if (target == null) return false;
		target.push(0, 0.8, 0);
		com.vincenthuto.hemomancy.common.manipulation.ductilis.DeadlyGazeManip.grip(target, drudge.getEyePosition(), 1, false);
		target.hurt(world.damageSources().mobAttack(drudge),
				(float) drudge.getAttributeValue(Attributes.ATTACK_DAMAGE) * 2.0f);
		return true;
	}

	/** blood_needle */
	public static boolean bloodNeedle(PathfinderMob drudge, Level world, BlockPos centre, int radius) {
		AABB area = new AABB(centre).inflate(radius);
		Monster target = nearestMonster(drudge, world, area);
		if (target == null) return false;
		float dmg = (float) drudge.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.4f;
		for (int i = 0; i < 5; i++) {
			target.hurt(world.damageSources().mobAttack(drudge), dmg);
		}
		return true;
	}

	/** blood_cloud */
	public static boolean bloodCloud(PathfinderMob drudge, Level world, BlockPos centre, int radius) {
		AABB area = new AABB(centre).inflate(radius / 2.0);
		List<Monster> mobs = monsters(drudge, world, area);
		if (mobs.isEmpty()) return false;
		mobs.forEach(m -> SchoolStates.apply(drudge, m, SchoolState.PRESSURE, 60));
		return true;
	}

	/** blood_rush */
	public static boolean bloodRush(PathfinderMob drudge, Level world, BlockPos centre, int radius) {
		drudge.addEffect(
				new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 1, false, false));
		AABB area = new AABB(centre).inflate(radius);
		world.getEntitiesOfClass(Player.class, area).forEach(p ->
				p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 1, false, true)));
		return true;
	}

	/** blood_aneurysm */
	public static boolean bloodAneurysm(PathfinderMob drudge, Level world, BlockPos centre, int radius) {
		AABB area = new AABB(centre).inflate(radius / 2.0);
		List<Monster> mobs = monsters(drudge, world, area);
		if (mobs.isEmpty()) return false;
		float dmg = (float) drudge.getAttributeValue(Attributes.ATTACK_DAMAGE);
		mobs.forEach(m -> {
			m.hurt(world.damageSources().mobAttack(drudge), dmg);
		});
		return true;
	}

	/** vital_effusion */
	public static boolean vitalEffusion(PathfinderMob drudge, Level world, BlockPos centre, int radius) {
		BlockPos p = centre;
		if (!(world instanceof net.minecraft.server.level.ServerLevel sLevel)) return false;
		boolean grew = false;
		for (BlockPos nearby : BlockPos.betweenClosed(p.offset(-2, -1, -2), p.offset(2, 1, 2))) {
			net.minecraft.world.level.block.state.BlockState state = world.getBlockState(nearby);
			if (state.getBlock() instanceof net.minecraft.world.level.block.BonemealableBlock bb
					&& bb.isValidBonemealTarget(sLevel, nearby, state)) {
				bb.performBonemeal(sLevel, world.random, nearby.immutable(), world.getBlockState(nearby.immutable()));
				grew = true;
			}
		}
		return grew;
	}

	/** ferric_transmutation */
	public static boolean ferricTransmutation(PathfinderMob drudge, Level world, BlockPos centre, int radius) {
		if (!(world instanceof ServerLevel sLevel)) return false;
		sLevel.addFreshEntity(new ItemEntity(sLevel,
				centre.getX() + 0.5, centre.getY() + 0.5, centre.getZ() + 0.5,
				new ItemStack(Items.IRON_INGOT)));
		return true;
	}

	/** activation_potential */
	public static boolean activationPotential(PathfinderMob drudge, Level world, BlockPos centre, int radius) {
		List<Monster> mobs = world.getEntitiesOfClass(Monster.class, drudge.getBoundingBox().inflate(5.0D),
				m -> ManipulationCombatHelper.canHarm(drudge, m, Reach.MONSTER_FILTER) && m.distanceTo(drudge) <= 5.0D);
		if (mobs.isEmpty()) return false;
		mobs.forEach(m -> {
			SchoolStates.apply(drudge, m, SchoolState.DISRUPTED, 10);
			m.hurt(world.damageSources().indirectMagic(drudge, drudge), 3.0F);
		});
		return true;
	}

	/** synaptic_jolt */
	public static boolean synapticJolt(PathfinderMob drudge, Level world, BlockPos centre, int radius) {
		AABB area = new AABB(centre).inflate(Math.min(radius, 7.0D));
		Monster target = nearestMonster(drudge, world, area);
		if (target == null) return false;
		DuctilisLightningEffects.synapticJolt(drudge, target);
		target.hurt(world.damageSources().mobAttack(drudge), 3.0F);
		return true;
	}

	/** conductive_mark */
	public static boolean conductiveMark(PathfinderMob drudge, Level world, BlockPos centre, int radius) {
		AABB area = new AABB(centre).inflate(Math.min(radius, 14.0D));
		Monster target = nearestMonster(drudge, world, area);
		if (target == null) return false;
		SchoolHitHelper.markConductive(target, ManipulationStatusRules.CONDUCTIVE_MARK_TICKS);
		DuctilisLightningEffects.conductiveMark(drudge, target);
		return true;
	}

	/** sanguine_ward */
	public static boolean sanguineWard(PathfinderMob drudge, Level world, BlockPos centre, int radius) {
		AABB area = new AABB(centre).inflate(radius);
		List<Player> players = world.getEntitiesOfClass(Player.class, area);
		if (players.isEmpty()) return false;
		players.forEach(p -> p.addEffect(
				new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 0, false, true)));
		return true;
	}

	/** hemolymphal_pulse */
	public static boolean hemolymphalPulse(PathfinderMob drudge, Level world, BlockPos centre, int radius) {
		AABB area = new AABB(centre).inflate(radius);
		List<Monster> nearby = monsters(drudge, world, area);
		if (nearby.isEmpty()) return false;
		nearby.forEach(e -> e.addEffect(
				new MobEffectInstance(MobEffects.GLOWING, 300, 0, false, false)));
		return true;
	}

	/** crimson_flame_conjuration */
	public static boolean crimsonFlameConjuration(PathfinderMob drudge, Level world, BlockPos centre, int radius) {
		AABB area = new AABB(centre).inflate(radius);
		Monster target = nearestMonster(drudge, world, area);
		if (target == null) return false;
		SchoolStates.apply(drudge, target, SchoolState.SEARING, 120);
		return true;
	}

	/** sanguine_mending */
	public static boolean sanguineMending(PathfinderMob drudge, Level world, BlockPos centre, int radius) {
		AABB area = new AABB(centre).inflate(radius);
		Player owner = world.getEntitiesOfClass(Player.class, area).stream()
				.min(Comparator.comparingDouble(drudge::distanceToSqr)).orElse(null);
		if (owner == null) return false;
		ItemStack toRepair = ItemStack.EMPTY;
		int maxDmg = 0;
		for (ItemStack armor : owner.getArmorSlots()) {
			if (armor.isDamageableItem() && armor.getDamageValue() > maxDmg) {
				maxDmg = armor.getDamageValue();
				toRepair = armor;
			}
		}
		if (toRepair.isEmpty() || !toRepair.isDamaged()) return false;
		toRepair.setDamageValue(toRepair.getDamageValue() - Math.min(toRepair.getDamageValue(), 100));
		return true;
	}

	/** hemosynthesis */
	public static boolean hemosynthesis(PathfinderMob drudge, Level world, BlockPos centre, int radius) {
		AABB area = new AABB(centre).inflate(radius);
		Player target = world.getEntitiesOfClass(Player.class, area).stream()
				.filter(p -> p.getHealth() < p.getMaxHealth())
				.min(Comparator.comparingDouble(drudge::distanceToSqr)).orElse(null);
		if (target == null) return false;
		target.heal(4.0f);
		return true;
	}

	/** blood_lamp */
	public static boolean bloodLamp(PathfinderMob drudge, Level world, BlockPos centre, int radius) {
		BlockPos darkest = null;
		int darkestLight = 15;
		int half = radius / 2;
		for (BlockPos p : BlockPos.betweenClosed(
				centre.offset(-half, -2, -half), centre.offset(half, 4, half))) {
			if (world.getBlockState(p).isAir()
					&& !world.getBlockState(p.below()).isAir()) {
				int light = world.getBrightness(net.minecraft.world.level.LightLayer.BLOCK, p);
				if (light < darkestLight) {
					darkestLight = light;
					darkest = p.immutable();
				}
			}
		}
		if (darkest == null || darkestLight >= 10) return false;
		world.setBlockAndUpdate(darkest, Blocks.TORCH.defaultBlockState());
		return true;
	}

	/** hematic_flare */
	public static boolean hematicFlare(PathfinderMob drudge, Level world, BlockPos centre, int radius) {
		AABB area = new AABB(centre).inflate(radius);
		Monster target = nearestMonster(drudge, world, area);
		if (target == null) return false;
		SchoolDamage.hurt(target, world.damageSources().magic(), 3.0F, 180, 1);
		return true;
	}

	/** crimson_harvest */
	public static boolean crimsonHarvest(PathfinderMob drudge, Level world, BlockPos centre, int radius) {
		if (!(world instanceof ServerLevel sLevel)) return false;
		boolean grew = false;
		int half = radius / 2;
		for (BlockPos p : BlockPos.betweenClosed(
				centre.offset(-half, -1, -half), centre.offset(half, 2, half))) {
			BlockState state = world.getBlockState(p);
			if (state.getBlock() instanceof BonemealableBlock growable
					&& growable.isValidBonemealTarget(world, p, state)
					&& growable.isBonemealSuccess(world, world.random, p, state)) {
				growable.performBonemeal(sLevel, world.random, p, state);
				grew = true;
			}
		}
		return grew;
	}

	/** glacial_grasp */
	public static boolean glacialGrasp(PathfinderMob drudge, Level world, BlockPos centre, int radius) {
		AABB area = new AABB(centre).inflate(radius);
		Monster target = nearestMonster(drudge, world, area);
		if (target == null) return false;
		SchoolDamage.hurt(target, world.damageSources().freeze(),
                (float) drudge.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.5f, 120, 1);
		return true;
	}

	/** sanguine_excavation */
	public static boolean sanguineExcavation(PathfinderMob drudge, Level world, BlockPos centre, int radius) {
		if (!(world instanceof ServerLevel)) return false;
		Vec3 look = drudge.getLookAngle();
		BlockPos target = BlockPos.containing(
				drudge.getEyePosition().add(look.scale(2.5)));
		BlockState state = world.getBlockState(target);
		if (state.isAir() || state.getDestroySpeed(world, target) < 0) return false;
		world.destroyBlock(target, true);
		return true;
	}

	/** iron_retort */
	public static boolean ironRetort(PathfinderMob drudge, Level world, BlockPos centre, int radius) {
		drudge.addEffect(new MobEffectInstance(EffectInit.iron_retort,
				ManipulationStatusRules.IRON_RETORT_TICKS, 0, false, true));
		return true;
	}

	/** sanguine_magnetism */
	public static boolean sanguineMagnetism(PathfinderMob drudge, Level world, BlockPos centre, int radius) {
		if (!(world instanceof ServerLevel serverLevel)) return false;
		AABB area = new AABB(centre).inflate(radius);
		Monster target = nearestMonster(drudge, world, area);
		Vec3 anchor = target != null ? target.position() : Vec3.atBottomCenterOf(centre);
		SanguineMagnetismManip.spawnMagneticPillar(drudge, serverLevel, anchor,
				ManipulationStatusRules.SANGUINE_MAGNETISM_TICKS);
		return true;
	}

	/** pyretic_forge */
	public static boolean pyreticForge(PathfinderMob drudge, Level world, BlockPos centre, int radius) {
		AABB area = new AABB(centre).inflate(radius);
		List<Monster> mobs = monsters(drudge, world, area);
		if (mobs.isEmpty()) return false;
		mobs.forEach(m -> {
			SchoolDamage.hurt(m, world.damageSources().onFire(), 3.0f, 200, 1);
		});
		return true;
	}

	/** umbral_step */
	public static boolean umbralStep(PathfinderMob drudge, Level world, BlockPos centre, int radius) {
		double angle = world.random.nextDouble() * 2 * Math.PI;
		double dist = radius * 0.5 + world.random.nextDouble() * radius * 0.5;
		double nx = centre.getX() + 0.5 + Math.cos(angle) * dist;
		double nz = centre.getZ() + 0.5 + Math.sin(angle) * dist;
		BlockPos dest = BlockPos.containing(nx, centre.getY(), nz);
		// Walk up until clear
		for (int i = 0; i < 10 && !world.getBlockState(dest).isAir(); i++) {
			dest = dest.above();
		}
		if (!world.getBlockState(dest).isAir()) return false;
		if (!BlackVeilCovenantManager.isDarkEnough(world, dest, 7)) return false;
		drudge.teleportTo(dest.getX() + 0.5, dest.getY(), dest.getZ() + 0.5);
		return true;
	}

	/** crimson_sight */
	public static boolean crimsonSight(PathfinderMob drudge, Level world, BlockPos centre, int radius) {
		AABB area = new AABB(centre).inflate(radius);
		List<Monster> mobs = monsters(drudge, world, area);
		if (mobs.isEmpty()) return false;
		mobs.forEach(m -> SchoolStates.apply(drudge, m, SchoolState.ILLUMINATED, 600));
		return true;
	}

	/** prismatic_reproof */
	public static boolean prismaticReproof(PathfinderMob drudge, Level world, BlockPos centre, int radius) {
		AABB area = new AABB(centre).inflate(radius);
		List<Monster> mobs = monsters(drudge, world, area);
		mobs.forEach(m -> {
			m.hurt(world.damageSources().magic(), 2.0F);
		});
		return !mobs.isEmpty();
	}

	/** hematic_beacon */
	public static boolean hematicBeacon(PathfinderMob drudge, Level world, BlockPos centre, int radius) {
		AABB area = new AABB(centre).inflate(radius);
		world.getEntitiesOfClass(Player.class, area).forEach(p -> {
			p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 160, 0, false, true));
			p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 160, 0, false, true));
		});
		monsters(drudge, world, area).forEach(m ->
				SchoolStates.apply(drudge, m, SchoolState.ILLUMINATED, 240));
		return true;
	}

	/** lumen_suture */
	public static boolean lumenSuture(PathfinderMob drudge, Level world, BlockPos centre, int radius) {
		AABB area = new AABB(centre).inflate(radius);
		Player ally = world.getEntitiesOfClass(Player.class, area).stream()
				.min(Comparator.comparingDouble(drudge::distanceToSqr)).orElse(null);
		if (ally == null) return false;
		ally.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 300, 1, false, true));
		ally.removeEffect(MobEffects.BLINDNESS);
        ally.removeEffect(EffectInit.obscured);
		return true;
	}

	/** vital_reservoir */
	public static boolean vitalReservoir(PathfinderMob drudge, Level world, BlockPos centre, int radius) {
		if (drudge instanceof DrudgeEntity drudgeEntity
				&& drudgeEntity.getBloodCharge() >= DrudgeEntity.MAX_BLOOD_CHARGE * 0.9f) return false;
		AABB area = new AABB(centre).inflate(radius);
		Monster target = nearestMonster(drudge, world, area);
		if (target == null) return false;
		target.hurt(world.damageSources().magic(), 2.0f);
		if (drudge instanceof DrudgeEntity drudgeEntity) {
			drudgeEntity.fillBloodCharge(200f);
		} else {
			drudge.heal(4.0F);
		}
		return true;
	}

	/** cryogenic_pulse */
	public static boolean cryogenicPulse(PathfinderMob drudge, Level world, BlockPos centre, int radius) {
		AABB area = new AABB(centre).inflate(radius);
		List<Monster> mobs = monsters(drudge, world, area);
		if (mobs.isEmpty()) return false;
		mobs.forEach(m -> SchoolStates.apply(drudge, m, SchoolState.RIME, 100));
		return true;
	}

	/** glacial_bastion */
	public static boolean glacialBastion(PathfinderMob drudge, Level world, BlockPos centre, int radius) {
		if (!(world instanceof ServerLevel sLevel)) return false;
		BlockPos base = drudge.blockPosition();
		int placed = 0;
		for (int r = 0; r < 8; r++) {
			double angle = r * Math.PI / 4.0;
			for (int dy = 0; dy < 3; dy++) {
				BlockPos wallPos = BlockPos.containing(
						base.getX() + 0.5 + 2.0 * Math.cos(angle),
						base.getY() + dy,
						base.getZ() + 0.5 + 2.0 * Math.sin(angle));
				if (TemporaryIceManager.place(sLevel, wallPos, BlockInit.frozen_cruor.get().defaultBlockState(), 500)) {
					placed++;
				}
			}
		}
		return placed > 0;
	}

	/** glacial_rampart */
	public static boolean glacialRampart(PathfinderMob drudge, Level world, BlockPos centre, int radius) {
		if (!(world instanceof ServerLevel sLevel)) return false;
		Vec3 look = drudge.getLookAngle();
		BlockPos base = BlockPos.containing(drudge.getEyePosition().add(look.scale(3.0)));
		int placed = 0;
		for (int dx = -1; dx <= 1; dx++) {
			for (int dy = 0; dy < 3; dy++) {
				BlockPos wallPos = base.offset(dx, dy, 0);
				if (TemporaryIceManager.place(sLevel, wallPos, BlockInit.frozen_cruor.get().defaultBlockState(), 500)) {
					placed++;
				}
			}
		}
		return placed > 0;
	}

	/** sanguine_ignition */
	public static boolean sanguineIgnition(PathfinderMob drudge, Level world, BlockPos centre, int radius) {
		AABB area = new AABB(centre).inflate(radius);
		Monster target = nearestMonster(drudge, world, area);
		if (target == null) return false;
		SchoolDamage.hurt(target, world.damageSources().onFire(), 2.0f, 160, 1);
		return true;
	}

	/** vitric_combustion */
	public static boolean vitricCombustion(PathfinderMob drudge, Level world, BlockPos centre, int radius) {
		AABB area = new AABB(centre).inflate(radius);
		List<Monster> mobs = monsters(drudge, world, area);
		if (mobs.isEmpty()) return false;
		float dmg = (float) drudge.getAttributeValue(Attributes.ATTACK_DAMAGE) * 2.0f;
		mobs.forEach(m -> {
			SchoolDamage.hurt(m, world.damageSources().onFire(), dmg, 240, 1);
		});
		return true;
	}

	/** cauterizing_rebuke */
	public static boolean cauterizingRebuke(PathfinderMob drudge, Level world, BlockPos centre, int radius) {
		drudge.removeEffect(MobEffects.POISON);
		drudge.removeEffect(MobEffects.WITHER);
        drudge.removeEffect(EffectInit.necrosis);
		AABB area = new AABB(centre).inflate(radius / 2.0);
		List<Monster> mobs = monsters(drudge, world, area);
		mobs.forEach(m -> {
			SchoolDamage.hurt(m, world.damageSources().onFire(), 2.0F, 100, 1);
		});
		return true;
	}

	/** scalding_updraft */
	public static boolean scaldingUpdraft(PathfinderMob drudge, Level world, BlockPos centre, int radius) {
		drudge.push(0, 0.9, 0);
		drudge.fallDistance = 0;
		return true;
	}

	/** void_shroud */
	public static boolean voidShroud(PathfinderMob drudge, Level world, BlockPos centre, int radius) {
		SchoolStates.apply(drudge, drudge, SchoolState.VEILED, 600);
		return true;
	}

	/** gloam_laceration */
	public static boolean gloamLaceration(PathfinderMob drudge, Level world, BlockPos centre, int radius) {
		AABB area = new AABB(centre).inflate(radius);
		Monster target = nearestMonster(drudge, world, area);
		if (target == null) return false;
		boolean ambush = drudge.isInvisible()
				|| BlackVeilCovenantManager.isDarkEnough(world, drudge.blockPosition(), 7);
        if (target.hurt(world.damageSources().magic(), ambush ? 6.0F : 3.5F))
            target.addEffect(new MobEffectInstance(EffectInit.blood_loss, 140, 0, false, true));
		return true;
	}

	/** blood_eclipse */
	public static boolean bloodEclipse(PathfinderMob drudge, Level world, BlockPos centre, int radius) {
		AABB area = new AABB(centre).inflate(radius);
		List<Monster> mobs = monsters(drudge, world, area);
		if (mobs.isEmpty()) return false;
		float dmg = (float) drudge.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.2f;
		mobs.forEach(m -> {
			SchoolDamage.hurt(m, world.damageSources().magic(), dmg, 100, 1);
		});
		return true;
	}

	/** blood_eclipse_mantle */
	public static boolean bloodEclipseMantle(PathfinderMob drudge, Level world, BlockPos centre, int radius) {
		drudge.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 1, false, true));
		drudge.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 300, 0, false, true));
		drudge.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 300, 0, false, true));
		return true;
	}

	/** hemorrhage */
	public static boolean hemorrhage(PathfinderMob drudge, Level world, BlockPos centre, int radius) {
		AABB area = new AABB(centre).inflate(radius);
		Monster target = nearestMonster(drudge, world, area);
		if (target == null) return false;
		SchoolStates.apply(drudge, target, SchoolState.NECROSIS, 100);
		return true;
	}

	/** exsanguinate */
	public static boolean exsanguinate(PathfinderMob drudge, Level world, BlockPos centre, int radius) {
		AABB area = new AABB(centre).inflate(radius);
		Monster target = nearestMonster(drudge, world, area);
		if (target == null) return false;
		float drain = target.getMaxHealth() * 0.2f;
		target.hurt(world.damageSources().magic(), drain);
		drudge.heal(drain * 0.5f);
		return true;
	}

	/** insatiable_hunger */
	public static boolean insatiableHunger(PathfinderMob drudge, Level world, BlockPos centre, int radius) {
		AABB area = new AABB(centre).inflate(radius);
		Monster target = nearestMonster(drudge, world, area);
		if (target == null) return false;
		target.addEffect(new MobEffectInstance(EffectInit.insatiable_hunger, 220, 0, false, true));
		return true;
	}

	/** grave_debt */
	public static boolean graveDebt(PathfinderMob drudge, Level world, BlockPos centre, int radius) {
		AABB area = new AABB(centre).inflate(radius);
		Monster target = nearestMonster(drudge, world, area);
		if (target == null) return false;
		SchoolHitHelper.markGraveDebt(target,
				drudge instanceof DrudgeEntity drudgeEntity ? drudgeEntity.getOwnerUUID() : null,
				ManipulationStatusRules.GRAVE_DEBT_TICKS);
		return true;
	}

	/** unclosing_eye */
	public static boolean unclosingEye(PathfinderMob drudge, Level world, BlockPos centre, int radius) {
		AABB area = new AABB(centre).inflate(radius);
		world.getEntitiesOfClass(LivingEntity.class, area).forEach(e ->
				SchoolStates.apply(drudge, e, SchoolState.ILLUMINATED, 1200));
		return true;
	}

	/** bloom_of_rot */
	public static boolean bloomOfRot(PathfinderMob drudge, Level world, BlockPos centre, int radius) {
		AABB area = new AABB(centre).inflate(radius);
		List<Monster> mobs = monsters(drudge, world, area);
		if (mobs.isEmpty()) return false;
		mobs.forEach(m -> {
			SchoolStates.apply(drudge, m, SchoolState.NECROSIS, 100);
		});
		return true;
	}

	/** endless_hour */
	public static boolean endlessHour(PathfinderMob drudge, Level world, BlockPos centre, int radius) {
		drudge.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 0, false, false));
		return true;
	}
}
