package net.collective.enchanced.common.mixin.enchantment.trident.scurry;

import net.collective.enchanced.common.index.ModEntityComponents;
import net.collective.enchanced.common.index.EnchancedEnchantments;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

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

    @Inject(method = "modifyAppliedDamage", at = @At("HEAD"), cancellable = true)
    public void haste$modifyAppliedDamage(DamageSource source, float amount, CallbackInfoReturnable<Float> cir) {
        var damage = amount;
        if (source.getAttacker() instanceof PlayerEntity living) {
            if (living.getEntityWorld() instanceof ServerWorld serverWorld) {
                if (living.getAttributes().getValue(EntityAttributes.ENTITY_INTERACTION_RANGE) >= living.distanceTo(this)) {

                    var hasteRegistry = serverWorld.getRegistryManager().getEntryOrThrow(EnchancedEnchantments.SCURRY.registryKey());
                    int level = EnchantmentHelper.getLevel(hasteRegistry, Objects.requireNonNull(living.getActiveOrMainHandStack()));
                    if (level > 0) {
                        var component = living.getComponent(ModEntityComponents.HASTE);
                        component.addHaste();
                        cir.setReturnValue(damage);

                    }
                }
            }
        }
    }

}
