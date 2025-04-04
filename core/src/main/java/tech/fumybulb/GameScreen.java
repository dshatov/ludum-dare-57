package tech.fumybulb;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class GameScreen extends ScreenAdapter {
    private final FixedDtUpdater fixedDtUpdater = new FixedDtUpdater();
    private final Player player = new Player();
    private final OrthographicCamera gameCamera = new OrthographicCamera();
    private final Viewport gameViewport = new FitViewport(Conf.VIEWPORT_WIDTH, Conf.VIEWPORT_HEIGHT, gameCamera);
    private final MainResources res;
    private final DebugOverlay debugOverlay;
    private final TiledMap tiledMap = new TmxMapLoader().load("test-level-big.tmx");
    private final OrthogonalTiledMapRenderer mapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
    private final CameraTarget cameraTarget = new CameraTarget();

    //------------------------------------------------------------------------------------------------------------------

    public GameScreen(MainResources res) {
        this.res = res;
        debugOverlay = new DebugOverlay(res, gameViewport, Conf.DEBUG_SHOW_PROCESS_CPU_LOAD);
        TiledMapTileLayer solids = (TiledMapTileLayer) tiledMap.getLayers().get("solids");
        player.solidsLayerCollisionTester = new SolidsLayerCollisionTester(solids);
        cameraTarget.setBounds(
            Conf.VIEWPORT_WIDTH / 2,
            Conf.VIEWPORT_HEIGHT / 2,
            solids.getWidth() * solids.getTileWidth() - Conf.VIEWPORT_WIDTH / 2,
            solids.getHeight() * solids.getTileHeight() - Conf.VIEWPORT_HEIGHT / 2
        );
    }

    //------------------------------------------------------------------------------------------------------------------

    @Override
    public void render(float delta) {
        debugOverlay.onRenderStart();
        update(delta);
        draw();
        debugOverlay.onRenderEnd();
    }

    @Override
    public void resize(int width, int height) {
        gameViewport.update(width, height, true);
        debugOverlay.resize(width, height);
    }

    //------------------------------------------------------------------------------------------------------------------

    private void update(float dt) {
        player.collectInput();
        fixedDtUpdater.update(dt, this::fixedUpdate);
        player.updateCameraTarget(dt, cameraTarget);
    }

    private void fixedUpdate(float dt) {
        player.fixedUpdate(dt);
    }

    private void draw() {
        ScreenUtils.clear(Color.BLACK);
        gameViewport.apply(true);
        res.shapeRenderer.setProjectionMatrix(gameCamera.combined);

        res.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        res.shapeRenderer.setColor(ConstPalette.BG);
        res.shapeRenderer.rect(0, 0, Conf.VIEWPORT_WIDTH, Conf.VIEWPORT_HEIGHT);

        cameraTarget.applyTo(gameCamera);
        res.shapeRenderer.setProjectionMatrix(gameCamera.combined);

        player.draw(res.shapeRenderer);
        res.shapeRenderer.setColor(Color.OLIVE);
        Actor.VIEWPORT_BOUNDS_SOLIDS.forEach(it -> it.draw(res.shapeRenderer));
        res.shapeRenderer.end();

        mapRenderer.setView(gameCamera);
        mapRenderer.render();
    }

    @Override
    public void dispose() {
        player.solidsLayerCollisionTester = null;
        mapRenderer.dispose();
        tiledMap.dispose();
    }
}
