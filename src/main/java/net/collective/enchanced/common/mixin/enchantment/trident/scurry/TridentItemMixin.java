package net.collective.enchanced.common.mixin.enchantment.trident.scurry;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.collective.enchanced.common.cca.entity.ScurryComponent;
import net.collective.enchanced.common.index.ModEntityComponents;
import net.collective.enchanced.common.index.EnchancedEnchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TridentItem;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(TridentItem.class)
public class TridentItemMixin {
    @ModifyVariable(
            method = "onStoppedUsing",
            at = @At("HEAD"),
            argsOnly = true
    )
    private int scurry$onStoppedUsing(int original, ItemStack itemStack, World world, LivingEntity user) {
        if (user instanceof PlayerEntity player && EnchancedEnchantments.hasEnchantment(world.getRegistryManager(), itemStack, EnchancedEnchantments.SCURRY)) {
            ScurryComponent component = player.getComponent(ModEntityComponents.SCURRY);
            original -= component.tridentChargeUp() + 10;
            component.clearScurry();
        }

        return original;
    }

    @ModifyExpressionValue(
            method = "onStoppedUsing",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/projectile/ProjectileEntity;spawnWithVelocity(Lnet/minecraft/entity/projectile/ProjectileEntity$ProjectileCreator;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;FFF)Lnet/minecraft/entity/projectile/ProjectileEntity;"
            )
    )
    private static <T extends ProjectileEntity> T scurry$spawnWithVelocity(T original) {
        Entity owner = original.getOwner();

        if (owner != null && owner.getEntity() instanceof LivingEntity livingEntity) {
            ScurryComponent component = livingEntity.getComponent(ModEntityComponents.SCURRY);

            double speed = component.multiplier() > 1
                    ? 2.5F + component.multiplier() - 1
                    : 2.5F;

            original.setVelocity(livingEntity.getRotationVector().multiply(speed));
        }

        return original;
    }
}
