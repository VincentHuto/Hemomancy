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

/**
 * Filters a {@link BookCodeModel} so only pages whose Liber entry has been
 * unlocked by the player are shown. Implements {@link IBookPageFilter} so it
 * can be injected wherever a filter is needed without coupling callers to this
 * specific implementation.
 */
public final class MemoBookFilter implements IBookPageFilter {

	/** Shared singleton — stateless, so safe to reuse. */
	public static final MemoBookFilter INSTANCE = new MemoBookFilter();

	private MemoBookFilter() {
	}

	@Override
	public BookCodeModel filter(BookCodeModel source, Player player) {
		return filterForPlayer(source, player);
	}

	public static BookCodeModel filterForPlayer(BookCodeModel source, Player player) {
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
	 *   <li>it is tracked by {@link LiberEntryDefinitions} for the current book and unlocked by the player.</li>
	 * </ul>
	 */
	private static boolean isPageVisible(ResourceLocation pageId,
			Set<ResourceLocation> gatedEntries, Set<ResourceLocation> unlockedEntries) {
		return pageId == null || (gatedEntries.contains(pageId) && unlockedEntries.contains(pageId));
	}
}
