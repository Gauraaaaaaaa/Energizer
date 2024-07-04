package com.gaura.energizer.utils;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;

public class StopSprintS2CPacket {

    public static void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {

        if (client.player != null) {

            ((IPlayerEntity) client.player).getStopSprint().putBoolean("stopSprint", buf.readBoolean());
        }
    }
}