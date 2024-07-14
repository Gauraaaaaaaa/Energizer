package com.gaura.energizer;

import com.gaura.energizer.config.EnergizerConfig;
import com.gaura.energizer.effect.VigorEffect;
import com.gaura.energizer.mixin.BrewingRecipeRegistryInvoker;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;

import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class Energizer implements ModInitializer {

	public static final String MOD_ID = "energizer";
	public static final String HEARTY_MEALS_MOD_ID = "heartymeals";
	public static final String NOSTALGIC_TWEAKS_MOD_ID = "nostalgic_tweaks";

	public static EnergizerConfig CONFIG = new EnergizerConfig();

	// Stamina attribute
	public static final EntityAttribute STAMINA_ATTRIBUTE = new ClampedEntityAttribute("attribute.name." + MOD_ID + ".player.stamina", 20, 1, 1024).setTracked(true);

	// Stamina data
	public static final TrackedData<Float> STAMINA_DATA = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.FLOAT);

	// Vigor effect
	public static final VigorEffect VIGOR = new VigorEffect(StatusEffectCategory.BENEFICIAL, 0x14B2FF);

	// Vigor potions
	public static final Potion VIGOR_POTION = new Potion(new StatusEffectInstance(VIGOR, CONFIG.vigor_potion_time * 20, 0));
	public static final Potion VIGOR_POTION_LONG = new Potion(new StatusEffectInstance(VIGOR, CONFIG.vigor_potion_long_time * 20, 0));

	@Override
	public void onInitialize() {

		// Registering Config
		AutoConfig.register(EnergizerConfig.class, JanksonConfigSerializer::new);
		CONFIG = AutoConfig.getConfigHolder(EnergizerConfig.class).getConfig();

		// Registering Stamina attribute
		Registry.register(Registries.ATTRIBUTE, new Identifier(MOD_ID, "generic.stamina"), STAMINA_ATTRIBUTE);

		// Registering Vigor effect
		Registry.register(Registries.STATUS_EFFECT, new Identifier(MOD_ID, "vigor"), VIGOR);

		if (CONFIG.vigor_potion) {

			// Registering Vigor potion
			Registry.register(Registries.POTION, new Identifier(MOD_ID, "vigor_potion"), VIGOR_POTION);
			BrewingRecipeRegistryInvoker.invokeRegisterPotionRecipe(Potions.AWKWARD, Items.BEETROOT, VIGOR_POTION);
		}

		if (CONFIG.vigor_potion_long) {

			// Registering Vigor long potion
			Registry.register(Registries.POTION, new Identifier(MOD_ID, "vigor_potion_long"), VIGOR_POTION_LONG);
			BrewingRecipeRegistryInvoker.invokeRegisterPotionRecipe(VIGOR_POTION, Items.REDSTONE, VIGOR_POTION_LONG);
		}
	}
}