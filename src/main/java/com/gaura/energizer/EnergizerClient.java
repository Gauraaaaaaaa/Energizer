package com.gaura.energizer;

import com.gaura.energizer.utils.StopSprintS2CPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.Identifier;

public class EnergizerClient implements ClientModInitializer {

    public static final Identifier STOP_SPRINT_ID = new Identifier(Energizer.MOD_ID, "stop_sprint");

    @Override
    public void onInitializeClient() {

        ClientPlayNetworking.registerGlobalReceiver(STOP_SPRINT_ID, StopSprintS2CPacket::receive);
    }
}