package thermite.therm;

import me.lortseam.completeconfig.api.ConfigEntry;
import me.lortseam.completeconfig.data.Config;
import me.lortseam.completeconfig.data.ConfigOptions;

import java.util.HashMap;
import java.util.Map;

public class ThermConfig extends Config
{
    //UPDATE - Added variables to make certain temperature modifiers easily configurable.
    @ConfigEntry(comment = "Variable for adjusting fireplace heat. Default 14. Increasing it will make it hotter.")
    public int fireplaceTempModifier = 14;
    @ConfigEntry(comment = "Variable for adjusting fireplace radius. Default is 40 blocks, but code checks < 40. Add +1 to desired value.")
    public int fireplaceRadius = 41;
    @ConfigEntry(comment = "Variable for adjusting night temp modifier. This is for the frigid climate. Increasing it will make it colder.")
    public float nightFrigidTempModifier = 12;
    @ConfigEntry(comment = "Variable for adjusting night temp modifier. This is for the cold climate. Increasing it will make it colder.")
    public float nightColdTempModifier = 10;
    @ConfigEntry(comment = "Variable for adjusting night temp modifier. This is for the temperate climate. Increasing it will make it colder.")
    public float nightTemperateTempModifier = 8;
    @ConfigEntry(comment = "Variable for adjusting night temp modifier. This is for the hot climate. Increasing it will make it colder.")
    public float nightHotTempModifier = 6;
    @ConfigEntry(comment = "Variable for adjusting night temp modifier. This is for the arid climate. Increasing it will make it colder.")
    public float nightAridTempModifier = 14;
    @ConfigEntry(comment = "Variable for adjusting rain modifier. Default 8. Increasing it will make it colder.")
    public int rainTempModifier = 8;
    @ConfigEntry(comment = "Variable for adjusting snow modifier. Default 8. Increasing it will make it colder")
    public int snowTempModifier = 8;
    @ConfigEntry(comment = "Variable for adjusting water modifier. Default 10. Increasing it will make it colder")
    public int waterTempModifier = 10;

    //Update - added option to control how often temperature checks occur
    @ConfigEntry(comment = "Variable for controlling how often the mod checks for temperature. Measured in ticks. 20 ticks = 1 second. Default 20")
    public int tempTickCount = 20;

    //Update - added boolean option to enable/disable temperature debug.
    @ConfigEntry(comment = "Variable to enable/disable debug log. Set to true to enable. Default false.")
    public boolean enableTemperatureDebug = true;

    //Update - added boolean option to enable/disable performance debug.
    @ConfigEntry(comment = "Variable to enable/disable performance debug. Set to true to enable. Default false.")
    public boolean enablePerformanceDebug = true;

    @ConfigEntry(comment = "X coordinate of temperature UI relative to its default position. (Default: 0)")
    public int temperatureXPos = 0;

    @ConfigEntry(comment = "Y coordinate of temperature UI relative to its default position. (Default: 0)")
    public int temperatureYPos = 0;

    @ConfigEntry(comment = "X coordinate of thermometer UI relative to its default position. (Default: 0)")
    public int thermometerXPos = 0;

    @ConfigEntry(comment = "Y coordinate of thermometer UI relative to its default position. (Default: 0)")
    public int thermometerYPos = 0;

    @ConfigEntry(comment = "Different styles for the temperature display. (options: gauge, glass_thermometer)")
    public String temperatureDisplayType = "glass_thermometer";

    @ConfigEntry(comment = "Whether or not temperature damage decreases your saturation. Beware disabling this makes it really easy to bypass temperature damage just by eating. (Default: true)")
    public boolean temperatureDamageDecreasesSaturation = true;

    @ConfigEntry(comment = "When enabled, being cold enough causes a blue outline effect. And being hot enough causes an orange one. (Default: true)")
    public boolean enableTemperatureVignette = false;

    @ConfigEntry(comment = "When enabled, particles will spawn showing the direction that the wind is flowing. More wind = more particles. (Default: true)")
    public boolean enableWindParticles = true;

    //game
    @ConfigEntry(comment = "How many levels of fire protection you have to wear for it to start providing hyperthermia resistance. (Default: 6)")
    public int fireProtectionLevelCount = 6;

    //TODO this
    @ConfigEntry(comment = "Hyperthermia damage per some... seconds. (Default: 1.5)")
    public float hyperthermiaDamage = 1.5f;

    @ConfigEntry(comment = "Hypothermia damage per some... seconds. (Default: 1.5)")
    public float hypothermiaDamage = 1.5f;

    @ConfigEntry(comment = "Helmets that will change your temperature.")
    public Map<String, Integer> helmetTempItems = new HashMap(Map.of("leather_helmet", 1));

    @ConfigEntry(comment = "Chestplates that will change your temperature.")
    public Map<String, Integer> chestplateTempItems = new HashMap(Map.of("leather_chestplate", 3));

    @ConfigEntry(comment = "Leggings that will change your temperature.")
    public Map<String, Integer> leggingTempItems = new HashMap(Map.of("leather_leggings", 2));

    @ConfigEntry(comment = "Boots that will change your temperature.")
    public Map<String, Integer> bootTempItems = new HashMap(Map.of("leather_boots", 1));

    @ConfigEntry(comment = "Items that when held will change your temperature.")
    public Map<String, Integer> heldTempItems = new HashMap(Map.of("torch", 3, "lava_bucket", 3));

    @ConfigEntry(comment = "Blocks that will heat you up when near.")
    public Map<String, Integer> heatingBlocks = new HashMap(Map.ofEntries(
            Map.entry("Block{minecraft:fire}", 3),
            Map.entry("Block{minecraft:lava}", 1),
            Map.entry("Block{minecraft:campfire}", 15),
            Map.entry("Block{minecraft:torch}", 1),
            Map.entry("Block{minecraft:wall_torch}", 1),
            Map.entry("Block{minecraft:soul_torch}", 1),
            Map.entry("Block{minecraft:soul_wall_torch}", 3),
            Map.entry("Block{minecraft:soul_campfire}", 15),
            Map.entry("Block{minecraft:lava_cauldron}", 6),
            Map.entry("Block{minecraft:furnace}[facing=north,lit=true]", 8),
            Map.entry("Block{minecraft:furnace}[facing=east,lit=true]", 8),
            Map.entry("Block{minecraft:furnace}[facing=south,lit=true]", 8),
            Map.entry("Block{minecraft:furnace}[facing=west,lit=true]", 8),
            Map.entry("Block{minecraft:blast_furnace}[facing=north,lit=true]", 8),
            Map.entry("Block{minecraft:blast_furnace}[facing=east,lit=true]", 8),
            Map.entry("Block{minecraft:blast_furnace}[facing=south,lit=true]", 8),
            Map.entry("Block{minecraft:blast_furnace}[facing=west,lit=true]", 8),
            Map.entry("Block{minecraft:smoker}[facing=north,lit=true]", 6),
            Map.entry("Block{minecraft:smoker}[facing=east,lit=true]", 6),
            Map.entry("Block{minecraft:smoker}[facing=south,lit=true]", 6),
            Map.entry("Block{minecraft:smoker}[facing=west,lit=true]", 6)
            ));

    @ConfigEntry(comment = "Blocks that will cool you down when near.")
    public Map<String, Integer> coolingBlocks = new HashMap(Map.ofEntries(
            Map.entry("Block{minecraft:ice}", 1),
            Map.entry("Block{minecraft:packed_ice}", 3),
            Map.entry("Block{minecraft:blue_ice}", 6),
            Map.entry("Block{minecraft:powder_snow}", 1)
            ));

    @ConfigEntry(comment = "Base temperature for frigid climates. (Default: 25.0)")
    public double frigidClimateTemp = 25;

    @ConfigEntry(comment = "Base temperature for cold climates. (Default: 30.0)")
    public double coldClimateTemp = 30;

    @ConfigEntry(comment = "Base temperature for temperate climates. (Default: 50.0)")
    public double temperateClimateTemp = 50;

    @ConfigEntry(comment = "Base temperature for hot climates. (Default: 55.0)")
    public double hotClimateTemp = 55;

    @ConfigEntry(comment = "Base temperature for arid climates. (Default: 70.0)")
    public double aridClimateTemp = 70;

    @ConfigEntry(comment = "First threshold for hypothermia, being below this you will start to freeze (Default: 35)")
    public int freezeThreshold1 = 35;

    @ConfigEntry(comment = "Second threshold for hypothermia, being below this you will freeze faster. (Default: 25)")
    public int freezeThreshold2 = 25;

    @ConfigEntry(comment = "First threshold for hyperthermia, being above this you will start to burn (Default: 65)")
    public int burnThreshold1 = 65;

    @ConfigEntry(comment = "Second threshold for hyperthermia, being above this you will burn faster (Default: 75)")
    public int burnThreshold2 = 75;

    @ConfigEntry(comment = "Damage interval for hypothermia and hyperthermia in seconds (Default: 3)")
    public int temperatureDamageInterval = 3;

    @ConfigEntry(comment = "Damage interval for extreme hypothermia and hyperthermia in seconds (Default: 2)")
    public int extremeTemperatureDamageInterval = 2;

    @ConfigEntry(comment = "Duration of the cooling effect of ice juice in ticks. (Default: 9600)")
    public int iceJuiceEffectDuration = 9600;

    @ConfigEntry(comment = "Disables or enables wind. (Default: true)")
    public boolean enableWind = true;

    @ConfigEntry(comment = "If disabled, wind will only be applied in the overworld. (Default: false)")
    public boolean multidimensionalWind = false;

    @ConfigEntry(comment = "Number of rays used in wind calculation. (Default: 32)")
    public int windRayCount = 32;

    @ConfigEntry(comment = "How many blocks long wind rays are. (Default: 32)")
    public int windRayLength = 32;

    public ThermConfig() {
        super(ConfigOptions.mod(ThermMod.modID));
    }

}