package thermite.therm.networking;

import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import thermite.therm.ThermClient;
import thermite.therm.ThermMod;

import java.util.concurrent.CompletableFuture;

public class ThermNetworkingClient
{
    public static final Identifier SEND_THERMPLAYERSTATE_S2C_PACKET_ID = new Identifier(ThermMod.modID, "send_thermplayerstate_s2c_packet");

    public static void registerS2CPackets() {
        ClientPlayNetworking.registerGlobalReceiver(
                SEND_THERMPLAYERSTATE_S2C_PACKET_ID,
                (client, handler, buf, responseSender) -> {

                    double temperature = buf.readDouble();
                    short td = buf.readShort();
                    double windPitch = buf.readDouble();
                    double windYaw = buf.readDouble();
                    double windTemp = buf.readDouble();

                    client.execute(() -> {
                        ThermClient.clientStoredTemperature = Math.round(temperature);
                        ThermClient.clientStoredTempDir = td;
                        ThermClient.clientStoredWindPitch = windPitch;
                        ThermClient.clientStoredWindYaw = windYaw;
                        ThermClient.clientStoredWindTemp = windTemp;
                    });
                }
        );
    }
    public static void registerVersionHandshake()
    {
        ClientLoginNetworking.registerGlobalReceiver(ThermNetworkingPackets.VERSION_PACKET,
                (client, handler, buf, listenerAdder) -> {

                    PacketByteBuf response = PacketByteBufs.create();

                    String version = FabricLoader.getInstance()
                            .getModContainer("therm")
                            .get()
                            .getMetadata()
                            .getVersion()
                            .getFriendlyString();

                    response.writeString(version);

                    return CompletableFuture.completedFuture(response);
                }
        );
    }
}
