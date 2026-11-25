package com.rigmod;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RigLogic {
    private static final Pattern NAME = Pattern.compile("^\\s*([1-9])\\s*([HhVv])\\s*$");

    /** side is 'H' (host) or 'V' (viewer). */
    public record Tag(char side, int number) {}

    private RigLogic() {}

    /** Parses items named "N H" / "N V". Returns null for anything else. */
    public static Tag tag(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        if (!stack.contains(DataComponentTypes.CUSTOM_NAME)) return null;
        Matcher m = NAME.matcher(stack.getName().getString());
        if (!m.matches()) return null;
        return new Tag(Character.toUpperCase(m.group(2).charAt(0)), m.group(1).charAt(0) - '0');
    }

    /**
     * RENDER-ONLY. Any "N H" / "N V" item that is NOT part of the dropper slot display
     * (on the ground, in hand, on the cursor, in the inventory...) is drawn as the number that "dropped"
     * for that side (chosen when the rig was turned on). The real item is never touched.
     */
    public static ItemStack disguise(ItemStack stack) {
        if (!RigState.enabled || stack == null || stack.isEmpty()) return stack;
        if (RigView.isNormal(stack)) return stack;
        Tag t = tag(stack);
        if (t == null) return stack;
        int shown = RigState.pick(t.side());
        return lookFor(t.side(), shown, stack.getCount(), stack.getName());
    }

    /** An item that looks like "<number> <side>". Uses a snapshot of the real one when available. */
    private static ItemStack lookFor(char side, int number, int count, Text originalName) {
        RigConfig cfg = RigConfig.INSTANCE;
        ItemStack out;

        ItemStack snap = RigView.snapshot(side, number);
        ItemStack template = RigState.dropTemplate;
        if (snap != null) {
            out = snap.copy();
        } else if (template != null && !template.isEmpty()) {
            out = template.copy();
        } else {
            Identifier id = Identifier.tryParse(cfg.dropItem == null ? "" : cfg.dropItem.trim());
            Item item = id == null ? Items.PAPER : Registries.ITEM.get(id);
            if (item == Items.AIR) item = Items.PAPER;
            out = new ItemStack(item);

            String model = cfg.dropModel;
            if (model != null && !model.isBlank()) {
                Identifier mid = Identifier.tryParse(model.trim());
                if (mid != null) out.set(DataComponentTypes.ITEM_MODEL, mid);
            }
        }

        out.setCount(count);
        out.set(DataComponentTypes.CUSTOM_NAME,
            Text.literal(number + " " + side).setStyle(originalName.getStyle()));
        return out;
    }
}
