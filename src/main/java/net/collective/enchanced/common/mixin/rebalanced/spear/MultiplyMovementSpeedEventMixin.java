package net.collective.enchanced.common.mixin.rebalanced.spear;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import moriyashiine.enchancement.api.event.MultiplyMovementSpeedEvent;
import net.collective.enchanced.common.index.EnchancedEnchantments;
import net.collective.enchanced.common.util.EnchantUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.tag.ItemTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MultiplyMovementSpeedEvent.class)
public interface MultiplyMovementSpeedEventMixin {
    @WrapOperation(
            method = "getMovementMultiplier",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;isUsingItem()Z"
            )
    )
    private static boolean spear$getMovementMultiplier(LivingEntity instance, Operation<Boolean> original) {
        if (instance.isUsingItem() && instance.getActiveOrMainHandStack().isIn(ItemTags.SPEARS)) {
            return false;
        }

        return original.call(instance);
    }
}
