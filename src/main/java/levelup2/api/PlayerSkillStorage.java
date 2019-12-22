package levelup2.api;

import com.google.gson.JsonObject;
import levelup2.skills.SkillRegistry;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;

public class PlayerSkillStorage {
    private IPlayerSkill skill;
    private int level;

    public PlayerSkillStorage(IPlayerSkill skill, int level) {
        this.skill = skill;
        this.level = level;
    }

    private PlayerSkillStorage() {}

    public static PlayerSkillStorage fromJson(JsonObject json) {
        String skill = JsonUtils.getString(json, "name");
        if (!skill.equals("")) {
            IPlayerSkill sk = SkillRegistry.getSkillFromName(new ResourceLocation(skill));
            if (sk != null) {
                return new PlayerSkillStorage(sk, JsonUtils.getInt(json, "level", 1));
            }
        }
        return null;
    }

    public IPlayerSkill getSkill() {
        return skill;
    }

    public int getLevel() {
        return level;
    }
}
