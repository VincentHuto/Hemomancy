package com.vincenthuto.hemomancy.client.screen.item;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.HemoAttachmentTypes;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumBloodFlow;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.IVascularSystem;
import com.vincenthuto.hutoslib.client.HLTextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A standalone screen that displays the player's Vascular System status.
 * Renders a full 3D player model that can be rotated by click-dragging.
 * Each body section is overlaid with a colored tint based on vein health:
 * green (100%) → yellow (50%) → red (15%) → black (0%/dead).
 */
public class VascularStatusScreen extends Screen {

	private static final int PREFERRED_GUI_WIDTH = 190;
	private static final int PREFERRED_GUI_HEIGHT = 254;

	/** Actual GUI dimensions, shrunk to fit the current window size. */
	private int guiWidth = PREFERRED_GUI_WIDTH;
	private int guiHeight = PREFERRED_GUI_HEIGHT;
	/** Scale factor (0-1) of actual size vs preferred size, used to shrink content proportionally. */
	private float guiScale = 1.0f;

	/** Number of animated vein tendrils swimming across the background. */
	private static final int VEIN_COUNT = 28;

	/**
	 * Stores per-vein parameters seeded once at init:
	 * [i][0] = startX ratio, [i][1] = startY ratio,
	 * [i][2] = base angle (radians), [i][3] = speed multiplier,
	 * [i][4] = amplitude, [i][5] = frequency, [i][6] = length (steps),
	 * [i][7] = thickness (1-3), [i][8] = red tint (0-1 extra brightness)
	 */
	private float[][] veinParams;

	/** Current Y-axis rotation of the player model (in degrees), controlled by mouse drag. */
	private float rotationY = 200.0f;
	/** Current X-axis rotation (pitch tilt), controlled by mouse drag. */
	private float rotationX = -10.0f;
	/** Whether the mouse is currently being dragged to rotate the model. */
	private boolean isDragging = false;

	/** Scale of the rendered player model in GUI units (at full size). */
	private static final int BASE_MODEL_SCALE = 55;

	/** Continuously increasing tick counter for driving the idle walk animation. */
	private float animationTicks = 0f;

	public VascularStatusScreen() {
		super(Component.literal("Vascular Status"));
	}

	public static void openScreen() {
		Minecraft.getInstance().setScreen(new VascularStatusScreen());
	}

	@Override
	protected void init() {
		super.init();

		// Compute GUI dimensions that fit within the screen with some padding
		int padding = 10;
		guiWidth = Math.min(PREFERRED_GUI_WIDTH, this.width - padding * 2);
		guiHeight = Math.min(PREFERRED_GUI_HEIGHT, this.height - padding * 2);
		guiScale = Math.min((float) guiWidth / PREFERRED_GUI_WIDTH, (float) guiHeight / PREFERRED_GUI_HEIGHT);

		// Seed vein parameters deterministically so they stay stable while the screen is open
		Random rand = new Random(42L);
		veinParams = new float[VEIN_COUNT][9];
		for (int i = 0; i < VEIN_COUNT; i++) {
			veinParams[i][0] = rand.nextFloat();                          // startX ratio
			veinParams[i][1] = rand.nextFloat();                          // startY ratio
			veinParams[i][2] = (float) (rand.nextFloat() * Math.PI * 2); // base angle
			veinParams[i][3] = 0.3f + rand.nextFloat() * 0.7f;           // speed mult
			veinParams[i][4] = 8f + rand.nextFloat() * 18f;              // amplitude
			veinParams[i][5] = 0.04f + rand.nextFloat() * 0.08f;         // frequency
			veinParams[i][6] = 60 + rand.nextInt(120);                    // length (steps)
			veinParams[i][7] = 1 + rand.nextInt(3);                       // thickness
			veinParams[i][8] = rand.nextFloat();                           // red tint brightness
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	/** Suppress the 1.21.1 menu_blur post-effect from Screen#renderBackground. */
	@Override
	public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		// intentionally empty
	}

	// ───── Mouse Interaction for 3D Rotation ─────

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0) {
			int centerX = this.width / 2;
			int centerY = this.height / 2;
			int guiLeft = centerX - guiWidth / 2;
			int guiTop = centerY - guiHeight / 2;
			// Only start dragging if inside the GUI panel
			if (mouseX >= guiLeft && mouseX <= guiLeft + guiWidth &&
				mouseY >= guiTop && mouseY <= guiTop + guiHeight) {
				isDragging = true;
				return true;
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (button == 0) {
			isDragging = false;
		}
		return super.mouseReleased(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (isDragging && button == 0) {
			rotationY += (float) dragX * 0.75f;
			rotationX += (float) dragY * 0.75f;
			rotationX = Mth.clamp(rotationX, -45.0f, 45.0f);
			return true;
		}
		return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
	}

	// ───── Render ─────

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		// Do NOT call renderBackground() — it applies blur. This is not a pause screen.
		// For a dim overlay, use a fill instead if needed.

		int centerX = this.width / 2;
		int centerY = this.height / 2;

		int guiLeft = centerX - guiWidth / 2;
		int guiTop = centerY - guiHeight / 2;

		// Animated vein background clipped to the GUI bounds
		renderVeinBackground(graphics, guiLeft, guiTop, guiWidth, guiHeight);

		// Programmatic dark-red border frame on top of the vein background
		drawBorder(graphics, guiLeft, guiTop, guiWidth, guiHeight);

		// Title
		graphics.drawCenteredString(this.font, this.title, centerX, guiTop + 6, 0xFFCC3344);
		graphics.drawCenteredString(this.font, "Drag to rotate • Hover for details", centerX, guiTop + 18, 0xFF553333);

		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) return;

		int scaledModelSize = (int) (BASE_MODEL_SCALE * guiScale);

		// Render 3D player model with vascular overlays
		HemoCapabilityAccess.getVascularSystem(player).ifPresent(vascular -> {
			// Scissor clip to keep the model inside the GUI panel
			graphics.enableScissor(guiLeft + 4, guiTop + 28, guiLeft + guiWidth - 4, guiTop + guiHeight - 18);
			render3DPlayerWithOverlays(graphics, vascular, player, centerX, centerY + (int)(20 * guiScale), mouseX, mouseY, scaledModelSize);
			graphics.disableScissor();
		});

		// Footer
		graphics.drawCenteredString(this.font, "§4§l— §c§oVenous Network §4§l—",
				centerX, guiTop + guiHeight - 14, 0xFF882222);
	}

	// ───── 3D Player Rendering with Vascular Overlays ─────

	/**
	 * Renders the player entity as a 3D model that can be rotated via mouse drag,
	 * with colored overlays on each body section representing vein health.
	 */
	private void render3DPlayerWithOverlays(GuiGraphics graphics, IVascularSystem vascular,
											LocalPlayer player, int posX, int posY,
											int mouseX, int mouseY, int modelScale) {
		// ── Step 1: Render the player model's body parts tinted with vascular health colors ──
		renderVascularModel(vascular, player, posX, posY, modelScale);

		// ── Step 2: Render hover tooltips based on projected 2D positions of body parts ──
		renderHoverTooltips(graphics, vascular, posX, posY, mouseX, mouseY, modelScale);
	}

	/**
	 * Renders the player model's body parts directly, each tinted with the vascular health color.
	 * No actual player entity is rendered — just the PlayerModel geometry with colored tinting.
	 */
	private void renderVascularModel(IVascularSystem vascular, LocalPlayer player,
									 int posX, int posY, int scale) {
		var modelViewStack = RenderSystem.getModelViewStack();
		modelViewStack.pushMatrix();
		modelViewStack.translate((float) posX, (float) posY, 1050.0F);
		modelViewStack.scale(1.0F, 1.0F, -1.0F);
		RenderSystem.applyModelViewMatrix();

		PoseStack poseStack = new PoseStack();
		poseStack.translate(0.0, 0.0, 1000.0);
		poseStack.scale(scale, scale, scale);

		// Apply pitch rotation (no Z flip — we're rendering ModelParts directly, not via entity renderer)
		poseStack.mulPose(new Quaternionf().rotationX(rotationX * ((float) Math.PI / 180.0f)));

		// Get the player renderer and its model
		EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
		PlayerRenderer playerRenderer = (PlayerRenderer) dispatcher.getRenderer(player);
		PlayerModel<AbstractClientPlayer> model = playerRenderer.getModel();

		animationTicks += 0.5f;
		float limbSwing = animationTicks * 0.6f;
		float limbSwingAmount = 0.6f;
		model.young = false;
		model.riding = false;
		model.setupAnim(player, limbSwing, limbSwingAmount, animationTicks, 0, 0);

		// Apply yaw to the whole model by translating into model space and rotating
		poseStack.pushPose();
		poseStack.mulPose(new Quaternionf().rotationY(rotationY * ((float) Math.PI / 180.0f)));

		// Shift the model up in model-space so it's vertically centered at posY.
		// PlayerModel spans from y≈-8 (head top) to y≈24 (feet) in model units;
		// midpoint is about y=8, i.e. 8/16 = 0.5 blocks. Translate up by that.
		poseStack.translate(0.0, -0.5, 0.0);

		Lighting.setupForEntityInInventory();

		MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();

		renderTintedPart(poseStack, bufferSource, model.head, vascular, EnumVeinSections.HEAD);
		renderTintedPart(poseStack, bufferSource, model.hat, vascular, EnumVeinSections.HEAD);
		// The torso model part covers both HEART and BODY sections — use their average health
		renderTintedPartBlended(poseStack, bufferSource, model.body, vascular,
				EnumVeinSections.HEART, EnumVeinSections.BODY);
		renderTintedPart(poseStack, bufferSource, model.rightArm, vascular, EnumVeinSections.ARMS);
		renderTintedPart(poseStack, bufferSource, model.leftArm, vascular, EnumVeinSections.ARMS);
		renderTintedPart(poseStack, bufferSource, model.rightLeg, vascular, EnumVeinSections.LEGS);
		renderTintedPart(poseStack, bufferSource, model.leftLeg, vascular, EnumVeinSections.LEGS);

		bufferSource.endBatch();
		RenderSystem.disableBlend();

		poseStack.popPose();

		modelViewStack.popMatrix();
		RenderSystem.applyModelViewMatrix();
		Lighting.setupFor3DItems();
	}

	/**
	 * Renders a single ModelPart using the player model's actual geometry,
	 * tinted with the health-based color for the given vein section.
	 */
	private float animTime = 0f;

	private void renderTintedPart(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource,
								  ModelPart part, IVascularSystem vascular,
								  EnumVeinSections section) {
		float health = Mth.clamp(vascular.getHealthBySection(section), 0f, 100f);
		int color = getColorForHealth(health);

		float a = ((color >> 24) & 0xFF) / 255.0f * 0.4f;
		float r = ((color >> 16) & 0xFF) / 255.0f;
		float g = ((color >> 8) & 0xFF) / 255.0f;
		float b = (color & 0xFF) / 255.0f;

		// Pulsing glow effect
		animTime += 0.016f; // ~60 FPS approximation

		float time = animTime;
		float pulse = 0.85f + 0.15f * Mth.sin(time * 2.0f + section.ordinal() * 1.3f);
		r *= pulse;
		g *= pulse;
		b *= pulse;

		int packedColor = net.minecraft.util.FastColor.ARGB32.colorFromFloat(a, r, g, b);
		VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucentCull(
				net.minecraft.resources.ResourceLocation.parse("minecraft:textures/misc/white.png")));

		// Render the actual model part geometry with the tint color
		part.render(poseStack, consumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, packedColor);
	}

	/**
	 * Renders a ModelPart tinted with the average health color of two vein sections.
	 * Used for the torso which covers both HEART and BODY.
	 */
	private void renderTintedPartBlended(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource,
										 ModelPart part, IVascularSystem vascular,
										 EnumVeinSections section1, EnumVeinSections section2) {
		float health1 = Mth.clamp(vascular.getHealthBySection(section1), 0f, 100f);
		float health2 = Mth.clamp(vascular.getHealthBySection(section2), 0f, 100f);
		float avgHealth = (health1 + health2) / 2f;
		int color = getColorForHealth(avgHealth);

		float a = ((color >> 24) & 0xFF) / 255.0f * 0.4f;
		float r = ((color >> 16) & 0xFF) / 255.0f;
		float g = ((color >> 8) & 0xFF) / 255.0f;
		float b = (color & 0xFF) / 255.0f;
		animTime += 0.016f; // ~60 FPS approximation

		float time = animTime;

		float pulse = 0.85f + 0.15f * Mth.sin(time * 2.0f + section1.ordinal() * 1.3f);
		r *= pulse;
		g *= pulse;
		b *= pulse;

		int packedColor = net.minecraft.util.FastColor.ARGB32.colorFromFloat(a, r, g, b);
		VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucentCull(
				net.minecraft.resources.ResourceLocation.parse("minecraft:textures/misc/white.png")));

		part.render(poseStack, consumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, packedColor);
	}

	// ───── Hover Tooltips with 2D Projection ─────

	/**
	 * Computes approximate 2D bounding boxes for each body section based on the current rotation,
	 * and renders a tooltip if the mouse hovers over one.
	 */
	private void renderHoverTooltips(GuiGraphics graphics, IVascularSystem vascular,
									 int posX, int posY, int mouseX, int mouseY, int modelScale) {
		// We project approximate center positions of each body part to 2D screen coords.
		// The player model is ~1.8 blocks tall, centered at posX/posY (feet at posY).
		// In model space (after scale): head top is at about -1.5, feet at +0.3 from center.

		float scale = modelScale;
		float yawRad = rotationY * ((float) Math.PI / 180.0f);
		float pitchRad = rotationX * ((float) Math.PI / 180.0f);
		float cosPitch = Mth.cos(pitchRad);
		float sinPitch = Mth.sin(pitchRad);
		float cosYaw = Mth.cos(yawRad);
		float sinYaw = Mth.sin(yawRad);

		// Define approximate model-space centers for each section (in blocks, relative to model center).
		// Y positive = down on screen (no Z-flip). The -0.5 translate centers the model,
		// so head center ≈ -0.5, body ≈ 0, legs ≈ +0.5 in blocks from origin.
		BodySectionDef[] defs = {
			new BodySectionDef(EnumVeinSections.HEAD,     0.0f, -0.55f, 0.0f, 22, 22),
			new BodySectionDef(EnumVeinSections.HEART,    0.0f, -0.15f, 0.0f, 24, 16),
			new BodySectionDef(EnumVeinSections.BODY,     0.0f,  0.15f, 0.0f, 24, 20),
			new BodySectionDef(EnumVeinSections.ARMS,     -0.40f, 0.0f, 0.0f, 14, 38),
			new BodySectionDef(EnumVeinSections.ARMS,      0.40f, 0.0f, 0.0f, 14, 38),
			new BodySectionDef(EnumVeinSections.LEGS,     -0.12f, 0.65f, 0.0f, 14, 36),
			new BodySectionDef(EnumVeinSections.LEGS,      0.12f, 0.65f, 0.0f, 14, 36),
		};

		EnumVeinSections hoveredSection = null;

		for (BodySectionDef def : defs) {
			// Apply yaw rotation (around Y axis) to the 3D position
			float rx = def.modelX * cosYaw - def.modelZ * sinYaw;
			float ry = def.modelY;
			float rz = def.modelX * sinYaw + def.modelZ * cosYaw;

			// Apply pitch rotation (around X axis)
			float ry2 = ry * cosPitch - rz * sinPitch;

			// Project to screen (simple orthographic — the entity render uses orthographic projection)
			int screenX = posX + (int) (rx * scale);
			int screenY = posY + (int) (ry2 * scale);

			int halfW = (int)(def.hitW * guiScale) / 2;
			int halfH = (int)(def.hitH * guiScale) / 2;

			if (mouseX >= screenX - halfW && mouseX <= screenX + halfW &&
				mouseY >= screenY - halfH && mouseY <= screenY + halfH) {
				hoveredSection = def.section;
				break;
			}
		}

		if (hoveredSection != null) {
			float health = Mth.clamp(vascular.getHealthBySection(hoveredSection), 0f, 100f);
			EnumBloodFlow flow = vascular.getBloodFlowBySection(hoveredSection);
			String name = HLTextUtils.toProperCase(hoveredSection.toString());
			String flowName = HLTextUtils.toProperCase(flow.toString());
			int nameColor = getTextColorForHealth(health);
			int flowColor = getFlowColor(flow);

			List<Component> tooltip = new ArrayList<>();
			tooltip.add(Component.literal("§l" + name).withStyle(s -> s.withColor(nameColor)));
			tooltip.add(Component.literal("Health: " + String.format("%.1f", health) + " / 100.0")
					.withStyle(s -> s.withColor(0xAAAAAA)));
			tooltip.add(Component.literal("Flow: " + flowName)
					.withStyle(s -> s.withColor(flowColor)));
			LocalPlayer player = Minecraft.getInstance().player;
			if (player != null) {
				EnumVeinSections selectedSection = hoveredSection;
				var memoryState = player.getData(HemoAttachmentTypes.MUSCLE_MEMORY);
				memoryState.getPrimed(selectedSection, player.level().getGameTime()).ifPresent(memory -> {
					long seconds = (memoryState.remainingTicks(selectedSection,
							player.level().getGameTime()) + 19L) / 20L;
					tooltip.add(Component.translatable("muscle_memory.hemomancy." + memory.id())
							.append(Component.literal(" • " + seconds + "s"))
							.withStyle(s -> s.withColor(0xCC3344)));
				});
			}
			graphics.renderTooltip(this.font, tooltip, java.util.Optional.empty(), mouseX, mouseY);
		}
	}

	/**
	 * Helper record for defining body section hit-test areas in 3D model space.
	 */
	private record BodySectionDef(EnumVeinSections section, float modelX, float modelY, float modelZ,
								  int hitW, int hitH) {}

	// ───── Gradient Dark-Red Border ─────

	private void drawBorder(GuiGraphics gfx, int x, int y, int w, int h) {
		int[] colors = {
			0xFF6B1010,  // outermost — bright crimson
			0xFF4A0C0C,  // mid-outer
			0xFF300808,  // mid-inner
			0xFF1A0404,  // innermost — near-black maroon
		};
		for (int i = 0; i < colors.length; i++) {
			int c = colors[i];
			int xi = x + i;
			int yi = y + i;
			int wi = w - i * 2;
			int hi = h - i * 2;
			gfx.fill(xi, yi, xi + wi, yi + 1, c);
			gfx.fill(xi, yi + hi - 1, xi + wi, yi + hi, c);
			gfx.fill(xi, yi, xi + 1, yi + hi, c);
			gfx.fill(xi + wi - 1, yi, xi + wi, yi + hi, c);
		}
		int hl = 0xFF8A1818;
		gfx.fill(x, y, x + 2, y + 2, hl);
		gfx.fill(x + w - 2, y, x + w, y + 2, hl);
		gfx.fill(x, y + h - 2, x + 2, y + h, hl);
		gfx.fill(x + w - 2, y + h - 2, x + w, y + h, hl);
	}

	// ───── Procedural Animated Vein Background ─────

	/**
	 * Renders a dark red/black background covered in swimming, squiggling vein tendrils.
	 * Each vein is a sine-wave curve that drifts over time, giving an organic pulsing feel.
	 * All rendering is clipped to the given GUI bounds.
	 */
	private void renderVeinBackground(GuiGraphics graphics, int gx, int gy, int gw, int gh) {
		// Scissor clip so nothing bleeds outside the GUI panel
		graphics.enableScissor(gx, gy, gx + gw, gy + gh);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();

		// Layer 1: solid near-black base filling GUI area
		graphics.fill(gx, gy, gx + gw, gy + gh, 0xFF0A0204);

		// Layer 2: subtle dark-red radial glow in the center of the GUI
		int cx = gx + gw / 2;
		int cy = gy + gh / 2;
		int glowRadius = Math.max(gw, gh) / 2;
		for (int ring = glowRadius; ring > 0; ring -= 4) {
			float t = (float) ring / glowRadius; // 1.0 at edge, 0.0 at center
			int alpha = (int) (35 * (1f - t));     // brighter toward center
			int red = (int) (40 * (1f - t));
			int color = (alpha << 24) | (red << 16);
			graphics.fill(cx - ring, cy - ring, cx + ring, cy + ring, color);
		}

		animTime += 0.016f; // ~60 FPS approximation

		float time = animTime;


		if (veinParams != null) {
			for (int i = 0; i < VEIN_COUNT; i++) {
				drawVeinTendril(graphics, i, time, gx, gy, gw, gh);
			}
		}

		// Layer 4: very subtle noise-like speckles for texture (static, cheap)
		Random speckRand = new Random(12345L);
		for (int s = 0; s < 120; s++) {
			int sx = gx + speckRand.nextInt(gw);
			int sy = gy + speckRand.nextInt(gh);
			int sr = 10 + speckRand.nextInt(20);
			int sg = speckRand.nextInt(6);
			int sa = 15 + speckRand.nextInt(25);
			graphics.fill(sx, sy, sx + 1, sy + 1, (sa << 24) | (sr << 16) | (sg << 8));
		}

		RenderSystem.disableBlend();
		graphics.disableScissor();
	}

	/**
	 * Draws a single animated vein tendril as a squiggling curve within the GUI bounds.
	 * The curve is built from small filled rectangles placed along a parametric path.
	 */
	private void drawVeinTendril(GuiGraphics graphics, int index, float time,
								 int gx, int gy, int gw, int gh) {
		float[] p = veinParams[index];
		float startX = gx + p[0] * gw;
		float startY = gy + p[1] * gh;
		float baseAngle = p[2];
		float speed = p[3];
		float amplitude = p[4];
		float frequency = p[5];
		int length = (int) p[6];
		int thickness = (int) p[7];
		float brightness = p[8];

		float angleDrift = baseAngle + 0.15f * Mth.sin(time * speed * 0.3f + index);
		float cosA = Mth.cos(angleDrift);
		float sinA = Mth.sin(angleDrift);

		float timeOffset = time * speed * 2.0f;

		int baseRed = (int) (40 + 50 * brightness);
		int baseGreen = (int) (2 + 8 * brightness);
		int baseBlue = (int) (5 + 5 * brightness);

		for (int step = 0; step < length; step++) {
			float t = step;

			float squiggle = amplitude * Mth.sin(frequency * t + timeOffset);
			float microSquiggle = (amplitude * 0.3f) * Mth.sin(frequency * 2.7f * t + timeOffset * 1.4f + index);
			float displacement = squiggle + microSquiggle;

			float px = startX + t * cosA * 1.5f - displacement * sinA;
			float py = startY + t * sinA * 1.5f + displacement * cosA;

			int ix = (int) px;
			int iy = (int) py;

			if (ix + thickness < gx || ix >= gx + gw || iy + thickness < gy || iy >= gy + gh) {
				continue;
			}

			float tipFade = 1f;
			if (step < 10) tipFade = step / 10f;
			else if (step > length - 10) tipFade = (length - step) / 10f;

			float pulse = 0.7f + 0.3f * Mth.sin(time * 1.5f + index * 0.5f + step * 0.02f);

			int alpha = (int) (Mth.clamp(tipFade * pulse * 180, 20, 200));
			int r = (int) Mth.clamp(baseRed * pulse, 0, 255);
			int g = (int) Mth.clamp(baseGreen * pulse * 0.5f, 0, 255);
			int b = (int) Mth.clamp(baseBlue * pulse * 0.3f, 0, 255);

			int color = (alpha << 24) | (r << 16) | (g << 8) | b;
			graphics.fill(ix, iy, ix + thickness, iy + thickness, color);
		}
	}

	// ───── Color Utilities ─────

	/**
	 * Maps health (0-100) to an ARGB color.
	 * 100 = bright green (semi-transparent)
	 * 50  = yellow
	 * 15  = red
	 * 0   = black (more opaque)
	 */
	private int getColorForHealth(float health) {
		float t = health / 100f; // 0.0 to 1.0

		int r, g, b;
		int alpha;

		if (t > 0.5f) {
			// Green → Yellow (100% → 50%)
			float localT = (t - 0.5f) / 0.5f; // 1.0 at 100%, 0.0 at 50%
			r = (int) (255 * (1f - localT));    // 0 at 100%, 255 at 50%
			g = 255;                             // always full green
			b = 0;
			alpha = (int) (100 + 55 * (1f - localT)); // 100 at 100%, 155 at 50%
		} else if (t > 0.15f) {
			// Yellow → Red (50% → 15%)
			float localT = (t - 0.15f) / 0.35f; // 1.0 at 50%, 0.0 at 15%
			r = 255;
			g = (int) (255 * localT);            // 255 at 50%, 0 at 15%
			b = 0;
			alpha = (int) (155 + 50 * (1f - localT)); // 155 at 50%, 205 at 15%
		} else {
			// Red → Black (15% → 0%)
			float localT = t / 0.15f;           // 1.0 at 15%, 0.0 at 0%
			r = (int) (255 * localT);
			g = 0;
			b = 0;
			alpha = (int) (205 + 50 * (1f - localT)); // 205 at 15%, 255 at 0%
		}

		return (alpha << 24) | (r << 16) | (g << 8) | b;
	}


	/**
	 * Returns a solid text color based on health percentage.
	 */
	private int getTextColorForHealth(float health) {
		if (health >= 75) return 0x55FF55;  // Green
		if (health >= 50) return 0xFFFF55;  // Yellow
		if (health >= 15) return 0xFF5555;  // Red
		return 0x555555;                     // Dark gray / black
	}

	/**
	 * Returns a color for the flow state name.
	 */
	private int getFlowColor(EnumBloodFlow flow) {
		return switch (flow) {
			case RAGING -> 0x55FF55;
			case FLOWING -> 0xAAFF55;
			case STABLE -> 0xFFFF55;
			case VARICOSE -> 0xFFAA00;
			case ClOTTED -> 0xFF5555;
			case DEAD -> 0x555555;
		};
	}
}
