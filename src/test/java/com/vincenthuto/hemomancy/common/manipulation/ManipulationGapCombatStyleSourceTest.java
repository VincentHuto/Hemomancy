package com.vincenthuto.hemomancy.common.manipulation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ManipulationGapCombatStyleSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();
	private static final String[] NEW_MANIPULATIONS = {
			"synaptic_jolt",
			"conductive_mark",
			"insatiable_hunger",
			"grave_debt",
			"iron_retort",
			"sanguine_magnetism"
	};
	private static final String[] NEW_EFFECTS = {
			"conductive_mark",
			"insatiable_hunger",
			"grave_debt",
			"iron_retort"
	};

	private ManipulationGapCombatStyleSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		manipulationsAreRegisteredWithPlannedCostsAndCooldowns();
		statusEffectsAndHooksAreRegistered();
		memoryItemsAndResourcesExist();
		treeAndEntityCastingSupportExists();
		playerImplementationsExposePlannedCombatRoles();
		documentationMentionsNewManipulations();
	}

	private static void manipulationsAreRegisteredWithPlannedCostsAndCooldowns() throws IOException {
		String manipInit = read("src/main/java/com/vincenthuto/hemomancy/common/init/ManipulationInit.java");

		assertContains("synaptic jolt registration", manipInit,
				"new SynapticJoltManip(\"synaptic_jolt\", 150");
		assertContains("synaptic jolt rank/tendency", manipInit,
				"EnumManipulationRank.HUMILIS, EnumBloodTendency.DUCTILIS, EnumVeinSections.HEAD");
		assertContains("synaptic jolt cooldown", manipInit, ".setCooldownTicks(25)");

		assertContains("conductive mark registration", manipInit,
				"new ConductiveMarkManip(\"conductive_mark\", 225");
		assertContains("conductive mark secondary", manipInit,
				".setSecondaryTend(EnumBloodTendency.FERRIC)");
		assertContains("conductive mark cooldown", manipInit, ".setCooldownTicks(50)");

		assertContains("insatiable hunger registration", manipInit,
				"new InsatiableHungerManip(\"insatiable_hunger\", 225");
		assertContains("insatiable hunger cooldown", manipInit, ".setCooldownTicks(70)");

		assertContains("grave debt registration", manipInit,
				"new GraveDebtManip(\"grave_debt\", 325");
		assertContains("grave debt cooldown", manipInit, ".setCooldownTicks(75)");

		assertContains("iron retort registration", manipInit,
				"new IronRetortManip(\"iron_retort\", 250");
		assertContains("iron retort secondary", manipInit,
				".setSecondaryTend(EnumBloodTendency.DUCTILIS)");
		assertContains("iron retort cooldown", manipInit, ".setCooldownTicks(80)");

		assertContains("sanguine magnetism registration", manipInit,
				"new SanguineMagnetismManip(\"sanguine_magnetism\", 450");
		assertContains("sanguine magnetism rank/tendency", manipInit,
				"EnumManipulationRank.SUMMA, EnumBloodTendency.FERRIC, EnumVeinSections.BODY");
		assertContains("sanguine magnetism cooldown", manipInit, ".setCooldownTicks(140)");
	}

	private static void statusEffectsAndHooksAreRegistered() throws IOException {
		String effectInit = read("src/main/java/com/vincenthuto/hemomancy/common/init/EffectInit.java");
		String events = read("src/main/java/com/vincenthuto/hemomancy/common/manipulation/ManipulationStatusEvents.java");
		String helper = read("src/main/java/com/vincenthuto/hemomancy/common/manipulation/SchoolHitHelper.java");
		String weaponEvents = read("src/main/java/com/vincenthuto/hemomancy/common/event/TendencyWeaponCombatEvents.java");

		for (String effect : NEW_EFFECTS) {
			assertContains(effect + " effect registered", effectInit, "EFFECTS.register(\"" + effect + "\"");
			assertResource(effect + " effect icon",
					"src/main/resources/assets/hemomancy/textures/mob_effect/" + effect + ".png");
		}
		assertContains("healing hook", events, "onLivingHeal(LivingHealEvent event)");
		assertContains("food hook", events, "onLivingItemFinish(LivingEntityUseItemEvent.Finish event)");
		assertContains("incoming damage hook", events, "onLivingIncomingDamage(LivingIncomingDamageEvent event)");
		assertContains("post damage hook", events, "onLivingDamagePost(LivingDamageEvent.Post event)");
		assertContains("death hook", events, "onLivingDeath(LivingDeathEvent event)");
		assertContains("school helper conductive trigger", helper, "tryTriggerConductiveArc");
		assertContains("living weapon reports school hit", weaponEvents, "SchoolHitHelper.tryTriggerConductiveArc");
	}

	private static void memoryItemsAndResourcesExist() throws IOException {
		String itemInit = read("src/main/java/com/vincenthuto/hemomancy/common/init/ItemInit.java");
		String lang = read("src/main/resources/assets/hemomancy/lang/en_us.json");
		for (String id : NEW_MANIPULATIONS) {
			assertContains(id + " memory item", itemInit,
					"memory_" + id + " = BASEITEMS.register(\"memory_" + id + "\"");
			assertContains(id + " item lang", lang,
					"\"item.hemomancy.memory_" + id + "\": \"Memory " + title(id) + "\"");
			assertContains(id + " manipulation desc", lang,
					"\"hemomancy.manipulation." + id + ".desc\"");
			assertContains(id + " inquiry lang", lang,
					"\"hemomancy.mnemonist.item_inquiry.memory_" + id + ".line1\"");
			assertResource(id + " model",
					"src/main/resources/assets/hemomancy/models/item/memory_" + id + ".json");
			assertResource(id + " overlay",
					"src/main/resources/assets/hemomancy/textures/item/memories/memory_" + id + "_overlay.png");
			assertResource(id + " dialogue",
					"src/main/resources/data/hemomancy/dialogue_inquiry/mnemonist/hemomancy/memory_" + id + ".json");
			assertResource(id + " recipe",
					"src/main/resources/data/hemomancy/recipe/memory_weaving/memory_" + id + ".json");
		}
	}

	private static void treeAndEntityCastingSupportExists() throws IOException {
		String treeInit = read("src/main/java/com/vincenthuto/hemomancy/common/init/ManipulationTreeInit.java");
		String entityEffects = read("src/main/java/com/vincenthuto/hemomancy/common/manipulation/EntityManipulationEffects.java");
		for (String id : NEW_MANIPULATIONS) {
			assertContains(id + " tree node", treeInit, "register(\"" + id + "\"");
			assertContains(id + " entity cast support", entityEffects, "\"" + id + "\"");
		}
	}

	private static void playerImplementationsExposePlannedCombatRoles() throws IOException {
		String synaptic = read("src/main/java/com/vincenthuto/hemomancy/common/manipulation/ductilis/SynapticJoltManip.java");
		assertContains("synaptic base damage", synaptic, "BASE_DAMAGE = 3.0F");
		assertContains("synaptic stagger", synaptic, "staggerTarget");
		assertContains("synaptic lightning", synaptic, "DuctilisLightningEffects.synapticJolt");
		assertContains("synaptic reports conductive school", synaptic, "EnumBloodTendency.DUCTILIS");

		String conductive = read("src/main/java/com/vincenthuto/hemomancy/common/manipulation/ductilis/ConductiveMarkManip.java");
		assertContains("conductive duration", conductive, "DURATION_TICKS = 160");
		assertContains("conductive effect", conductive, "EffectInit.conductive_mark");
		assertContains("conductive metadata", conductive, "SchoolHitHelper.markConductive");

		String hunger = read("src/main/java/com/vincenthuto/hemomancy/common/manipulation/mortem/InsatiableHungerManip.java");
		assertContains("hunger duration", hunger, "DURATION_TICKS = 220");
		assertContains("hunger effect", hunger, "EffectInit.insatiable_hunger");

		String grave = read("src/main/java/com/vincenthuto/hemomancy/common/manipulation/mortem/GraveDebtManip.java");
		assertContains("grave duration", grave, "DURATION_TICKS = 180");
		assertContains("grave effect", grave, "EffectInit.grave_debt");
		assertContains("grave owner metadata", grave, "SchoolHitHelper.markGraveDebt");

		String retort = read("src/main/java/com/vincenthuto/hemomancy/common/manipulation/ferric/IronRetortManip.java");
		assertContains("retort duration", retort, "DURATION_TICKS = 60");
		assertContains("retort effect", retort, "EffectInit.iron_retort");

		String magnetism = read("src/main/java/com/vincenthuto/hemomancy/common/manipulation/ferric/SanguineMagnetismManip.java");
		assertContains("magnetism duration", magnetism, "DURATION_TICKS = 120");
		assertContains("magnetism magnetic flag", magnetism, "setMagnetic");
		assertContains("magnetism uses iron pillar", magnetism, "EntityIronPillar");

		String pillar = read("src/main/java/com/vincenthuto/hemomancy/common/entity/summon/EntityIronPillar.java");
		assertContains("pillar magnetic field", pillar, "MAGNETIC_RADIUS = 8.0D");
		assertContains("pillar hostile only", pillar, "target instanceof Monster");
	}

	private static void documentationMentionsNewManipulations() throws IOException {
		String reference = read("docs/HEMOMANCY_REFERENCE.md");
		for (String id : NEW_MANIPULATIONS) {
			assertContains(id + " docs", reference, title(id));
		}
	}

	private static String read(String path) throws IOException {
		return Files.readString(ROOT.resolve(path)).replace("\r\n", "\n");
	}

	private static void assertResource(String label, String path) {
		if (!Files.exists(ROOT.resolve(path))) {
			throw new AssertionError(label + " missing at " + path);
		}
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing `" + expected + "`");
		}
	}

	private static String title(String id) {
		StringBuilder title = new StringBuilder();
		for (String part : id.split("_")) {
			if (!title.isEmpty()) {
				title.append(' ');
			}
			title.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
		}
		return title.toString();
	}
}
