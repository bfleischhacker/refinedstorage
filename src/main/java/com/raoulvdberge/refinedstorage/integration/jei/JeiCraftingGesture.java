package com.raoulvdberge.refinedstorage.integration.jei;

import com.raoulvdberge.refinedstorage.gui.grid.CraftingRequestGesture;
import mezz.jei.api.gui.IRecipeLayout;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import org.lwjgl.input.Mouse;

/** Old JEI disables the plus button for USER_FACING errors. Hook only that hovered button. */
public final class JeiCraftingGesture {
    private static GuiScreen screen;
    private static GuiButton button;
    private static Runnable action;

    static void hover(IRecipeLayout layout, Runnable request) {
        try {
            // Public in JEI 4.14.3.242, but deliberately absent from the stable API.
            Object candidate = layout.getClass().getMethod("getRecipeTransferButton").invoke(layout);
            if (candidate instanceof GuiButton) {
                screen = Minecraft.getMinecraft().currentScreen;
                button = (GuiButton) candidate;
                action = request;
            }
        } catch (ReflectiveOperationException ignored) {
            // Other JEI implementations retain their ordinary transfer-error behavior.
        }
    }

    @SubscribeEvent
    public void beforeDraw(GuiScreenEvent.DrawScreenEvent.Pre event) {
        clear(); // A tooltip from another page, recipe or screen must never remain clickable.
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void mouse(GuiScreenEvent.MouseInputEvent.Pre event) {
        if (Mouse.getEventButton() != 0 || !Mouse.getEventButtonState() ||
            !CraftingRequestGesture.isActive() || action == null || event.getGui() != screen) { return; }
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution resolution = new ScaledResolution(mc);
        int x = Mouse.getEventX() * resolution.getScaledWidth() / mc.displayWidth;
        int y = resolution.getScaledHeight() - Mouse.getEventY() * resolution.getScaledHeight() / mc.displayHeight - 1;
        if (!button.visible || x < button.x || y < button.y ||
            x >= button.x + button.width || y >= button.y + button.height) { return; }
        Runnable request = action;
        clear(); // Consume once; button-up and repeated input cannot launch more requests.
        event.setCanceled(true);
        request.run();
    }

    private static void clear() { screen = null; button = null; action = null; }
}
