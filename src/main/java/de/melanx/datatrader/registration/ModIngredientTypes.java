package de.melanx.datatrader.registration;

import de.melanx.datatrader.ingredients.TaggedDataComponentIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import org.moddingx.libx.annotation.registration.RegisterClass;

@RegisterClass(registry = "INGREDIENT_TYPES")
public class ModIngredientTypes {

    public static final IngredientType<TaggedDataComponentIngredient> tagWithComponents = new IngredientType<>(TaggedDataComponentIngredient.CODEC);
}
