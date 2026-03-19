package net.collective.enchanced.common.mixin.rebalanced.crossbow;

import moriyashiine.enchancement.common.init.ModEnchantments;
import net.collective.enchanced.Enchanced;
import net.collective.enchanced.common.index.EnchancedEnchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.CrossbowItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(CrossbowItem.class)
public class CrossbowItemMixin {
    @ModifyVariable(method = "shoot", at = @At("HEAD"), ordinal = 1, argsOnly = true)
    private float shoot(float divergence, LivingEntity shooter, ProjectileEntity projectile) {
        if (projectile.getWeaponStack() != null && EnchancedEnchantments.hasEnchantment(shooter.getRegistryManager(), projectile.getWeaponStack(), ModEnchantments.SCATTER)) {
            return Enchanced.getScatterDivergence();
        }

        return divergence;
    }
}
