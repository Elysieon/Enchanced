package net.collective.enchanced.common.util;

import moriyashiine.enchancement.common.ModConfig;
import moriyashiine.enchancement.common.init.ModEnchantments;
import net.collectively.geode.registration.GeodeEnchantment;
import net.fabricmc.fabric.api.item.v1.EnchantingContext;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.world.RegistryWorldView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public interface EnchantUtils {
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

    static boolean hasEnchantment(RegistryEntryLookup.RegistryLookup registryLookup, ItemStack itemStack, RegistryKey<Enchantment> enchantment) {
        var enchantmentRegistry = registryLookup.getEntryOrThrow(enchantment);
        return EnchantmentHelper.getLevel(enchantmentRegistry, itemStack) > 0;
    }

    static boolean hasEnchantment(RegistryEntryLookup.RegistryLookup registryLookup, ItemStack itemStack, GeodeEnchantment enchantment) {return hasEnchantment(registryLookup, itemStack, enchantment.registryKey());}

    static boolean hasEnchantment(Entity registryProvider, ItemStack itemStack, RegistryKey<Enchantment> enchantment) {return hasEnchantment(registryProvider.getRegistryManager(), itemStack, enchantment);}
    static boolean hasEnchantment(Entity registryProvider, ItemStack itemStack, GeodeEnchantment enchantment) {return hasEnchantment(registryProvider.getRegistryManager(), itemStack, enchantment);}

    static boolean hasEnchantment(RegistryWorldView registryProvider, ItemStack itemStack, RegistryKey<Enchantment> enchantment) {return hasEnchantment(registryProvider.getRegistryManager(), itemStack, enchantment);}
    static boolean hasEnchantment(RegistryWorldView registryProvider, ItemStack itemStack, GeodeEnchantment enchantment) {return hasEnchantment(registryProvider.getRegistryManager(), itemStack, enchantment);}
}
