package com.vincenthuto.hemomancy.client.screen.codex;

import java.util.ArrayList;
import java.util.List;

import com.vincenthuto.hemomancy.client.screen.codex.objects.EntryObject;
import com.vincenthuto.hemomancy.client.screen.codex.pages.BookPage;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

public class BookEntry implements ProgressionEntry<EntryObject>{
	
    public final ItemStack iconStack;
    public final String identifier;
    public final int xOffset;
    public final int yOffset;
    public int chapter;
    public List<BookPage> pages = new ArrayList<>();
    public ObjectSupplier<EntryObject> objectSupplier = EntryObject::new;
    public boolean isSoulwood;
    public boolean isDark;

    public BookEntry(String identifier,int chapter, int xOffset, int yOffset) {
        this.chapter = chapter;

        this.iconStack = null;
        this.identifier = identifier;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
    }

    public BookEntry(String identifier, int chapter,Item item, int xOffset, int yOffset) {
        this.iconStack = item.getDefaultInstance();
        this.chapter = chapter;

        this.identifier = identifier;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.chapter = chapter;
    }

    public String translationKey() {
        return "malum.gui.book.entry." + identifier;
    }

    public String descriptionTranslationKey() {
        return "malum.gui.book.entry." + identifier + ".description";
    }
    
    public boolean isDark() {
		return isDark;
	}
    
    public boolean isSoulwood() {
		return isSoulwood;
	}
    
    public List<BookPage> getPages() {
		return pages;
	}
    
	@Override
    public BookEntry setSoulwood() {
        isSoulwood = true;
        return this;
    }
	@Override
    public BookEntry setDark() {
        isDark = true;
        return this;
    }

    @Override
    public BookEntry addPage(BookPage page) {
        if (page.isValid()) {
            pages.add(page.setParentEntry(this));
        }
        return this;
    }

    public BookEntry addModCompatPage(BookPage page, String modId) {
        if (ModList.get().isLoaded(modId)) {
            addPage(page);
        }
        return this;
    }
	@Override
    public BookEntry setObjectSupplier(ObjectSupplier<EntryObject> objectSupplier) {
        this.objectSupplier = objectSupplier;
        return this;
    }

  
	@Override
    public int getChapter() {
		return chapter;
	}
    @Override
    public void setChapter(int chapter) {
		this.chapter = chapter;
	}

	@Override
	public int getYOffset() {
		return yOffset;
	}

	@Override
	public int getXOffset() {
		return xOffset;
	}

	@Override
	public ObjectSupplier<EntryObject> getObjectSupplier() {
		return objectSupplier;
	}

	@Override
	public ItemStack getIconStack() {
		return iconStack;
	}
}