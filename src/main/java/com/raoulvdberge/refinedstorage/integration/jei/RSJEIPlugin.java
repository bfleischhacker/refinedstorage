package com.raoulvdberge.refinedstorage.integration.jei;

import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import net.minecraftforge.common.MinecraftForge;

@JEIPlugin
public class RSJEIPlugin implements IModPlugin {
    public static RSJEIPlugin INSTANCE;

    private IJeiRuntime runtime;

    @Override
    public void register(IModRegistry registry) {
        INSTANCE = this;
        MinecraftForge.EVENT_BUS.register(new JeiCraftingGesture());

        registry.getRecipeTransferRegistry().addUniversalRecipeTransferHandler(new RecipeTransferHandlerGrid(registry.getJeiHelpers().recipeTransferHandlerHelper()));

        registry.addAdvancedGuiHandlers(new AdvancedGuiHandler());

        registry.addRecipeRegistryPlugin(new RecipeRegistryPluginCover());
        registry.addRecipeRegistryPlugin(new RecipeRegistryPluginHollowCover());

        // TODO: https://github.com/mezz/JustEnoughItems/issues/1307
        // registry.addGhostIngredientHandler(GuiBase.class, new GhostIngredientHandler());
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        this.runtime = runtime;
    }

    public IJeiRuntime getRuntime() {
        return runtime;
    }
}
