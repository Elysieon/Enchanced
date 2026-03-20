package net.collective.enchanced.common.mixin.enchantment_table;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import moriyashiine.enchancement.client.gui.screen.ingame.EnchantingTableScreen;
import moriyashiine.enchancement.common.Enchancement;
import moriyashiine.enchancement.common.screenhandler.EnchantingTableScreenHandler;
import moriyashiine.enchancement.common.util.EnchancementUtil;
import moriyashiine.strawberrylib.api.module.SLibClientUtils;
import net.collective.enchanced.Enchanced;
import net.collective.enchanced.common.util.EnchantUtils;
import net.collectively.geode.math.math;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.List;

@Mixin(EnchantingTableScreen.class)
public abstract class EnchantingTableScreenMixin {
    @Unique private static final Identifier UP_ARROW_TEXTURE = Enchancement.id("container/enchanting_table/up_arrow");
    @Unique private static final Identifier UP_ARROW_HIGHLIGHTED_TEXTURE = Enchancement.id("container/enchanting_table/up_arrow_highlighted");
    @Unique private static final Identifier DOWN_ARROW_TEXTURE = Enchancement.id("container/enchanting_table/down_arrow");
    @Unique private static final Identifier DOWN_ARROW_HIGHLIGHTED_TEXTURE = Enchancement.id("container/enchanting_table/down_arrow_highlighted");
    @Unique private static final Identifier CHECKMARK_TEXTURE = Enchancement.id("container/enchanting_table/checkmark");
    @Unique private static final Identifier CHECKMARK_HIGHLIGHTED_TEXTURE = Enchancement.id("container/enchanting_table/checkmark_highlighted");
    @Unique private static final Identifier LOCK_TEXTURE = Enchanced.id("container/enchanting_table/lock");
    @Unique private static final Identifier LOCKED_TAB_TEXTURE = Enchanced.id("container/enchanting_table/locked_tab");
    @Unique private static final Identifier ENTRY_LINE_1_TEXTURE = Enchanced.id("container/enchanting_table/entry_line_1");
    @Unique private static final Identifier ENTRY_LINE_2_TEXTURE = Enchanced.id("container/enchanting_table/entry_line_2");
    @Unique private static final Identifier ENTRY_LINE_3_TEXTURE = Enchanced.id("container/enchanting_table/entry_line_3");
    @Unique private static final Identifier ENTRY_LINE_4_TEXTURE = Enchanced.id("container/enchanting_table/entry_line_4");
    @Unique private static final Identifier[] ENTRY_LINE_TEXTURES = new Identifier[] {ENTRY_LINE_1_TEXTURE, ENTRY_LINE_2_TEXTURE, ENTRY_LINE_3_TEXTURE, ENTRY_LINE_4_TEXTURE};
    @Unique private static final StyleSpriteSource.Font GALACTIC_FONT = new StyleSpriteSource.Font(Identifier.ofVanilla("alt"));

    @Shadow
    private static boolean isInUpButtonBounds(int posX, int posY, int mouseX, int mouseY) {
        return false;
    }

    @Shadow
    private static boolean isInDownButtonBounds(int posX, int posY, int mouseX, int mouseY) {
        return false;
    }

    @Shadow
    private static boolean isInEnchantButtonBounds(int posX, int posY, int mouseX, int mouseY) {
        return false;
    }

    @Shadow
    private static boolean isInBounds(int posX, int posY, int mouseX, int mouseY, int startX, int endX, int startY, int endY) {
        return false;
    }

    @Shadow
    private List<Text> infoTexts;
    @Shadow
    private int materialIndex;
    @Shadow
    private int highlightedEnchantmentIndex;

    @Shadow
    private float nextPageAngle;

    @WrapMethod(method = "mouseScrolled")
    public boolean enchanting_table$mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount, Operation<Boolean> original) {
        EnchantingTableScreen screen = (EnchantingTableScreen) (Object) this;
        EnchantingTableScreenHandler handler = screen.getScreenHandler();
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerInteractionManager interactionManager = client.interactionManager;
        ClientWorld clientWorld = client.world;

        if (interactionManager == null || clientWorld == null) {
            return original.call(mouseX, mouseY, horizontalAmount, verticalAmount);
        }

        ItemStack enchantingStack = handler.getSlot(0).getStack();
        List<RegistryEntry.Reference<Enchantment>> allEnchantments = EnchantUtils.getAllEnchantmentsForStack(clientWorld.getRegistryManager(), handler.validEnchantments::contains, enchantingStack);

        if (allEnchantments.size() > 4) {
            int delta = verticalAmount > (double)0.0F ? -1 : 1;
            handler.updateViewIndex(verticalAmount > (double)0.0F);
            interactionManager.clickButton(handler.syncId, verticalAmount > (double)0.0F ? 1 : 2);
            this.nextPageAngle += (float)delta;
            return true;
        }

        return original.call(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @WrapMethod(method = "mouseClicked")
    public boolean enchanting_table$mouseClicked(Click click, boolean doubled, Operation<Boolean> original) {
        EnchantingTableScreen screen = (EnchantingTableScreen) (Object) this;
        EnchantingTableScreenHandler handler = screen.getScreenHandler();
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity clientPlayer = client.player;
        ClientPlayerInteractionManager interactionManager = client.interactionManager;
        ClientWorld clientWorld = client.world;
        
        if (clientPlayer == null || interactionManager == null || clientWorld == null) {
            return original.call(click, doubled);
        }

        int posX = (screen.width - 176) / 2;
        int posY = (screen.height - 166) / 2 - 16;

        if (handler.canEnchant(clientPlayer, clientPlayer.isCreative())
                && isInEnchantButtonBounds(posX, posY, (int)click.x(), (int)click.y())
                && !handler.selectedEnchantments.isEmpty()
                && handler.onButtonClick(clientPlayer, 0)) {

            interactionManager.clickButton(handler.syncId, 0);
            return true;
        }

        ItemStack enchantingStack = handler.getSlot(0).getStack();
        List<RegistryEntry.Reference<Enchantment>> allEnchantments = EnchantUtils.getAllEnchantmentsForStack(clientWorld.getRegistryManager(), handler.validEnchantments::contains, enchantingStack);

        if (allEnchantments.size() > 4) {
            if (isInUpButtonBounds(posX, posY, (int)click.x(), (int)click.y()) && handler.onButtonClick(clientPlayer, 1)) {
                interactionManager.clickButton(handler.syncId, 1);
                client.getSoundManager().play(PositionedSoundInstance.ui(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                ++this.nextPageAngle;
                return true;
            }

            if (isInDownButtonBounds(posX, posY, (int)click.x(), (int)click.y()) && handler.onButtonClick(clientPlayer, 2)) {
                interactionManager.clickButton(handler.syncId, 2);
                client.getSoundManager().play(PositionedSoundInstance.ui(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                --this.nextPageAngle;
                return true;
            }
        }

        if (this.highlightedEnchantmentIndex >= 0 && handler.onButtonClick(clientPlayer, this.highlightedEnchantmentIndex + 4)) {
            interactionManager.clickButton(handler.syncId, this.highlightedEnchantmentIndex + 4);
            client.getSoundManager().play(PositionedSoundInstance.ui(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }

        return original.call(click, doubled);
    }

    @WrapMethod(method = "drawMain")
    private void enchanting_table$drawMain(DrawContext context, int mouseX, int mouseY, int posX, int posY, Operation<Void> original) {
        EnchantingTableScreen screen = (EnchantingTableScreen) (Object) this;
        EnchantingTableScreenHandler handler = screen.getScreenHandler();
        MinecraftClient client = MinecraftClient.getInstance();
        ClientWorld world = client.world;
        ClientPlayerEntity clientPlayer = client.player;

        if (world == null || clientPlayer == null) {
            original.call(context, mouseX, mouseY, posX, posY);
            return;
        }

        TextRenderer textRenderer = screen.getTextRenderer();

        ItemStack enchantingStack = handler.getSlot(0).getStack();
        List<RegistryEntry.Reference<Enchantment>> allEnchantments = EnchantUtils.getAllEnchantmentsForStack(world.getRegistryManager(), handler.validEnchantments::contains, enchantingStack);

        if (allEnchantments.size() > 4) {
            if (isInUpButtonBounds(posX, posY, mouseX, mouseY)) {
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, UP_ARROW_HIGHLIGHTED_TEXTURE, posX + 154, posY + 34, 16, 16);
            } else {
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, UP_ARROW_TEXTURE, posX + 154, posY + 34, 16, 16);
            }

            if (isInDownButtonBounds(posX, posY, mouseX, mouseY)) {
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, DOWN_ARROW_HIGHLIGHTED_TEXTURE, posX + 154, posY + 51, 16, 16);
            } else {
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, DOWN_ARROW_TEXTURE, posX + 154, posY + 51, 16, 16);
            }
        }

        if (isInEnchantButtonBounds(posX, posY, mouseX, mouseY)) {
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, CHECKMARK_HIGHLIGHTED_TEXTURE, posX + 154, posY + 72, 16, 16);
            if (this.infoTexts == null) {
                MutableText xpCost = Text.translatable("tooltip.enchancement.experience_level_cost", handler.getCost()).formatted(Formatting.GREEN);
                MutableText lapisCost = Text.translatable("tooltip.enchancement.material_cost", handler.getCost(), Text.translatable(Items.LAPIS_LAZULI.getTranslationKey())).formatted(Formatting.GREEN);
                MutableText materialCost = null;
                if (!handler.getEnchantingMaterial().isEmpty()) {
                    MutableText itemName = Text.translatable(handler.getEnchantingMaterial().get(this.materialIndex).value().getTranslationKey());

                    if (handler.slots.get(2).getStack().isOf(Items.ENCHANTED_BOOK)) {
                        itemName = Text.translatable(handler.slots.get(2).getStack().getItem().getTranslationKey());
                    }

                    materialCost = Text.translatable("tooltip.enchancement.material_cost", handler.getCost(), itemName).formatted(Formatting.GREEN);
                }

                if (!clientPlayer.isCreative()) {
                    if (clientPlayer.experienceLevel < handler.getCost()) {
                        xpCost.formatted(Formatting.RED);
                    }

                    if (handler.getSlot(1).getStack().getCount() < handler.getCost()) {
                        lapisCost.formatted(Formatting.RED);
                    }

                    if (materialCost != null && handler.getSlot(2).getStack().getCount() < handler.getCost()) {
                        materialCost.formatted(Formatting.RED);
                    }
                }

                if (materialCost == null) {
                    this.infoTexts = List.of(xpCost, lapisCost);
                } else {
                    this.infoTexts = List.of(xpCost, lapisCost, materialCost);
                }
            }

            context.drawTooltip(textRenderer, this.infoTexts, mouseX, mouseY);
        } else {
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, CHECKMARK_TEXTURE, posX + 154, posY + 72, 16, 16);
            this.infoTexts = null;
        }

        this.highlightedEnchantmentIndex = -1;

        for (int i = 0; i < allEnchantments.size() && i < 4; ++i) {
            RegistryEntry<Enchantment> enchantment;
            if (allEnchantments.size() <= 4) {
                enchantment = allEnchantments.get(i);
            } else {
                enchantment = allEnchantments.get((i + handler.viewIndex) % allEnchantments.size());
            }

            boolean isUnlocked = handler.validEnchantments.contains(enchantment);

            MutableText enchantmentName = enchantment.value().description().copy();
            boolean isAllowed = EnchantmentHelper.isCompatible(handler.selectedEnchantments, enchantment) && !EnchancementUtil.exceedsLimit(enchantingStack, enchantingStack.getEnchantments().getSize() + handler.selectedEnchantments.size() + 1);
            enchantmentName = Text.literal(textRenderer.trimToWidth(enchantmentName.getString(), 80));

            if (!isUnlocked) {
                enchantmentName.formatted(Formatting.DARK_GRAY, Formatting.STRIKETHROUGH);
            } else {
                if (handler.selectedEnchantments.contains(enchantment)) {
                    enchantmentName.formatted(Formatting.DARK_GREEN);
                } else {
                    if (isAllowed) {
                        enchantmentName.formatted(Formatting.BLACK);
                    } else {
                        enchantmentName.formatted(Formatting.DARK_RED, Formatting.STRIKETHROUGH);
                    }
                }
            }

            context.drawText(textRenderer, enchantmentName, posX + 66, posY + 16 + i * 19, -1, false);

            if (!isUnlocked) {
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, LOCK_TEXTURE, posX + 66 + math.min(80, textRenderer.getWidth(enchantmentName)) + 3, posY + 16 + i * 19 - 2, 9, 12);
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, LOCKED_TAB_TEXTURE, posX + 66 - 7, posY + 16 + i * 19 - 2, 5, 11);
            }

            // context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, ENTRY_LINE_TEXTURES[i], posX + 66 - 7, posY + 16 + i * 19 - 2, 93, 19);

            if (isInBounds(posX, posY + 11 + i * 19, mouseX, mouseY, 64, 67 + textRenderer.getWidth(enchantmentName), 0, 16)) {
                if ((isAllowed || !isUnlocked) || handler.selectedEnchantments.contains(enchantment)) {
                    this.highlightedEnchantmentIndex = i;

                    if (!isUnlocked) {
                        highlightedEnchantmentIndex = -1;
                    }
                }

                if (this.infoTexts == null) {
                    MutableText name = enchantment.value().description().copy().formatted(Formatting.GRAY);
                    MutableText description = Text.translatable(EnchancementUtil.getTranslationKey(enchantment) + ".desc")
                            .formatted(Formatting.DARK_GRAY);

                    if (!isUnlocked) {
                        description.styled(style -> style.withFont(GALACTIC_FONT));
                    }

                    if (description.getString().isEmpty()) {
                        this.infoTexts = List.of(name);
                    } else {
                        this.infoTexts = new ArrayList<>();
                        this.infoTexts.add(name);
                        this.infoTexts.addAll(SLibClientUtils.wrapText(Text.literal(" - ").formatted(Formatting.GRAY).append(description)));
                    }
                }

                context.drawTooltip(textRenderer, this.infoTexts, mouseX, mouseY);
            } else {
                this.infoTexts = null;
            }
        }
    }
}
