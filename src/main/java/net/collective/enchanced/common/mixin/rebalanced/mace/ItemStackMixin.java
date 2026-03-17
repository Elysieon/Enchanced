package net.collective.enchanced.common.mixin.rebalanced.mace;

import moriyashiine.enchancement.common.util.EnchancementUtil;
import moriyashiine.enchancement.common.util.enchantment.MaceEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MaceItem;
import net.minecraft.stat.Stats;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    // Fightning Moriya's Code Episode 1: Mace Cooldown War
    @Shadow
    public abstract Item getItem();

    @Shadow
    public abstract int getMaxUseTime(LivingEntity var1);

    @Inject(
            method = {"onStoppedUsing"},
            at = {@At("HEAD")},
            cancellable = true,
            order = 1
    )
    private void enchanced$Rebalanced(World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        if (user instanceof PlayerEntity player) {
            for(MaceEffect effect : MaceEffect.EFFECTS) {
                if (effect.isUsing(player)) {
                    int useTime = this.getMaxUseTime(user) - remainingUseTicks;
                    if (useTime >= EnchancementUtil.getTridentChargeTime()) {
                        player.incrementStat(Stats.USED.getOrCreateStat(this.getItem()));
                        effect.use(world, player, player.getActiveItem());
                    }
                    ci.cancel();
                    return;
                }
            }
        }

    }
}
