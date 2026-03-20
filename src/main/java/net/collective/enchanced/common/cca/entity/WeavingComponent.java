package net.collective.enchanced.common.cca.entity;

import net.collective.enchanced.common.cca.SynedPlayerEntityComponent;
import net.collective.enchanced.common.index.EnchancedEnchantments;
import net.collective.enchanced.common.index.ModEntityComponents;
import net.collective.enchanced.common.payload.WeavingC2SPayload;
import net.collective.enchanced.common.util.EnchantUtils;
import net.collectively.geode.math.math;
import net.collectively.geode.types.double3;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.ladysnake.cca.api.v3.component.ComponentKey;

public class WeavingComponent extends SynedPlayerEntityComponent {
    private static final String GROUND_TIME_KEY = "ground_time";
    private int groundTime;

    private static final String DELAY_KEY = "delay";
    private int delay;

    private static final String AIR_TIME_KEY = "air_time";
    private int airTime;

    private static final String COUNT_KEY = "count";
    public static final int MAX_JUMPS = 2;
    private int count = 2;

    private static final String REGENERATION_TICKS_KEY = "regeneration_ticks";
    private static final int MAX_REGENERATION_TICKS = 20;
    private int regenerationTicks;

    private static final String SHOULD_REFRESH_KEY = "should_refresh";
    private boolean shouldRefresh;

    public WeavingComponent(PlayerEntity playerEntity) {
        super(playerEntity);
    }

    @Override
    protected ComponentKey<? extends SynedPlayerEntityComponent> getComponentKey() {
        return ModEntityComponents.WEAVING;
    }

    public double regenerationProgress() {
        return 1d - (regenerationTicks / (double) MAX_REGENERATION_TICKS);
    }

    public int count() {
        return count;
    }

    private void tickTimers() {
        if (delay > 0) {
            delay--;
        }

        if (player().isOnGround()) {
            airTime = 0;
            groundTime++;
            return;
        }

        airTime++;
        groundTime = 0;
    }

    private void tickRefreshJumps() {
        if (groundTime > 1) {
            shouldRefresh = true;
        }
    }

    private void regenerateJumps() {
        if (count < MAX_JUMPS && shouldRefresh) {
            regenerationTicks--;

            if (regenerationTicks <= 0) {
                count++;

                if (count < MAX_JUMPS) {
                    regenerationTicks = MAX_REGENERATION_TICKS;
                }
            }

            sync();
        }
    }

    private boolean canDetectWallJump() {
        return !player().isSpectator() && hasWeaving() && count > 0 && delay <= 0 && airTime > 4;
    }

    private void tickWallJumpDetection() {
        double3 position = new double3(player().getEntityPos()).addY(1);
        double3 horizontalRotation = new double3(player().getRotationVector(0, player().getHeadYaw()));
        double3 playerRotation = new double3(horizontalRotation);

        if (player() instanceof ClientPlayerEntity clientPlayer) {
            double horizontalInputStrength = clientPlayer.input.getMovementInput().x * 1.5;
            playerRotation = playerRotation.add(playerRotation.cross(double3.down).mul(horizontalInputStrength));
        }

        playerRotation = playerRotation.normalize();

        RaycastContext raycastContext = new RaycastContext(
                position.toVec3d(), position.add(playerRotation.mul(0.5)).toVec3d(),
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                player()
        );

        World world = player().getEntityWorld();
        BlockHitResult result = world.raycast(raycastContext);

        boolean canWallJump = false;
        double3 wallJumpDirection = double3.zero;
        double wallJumpStrength = math.clamp(player().getVelocity().length() * 1.5f, 1, 2);

        if (result.getType() != HitResult.Type.MISS) {
            canWallJump = true;
            double3 baseDirection = new double3(0, 1.9, 0);
            double3 side = new double3(result.getSide().getDoubleVector());
            double3 horizontalDirection = new double3(Direction.getFacing(player().getRotationVector()).getDoubleVector()).withY(0);
            double dot = 1 - math.max(0, horizontalDirection.mul(-1).dot(side));
            baseDirection = baseDirection.add(0, dot, 0);
            baseDirection = baseDirection.add(side.mul(1.65)).add(horizontalDirection.mul(dot * 3.2));
            wallJumpDirection = baseDirection;
        } else {
            raycastContext = new RaycastContext(
                    position.toVec3d(), position.add(horizontalRotation.mul(-1)).toVec3d(),
                    RaycastContext.ShapeType.COLLIDER,
                    RaycastContext.FluidHandling.NONE,
                    player()
            );

            result = player()
                    .getEntityWorld()
                    .raycast(raycastContext);

            if (result.getType() != HitResult.Type.MISS) {
                canWallJump = true;
                wallJumpDirection = new double3(player().getRotationVector()).addY(1);
            }
        }

        if (canWallJump && player().isJumping()) {
            wallJumpDirection = wallJumpDirection.normalize().mul(wallJumpStrength);
            player().setVelocity(wallJumpDirection.toVec3d());

            onJump();
            ClientPlayNetworking.send(new WeavingC2SPayload());
        }
    }

    @Override
    public void tick() {
        tickTimers();
        tickRefreshJumps();
        regenerateJumps();

        if (canDetectWallJump()) {
            tickWallJumpDetection();
        }

        sync();
    }

    public void onJump() {
        player().setOnGround(true);
        player().fallDistance = 0;

        shouldRefresh = false;
        delay = 10;
        count--;

        if (regenerationTicks <= 0) {
            regenerationTicks = MAX_REGENERATION_TICKS;
        }

        sync();
    }

    @Override
    public void readData(ReadView readView) {
        delay = readView.getInt(DELAY_KEY, 0);
        airTime = readView.getInt(AIR_TIME_KEY, 0);
        count = readView.getInt(COUNT_KEY, 0);
        regenerationTicks = readView.getInt(REGENERATION_TICKS_KEY, 0);
        shouldRefresh = readView.getBoolean(SHOULD_REFRESH_KEY, false);
        groundTime = readView.getInt(GROUND_TIME_KEY, 0);
    }

    @Override
    public void writeData(WriteView writeView) {
        writeView.putInt(DELAY_KEY, delay);
        writeView.putInt(AIR_TIME_KEY, airTime);
        writeView.putInt(COUNT_KEY, count);
        writeView.putInt(REGENERATION_TICKS_KEY, regenerationTicks);
        writeView.putBoolean(SHOULD_REFRESH_KEY, shouldRefresh);
        writeView.putInt(GROUND_TIME_KEY, groundTime);
    }

    public static boolean hasWeaving(LivingEntity livingEntity) {
        ItemStack feetStack = livingEntity.getEquippedStack(EquipmentSlot.FEET);
        return !feetStack.isEmpty() && EnchantUtils.hasEnchantment(livingEntity, feetStack, EnchancedEnchantments.WEAVING);
    }

    public boolean hasWeaving() {
        return hasWeaving(player());
    }
}
