package net.collective.enchanced.common.index;

import net.collective.enchanced.Enchanced;
import net.collectively.geode.registration.GeodeEnchantment;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;

import java.util.Objects;

public interface EnchancedEnchantments {
    GeodeEnchantment WEAVING = Enchanced.geode.registerEnchantment("weaving");
    GeodeEnchantment STENOSIS = Enchanced.geode.registerEnchantment("stenosis");
    GeodeEnchantment SCURRY = Enchanced.geode.registerEnchantment("scurry");
    GeodeEnchantment OVERCLOCKED = Enchanced.geode.registerEnchantment("overclocked");
    GeodeEnchantment MULTISHOT = Enchanced.geode.registerEnchantment("multishot");
    GeodeEnchantment JOUST = Enchanced.geode.registerEnchantment("joust");

    GeodeEnchantment IMPALING = Enchanced.geode.registerEnchantment("impaling");

    static void init() {
    }
}
