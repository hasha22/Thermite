package thermite.therm.networking;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;
import thermite.therm.ThermMod;
import thermite.therm.networking.packet.DrinkIceJuiceC2SPacket;

public class ThermNetworkingServer
{
    public static final Identifier DRINK_ICE_JUICE_C2S_PACKET_ID = new Identifier(ThermMod.modid, "drink_ice_juice_c2s_packet");

    public static void registerC2SPackets() {
        ServerPlayNetworking.registerGlobalReceiver(
                DRINK_ICE_JUICE_C2S_PACKET_ID,
                DrinkIceJuiceC2SPacket::receive
        );
    }
}
