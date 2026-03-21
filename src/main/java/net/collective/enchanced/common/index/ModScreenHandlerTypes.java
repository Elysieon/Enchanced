package net.collective.enchanced.common.index;

import net.collective.enchanced.Enchanced;
import net.collective.enchanced.common.gui.AnvilScreenHandler;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;

public interface ModScreenHandlerTypes {
    ScreenHandlerType<AnvilScreenHandler> ANVIL = Registry.register(
            Registries.SCREEN_HANDLER,
            Enchanced.id("anvil"),
            new ScreenHandlerType<>(AnvilScreenHandler::new, FeatureFlags.VANILLA_FEATURES)
    );

    static void init() {}
}
