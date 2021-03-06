/*
 * This file is part of MyPet
 *
 * Copyright © 2011-2019 Keyle
 * MyPet is licensed under the GNU Lesser General Public License.
 *
 * MyPet is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MyPet is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package de.Keyle.MyPet.api.skill.skilltree;

import de.Keyle.MyPet.MyPetApi;
import de.Keyle.MyPet.api.Util;
import de.Keyle.MyPet.api.entity.MyPetType;
import de.Keyle.MyPet.api.skill.Upgrade;
import de.Keyle.MyPet.api.skill.modifier.UpgradeBooleanModifier;
import de.Keyle.MyPet.api.skill.modifier.UpgradeEnumModifier;
import de.Keyle.MyPet.api.skill.modifier.UpgradeIntegerModifier;
import de.Keyle.MyPet.api.skill.modifier.UpgradeNumberModifier;
import de.Keyle.MyPet.api.skill.skills.Ranged;
import de.Keyle.MyPet.api.skill.skilltree.levelrule.DynamicLevelRule;
import de.Keyle.MyPet.api.skill.skilltree.levelrule.LevelRule;
import de.Keyle.MyPet.api.skill.skilltree.levelrule.StaticLevelRule;
import de.Keyle.MyPet.api.skill.upgrades.*;
import de.Keyle.MyPet.api.util.configuration.settings.Settings;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SkillTreeLoaderJSON {

    final static Pattern LEVEL_RULE_REGEX = Pattern.compile("(?:%(\\d+))|(?:<(\\d+))|(?:>(\\d+))");

    public static void loadSkilltrees(File skilltreePath) {
        File[] skilltreeFiles = skilltreePath.listFiles(pathname -> pathname.getAbsolutePath().endsWith(".st.json"));
        if (skilltreeFiles != null) {
            for (File skilltreeFile : skilltreeFiles) {
                loadSkilltree(skilltreeFile);
            }
        }
    }

    public static void loadSkilltree(File skilltreeFile) {
        if (skilltreeFile.exists()) {
            try {
                loadSkilltree(loadJsonObject(skilltreeFile));
            } catch (Exception e) {
                MyPetApi.getLogger().warning("Error in " + skilltreeFile.getName() + " -> Skilltree not loaded.");
                MyPetApi.getLogger().warning(e.getMessage());
            }
        }
    }

    public static void loadSkilltree(JSONObject skilltreeObject) {
        if (!containsKey(skilltreeObject, "ID")) {
            return;
        }

        Skilltree skilltree;
        String skilltreeID = get(skilltreeObject, "ID").toString();

        if (MyPetApi.getSkilltreeManager().hasSkilltree(skilltreeID)) {
            return;
        }

        skilltree = new Skilltree(skilltreeID);

        if (containsKey(skilltreeObject, "Name")) {
            skilltree.setDisplayName(get(skilltreeObject, "Name").toString());
        }
        if (containsKey(skilltreeObject, "Permission")) {
            String permission = get(skilltreeObject, "Permission").toString();
            Settings settings = new Settings("Permission");
            settings.load(permission);
            skilltree.addRequirementSettings(settings);
            //TODO warnung zum aktualisieren
        }
        if (containsKey(skilltreeObject, "Display")) {
            skilltree.setDisplayName(get(skilltreeObject, "Display").toString());
        }
        if (containsKey(skilltreeObject, "MaxLevel")) {
            skilltree.setMaxLevel(((Number) get(skilltreeObject, "MaxLevel")).intValue());
        }
        if (containsKey(skilltreeObject, "RequiredLevel")) {
            skilltree.setRequiredLevel(((Number) get(skilltreeObject, "RequiredLevel")).intValue());
        }
        if (containsKey(skilltreeObject, "Order")) {
            skilltree.setOrder(((Number) get(skilltreeObject, "Order")).intValue());
        }
        if (containsKey(skilltreeObject, "Weight")) {
            skilltree.setWeight(((Number) get(skilltreeObject, "Weight")).doubleValue());
        }
        if (containsKey(skilltreeObject, "MobTypes")) {
            JSONArray mobTypeArray = (JSONArray) get(skilltreeObject, "MobTypes");
            Set<MyPetType> mobTypes = new HashSet<>();
            if (mobTypeArray.size() == 0) {
                Collections.addAll(mobTypes, MyPetType.values());
            } else {
                boolean allNegative = true;
                for (Object o : mobTypeArray) {
                    String type = o.toString();
                    if (!type.startsWith("-")) {
                        allNegative = false;
                        break;
                    }
                }
                if (allNegative) {
                    Collections.addAll(mobTypes, MyPetType.values());
                }
                for (Object o : mobTypeArray) {
                    String type = o.toString();
                    if (type.equals("*")) {
                        Collections.addAll(mobTypes, MyPetType.values());
                    } else {
                        boolean negative = false;
                        if (type.startsWith("-")) {
                            type = type.substring(1);
                            negative = true;
                        }
                        MyPetType mobType = MyPetType.byName(type);
                        if (mobType != null) {
                            if (negative) {
                                mobTypes.remove(mobType);
                            } else {
                                mobTypes.add(mobType);
                            }
                        }
                    }
                }
            }
            skilltree.setMobTypes(mobTypes);
        }
        if (containsKey(skilltreeObject, "Icon")) {
            JSONObject iconObject = (JSONObject) get(skilltreeObject, "Icon");
            SkilltreeIcon icon = new SkilltreeIcon();
            if (containsKey(iconObject, "Material")) {
                icon.setMaterial(get(iconObject, "Material").toString());
            }
            if (containsKey(iconObject, "Glowing")) {
                icon.setGlowing((Boolean) get(iconObject, "Glowing"));
            }
            skilltree.setIcon(icon);
        }
        if (containsKey(skilltreeObject, "Inheritance")) {
            JSONObject inheritanceObject = (JSONObject) get(skilltreeObject, "Inheritance");
            if (containsKey(inheritanceObject, "Skilltree")) {
                skilltree.setInheritedSkilltreeName(get(inheritanceObject, "Skilltree").toString());
            }
        }
        if (containsKey(skilltreeObject, "Description")) {
            JSONArray descriptionArray = (JSONArray) get(skilltreeObject, "Description");
            for (Object lvl_object : descriptionArray) {
                skilltree.addDescriptionLine(String.valueOf(lvl_object));
            }
        }
        if (containsKey(skilltreeObject, "Notifications")) {
            JSONObject notificationsObject = (JSONObject) get(skilltreeObject, "Notifications");
            for (Object ooo : notificationsObject.keySet()) {
                String levelRuleString = ooo.toString();
                LevelRule levelRule = loadLevelRule(levelRuleString);
                String message = notificationsObject.get(ooo).toString();
                skilltree.addNotification(levelRule, message);
            }
        }
        if (containsKey(skilltreeObject, "Requirements")) {
            JSONArray requirementsArray = (JSONArray) get(skilltreeObject, "Requirements");
            for (Object ooo : requirementsArray) {
                boolean hasParameter = ooo.toString().contains(":");
                String[] data = ooo.toString().split(":", 2);
                Settings settings = new Settings(data[0]);
                if (hasParameter) {
                    settings.load(data[1]);
                }
                skilltree.addRequirementSettings(settings);
            }
        }
        if (containsKey(skilltreeObject, "Skills")) {
            JSONObject skillsObject = (JSONObject) get(skilltreeObject, "Skills");
            for (Object oo : skillsObject.keySet()) {
                JSONObject skillObject = (JSONObject) skillsObject.get(oo);
                String skillName = oo.toString();

                if (containsKey(skillObject, "Upgrades")) {
                    JSONObject upgradesObject = (JSONObject) get(skillObject, "Upgrades");

                    for (Object ooo : upgradesObject.keySet()) {
                        String levelRuleString = ooo.toString();
                        LevelRule levelRule = loadLevelRule(levelRuleString);

                        JSONObject upgradeObject = (JSONObject) upgradesObject.get(ooo);
                        Upgrade upgrade = loadUpgrade(skillName, upgradeObject);

                        skilltree.addUpgrade(levelRule, upgrade);
                    }
                }
            }
        }

        MyPetApi.getSkilltreeManager().registerSkilltree(skilltree);
    }

    private static LevelRule loadLevelRule(String levelRuleString) {
        LevelRule levelRule;
        if (levelRuleString.contains("%")) {
            int modulo = 1;
            int min = 0;
            int max = 0;
            Matcher matcher = LEVEL_RULE_REGEX.matcher(levelRuleString);
            while (matcher.find()) {
                if (matcher.group(0).startsWith("%")) {
                    modulo = Math.max(1, Integer.parseInt(matcher.group(1)));
                } else if (matcher.group(0).startsWith(">")) {
                    min = Integer.parseInt(matcher.group(3));
                } else if (matcher.group(0).startsWith("<")) {
                    max = Integer.parseInt(matcher.group(2));
                }
            }
            levelRule = new DynamicLevelRule(modulo, min, max);
        } else {
            String[] levelStrings = levelRuleString.split(",");
            List<Integer> levels = new ArrayList<>();
            for (String levelString : levelStrings) {
                if (Util.isInt(levelString.trim())) {
                    levels.add(Integer.parseInt(levelString.trim()));
                }
            }
            levelRule = new StaticLevelRule(levels);
        }
        return levelRule;
    }

    private static Upgrade loadUpgrade(String skillName, JSONObject upgradeObject) {
        Upgrade upgrade = null;
        switch (skillName.toLowerCase()) {
            case "backpack": {

                upgrade = new BackpackUpgrade()
                        .setRowsModifier(parseNumberModifier(get(upgradeObject, "rows")))
                        .setDropOnDeathModifier(parseBooleanModifier(get(upgradeObject, "drop")));
                break;
            }
            case "beacon": {
                JSONObject buffsObject = (JSONObject) get(upgradeObject, "buffs");
                upgrade = new BeaconUpgrade()
                        .setRangeModifier(parseNumberModifier(get(upgradeObject, "range")))
                        .setDurationModifier(parseIntegerModifier(get(upgradeObject, "duration")))
                        .setNumberOfBuffsModifier(parseIntegerModifier(get(upgradeObject, "count")))
                        .setAbsorptionModifier(parseIntegerModifier(get(buffsObject, "absorption")))
                        .setFireResistanceModifier(parseBooleanModifier(get(buffsObject, "fireresistance")))
                        .setHasteModifier(parseIntegerModifier(get(buffsObject, "haste")))
                        .setLuckModifier(parseBooleanModifier(get(buffsObject, "luck")))
                        .setNightVisionModifier(parseBooleanModifier(get(buffsObject, "nightvision")))
                        .setResistanceModifier(parseIntegerModifier(get(buffsObject, "resistance")))
                        .setSpeedModifier(parseIntegerModifier(get(buffsObject, "speed")))
                        .setStrengthModifier(parseIntegerModifier(get(buffsObject, "strength")))
                        .setWaterBreathingModifier(parseBooleanModifier(get(buffsObject, "waterbreathing")))
                        .setRegenerationModifier(parseIntegerModifier(get(buffsObject, "regeneration")))
                        .setInvisibilityModifier(parseBooleanModifier(get(buffsObject, "invisibility")))
                        .setJumpBoostModifier(parseIntegerModifier(get(buffsObject, "jumpboost")));
                break;
            }
            case "behavior": {
                upgrade = new BehaviorUpgrade()
                        .setAggroModifier(parseBooleanModifier(get(upgradeObject, "aggro")))
                        .setDuelModifier(parseBooleanModifier(get(upgradeObject, "duel")))
                        .setFarmModifier(parseBooleanModifier(get(upgradeObject, "farm")))
                        .setFriendlyModifier(parseBooleanModifier(get(upgradeObject, "friend")))
                        .setRaidModifier(parseBooleanModifier(get(upgradeObject, "raid")));
                break;
            }
            case "control": {
                upgrade = new ControlUpgrade()
                        .setActiveModifier(parseBooleanModifier(get(upgradeObject, "active")));
                break;
            }
            case "damage": {
                upgrade = new DamageUpgrade()
                        .setDamageModifier(parseNumberModifier(get(upgradeObject, "damage")));
                break;
            }
            case "fire": {
                upgrade = new FireUpgrade()
                        .setChanceModifier(parseIntegerModifier(get(upgradeObject, "chance")))
                        .setDurationModifier(parseIntegerModifier(get(upgradeObject, "duration")));
                break;
            }
            case "heal": {
                upgrade = new HealUpgrade()
                        .setHealModifier(parseNumberModifier(get(upgradeObject, "health")))
                        .setTimerModifier(parseIntegerModifier(get(upgradeObject, "timer")));
                break;
            }
            case "knockback": {
                upgrade = new KnockbackUpgrade()
                        .setChanceModifier(parseIntegerModifier(get(upgradeObject, "chance")));
                break;
            }
            case "life": {
                upgrade = new LifeUpgrade()
                        .setLifeModifier(parseNumberModifier(get(upgradeObject, "health")));
                break;
            }
            case "lightning": {
                upgrade = new LightningUpgrade()
                        .setChanceModifier(parseIntegerModifier(get(upgradeObject, "chance")))
                        .setDamageModifier(parseNumberModifier(get(upgradeObject, "damage")));
                break;
            }
            case "pickup": {
                upgrade = new PickupUpgrade()
                        .setRangeModifier(parseNumberModifier(get(upgradeObject, "range")))
                        .setPickupExpModifier(parseBooleanModifier(get(upgradeObject, "exp")));
                break;
            }
            case "poison": {
                upgrade = new PoisonUpgrade()
                        .setChanceModifier(parseIntegerModifier(get(upgradeObject, "chance")))
                        .setDurationModifier(parseIntegerModifier(get(upgradeObject, "duration")));
                break;
            }
            case "ranged": {
                upgrade = new RangedUpgrade()
                        .setDamageModifier(parseNumberModifier(get(upgradeObject, "damage")))
                        .setRateOfFireModifier(parseIntegerModifier(get(upgradeObject, "rate")))
                        .setProjectileModifier(parseEnumModifier(get(upgradeObject, "projectile"), Ranged.Projectile.class, Ranged.Projectile.Arrow));
                break;
            }
            case "ride": {
                upgrade = new RideUpgrade()
                        .setActiveModifier(parseBooleanModifier(get(upgradeObject, "active")))
                        .setSpeedIncreaseModifier(parseIntegerModifier(get(upgradeObject, "speed")))
                        .setJumpHeightModifier(parseNumberModifier(get(upgradeObject, "jumpheight")))
                        .setFlyLimitModifier(parseNumberModifier(get(upgradeObject, "flylimit")))
                        .setFlyRegenRateModifier(parseNumberModifier(get(upgradeObject, "flyregenrate")))
                        .setCanFlyModifier(parseBooleanModifier(get(upgradeObject, "canfly")));
                break;
            }
            case "shield": {
                upgrade = new ShieldUpgrade()
                        .setChanceModifier(parseIntegerModifier(get(upgradeObject, "chance")))
                        .setRedirectedDamageModifier(parseIntegerModifier(get(upgradeObject, "redirect")));
                break;
            }
            case "slow": {
                upgrade = new SlowUpgrade()
                        .setChanceModifier(parseIntegerModifier(get(upgradeObject, "chance")))
                        .setDurationModifier(parseIntegerModifier(get(upgradeObject, "duration")));
                break;
            }
            case "sprint": {
                upgrade = new SprintUpgrade()
                        .setActiveModifier(parseBooleanModifier(get(upgradeObject, "active")));
                break;
            }
            case "stomp": {
                upgrade = new StompUpgrade()
                        .setChanceModifier(parseIntegerModifier(get(upgradeObject, "chance")))
                        .setDamageModifier(parseNumberModifier(get(upgradeObject, "damage")));
                break;
            }
            case "thorns": {
                upgrade = new ThornsUpgrade()
                        .setChanceModifier(parseIntegerModifier(get(upgradeObject, "chance")))
                        .setReflectedDamageModifier(parseIntegerModifier(get(upgradeObject, "reflection")));
                break;
            }
            case "wither": {
                upgrade = new WitherUpgrade()
                        .setChanceModifier(parseIntegerModifier(get(upgradeObject, "chance")))
                        .setDurationModifier(parseIntegerModifier(get(upgradeObject, "duration")));
                break;
            }
        }
        return upgrade;
    }

    private static JSONObject loadJsonObject(File jsonFile) throws IOException, ParseException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(jsonFile), StandardCharsets.UTF_8))) {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(reader);
            if (obj instanceof JSONObject) {
                return (JSONObject) obj;
            }
        }
        return null;
    }

    private static Object get(JSONObject o, String key) {
        if (o != null) {
            for (Object keyObject : o.keySet()) {
                if (keyObject.toString().equalsIgnoreCase(key)) {
                    return o.get(keyObject);
                }
            }
        }
        return null;
    }

    private static boolean containsKey(JSONObject o, String key) {
        for (Object keyObject : o.keySet()) {
            if (keyObject.toString().equalsIgnoreCase(key)) {
                return true;
            }
        }
        return false;
    }

    private static UpgradeNumberModifier parseNumberModifier(Object modifierObject) {
        if (modifierObject instanceof String) {
            String modifierString = modifierObject.toString();
            UpgradeNumberModifier.Type type;
            if (modifierString.startsWith("+")) {
                type = UpgradeNumberModifier.Type.Add;
            } else if (modifierString.startsWith("-")) {
                type = UpgradeNumberModifier.Type.Subtract;
            } else {
                return null;
            }
            BigDecimal value = new BigDecimal(modifierString.substring(1));
            return new UpgradeNumberModifier(value, type);
        }
        return null;
    }

    private static UpgradeIntegerModifier parseIntegerModifier(Object modifierObject) {
        if (modifierObject instanceof String) {
            String modifierString = modifierObject.toString();
            UpgradeNumberModifier.Type type;
            if (modifierString.startsWith("+")) {
                type = UpgradeNumberModifier.Type.Add;
            } else if (modifierString.startsWith("-")) {
                type = UpgradeNumberModifier.Type.Subtract;
            } else {
                return null;
            }
            BigDecimal value = new BigDecimal(modifierString.substring(1));
            return new UpgradeIntegerModifier(value.intValue(), type);
        }
        return null;
    }

    private static UpgradeBooleanModifier parseBooleanModifier(Object modifierObject) {
        if (modifierObject instanceof Boolean) {
            if ((Boolean) modifierObject) {
                return UpgradeBooleanModifier.True;
            } else {
                return UpgradeBooleanModifier.False;
            }
        }
        return null;
    }

    private static <T extends Enum> UpgradeEnumModifier<T> parseEnumModifier(Object modifierObject, Class<T> e, T def) {
        if (modifierObject instanceof String) {
            String modifierString = modifierObject.toString();
            for (T c : e.getEnumConstants()) {
                if (c.name().equalsIgnoreCase(modifierString)) {
                    return new UpgradeEnumModifier<>(c);
                }
            }
        }
        return new UpgradeEnumModifier<>(def);
    }
}