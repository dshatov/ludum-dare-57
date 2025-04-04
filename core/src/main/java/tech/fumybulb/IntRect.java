package tech.fumybulb;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class IntRect {
    public int x;
    public int y;
    public int w;
    public int h;

    // -----------------------------------------------------------------------------------------------------------------

    @SuppressWarnings("unused")
    public IntRect() {
        this(0, 0);
    }

    public IntRect(final int x, final int y) {
        this(x, y, Conf.TILE_SIZE, Conf.TILE_SIZE);
    }

    public IntRect(final int x, final int y, final int w, final int h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    // -----------------------------------------------------------------------------------------------------------------

    public boolean overlaps(final IntRect other) {
        return x < other.x + other.w
            && other.x < x + w
            && y < other.y + other.h
            && other.y < y + h;
    }

    public void draw(final ShapeRenderer sr) {
        sr.rect(x, y, w, h);
    }
}
