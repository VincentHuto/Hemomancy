package com.vincenthuto.hemomancy.client.screen.item;

import com.vincenthuto.hemomancy.client.data.BloodlinePetitionClientData;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineSavedData;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketAnswerBloodlinePetition;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketLedgerAction;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncBloodlinePetitions.Petition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

import com.vincenthuto.hemomancy.client.screen.skilltree.harbinger.VeinBackgroundRenderer;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.ScreenDrawUtils;
import com.vincenthuto.hemomancy.common.util.HemoText;

import java.util.List;

/**
	 * The Ancestral Ledger screen provides GUI buttons for bloodline fane actions:
 * <ul>
	 *   <li>Summon recruited NPC Harbingers (within the Founding Fane)</li>
	 *   <li>Recall to the fane recall point (from anywhere)</li>
	 *   <li>Set the fane recall point to current position (leader only, within the Founding Fane)</li>
 *   <li>Disband the line (leader) or leave it (member, a second click confirms)</li>
 *   <li>The leader's petition row: step through waiting petitions, Admit or Refuse (PT-35)</li>
 * </ul>
 * Matches the visual style of BloodlinePoolScreen with procedural vein background.
 */
public class LedgerScreen extends Screen {

	private static final int GUI_WIDTH = 200;
	private static final int GUI_HEIGHT = 190;
	/** The leader's panel is taller by the petition row. */
	private static final int LEADER_GUI_HEIGHT = 244;

	private static final VeinBackgroundRenderer.Style LEDGER_VEINS = new VeinBackgroundRenderer.Style(
			VeinBackgroundRenderer.Field.STANDARD.count(18).seed(77L), VeinBackgroundRenderer.Palette.CRIMSON,
			VeinBackgroundRenderer.Speckles.Scattered.CRIMSON.count(80));
	private final VeinBackgroundRenderer veins = new VeinBackgroundRenderer(LEDGER_VEINS);
	private Button setRecallButton;
	private Button disbandButton;
	private Button leaveButton;
	private Button previousButton;
	private Button admitButton;
	private Button refuseButton;
	private Button nextButton;
	private boolean leaveArmed;
	private int petitionIndex;
	/** Whether the widgets were laid out for the leader's taller panel. */
	private boolean leaderLayout;

	public LedgerScreen() {
		super(Component.translatable("screen.hemomancy.ledger.ancestral_ledger"));
	}

	public static void openScreen() {
		Minecraft.getInstance().setScreen(new LedgerScreen());
	}

	@Override
	protected void init() {
		super.init();
		leaderLayout = isLeader();

		int centerX = this.width / 2;
		int guiLeft = centerX - GUI_WIDTH / 2;
		int guiTop = this.height / 2 - panelHeight() / 2;

		int btnW = GUI_WIDTH - 24;
		int btnX = guiLeft + 12;
		int y = guiTop + 70;

		// ── Summon NPCs Button ──
		addRenderableWidget(Button.builder(
				Component.translatable("screen.hemomancy.ledger.summon_harbingers"),
				btn -> {
					PacketHandler.sendToServer(
							new PacketLedgerAction(PacketLedgerAction.ACTION_SUMMON_NPCS));
					onClose();
				}).bounds(btnX, y, btnW, 20).build());

		y += 28;

		// ── Recall to Fane Button ──
		addRenderableWidget(Button.builder(
				Component.translatable("screen.hemomancy.ledger.recall_to_fane"),
				btn -> {
					PacketHandler.sendToServer(
							new PacketLedgerAction(PacketLedgerAction.ACTION_RECALL_TO_LODGE));
					onClose();
				}).bounds(btnX, y, btnW, 20).build());

		y += 28;

		// ── Set Fane Recall Point Button (leader only) ──
		setRecallButton = addRenderableWidget(Button.builder(
				Component.translatable("screen.hemomancy.ledger.set_fane_recall"),
				btn -> {
					PacketHandler.sendToServer(
							new PacketLedgerAction(PacketLedgerAction.ACTION_SET_RECALL_POINT));
					onClose();
				}).bounds(btnX, y, btnW, 20).build());

		y += 28;

		// ── Disband (leader) or Leave (member) in the same place ──
		disbandButton = addRenderableWidget(Button.builder(
				Component.translatable("screen.hemomancy.ledger.disband_bloodline"),
				btn -> {
					PacketHandler.sendToServer(
							new PacketLedgerAction(PacketLedgerAction.ACTION_DISBAND_BLOODLINE));
					onClose();
				}).bounds(btnX, y, btnW, 20).build());
		leaveButton = addRenderableWidget(Button.builder(
				Component.translatable(leaveArmed ? "screen.hemomancy.ledger.leave_confirm"
						: "screen.hemomancy.ledger.leave_bloodline"),
				btn -> {
					if (!leaveArmed) {
						leaveArmed = true;
						btn.setMessage(Component.translatable("screen.hemomancy.ledger.leave_confirm"));
						return;
					}
					PacketHandler.sendToServer(
							new PacketLedgerAction(PacketLedgerAction.ACTION_LEAVE_BLOODLINE));
					onClose();
				}).bounds(btnX, y, btnW, 20).build());

		// ── The leader's petition row (NQ-11-7) ──
		int rowY = guiTop + 214;
		int answerW = (btnW - 2 * 20 - 4 * 4) / 2;
		previousButton = addRenderableWidget(Button.builder(Component.translatable("screen.hemomancy.ledger.petition_previous"),
				btn -> step(-1)).bounds(btnX, rowY, 20, 20).build());
		admitButton = addRenderableWidget(Button.builder(Component.translatable("screen.hemomancy.ledger.admit"),
				btn -> answer(true)).bounds(btnX + 24, rowY, answerW, 20).build());
		refuseButton = addRenderableWidget(Button.builder(Component.translatable("screen.hemomancy.ledger.refuse"),
				btn -> answer(false)).bounds(btnX + 28 + answerW, rowY, answerW, 20).build());
		nextButton = addRenderableWidget(Button.builder(Component.translatable("screen.hemomancy.ledger.petition_next"),
				btn -> step(1)).bounds(btnX + btnW - 20, rowY, 20, 20).build());
		updateRoleControls();
	}

	@Override
	public void tick() {
		super.tick();
		if (isLeader() != leaderLayout) rebuildWidgets();
		updateRoleControls();
	}

	private int panelHeight() {
		return leaderLayout ? LEADER_GUI_HEIGHT : GUI_HEIGHT;
	}

	private static boolean isLeader() {
		LocalPlayer player = Minecraft.getInstance().player;
		return player != null && HemoCapabilityAccess.getBloodVolume(player).getBloodLine().canManage(player.getUUID());
	}

	private void updateRoleControls() {
		setRecallButton.active = leaderLayout;
		setRecallButton.setTooltip(leaderLayout ? null : Tooltip.create(Component.translatable("hemomancy.ledger.recall.not_leader")));
		disbandButton.visible = leaderLayout;
		// The leader cannot leave (NQ-11-6): the Disband button says so.
		disbandButton.setTooltip(Tooltip.create(Component.translatable("hemomancy.bloodline.leave.leader")));
		leaveButton.visible = !leaderLayout;
		List<Petition> petitions = BloodlinePetitionClientData.petitions();
		petitionIndex = petitions.isEmpty() ? 0 : Math.floorMod(petitionIndex, petitions.size());
		for (Button button : List.of(previousButton, admitButton, refuseButton, nextButton)) {
			button.visible = leaderLayout;
		}
		admitButton.active = !petitions.isEmpty();
		refuseButton.active = !petitions.isEmpty();
		previousButton.active = petitions.size() > 1;
		nextButton.active = petitions.size() > 1;
	}

	private void step(int by) {
		int count = BloodlinePetitionClientData.petitions().size();
		if (count > 0) petitionIndex = Math.floorMod(petitionIndex + by, count);
	}

	private void answer(boolean admit) {
		List<Petition> petitions = BloodlinePetitionClientData.petitions();
		if (petitions.isEmpty()) return;
		PacketHandler.sendToServer(new PacketAnswerBloodlinePetition(petitions.get(petitionIndex).applicant(), admit));
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

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		int panelHeight = panelHeight();
		int centerX = this.width / 2;
		int centerY = this.height / 2;
		int guiLeft = centerX - GUI_WIDTH / 2;
		int guiTop = centerY - panelHeight / 2;

		// Animated vein background
		veins.render(graphics, guiLeft, guiTop, GUI_WIDTH, panelHeight);

		// Border
		ScreenDrawUtils.drawBorder(graphics, guiLeft, guiTop, GUI_WIDTH, panelHeight, 0xFF330808, 0xFF220606);

		// Title
		graphics.drawCenteredString(this.font, this.title, centerX, guiTop + 8, 0xFFCC3344);

		// Bloodline info
		LocalPlayer player = Minecraft.getInstance().player;
		if (player != null) {
			var volume = HemoCapabilityAccess.getBloodVolume(player);
			Bloodline bloodline = volume.getBloodLine();
			if (bloodline.isValid()) {
				graphics.drawCenteredString(this.font,
						bloodline.displayName().copy()
								.withStyle(s -> s.withColor(0xFFDD6666).withBold(true)),
						centerX, guiTop + 24, 0xFFFFFFFF);

				int npcCount = bloodline.getNpcMemberCount();
				int memberCount = bloodline.getPlayerUUIDS().size();
				graphics.drawCenteredString(this.font,
						HemoText.translatable("screen.hemomancy.ledger.members_recruits", memberCount, npcCount),
						centerX, guiTop + 38, 0xFFAAAAAA);

				Component role = Component.translatable(leaderLayout ? "screen.hemomancy.ledger.role.leader"
						: "screen.hemomancy.ledger.role.member");
				graphics.drawCenteredString(this.font,
						Component.translatable("screen.hemomancy.ledger.role", role),
						centerX, guiTop + 52, 0xFF888888);
				if (leaderLayout) renderPetitions(graphics, centerX, guiTop + 186);
			} else {
				graphics.drawCenteredString(this.font,
						Component.translatable("screen.hemomancy.ledger.no_bloodline_sign_the_ledger_first"),
						centerX, guiTop + 40, 0xFFAA4444);
			}
		}

		// Render widgets on top
		super.render(graphics, mouseX, mouseY, partialTick);
	}

	private void renderPetitions(GuiGraphics graphics, int centerX, int y) {
		List<Petition> petitions = BloodlinePetitionClientData.petitions();
		if (petitions.isEmpty()) {
			graphics.drawCenteredString(this.font, Component.translatable("screen.hemomancy.ledger.petition_none"),
					centerX, y + 6, 0xFF888888);
			return;
		}
		graphics.drawCenteredString(this.font, Component.translatable("screen.hemomancy.ledger.petitions",
				petitionIndex + 1, petitions.size(), BloodlineSavedData.MAX_PETITIONS), centerX, y, 0xFFAAAAAA);
		graphics.drawCenteredString(this.font, Component.literal(petitions.get(petitionIndex).name())
				.withStyle(s -> s.withColor(0xFFDD6666)), centerX, y + 12, 0xFFFFFFFF);
	}

}
