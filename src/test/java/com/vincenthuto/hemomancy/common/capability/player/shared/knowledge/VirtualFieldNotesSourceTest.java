package com.vincenthuto.hemomancy.common.capability.player.shared.knowledge;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class VirtualFieldNotesSourceTest {
	private static final Path JAVA_ROOT = Path.of("src/main/java");
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");

	private VirtualFieldNotesSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		liberKnowledgePersistsPendingMemos();
		packetSyncCarriesPendingMemos();
		memoHelperCapturesIntoPlayerKnowledge();
		dictationTableDictatesPlacedLiberOnNormalClick();
	}

	private static void liberKnowledgePersistsPendingMemos() throws IOException {
		String source = read("com/vincenthuto/hemomancy/common/capability/player/shared/knowledge/LiberKnowledge.java");
		assertContains("pending NBT key", source, "PendingMemos");
		assertContains("record pending API", source, "recordPendingMemo(ResourceLocation memoId)");
		assertContains("remove pending API", source, "removePendingMemos(");
		assertContains("pending path count API", source, "countPendingMemosByPath(MemoDefinition.MemoPath path)");
		assertContains("serialize pending memos", source, "tag.put(TAG_PENDING_MEMOS");
		assertContains("sync copy includes pending memos", source, "pendingMemos.addAll(liber.getPendingMemos())");
	}

	private static void packetSyncCarriesPendingMemos() throws IOException {
		String source = read("com/vincenthuto/hemomancy/common/network/capa/PacketSyncLiberKnowledge.java");
		assertContains("packet stores pending memos", source, "private final Set<ResourceLocation> pendingMemos");
		assertContains("packet encodes pending memos", source, "writeIds(buf, msg.pendingMemos)");
		assertContains("packet decodes pending memos", source, "Set<ResourceLocation> pendingMemos = readIds(buf)");
		assertContains("packet hydrates pending memos", read("com/vincenthuto/hemomancy/client/network/ClientboundStateHandlers.java"), "synced.recordPendingMemo(memo)");
	}

	private static void memoHelperCapturesIntoPlayerKnowledge() throws IOException {
		String source = read("com/vincenthuto/hemomancy/common/capability/player/shared/knowledge/discovery/MemoHelper.java");
		assertContains("capture records pending memo", source, "knowledge.recordPendingMemo(memoId)");
		assertContains("dictation filter helper exists", source, "getDictatableMemosForPath(");
		assertContains("dictation removes written pending memos", source, "knowledge.removePendingMemos(dictatedMemos)");
		assertDoesNotContain("capture does not require field notes", captureMethod(source), "findFieldNotes(player)");
		assertDoesNotContain("capture does not require ink", captureMethod(source), "getInkPath(notes)");
	}

    private static void dictationTableDictatesPlacedLiberOnNormalClick() throws IOException {
        String source = Files.readString(Path.of("../HutosLib/src/main/java/com/vincenthuto/hutoslib/common/block/DictationTableBlock.java"));
        assertContains("sneak click returns book", source, "player.isShiftKeyDown()");
        assertContains("shared provider dictates placed book", source, "FieldNotes.dictate(serverPlayer, table.getBook(), this)");
        assertContains("no pending notes opens reader", source, "BookReaderScreen.openPlaced(pos, table.getBook())");
        String provider = read("com/vincenthuto/hemomancy/common/capability/player/shared/knowledge/discovery/HemomancyFieldNotes.java");
        assertContains("Hemomancy requires its Escritoire", provider, "HarbingerEscritoireBlock");
        assertContains("provider preserves cost path", provider, "MemoHelper.dictatePendingToLiber(player, book)");
    }

	private static String captureMethod(String source) {
		int start = source.indexOf("public static CaptureResult captureMemo");
		int end = source.indexOf("public static DictationResult", start);
		if (start < 0 || end < 0) {
			throw new AssertionError("capture method boundaries changed");
		}
		return source.substring(start, end);
	}

	private static String read(String relativePath) throws IOException {
		return Files.readString(JAVA_ROOT.resolve(relativePath)).replace("\r\n", "\n");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}

	private static void assertDoesNotContain(String label, String text, String forbidden) {
		if (text.contains(forbidden)) {
			throw new AssertionError(label + ": still contains " + forbidden);
		}
	}

}
