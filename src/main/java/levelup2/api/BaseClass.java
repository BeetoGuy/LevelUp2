package levelup2.api;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import levelup2.skills.SkillRegistry;
import levelup2.util.ClassProperties;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.List;

public class BaseClass implements ICharacterClass {
    private ResourceLocation className;
    private IPlayerSkill bonusSkill;
    private List<PlayerSkillStorage> applicableSkills;

    public BaseClass(ResourceLocation location, IPlayerSkill spec, List<PlayerSkillStorage> skillBonuses) {
        className = location;
        bonusSkill = spec;
        applicableSkills = skillBonuses;
    }

    public static BaseClass fromProperties(ClassProperties props) {
        ResourceLocation location = props.getClassName();
        IPlayerSkill skill = SkillRegistry.getSkillFromName(props.getSpecSkill());
        List<PlayerSkillStorage> skills = props.getBonusSkills();
        return new BaseClass(location, skill, skills);
    }

    public BaseClass fromJson(JsonObject json) {
        ResourceLocation location = new ResourceLocation(JsonUtils.getString(json, "name"));
        IPlayerSkill bonus = SkillRegistry.getSkillFromName(new ResourceLocation(JsonUtils.getString(json, "bonus")));
        List<PlayerSkillStorage> skillBonuses = Lists.newArrayList();
        for (JsonElement obj : JsonUtils.getJsonArray(json, "skills")) {
            skillBonuses.add(PlayerSkillStorage.fromJson(obj.getAsJsonObject()));
        }
        return new BaseClass(location, bonus, skillBonuses);
    }

    @Override
    @Nonnull
    public ResourceLocation getClassName() {
        return className;
    }

    @Override
    @Nonnull
    public IPlayerSkill getSpecializationSkill() {
        return bonusSkill;
    }

    @Override
    public List<PlayerSkillStorage> getSkillBonuses() {
        return applicableSkills;
    }
}
