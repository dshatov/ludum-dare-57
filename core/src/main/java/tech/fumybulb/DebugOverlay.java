package tech.fumybulb;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.PerformanceCounter;
import com.badlogic.gdx.utils.StringBuilder;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;
import java.text.DecimalFormat;
import java.util.function.DoubleSupplier;

@SuppressWarnings("GDXJavaProfilingCode")
public class DebugOverlay {
    private final MainResources res;
    private final Viewport gameViewport;

    private final DecimalFormat decimalFormat = new DecimalFormat("0.000");
    private final Viewport screenViewport = new ScreenViewport();
    private final PerformanceCounter renderPerfCounter = new PerformanceCounter("render");
    private final StringBuilder stringBuilder = new StringBuilder();

    private boolean hidden = true;

    /**
     * Supplier of process CPU load. Costly!
     */
    private final DoubleSupplier processCpuLoad;

    public DebugOverlay(MainResources res, Viewport gameViewport, boolean showProcessCpuLoad) {
        this.res = res;
        this.gameViewport = gameViewport;

        processCpuLoad = showProcessCpuLoad
            ? new ThrottledDoubleSupplier(DebugOverlay::getProcessCpuLoad, 2000L)
            : () -> -1;
    }

    /**
     * @return process CPU load (percents). Costly call!
     */
    private static double getProcessCpuLoad() {
        return ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class).getProcessCpuLoad()
//            * Runtime.getRuntime().availableProcessors()
            * 100;
    }

    /**
     * Supposed to be called at start (!) of each render callback at owner side.
     */
    public void onRenderStart() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
            hidden = !hidden;
            renderPerfCounter.reset();
        }
        if (hidden) {
            return;
        }

        renderPerfCounter.tick();
        renderPerfCounter.start();
    }

    /**
     * Supposed to be called at end (!) of each render callback at owner side.
     */
    public void onRenderEnd() {
        if (hidden) {
            return;
        }

        debugDrawTilesGrid();

        long totalMem = Runtime.getRuntime().totalMemory();
        long freeMem = Runtime.getRuntime().freeMemory();
        stringBuilder.setLength(0);
        stringBuilder
            .append("DEBUG OVERLAY. Press F1 to hide.\n")
            .append("FPS: ").append(Gdx.graphics.getFramesPerSecond()).append('\n')
            .append("Java heap used: ").append((totalMem - freeMem) >> 20)
            .append(" of ").append(totalMem >> 20).append(" MiB (free: ")
            .append(freeMem >> 20).append(" MiB)\n")
            .append("Process load: ").append(decimalFormat.format(processCpuLoad.getAsDouble())).append("%\n")
            .append("Update load: ").append(decimalFormat.format(renderPerfCounter.load.value * 100)).append("%\n");

        screenViewport.apply();
        res.spriteBatch.setProjectionMatrix(screenViewport.getCamera().combined);
        res.spriteBatch.begin();
        res.spriteBatch.setColor(Colors.TEXT);
        res.font.draw(res.spriteBatch, stringBuilder, 0, screenViewport.getScreenHeight());
        res.spriteBatch.end();

        renderPerfCounter.stop();
    }

    public void resize(int width, int height) {
        screenViewport.update(width, height, true);
    }

    private void debugDrawTilesGrid() {
        Vector3 gameCameraPos = gameViewport.getCamera().position;
        int gridOffsetX = ((int) -gameCameraPos.x) % Conf.TILE_SIZE;
        int gridOffsetY = ((int) -gameCameraPos.y + 2) % Conf.TILE_SIZE;

        gameViewport.apply(true);
        res.shapeRenderer.setProjectionMatrix(gameViewport.getCamera().combined);
        res.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        res.shapeRenderer.setColor(Colors.PIXELS_GRID);
        drawGrid(0, 0, 1);
        res.shapeRenderer.setColor(Colors.TILES_GRID);
        drawGrid(gridOffsetX, gridOffsetY, Conf.TILE_SIZE);
        res.shapeRenderer.end();
    }

    @SuppressWarnings("SameParameterValue")
    private void drawGrid(int x, int y, int step) {
        for (; x < Conf.VIEWPORT_WIDTH; x += step) {
            res.shapeRenderer.line(x, 0, x, Conf.VIEWPORT_HEIGHT);
        }
        for (; y < Conf.VIEWPORT_HEIGHT; y += step) {
            res.shapeRenderer.line(0, y, Conf.VIEWPORT_WIDTH, y);
        }
    }

    //------------------------------------------------------------------------------------------------------------------

    static final class Colors {
        static final Color TILES_GRID = Color.GRAY;
        static final Color PIXELS_GRID = Color.DARK_GRAY;
        static final Color TEXT = Color.WHITE;
    }

    //------------------------------------------------------------------------------------------------------------------

    public static class ThrottledDoubleSupplier implements DoubleSupplier {
        private final DoubleSupplier supplier;
        private final long updatePeriodMillis;

        private double lastValue = 0.0d;
        private long lastUpdateTime = 0L;

        ThrottledDoubleSupplier(DoubleSupplier supplier, long updatePeriodMillis) {
            this.supplier = supplier;
            this.updatePeriodMillis = updatePeriodMillis;
        }

        @Override
        public double getAsDouble() {
            final long now = System.currentTimeMillis();
            if (lastUpdateTime + updatePeriodMillis <= now) {
                lastUpdateTime = now;
                lastValue = supplier.getAsDouble();
            }
            return lastValue;
        }
    }
}
