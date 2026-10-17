package com.raoulvdberge.refinedstorage.integration.jei;

import com.raoulvdberge.refinedstorage.api.util.IComparer;
import com.raoulvdberge.refinedstorage.apiimpl.API;
import com.raoulvdberge.refinedstorage.gui.grid.GuiGrid;
import com.raoulvdberge.refinedstorage.gui.grid.stack.GridStackItem;
import com.raoulvdberge.refinedstorage.gui.grid.stack.IGridStack;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IRecipeLayout;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import java.util.*;

final class JeiCraftingPlan {
    final MissingIngredientPlanner<ItemStack> plan;
    final List<GridStackItem> patterns = new ArrayList<>();

    JeiCraftingPlan(GuiGrid grid, IRecipeLayout layout, EntityPlayer player) {
        List<MissingIngredientPlanner.Supply<ItemStack>> supplies = new ArrayList<>();
        for (IGridStack entry : grid.getView().getAllStacks()) {
            if (entry instanceof GridStackItem) {
                GridStackItem item = (GridStackItem) entry;
                supplies.add(new MissingIngredientPlanner.Supply<>(item.getStack(), item.getQuantity()));
                if (item.isCraftable()) { patterns.add(item); }
            }
        }
        InventoryCrafting matrix = grid.getGrid().getCraftingMatrix();
        if (matrix != null) {
            for (int i = 0; i < matrix.getSizeInventory(); i++) {
                ItemStack item = matrix.getStackInSlot(i);
                supplies.add(new MissingIngredientPlanner.Supply<>(item, item.getCount()));
            }
        }
        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            ItemStack item = player.inventory.getStackInSlot(i);
            supplies.add(new MissingIngredientPlanner.Supply<>(item, item.getCount()));
        }
        List<List<ItemStack>> slots = new ArrayList<>();
        Map<Integer, ? extends IGuiIngredient<ItemStack>> ingredients = layout.getItemStacks().getGuiIngredients();
        for (int i = 1; i <= 9; i++) {
            List<ItemStack> alternatives = new ArrayList<>();
            IGuiIngredient<ItemStack> ingredient = ingredients.get(i);
            if (ingredient != null && ingredient.isInput()) {
                for (ItemStack item : ingredient.getAllIngredients()) {
                    if (item != null && !item.isEmpty()) { alternatives.add(item); }
                }
            }
            slots.add(alternatives);
        }
        List<ItemStack> outputs = new ArrayList<>();
        for (GridStackItem pattern : patterns) { outputs.add(pattern.getStack()); }
        plan = new MissingIngredientPlanner<>(slots, supplies, outputs, JeiCraftingPlan::matches);
    }

    static boolean matches(ItemStack a, ItemStack b) {
        return API.instance().getComparer().isEqual(a, b, IComparer.COMPARE_NBT |
            (a.getItem().isDamageable() ? 0 : IComparer.COMPARE_DAMAGE));
    }
}
