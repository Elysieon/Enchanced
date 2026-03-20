package net.collective.enchanced.common.index;

import net.collective.enchanced.common.entity.ThrownSpearEntity;
import net.collective.enchanced.common.entity.ThrownSpearEntityHitbox;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;

import static net.collective.enchanced.Enchanced.geode;

public interface ModEntityTypes {
    EntityType<ThrownSpearEntity> THROWN_SPEAR = geode.registerEntity(
            "thrown_spear",
            EntityType.Builder.<ThrownSpearEntity>create(ThrownSpearEntity::new, SpawnGroup.MISC)
                    .dimensions(0.5f, 0.5f)
                    .eyeHeight(0.115f)
                    .trackingTickInterval(15)
                    .trackingTickInterval(2)
    );
    EntityType<ThrownSpearEntityHitbox> THROWN_SPEAR_HITBOX = geode.registerEntity(
            "thrown_spear_hitbox",
            EntityType.Builder.<ThrownSpearEntityHitbox>create(ThrownSpearEntityHitbox::new, SpawnGroup.MISC)
                    .dimensions(0.35f, 0.35f)
                    .eyeHeight(0.115f)
                    .trackingTickInterval(15)
                    .trackingTickInterval(2)
                    .disableSummon()
    );

    static void init() {
    }
}
