package com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hutoslib.common.book.filter.IBookPageFilter;
import com.vincenthuto.hutoslib.common.data.book.BookCodeModel;
import com.vincenthuto.hutoslib.common.data.book.BookDataTemplate;
import com.vincenthuto.hutoslib.common.data.book.ChapterTemplate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Implements {@link IBookPageFilter} to hide Liber Sanguinum and Liber
 * Immaculatus pages that the player has not yet unlocked. Pages without an ID
 * (title / decorative pages) are always visible.
 *
 * <p>Set this on a book model via
 * {@link BookCodeModel#setPageFilter(IBookPageFilter)} so the screen applies
 * it automatically at open time.
 */
public final class MemoBookFilter implements IBookPageFilter {

	@Override
	public BookCodeModel filter(BookCodeModel source, Player player) {
		if (source == null || player == null) {
			return source;
		}
		String bookPath = source.getResourceLocation().getPath();
		if (!"sanctumsanguinium".equals(bookPath) && !"liberimmaculatus".equals(bookPath)) {
			return source;
		}
		final String entryPrefix = bookPath + "/";

		Set<ResourceLocation> unlockedEntries = HemoCapabilityAccess.getLiberKnowledge(player)
				.map(knowledge -> knowledge.getUnlockedEntries().stream()
						.filter(entry -> entry.getPath().startsWith(entryPrefix))
						.collect(java.util.stream.Collectors.toSet()))
				.orElse(Set.of());
		Set<ResourceLocation> gatedEntries = new HashSet<>();
		for (LiberEntryDefinition definition : LiberEntryDefinitions.all()) {
			if (definition.entryId().getPath().startsWith(entryPrefix)) {
				gatedEntries.add(definition.entryId());
			}
		}

		BookCodeModel filtered = new BookCodeModel(source.getResourceLocation(), source.getTemplate());
		List<ChapterTemplate> chapters = new ArrayList<>();
		for (ChapterTemplate chapter : source.getChapters()) {
			ChapterTemplate chapterCopy = new ChapterTemplate(
					chapter.getOrdinality(),
					chapter.getTexture(),
					chapter.getColor(),
					chapter.getTitle(),
					chapter.getSubtitle(),
					chapter.getIcon());
			if (chapter.getId() != null) {
				chapterCopy.setId(chapter.getId());
			}
			List<BookDataTemplate> pages = new ArrayList<>();
			for (BookDataTemplate page : chapter.getPages()) {
				if (isPageVisible(page.getId(), gatedEntries, unlockedEntries)) {
					pages.add(page);
				}
			}
			chapterCopy.setPages(pages);
			if (!pages.isEmpty()) {
				chapters.add(chapterCopy);
			}
		}
		filtered.setChapters(chapters);
		filtered.setPageFilter(source.getPageFilter());
		filtered.setTheme(source.getTheme());
		return filtered;
	}

	/**
	 * A page is visible when:
	 * <ul>
	 *   <li>it has no ID (e.g. a title or decorative page), or</li>
	 *   <li>it is registered in {@link LiberEntryDefinitions} for the current
	 *       book <em>and</em> the player has unlocked it.</li>
	 * </ul>
	 *
	 * <p>Pages that are not registered in {@link LiberEntryDefinitions} are
	 * treated as gated-but-undefined and remain hidden. This matches the
	 * design intent recorded in {@code HEMOMANCY_REFERENCE.md}: discoverable
	 * pages must be explicitly registered with a discovery trigger before
	 * they can appear.
	 */
	private static boolean isPageVisible(ResourceLocation pageId,
			Set<ResourceLocation> gatedEntries, Set<ResourceLocation> unlockedEntries) {
		if (pageId == null) {
			return true;
		}
		return gatedEntries.contains(pageId) && unlockedEntries.contains(pageId);
	}
}

