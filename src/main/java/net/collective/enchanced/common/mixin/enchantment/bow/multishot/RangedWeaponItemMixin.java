package net.collective.enchanced.common.mixin.enchantment.bow.multishot;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.collective.enchanced.common.index.EnchancedEnchantments;
import net.collective.enchanced.common.util.EnchantUtils;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(RangedWeaponItem.class)
public class RangedWeaponItemMixin {
    @ModifyExpressionValue(
            method = "shootAll",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/enchantment/EnchantmentHelper;getProjectileSpread(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/Entity;F)F"
            )
    )
    private float enchanced$shootAll(float original,
                                     ServerWorld world,
                                     LivingEntity shooter,
                                     Hand hand,
                                     ItemStack stack,
                                     List<ItemStack> projectiles,
                                     float speed,
                                     float divergence,
                                     boolean critical,
                                     @Nullable LivingEntity target) {
        if (!EnchantUtils.hasEnchantment(world, stack, EnchancedEnchantments.MULTISHOT)) {
            return original;
        }

        return original + 10f;
    }
}
