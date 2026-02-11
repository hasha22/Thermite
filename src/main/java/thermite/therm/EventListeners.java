package thermite.therm;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionType;
import thermite.therm.block.FireplaceBlock;
import thermite.therm.block.ThermBlocks;
import thermite.therm.effect.ThermStatusEffects;
import thermite.therm.networking.ThermNetworkingClient;

import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static thermite.therm.ThermMod.modVersion;

public class EventListeners {

    public static int tempTickCounter = 0;

    public static void register() {

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
        {
            ServerState serverState = ServerState.getServerState(Objects.requireNonNull(handler.player.getWorld().getServer()));

            if (!Objects.equals(serverState.worldVersion, modVersion))
            {

                serverState.windTempModifierRange = 8;
                serverState.windRandomizeTick = 24000;
                serverState.worldVersion = modVersion;

                serverState.players.forEach((uuid, state) -> {
                    state.windTurbulence = 23;
                });

                serverState.markDirty();
                ThermMod.LOGGER.info("Updated Thermite ServerState.");

            }
        });

        ServerTickEvents.END_SERVER_TICK.register((server) ->
        {
            ServerState serverState = ServerState.getServerState(server);

            if (serverState.windRandomizeTick >= 24000)
            {
                serverState.windRandomizeTick = 0;

                Random rand = new Random();
                serverState.windPitch = 360*Math.PI/180;
                serverState.windYaw = rand.nextDouble(0, 360)*Math.PI/180;
                serverState.windTempModifier = rand.nextDouble(-serverState.windTempModifierRange, serverState.windTempModifierRange);
                serverState.precipitationWindModifier = rand.nextDouble(-serverState.windTempModifierRange, 0);

                serverState.markDirty();
                ThermMod.LOGGER.info("========WIND RANDOMIZED========");

            }
            serverState.windRandomizeTick += 1;

            server.getPlayerManager().getPlayerList().forEach((player) ->
            {
                if (!player.isSpectator() && !player.isCreative())
                {
                    //Calls Temp Tick Logic
                    temperatureTick(server, player);
                }
            });
        });
    }
    //UPDATE - Removed Player C2S Temperature Tick Packet, moved logic to where it should be
    private static void temperatureTick(MinecraftServer server, ServerPlayerEntity player)
    {
        ServerState serverState = ServerState.getServerState(server);
        ThermPlayerState playerState = ServerState.getPlayerState(player);

        tempTickCounter++;
        if (tempTickCounter < ThermMod.config.tempTickCount)
        {
            return;
        }
        tempTickCounter = 0;

        float temp = player.getWorld().getBiome(player.getBlockPos()).value().getTemperature();
        short tempDir = (short) (playerState.restingTemp - playerState.temp);

        String climate = ThermUtil.getClimate(temp);

        float nightRTemp = 0;

        if (Objects.equals(climate, "frigid"))
        {
            playerState.minTemp = 0;
            playerState.maxTemp = 80;
            playerState.restingTemp = ThermMod.config.frigidClimateTemp;
            nightRTemp = ThermMod.config.nightFrigidTempModifier;
        }
        else if (Objects.equals(climate, "cold"))
        {
            playerState.minTemp = 0;
            playerState.maxTemp = 100;
            playerState.restingTemp = ThermMod.config.coldClimateTemp;
            nightRTemp = ThermMod.config.nightColdTempModifier;
        }
        else if (Objects.equals(climate, "temperate"))
        {
            playerState.minTemp = 0;
            playerState.maxTemp = 100;
            playerState.restingTemp = ThermMod.config.temperateClimateTemp;
            nightRTemp = ThermMod.config.nightTemperateTempModifier;
        }
        else if (Objects.equals(climate, "hot"))
        {
            playerState.minTemp = 40;
            playerState.maxTemp = 120;
            playerState.restingTemp = ThermMod.config.hotClimateTemp;
            nightRTemp = ThermMod.config.nightHotTempModifier;
        }
        else if (Objects.equals(climate, "arid"))
        {
            playerState.minTemp = 40;
            playerState.maxTemp = 120;
            playerState.restingTemp = ThermMod.config.aridClimateTemp;
            nightRTemp = ThermMod.config.nightAridTempModifier;
        }

        DimensionType dim = player.getWorld().getDimension();
        if (dim.natural())
        {
            if (!player.getWorld().isDay())
            {
                playerState.restingTemp -= nightRTemp;
            }
        }

        Biome.Precipitation precip = player.getWorld().getBiome(player.getBlockPos()).value().getPrecipitation(player.getBlockPos());

        if (precip == Biome.Precipitation.RAIN)
        {
            if (player.getWorld().isRaining())
            {
                if (player.isWet() && !player.isTouchingWater())
                {
                    playerState.restingTemp -= ThermMod.config.rainTempModifier;
                }
            }
        }
        else if (precip == Biome.Precipitation.SNOW)
        {
            if (player.getWorld().isRaining())
            {
                playerState.restingTemp -= ThermMod.config.snowTempModifier;
            }
        }

        //armor items
        AtomicInteger armorHeat = new AtomicInteger();
        ThermMod.config.bootTempItems.forEach((it, t) -> {
            if (Objects.equals(player.getInventory().getArmorStack(0).getItem().toString(), it)) {
                playerState.restingTemp += t + player.getInventory().getArmorStack(0).getNbt().getInt("wool");
                armorHeat.addAndGet(t + player.getInventory().getArmorStack(0).getNbt().getInt("wool"));
            }
        });
        ThermMod.config.leggingTempItems.forEach((it, t) -> {
            if (Objects.equals(player.getInventory().getArmorStack(1).getItem().toString(), it)) {
                playerState.restingTemp += t + player.getInventory().getArmorStack(1).getNbt().getInt("wool");
                armorHeat.addAndGet(t + player.getInventory().getArmorStack(1).getNbt().getInt("wool"));
            }
        });
        ThermMod.config.chestplateTempItems.forEach((it, t) -> {
            if (Objects.equals(player.getInventory().getArmorStack(2).getItem().toString(), it)) {
                playerState.restingTemp += t + player.getInventory().getArmorStack(2).getNbt().getInt("wool");
                armorHeat.addAndGet(t + player.getInventory().getArmorStack(2).getNbt().getInt("wool"));
            }
        });
        ThermMod.config.helmetTempItems.forEach((it, t) -> {
            if (Objects.equals(player.getInventory().getArmorStack(3).getItem().toString(), it)) {
                playerState.restingTemp += t + player.getInventory().getArmorStack(3).getNbt().getInt("wool");
                armorHeat.addAndGet(t + player.getInventory().getArmorStack(3).getNbt().getInt("wool"));
            }
        });

        //UPDATE - if a heat source is held in both the main hand and offhand, it only applies the highest heat value
        double heldHeat = 0;

        for (var entry : ThermMod.config.heldTempItems.entrySet())
        {
            String key = entry.getKey();
            double value = entry.getValue();

            boolean mainMatch = Objects.equals(player.getInventory().getMainHandStack().getItem().toString(), key);
            boolean offMatch  = Objects.equals(player.getInventory().offHand.get(0).getItem().toString(), key);

            if (mainMatch || offMatch)
            {
                if (value > heldHeat)
                {
                    heldHeat = value;
                }
            }
        }
        playerState.restingTemp += heldHeat;

        Vec3d pos = player.getPos();

        //UPDATE - removed stacking heat, changed to find the single strongest heating block in range and apply only that
        final AtomicReference<Double>[] strongestHeat = new AtomicReference[]{new AtomicReference<>(0.0)};

        Stream<BlockState> heatBlockBox = player.getWorld().getStatesInBox(Box.of(pos, 4, 4, 4));

        heatBlockBox.forEach((state) ->
        {
            for (var entry : ThermMod.config.heatingBlocks.entrySet()) {
                String key = entry.getKey();
                double value = entry.getValue();

                if (Objects.equals(state.toString(), key))
                {
                    if (state.isOf(Blocks.CAMPFIRE) || state.isOf(Blocks.SOUL_CAMPFIRE))
                    {
                        if (state.get(CampfireBlock.LIT))
                        {
                            if (value > strongestHeat[0].get()) strongestHeat[0].set(value);
                        }
                    }
                    else
                    {
                        if (value > strongestHeat[0].get()) strongestHeat[0].set(value);
                    }
                } else if (Objects.equals(state.getBlock().toString(), key) && !Objects.equals(state.toString(), state.getBlock().toString())) {
                    if (value > strongestHeat[0].get()) strongestHeat[0].set(value);
                }
            }
        });
        playerState.restingTemp += strongestHeat[0].get();

        final AtomicReference<Double>[] strongestCold = new AtomicReference[]{new AtomicReference<>(0.0)};
        Stream<BlockState> coldBlockBox = player.getWorld().getStatesInBox(Box.of(pos, 2, 3, 2));

        coldBlockBox.forEach((state) ->
        {
            for (var entry : ThermMod.config.coolingBlocks.entrySet())
            {
                String key = entry.getKey();
                double value = entry.getValue();

                if (Objects.equals(state.toString(), key))
                {
                    if (armorHeat.get() < 2)
                    {
                        if (value > strongestCold[0].get()) strongestCold[0].set(value);
                    }
                }
                else if (Objects.equals(state.getBlock().toString(), key) && !Objects.equals(state.toString(), state.getBlock().toString()))
                {
                    if (armorHeat.get() < 2)
                    {
                        if (value > strongestCold[0].get()) strongestCold[0].set(value);
                    }
                }
            }
        });
        playerState.restingTemp -= strongestCold[0].get();

        if(ThermMod.config.enableTemperatureDebug)
        {
            player.sendMessage(Text.literal(
                    "Temp: " + playerState.temp + "  Resting: " + playerState.restingTemp),
                    true
            );
        }

        //wind and fireplaces
        if (playerState.searchFireplaceTick <= 0)
        {
            playerState.searchFireplaceTick = 20;
            AtomicInteger fireplaces = new AtomicInteger();
            Stream<BlockState> fireplaceBox = player.getWorld().getStatesInBox(Box.of(pos, 80, 80, 80));
            fireplaceBox.forEach((state) -> {
                if (state.isOf(ThermBlocks.FIREPLACE_BLOCK))
                {
                    if (state.get(FireplaceBlock.LIT))
                    {
                        fireplaces.addAndGet(1);
                    }
                }
            });
            playerState.fireplaces = fireplaces.get();

            // this runs in console, can be kept enabled at all times
            ThermMod.LOGGER.info("fireplaces=" + playerState.fireplaces
                    + " restingTemp=" + playerState.restingTemp
                    + " temp=" + playerState.temp
                    + " windTemp=" + playerState.windTemp);

            //wind
            if (ThermMod.config.enableWind) {
                if (ThermMod.config.multidimensionalWind || dim.natural()) {
                    //wind base temperature calculation
                    double calcWindTemp = serverState.windTempModifier;

                    if (player.getPos().y > 62) {
                        double heightAddition = (player.getPos().y-62);
                        if (player.getPos().y <= 150) {
                            heightAddition = heightAddition/7;
                        } else {
                            heightAddition = heightAddition/8;
                        }
                        calcWindTemp -= heightAddition;
                    }

                    if (precip == Biome.Precipitation.RAIN)
                    {
                        if (player.getWorld().isRaining())
                        {
                            calcWindTemp += serverState.precipitationWindModifier;
                        } else if (player.getWorld().isThundering()) {
                            calcWindTemp += serverState.precipitationWindModifier;
                        }
                    } else if (precip == Biome.Precipitation.SNOW) {
                        if (player.getWorld().isRaining()) {
                            calcWindTemp += serverState.precipitationWindModifier*1.3;
                        }
                    }

                    playerState.baseWindTemp = calcWindTemp;
                    if (playerState.baseWindTemp > 0) {
                        playerState.baseWindTemp = 0;
                    }

                    //wind ray calculation
                    Random rand = new Random();
                    int unblockedRays = ThermMod.config.windRayCount;
                    for (int i = 0; i < ThermMod.config.windRayCount; i++) {

                        double turbulence = playerState.windTurbulence*Math.PI/180;

                        Vec3d dir = new Vec3d((Math.cos(serverState.windPitch+rand.nextDouble(-turbulence, turbulence)) * Math.cos(serverState.windYaw+rand.nextDouble(-turbulence, turbulence))), (Math.sin(serverState.windPitch+rand.nextDouble(-turbulence, turbulence)) * Math.cos(serverState.windYaw+rand.nextDouble(-turbulence, turbulence))), Math.sin(serverState.windYaw+rand.nextDouble(-turbulence, turbulence)));

                        Vec3d startPos = new Vec3d(player.getPos().x, player.getPos().y + 1, player.getPos().z);

                        BlockHitResult r = player.getWorld().raycast(new RaycastContext(startPos, startPos.add(dir.multiply(ThermMod.config.windRayLength)), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.WATER, player));
                        if (!player.getWorld().getBlockState(r.getBlockPos()).isAir()) {
                            unblockedRays -= 1;
                        }
                    }
                    playerState.windTemp = playerState.baseWindTemp * ((double) unblockedRays /ThermMod.config.windRayCount);
                } else if (!ThermMod.config.multidimensionalWind && !dim.natural()) {
                    playerState.windTemp = 0;
                }
            }
        }
        playerState.restingTemp += playerState.windTemp;
        playerState.searchFireplaceTick -= 1;

        if (player.isTouchingWater()) {
            playerState.restingTemp -= ThermMod.config.waterTempModifier;
        }

        //fire protection
        int fireProt = 0;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < player.getInventory().getArmorStack(i).getEnchantments().size(); j++) {
                if (Objects.equals(player.getInventory().getArmorStack(i).getEnchantments().getCompound(j).getString("id"), "minecraft:fire_protection")) {
                    fireProt += player.getInventory().getArmorStack(i).getEnchantments().getCompound(j).getInt("lvl");
                }
            }
        }

        player.getStatusEffects().forEach((i) -> {
            if (i.getTranslationKey().equals(ThermStatusEffects.COOLING.getTranslationKey())) {
                playerState.restingTemp -= (10 + (10 * i.getAmplifier()));
            }
        });
        playerState.restingTemp += (playerState.fireplaces * ThermMod.config.fireplaceTempModifier);

        if (Math.round(playerState.restingTemp) > Math.round(playerState.temp)) {
            playerState.temp += 0.25;
        } else if (Math.round(playerState.restingTemp) < Math.round(playerState.temp)) {
            playerState.temp -= 0.25;
        } else if (Math.round(playerState.restingTemp) == Math.round(playerState.temp)) {}

        if (playerState.temp <= ThermMod.config.freezeThreshold1 && playerState.temp > ThermMod.config.freezeThreshold2) {
            playerState.damageType = "freeze";
            playerState.maxDamageTick = ThermMod.config.temperatureDamageInterval;
        } else if (playerState.temp <= ThermMod.config.freezeThreshold2) {
            playerState.damageType = "freeze";
            playerState.maxDamageTick = ThermMod.config.extremeTemperatureDamageInterval;
        } else if (playerState.temp >= ThermMod.config.burnThreshold1 && playerState.temp < ThermMod.config.burnThreshold2) {
            playerState.damageType = "burn";
            playerState.maxDamageTick = ThermMod.config.temperatureDamageInterval;
        } else if (playerState.temp >= ThermMod.config.burnThreshold2) {
            playerState.damageType = "burn";
            playerState.maxDamageTick = ThermMod.config.extremeTemperatureDamageInterval;
        } else {
            playerState.damageTick = 0;
            playerState.damageType = "";
        }

        if (Objects.equals(playerState.damageType, "freeze")) {
            if (ThermMod.config.temperatureDamageDecreasesSaturation) {player.getHungerManager().setSaturationLevel(0f);}
            if (playerState.damageTick < playerState.maxDamageTick) {
                playerState.damageTick += 1;
            }
            if (playerState.damageTick >= playerState.maxDamageTick) {
                playerState.damageTick = 0;
                player.damage(player.getWorld().getDamageSources().freeze(), ThermMod.config.hypothermiaDamage);
            }
        } else if (Objects.equals(playerState.damageType, "burn")) {
            boolean res = false;
            try {
                String fireRes = Objects.requireNonNull(player.getStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE).getEffectType())).getEffectType().getName().getString();
                res = true;
            } catch (NullPointerException err) {res = false;}
            boolean protRes = false;
            int fireProtOver = fireProt-ThermMod.config.fireProtectionLevelCount;
            if (fireProt >= ThermMod.config.fireProtectionLevelCount && playerState.temp <= 70+(fireProtOver)) {
                protRes = true;
            }
            if (!res && !protRes) {

                if (ThermMod.config.temperatureDamageDecreasesSaturation) {player.getHungerManager().setSaturationLevel(0f);}
                if (playerState.damageTick < playerState.maxDamageTick) {
                    playerState.damageTick += 1;
                }
                if (playerState.damageTick >= playerState.maxDamageTick) {
                    playerState.damageTick = 0;
                    player.damage(player.getWorld().getDamageSources().onFire(), ThermMod.config.hyperthermiaDamage);
                }
            }
        }

        if (player.getHealth() <= 0.0) {
            playerState.temp = 50;
            playerState.damageTick = 0;
        }
        serverState.markDirty();
        sentTemperatureSync(player, playerState, serverState, tempDir);

    }
    private static void sentTemperatureSync(ServerPlayerEntity player, ThermPlayerState state, ServerState serverState, short dir)
    {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeDouble(state.temp);
        buf.writeShort(dir);
        buf.writeDouble(serverState.windPitch);
        buf.writeDouble(serverState.windYaw);
        buf.writeDouble(state.windTemp);

        ServerPlayNetworking.send(
                player,
                ThermNetworkingClient.SEND_THERMPLAYERSTATE_S2C_PACKET_ID,
                buf
        );
    }
}
