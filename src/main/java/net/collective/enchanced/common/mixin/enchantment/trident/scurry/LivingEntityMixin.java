package net.collective.enchanced.common.mixin.enchantment.trident.scurry;

import net.collective.enchanced.common.cca.entity.ScurryComponent;
import net.collective.enchanced.common.index.ModEntityComponents;
import net.collective.enchanced.common.index.EnchancedEnchantments;
import net.collective.enchanced.common.util.ItemStackUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import org.jspecify.annotations.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    @Shadow
    public abstract float getHeadYaw();

    @Shadow
    public abstract @Nullable LivingEntity getEntity();

    @Shadow
    public abstract ItemStack getWeaponStack();

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "modifyAppliedDamage", at = @At("HEAD"))
    public void haste$modifyAppliedDamage(DamageSource source, float amount, CallbackInfoReturnable<Float> cir) {
        if (amount < 2.2) {
            return;
        }

        if (source.getAttacker() instanceof PlayerEntity attacker) {
            ItemStack weaponStack = source.getWeaponStack();

            if (ItemStackUtil.isNullOrEmpty(weaponStack)) weaponStack = attacker.getMainHandStack();
            if (ItemStackUtil.isNullOrEmpty(weaponStack)) weaponStack = attacker.getOffHandStack();

            if (ItemStackUtil.isNullOrEmpty(weaponStack)) {
                return;
            }

            double interactionRange = attacker.getAttributes().getValue(EntityAttributes.ENTITY_INTERACTION_RANGE);
            if (interactionRange >= attacker.distanceTo(this)) {
                if (EnchancedEnchantments.hasEnchantment(attacker.getRegistryManager(), weaponStack, EnchancedEnchantments.SCURRY)) {
                    ScurryComponent component = attacker.getComponent(ModEntityComponents.SCURRY);
                    component.increaseScurry();
                }
            }
        }
    }
}