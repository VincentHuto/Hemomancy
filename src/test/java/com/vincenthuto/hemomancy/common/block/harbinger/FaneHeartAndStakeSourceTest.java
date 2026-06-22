package com.vincenthuto.hemomancy.common.block.harbinger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FaneHeartAndStakeSourceTest {
	private static final Path BLOODWELL = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/functional/ConsecratedBloodwellBlock.java");
	private static final Path STAKE = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/functional/HematicStakeBlock.java");
	private static final Path BLOCK_INIT = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/init/BlockInit.java");
	private static final Path BLOCK_ENTITY_INIT = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/init/BlockEntityInit.java");
	private static final Path BLOODWELL_BLOCK_ENTITY = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/tile/functional/ConsecratedBloodwellBlockEntity.java");
	private static final Path BLOCK_BLOOD_ENDPOINT = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/block/shared/BlockBloodEndpoint.java");
	private static final Path BLOCK_BLOOD_INTERACTIONS = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/block/shared/BlockBloodInteractions.java");
	private static final Path BLOOD_ABSORPTION = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/living/BloodAbsorptionItem.java");
	private static final Path BLOOD_PROJECTION = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/living/BloodProjectionItem.java");
	private static final Path BLOOD_PROJECTION_EVENTS = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/event/BloodProjectionInteractionEvents.java");
	private static final Path STAKE_BLOCK_ENTITY = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/tile/functional/HematicStakeBlockEntity.java");
	private static final Path STAKE_BLOCK_RENDERER = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/render/tile/functional/HematicStakeRenderer.java");
	private static final Path BLOODWELL_RENDERER = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/render/tile/functional/ConsecratedBloodwellRenderer.java");
	private static final Path HEMO_RENDER_TYPES = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/render/HemoRenderTypes.java");
	private static final Path STAKE_ITEM = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tile/HematicStakeBlockItem.java");
	private static final Path STAKE_ITEM_RENDERER = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/render/item/tile/functional/HematicStakeItemRenderer.java");
	private static final Path CLIENT_EVENTS = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/event/ClientEvents.java");
	private static final Path STAKE_AUTHORITY_EVENTS = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/event/worldevent/HematicStakeAuthorityEvents.java");
	private static final Path FANE_DATA = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/event/worldevent/FoundingFaneSavedData.java");
	private static final Path HARBINGER_RITES = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/rite/harbinger/HarbingerCardinalRiteEvents.java");
	private static final Path STAKE_ITEM_MODEL = Path.of(
			"src/main/resources/assets/hemomancy/models/item/hematic_stake.json");
	private static final Path STAKE_BLOCK_MODEL = Path.of(
			"src/main/resources/assets/hemomancy/models/block/hematic_stake.json");
	private static final Path LANG = Path.of("src/main/resources/assets/hemomancy/lang/en_us.json");

	private FaneHeartAndStakeSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String bloodwell = Files.readString(BLOODWELL).replace("\r\n", "\n");
		String stake = Files.readString(STAKE).replace("\r\n", "\n");
		String blockInit = Files.readString(BLOCK_INIT).replace("\r\n", "\n");
		String blockEntityInit = Files.readString(BLOCK_ENTITY_INIT).replace("\r\n", "\n");
		String bloodwellBlockEntity = Files.readString(BLOODWELL_BLOCK_ENTITY).replace("\r\n", "\n");
		String blockBloodEndpoint = Files.readString(BLOCK_BLOOD_ENDPOINT).replace("\r\n", "\n");
		String blockBloodInteractions = Files.readString(BLOCK_BLOOD_INTERACTIONS).replace("\r\n", "\n");
		String bloodAbsorption = Files.readString(BLOOD_ABSORPTION).replace("\r\n", "\n");
		String bloodProjection = Files.readString(BLOOD_PROJECTION).replace("\r\n", "\n");
		String bloodProjectionEvents = Files.readString(BLOOD_PROJECTION_EVENTS).replace("\r\n", "\n");
		String stakeBlockEntity = Files.readString(STAKE_BLOCK_ENTITY).replace("\r\n", "\n");
		String stakeBlockRenderer = Files.readString(STAKE_BLOCK_RENDERER).replace("\r\n", "\n");
		String bloodwellRenderer = Files.readString(BLOODWELL_RENDERER).replace("\r\n", "\n");
		String hemoRenderTypes = Files.readString(HEMO_RENDER_TYPES).replace("\r\n", "\n");
		String stakeItem = Files.readString(STAKE_ITEM).replace("\r\n", "\n");
		String stakeItemRenderer = Files.readString(STAKE_ITEM_RENDERER).replace("\r\n", "\n");
		String clientEvents = Files.readString(CLIENT_EVENTS).replace("\r\n", "\n");
		String stakeAuthorityEvents = Files.readString(STAKE_AUTHORITY_EVENTS).replace("\r\n", "\n");
		String faneData = Files.readString(FANE_DATA).replace("\r\n", "\n");
		String harbingerRites = Files.readString(HARBINGER_RITES).replace("\r\n", "\n");
		String stakeItemModel = Files.readString(STAKE_ITEM_MODEL).replace("\r\n", "\n");
		String stakeBlockModel = Files.readString(STAKE_BLOCK_MODEL).replace("\r\n", "\n");
		String lang = Files.readString(LANG).replace("\r\n", "\n");

		assertContains("bloodwell rejects unregistered placements", bloodwell, "isRegisteredBloodwell");
		assertContains("bloodwell collapses fane on removal", bloodwell, "removeHeartAndGetStakes");
		assertContains("bloodwell breaks associated stakes on removal", bloodwell, "removeBlock(stakePos, false)");
		assertContains("bloodwell uses footprint membership", bloodwell, "isInOwnFane");
		assertContains("bloodwell default constructor does not occlude supporting blocks", bloodwell, ".noOcclusion()");
		assertContains("bloodwell opens bloodline pool screen", bloodwell, "BloodlinePoolScreen.openScreen()");
		assertContains("bloodwell lets absorption/projection tools target endpoint", bloodwell,
				"ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION");
		assertContains("bloodwell implements dynamic block blood endpoint", bloodwell,
				"implements BlockBloodEndpoint");
		assertContains("bloodwell projection requires bound bloodline", bloodwell, "canUseBloodwell(player, pos)");
		assertContains("bloodwell projection rejects outsiders", bloodwell,
				"block.hemomancy.consecrated_bloodwell.not_bloodline");
		assertContains("bloodwell transfers projection directly to shared pool", bloodwell,
				"savedData.contributeBlood(bloodline.getBloodlineUUID()");
		assertContains("bloodwell transfers absorption directly from shared pool", bloodwell,
				"savedData.drawBlood(bloodline.getBloodlineUUID()");
		assertContains("bloodwell syncs changed bloodline pool after endpoint transfer", bloodwell,
				"syncBloodlinePool(player, globalLine)");
		assertNotContains("bloodwell no longer owns fixed 25000ml local buffer", bloodwellBlockEntity, "MAX_BLOOD");
		assertNotContains("bloodwell no longer implements reservoir storage", bloodwellBlockEntity,
				"implements IBloodReservoir");
		assertNotContains("bloodwell no longer saves local blood level", bloodwellBlockEntity, "TAG_BLOOD_LEVEL");
		assertContains("bloodwell block entity syncs bloodline pool fullness", bloodwellBlockEntity,
				"syncFromBloodlinePool");
		assertContains("dynamic block endpoint exposes absorption", blockBloodEndpoint,
				"absorbBloodFromBlock");
		assertContains("dynamic block endpoint exposes projection", blockBloodEndpoint,
				"projectBloodIntoBlock");
		assertContains("dynamic block interaction helper resolves block endpoints", blockBloodInteractions,
				"state.getBlock() instanceof BlockBloodEndpoint");
		assertContains("dynamic block interaction helper absorbs from reservoirs", blockBloodInteractions,
				"tryAbsorbFromReservoir");
		assertContains("dynamic block interaction helper drains reservoir blood", blockBloodInteractions,
				"reservoirVolume.subtractBloodVolume");
		assertContains("dynamic block interaction helper fills player blood from reservoir", blockBloodInteractions,
				"playerVolume.fill");
		assertContains("dynamic block interaction helper updates reservoirs", blockBloodInteractions,
				"reservoir.sendUpdates()");
		assertContains("dynamic block interaction helper syncs player after tile absorption", blockBloodInteractions,
				"new BloodVolumeServerPacket(playerVolume)");
		assertContains("blood absorption tries block endpoints before entity absorption", bloodAbsorption,
				"BlockBloodInteractions.tryAbsorbFromLookedAtBlock");
		assertContains("blood projection tries block endpoints before reservoir handling", bloodProjection,
				"BlockBloodInteractions.tryProjectIntoLookedAtBlock");
		assertContains("blood tool interaction event recognizes block endpoints", bloodProjectionEvents,
				"BlockBloodEndpoint");
		assertContains("blood tool interaction event recognizes absorption", bloodProjectionEvents,
				"BloodAbsorptionItem");
		assertContains("stake validates connected placement", stake, "canPlaceStake");
		assertContains("stake registers into fane", stake, "addStake");
		assertContains("stake unregisters on removal", stake, "removeStake");
		assertContains("stake validates progenitor authority", stake, "isProgenitor");
		assertContains("stake manifests from authority action", stake, "manifestStake");
		assertContains("stake is entity rendered", stake, "RenderShape.ENTITYBLOCK_ANIMATED");
		assertContains("stake creates block entity", stake, "new HematicStakeBlockEntity");
		assertContains("registers hematic stake block", blockInit, "hematic_stake");
		assertContains("bloodwell registration does not occlude supporting blocks", blockInit,
				"sound(SoundType.METAL).noOcclusion().lightLevel(s -> 3)");
		assertContains("stake has no collision", blockInit, ".noCollission()");
		assertContains("stake is instant to mine", blockInit, ".instabreak()");
		assertContains("registers hematic stake block entity", blockEntityInit, "HematicStakeBlockEntity");
		assertContains("stake block item uses custom renderer", blockInit, "new HematicStakeBlockItem");
		assertContains("stake authority event handles empty-hand placement", stakeAuthorityEvents,
				"PlayerInteractEvent.RightClickBlock");
		assertContains("stake authority respects block entity interactions before manifesting", stakeAuthorityEvents,
				"shouldRespectClickedBlockInteraction(level, event.getPos())");
		assertContains("stake authority yields to interactive block entity surfaces", stakeAuthorityEvents,
				"state.hasBlockEntity()");
		assertBefore("stake authority checks clicked block interaction before manifesting stake", stakeAuthorityEvents,
				"shouldRespectClickedBlockInteraction(level, event.getPos())",
				"HematicStakeBlock.manifestStake");
		assertContains("stake authority event handles break protection", stakeAuthorityEvents,
				"BlockEvent.BreakEvent");
		assertContains("stake authority cancels non-owner breaks", stakeAuthorityEvents, "event.setCanceled(true)");
		assertContains("stake owner lookup uses explicit stake list", faneData, "findOwnerForStake");
		assertContains("fane data returns stakes when heart collapses", faneData, "removeHeartAndGetStakes");
		assertContains("fane data returns stakes when reconsecrating", faneData, "removeStakesAndGet");
		assertContains("reconsecration breaks old stakes", harbingerRites, "removeStakesAndGet");
		assertContains("reconsecration removes old stake blocks", harbingerRites, "removeBlock(stakePos, false)");
		assertContains("stake block entity is render-only", stakeBlockEntity, "sole purpose is");
		assertContains("stake block renderer uses monolith material", stakeBlockRenderer,
				"HemoRenderTypes.monolithFragment");
		assertContains("stake renderer draws jagged sovereign shard", stakeBlockRenderer, "drawJaggedStakeShard");
		assertContains("bloodwell renderer draws central fountain jet", bloodwellRenderer, "renderCentralBloodJet");
		assertContains("bloodwell renderer draws falling fountain arcs", bloodwellRenderer, "renderFountainArc");
		assertContains("bloodwell renderer responds to stored blood", bloodwellRenderer, "getStoredBlood");
		assertContains("bloodwell renderer uses unclamped pressure scaling", bloodwellRenderer, "bloodPressure(fullness)");
		assertContains("bloodwell renderer keeps low wells modest", bloodwellRenderer, "LOW_JET_HEIGHT");
		assertContains("bloodwell renderer expands when full", bloodwellRenderer, "FULL_JET_HEIGHT");
		assertContains("bloodwell arc count scales with fullness", bloodwellRenderer, "activeArcCount");
		assertContains("bloodwell renderer spawns real particles", bloodwellRenderer, "spawnBloodwellParticles");
		assertContains("bloodwell renderer uses actual level particles", bloodwellRenderer, "level.addParticle");
		assertContains("bloodwell renderer uses absorbed blood cell particles", bloodwellRenderer,
				"AbsorbedBloodCellParticleFactory");
		assertContains("bloodwell renderer uses glow particles", bloodwellRenderer, "GlowParticleFactory");
		assertContains("bloodwell renderer uses blood particle color", bloodwellRenderer, "ParticleColor.BLOOD");
		assertNotContains("bloodwell renderer removes fake tip mist geometry", bloodwellRenderer, "renderJetMist");
		assertNotContains("bloodwell renderer removes fake ambient mote geometry", bloodwellRenderer, "renderBloodMotes");
		assertNotContains("bloodwell renderer removes crossed quad motes", bloodwellRenderer, "drawMote");
		assertNotContains("bloodwell renderer removes renderer mote budget", bloodwellRenderer, "MOTE_COUNT");
		assertNotContains("bloodwell renderer removes flat crown quad", bloodwellRenderer, "topY + crown");
		assertContains("bloodwell renderer smooths stream arcs", bloodwellRenderer, "renderSmoothFountainArc");
		assertContains("bloodwell stream arcs use tapered ribbons", bloodwellRenderer, "taperedRibbon");
		assertContains("bloodwell stream arcs use continuous alpha", bloodwellRenderer, "arcAlphaAt");
		assertNotContains("bloodwell stream arcs avoid segment overlap padding", bloodwellRenderer, "ARC_STRIP_OVERLAP");
		assertNotContains("bloodwell stream arcs do not backtrack segment starts", bloodwellRenderer,
				"- ARC_STRIP_OVERLAP");
		assertNotContains("bloodwell stream arcs do not extend segment ends", bloodwellRenderer,
				"+ ARC_STRIP_OVERLAP");
		assertNotContains("bloodwell stream arcs remove phased texture chunks", bloodwellRenderer, "phase * 0.6f");
		assertNotContains("bloodwell stream arcs avoid end gaps", bloodwellRenderer, "if (t1 > 1.0f)");
		assertContains("bloodwell renderer is animated", bloodwellRenderer, "Mth.sin");
		assertContains("bloodwell renderer uses blood fountain render type", hemoRenderTypes, "BLOODWELL_FOUNTAIN");
		assertContains("bloodwell fountain render type writes depth for water sorting",
				renderTypeBlock(hemoRenderTypes, "BLOODWELL_FOUNTAIN"), "setWriteMaskState(RenderType.COLOR_DEPTH_WRITE)");
		assertContains("stake renderer registered", clientEvents,
				"BlockEntityRenderers.register(BlockEntityInit.hematic_stake.get(), HematicStakeRenderer::new)");
		assertContains("bloodwell renderer registered", clientEvents,
				"BlockEntityRenderers.register(BlockEntityInit.consecrated_bloodwell.get(), ConsecratedBloodwellRenderer::new)");
		assertContains("stake item implements custom renderer provider", stakeItem,
				"implements HemoClientItemExtensionsProvider");
		assertContains("stake item renderer uses monolith material", stakeItemRenderer,
				"HemoRenderTypes.monolithFragment");
		assertContains("stake item model routes to custom renderer", stakeItemModel, "\"parent\": \"builtin/entity\"");
		assertContains("stake block model no longer draws lightning-rod geometry", stakeBlockModel, "\"elements\": []");
		assertContains("adds hematic stake translation", lang, "\"block.hemomancy.hematic_stake\"");
		assertContains("adds bloodwell bloodline-gate translation", lang,
				"\"block.hemomancy.consecrated_bloodwell.not_bloodline\"");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}

	private static void assertNotContains(String label, String text, String unexpected) {
		if (text.contains(unexpected)) {
			throw new AssertionError(label + ": unexpected " + unexpected);
		}
	}

	private static void assertBefore(String label, String text, String first, String second) {
		int firstIndex = text.indexOf(first);
		int secondIndex = text.indexOf(second);
		if (firstIndex < 0 || secondIndex < 0 || firstIndex > secondIndex) {
			throw new AssertionError(label + ": expected " + first + " before " + second);
		}
	}

	private static String renderTypeBlock(String text, String constantName) {
		int start = text.indexOf("public static final RenderType " + constantName);
		if (start < 0) {
			throw new AssertionError("missing render type constant " + constantName);
		}
		int end = text.indexOf("public static", start + 1);
		return end >= 0 ? text.substring(start, end) : text.substring(start);
	}
}
