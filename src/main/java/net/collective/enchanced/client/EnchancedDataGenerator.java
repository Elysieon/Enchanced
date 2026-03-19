package net.collective.enchanced.client;

import net.collective.enchanced.Enchanced;
import net.collective.enchanced.common.index.EnchancedEnchantments;
import net.collectively.geode.datagen.GeodeDataGeneration;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.enchantment.EnchantmentLevelBasedValue;
import net.minecraft.enchantment.effect.AttributeEnchantmentEffect;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;

import java.util.concurrent.CompletableFuture;

public class EnchancedDataGenerator implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(Generator::new);
    }

    public static class Generator extends GeodeDataGeneration {

        public Generator(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
            super(dataOutput, registriesFuture);
        }

        @Override
        protected void generate() {
            addEnchantment(EnchancedEnchantments.WEAVING)
                    .translateDescription("WIP")
                    .autoTranslate()
                    .enchantment(ItemTags.FOOT_ARMOR_ENCHANTABLE)
                    .primaryItems(ItemTags.FOOT_ARMOR_ENCHANTABLE)
                    .addSlot(AttributeModifierSlot.FEET)
                    .addEffect(EnchantmentEffectComponentTypes.ATTRIBUTES, new AttributeEnchantmentEffect(linkedModId("enchantment.weaving"), EntityAttributes.SNEAKING_SPEED, EnchantmentLevelBasedValue.linear(0.65F), EntityAttributeModifier.Operation.ADD_VALUE))
                    .build();
            addEnchantment(EnchancedEnchantments.STENOSIS)
                    .translateDescription("Increased damage when striking an enemy from behind.")
                    .autoTranslate()
                    .enchantment(ItemTags.MELEE_WEAPON_ENCHANTABLE)
                    .primaryItems(ItemTags.MELEE_WEAPON_ENCHANTABLE)
                    .build();
            addEnchantment(EnchancedEnchantments.SCURRY)
                    .translateDescription("Repeated strikes increases your attack, throw and charge speed.")
                    .autoTranslate()
                    .enchantment(ItemTags.TRIDENT_ENCHANTABLE)
                    .primaryItems(ItemTags.TRIDENT_ENCHANTABLE)
                    .build();
            addEnchantment(EnchancedEnchantments.OVERCLOCKED)
                    .translateDescription("WIP")
                    .autoTranslate()
                    .enchantment(ItemTags.MACE_ENCHANTABLE)
                    .primaryItems(ItemTags.MACE_ENCHANTABLE)
                    .build();
            addEnchantment(EnchancedEnchantments.MULTISHOT)
                    .translateDescription("Causes three arrows to shoot instead of one.")
                    .autoTranslate()
                    .enchantment(ItemTags.BOW_ENCHANTABLE)
                    .primaryItems(ItemTags.BOW_ENCHANTABLE)
                    .build();
            addEnchantment(EnchancedEnchantments.JOUST)
                    .translateDescription("Spearing is replaced with a chargeable jab.")
                    .autoTranslate()
                    .enchantment(ItemTags.SPEARS)
                    .primaryItems(ItemTags.SPEARS)
                    .build();
        }

        @Override
        public String getModId() {
            return Enchanced.MOD_ID;
        }
    }
}
