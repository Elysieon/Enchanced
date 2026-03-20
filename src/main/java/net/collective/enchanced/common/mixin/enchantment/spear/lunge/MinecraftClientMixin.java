package net.collective.enchanced.common.mixin.enchantment.spear.lunge;

import net.collective.enchanced.common.cca.entity.LungeComponent;
import net.collective.enchanced.common.index.EnchancedEnchantments;
import net.collective.enchanced.common.index.ModEntityComponents;
import net.collective.enchanced.common.payload.LungeC2SPayload;
import net.collective.enchanced.common.util.EnchantUtils;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Shadow
    @Final
    public GameOptions options;

    @Inject(method = {"handleInputEvents"}, at = {@At("HEAD")})
    private void lunge$handleInputEvents(CallbackInfo cir) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null) return;

        ItemStack activeStack = player.getActiveOrMainHandStack();
        // Has to have a stack in hand.
        if (activeStack == null || activeStack.isEmpty() || !activeStack.isIn(ItemTags.SPEARS)) return;
        // The player has to be using the item and the item has to not be on cooldown.
        if (!player.isUsingItem() || player.getItemCooldownManager().isCoolingDown(activeStack)) return;
        // The stack has to be enchanted with lunge.
        if (!EnchantUtils.hasEnchantment(player, activeStack, Enchantments.LUNGE)) return;

        while (this.options.attackKey.wasPressed()) {
            LungeComponent component = player.getComponent(ModEntityComponents.LUNGE);
            // Client activation.
            component.activateLunge(player.getActiveOrMainHandStack());
            // Server activation.
            ClientPlayNetworking.send(new LungeC2SPayload(player.getId()));
        }
    }
}
