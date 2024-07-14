package com.gaura.energizer.mixin;

import com.gaura.energizer.Energizer;
import com.gaura.energizer.utils.IPlayerEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    private static final Identifier STAMINA_ICONS = new Identifier(Energizer.MOD_ID, "textures/stamina/stamina_icons.png");

    private static final int X_OFFSET = 82;
    private static final int Y_OFFSET = 39;

    private int lastFullFillTime = -1;

    @Inject(method = "renderStatusBars", at = @At("TAIL"))
    private void renderStaminaBar(DrawContext context, CallbackInfo ci) {

        MinecraftClient client = MinecraftClient.getInstance();

        if (((InGameHudInvoker) this).invokeGetHeartCount(((InGameHudInvoker) this).invokeGetRiddenEntity()) == 0) {

            int x = (client.getWindow().getScaledWidth() / 2) + X_OFFSET + Energizer.CONFIG.x_offset_stamina_bar;
            int y = client.getWindow().getScaledHeight() - Y_OFFSET - Energizer.CONFIG.y_offset_stamina_bar;

            int vigorIndex = -1;

            float maxStamina = (float) client.player.getAttributeValue(Energizer.STAMINA_ATTRIBUTE);
            float currentStamina = client.player.getDataTracker().get(Energizer.STAMINA_DATA);

            boolean hasHunger = client.player.hasStatusEffect(StatusEffects.HUNGER);
            boolean hasVigor = client.player.hasStatusEffect(Energizer.VIGOR);
            boolean stopSprint = ((IPlayerEntity) client.player).getStopSprint().getBoolean("stopSprint");

            int lines = (int) Math.ceil(maxStamina / 20);
            int fullIconsPerLine = (int) (currentStamina / 2);
            boolean halfIcon = Math.floor(currentStamina) % 2 != 0;
            int backgroundsPerLine = (int) Math.ceil(maxStamina / 2);

            if (hasVigor && Energizer.CONFIG.vigor_wave) {

                vigorIndex = client.inGameHud.getTicks() % MathHelper.ceil(maxStamina + 5.0F);
            }

            if (currentStamina == maxStamina) {

                if (lastFullFillTime == -1) {

                    lastFullFillTime = client.inGameHud.getTicks();
                }
            }
            else {

                lastFullFillTime = -1;
            }

            int yDecrement = getYDecrement(maxStamina);

            for (int line = 0; line < lines; line++, fullIconsPerLine -= 10, backgroundsPerLine -= 10) {

                for (int i = 0; i < Math.min(backgroundsPerLine, 10); i++) {

                    int u = ((lastFullFillTime != -1) && ((client.inGameHud.getTicks() - lastFullFillTime) < 3) && Energizer.CONFIG.stamina_blink) ? 9 : 0;

                    int vigor = getVigorIndex(i, maxStamina, vigorIndex, line);

                    context.drawTexture(STAMINA_ICONS, x - i * 8, y - line * yDecrement - vigor, u, 0, 9, 9, 81, 9);
                }

                for (int i = 0; i < Math.min(fullIconsPerLine, 10); i++) {

                    int u = hasVigor ? 72 : stopSprint ? 36 : hasHunger ? 54 : 18;

                    int vigor = getVigorIndex(i, maxStamina, vigorIndex, line);

                    context.drawTexture(STAMINA_ICONS, x - i * 8, y - line * yDecrement - vigor, u, 0, 9, 9, 81, 9);
                }

                if (halfIcon && fullIconsPerLine < 10) {

                    int u = stopSprint ? 45 : hasHunger ? 63 : 27;
                    context.drawTexture(STAMINA_ICONS, x - fullIconsPerLine * 8, y - line * yDecrement, u, 0, 9, 9, 81, 9);
                    halfIcon = false;
                }
            }
        }
    }

    private int getVigorIndex(int i, float maxStamina, int vigorIndex, int line) {

        int index = 0;

        if ((i < maxStamina) && (i == (vigorIndex - (line * 10)))) {

            index = 2;
        }

        return index;
    }

    private int getYDecrement(float maxStamina) {

        int yDecrement;

        if (maxStamina <= 40) {
            yDecrement = 10;
        } else if (maxStamina <= 60) {
            yDecrement = 9;
        } else if (maxStamina <= 80) {
            yDecrement = 8;
        } else if (maxStamina <= 100) {
            yDecrement = 7;
        } else if (maxStamina <= 120) {
            yDecrement = 6;
        } else if (maxStamina <= 140) {
            yDecrement = 5;
        } else if (maxStamina <= 160) {
            yDecrement = 4;
        } else {
            yDecrement = 3;
        }

        return yDecrement;
    }
}