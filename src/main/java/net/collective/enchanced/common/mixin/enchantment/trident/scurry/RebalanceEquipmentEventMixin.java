package net.collective.enchanced.common.mixin.enchantment.trident.scurry;

import moriyashiine.enchancement.common.ModConfig;
import moriyashiine.enchancement.common.event.config.RebalanceEquipmentEvent;
import moriyashiine.enchancement.common.init.ModSoundEvents;
import moriyashiine.enchancement.common.util.EnchancementUtil;
import moriyashiine.enchancement.common.util.enchantment.MaceEffect;
import moriyashiine.strawberrylib.api.module.SLibUtils;
import net.collective.enchanced.common.cca.entity.ScurryComponent;
import net.collective.enchanced.common.index.EnchancedEnchantments;
import net.collective.enchanced.common.index.ModEntityComponents;
import net.collectively.geode.math.math;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TridentItem;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RebalanceEquipmentEvent.Tick.class)
public class RebalanceEquipmentEventMixin {
    @Unique
    private static boolean isValid(PlayerEntity player) {
        if (player.getActiveItem().getItem() instanceof TridentItem) {
            return true;
        }

        return MaceEffect.EFFECTS
                .stream()
                .anyMatch(effect -> effect.isUsing(player));
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void scurry$tick(ServerWorld world, Entity entity, CallbackInfo ci) {
        if (ModConfig.rebalanceEquipment && entity instanceof PlayerEntity player) {
            ItemStack itemStack = player.getActiveOrMainHandStack();
            if (EnchancedEnchantments.hasEnchantment(world.getRegistryManager(), itemStack, EnchancedEnchantments.SCURRY)) {
                ScurryComponent component = player.getComponent(ModEntityComponents.SCURRY);
                int scurryChargeTime = math.round(EnchancementUtil.getTridentChargeTime() - component.tridentChargeUp() + 10);

                if (scurryChargeTime < 0) {
                    scurryChargeTime = 0;
                }

                // TODO: Remove Debugging code, AFTER you've added a way to tell a different way.

                if (player.getItemUseTime() == scurryChargeTime && isValid(player)) {
                    SLibUtils.playSound(entity, ModSoundEvents.ENTITY_GENERIC_PING, 1f, (float) (component.multiplier() / 2d));
                }

                ci.cancel();
            }
        }
    }
}
