package com.gaura.energizer.mixin;

import com.gaura.energizer.Energizer;
import com.gaura.energizer.utils.IPlayerEntity;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin implements IPlayerEntity {

    @Inject(method = "canConsume", at = @At("RETURN"), cancellable = true)
    public void canConsume(boolean ignoreHunger, CallbackInfoReturnable<Boolean> cir) {

        if (!FabricLoader.getInstance().isModLoaded(Energizer.HEARTY_MEALS_MOD_ID) && Energizer.CONFIG.remove_hunger) {

            cir.setReturnValue(true);
        }
    }

    @Inject(method = "eatFood", at = @At("HEAD"), cancellable = true)
    public void eatFood(World world, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {

        if (!FabricLoader.getInstance().isModLoaded(Energizer.HEARTY_MEALS_MOD_ID) && Energizer.CONFIG.remove_hunger) {

            PlayerEntity player = (PlayerEntity) (Object) this;

            if (stack.isFood() && player instanceof ServerPlayerEntity serverPlayer && !world.isClient()) {

                serverPlayer.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));

                world.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), SoundEvents.ENTITY_PLAYER_BURP, SoundCategory.PLAYERS, 0.5F, world.random.nextFloat() * 0.1F + 0.9F);

                ((LivingEntityInvoker) serverPlayer).invokeApplyFoodEffects(stack, world, serverPlayer);

                Criteria.CONSUME_ITEM.trigger(serverPlayer, stack);

                serverPlayer.heal(stack.getItem().getFoodComponent().getHunger() * Energizer.CONFIG.heal_multiplier);

                stack.decrement(1);

                serverPlayer.emitGameEvent(GameEvent.EAT);
            }

            cir.setReturnValue(stack);
        }
    }

    @Inject(method = "createPlayerAttributes", at = @At("RETURN"))
    private static void addStaminaAttribute(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {

        cir.getReturnValue().add(Energizer.STAMINA_ATTRIBUTE);
    }

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    protected void addStaminaDataTracker(CallbackInfo ci) {

        PlayerEntity player = (PlayerEntity) (Object) this;

        player.getDataTracker().startTracking(Energizer.STAMINA_DATA, 1.0F);
    }

    public boolean stopSprint = false;

    @Inject(method = "tickMovement", at = @At("HEAD"))
    private void updateStamina(CallbackInfo ci) {

        PlayerEntity player = (PlayerEntity) (Object) this;

        if (!player.isCreative() && !player.isSpectator()) {

            boolean hasHunger = player.hasStatusEffect(StatusEffects.HUNGER);
            float staminaDecrease = hasHunger ? Energizer.CONFIG.stamina_decrease_hunger : Energizer.CONFIG.stamina_decrease;
            float staminaIncrease = (hasHunger && stopSprint) ? Energizer.CONFIG.stamina_increase_hunger_empty : hasHunger ? Energizer.CONFIG.stamina_increase_hunger : stopSprint ? Energizer.CONFIG.stamina_increase_empty : Energizer.CONFIG.stamina_increase;

            if (player.hasStatusEffect(Energizer.VIGOR)) {

                this.setStamina(this.getMaxStamina());
                this.stopSprint = false;
            }
            else {

                if (player.hasVehicle()) {

                    if (this.getStamina() < this.getMaxStamina()) {

                        this.setStamina(this.getStamina() + staminaIncrease);
                    }
                }
                else {

                    if ((player.isSprinting() || player.isSwimming()) && !stopSprint) {

                        this.setStamina(this.getStamina() - staminaDecrease);

                        if (this.getStamina() <= 0.0F) {

                            this.stopSprint = true;
                        }
                    }
                    else if (this.getStamina() < this.getMaxStamina()) {

                        this.setStamina(this.getStamina() + staminaIncrease);
                    }
                    else if (this.getStamina() == this.getMaxStamina()) {

                        this.stopSprint = false;
                    }
                }
            }

            if (this.stopSprint || (!MinecraftClient.getInstance().options.sprintKey.isPressed() && Energizer.CONFIG.sprint_keybind)) {

                player.setSprinting(false);
            }
        }
    }

    private float getStamina() {

        PlayerEntity player = (PlayerEntity) (Object) this;

        return player.getDataTracker().get(Energizer.STAMINA_DATA);
    }

    private void setStamina(float stamina) {

        PlayerEntity player = (PlayerEntity) (Object) this;

        player.getDataTracker().set(Energizer.STAMINA_DATA, MathHelper.clamp(stamina, 0.0F, this.getMaxStamina()));
    }

    private float getMaxStamina() {

        PlayerEntity player = (PlayerEntity) (Object) this;

        return (float) player.getAttributeValue(Energizer.STAMINA_ATTRIBUTE);
    }

    @Override
    public boolean getStopSprint() {

        return this.stopSprint;
    }
}