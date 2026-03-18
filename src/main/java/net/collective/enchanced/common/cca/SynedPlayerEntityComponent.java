package net.collective.enchanced.common.cca;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.ladysnake.cca.api.v3.component.Component;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.CommonTickingComponent;

public abstract class SynedPlayerEntityComponent implements Component, AutoSyncedComponent, CommonTickingComponent {
    private final PlayerEntity playerEntity;

    public SynedPlayerEntityComponent(PlayerEntity playerEntity) {
        this.playerEntity = playerEntity;
    }

    // region ---- RECORD ACCESSOR ----

    public PlayerEntity player() {
        return playerEntity;
    }

    public World world() {
        return playerEntity.getEntityWorld();
    }

    // endregion

    // region -------- SYNCING --------

    protected abstract ComponentKey<? extends SynedPlayerEntityComponent> getComponentKey();

    public final void sync(LivingEntity target) {
        getComponentKey().sync(target);
    }

    public final void sync() {
        sync(playerEntity);
    }

    // endregion
}