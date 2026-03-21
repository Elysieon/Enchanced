package net.collective.enchanced.common.cca.entity;

import net.collective.enchanced.api.debugging.DebugMessages;
import net.collective.enchanced.common.cca.SyncedPlayerEntityComponent;
import net.collective.enchanced.common.index.ModEntityComponents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.ladysnake.cca.api.v3.component.ComponentKey;

public class DebugMessageComponent extends SyncedPlayerEntityComponent {
    public DebugMessageComponent(PlayerEntity playerEntity) {
        super(playerEntity);
    }

    @Override
    protected ComponentKey<? extends SyncedPlayerEntityComponent> getComponentKey() {
        return ModEntityComponents.DEBUG_MESSAGE;
    }

    @Override
    public void tick() {

    }

    @Override
    public void clientTick() {
        super.clientTick();

        if (DebugMessages.clientMessages.isEmpty()) {
            return;
        }

        DebugMessages.clientMessages.forEach(x -> {
            player().sendMessage(
                    Text.literal("[CLIENT] ").formatted(Formatting.YELLOW)
                            .append(Text.literal(x).formatted(Formatting.GRAY)),
                    false
            );
        });

        DebugMessages.clientMessages.clear();
    }

    @Override
    public void serverTick() {
        super.serverTick();

        if (DebugMessages.serverMessages.isEmpty()) {
            return;
        }

        DebugMessages.serverMessages.forEach(x -> {
            player().sendMessage(
                    Text.literal("[SERVER] ").formatted(Formatting.AQUA)
                            .append(Text.literal(x).formatted(Formatting.GRAY)),
                    false
            );
        });

        DebugMessages.serverMessages.clear();
    }

    @Override
    public void readData(ReadView readView) {
    }

    @Override
    public void writeData(WriteView writeView) {
    }
}