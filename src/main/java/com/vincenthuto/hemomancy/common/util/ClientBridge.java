package com.vincenthuto.hemomancy.common.util;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.common.item.shared.MnemonicBlueprintTarget;
import com.vincenthuto.hemomancy.common.rite.CardinalRitePurpose;
import com.vincenthuto.hemomancy.common.rite.harbinger.ActiveRiteData;
import com.vincenthuto.hutoslib.client.screen.guide.BookBodyRenderer;
import com.vincenthuto.hutoslib.common.data.book.BookCodeModel;
import com.vincenthuto.hutoslib.common.data.book.ChapterTemplate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * What common code asks of the game client: open a screen, read the local player or a key, look at client-side
 * caches. Common code calls {@link #get()} only on the logical client (a client-side level, tooltip or use), and the
 * client installs its implementation ({@code client.ClientBridgeImpl}) at setup. Until then, and always on a
 * dedicated server, every answer is neutral and every action does nothing, so common code never names a client
 * class.
 */
public interface ClientBridge {

	static ClientBridge get() {
		return Installed.bridge;
	}

	static void install(ClientBridge bridge) {
		Installed.bridge = bridge;
	}

	/** The local player, or null when there is none. */
	@Nullable
	default Player localPlayer() {
		return null;
	}

	/** The client level, or null when no world is loaded. */
	@Nullable
	default Level clientLevel() {
		return null;
	}

	/** The client level's registries, or {@link RegistryAccess#EMPTY} when no world is loaded. */
	default HolderLookup.Provider clientRegistries() {
		Level level = clientLevel();
		return level == null ? RegistryAccess.EMPTY : level.registryAccess();
	}

	default boolean hasShiftDown() {
		return false;
	}

	/** The key translated in the client's language, or the key itself when it has no translation. */
	default String translate(String key) {
		return key;
	}

	default boolean hasTranslation(String key) {
		return !translate(key).equals(key);
	}

	/** The purpose of the synced rite space covering a position. */
	default Optional<CardinalRitePurpose> riteSpacePurposeAt(BlockPos pos) {
		return Optional.empty();
	}

	/** The active rites from the last sync. */
	default List<ActiveRiteData.RiteEntry> activeRites() {
		return List.of();
	}

	default void openHarbingerProgress() {
	}

	default void openSynapticLoadout(BlockPos pos) {
	}

	default void openBloodlinePool() {
	}

	default void openLedger() {
	}

	default void openScarBinderViewer(ItemStack binder) {
	}

	default void openVascularStatus() {
	}

	default void openUnstainedProgress() {
	}

	default void openRiteHint(ResourceLocation recipeId) {
	}

	default void openBloodStructureHint(ResourceLocation recipeId) {
	}

	default void openBloodStructureGuidePage(int pageNum, BookCodeModel book, ChapterTemplate chapter) {
	}

	/** The Liber renderer for blood-structure pages, or null on a server. */
	@Nullable
	default BookBodyRenderer bloodStructureBookRenderer() {
		return null;
	}

	default void anchorBlueprint(MnemonicBlueprintTarget target, BlockPos center, Direction facing) {
	}

	/** Clears the anchored blueprint projection; true when one was showing. */
	default boolean clearBlueprint() {
		return false;
	}

	default void spawnFirstPersonCellHandParticles(ItemStack stack, HumanoidArm arm) {
	}

	/** Holds the installed bridge; an interface cannot keep a mutable static field itself. */
	final class Installed {
		private static volatile ClientBridge bridge = new ClientBridge() {
		};

		private Installed() {
		}
	}
}
