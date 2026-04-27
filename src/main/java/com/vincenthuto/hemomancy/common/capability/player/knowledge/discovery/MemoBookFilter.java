package com.vincenthuto.hemomancy.common.capability.player.knowledge.discovery;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.vincenthuto.hutoslib.common.data.book.BookCodeModel;
import com.vincenthuto.hutoslib.common.data.book.BookDataTemplate;
import com.vincenthuto.hutoslib.common.data.book.ChapterTemplate;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public final class MemoBookFilter {
	private MemoBookFilter() {
	}

	public static BookCodeModel filterForPlayer(BookCodeModel source, Player player) {
		if (source == null || player == null) {
			return source;
		}
		String bookPath = source.getResourceLocation().getPath();
		if (!"sanctumsanguinium".equals(bookPath) && !"liberimmaculatus".equals(bookPath)) {
			return source;
		}

		Set<ResourceLocation> unlockedEntries = HemoCapabilityAccess.getLiberKnowledge(player)
				.map(knowledge -> knowledge.getUnlockedEntries())
				.orElse(Set.of());
		Set<ResourceLocation> gatedEntries = new HashSet<>();
		for (LiberEntryDefinition definition : LiberEntryDefinitions.all()) {
			gatedEntries.add(definition.entryId());
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
		return filtered;
	}

	/**
	 * A page is visible when:
	 * <ul>
	 *   <li>it has no ID (e.g. a title or decorative page), or</li>
	 *   <li>its ID is not tracked by {@link LiberEntryDefinitions} (ungated content), or</li>
	 *   <li>it is a tracked entry that the player has already unlocked.</li>
	 * </ul>
	 */
	private static boolean isPageVisible(ResourceLocation pageId,
			Set<ResourceLocation> gatedEntries, Set<ResourceLocation> unlockedEntries) {
		return pageId == null || !gatedEntries.contains(pageId) || unlockedEntries.contains(pageId);
	}
}
