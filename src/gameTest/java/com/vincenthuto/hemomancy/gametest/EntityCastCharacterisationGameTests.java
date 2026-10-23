package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EntityManipulationEffects;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCastContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Characterisation of every manipulation an entity can cast, before the name switch in
 * {@code EntityManipulationEffects} becomes a registration-time hook (step 11, item 4).
 *
 * <p>Each of the 34 casts the switch knows, and one it does not, is cast by a Husk at a Cow four blocks ahead,
 * through the shared entry point. The fingerprint records whether the cast went off and whether a Will may choose it,
 * both mobs' health, effects, persistent-data keys and movement, the entity types it spawned and the blocks it
 * changed. Random amounts (needle counts, ice durations) are not recorded, only that something appeared.
 */
@GameTestHolder("entity_cast_validation")
@PrefixGameTestTemplate(false)
public final class EntityCastCharacterisationGameTests {
	private static final String GOLDEN = "/entitycast/entity-cast-fingerprints.txt";
	private static final List<String> CASTS = List.of(
			"blood_shot", "blood_needle", "blood_aneurysm", "blood_cloud", "crimson_flame_conjuration",
			"sanguine_ignition", "scalding_updraft", "vitric_combustion", "deadly_gaze", "hemolymphal_pulse",
			"activation_potential", "synaptic_jolt", "conductive_mark", "hematic_flare", "hematic_beacon",
			"prismatic_reproof", "unclosing_eye", "hemorrhage", "exsanguinate", "insatiable_hunger", "grave_debt",
			"bloom_of_rot", "cryogenic_pulse", "glacial_grasp", "glacial_bastion", "glacial_rampart", "pyretic_forge",
			"iron_retort", "sanguine_magnetism", "void_shroud", "gloam_laceration", "umbral_step", "blood_eclipse",
			"blood_eclipse_mantle",
			// No entity cast: it must report that it did nothing.
			"blood_rush");

	private EntityCastCharacterisationGameTests() {
	}

	@GameTest(template = "wide", timeoutTicks = 200, batch = "entity_casts")
	public static void everyEntityCastKeepsItsOutcome(GameTestHelper helper) {
		List<String> lines = new ArrayList<>();
		for (String name : CASTS) lines.add(fingerprint(helper, name));
		PlayerFingerprints.assertGolden(helper, GOLDEN, "entity-cast-fingerprints.actual.txt", String.join("\n", lines) + "\n");
		helper.succeed();
	}

	private static String fingerprint(GameTestHelper helper, String name) {
		ServerLevel level = helper.getLevel();
		BlockPos origin = helper.absolutePos(new BlockPos(48, 2, 48));
		Map<BlockPos, BlockState> blocks = snapshot(level, origin);
		Mob caster = EntityType.HUSK.create(level);
		LivingEntity target = EntityType.COW.create(level);
		caster.moveTo(origin.getX() + 0.5D, origin.getY(), origin.getZ() + 0.5D, -90.0F, 0.0F);
		target.moveTo(origin.getX() + 4.5D, origin.getY(), origin.getZ() + 0.5D, 90.0F, 0.0F);
		caster.setNoAi(true);
		((Mob) target).setNoAi(true);
		level.addFreshEntity(caster);
		level.addFreshEntity(target);
		caster.setTarget(target);
		Set<Integer> present = new HashSet<>();
		for (Entity entity : level.getEntities((Entity) null, new AABB(origin).inflate(40.0D), e -> true)) {
			present.add(entity.getId());
		}
		Vec3 casterStart = caster.position();
		Vec3 targetStart = target.position();

		BloodManipulation manipulation = ManipulationInit.getByName(name);
		boolean supported = EntityManipulationEffects.isSupported(manipulation);
		boolean cast = EntityManipulationEffects.cast(manipulation,
				ManipulationCastContext.forWill(caster, target, ItemStack.EMPTY));

		TreeSet<String> spawned = new TreeSet<>();
		for (Entity entity : level.getEntities((Entity) null, new AABB(origin).inflate(40.0D),
				e -> !present.contains(e.getId()))) {
			spawned.add(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString());
			entity.discard();
		}
		TreeSet<String> changed = new TreeSet<>();
		for (Map.Entry<BlockPos, BlockState> entry : blocks.entrySet()) {
			BlockState now = level.getBlockState(entry.getKey());
			if (now != entry.getValue()) {
				changed.add(BuiltInRegistries.BLOCK.getKey(now.getBlock()).toString());
				level.setBlock(entry.getKey(), entry.getValue(), 2);
			}
		}
		String line = name + " cast=" + cast + " supported=" + supported
				+ " caster=" + describe(caster, casterStart) + " target=" + describe(target, targetStart)
				+ " spawned=" + spawned + " blocks=" + changed;
		caster.discard();
		target.discard();
		return line;
	}

	private static String describe(LivingEntity entity, Vec3 start) {
		TreeSet<String> effects = new TreeSet<>();
		for (MobEffectInstance effect : entity.getActiveEffects()) {
			effects.add(effect.getEffect().unwrapKey().map(key -> key.location().getPath()).orElse("?")
					+ "@" + effect.getAmplifier());
		}
		return "{health=" + String.format(Locale.ROOT, "%.1f", entity.getHealth())
				+ " effects=" + effects
				+ " data=" + new TreeSet<>(entity.getPersistentData().getAllKeys())
				+ " moved=" + String.format(Locale.ROOT, "%.1f", entity.position().distanceTo(start))
				+ " pushed=" + (entity.getDeltaMovement().lengthSqr() > 1.0E-4D) + "}";
	}

	private static Map<BlockPos, BlockState> snapshot(ServerLevel level, BlockPos origin) {
		Map<BlockPos, BlockState> blocks = new HashMap<>();
		for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-8, -1, -8), origin.offset(12, 5, 8))) {
			blocks.put(pos.immutable(), level.getBlockState(pos));
		}
		return blocks;
	}
}
