package com.vincenthuto.hemomancy.client.screen.codex;

import java.util.List;

import com.vincenthuto.hemomancy.client.screen.codex.objects.ProgressionObject;
import com.vincenthuto.hemomancy.client.screen.codex.pages.BookPage;
import com.vincenthuto.hutoslib.common.data.skilltree.SkillTreeCodeModel;

import net.minecraft.world.item.ItemStack;

public interface ProgressionEntry<T extends ProgressionObject> {

	int getYOffset();

	int getXOffset();

	public int getChapter();

	public void setChapter(int chapter);

	public String getParentId();

	public void setParentId(String parentId);

	public boolean isDark();

	public boolean isSoulwood();

	public default ItemStack getIconStack() {
		return ItemStack.EMPTY;
	}

	public String getIdentifier();

	public ProgressionEntry setDark();

	public ProgressionEntry setSoulwood();

	public String translationKey();

	public String descriptionTranslationKey();

	public List<BookPage> getPages();

	public ProgressionEntry<T> addPage(BookPage page);

	public ProgressionEntry<T> setObjectSupplier(ObjectSupplier<T> objectSupplier);

	ObjectSupplier<T> getObjectSupplier();

	public interface ObjectSupplier<T> {
		ProgressionObject getEntryObject(AbstractProgressionCodexScreen screen,SkillTreeCodeModel model, String identifier, int chapter,
				String parentId, ProgressionEntry entry, int x, int y);
	}

}
