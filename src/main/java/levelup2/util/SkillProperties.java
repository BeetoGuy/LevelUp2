package levelup2.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import levelup2.api.IPlayerSkill;
import levelup2.skills.SkillRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.util.ArrayList;
import java.util.List;

public class SkillProperties {
    private ResourceLocation skillName;
    private ResourceLocation skillType;
    private int[] levels;
    private ResourceLocation[] prerequisites;
    private int column;
    private int row;
    private boolean enabled;
    private boolean active;
    private double divisor;
    private ItemStack repStack = ItemStack.EMPTY;

    public static SkillProperties fromJson(JsonObject obj) {
        int[] levels = getLevels(obj);
        ResourceLocation skillName = new ResourceLocation(JsonUtils.getString(obj, "name"));
        ResourceLocation[] prerequisites = getPrerequisites(obj);
        int column = JsonUtils.getInt(obj, "column", 0);
        int row = JsonUtils.getInt(obj, "row", 0);
        boolean enabled = JsonUtils.getBoolean(obj, "enabled", true);
        boolean active = JsonUtils.getBoolean(obj, "active", true);
        ResourceLocation type = new ResourceLocation(JsonUtils.getString(obj, "type"));
        double divisor = JsonUtils.getInt(obj, "divisor", 1);
        ItemStack rep = ShapedRecipes.deserializeItem(JsonUtils.getJsonObject(obj, "stack"), false);
        return new SkillProperties(skillName, type, levels, prerequisites, column, row, enabled, active, divisor, rep);
    }

    public SkillProperties(ResourceLocation skillName, ResourceLocation skillType, int[] levelCosts, ResourceLocation[] prerequisites, int column, int row, boolean enabled, boolean active, double divisor, ItemStack rep) {
        this.skillName = skillName;
        this.skillType = skillType;
        this.levels = levelCosts;
        this.prerequisites = prerequisites;
        this.column = column;
        this.row = row;
        this.enabled = enabled;
        this.active = active;
        this.repStack = rep;
        this.divisor = divisor;
    }

    public ResourceLocation getName() {
        return skillName;
    }

    public ResourceLocation getType() {
        return skillType;
    }

    public int[] getLevels() {
        return levels;
    }

    public ResourceLocation[] getPrerequisites() {
        return prerequisites;
    }

    public int getColumn() {
        return column;
    }

    public int getRow() {
        return row;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isActive() {
        return active;
    }

    public double getDivisor() {
        return divisor;
    }

    public ItemStack getRepStack() {
        return repStack;
    }

    public void writeToBytes(ByteBuf buf) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("name", skillName.toString());
        tag.setString("type", skillType.toString());
        if (getLevels() != null && getLevels().length > 0)
            tag.setIntArray("levels", getLevels());
        if (getPrerequisites() != null && getPrerequisites().length > 0) {
            NBTTagCompound prereqs = new NBTTagCompound();
            prereqs.setByte("Size", (byte)getPrerequisites().length);
            for (int i = 0; i < getPrerequisites().length; i++) {
                ResourceLocation prereq = getPrerequisites()[i];
                prereqs.setString("prereq_" + i, prereq.toString());
            }
            tag.setTag("prereqs", prereqs);
        }
        tag.setInteger("column", getColumn());
        tag.setInteger("row", getRow());
        tag.setBoolean("enabled", isEnabled());
        tag.setBoolean("active", isActive());
        tag.setDouble("divisor", getDivisor());
        if (!repStack.isEmpty()) {
            tag.setTag("item", repStack.serializeNBT());
        }
        ByteBufUtils.writeTag(buf, tag);
    }

    public static SkillProperties fromNBT(NBTTagCompound tag) {
        ResourceLocation name = new ResourceLocation(tag.getString("name"));
        ResourceLocation type = new ResourceLocation(tag.getString("type"));
        int[] levels = tag.hasKey("levels") ? tag.getIntArray("levels") : new int[0];
        ResourceLocation[] prereqs = new ResourceLocation[0];
        if (tag.hasKey("prereqs")) {
            NBTTagCompound prereq = tag.getCompoundTag("prereqs");
            byte size = prereq.getByte("Size");
            prereqs = new ResourceLocation[size];
            for (int i = 0; i < size; i++) {
                prereqs[i] = new ResourceLocation(prereq.getString("prereq_" + i));
            }
        }
        int column = tag.getInteger("column");
        int row = tag.getInteger("row");
        boolean enabled = tag.getBoolean("enabled");
        boolean active = tag.getBoolean("active");
        double divisor = tag.getDouble("divisor");
        ItemStack stack = ItemStack.EMPTY;
        if (tag.hasKey("item")) {
            stack = new ItemStack(tag.getCompoundTag("item"));
        }
        return new SkillProperties(name, type, levels, prereqs, column, row, enabled, active, divisor, stack);
    }

    private static int[] getLevels(JsonObject obj) {
        JsonArray levels = JsonUtils.getJsonArray(obj, "levels", null);
        if (levels != null) {
            List<Integer> lvl = new ArrayList<>();
            for (JsonElement element : levels) {
                lvl.add(element.getAsInt());
            }
            int[] lvls = new int[lvl.size()];
            for (int i = 0; i < lvls.length; i++) {
                lvls[i] = lvl.get(i);
            }
            return lvls;
        }
        return new int[0];
    }

    private static ResourceLocation[] getPrerequisites(JsonObject obj) {
        JsonArray prereqs = JsonUtils.getJsonArray(obj, "prerequisites", null);
        if (prereqs != null) {
            List<String> prereq = new ArrayList<>();
            for (JsonElement element : prereqs) {
                prereq.add(element.getAsString());
            }
            ResourceLocation[] str = new ResourceLocation[prereq.size()];
            for (int i = 0; i < str.length; i++)
                str[i] = new ResourceLocation(prereq.get(i));
            return str;
        }
        return new ResourceLocation[0];
    }
}
