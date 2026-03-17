package net.collective.enchanced.common.index;

import moriyashiine.enchancement.common.Enchancement;
import moriyashiine.enchancement.common.component.entity.GroundedCooldownComponent;
import net.collective.enchanced.Enchanced;
import net.collective.enchanced.common.cca.GroundLayedCooldownComponent;
import net.collective.enchanced.common.cca.HasteComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;

public class ModEntityComponents implements EntityComponentInitializer {
    public static final ComponentKey<HasteComponent> HASTE = ComponentRegistry.getOrCreate(Enchanced.id("haste"), HasteComponent.class);
    public static final ComponentKey<GroundLayedCooldownComponent> GROUNDLAYEDCOOLDOWN = ComponentRegistry.getOrCreate(Enchanced.id("groundlayedcooldown"), GroundLayedCooldownComponent.class);

    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.beginRegistration(LivingEntity.class, HASTE).respawnStrategy(RespawnCopyStrategy.NEVER_COPY).end(HasteComponent::new);
        registry.beginRegistration(PlayerEntity.class, GROUNDLAYEDCOOLDOWN).respawnStrategy(RespawnCopyStrategy.NEVER_COPY).end(GroundLayedCooldownComponent::new);

    }
}