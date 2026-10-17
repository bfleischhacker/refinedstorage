package com.raoulvdberge.refinedstorage.gui;

/** Optional parent-screen callback after the player confirms a request. */
public interface ICraftingRequestConfirmation {
    void onRequestSubmitted(int hash);
}
