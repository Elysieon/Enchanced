package net.collective.enchanced.client;

import net.collective.enchanced.client.init.ModCustomPoses;
import net.collective.enchanced.client.render.enchantments.weaving.hud.WeavingHudElement;
import net.collective.enchanced.client.render.entity.thrown_spear.ThrownSpearEntityHitboxRenderer;
import net.collective.enchanced.client.render.entity.thrown_spear.ThrownSpearEntityRenderer;
import net.collective.enchanced.common.entity.ThrownSpearEntity;
import net.collective.enchanced.common.index.ModEntityTypes;
import net.collective.enchanced.common.payload.ThrownSpearSyncS2CPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.impl.client.rendering.EntityRendererRegistryImpl;
import net.minecraft.world.World;

public class EnchancedClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModCustomPoses.init();

        HudElementRegistry.addFirst(WeavingHudElement.IDENTIFIER, new WeavingHudElement());

        EntityRendererRegistryImpl.register(ModEntityTypes.THROWN_SPEAR, ThrownSpearEntityRenderer::new);
        EntityRendererRegistryImpl.register(ModEntityTypes.THROWN_SPEAR_HITBOX, ThrownSpearEntityHitboxRenderer::new);

        ClientPlayNetworking.registerGlobalReceiver(ThrownSpearSyncS2CPayload.ID, EnchancedClient::receiveThrownSpearSyncS2C);
    }

    private static void receiveThrownSpearSyncS2C(ThrownSpearSyncS2CPayload payload, ClientPlayNetworking.Context context) {
        if (context.player().getEntityWorld().getEntityById(payload.id()) instanceof ThrownSpearEntity thrownSpearEntity) {
            thrownSpearEntity.setRenderedItemStack(payload.itemStack());
        }
    }
}
