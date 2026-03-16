package net.collective.enchanced.common.mixin.enchantment.trident.scurry;

import net.collective.enchanced.common.index.ModEntityComponents;
import net.collective.enchanced.common.index.EnchancedEnchantments;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TridentItem;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Objects;

@Mixin(TridentItem.class)
public class TridentItemMixin {
    @ModifyVariable(
            method = {"onStoppedUsing"}, at = @At("HEAD"), argsOnly = true)
    private int scurry$onStoppedUsing(int original, ItemStack itemStack, World world, LivingEntity user) {
        var enchantmentReference = world.getRegistryManager().getEntryOrThrow(EnchancedEnchantments.SCURRY.registryKey());
        int level = EnchantmentHelper.getLevel(enchantmentReference, Objects.requireNonNull(user.getActiveOrMainHandStack()));
        if (level > 0 && user instanceof PlayerEntity player) {
            var component = player.getComponent(ModEntityComponents.HASTE);
            original = (int) (original - component.getHasteMultiplier() * 2);
        }
        return original;
    }
}
