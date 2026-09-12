package com.vincenthuto.hemomancy.client.screen.radial;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.apache.logging.log4j.util.TriConsumer;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

public class GenericRadialMenu {
	public static final float OPEN_ANIMATION_LENGTH = 2.5f;

	public final IRadialMenuHost host;
	private final List<RadialMenuItem> items = Lists.newArrayList();
	private final List<RadialMenuItem> visibleItems = Lists.newArrayList();
	private final List<RadialMenuItem> innerItems = Lists.newArrayList();
	private final List<RadialMenuItem> visibleInnerItems = Lists.newArrayList();
	private final Minecraft minecraft;
	public int backgroundColor = 0x3F000000;
	public int backgroundColorHover = 0x3FFFFFFF;

	public enum State {
		INITIALIZING, OPENING, NORMAL, CLOSING, CLOSED
	}

	private State state = State.INITIALIZING;
	public double startAnimation;
	public float animProgress;
	public float centerRadius;
	public float innerRadiusOut;
	public float innerItemRadius;
	public float radiusIn;
	public float radiusOut;
	public float itemRadius;
	public float animTop;
	private float targetCenterRadius = 18;
	private float targetInnerRadiusOut = 42;
	private float targetRadiusIn = 50;
	private float targetRadiusOut = 86;

	private MutableComponent centralText;

	public GenericRadialMenu(Minecraft minecraft, IRadialMenuHost host) {
		this.minecraft = minecraft;
		this.host = host;
	}

	public void setCentralText(@Nullable MutableComponent centralText) {
		this.centralText = centralText;
	}

	public MutableComponent getCentralText() {
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
		for (RadialMenuItem item : visibleInnerItems) {
			if (item.isHovered())
				return item;
		}
		for (RadialMenuItem item : visibleItems) {
			if (item.isHovered())
				return item;
		}
		return null;
	}

	public void setHovered(int which) {
		for (RadialMenuItem item : visibleInnerItems) {
			item.setHovered(false);
		}
		for (int i = 0; i < visibleItems.size(); i++) {
			visibleItems.get(i).setHovered(i == which);
		}
	}

	public int getVisibleItemCount() {
		return visibleItems.size();
	}

	public void setRadii(float center, float innerOut, float outerIn, float outerOut) {
		targetCenterRadius = center;
		targetInnerRadiusOut = innerOut;
		targetRadiusIn = outerIn;
		targetRadiusOut = outerOut;
	}

	public int getTotalVisibleItemCount() {
		return visibleItems.size() + visibleInnerItems.size();
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

	public void onClickOutside() {
	}

	public boolean isClosed() {
		return state == State.CLOSED;
	}

	public boolean isReady() {
		return state == State.NORMAL;
	}

	public void visibilityChanged(RadialMenuItem item) {
		visibleItems.clear();
		for (RadialMenuItem radialMenuItem : items) {
			if (radialMenuItem.isVisible()) {
				visibleItems.add(radialMenuItem);
			}
		}
		visibleInnerItems.clear();
		for (RadialMenuItem radialMenuItem : innerItems) {
			if (radialMenuItem.isVisible()) {
				visibleInnerItems.add(radialMenuItem);
			}
		}
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

	public void addInner(RadialMenuItem item) {
		innerItems.add(item);
		if (item.isVisible()) {
			visibleInnerItems.add(item);
		}
	}

	public void addAllInner(Collection<? extends RadialMenuItem> cachedMenuItems) {
		innerItems.addAll(cachedMenuItems);
		for (RadialMenuItem cachedMenuItem : cachedMenuItems) {
			if (cachedMenuItem.isVisible()) {
				visibleInnerItems.add(cachedMenuItem);
			}
		}
	}

	public void clear() {
		items.clear();
		visibleItems.clear();
		innerItems.clear();
		visibleInnerItems.clear();
	}

	public void selectVeinyBorder(RadialMenuItem selected) {
		for (RadialMenuItem item : items) {
			item.setVeinyBorder(item == selected);
		}
		for (RadialMenuItem item : innerItems) {
			item.setVeinyBorder(item == selected);
		}
	}

	public void close() {
		Screen owner = host.getScreen();
		state = State.CLOSING;
		startAnimation = minecraft.level.getGameTime() + (double) minecraft.getTimer().getGameTimeDeltaPartialTick(false);
		animProgress = 1.0f;
		setHovered(-1);
	}

	public void tick() {
		if (state == State.INITIALIZING) {
			startAnimation = minecraft.level.getGameTime() + (double) minecraft.getTimer().getGameTimeDeltaPartialTick(false);
			state = State.OPENING;
			animProgress = 0;
		}

	}

	public void draw(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
		updateAnimationState(partialTicks);

		if (isClosed())
			return;

		if (isReady())
			processMouse(mouseX, mouseY);

		Screen owner = host.getScreen();
		Font font = host.getFontRenderer();

		boolean animated = state == State.OPENING || state == State.CLOSING;
		centerRadius = animated ? Math.max(0.1f, targetCenterRadius * animProgress) : targetCenterRadius;
		innerRadiusOut = animated ? Math.max(centerRadius + 0.1f, targetInnerRadiusOut * animProgress) : targetInnerRadiusOut;
		radiusIn = animated ? Math.max(innerRadiusOut + 0.1f, targetRadiusIn * animProgress) : targetRadiusIn;
		radiusOut = animated ? Math.max(radiusIn + 0.1f, targetRadiusOut * animProgress) : targetRadiusOut;
		innerItemRadius = (centerRadius + innerRadiusOut) * 0.5f;
		itemRadius = (radiusIn + radiusOut) * 0.5f;
		animTop = animated ? (1 - animProgress) * owner.height / 2.0f : 0;

		int x = owner.width / 2;
		int y = owner.height / 2;
		float z = 0;

		var poseStack = graphics.pose();
		poseStack.pushPose();
		poseStack.translate(0, animTop, 0);

		drawBackground(poseStack, x, y, z, radiusIn, radiusOut);
		drawVeinyBorders(x,y,z+1,partialTicks);

		poseStack.popPose();

		if (isReady()) {
			poseStack.pushPose();
			drawItems(graphics, x, y, z, owner.width, owner.height, font);
			poseStack.popPose();

			MutableComponent currentCentralText = centralText;
			for (RadialMenuItem item : visibleInnerItems) {
				if (item.isHovered()) {
					if (item.getCentralText() != null)
						currentCentralText = item.getCentralText();
					break;
				}
			}
			for (RadialMenuItem item : visibleItems) {
				if (item.isHovered()) {
					if (item.getCentralText() != null)
						currentCentralText = item.getCentralText();
					break;
				}
			}

			if (currentCentralText != null) {
				int centralTextWidth = Math.max(24, Mth.floor(centerRadius * 2 - 8));
				List<FormattedCharSequence> lines = font.split(currentCentralText, centralTextWidth);
				float textHeight = font.lineHeight * lines.size();
				float centralTextScale = Math.min(1.0f, Math.max(0.65f,
						(centerRadius * 2 - 8) / Math.max(font.lineHeight, textHeight)));
				poseStack.pushPose();
				poseStack.translate(owner.width / 2.0f, owner.height / 2.0f, 0);
				poseStack.scale(centralTextScale, centralTextScale, 1.0f);
				float blockTop = -textHeight / 2.0f;
				for (int i = 0; i < lines.size(); i++) {
					FormattedCharSequence text = lines.get(i);
					float textX = -font.width(text) / 2.0f;
					graphics.drawString(font, text, textX, blockTop + font.lineHeight * i, 0xFFFFFFFF, true);
				}
				poseStack.popPose();
			}

			poseStack.pushPose();
			drawTooltips(graphics, mouseX, mouseY);
			poseStack.popPose();
		}
	}

	private void updateAnimationState(float partialTicks) {
		float openAnimation = 0;
		Screen owner = host.getScreen();
		switch (state) {
		case OPENING:
			openAnimation = (float) ((minecraft.level.getGameTime() + partialTicks - startAnimation)
					/ OPEN_ANIMATION_LENGTH);
			if (openAnimation >= 1.0 || getTotalVisibleItemCount() == 0) {
				openAnimation = 1;
				state = State.NORMAL;
			}
			break;
		case CLOSING:
			openAnimation = 1
					- (float) ((minecraft.level.getGameTime() + partialTicks - startAnimation) / OPEN_ANIMATION_LENGTH);
			if (openAnimation <= 0 || getTotalVisibleItemCount() == 0) {
				openAnimation = 0;
				state = State.CLOSED;
			}
			break;
		}
		animProgress = openAnimation;
	}

	private void drawTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
		Screen owner = host.getScreen();
		Font fontRenderer = host.getFontRenderer();
		for (RadialMenuItem item : visibleInnerItems) {
			if (item.isHovered()) {
				DrawingContext context = new DrawingContext(graphics, owner.width, owner.height, mouseX, mouseY, 0,
						fontRenderer);
				item.drawTooltips(context);
			}
		}
		for (RadialMenuItem item : visibleItems) {
			if (item.isHovered()) {
				DrawingContext context = new DrawingContext(graphics, owner.width, owner.height, mouseX, mouseY, 0,
						fontRenderer);
				item.drawTooltips(context);
			}
		}
	}

	private void drawItems(GuiGraphics graphics, int x, int y, float z, int width, int height, Font font) {
		iterateBand(visibleInnerItems, (item, s, e) -> {
			float middle = (s + e) * 0.5f;
			float posX = x + innerItemRadius * (float) Math.cos(middle);
			float posY = y + innerItemRadius * (float) Math.sin(middle);

			DrawingContext context = new DrawingContext(graphics, width, height, posX, posY, z, font);
			item.draw(context);
		});
		iterateBand(visibleItems, (item, s, e) -> {
			float middle = (s + e) * 0.5f;
			float posX = x + itemRadius * (float) Math.cos(middle);
			float posY = y + itemRadius * (float) Math.sin(middle);

			DrawingContext context = new DrawingContext(graphics, width, height, posX, posY, z, font);
			item.draw(context);
		});
	}

	private void iterateVisible(TriConsumer<RadialMenuItem, Float, Float> consumer) {
		iterateBand(visibleItems, consumer);
	}

	private void iterateBand(List<RadialMenuItem> visible, TriConsumer<RadialMenuItem, Float, Float> consumer) {
		int numItems = visible.size();
		for (int i = 0; i < numItems; i++) {
			float s = (float) getAngleFor(i - 0.5, numItems);
			float e = (float) getAngleFor(i + 0.5, numItems);

			RadialMenuItem item = visible.get(i);
			consumer.accept(item, s, e);
		}
	}

	private void drawBackground(PoseStack matrixStack, float x, float y, float z, float radiusIn, float radiusOut) {
		if (getTotalVisibleItemCount() > 0) {
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			RenderSystem.setShader(GameRenderer::getPositionColorShader);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

			Tesselator tessellator = Tesselator.getInstance();
			BufferBuilder buffer = tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
			iterateBand(visibleInnerItems, (item, s, e) -> {
				int color = item.getBackgroundColor(item.isHovered() ? backgroundColorHover : backgroundColor);
				drawPieArc(buffer, x, y, z, centerRadius, innerRadiusOut, s, e, color);
			});
			iterateBand(visibleItems, (item, s, e) -> {
				int color = item.getBackgroundColor(item.isHovered() ? backgroundColorHover : backgroundColor);
				drawPieArc(buffer, x, y, z, radiusIn, radiusOut, s, e, color);
			});
			BufferUploader.drawWithShader(buffer.buildOrThrow());
			RenderSystem.disableBlend();
		}
	}

	private static final float PRECISION = 2.5f / 360.0f;

	private void drawVeinyBorders(float x,float y,float z,float partialTicks) {
		if(visibleInnerItems.stream().noneMatch(RadialMenuItem::hasVeinyBorder)
				&& visibleItems.stream().noneMatch(RadialMenuItem::hasVeinyBorder))return;
		RenderSystem.disableDepthTest();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		BufferBuilder buffer=Tesselator.getInstance().begin(VertexFormat.Mode.QUADS,DefaultVertexFormat.POSITION_COLOR);
		double time=minecraft.level.getGameTime()+partialTicks;
		int[] seed={0};
		iterateBand(visibleInnerItems,(item,start,end)->{
			if(item.hasVeinyBorder())drawVeinSegments(buffer,x,y,z,start,end,centerRadius,innerRadiusOut,seed[0],time);
			seed[0]++;
		});
		iterateBand(visibleItems,(item,start,end)->{
			if(item.hasVeinyBorder())drawVeinSegments(buffer,x,y,z,start,end,radiusIn,radiusOut,seed[0],time);
			seed[0]++;
		});
		BufferUploader.drawWithShader(buffer.buildOrThrow());
		RenderSystem.disableBlend();
		RenderSystem.enableDepthTest();
	}

	private static void drawVeinSegments(BufferBuilder buffer,float centerX,float centerY,float z,
			float start,float end,float inner,float outer,int seed,double time) {
		var paths = RadialVeinBorderGeometry.paths(start, end, inner, outer, seed, time);
		for (var path : paths) {
			drawVeinPath(buffer, centerX, centerY, z, path, 1f, true);
		}
		for (var path : paths) {
			drawVeinPath(buffer, centerX, centerY, z, path, .38f, false);
		}
	}

	private record VeinStation(float x, float y, float nx, float ny, float opacity) {}

	private static void drawVeinPath(BufferBuilder buffer, float centerX, float centerY, float z,
			RadialVeinBorderGeometry.VeinPath path, float halfWidth, boolean glow) {
		var points = path.points();
		int count = points.size();
		VeinStation first = null, previous = null;
		for (int i = 0; i < count + (path.closed() ? 1 : 0); i++) {
			VeinStation current;
			if (i == count) {
				current = first;
			} else {
				var at = points.get(i);
				var before = points.get(path.closed() ? (i + count - 1) % count : Math.max(0, i - 1));
				var after = points.get(path.closed() ? (i + 1) % count : Math.min(count - 1, i + 1));
				float dx = after.x() - before.x(), dy = after.y() - before.y();
				float length = (float)Math.hypot(dx, dy);
				float nx = -dy / Math.max(.000001f, length), ny = dx / Math.max(.000001f, length);
				float t = path.closed() ? 0 : i / (float)(count - 1);
				float width = halfWidth * path.strength() * (1 - .96f * t);
				// As with Lux strands, share each station's edges and keep tight joins from folding.
				if (path.closed() || i > 0) width = Math.min(width, veinJoinWidth(before, at, nx, ny));
				if (path.closed() || i + 1 < count) width = Math.min(width, veinJoinWidth(at, after, nx, ny));
				current = new VeinStation(centerX + at.x(), centerY + at.y(), nx * width, ny * width, 1 - t);
			}
			if (previous != null) {
				if (glow) {
					drawVeinStrip(buffer, previous, current, z, 1, 0, 165, 12, 37, 0, 75);
					drawVeinStrip(buffer, previous, current, z, 0, -1, 165, 12, 37, 75, 0);
				} else {
					drawVeinStrip(buffer, previous, current, z, 1, -1, 255, 64, 88, 235, 235);
				}
			}
			if (first == null) first = current;
			previous = current;
		}
	}

	private static float veinJoinWidth(RadialVeinBorderGeometry.Point from, RadialVeinBorderGeometry.Point to,
			float nx, float ny) {
		float dx = to.x() - from.x(), dy = to.y() - from.y();
		return .45f * (dx * dx + dy * dy) / Math.max(.000001f, Math.abs(dx * nx + dy * ny));
	}

	private static void drawVeinStrip(BufferBuilder buffer, VeinStation from, VeinStation to, float z,
			float firstSide, float secondSide, int red, int green, int blue, int firstAlpha, int secondAlpha) {
		// Keep the GUI-facing winding for both the core and the feathered glow.
		buffer.addVertex(from.x() + from.nx() * firstSide, from.y() + from.ny() * firstSide, z)
				.setColor(red, green, blue, (int)(firstAlpha * from.opacity()));
		buffer.addVertex(to.x() + to.nx() * firstSide, to.y() + to.ny() * firstSide, z)
				.setColor(red, green, blue, (int)(firstAlpha * to.opacity()));
		buffer.addVertex(to.x() + to.nx() * secondSide, to.y() + to.ny() * secondSide, z)
				.setColor(red, green, blue, (int)(secondAlpha * to.opacity()));
		buffer.addVertex(from.x() + from.nx() * secondSide, from.y() + from.ny() * secondSide, z)
				.setColor(red, green, blue, (int)(secondAlpha * from.opacity()));
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

			float pos1InX = x + radiusIn * (float) Math.cos(angle1);
			float pos1InY = y + radiusIn * (float) Math.sin(angle1);
			float pos1OutX = x + radiusOut * (float) Math.cos(angle1);
			float pos1OutY = y + radiusOut * (float) Math.sin(angle1);
			float pos2OutX = x + radiusOut * (float) Math.cos(angle2);
			float pos2OutY = y + radiusOut * (float) Math.sin(angle2);
			float pos2InX = x + radiusIn * (float) Math.cos(angle2);
			float pos2InY = y + radiusIn * (float) Math.sin(angle2);

			buffer.addVertex(pos1OutX, pos1OutY, z).setColor(r, g, b, a);
			buffer.addVertex(pos1InX, pos1InY, z).setColor(r, g, b, a);
			buffer.addVertex(pos2InX, pos2InY, z).setColor(r, g, b, a);
			buffer.addVertex(pos2OutX, pos2OutY, z).setColor(r, g, b, a);
		}
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

	private void moveMouseToItem(int which, int numItems) {
		Screen owner = host.getScreen();
		int x = owner.width / 2;
		int y = owner.height / 2;
		float angle = (float) getAngleFor(which, numItems);
		setMousePosition(x + itemRadius * Math.cos(angle), y + itemRadius * Math.sin(angle));
	}

	private void setMousePosition(double x, double y) {
		Screen owner = host.getScreen();
		Window mainWindow = minecraft.getWindow();
		GLFW.glfwSetCursorPos(mainWindow.getWindow(), (int) (x * mainWindow.getScreenWidth() / owner.width),
				(int) (y * mainWindow.getScreenHeight() / owner.height));
	}

	private static final double TWO_PI = 2.0 * Math.PI;

	private void processMouse(int mouseX, int mouseY) {
		if (!isReady())
			return;

		Screen owner = host.getScreen();
		int x = owner.width / 2;
		int y = owner.height / 2;
		double a = Math.atan2(mouseY - y, mouseX - x);
		double d = Math.sqrt(Math.pow(mouseX - x, 2) + Math.pow(mouseY - y, 2));

		RadialMenuItem hoveredItem = findHoveredInBand(visibleInnerItems, a, d, centerRadius, innerRadiusOut);
		if (hoveredItem == null) {
			hoveredItem = findHoveredInBand(visibleItems, a, d, radiusIn, radiusOut);
		}
		for (RadialMenuItem item : visibleInnerItems) {
			item.setHovered(item == hoveredItem);
		}
		for (RadialMenuItem item : visibleItems) {
			item.setHovered(item == hoveredItem);
		}

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

	@Nullable
	private RadialMenuItem findHoveredInBand(List<RadialMenuItem> visible, double angle, double distance,
			float innerRadius, float outerRadius) {
		int numItems = visible.size();
		if (numItems == 0) {
			return null;
		}
		double normalizedAngle = angle;
		double s0 = getAngleFor(0 - 0.5, numItems);
		double s1 = getAngleFor(numItems - 0.5, numItems);
		while (normalizedAngle < s0) {
			normalizedAngle += TWO_PI;
		}
		while (normalizedAngle >= s1) {
			normalizedAngle -= TWO_PI;
		}
		for (int i = 0; i < numItems; i++) {
			float s = (float) getAngleFor(i - 0.5, numItems);
			float e = (float) getAngleFor(i + 0.5, numItems);
			if (normalizedAngle >= s && normalizedAngle < e && distance >= innerRadius && distance < outerRadius) {
				return visible.get(i);
			}
		}
		return null;
	}

	private double getAngleFor(double i, int numItems) {
		if (numItems == 0)
			return 0;
		double angle = ((i / numItems) + 0.25) * TWO_PI + Math.PI;
		return angle;
	}
}
