package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

public final class DialogueAttentionResolver {
	private DialogueAttentionResolver() {
	}

	public static DialogueAttention from(DialoguePresentation presentation) {
		DialogueTopic topic = topic(presentation);
		return topic == null ? DialogueAttention.NONE : topic.attention();
	}

	public static DialogueTopic topic(DialoguePresentation presentation) {
		DialogueTopic result = null;
		for (DialogueTopic topic : presentation.topics()) {
			if (!topic.state().enabled() || topic.attention() == DialogueAttention.NONE) continue;
			if (result == null || topic.attention().ordinal() > result.attention().ordinal()) result = topic;
		}
		return result;
	}
}
