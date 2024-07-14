package com.gaura.energizer.mixin;

import com.gaura.energizer.Energizer;
import com.gaura.energizer.EnergizerClient;
import com.gaura.energizer.utils.IPlayerEntity;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin implements IPlayerEntity {

    @Shadow @Nullable public FishingBobberEntity fishHook;

    @Inject(method = "canConsume", at = @At("RETURN"), cancellable = true)
    public void canConsume(boolean ignoreHunger, CallbackInfoReturnable<Boolean> cir) {

        if (!FabricLoader.getInstance().isModLoaded(Energizer.HEARTY_MEALS_MOD_ID) && Energizer.CONFIG.remove_hunger) {

            PlayerEntity player = (PlayerEntity) (Object) this;

            cir.setReturnValue(!(player.getHealth() == player.getMaxHealth()) || ignoreHunger);
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

    public boolean stopSprint;

    private long lastStaminaLossTime = 0;

    @Inject(method = "tickMovement", at = @At("HEAD"))
    private void updateStamina(CallbackInfo ci) {

        PlayerEntity player = (PlayerEntity) (Object) this;
        long currentTime = player.getWorld().getTime();

        if (!player.isCreative() && !player.isSpectator() && !player.getWorld().isClient) {

            boolean hasHunger = player.hasStatusEffect(StatusEffects.HUNGER);
            float staminaDecrease = 0.0F;
            float staminaIncrease = (hasHunger && stopSprint) ? Energizer.CONFIG.stamina_increase_hunger_empty : hasHunger ? Energizer.CONFIG.stamina_increase_hunger : stopSprint ? Energizer.CONFIG.stamina_increase_empty : Energizer.CONFIG.stamina_increase;

            if (player.hasStatusEffect(Energizer.VIGOR)) {

                this.setStamina(this.getMaxStamina());
                this.stopSprint = false;
            }
            else {

                if (player.hasVehicle()) {

                    if ((this.getStamina() < this.getMaxStamina()) && ((currentTime - lastStaminaLossTime) >= Energizer.CONFIG.stamina_regeneration_delay)) {

                        this.setStamina(this.getStamina() + staminaIncrease);
                    }
                }
                else {

                    if (player.isSprinting() && !player.isSubmergedInWater() && !stopSprint) {

                        staminaDecrease = hasHunger ? Energizer.CONFIG.sprinting_stamina_decrease_hunger : Energizer.CONFIG.sprinting_stamina_decrease;
                        this.setStamina(this.getStamina() - staminaDecrease);
                        lastStaminaLossTime = currentTime;

                        if (this.getStamina() <= 0.0F && Energizer.CONFIG.disable_sprint_swim_empty_stamina) {

                            this.stopSprint = true;
                        }
                    }
                    else if (player.isSwimming() && player.isSubmergedInWater() && Energizer.CONFIG.swimming_cost_stamina && !stopSprint) {

                        staminaDecrease = hasHunger ? Energizer.CONFIG.swimming_stamina_decrease_hunger : Energizer.CONFIG.swimming_stamina_decrease;
                        this.setStamina(this.getStamina() - staminaDecrease);
                        lastStaminaLossTime = currentTime;

                        if (this.getStamina() <= 0.0F && Energizer.CONFIG.disable_sprint_swim_empty_stamina) {

                            this.stopSprint = true;
                        }
                    }
                    else if ((this.getStamina() < this.getMaxStamina()) && ((currentTime - lastStaminaLossTime) >= Energizer.CONFIG.stamina_regeneration_delay)) {

                        this.setStamina(this.getStamina() + staminaIncrease);
                    }
                    else if (this.getStamina() == this.getMaxStamina()) {

                        this.stopSprint = false;
                    }
                }
            }

            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeBoolean(this.stopSprint);
            ServerPlayNetworking.send((ServerPlayerEntity) player, EnergizerClient.STOP_SPRINT_ID, buf);
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

    private NbtCompound persistentData;

    @Override
    public NbtCompound getStopSprint() {

        if (this.persistentData == null) {

            this.persistentData = new NbtCompound();
        }

        return persistentData;
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {

        nbt.putBoolean("StopSprint", this.stopSprint);
        nbt.putFloat("Stamina", this.getStamina());
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {

        if (nbt.contains("Stamina", NbtElement.NUMBER_TYPE)) {

            this.setStamina(nbt.getFloat("Stamina"));
        }
        if (nbt.contains("StopSprint")) {

            this.stopSprint = nbt.getBoolean("StopSprint");
        }
    }
}