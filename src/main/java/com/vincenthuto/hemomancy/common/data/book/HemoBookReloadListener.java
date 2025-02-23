package com.vincenthuto.hemomancy.common.data.book;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hutoslib.HutosLib;
import com.vincenthuto.hutoslib.common.data.book.BookDataTemplate;
import com.vincenthuto.hutoslib.common.data.shadow.PlaceboJsonReloadListener;

public class HemoBookReloadListener extends PlaceboJsonReloadListener<BookDataTemplate> {
	
	public static final HemoBookReloadListener INSTANCE = new HemoBookReloadListener();

	public HemoBookReloadListener() {
		super(Hemomancy.LOGGER, "books", true, true);
	}


	@Override
	protected void registerBuiltinSerializers() {
		this.registerSerializer(HutosLib.rloc("hemopage"), BloodStructurePageTemplate.SERIALIZER);
	}
}
