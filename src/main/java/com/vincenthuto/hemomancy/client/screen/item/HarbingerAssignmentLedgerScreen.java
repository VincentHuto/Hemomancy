package com.vincenthuto.hemomancy.client.screen.item;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.screen.skilltree.harbinger.VeinBackgroundRenderer;
import com.vincenthuto.hemomancy.client.screen.skilltree.shared.MilestoneDrawerState;
import com.vincenthuto.hemomancy.client.screen.skilltree.shared.MilestoneDrawerView;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.ProgressScreenContext;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.ScreenDrawUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

public class HarbingerAssignmentLedgerScreen extends Screen {
	private static final int SCREEN_MARGIN = 16;
	private static final int CONTENT_PAD = 18;
	private static final int DRAWER_RESERVED_WIDTH = 176;
	private static final int COLLAPSED_DRAWER_RESERVED_WIDTH = 34;
	private static final int SECTION_GAP = 12;
	private static final int CARD_GAP = 5;
	private static final int PORTRAIT_GAP = 5;
	private static final int PORTRAIT_TEXTURE_SIZE = 48;
	private static final int ASSIGNMENT_CARD_HEIGHT = 30;
	private static final int COLLAPSED_ASSIGNMENT_HEIGHT = ASSIGNMENT_CARD_HEIGHT;
	private static final int GLOBAL_BUTTON_HEIGHT = 16;
	private static final int GLOBAL_BUTTON_GAP = 5;
	private static final int COLLAPSE_ALL_BUTTON_WIDTH = 80;
	private static final int EXPAND_ALL_BUTTON_WIDTH = 72;
	private static final int ASSIGNMENT_LIST_TOP_OFFSET = 57;
	private static final int FIRST_BLOODCRAFT_HEIGHT = 145;
	private static final int HERMIT_ROAD_HEIGHT = 110;
	private static final int FIRST_SEPARATION_HEIGHT = 180;
	private static final int BODY_ANSWERS_HEIGHT = 143;
	private static final int RED_TAXONOMY_HEIGHT = 56;
	private static final int ENZYME_MASTERY_HEIGHT = 56;
	private static final int LIVING_BESTIARY_HEIGHT = 68;
	private static final int WOVEN_VESSEL_HEIGHT = 68;
	private static final int VEIN_MASON_HEIGHT = 68;
	private static final int THE_WORN_VOW_HEIGHT = 68;
	private static final int THE_THREE_ANSWERS_HEIGHT = 68;
	private static final int CRIMSON_VESTMENT_HEIGHT = 68;
	private static final int WEIGHT_OF_THE_FRAME_HEIGHT = 68;
	private static final int THE_ASSUMED_LIMB_HEIGHT = 68;
	private static final int COVENANT_WRITTEN_HEIGHT = 68;
	private static final int LIVING_COVENANT_HEIGHT = 68;
	private static final int BLOOD_BENEATH_BLOOD_HEIGHT = 68;
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
	private static final ResourceLocation VICAR_PORTRAIT =
			Hemomancy.rloc("textures/entity/harbinger_vicar/harbinger_vicar_portrait.png");
	private static final ResourceLocation ALCHEMIST_PORTRAIT =
			Hemomancy.rloc("textures/entity/harbinger_alchemist/harbinger_alchemist_portrait.png");
	private static final ResourceLocation MNEMONIST_PORTRAIT =
			Hemomancy.rloc("textures/entity/harbinger_mnemonist/harbinger_mnemonist_portrait.png");
	private static final ResourceLocation VEIN_MASON_PORTRAIT =
			Hemomancy.rloc("textures/entity/harbinger_cicatrix_anchorite/harbinger_cicatrix_anchorite_portrait.png");
	private static final ResourceLocation ARTIFICER_PORTRAIT =
			Hemomancy.rloc("textures/entity/harbinger_artificer/harbinger_artificer_portrait.png");

	private enum AssignmentCategory {
		MAIN("screen.hemomancy.harbinger_assignment_ledger.assignment_label.main"),
		SIDE("screen.hemomancy.harbinger_assignment_ledger.assignment_label.side"),
		VOCATION("screen.hemomancy.harbinger_assignment_ledger.assignment_label.vocation"),
		CATALOGUE("screen.hemomancy.harbinger_assignment_ledger.assignment_label.catalogue");

		private final String labelKey;
		AssignmentCategory(String labelKey) { this.labelKey = labelKey; }
	}

	private enum AssignmentSection {
		FIRST_BLOODCRAFT(1, AssignmentCategory.MAIN, FIRST_BLOODCRAFT_HEIGHT),
		HERMIT_ROAD(1, AssignmentCategory.SIDE, HERMIT_ROAD_HEIGHT),
		FIRST_SEPARATION(2, AssignmentCategory.MAIN, FIRST_SEPARATION_HEIGHT),
		BODY_ANSWERS(2, AssignmentCategory.SIDE, BODY_ANSWERS_HEIGHT),
		RED_TAXONOMY(2, AssignmentCategory.CATALOGUE, RED_TAXONOMY_HEIGHT),
		LIVING_BESTIARY(2, AssignmentCategory.CATALOGUE, LIVING_BESTIARY_HEIGHT),
		ENZYME_MASTERY(2, AssignmentCategory.CATALOGUE, ENZYME_MASTERY_HEIGHT),
		THE_WORN_VOW(2, AssignmentCategory.VOCATION, THE_WORN_VOW_HEIGHT),
		WOVEN_VESSEL(3, AssignmentCategory.MAIN, WOVEN_VESSEL_HEIGHT),
		THE_THREE_ANSWERS(3, AssignmentCategory.VOCATION, THE_THREE_ANSWERS_HEIGHT),
		VEIN_MASON(4, AssignmentCategory.MAIN, VEIN_MASON_HEIGHT),
		COVENANT_WRITTEN(5, AssignmentCategory.MAIN, COVENANT_WRITTEN_HEIGHT),
		CRIMSON_VESTMENT(5, AssignmentCategory.VOCATION, CRIMSON_VESTMENT_HEIGHT),
		THE_ASSUMED_LIMB(5, AssignmentCategory.VOCATION, THE_ASSUMED_LIMB_HEIGHT),
		LIVING_COVENANT(6, AssignmentCategory.MAIN, LIVING_COVENANT_HEIGHT),
		BLOOD_BENEATH_BLOOD(7, AssignmentCategory.MAIN, BLOOD_BENEATH_BLOOD_HEIGHT),
		WEIGHT_OF_THE_FRAME(7, AssignmentCategory.VOCATION, WEIGHT_OF_THE_FRAME_HEIGHT);

		private final int assignmentDegree;
		private final AssignmentCategory category;
		private final int expandedHeight;

		AssignmentSection(int assignmentDegree, AssignmentCategory category, int expandedHeight) {
			this.assignmentDegree = assignmentDegree;
			this.category = category;
			this.expandedHeight = expandedHeight;
		}
	}

	private static final List<AssignmentSection> ORDERED_ASSIGNMENT_SECTIONS = Arrays.stream(AssignmentSection.values())
			.sorted(Comparator.comparingInt((AssignmentSection section) -> section.assignmentDegree)
					.thenComparingInt(Enum::ordinal))
			.toList();

	private record AssignmentHitbox(AssignmentSection section, int x, int y, int w, int h) {
		boolean contains(double mx, double my) {
			return mx >= x && mx <= x + w && my >= y && my <= y + h;
		}
	}

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
	private final boolean bodyAnswersBriefed;
	private final boolean bodyAnswersComplete;
	private final int muscleMemoryCount;
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
	private final boolean artificerArmaturePlaced;
	private final boolean artificerFirstHematicUpgrade;
	private final boolean artificerHematicIronFitting;
	private final boolean artificerFirstForkUpgrade;
	private final boolean artificerForkFitting;
	private final boolean artificerFrameConsecrated;
	private final boolean artificerFirstBloodLustUpgrade;
	private final boolean artificerBloodLustFitting;
	private final boolean artificerMonolithicFrame;
	private final boolean artificerFirstD7Upgrade;
	private final boolean artificerD7Fitting;
	private final boolean artificerFirstLivingGraft;
	private final int artificerLivingWeaponFormCount;
	private final boolean artificerLivingArsenalFitting;
	private final boolean foundedBloodline;
	private final boolean foundingFaneEstablished;
	private final boolean chamberReturned;
	private final boolean covenantThroneBound;
	private final boolean covenantVigilCompleted;
	private final boolean livingCovenantComplete;
	private final int pomesConsumed;
	private final boolean qliphothCommunionComplete;
	private final boolean silentPending;
	private final boolean severedPortalOpen;
	private final boolean silentArchon;
	private final EnumSet<AssignmentSection> collapsedAssignments = EnumSet.noneOf(AssignmentSection.class);
	private final List<AssignmentHitbox> assignmentHitboxes = new ArrayList<>();
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
			boolean bodyAnswersBriefed, boolean bodyAnswersComplete, int muscleMemoryCount,
			int redTaxonomyCount, boolean redTaxonomyComplete,
			int enzymeMasteryCount, boolean enzymeMasteryComplete,
			int livingBestiaryCount, int livingBestiaryTotal, int morphlingLayerCount,
			boolean hasBlankHematicMemory, boolean mnemonistWovenVesselComplete,
			boolean mnemonistFirstWeaveComplete,
			boolean vicarMasonsRespiteDirective, boolean veinMasonFirstLesson,
			boolean veinMasonFirstScarCarved, boolean veinMasonFirstScarLearned,
			boolean veinMasonFirstEffigyPattern, boolean veinMasonFirstEffigyLoadout,
			boolean artificerArmaturePlaced, boolean artificerFirstHematicUpgrade,
			boolean artificerHematicIronFitting, boolean artificerFirstForkUpgrade, boolean artificerForkFitting,
			boolean artificerFrameConsecrated, boolean artificerFirstBloodLustUpgrade,
			boolean artificerBloodLustFitting, boolean artificerMonolithicFrame,
			boolean artificerFirstD7Upgrade, boolean artificerD7Fitting, boolean artificerFirstLivingGraft,
			int artificerLivingWeaponFormCount, boolean artificerLivingArsenalFitting,
			boolean foundedBloodline, boolean foundingFaneEstablished, boolean chamberReturned,
			boolean covenantThroneBound, boolean covenantVigilCompleted, boolean livingCovenantComplete,
			int pomesConsumed, boolean qliphothCommunionComplete, boolean silentPending,
			boolean severedPortalOpen, boolean silentArchon) {
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
		this.bodyAnswersBriefed = bodyAnswersBriefed;
		this.bodyAnswersComplete = bodyAnswersComplete;
		this.muscleMemoryCount = muscleMemoryCount;
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
		this.artificerArmaturePlaced = artificerArmaturePlaced;
		this.artificerFirstHematicUpgrade = artificerFirstHematicUpgrade;
		this.artificerHematicIronFitting = artificerHematicIronFitting;
		this.artificerFirstForkUpgrade = artificerFirstForkUpgrade;
		this.artificerForkFitting = artificerForkFitting;
		this.artificerFrameConsecrated = artificerFrameConsecrated;
		this.artificerFirstBloodLustUpgrade = artificerFirstBloodLustUpgrade;
		this.artificerBloodLustFitting = artificerBloodLustFitting;
		this.artificerMonolithicFrame = artificerMonolithicFrame;
		this.artificerFirstD7Upgrade = artificerFirstD7Upgrade;
		this.artificerD7Fitting = artificerD7Fitting;
		this.artificerFirstLivingGraft = artificerFirstLivingGraft;
		this.artificerLivingWeaponFormCount = artificerLivingWeaponFormCount;
		this.artificerLivingArsenalFitting = artificerLivingArsenalFitting;
		this.foundedBloodline = foundedBloodline;
		this.foundingFaneEstablished = foundingFaneEstablished;
		this.chamberReturned = chamberReturned;
		this.covenantThroneBound = covenantThroneBound;
		this.covenantVigilCompleted = covenantVigilCompleted;
		this.livingCovenantComplete = livingCovenantComplete;
		this.pomesConsumed = pomesConsumed;
		this.qliphothCommunionComplete = qliphothCommunionComplete;
		this.silentPending = silentPending;
		this.severedPortalOpen = severedPortalOpen;
		this.silentArchon = silentArchon;
	}

	public static void open(int degree, boolean firstAwakening, boolean degreeOne,
			boolean vesselFilled, boolean liberSanguinumCrafted, boolean hematicIronBlockCrafted,
			boolean firstRemnant, boolean ledgerGranted,
			boolean hasVialCentrifuge, boolean hasSampledBloodVial,
			boolean firstSeparationStarted, boolean hasAnyEnzyme,
			boolean bodyAnswersBriefed, boolean bodyAnswersComplete, int muscleMemoryCount,
			int redTaxonomyCount, boolean redTaxonomyComplete,
			int enzymeMasteryCount, boolean enzymeMasteryComplete,
			int livingBestiaryCount, int livingBestiaryTotal, int morphlingLayerCount,
			boolean hasBlankHematicMemory, boolean mnemonistWovenVesselComplete,
			boolean mnemonistFirstWeaveComplete,
			boolean vicarMasonsRespiteDirective, boolean veinMasonFirstLesson,
			boolean veinMasonFirstScarCarved, boolean veinMasonFirstScarLearned,
			boolean veinMasonFirstEffigyPattern, boolean veinMasonFirstEffigyLoadout,
			boolean artificerArmaturePlaced, boolean artificerFirstHematicUpgrade,
			boolean artificerHematicIronFitting, boolean artificerFirstForkUpgrade, boolean artificerForkFitting,
			boolean artificerFrameConsecrated, boolean artificerFirstBloodLustUpgrade,
			boolean artificerBloodLustFitting, boolean artificerMonolithicFrame,
			boolean artificerFirstD7Upgrade, boolean artificerD7Fitting, boolean artificerFirstLivingGraft,
			int artificerLivingWeaponFormCount, boolean artificerLivingArsenalFitting,
			boolean foundedBloodline, boolean foundingFaneEstablished, boolean chamberReturned,
			boolean covenantThroneBound, boolean covenantVigilCompleted, boolean livingCovenantComplete,
			int pomesConsumed, boolean qliphothCommunionComplete, boolean silentPending,
			boolean severedPortalOpen, boolean silentArchon) {
		Minecraft.getInstance().setScreen(new HarbingerAssignmentLedgerScreen(
				degree, firstAwakening, degreeOne, vesselFilled, liberSanguinumCrafted, hematicIronBlockCrafted,
				firstRemnant, ledgerGranted, hasVialCentrifuge, hasSampledBloodVial,
				firstSeparationStarted, hasAnyEnzyme,
				bodyAnswersBriefed, bodyAnswersComplete, muscleMemoryCount,
				redTaxonomyCount, redTaxonomyComplete, enzymeMasteryCount, enzymeMasteryComplete,
				livingBestiaryCount, livingBestiaryTotal, morphlingLayerCount, hasBlankHematicMemory,
				mnemonistWovenVesselComplete, mnemonistFirstWeaveComplete, vicarMasonsRespiteDirective,
				veinMasonFirstLesson, veinMasonFirstScarCarved, veinMasonFirstScarLearned,
				veinMasonFirstEffigyPattern, veinMasonFirstEffigyLoadout,
				artificerArmaturePlaced, artificerFirstHematicUpgrade, artificerHematicIronFitting,
				artificerFirstForkUpgrade, artificerForkFitting, artificerFrameConsecrated,
				artificerFirstBloodLustUpgrade, artificerBloodLustFitting, artificerMonolithicFrame,
				artificerFirstD7Upgrade, artificerD7Fitting, artificerFirstLivingGraft,
				artificerLivingWeaponFormCount, artificerLivingArsenalFitting,
				foundedBloodline, foundingFaneEstablished, chamberReturned,
				covenantThroneBound, covenantVigilCompleted, livingCovenantComplete,
				pomesConsumed, qliphothCommunionComplete, silentPending, severedPortalOpen, silentArchon));
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
		for (AssignmentSection section : AssignmentSection.values()) {
			if (section.category != AssignmentCategory.MAIN) collapsedAssignments.add(section);
		}
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
		if (button == 0 && isOverCollapseAllButton(mx, my)) {
			collapseAllAssignments();
			return true;
		}
		if (button == 0 && isOverExpandAllButton(mx, my)) {
			expandAllAssignments();
			return true;
		}
		if (button == 0 && toggleAssignmentAt(mx, my)) {
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
		renderCollapseControls(gfx, mouseX, mouseY);
		drawProgressBar(gfx, x, y + 44, w, 8, completedCount(), 18);

		int listY = assignmentListY();
		int listH = assignmentListHeight();
		int listW = w - SCROLLBAR_WIDTH - 4;
		int totalH = totalAssignmentContentHeight();
		assignmentMaxScroll = Math.max(0, totalH - listH);
		assignmentScrollOffset = clampAssignmentScroll(assignmentScrollOffset);

		gfx.enableScissor(x, listY, x + w - SCROLLBAR_WIDTH, listY + listH);
		assignmentHitboxes.clear();
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

	private void renderCollapseControls(GuiGraphics gfx, int mouseX, int mouseY) {
		renderLedgerButton(gfx, collapseAllButtonX(), globalButtonY(), COLLAPSE_ALL_BUTTON_WIDTH, GLOBAL_BUTTON_HEIGHT,
				Component.translatable("screen.hemomancy.harbinger_assignment_ledger.collapse_all"),
				isOverCollapseAllButton(mouseX, mouseY));
		renderLedgerButton(gfx, expandAllButtonX(), globalButtonY(), EXPAND_ALL_BUTTON_WIDTH, GLOBAL_BUTTON_HEIGHT,
				Component.translatable("screen.hemomancy.harbinger_assignment_ledger.expand_all"),
				isOverExpandAllButton(mouseX, mouseY));
	}

	private void renderLedgerButton(GuiGraphics gfx, int x, int y, int w, int h, Component label, boolean hovered) {
		gfx.fill(x, y, x + w, y + h, hovered ? 0xE03B2420 : PANEL_ROW);
		ScreenDrawUtils.drawBorder(gfx, x, y, w, h, hovered ? BORDER : BORDER_MUTED, 0xAA1E0D0B);
		String text = truncateWithEllipsis(label.getString(), Math.max(20, w - 10));
		int textX = x + Math.max(4, (w - font.width(text)) / 2);
		gfx.drawString(font, text, textX, y + 4, hovered ? CURRENT : TEXT, false);
	}

	private void collapseAllAssignments() {
		collapsedAssignments.clear();
		collapsedAssignments.addAll(EnumSet.allOf(AssignmentSection.class));
		assignmentMaxScroll = Math.max(0, totalAssignmentContentHeight() - assignmentListHeight());
		assignmentScrollOffset = clampAssignmentScroll(assignmentScrollOffset);
	}

	private void expandAllAssignments() {
		collapsedAssignments.clear();
		assignmentMaxScroll = Math.max(0, totalAssignmentContentHeight() - assignmentListHeight());
		assignmentScrollOffset = clampAssignmentScroll(assignmentScrollOffset);
	}

	private boolean isOverCollapseAllButton(double mx, double my) {
		return mx >= collapseAllButtonX() && mx <= collapseAllButtonX() + COLLAPSE_ALL_BUTTON_WIDTH
				&& my >= globalButtonY() && my <= globalButtonY() + GLOBAL_BUTTON_HEIGHT;
	}

	private boolean isOverExpandAllButton(double mx, double my) {
		return mx >= expandAllButtonX() && mx <= expandAllButtonX() + EXPAND_ALL_BUTTON_WIDTH
				&& my >= globalButtonY() && my <= globalButtonY() + GLOBAL_BUTTON_HEIGHT;
	}

	private int collapseAllButtonX() {
		return contentRight() - EXPAND_ALL_BUTTON_WIDTH - GLOBAL_BUTTON_GAP - COLLAPSE_ALL_BUTTON_WIDTH;
	}

	private int expandAllButtonX() {
		return contentRight() - EXPAND_ALL_BUTTON_WIDTH;
	}

	private int globalButtonY() {
		return contentTop() + 23;
	}

	private boolean renderCollapsedAssignmentIfNeeded(GuiGraphics gfx, AssignmentSection section,
			int x, int y, int w, ResourceLocation portrait, String titleKey, String progressKey,
			int progress, int total, boolean done) {
		assignmentHitboxes.add(new AssignmentHitbox(section, x, y, w, assignmentHeight(section)));
		if (!isAssignmentCollapsed(section)) {
			return false;
		}
		renderCollapsedAssignment(gfx, section, x, y, w, portrait, titleKey, progressKey, progress, total, done);
		return true;
	}

	private void renderCollapsedAssignment(GuiGraphics gfx, AssignmentSection section, int x, int y, int w, ResourceLocation portrait,
			String titleKey, String progressKey, int progress, int total, boolean done) {
		int h = COLLAPSED_ASSIGNMENT_HEIGHT;
		renderAssignerPortrait(gfx, portrait, x, y, h);
		int cardX = compactCardX(x, h);
		int cardW = compactCardWidth(w, h);
		gfx.fill(cardX, y, cardX + cardW, y + h, done ? PANEL_DONE : PANEL_ROW);
		ScreenDrawUtils.drawBorder(gfx, cardX, y, cardW, h, done ? BORDER : BORDER_MUTED, 0xAA1E0D0B);

		Component progressText = Component.translatable(progressKey, progress, total);
		int progressW = font.width(progressText);
		int titleX = cardX + 22;
		int progressX = cardX + cardW - progressW - 8;
		int titleMaxW = Math.max(20, progressX - titleX - 8);
		String title = truncateWithEllipsis(Component.translatable(titleKey).getString(), titleMaxW);

		gfx.drawString(font, Component.literal("+"), cardX + 8, y + 10, done ? DONE : CURRENT, false);
		renderAssignmentLabel(gfx, section, cardX + 22, y + 4, done ? DONE : MUTED);
		gfx.drawString(font, title, titleX, y + 14, done ? DONE : TITLE, false);
		if (progressX > titleX) {
			gfx.drawString(font, progressText, progressX, y + 14, done ? DONE : CURRENT, false);
		}
	}

	private boolean isAssignmentCollapsed(AssignmentSection section) {
		return collapsedAssignments.contains(section);
	}

	private int assignmentHeight(AssignmentSection section) {
		return isAssignmentCollapsed(section) ? COLLAPSED_ASSIGNMENT_HEIGHT : section.expandedHeight;
	}

	private boolean toggleAssignmentAt(double mx, double my) {
		if (!isOverAssignmentPanel(mx, my)) {
			return false;
		}
		for (AssignmentHitbox hitbox : assignmentHitboxes) {
			if (hitbox.contains(mx, my)) {
				if (collapsedAssignments.contains(hitbox.section())) {
					collapsedAssignments.remove(hitbox.section());
				} else {
					collapsedAssignments.add(hitbox.section());
				}
				assignmentScrollOffset = clampAssignmentScroll(assignmentScrollOffset);
				return true;
			}
		}
		return false;
	}

	private int renderAssignmentList(GuiGraphics gfx, int x, int cardY, int w, int mouseX, int mouseY) {
		for (AssignmentSection section : ORDERED_ASSIGNMENT_SECTIONS) {
			renderAssignmentSection(gfx, section, x, cardY, w, mouseX, mouseY);
			cardY += assignmentHeight(section) + SECTION_GAP;
		}
		return totalAssignmentContentHeight();
	}

	private void renderAssignmentSection(GuiGraphics gfx, AssignmentSection section,
			int x, int y, int w, int mouseX, int mouseY) {
		switch (section) {
			case FIRST_BLOODCRAFT -> renderFirstBloodcraft(gfx, x, y, w, mouseX, mouseY);
			case HERMIT_ROAD -> renderHermitRoad(gfx, x, y, w, mouseX, mouseY);
			case FIRST_SEPARATION -> renderFirstSeparation(gfx, x, y, w, mouseX, mouseY);
			case BODY_ANSWERS -> renderBodyAnswers(gfx, x, y, w, mouseX, mouseY);
			case RED_TAXONOMY -> renderRedTaxonomy(gfx, x, y, w, mouseX, mouseY);
			case LIVING_BESTIARY -> renderLivingBestiary(gfx, x, y, w, mouseX, mouseY);
			case ENZYME_MASTERY -> renderEnzymeMastery(gfx, x, y, w, mouseX, mouseY);
			case THE_WORN_VOW -> renderTheWornVow(gfx, x, y, w, mouseX, mouseY);
			case WOVEN_VESSEL -> renderWovenVessel(gfx, x, y, w, mouseX, mouseY);
			case THE_THREE_ANSWERS -> renderTheThreeAnswers(gfx, x, y, w, mouseX, mouseY);
			case VEIN_MASON -> renderVeinMason(gfx, x, y, w, mouseX, mouseY);
			case COVENANT_WRITTEN -> renderCovenantWritten(gfx, x, y, w, mouseX, mouseY);
			case CRIMSON_VESTMENT -> renderCrimsonVestment(gfx, x, y, w, mouseX, mouseY);
			case THE_ASSUMED_LIMB -> renderTheAssumedLimb(gfx, x, y, w, mouseX, mouseY);
			case LIVING_COVENANT -> renderLivingCovenant(gfx, x, y, w, mouseX, mouseY);
			case BLOOD_BENEATH_BLOOD -> renderBloodBeneathBlood(gfx, x, y, w, mouseX, mouseY);
			case WEIGHT_OF_THE_FRAME -> renderWeightOfTheFrame(gfx, x, y, w, mouseX, mouseY);
		}
	}

	private void renderFirstBloodcraft(GuiGraphics gfx, int x, int y, int w, int mouseX, int mouseY) {
		int progress = firstBloodcraftProgress();
		if (renderCollapsedAssignmentIfNeeded(gfx, AssignmentSection.FIRST_BLOODCRAFT, x, y, w, VICAR_PORTRAIT,
				"screen.hemomancy.harbinger_assignment_ledger.first_bloodcraft.title",
				"screen.hemomancy.harbinger_assignment_ledger.first_bloodcraft.progress",
				progress, 3, progress >= 3)) {
			return;
		}
		gfx.fill(x, y, x + w, y + FIRST_BLOODCRAFT_HEIGHT, PANEL_DARK);
		ScreenDrawUtils.drawBorder(gfx, x, y, w, FIRST_BLOODCRAFT_HEIGHT, BORDER, BORDER_MUTED);
		renderGroupHeader(gfx, AssignmentSection.FIRST_BLOODCRAFT, x + 8, y + 6, w - 16,
				"screen.hemomancy.harbinger_assignment_ledger.first_bloodcraft.title",
				"screen.hemomancy.harbinger_assignment_ledger.first_bloodcraft.progress",
				progress, 3, progress >= 3);
		drawProgressBar(gfx, x + 8, y + 31, w - 16, 7, progress, 3);
		int rowY = y + 42;
		renderAssignmentCard(gfx, x + 8, rowY, w - 16, ASSIGNMENT_CARD_HEIGHT, vesselFilled, VICAR_PORTRAIT,
				"screen.hemomancy.harbinger_assignment_ledger.step.fill_vessel",
				"screen.hemomancy.harbinger_assignment_ledger.step.fill_vessel.desc", mouseX, mouseY);
		rowY += ASSIGNMENT_CARD_HEIGHT + CARD_GAP;
		renderAssignmentCard(gfx, x + 8, rowY, w - 16, ASSIGNMENT_CARD_HEIGHT, liberSanguinumCrafted, VICAR_PORTRAIT,
				"screen.hemomancy.harbinger_assignment_ledger.step.craft_liber",
				"screen.hemomancy.harbinger_assignment_ledger.step.craft_liber.desc", mouseX, mouseY);
		rowY += ASSIGNMENT_CARD_HEIGHT + CARD_GAP;
		renderAssignmentCard(gfx, x + 8, rowY, w - 16, ASSIGNMENT_CARD_HEIGHT, hematicIronBlockCrafted, VICAR_PORTRAIT,
				"screen.hemomancy.harbinger_assignment_ledger.step.craft_hematic_iron",
				"screen.hemomancy.harbinger_assignment_ledger.step.craft_hematic_iron.desc", mouseX, mouseY);
	}

	private void renderHermitRoad(GuiGraphics gfx, int x, int y, int w, int mouseX, int mouseY) {
		int progress = hermitRoadProgress();
		if (renderCollapsedAssignmentIfNeeded(gfx, AssignmentSection.HERMIT_ROAD, x, y, w, VICAR_PORTRAIT,
				"screen.hemomancy.harbinger_assignment_ledger.hermit_road.side_title",
				"screen.hemomancy.harbinger_assignment_ledger.hermit_road.progress",
				progress, 3, progress >= 3)) {
			return;
		}
		gfx.fill(x, y, x + w, y + HERMIT_ROAD_HEIGHT, PANEL_DARK);
		ScreenDrawUtils.drawBorder(gfx, x, y, w, HERMIT_ROAD_HEIGHT, BORDER, BORDER_MUTED);
		renderGroupHeader(gfx, AssignmentSection.HERMIT_ROAD, x + 8, y + 6, w - 16,
				"screen.hemomancy.harbinger_assignment_ledger.hermit_road.side_title",
				"screen.hemomancy.harbinger_assignment_ledger.hermit_road.progress",
				progress, 3, progress >= 3);
		drawProgressBar(gfx, x + 8, y + 31, w - 16, 7, progress, 3);
		int rowY = y + 42;
		renderAssignmentCard(gfx, x + 8, rowY, w - 16, ASSIGNMENT_CARD_HEIGHT, firstRemnant, VICAR_PORTRAIT,
				"screen.hemomancy.harbinger_assignment_ledger.step.remnant",
				"screen.hemomancy.harbinger_assignment_ledger.step.remnant.desc", mouseX, mouseY);
		rowY += ASSIGNMENT_CARD_HEIGHT + CARD_GAP;
		renderAssignmentCard(gfx, x + 8, rowY, w - 16, ASSIGNMENT_CARD_HEIGHT, ledgerGranted, VICAR_PORTRAIT,
				"screen.hemomancy.harbinger_assignment_ledger.step.ledger",
				"screen.hemomancy.harbinger_assignment_ledger.step.ledger.desc", mouseX, mouseY);
	}

	private void renderFirstSeparation(GuiGraphics gfx, int x, int y, int w, int mouseX, int mouseY) {
		int progress = firstSeparationProgress();
		if (renderCollapsedAssignmentIfNeeded(gfx, AssignmentSection.FIRST_SEPARATION, x, y, w, ALCHEMIST_PORTRAIT,
				"screen.hemomancy.harbinger_assignment_ledger.first_separation.title",
				"screen.hemomancy.harbinger_assignment_ledger.first_separation.progress",
				progress, 4, progress >= 4)) {
			return;
		}
		gfx.fill(x, y, x + w, y + FIRST_SEPARATION_HEIGHT, PANEL_DARK);
		ScreenDrawUtils.drawBorder(gfx, x, y, w, FIRST_SEPARATION_HEIGHT, BORDER, BORDER_MUTED);
		renderGroupHeader(gfx, AssignmentSection.FIRST_SEPARATION, x + 8, y + 6, w - 16,
				"screen.hemomancy.harbinger_assignment_ledger.first_separation.title",
				"screen.hemomancy.harbinger_assignment_ledger.first_separation.progress",
				progress, 4, progress >= 4);
		drawProgressBar(gfx, x + 8, y + 31, w - 16, 7, progress, 4);
		int rowY = y + 42;
		renderAssignmentCard(gfx, x + 8, rowY, w - 16, ASSIGNMENT_CARD_HEIGHT, hasVialCentrifuge, ALCHEMIST_PORTRAIT,
				"screen.hemomancy.harbinger_assignment_ledger.step.obtain_centrifuge",
				"screen.hemomancy.harbinger_assignment_ledger.step.obtain_centrifuge.desc", mouseX, mouseY);
		rowY += ASSIGNMENT_CARD_HEIGHT + CARD_GAP;
		renderAssignmentCard(gfx, x + 8, rowY, w - 16, ASSIGNMENT_CARD_HEIGHT, hasSampledBloodVial, ALCHEMIST_PORTRAIT,
				"screen.hemomancy.harbinger_assignment_ledger.step.sample_blood_vial",
				"screen.hemomancy.harbinger_assignment_ledger.step.sample_blood_vial.desc", mouseX, mouseY);
		rowY += ASSIGNMENT_CARD_HEIGHT + CARD_GAP;
		renderAssignmentCard(gfx, x + 8, rowY, w - 16, ASSIGNMENT_CARD_HEIGHT, firstSeparationStarted, ALCHEMIST_PORTRAIT,
				"screen.hemomancy.harbinger_assignment_ledger.step.start_separation",
				"screen.hemomancy.harbinger_assignment_ledger.step.start_separation.desc", mouseX, mouseY);
		rowY += ASSIGNMENT_CARD_HEIGHT + CARD_GAP;
		renderAssignmentCard(gfx, x + 8, rowY, w - 16, ASSIGNMENT_CARD_HEIGHT, hasAnyEnzyme, ALCHEMIST_PORTRAIT,
				"screen.hemomancy.harbinger_assignment_ledger.step.obtain_enzyme",
				"screen.hemomancy.harbinger_assignment_ledger.step.obtain_enzyme.desc", mouseX, mouseY);
	}

	private void renderBodyAnswers(GuiGraphics gfx, int x, int y, int w, int mouseX, int mouseY) {
		int progress = bodyAnswersProgress();
		if (renderCollapsedAssignmentIfNeeded(gfx, AssignmentSection.BODY_ANSWERS, x, y, w, ALCHEMIST_PORTRAIT,
				"screen.hemomancy.harbinger_assignment_ledger.body_answers.side_title",
				"screen.hemomancy.harbinger_assignment_ledger.body_answers.progress",
				progress, 2, progress >= 2)) {
			return;
		}
		gfx.fill(x, y, x + w, y + BODY_ANSWERS_HEIGHT, PANEL_DARK);
		ScreenDrawUtils.drawBorder(gfx, x, y, w, BODY_ANSWERS_HEIGHT, BORDER, BORDER_MUTED);
		renderGroupHeader(gfx, AssignmentSection.BODY_ANSWERS, x + 8, y + 6, w - 16,
				"screen.hemomancy.harbinger_assignment_ledger.body_answers.side_title",
				"screen.hemomancy.harbinger_assignment_ledger.body_answers.progress",
				progress, 2, progress >= 2);
		drawProgressBar(gfx, x + 8, y + 31, w - 16, 7, progress, 2);
		int rowY = y + 42;
		renderAssignmentCard(gfx, x + 8, rowY, w - 16, ASSIGNMENT_CARD_HEIGHT, bodyAnswersBriefed,
				ALCHEMIST_PORTRAIT,
				"screen.hemomancy.harbinger_assignment_ledger.step.body_answers_briefed",
				"screen.hemomancy.harbinger_assignment_ledger.step.body_answers_briefed.desc", mouseX, mouseY);
		rowY += ASSIGNMENT_CARD_HEIGHT + CARD_GAP;
		renderAssignmentCard(gfx, x + 8, rowY, w - 16, ASSIGNMENT_CARD_HEIGHT, bodyAnswersComplete,
				ALCHEMIST_PORTRAIT,
				"screen.hemomancy.harbinger_assignment_ledger.step.body_answers_complete",
				"screen.hemomancy.harbinger_assignment_ledger.step.body_answers_complete.desc", mouseX, mouseY);
		rowY += ASSIGNMENT_CARD_HEIGHT + CARD_GAP;
		renderAssignmentCard(gfx, x + 8, rowY, w - 16, ASSIGNMENT_CARD_HEIGHT, muscleMemoryCount >= 8,
				ALCHEMIST_PORTRAIT,
				"screen.hemomancy.harbinger_assignment_ledger.step.muscle_memory_catalogue",
				"screen.hemomancy.harbinger_assignment_ledger.step.muscle_memory_catalogue.desc", mouseX, mouseY);
	}

	private void renderAssignmentCard(GuiGraphics gfx, int x, int y, int w, int h, boolean done,
			ResourceLocation assignerPortrait, String titleKey, String descriptionKey, int mouseX, int mouseY) {
		int portraitSize = h;
		renderAssignerPortrait(gfx, assignerPortrait, x, y, portraitSize);
		int cardX = x + portraitSize + PORTRAIT_GAP;
		int cardW = Math.max(40, w - portraitSize - PORTRAIT_GAP);
		gfx.fill(cardX, y, cardX + cardW, y + h, done ? PANEL_DONE : PANEL_ROW);
		ScreenDrawUtils.drawBorder(gfx, cardX, y, cardW, h, done ? BORDER : BORDER_MUTED, 0xAA1E0D0B);
		gfx.drawString(font, Component.literal(done ? "[x]" : "[ ]"), cardX + 8, y + 6, done ? DONE : CURRENT, false);
		gfx.drawString(font, Component.translatable(titleKey), cardX + 31, y + 5, done ? DONE : TITLE, false);
		renderTruncatedDescription(gfx, Component.translatable(descriptionKey), cardX + 31, y + 17,
				Math.max(20, cardW - 46), done ? TEXT : MUTED, mouseX, mouseY);
	}

	private void renderAssignerPortrait(GuiGraphics gfx, ResourceLocation assignerPortrait, int x, int y, int size) {
		gfx.fill(x, y, x + size, y + size, PANEL_ROW);
		ScreenDrawUtils.drawBorder(gfx, x, y, size, size, BORDER, BORDER_MUTED);

		int innerSize = Math.max(1, size - 2);
		AbstractTexture portraitTexture = Minecraft.getInstance().getTextureManager().getTexture(assignerPortrait);
		portraitTexture.setBlurMipmap(false, false);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		gfx.blit(assignerPortrait, x + 1, y + 1, innerSize, innerSize,
				0.0F, 0.0F,
				PORTRAIT_TEXTURE_SIZE, PORTRAIT_TEXTURE_SIZE,
				PORTRAIT_TEXTURE_SIZE, PORTRAIT_TEXTURE_SIZE);
		portraitTexture.restoreLastBlurMipmap();
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
	}

	private int compactCardX(int x, int h) {
		return x + h + PORTRAIT_GAP;
	}

	private int compactCardWidth(int w, int h) {
		return Math.max(40, w - h - PORTRAIT_GAP);
	}

	private void renderRedTaxonomy(GuiGraphics gfx, int x, int y, int w, int mouseX, int mouseY) {
		int progress = Math.min(redTaxonomyCount, 4);
		if (renderCollapsedAssignmentIfNeeded(gfx, AssignmentSection.RED_TAXONOMY, x, y, w, ALCHEMIST_PORTRAIT,
				"screen.hemomancy.harbinger_assignment_ledger.red_taxonomy.side_title",
				"screen.hemomancy.harbinger_assignment_ledger.red_taxonomy.progress",
				progress, 4, redTaxonomyComplete)) {
			return;
		}
		int h = RED_TAXONOMY_HEIGHT;
		renderAssignerPortrait(gfx, ALCHEMIST_PORTRAIT, x, y, h);
		int cardX = compactCardX(x, h);
		int cardW = compactCardWidth(w, h);
		gfx.fill(cardX, y, cardX + cardW, y + h, PANEL_DARK);
		ScreenDrawUtils.drawBorder(gfx, cardX, y, cardW, h, BORDER, BORDER_MUTED);
		renderTaxonomyHeader(gfx, cardX + 8, y + 6, cardW - 16);
		drawProgressBar(gfx, cardX + 8, y + 31, cardW - 16, 7, progress, 4);
		renderTruncatedDescription(gfx, Component.translatable(redTaxonomyComplete
						? "screen.hemomancy.harbinger_assignment_ledger.red_taxonomy.complete"
						: "screen.hemomancy.harbinger_assignment_ledger.red_taxonomy.desc"),
				cardX + 8, y + 42, Math.max(20, cardW - 16), redTaxonomyComplete ? TEXT : MUTED, mouseX, mouseY);
	}

	private void renderTaxonomyHeader(GuiGraphics gfx, int x, int y, int w) {
		renderGroupHeader(gfx, AssignmentSection.RED_TAXONOMY, x, y, w,
				"screen.hemomancy.harbinger_assignment_ledger.red_taxonomy.side_title",
				"screen.hemomancy.harbinger_assignment_ledger.red_taxonomy.progress",
				redTaxonomyCount, 4, redTaxonomyComplete);
	}

	private void renderEnzymeMastery(GuiGraphics gfx, int x, int y, int w, int mouseX, int mouseY) {
		int progress = Math.min(enzymeMasteryCount, 8);
		if (renderCollapsedAssignmentIfNeeded(gfx, AssignmentSection.ENZYME_MASTERY, x, y, w, ALCHEMIST_PORTRAIT,
				"screen.hemomancy.harbinger_assignment_ledger.enzyme_mastery.side_title",
				"screen.hemomancy.harbinger_assignment_ledger.enzyme_mastery.progress",
				progress, 8, enzymeMasteryComplete)) {
			return;
		}
		int h = ENZYME_MASTERY_HEIGHT;
		renderAssignerPortrait(gfx, ALCHEMIST_PORTRAIT, x, y, h);
		int cardX = compactCardX(x, h);
		int cardW = compactCardWidth(w, h);
		gfx.fill(cardX, y, cardX + cardW, y + h, PANEL_DARK);
		ScreenDrawUtils.drawBorder(gfx, cardX, y, cardW, h, BORDER, BORDER_MUTED);
		renderEnzymeMasteryHeader(gfx, cardX + 8, y + 6, cardW - 16);
		drawProgressBar(gfx, cardX + 8, y + 31, cardW - 16, 7, progress, 8);
		renderTruncatedDescription(gfx, Component.translatable(enzymeMasteryComplete
						? "screen.hemomancy.harbinger_assignment_ledger.enzyme_mastery.complete"
						: "screen.hemomancy.harbinger_assignment_ledger.enzyme_mastery.desc"),
				cardX + 8, y + 42, Math.max(20, cardW - 16), enzymeMasteryComplete ? TEXT : MUTED, mouseX, mouseY);
	}

	private void renderLivingBestiary(GuiGraphics gfx, int x, int y, int w, int mouseX, int mouseY) {
		int total = Math.max(1, livingBestiaryTotal);
		int progress = Math.min(livingBestiaryCount, total);
		boolean done = livingBestiaryCount >= total;
		if (renderCollapsedAssignmentIfNeeded(gfx, AssignmentSection.LIVING_BESTIARY, x, y, w, ALCHEMIST_PORTRAIT,
				"screen.hemomancy.harbinger_assignment_ledger.living_bestiary.side_title",
				"screen.hemomancy.harbinger_assignment_ledger.living_bestiary.progress",
				progress, total, done)) {
			return;
		}
		int h = LIVING_BESTIARY_HEIGHT;
		renderAssignerPortrait(gfx, ALCHEMIST_PORTRAIT, x, y, h);
		int cardX = compactCardX(x, h);
		int cardW = compactCardWidth(w, h);
		gfx.fill(cardX, y, cardX + cardW, y + h, PANEL_DARK);
		ScreenDrawUtils.drawBorder(gfx, cardX, y, cardW, h, BORDER, BORDER_MUTED);
		renderLivingBestiaryHeader(gfx, cardX + 8, y + 6, cardW - 16, total);
		drawProgressBar(gfx, cardX + 8, y + 31, cardW - 16, 7, progress, total);
		renderTruncatedDescription(gfx,
				Component.translatable("screen.hemomancy.harbinger_assignment_ledger.living_bestiary.desc",
						morphlingLayerCount),
				cardX + 8, y + 42, Math.max(20, cardW - 16), MUTED, mouseX, mouseY);
	}

	private void renderLivingBestiaryHeader(GuiGraphics gfx, int x, int y, int w, int total) {
		renderGroupHeader(gfx, AssignmentSection.LIVING_BESTIARY, x, y, w,
				"screen.hemomancy.harbinger_assignment_ledger.living_bestiary.side_title",
				"screen.hemomancy.harbinger_assignment_ledger.living_bestiary.progress",
				livingBestiaryCount, total, livingBestiaryCount >= total);
	}

	private void renderEnzymeMasteryHeader(GuiGraphics gfx, int x, int y, int w) {
		renderGroupHeader(gfx, AssignmentSection.ENZYME_MASTERY, x, y, w,
				"screen.hemomancy.harbinger_assignment_ledger.enzyme_mastery.side_title",
				"screen.hemomancy.harbinger_assignment_ledger.enzyme_mastery.progress",
				enzymeMasteryCount, 8, enzymeMasteryComplete);
	}

	private void renderWovenVessel(GuiGraphics gfx, int x, int y, int w, int mouseX, int mouseY) {
		int progress = wovenVesselProgress();
		if (renderCollapsedAssignmentIfNeeded(gfx, AssignmentSection.WOVEN_VESSEL, x, y, w, MNEMONIST_PORTRAIT,
				"screen.hemomancy.harbinger_assignment_ledger.woven_vessel.title",
				"screen.hemomancy.harbinger_assignment_ledger.woven_vessel.progress",
				progress, 3, mnemonistFirstWeaveComplete)) {
			return;
		}
		int h = WOVEN_VESSEL_HEIGHT;
		renderAssignerPortrait(gfx, MNEMONIST_PORTRAIT, x, y, h);
		int cardX = compactCardX(x, h);
		int cardW = compactCardWidth(w, h);
		gfx.fill(cardX, y, cardX + cardW, y + h, PANEL_DARK);
		ScreenDrawUtils.drawBorder(gfx, cardX, y, cardW, h, BORDER, BORDER_MUTED);
		renderWovenVesselHeader(gfx, cardX + 8, y + 6, cardW - 16, progress);
		drawProgressBar(gfx, cardX + 8, y + 31, cardW - 16, 7, progress, 3);
		renderTruncatedDescription(gfx, Component.translatable(mnemonistFirstWeaveComplete
						? "screen.hemomancy.harbinger_assignment_ledger.woven_vessel.complete"
						: "screen.hemomancy.harbinger_assignment_ledger.woven_vessel.desc"),
				cardX + 8, y + 42, Math.max(20, cardW - 16), mnemonistFirstWeaveComplete ? TEXT : MUTED, mouseX, mouseY);
	}

	private void renderWovenVesselHeader(GuiGraphics gfx, int x, int y, int w, int progress) {
		renderGroupHeader(gfx, AssignmentSection.WOVEN_VESSEL, x, y, w,
				"screen.hemomancy.harbinger_assignment_ledger.woven_vessel.title",
				"screen.hemomancy.harbinger_assignment_ledger.woven_vessel.progress",
				progress, 3, mnemonistFirstWeaveComplete);
	}

	private int wovenVesselProgress() {
		int completed = 0;
		if (hasBlankHematicMemory) completed++;
		if (mnemonistWovenVesselComplete) completed++;
		if (mnemonistFirstWeaveComplete) completed++;
		return completed;
	}

	private void renderGroupHeader(GuiGraphics gfx, AssignmentSection section, int x, int y, int w, String titleKey, String progressKey,
			int progress, int total, boolean done) {
		Component progressText = Component.translatable(progressKey, progress, total);
		int progressX = x + w - font.width(progressText);
		int titleMaxW = Math.max(20, progressX - x - 8);
		String title = truncateWithEllipsis(Component.translatable(titleKey).getString(), titleMaxW);
		renderAssignmentLabel(gfx, section, x, y, done ? DONE : MUTED);
		gfx.drawString(font, title, x, y + 8, done ? DONE : HEADER, false);
		gfx.drawString(font, progressText, progressX, y + 8, done ? DONE : CURRENT, false);
	}

	private void renderAssignmentLabel(GuiGraphics gfx, AssignmentSection section, int x, int y, int color) {
		float scale = 0.75F;
		gfx.pose().pushPose();
		gfx.pose().scale(scale, scale, 1.0F);
		gfx.drawString(font, Component.translatable(section.category.labelKey, section.assignmentDegree),
				(int) (x / scale), (int) (y / scale), color, false);
		gfx.pose().popPose();
	}

	private void renderVeinMason(GuiGraphics gfx, int x, int y, int w, int mouseX, int mouseY) {
		int progress = veinMasonProgress();
		if (renderCollapsedAssignmentIfNeeded(gfx, AssignmentSection.VEIN_MASON, x, y, w, VEIN_MASON_PORTRAIT,
				"screen.hemomancy.harbinger_assignment_ledger.vein_mason.title",
				"screen.hemomancy.harbinger_assignment_ledger.vein_mason.progress",
				progress, 4, veinMasonFirstEffigyLoadout)) {
			return;
		}
		int h = VEIN_MASON_HEIGHT;
		renderAssignerPortrait(gfx, VEIN_MASON_PORTRAIT, x, y, h);
		int cardX = compactCardX(x, h);
		int cardW = compactCardWidth(w, h);
		gfx.fill(cardX, y, cardX + cardW, y + h, PANEL_DARK);
		ScreenDrawUtils.drawBorder(gfx, cardX, y, cardW, h, BORDER, BORDER_MUTED);
		renderVeinMasonHeader(gfx, cardX + 8, y + 6, cardW - 16, progress);
		drawProgressBar(gfx, cardX + 8, y + 31, cardW - 16, 7, progress, 4);
		renderTruncatedDescription(gfx, Component.translatable(veinMasonDescriptionKey()),
				cardX + 8, y + 42, Math.max(20, cardW - 16), veinMasonFirstEffigyLoadout ? TEXT : MUTED, mouseX, mouseY);
	}

	private void renderVeinMasonHeader(GuiGraphics gfx, int x, int y, int w, int progress) {
		renderGroupHeader(gfx, AssignmentSection.VEIN_MASON, x, y, w,
				"screen.hemomancy.harbinger_assignment_ledger.vein_mason.title",
				"screen.hemomancy.harbinger_assignment_ledger.vein_mason.progress",
				progress, 4, veinMasonFirstEffigyLoadout);
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

	private void renderCovenantWritten(GuiGraphics gfx, int x, int y, int w, int mouseX, int mouseY) {
		int progress = (foundedBloodline ? 1 : 0) + (foundingFaneEstablished ? 1 : 0);
		renderCompactChapter(gfx, AssignmentSection.COVENANT_WRITTEN, x, y, w, VICAR_PORTRAIT,
				"screen.hemomancy.harbinger_assignment_ledger.covenant_written.title",
				"screen.hemomancy.harbinger_assignment_ledger.covenant_written.progress",
				progress, 2, foundingFaneEstablished,
				foundingFaneEstablished
						? "screen.hemomancy.harbinger_assignment_ledger.covenant_written.complete"
						: "screen.hemomancy.harbinger_assignment_ledger.covenant_written.desc",
				mouseX, mouseY);
	}

	private void renderLivingCovenant(GuiGraphics gfx, int x, int y, int w, int mouseX, int mouseY) {
		int progress = (chamberReturned ? 1 : 0) + (covenantThroneBound ? 1 : 0)
				+ (covenantVigilCompleted ? 1 : 0);
		renderCompactChapter(gfx, AssignmentSection.LIVING_COVENANT, x, y, w, VICAR_PORTRAIT,
				"screen.hemomancy.harbinger_assignment_ledger.living_covenant.title",
				"screen.hemomancy.harbinger_assignment_ledger.living_covenant.progress",
				progress, 3, livingCovenantComplete,
				livingCovenantComplete
						? "screen.hemomancy.harbinger_assignment_ledger.living_covenant.complete"
						: "screen.hemomancy.harbinger_assignment_ledger.living_covenant.desc",
				mouseX, mouseY);
	}

	private void renderBloodBeneathBlood(GuiGraphics gfx, int x, int y, int w, int mouseX, int mouseY) {
		int progress = silentArchon ? 12 : Math.min(9, pomesConsumed)
				+ (qliphothCommunionComplete ? 1 : 0) + (silentPending ? 1 : 0)
				+ (severedPortalOpen ? 1 : 0);
		String descriptionKey;
		if (silentArchon) {
			descriptionKey = "screen.hemomancy.harbinger_assignment_ledger.blood_beneath.complete";
		} else if (severedPortalOpen) {
			descriptionKey = "screen.hemomancy.harbinger_assignment_ledger.blood_beneath.ordeal";
		} else if (silentPending) {
			descriptionKey = "screen.hemomancy.harbinger_assignment_ledger.blood_beneath.prune";
		} else if (qliphothCommunionComplete) {
			descriptionKey = "screen.hemomancy.harbinger_assignment_ledger.blood_beneath.revelation";
		} else {
			descriptionKey = "screen.hemomancy.harbinger_assignment_ledger.blood_beneath.pomes";
		}
		renderCompactChapter(gfx, AssignmentSection.BLOOD_BENEATH_BLOOD, x, y, w, VICAR_PORTRAIT,
				"screen.hemomancy.harbinger_assignment_ledger.blood_beneath.title",
				"screen.hemomancy.harbinger_assignment_ledger.blood_beneath.progress",
				progress, 12, silentArchon, descriptionKey, mouseX, mouseY);
	}

	private void renderCompactChapter(GuiGraphics gfx, AssignmentSection section, int x, int y, int w,
			ResourceLocation portrait, String titleKey, String progressKey, int progress, int total,
			boolean done, String descriptionKey, int mouseX, int mouseY) {
		if (renderCollapsedAssignmentIfNeeded(gfx, section, x, y, w, portrait,
				titleKey, progressKey, progress, total, done)) return;
		int h = section.expandedHeight;
		renderAssignerPortrait(gfx, portrait, x, y, h);
		int cardX = compactCardX(x, h);
		int cardW = compactCardWidth(w, h);
		gfx.fill(cardX, y, cardX + cardW, y + h, PANEL_DARK);
		ScreenDrawUtils.drawBorder(gfx, cardX, y, cardW, h, BORDER, BORDER_MUTED);
		renderGroupHeader(gfx, section, cardX + 8, y + 6, cardW - 16,
				titleKey, progressKey, progress, total, done);
		drawProgressBar(gfx, cardX + 8, y + 31, cardW - 16, 7, progress, total);
		renderTruncatedDescription(gfx, Component.translatable(descriptionKey),
				cardX + 8, y + 42, Math.max(20, cardW - 16), done ? TEXT : MUTED, mouseX, mouseY);
	}

	private void renderTheWornVow(GuiGraphics gfx, int x, int y, int w, int mouseX, int mouseY) {
		int progress = theWornVowProgress();
		if (renderCollapsedAssignmentIfNeeded(gfx, AssignmentSection.THE_WORN_VOW, x, y, w, ARTIFICER_PORTRAIT,
				"screen.hemomancy.harbinger_assignment_ledger.the_worn_vow.title",
				"screen.hemomancy.harbinger_assignment_ledger.the_worn_vow.progress",
				progress, 3, artificerHematicIronFitting)) {
			return;
		}
		renderCompactArtificerAssignment(gfx, AssignmentSection.THE_WORN_VOW, x, y, w,
				ARTIFICER_PORTRAIT,
				"screen.hemomancy.harbinger_assignment_ledger.the_worn_vow.title",
				"screen.hemomancy.harbinger_assignment_ledger.the_worn_vow.progress",
				progress, 3, artificerHematicIronFitting,
				theWornVowDescriptionKey(), mouseX, mouseY);
	}

	private void renderTheThreeAnswers(GuiGraphics gfx, int x, int y, int w, int mouseX, int mouseY) {
		int progress = theThreeAnswersProgress();
		if (renderCollapsedAssignmentIfNeeded(gfx, AssignmentSection.THE_THREE_ANSWERS, x, y, w, ARTIFICER_PORTRAIT,
				"screen.hemomancy.harbinger_assignment_ledger.the_three_answers.title",
				"screen.hemomancy.harbinger_assignment_ledger.the_three_answers.progress",
				progress, 2, artificerForkFitting)) {
			return;
		}
		renderCompactArtificerAssignment(gfx, AssignmentSection.THE_THREE_ANSWERS, x, y, w,
				ARTIFICER_PORTRAIT,
				"screen.hemomancy.harbinger_assignment_ledger.the_three_answers.title",
				"screen.hemomancy.harbinger_assignment_ledger.the_three_answers.progress",
				progress, 2, artificerForkFitting,
				theThreeAnswersDescriptionKey(), mouseX, mouseY);
	}

	private void renderCrimsonVestment(GuiGraphics gfx, int x, int y, int w, int mouseX, int mouseY) {
		int progress = crimsonVestmentProgress();
		if (renderCollapsedAssignmentIfNeeded(gfx, AssignmentSection.CRIMSON_VESTMENT, x, y, w, ARTIFICER_PORTRAIT,
				"screen.hemomancy.harbinger_assignment_ledger.crimson_vestment.title",
				"screen.hemomancy.harbinger_assignment_ledger.crimson_vestment.progress",
				progress, 3, artificerBloodLustFitting)) {
			return;
		}
		renderCompactArtificerAssignment(gfx, AssignmentSection.CRIMSON_VESTMENT, x, y, w,
				ARTIFICER_PORTRAIT,
				"screen.hemomancy.harbinger_assignment_ledger.crimson_vestment.title",
				"screen.hemomancy.harbinger_assignment_ledger.crimson_vestment.progress",
				progress, 3, artificerBloodLustFitting,
				crimsonVestmentDescriptionKey(), mouseX, mouseY);
	}

	private void renderWeightOfTheFrame(GuiGraphics gfx, int x, int y, int w, int mouseX, int mouseY) {
		int progress = weightOfTheFrameProgress();
		if (renderCollapsedAssignmentIfNeeded(gfx, AssignmentSection.WEIGHT_OF_THE_FRAME, x, y, w, ARTIFICER_PORTRAIT,
				"screen.hemomancy.harbinger_assignment_ledger.weight_of_the_frame.title",
				"screen.hemomancy.harbinger_assignment_ledger.weight_of_the_frame.progress",
				progress, 3, artificerD7Fitting)) {
			return;
		}
		renderCompactArtificerAssignment(gfx, AssignmentSection.WEIGHT_OF_THE_FRAME, x, y, w,
				ARTIFICER_PORTRAIT,
				"screen.hemomancy.harbinger_assignment_ledger.weight_of_the_frame.title",
				"screen.hemomancy.harbinger_assignment_ledger.weight_of_the_frame.progress",
				progress, 3, artificerD7Fitting,
				weightOfTheFrameDescriptionKey(), mouseX, mouseY);
	}

	private void renderTheAssumedLimb(GuiGraphics gfx, int x, int y, int w, int mouseX, int mouseY) {
		int progress = theAssumedLimbProgress();
		if (renderCollapsedAssignmentIfNeeded(gfx, AssignmentSection.THE_ASSUMED_LIMB, x, y, w, ARTIFICER_PORTRAIT,
				"screen.hemomancy.harbinger_assignment_ledger.the_assumed_limb.side_title",
				"screen.hemomancy.harbinger_assignment_ledger.the_assumed_limb.progress",
				progress, 3, artificerLivingArsenalFitting)) {
			return;
		}
		renderCompactArtificerAssignment(gfx, AssignmentSection.THE_ASSUMED_LIMB, x, y, w,
				ARTIFICER_PORTRAIT,
				"screen.hemomancy.harbinger_assignment_ledger.the_assumed_limb.side_title",
				"screen.hemomancy.harbinger_assignment_ledger.the_assumed_limb.progress",
				progress, 3, artificerLivingArsenalFitting,
				theAssumedLimbDescriptionKey(), mouseX, mouseY);
	}

	private void renderCompactArtificerAssignment(GuiGraphics gfx, AssignmentSection section, int x, int y, int w,
			ResourceLocation assignerPortrait, String titleKey,
			String progressKey, int progress, int total, boolean done, String descriptionKey, int mouseX, int mouseY) {
		int h = THE_WORN_VOW_HEIGHT;
		renderAssignerPortrait(gfx, assignerPortrait, x, y, h);
		int cardX = compactCardX(x, h);
		int cardW = compactCardWidth(w, h);
		gfx.fill(cardX, y, cardX + cardW, y + h, PANEL_DARK);
		ScreenDrawUtils.drawBorder(gfx, cardX, y, cardW, h, BORDER, BORDER_MUTED);
		renderGroupHeader(gfx, section, cardX + 8, y + 6, cardW - 16,
				titleKey, progressKey, progress, total, done);
		drawProgressBar(gfx, cardX + 8, y + 31, cardW - 16, 7, progress, total);
		renderTruncatedDescription(gfx, Component.translatable(descriptionKey,
						Math.min(artificerLivingWeaponFormCount, 7), 7),
				cardX + 8, y + 42, Math.max(20, cardW - 16), done ? TEXT : MUTED, mouseX, mouseY);
	}

	private int theWornVowProgress() {
		int completed = 0;
		if (artificerArmaturePlaced) completed++;
		if (artificerFirstHematicUpgrade) completed++;
		if (artificerHematicIronFitting) completed++;
		return completed;
	}

	private int theThreeAnswersProgress() {
		int completed = 0;
		if (artificerFirstForkUpgrade) completed++;
		if (artificerForkFitting) completed++;
		return completed;
	}

	private int crimsonVestmentProgress() {
		int completed = 0;
		if (artificerFrameConsecrated) completed++;
		if (artificerFirstBloodLustUpgrade) completed++;
		if (artificerBloodLustFitting) completed++;
		return completed;
	}

	private int weightOfTheFrameProgress() {
		int completed = 0;
		if (artificerMonolithicFrame) completed++;
		if (artificerFirstD7Upgrade) completed++;
		if (artificerD7Fitting) completed++;
		return completed;
	}

	private int theAssumedLimbProgress() {
		int completed = 0;
		if (artificerFirstLivingGraft) completed++;
		if (artificerLivingWeaponFormCount >= 7) completed++;
		if (artificerLivingArsenalFitting) completed++;
		return completed;
	}

	private String theWornVowDescriptionKey() {
		if (artificerHematicIronFitting) {
			return "screen.hemomancy.harbinger_assignment_ledger.the_worn_vow.complete";
		}
		if (!artificerArmaturePlaced) {
			return "screen.hemomancy.harbinger_assignment_ledger.the_worn_vow.place";
		}
		if (!artificerFirstHematicUpgrade) {
			return "screen.hemomancy.harbinger_assignment_ledger.the_worn_vow.upgrade";
		}
		return "screen.hemomancy.harbinger_assignment_ledger.the_worn_vow.return";
	}

	private String theThreeAnswersDescriptionKey() {
		if (artificerForkFitting) {
			return "screen.hemomancy.harbinger_assignment_ledger.the_three_answers.complete";
		}
		if (!artificerFirstForkUpgrade) {
			return "screen.hemomancy.harbinger_assignment_ledger.the_three_answers.upgrade";
		}
		return "screen.hemomancy.harbinger_assignment_ledger.the_three_answers.return";
	}

	private String crimsonVestmentDescriptionKey() {
		if (artificerBloodLustFitting) {
			return "screen.hemomancy.harbinger_assignment_ledger.crimson_vestment.complete";
		}
		if (!artificerFrameConsecrated) {
			return "screen.hemomancy.harbinger_assignment_ledger.crimson_vestment.consecrate";
		}
		if (!artificerFirstBloodLustUpgrade) {
			return "screen.hemomancy.harbinger_assignment_ledger.crimson_vestment.upgrade";
		}
		return "screen.hemomancy.harbinger_assignment_ledger.crimson_vestment.return";
	}

	private String weightOfTheFrameDescriptionKey() {
		if (artificerD7Fitting) {
			return "screen.hemomancy.harbinger_assignment_ledger.weight_of_the_frame.complete";
		}
		if (!artificerMonolithicFrame) {
			return "screen.hemomancy.harbinger_assignment_ledger.weight_of_the_frame.cornerstone";
		}
		if (!artificerFirstD7Upgrade) {
			return "screen.hemomancy.harbinger_assignment_ledger.weight_of_the_frame.upgrade";
		}
		return "screen.hemomancy.harbinger_assignment_ledger.weight_of_the_frame.return";
	}

	private String theAssumedLimbDescriptionKey() {
		if (artificerLivingArsenalFitting) {
			return "screen.hemomancy.harbinger_assignment_ledger.the_assumed_limb.complete";
		}
		if (!artificerFirstLivingGraft) {
			return "screen.hemomancy.harbinger_assignment_ledger.the_assumed_limb.first";
		}
		if (artificerLivingWeaponFormCount < 7) {
			return "screen.hemomancy.harbinger_assignment_ledger.the_assumed_limb.forms";
		}
		return "screen.hemomancy.harbinger_assignment_ledger.the_assumed_limb.return";
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
		if (artificerHematicIronFitting) {
			completed++;
		}
		if (artificerForkFitting) {
			completed++;
		}
		if (artificerBloodLustFitting) {
			completed++;
		}
		if (artificerD7Fitting) {
			completed++;
		}
		if (artificerLivingArsenalFitting) {
			completed++;
		}
		return completed;
	}

	private int totalAssignmentContentHeight() {
		int total = 0;
		for (AssignmentSection section : ORDERED_ASSIGNMENT_SECTIONS) {
			if (total > 0) {
				total += SECTION_GAP;
			}
			total += assignmentHeight(section);
		}
		return total;
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

	private int bodyAnswersProgress() {
		int completed = 0;
		if (bodyAnswersBriefed) completed++;
		if (bodyAnswersComplete) completed++;
		if (muscleMemoryCount >= 8) completed++;
		return completed;
	}

	private int clampAssignmentScroll(int scroll) {
		return Math.max(0, Math.min(scroll, assignmentMaxScroll));
	}

	private boolean isOverAssignmentPanel(double mx, double my) {
		return mx >= contentLeft() && mx <= contentRight()
				&& my >= assignmentListY() && my <= contentBottom();
	}

	private int assignmentListY() {
		return contentTop() + ASSIGNMENT_LIST_TOP_OFFSET;
	}

	private int assignmentListHeight() {
		return Math.max(38, contentBottom() - assignmentListY());
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
