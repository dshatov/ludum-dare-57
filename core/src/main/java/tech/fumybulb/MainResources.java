package tech.fumybulb;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Disposable;

import java.util.Arrays;

public class MainResources implements Disposable {
    public final ShapeRenderer shapeRenderer;
    public final SpriteBatch spriteBatch;
    public final BitmapFont font;

    public final Texture rabbitSpriteSheetTexture;

    public MainResources() {
        shapeRenderer = new ShapeRenderer();
        spriteBatch = new SpriteBatch();
        font = new BitmapFont();

        rabbitSpriteSheetTexture = new Texture(Gdx.files.internal("rabbit.png"));
    }

    @Override
    public void dispose() {
        rabbitSpriteSheetTexture.dispose();
        font.dispose();
        spriteBatch.dispose();
        shapeRenderer.dispose();
    }
}
