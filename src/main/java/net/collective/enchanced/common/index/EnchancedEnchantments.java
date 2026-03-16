package net.collective.enchanced.common.index;

import net.collective.enchanced.Enchanced;
import net.collectively.geode.registration.GeodeEnchantment;

public interface EnchancedEnchantments {
    GeodeEnchantment WEAVING = Enchanced.geode.registerEnchantment("weaving");
    GeodeEnchantment STENOSIS = Enchanced.geode.registerEnchantment("stenosis");
    GeodeEnchantment SCURRY = Enchanced.geode.registerEnchantment("scurry");
    static void init() {
    }
}
