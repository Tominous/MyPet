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

package de.Keyle.MyPet.compat.v1_13_R2.entity.types;

import de.Keyle.MyPet.api.Configuration;
import de.Keyle.MyPet.api.entity.EntitySize;
import de.Keyle.MyPet.api.entity.MyPet;
import de.Keyle.MyPet.api.entity.types.MySheep;
import de.Keyle.MyPet.compat.v1_13_R2.entity.EntityMyPet;
import de.Keyle.MyPet.compat.v1_13_R2.entity.ai.movement.EatGrass;
import net.minecraft.server.v1_13_R2.*;
import org.bukkit.DyeColor;

import java.util.HashMap;
import java.util.Map;

@EntitySize(width = 0.7F, height = 1.2349999f)
public class EntityMySheep extends EntityMyPet {

    private static final DataWatcherObject<Boolean> AGE_WATCHER = DataWatcher.a(EntityMySheep.class, DataWatcherRegistry.i);
    private static final DataWatcherObject<Byte> COLOR_WATCHER = DataWatcher.a(EntityMySheep.class, DataWatcherRegistry.a);

    private static Map<EnumColor, Block> colorMap = new HashMap<>();

    static {
        colorMap.put(EnumColor.WHITE, Blocks.WHITE_WOOL);
        colorMap.put(EnumColor.ORANGE, Blocks.ORANGE_WOOL);
        colorMap.put(EnumColor.MAGENTA, Blocks.MAGENTA_WOOL);
        colorMap.put(EnumColor.LIGHT_BLUE, Blocks.LIGHT_BLUE_WOOL);
        colorMap.put(EnumColor.YELLOW, Blocks.YELLOW_WOOL);
        colorMap.put(EnumColor.LIME, Blocks.LIME_WOOL);
        colorMap.put(EnumColor.PINK, Blocks.PINK_WOOL);
        colorMap.put(EnumColor.GRAY, Blocks.GRAY_WOOL);
        colorMap.put(EnumColor.LIGHT_GRAY, Blocks.LIGHT_GRAY_WOOL);
        colorMap.put(EnumColor.CYAN, Blocks.CYAN_WOOL);
        colorMap.put(EnumColor.PURPLE, Blocks.PURPLE_WOOL);
        colorMap.put(EnumColor.BLUE, Blocks.BLUE_WOOL);
        colorMap.put(EnumColor.BROWN, Blocks.BROWN_WOOL);
        colorMap.put(EnumColor.GREEN, Blocks.GREEN_WOOL);
        colorMap.put(EnumColor.RED, Blocks.RED_WOOL);
        colorMap.put(EnumColor.BLACK, Blocks.BLACK_WOOL);
    }

    public EntityMySheep(World world, MyPet myPet) {
        super(EntityTypes.SHEEP, world, myPet);
    }

    @Override
    protected String getDeathSound() {
        return "entity.sheep.death";
    }

    @Override
    protected String getHurtSound() {
        return "entity.sheep.hurt";
    }

    protected String getLivingSound() {
        return "entity.sheep.ambient";
    }

    public boolean handlePlayerInteraction(EntityHuman entityhuman, EnumHand enumhand, ItemStack itemStack) {
        if (super.handlePlayerInteraction(entityhuman, enumhand, itemStack)) {
            return true;
        }

        if (getOwner().equals(entityhuman) && itemStack != null && canUseItem()) {
            if (itemStack.getItem() instanceof ItemDye && ((ItemDye) itemStack.getItem()).d().ordinal() != getMyPet().getColor().ordinal() && !getMyPet().isSheared()) {
                getMyPet().setColor(DyeColor.values()[((ItemDye) itemStack.getItem()).d().ordinal()]);
                if (itemStack != ItemStack.a && !entityhuman.abilities.canInstantlyBuild) {
                    itemStack.subtract(1);
                    if (itemStack.getCount() <= 0) {
                        entityhuman.inventory.setItem(entityhuman.inventory.itemInHandIndex, ItemStack.a);
                    }
                }
                return true;
            } else if (itemStack.getItem() == Items.SHEARS && Configuration.MyPet.Sheep.CAN_BE_SHEARED && !getMyPet().isSheared()) {
                getMyPet().setSheared(true);
                int woolDropCount = 1 + this.random.nextInt(3);

                for (int j = 0; j < woolDropCount; ++j) {
                    EntityItem entityitem = new EntityItem(this.world, this.locX, this.locY + 1, this.locZ, new ItemStack(colorMap.get(EnumColor.values()[getMyPet().getColor().ordinal()])));
                    entityitem.pickupDelay = 10;
                    entityitem.motY += (double) (this.random.nextFloat() * 0.05F);
                    this.world.addEntity(entityitem);
                }
                makeSound("entity.sheep.shear", 1.0F, 1.0F);
                if (itemStack != ItemStack.a && !entityhuman.abilities.canInstantlyBuild) {
                    itemStack.damage(1, entityhuman);
                }
                return true;
            } else if (Configuration.MyPet.Sheep.GROW_UP_ITEM.compare(itemStack) && getMyPet().isBaby() && getOwner().getPlayer().isSneaking()) {
                if (itemStack != ItemStack.a && !entityhuman.abilities.canInstantlyBuild) {
                    itemStack.subtract(1);
                    if (itemStack.getCount() <= 0) {
                        entityhuman.inventory.setItem(entityhuman.inventory.itemInHandIndex, ItemStack.a);
                    }
                }
                getMyPet().setBaby(false);
                return true;
            }
        }
        return false;
    }

    protected void initDatawatcher() {
        super.initDatawatcher();
        this.datawatcher.register(AGE_WATCHER, false);
        this.datawatcher.register(COLOR_WATCHER, (byte) 0); // color/sheared
    }

    @Override
    public void updateVisuals() {
        this.datawatcher.set(AGE_WATCHER, getMyPet().isBaby());

        byte data = (byte) (getMyPet().isSheared() ? 16 : 0);
        this.datawatcher.set(COLOR_WATCHER, (byte) (data & 0xF0 | getMyPet().getColor().ordinal() & 0xF));
    }

    public void playPetStepSound() {
        makeSound("entity.sheep.step", 0.15F, 1.0F);
    }

    public MySheep getMyPet() {
        return (MySheep) myPet;
    }

    public void setPathfinder() {
        super.setPathfinder();
        petPathfinderSelector.addGoal("EatGrass", new EatGrass(this));
    }
}