package tech.fumybulb;

import java.util.List;

public class Actor extends IntRect {

    protected float xRemainder = 0.0f;
    protected float yRemainder = 0.0f;

    // -----------------------------------------------------------------------------------------------------------------

    @SuppressWarnings("unused")
    public Actor() {
        this(0, 0);
    }

    public Actor(final int x, final int y) {
        this(x, y, Conf.TILE_SIZE, Conf.TILE_SIZE);
    }

    public Actor(final int x, final int y, final int w, final int h) {
        super(x, y, w, h);
    }

    // -----------------------------------------------------------------------------------------------------------------

    public int moveX(final float dx, final Runnable onCollide) {
        int actualDx = 0;
        xRemainder += dx;
        int move = Math.round(xRemainder);
        if (move != 0) {
            xRemainder -= move;
            int sign = Integer.signum(move);
            while (move != 0) {
                x += sign;
                if (!overlapsSolids()) {
                    move -= sign;
                    actualDx += sign;
                } else {
                    x -= sign;
                    if (onCollide != null) {
                        onCollide.run();
                    }
                    break;
                }
            }
        }
        return actualDx;
    }

    public int moveY(final float dy, final Runnable onCollide) {
        int actualDy = 0;
        yRemainder += dy;
        int move = Math.round(yRemainder);
        if (move != 0) {
            yRemainder -= move;
            int sign = Integer.signum(move);
            while (move != 0) {
                y += sign;
                if (!overlapsSolids()) {
                    move -= sign;
                    actualDy += sign;
                } else {
                    y -= sign;
                    if (onCollide != null) {
                        onCollide.run();
                    }
                    break;
                }
            }
        }
        return actualDy;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    protected boolean overlapsSolids() {
        // TODO debug, replace with actual Solids
        return VIEWPORT_BOUNDS_SOLIDS.stream().anyMatch(this::overlaps);
    }

    // -----------------------------------------------------------------------------------------------------------------

    // TODO debug, replace with actual Solids
    public static final List<IntRect> VIEWPORT_BOUNDS_SOLIDS = List.of(
//        new IntRect(-Conf.TILE_SIZE, 0, Conf.TILE_SIZE, Conf.VIEWPORT_HEIGHT),
//        new IntRect(Conf.VIEWPORT_WIDTH, 0, Conf.TILE_SIZE, Conf.VIEWPORT_HEIGHT),
//        new IntRect(0, -Conf.TILE_SIZE, Conf.VIEWPORT_WIDTH, Conf.TILE_SIZE),
//        new IntRect(0, Conf.VIEWPORT_HEIGHT, Conf.VIEWPORT_WIDTH, Conf.TILE_SIZE)
    );
}
