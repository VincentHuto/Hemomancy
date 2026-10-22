package com.vincenthuto.hemomancy.client;

import java.util.List;
import java.util.Optional;

import com.vincenthuto.hemomancy.client.data.ActiveRiteClientData;
import com.vincenthuto.hemomancy.client.render.item.harbinger.CellHandParticleEffects;
import com.vincenthuto.hemomancy.client.render.world.MnemonicBlueprintRenderer;
import com.vincenthuto.hemomancy.client.render.world.zone.RiteSpaceClientData;
import com.vincenthuto.hemomancy.client.screen.item.BloodStructureHintScreen;
import com.vincenthuto.hemomancy.client.screen.item.BloodlinePoolScreen;
import com.vincenthuto.hemomancy.client.screen.item.LedgerScreen;
import com.vincenthuto.hemomancy.client.screen.item.RiteHintScreen;
import com.vincenthuto.hemomancy.client.screen.item.VascularStatusScreen;
import com.vincenthuto.hemomancy.client.screen.item.guide.BloodStructureBookRenderer;
import com.vincenthuto.hemomancy.client.screen.item.guide.HemoBloodStructureGuidePage;
import com.vincenthuto.hemomancy.client.screen.skilltree.harbinger.HarbingerProgressScreen;
import com.vincenthuto.hemomancy.client.screen.skilltree.unstained.UnstainedProgressScreen;
import com.vincenthuto.hemomancy.client.screen.tile.crafting.scar.ScreenScarBinderViewer;
import com.vincenthuto.hemomancy.client.screen.tile.functional.SynapticLoadoutScreen;
import com.vincenthuto.hemomancy.common.item.shared.MnemonicBlueprintTarget;
import com.vincenthuto.hemomancy.common.rite.CardinalRitePurpose;
import com.vincenthuto.hemomancy.common.rite.harbinger.ActiveRiteData;
import com.vincenthuto.hemomancy.common.util.ClientBridge;
import com.vincenthuto.hutoslib.client.screen.guide.BookBodyRenderer;
import com.vincenthuto.hutoslib.common.data.book.BookCodeModel;
import com.vincenthuto.hutoslib.common.data.book.ChapterTemplate;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** The client's answers to {@link ClientBridge}, installed by {@code ClientSetupRegistrar}. */
public final class ClientBridgeImpl implements ClientBridge {

	@Override
	public Player localPlayer() {
		return Minecraft.getInstance().player;
	}

	@Override
	public Level clientLevel() {
		return Minecraft.getInstance().level;
	}

	@Override
	public boolean hasShiftDown() {
		return Screen.hasShiftDown();
	}

	@Override
	public String translate(String key) {
		return I18n.get(key);
	}

	@Override
	public Optional<CardinalRitePurpose> riteSpacePurposeAt(BlockPos pos) {
		return RiteSpaceClientData.purposeAt(pos);
	}

	@Override
	public List<ActiveRiteData.RiteEntry> activeRites() {
		return ActiveRiteClientData.getActiveRites();
	}

	@Override
	public void openHarbingerProgress() {
		HarbingerProgressScreen.openScreen();
	}

	@Override
	public void openSynapticLoadout(BlockPos pos) {
		SynapticLoadoutScreen.openScreen(pos);
	}

	@Override
	public void openBloodlinePool() {
		BloodlinePoolScreen.openScreen();
	}

	@Override
	public void openLedger() {
		LedgerScreen.openScreen();
	}

	@Override
	public void openScarBinderViewer(ItemStack binder) {
		ScreenScarBinderViewer.openScreenViaItem(binder);
	}

	@Override
	public void openVascularStatus() {
		VascularStatusScreen.openScreen();
	}

	@Override
	public void openUnstainedProgress() {
		UnstainedProgressScreen.openScreen();
	}

	@Override
	public void openRiteHint(ResourceLocation recipeId) {
		RiteHintScreen.open(recipeId);
	}

	@Override
	public void openBloodStructureHint(ResourceLocation recipeId) {
		BloodStructureHintScreen.open(recipeId);
	}

	@Override
	public void openBloodStructureGuidePage(int pageNum, BookCodeModel book, ChapterTemplate chapter) {
		HemoBloodStructureGuidePage.openScreenViaItem(pageNum, book, chapter);
	}

	@Override
	public BookBodyRenderer bloodStructureBookRenderer() {
		return BloodStructureBookRenderer.INSTANCE;
	}

	@Override
	public void anchorBlueprint(MnemonicBlueprintTarget target, BlockPos center, Direction facing) {
		MnemonicBlueprintRenderer.anchor(target, center, facing);
	}

	@Override
	public boolean clearBlueprint() {
		return MnemonicBlueprintRenderer.clear();
	}

	@Override
	public void spawnFirstPersonCellHandParticles(ItemStack stack, HumanoidArm arm) {
		CellHandParticleEffects.spawnFirstPersonParticlesForStack(stack, arm);
	}
}
