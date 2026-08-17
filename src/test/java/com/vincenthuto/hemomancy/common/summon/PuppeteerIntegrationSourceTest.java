package com.vincenthuto.hemomancy.common.summon;

import java.nio.file.Files;
import java.nio.file.Path;

public final class PuppeteerIntegrationSourceTest {
	private PuppeteerIntegrationSourceTest() {
	}

	public static void main(String[] args) throws Exception {
		String crossbar = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/MarionetteCrossbarItem.java");
		String spindle = read("src/main/java/com/vincenthuto/hemomancy/common/tile/crafting/PuppeteersSpindleBlockEntity.java");
		String packet = read("src/main/java/com/vincenthuto/hemomancy/common/network/summon/PacketPuppeteersSpindleAction.java");
		String radialPacket = read("src/main/java/com/vincenthuto/hemomancy/common/network/summon/PacketCrossbarRadialAction.java");
		String packetHandler = read("src/main/java/com/vincenthuto/hemomancy/common/network/PacketHandler.java");
		String behavior = read("src/main/java/com/vincenthuto/hemomancy/common/entity/summon/BoundSummonBehavior.java");
		String commands = read("src/main/java/com/vincenthuto/hemomancy/common/summon/PuppeteerCommandEvents.java");
		String will = read("src/main/java/com/vincenthuto/hemomancy/common/entity/mob/monster/will/WillEntity.java");
		String factory = read("src/main/java/com/vincenthuto/hemomancy/common/summon/PuppeteerSummonFactory.java");
		String crossbarCommands = read("src/main/java/com/vincenthuto/hemomancy/common/summon/PuppeteerCrossbarCommands.java");
		String screen = read("src/main/java/com/vincenthuto/hemomancy/client/screen/item/PuppeteersSpindleScreen.java");
		String radialScreen = read("src/main/java/com/vincenthuto/hemomancy/client/screen/summon/CrossbarRadialScreen.java");
		String clientEvents = read("src/main/java/com/vincenthuto/hemomancy/client/event/ClientEvents.java");
		String summonsTab = read("src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/harbinger/SummonsTabView.java");
		String craftingController = read("src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/shared/CraftingTabController.java");
		String craftingView = read("src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/shared/CraftingTabView.java");
		String spawnerScreen = read("src/main/java/com/vincenthuto/hemomancy/client/screen/item/StructureSpawnerScreen.java");
		String placeStructurePacket = read("src/main/java/com/vincenthuto/hemomancy/common/network/PlaceStructurePacket.java");
		String vultureTrial = read("src/main/resources/data/hemomancy/recipe/cardinal_rite/puppeteer_trial_veinwing_vulture.json");
		String spitterTrial = read("src/main/resources/data/hemomancy/recipe/cardinal_rite/puppeteer_trial_marrow_spitter.json");
		String hulkTrial = read("src/main/resources/data/hemomancy/recipe/cardinal_rite/puppeteer_trial_gorebound_hulk.json");
		String mnemonistTrial = read("src/main/resources/data/hemomancy/recipe/cardinal_rite/puppeteer_trial_mnemonist_puppet.json");
		String summonSkills = read("src/main/java/com/vincenthuto/hemomancy/common/init/skills/SummonSkillBranch.java");
		String skillHelper = read("src/main/java/com/vincenthuto/hemomancy/common/capability/player/shared/skill/SkillPointHelper.java");
		String menu = read("src/main/java/com/vincenthuto/hemomancy/common/menu/PuppeteersSpindleMenu.java");
		String vulture = read("src/main/java/com/vincenthuto/hemomancy/common/entity/summon/VeinwingVultureEntity.java");
		String spitter = read("src/main/java/com/vincenthuto/hemomancy/common/entity/summon/MarrowSpitterEntity.java");
		String hulk = read("src/main/java/com/vincenthuto/hemomancy/common/entity/summon/GoreboundHulkEntity.java");
		String puppet = read("src/main/java/com/vincenthuto/hemomancy/common/entity/summon/MnemonistPuppetEntity.java");
		String groundPuppet = read("src/main/java/com/vincenthuto/hemomancy/common/entity/summon/GroundPuppetEntity.java");
		String lang = read("src/main/resources/assets/hemomancy/lang/en_us.json");
		String reference = read("docs/HEMOMANCY_REFERENCE.md");
		String loreReference = read("docs/LORE_REFERENCE.md");
		String harbingerWiki = read("wiki/Harbinger-Path.md");
		String mechanicsWiki = read("wiki/Advanced-Mechanics.md");
		String attachments = read("src/main/java/com/vincenthuto/hemomancy/common/capability/HemoAttachmentTypes.java");
		String bloodShot = read("src/main/java/com/vincenthuto/hemomancy/common/entity/projectile/BloodShotEntity.java");
		String craftingKey = read("src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/BloodCraftingKeyPressPacket.java");
		String craftingHelper = read("src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/BloodStructureCraftingHelper.java");
		String feedManager = read("src/main/java/com/vincenthuto/hemomancy/common/event/BloodStructureFeedManager.java");
		String genericRadial = read("src/main/java/com/vincenthuto/hemomancy/client/screen/radial/GenericRadialMenu.java");
		String textRadialItem = read("src/main/java/com/vincenthuto/hemomancy/client/screen/radial/TextRadialMenuItem.java");
		String threadRenderer = read("src/main/java/com/vincenthuto/hemomancy/client/render/world/PuppeteerThreadRenderer.java");
		String mnemonist = read("src/main/java/com/vincenthuto/hemomancy/common/entity/npc/harbinger/HarbingerMnemonistEntity.java");
		String mnemonistDialogue = read("src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/HarbingerMnemonistDialogueTrees.java");

		assertContains(crossbar, "TAG_BOUND_OWNER");
		assertContains(crossbar, "TAG_THREAD_CAPACITY");
		assertContains(crossbar, "TAG_COMMAND_MODE");
		assertContains(crossbar, "TAG_GUARD_POSITION");
		assertContains(crossbar, "TAG_GUARD_DIMENSION");
		assertContains(crossbar, "getCommandMode");
		assertContains(crossbar, "setGuardAnchor");
		assertContains(crossbar, "clearGuardAnchor");
		assertContains(crossbar, "player.startUsingItem(hand)");
		assertOrdered(crossbar, "if (!isBoundTo(stack, player))", "player.startUsingItem(hand)");
		assertContains(crossbar, "releaseUsing");
		assertContains(crossbar, "elapsed < RADIAL_HOLD_TICKS");
		assertNotContains(crossbar, "cycleSelectedSummon(stack, player)");
		assertContains(clientEvents, "CrossbarRadialScreen");
		assertContains(clientEvents, "MarionetteCrossbarItem.RADIAL_HOLD_TICKS");
		assertContains(radialScreen, "addAllInner");
		assertContains(radialScreen, "PuppeteerCommandMode.values()");
		assertContains(radialScreen, "getSkeinTranspositionLevel");
		assertContains(radialScreen, "PacketCrossbarRadialAction.Action.HOT_SWAP");
		assertContains(radialScreen, "isPauseScreen");
		assertContains(radialScreen, "protected void init()");
		assertContains(radialScreen, "selectionHandled");
		assertContains(radialScreen, "finishSelectionOnce");
		assertNotContains(radialScreen, "else if (!Minecraft.getInstance().options.keyUse.isDown()) menu.clickItem()");
		assertContains(clientEvents, "if (!isKeyDown(mc.options.keyUse))");
		assertContains(radialScreen, "ClientEvents.isKeyDown(Minecraft.getInstance().options.keyUse)");
		assertContains(radialScreen, "menu.setRadii(34, 70, 78, 108)");
		assertContains(radialScreen, "item.setTextLayout(0.8f, 48)");
		assertContains(radialScreen, "item.setTextLayout(0.72f, 54)");
		assertContains(genericRadial, "font.split(currentCentralText, centralTextWidth)");
		assertContains(genericRadial, "centralTextScale");
		assertContains(textRadialItem, "setTextLayout(float scale, int maxWidth)");
		assertContains(clientEvents, "if (mc.screen instanceof CrossbarRadialScreen)");
		assertContains(clientEvents, "!MarionetteCrossbarItem.isBoundTo(mc.player.getUseItem(), mc.player)");
		assertOrdered(radialScreen, "if (definition.name().equals(selected)) return null;",
				"MarionetteCrossbarItem.getThread(crossbar) < cost");
		assertContains(radialScreen, "projectedTotal > totalCap");
		assertContains(summonSkills, "skill_skein_transposition");
		assertContains(summonSkills, "new SkillPoint(49, \"skill_skein_transposition\", 500, 1");
		assertContains(summonSkills, "setRequiredDegree(5)");
		assertContains(summonSkills, "SkillPointInit.skill_far_tether");
		assertContains(skillHelper, "getSkeinTranspositionLevel");
		assertContains(lang, "skill.hemomancy.skill_skein_transposition.desc");
		assertContains(reference, "Skein Transposition");
		assertContains(reference, "**Follow**, **Guard**, **Hunt**, and **Passive**");
		assertContains(loreReference, "Skein Transposition");
		assertContains(harbingerWiki, "Skein Transposition");
		assertContains(mechanicsWiki, "Skein Transposition");
		assertNotContains(reference, "sneak-use cycles known shapes");
		assertNotContains(mechanicsWiki, "sneak-use cycles learned shapes");
		assertContains(crossbarCommands, "setMode(ServerPlayer player");
		assertContains(crossbarCommands, "hotSwap(ServerPlayer player");
		assertOrdered(crossbarCommands, "addFreshEntity(candidate)", "consumeThread(crossbar, summonCost)",
				"oldBody.discard()", "setSelectedSummonName(crossbar, selectedName)");
		assertContains(crossbarCommands, "getSkeinTranspositionLevel(player) <= 0");
		assertContains(crossbarCommands, "projectedShapedCount");
		assertContains(crossbarCommands, "!BoundSummonBehavior.isClaimedWill(body)");
		assertContains(crossbar, "isBoundTo");
		assertContains(crossbar, "getThreadCapacity");
		assertContains(crossbar, "prepareSelectedSummon");
		assertContains(crossbar, "PuppeteerSummonRules.adjustedThreadCost");
		assertContains(crossbar, "PuppeteerSummonRules.effectiveCommandRange");
		assertContains(crossbar, "PuppeteerSummonRules.canRetainBody");
		assertContains(crossbar, "PuppeteerSummonRules.clampThreadToCapacity(tag.getInt(TAG_THREAD), capacity)");
		assertContains(crossbar, "if (!player.level().addFreshEntity(mob))");
		assertNotContains(crossbar, "tickCount % 1200");
		assertContains(spindle, "PuppeteerSummonRules.threadChargeFromItems");
		assertContains(spindle, "MarionetteCrossbarItem.getThreadCapacity(crossbar)");
		assertContains(packet, "menu.prepareSelection(player, msg.summonName)");
		assertNotContains(packet, "Action.BIND");
		assertContains(radialPacket, "UUID crossbarId");
		assertContains(radialPacket, "findEquippedCrossbar(player, msg.crossbarId)");
		assertContains(radialPacket, "PuppeteerCrossbarCommands.setMode");
		assertContains(radialPacket, "PuppeteerCrossbarCommands.hotSwap");
		assertContains(packetHandler, "playToServer(PacketCrossbarRadialAction.TYPE");
		assertNotContains(packet, "SELECT,");
		assertNotContains(packet, "CALL_OR_RECALL");
		assertNotContains(packet, "callOrRecallSelectedSummon(crossbar, player)");
		assertContains(behavior, "PuppeteerSummonRules.upkeepDue");
		assertContains(behavior, "PuppeteerSummonRules.adjustedThreadCost");
		assertContains(behavior, "PuppeteerSummonRules.effectiveCommandRange");
		assertContains(behavior, "PuppeteerSummonRules.interferedThreadUpkeep");
		assertContains(behavior, "MorphlingItem.markFedNow");
		assertOrdered(behavior, "if (!MarionetteCrossbarItem.consumeThread", "MorphlingItem.markFedNow");
		assertContains(behavior, "hasActiveOwnedTether");
		assertContains(behavior, "hemomancy.summon.dimension.unravel");
		assertContains(behavior, "target instanceof Enemy");
		assertContains(behavior, "case PASSIVE -> Optional.empty()");
		assertContains(behavior, "PuppeteerCommandMode.GUARD");
		assertContains(behavior, "case HUNT -> findTarget");
		assertContains(behavior, "findRetaliationTarget");
		assertContains(behavior, "TAG_FOCUS_TARGET");
		assertContains(behavior, "setCommandMode(equippedCrossbar, PuppeteerCommandMode.FOLLOW)");
		assertContains(behavior, "getEntitiesOfClass(Mob.class");
		assertContains(behavior, "TAG_OWNER_SESSION");
		assertContains(behavior, "ownerSessionMatches");
		assertContains(behavior, "reconcileLoadedActiveCap");
		assertOrdered(behavior,
				"if (mob.getTarget() == null || !mob.getTarget().isAlive() || !canAttack(mob, summon, mob.getTarget())",
				"mob.setTarget(null);",
				"findTarget(mob, summon, owner, range)");
		assertContains(behavior, "withinTetherRange(owner.distanceToSqr(mob.getTarget()), range)");
		assertContains(commands, "AttackEntityEvent");
		assertContains(commands, "focusTarget");
		assertContains(commands, "target instanceof Enemy");
		assertContains(commands, "EventPriority.LOWEST");
		assertContains(commands, "PlayerLoggedInEvent");
		assertContains(commands, "PlayerLoggedOutEvent");
		assertContains(commands, "PlayerChangedDimensionEvent");
		assertContains(commands, "PlayerRespawnEvent");
		assertContains(commands, "rotateOwnerSession");
		assertContains(crossbar, "target instanceof Enemy");
		assertContains(factory, "bindOwnerSession");
		assertContains(factory, "setPersistenceRequired");
		assertContains(vulture, "shouldDespawnInPeaceful");
		assertContains(vulture, "if (hemomancy$isTrialSummon()) this.noPhysics = false;");
		assertNotContains(craftingHelper, "PuppeteerTrialRecipe");
		assertNotContains(feedManager, "PuppeteerTrialRecipe");
		assertNotContains(craftingKey, "if (tryActivatePuppeteerTrial(player, sLevel, hitPos))");
		assertContains(spitter, "shouldDespawnInPeaceful");
		assertContains(spitter, "protected boolean isSunBurnTick()");
		assertContains(spitter, "new BloodShotEntity(level(), this)");
		assertContains(spitter, "performRangedAttack");
		assertContains(spitter, "tickBoundOrbit");
		assertContains(spitter, "setNoGravity(true)");
		assertNotContains(spitter, "Items.BOW");
		assertNotContains(spitter, "RangedBowAttackGoal");
		assertNotContains(spitter, "getNavigation().moveTo(owner");
		assertContains(bloodShot, "owner instanceof BoundPuppeteerSummon");
		assertContains(hulk, "shouldDespawnInPeaceful");
		assertContains(hulk, "extends GroundPuppetEntity");
		assertContains(puppet, "shouldDespawnInPeaceful");
		assertContains(puppet, "extends GroundPuppetEntity");
		assertOrdered(puppet, "BoundSummonBehavior.trialServerTick(this, this);", "tickMemoryReplay();", "return;");
		assertContains(groundPuppet, "protected boolean isSunSensitive()");
		assertContains(groundPuppet, "protected boolean convertsInWater()");
		assertContains(groundPuppet, "setCanPickUpLoot(false)");
		assertContains(groundPuppet, "setBaby(false)");
		assertContains(groundPuppet, "populateDefaultEquipmentSlots");
		assertOrdered(will,
				"if (!(stack.getItem() instanceof MarionetteCrossbarItem))",
				"MarionetteCrossbarItem.validateControl(stack, serverPlayer, true)",
				"WillBendRules.resolve");
		assertOrdered(will,
				"outcome.verb() == WillBendRules.BendVerb.COMMANDEER",
				"hemomancy$setOwnerUUID(serverPlayer.getUUID())",
				"setTarget(null)",
				"getNavigation().stop()");
		assertContains(will, "bindOwnerSession(this, serverPlayer)");
		assertContains(will, "setPersistenceRequired");
		assertContains(will, "shouldDespawnInPeaceful");
		assertContains(screen, "sendPrepare(selectedSummonName())");
		assertNotContains(screen, "addActionButtons");
		assertNotContains(screen, "SpindleButton");
		assertContains(menu, "ensureCrossbarAttuned(playerInventory.player)");
		assertContains(menu, "prepareSelection(Player player, String summonName)");
		assertNotContains(screen, "Action.SELECT");
		assertContains(screen, "PuppeteerSummonRules.adjustedThreadCost");
		assertContains(screen, "private static final int GUI_WIDTH = 280;");
		assertContains(screen, "private static final int PATTERN_W = 140;");
		assertNotContains(screen, "statusLabel(");
		assertNotContains(screen, "status_ready");
		assertNotContains(screen, "WORK_X");
		assertNotContains(screen, "WORK_W");
		assertNotContains(screen, "puppeteers_spindle.work");
		assertContains(screen, "drawMeterLabelValue");
		assertContains(screen, "drawThreadSpoolMeter");
		assertContains(screen, "drawThreadWraps");
		assertContains(screen, "drawSpoolEndCaps");
		assertContains(screen, "shadeColor(threadColor, 0.55f)");
		assertContains(screen, "shadeColor(threadColor, 0.78f)");
		assertNotContains(screen, "private void drawMeter(");
		assertContains(screen, "puppeteers_spindle.thread_slot_short");
		assertContains(menu, "78 + col * 18");
		assertContains(summonsTab, "PuppeteerSummonRules.adjustedThreadCost");
		assertContains(summonsTab, "PuppeteerSummonRules.effectiveCommandRange");
		assertContains(radialScreen, "crossbar.interference.gnawed");
		assertContains(threadRenderer, "gnawed");
		assertContains(mnemonist, "BoundSummonBehavior.hasActiveOwnedTether");
		assertContains(mnemonistDialogue, "morphling_puppet_interference");
		assertContains(summonsTab, "primaryOffering(definition)");
		assertNotContains(craftingController, "PuppeteerTrialRecipe");
		assertNotContains(craftingView, "PuppeteerTrialRecipe");
		assertNotContains(spawnerScreen, "PUPPETEER_TRIAL");
		assertNotContains(placeStructurePacket, "PUPPETEER_TRIAL");
		assertContains(vultureTrial, "\"floor\": \"hemomancy:dominion_lesser\"");
		assertContains(vultureTrial, "\"item\": \"hemomancy:veinwing_harness\"");
		assertContains(spitterTrial, "\"item\": \"hemomancy:marrow_spitter_carriage\"");
		assertContains(hulkTrial, "\"item\": \"hemomancy:gorebound_yoke\"");
		assertContains(mnemonistTrial, "\"item\": \"hemomancy:mnemonist_cradle\"");
		assertContains(mnemonistTrial, "\"consume_medium_on_success\": false");
		assertContains(lang, "tether range");
		assertContains(reference, "Mnemonic Ambergris");
		assertContains(reference, "1 item = 8 thread charge");
		assertContains(attachments, "AttachmentType.serializable(KnownSummons::new).copyOnDeath().build()");
	}

	private static String read(String path) throws Exception {
		return Files.readString(Path.of(path));
	}

	private static void assertContains(String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError("Expected source to contain: " + expected);
		}
	}

	private static void assertNotContains(String text, String unexpected) {
		if (text.contains(unexpected)) {
			throw new AssertionError("Expected source not to contain: " + unexpected);
		}
	}

	private static void assertOrdered(String text, String... expected) {
		int cursor = -1;
		for (String value : expected) {
			cursor = text.indexOf(value, cursor + 1);
			if (cursor < 0) {
				throw new AssertionError("Expected source sequence to contain: " + value);
			}
		}
	}
}
