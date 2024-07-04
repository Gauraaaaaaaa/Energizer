package com.gaura.energizer.mixin;

import com.gaura.energizer.Energizer;
import com.gaura.energizer.utils.IPlayerEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {

    @Inject(method = "canSprint", at = @At("RETURN"), cancellable = true)
    private void canSprint(CallbackInfoReturnable<Boolean> cir) {

        ClientPlayerEntity player = MinecraftClient.getInstance().player;

        if (player != null) {

            boolean stopSprint = ((IPlayerEntity) player).getStopSprint().getBoolean("stopSprint");

            if (Energizer.CONFIG.can_continue_sprinting && (!stopSprint && (MinecraftClient.getInstance().options.sprintKey.isPressed() && Energizer.CONFIG.sprint_keybind))) {

                cir.setReturnValue(true);
            }
            else if (stopSprint || (!MinecraftClient.getInstance().options.sprintKey.isPressed() && Energizer.CONFIG.sprint_keybind)) {

                cir.setReturnValue(false);
            }
        }
    }
}
