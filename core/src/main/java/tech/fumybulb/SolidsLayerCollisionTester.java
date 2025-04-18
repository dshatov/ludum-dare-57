package tech.fumybulb;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;

import java.util.function.Consumer;

public class SolidsLayerCollisionTester {
    private final TiledMapTileLayer layer;
    private final IntRect tileRect;

    public SolidsLayerCollisionTester(TiledMapTileLayer layer) {
        this.layer = layer;
        tileRect = new IntRect(0, 0, Conf.TILE_SIZE, Conf.TILE_SIZE);
    }

    public int getTilesCount() {
        int count = 0;
        for (int i = 0; i < layer.getWidth(); i++) {
            for (int j = 0; j < layer.getHeight(); j++) {
                if (layer.getCell(i, j) != null) {
                    count++;
                }
            }
        }
        return count;
    }

    public boolean overlaps(final IntRect other, Consumer<TiledMapTileLayer.Cell> onCollide) {
        if (other.y < 0 || other.x < 0) {
            return true;
        }
        if (other.x + other.w > layer.getWidth() * layer.getTileWidth()) {
            return true;
        }

        int startX = other.x / Conf.TILE_SIZE;
        int endX = (other.x + other.w - 1) / Conf.TILE_SIZE + 1;

        int startY = other.y / Conf.TILE_SIZE;
        int endY = (other.y + other.h - 1) / Conf.TILE_SIZE + 1;

        for (int x = startX; x < endX; x++) {
            for (int y = startY; y < endY; y++) {
                TiledMapTileLayer.Cell cell = layer.getCell(x, y);
                if (cell != null) {
                    tileRect.x = x * Conf.TILE_SIZE;
                    tileRect.y = y * Conf.TILE_SIZE;
                    if (onCollide != null) {
                        onCollide.accept(cell);
                        layer.setCell(x, y, null);
                    }
                    if (tileRect.overlaps(other)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
