package com.vincenthuto.hemomancy.common.data.book;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.vincenthuto.hemomancy.client.screen.item.guide.HemoBloodStructureGuidePage;
import com.vincenthuto.hutoslib.client.HLLocHelper;
import com.vincenthuto.hutoslib.common.data.book.BookCodeModel;
import com.vincenthuto.hutoslib.common.data.book.BookDataTemplate;
import com.vincenthuto.hutoslib.common.data.book.ChapterTemplate;
import com.vincenthuto.hutoslib.common.data.book.PageTemplate;
import com.vincenthuto.hutoslib.common.data.shadow.PSerializer;

import net.minecraft.resources.ResourceLocation;

public class BloodStructurePageTemplate extends PageTemplate {

	public static final Codec<BloodStructurePageTemplate> CODEC = RecordCodecBuilder.create(inst -> inst
			.group(PageTemplate.MAP_CODEC.forGetter(page -> page),
					Codec.STRING.fieldOf("structureloc").forGetter(BloodStructurePageTemplate::getStructureloc))
			.apply(inst, BloodStructurePageTemplate::new));
	public static final PSerializer<BloodStructurePageTemplate> SERIALIZER = PSerializer.fromCodec("bloodstructure",
			CODEC);

	String structureloc;

	public BloodStructurePageTemplate(int ordinality, String texture, String title, String subtitle, String text,
			String icon, String structureloc) {
		super(ordinality, texture, title, subtitle, text, icon);
		this.structureloc = structureloc;
	}

	private BloodStructurePageTemplate(PageTemplate source, String structureloc) {
		super(source.getOrdinality(), source.getTexture(), source.getTitle(), source.getSubtitle(), source.getText(),
				source.getIcon(), source.getRequiresEntry(), source.getPresentation());
		this.structureloc = structureloc;
	}

	public String getStructureloc() {
		return structureloc;
	}

	public void setStructureloc(String structureloc) {
		this.structureloc = structureloc;
	}

	public ResourceLocation getStructureKey() {
		return HLLocHelper.getBySplit(structureloc);
	}
	@Override
	public com.vincenthuto.hutoslib.client.screen.guide.BookBodyRenderer getBodyRenderer() {
		return super.getBodyRenderer() != null ? super.getBodyRenderer()
				: com.vincenthuto.hemomancy.client.screen.item.guide.BloodStructureBookRenderer.INSTANCE;
	}
	@Override
	public PSerializer<? extends BookDataTemplate> getSerializer() {
		return SERIALIZER;
	}


	@Override
	public void getPageScreen(int pageNum, BookCodeModel book, ChapterTemplate chapter) {
		HemoBloodStructureGuidePage.openScreenViaItem(pageNum, book, chapter);
	}
}
