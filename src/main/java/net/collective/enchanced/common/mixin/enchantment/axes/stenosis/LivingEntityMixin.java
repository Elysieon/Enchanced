package net.collective.enchanced.common.mixin.enchantment.axes.stenosis;

import net.collective.enchanced.common.index.ModSoundEvent;
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
import net.minecraft.util.math.MathHelper;
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
    public void stenosis$modifyAppliedDamage(DamageSource source, float amount, CallbackInfoReturnable<Float> cir) {
        var damage = amount;
        if (source.getAttacker() instanceof PlayerEntity living) {
            if (living.getEntityWorld() instanceof ServerWorld serverWorld) {
                if (living.getAttributes().getValue(EntityAttributes.ENTITY_INTERACTION_RANGE) >= living.distanceTo(this)) {

                    var stenosisRegistry = serverWorld.getRegistryManager().getEntryOrThrow(EnchancedEnchantments.STENOSIS.registryKey());
                    int level = EnchantmentHelper.getLevel(stenosisRegistry, Objects.requireNonNull(living.getActiveOrMainHandStack()));
                    if (level > 0 && Math.abs(MathHelper.subtractAngles(this.getHeadYaw(), source.getSource().getHeadYaw())) <= 75.0F) {
                        this.getEntityWorld().playSound(null, this.getBlockPos(), ModSoundEvent.ENTITY_GENERIC_STENOSIS, this.getSoundCategory(), 0.35F + (amount / 35),MathHelper.nextFloat(serverWorld.getRandom(), .9F,1.15F));
                        damage = damage * 1.5F;
                        cir.setReturnValue(damage);
                    }
                }
            }
        }
    }

}
