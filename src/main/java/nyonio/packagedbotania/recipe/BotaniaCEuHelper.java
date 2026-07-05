package nyonio.packagedbotania.recipe;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;

public class BotaniaCEuHelper {

    private static Boolean loaded = null;
    private static Field retainedItemsSetField;
    private static Method stringifyStackMethod;

    public static boolean isLoaded() {
        if(loaded == null) {
            try {
                Class<?> configHandlerClass = Class.forName("vazkii.botania.common.core.handler.ConfigHandler");
                retainedItemsSetField = configHandlerClass.getField("runicAltarRetainedItemsSet");
                Class<?> inventoryHelperClass = Class.forName("vazkii.botania.common.core.helper.InventoryHelper");
                stringifyStackMethod = inventoryHelperClass.getMethod("stringifyStack", ItemStack.class);
                loaded = true;
            } catch(Exception e) {
                loaded = false;
            }
        }
        return loaded;
    }

    public static boolean shouldRetainAfterCraft(ItemStack stack) {
        if(!isLoaded()) {
            return false;
        }
        try {
            Set<?> retainedSet = (Set<?>) retainedItemsSetField.get(null);
            String itemString = (String) stringifyStackMethod.invoke(null, stack);
            return retainedSet.contains(itemString);
        } catch(Exception e) {
            return false;
        }
    }
}
