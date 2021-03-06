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

package de.Keyle.MyPet.compat.v1_12_R1.skill.skills.ranged.nms;

import de.Keyle.MyPet.api.entity.skill.ranged.EntityMyPetProjectile;
import de.Keyle.MyPet.api.util.Compat;
import de.Keyle.MyPet.compat.v1_12_R1.entity.EntityMyPet;
import de.Keyle.MyPet.compat.v1_12_R1.skill.skills.ranged.bukkit.CraftMyPetLlamaSpit;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;

@Compat("v1_12_R1")
public class MyPetLlamaSpit extends EntityLlamaSpit implements EntityMyPetProjectile {

    @Setter @Getter protected float damage = 0;

    public MyPetLlamaSpit(World world, EntityMyPet entityMyPet) {
        super(world);
        this.shooter = entityMyPet;
        this.setPosition(entityMyPet.locX - (double) (entityMyPet.width + 1.0F) * 0.5D * (double) MathHelper.sin(entityMyPet.aN * 0.017453292F),
                entityMyPet.locY + (double) entityMyPet.getHeadHeight() - 0.10000000149011612D,
                entityMyPet.locZ + (double) (entityMyPet.width + 1.0F) * 0.5D * (double) MathHelper.cos(entityMyPet.aN * 0.017453292F));
        this.setSize(0.25F, 0.25F);
    }

    @Override
    public EntityMyPet getShooter() {
        return (EntityMyPet) this.shooter;
    }

    @Override
    public CraftEntity getBukkitEntity() {
        if (this.bukkitEntity == null) {
            this.bukkitEntity = new CraftMyPetLlamaSpit(this.world.getServer(), this);
        }
        return this.bukkitEntity;
    }

    @Override
    public void a(NBTTagCompound nbtTagCompound) {
    }

    @Override
    public void b(NBTTagCompound nbtTagCompound) {
    }

    public boolean damageEntity(DamageSource damagesource, float f) {
        return false;
    }

    public void a(MovingObjectPosition movingobjectposition) {
        if (movingobjectposition.entity != null && this.shooter != null) {
            movingobjectposition.entity.damageEntity(DamageSource.a(this, this.shooter).b(), getDamage());
        }
        this.die();
    }
}