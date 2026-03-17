package net.collective.enchanced.common.cca.entity;

import net.collective.enchanced.common.cca.SyncedLivingEntityComponent;
import net.collective.enchanced.common.index.ModEntityComponents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.tick.CommonTickingComponent;

/*
This is heavily inspired by Powercyphe's code!
https://github.com/Powercyphe
 */

public class LungeComponent implements CommonTickingComponent {
    private static final String DASHING_TICKS = "dashingTicks";
    private static final int DASHING_TICKS_MAX = 8;
    private int dashingTicks = -5;

    private static final String DASH_DIRECTION = "dashDirection";
    private Vec3d dashDirection = Vec3d.ZERO;
    
    private ItemStack heldStack = ItemStack.EMPTY;

    private final PlayerEntity player;

    public LungeComponent(PlayerEntity player) {
        this.player = player;
    }

    protected ComponentKey<? extends LungeComponent> getComponentKey() {
        return ModEntityComponents.LUNGE;
    }
    public void sync() {
        getComponentKey().sync(this.player);
    }

    public World getWorld() {
        return this.player.getEntityWorld();
    }

    @Override
    public void tick() {
        if (this.isDashing() || this.isRecovering()) {
            if (this.dashingTicks == -4) {
                //TODO: Figure out how to stop using :c
//                player.sendMessage(Text.literal("" + this.player.getEntityWorld().isClient()), false);
//                this.player.stopUsingItem();
            }

            this.dashingTicks--;

            if (this.isDashing()) {
                if (!this.player.getActiveOrMainHandStack().equals(this.heldStack)) {
                    this.dashingTicks = 0;
                    return;
                }
                this.player.setVelocity(this.dashDirection.multiply(this.getDashProgress()));

            }
        }
        this.sync();
    }

    public void activateLunge(ItemStack stack) {
        if (stack.isIn(ItemTags.SPEARS)) {
            this.player.getItemCooldownManager().set(stack, 285);

            this.dashingTicks = DASHING_TICKS_MAX;

            Vec3d dir = this.player.getRotationVector(Math.clamp(this.player.getPitch(), -9F, 9F), this.player.getYaw());
            this.dashDirection = dir.normalize().multiply(2);
            this.player.setVelocity(this.dashDirection);

            this.heldStack = stack;
            this.sync();
        }
    }

    public float getDashProgress() {
        return this.getDashProgress(0);
    }

    public float getDashProgress(float tickProgress) {
        return Math.clamp((float) this.dashingTicks - tickProgress, 0F, DASHING_TICKS_MAX) / (float) DASHING_TICKS_MAX;
    }

    public float getRecoveryProgress() {
        return this.getRecoveryProgress(0F);
    }

    public float getRecoveryProgress(float tickProgress) {
        return 1F - (Math.clamp(this.dashingTicks - tickProgress, -5F, 0F) / -5F);
    }
    public boolean isDashing() {
        return this.dashingTicks > 0;
    }

    public boolean isRecovering() {
        return this.dashingTicks <= 0 && this.dashingTicks > -5;
    }

    @Override
    public void readData(ReadView readView) {
        this.dashingTicks = readView.getInt(DASHING_TICKS, -5);
        this.dashDirection = readView.read(DASH_DIRECTION, Vec3d.CODEC).orElse(Vec3d.ZERO);
    }

    @Override
    public void writeData(WriteView writeView) {
        writeView.putInt(DASHING_TICKS, this.dashingTicks);
        writeView.put(DASH_DIRECTION, Vec3d.CODEC, this.dashDirection);
    }

}