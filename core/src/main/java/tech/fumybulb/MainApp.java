package tech.fumybulb;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

public class MainApp extends Game {
    private final Runnable onCreate;

    private MainResources res;

    @SuppressWarnings("unused")
    public MainApp() {
        this(() -> {});
    }

    public MainApp(Runnable onCreate) {
        this.onCreate = onCreate;
    }

    @Override
    public void create() {
        onCreate.run();
        res = new MainResources();
        setScreen(new GameScreen(res));
    }

    @Override
    public void render() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            setScreen(new GameScreen(res));
        }
        super.render();
    }

    @Override
    public void dispose() {
        res.dispose();
    }
}
