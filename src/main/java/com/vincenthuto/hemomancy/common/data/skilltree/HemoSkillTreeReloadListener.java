package com.vincenthuto.hemomancy.common.data.skilltree;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hutoslib.common.data.book.BookDataTemplate;
import com.vincenthuto.hutoslib.common.data.shadow.PlaceboJsonReloadListener;

public class HemoSkillTreeReloadListener extends PlaceboJsonReloadListener<BookDataTemplate> {
	
	public static final HemoSkillTreeReloadListener INSTANCE = new HemoSkillTreeReloadListener();

	public HemoSkillTreeReloadListener() {
		super(Hemomancy.LOGGER, "skilltrees", true, true);
	}


	@Override
	protected void registerBuiltinSerializers() {
		//this.registerSerializer(HutosLib.rloc("hemopage"), BloodStructurePageTemplate.SERIALIZER);
	}
}
