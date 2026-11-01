package com.vincenthuto.hemomancy.client.screen.dialogue;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.*;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.dialogue.DialogueOptionPacket;
import com.vincenthuto.hemomancy.common.network.dialogue.DialogueRewardChoicePacket;
import com.vincenthuto.hemomancy.common.network.dialogue.DialogueTopicOpenedPacket;
import com.vincenthuto.hemomancy.common.network.trade.NpcTradeSeenPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@OnlyIn(Dist.CLIENT)
public final class DialogueScreen extends Screen {
	private static final int PORTRAIT_SIZE = 56;
	private static final int LINE_HEIGHT = 12;
	private static final int CONTENT_PAD = 6;
	private static final int SCROLL_STEP = 24;
	private static final String PNG_SUFFIX = ".png";
	private static final String PORTRAIT_SUFFIX = "_portrait.png";

	private final DialogueTree tree;
	private final DialogueThemeStyle style;
	private final DialogueNavigationState navigation;
	private final List<ClickTarget> clickTargets = new ArrayList<>();
	private final Set<String> locallyReadTopics = new HashSet<>();
	private static long nextClaimRequest;
	private record PendingClaim(DialogueNode node, DialogueOption option) {}
	private final java.util.Map<Long, PendingClaim> pendingClaims = new java.util.HashMap<>();
	private DialogueNode currentNode;
	private java.util.UUID pendingWhisperId;
	private DialogueLayout layout;
	private ResourceLocation resolvedPortraitIcon;
	private boolean resolvedPortraitIsCompanion;
	private ResourceLocation resolvedCrest;
	private int scrollOffset;
	private int maxScroll;
	private ItemStack hoveredInquiryItem = ItemStack.EMPTY;
	private boolean hoveredLoreMarker;
	/** The Trade box as last sent: on open, then replaced by every trade result for this speaker. */
	private com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeBoard tradeBoard;
	/** The last trade result's outcome and reason key, for the Trade view; null before any trade. */
	private TradeReply lastTradeReply;
	private final DialogueTradeView tradeView;

	/** How the last trade request ended. */
	public record TradeReply(ResourceLocation offerId,
			com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeService.Outcome outcome, String reasonKey) {}

	private DialogueScreen(DialogueTree tree) {
		super(Component.empty());
		this.tree = tree;
		this.currentNode = tree.getStartNode();
		this.style = DialogueThemeStyle.forTheme(tree.theme());
		this.tradeBoard = tree.presentation().trade();
		this.tradeView = new DialogueTradeView(tree.entityId(), style);
		this.navigation = tree.presentation().mode() == DialogueScreenMode.TOPIC_HUB
				? DialogueNavigationState.hub()
				: DialogueNavigationState.focused(tree.startNodeId());
		DialogueTopic attentionTopic = DialogueAttentionResolver.topic(tree.presentation());
		if (attentionTopic != null && tree.getNode(attentionTopic.targetNodeId()) != null) {
			navigation.openCategory(attentionTopic.category());
			openTopic(attentionTopic);
		}
	}

	public static void openWhisper(DialogueTree tree, java.util.UUID id) {
		Screen current = Minecraft.getInstance().screen;
		if (current != null && !(current instanceof net.minecraft.client.gui.screens.ChatScreen)) return;
		open(tree);
		((DialogueScreen) Minecraft.getInstance().screen).pendingWhisperId = id;
	}

	@Override
	public void onClose() {
		if (pendingWhisperId != null) {
			PacketHandler.sendToServer(new com.vincenthuto.hemomancy.common.network.dialogue.ClosePendingWhisperPacket(pendingWhisperId));
			pendingWhisperId = null;
		}
		super.onClose();
	}

	public static void open(DialogueTree tree) {
		Minecraft.getInstance().setScreen(new DialogueScreen(tree));
		if (tree.entityId() == 0
				&& com.vincenthuto.hemomancy.client.screen.overlay.FungalWhisperVignetteOverlay.instance != null) {
			com.vincenthuto.hemomancy.client.screen.overlay.FungalWhisperVignetteOverlay.instance.trigger();
		}
	}

	@Override
	protected void init() {
		layout = DialogueLayout.calculate(width, height);
		resolvePortraitIcon();
		resetScroll();
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
		layout = DialogueLayout.calculate(width, height);
		clickTargets.clear();
		hoveredInquiryItem = ItemStack.EMPTY;
		hoveredLoreMarker = false;
		if (!inTradeBox()) tradeView.reset();
		resetGuiState();
		gfx.blitSprite(style.frameSprite(), layout.panel().x(), layout.panel().y(),
				layout.panel().width(), layout.panel().height());
		renderHeader(gfx);
		renderContent(gfx, mouseX, mouseY);
		renderFooter(gfx, mouseX, mouseY);
		resetGuiState();
		super.render(gfx, mouseX, mouseY, partialTick);
		if (!hoveredInquiryItem.isEmpty()) {
			gfx.renderTooltip(font, hoveredInquiryItem, mouseX, mouseY);
		} else if (hoveredLoreMarker) {
			gfx.renderTooltip(font, Component.translatable("hemomancy.dialogue.topic.lore_marker"), mouseX, mouseY);
		} else if (inTradeBox() && !tradeView.hoveredStack().isEmpty()) {
			gfx.renderTooltip(font, tradeView.hoveredStack(), mouseX, mouseY);
		} else if (inTradeBox() && !tradeView.hoveredText().isEmpty()) {
			gfx.renderComponentTooltip(font, tradeView.hoveredText(), mouseX, mouseY);
		}
	}

	private static Component resolveDialogueLine(String value) {
		if (value.startsWith(DialogueNode.TRANSLATE_PREFIX)) {
			String[] parts = value.substring(DialogueNode.TRANSLATE_PREFIX.length())
					.split(Pattern.quote(DialogueNode.ARGUMENT_SEPARATOR), -1);
			return Component.translatable(parts[0], (Object[]) Arrays.copyOfRange(parts, 1, parts.length));
		}
		return value.startsWith(DialogueNode.LITERAL_PREFIX)
				? Component.literal(value.substring(DialogueNode.LITERAL_PREFIX.length()))
				: Component.translatable(value);
	}

	private void renderHeader(GuiGraphics gfx) {
		var header = layout.header();
		int portraitSize = Math.min(PORTRAIT_SIZE, header.height() - 4);
		renderPortrait(gfx, header.x() + 2, header.y() + 2, portraitSize);
		gfx.blitSprite(crest(),
				header.right() - 20, header.y() + 4, 16, 16);
		int textX = header.x() + portraitSize + 12;
		gfx.drawString(font, Component.translatable(tree.speakerName()), textX, header.y() + 5,
				style.speakerColor(), true);
		int textWidth = Math.max(1, header.right() - textX - 24);
		int y = header.y() + 20;
		DialogueNode headerNode = navigation.view() == DialogueNavigationState.View.NODE ? null : tree.getStartNode();
		Component reaction = inTradeBox() ? tradeView.reaction() : null;
		if (headerNode != null) {
			var lines = reaction != null ? font.split(reaction, textWidth) : headerNode.lines().stream()
					.flatMap(key -> font.split(resolveDialogueLine(key), textWidth).stream()).toList();
			boolean overflow = y + Math.max(0, lines.size() - 1) * LINE_HEIGHT + font.lineHeight > header.bottom() - 2;
			int limit = overflow ? header.bottom() - LINE_HEIGHT - 3 : header.bottom() - 2;
			for (var line : lines) {
				if (y + font.lineHeight > limit) break;
				gfx.drawString(font, line, textX, y, style.textColor(), false);
				y += LINE_HEIGHT;
			}
			if (overflow) {
				int buttonY = header.bottom() - LINE_HEIGHT - 2;
				gfx.drawString(font, Component.translatable("hemomancy.dialogue.ui.read_greeting"),
						textX, buttonY, style.optionColor(), true);
				clickTargets.add(new ClickTarget(textX, buttonY, textWidth, LINE_HEIGHT,
						TargetKind.GREETING, null, null, -1, true));
			}
		}
		gfx.fill(header.x(), header.bottom() - 1, header.right(), header.bottom(), style.separatorColor());
	}

	private void renderContent(GuiGraphics gfx, int mouseX, int mouseY) {
		var content = layout.content();
		gfx.enableScissor(content.x(), content.y(), content.right(), content.bottom());
		int measured;
		try {
			measured = switch (navigation.view()) {
				case HUB -> renderHub(gfx, mouseX, mouseY, content.y() - scrollOffset);
				case CATEGORY -> renderCategory(gfx, mouseX, mouseY, content.y() - scrollOffset);
				case NODE -> renderNode(gfx, mouseX, mouseY, content.y() - scrollOffset);
			};
		} finally {
			gfx.disableScissor();
		}
		clickTargets.removeIf(target -> target.contentTarget()
				&& !DialogueHitRules.intersects(target.rect(), content));
		maxScroll = Math.max(0, measured - content.height());
		scrollOffset = Mth.clamp(scrollOffset, 0, maxScroll);
		if (maxScroll > 0) renderScrollbar(gfx);
	}

	private int renderHub(GuiGraphics gfx, int mouseX, int mouseY, int startY) {
		var content = layout.content();
		DialogueHubLayout hub = DialogueHubLayout.calculate(layout);
		List<DialogueCategory> categories = tree.presentation().categories();
		for (int i = 0; i < categories.size(); i++) {
			DialogueCategory category = categories.get(i);
			DialogueLayout.Rect card = hub.cards().get(i);
			int x = card.x();
			int y = card.y() - scrollOffset;
			int cardWidth = card.width();
			int cardHeight = card.height();
			boolean enabled = tree.presentation().hasTopics(category)
					|| category == DialogueCategory.TRADE && tradeBoard.present();
			boolean selected = enabled && (contains(x, y, cardWidth, cardHeight, mouseX, mouseY)
					|| navigation.focusIndex() == i);
			gfx.blitSprite(enabled ? DialogueThemeStyle.categoryCard(category, selected)
					: style.cardDisabledSprite(), x, y, cardWidth, cardHeight);
			ResourceLocation icon = DialogueThemeStyle.categoryIcon(category);
			gfx.blitSprite(icon, x + 8, y + 8, 16, 16);
			gfx.drawString(font, categoryTitle(category), x + 30, y + 8,
					enabled ? style.optionColor() : style.disabledColor(), true);
			Component summary = enabled
					? Component.translatable(categorySummary(category))
					: Component.translatable("hemomancy.dialogue.ui.no_topics");
			drawWrapped(gfx, summary, x + 30, y + 23, cardWidth - 38, 2,
					enabled ? style.textColor() : style.disabledColor());
			if (enabled && hasUnread(category)) {
				gfx.blitSprite(DialogueThemeStyle.statusIcon("unread"), x + cardWidth - 14, y + 5, 8, 8);
			}
			clickTargets.add(ClickTarget.category(x, y, cardWidth, cardHeight, category, enabled));
		}
		DialogueLayout.Rect last = hub.cards().getLast();
		return Math.max(content.height(), last.bottom() - content.y() + CONTENT_PAD);
	}

	private int renderCategory(GuiGraphics gfx, int mouseX, int mouseY, int startY) {
		var content = layout.content();
		List<DialogueTopic> topics = tree.presentation().topics(navigation.category());
		if (navigation.category() == DialogueCategory.INQUIRIES) {
			return renderInquiryGrid(gfx, mouseX, mouseY, startY, content, topics);
		}
		if (navigation.category() == DialogueCategory.TRADE) {
			return tradeView.render(gfx, font, tradeBoard, crest(),
					content, startY, mouseX, mouseY, (x, y, width, height, enabled, action) ->
							clickTargets.add(ClickTarget.trade(x, y, width, height, enabled, action)));
		}
		int y = startY + CONTENT_PAD;
		int width = content.width() - CONTENT_PAD * 2;
		for (int i = 0; i < topics.size(); i++) {
			DialogueTopic topic = topics.get(i);
			int height = navigation.category() == DialogueCategory.QUESTS ? 54 : 44;
			boolean enabled = topic.state().enabled();
			int x = content.x() + CONTENT_PAD;
			DialogueLayout.Rect card = new DialogueLayout.Rect(x, y, width, height);
			boolean showUnread = topic.unread() && !locallyReadTopics.contains(topic.id());
			DialogueTopicCardLayout cardLayout = DialogueTopicCardLayout.calculate(card, showUnread);
			renderCard(gfx, x, y, width, height, mouseX, mouseY,
					enabled, navigation.focusIndex() == i);
			if (topic.displayItemId() != null) {
				ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(topic.displayItemId()));
				gfx.renderItem(stack, x + 8, y + 9);
			} else {
				gfx.blitSprite(topic.icon() != null ? topic.icon() : DialogueThemeStyle.categoryIcon(topic.category()),
						x + 8, y + 9, 16, 16);
				if (topic.lore() && content.contains(mouseX, mouseY) && contains(x + 8, y + 9, 16, 16, mouseX, mouseY)) {
					hoveredLoreMarker = true;
				}
			}
			int color = enabled ? style.optionColor() : style.disabledColor();
			Component title = topic.displayItemId() != null
					? new ItemStack(BuiltInRegistries.ITEM.get(topic.displayItemId())).getHoverName()
					: Component.translatable(topic.titleKey());
			var titleLines = font.split(title, cardLayout.title().width());
			if (!titleLines.isEmpty()) {
				gfx.drawString(font, titleLines.getFirst(), cardLayout.title().x(), cardLayout.title().y(), color, true);
			}
			drawWrapped(gfx, Component.translatable(topic.summaryKey()), cardLayout.summary().x(),
					cardLayout.summary().y(), cardLayout.summary().width(), 2,
					enabled ? style.textColor() : style.disabledColor());
			renderTopicStatus(gfx, topic, x, y, width, height);
			clickTargets.add(ClickTarget.topic(x, y, width, height, topic, enabled));
			y += height + 6;
		}
		return Math.max(content.height(), y - startY + CONTENT_PAD);
	}

	private int renderInquiryGrid(GuiGraphics gfx, int mouseX, int mouseY, int startY,
			DialogueLayout.Rect content, List<DialogueTopic> topics) {
		DialogueLayout.Rect scrolledContent = new DialogueLayout.Rect(
				content.x(), startY, content.width(), content.height());
		DialogueInquiryGridLayout grid = DialogueInquiryGridLayout.calculate(scrolledContent, topics.size());
		for (int i = 0; i < topics.size(); i++) {
			DialogueTopic topic = topics.get(i);
			DialogueLayout.Rect cell = grid.cells().get(i);
			boolean enabled = topic.state().enabled();
			renderCard(gfx, cell.x(), cell.y(), cell.width(), cell.height(), mouseX, mouseY,
					enabled, navigation.focusIndex() == i);
			if (topic.displayItemId() != null) {
				ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(topic.displayItemId()));
				gfx.renderItem(stack, cell.x() + 6, cell.y() + 6);
				if (content.contains(mouseX, mouseY) && cell.contains(mouseX, mouseY)) {
					hoveredInquiryItem = stack;
				}
			}
			clickTargets.add(ClickTarget.topic(cell.x(), cell.y(), cell.width(), cell.height(), topic, enabled));
		}
		return Math.max(content.height(), grid.contentHeight());
	}

	private void renderTopicStatus(GuiGraphics gfx, DialogueTopic topic, int x, int y, int width, int height) {
		if (topic.unread() && !locallyReadTopics.contains(topic.id())) {
			gfx.blitSprite(DialogueThemeStyle.statusIcon("unread"), x + width - 14, y + 5, 8, 8);
		}
		if (topic.state() != DialogueTopicState.AVAILABLE) {
			String state = topic.state().name().toLowerCase();
			gfx.blitSprite(DialogueThemeStyle.statusIcon(state), x + width - 14, y + height - 14, 8, 8);
		}
		if (topic.progress() != null && topic.progress().target() > 0) {
			int barX = x + 32;
			int barY = y + height - 9;
			int barW = width - 52;
			gfx.fill(barX, barY, barX + barW, barY + 3, style.progressBackColor());
			float ratio = Mth.clamp(topic.progress().current() / (float) topic.progress().target(), 0F, 1F);
			gfx.fill(barX, barY, barX + Math.round(barW * ratio), barY + 3, style.progressColor());
		}
	}

	private int renderNode(GuiGraphics gfx, int mouseX, int mouseY, int startY) {
		var content = layout.content();
		if (currentNode == null) return content.height();
		int x = content.x() + CONTENT_PAD;
		int width = content.width() - CONTENT_PAD * 2;
		int y = startY + CONTENT_PAD;
		for (String key : currentNode.lines()) {
			for (var line : font.split(resolveDialogueLine(key), width)) {
				gfx.drawString(font, line, x, y, style.textColor(), false);
				y += LINE_HEIGHT;
			}
			y += 3;
		}
		y += 5;
		for (int i = 0; i < currentNode.options().size(); i++) {
			DialogueOption option = currentNode.options().get(i);
			int lines = Math.max(1, font.split(Component.translatable(option.text()), width - 24).size());
			int height = Math.max(24, lines * LINE_HEIGHT + 10);
			boolean enabled = option.presentation().enabled() && !isClaimPending(option);
			renderResponse(gfx, x, y, width, height, mouseX, mouseY, enabled,
					navigation.focusIndex() == i, option);
			clickTargets.add(ClickTarget.option(x, y, width, height, i, enabled));
			y += height + 5;
		}
		return Math.max(content.height(), y - startY + CONTENT_PAD);
	}

	private void renderResponse(GuiGraphics gfx, int x, int y, int width, int height, int mouseX, int mouseY,
			boolean enabled, boolean focused, DialogueOption option) {
		boolean hovered = enabled && contains(x, y, width, height, mouseX, mouseY);
		ResourceLocation sprite = !enabled ? style.buttonDisabledSprite()
				: (hovered || focused ? style.buttonSelectedSprite() : style.buttonSprite());
		gfx.blitSprite(sprite, x, y, width, height);
		int color = enabled ? style.optionColor() : style.disabledColor();
		gfx.drawString(font, Component.literal("> ").append(Component.translatable(option.text())),
				x + 8, y + 7, color, false);
		if (option.presentation().detailKey() != null) {
			gfx.drawString(font, Component.translatable(option.presentation().detailKey()),
				x + 16, y + height - 11, style.disabledColor(), false);
		}
	}

	private void renderCard(GuiGraphics gfx, int x, int y, int width, int height, int mouseX, int mouseY,
			boolean enabled, boolean focused) {
		boolean hovered = enabled && contains(x, y, width, height, mouseX, mouseY);
		ResourceLocation sprite = !enabled ? style.cardDisabledSprite()
				: (hovered || focused ? style.cardSelectedSprite() : style.cardSprite());
		gfx.blitSprite(sprite, x, y, width, height);
	}

	private void renderFooter(GuiGraphics gfx, int mouseX, int mouseY) {
		var footer = layout.footer();
		if (navigation.view() == DialogueNavigationState.View.HUB
				&& tree.presentation().mode() == DialogueScreenMode.TOPIC_HUB) {
			DialogueLayout.Rect leave = DialogueHubLayout.calculate(layout).leave();
			boolean focused = navigation.focusIndex() == clickTargets.size();
			boolean hovered = leave.contains(mouseX, mouseY);
			gfx.blitSprite(hovered || focused ? style.buttonSelectedSprite() : style.buttonSprite(),
					leave.x(), leave.y(), leave.width(), leave.height());
			gfx.blitSprite(DialogueThemeStyle.statusIcon("leave"), leave.x() + 7, leave.y() + 2, 16, 16);
			gfx.drawString(font, Component.translatable("hemomancy.dialogue.ui.leave"),
					leave.x() + 28, leave.y() + 6, style.optionColor(), false);
			clickTargets.add(ClickTarget.close(leave.x(), leave.y(), leave.width(), leave.height()));
			return;
		}
		boolean canBack = navigation.view() != DialogueNavigationState.View.HUB
				&& tree.presentation().mode() == DialogueScreenMode.TOPIC_HUB;
		String key = canBack ? "hemomancy.dialogue.ui.back" : "hemomancy.dialogue.ui.leave";
		int width = 92;
		int x = footer.right() - width;
		int y = footer.y();
		gfx.blitSprite(contains(x, y, width, footer.height(), mouseX, mouseY)
				? style.buttonSelectedSprite() : style.buttonSprite(), x, y, width, footer.height());
		gfx.drawCenteredString(font, Component.translatable(key), x + width / 2, y + 5, style.optionColor());
		clickTargets.add(canBack ? ClickTarget.back(x, y, width, footer.height())
				: ClickTarget.close(x, y, width, footer.height()));
	}

	private void renderScrollbar(GuiGraphics gfx) {
		var content = layout.content();
		int x = content.right() - 3;
		int thumbHeight = Math.max(16, content.height() * content.height() / (content.height() + maxScroll));
		int travel = Math.max(1, content.height() - thumbHeight);
		int y = content.y() + Math.round(travel * (scrollOffset / (float) maxScroll));
		gfx.fill(x, content.y(), x + 2, content.bottom(), style.scrollTrackColor());
		gfx.fill(x - 1, y, x + 3, y + thumbHeight, style.scrollThumbColor());
	}

	private void renderPortrait(GuiGraphics gfx, int x, int y, int size) {
		ResourceLocation icon = resolvedPortraitIcon != null ? resolvedPortraitIcon : tree.speakerIcon();
		AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(icon);
		texture.setBlurMipmap(false, false);
		gfx.blitSprite(style.portraitFrameSprite(), x - 2, y - 2, size + 4, size + 4);
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		if (resolvedPortraitIsCompanion) {
			gfx.blit(icon, x, y, size, size, 0F, 0F, 48, 48, 48, 48);
		} else {
			gfx.blit(icon, x, y, size, size, 8F, 8F, 8, 8, 64, 64);
		}
		texture.restoreLastBlurMipmap();
	}

	/** The speaker's crest, or the default crest when no sprite was drawn for them (the Circus has none yet). */
	private ResourceLocation crest() {
		if (resolvedCrest == null) {
			ResourceLocation crest = DialogueThemeStyle.crest(tree.presentation().styleId());
			boolean missing = Minecraft.getInstance().getGuiSprites().getSprite(crest).contents().name()
					.equals(net.minecraft.client.renderer.texture.MissingTextureAtlasSprite.getLocation());
			resolvedCrest = missing ? DialogueThemeStyle.crest(null) : crest;
		}
		return resolvedCrest;
	}

	private void resolvePortraitIcon() {
		ResourceLocation base = tree.speakerIcon();
		for (ResourceLocation candidate : companionPortraitCandidates(base)) {
			if (Minecraft.getInstance().getResourceManager().getResource(candidate).isPresent()) {
				resolvedPortraitIcon = candidate;
				resolvedPortraitIsCompanion = true;
				return;
			}
		}
		resolvedPortraitIcon = base;
		resolvedPortraitIsCompanion = false;
	}

	private List<ResourceLocation> companionPortraitCandidates(ResourceLocation baseTexture) {
		String path = baseTexture.getPath();
		if (path.endsWith(PNG_SUFFIX)) {
			String base = path.substring(0, path.length() - PNG_SUFFIX.length());
			return List.of(ResourceLocation.fromNamespaceAndPath(baseTexture.getNamespace(), base + PORTRAIT_SUFFIX),
					ResourceLocation.fromNamespaceAndPath(baseTexture.getNamespace(), base + "portrait.png"));
		}
		return List.of(ResourceLocation.fromNamespaceAndPath(baseTexture.getNamespace(), path + "_portrait"));
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0) {
			for (int i = 0; i < clickTargets.size(); i++) {
				ClickTarget target = clickTargets.get(i);
				if (target.contentTarget()
						? DialogueHitRules.containsVisible(target.rect(), layout.content(), mouseX, mouseY)
						: target.contains(mouseX, mouseY)) {
					navigation.setFocusIndex(i);
					activate(target);
					return true;
				}
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (layout.content().contains(mouseX, mouseY) && maxScroll > 0) {
			scrollOffset = Mth.clamp(scrollOffset - (int) Math.signum(scrollY) * SCROLL_STEP, 0, maxScroll);
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
			if (navigation.back()) {
				currentNode = tree.getStartNode();
				resetScroll();
				return true;
			}
			onClose();
			return true;
		}
		if (keyCode == GLFW.GLFW_KEY_UP || keyCode == GLFW.GLFW_KEY_LEFT) {
			navigation.moveFocus(-1, clickTargets.size());
			return true;
		}
		if (keyCode == GLFW.GLFW_KEY_DOWN || keyCode == GLFW.GLFW_KEY_RIGHT || keyCode == GLFW.GLFW_KEY_TAB) {
			navigation.moveFocus(1, clickTargets.size());
			return true;
		}
		if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_SPACE) {
			activateFocused();
			return true;
		}
		if (keyCode >= GLFW.GLFW_KEY_1 && keyCode <= GLFW.GLFW_KEY_9) {
			int index = keyCode - GLFW.GLFW_KEY_1;
			if (index < clickTargets.size()) activate(clickTargets.get(index));
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	private void activateFocused() {
		if (!clickTargets.isEmpty()) {
			activate(clickTargets.get(Mth.clamp(navigation.focusIndex(), 0, clickTargets.size() - 1)));
		}
	}

	private void activate(ClickTarget target) {
		if (!target.enabled) return;
		switch (target.kind) {
			case GREETING -> {
				currentNode = tree.getStartNode();
				navigation.openNode(currentNode.id());
			}
			case CATEGORY -> enterCategory(target.category);
			case TOPIC -> openTopic(target.topic);
			case OPTION -> selectOption(target.optionIndex);
			case BACK -> {
				navigation.back();
				currentNode = tree.getStartNode();
			}
			case CLOSE -> onClose();
			case TRADE -> {
				target.action.run();
				return;
			}
		}
		resetScroll();
	}

	private void openTopic(DialogueTopic topic) {
		if (DialogueHubFactory.TRADE_TARGET.equals(topic.targetNodeId())) {
			enterCategory(DialogueCategory.TRADE);
			return;
		}
		DialogueNode node = tree.getNode(topic.targetNodeId());
		if (node == null) return;
		currentNode = node;
		navigation.openNode(node.id());
		if (topic.lore() && topic.unread() && locallyReadTopics.add(topic.id())) {
			ResourceLocation dialogueId = tree.presentation().dialogueId();
			ResourceLocation readId = ResourceLocation.fromNamespaceAndPath(dialogueId.getNamespace(),
					dialogueId.getPath() + "/" + topic.readPath());
			PacketHandler.sendToServer(new DialogueTopicOpenedPacket(readId, tree.entityId()));
		}
	}

	private void selectOption(int index) {
		if (currentNode == null || index < 0 || index >= currentNode.options().size()) return;
		DialogueOption option = currentNode.options().get(index);
		if (!option.presentation().enabled() || isClaimPending(option)) return;
		if (DialogueRewardClaims.requiresAcknowledgement(option.eventId())) {
			long request = ++nextClaimRequest;
			pendingClaims.put(request, new PendingClaim(currentNode, option));
			PacketHandler.sendToServer(new DialogueRewardChoicePacket(option.eventId(), tree.entityId(), request));
			return;
		}
		if (option.eventId() != null && !option.eventId().isEmpty()) {
			PacketHandler.sendToServer(new DialogueOptionPacket(option.eventId(), tree.entityId()));
		}
		followOption(option);
	}

	private boolean isClaimPending(DialogueOption option) {
		return pendingClaims.values().stream().anyMatch(p -> p.node() == currentNode && p.option() == option);
	}

	/**
	 * A trade result for the open dialogue: replaces its Trade box with the fresh one and keeps the outcome. A result for
	 * another speaker, or with no dialogue open, is dropped.
	 */
	public static void receiveTradeResult(int entityId, TradeReply reply,
			com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeBoard board) {
		if (!(Minecraft.getInstance().screen instanceof DialogueScreen screen) || screen.tree.entityId() != entityId) return;
		screen.tradeBoard = board;
		screen.lastTradeReply = reply;
		screen.tradeView.onResult(reply, board);
	}

	/** Whether the Trade box is the open view. */
	private boolean inTradeBox() {
		return navigation.view() == DialogueNavigationState.View.CATEGORY
				&& navigation.category() == DialogueCategory.TRADE;
	}

	/** Opens a hub box, as clicking its card does (the review client drives the Trade box this way). */
	public void openCategory(DialogueCategory category) {
		enterCategory(category);
		resetScroll();
	}

	/**
	 * Opens a hub box. Opening the Trade box shows today's daily cards, so their unread dots clear here and the server
	 * records them as seen.
	 */
	private void enterCategory(DialogueCategory category) {
		navigation.openCategory(category);
		if (category != DialogueCategory.TRADE) return;
		boolean marked = false;
		for (DialogueTopic topic : tree.presentation().topics(DialogueCategory.TRADE)) {
			if (topic.unread() && locallyReadTopics.add(topic.id())) marked = true;
		}
		if (marked && tree.entityId() != 0) {
			PacketHandler.sendToServer(new NpcTradeSeenPacket(tree.entityId()));
		}
	}

	/** Whether a hub box holds a card the player hasn't opened yet (an unread lore line or a new daily want). */
	private boolean hasUnread(DialogueCategory category) {
		for (DialogueTopic topic : tree.presentation().topics(category)) {
			if (topic.unread() && !locallyReadTopics.contains(topic.id())) return true;
		}
		return false;
	}

	/** Opens a want card's selection strip in the Trade box. */
	public void expandTradeCard(ResourceLocation offerId) {
		openCategory(DialogueCategory.TRADE);
		tradeView.expand(offerId);
	}

	/** The speaker entity this dialogue is open on (0 for a whisper). */
	public int entityId() {
		return tree.entityId();
	}

	public com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeBoard tradeBoard() {
		return tradeBoard;
	}

	@org.jetbrains.annotations.Nullable
	public TradeReply lastTradeReply() {
		return lastTradeReply;
	}

	public static void receiveRewardResult(long requestId, boolean delivered) {
		if (!(Minecraft.getInstance().screen instanceof DialogueScreen screen)) return;
		PendingClaim claim = screen.pendingClaims.remove(requestId);
		if (claim == null) return;
		if (delivered) {
			if (screen.currentNode == claim.node()) screen.followOption(claim.option());
		} else if (Minecraft.getInstance().player != null) {
			Minecraft.getInstance().player.displayClientMessage(Component.translatable(
					"hemomancy.dialogue.reward_not_delivered"), false);
		}
	}

	private void followOption(DialogueOption option) {
		if (option.nextNodeId() == null) {
			onClose();
			return;
		}
		if (tree.presentation().mode() == DialogueScreenMode.TOPIC_HUB
				&& option.nextNodeId().equals(tree.startNodeId())) {
			navigation.toHub();
			currentNode = tree.getStartNode();
			return;
		}
		DialogueNode next = tree.getNode(option.nextNodeId());
		if (next != null) {
			currentNode = next;
			navigation.openNode(next.id());
		}
	}

	private void drawWrapped(GuiGraphics gfx, Component text, int x, int y, int width, int maxLines, int color) {
		List<net.minecraft.util.FormattedCharSequence> lines = font.split(text, Math.max(1, width));
		for (int i = 0; i < Math.min(maxLines, lines.size()); i++) {
			gfx.drawString(font, lines.get(i), x, y + i * LINE_HEIGHT, color, false);
		}
	}

	private Component categoryTitle(DialogueCategory category) {
		return Component.translatable("hemomancy.dialogue.category." + category.name().toLowerCase());
	}

	private String categorySummary(DialogueCategory category) {
		return "hemomancy.dialogue.category." + category.name().toLowerCase() + ".summary";
	}

	private void resetScroll() {
		scrollOffset = 0;
		maxScroll = 0;
	}

	private void resetGuiState() {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
		RenderSystem.disableDepthTest();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
	}

	@Override
	public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		// Intentionally no blur; render() applies a light dim behind the centered modal.
	}

	private static boolean contains(int x, int y, int width, int height, double mouseX, double mouseY) {
		return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
	}

	private enum TargetKind { GREETING, CATEGORY, TOPIC, OPTION, BACK, CLOSE, TRADE }

	private record ClickTarget(int x, int y, int width, int height, TargetKind kind,
			DialogueCategory category, DialogueTopic topic, int optionIndex, boolean enabled, Runnable action) {
		ClickTarget(int x, int y, int width, int height, TargetKind kind, DialogueCategory category,
				DialogueTopic topic, int optionIndex, boolean enabled) {
			this(x, y, width, height, kind, category, topic, optionIndex, enabled, null);
		}
		/** A Trade box control: a card, a slot well or Offer. Activating it runs {@code action} and keeps the scroll. */
		static ClickTarget trade(int x, int y, int width, int height, boolean enabled, Runnable action) {
			return new ClickTarget(x, y, width, height, TargetKind.TRADE, null, null, -1, enabled, action);
		}
		static ClickTarget category(int x, int y, int width, int height, DialogueCategory category, boolean enabled) {
			return new ClickTarget(x, y, width, height, TargetKind.CATEGORY, category, null, -1, enabled);
		}
		static ClickTarget topic(int x, int y, int width, int height, DialogueTopic topic, boolean enabled) {
			return new ClickTarget(x, y, width, height, TargetKind.TOPIC, null, topic, -1, enabled);
		}
		static ClickTarget option(int x, int y, int width, int height, int index, boolean enabled) {
			return new ClickTarget(x, y, width, height, TargetKind.OPTION, null, null, index, enabled);
		}
		static ClickTarget back(int x, int y, int width, int height) {
			return new ClickTarget(x, y, width, height, TargetKind.BACK, null, null, -1, true);
		}
		static ClickTarget close(int x, int y, int width, int height) {
			return new ClickTarget(x, y, width, height, TargetKind.CLOSE, null, null, -1, true);
		}
		boolean contains(double mouseX, double mouseY) {
			return DialogueScreen.contains(x, y, width, height, mouseX, mouseY);
		}
		boolean contentTarget() {
			return kind == TargetKind.CATEGORY || kind == TargetKind.TOPIC || kind == TargetKind.OPTION
					|| kind == TargetKind.TRADE;
		}
		DialogueLayout.Rect rect() {
			return new DialogueLayout.Rect(x, y, width, height);
		}
	}
}
