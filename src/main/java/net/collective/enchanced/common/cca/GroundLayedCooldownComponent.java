package net.collective.enchanced.common.cca;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import moriyashiine.enchancement.common.init.ModEntityComponents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import org.ladysnake.cca.api.v3.component.tick.CommonTickingComponent;

public class GroundLayedCooldownComponent implements CommonTickingComponent {
    private static final int MAX_WAIT_TICKS = 5;
    private final PlayerEntity obj;
    private final List<GroundLayedCooldown> cooldowns = new ArrayList();
    private int airTicks = 0;
    private int waitTicks = 0;

    public GroundLayedCooldownComponent(PlayerEntity obj) {
        this.obj = obj;
    }

    public void readData(ReadView readView) {
        this.cooldowns.clear();
        this.cooldowns.addAll((Collection)readView.read("Cooldowns", GroundLayedCooldown.CODEC.listOf()).orElse(List.of()));
        this.airTicks = readView.getInt("AirTicks", 0);
        this.waitTicks = readView.getInt("WaitTicks", 0);
    }

    public void writeData(WriteView writeView) {
        writeView.put("Cooldowns", GroundLayedCooldown.CODEC.listOf(), this.cooldowns);
        writeView.putInt("AirTicks", this.airTicks);
        writeView.putInt("WaitTicks", this.waitTicks);
    }

    public void tick() {
        if (!this.cooldowns.isEmpty()) {
            if (this.obj.isOnGround()) {
                this.cooldowns.forEach((groundlayedCooldown) -> this.obj.getItemCooldownManager().set(groundlayedCooldown.stack(), groundlayedCooldown.cooldown()));
                this.airTicks = 0;
                ++this.waitTicks;
                if (this.waitTicks >= 5) {
                    this.cooldowns.clear();
                }
            } else if (++this.airTicks >= 5) {
                this.waitTicks = 5;
            }
        } else {
            this.airTicks = this.waitTicks = 0;
        }

    }

    public void putOnCooldown(ItemStack stack, int cooldown) {
        this.cooldowns.add(new GroundLayedCooldown(stack, cooldown));
    }

    private static record GroundLayedCooldown(ItemStack stack, int cooldown) {
        private static final Codec<GroundLayedCooldown> CODEC = RecordCodecBuilder.create((instance) -> instance.group(ItemStack.CODEC.fieldOf("stack").forGetter(GroundLayedCooldown::stack), Codec.INT.fieldOf("cooldown").forGetter(GroundLayedCooldown::cooldown)).apply(instance, GroundLayedCooldown::new));
    }
}
