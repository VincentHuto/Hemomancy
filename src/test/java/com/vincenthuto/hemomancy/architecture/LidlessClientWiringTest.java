package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The Lidless Door crossing reaches the client through the existing paths: its state payload is dispatched to one
 * client handler, its client state is cleared with the session, and its client events hold the crossing player.
 */
class LidlessClientWiringTest {
	@Test
	void statePacketIsDispatchedToTheClientHandler() {
		String handlers = SourceAssertions.readMainJava("client/network/ClientboundEffectHandlers.java");
		assertTrue(handlers.contains("ClientPayloadHandlers.register(LidlessPassageStatePacket.TYPE"),
				"the state payload is dispatched through ClientboundEffectHandlers");
	}

	@Test
	void clientStateIsClearedWithTheSession() {
		String session = SourceAssertions.readMainJava("client/event/ClientSessionEvents.java");
		assertTrue(session.contains("LidlessPassageClientState.clear()"), "ClientSessionEvents clears the crossing state");
		String bridge = SourceAssertions.readMainJava("client/ClientBridgeImpl.java");
		assertTrue(bridge.contains("LidlessPassageClientState.isCrossing("), "the bridge answers from the client state");
	}

	@Test
	void crossingViewDrawsThroughTheExistingOverlayAndDispatcher() {
		String overlay = SourceAssertions.readMainJava("client/event/ScreenOverlayEvents.java");
		assertTrue(overlay.contains("LidlessPassageView.renderGrade("), "the view is a full-screen grade under the HUD");
		String dispatcher = SourceAssertions.readMainJava("client/render/world/WorldEffectDispatcher.java");
		assertTrue(dispatcher.contains("LidlessPassageView.captureProjection(event)"),
				"the view reads the level projection from the dispatcher, not its own subscriber");
		assertTrue(dispatcher.contains("ChamberProxyView.render(event)"),
				"the Chamber stand-in is drawn in the dispatcher's sky stage, before any block entity samples it");
		String husk = SourceAssertions.readMainJava("client/render/tile/harbinger/functional/SanguineMonolithRenderer.java");
		assertTrue(husk.contains("ChamberProxyView.request(te, facing)"), "the husk asks for the view as it draws its eye");
		String session = SourceAssertions.readMainJava("client/event/ClientSessionEvents.java");
		assertTrue(session.contains("ChamberProxyView.clear()"), "and the view goes with the level");
		assertTrue(dispatcher.contains("LidlessPassageView.renderFocus(event)"),
				"the seed mask is drawn from the dispatcher, not its own subscriber");
	}

	@Test
	void theLiveEyePassDrawsTheOwnersRoomTheWayTheLevelIsDrawn() {
		String pass = SourceAssertions.readMainJava("client/render/world/chamberofwill/ChamberProxyView.java");
		assertTrue(pass.contains("ChamberSkyThemeRegistry.withTheme("),
				"the sky effects read the active theme, so the pass draws under the snapshot's theme, not the viewer's own");
		assertTrue(pass.contains("modelViewStack.mul(modelView)"),
				"the camera's rotation goes on the model-view stack as the level renderer puts it, so shaders that read Position as camera-relative world space (the inner eye's void) stay right");
		assertFalse(pass.contains("pose.mulPose(modelView)"), "and the world passes use identity poses, not the rotation folded in");
		assertTrue(pass.contains("RenderSystem.getShaderFogColor().clone()"), "the fog colour is copied before the pass, not aliased");
		assertTrue(pass.indexOf("RenderSystem.depthMask(true)") < pass.indexOf("GL11.GL_DEPTH_BUFFER_BIT"), "the depth mask is on before the depth clear");
		assertTrue(pass.contains("standIn.tickCount = (int)"), "the stand-in's clock is the level's, not the frame counter");
		assertFalse(pass.contains("\"silent_archon\""), "the inner eye's theme comes from the husk, not from a theme id string");
		assertTrue(pass.contains("public static void invalidateBake()"), "a resource reload rebakes the room's blocks");
		String reload = SourceAssertions.readMainJava("client/registry/ClientReloadListenerRegistrar.java");
		assertTrue(reload.contains("ChamberProxyView.invalidateBake()"), "and is wired to the reload listener");
	}

	@Test
	void clientEventsHoldTheCrossingPlayer() {
		String events = SourceAssertions.readMainJava("client/event/LidlessPassageClientEvents.java");
		for (String hook : List.of("RenderPlayerEvent.Pre", "RenderGuiLayerEvent.Pre", "MovementInputUpdateEvent",
				"RenderHandEvent", "InteractionKeyMappingTriggered", "RenderBlockScreenEffectEvent")) {
			assertTrue(events.contains(hook), hook);
		}
	}

	@Test
	void theSeedInTheCrossingIsDrawnUnderTheCamerasRotation() {
		// AFTER_LEVEL fires after the level renderer has popped the camera's rotation off the model-view stack, so a
		// pass that draws world positions there must put it back, or the Seed sits where the camera points.
		String view = SourceAssertions.readMainJava("client/screen/overlay/LidlessPassageView.java");
		assertTrue(view.contains("modelViewStack.mul(event.getModelViewMatrix())"),
				"the focus pass puts the camera's rotation on the model-view stack before drawing the Seed");
	}

	@Test
	void theLiveEyeIsAWindowAtTheGateNotAViewOfIt() {
		String pass = SourceAssertions.readMainJava("client/render/world/chamberofwill/ChamberProxyView.java");
		assertFalse(pass.contains("LidlessPortalPanel.draw("), "the gate's own eye is the window being looked through, never drawn in the view");
		String rules = SourceAssertions.readMainJava("client/render/world/chamberofwill/ChamberProxyRules.java");
		assertFalse(rules.contains("PUSH"), "the view stands where the viewer stands, not pushed beyond the gate");
	}

	@Test
	void thePortalPanelWritesDepthSoWhatIsBehindItStaysBehind() {
		// Block entities draw before the translucent chunk pass: a panel that wrote no depth let water behind it draw over
		// the void. The body is drawn hard and writes depth; the rim's soft fringe is drawn again without, so a fringe
		// pixel never writes a depth that would cull what is behind it after it had blended over the sky.
		String types = SourceAssertions.readMainJava("client/render/HemoRenderTypes.java");
		int solid = types.indexOf("RenderType.create(\"lidless_portal_solid\"");
		int solidEnd = types.indexOf("createCompositeState", solid);
		assertTrue(solid > 0 && solidEnd > solid, "a solid portal type exists");
		assertTrue(types.substring(solid, solidEnd).contains("RenderType.COLOR_DEPTH_WRITE"), "the solid body writes depth");
		int soft = types.indexOf("RenderType.create(\"lidless_portal\"");
		int softEnd = types.indexOf("createCompositeState", soft);
		assertTrue(types.substring(soft, softEnd).contains("RenderType.COLOR_WRITE"), "the soft fringe does not");
		String panel = SourceAssertions.readMainJava("client/render/tile/shared/LidlessPortalPanel.java");
		assertTrue(panel.indexOf("HemoRenderTypes.lidlessPortalSolid(") < panel.indexOf("HemoRenderTypes.lidlessPortal("), "the body first, then the fringe");
		assertFalse(panel.contains("seed + side * 0.5F"), "both faces share one rim, so the back face's rim never peeks out past the front's");
	}

	@Test
	void theWalkInLooksIntoTheChamberThroughTheHole() {
		// On a gateway crossing the hole at the end of the passage is the same window the eye was: the live view,
		// its camera carried inside the gate as far as the walk has gone, so the Chamber comes closer step by step.
		String view = SourceAssertions.readMainJava("client/screen/overlay/LidlessPassageView.java");
		assertTrue(view.contains("ChamberProxyView.requestCrossing("), "the view asks for the Chamber drawn from the walk's camera");
		assertTrue(view.contains("ChamberProxyView.textureFor(view.door())"), "and hands the hole that texture");
		assertTrue(view.contains("RenderSystem.setShaderTexture(2, "), "in sampler slot 2");
		String proxy = SourceAssertions.readMainJava("client/render/world/chamberofwill/ChamberProxyView.java");
		assertTrue(proxy.contains("public static void requestCrossing("), "the proxy view takes a crossing's request");
	}

	@Test
	void theWalkOutLooksBackAtTheOverworldThroughTheHole() {
		String view = SourceAssertions.readMainJava("client/screen/overlay/LidlessPassageView.java");
		assertTrue(view.contains("ChamberProxyView.requestReturn("), "the walk out asks for the overworld around the husk");
		String proxy = SourceAssertions.readMainJava("client/render/world/chamberofwill/ChamberProxyView.java");
		assertTrue(proxy.contains("public static void requestReturn("), "drawn by the proxy view from the gate's outside snapshot");
		String gate = SourceAssertions.readMainJava("common/tile/shared/ChamberGateBlockEntity.java");
		assertTrue(gate.contains("public HuskOutsideSnapshot outside()"), "which the gate carries to the client");
		String husk = SourceAssertions.readMainJava("common/tile/harbinger/functional/SanguineMonolithBlockEntity.java");
		assertTrue(husk.contains("HuskOutsideSnapshot.take("), "taken by the husk when it takes the room");
	}

	@Test
	void theGatesEyeInTheChamberShowsTheOverworld() {
		String gate = SourceAssertions.readMainJava("client/render/tile/shared/ChamberGateRenderer.java");
		assertTrue(gate.contains("ChamberProxyView.requestGate(gate)"), "the gate asks for the overworld around its husk");
		assertTrue(gate.contains("ChamberProxyView.textureFor(gate.getBlockPos())"), "and shows it in its eye");
		String proxy = SourceAssertions.readMainJava("client/render/world/chamberofwill/ChamberProxyView.java");
		assertTrue(proxy.contains("public static void requestGate("), "drawn by the proxy view from the gate's outside snapshot");
	}

	@Test
	void theWalkContinuesTheEyeAndTheVeilContinuesTheWalk() {
		// From outside the eye is a diamond as wide as the view; the walk must not start over as a small round hole.
		String view = SourceAssertions.readMainJava("client/screen/overlay/LidlessPassageView.java");
		assertTrue(view.contains("LidlessHoleRules.gatewayRadius(along)"), "a gateway walk's hole starts at the eye's size and opens past the view");
		assertTrue(view.contains("\"EyeShape\""), "and is the eye's diamond, not the Seed walk's round hole");
		String state = SourceAssertions.readMainJava("client/data/LidlessPassageClientState.java");
		assertTrue(state.contains("ChamberProxyView.detachTarget(ended.door())"), "the walk's last live frame is handed to the veil");
		String screen = SourceAssertions.readMainJava("client/screen/ChamberTransitionScreen.java");
		assertTrue(screen.contains("armed.holeRadius()") && screen.contains("armed.kept()"), "which draws on from the walk's hole with that frame in it");
		assertTrue(screen.contains("ChamberVeil.peek()") && screen.contains("ChamberVeil.release(armed)"),
				"read without spending, since the client builds the screen more than once over one change, and spent by the one the level arrived under");
		assertTrue(screen.contains("public void removed()"), "and lets the frame go when the level is in");
	}

	@Test
	void theBakedBlocksKeepTheirTint() {
		// Grass and leaves are tinted quads; baked white they come out grey, and the overworld through the gate looked
		// black and white. Without a level to read a biome from, the default tints are used.
		String proxy = SourceAssertions.readMainJava("client/render/world/chamberofwill/ChamberProxyView.java");
		assertTrue(proxy.contains("mc.getBlockColors().getColor(state, null, null, 0)"), "each block's default tint is looked up");
		assertFalse(proxy.contains("model, 1.0F, 1.0F, 1.0F, FULL_BRIGHT"), "and not baked white");
	}

	@Test
	void theOverworldThroughTheGateFadesIntoAHazeNotAVoid() {
		// The snapshot ends twelve blocks out; beyond it a flat sky colour read as an unlit void. The blocks fog out
		// into a horizon haze over the last half of that reach, under a graded sky.
		String proxy = SourceAssertions.readMainJava("client/render/world/chamberofwill/ChamberProxyView.java");
		assertTrue(proxy.contains("RenderSystem.setShaderFogStart(HuskOutsideSnapshot.RADIUS * ") && proxy.contains("RenderSystem.setShaderFogEnd(HuskOutsideSnapshot.RADIUS * "),
				"the outside blocks fog out by the snapshot's reach");
		assertTrue(proxy.contains("private static void skyDome("), "under a graded sky dome");
	}
}
