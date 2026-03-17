package net.collective.enchanced.common.mixin.enchantment.trident.scurry;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.collective.enchanced.common.index.ModEntityComponents;
import net.collective.enchanced.common.index.EnchancedEnchantments;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TridentItem;
import net.minecraft.server.world.ServerWorld;
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
            original = original - component.getTridentChargeUp() + 10;
            component.clearHaste();
        }
        return original;
    }

    @ModifyExpressionValue(method = "onStoppedUsing",at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/ProjectileEntity;spawnWithVelocity(Lnet/minecraft/entity/projectile/ProjectileEntity$ProjectileCreator;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;FFF)Lnet/minecraft/entity/projectile/ProjectileEntity;"))
    private static <T extends ProjectileEntity> T scurry$spawnWithVelocity(T original) {
        if (original.getOwner() != null && original.getOwner().getEntity() instanceof LivingEntity livingEntity) {
            var component = livingEntity.getComponent(ModEntityComponents.HASTE);
            var speed = component.getHasteMultiplier() > 1 ? 2.5F + component.getHasteMultiplier() - 1 : 2.5F;
            original.setVelocity(livingEntity.getRotationVector().multiply(speed));
        }
        return original;
    }
}
