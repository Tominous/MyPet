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

package de.Keyle.MyPet.entity.leashing;

import de.Keyle.MyPet.api.Util;
import de.Keyle.MyPet.api.entity.leashing.LeashFlag;
import de.Keyle.MyPet.api.entity.leashing.LeashFlagName;
import de.Keyle.MyPet.api.util.configuration.settings.Setting;
import de.Keyle.MyPet.api.util.configuration.settings.Settings;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

@LeashFlagName("BelowHP")
public class BelowHpFlag implements LeashFlag {
    @Override
    public boolean check(Player player, LivingEntity entity, double damage, Settings settings) {
        for (Setting setting : settings.all()) {
            boolean isPercent = setting.getValue().endsWith("%");
            String valueString = setting.getValue();
            if (isPercent) {
                valueString = valueString.substring(0, valueString.length() - 1);
            }

            if (isPercent) {
                if (Util.isInt(valueString)) {
                    int percent = Integer.parseInt(valueString);
                    return (entity.getHealth() - damage) * 100 / entity.getMaxHealth() <= percent;
                }
            } else {
                if (Util.isDouble(valueString)) {
                    int below = Integer.parseInt(valueString);
                    return entity.getHealth() - damage <= below;
                }
            }

        }
        return true;
    }
}
