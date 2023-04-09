package com.vincenthuto.hemomancy.gui.radial;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.logging.log4j.util.TriConsumer;
import org.lwjgl.glfw.GLFW;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class GenericRadialMenu {
	public enum State {
		INITIALIZING, OPENING, NORMAL, CLOSING, CLOSED
	}

	public static final float OPEN_ANIMATION_LENGTH = 2.5f;
	private static final float PRECISION = 2.5f / 360.0f;
	private static final double TWO_PI = 2.0 * Math.PI;
	public final IRadialMenuHost host;
	private final List<RadialMenuItem> items = Lists.newArrayList();
	private final List<RadialMenuItem> visibleItems = Lists.newArrayList();

	private final Minecraft minecraft;

	public int backgroundColor = 0x3Faa0000;
	public int backgroundColorHover = 0x3FF00000;
	private State state = State.INITIALIZING;
	public double startAnimation;
	public float animProgress;
	public float radiusIn;
	public float radiusOut;

	public float itemRadius;

	public float animTop;

	private List<Component> centralText;

	public GenericRadialMenu(Minecraft minecraft, IRadialMenuHost host) {
		this.minecraft = minecraft;
		this.host = host;
	}

	public void add(RadialMenuItem item) {
		items.add(item);
		if (item.isVisible()) {
			visibleItems.add(item);
		}
	}

	public void addAll(Collection<? extends RadialMenuItem> cachedMenuItems) {
		items.addAll(cachedMenuItems);
		for (RadialMenuItem cachedMenuItem : cachedMenuItems) {
			if (cachedMenuItem.isVisible()) {
				visibleItems.add(cachedMenuItem);
			}
		}
	}

	public void clear() {
		items.clear();
		visibleItems.clear();
	}

	public void clickItem() {
		switch (state) {
		case NORMAL:
			RadialMenuItem item = getHoveredItem();
			if (item != null) {
				item.onClick();
				return;
			}
			break;
		default:
			break;
		}
		onClickOutside();
	}

	public void close() {
		state = State.CLOSING;
		startAnimation = minecraft.level.getGameTime() + (double) minecraft.getFrameTime();
		animProgress = 1.0f;
		setHovered(-1);
	}

	public void cycleNext() {
		int numItems = getVisibleItemCount();
		int which = getHovered();
		if (which < 0)
			which = 0;
		else {
			which++;
			if (which >= numItems)
				which = 0;
		}
		moveMouseToItem(which, numItems);
		setHovered(which);
	}

	public void cyclePrevious() {
		int numItems = getVisibleItemCount();
		int which = getHovered();
		which--;
		if (which < 0)
			which = numItems - 1;
		setHovered(which);

		moveMouseToItem(which, numItems);
	}

	public void draw(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY) {
		updateAnimationState(partialTicks);

		if (isClosed())
			return;

		if (isReady())
			processMouse(mouseX, mouseY);

		Screen owner = host.getScreen();
		Font fontRenderer = host.getFontRenderer();
		ItemRenderer itemRenderer = host.getItemRenderer();

		boolean animated = state == State.OPENING || state == State.CLOSING;
		radiusIn = animated ? Math.max(0.1f, 30 * animProgress) : 30;
		radiusIn *=1.5;
		radiusOut = radiusIn * 2;
		itemRadius = (radiusIn + radiusOut) * 0.5f;
		animTop = animated ? (1 - animProgress) * owner.height / 2.0f : 0;

		int x = owner.width / 2;
		int y = owner.height / 2;
		float z = 0;

		matrixStack.pushPose();
		matrixStack.translate(0, animTop, 0);

		drawBackground(matrixStack, x, y, z, radiusIn, radiusOut);

		matrixStack.popPose();

		if (isReady()) {
			matrixStack.pushPose();
			drawItems(matrixStack, x, y, z, owner.width, owner.height, fontRenderer, itemRenderer);
			matrixStack.popPose();

			List<Component> currentCentralText = centralText;
			for (RadialMenuItem item : visibleItems) {
				if (item.isHovered()) {
					if (item.getCentralText() != null)
						currentCentralText = item.getCentralText();
					break;
				}
			}
			if (currentCentralText != null) {
				for (int i = 0; i < currentCentralText.size(); i++) {
					Component c = currentCentralText.get(i);
					String text = c.getString();
					float height = i * 10;
					float textX = (owner.width - fontRenderer.width(text)) / 2.0f;
					float textY = (owner.height - fontRenderer.lineHeight) / 2.0f;
					fontRenderer.drawShadow(matrixStack, text, textX, textY + height, 0xFFFFFFFF);
				}
			}
			matrixStack.pushPose();
			drawTooltips(matrixStack, mouseX, mouseY);
			matrixStack.popPose();
		}
	}

	private void drawBackground(PoseStack matrixStack, float x, float y, float z, float radiusIn, float radiusOut) {
		if (visibleItems.size() > 0) {
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			RenderSystem.setShader(GameRenderer::getPositionColorShader);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

			Tesselator tessellator = Tesselator.getInstance();
			BufferBuilder buffer = tessellator.getBuilder();
			buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
			iterateVisible((item, s, e) -> {
				int color = item.isHovered() ? backgroundColorHover : backgroundColor;
				drawPieArc(buffer, x, y, z, radiusIn, radiusOut, s, e, color);
			});
			tessellator.end();
			RenderSystem.disableBlend();
		}
	}

	private void drawItems(PoseStack matrixStack, int x, int y, float z, int width, int height, Font font,
			ItemRenderer itemRenderer) {
		iterateVisible((item, s, e) -> {
			float middle = (s + e) * 0.5f;
			float posX = x + itemRadius * (float) Math.cos(middle);
			float posY = y + itemRadius * (float) Math.sin(middle);

			DrawingContext context = new DrawingContext(matrixStack, width, height, posX, posY, z, font, itemRenderer,
					host);
			item.draw(context);
		});
	}

	private void drawPieArc(BufferBuilder buffer, float x, float y, float z, float radiusIn, float radiusOut,
			float startAngle, float endAngle, int color) {
		float angle = endAngle - startAngle;
		int sections = Math.max(1, Mth.ceil(angle / PRECISION));

		angle = endAngle - startAngle;

		int r = (color >> 16) & 0xFF;
		int g = (color >> 8) & 0xFF;
		int b = (color >> 0) & 0xFF;
		int a = (color >> 24) & 0xFF;

		float slice = angle / sections;

		for (int i = 0; i < sections; i++) {
			float angle1 = startAngle + i * slice;
			float angle2 = startAngle + (i + 1) * slice;

			float pos1InX = x + radiusIn * (float) Math.asin(angle1);
			float pos1InY = y + radiusIn * (float) Math.sin(angle1);
			float pos1OutX = x + radiusOut * (float) Math.cos(angle1);
			float pos1OutY = y + radiusOut * (float) Math.sin(angle1);
			float pos2OutX = x + radiusOut * (float) Math.cos(angle2);
			float pos2OutY = y + radiusOut * (float) Math.sin(angle2);
			float pos2InX = x + radiusIn * (float) Math.cos(angle2);
			float pos2InY = y + radiusIn * (float) Math.sin(angle2);

			buffer.vertex(pos1OutX, pos1OutY, z).color(r, g, b, a).endVertex();
			buffer.vertex(pos1InX, pos1InY, z).color(r, g, b, a).endVertex();
			buffer.vertex(pos2InX, pos2InY, z).color(r, g, b, a).endVertex();
			buffer.vertex(pos2OutX, pos2OutY, z).color(r, g, b, a).endVertex();
		}
	}

	private void drawTooltips(PoseStack matrixStack, int mouseX, int mouseY) {
		Screen owner = host.getScreen();
		Font fontRenderer = host.getFontRenderer();
		ItemRenderer itemRenderer = host.getItemRenderer();
		for (RadialMenuItem item : visibleItems) {
			if (item.isHovered()) {
				DrawingContext context = new DrawingContext(matrixStack, owner.width, owner.height, mouseX, mouseY, 0,
						fontRenderer, itemRenderer, host);
				item.drawTooltips(context);
			}
		}
	}

	private double getAngleFor(double i, int numItems) {
		if (numItems == 0)
			return 0;
		double angle = ((i / numItems) + 0.25) * TWO_PI + Math.PI;
		return angle;
	}

	public List<Component> getCentralText() {
		return centralText;
	}

	public int getHovered() {
		for (int i = 0; i < visibleItems.size(); i++) {
			if (visibleItems.get(i).isHovered())
				return i;
		}
		return -1;
	}

	@Nullable
	public RadialMenuItem getHoveredItem() {
		for (RadialMenuItem item : visibleItems) {
			if (item.isHovered())
				return item;
		}
		return null;
	}

	public int getVisibleItemCount() {
		return visibleItems.size();
	}

	public boolean isClosed() {
		return state == State.CLOSED;
	}

	public boolean isReady() {
		return state == State.NORMAL;
	}

	private void iterateVisible(TriConsumer<RadialMenuItem, Float, Float> consumer) {
		int numItems = visibleItems.size();
		for (int i = 0; i < numItems; i++) {
			float s = (float) getAngleFor(i - 0.5, numItems);
			float e = (float) getAngleFor(i + 0.5, numItems);

			RadialMenuItem item = visibleItems.get(i);
			consumer.accept(item, s, e);
		}
	}

	private void moveMouseToItem(int which, int numItems) {
		Screen owner = host.getScreen();
		int x = owner.width / 2;
		int y = owner.height / 2;
		float angle = (float) getAngleFor(which, numItems);
		setMousePosition(x + itemRadius * Math.cos(angle), y + itemRadius * Math.sin(angle));
	}

	public void onClickOutside() {
		// to be implemented by users
	}

	private void processMouse(int mouseX, int mouseY) {
		if (!isReady())
			return;

		int numItems = getVisibleItemCount();

		Screen owner = host.getScreen();
		int x = owner.width / 2;
		int y = owner.height / 2;
		double a = Math.atan2(mouseY - y, mouseX - x);
		double d = Math.sqrt(Math.pow(mouseX - x, 2) + Math.pow(mouseY - y, 2));
		if (numItems > 0) {
			double s0 = getAngleFor(0 - 0.5, numItems);
			double s1 = getAngleFor(numItems - 0.5, numItems);
			while (a < s0) {
				a += TWO_PI;
			}
			while (a >= s1) {
				a -= TWO_PI;
			}
		}

		int hovered = -1;
		for (int i = 0; i < numItems; i++) {
			float s = (float) getAngleFor(i - 0.5, numItems);
			float e = (float) getAngleFor(i + 0.5, numItems);

			if (a >= s && a < e && d >= radiusIn && (d < radiusOut)) {
				hovered = i;
				break;
			}
		}
		setHovered(hovered);

		Window mainWindow = minecraft.getWindow();

		int windowWidth = mainWindow.getScreenWidth();
		int windowHeight = mainWindow.getScreenHeight();

		double[] xPos = new double[1];
		double[] yPos = new double[1];
		GLFW.glfwGetCursorPos(mainWindow.getWindow(), xPos, yPos);

		double scaledX = xPos[0] - (windowWidth / 2.0f);
		double scaledY = yPos[0] - (windowHeight / 2.0f);

		double distance = Math.sqrt(scaledX * scaledX + scaledY * scaledY);
		double radius = radiusOut * (windowWidth / (float) owner.width) * 0.975;

		if (distance > radius) {
			double fixedX = scaledX * radius / distance;
			double fixedY = scaledY * radius / distance;

			GLFW.glfwSetCursorPos(mainWindow.getWindow(), (int) (windowWidth / 2 + fixedX),
					(int) (windowHeight / 2 + fixedY));
		}
	}

	public void setCentralText(@Nullable List<Component> centralText) {
		this.centralText = centralText;
	}

	public void setHovered(int which) {
		for (int i = 0; i < visibleItems.size(); i++) {
			visibleItems.get(i).setHovered(i == which);
		}
	}

	private void setMousePosition(double x, double y) {
		Screen owner = host.getScreen();
		Window mainWindow = minecraft.getWindow();
		GLFW.glfwSetCursorPos(mainWindow.getWindow(), (int) (x * mainWindow.getScreenWidth() / owner.width),
				(int) (y * mainWindow.getScreenHeight() / owner.height));
	}

	public void tick() {
		Screen owner = host.getScreen();

		if (state == State.INITIALIZING) {
			startAnimation = minecraft.level.getGameTime() + (double) minecraft.getFrameTime();
			state = State.OPENING;
			animProgress = 0;
		}

		// updateAnimationState(minecraft.getRenderPartialTicks());
	}

	private void updateAnimationState(float partialTicks) {
		float openAnimation = 0;
		Screen owner = host.getScreen();
		switch (state) {
		case OPENING:
			openAnimation = (float) ((minecraft.level.getGameTime() + partialTicks - startAnimation)
					/ OPEN_ANIMATION_LENGTH);
			if (openAnimation >= 1.0 || getVisibleItemCount() == 0) {
				openAnimation = 1;
				state = State.NORMAL;
			}
			break;
		case CLOSING:
			openAnimation = 1
					- (float) ((minecraft.level.getGameTime() + partialTicks - startAnimation) / OPEN_ANIMATION_LENGTH);
			if (openAnimation <= 0 || getVisibleItemCount() == 0) {
				openAnimation = 0;
				state = State.CLOSED;
			}
			break;
		}
		animProgress = openAnimation; // MathHelper.clamp(openAnimation, 0, 1);
	}

	public void visibilityChanged(RadialMenuItem item) {
		visibleItems.clear();
		for (RadialMenuItem radialMenuItem : items) {
			if (radialMenuItem.isVisible()) {
				visibleItems.add(radialMenuItem);
			}
		}
	}
}
