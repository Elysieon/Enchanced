package net.collective.enchanced;

import eu.midnightdust.lib.config.MidnightConfig;
import moriyashiine.enchancement.common.Enchancement;
import moriyashiine.enchancement.common.ModConfig;
import moriyashiine.enchancement.common.component.entity.SlamComponent;
import moriyashiine.enchancement.common.init.ModEnchantments;
import moriyashiine.strawberrylib.api.module.SLibUtils;
import net.collective.enchanced.common.cca.entity.WeavingComponent;
import net.collective.enchanced.common.index.*;
import net.collective.enchanced.common.payload.LungeC2SPayload;
import net.collective.enchanced.common.payload.ThrownSpearSyncS2CPayload;
import net.collective.enchanced.common.payload.WeavingC2SPayload;
import net.collectively.geode.Geode;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class Enchanced implements ModInitializer {
    public static final String MOD_ID = "enchanced";
    public static final Geode geode = Geode.create(MOD_ID);

    public static Identifier id(String identifier) {
        return Identifier.of(MOD_ID, identifier);
    }

    public static final Identifier[] DISABLED_ENCHANTMENTS = new Identifier[] {
            Identifier.ofVanilla("fire_aspect"),
            Identifier.of("enchancement", "sticky"),
            Identifier.of("enchancement", "meteor"),
            Identifier.of("enchancement", "delay"),
    };

    @Override
    public void onInitialize() {
        Enchancement.LOGGER.info("Loading Enchancement");

        // Init
        EnchancedEnchantments.init();
        ModSoundEvent.init();
        ModLootConditionTypes.init();
        ModEntityTypes.init();
        ModScreenHandlerTypes.init();

        // Register Packet Stuff
        PayloadTypeRegistry.playC2S().register(LungeC2SPayload.ID, LungeC2SPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(WeavingC2SPayload.ID, WeavingC2SPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ThrownSpearSyncS2CPayload.ID, ThrownSpearSyncS2CPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(WeavingC2SPayload.ID, (payload, ctx) -> {
            PlayerEntity player = ctx.player();
            WeavingComponent component = player.getComponent(ModEntityComponents.WEAVING);
            component.onJump();
        });

        ServerPlayNetworking.registerGlobalReceiver(LungeC2SPayload.ID, (payload, context) -> {
            PlayerEntity player = context.player();
            var itemstack = player.getActiveOrMainHandStack();
            if (itemstack.isIn(ItemTags.SPEARS)) {
                var component = player.getComponent(ModEntityComponents.LUNGE);
                component.activateLunge(player.getActiveOrMainHandStack());

                SLibUtils.playSound(player, SoundEvents.ITEM_SPEAR_LUNGE_1.value(), 2F, MathHelper.nextFloat(player.getRandom(), 0.89F, 1f));
            }
        });

        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (source.isOf(DamageTypes.SPEAR) && source.getAttacker() instanceof PlayerEntity player) {
                SlamComponent component = player.getComponent(moriyashiine.enchancement.common.init.ModEntityComponents.SLAM);
                return !component.isSlamming();
            }

            return true;
        });

        geode.register();

        MidnightConfig.init("enchanced", EnchancedConfig.class);
    }

    public static float getScatterDivergence() {
        return 1.4f;
    }
}
