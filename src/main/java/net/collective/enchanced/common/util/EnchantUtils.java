package net.collective.enchanced.common.util;

import moriyashiine.enchancement.common.ModConfig;
import moriyashiine.enchancement.common.init.ModEnchantments;
import moriyashiine.enchancement.common.util.config.OverhaulMode;
import net.collective.enchanced.api.debugging.DebugMessages;
import net.collectively.geode.debug.Draw;
import net.collectively.geode.registration.GeodeEnchantment;
import net.collectively.geode.types.double3;
import net.fabricmc.fabric.api.item.v1.EnchantingContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RegistryWorldView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
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

    static boolean hasEnchantment(RegistryEntryLookup.RegistryLookup registryLookup, ItemStack itemStack, GeodeEnchantment enchantment) {
        return hasEnchantment(registryLookup, itemStack, enchantment.registryKey());
    }

    static boolean hasEnchantment(Entity registryProvider, ItemStack itemStack, RegistryKey<Enchantment> enchantment) {
        return hasEnchantment(registryProvider.getRegistryManager(), itemStack, enchantment);
    }

    static boolean hasEnchantment(Entity registryProvider, ItemStack itemStack, GeodeEnchantment enchantment) {
        return hasEnchantment(registryProvider.getRegistryManager(), itemStack, enchantment);
    }

    static boolean hasEnchantment(RegistryWorldView registryProvider, ItemStack itemStack, RegistryKey<Enchantment> enchantment) {
        return hasEnchantment(registryProvider.getRegistryManager(), itemStack, enchantment);
    }

    static boolean hasEnchantment(RegistryWorldView registryProvider, ItemStack itemStack, GeodeEnchantment enchantment) {
        return hasEnchantment(registryProvider.getRegistryManager(), itemStack, enchantment);
    }

    static boolean isVisiblePowerSource(World world, BlockPos tablePos, BlockPos sourcePos, @Nullable ServerPlayerEntity player) {
        BlockState sourceState = world.getBlockState(sourcePos);

        if (!sourceState.isIn(BlockTags.ENCHANTMENT_POWER_PROVIDER) && !(ModConfig.overhaulEnchanting != OverhaulMode.DISABLED && sourceState.isOf(Blocks.CHISELED_BOOKSHELF))) {
            return false;
        }

        double3 start = new double3(tablePos.toCenterPos());
        double3 end = new double3(sourcePos.toCenterPos());

        start = start.add(end.sub(start).normalize().mul(0.75));
        end = end.add(Direction.getFacing(start.sub(end).normalize().withY(0).toVec3d()).getDoubleVector().multiply(0.4));
        // Draw.box(end, new double3(0.1), 0xFFff55ff);

        RaycastContext ctx = new RaycastContext(
                start.toVec3d(), end.toVec3d(),
                RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE,
                Optional.ofNullable(player).map(ShapeContext::of).orElse(ShapeContext.absent())
        );

        if (world.raycast(ctx) instanceof BlockHitResult blockHitResult && blockHitResult.getType() != HitResult.Type.MISS) {
            boolean hasHit = blockHitResult.getBlockPos().equals(sourcePos);
            // Draw.box(new double3(blockHitResult.getBlockPos().toCenterPos()), new double3(1.05), hasHit ? 0xFF55ff55 : 0xFFff5555);
            // Draw.line(start, end, hasHit ? 0x4455FF55 : 0xFFffff55);
            return hasHit;
        }

        // Draw.line(start, end, 0x22ff5555);
        return false;
    }
}
