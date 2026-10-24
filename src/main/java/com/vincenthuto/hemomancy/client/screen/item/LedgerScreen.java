package com.vincenthuto.hemomancy.client.screen.item;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketLedgerAction;
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

/**
	 * The Ancestral Ledger screen provides GUI buttons for bloodline fane actions:
 * <ul>
	 *   <li>Summon recruited NPC Harbingers (within the Founding Fane)</li>
	 *   <li>Recall to the fane recall point (from anywhere)</li>
	 *   <li>Set the fane recall point to current position (leader only, within the Founding Fane)</li>
 * </ul>
 * Matches the visual style of BloodlinePoolScreen with procedural vein background.
 */
public class LedgerScreen extends Screen {

	private static final int GUI_WIDTH = 200;
	private static final int GUI_HEIGHT = 190;

	private static final VeinBackgroundRenderer.Style LEDGER_VEINS = new VeinBackgroundRenderer.Style(
			VeinBackgroundRenderer.Field.STANDARD.count(18).seed(77L), VeinBackgroundRenderer.Palette.CRIMSON,
			VeinBackgroundRenderer.Speckles.Scattered.CRIMSON.count(80));
	private final VeinBackgroundRenderer veins = new VeinBackgroundRenderer(LEDGER_VEINS);
	private Button setRecallButton;
	private Button disbandButton;

	public LedgerScreen() {
		super(Component.translatable("screen.hemomancy.ledger.ancestral_ledger"));
	}

	public static void openScreen() {
		Minecraft.getInstance().setScreen(new LedgerScreen());
	}

	@Override
	protected void init() {
		super.init();

		int centerX = this.width / 2;
		int guiLeft = centerX - GUI_WIDTH / 2;
		int guiTop = this.height / 2 - GUI_HEIGHT / 2;

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

		// ── Disband Bloodline Button (leader only) ──
		disbandButton = addRenderableWidget(Button.builder(
				Component.translatable("screen.hemomancy.ledger.disband_bloodline"),
				btn -> {
					PacketHandler.sendToServer(
							new PacketLedgerAction(PacketLedgerAction.ACTION_DISBAND_BLOODLINE));
					onClose();
				}).bounds(btnX, y, btnW, 20).build());
		updateRoleControls();
	}

	@Override
	public void tick() {
		super.tick();
		updateRoleControls();
	}

	private void updateRoleControls() {
		LocalPlayer player = Minecraft.getInstance().player;
		boolean canManage = player != null && HemoCapabilityAccess.getBloodVolume(player).getBloodLine().canManage(player.getUUID());
		setRecallButton.active = canManage;
		disbandButton.active = canManage;
		setRecallButton.setTooltip(canManage ? null : Tooltip.create(Component.translatable("hemomancy.ledger.recall.not_leader")));
		disbandButton.setTooltip(canManage ? null : Tooltip.create(Component.translatable("hemomancy.ledger.disband.not_leader")));
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
		int centerX = this.width / 2;
		int centerY = this.height / 2;
		int guiLeft = centerX - GUI_WIDTH / 2;
		int guiTop = centerY - GUI_HEIGHT / 2;

		// Animated vein background
		veins.render(graphics, guiLeft, guiTop, GUI_WIDTH, GUI_HEIGHT);

		// Border
		ScreenDrawUtils.drawBorder(graphics, guiLeft, guiTop, GUI_WIDTH, GUI_HEIGHT, 0xFF330808, 0xFF220606);

		// Title
		graphics.drawCenteredString(this.font, this.title, centerX, guiTop + 8, 0xFFCC3344);

		// Bloodline info
		LocalPlayer player = Minecraft.getInstance().player;
		if (player != null) {
			var volume = HemoCapabilityAccess.getBloodVolume(player);
			Bloodline bloodline = volume.getBloodLine();
			if (bloodline.isValid()) {
				graphics.drawCenteredString(this.font,
						Component.literal(bloodline.getName())
								.withStyle(s -> s.withColor(0xFFDD6666).withBold(true)),
						centerX, guiTop + 24, 0xFFFFFFFF);

				int npcCount = bloodline.getNpcMemberCount();
				int memberCount = bloodline.getPlayerUUIDS().size();
				graphics.drawCenteredString(this.font,
						HemoText.translatable("screen.hemomancy.ledger.members_recruits", memberCount, npcCount),
						centerX, guiTop + 38, 0xFFAAAAAA);

				boolean isLeader = bloodline.getLeaderUUID().equals(player.getUUID());
				String role = isLeader ? "Leader" : "Member";
				graphics.drawCenteredString(this.font,
						HemoText.translatable("screen.hemomancy.ledger.role", role),
						centerX, guiTop + 52, 0xFF888888);
			} else {
				graphics.drawCenteredString(this.font,
						Component.translatable("screen.hemomancy.ledger.no_bloodline_sign_the_ledger_first"),
						centerX, guiTop + 40, 0xFFAA4444);
			}
		}

		// Render widgets on top
		super.render(graphics, mouseX, mouseY, partialTick);
	}


}
