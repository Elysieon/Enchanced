package net.collective.enchanced.common.util;

import moriyashiine.enchancement.common.ModConfig;
import moriyashiine.enchancement.common.init.ModEnchantments;
import net.fabricmc.fabric.api.item.v1.EnchantingContext;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EnchantmentTags;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public interface EnchantingHelper {
    static boolean canApplyEnchantment(RegistryEntry<Enchantment> enchantment, ItemStack itemStack) {
        return !itemStack.isEmpty()
                && itemStack.canBeEnchantedWith(enchantment, EnchantingContext.ACCEPTABLE)
                && ((ModConfig.overhaulEnchanting.allowsTreasure() && enchantment.isIn(EnchantmentTags.TREASURE)) || enchantment.isIn(EnchantmentTags.IN_ENCHANTING_TABLE));
    }

    static List<RegistryEntry.Reference<Enchantment>> getAllEnchantmentsForStack(DynamicRegistryManager registryManager, Predicate<RegistryEntry.Reference<Enchantment>> isUnlocked, ItemStack itemStack) {
        List<RegistryEntry.Reference<Enchantment>> allEnchantments = new ArrayList<>(registryManager.getOrThrow(RegistryKeys.ENCHANTMENT).streamEntries().toList());
        return filterEnchantments(allEnchantments, isUnlocked, itemStack);
    }

    static List<RegistryEntry.Reference<Enchantment>> filterEnchantments(List<RegistryEntry.Reference<Enchantment>> list, Predicate<RegistryEntry.Reference<Enchantment>> isUnlocked, ItemStack itemStack) {
        list.removeIf(enchantment -> !canApplyEnchantment(enchantment, itemStack));
        list.sort(Comparator.comparing(enchantment -> enchantment.getKey().orElse(ModEnchantments.EMPTY_KEY).getValue().getPath()));
        list.sort(Comparator.comparing(t -> !isUnlocked.test(t)));
        return list;
    }
}
