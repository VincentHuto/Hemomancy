package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.armor.ability.SheolicBastionBloodlustArmorAbilityHandler;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.equipment.HarbingerEquipmentEntityEventHandler;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.thelemicmemory.ThelemicMemory;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.EnumSkillStates;
import com.vincenthuto.hemomancy.common.event.LastRiteHelper;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.init.SkillPointInit;
import com.vincenthuto.hemomancy.common.item.harbinger.memory.HematicMemoryToolEvents;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingStrains;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationChannelManager;
import com.vincenthuto.hemomancy.common.rite.unstained.StillWatersSavedData;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Characterisation of the damage stages and death-saves as they stand, before G-1/F-18 gives the damage handlers one
 * stage table (chunk 17 T9). Each row is a real hit through the whole event chain, so a later reorder shows as a
 * named line in a golden. Each line also says how its modifier was armed, so a row that silently stops arming shows too.
 */
@GameTestHolder("combat_order_validation")
@PrefixGameTestTemplate(false)
public final class CombatStageCharacterisationGameTests {
	private static final String ARENA = "ductilis_arena";
	private static final String STAGES = "/combat/damage-stages.txt";
	private static final String SAVES = "/combat/death-saves.txt";
	private static final float HIT = 10.0F;
	private static final float LETHAL = 40.0F;
	private static final Vec3 VICTIM = new Vec3(4.5D, 2.0D, 4.5D);

	private CombatStageCharacterisationGameTests() {
	}

	/** How the hit reaches the victim. */
	private enum Kind {
		MELEE, PROJECTILE, MAGIC, PLAYER, HEMOMANCY_MOB, EFFIGY
	}

	/** Arms one modifier on the victim (or the attacking player) and says what it armed. */
	@FunctionalInterface
	private interface Arm {
		String arm(GameTestHelper h, ServerPlayer victim, LivingEntity attacker);
	}

	private record Row(String name, Kind kind, Arm arm) {
	}

	/**
	 * A fixed 10-point hit on a player with active blood: final damage taken, blood lost and total vein strain, for no
	 * modifier, each modifier alone, two stacks and a hit zeroed late.
	 */
	@GameTest(templateNamespace = "combat_order_validation", template = ARENA, timeoutTicks = 100, batch = "combat_stage_golden")
	public static void damageStagesKeepTheirAmounts(GameTestHelper h) {
		List<Row> rows = List.of(
				new Row("none", Kind.MELEE, (helper, v, a) -> "-"),
				new Row("none", Kind.PROJECTILE, (helper, v, a) -> "-"),
				new Row("none", Kind.MAGIC, (helper, v, a) -> "-"),
				new Row("none", Kind.PLAYER, (helper, v, a) -> "-"),
				new Row("none", Kind.HEMOMANCY_MOB, (helper, v, a) -> "-"),
				new Row("Sheolic stance", Kind.MELEE, (helper, v, a) -> sheolicStance(v)),
				new Row("Silent Archon negation", Kind.MELEE, (helper, v, a) -> silentArchon(v)),
				new Row("avatar", Kind.MELEE, (helper, v, a) -> avatar(v)),
				new Row("sabatons", Kind.PROJECTILE, (helper, v, a) -> sabatons(v)),
				new Row("Chitinite", Kind.PROJECTILE, (helper, v, a) -> chitinite(v)),
				new Row("Iron Will", Kind.MELEE, (helper, v, a) -> ironWill(v)),
				new Row("Still Waters", Kind.MAGIC, (helper, v, a) -> stillWaters(helper, v)),
				new Row("memory tool effigy", Kind.EFFIGY, (helper, v, a) -> "effigy owner=victim"),
				new Row("Second Pulse", Kind.MELEE, (helper, v, a) -> secondPulse(v)),
				new Row("Sanguine Fists", Kind.PLAYER, (helper, v, a) -> sanguineFists(v, (ServerPlayer) a)),
				new Row("Penumbral ambush", Kind.PLAYER, (helper, v, a) -> penumbral((ServerPlayer) a)),
				new Row("Silver Ward (Unstained milestone)", Kind.HEMOMANCY_MOB, (helper, v, a) -> silverWard(v)),
				new Row("avatar + sabatons", Kind.PROJECTILE, (helper, v, a) -> avatar(v) + " " + sabatons(v)),
				new Row("Iron Will + avatar", Kind.MELEE, (helper, v, a) -> ironWill(v) + " " + avatar(v)),
				new Row("zeroed at LOW", Kind.MELEE, (helper, v, a) -> "listener"));
		h.runAfterDelay(2, () -> {
			var server = h.getLevel().getServer();
			boolean pvp = server.isPvpAllowed();
			server.setPvpAllowed(true);
			List<String> lines = new ArrayList<>();
			try {
				for (Row row : rows) lines.add(stageLine(h, row));
				lines.add(mobLine(h, false));
				lines.add(mobLine(h, true));
			} finally {
				server.setPvpAllowed(pvp);
			}
			PlayerFingerprints.assertGolden(h, STAGES, "damage-stages.actual.txt", String.join("\n", lines) + "\n");
			h.succeed();
		});
	}

	private static String stageLine(GameTestHelper h, Row row) {
		ServerPlayer victim = MorphlingPowerGameTests.host(h);
		victim.setGameMode(GameType.SURVIVAL);
		victim.moveTo(h.absoluteVec(VICTIM), 90.0F, 0.0F);
		LivingEntity attacker = attacker(h, row.kind, victim);
		Consumer<LivingDamageEvent.Pre> zero = event -> {
			if (event.getEntity() == victim) event.setNewDamage(0.0F);
		};
		boolean zeroed = row.name.equals("zeroed at LOW");
		try {
			String armed = row.arm.arm(h, victim, attacker);
			if (zeroed) NeoForge.EVENT_BUS.addListener(EventPriority.LOW, zero);
			var blood = HemoCapabilityAccess.getBloodVolume(victim);
			float before = victim.getHealth() + victim.getAbsorptionAmount();
			double bloodBefore = blood.getBloodVolume();
			float veinsBefore = veins(victim);
			boolean landed = victim.hurt(source(victim, row.kind, attacker), HIT);
			float taken = before - victim.getHealth() - victim.getAbsorptionAmount();
			return String.format(Locale.ROOT, "%s | %s | armed %s | landed=%s taken=%.2f blood=%.2f strain=%.2f",
					row.name, row.kind.name().toLowerCase(Locale.ROOT), armed, landed, taken,
					bloodBefore - blood.getBloodVolume(), veinsBefore - veins(victim));
		} finally {
			if (zeroed) NeoForge.EVENT_BUS.unregister(zero);
			if (attacker instanceof ServerPlayer player) ManipulationChannelManager.stop(player, false);
			attacker.discard();
			victim.discard();
		}
	}

	/**
	 * The Absolved attacker bonus only reaches Hemomancy mobs, so its row is a player's 10-point hit on a 40-health
	 * Desiccant, with a control row from a player who has not begun purification.
	 */
	private static String mobLine(GameTestHelper h, boolean absolved) {
		ServerPlayer attacker = MorphlingPowerGameTests.host(h);
		attacker.setGameMode(GameType.SURVIVAL);
		Mob victim = EntityInit.desiccant.get().create(h.getLevel());
		try {
			victim.setNoAi(true);
			victim.moveTo(h.absoluteVec(VICTIM), 90.0F, 0.0F);
			victim.getAttribute(Attributes.MAX_HEALTH).setBaseValue(40.0D);
			victim.setHealth(40.0F);
			h.getLevel().addFreshEntity(victim);
			attacker.moveTo(victim.position().add(-2.0D, 0.0D, 0.0D), -90.0F, 0.0F);
			String armed = "-";
			if (absolved) {
				var progress = HemoCapabilityAccess.getUnstainedProgress(attacker);
				progress.setBegunPurification(true);
				progress.setPurity(80.0F);
				armed = "purity=80";
			}
			float before = victim.getHealth();
			boolean landed = victim.hurt(victim.damageSources().playerAttack(attacker), HIT);
			return String.format(Locale.ROOT, "%s | player on hemomancy_mob | armed %s | landed=%s taken=%.2f",
					absolved ? "Absolved bonus" : "none", armed, landed, before - victim.getHealth());
		} finally {
			victim.discard();
			attacker.discard();
		}
	}

	/**
	 * A lethal 40-point magic hit on a 20-health player with each death-save armed alone and in pairs: who fired, blood
	 * paid, health after, the armed Last Rite and the shared cooldown left. The pair arms its first save, then its
	 * second, so the Last Rite armed before the hit is the second's when both are Last Rites.
	 */
	@GameTest(templateNamespace = "combat_order_validation", template = ARENA, timeoutTicks = 100, batch = "combat_death_save_golden")
	public static void deathSavesKeepTheirOrder(GameTestHelper h) {
		List<String> saves = List.of("Silent Refusal", "Phoenix Debt", "Ink Mantle", "Cryptobiosis", "Curved Horn");
		h.runAfterDelay(2, () -> {
			List<String> lines = new ArrayList<>();
			lines.add(saveLine(h, List.of()));
			for (String save : saves) lines.add(saveLine(h, List.of(save)));
			for (int i = 0; i < saves.size(); i++) {
				for (int j = i + 1; j < saves.size(); j++) lines.add(saveLine(h, List.of(saves.get(i), saves.get(j))));
			}
			PlayerFingerprints.assertGolden(h, SAVES, "death-saves.actual.txt", String.join("\n", lines) + "\n");
			h.succeed();
		});
	}

	private static String saveLine(GameTestHelper h, List<String> saves) {
		String name = saves.isEmpty() ? "none" : String.join(" + ", saves);
		if (saves.contains("Ink Mantle") && saves.contains("Cryptobiosis")) {
			return name + " | cannot arm together: one Morphling slot";
		}
		ServerPlayer victim = MorphlingPowerGameTests.host(h);
		victim.setGameMode(GameType.SURVIVAL);
		victim.moveTo(h.absoluteVec(VICTIM), 90.0F, 0.0F);
		try {
			for (String save : saves) armSave(h, victim, save);
			var blood = HemoCapabilityAccess.getBloodVolume(victim);
			var guardrails = HemoCapabilityAccess.getPowerGuardrails(victim);
			String armed = LastRiteHelper.getArmedSource(victim);
			ItemStack horn = HemoCapabilityAccess.getEquipment(victim).getStackInSlot(5);
			double bloodBefore = blood.getBloodVolume();
			long now = h.getLevel().getGameTime();
			victim.hurt(victim.damageSources().magic(), LETHAL);
			long cooldown = Math.max(0L, guardrails.getLastRiteCooldownUntil() - now);
			return String.format(Locale.ROOT,
					"%s | armed %s | alive=%s health=%.2f paid=%.2f | rite after %s cooldown=%d | horn damage=%d",
					name, armed.isEmpty() ? "-" : armed, victim.isAlive(), victim.getHealth(),
					bloodBefore - blood.getBloodVolume(), LastRiteHelper.getArmedSource(victim).isEmpty() ? "-"
							: LastRiteHelper.getArmedSource(victim), cooldown,
					horn.isEmpty() ? -1 : horn.getDamageValue());
		} finally {
			victim.discard();
		}
	}

	/** F-17: the Horn is a Last Rite, so a second lethal hit inside the shared cooldown kills. */
	@GameTest(templateNamespace = "combat_order_validation", template = ARENA, timeoutTicks = 100, batch = "combat_death_save_golden")
	public static void curvedHornSavesOnceInsideTheSharedCooldown(GameTestHelper h) {
		h.runAfterDelay(2, () -> {
			ServerPlayer victim = MorphlingPowerGameTests.host(h);
			try {
				victim.setGameMode(GameType.SURVIVAL);
				victim.moveTo(h.absoluteVec(VICTIM), 90.0F, 0.0F);
				armSave(h, victim, "Curved Horn");
				h.assertTrue(LastRiteHelper.CURVED_HORN_ID.equals(LastRiteHelper.getArmedSource(victim)),
						"the worn Horn did not arm with nothing else armed");
				victim.hurt(victim.damageSources().magic(), LETHAL);
				h.assertTrue(victim.isAlive() && victim.getHealth() == 1.0F, "the Horn did not save the first death");
				victim.invulnerableTime = 0;
				victim.hurt(victim.damageSources().magic(), LETHAL);
				h.assertTrue(!victim.isAlive(), "the Horn saved a second death inside the shared cooldown");
				h.assertTrue(HemoCapabilityAccess.getEquipment(victim).getStackInSlot(5).getDamageValue() == 1,
						"the Horn wore more than once");
				h.succeed();
			} finally {
				victim.discard();
			}
		});
	}

	/** NQ-17-3: the Horn yields, so a Last Rite armed later takes the slot from it. */
	@GameTest(templateNamespace = "combat_order_validation", template = ARENA, timeoutTicks = 100, batch = "combat_death_save_golden")
	public static void curvedHornYieldsToALaterLastRite(GameTestHelper h) {
		h.runAfterDelay(2, () -> {
			ServerPlayer victim = MorphlingPowerGameTests.host(h);
			try {
				armSave(h, victim, "Curved Horn");
				LastRiteHelper.armForMorphlingIfUnarmed(victim, MorphlingPowerGameTests.equip(victim, MorphlingStrains.INK_CAP, 5));
				h.assertTrue(LastRiteHelper.INK_MANTLE_ID.equals(LastRiteHelper.getArmedSource(victim)),
						"a Morphling Last Rite did not arm over the Horn");
				HarbingerEquipmentEntityEventHandler.syncCurvedHornRite(victim);
				h.assertTrue(LastRiteHelper.INK_MANTLE_ID.equals(LastRiteHelper.getArmedSource(victim)),
						"the Horn took the slot back");
				HemoCapabilityAccess.getEquipment(victim).setStackInSlot(5, ItemStack.EMPTY);
				LastRiteHelper.clearMorphlingRites(victim);
				h.assertTrue(!LastRiteHelper.hasArmedSource(victim), "a removed Horn stayed armed");
				h.succeed();
			} finally {
				victim.discard();
			}
		});
	}

	private static void armSave(GameTestHelper h, ServerPlayer victim, String save) {
		switch (save) {
			case "Silent Refusal" -> {
				silentArchon(victim);
				LastRiteHelper.arm(victim, LastRiteHelper.SILENT_REFUSAL_ID);
			}
			case "Phoenix Debt" -> {
				var phoenix = ManipulationInit.phoenix_debt.get();
				var known = HemoCapabilityAccess.getKnownManipulations(victim);
				known.getKnownManips().put(phoenix, new ManipLevel(4, 185));
				known.setEquippedManipNames(List.of(phoenix.getName()));
				known.togglePassive(phoenix.getName());
				HemoCapabilityAccess.getBloodTemperament(victim).setTemperAlignment(EnumBloodTemper.FLAMMEUS, 100);
				LastRiteHelper.arm(victim, LastRiteHelper.PHOENIX_DEBT_ID);
			}
			case "Ink Mantle" -> LastRiteHelper.armForMorphling(victim,
					MorphlingPowerGameTests.equip(victim, MorphlingStrains.INK_CAP, 5));
			case "Cryptobiosis" -> LastRiteHelper.armForMorphling(victim,
					MorphlingPowerGameTests.equip(victim, MorphlingStrains.HUSHBELL, 5));
			case "Curved Horn" -> {
				HemoCapabilityAccess.getEquipment(victim).setStackInSlot(5, new ItemStack(ItemInit.curved_horn.get()));
				HarbingerEquipmentEntityEventHandler.syncCurvedHornRite(victim);
			}
			default -> throw new IllegalArgumentException(save);
		}
	}

	private static LivingEntity attacker(GameTestHelper h, Kind kind, ServerPlayer victim) {
		Vec3 at = victim.position().add(-2.0D, 0.0D, 0.0D);
		if (kind == Kind.PLAYER) {
			ServerPlayer attacker = MorphlingPowerGameTests.host(h);
			attacker.setGameMode(GameType.SURVIVAL);
			attacker.moveTo(at, -90.0F, 0.0F);
			return attacker;
		}
		EntityType<? extends Mob> type = kind == Kind.HEMOMANCY_MOB ? EntityInit.desiccant.get() : EntityType.HUSK;
		Mob mob = type.create(h.getLevel());
		mob.setNoAi(true);
		mob.moveTo(at, -90.0F, 0.0F);
		h.getLevel().addFreshEntity(mob);
		if (kind == Kind.EFFIGY) {
			mob.getPersistentData().putBoolean(HematicMemoryToolEvents.TAG_EFFIGY, true);
			mob.getPersistentData().putUUID(HematicMemoryToolEvents.TAG_OWNER, victim.getUUID());
		}
		return mob;
	}

	private static DamageSource source(ServerPlayer victim, Kind kind, LivingEntity attacker) {
		var sources = victim.damageSources();
		return switch (kind) {
			case MELEE, HEMOMANCY_MOB, EFFIGY -> sources.mobAttack(attacker);
			case PROJECTILE -> {
				Arrow arrow = new Arrow(EntityType.ARROW, victim.level());
				arrow.setOwner(attacker);
				arrow.setPos(attacker.position());
				yield sources.arrow(arrow, attacker);
			}
			case MAGIC -> sources.magic();
			case PLAYER -> sources.playerAttack((ServerPlayer) attacker);
		};
	}

	private static float veins(ServerPlayer player) {
		float total = 0.0F;
		for (Float health : HemoCapabilityAccess.getVascularSystem(player).getVascularSystem().values()) total += health;
		return total;
	}

	private static void wear(ServerPlayer player, Supplier<Item> head, Supplier<Item> chest, Supplier<Item> legs, Supplier<Item> feet) {
		player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(head.get()));
		player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(chest.get()));
		player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(legs.get()));
		player.setItemSlot(EquipmentSlot.FEET, new ItemStack(feet.get()));
	}

	private static String sheolicStance(ServerPlayer victim) {
		wear(victim, ItemInit.sheolic_blood_lust_helm, ItemInit.sheolic_blood_lust_chest, ItemInit.sheolic_blood_lust_legs,
				ItemInit.sheolic_blood_lust_boots);
		SheolicBastionBloodlustArmorAbilityHandler.activateBastionStance(victim);
		return "bastion=" + SheolicBastionBloodlustArmorAbilityHandler.isBastionActive(victim);
	}

	private static String silentArchon(ServerPlayer victim) {
		var degree = HemoCapabilityAccess.getInitiatoryDegree(victim);
		degree.setDegreeNumber(8);
		degree.setArchonPath(EnumArchonPath.SILENTIUM);
		wear(victim, ItemInit.silent_archon_helm, ItemInit.silent_archon_chestplate, ItemInit.silent_archon_leggings,
				ItemInit.silent_archon_boots);
		return "set=" + com.vincenthuto.hemomancy.common.armor.ability.SilentArchonArmorAbilityHandler.hasFullSilentArchonSet(victim);
	}

	private static String avatar(ServerPlayer victim) {
		HemoCapabilityAccess.getKnownManipulations(victim).setActiveAvatarForm("summon_avatar");
		return "avatar=summon_avatar";
	}

	private static String sabatons(ServerPlayer victim) {
		victim.setItemSlot(EquipmentSlot.FEET, new ItemStack(ItemInit.venous_strider_sabatons.get()));
		return "feet=sabatons";
	}

	private static String chitinite(ServerPlayer victim) {
		wear(victim, ItemInit.chitinite_helm, ItemInit.chitinite_chestplate, ItemInit.chitinite_leggings, ItemInit.chitinite_boots);
		return "set=chitinite";
	}

	private static String ironWill(ServerPlayer victim) {
		HemoCapabilityAccess.getSkillProgress(victim).setSkill(SkillPointInit.skill_iron_will, EnumSkillStates.UNLOCKED, 3);
		HemoCapabilityAccess.getBloodVolume(victim).setBloodVolume(400.0D);
		return "level=3 blood=400/5000";
	}

	private static String stillWaters(GameTestHelper h, ServerPlayer victim) {
		var level = h.getLevel();
		StillWatersSavedData.get(level.getServer().overworld()).addEntry(new StillWatersSavedData.StillWatersEntry(
				victim.getUUID(), victim.blockPosition(), level.dimension().location().toString(), 3, level.getGameTime() + 5L));
		return "zone r=3";
	}

	private static String secondPulse(ServerPlayer victim) {
		var memory = HemoCapabilityAccess.thelemicMemory(victim);
		memory.learnAndAddReserve(ThelemicMemory.SECOND_PULSE, 6000);
		victim.setHealth(8.0F);
		return "enabled=" + (memory.activate(ThelemicMemory.SECOND_PULSE) && memory.isEnabled(ThelemicMemory.SECOND_PULSE))
				+ " health=8";
	}

	private static String sanguineFists(ServerPlayer victim, ServerPlayer attacker) {
		var memory = HemoCapabilityAccess.thelemicMemory(attacker);
		memory.learnAndAddReserve(ThelemicMemory.SANGUINE_FISTS, 6000);
		boolean enabled = memory.activate(ThelemicMemory.SANGUINE_FISTS) && memory.isEnabled(ThelemicMemory.SANGUINE_FISTS);
		NeoForge.EVENT_BUS.post(new AttackEntityEvent(attacker, victim));
		return "enabled=" + enabled + " swing posted";
	}

	private static String penumbral(ServerPlayer attacker) {
		var drift = ManipulationInit.penumbral_drift.get();
		var known = HemoCapabilityAccess.getKnownManipulations(attacker);
		known.getKnownManips().put(drift, new ManipLevel(1, 0));
		known.setEquippedManipNames(List.of(drift.getName()));
		known.setSelectedManip(drift);
		HemoCapabilityAccess.getBloodTemperament(attacker).setTemperAlignment(EnumBloodTemper.TENEBRIS, 100);
		ManipulationChannelManager.start(attacker);
		return "channeling=" + ManipulationChannelManager.isChanneling(attacker.getUUID());
	}

	private static String silverWard(ServerPlayer victim) {
		victim.addEffect(new MobEffectInstance(EffectInit.silver_ward, 200, 0));
		return "silver_ward I";
	}
}
