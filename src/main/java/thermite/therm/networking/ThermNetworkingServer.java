package thermite.therm.networking;

import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import thermite.therm.ThermMod;
import thermite.therm.networking.packet.DrinkIceJuiceC2SPacket;

public class ThermNetworkingServer
{
    public static final Identifier DRINK_ICE_JUICE_C2S_PACKET_ID = new Identifier(ThermMod.modID, "drink_ice_juice_c2s_packet");

    public static void registerC2SPackets() {
        ServerPlayNetworking.registerGlobalReceiver(
                DRINK_ICE_JUICE_C2S_PACKET_ID,
                DrinkIceJuiceC2SPacket::receive
        );
    }
    public static void registerVersionHandshake()
    {
        Object ThermLoginNetworking;
        ServerLoginNetworking.registerGlobalReceiver(ThermNetworkingPackets.VERSION_PACKET,
                (server, handler, understood, buf, synchronizer, responseSender) ->
                {
                    String clientVersion = buf.readString();

                    String serverVersion = FabricLoader.getInstance()
                            .getModContainer("therm")
                            .get()
                            .getMetadata()
                            .getVersion()
                            .getFriendlyString();

                    if (!clientVersion.equals(serverVersion)) {
                        handler.disconnect(Text.literal(
                                "§cThermite version mismatch!\n" +
                                        "Server: " + serverVersion +
                                        "\nClient: " + clientVersion +
                                        "\n\nDownload the correct version: https://modrinth.com/modpack/survive-the-winter"
                        ));
                    }
                }
        );
    }
}

