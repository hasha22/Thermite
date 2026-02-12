package thermite.therm.networking;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;
import thermite.therm.ThermClient;
import thermite.therm.ThermMod;
import thermite.therm.networking.packet.DrinkIceJuiceC2SPacket;

public class ThermNetworkingPackets
{
    public static final Identifier VERSION_PACKET = new Identifier("therm", "version_check");
}