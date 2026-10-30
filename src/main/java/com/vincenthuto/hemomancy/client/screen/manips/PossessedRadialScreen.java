package com.vincenthuto.hemomancy.client.screen.manips;

import com.vincenthuto.hemomancy.client.event.PossessionClientEvents;
import com.vincenthuto.hemomancy.client.registry.KeyMappings;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.screen.radial.GenericRadialMenu;
import com.vincenthuto.hemomancy.client.screen.radial.IRadialMenuHost;
import com.vincenthuto.hemomancy.client.screen.radial.RadialMenuItem;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.possession.Possession;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.possession.PossessionAction;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.possession.PossessionRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.possession.PossessionWheelRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.possession.SurrogateForm;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PossessionActionPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The manipulation wheel while a will is away: the visit's controls on the inner ring, the borrowed body's gifts on
 * the outer ring, and in the centre the body itself turning slowly, its name and the Concentration left. Released
 * like the manipulation wheel; every choice goes to the server as a {@link PossessionActionPacket}.
 */
public class PossessedRadialScreen extends Screen {
	private static final int NAME = 0xFFE2CEC8;
	private static final int FULL = 0xFFB8A0E0;
	private static final int SHELL = 0xFFD8D4CC;
	private static final float CENTRE = 36f;

	private final Minecraft mc = Minecraft.getInstance();
	private final GenericRadialMenu menu;
	/** Whether releasing the wheel key chooses the hovered slice; a review keeps the wheel open without the key. */
	private final boolean followsKey;
	private SurrogateForm built;

	public PossessedRadialScreen() {
		this(true);
	}

	public PossessedRadialScreen(boolean followsKey) {
		super(Component.translatable("screen.hemomancy.possession_wheel"));
		menu = new GenericRadialMenu(mc, new IRadialMenuHost() {
			@Override
			public void renderTooltip(GuiGraphics graphics, ItemStack stack, int mouseX, int mouseY) {
				graphics.renderTooltip(font, stack, mouseX, mouseY);
			}

			@Override
			public Screen getScreen() {
				return PossessedRadialScreen.this;
			}

			@Override
			public Font getFontRenderer() {
				return font;
			}
		}) {
			@Override
			public void onClickOutside() {
				close();
			}
		};
		menu.setRadii(CENTRE, 62f, 68f, 104f);
		// Violet slices that still read against the Gardens' dark; the manipulation wheel's black ones vanish there.
		menu.backgroundColor = 0x7A140C1C;
		menu.backgroundColorHover = 0x8A3A2A58;
		this.followsKey = followsKey;
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		menu.clickItem();
		return super.mouseReleased(mouseX, mouseY, button);
	}

	private Possession possession() {
		return HemoCapabilityAccess.getPossession(mc.player);
	}

	private void build(SurrogateForm form) {
		menu.clear();
		List<RadialMenuItem> inner = new ArrayList<>();
		for (PossessionAction action : PossessionWheelRules.visit(form)) inner.add(item(form, action));
		List<RadialMenuItem> outer = new ArrayList<>();
		for (PossessionAction action : PossessionWheelRules.body(form)) outer.add(item(form, action));
		menu.addAllInner(inner);
		menu.addAll(outer);
		built = form;
	}

	private PossessionRadialMenuItem item(SurrogateForm form, PossessionAction action) {
		PossessionRadialMenuItem item = new PossessionRadialMenuItem(menu, action, () -> resting(form, action),
				() -> possession().toggled(action), () -> tooltip(form, action), () -> {
					PacketHandler.sendToServer(PossessionActionPacket.of(action));
					menu.close();
				});
		item.setVisible(true);
		return item;
	}

	/** The share of a rest still to run: a gift's cooldown, or the return hold while it draws the will home. */
	private double resting(SurrogateForm form, PossessionAction action) {
		Possession possession = possession();
		long now = mc.level.getGameTime();
		if (action == PossessionAction.RETURN) {
			return possession.returning()
					? (possession.returnAt() - now) / (double) PossessionRules.RETURN_HOLD_TICKS : 0.0;
		}
		int total = PossessionWheelRules.cooldownTicks(form, action);
		long left = possession.readyAt(action) - now;
		return total <= 0 || left <= 0 ? 0.0 : left / (double) total;
	}

	private List<Component> tooltip(SurrogateForm form, PossessionAction action) {
		List<Component> lines = new ArrayList<>();
		Component name = Component.translatable("possession_action.hemomancy." + action.id());
		long left = possession().readyAt(action) - mc.level.getGameTime();
		lines.add(left > 0 ? Component.translatable("screen.hemomancy.possession_wheel.cooling", name,
				(left + 19L) / 20L).withStyle(ChatFormatting.DARK_PURPLE) : name);
		lines.add(Component.translatable("possession_action.hemomancy." + action.id() + ".desc")
				.withStyle(ChatFormatting.GRAY));
		if (PossessionWheelRules.isToggle(action)) {
			lines.add(Component.translatable(possession().toggled(action) ? "screen.hemomancy.possession_wheel.on"
					: "screen.hemomancy.possession_wheel.off").withStyle(ChatFormatting.DARK_GRAY));
		}
		return lines;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		super.render(graphics, mouseX, mouseY, partialTicks);
		if (mc.player == null) return;
		SurrogateForm form = possession().form();
		if (form == null) return;
		if (form != built) build(form);
		menu.draw(graphics, partialTicks, mouseX, mouseY);
		if (menu.isReady()) drawCentre(graphics, form, partialTicks);
	}

	/** The body turning in the centre, its name above it and the Concentration left below. */
	private void drawCentre(GuiGraphics graphics, SurrogateForm form, float partialTicks) {
		int x = width / 2;
		int y = height / 2;
		LivingEntity body = PossessionClientEvents.wheelBody(mc.player);
		if (body != null) drawBody(graphics, body, x, y + 4, partialTicks);

		Component name = Component.translatable("surrogate.hemomancy." + form.name().toLowerCase(Locale.ROOT));
		drawSmall(graphics, name, x, y - (int) CENTRE + 8, NAME);

		Possession possession = possession();
		Component count = Component.translatable("screen.hemomancy.possession_wheel.concentration",
				Mth.ceil(possession.concentration()), Mth.ceil(possession.max()));
		int countWidth = (int) (font.width(count) * 0.75f);
		int shell = possession.shell();
		int pips = shell * 4;
		drawSmall(graphics, count, x - pips / 2, y + (int) CENTRE - 14, FULL);
		for (int i = 0; i < shell; i++) {
			int left = x - pips / 2 + countWidth / 2 + 3 + i * 4;
			graphics.fill(left - 1, y + (int) CENTRE - 15, left + 3, y + (int) CENTRE - 11, 0xFF0C0810);
			graphics.fill(left, y + (int) CENTRE - 14, left + 2, y + (int) CENTRE - 12, SHELL);
		}
	}

	private void drawSmall(GuiGraphics graphics, Component text, int x, int y, int colour) {
		var pose = graphics.pose();
		pose.pushPose();
		pose.translate(x, y, 200);
		pose.scale(0.75f, 0.75f, 1f);
		graphics.drawString(font, text, -font.width(text) / 2, 0, colour, true);
		pose.popPose();
	}

	private static void drawBody(GuiGraphics graphics, LivingEntity body, int x, int y, float partialTicks) {
		float spin = AnimationTime.gameTime(body.level(), partialTicks) * 2f % 360f;
		float size = Math.max(body.getBbHeight(), body.getBbWidth());
		float scale = 30f / Math.max(0.5f, size);
		float bodyRot = body.yBodyRot, bodyRotO = body.yBodyRotO, yRot = body.getYRot(), yRotO = body.yRotO;
		float xRot = body.getXRot(), xRotO = body.xRotO, head = body.yHeadRot, headO = body.yHeadRotO;
		body.yBodyRot = body.yBodyRotO = 180f + spin;
		body.setYRot(180f + spin);
		body.yRotO = body.getYRot();
		body.setXRot(0f);
		body.xRotO = 0f;
		body.yHeadRot = body.yHeadRotO = body.getYRot();
		InventoryScreen.renderEntityInInventory(graphics, x, y, scale,
				new Vector3f(0f, body.getBbHeight() / 2f, 0f), new Quaternionf().rotateZ((float) Math.PI), null, body);
		body.yBodyRot = bodyRot;
		body.yBodyRotO = bodyRotO;
		body.setYRot(yRot);
		body.yRotO = yRotO;
		body.setXRot(xRot);
		body.xRotO = xRotO;
		body.yHeadRot = head;
		body.yHeadRotO = headO;
	}

	@Override
	public void tick() {
		super.tick();
		menu.tick();
		if (menu.isClosed() || mc.player == null || !HemoCapabilityAccess.isPossessing(mc.player)) {
			mc.setScreen(null);
			return;
		}
		if (followsKey && menu.isReady() && !KeyMappings.isKeyDown(KeyMappings.openVascCharmMenu)) menu.clickItem();
	}
}
