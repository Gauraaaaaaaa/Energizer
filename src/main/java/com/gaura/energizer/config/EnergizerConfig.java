package com.gaura.energizer.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = "energizer")
@Config.Gui.Background("minecraft:textures/block/yellow_wool.png")
public class EnergizerConfig implements ConfigData {

    // STAMINA

    @ConfigEntry.Category("stamina")
    @Comment("The amount of stamina decreased per tick when sprinting.")
    public float stamina_decrease = 0.5F;

    @ConfigEntry.Category("stamina")
    @Comment("The amount of stamina decreased per tick when sprinting and hungry.")
    public float stamina_decrease_hunger = 1F;

    @ConfigEntry.Category("stamina")
    @Comment("The amount of stamina increased per tick when not sprinting.")
    public float stamina_increase = 0.5F;

    @ConfigEntry.Category("stamina")
    @Comment("The amount of stamina increased per tick when not sprinting and hungry.")
    public float stamina_increase_hunger = 0.25F;

    @ConfigEntry.Category("stamina")
    @Comment("The amount of stamina increased per tick when not sprinting and stamina completely empty.")
    public float stamina_increase_empty = 0.25F;

    @ConfigEntry.Category("stamina")
    @Comment("The amount of stamina increased per tick when not sprinting, hungry and stamina completely empty.")
    public float stamina_increase_hunger_empty = 0.125F;

    @ConfigEntry.Category("stamina")
    @Comment("If the stamina bar should blink when stamina is full.")
    public boolean stamina_blink = true;

    @ConfigEntry.Category("stamina")
    @Comment("If the stamina bar should wave when having Vigor effect.")
    public boolean vigor_wave = true;

    @ConfigEntry.Category("stamina")
    @Comment("If you must keep pressing the sprint keybind to sprint, rather than toggling it.")
    public boolean sprint_keybind = true;

    @ConfigEntry.Category("stamina")
    @Comment("If the player will jump lower when the stamina bar is empty.")
    public boolean lower_jump = true;

    @ConfigEntry.Category("stamina")
    @Comment("The multiplier for the jump height when the stamina bar is empty.")
    public float lower_jump_multiplier = 0.75F;

    @ConfigEntry.Category("stamina")
    @Comment("If the player will walk slower when the stamina bar is empty.")
    public boolean slower_walk = true;

    @ConfigEntry.Category("stamina")
    @Comment("The multiplier for the walk speed when the stamina bar is empty.")
    public float slower_walk_multiplier = 0.5F;

    @ConfigEntry.Category("stamina")
    @Comment("The x offset for the stamina bar.")
    public int x_offset_stamina_bar = 0;

    @ConfigEntry.Category("stamina")
    @Comment("The y offset for the stamina bar.")
    public int y_offset_stamina_bar = 0;

    // HUNGER

    @ConfigEntry.Category("hunger")
    @Comment("If the hunger should be removed.")
    public boolean remove_hunger = true;

    @ConfigEntry.Category("hunger")
    @Comment("If the player can sprint when the hunger bar is below 6 (3 icons).")
    public boolean can_continue_sprinting = false;

    @ConfigEntry.Category("hunger")
    @Comment("The x offset for the hunger bar.")
    public int x_offset_hunger_bar = 0;

    @ConfigEntry.Category("hunger")
    @Comment("The y offset for the hunger bar.")
    public int y_offset_hunger_bar = 0;

    // AIR

    @ConfigEntry.Category("air")
    @Comment("If the air bar should be synced with the stamina bar (on top of it).")
    public boolean sync_with_stamina_bar = true;

    @ConfigEntry.Category("air")
    @Comment("The x offset for the air bar.")
    public int x_offset_air_bar = 0;

    @ConfigEntry.Category("air")
    @Comment("The y offset for the air bar.")
    public int y_offset_air_bar = 0;

    // POTION

    @ConfigEntry.Category("potion")
    @ConfigEntry.Gui.RequiresRestart
    @Comment("If the vigor potion should be available.")
    public boolean vigor_potion = true;

    @ConfigEntry.Category("potion")
    @ConfigEntry.Gui.RequiresRestart
    @Comment("The time the vigor potion should last (in seconds).")
    public int vigor_potion_time = 10;

    @ConfigEntry.Category("potion")
    @ConfigEntry.Gui.RequiresRestart
    @Comment("If the long vigor potion should be available.")
    public boolean vigor_potion_long = true;

    @ConfigEntry.Category("potion")
    @ConfigEntry.Gui.RequiresRestart
    @Comment("The time the long vigor potion should last (in seconds).")
    public int vigor_potion_long_time = 30;

    // HEAL

    @ConfigEntry.Category("heal")
    @Comment("The multiplier for the amount of health restored by food (do nothing if 'Remove Hunger' is disabled).")
    public int heal_multiplier = 1;
}
