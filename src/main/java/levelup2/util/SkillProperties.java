package levelup2.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import levelup2.api.IPlayerSkill;
import levelup2.skills.SkillRegistry;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.util.ArrayList;
import java.util.List;

public class SkillProperties {
    private String skillName;
    private int[] levels;
    private String[] prerequisites;
    private int column;
    private int row;
    private boolean enabled;
    private boolean active;

    public static SkillProperties fromJson(String skillName, JsonObject obj) {
        int[] levels = getLevels(obj);
        String[] prerequisites = getPrerequisites(obj);
        int column = JsonUtils.getInt(obj, "column", 0);
        int row = JsonUtils.getInt(obj, "row", 0);
        boolean enabled = JsonUtils.getBoolean(obj, "enabled", true);
        boolean active = JsonUtils.getBoolean(obj, "active", true);
        return new SkillProperties(skillName, levels, prerequisites, column, row, enabled, active);
    }

    public SkillProperties(String skillName, int[] levelCosts, String[] prerequisites, int column, int row, boolean enabled, boolean active) {
        this.skillName = skillName;
        this.levels = levelCosts;
        this.prerequisites = prerequisites;
        this.column = column;
        this.row = row;
        this.enabled = enabled;
        this.active = active;
    }

    public int[] getLevels() {
        return levels;
    }

    public String[] getPrerequisites() {
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

    public void writeToBytes(ByteBuf buf) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("name", skillName);
        if (getLevels() != null && getLevels().length > 0)
            tag.setIntArray("levels", getLevels());
        NBTTagCompound prereqs = new NBTTagCompound();
        if (getPrerequisites() != null && getPrerequisites().length > 0) {
            for (int i = 0; i < getPrerequisites().length; i++) {
                String prereq = getPrerequisites()[i];
                prereqs.setString("prereq_" + i, prereq);
            }
            tag.setTag("prereqs", prereqs);
        }
        tag.setInteger("column", getColumn());
        tag.setInteger("row", getRow());
        tag.setBoolean("enabled", isEnabled());
        tag.setBoolean("active", isActive());
        ByteBufUtils.writeTag(buf, tag);
    }

    public static void fromNBT(NBTTagCompound tag) {
        IPlayerSkill skill = SkillRegistry.getSkillFromName(tag.getString("name"));
        if (skill != null) {
            if (tag.hasKey("levels"))
                skill.setLevelCosts(tag.getIntArray("levels"));
            else skill.setLevelCosts(new int[0]);
            if (tag.hasKey("prereqs")) {
                NBTTagCompound t = tag.getCompoundTag("prereqs");
                int i = 0;
                List<String> prereqs = new ArrayList<>();
                while (t.hasKey("prereq_" + i)) {
                    prereqs.add(t.getString("prereq_" + i));
                    i++;
                }
                String[] str = new String[prereqs.size()];
                for (int j = 0; j < str.length; j++)
                    str[j] = prereqs.get(j);
                skill.setPrerequisites(str);
            } else skill.setPrerequisites(new String[0]);
            skill.setSkillColumn(tag.getInteger("column"));
            skill.setSkillRow(tag.getInteger("row"));
            skill.setEnabled(tag.getBoolean("enabled"));
            skill.setActive(tag.getBoolean("active"));
        }
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

    private static String[] getPrerequisites(JsonObject obj) {
        JsonArray prereqs = JsonUtils.getJsonArray(obj, "prerequisites", null);
        if (prereqs != null) {
            List<String> prereq = new ArrayList<>();
            for (JsonElement element : prereqs) {
                prereq.add(element.getAsString());
            }
            String[] str = new String[prereq.size()];
            for (int i = 0; i < str.length; i++)
                str[i] = prereq.get(i);
            return str;
        }
        return new String[0];
    }
}
