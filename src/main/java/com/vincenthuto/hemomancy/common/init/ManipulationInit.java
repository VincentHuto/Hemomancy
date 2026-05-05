package com.vincenthuto.hemomancy.common.init;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.entity.npc.DrudgeEntity;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.DrudgeAction;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.common.manipulation.animus.BloodAneurysmManip;
import com.vincenthuto.hemomancy.common.manipulation.animus.BloodCloudManip;
import com.vincenthuto.hemomancy.common.manipulation.animus.VitalEffusionManip;
import com.vincenthuto.hemomancy.common.manipulation.animus.BloodNeedleManip;
import com.vincenthuto.hemomancy.common.manipulation.animus.BloodRushManip;
import com.vincenthuto.hemomancy.common.manipulation.animus.BloodShotManip;
import com.vincenthuto.hemomancy.common.manipulation.animus.CrimsonFlameConjurationManip;
import com.vincenthuto.hemomancy.common.manipulation.animus.DeadlyGazeManip;
import com.vincenthuto.hemomancy.common.manipulation.animus.SummonAvatarManip;
import com.vincenthuto.hemomancy.common.manipulation.animus.SummonThrallManip;
import com.vincenthuto.hemomancy.common.manipulation.congeatio.CryogenicPulseManip;
import com.vincenthuto.hemomancy.common.manipulation.congeatio.GlacialBastionManip;
import com.vincenthuto.hemomancy.common.manipulation.congeatio.GlacialCirculationManip;
import com.vincenthuto.hemomancy.common.manipulation.congeatio.GlacialGraspManip;
import com.vincenthuto.hemomancy.common.manipulation.congeatio.OsseousBloomManip;
import com.vincenthuto.hemomancy.common.manipulation.ductilis.ActivationPotentialManip;
import com.vincenthuto.hemomancy.common.manipulation.ductilis.CrimsonHarvestManip;
import com.vincenthuto.hemomancy.common.manipulation.ductilis.HemolymphalPulseManip;
import com.vincenthuto.hemomancy.common.manipulation.ductilis.SanguineWardManip;
import com.vincenthuto.hemomancy.common.manipulation.ferric.ConjurationManip;
import com.vincenthuto.hemomancy.common.manipulation.ferric.FerricResonanceManip;
import com.vincenthuto.hemomancy.common.manipulation.ferric.FerricTransmutationManip;
import com.vincenthuto.hemomancy.common.manipulation.ferric.SanguineExcavationManip;
import com.vincenthuto.hemomancy.common.manipulation.ferric.SanguineMendingManip;
import com.vincenthuto.hemomancy.common.manipulation.ferric.VascularDowsingManip;
import com.vincenthuto.hemomancy.common.manipulation.flammeus.PyreticForgeManip;
import com.vincenthuto.hemomancy.common.manipulation.flammeus.SanguineIgnitionManip;
import com.vincenthuto.hemomancy.common.manipulation.flammeus.VitricCombustionManip;
import com.vincenthuto.hemomancy.common.manipulation.lux.BloodLampManip;
import com.vincenthuto.hemomancy.common.manipulation.lux.CrimsonSightManip;
import com.vincenthuto.hemomancy.common.manipulation.lux.HemosynthesisManip;
import com.vincenthuto.hemomancy.common.manipulation.mortem.ExsanguinateManip;
import com.vincenthuto.hemomancy.common.manipulation.mortem.HemorrhageManip;
import com.vincenthuto.hemomancy.common.manipulation.mortem.VitalReservoirManip;
import com.vincenthuto.hemomancy.common.manipulation.tenebris.BloodEclipseManip;
import com.vincenthuto.hemomancy.common.manipulation.tenebris.UmbralStepManip;
import com.vincenthuto.hemomancy.common.manipulation.tenebris.VoidShroudManip;
import com.vincenthuto.hemomancy.common.manipulation.saint.CrimsonTitheManip;
import com.vincenthuto.hemomancy.common.manipulation.saint.UnclosingEyeManip;
import com.vincenthuto.hemomancy.common.manipulation.saint.BloomOfRotManip;
import com.vincenthuto.hemomancy.common.manipulation.saint.EndlessHourManip;

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
import net.neoforged.neoforge.registries.RegistryBuilder;

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
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.FERRIC, EnumVeinSections.ARMS)
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
					EnumManipulationRank.HUMILIS, EnumBloodTendency.ANIMUS, EnumVeinSections.HEAD)
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

	public static final DeferredHolder<BloodManipulation, BloodManipulation> sanguine_ward = MANIPS.register("sanguine_ward",
			() -> new SanguineWardManip("sanguine_ward", 10, 10, 0, EnumManipulationType.CONTINUOUS,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.DUCTILIS, EnumVeinSections.BODY)
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
			() -> new ConjurationManip("conjure_blade", ItemInit.living_blade, 1000, 0, 0,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.FERRIC, EnumVeinSections.ARMS)
					.setCooldownTicks(40)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, "Not usable by Drudges"));


	public static final DeferredHolder<BloodManipulation, BloodManipulation> blood_absorption = MANIPS.register("blood_absorption",
			() -> new ConjurationManip("blood_absorption", ItemInit.blood_absorption, 1000, 0, 0,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.FERRIC, EnumVeinSections.ARMS)
					.setCooldownTicks(40)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, "Not usable by Drudges"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> blood_projection = MANIPS.register("blood_projection",
			() -> new ConjurationManip("blood_projection", ItemInit.blood_projection, 1000, 0, 0,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.FERRIC, EnumVeinSections.ARMS)
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
					.setCooldownTicks(15)
					.setDrudgeAction((drudge, world, centre, radius) -> {
						AABB area = new AABB(centre).inflate(radius);
						Monster target = world.getEntitiesOfClass(Monster.class, area).stream()
								.min(Comparator.comparingDouble(drudge::distanceToSqr)).orElse(null);
						if (target == null) return false;
						target.igniteForSeconds(6);
						return true;
					}, "Sets nearest hostile on fire"));

	// ── Utilitarian Manipulations ──

	public static final DeferredHolder<BloodManipulation, BloodManipulation> sanguine_mending = MANIPS.register("sanguine_mending",
			() -> new SanguineMendingManip("sanguine_mending", 150, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.FERRIC, EnumVeinSections.ARMS)
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

	public static final DeferredHolder<BloodManipulation, BloodManipulation> crimson_harvest = MANIPS.register("crimson_harvest",
			() -> new CrimsonHarvestManip("crimson_harvest", 200, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.DUCTILIS, EnumVeinSections.LEGS)
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
					.setCooldownTicks(400)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, "Not usable by Drudges"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> ferric_resonance = MANIPS.register("ferric_resonance",
			() -> new FerricResonanceManip("ferric_resonance", 600, 20, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.FERRIC, EnumVeinSections.ARMS)
					.setCooldownTicks(200)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, "Not usable by Drudges"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> pyretic_forge = MANIPS.register("pyretic_forge",
			() -> new PyreticForgeManip("pyretic_forge", 350, 10, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.FLAMMEUS, EnumVeinSections.BODY)
					.setCooldownTicks(30)
					.setDrudgeAction((drudge, world, centre, radius) -> {
						AABB area = new AABB(centre).inflate(radius);
						List<Monster> mobs = world.getEntitiesOfClass(Monster.class, area);
						if (mobs.isEmpty()) return false;
						mobs.forEach(m -> {
							m.igniteForSeconds(10);
							m.hurt(world.damageSources().onFire(), 3.0f);
						});
						return true;
					}, "Burns all hostiles in area"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> umbral_step = MANIPS.register("umbral_step",
			() -> new UmbralStepManip("umbral_step", 300, 10, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.TENEBRIS, EnumVeinSections.LEGS)
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
						if (world.getMaxLocalRawBrightness(dest) > 7) return false;
						drudge.teleportTo(dest.getX() + 0.5, dest.getY(), dest.getZ() + 0.5);
						return true;
					}, "Teleports Drudge to a dark spot within radius"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> crimson_sight = MANIPS.register("crimson_sight",
			() -> new CrimsonSightManip("crimson_sight", 250, 10, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.LUX, EnumVeinSections.HEAD)
					.setCooldownTicks(60)
					.setDrudgeAction((drudge, world, centre, radius) -> {
						AABB area = new AABB(centre).inflate(radius);
						List<Monster> mobs = world.getEntitiesOfClass(Monster.class, area);
						if (mobs.isEmpty()) return false;
						mobs.forEach(m -> m.addEffect(
								new MobEffectInstance(MobEffects.GLOWING, 600, 0, false, true)));
						return true;
					}, "Applies Glowing to nearby hostiles"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> vital_reservoir = MANIPS.register("vital_reservoir",
			() -> new VitalReservoirManip("vital_reservoir", 50, 10, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.MORTEM, EnumVeinSections.HEART)
					.setCooldownTicks(60)
					.setDrudgeAction((drudge, world, centre, radius) -> {
						if (drudge.getBloodCharge() >= DrudgeEntity.MAX_BLOOD_CHARGE * 0.9f) return false;
						AABB area = new AABB(centre).inflate(radius);
						Monster target = world.getEntitiesOfClass(Monster.class, area).stream()
								.min(Comparator.comparingDouble(drudge::distanceToSqr)).orElse(null);
						if (target == null) return false;
						target.hurt(world.damageSources().magic(), 2.0f);
						drudge.fillBloodCharge(200f);
						return true;
					}, "Draws blood from nearby hostile"));

	// ── CONGEATIO — expanded tendencies ──

	public static final DeferredHolder<BloodManipulation, BloodManipulation> cryogenic_pulse = MANIPS.register("cryogenic_pulse",
			() -> new CryogenicPulseManip("cryogenic_pulse", 150, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.CONGEATIO, EnumVeinSections.BODY)
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
					.setCooldownTicks(100));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> glacial_bastion = MANIPS.register("glacial_bastion",
			() -> new GlacialBastionManip("glacial_bastion", 350, 10, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.CONGEATIO, EnumVeinSections.ARMS)
					.setCooldownTicks(50)
					.setDrudgeAction((drudge, world, centre, radius) -> {
						if (!(world instanceof ServerLevel)) return false;
						BlockPos base = drudge.blockPosition();
						int placed = 0;
						for (int r = 0; r < 8; r++) {
							double angle = r * Math.PI / 4.0;
							for (int dy = 0; dy < 3; dy++) {
								BlockPos wallPos = BlockPos.containing(
										base.getX() + 0.5 + 2.0 * Math.cos(angle),
										base.getY() + dy,
										base.getZ() + 0.5 + 2.0 * Math.sin(angle));
								if (world.getBlockState(wallPos).isAir()) {
									world.setBlock(wallPos, Blocks.PACKED_ICE.defaultBlockState(), 3);
									placed++;
								}
							}
						}
						return placed > 0;
					}, "Raises a ring of packed ice around the Drudge"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> osseous_bloom = MANIPS.register("osseous_bloom",
			() -> new OsseousBloomManip("osseous_bloom", 600, 25, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.SUMMA, EnumBloodTendency.CONGEATIO, EnumVeinSections.BODY)
					.setCooldownTicks(60));

	// ── FLAMMEUS — expanded tendencies ──

	public static final DeferredHolder<BloodManipulation, BloodManipulation> sanguine_ignition = MANIPS.register("sanguine_ignition",
			() -> new SanguineIgnitionManip("sanguine_ignition", 125, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.FLAMMEUS, EnumVeinSections.BODY)
					.setCooldownTicks(25)
					.setDrudgeAction((drudge, world, centre, radius) -> {
						AABB area = new AABB(centre).inflate(radius);
						Monster target = world.getEntitiesOfClass(Monster.class, area).stream()
								.min(Comparator.comparingDouble(drudge::distanceToSqr)).orElse(null);
						if (target == null) return false;
						target.igniteForSeconds(8);
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
							m.igniteForSeconds(12);
							m.hurt(world.damageSources().onFire(), dmg);
						});
						return true;
					}, "Heavy fire damage to all hostiles in area"));

	// ── TENEBRIS — expanded tendencies ──

	public static final DeferredHolder<BloodManipulation, BloodManipulation> void_shroud = MANIPS.register("void_shroud",
			() -> new VoidShroudManip("void_shroud", 100, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.TENEBRIS, EnumVeinSections.BODY)
					.setCooldownTicks(20)
					.setDrudgeAction((drudge, world, centre, radius) -> {
						drudge.addEffect(
								new MobEffectInstance(MobEffects.INVISIBILITY, 600, 0, false, false));
						return true;
					}, "Cloaks the Drudge with Invisibility"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> blood_eclipse = MANIPS.register("blood_eclipse",
			() -> new BloodEclipseManip("blood_eclipse", 300, 10, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.TENEBRIS, EnumVeinSections.HEAD)
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

	// ── MORTEM — expanded tendencies ──

	public static final DeferredHolder<BloodManipulation, BloodManipulation> hemorrhage = MANIPS.register("hemorrhage",
			() -> new HemorrhageManip("hemorrhage", 100, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.MORTEM, EnumVeinSections.ARMS)
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

	// ── SAINT — Canon Memories (imprinted from Sainted Mausoleums) ──

	public static final DeferredHolder<BloodManipulation, BloodManipulation> crimson_tithe = MANIPS.register("crimson_tithe",
			() -> new CrimsonTitheManip("crimson_tithe", 400, 25, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.SUMMA, EnumBloodTendency.MORTEM, EnumVeinSections.HEART)
					.setCooldownTicks(100)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, "Not usable by Drudges"));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> unclosing_eye = MANIPS.register("unclosing_eye",
			() -> new UnclosingEyeManip("unclosing_eye", 350, 20, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.SUMMA, EnumBloodTendency.LUX, EnumVeinSections.HEAD)
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
