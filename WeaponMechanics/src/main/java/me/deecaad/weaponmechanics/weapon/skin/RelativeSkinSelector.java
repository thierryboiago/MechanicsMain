package me.deecaad.weaponmechanics.weapon.skin;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class RelativeSkinSelector implements SkinSelector, Serializer<RelativeSkinSelector> {

    private BaseSkin base;
    private Map<String, RelativeSkin> skins;
    private Map<SkinAction, RelativeSkin> actions;
    private Map<String, RelativeSkin> attachments;

    /**
     * Default constructor for serializer
     */
    public RelativeSkinSelector() {
    }

    public RelativeSkinSelector(BaseSkin base, Map<String, RelativeSkin> skins, Map<SkinAction, RelativeSkin> actions, Map<String, RelativeSkin> attachments) {
        this.base = base;
        this.skins = skins;
        this.actions = actions;
        this.attachments = attachments;
    }

    @Override
    public @NotNull BaseSkin getDefaultSkin() {
        return base;
    }

    public Map<String, RelativeSkin> getSkins() {
        return skins;
    }

    public Map<SkinAction, RelativeSkin> getActions() {
        return actions;
    }

    public Map<String, RelativeSkin> getAttachments() {
        return attachments;
    }

    @Override
    public boolean hasAction(@Nullable String skin, @Nullable SkinAction action) {
        return actions.containsKey(action);
    }

    @Override
    public void apply(@NotNull ItemStack weapon, @Nullable String skin, @Nullable SkinAction action, @Nullable String[] attachments) {

        // Ideally this will never be true, but sometimes we try to apply skins
        // to AIR which causes errors.
        if (!weapon.hasItemMeta())
            return;

        // We set these to null because we must skip them. base is always
        // added, we shouldn't add it twice.
        if ("default".equals(skin))
            skin = null;
        if (action == SkinAction.DEFAULT)
            action = null;

        // Determine which custom model data to use.
        int customModelData = base.getCustomModelData();
        RelativeSkin relativeSkin;
        if (skin != null && (relativeSkin = skins.get(skin)) != null)
            customModelData += relativeSkin.getCustomModelData();
        RelativeSkin relativeAction;
        if (action != null && (relativeAction = actions.get(action)) != null)
            customModelData += relativeAction.getCustomModelData();
        if (attachments != null) {
            for (String attachment : attachments) {
                RelativeSkin relativeAttachment = this.attachments.get(attachment);
                if (relativeAttachment != null)
                    customModelData += relativeAttachment.getCustomModelData();
            }
        }

        if (base.hasType() && weapon.getType() != base.getType())
            weapon.setType(base.getType());

        ItemMeta meta = weapon.getItemMeta();
        meta.setCustomModelData(customModelData);
        weapon.setItemMeta(meta);
    }

    @NotNull
    @Override
    public RelativeSkinSelector serialize(SerializeData data) throws SerializerException {

        Map<String, RelativeSkin> skins = new LinkedHashMap<>();
        Map<SkinAction, RelativeSkin> actions = new LinkedHashMap<>();
        Map<String, RelativeSkin> attachments = new LinkedHashMap<>();

        // The base skin can choose the skin material, but usually it's just an int
        BaseSkin base;
        if (data.of("Default").is(ConfigurationSection.class))
            base = data.of("Default").serialize(BaseSkin.class);
        else
            base = new BaseSkin(data.of("Default").assertExists().getInt());

        // Now everything else is expected to be a relative skin. Anything else
        // is an error.
        ConfigurationSection section = data.of().assertExists().assertType(ConfigurationSection.class).get();
        Set<String> keys = section.getKeys(false);
        for (String key : keys) {

            // Attachments -> it is handled later
            // Item -> it is specifically for hand items, and handled by WMC
            // Default -> it is already handled in the BaseSkin
            if ("Attachments".equals(key) || "Item".equals(key) || "Default".equals(key))
                continue;

            // When this is null, that means that it is a skin
            SkinAction action = SkinAction.fromString(key);
            if (action == null) {
                skins.put(key, data.of(key).serialize(RelativeSkin.class));
                continue;
            }

            // SCOPE_STACK matches SCOPE_1, SCOPE_2, etc.. So we need to make a proper one
            if (action == SkinAction.SCOPE_STACK)
                action = new SkinAction(key);

            actions.put(action, data.of(key).serialize(RelativeSkin.class));
        }

        // Check for attachments
        ConfigurationSection attachmentSection = data.of("Attachments").assertType(ConfigurationSection.class).get(null);
        Set<String> attachmentKeys = attachmentSection == null ? null : attachmentSection.getKeys(false);

        if (attachmentKeys != null) {
            for (String attachment : attachmentKeys) {

                // We can't really check if an attachment exists since we
                // cannot access the WMP API, so users may be prone to error
                RelativeSkin relativeSkin = data.of("Attachments." + attachment).serialize(RelativeSkin.class);
                if (relativeSkin == null) {
                    throw data.exception("Attachments." + attachment, "Some error occurred when making the relative skin",
                            "Please make sure you formatted things correctly... An example value is something like '+10' or '+100'");
                }

                attachments.put(attachment, relativeSkin);
            }
        }

        return new RelativeSkinSelector(base, skins, actions, attachments);
    }
}
