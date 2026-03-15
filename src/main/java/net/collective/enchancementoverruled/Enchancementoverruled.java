package net.collective.enchancementoverruled;

import moriyashiine.enchancement.common.Enchancement;
import moriyashiine.enchancement.common.ModConfig;
import moriyashiine.enchancement.common.init.ModEnchantments;
import net.collective.enchancementoverruled.common.index.ModLootConditionTypes;
import net.collective.enchancementoverruled.common.index.ModSoundEvent;
import net.collective.enchancementoverruled.common.index.OverruledEnchantments;
import net.collectively.geode.Geode;
import net.fabricmc.api.ModInitializer;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.util.Identifier;

public class Enchancementoverruled implements ModInitializer {
    public static final String MOD_ID = "enchancementoverruled";
    public static final Geode geode = Geode.create(MOD_ID);

    public static Identifier id(String identifier) {
        return Identifier.of(MOD_ID, identifier);
    }

    @Override
    public void onInitialize() {
        Enchancement.LOGGER.info("Loading Enchancement");

        // Init
        OverruledEnchantments.init();
        ModSoundEvent.init();
        ModLootConditionTypes.init();

        // Remove Enchantments!
        if (!ModConfig.disallowedEnchantments.contains(Enchantments.FIRE_ASPECT.getValue().toString())) ModConfig.disallowedEnchantments.add(Enchantments.FIRE_ASPECT.getValue().toString());
        if (!ModConfig.disallowedEnchantments.contains(ModEnchantments.STICKY.getValue().toString())) ModConfig.disallowedEnchantments.add(ModEnchantments.STICKY.getValue().toString());

    }
}
