package com.raoulvdberge.refinedstorage.integration.jei;

import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.OreDictionary;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class JeiIngredientMatchingTest {
    @BeforeClass public static void bootstrap() { Bootstrap.register(); }

    @Test public void nbtIsNeverIgnoredForStockOrPatternOutputs() {
        ItemStack required = new ItemStack(Items.PAPER);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("recipe", "one");
        required.setTagCompound(tag);
        ItemStack other = required.copy();
        other.getTagCompound().setString("recipe", "two");
        assertFalse(JeiCraftingPlan.matches(required, other));
        assertTrue(JeiCraftingPlan.matches(required, required.copy()));
    }
    @Test public void metadataAndJeiWildcardFollowServerComparer() {
        ItemStack coal = new ItemStack(Items.COAL, 1, 0);
        ItemStack charcoal = new ItemStack(Items.COAL, 1, 1);
        assertFalse(JeiCraftingPlan.matches(coal, charcoal));
        assertTrue(JeiCraftingPlan.matches(new ItemStack(Items.COAL, 1, OreDictionary.WILDCARD_VALUE), charcoal));
    }
    @Test public void toolDurabilityMatchesExistingTransferSemantics() {
        assertTrue(JeiCraftingPlan.matches(new ItemStack(Items.IRON_PICKAXE, 1, 0),
            new ItemStack(Items.IRON_PICKAXE, 1, 17)));
    }
}
