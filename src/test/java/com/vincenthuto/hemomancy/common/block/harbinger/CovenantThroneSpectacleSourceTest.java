package com.vincenthuto.hemomancy.common.block.harbinger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class CovenantThroneSpectacleSourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");

	private CovenantThroneSpectacleSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String block = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/block/harbinger/functional/CovenantThroneBlock.java"));
		String blockEntity = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/tile/functional/CovenantThroneBlockEntity.java"));
		String model = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/model/tile/functional/CovenantThroneModel.java"));
		String renderer = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/render/tile/functional/CovenantThroneRenderer.java"));
		String itemRenderer = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/render/item/tile/functional/CovenantThroneItemRenderer.java"));
		String entityInit = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/init/EntityInit.java"));

		assertContains("throne should define local true-multiblock filler footprint",
				block, "LOCAL_FILLER_OFFSETS");
		assertContains("throne should rotate filler offsets by facing",
				block, "rotateOffset");
		assertContains("throne should place rotated fillers",
				block, "placeFillers(Level level, BlockPos mainPos, BlockState mainState)");
		assertContains("throne should mount a dedicated invisible seat",
				block, "CovenantThroneSeatEntity");
		assertContains("right-click while mounted should trigger trance",
				block, "isSeatedOnThisThrone");

		assertContains("block entity should cover the whole 3x3x1 render bounds",
				blockEntity, "IMultiBlockEntity");
		assertContains("block entity should not frustum-cull the oversized throne model",
				blockEntity, "AABB.INFINITE");
		assertContains("block entity should sync seated player state",
				blockEntity, "setSeatedPlayer");
		assertContains("block entity should sync trance visual state",
				blockEntity, "startTranceVisual");

		assertContains("entity registry should include the invisible throne seat",
				entityInit, "covenant_throne_seat");

		assertContains("model should include the rear opened-monolith wall",
				model, "rearWall");
		assertContains("model should include side pylons",
				model, "leftPylon");
		assertContains("model should include side pylons",
				model, "rightPylon");
		assertContains("model should include a recessed seat wound",
				model, "seatWound");
		assertContains("model should include crown spires",
				model, "centerCrown");

		assertContains("renderer should brighten when the throne is seated",
				renderer, "getWakeIntensity");
		assertContains("renderer should spread tendrils across the seat",
				renderer, "renderSeatTendrils");
		assertContains("renderer should spread tendrils across the pylons",
				renderer, "renderPylonTendrils");
		assertContains("renderer should keep pylon tendrils on actual pylon geometry",
				renderer, "isOnPylonSide");
		assertContains("renderer should spread tendrils across side faces",
				renderer, "renderSidePanelFaceTendrils");
		assertContains("renderer should keep side face tendrils on pylon geometry",
				renderer, "clampToPylonX");
		assertContains("renderer should define the pylon-safe tendril band",
				renderer, "PYLON_X_INNER");
		assertContains("renderer should spread tendrils across arm tops",
				renderer, "renderArmTopTendrils");
		assertContains("renderer should keep arm-top tendrils on actual arm geometry",
				renderer, "isOnArmTop");
		assertContains("renderer should render a glowing circle of Willis back motif",
				renderer, "renderCircleOfWillisMotif");
		assertContains("renderer should place the Willis motif on the rear slab plane",
				renderer, "BK_REAR_Z");
		assertContains("renderer should only show the Willis motif from the throne back",
				renderer, "shouldRenderRearMotif");
		assertContains("renderer should build the motif from vessel strokes",
				renderer, "renderVesselSegment");
		assertContains("renderer should build curved Willis vessels",
				renderer, "renderWillisCurve");
		assertContains("renderer should use an organic rear vessel loop",
				renderer, "renderWillisOrganicLoop");
		assertContains("renderer should not alternate stale motif buffers mid-draw",
				renderer, "renderWillisSegment(VertexConsumer vc");
		assertContains("renderer should render a trance aura pulse",
				renderer, "renderTranceAura");
		assertContains("renderer should render even when the origin block leaves the frustum",
				renderer, "shouldRenderOffScreen");

		assertContains("item renderer should scale the larger throne model up in GUI",
				itemRenderer, "GUI_MODEL_SCALE");
		assertContains("item renderer should use a cleaner GUI-facing rotation",
				itemRenderer, "Vector3.YP, 28");
	}

	private static String read(Path path) throws IOException {
		if (!Files.exists(path)) {
			throw new AssertionError("missing " + path);
		}
		return Files.readString(path).replace("\r\n", "\n");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}
}
