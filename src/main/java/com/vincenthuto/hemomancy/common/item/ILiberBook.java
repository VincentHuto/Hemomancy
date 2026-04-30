package com.vincenthuto.hemomancy.common.item;

/**
 * Marker interface implemented by all Hemomancy Liber book items
 * (Liber Sanguinum and Liber Immaculatus).
 *
 * <p>This lets {@link com.vincenthuto.hemomancy.common.capability.player.knowledge.discovery.MemoHelper}
 * identify liber items via {@code instanceof} rather than hard-coding
 * {@link com.vincenthuto.hemomancy.common.init.ItemInit} field references,
 * making it easier to add additional liber books in the future without
 * changing the helper class.
 */
public interface ILiberBook {
    /**
     * The resource-location path of the book within the HutosLib
     * {@code BookPlaceboReloadListener} registry (e.g. {@code "sanctumsanguinium"}
     * or {@code "liberimmaculatus"}).
     */
    String getBookPath();
}
