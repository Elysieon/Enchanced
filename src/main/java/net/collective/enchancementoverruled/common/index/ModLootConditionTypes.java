package net.collective.enchancementoverruled.common.index;

import com.mojang.serialization.MapCodec;
import net.collective.enchancementoverruled.Enchancementoverruled;
import net.collective.enchancementoverruled.common.loot.condition.UsingItemLootCondition;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public interface ModLootConditionTypes {
    LootConditionType USING_ITEM = registerLootConditionType("using_item", UsingItemLootCondition.CODEC);

    static void init() {}

    static LootConditionType registerLootConditionType(String name, MapCodec<? extends LootCondition> codec) {
        return Registry.register(Registries.LOOT_CONDITION_TYPE, Enchancementoverruled.id(name), new LootConditionType(codec));
    }
}
