package tech.fumybulb;

import it.unimi.dsi.fastutil.floats.FloatConsumer;

public class FixedDtUpdater {
    private final float fixedDt;

    private float remainder = 0;

    public FixedDtUpdater() {
        this(Conf.FIXED_UPDATE_DT_SECONDS);
    }

    public FixedDtUpdater(float fixedDt) {
        this.fixedDt = fixedDt;
    }

    public void update(float dt, FloatConsumer fixedUpdate) {
        remainder = Math.min(remainder + dt, Conf.FIXED_UPDATE_REMAINDER_LIMIT_SECONDS);
        while (fixedDt <= remainder) {
            remainder -= fixedDt;
            fixedUpdate.accept(fixedDt);
        }
    }
}
