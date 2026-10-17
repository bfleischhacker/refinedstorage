package com.raoulvdberge.refinedstorage.gui.grid;

import net.minecraft.client.gui.GuiScreen;

/** Shared with the JEI bridge; upstream has no configurable binding for this gesture. */
public final class CraftingRequestGesture {
    private CraftingRequestGesture() { }

    public static boolean isActive() {
        return GuiScreen.isShiftKeyDown() && GuiScreen.isCtrlKeyDown();
    }
}
