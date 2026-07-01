package com.vincenthuto.hemomancy.client.screen.item;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vincenthuto.hemomancy.client.screen.skilltree.harbinger.VeinBackgroundRenderer;
import com.vincenthuto.hemomancy.client.screen.skilltree.shared.MilestoneDrawerState;
import com.vincenthuto.hemomancy.client.screen.skilltree.shared.MilestoneDrawerView;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.ProgressScreenContext;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.ScreenDrawUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public class HarbingerAssignmentLedgerScreen extends Screen {
	private static final int SCREEN_MARGIN = 16;
	private static final int CONTENT_PAD = 18;
	private static final int DRAWER_RESERVED_WIDTH = 176;
	private static final int COLLAPSED_DRAWER_RESERVED_WIDTH = 34;
	private static final int SECTION_GAP = 12;
	private static final int CARD_GAP = 5;
	private static final int ASSIGNMENT_CARD_HEIGHT = 30;
	private static final int FIRST_BLOODCRAFT_HEIGHT = 145;
	private static final int HERMIT_ROAD_HEIGHT = 110;
	private static final int FIRST_SEPARATION_HEIGHT = 180;
	private static final int RED_TAXONOMY_HEIGHT = 56;
	private static final int ENZYME_MASTERY_HEIGHT = 56;
	private static final int LIVING_BESTIARY_HEIGHT = 68;
	private static final int WOVEN_VESSEL_HEIGHT = 68;
	private static final int VEIN_MASON_HEIGHT = 68;
	private static final int SCROLLBAR_WIDTH = 6;
	private static final int SCROLL_STEP = 18;
	private static final int PANEL = 0xD0140505;
	private static final int PANEL_DARK = 0xE0160808;
	private static final int PANEL_ROW = 0xD6281815;
	private static final int PANEL_DONE = 0xD6262217;
	private static final int BORDER = 0xFF552020;
	private static final int BORDER_MUTED = 0xFF3B2420;
	private static final int TITLE = 0xFFE5B19D;
	private static final int TEXT = 0xFFD7C4B6;
	private static final int MUTED = 0xFF9A8A80;
	private static final int DONE = 0xFFB7C982;
	private static final int CURRENT = 0xFFE0A75B;
	private static final int HEADER = 0xFFCC3333;
	private static final int TOOLTIP_CURSOR_OFFSET = 12;
	private static final int TOOLTIP_SCREEN_PAD = 8;
	private static final int TOOLTIP_TEXT_MAX_WIDTH = 240;
	private static final int TOOLTIP_PAD = 4;

	private final int degree;
	private final boolean firstAwakening;
	private final boolean degreeOne;
	private final boolean vesselFilled;
	private final boolean liberSanguinumCrafted;
	private final boolean hematicIronBlockCrafted;
	private final boolean firstRemnant;
	private final boolean ledgerGranted;
	private final boolean hasVialCentrifuge;
	private final boolean hasSampledBloodVial;
	private final boolean firstSeparationStarted;
	private final boolean hasAnyEnzyme;
	private final int redTaxonomyCount;
	private final boolean redTaxonomyComplete;
	private final int enzymeMasteryCount;
	private final boolean enzymeMasteryComplete;
	private final int livingBestiaryCount;
	private final int livingBestiaryTotal;
	private final int morphlingLayerCount;
	private final boolean hasBlankHematicMemory;
	private final boolean mnemonistWovenVesselComplete;
	private final boolean mnemonistFirstWeaveComplete;
	private final boolean vicarMasonsRespiteDirective;
	private final boolean veinMasonFirstLesson;
	private final boolean veinMasonFirstScarCarved;
	private final boolean veinMasonFirstScarLearned;
	private final boolean veinMasonFirstEffigyPattern;
	private final boolean veinMasonFirstEffigyLoadout;
	private final MilestoneDrawerState milestoneState = new MilestoneDrawerState();
	private final VeinBackgroundRenderer veinBg = new VeinBackgroundRenderer();
	private int left;
	private int top;
	private int panelWidth;
	private int panelHeight;
	private int assignmentScrollOffset;
	private int assignmentMaxScroll;
	private Component hoveredAssignmentDescription;

	private HarbingerAssignmentLedgerScreen(int degree, boolean firstAwakening, boolean degreeOne,
			boolean vesselFilled, boolean liberSanguinumCrafted, boolean hematicIronBlockCrafted,
			boolean firstRemnant, boolean ledgerGranted,
			boolean hasVialCentrifuge, boolean hasSampledBloodVial,
			boolean firstSeparationStarted, boolean hasAnyEnzyme,
			int redTaxonomyCount, boolean redTaxonomyComplete,
			int enzymeMasteryCount, boolean enzymeMasteryComplete,
			int livingBestiaryCount, int livingBestiaryTotal, int morphlingLayerCount,
			boolean hasBlankHematicMemory, boolean mnemonistWovenVesselComplete,
			boolean mnemonistFirstWeaveComplete,
			boolean vicarMasonsRespiteDirective, boolean veinMasonFirstLesson,
			boolean veinMasonFirstScarCarved, boolean veinMasonFirstScarLearned,
			boolean veinMasonFirstEffigyPattern, boolean veinMasonFirstEffigyLoadout) {
		super(Component.translatable("screen.hemomancy.harbinger_assignment_ledger.title"));
		this.degree = degree;
		this.firstAwakening = firstAwakening;
		this.degreeOne = degreeOne;
		this.vesselFilled = vesselFilled;
		this.liberSanguinumCrafted = liberSanguinumCrafted;
		this.hematicIronBlockCrafted = hematicIronBlockCrafted;
		this.firstRemnant = firstRemnant;
		this.ledgerGranted = ledgerGranted;
		this.hasVialCentrifuge = hasVialCentrifuge;
		this.hasSampledBloodVial = hasSampledBloodVial;
		this.firstSeparationStarted = firstSeparationStarted;
		this.hasAnyEnzyme = hasAnyEnzyme;
		this.redTaxonomyCount = redTaxonomyCount;
		this.redTaxonomyComplete = redTaxonomyComplete;
		this.enzymeMasteryCount = enzymeMasteryCount;
		this.enzymeMasteryComplete = enzymeMasteryComplete;
		this.livingBestiaryCount = livingBestiaryCount;
		this.livingBestiaryTotal = livingBestiaryTotal;
		this.morphlingLayerCount = morphlingLayerCount;
		this.hasBlankHematicMemory = hasBlankHematicMemory;
		this.mnemonistWovenVesselComplete = mnemonistWovenVesselComplete;
		this.mnemonistFirstWeaveComplete = mnemonistFirstWeaveComplete;
		this.vicarMasonsRespiteDirective = vicarMasonsRespiteDirective;
		this.veinMasonFirstLesson = veinMasonFirstLesson;
		this.veinMasonFirstScarCarved = veinMasonFirstScarCarved;
		this.veinMasonFirstScarLearned = veinMasonFirstScarLearned;
		this.veinMasonFirstEffigyPattern = veinMasonFirstEffigyPattern;
		this.veinMasonFirstEffigyLoadout = veinMasonFirstEffigyLoadout;
	}

	public static void open(int degree, boolean firstAwakening, boolean degreeOne,
			boolean vesselFilled, boolean liberSanguinumCrafted, boolean hematicIronBlockCrafted,
			boolean firstRemnant, boolean ledgerGranted,
			boolean hasVialCentrifuge, boolean hasSampledBloodVial,
			boolean firstSeparationStarted, boolean hasAnyEnzyme,
			int redTaxonomyCount, boolean redTaxonomyComplete,
			int enzymeMasteryCount, boolean enzymeMasteryComplete,
			int livingBestiaryCount, int livingBestiaryTotal, int morphlingLayerCount,
			boolean hasBlankHematicMemory, boolean mnemonistWovenVesselComplete,
			boolean mnemonistFirstWeaveComplete,
			boolean vicarMasonsRespiteDirective, boolean veinMasonFirstLesson,
			boolean veinMasonFirstScarCarved, boolean veinMasonFirstScarLearned,
			boolean veinMasonFirstEffigyPattern, boolean veinMasonFirstEffigyLoadout) {
		Minecraft.getInstance().setScreen(new HarbingerAssignmentLedgerScreen(
				degree, firstAwakening, degreeOne, vesselFilled, liberSanguinumCrafted, hematicIronBlockCrafted,
				firstRemnant, ledgerGranted, hasVialCentrifuge, hasSampledBloodVial,
				firstSeparationStarted, hasAnyEnzyme,
				redTaxonomyCount, redTaxonomyComplete, enzymeMasteryCount, enzymeMasteryComplete,
				livingBestiaryCount, livingBestiaryTotal, morphlingLayerCount, hasBlankHematicMemory,
				mnemonistWovenVesselComplete, mnemonistFirstWeaveComplete, vicarMasonsRespiteDirective,
				veinMasonFirstLesson, veinMasonFirstScarCarved, veinMasonFirstScarLearned,
				veinMasonFirstEffigyPattern, veinMasonFirstEffigyLoadout));
	}

	@Override
	protected void init() {
		this.left = SCREEN_MARGIN;
		this.top = SCREEN_MARGIN;
		this.panelWidth = this.width - SCREEN_MARGIN * 2;
		this.panelHeight = this.height - SCREEN_MARGIN * 2;
		this.milestoneState.open = true;
		this.milestoneState.scrollOffset = 0;
		this.assignmentScrollOffset = 0;
		this.assignmentMaxScroll = 0;
		this.clearWidgets();
	}

	@Override
	public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
	}

	@Override
	public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableBlend();

		veinBg.render(gfx, left, top, panelWidth, panelHeight);
		ScreenDrawUtils.drawBorder(gfx, left, top, panelWidth, panelHeight, 0xFF330808, 0xFF220606);
		gfx.drawCenteredString(font, this.title, left + panelWidth / 2, top + 9, TITLE);
		hoveredAssignmentDescription = null;

		int contentLeft = contentLeft();
		int contentRight = contentRight();
		int contentTop = contentTop();
		int contentBottom = contentBottom();
		int contentW = Math.max(180, contentRight - contentLeft);
		int contentH = Math.max(120, contentBottom - contentTop);

		gfx.fill(contentLeft - 8, contentTop - 6, contentRight + 8, contentBottom + 6, PANEL);
		ScreenDrawUtils.drawBorder(gfx, contentLeft - 8, contentTop - 6,
				contentW + 16, contentH + 12, BORDER, BORDER_MUTED);
		renderAssignments(gfx, contentLeft, contentTop, contentW, mouseX, mouseY);

		ProgressScreenContext ctx = makeMilestoneContext();
		MilestoneDrawerView.draw(gfx, ctx, milestoneState, mouseX, mouseY);

		super.render(gfx, mouseX, mouseY, partialTick);

		if (MilestoneDrawerView.isOverToggle(ctx, milestoneState, mouseX, mouseY)) {
			String tipText = milestoneState.open ? "Hide Milestones" : "Show Milestones";
			renderRightAnchoredTooltip(gfx, font.split(Component.literal(tipText), TOOLTIP_TEXT_MAX_WIDTH),
					mouseX, mouseY);
		}
		renderAssignmentDescriptionTooltip(gfx, mouseX, mouseY);
	}

	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		if (button == 0 && MilestoneDrawerView.isOverToggle(makeMilestoneContext(), milestoneState, mx, my)) {
			milestoneState.open = !milestoneState.open;
			milestoneState.scrollOffset = 0;
			return true;
		}
		return super.mouseClicked(mx, my, button);
	}

	@Override
	public boolean mouseScrolled(double mx, double my, double scrollX, double scrollY) {
		ProgressScreenContext ctx = makeMilestoneContext();
		if (milestoneState.open && MilestoneDrawerView.isOverDrawer(ctx, milestoneState, mx, my)) {
			milestoneState.scrollOffset = Math.max(0, milestoneState.scrollOffset - (int) (scrollY * 12));
			return true;
		}
		if (isOverAssignmentPanel(mx, my)) {
			assignmentScrollOffset = clampAssignmentScroll(assignmentScrollOffset - (int) (scrollY * SCROLL_STEP));
			return true;
		}
		return super.mouseScrolled(mx, my, scrollX, scrollY);
	}

	private void renderAssignments(GuiGraphics gfx, int x, int y, int w, int mouseX, int mouseY) {
		Component degreeText = Component.translatable("screen.hemomancy.harbinger_assignment_ledger.degree", degree);
		renderAssignmentHeader(gfx, x, y, w,
				Component.translatable("screen.hemomancy.harbinger_assignment_ledger.header"), degreeText);
		drawProgressBar(gfx, x, y + 25, w, 8, completedCount(), 13);

		int listY = y + 38;
		int listH = Math.max(38, contentBottom() - listY);
		int listW = w - SCROLLBAR_WIDTH - 4;
		int totalH = totalAssignmentContentHeight();
		assignmentMaxScroll = Math.max(0, totalH - listH);
		assignmentScrollOffset = clampAssignmentScroll(assignmentScrollOffset);

		gfx.enableScissor(x, listY, x + w - SCROLLBAR_WIDTH, listY + listH);
		renderAssignmentList(gfx, x, listY - assignmentScrollOffset, listW, mouseX, mouseY);
		gfx.disableScissor();
		drawAssignmentScrollbar(gfx, x + w - SCROLLBAR_WIDTH, listY, SCROLLBAR_WIDTH, listH, totalH);
	}

	private void renderAssignmentHeader(GuiGraphics gfx, int x, int y, int w, Component title, Component meta) {
		List<FormattedCharSequence> wrapped = font.split(title, w);
		if (!wrapped.isEmpty()) {
			gfx.drawString(font, wrapped.get(0), x, y, HEADER, false);
		}
		gfx.drawString(font, meta, x, y + 12, MUTED, false);
	}

	private int renderAssignmentList(GuiGraphics gfx, int x, int cardY, int w, int mouseX, int mouseY) {
		renderFirstBloodcraft(gfx, x, cardY, w, mouseX, mouseY);
		cardY += FIRST_BLOODCRAFT_HEIGHT + SECTION_GAP;
		renderHermitRoad(gfx, x, cardY, w, mouseX, mouseY);
		cardY += HERMIT_ROAD_HEIGHT + SECTION_GAP;
		renderFirstSeparation(gfx, x, cardY, w, mouseX, mouseY);
		cardY += FIRST_SEPARATION_HEIGHT + SECTION_GAP;
		renderRedTaxonomy(gfx, x, cardY, w, mouseX, mouseY);
		cardY += RED_TAXONOMY_HEIGHT + SECTION_GAP;
		renderLivingBestiary(gfx, x, cardY, w, mouseX, mouseY);
		cardY += LIVING_BESTIARY_HEIGHT + SECTION_GAP;
		renderEnzymeMastery(gfx, x, cardY, w, mouseX, mouseY);
		cardY += ENZYME_MASTERY_HEIGHT + SECTION_GAP;
		renderWovenVessel(gfx, x, cardY, w, mouseX, mouseY);
		cardY += WOVEN_VESSEL_HEIGHT + SECTION_GAP;
		renderVeinMason(gfx, x, cardY, w, mouseX, mouseY);
		return totalAssignmentContentHeight();
	}

	private void renderFirstBloodcraft(GuiGraphics gfx, int x, int y, int w, int mouseX, int mouseY) {
		gfx.fill(x, y, x + w, y + FIRST_BLOODCRAFT_HEIGHT, PANEL_DARK);
		ScreenDrawUtils.drawBorder(gfx, x, y, w, FIRST_BLOODCRAFT_HEIGHT, BORDER, BORDER_MUTED);
		renderGroupHeader(gfx, x + 8, y + 6, w - 16,
				"screen.hemomancy.harbinger_assignment_ledger.first_bloodcraft.title",
				"screen.hemomancy.harbinger_assignment_ledger.first_bloodcraft.progress",
				firstBloodcraftProgress(), 3, firstBloodcraftProgress() >= 3);
		drawProgressBar(gfx, x + 8, y + 31, w - 16, 7, firstBloodcraftProgress(), 3);
		int rowY = y + 42;
		renderAssignmentCard(gfx, x + 8, rowY, w - 16, ASSIGNMENT_CARD_HEIGHT, vesselFilled,
				"screen.hemomancy.harbinger_assignment_ledger.step.fill_vessel",
				"screen.hemomancy.harbinger_assignment_ledger.step.fill_vessel.desc", mouseX, mouseY);
		rowY += ASSIGNMENT_CARD_HEIGHT + CARD_GAP;
		renderAssignmentCard(gfx, x + 8, rowY, w - 16, ASSIGNMENT_CARD_HEIGHT, liberSanguinumCrafted,
				"screen.hemomancy.harbinger_assignment_ledger.step.craft_liber",
				"screen.hemomancy.harbinger_assignment_ledger.step.craft_liber.desc", mouseX, mouseY);
		rowY += ASSIGNMENT_CARD_HEIGHT + CARD_GAP;
		renderAssignmentCard(gfx, x + 8, rowY, w - 16, ASSIGNMENT_CARD_HEIGHT, hematicIronBlockCrafted,
				"screen.hemomancy.harbinger_assignment_ledger.step.craft_hematic_iron",
				"screen.hemomancy.harbinger_assignment_ledger.step.craft_hematic_iron.desc", mouseX, mouseY);
	}

	private void renderHermitRoad(GuiGraphics gfx, int x, int y, int w, int mouseX, int mouseY) {
		gfx.fill(x, y, x + w, y + HERMIT_ROAD_HEIGHT, PANEL_DARK);
		ScreenDrawUtils.drawBorder(gfx, x, y, w, HERMIT_ROAD_HEIGHT, BORDER, BORDER_MUTED);
		renderGroupHeader(gfx, x + 8, y + 6, w - 16,
				"screen.hemomancy.harbinger_assignment_ledger.hermit_road.side_title",
				"screen.hemomancy.harbinger_assignment_ledger.hermit_road.progress",
				hermitRoadProgress(), 2, hermitRoadProgress() >= 2);
		drawProgressBar(gfx, x + 8, y + 31, w - 16, 7, hermitRoadProgress(), 2);
		int rowY = y + 42;
		renderAssignmentCard(gfx, x + 8, rowY, w - 16, ASSIGNMENT_CARD_HEIGHT, firstRemnant,
				"screen.hemomancy.harbinger_assignment_ledger.step.remnant",
				"screen.hemomancy.harbinger_assignment_ledger.step.remnant.desc", mouseX, mouseY);
		rowY += ASSIGNMENT_CARD_HEIGHT + CARD_GAP;
		renderAssignmentCard(gfx, x + 8, rowY, w - 16, ASSIGNMENT_CARD_HEIGHT, ledgerGranted,
				"screen.hemomancy.harbinger_assignment_ledger.step.ledger",
				"screen.hemomancy.harbinger_assignment_ledger.step.ledger.desc", mouseX, mouseY);
	}

	private void renderFirstSeparation(GuiGraphics gfx, int x, int y, int w, int mouseX, int mouseY) {
		gfx.fill(x, y, x + w, y + FIRST_SEPARATION_HEIGHT, PANEL_DARK);
		ScreenDrawUtils.drawBorder(gfx, x, y, w, FIRST_SEPARATION_HEIGHT, BORDER, BORDER_MUTED);
		renderGroupHeader(gfx, x + 8, y + 6, w - 16,
				"screen.hemomancy.harbinger_assignment_ledger.first_separation.title",
				"screen.hemomancy.harbinger_assignment_ledger.first_separation.progress",
				firstSeparationProgress(), 4, firstSeparationProgress() >= 4);
		drawProgressBar(gfx, x + 8, y + 31, w - 16, 7, firstSeparationProgress(), 4);
		int rowY = y + 42;
		renderAssignmentCard(gfx, x + 8, rowY, w - 16, ASSIGNMENT_CARD_HEIGHT, hasVialCentrifuge,
				"screen.hemomancy.harbinger_assignment_ledger.step.obtain_centrifuge",
				"screen.hemomancy.harbinger_assignment_ledger.step.obtain_centrifuge.desc", mouseX, mouseY);
		rowY += ASSIGNMENT_CARD_HEIGHT + CARD_GAP;
		renderAssignmentCard(gfx, x + 8, rowY, w - 16, ASSIGNMENT_CARD_HEIGHT, hasSampledBloodVial,
				"screen.hemomancy.harbinger_assignment_ledger.step.sample_blood_vial",
				"screen.hemomancy.harbinger_assignment_ledger.step.sample_blood_vial.desc", mouseX, mouseY);
		rowY += ASSIGNMENT_CARD_HEIGHT + CARD_GAP;
		renderAssignmentCard(gfx, x + 8, rowY, w - 16, ASSIGNMENT_CARD_HEIGHT, firstSeparationStarted,
				"screen.hemomancy.harbinger_assignment_ledger.step.start_separation",
				"screen.hemomancy.harbinger_assignment_ledger.step.start_separation.desc", mouseX, mouseY);
		rowY += ASSIGNMENT_CARD_HEIGHT + CARD_GAP;
		renderAssignmentCard(gfx, x + 8, rowY, w - 16, ASSIGNMENT_CARD_HEIGHT, hasAnyEnzyme,
				"screen.hemomancy.harbinger_assignment_ledger.step.obtain_enzyme",
				"screen.hemomancy.harbinger_assignment_ledger.step.obtain_enzyme.desc", mouseX, mouseY);
	}

	private void renderAssignmentCard(GuiGraphics gfx, int x, int y, int w, int h, boolean done,
			String titleKey, String descriptionKey, int mouseX, int mouseY) {
		gfx.fill(x, y, x + w, y + h, done ? PANEL_DONE : PANEL_ROW);
		ScreenDrawUtils.drawBorder(gfx, x, y, w, h, done ? BORDER : BORDER_MUTED, 0xAA1E0D0B);
		gfx.drawString(font, Component.literal(done ? "[x]" : "[ ]"), x + 8, y + 6, done ? DONE : CURRENT, false);
		gfx.drawString(font, Component.translatable(titleKey), x + 31, y + 5, done ? DONE : TITLE, false);
		renderTruncatedDescription(gfx, Component.translatable(descriptionKey), x + 31, y + 17,
				w - 46, done ? TEXT : MUTED, mouseX, mouseY);
	}

	private void renderRedTaxonomy(GuiGraphics gfx, int x, int y, int w, int mouseX, int mouseY) {
		gfx.fill(x, y, x + w, y + RED_TAXONOMY_HEIGHT, PANEL_DARK);
		ScreenDrawUtils.drawBorder(gfx, x, y, w, RED_TAXONOMY_HEIGHT, BORDER, BORDER_MUTED);
		renderTaxonomyHeader(gfx, x + 8, y + 6, w - 16);
		drawProgressBar(gfx, x + 8, y + 31, w - 16, 7, Math.min(redTaxonomyCount, 4), 4);
		renderTruncatedDescription(gfx, Component.translatable(redTaxonomyComplete
						? "screen.hemomancy.harbinger_assignment_ledger.red_taxonomy.complete"
						: "screen.hemomancy.harbinger_assignment_ledger.red_taxonomy.desc"),
				x + 8, y + 42, w - 16, redTaxonomyComplete ? TEXT : MUTED, mouseX, mouseY);
	}

	private void renderTaxonomyHeader(GuiGraphics gfx, int x, int y, int w) {
		List<FormattedCharSequence> wrapped = font.split(
				Component.translatable("screen.hemomancy.harbinger_assignment_ledger.red_taxonomy.side_title"), w);
		if (!wrapped.isEmpty()) {
			gfx.drawString(font, wrapped.get(0), x, y, HEADER, false);
		}
		gfx.drawString(font,
				Component.translatable("screen.hemomancy.harbinger_assignment_ledger.red_taxonomy.progress",
						redTaxonomyCount, 4),
				x, y + 12, redTaxonomyComplete ? DONE : CURRENT, false);
	}

	private void renderEnzymeMastery(GuiGraphics gfx, int x, int y, int w, int mouseX, int mouseY) {
		gfx.fill(x, y, x + w, y + ENZYME_MASTERY_HEIGHT, PANEL_DARK);
		ScreenDrawUtils.drawBorder(gfx, x, y, w, ENZYME_MASTERY_HEIGHT, BORDER, BORDER_MUTED);
		renderEnzymeMasteryHeader(gfx, x + 8, y + 6, w - 16);
		drawProgressBar(gfx, x + 8, y + 31, w - 16, 7, Math.min(enzymeMasteryCount, 8), 8);
		renderTruncatedDescription(gfx, Component.translatable(enzymeMasteryComplete
						? "screen.hemomancy.harbinger_assignment_ledger.enzyme_mastery.complete"
						: "screen.hemomancy.harbinger_assignment_ledger.enzyme_mastery.desc"),
				x + 8, y + 42, w - 16, enzymeMasteryComplete ? TEXT : MUTED, mouseX, mouseY);
	}

	private void renderLivingBestiary(GuiGraphics gfx, int x, int y, int w, int mouseX, int mouseY) {
		int total = Math.max(1, livingBestiaryTotal);
		gfx.fill(x, y, x + w, y + LIVING_BESTIARY_HEIGHT, PANEL_DARK);
		ScreenDrawUtils.drawBorder(gfx, x, y, w, LIVING_BESTIARY_HEIGHT, BORDER, BORDER_MUTED);
		renderLivingBestiaryHeader(gfx, x + 8, y + 6, w - 16, total);
		drawProgressBar(gfx, x + 8, y + 31, w - 16, 7, Math.min(livingBestiaryCount, total), total);
		renderTruncatedDescription(gfx,
				Component.translatable("screen.hemomancy.harbinger_assignment_ledger.living_bestiary.desc",
						morphlingLayerCount),
				x + 8, y + 42, w - 16, MUTED, mouseX, mouseY);
	}

	private void renderLivingBestiaryHeader(GuiGraphics gfx, int x, int y, int w, int total) {
		List<FormattedCharSequence> wrapped = font.split(
				Component.translatable("screen.hemomancy.harbinger_assignment_ledger.living_bestiary.side_title"), w);
		if (!wrapped.isEmpty()) {
			gfx.drawString(font, wrapped.get(0), x, y, HEADER, false);
		}
		gfx.drawString(font,
				Component.translatable("screen.hemomancy.harbinger_assignment_ledger.living_bestiary.progress",
						livingBestiaryCount, total),
				x, y + 12, livingBestiaryCount >= total ? DONE : CURRENT, false);
	}

	private void renderEnzymeMasteryHeader(GuiGraphics gfx, int x, int y, int w) {
		List<FormattedCharSequence> wrapped = font.split(
				Component.translatable("screen.hemomancy.harbinger_assignment_ledger.enzyme_mastery.side_title"), w);
		if (!wrapped.isEmpty()) {
			gfx.drawString(font, wrapped.get(0), x, y, HEADER, false);
		}
		gfx.drawString(font,
				Component.translatable("screen.hemomancy.harbinger_assignment_ledger.enzyme_mastery.progress",
						enzymeMasteryCount, 8),
				x, y + 12, enzymeMasteryComplete ? DONE : CURRENT, false);
	}

	private void renderWovenVessel(GuiGraphics gfx, int x, int y, int w, int mouseX, int mouseY) {
		gfx.fill(x, y, x + w, y + WOVEN_VESSEL_HEIGHT, PANEL_DARK);
		ScreenDrawUtils.drawBorder(gfx, x, y, w, WOVEN_VESSEL_HEIGHT, BORDER, BORDER_MUTED);
		renderWovenVesselHeader(gfx, x + 8, y + 6, w - 16);
		drawProgressBar(gfx, x + 8, y + 31, w - 16, 7, wovenVesselProgress(), 3);
		renderTruncatedDescription(gfx, Component.translatable(mnemonistFirstWeaveComplete
						? "screen.hemomancy.harbinger_assignment_ledger.woven_vessel.complete"
						: "screen.hemomancy.harbinger_assignment_ledger.woven_vessel.desc"),
				x + 8, y + 42, w - 16, mnemonistFirstWeaveComplete ? TEXT : MUTED, mouseX, mouseY);
	}

	private void renderWovenVesselHeader(GuiGraphics gfx, int x, int y, int w) {
		List<FormattedCharSequence> wrapped = font.split(
				Component.translatable("screen.hemomancy.harbinger_assignment_ledger.woven_vessel.title"), w);
		if (!wrapped.isEmpty()) {
			gfx.drawString(font, wrapped.get(0), x, y, HEADER, false);
		}
		gfx.drawString(font,
				Component.translatable("screen.hemomancy.harbinger_assignment_ledger.woven_vessel.progress",
						wovenVesselProgress(), 3),
				x, y + 12, mnemonistFirstWeaveComplete ? DONE : CURRENT, false);
	}

	private int wovenVesselProgress() {
		int completed = 0;
		if (hasBlankHematicMemory) completed++;
		if (mnemonistWovenVesselComplete) completed++;
		if (mnemonistFirstWeaveComplete) completed++;
		return completed;
	}

	private void renderGroupHeader(GuiGraphics gfx, int x, int y, int w, String titleKey, String progressKey,
			int progress, int total, boolean done) {
		List<FormattedCharSequence> wrapped = font.split(Component.translatable(titleKey), w);
		if (!wrapped.isEmpty()) {
			gfx.drawString(font, wrapped.get(0), x, y, HEADER, false);
		}
		gfx.drawString(font, Component.translatable(progressKey, progress, total),
				x, y + 12, done ? DONE : CURRENT, false);
	}

	private void renderVeinMason(GuiGraphics gfx, int x, int y, int w, int mouseX, int mouseY) {
		gfx.fill(x, y, x + w, y + VEIN_MASON_HEIGHT, PANEL_DARK);
		ScreenDrawUtils.drawBorder(gfx, x, y, w, VEIN_MASON_HEIGHT, BORDER, BORDER_MUTED);
		renderVeinMasonHeader(gfx, x + 8, y + 6, w - 16);
		drawProgressBar(gfx, x + 8, y + 31, w - 16, 7, veinMasonProgress(), 4);
		renderTruncatedDescription(gfx, Component.translatable(veinMasonDescriptionKey()),
				x + 8, y + 42, w - 16, veinMasonFirstEffigyLoadout ? TEXT : MUTED, mouseX, mouseY);
	}

	private void renderVeinMasonHeader(GuiGraphics gfx, int x, int y, int w) {
		List<FormattedCharSequence> wrapped = font.split(
				Component.translatable("screen.hemomancy.harbinger_assignment_ledger.vein_mason.title"), w);
		if (!wrapped.isEmpty()) {
			gfx.drawString(font, wrapped.get(0), x, y, HEADER, false);
		}
		gfx.drawString(font,
				Component.translatable("screen.hemomancy.harbinger_assignment_ledger.vein_mason.progress",
						veinMasonProgress(), 4),
				x, y + 12, veinMasonFirstEffigyLoadout ? DONE : CURRENT, false);
	}

	private String veinMasonDescriptionKey() {
		if (veinMasonFirstEffigyLoadout) {
			return "screen.hemomancy.harbinger_assignment_ledger.vein_mason.reward";
		}
		if (!vicarMasonsRespiteDirective) {
			return "screen.hemomancy.harbinger_assignment_ledger.vein_mason.desc";
		}
		if (!veinMasonFirstLesson) {
			return "screen.hemomancy.harbinger_assignment_ledger.vein_mason.find";
		}
		if (!veinMasonFirstScarLearned) {
			return veinMasonFirstScarCarved
					? "screen.hemomancy.harbinger_assignment_ledger.vein_mason.burn_scar"
					: "screen.hemomancy.harbinger_assignment_ledger.vein_mason.carve_scar";
		}
		if (!veinMasonFirstEffigyPattern) {
			return "screen.hemomancy.harbinger_assignment_ledger.vein_mason.prepare_pattern";
		}
		return "screen.hemomancy.harbinger_assignment_ledger.vein_mason.commit_loadout";
	}

	private int veinMasonProgress() {
		int completed = 0;
		if (veinMasonFirstLesson) completed++;
		if (veinMasonFirstScarLearned) completed++;
		if (veinMasonFirstEffigyPattern) completed++;
		if (veinMasonFirstEffigyLoadout) completed++;
		return completed;
	}

	private void drawAssignmentScrollbar(GuiGraphics gfx, int x, int y, int w, int h, int totalH) {
		gfx.fill(x, y, x + w, y + h, 0xAA100504);
		ScreenDrawUtils.drawSimpleBorder(gfx, x, y, w, h, BORDER_MUTED);
		if (assignmentMaxScroll <= 0 || totalH <= 0) {
			gfx.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0x663B2420);
			return;
		}
		int thumbH = Math.max(14, h * h / totalH);
		int travel = Math.max(1, h - thumbH - 2);
		int thumbY = y + 1 + (int) (travel * (assignmentScrollOffset / (float) assignmentMaxScroll));
		gfx.fill(x + 1, thumbY, x + w - 1, thumbY + thumbH, 0xFF8D2323);
	}

	private void renderTruncatedDescription(GuiGraphics gfx, Component description, int x, int y, int maxW,
			int color, int mouseX, int mouseY) {
		String text = description.getString();
		String visible = truncateWithEllipsis(text, maxW);
		gfx.drawString(font, visible, x, y, color, false);
		if (!visible.equals(text) && isMouseOverLine(mouseX, mouseY, x, y, maxW)) {
			hoveredAssignmentDescription = description;
		}
	}

	private String truncateWithEllipsis(String text, int maxW) {
		if (font.width(text) <= maxW) {
			return text;
		}
		String ellipsis = "...";
		int textW = Math.max(0, maxW - font.width(ellipsis));
		return font.plainSubstrByWidth(text, textW) + ellipsis;
	}

	private boolean isMouseOverLine(int mouseX, int mouseY, int x, int y, int w) {
		return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + font.lineHeight;
	}

	private void renderAssignmentDescriptionTooltip(GuiGraphics gfx, int mouseX, int mouseY) {
		if (hoveredAssignmentDescription != null) {
			int tooltipX = mouseX + TOOLTIP_CURSOR_OFFSET;
			int availableWidth = Math.max(40,
					this.width - tooltipX - TOOLTIP_SCREEN_PAD - TOOLTIP_PAD * 2);
			int wrapWidth = Math.min(TOOLTIP_TEXT_MAX_WIDTH, availableWidth);
			renderRightAnchoredTooltip(gfx, font.split(hoveredAssignmentDescription, wrapWidth), mouseX, mouseY);
		}
	}

	private void renderRightAnchoredTooltip(GuiGraphics gfx, List<FormattedCharSequence> lines, int mouseX, int mouseY) {
		if (lines.isEmpty()) {
			return;
		}
		int tooltipX = mouseX + TOOLTIP_CURSOR_OFFSET;
		int maxWidth = Math.max(40, this.width - tooltipX - TOOLTIP_SCREEN_PAD - TOOLTIP_PAD * 2);
		int textW = 0;
		for (FormattedCharSequence line : lines) {
			textW = Math.max(textW, Math.min(font.width(line), maxWidth));
		}
		int tooltipW = textW + TOOLTIP_PAD * 2;
		int tooltipH = lines.size() * font.lineHeight + TOOLTIP_PAD * 2;
		int tooltipY = Math.max(TOOLTIP_SCREEN_PAD,
				Math.min(mouseY - 4, this.height - tooltipH - TOOLTIP_SCREEN_PAD));

		var pose = gfx.pose();
		pose.pushPose();
		pose.translate(0.0F, 0.0F, 900.0F);
		gfx.fill(tooltipX - 1, tooltipY - 1, tooltipX + tooltipW + 1, tooltipY + tooltipH + 1, 0xEE12021E);
		ScreenDrawUtils.drawBorder(gfx, tooltipX - 1, tooltipY - 1, tooltipW + 2, tooltipH + 2,
				0xFF3B005F, 0xFF19002E);
		for (int i = 0; i < lines.size(); i++) {
			gfx.drawString(font, lines.get(i), tooltipX + TOOLTIP_PAD,
					tooltipY + TOOLTIP_PAD + i * font.lineHeight, 0xFFFFFFFF, false);
		}
		pose.popPose();
	}

	private void drawProgressBar(GuiGraphics gfx, int x, int y, int w, int h, int completed, int total) {
		gfx.fill(x, y, x + w, y + h, 0xFF2A1715);
		ScreenDrawUtils.drawSimpleBorder(gfx, x, y, w, h, BORDER_MUTED);
		int fillW = total <= 0 ? 0 : (int) ((w - 2) * (completed / (float) total));
		gfx.fill(x + 1, y + 1, x + 1 + fillW, y + h - 1, 0xFF8D2323);
	}

	private int completedCount() {
		int completed = 0;
		if (vesselFilled) completed++;
		if (liberSanguinumCrafted) completed++;
		if (hematicIronBlockCrafted) completed++;
		if (firstRemnant) completed++;
		if (ledgerGranted) completed++;
		if (hasVialCentrifuge) completed++;
		if (hasSampledBloodVial) completed++;
		if (firstSeparationStarted) completed++;
		if (hasAnyEnzyme) completed++;
		if (redTaxonomyComplete) {
			completed++;
		}
		if (enzymeMasteryComplete) {
			completed++;
		}
		if (mnemonistFirstWeaveComplete) {
			completed++;
		}
		if (veinMasonFirstEffigyLoadout) {
			completed++;
		}
		return completed;
	}

	private int totalAssignmentContentHeight() {
		return FIRST_BLOODCRAFT_HEIGHT + SECTION_GAP + HERMIT_ROAD_HEIGHT
				+ SECTION_GAP + FIRST_SEPARATION_HEIGHT
				+ SECTION_GAP + RED_TAXONOMY_HEIGHT + SECTION_GAP + LIVING_BESTIARY_HEIGHT
				+ SECTION_GAP + ENZYME_MASTERY_HEIGHT
				+ SECTION_GAP + WOVEN_VESSEL_HEIGHT
				+ SECTION_GAP + VEIN_MASON_HEIGHT;
	}

	private int firstBloodcraftProgress() {
		int completed = 0;
		if (vesselFilled) completed++;
		if (liberSanguinumCrafted) completed++;
		if (hematicIronBlockCrafted) completed++;
		return completed;
	}

	private int hermitRoadProgress() {
		int completed = 0;
		if (firstRemnant) completed++;
		if (ledgerGranted) completed++;
		return completed;
	}

	private int firstSeparationProgress() {
		int completed = 0;
		if (hasVialCentrifuge) completed++;
		if (hasSampledBloodVial) completed++;
		if (firstSeparationStarted) completed++;
		if (hasAnyEnzyme) completed++;
		return completed;
	}

	private int clampAssignmentScroll(int scroll) {
		return Math.max(0, Math.min(scroll, assignmentMaxScroll));
	}

	private boolean isOverAssignmentPanel(double mx, double my) {
		return mx >= contentLeft() && mx <= contentRight()
				&& my >= contentTop() + 38 && my <= contentBottom();
	}

	private int contentLeft() {
		return milestoneState.open ? left + DRAWER_RESERVED_WIDTH : left + COLLAPSED_DRAWER_RESERVED_WIDTH + CONTENT_PAD;
	}

	private int contentRight() {
		return left + panelWidth - CONTENT_PAD;
	}

	private int contentTop() {
		return top + 30;
	}

	private int contentBottom() {
		return top + panelHeight - CONTENT_PAD;
	}

	private ProgressScreenContext makeMilestoneContext() {
		return new ProgressScreenContext(font, left, top, panelWidth, panelHeight, degree);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
