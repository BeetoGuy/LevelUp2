package levelup2.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import levelup2.util.SkillProperties;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;

public class BaseSkill implements IPlayerSkill {
    private int[] skillLevels = {};
    private ResourceLocation skillType;
    private ResourceLocation skillName;
    private ResourceLocation[] prereqs = {};
    private int column = 0;
    private int row = 0;
    private boolean enabled = true;
    private boolean active = true;
    private ItemStack repStack;

    public static BaseSkill fromProps(SkillProperties props) {
        BaseSkill skill = new BaseSkill();
        skill.skillName = props.getName();
        skill.skillType = props.getType();
        skill.skillLevels = props.getLevels();
        skill.prereqs = props.getPrerequisites();
        skill.column = props.getColumn();
        skill.row = props.getRow();
        skill.enabled = props.isEnabled();
        skill.active = props.isActive();
        skill.repStack = props.getRepStack();
        return skill;
    }

    public BaseSkill fromJson(JsonObject json) {
        BaseSkill skill = new BaseSkill();
        skill.skillType = new ResourceLocation(JsonUtils.getString(json, "type"));
        skill.skillName = new ResourceLocation(JsonUtils.getString(json, "name"));
        JsonArray levels = JsonUtils.getJsonArray(json, "levels", null);
        if (levels != null) {
            skill.skillLevels = new int[levels.size()];
            for (int i = 0; i < skill.skillLevels.length; i++) {
                skill.skillLevels[i] = levels.get(i).getAsInt();
            }
        } else {
            skill.skillLevels = new int[]{-1};
        }
        JsonArray prereq = JsonUtils.getJsonArray(json, "prerequisites", null);
        if (prereq != null) {
            skill.prereqs = new ResourceLocation[prereq.size()];
            if (skill.prereqs.length != 0) {
                for (int i = 0; i < skill.prereqs.length; i++) {
                    skill.prereqs[i] = new ResourceLocation(prereq.get(i).getAsString());
                }
            }
        }
        skill.column = JsonUtils.getInt(json, "column", 0);
        skill.row = JsonUtils.getInt(json, "row", 0);
        skill.enabled = JsonUtils.getBoolean(json, "enabled", true);
        skill.active = JsonUtils.getBoolean(json, "active", true);
        skill.repStack = ShapedRecipes.deserializeItem(JsonUtils.getJsonObject(json, "stack"), false);
        return skill;
    }

    @Override
    public void setLevelCosts(int[] levelCosts) {
        skillLevels = levelCosts;
    }

    @Override
    public int getLevelCost(int currentLevel) {
        if (currentLevel >= 0 && currentLevel < getMaxLevel())
            return skillLevels[currentLevel];
        return -1;
    }

    @Override
    public ItemStack getRepresentativeStack() {
        return repStack != null ? repStack : ItemStack.EMPTY;
    }

    @Override
    public void setRepresentativeStack(ItemStack stack) {
        repStack = stack;
    }

    @Override
    public ResourceLocation getSkillType() {
        return skillType;
    }

    @Override
    public void setSkillType(ResourceLocation type) {
        skillType = type;
    }

    @Override
    public ResourceLocation getSkillName() {
        return skillName;
    }

    @Override
    public void setSkillName(ResourceLocation skillName) {
        this.skillName = skillName;
    }

    @Override
    public void setPrerequisites(ResourceLocation[] prerequisites) {
        prereqs = prerequisites;
    }

    @Override
    public ResourceLocation[] getPrerequisites() {
        return prereqs;
    }

    @Override
    public void setSkillColumn(int column) {
        this.column = column;
    }

    @Override
    public int getSkillColumn() {
        return column;
    }

    @Override
    public void setSkillRow(int row) {
        this.row = row;
    }

    @Override
    public int getSkillRow() {
        return row;
    }

    @Override
    public int getMaxLevel() {
        return skillLevels.length;
    }

    @Override
    public void setEnabled(boolean isEnabled) {
        enabled = isEnabled;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean isMaxLevel(int level) {
        return level == getMaxLevel();
    }
/*
    @Override
    public String getJsonLocation() {
        String category = getSkillType() == 0 ? "mining" : getSkillType() == 1 ? "crafting" : "combat";
        ResourceLocation location = new ResourceLocation(getSkillName());
        return category + "/" + location.getResourcePath();
    }

    @Override
    public boolean hasExternalJson() {
        return true;
    }*/

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void setActive(boolean isActive) {
        active = isActive;
    }
}
