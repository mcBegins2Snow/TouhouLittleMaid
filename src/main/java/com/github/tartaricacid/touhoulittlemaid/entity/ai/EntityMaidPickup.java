package com.github.tartaricacid.touhoulittlemaid.entity.ai;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;

import java.util.List;

public class EntityMaidPickup extends EntityAIBase {
    private EntityMaid entityMaid;
    private float speed;
    private Entity entityPickup;
    private int countTime;
    private List<Entity> list;

    public EntityMaidPickup(EntityMaid entityMaid, float speed) {
        this.entityMaid = entityMaid;
        this.speed = speed;
        this.countTime = 10;
        setMutexBits(1 | 2);
    }

    @Override
    public boolean shouldExecute() {
        // 模式判定，如果模式不对，不进行拾取
        if (!entityMaid.isPickup() || entityMaid.isSitting()) {
            return false;
        }

        // 计时判定，时间不到不进行拾取
        if (countTime > 0) {
            countTime--;
            return false;
        }

        countTime = 10;

        // 获取初始拾取列表
        list = this.entityMaid.world.getEntitiesInAABBexcluding(entityMaid,
                this.entityMaid.getEntityBoundingBox().expand(8, 2, 8).expand(-8, -2, -8),
                EntityMaid.IS_PICKUP);

        // 列表不为空，就执行
        return !list.isEmpty();
    }

    @Override
    public void updateTask() {
        // 检查拾取的对象
        if (entityPickup != null && entityPickup.isEntityAlive()) {
            entityMaid.getLookHelper().setLookPositionWithEntity(entityPickup, 30f, entityMaid.getVerticalFaceSpeed());
            entityMaid.getNavigator().tryMoveToXYZ(entityPickup.posX, entityPickup.posY, entityPickup.posZ, speed);
        }

        // 拾取对象为空，或者没活着，那就遍历获取下一个
        else {
            if (!list.isEmpty()) {
                for (Entity entity : list) {
                    // 物品活着，而且能塞入女仆背包
                    if (entity instanceof EntityItem && entity.isEntityAlive() && entityMaid.canEntityBeSeen(entity) &&
                            entityMaid.canInsertSlot(((EntityItem) entity).getItem())) {
                        entityPickup = entity;
                        return;
                    }

                    // 经验球
                    if (entity instanceof EntityXPOrb && entity.isEntityAlive() && entityMaid.canEntityBeSeen(entity)) {
                        entityPickup = entity;
                        return;
                    }
                }
            }

            // 如果都不符合，那就清空列表
            list.clear();
        }
    }

    @Override
    public void resetTask() {
        entityPickup = null;
        list.clear();
        entityMaid.getNavigator().clearPath();
    }

    @Override
    public boolean shouldContinueExecuting() {
        return !list.isEmpty() && !entityMaid.isSitting() && entityMaid.isPickup();
    }
}
