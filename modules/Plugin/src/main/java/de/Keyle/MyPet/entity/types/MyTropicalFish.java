/*
 * This file is part of MyPet
 *
 * Copyright © 2011-2018 Keyle
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

package de.Keyle.MyPet.entity.types;

import de.Keyle.MyPet.api.Util;
import de.Keyle.MyPet.api.entity.MyPetType;
import de.Keyle.MyPet.api.player.MyPetPlayer;
import de.Keyle.MyPet.entity.MyPet;
import de.keyle.knbt.TagCompound;
import de.keyle.knbt.TagInt;
import org.bukkit.ChatColor;

public class MyTropicalFish extends MyPet implements de.Keyle.MyPet.api.entity.types.MyTropicalFish {

    protected int variant = 0;

    public MyTropicalFish(MyPetPlayer petOwner) {
        super(petOwner);
    }

    @Override
    public MyPetType getPetType() {
        return MyPetType.TropicalFish;
    }

    public int getVariant() {
        return variant;
    }

    public void setVariant(int variant) {
        this.variant = Util.clamp(variant, 0, 235799809);
        if (status == PetState.Here) {
            getEntity().get().getHandle().updateVisuals();
        }
    }

    @Override
    public TagCompound writeExtendedInfo() {
        TagCompound info = super.writeExtendedInfo();
        info.getCompoundData().put("Variant", new TagInt(getVariant()));
        return info;
    }

    @Override
    public void readExtendedInfo(TagCompound info) {
        if (info.getCompoundData().containsKey("Variant")) {
            setVariant(info.getAs("Variant", TagInt.class).getIntData());
        }
    }

    public static int generateVariant(int shape, int pattern, int baseColor, int patternColor) {
        return shape & 255 | (pattern & 255) << 8 | (baseColor & 255) << 16 | (patternColor & 255) << 24;
    }

    @Override
    public String toString() {
        return "MyTropicalFish{owner=" + getOwner().getName() + ", name=" + ChatColor.stripColor(petName) + ", exp=" + experience.getExp() + "/" + experience.getRequiredExp() + ", lv=" + experience.getLevel() + ", status=" + status.name() + ", skilltree=" + (skilltree != null ? skilltree.getName() : "-") + ", worldgroup=" + worldGroup + ", variant=" + variant + "}";
    }
}