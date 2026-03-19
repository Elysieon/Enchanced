package net.collective.enchanced.common.index;

import net.collective.enchanced.Enchanced;
import net.collective.enchanced.common.cca.entity.GroundLayedCooldownComponent;
import net.collective.enchanced.common.cca.entity.ScurryComponent;
import net.collective.enchanced.common.cca.entity.JoustComponent;
import net.collective.enchanced.common.cca.entity.LungeComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;

public class ModEntityComponents implements EntityComponentInitializer {
    public static final ComponentKey<ScurryComponent> SCURRY = ComponentRegistry.getOrCreate(Enchanced.id("scurry"), ScurryComponent.class);
    public static final ComponentKey<GroundLayedCooldownComponent> GROUNDLAYEDCOOLDOWN = ComponentRegistry.getOrCreate(Enchanced.id("groundlayedcooldown"), GroundLayedCooldownComponent.class);
    public static final ComponentKey<LungeComponent> LUNGE = ComponentRegistry.getOrCreate(Enchanced.id("lunge"), LungeComponent.class);
    public static final ComponentKey<JoustComponent> JOUST = ComponentRegistry.getOrCreate(Enchanced.id("joust"), JoustComponent.class);

    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.beginRegistration(LivingEntity.class, SCURRY).respawnStrategy(RespawnCopyStrategy.NEVER_COPY).end(ScurryComponent::new);
        registry.beginRegistration(PlayerEntity.class, GROUNDLAYEDCOOLDOWN).respawnStrategy(RespawnCopyStrategy.NEVER_COPY).end(GroundLayedCooldownComponent::new);
        registry.beginRegistration(PlayerEntity.class, LUNGE).respawnStrategy(RespawnCopyStrategy.NEVER_COPY).end(LungeComponent::new);
        registry.beginRegistration(PlayerEntity.class, JOUST).respawnStrategy(RespawnCopyStrategy.NEVER_COPY).end(JoustComponent::new);

    }
}