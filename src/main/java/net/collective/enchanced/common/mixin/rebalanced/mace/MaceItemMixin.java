package net.collective.enchanced.common.mixin.rebalanced.mace;

import moriyashiine.enchancement.common.component.entity.GroundedCooldownComponent;
import moriyashiine.enchancement.common.init.ModEntityComponents;
import net.collective.enchanced.common.index.EnchancedEnchantments;
import net.collective.enchanced.common.util.EnchantUtils;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MaceItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(MaceItem.class)
public class MaceItemMixin {
    @Unique
    @Final
    private static final int MACE_COOLDOWN = 440;

    @Inject(method = "postDamageEntity", at = @At("HEAD"))
    private void enchanced$Rebalanced(ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfo ci) {
        if (attacker instanceof PlayerEntity player) {
            if (EnchantUtils.hasEnchantment(player, stack, Enchantments.WIND_BURST)) {
                (net.collective.enchanced.common.index.ModEntityComponents.GROUNDLAYEDCOOLDOWN.get(player)).putOnCooldown(stack, MACE_COOLDOWN);
                return;
            }

            (ModEntityComponents.GROUNDED_COOLDOWN.get(player)).putOnCooldown(stack, MACE_COOLDOWN);
        }
    }

}
