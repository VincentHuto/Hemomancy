//package com.vincenthuto.hemomancy.client.screen.codex;
//
//import java.util.List;
//
//import com.vincenthuto.hemomancy.client.screen.codex.objects.ChapterObject;
//import com.vincenthuto.hemomancy.client.screen.codex.pages.BookPage;
//
//import net.minecraft.world.item.Item;
//import net.minecraft.world.item.ItemStack;
//
//public class ChapterEntry implements ProgressionEntry<ChapterObject>{
//    public  ItemStack iconStack;
//    public  String identifier;
//    public  int xOffset;
//    public  int yOffset;
//    public  int chapterNum;
//    public  ObjectSupplier<ChapterObject> objectSupplier = ChapterObject::new;
//    public boolean isSoulwood;
//    public boolean isDark;
//
//    public ChapterEntry(String identifier,int chapterNum, int xOffset, int yOffset) {
//        this.iconStack = null;
//        this.identifier = identifier;
//        this.xOffset = xOffset;
//        this.yOffset = yOffset;
//        this.chapterNum = chapterNum;
//    }
//
//    public ChapterEntry(String identifier, int chapterNum,Item item, int xOffset, int yOffset) {
//        this.iconStack = item.getDefaultInstance();
//        this.identifier = identifier;
//        this.xOffset = xOffset;
//        this.yOffset = yOffset;
//        this.chapterNum = chapterNum;
//
//    }
//
//    public String translationKey() {
//        return "hemo.gui.book.entry." + identifier;
//    }
//
//    public String descriptionTranslationKey() {
//        return "hemo.gui.book.entry." + identifier + ".description";
//    }
//
//    public ChapterEntry setSoulwood() {
//        isSoulwood = true;
//        return this;
//    }
//
//    public ChapterEntry setDark() {
//        isDark = true;
//        return this;
//    }
//
//    @Override
//    public ChapterEntry setObjectSupplier(ObjectSupplier<ChapterObject> objectSupplier) {
//        this.objectSupplier = objectSupplier;
//        return this;
//    }	
//    
//    
//	@Override
//	public ObjectSupplier<ChapterObject> getObjectSupplier() {
//		return objectSupplier;
//	}
//
//    
//    public ItemStack getIconStack() {
//		return iconStack;
//	}
//
//	@Override
//	public int getYOffset() {
//		return yOffset;
//	}
//
//	@Override
//	public int getXOffset() {
//		return xOffset;
//	}
//
//	@Override
//    public boolean isDark() {
//		return isDark;
//	}
//	@Override
//    public boolean isSoulwood() {
//		return isSoulwood;
//	}
//
//	@Override
//	public List<BookPage> getPages() {
//		return null;
//	}
//
//	@Override
//	public ProgressionEntry<ChapterObject> addPage(BookPage page) {
//		return null;
//	}
//
//	@Override
//	public int getChapter() {
//		return -1;
//	}
//
//	@Override
//	public void setChapter(int chapter) {
//		
//	}
//
//	@Override
//	public String getParentId() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public void setParentId(String parentId) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public String getIdentifier() {
//		// TODO Auto-generated method stub
//		return identifier;
//	}
//}
