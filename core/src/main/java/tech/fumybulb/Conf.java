package tech.fumybulb;

public class Conf {
    public static final int VIEWPORT_WIDTH = 320;
    public static final int VIEWPORT_HEIGHT = 180;

    public static final int WIN_WIDTH = VIEWPORT_WIDTH * 4;
    public static final int WIN_HEIGHT = VIEWPORT_HEIGHT * 4;

    public static final int TILE_SIZE = 8;

    public static final float FIXED_UPDATE_DT_SECONDS = 1.f / 240;
    public static final float FIXED_UPDATE_REMAINDER_LIMIT_SECONDS = 2;

    public static final boolean DEBUG_SHOW_PROCESS_CPU_LOAD = false;
}
