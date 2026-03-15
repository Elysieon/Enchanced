package net.collective.enchancementoverruled.common.loot.condition;

import com.mojang.serialization.MapCodec;
import moriyashiine.enchancement.common.init.ModLootConditionTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;

public class UsingItemLootCondition implements LootCondition {
    public static final UsingItemLootCondition INSTANCE = new UsingItemLootCondition();
    public static final MapCodec<UsingItemLootCondition> CODEC = MapCodec.unit(INSTANCE);

    private UsingItemLootCondition() {
    }

    public LootConditionType getType() {
        return ModLootConditionTypes.WET;
    }

    public boolean test(LootContext context) {
        Entity entity = context.get(LootContextParameters.THIS_ENTITY);
        return entity instanceof LivingEntity livingEntity && livingEntity.isUsingItem();
    }
}