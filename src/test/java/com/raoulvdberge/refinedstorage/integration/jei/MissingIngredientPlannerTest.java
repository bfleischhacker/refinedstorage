package com.raoulvdberge.refinedstorage.integration.jei;

import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;

public class MissingIngredientPlannerTest {
    private MissingIngredientPlanner<String> plan(List<List<String>> slots,
        List<MissingIngredientPlanner.Supply<String>> stock, String... patterns) {
        return new MissingIngredientPlanner<>(slots, stock, Arrays.asList(patterns), String::equals);
    }
    private List<List<String>> slots(String... items) {
        List<List<String>> result = new ArrayList<>();
        for (String item : items) { result.add(Collections.singletonList(item)); }
        return result;
    }
    private MissingIngredientPlanner.Supply<String> stock(String item, int count) {
        return new MissingIngredientPlanner.Supply<>(item, count);
    }

    @Test public void repeatedSlotsRequestOnlyDeficit() {
        MissingIngredientPlanner<String> p = plan(slots("iron", "iron", "iron"),
            Arrays.asList(stock("iron", 1)), "iron");
        assertEquals(Integer.valueOf(2), p.requests.get(0));
        assertEquals(Arrays.asList(2, 3), p.craftableSlots);
    }
    @Test public void networkMatrixAndPlayerInventoryAreConsumedOnce() {
        MissingIngredientPlanner<String> p = plan(slots("iron", "iron", "iron", "iron"),
            Arrays.asList(stock("iron", 1), stock("iron", 1), stock("iron", 1)), "iron");
        assertEquals(Integer.valueOf(1), p.requests.get(0));
    }
    @Test public void stockedAlternativeWinsOverCraftableAlternative() {
        MissingIngredientPlanner<String> p = plan(Arrays.asList(Arrays.asList("oak", "birch")),
            Arrays.asList(stock("birch", 1)), "oak");
        assertTrue(p.requests.isEmpty());
        assertTrue(p.missing.isEmpty());
    }
    @Test public void craftableAlternativeIsUsedWhenDisplayedAlternativeIsUncraftable() {
        MissingIngredientPlanner<String> p = plan(Arrays.asList(Arrays.asList("oak", "birch")),
            Collections.emptyList(), "birch");
        assertEquals(Integer.valueOf(1), p.requests.get(0));
        assertTrue(p.missing.isEmpty());
    }
    @Test public void mixedMissingInputsPreserveRedAndBlueSlots() {
        MissingIngredientPlanner<String> p = plan(slots("iron", "diamond", "iron"),
            Collections.emptyList(), "iron");
        assertEquals(Arrays.asList(2), p.missing);
        assertEquals(Arrays.asList(1, 3), p.craftableSlots);
        assertEquals(Integer.valueOf(2), p.requests.get(0));
    }
    @Test public void zeroQuantityPatternDisplayDoesNotCountAsStock() {
        MissingIngredientPlanner<String> p = plan(slots("iron"),
            Arrays.asList(stock("iron", 0)), "iron");
        assertEquals(Integer.valueOf(1), p.requests.get(0));
    }
    @Test public void emptySlotsAndNonMatchingStockDoNotCreateRequests() {
        MissingIngredientPlanner<String> p = plan(Arrays.asList(Collections.emptyList(), Arrays.asList("a")),
            Arrays.asList(stock("b", 64)), "b");
        assertEquals(Arrays.asList(2), p.missing);
        assertTrue(p.requests.isEmpty());
    }
    @Test public void fullyStockedRecipeCreatesNoRequests() {
        MissingIngredientPlanner<String> p = plan(slots("a", "b", "a"),
            Arrays.asList(stock("a", 2), stock("b", 1)), "a", "b");
        assertTrue(p.requests.isEmpty());
        assertTrue(p.missing.isEmpty());
    }
    @Test public void overlappingSubstitutesDoNotRequestItemsAlreadyInStock() {
        MissingIngredientPlanner<String> p = plan(Arrays.asList(Arrays.asList("oak", "birch"), Arrays.asList("oak")),
            Arrays.asList(stock("oak", 1), stock("birch", 1)), "oak", "birch");
        assertTrue(p.requests.isEmpty());
        assertTrue(p.missing.isEmpty());
    }
    @Test public void scarceStockIsReservedForUncraftableSlot() {
        MissingIngredientPlanner<String> p = plan(Arrays.asList(Arrays.asList("oak", "birch"), Arrays.asList("oak")),
            Arrays.asList(stock("oak", 1)), "birch");
        assertTrue(p.missing.isEmpty());
        assertEquals(Integer.valueOf(1), p.requests.get(0));
        assertEquals(Arrays.asList(1), p.craftableSlots);
    }
    @Test public void allocationsMatchExhaustiveSearchAcrossFiveHundredInventories() {
        Random random = new Random(1731);
        String[] kinds = {"a", "b", "c", "d"};
        for (int trial = 0; trial < 500; trial++) {
            List<List<String>> slots = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                List<String> alternatives = new ArrayList<>();
                for (String kind : kinds) { if (random.nextBoolean()) { alternatives.add(kind); } }
                if (alternatives.isEmpty()) { alternatives.add("a"); }
                slots.add(alternatives);
            }
            int[] counts = new int[4];
            List<MissingIngredientPlanner.Supply<String>> inventory = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                counts[i] = random.nextInt(3);
                inventory.add(stock(kinds[i], counts[i]));
            }
            MissingIngredientPlanner<String> p = plan(slots, inventory, kinds);
            int requested = p.requests.values().stream().mapToInt(Integer::intValue).sum();
            assertEquals("trial " + trial, 5 - maximum(slots, 0, counts), requested);
        }
    }
    private int maximum(List<List<String>> slots, int slot, int[] counts) {
        if (slot == slots.size()) { return 0; }
        int best = maximum(slots, slot + 1, counts);
        for (String alternative : slots.get(slot)) {
            int kind = alternative.charAt(0) - 'a';
            if (counts[kind] > 0) {
                counts[kind]--;
                best = Math.max(best, 1 + maximum(slots, slot + 1, counts));
                counts[kind]++;
            }
        }
        return best;
    }
}
