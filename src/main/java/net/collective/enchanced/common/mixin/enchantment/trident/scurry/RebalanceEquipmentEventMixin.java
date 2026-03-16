package net.collective.enchanced.common.mixin.enchantment.trident.scurry;

import moriyashiine.enchancement.common.ModConfig;
import moriyashiine.enchancement.common.event.config.RebalanceEquipmentEvent;
import moriyashiine.enchancement.common.init.ModSoundEvents;
import moriyashiine.enchancement.common.util.EnchancementUtil;
import moriyashiine.enchancement.common.util.enchantment.MaceEffect;
import moriyashiine.strawberrylib.api.module.SLibUtils;
import net.collective.enchanced.common.index.EnchancedEnchantments;
import net.collective.enchanced.common.index.ModEntityComponents;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.item.TridentItem;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(RebalanceEquipmentEvent.Tick.class)
public class RebalanceEquipmentEventMixin {

    @Unique
    private static boolean isValid(PlayerEntity player) {
        return player.getActiveItem().getItem() instanceof TridentItem || MaceEffect.EFFECTS.stream().anyMatch((effect) -> effect.isUsing(player));
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void scurry$tick(ServerWorld world, Entity entity, CallbackInfo ci) {
        if (ModConfig.rebalanceEquipment && entity instanceof PlayerEntity player) {
            var enchantmentReference = world.getRegistryManager().getEntryOrThrow(EnchancedEnchantments.SCURRY.registryKey());
            int level = EnchantmentHelper.getLevel(enchantmentReference, Objects.requireNonNull(player.getActiveOrMainHandStack()));
            if (level > 0) {
                var component = player.getComponent(ModEntityComponents.HASTE);
                if (player.getItemUseTime() == EnchancementUtil.getTridentChargeTime() - component.getHasteMultiplier() * 2 && isValid(player)) {
                    SLibUtils.playSound(entity, ModSoundEvents.ENTITY_GENERIC_PING);
                    ci.cancel();
                }
            }
        }
    }




}
