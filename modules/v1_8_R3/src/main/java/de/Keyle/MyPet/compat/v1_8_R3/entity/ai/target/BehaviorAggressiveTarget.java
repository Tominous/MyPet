/*
 * This file is part of mypet-v1_8_R3
 *
 * Copyright (C) 2011-2016 Keyle
 * mypet-v1_8_R3 is licensed under the GNU Lesser General Public License.
 *
 * mypet-v1_8_R3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * mypet-v1_8_R3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package de.Keyle.MyPet.compat.v1_8_R3.entity.ai.target;

import de.Keyle.MyPet.MyPetApi;
import de.Keyle.MyPet.api.entity.ActiveMyPet;
import de.Keyle.MyPet.api.entity.ai.AIGoal;
import de.Keyle.MyPet.api.skill.skills.BehaviorInfo.BehaviorState;
import de.Keyle.MyPet.compat.v1_8_R3.entity.EntityMyPet;
import de.Keyle.MyPet.skill.skills.Behavior;
import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.EntityLiving;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.EntityTameableAnimal;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityTargetEvent;

public class BehaviorAggressiveTarget extends AIGoal {
    private ActiveMyPet myPet;
    private EntityMyPet petEntity;
    private EntityPlayer petOwnerEntity;
    private EntityLiving target;
    private float range;
    private Behavior behaviorSkill = null;

    public BehaviorAggressiveTarget(EntityMyPet petEntity, float range) {
        this.petEntity = petEntity;
        this.myPet = petEntity.getMyPet();
        this.petOwnerEntity = ((CraftPlayer) myPet.getOwner().getPlayer()).getHandle();
        this.range = range;
        if (myPet.getSkills().hasSkill(Behavior.class)) {
            behaviorSkill = myPet.getSkills().getSkill(Behavior.class);
        }
    }

    @Override
    public boolean shouldStart() {
        if (behaviorSkill == null || !behaviorSkill.isActive() || behaviorSkill.getBehavior() != BehaviorState.Aggressive) {
            return false;
        }
        if (myPet.getDamage() <= 0 && myPet.getRangedDamage() <= 0) {
            return false;
        }
        if (!myPet.getEntity().canMove()) {
            return false;
        }
        if (petEntity.getGoalTarget() != null && petEntity.getGoalTarget().isAlive()) {
            return false;
        }

        for (Object entityObj : this.petEntity.world.a(EntityLiving.class, this.petOwnerEntity.getBoundingBox().grow((double) range, (double) range, (double) range))) {
            EntityLiving entityLiving = (EntityLiving) entityObj;

            if (entityLiving != petEntity && !(entityLiving instanceof EntityArmorStand) && entityLiving.isAlive() && petEntity.h(entityLiving) <= 91) {
                if (entityLiving instanceof EntityPlayer) {
                    Player targetPlayer = (Player) entityLiving.getBukkitEntity();
                    if (myPet.getOwner().equals(targetPlayer)) {
                        continue;
                    }
                    if (!MyPetApi.getHookManager().canHurt(myPet.getOwner().getPlayer(), targetPlayer, true)) {
                        continue;
                    }
                } else if (entityLiving instanceof EntityMyPet) {
                    ActiveMyPet targetMyPet = ((EntityMyPet) entityLiving).getMyPet();
                    if (!MyPetApi.getHookManager().canHurt(myPet.getOwner().getPlayer(), targetMyPet.getOwner().getPlayer(), true)) {
                        continue;
                    }
                } else if (entityLiving instanceof EntityTameableAnimal) {
                    EntityTameableAnimal tameable = (EntityTameableAnimal) entityLiving;
                    if (tameable.isTamed() && tameable.getOwner() != null) {
                        Player tameableOwner = (Player) tameable.getOwner().getBukkitEntity();
                        if (myPet.getOwner().equals(tameableOwner)) {
                            continue;
                        } else if (!MyPetApi.getHookManager().canHurt(myPet.getOwner().getPlayer(), tameableOwner, true)) {
                            continue;
                        }
                    }
                }
                if (!MyPetApi.getHookManager().canHurt(myPet.getOwner().getPlayer(), entityLiving.getBukkitEntity())) {
                    continue;
                }
                this.target = entityLiving;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean shouldFinish() {
        if (!petEntity.canMove()) {
            return true;
        } else if (petEntity.getGoalTarget() == null) {
            return true;
        } else if (!petEntity.getGoalTarget().isAlive()) {
            return true;
        } else if (behaviorSkill.getBehavior() != BehaviorState.Aggressive) {
            return true;
        } else if (myPet.getDamage() <= 0 && myPet.getRangedDamage() <= 0) {
            return true;
        } else if (petEntity.getGoalTarget().world != petEntity.world) {
            return true;
        } else if (petEntity.h(petEntity.getGoalTarget()) > 400) {
            return true;
        } else if (petEntity.h(((CraftPlayer) petEntity.getOwner().getPlayer()).getHandle()) > 600) {
            return true;
        }
        return false;
    }

    @Override
    public void start() {
        petEntity.setGoalTarget(this.target, EntityTargetEvent.TargetReason.RANDOM_TARGET, false);
    }

    @Override
    public void finish() {
        petEntity.setGoalTarget(null, EntityTargetEvent.TargetReason.FORGOT_TARGET, false);
        target = null;
    }
}