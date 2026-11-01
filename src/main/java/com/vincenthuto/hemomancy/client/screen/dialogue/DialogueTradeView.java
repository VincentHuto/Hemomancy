package com.vincenthuto.hemomancy.client.screen.dialogue;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.screen.dialogue.DialogueTradePicks.Carried;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.ScreenDrawUtils;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeAppraisals;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeAppraisals.Grade;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeBoard;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeCard;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeGive;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeMatch;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeRules;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeService;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeSides;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeWant;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.trade.NpcTradeRequestPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * The Trade box inside {@link DialogueScreen}: the rapport bar and the speaker's want cards, or their refusal line.
 * The client matches and grades its own inventory with the shared {@link NpcTradeMatch} and {@link NpcTradeAppraisals}
 * (a preview; the server checks again). Clicking a card opens its selection strip with the best stacks pre-picked;
 * Offer sends an {@link NpcTradeRequestPacket} and greys the card until the reply, or until a short timeout, since a
 * rate-limited request gets no reply.
 */
@OnlyIn(Dist.CLIENT)
final class DialogueTradeView {
	/** Two seconds: long past any reply, so a dropped request doesn't leave the card grey. */
	static final int PENDING_TIMEOUT_TICKS = 40;
	private static final int LINE_HEIGHT = 9;
	private static final int ICON_STEP = 18;
	private static final int MAIN_SLOTS = 36;
	private static final ResourceLocation BLOOD_DROP = DialogueThemeStyle.statusIcon("blood_drop");
	private static final ResourceLocation ARROW = DialogueThemeStyle.statusIcon("trade_arrow");
	private static final ResourceLocation LOCKED = DialogueThemeStyle.statusIcon("locked");

	/** Where the screen collects the view's click targets. */
	interface Targets {
		void add(int x, int y, int width, int height, boolean enabled, Runnable action);
	}

	private final int entityId;
	private final DialogueThemeStyle style;
	private final Map<NpcTradeGive, ItemStack> giveStacks = new HashMap<>();
	private final Map<NpcTradeMatch, ItemStack[]> displayStacks = new HashMap<>();
	private ResourceLocation expanded;
	private List<Integer> picked = List.of();
	private boolean pickedByPlayer;
	private ResourceLocation pending;
	private long pendingSince;
	private ResourceLocation noteOffer;
	private String noteKey;
	private Component reaction;
	private ItemStack hoveredStack = ItemStack.EMPTY;
	private List<Component> hoveredText = List.of();

	DialogueTradeView(int entityId, DialogueThemeStyle style) {
		this.entityId = entityId;
		this.style = style;
	}

	/** Draws the box from {@code startY} (already scrolled) and returns its height. */
	int render(GuiGraphics gfx, Font font, NpcTradeBoard board, ResourceLocation crest, DialogueLayout.Rect content,
			int startY, int mouseX, int mouseY, Targets targets) {
		hoveredStack = ItemStack.EMPTY;
		hoveredText = List.of();
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.level == null) return content.height();
		if (pending != null && mc.level.getGameTime() - pendingSince > PENDING_TIMEOUT_TICKS) pending = null;
		DialogueLayout.Rect scrolled = new DialogueLayout.Rect(content.x(), startY, content.width(), content.height());
		if (board.refusalKey().isPresent()) {
			drawCentered(gfx, font, Component.translatable(board.refusalKey().get()), scrolled, style.textColor());
			return content.height();
		}
		if (board.cards().isEmpty()) {
			drawCentered(gfx, font, Component.translatable("hemomancy.trade.ui.empty"), scrolled, style.disabledColor());
			return content.height();
		}

		Inventory inventory = mc.player.getInventory();
		List<Row> rows = rows(board, inventory);
		int expandedIndex = -1;
		for (int i = 0; i < rows.size(); i++) if (rows.get(i).card().offerId().equals(expanded)) expandedIndex = i;
		if (expandedIndex < 0) expanded = null;
		Row open = expandedIndex >= 0 ? rows.get(expandedIndex) : null;
		if (open != null) {
			picked = pickedByPlayer ? DialogueTradePicks.retain(picked, open.stacks())
					: DialogueTradePicks.prePick(open.stacks(), open.counts());
		}
		boolean note = open != null && open.card().offerId().equals(noteOffer) && noteKey != null;
		DialogueTradeLayout layout = DialogueTradeLayout.calculate(scrolled, rows.size(), expandedIndex,
				open == null ? 0 : open.stacks().size(), note);

		renderRapport(gfx, font, board, layout.rapport());
		float time = AnimationTime.gameTime(mc.level);
		for (int i = 0; i < rows.size(); i++) {
			renderCard(gfx, font, rows.get(i), layout.cards().get(i), crest, content, time, mouseX, mouseY, targets);
		}
		return layout.contentHeight();
	}

	/** The NPC's reaction to the last finished trade, for the header; null when there is none. */
	Component reaction() {
		return reaction;
	}

	ItemStack hoveredStack() {
		return hoveredStack;
	}

	List<Component> hoveredText() {
		return hoveredText;
	}

	/** Leaves the box: forgets the open card, the note and the reaction. A pending request still times out. */
	void reset() {
		expanded = null;
		picked = List.of();
		pickedByPlayer = false;
		noteOffer = null;
		noteKey = null;
		reaction = null;
	}

	/** Opens {@code offerId}'s strip, as a click on its card does. */
	void expand(ResourceLocation offerId) {
		expanded = offerId;
		picked = List.of();
		pickedByPlayer = false;
		noteOffer = null;
		noteKey = null;
	}

	/** A trade reply for this speaker: clears the pending card, then thanks or notes the refusal under it. */
	void onResult(DialogueScreen.TradeReply reply, NpcTradeBoard board) {
		if (reply.offerId().equals(pending)) pending = null;
		if (reply.outcome() == NpcTradeService.Outcome.REFUSED) {
			noteOffer = reply.offerId();
			noteKey = voicedReason(board.speaker(), reply.reasonKey());
			expanded = reply.offerId();
			return;
		}
		boolean fine = reply.outcome() == NpcTradeService.Outcome.DONE_FINE;
		reaction = Component.translatable(thanksKey(board.speaker(), fine, reply.reasonKey()));
		expanded = null;
		picked = List.of();
		pickedByPlayer = false;
		noteOffer = null;
		noteKey = null;
		Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.ITEM_PICKUP,
				fine ? 1.2F : 1.0F, 0.6F));
	}

	/** The speaker's own thanks ({@code hemomancy.trade.<speaker>.thanks[_fine]}) if written, else the plain result. */
	private static String thanksKey(String speaker, boolean fine, String fallback) {
		String base = "hemomancy.trade." + speaker + ".thanks";
		if (fine && I18n.exists(base + "_fine")) return base + "_fine";
		return I18n.exists(base) ? base : fallback;
	}

	/**
	 * The speaker's own words for an appraisal or stained refusal ({@code hemomancy.trade.<speaker>.appraise.poor},
	 * {@code .stained}) if written, else the plain reason.
	 */
	private static String voicedReason(String speaker, String reason) {
		String voiced = switch (reason) {
			case NpcTradeService.REFUSED_POOR -> "hemomancy.trade." + speaker + ".appraise.poor";
			case NpcTradeService.REFUSED_STAINED -> "hemomancy.trade." + speaker + ".stained";
			default -> null;
		};
		return voiced != null && I18n.exists(voiced) ? voiced : reason;
	}

	private record Row(NpcTradeCard card, List<DialogueTradePicks.Stack> stacks, int[] counts, Carried carried) {}

	/** The cards with their matching stacks, those the player carries enough for first within each order band. */
	private List<Row> rows(NpcTradeBoard board, Inventory inventory) {
		List<Row> rows = new ArrayList<>(board.cards().size());
		for (NpcTradeCard card : board.cards()) {
			List<NpcTradeWant.Items> parts = card.want().itemParts();
			int[] counts = parts.stream().mapToInt(NpcTradeWant.Items::count).toArray();
			List<DialogueTradePicks.Stack> stacks = stacks(board.speaker(), parts, inventory);
			rows.add(new Row(card, stacks, counts, DialogueTradePicks.carried(stacks, counts)));
		}
		rows.sort(Comparator.comparingInt((Row row) -> row.card().order())
				.thenComparingInt(row -> row.carried() == Carried.ENOUGH ? 0 : 1));
		return rows;
	}

	/** Every main-inventory stack that pays a part, graded the way the server will; a stained stack is poor here. */
	private static List<DialogueTradePicks.Stack> stacks(String speaker, List<NpcTradeWant.Items> parts, Inventory inventory) {
		List<DialogueTradePicks.Stack> stacks = new ArrayList<>();
		if (parts.isEmpty()) return stacks;
		for (int slot = 0; slot < Math.min(MAIN_SLOTS, inventory.items.size()); slot++) {
			ItemStack stack = inventory.items.get(slot);
			if (stack.isEmpty()) continue;
			Grade[] byPart = new Grade[parts.size()];
			boolean any = false;
			for (int p = 0; p < parts.size(); p++) {
				NpcTradeWant.Items part = parts.get(p);
				if (part.match().matches(stack)) {
					byPart[p] = NpcTradeSides.refusesStained(speaker, stack) ? Grade.POOR
							: NpcTradeAppraisals.grade(stack, part.appraisal(), true);
					any = true;
				}
			}
			if (any) stacks.add(new DialogueTradePicks.Stack(slot, stack.getCount(), byPart));
		}
		return stacks;
	}

	private void renderRapport(GuiGraphics gfx, Font font, NpcTradeBoard board, DialogueLayout.Rect bar) {
		Component label = Component.translatable("hemomancy.trade.ui.rapport",
				Component.translatable(NpcTradeRules.tierKey(board.tier())));
		gfx.drawString(font, label, bar.x(), bar.y() + 2, style.optionColor(), false);
		int barX = bar.x() + font.width(label) + 8;
		int barW = Math.max(8, bar.right() - barX);
		int barY = bar.y() + 5;
		gfx.fill(barX, barY, barX + barW, barY + 3, style.progressBackColor());
		gfx.fill(barX, barY, barX + Math.round(barW * DialogueTradeLayout.rapportFill(board.rapport())), barY + 3,
				style.progressColor());
	}

	private void renderCard(GuiGraphics gfx, Font font, Row row, DialogueTradeLayout.Card layout, ResourceLocation crest,
			DialogueLayout.Rect viewport, float time, int mouseX, int mouseY, Targets targets) {
		NpcTradeCard card = row.card();
		DialogueLayout.Rect body = layout.body();
		boolean waiting = card.offerId().equals(pending);
		boolean open = card.state() == NpcTradeCard.State.OPEN;
		boolean enabled = open && !waiting;
		boolean hovered = enabled && viewport.contains(mouseX, mouseY) && body.contains(mouseX, mouseY);
		DialogueLayout.Rect frame = layout.frame();
		gfx.blitSprite(!enabled ? style.cardDisabledSprite()
				: hovered || layout.expanded() ? style.cardSelectedSprite() : style.cardSprite(),
				frame.x(), frame.y(), frame.width(), frame.height());

		int x = body.x() + 8;
		int y = body.y() + 5;
		x = renderWant(gfx, font, card, crest, x, y, time, viewport, mouseX, mouseY);
		gfx.blitSprite(ARROW, x + 1, y + 2, 12, 12);
		x += 16;
		for (NpcTradeGive give : card.give()) {
			ItemStack stack = giveStack(give);
			drawStack(gfx, font, stack, give.count(), x, y, viewport, mouseX, mouseY);
			x += ICON_STEP;
		}
		if (!card.giveFine().isEmpty()) {
			gfx.blitSprite(DialogueThemeStyle.statusIcon("grade_fine"), x, y, 8, 8);
			if (viewport.contains(mouseX, mouseY) && contains(x, y, 8, 8, mouseX, mouseY)) {
				List<Component> lines = new ArrayList<>();
				lines.add(Component.translatable("hemomancy.trade.ui.fine_pays"));
				for (NpcTradeGive give : card.giveFine()) {
					lines.add(Component.translatable("hemomancy.trade.ui.give_count", giveStack(give).getHoverName(),
							give.count()));
				}
				hoveredText = lines;
			}
		}
		renderStatus(gfx, font, card, body, viewport, mouseX, mouseY);
		if (card.daily()) renderTodayRibbon(gfx, font, frame);

		int textColor = enabled || waiting ? style.textColor() : style.disabledColor();
		int titleWidth = body.width() - 16 - 12;
		List<FormattedCharSequence> title = font.split(Component.translatable(card.titleKey()), titleWidth);
		if (!title.isEmpty()) {
			gfx.drawString(font, title.getFirst(), body.x() + 8, body.y() + 24,
					enabled ? style.optionColor() : style.disabledColor(), true);
		}
		renderCarried(gfx, row.carried(), body.right() - 16, body.y() + 24, viewport, mouseX, mouseY);
		List<FormattedCharSequence> line = font.split(Component.translatable(card.lineKey()), body.width() - 16);
		for (int i = 0; i < Math.min(2, line.size()); i++) {
			gfx.drawString(font, line.get(i), body.x() + 8, body.y() + 34 + i * LINE_HEIGHT, textColor, false);
		}
		ResourceLocation offerId = card.offerId();
		targets.add(body.x(), body.y(), body.width(), body.height(), enabled, () -> {
			if (offerId.equals(expanded)) {
				reset();
			} else {
				expand(offerId);
			}
		});
		if (layout.expanded()) renderStrip(gfx, font, row, layout, enabled, viewport, mouseX, mouseY, targets);
	}

	/** The want side: cycling ingredient stacks with counts, a blood drop and its amount, or the house crest. */
	private int renderWant(GuiGraphics gfx, Font font, NpcTradeCard card, ResourceLocation crest, int x, int y,
			float time, DialogueLayout.Rect viewport, int mouseX, int mouseY) {
		switch (card.want()) {
			case NpcTradeWant.Blood blood -> {
				gfx.blitSprite(BLOOD_DROP, x, y, 16, 16);
				Component amount = Component.translatable("hemomancy.trade.ui.blood", blood.amount());
				gfx.drawString(font, amount, x + 18, y + 4, style.optionColor(), false);
				return x + 18 + font.width(amount) + 4;
			}
			case NpcTradeWant.Custom custom -> {
				gfx.blitSprite(crest, x, y, 16, 16);
				if (viewport.contains(mouseX, mouseY) && contains(x, y, 16, 16, mouseX, mouseY)) {
					hoveredText = List.of(Component.translatable("hemomancy.trade.ui.custom"));
				}
				return x + ICON_STEP + 2;
			}
			case NpcTradeWant.Act act -> {
				Component label = Component.translatable("hemomancy.trade.ui.act." + act.act());
				gfx.drawString(font, label, x, y + 4, style.optionColor(), false);
				if (viewport.contains(mouseX, mouseY) && contains(x, y, font.width(label), 16, mouseX, mouseY)) {
					hoveredText = List.of(Component.translatable("hemomancy.trade.ui.act." + act.act() + ".hint"));
				}
				return x + font.width(label) + 4;
			}
			default -> {
				for (NpcTradeWant.Items part : card.want().itemParts()) {
					ItemStack[] options = displayStacks.computeIfAbsent(part.match(),
							match -> match.toIngredient().getItems());
					if (options.length > 0) {
						ItemStack shown = options[Math.floorMod((int) (time / 20F), options.length)];
						drawStack(gfx, font, shown, part.count(), x, y, viewport, mouseX, mouseY);
					}
					x += ICON_STEP;
				}
				return x + 2;
			}
		}
	}

	/** Today's pick from a rotation pool: a small ribbon over the card's top-left corner. */
	private void renderTodayRibbon(GuiGraphics gfx, Font font, DialogueLayout.Rect frame) {
		Component label = Component.translatable("hemomancy.trade.ui.today");
		int width = font.width(label) + 6;
		int x = frame.x() + 4;
		int y = frame.y() - 4;
		gfx.fill(x, y, x + width, y + 10, style.progressColor());
		gfx.fill(x, y + 10, x + 3, y + 12, style.progressBackColor());
		gfx.drawString(font, label, x + 3, y + 1, style.optionColor(), false);
	}

	/** Right edge: uses left, the restock wait, or the lock reason. */
	private void renderStatus(GuiGraphics gfx, Font font, NpcTradeCard card, DialogueLayout.Rect body,
			DialogueLayout.Rect viewport, int mouseX, int mouseY) {
		Component status = switch (card.state()) {
			case OPEN -> card.usesLeft() == NpcTradeCard.UNLIMITED ? null
					: Component.translatable("hemomancy.trade.ui.uses_left", card.usesLeft());
			case SOLD_OUT -> card.daysToRestock() == 1
					? Component.translatable("hemomancy.trade.ui.restocks_one")
					: Component.translatable("hemomancy.trade.ui.restocks_in", card.daysToRestock());
			case LOCKED -> Component.translatable(card.lockKey().orElse("hemomancy.trade.locked.advancement"));
		};
		if (status == null) return;
		int right = body.right() - 8;
		int width = font.width(status);
		int color = card.state() == NpcTradeCard.State.OPEN ? style.textColor() : style.disabledColor();
		gfx.drawString(font, status, right - width, body.y() + 9, color, false);
		if (card.state() == NpcTradeCard.State.LOCKED) {
			gfx.blitSprite(LOCKED, right - width - 11, body.y() + 8, 9, 9);
		}
	}

	private void renderCarried(GuiGraphics gfx, Carried carried, int x, int y, DialogueLayout.Rect viewport,
			int mouseX, int mouseY) {
		if (carried == Carried.NONE) return;
		boolean enough = carried == Carried.ENOUGH;
		gfx.blitSprite(DialogueThemeStyle.statusIcon(enough ? "carried_full" : "carried_some"), x, y, 8, 8);
		if (viewport.contains(mouseX, mouseY) && contains(x, y, 8, 8, mouseX, mouseY)) {
			hoveredText = List.of(Component.translatable(enough ? "hemomancy.trade.ui.carried.enough"
					: "hemomancy.trade.ui.carried.some"));
		}
	}

	/** The selection strip: a refusal note, the matching stacks in wells with their grades, and Offer. */
	private void renderStrip(GuiGraphics gfx, Font font, Row row, DialogueTradeLayout.Card layout, boolean enabled,
			DialogueLayout.Rect viewport, int mouseX, int mouseY, Targets targets) {
		NpcTradeCard card = row.card();
		if (layout.note() != null) {
			gfx.drawString(font, Component.translatable(noteKey), layout.note().x(), layout.note().y() + 2,
					style.dangerColor(), false);
		}
		List<DialogueTradePicks.Stack> stacks = row.stacks();
		Inventory inventory = Minecraft.getInstance().player.getInventory();
		for (int i = 0; i < stacks.size(); i++) {
			DialogueTradePicks.Stack stack = stacks.get(i);
			DialogueLayout.Rect well = layout.wells().get(i);
			ScreenDrawUtils.drawSlotWell(gfx, well.x(), well.y(), style.wellDarkColor(), style.wellBackgroundColor(),
					style.wellLightColor());
			ItemStack item = inventory.items.get(stack.slot());
			gfx.renderItem(item, well.x(), well.y());
			gfx.renderItemDecorations(font, item, well.x(), well.y());
			Grade grade = stack.shownGrade();
			gfx.blitSprite(DialogueThemeStyle.statusIcon("grade_" + grade.name().toLowerCase(java.util.Locale.ROOT)),
					well.right() - 7, well.y() - 1, 8, 8);
			if (picked.contains(stack.slot())) {
				ScreenDrawUtils.drawSimpleBorder(gfx, well.x() - 2, well.y() - 2, 20, 20, style.progressColor());
			}
			if (viewport.contains(mouseX, mouseY) && well.contains(mouseX, mouseY)) {
				List<Component> lines = new ArrayList<>(Screen.getTooltipFromItem(Minecraft.getInstance(), item));
				lines.add(Component.translatable("hemomancy.trade.ui.grade", Component.translatable(grade.key())));
				hoveredText = lines;
			}
			int slot = stack.slot();
			targets.add(well.x() - 1, well.y() - 1, 18, 18, enabled, () -> {
				picked = DialogueTradePicks.toggle(picked, slot);
				pickedByPlayer = true;
			});
		}
		if (stacks.isEmpty()) {
			String hint = switch (card.want()) {
				case NpcTradeWant.Blood blood -> "hemomancy.trade.ui.blood_confirm";
				case NpcTradeWant.Custom custom -> "hemomancy.trade.ui.custom_confirm";
				case NpcTradeWant.Act act -> "hemomancy.trade.ui.act_confirm";
				default -> "hemomancy.trade.refused.missing";
			};
			List<FormattedCharSequence> lines = font.split(Component.translatable(hint),
					layout.offer().x() - layout.body().x() - 16);
			if (!lines.isEmpty()) {
				gfx.drawString(font, lines.getFirst(), layout.body().x() + 8, layout.offer().y() + 5, style.textColor(),
						false);
			}
		}

		boolean waiting = card.offerId().equals(pending);
		boolean ready = enabled && switch (card.want()) {
			case NpcTradeWant.Blood blood -> true;
			case NpcTradeWant.Custom custom -> true;
			case NpcTradeWant.Act act -> true;
			default -> DialogueTradePicks.predict(stacks, row.counts(), picked)
					.filter(grade -> grade != Grade.POOR).isPresent();
		};
		DialogueLayout.Rect offer = layout.offer();
		boolean hovered = ready && viewport.contains(mouseX, mouseY) && offer.contains(mouseX, mouseY);
		gfx.blitSprite(!ready ? style.buttonDisabledSprite() : hovered ? style.buttonSelectedSprite()
				: style.buttonSprite(), offer.x(), offer.y(), offer.width(), offer.height());
		gfx.drawCenteredString(font, Component.translatable(waiting ? "hemomancy.trade.ui.offering"
				: "hemomancy.trade.ui.offer"), offer.x() + offer.width() / 2, offer.y() + 5,
				ready ? style.optionColor() : style.disabledColor());
		targets.add(offer.x(), offer.y(), offer.width(), offer.height(), ready, () -> send(card));
	}

	private void send(NpcTradeCard card) {
		int[] slots = switch (card.want()) {
			case NpcTradeWant.Blood blood -> new int[0];
			case NpcTradeWant.Act act -> new int[0];
			// The server takes the first chosen slot that holds the house's custom, so offer it the whole pack.
			case NpcTradeWant.Custom custom -> IntStream.range(0, MAIN_SLOTS).toArray();
			default -> picked.stream().mapToInt(Integer::intValue).limit(NpcTradeRules.MAX_SLOTS).toArray();
		};
		PacketHandler.sendToServer(new NpcTradeRequestPacket(entityId, card.offerId(), slots));
		pending = card.offerId();
		pendingSince = Minecraft.getInstance().level == null ? 0 : Minecraft.getInstance().level.getGameTime();
		noteOffer = null;
		noteKey = null;
	}

	private void drawStack(GuiGraphics gfx, Font font, ItemStack stack, int count, int x, int y,
			DialogueLayout.Rect viewport, int mouseX, int mouseY) {
		if (stack.isEmpty()) return;
		gfx.renderItem(stack, x, y);
		gfx.renderItemDecorations(font, stack, x, y, count > 1 ? String.valueOf(count) : null);
		if (viewport.contains(mouseX, mouseY) && contains(x, y, 16, 16, mouseX, mouseY)) hoveredStack = stack;
	}

	private ItemStack giveStack(NpcTradeGive give) {
		return giveStacks.computeIfAbsent(give, this::decode);
	}

	private ItemStack decode(NpcTradeGive give) {
		var level = Minecraft.getInstance().level;
		if (level == null) return ItemStack.EMPTY;
		try {
			return give.toStack(level.registryAccess());
		} catch (RuntimeException e) {
			Hemomancy.LOGGER.warn("Trade give {} could not be shown: {}", give.item(), e.getMessage());
			return ItemStack.EMPTY;
		}
	}

	private static void drawCentered(GuiGraphics gfx, Font font, Component text, DialogueLayout.Rect area, int color) {
		List<FormattedCharSequence> lines = font.split(text, Math.max(1, area.width() - 32));
		int y = area.y() + (area.height() - lines.size() * (LINE_HEIGHT + 3)) / 2;
		for (FormattedCharSequence line : lines) {
			gfx.drawString(font, line, area.x() + (area.width() - font.width(line)) / 2, y, color, false);
			y += LINE_HEIGHT + 3;
		}
	}

	private static boolean contains(int x, int y, int width, int height, double mouseX, double mouseY) {
		return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
	}
}
