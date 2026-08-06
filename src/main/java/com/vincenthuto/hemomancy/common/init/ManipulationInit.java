package com.vincenthuto.hemomancy.common.init;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.entity.npc.DrudgeEntity;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.DrudgeAction;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationStatusRules;
import com.vincenthuto.hemomancy.common.manipulation.SchoolHitHelper;
import com.vincenthuto.hemomancy.common.manipulation.animus.*;
import com.vincenthuto.hemomancy.common.manipulation.congeatio.*;
import com.vincenthuto.hemomancy.common.manipulation.ductilis.ActivationPotentialManip;
import com.vincenthuto.hemomancy.common.manipulation.ductilis.ConductiveMarkManip;
import com.vincenthuto.hemomancy.common.manipulation.ductilis.CrimsonHarvestManip;
import com.vincenthuto.hemomancy.common.manipulation.ductilis.DuctilisLightningEffects;
import com.vincenthuto.hemomancy.common.manipulation.ductilis.HemolymphalPulseManip;
import com.vincenthuto.hemomancy.common.manipulation.ductilis.SanguineWardManip;
import com.vincenthuto.hemomancy.common.manipulation.ductilis.SynapticJoltManip;
import com.vincenthuto.hemomancy.common.manipulation.ferric.*;
import com.vincenthuto.hemomancy.common.manipulation.flammeus.*;
import com.vincenthuto.hemomancy.common.manipulation.lux.*;
import com.vincenthuto.hemomancy.common.manipulation.mortem.ExsanguinateManip;
import com.vincenthuto.hemomancy.common.manipulation.mortem.GraveDebtManip;
import com.vincenthuto.hemomancy.common.manipulation.mortem.HemorrhageManip;
import com.vincenthuto.hemomancy.common.manipulation.mortem.InsatiableHungerManip;
import com.vincenthuto.hemomancy.common.manipulation.mortem.VitalReservoirManip;
import com.vincenthuto.hemomancy.common.manipulation.saint.BloomOfRotManip;
import com.vincenthuto.hemomancy.common.manipulation.saint.CrimsonTitheManip;
import com.vincenthuto.hemomancy.common.manipulation.saint.EndlessHourManip;
import com.vincenthuto.hemomancy.common.manipulation.saint.UnclosingEyeManip;
import com.vincenthuto.hemomancy.common.manipulation.tenebris.*;
import com.vincenthuto.hemomancy.common.util.CrimsonFireHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ManipulationInit {

	public static final ResourceKey<Registry<BloodManipulation>> MANIP_KEY = ResourceKey
			.createRegistryKey(Hemomancy.rloc("bloodmanipulations"));

	public static final DeferredRegister<BloodManipulation> MANIPS = DeferredRegister.create(MANIP_KEY,
			Hemomancy.MOD_ID);

	public static final Registry<BloodManipulation> MANIPS_TYPE_REGISTRY = MANIPS
			.makeRegistry(builder -> builder.maxId(Integer.MAX_VALUE - 1)
					.defaultKey(Hemomancy.rloc("venous_travel")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> venous_travel = MANIPS.register("venous_travel",
			() -> new BloodManipulation("venous_travel", 1000, 0, 0, EnumManipulationType.CONTINUOUS,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.DUCTILIS, EnumVeinSections.ARMS)
					.setSecondaryTend(EnumBloodTendency.ANIMUS)
					.setCooldownTicks(20)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, "Not usable by Drudges"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> blood_shot = MANIPS.register("blood_shot",
			() -> new BloodShotManip("blood_shot", 100, 0, 0, EnumManipulationType.QUICK, EnumManipulationRank.HUMILIS,
					EnumBloodTendency.ANIMUS, EnumVeinSections.HEAD)
					.setCooldownTicks(10)
					.setDrudgeAction((drudge, world, centre, radius) -> {
						AABB area = new AABB(centre).inflate(radius);
						Monster target = world.getEntitiesOfClass(Monster.class, area).stream()
								.min(Comparator.comparingDouble(drudge::distanceToSqr)).orElse(null);
						if (target == null) return false;
						target.hurt(world.damageSources().mobAttack(drudge),
								(float) drudge.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.5f);
						return true;
					}, "Ranged strike vs nearest hostile"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> deadly_gaze = MANIPS.register("deadly_gaze",
			() -> new DeadlyGazeManip("deadly_gaze", 100, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.DUCTILIS, EnumVeinSections.HEAD)
					.setSecondaryTend(EnumBloodTendency.TENEBRIS)
					.setCooldownTicks(20)
					.setDrudgeAction((drudge, world, centre, radius) -> {
						AABB area = new AABB(centre).inflate(radius);
						Monster target = world.getEntitiesOfClass(Monster.class, area).stream()
								.min(Comparator.comparingDouble(drudge::distanceToSqr)).orElse(null);
						if (target == null) return false;
						target.push(0, 0.8, 0);
						target.hurt(world.damageSources().mobAttack(drudge),
								(float) drudge.getAttributeValue(Attributes.ATTACK_DAMAGE) * 2.0f);
						return true;
					}, "Knockback and heavy strike vs nearest hostile"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> summon_avatar = MANIPS.register("summon_avatar",
			() -> new SummonAvatarManip("summon_avatar", 500, 50, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.SUMMA, EnumBloodTendency.ANIMUS, EnumVeinSections.BODY)
					.setCooldownTicks(100)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, "Not usable by Drudges"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> blood_needle = MANIPS.register("blood_needle",
			() -> new BloodNeedleManip("blood_needle", 100, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.ANIMUS, EnumVeinSections.HEAD)
					.setSecondaryTend(EnumBloodTendency.FERRIC)
					.setCooldownTicks(10)
					.setDrudgeAction((drudge, world, centre, radius) -> {
						AABB area = new AABB(centre).inflate(radius);
						Monster target = world.getEntitiesOfClass(Monster.class, area).stream()
								.min(Comparator.comparingDouble(drudge::distanceToSqr)).orElse(null);
						if (target == null) return false;
						float dmg = (float) drudge.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.4f;
						for (int i = 0; i < 5; i++) {
							target.hurt(world.damageSources().mobAttack(drudge), dmg);
						}
						return true;
					}, "Needle volley vs nearest hostile"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> blood_cloud = MANIPS.register("blood_cloud",
			() -> new BloodCloudManip("blood_cloud", 300, 25, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.SUMMA, EnumBloodTendency.ANIMUS, EnumVeinSections.HEAD)
					.setSecondaryTend(EnumBloodTendency.MORTEM)
					.setCooldownTicks(40)
					.setDrudgeAction((drudge, world, centre, radius) -> {
						AABB area = new AABB(centre).inflate(radius / 2.0);
						List<Monster> mobs = world.getEntitiesOfClass(Monster.class, area);
						if (mobs.isEmpty()) return false;
						mobs.forEach(m -> m.addEffect(
								new MobEffectInstance(MobEffects.WITHER, 60, 0, false, true)));
						return true;
					}, "Applies Wither to nearby hostiles"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> blood_rush = MANIPS.register("blood_rush",
			() -> new BloodRushManip("blood_rush", 100, 0, 0, EnumManipulationType.PASSIVE,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.ANIMUS, EnumVeinSections.BODY)
					.setSecondaryTend(EnumBloodTendency.FLAMMEUS)
					.setCooldownTicks(60)
					.setDrudgeAction((drudge, world, centre, radius) -> {
						drudge.addEffect(
								new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 1, false, false));
						AABB area = new AABB(centre).inflate(radius);
						world.getEntitiesOfClass(Player.class, area).forEach(p ->
								p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 1, false, true)));
						return true;
					}, "Speed boost to Drudge and nearby allies"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> blood_aneurysm = MANIPS.register("blood_aneurysm",
			() -> new BloodAneurysmManip("blood_aneurysm", 400, 25, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.SUMMA, EnumBloodTendency.ANIMUS, EnumVeinSections.BODY)
					.setSecondaryTend(EnumBloodTendency.MORTEM)
					.setCooldownTicks(40)
					.setDrudgeAction((drudge, world, centre, radius) -> {
						AABB area = new AABB(centre).inflate(radius / 2.0);
						List<Monster> mobs = world.getEntitiesOfClass(Monster.class, area);
						if (mobs.isEmpty()) return false;
						float dmg = (float) drudge.getAttributeValue(Attributes.ATTACK_DAMAGE);
						mobs.forEach(m -> {
							m.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0, false, true));
							m.hurt(world.damageSources().mobAttack(drudge), dmg);
						});
						return true;
					}, "Area nausea pulse on hostiles"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> vital_effusion = MANIPS.register("vital_effusion",
			() -> new VitalEffusionManip("vital_effusion", 350, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.ANIMUS, EnumVeinSections.BODY)
					.setSecondaryTend(EnumBloodTendency.DUCTILIS)
					.setCooldownTicks(60)
					.setDrudgeAction((drudge, world, centre, radius) -> {
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
					}, "Accelerates crop growth near the Drudge's position"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> ferric_transmutation = MANIPS.register("ferric_transmutation",
			() -> new FerricTransmutationManip("ferric_transmutation", 1000, 50, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.SUMMA, EnumBloodTendency.FERRIC, EnumVeinSections.BODY)
					.setSecondaryTend(EnumBloodTendency.DUCTILIS)
					.setCooldownTicks(20)
					.setDrudgeAction((drudge, world, centre, radius) -> {
						if (!(world instanceof ServerLevel sLevel)) return false;
						sLevel.addFreshEntity(new ItemEntity(sLevel,
								centre.getX() + 0.5, centre.getY() + 0.5, centre.getZ() + 0.5,
								new ItemStack(Items.IRON_INGOT)));
						return true;
					}, "Produces an iron ingot"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> activation_potential = MANIPS.register("activation_potential",
			() -> new ActivationPotentialManip("activation_potential", 200, 10, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.DUCTILIS, EnumVeinSections.BODY)
					.setCooldownTicks(30)
					.setDrudgeAction((drudge, world, centre, radius) -> {
						AABB area = new AABB(centre).inflate(radius);
						List<Player> players = world.getEntitiesOfClass(Player.class, area);
						if (players.isEmpty()) return false;
						players.forEach(p -> p.addEffect(
								new MobEffectInstance(MobEffects.REGENERATION, 100, 1, false, true)));
						return true;
					}, "Grants Regeneration to nearby allies"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> synaptic_jolt = MANIPS.register("synaptic_jolt",
			() -> new SynapticJoltManip("synaptic_jolt", 150, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.DUCTILIS, EnumVeinSections.HEAD)
					.setCooldownTicks(25)
					.setDrudgeAction((drudge, world, centre, radius) -> {
						AABB area = new AABB(centre).inflate(Math.min(radius, 7.0D));
						Monster target = world.getEntitiesOfClass(Monster.class, area).stream()
								.min(Comparator.comparingDouble(drudge::distanceToSqr)).orElse(null);
						if (target == null) return false;
						SynapticJoltManip.staggerTarget(target);
						DuctilisLightningEffects.synapticJolt(drudge, target);
						target.hurt(world.damageSources().mobAttack(drudge), 3.0F);
						return true;
					}, "Interrupts and staggers nearest hostile"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> conductive_mark = MANIPS.register("conductive_mark",
			() -> new ConductiveMarkManip("conductive_mark", 225, 10, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.DUCTILIS, EnumVeinSections.HEAD)
					.setSecondaryTend(EnumBloodTendency.FERRIC)
					.setCooldownTicks(50)
					.setDrudgeAction((drudge, world, centre, radius) -> {
						AABB area = new AABB(centre).inflate(Math.min(radius, 14.0D));
						Monster target = world.getEntitiesOfClass(Monster.class, area).stream()
								.min(Comparator.comparingDouble(drudge::distanceToSqr)).orElse(null);
						if (target == null) return false;
						SchoolHitHelper.markConductive(target, ManipulationStatusRules.CONDUCTIVE_MARK_TICKS);
						DuctilisLightningEffects.conductiveMark(drudge, target);
						return true;
					}, "Marks nearest hostile for conductive arcs"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> sanguine_ward = MANIPS.register("sanguine_ward",
			() -> new SanguineWardManip("sanguine_ward", 10, 10, 0, EnumManipulationType.CONTINUOUS,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.DUCTILIS, EnumVeinSections.BODY)
					.setSecondaryTend(EnumBloodTendency.FERRIC)
					.setCooldownTicks(20)
					.setDrudgeAction((drudge, world, centre, radius) -> {
						AABB area = new AABB(centre).inflate(radius);
						List<Player> players = world.getEntitiesOfClass(Player.class, area);
						if (players.isEmpty()) return false;
						players.forEach(p -> p.addEffect(
								new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 0, false, true)));
						return true;
					}, "Grants Resistance to nearby allies"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> hemolymphal_pulse = MANIPS.register("hemolymphal_pulse",
			() -> new HemolymphalPulseManip("hemolymphal_pulse", 400, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.DUCTILIS, EnumVeinSections.HEAD)
					.setSecondaryTend(EnumBloodTendency.ANIMUS)
					.setCooldownTicks(300)
					.setDrudgeAction((drudge, world, centre, radius) -> {
						AABB area = new AABB(centre).inflate(radius);
						List<net.minecraft.world.entity.LivingEntity> nearby =
								world.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, area);
						if (nearby.isEmpty()) return false;
						nearby.forEach(e -> e.addEffect(
								new MobEffectInstance(MobEffects.GLOWING, 300, 0, false, false)));
						return true;
					}, "Applies Glowing to all nearby entities"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> conjure_blade = MANIPS.register("conjure_blade",
			() -> new StaffWeaponFormManip("conjure_blade", ItemInit.living_blade, 0, 0,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.ANIMUS, EnumVeinSections.ARMS)
					.setSecondaryTend(EnumBloodTendency.FERRIC)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, "Not usable by Drudges"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> conjure_axe = MANIPS.register("conjure_axe",
			() -> new StaffWeaponFormManip("conjure_axe", ItemInit.living_axe, 0, 0,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.MORTEM, EnumVeinSections.ARMS)
					.setSecondaryTend(EnumBloodTendency.FERRIC)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, "Not usable by Drudges"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> conjure_spear = MANIPS.register("conjure_spear",
			() -> new StaffWeaponFormManip("conjure_spear", ItemInit.living_spear, 0, 0,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.LUX, EnumVeinSections.ARMS)
					.setSecondaryTend(EnumBloodTendency.FERRIC)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, "Not usable by Drudges"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> conjure_claws = MANIPS.register("conjure_claws",
			() -> new StaffWeaponFormManip("conjure_claws", ItemInit.living_baghnakh, 0, 0,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.TENEBRIS, EnumVeinSections.ARMS)
					.setSecondaryTend(EnumBloodTendency.MORTEM)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, "Not usable by Drudges"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> conjure_crossbow = MANIPS.register("conjure_crossbow",
			() -> new StaffWeaponFormManip("conjure_crossbow", ItemInit.living_crossbow, 0, 0,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.DUCTILIS, EnumVeinSections.ARMS)
					.setSecondaryTend(EnumBloodTendency.FERRIC)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, "Not usable by Drudges"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> conjure_torch = MANIPS.register("conjure_torch",
			() -> new StaffWeaponFormManip("conjure_torch", ItemInit.living_torch, 0, 0,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.FLAMMEUS, EnumVeinSections.ARMS)
					.setSecondaryTend(EnumBloodTendency.LUX)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, "Not usable by Drudges"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> conjure_flail = MANIPS.register("conjure_flail",
			() -> new StaffWeaponFormManip("conjure_flail", ItemInit.living_flail, 0, 0,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.CONGEATIO, EnumVeinSections.ARMS)
					.setSecondaryTend(EnumBloodTendency.FERRIC)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, "Not usable by Drudges"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> conjure_sickle = MANIPS.register("conjure_sickle",
			() -> new StaffWeaponFormManip("conjure_sickle", ItemInit.living_sickle, 0, 0,
					EnumManipulationRank.PERFECTUS, EnumBloodTendency.MORTEM, EnumVeinSections.ARMS)
					.setSecondaryTend(EnumBloodTendency.FERRIC)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, "Not usable by Drudges"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> conjure_staff = MANIPS.register("conjure_staff",
			() -> new ConjurationManip("conjure_staff", ItemInit.living_staff, 1000, 0, 0,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.FERRIC, EnumVeinSections.ARMS)
					.setSecondaryTend(EnumBloodTendency.ANIMUS)
					.setCooldownTicks(40)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, "Not usable by Drudges"));


	public static final DeferredHolder<BloodManipulation, BloodManipulation> blood_absorption = MANIPS.register("blood_absorption",
			() -> new ConjurationManip("blood_absorption", ItemInit.blood_absorption, 1000, 0, 0,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.FERRIC, EnumVeinSections.ARMS)
					.setSecondaryTend(EnumBloodTendency.ANIMUS)
					.setCooldownTicks(40)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, "Not usable by Drudges"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> blood_projection = MANIPS.register("blood_projection",
			() -> new ConjurationManip("blood_projection", ItemInit.blood_projection, 1000, 0, 0,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.FERRIC, EnumVeinSections.ARMS)
					.setSecondaryTend(EnumBloodTendency.ANIMUS)
					.setCooldownTicks(40)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, "Not usable by Drudges"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> summon_thrall = MANIPS.register("summon_thrall",
			() -> new SummonThrallManip("summon_thrall", 500, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.ANIMUS, EnumVeinSections.BODY)
					.setCooldownTicks(60)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, "Not usable by Drudges"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> crimson_flame_conjuration = MANIPS.register("crimson_flame_conjuration",
			() -> new CrimsonFlameConjurationManip("crimson_flame_conjuration", 150, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.FLAMMEUS, EnumVeinSections.ARMS)
					.setSecondaryTend(EnumBloodTendency.ANIMUS)
					.setCooldownTicks(15)
					.setDrudgeAction((drudge, world, centre, radius) -> {
						AABB area = new AABB(centre).inflate(radius);
						Monster target = world.getEntitiesOfClass(Monster.class, area).stream()
								.min(Comparator.comparingDouble(drudge::distanceToSqr)).orElse(null);
						if (target == null) return false;
						CrimsonFireHelper.igniteCrimson(target, 6);
						return true;
					}, "Sets nearest hostile on fire"));

	// ── Utilitarian Manipulations ──

	public static final DeferredHolder<BloodManipulation, BloodManipulation> sanguine_mending = MANIPS.register("sanguine_mending",
			() -> new SanguineMendingManip("sanguine_mending", 150, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.FERRIC, EnumVeinSections.ARMS)
					.setSecondaryTend(EnumBloodTendency.ANIMUS)
					.setCooldownTicks(30)
					.setDrudgeAction((drudge, world, centre, radius) -> {
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
					}, "Repairs most-damaged armor of nearest ally"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> hemosynthesis = MANIPS.register("hemosynthesis",
			() -> new HemosynthesisManip("hemosynthesis", 200, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.LUX, EnumVeinSections.BODY)
					.setSecondaryTend(EnumBloodTendency.ANIMUS)
					.setCooldownTicks(40)
					.setDrudgeAction((drudge, world, centre, radius) -> {
						AABB area = new AABB(centre).inflate(radius);
						Player target = world.getEntitiesOfClass(Player.class, area).stream()
								.filter(p -> p.getHealth() < p.getMaxHealth())
								.min(Comparator.comparingDouble(drudge::distanceToSqr)).orElse(null);
						if (target == null) return false;
						target.heal(4.0f);
						return true;
					}, "Heals the most-wounded nearby ally"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> blood_lamp = MANIPS.register("blood_lamp",
			() -> new BloodLampManip("blood_lamp", 75, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.LUX, EnumVeinSections.ARMS)
					.setCooldownTicks(10)
					.setDrudgeAction((drudge, world, centre, radius) -> {
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
					}, "Places a light source in the dark"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> hematic_flare = MANIPS.register("hematic_flare",
			() -> new HematicFlareManip("hematic_flare", 125, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.LUX, EnumVeinSections.HEAD)
					.setSecondaryTend(EnumBloodTendency.FLAMMEUS)
					.setCooldownTicks(30)
					.setDrudgeAction((drudge, world, centre, radius) -> {
						AABB area = new AABB(centre).inflate(radius);
						Monster target = world.getEntitiesOfClass(Monster.class, area).stream()
								.min(Comparator.comparingDouble(drudge::distanceToSqr)).orElse(null);
						if (target == null) return false;
						boolean concealed = target.hasEffect(MobEffects.INVISIBILITY);
						if (concealed) {
							target.removeEffect(MobEffects.INVISIBILITY);
						}
						target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 180, 0, false, true));
						target.hurt(world.damageSources().magic(), concealed ? 5.0F : 3.0F);
						return true;
					}, "Marks and burns the nearest hidden hostile"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> crimson_harvest = MANIPS.register("crimson_harvest",
			() -> new CrimsonHarvestManip("crimson_harvest", 200, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.DUCTILIS, EnumVeinSections.LEGS)
					.setSecondaryTend(EnumBloodTendency.ANIMUS)
					.setCooldownTicks(60)
					.setDrudgeAction((drudge, world, centre, radius) -> {
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
					}, "Accelerates crop growth nearby"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> glacial_grasp = MANIPS.register("glacial_grasp",
			() -> new GlacialGraspManip("glacial_grasp", 125, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.CONGEATIO, EnumVeinSections.ARMS)
					.setSecondaryTend(EnumBloodTendency.TENEBRIS)
					.setCooldownTicks(20)
					.setDrudgeAction((drudge, world, centre, radius) -> {
						AABB area = new AABB(centre).inflate(radius);
						Monster target = world.getEntitiesOfClass(Monster.class, area).stream()
								.min(Comparator.comparingDouble(drudge::distanceToSqr)).orElse(null);
						if (target == null) return false;
						target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 4, false, true));
						target.hurt(world.damageSources().freeze(),
								(float) drudge.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.5f);
						return true;
					}, "Freezes and damages nearest hostile"));

	// ── Mid-Game Utilitarian Manipulations (MEDIOCRITAS) ──

	public static final DeferredHolder<BloodManipulation, BloodManipulation> sanguine_excavation = MANIPS.register("sanguine_excavation",
			() -> new SanguineExcavationManip("sanguine_excavation", 400, 10, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.FERRIC, EnumVeinSections.ARMS)
					.setSecondaryTend(EnumBloodTendency.MORTEM)
					.setCooldownTicks(40)
					.setDrudgeAction((drudge, world, centre, radius) -> {
						if (!(world instanceof ServerLevel)) return false;
						Vec3 look = drudge.getLookAngle();
						BlockPos target = BlockPos.containing(
								drudge.getEyePosition().add(look.scale(2.5)));
						BlockState state = world.getBlockState(target);
						if (state.isAir() || state.getDestroySpeed(world, target) < 0) return false;
						world.destroyBlock(target, true);
						return true;
					}, "Mines the block the Drudge faces"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> vascular_dowsing = MANIPS.register("vascular_dowsing",
			() -> new VascularDowsingManip("vascular_dowsing", 500, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.FERRIC, EnumVeinSections.ARMS)
					.setSecondaryTend(EnumBloodTendency.LUX)
					.setCooldownTicks(400)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, "Not usable by Drudges"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> ferric_resonance = MANIPS.register("ferric_resonance",
			() -> new FerricResonanceManip("ferric_resonance", 600, 20, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.FERRIC, EnumVeinSections.ARMS)
					.setSecondaryTend(EnumBloodTendency.FLAMMEUS)
					.setCooldownTicks(200)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, "Not usable by Drudges"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> iron_retort = MANIPS.register("iron_retort",
			() -> new IronRetortManip("iron_retort", 250, 10, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.FERRIC, EnumVeinSections.BODY)
					.setSecondaryTend(EnumBloodTendency.DUCTILIS)
					.setCooldownTicks(80)
					.setDrudgeAction((drudge, world, centre, radius) -> {
						drudge.addEffect(new MobEffectInstance(EffectInit.iron_retort,
								ManipulationStatusRules.IRON_RETORT_TICKS, 0, false, true));
						return true;
					}, "Brief retaliatory iron guard"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> sanguine_magnetism = MANIPS.register("sanguine_magnetism",
			() -> new SanguineMagnetismManip("sanguine_magnetism", 450, 20, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.SUMMA, EnumBloodTendency.FERRIC, EnumVeinSections.BODY)
					.setSecondaryTend(EnumBloodTendency.DUCTILIS)
					.setCooldownTicks(140)
					.setDrudgeAction((drudge, world, centre, radius) -> {
						if (!(world instanceof ServerLevel serverLevel)) return false;
						AABB area = new AABB(centre).inflate(radius);
						Monster target = world.getEntitiesOfClass(Monster.class, area).stream()
								.min(Comparator.comparingDouble(drudge::distanceToSqr)).orElse(null);
						Vec3 anchor = target != null ? target.position() : Vec3.atBottomCenterOf(centre);
						SanguineMagnetismManip.spawnMagneticPillar(drudge, serverLevel, anchor,
								ManipulationStatusRules.SANGUINE_MAGNETISM_TICKS);
						return true;
					}, "Raises a magnetic iron pillar that traps hostiles"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> pyretic_forge = MANIPS.register("pyretic_forge",
			() -> new PyreticForgeManip("pyretic_forge", 350, 10, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.FLAMMEUS, EnumVeinSections.BODY)
					.setSecondaryTend(EnumBloodTendency.FERRIC)
					.setCooldownTicks(30)
					.setDrudgeAction((drudge, world, centre, radius) -> {
						AABB area = new AABB(centre).inflate(radius);
						List<Monster> mobs = world.getEntitiesOfClass(Monster.class, area);
						if (mobs.isEmpty()) return false;
						mobs.forEach(m -> {
							CrimsonFireHelper.igniteCrimson(m, 10);
							m.hurt(world.damageSources().onFire(), 3.0f);
						});
						return true;
					}, "Burns all hostiles in area"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> umbral_step = MANIPS.register("umbral_step",
			() -> new UmbralStepManip("umbral_step", 300, 10, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.TENEBRIS, EnumVeinSections.LEGS)
					.setSecondaryTend(EnumBloodTendency.DUCTILIS)
					.setCooldownTicks(40)
					.setDrudgeAction((drudge, world, centre, radius) -> {
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
					}, "Teleports Drudge to a dark spot within radius"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> crimson_sight = MANIPS.register("crimson_sight",
			() -> new CrimsonSightManip("crimson_sight", 250, 10, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.LUX, EnumVeinSections.HEAD)
					.setSecondaryTend(EnumBloodTendency.TENEBRIS)
					.setCooldownTicks(60)
					.setDrudgeAction((drudge, world, centre, radius) -> {
						AABB area = new AABB(centre).inflate(radius);
						List<Monster> mobs = world.getEntitiesOfClass(Monster.class, area);
						if (mobs.isEmpty()) return false;
						mobs.forEach(m -> m.addEffect(
								new MobEffectInstance(MobEffects.GLOWING, 600, 0, false, true)));
						return true;
					}, "Applies Glowing to nearby hostiles"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> prismatic_reproof = MANIPS.register("prismatic_reproof",
			() -> new PrismaticReproofManip("prismatic_reproof", 325, 10, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.LUX, EnumVeinSections.HEAD)
					.setSecondaryTend(EnumBloodTendency.FERRIC)
					.setCooldownTicks(80)
					.setDrudgeAction((drudge, world, centre, radius) -> {
						AABB area = new AABB(centre).inflate(radius);
						List<Monster> mobs = world.getEntitiesOfClass(Monster.class, area);
						mobs.forEach(m -> {
							m.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0, false, true));
							m.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 140, 0, false, true));
						});
						return !mobs.isEmpty();
					}, "Blinds and weakens hostile mobs"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> hematic_beacon = MANIPS.register("hematic_beacon",
			() -> new HematicBeaconManip("hematic_beacon", 350, 10, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.LUX, EnumVeinSections.BODY)
					.setSecondaryTend(EnumBloodTendency.ANIMUS)
					.setCooldownTicks(160)
					.setDrudgeAction((drudge, world, centre, radius) -> {
						AABB area = new AABB(centre).inflate(radius);
						world.getEntitiesOfClass(Player.class, area).forEach(p -> {
							p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 160, 0, false, true));
							p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 160, 0, false, true));
						});
						world.getEntitiesOfClass(Monster.class, area).forEach(m ->
								m.addEffect(new MobEffectInstance(MobEffects.GLOWING, 240, 0, false, true)));
						return true;
					}, "Marks hostiles and steadies nearby allies"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> lumen_suture = MANIPS.register("lumen_suture",
			() -> new LumenSutureManip("lumen_suture", 250, 10, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.LUX, EnumVeinSections.ARMS)
					.setSecondaryTend(EnumBloodTendency.DUCTILIS)
					.setCooldownTicks(120)
					.setDrudgeAction((drudge, world, centre, radius) -> {
						AABB area = new AABB(centre).inflate(radius);
						Player ally = world.getEntitiesOfClass(Player.class, area).stream()
								.min(Comparator.comparingDouble(drudge::distanceToSqr)).orElse(null);
						if (ally == null) return false;
						ally.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 300, 1, false, true));
						ally.removeEffect(MobEffects.BLINDNESS);
						return true;
					}, "Grants absorption and clears blindness"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> vital_reservoir = MANIPS.register("vital_reservoir",
			() -> new VitalReservoirManip("vital_reservoir", 50, 10, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.MORTEM, EnumVeinSections.HEART)
					.setSecondaryTend(EnumBloodTendency.ANIMUS)
					.setCooldownTicks(60)
					.setDrudgeAction((drudge, world, centre, radius) -> {
						if (drudge instanceof DrudgeEntity drudgeEntity
								&& drudgeEntity.getBloodCharge() >= DrudgeEntity.MAX_BLOOD_CHARGE * 0.9f) return false;
						AABB area = new AABB(centre).inflate(radius);
						Monster target = world.getEntitiesOfClass(Monster.class, area).stream()
								.min(Comparator.comparingDouble(drudge::distanceToSqr)).orElse(null);
						if (target == null) return false;
						target.hurt(world.damageSources().magic(), 2.0f);
						if (drudge instanceof DrudgeEntity drudgeEntity) {
							drudgeEntity.fillBloodCharge(200f);
						} else {
							drudge.heal(4.0F);
						}
						return true;
					}, "Draws blood from nearby hostile"));

	// ── CONGEATIO — expanded tendencies ──

	public static final DeferredHolder<BloodManipulation, BloodManipulation> cryogenic_pulse = MANIPS.register("cryogenic_pulse",
			() -> new CryogenicPulseManip("cryogenic_pulse", 150, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.CONGEATIO, EnumVeinSections.BODY)
					.setSecondaryTend(EnumBloodTendency.FLAMMEUS)
					.setCooldownTicks(30)
					.setDrudgeAction((drudge, world, centre, radius) -> {
						AABB area = new AABB(centre).inflate(radius);
						List<Monster> mobs = world.getEntitiesOfClass(Monster.class, area);
						if (mobs.isEmpty()) return false;
						mobs.forEach(m -> m.addEffect(
								new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2, false, true)));
						return true;
					}, "Slows all hostiles in area"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> glacial_circulation = MANIPS.register("glacial_circulation",
			() -> new GlacialCirculationManip("glacial_circulation", 175, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.CONGEATIO, EnumVeinSections.BODY)
					.setSecondaryTend(EnumBloodTendency.ANIMUS)
					.setCooldownTicks(100));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> glacial_bastion = MANIPS.register("glacial_bastion",
			() -> new GlacialBastionManip("glacial_bastion", 350, 10, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.CONGEATIO, EnumVeinSections.ARMS)
					.setSecondaryTend(EnumBloodTendency.FERRIC)
					.setCooldownTicks(50)
					.setDrudgeAction((drudge, world, centre, radius) -> {
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
								if (TemporaryIceManager.place(sLevel, wallPos, Blocks.PACKED_ICE.defaultBlockState(), 500)) {
									placed++;
								}
							}
						}
						return placed > 0;
					}, "Raises temporary packed ice around the Drudge"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> glacial_rampart = MANIPS.register("glacial_rampart",
			() -> new GlacialRampartManip("glacial_rampart", 350, 10, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.CONGEATIO, EnumVeinSections.ARMS)
					.setSecondaryTend(EnumBloodTendency.FERRIC)
					.setCooldownTicks(50)
					.setDrudgeAction((drudge, world, centre, radius) -> {
						if (!(world instanceof ServerLevel sLevel)) return false;
						Vec3 look = drudge.getLookAngle();
						BlockPos base = BlockPos.containing(drudge.getEyePosition().add(look.scale(3.0)));
						int placed = 0;
						for (int dx = -1; dx <= 1; dx++) {
							for (int dy = 0; dy < 3; dy++) {
								BlockPos wallPos = base.offset(dx, dy, 0);
								if (TemporaryIceManager.place(sLevel, wallPos, Blocks.PACKED_ICE.defaultBlockState(), 500)) {
									placed++;
								}
							}
						}
						return placed > 0;
					}, "Raises a temporary packed ice wall"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> osseous_bloom = MANIPS.register("osseous_bloom",
			() -> new OsseousBloomManip("osseous_bloom", 600, 25, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.SUMMA, EnumBloodTendency.CONGEATIO, EnumVeinSections.BODY)
					.setSecondaryTend(EnumBloodTendency.MORTEM)
					.setCooldownTicks(60));

	// ── FLAMMEUS — expanded tendencies ──

	public static final DeferredHolder<BloodManipulation, BloodManipulation> sanguine_ignition = MANIPS.register("sanguine_ignition",
			() -> new SanguineIgnitionManip("sanguine_ignition", 125, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.FLAMMEUS, EnumVeinSections.BODY)
					.setSecondaryTend(EnumBloodTendency.ANIMUS)
					.setCooldownTicks(25)
					.setDrudgeAction((drudge, world, centre, radius) -> {
						AABB area = new AABB(centre).inflate(radius);
						Monster target = world.getEntitiesOfClass(Monster.class, area).stream()
								.min(Comparator.comparingDouble(drudge::distanceToSqr)).orElse(null);
						if (target == null) return false;
						CrimsonFireHelper.igniteCrimson(target, 8);
						target.hurt(world.damageSources().onFire(), 2.0f);
						return true;
					}, "Sets nearest hostile on fire"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> vitric_combustion = MANIPS.register("vitric_combustion",
			() -> new VitricCombustionManip("vitric_combustion", 500, 25, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.SUMMA, EnumBloodTendency.FLAMMEUS, EnumVeinSections.BODY)
					.setCooldownTicks(60)
					.setDrudgeAction((drudge, world, centre, radius) -> {
						AABB area = new AABB(centre).inflate(radius);
						List<Monster> mobs = world.getEntitiesOfClass(Monster.class, area);
						if (mobs.isEmpty()) return false;
						float dmg = (float) drudge.getAttributeValue(Attributes.ATTACK_DAMAGE) * 2.0f;
						mobs.forEach(m -> {
							CrimsonFireHelper.igniteCrimson(m, 12);
							m.hurt(world.damageSources().onFire(), dmg);
						});
						return true;
					}, "Heavy fire damage to all hostiles in area"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> cauterizing_rebuke = MANIPS.register("cauterizing_rebuke",
			() -> new CauterizingRebukeManip("cauterizing_rebuke", 275, 10, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.FLAMMEUS, EnumVeinSections.BODY)
					.setSecondaryTend(EnumBloodTendency.ANIMUS)
					.setCooldownTicks(90)
					.setDrudgeAction((drudge, world, centre, radius) -> {
						drudge.removeEffect(MobEffects.POISON);
						drudge.removeEffect(MobEffects.WITHER);
						AABB area = new AABB(centre).inflate(radius / 2.0);
						List<Monster> mobs = world.getEntitiesOfClass(Monster.class, area);
						mobs.forEach(m -> {
							CrimsonFireHelper.igniteCrimson(m, 5);
							m.hurt(world.damageSources().onFire(), 2.0F);
						});
						return true;
					}, "Cleanses poison/wither and burns nearby hostiles"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> scalding_updraft = MANIPS.register("scalding_updraft",
			() -> new ScaldingUpdraftManip("scalding_updraft", 225, 5, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.FLAMMEUS, EnumVeinSections.LEGS)
					.setSecondaryTend(EnumBloodTendency.DUCTILIS)
					.setCooldownTicks(80)
					.setDrudgeAction((drudge, world, centre, radius) -> {
						drudge.push(0, 0.9, 0);
						drudge.fallDistance = 0;
						return true;
					}, "Launches the Drudge upward on hot air"));

	// ── TENEBRIS — expanded tendencies ──

	public static final DeferredHolder<BloodManipulation, BloodManipulation> void_shroud = MANIPS.register("void_shroud",
			() -> new VoidShroudManip("void_shroud", 100, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.TENEBRIS, EnumVeinSections.BODY)
					.setSecondaryTend(EnumBloodTendency.MORTEM)
					.setCooldownTicks(20)
					.setDrudgeAction((drudge, world, centre, radius) -> {
						drudge.addEffect(
								new MobEffectInstance(MobEffects.INVISIBILITY, 600, 0, false, false));
						return true;
					}, "Cloaks the Drudge with Invisibility"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> gloam_laceration = MANIPS.register("gloam_laceration",
			() -> new GloamLacerationManip("gloam_laceration", 175, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.TENEBRIS, EnumVeinSections.ARMS)
					.setSecondaryTend(EnumBloodTendency.MORTEM)
					.setCooldownTicks(35)
					.setDrudgeAction((drudge, world, centre, radius) -> {
						AABB area = new AABB(centre).inflate(radius);
						Monster target = world.getEntitiesOfClass(Monster.class, area).stream()
								.min(Comparator.comparingDouble(drudge::distanceToSqr)).orElse(null);
						if (target == null) return false;
						boolean ambush = drudge.hasEffect(MobEffects.INVISIBILITY)
								|| BlackVeilCovenantManager.isDarkEnough(world, drudge.blockPosition(), 7);
						target.addEffect(new MobEffectInstance(EffectInit.blood_loss, 140, 0, false, true));
						target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 0, false, true));
						target.hurt(world.damageSources().magic(), ambush ? 6.0F : 3.5F);
						return true;
					}, "Slashes the nearest hostile from darkness"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> blood_eclipse = MANIPS.register("blood_eclipse",
			() -> new BloodEclipseManip("blood_eclipse", 300, 10, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.TENEBRIS, EnumVeinSections.HEAD)
					.setSecondaryTend(EnumBloodTendency.LUX)
					.setCooldownTicks(45)
					.setDrudgeAction((drudge, world, centre, radius) -> {
						AABB area = new AABB(centre).inflate(radius);
						List<Monster> mobs = world.getEntitiesOfClass(Monster.class, area);
						if (mobs.isEmpty()) return false;
						float dmg = (float) drudge.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.2f;
						mobs.forEach(m -> {
							m.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0, false, true));
							m.hurt(world.damageSources().magic(), dmg);
						});
						return true;
					}, "Blinds and damages all hostiles in area"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> black_veil_covenant = MANIPS.register("black_veil_covenant",
			() -> new BlackVeilCovenantManip("black_veil_covenant", 425, 15, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.TENEBRIS, EnumVeinSections.BODY)
					.setSecondaryTend(EnumBloodTendency.MORTEM)
					.setCooldownTicks(220)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, "Not usable by Drudges"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> umbral_reversal = MANIPS.register("umbral_reversal",
			() -> new UmbralReversalManip("umbral_reversal", 375, 15, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.TENEBRIS, EnumVeinSections.LEGS)
					.setSecondaryTend(EnumBloodTendency.ANIMUS)
					.setCooldownTicks(100)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, "Not usable by Drudges"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> blood_eclipse_mantle = MANIPS.register("blood_eclipse_mantle",
			() -> new BloodEclipseMantleManip("blood_eclipse_mantle", 325, 10, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.TENEBRIS, EnumVeinSections.BODY)
					.setSecondaryTend(EnumBloodTendency.FLAMMEUS)
					.setCooldownTicks(180)
					.setDrudgeAction((drudge, world, centre, radius) -> {
						drudge.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 1, false, true));
						drudge.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 300, 0, false, true));
						drudge.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 300, 0, false, true));
						return true;
					}, "Grants eclipse resistance with a weakness tradeoff"));

	// ── MORTEM — expanded tendencies ──

	public static final DeferredHolder<BloodManipulation, BloodManipulation> hemorrhage = MANIPS.register("hemorrhage",
			() -> new HemorrhageManip("hemorrhage", 100, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.MORTEM, EnumVeinSections.ARMS)
					.setSecondaryTend(EnumBloodTendency.ANIMUS)
					.setCooldownTicks(20)
					.setDrudgeAction((drudge, world, centre, radius) -> {
						AABB area = new AABB(centre).inflate(radius);
						Monster target = world.getEntitiesOfClass(Monster.class, area).stream()
								.min(Comparator.comparingDouble(drudge::distanceToSqr)).orElse(null);
						if (target == null) return false;
						target.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 0, false, true));
						return true;
					}, "Applies Wither to nearest hostile"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> exsanguinate = MANIPS.register("exsanguinate",
			() -> new ExsanguinateManip("exsanguinate", 300, 10, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.MORTEM, EnumVeinSections.ARMS)
					.setSecondaryTend(EnumBloodTendency.DUCTILIS)
					.setCooldownTicks(50)
					.setDrudgeAction((drudge, world, centre, radius) -> {
						AABB area = new AABB(centre).inflate(radius);
						Monster target = world.getEntitiesOfClass(Monster.class, area).stream()
								.min(Comparator.comparingDouble(drudge::distanceToSqr)).orElse(null);
						if (target == null) return false;
						float drain = target.getMaxHealth() * 0.2f;
						target.hurt(world.damageSources().magic(), drain);
						drudge.heal(drain * 0.5f);
						return true;
					}, "Drains and reflects HP from nearest hostile"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> insatiable_hunger = MANIPS.register("insatiable_hunger",
			() -> new InsatiableHungerManip("insatiable_hunger", 225, 10, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.MORTEM, EnumVeinSections.BODY)
					.setSecondaryTend(EnumBloodTendency.ANIMUS)
					.setCooldownTicks(70)
					.setDrudgeAction((drudge, world, centre, radius) -> {
						AABB area = new AABB(centre).inflate(radius);
						Monster target = world.getEntitiesOfClass(Monster.class, area).stream()
								.min(Comparator.comparingDouble(drudge::distanceToSqr)).orElse(null);
						if (target == null) return false;
						target.addEffect(new MobEffectInstance(EffectInit.insatiable_hunger, 220, 0, false, true));
						return true;
					}, "Curses nearest hostile with anti-healing hunger"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> grave_debt = MANIPS.register("grave_debt",
			() -> new GraveDebtManip("grave_debt", 325, 10, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.MORTEM, EnumVeinSections.HEART)
					.setSecondaryTend(EnumBloodTendency.ANIMUS)
					.setCooldownTicks(75)
					.setDrudgeAction((drudge, world, centre, radius) -> {
						AABB area = new AABB(centre).inflate(radius);
						Monster target = world.getEntitiesOfClass(Monster.class, area).stream()
								.min(Comparator.comparingDouble(drudge::distanceToSqr)).orElse(null);
						if (target == null) return false;
						SchoolHitHelper.markGraveDebt(target,
								drudge instanceof DrudgeEntity drudgeEntity ? drudgeEntity.getOwnerUUID() : null,
								ManipulationStatusRules.GRAVE_DEBT_TICKS);
						return true;
					}, "Marks nearest hostile to burst at low health"));

	// ── SAINT — Canon Memories (imprinted from Sainted Mausoleums) ──

	public static final DeferredHolder<BloodManipulation, BloodManipulation> crimson_tithe = MANIPS.register("crimson_tithe",
			() -> new CrimsonTitheManip("crimson_tithe", 400, 25, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.SUMMA, EnumBloodTendency.MORTEM, EnumVeinSections.HEART)
					.setSecondaryTend(EnumBloodTendency.LUX)
					.setCooldownTicks(100)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, "Not usable by Drudges"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> unclosing_eye = MANIPS.register("unclosing_eye",
			() -> new UnclosingEyeManip("unclosing_eye", 350, 20, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.SUMMA, EnumBloodTendency.LUX, EnumVeinSections.HEAD)
					.setSecondaryTend(EnumBloodTendency.MORTEM)
					.setCooldownTicks(120)
					.setDrudgeAction((drudge, world, centre, radius) -> {
						AABB area = new AABB(centre).inflate(radius);
						world.getEntitiesOfClass(LivingEntity.class, area).forEach(e ->
								e.addEffect(new MobEffectInstance(MobEffects.GLOWING, 1200, 0, false, false)));
						return true;
					}, "Applies Glowing to all nearby entities"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> bloom_of_rot = MANIPS.register("bloom_of_rot",
			() -> new BloomOfRotManip("bloom_of_rot", 500, 25, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.SUMMA, EnumBloodTendency.MORTEM, EnumVeinSections.BODY)
					.setSecondaryTend(EnumBloodTendency.ANIMUS)
					.setCooldownTicks(80)
					.setDrudgeAction((drudge, world, centre, radius) -> {
						AABB area = new AABB(centre).inflate(radius);
						List<Monster> mobs = world.getEntitiesOfClass(Monster.class, area);
						if (mobs.isEmpty()) return false;
						mobs.forEach(m -> {
							m.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1, false, true));
							m.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0, false, true));
						});
						return true;
					}, "Applies Wither and Poison to all hostiles in area"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> endless_hour = MANIPS.register("endless_hour",
			() -> new EndlessHourManip("endless_hour", 600, 30, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.SUMMA, EnumBloodTendency.CONGEATIO, EnumVeinSections.BODY)
					.setSecondaryTend(EnumBloodTendency.LUX)
					.setCooldownTicks(200)
					.setDrudgeAction((drudge, world, centre, radius) -> {
						drudge.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 400, 2, false, false));
						drudge.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 400, 2, false, false));
						return true;
					}, "Grants Drudge protective absorption and resistance"));

	public static List<BloodManipulation> getAllEntries() {
		List<BloodManipulation> entries = new ArrayList<>();
		Registry<BloodManipulation> registry = MANIPS_TYPE_REGISTRY;
		if (registry != null) {
			registry.forEach(entries::add);
		}
		return entries;
	}

	public static BloodManipulation getByName(String name) {
		for (BloodManipulation manip : getAllEntries()) {
			if (name.equals(manip.getName())) {
				return manip;
			}
		}
		return null;
	}
}
