package me.deecaad.weaponmechanics.weapon.reload.ammo;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.SerializerException;
import me.deecaad.weaponmechanics.weapon.info.WeaponConverter;
import org.jetbrains.annotations.NotNull;

public class AmmoConverter extends WeaponConverter {

    public AmmoConverter() {
        super();
    }

    public AmmoConverter(boolean type, boolean name, boolean lore, boolean enchantments) {
        super(type, name, lore, enchantments);
    }

    @Override
    public String getKeyword() {
        return "Ammo_Converter_Check";
    }

    @Override
    public @NotNull AmmoConverter serialize(SerializeData data) throws SerializerException {
        boolean type = data.of("Type").getBool(false);
        boolean name = data.of("Name").getBool(false);
        boolean lore = data.of("Lore").getBool(false);
        boolean enchantments = data.of("Enchantments").getBool(false);

        if (!type && !name && !lore && !enchantments) {
            throw data.exception(null, "'Type', 'Name', 'Lore', and 'Enchantments' are all 'false'",
                    "One of them should be 'true' to allow ammo conversion",
                    "If you want to remove the ammo conversion feature, remove the '" + getKeyword() + "' option from config");
        }

        return new AmmoConverter(type, name, lore, enchantments);
    }
}