package net.collective.enchanced.common.mixin.rebalanced.crossbow.scatter;

import moriyashiine.enchancement.common.enchantment.effect.ScatterShotEffect;
import net.collective.enchanced.EnchancedConfig;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;

@Mixin(RangedWeaponItem.class)
public class RangedWeaponItemMixin {
    @ModifyVariable(
            method = "shootAll",
            at = @At("HEAD"),
            ordinal = 1,
            argsOnly = true,
            order = 1100
    )
    private float enchanced$scatterShot$modifyDivergence(float value, ServerWorld world, LivingEntity shooter, Hand hand, ItemStack stack, List<ItemStack> projectiles) {
        return ScatterShotEffect.hasScatterShot ? EnchancedConfig.scatterDivergence : value;
    }
}
