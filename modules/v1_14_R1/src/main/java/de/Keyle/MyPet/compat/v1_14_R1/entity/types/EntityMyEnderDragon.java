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

package de.Keyle.MyPet.compat.v1_14_R1.entity.types;

import de.Keyle.MyPet.api.Configuration;
import de.Keyle.MyPet.api.entity.EntitySize;
import de.Keyle.MyPet.api.entity.MyPet;
import de.Keyle.MyPet.compat.v1_14_R1.entity.EntityMyPet;
import de.Keyle.MyPet.compat.v1_14_R1.entity.EntityMyPetPart;
import de.Keyle.MyPet.compat.v1_14_R1.entity.ai.attack.MeleeAttack;
import net.minecraft.server.v1_14_R1.Entity;
import net.minecraft.server.v1_14_R1.World;

@EntitySize(width = 4.F, height = 4.F)
public class EntityMyEnderDragon extends EntityMyPet {

    public EntityMyPetPart[] children;

    public EntityMyEnderDragon(World world, MyPet myPet) {
        super(world, myPet);

        children = new EntityMyPetPart[]{
                new EntityMyPetPart(this, "head", 1.0F, 1.0F),
                new EntityMyPetPart(this, "neck", 3.0F, 3.0F),
                new EntityMyPetPart(this, "body", 5.0F, 3.0F),
                new EntityMyPetPart(this, "tail", 2.0F, 2.0F),
                new EntityMyPetPart(this, "tail", 2.0F, 2.0F),
                new EntityMyPetPart(this, "tail", 2.0F, 2.0F),
                new EntityMyPetPart(this, "wing", 4.0F, 2.0F),
                new EntityMyPetPart(this, "wing", 4.0F, 2.0F),
        };
    }

    @Override
    protected String getDeathSound() {
        return "entity.ender_dragon.death";
    }

    @Override
    protected String getHurtSound() {
        return "entity.ender_dragon.hurt";
    }

    protected String getLivingSound() {
        return "entity.ender_dragon.ambient";
    }

    public void setPathfinder() {
        super.setPathfinder();
        petPathfinderSelector.replaceGoal("MeleeAttack", new MeleeAttack(this, 0.1F, 8.5, 20));
    }

    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (Configuration.MyPet.EnderDragon.CAN_GLIDE) {
            if (!this.onGround && this.getMot().y < 0.0D) {
                this.setMot(getMot().d(1, 0.6D, 1));
            }
        }
    }


    /**
     * -> disable falldamage
     */
    public void b(float f, float f1) {
        if (!Configuration.MyPet.EnderDragon.CAN_GLIDE) {
            super.b(f, f1);
        }
    }

    public Entity[] bi() {
        return this.children;
    }
}