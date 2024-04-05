package de.melanx.datatrader.trader;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;

public class SetTargetLookingAtPlayerGoal extends LookAtPlayerGoal {

    public SetTargetLookingAtPlayerGoal(Mob mob, Class<? extends LivingEntity> lookAtType, float lookDistance, float probability) {
        super(mob, lookAtType, lookDistance, probability);
    }

    @Override
    public void start() {
        super.start();
        this.mob.setTarget((LivingEntity) this.lookAt);
    }

    @Override
    public void stop() {
        super.stop();
        this.mob.setTarget(null);
    }
}
