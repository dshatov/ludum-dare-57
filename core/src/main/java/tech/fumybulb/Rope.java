package tech.fumybulb;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

public class Rope {
    private final int MAX_LENGTH = 256;

    private final Pool<Pixel> pixelsPool = new Pool<>() {
        @Override
        protected Pixel newObject() {
            return new Pixel();
        }
    };

    private final Array<Pixel> pixels = new Array<>();

    public void clear() {
        pixels.forEach(pixelsPool::free);
        pixels.clear();
    }

    public void start(int x, int y) {
        clear();
        add(x, y);
    }

    public void add(int x, int y) {
        Pixel pixel = pixelsPool.obtain();
        pixel.setPos(x, y);
        pixels.add(pixel);
    }

    public void draw(final ShapeRenderer sr) {
        sr.setColor(Color.ORANGE);
        pixels.forEach(px -> px.draw(sr));
    }

    public static class Pixel extends Actor implements Pool.Poolable {
        private Pixel prev;
        private Pixel next;

        Pixel(Pixel prev) {
            super(0, 0, 1, 1);
            this.prev = prev;
            this.prev.next = next;
            this.next = null;
        }

        @Override
        public void reset() {
            prev = null;
            next = null;
        }

        public int pullPrevX(int dx) {
            if (dx == 0 || prev == null) {
                return 0;
            }
            int actualDx = 0;
            int remainingDx = dx;
            int dxSignum = Integer.signum(dx);
            while (remainingDx != 0 && Math.abs(x + dxSignum - prev.x) < 2) {
                x += dxSignum;
                actualDx += dxSignum;
                remainingDx -= dxSignum;
            }

            if (remainingDx != 0) {
                int actualPullPrevDx = prev.pullPrevX(remainingDx);
                x += actualPullPrevDx;
                actualDx += actualPullPrevDx;
            }
            return actualDx;
        }

        @Override
        public int moveX(float dx, Runnable onCollide) {
            int actualDx = super.moveX(dx, onCollide);
            if (actualDx == 0) {
                return actualDx;
            }
            if (prev != null) {
                int prevDx = x - prev.x;
                if (Math.abs(prevDx) > 1) {
                    int signum = Integer.signum(actualDx);
                    int prevTargetDx = prevDx - signum;
                    int actualPullPrevDx = prev.pullPrevX(prevTargetDx);
                    int slideDx = prevTargetDx - actualPullPrevDx;
                    Pixel rayStart = this;
                    for (int i = 0; i < slideDx && rayStart.prev != null; i++) {
                        rayStart = rayStart.prev;
                    }
                    while (rayStart.next != null && slideDx != 0) {
                        slideDx -= signum;
                        // TODO
                    }
                    // TODO
                }
                // TODO
            }
            // TODO
            return actualDx;
        }

        @Override
        public int moveY(float dy, Runnable onCollide) {
            return super.moveY(dy, onCollide);
        }
    }
}
