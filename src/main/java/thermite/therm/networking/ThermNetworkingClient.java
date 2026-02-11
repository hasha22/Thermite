package thermite.therm.networking;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.Identifier;
import thermite.therm.ThermClient;
import thermite.therm.ThermMod;

public class ThermNetworkingClient
{
    public static final Identifier SEND_THERMPLAYERSTATE_S2C_PACKET_ID = new Identifier(ThermMod.modid, "send_thermplayerstate_s2c_packet");

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
}
