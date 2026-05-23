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
		inventoryButtonUsesPngTextures();
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
		assertContains("packet hydrates pending memos", source, "synced.recordPendingMemo(memo)");
	}

	private static void memoHelperCapturesIntoPlayerKnowledge() throws IOException {
		String source = read("com/vincenthuto/hemomancy/common/capability/player/shared/knowledge/discovery/MemoHelper.java");
		assertContains("capture records pending memo", source, "knowledge.recordPendingMemo(memoId)");
		assertContains("legacy notes are migrated", source, "migrateLegacyFieldNotes(player)");
		assertContains("dictation filter helper exists", source, "getDictatableMemosForPath(");
		assertContains("dictation removes written pending memos", source, "knowledge.removePendingMemos(dictatedMemos)");
		assertDoesNotContain("capture does not require field notes", captureMethod(source), "findFieldNotes(player)");
		assertDoesNotContain("capture does not require ink", captureMethod(source), "getInkPath(notes)");
	}

	private static void dictationTableDictatesPlacedLiberOnNormalClick() throws IOException {
		String source = read("com/vincenthuto/hemomancy/common/block/inscription/DictationTableBlock.java");
		assertContains("normal click dictates pending memos", source, "dictateStoredLiber");
		assertContains("sneak click removes liber", source, "player.isShiftKeyDown()");
		assertContains("dictation uses placed liber", source, "MemoHelper.dictatePendingToLiber(serverPlayer, table.getLiber())");
	}

	private static void inventoryButtonUsesPngTextures() throws IOException {
		String source = read("com/vincenthuto/hemomancy/client/screen/inventory/VirtualFieldNotesButton.java");
		assertContains("button texture constant", source, "virtual_field_notes_button.png");
		assertContains("book texture constant", source, "virtual_field_notes_book.png");
		assertContains("button draws pngs", source, "graphics.blit(BUTTON_TEXTURE");
		assertContains("book draws png", source, "graphics.blit(BOOK_TEXTURE");
		assertDoesNotContain("button no longer uses procedural fills", source, "graphics.fill(");
		assertPngResourceExists("button png", "assets/hemomancy/textures/gui/virtual_field_notes_button.png");
		assertPngResourceExists("book png", "assets/hemomancy/textures/gui/virtual_field_notes_book.png");
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

	private static void assertPngResourceExists(String label, String relativePath) throws IOException {
		Path resource = RESOURCE_ROOT.resolve(relativePath);
		if (!Files.isRegularFile(resource)) {
			throw new AssertionError(label + ": missing " + relativePath);
		}
		byte[] bytes = Files.readAllBytes(resource);
		byte[] pngHeader = new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
		if (bytes.length < pngHeader.length) {
			throw new AssertionError(label + ": empty or truncated PNG " + relativePath);
		}
		for (int i = 0; i < pngHeader.length; i++) {
			if (bytes[i] != pngHeader[i]) {
				throw new AssertionError(label + ": not a PNG " + relativePath);
			}
		}
	}
}
