package tech.fumybulb;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.MathUtils;

public class CameraTarget {
    private int cameraX;
    private int cameraY;

    private int minX = Integer.MIN_VALUE;
    private int minY = Integer.MIN_VALUE;
    private int maxX = Integer.MAX_VALUE;
    private int maxY = Integer.MAX_VALUE;

    public void applyTo(Camera camera) {
        camera.position.set(cameraX, cameraY,camera.position.z);
        camera.update();
    }

    public void update(float dt, int targetX, int targetY) {
        int dx = targetX - cameraX;
        int absDx = Math.abs(dx);
        if (absDx > 16) {
            cameraX += (int) (dt * dx * absDx * 0.1f);
        }
        int dy = targetY - cameraY;
        int absDy = Math.abs(dy);
        if (absDy > 8) {
            cameraY += (int) (dt * dy * absDy * 0.1f);
        }

        cameraX = MathUtils.clamp(cameraX, minX, maxX);
        cameraY = MathUtils.clamp(cameraY, minY, maxY);
    }

    public void setBounds(int minX, int minY, int maxX, int maxY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
    }
}
