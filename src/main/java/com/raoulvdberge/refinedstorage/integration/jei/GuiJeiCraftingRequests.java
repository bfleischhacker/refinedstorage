package com.raoulvdberge.refinedstorage.integration.jei;

import com.raoulvdberge.refinedstorage.gui.GuiBase;
import com.raoulvdberge.refinedstorage.gui.ICraftingRequestConfirmation;
import com.raoulvdberge.refinedstorage.gui.grid.GuiGrid;
import com.raoulvdberge.refinedstorage.gui.grid.GuiGridCraftingSettings;
import com.raoulvdberge.refinedstorage.gui.grid.stack.GridStackItem;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import org.lwjgl.input.Keyboard;
import java.io.IOException;
import java.util.*;

/** Each distinct output is reviewed separately through the existing RS resource preview. */
final class GuiJeiCraftingRequests extends GuiBase implements ICraftingRequestConfirmation {
    private final GuiGrid parent;
    private final Map<Integer, GridStackItem> items = new LinkedHashMap<>();
    private final Map<Integer, Integer> quantities = new LinkedHashMap<>();
    private final boolean hasUncraftable;
    private final Map<Integer, Integer> buttons = new HashMap<>();
    private GuiButton done;

    GuiJeiCraftingRequests(GuiGrid parent, JeiCraftingPlan plan) {
        super(new Container() {
            @Override public boolean canInteractWith(EntityPlayer player) { return false; }
        }, 254, 254);
        this.parent = parent;
        hasUncraftable = !plan.plan.missing.isEmpty();
        plan.plan.requests.forEach((index, count) -> {
            GridStackItem item = plan.patterns.get(index);
            items.put(item.getHash(), item);
            quantities.merge(item.getHash(), count, Integer::sum);
        });
    }

    @Override public void init(int x, int y) {
        buttons.clear();
        int row = 0;
        for (Map.Entry<Integer, Integer> request : quantities.entrySet()) {
            String label = request.getValue() + " x " + items.get(request.getKey()).getName();
            GuiButton button = addButton(x + 7, y + 39 + 20 * row++, 240, 20,
                fontRenderer.trimStringToWidth(label, 230));
            buttons.put(button.id, request.getKey());
        }
        done = addButton(x + 77, y + 229, 100, 20, t("gui.done"));
    }

    @Override public void update(int x, int y) { }
    @Override public void drawBackground(int x, int y, int mouseX, int mouseY) {
        drawRect(x, y, x + screenWidth, y + screenHeight, 0xFFCCCCCC);
    }
    @Override public void drawForeground(int mouseX, int mouseY) {
        drawString(7, 7, t("gui.refinedstorage:jei.request.title"));
        drawString(7, 21, t(hasUncraftable ? "gui.refinedstorage:jei.request.uncraftable" :
            "gui.refinedstorage:jei.request.review"));
    }
    @Override protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == done.id) {
            mc.displayGuiScreen(parent);
        } else if (buttons.containsKey(button.id)) {
            int hash = buttons.get(button.id);
            int quantity = quantities.get(hash);
            mc.displayGuiScreen(new GuiGridCraftingSettings(this, mc.player, items.get(hash)) {
                @Override protected int getDefaultAmount() { return quantity; }
                @Override protected void onOkButtonPressed(boolean shiftDown) {
                    if (okButton.enabled) { super.onOkButtonPressed(false); }
                }
            });
        }
    }
    @Override protected void keyTyped(char character, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) { mc.displayGuiScreen(parent); }
        else { super.keyTyped(character, keyCode); }
    }
    @Override public void onRequestSubmitted(int hash) {
        quantities.remove(hash); // Cancelled previews keep the entry; confirmed ones cannot be submitted twice.
    }
}
