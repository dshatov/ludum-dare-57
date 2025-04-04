package tech.fumybulb;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

import java.util.EnumSet;
import java.util.Map;

public class Player extends Actor {

    private static final Map<State, Color> STATES_COLORS = Map.of(
        State.RUNNING, Color.RED,
        State.FALLING, Color.FOREST,
        State.JUMPING, Color.YELLOW,
        State.STANDING, Color.SKY
    );

    private static final float MAX_MOVEMENT_VX = 120;
    private static final float MOVEMENT_AX = 240;
    private static final float FALLING_AY = 800;
    private static final float FALLING_WHEN_JUMPING_AY = 600;
    private static final float MAX_FALLING_VY = 800;
    private static final float JUMPING_INSTANT_VY = 240;

    /**
     * Main usage: do jump on landing if key was pressed slighly before landing.
     */
    private static final float INPUT_BUFFERING_SECONDS = 0.15f;

    /**
     * Main usage: do jump when FALLING if STANDING/RUNNING state was active slighly before FALLING.
     */
    private static final float STATE_BUFFERING_SECONDS = 0.05f;

    //------------------------------------------------------------------------------------------------------------------

    private final EnumSet<ActionInput> currentInputs = EnumSet.noneOf(ActionInput.class);
    private final EnumSet<ActionInput> previousInput = EnumSet.noneOf(ActionInput.class);
    private final float[] inputChangeTime = new float[ActionInput.INSTANCES.length];

    private float vx = 0;
    private float vy = 0;
    private float ax = 0;
    private float ay = 0;

    private State state = State.STANDING;
    private State previousState = State.RUNNING;
    private float stateChangeTime = 3600;

    public SolidsLayerCollisionTester solidsLayerCollisionTester = null;

    //------------------------------------------------------------------------------------------------------------------

    @SuppressWarnings("unused")
    public Player() {
        this(0, 0);
    }

    public Player(final int x, final int y) {
        this(x, y, Conf.TILE_SIZE - 5, Conf.TILE_SIZE - 5);
    }

    public Player(final int x, final int y, final int w, final int h) {
        super(x, y, w, h);
    }

    @Override
    public void draw(final ShapeRenderer sr) {
        sr.setColor(STATES_COLORS.get(state));
        super.draw(sr);
    }

    public void collectInput() {
        previousInput.clear();
        previousInput.addAll(currentInputs);

        for (int i = 0; i < ActionInput.INSTANCES.length; i++) {
            ActionInput actionInput = ActionInput.INSTANCES[i];
            boolean isChanged = actionInput.isPressed()
                ? currentInputs.add(actionInput)
                : currentInputs.remove(actionInput);
            if (isChanged) {
                inputChangeTime[i] = 0;
            }
        }
    }

    public void fixedUpdate(final float dt) {
        for (int i = 0; i < inputChangeTime.length; i++) {
            inputChangeTime[i] += dt;
        }
        stateChangeTime += dt;
        for (ActionInput actionInput : ActionInput.INSTANCES) {
            InputState inputState = actionInput.getState(this);
            handleInput(dt, actionInput, inputState);
        }
        state.update(this, dt);
    }

    public void updateCameraTarget(float dt, CameraTarget cameraTarget) {
        cameraTarget.update(dt, x, y);
    }

    public boolean hasTinyTimeSinceStateUpdate() {
        return stateChangeTime <= STATE_BUFFERING_SECONDS;
    }

    //------------------------------------------------------------------------------------------------------------------

    @Override
    protected boolean overlapsSolids() {
        return super.overlapsSolids() || overlapsSolidTiles();
    }

    private boolean overlapsSolidTiles() {
        if (solidsLayerCollisionTester == null) {
            return false;
        }

        return solidsLayerCollisionTester.overlaps(this);
    }

    //------------------------------------------------------------------------------------------------------------------

    private void setState(State newState) {
        if (state == newState) {
            return;
        }
        stateChangeTime = 0;
        previousState = state;
        state = newState;

        state.enterState(this);
    }

    //------------------------------------------------------------------------------------------------------------------

    private void handleInput(final float dt, final ActionInput input, InputState inputState) {
        state.handleInput(this, dt, input, inputState);

        previousInput.clear();
        previousInput.addAll(currentInputs);
    }

    //------------------------------------------------------------------------------------------------------------------

    private void applyForces(float dt) {
        vx = MathUtils.clamp(vx + dt * ax, -MAX_MOVEMENT_VX, MAX_MOVEMENT_VX);
        vy = MathUtils.clamp(vy + dt * ay, -MAX_FALLING_VY, MAX_FALLING_VY);
    }

    //------------------------------------------------------------------------------------------------------------------

    private int applyVelocityX(final float dt) {
        return moveX(dt * vx, () -> vx = 0);
    }

    private int applyVelocityY(final float dt) {
        return moveY(dt * vy, () -> vy = 0);
    }

    //------------------------------------------------------------------------------------------------------------------

    private enum ActionInput {
        JUMP(new int[]{
            Input.Keys.SPACE,
            Input.Keys.W,
        }),
        LEFT(new int[]{
            Input.Keys.LEFT,
            Input.Keys.A,
        }),
        RIGHT(new int[]{
            Input.Keys.RIGHT,
            Input.Keys.D,
        }),
        ;

        public static final ActionInput[] INSTANCES = ActionInput.values();

        private final int[] keys;

        ActionInput(int[] keys) {
            this.keys = keys;
        }

        boolean isPressed() {
            for (int key : keys) {
                if (Gdx.input.isKeyPressed(key)) {
                    return true;
                }
            }
            return false;
        }

        InputState getState(Player player) {
            boolean isCurrent = player.currentInputs.contains(this);
            boolean isPrevious = player.previousInput.contains(this);
            if (isCurrent) {
                return isPrevious
                    ? InputState.PRESSED
                    : InputState.JUST_PRESSED;
            }
            return isPrevious
                ? InputState.JUST_RELEASED
                : InputState.RELEASED;
        }

        float getTimeSinceUpdate(Player player) {
            return player.inputChangeTime[this.ordinal()];
        }

        boolean hasTinyTimeSinceUpdate(Player player) {
            return getTimeSinceUpdate(player) <= INPUT_BUFFERING_SECONDS;
        }
    }

    //------------------------------------------------------------------------------------------------------------------

    // TODO: remove InputState?
    private enum InputState {
        JUST_PRESSED(true),
        JUST_RELEASED(false),
        PRESSED(true),
        RELEASED(false),
        ;

        public final boolean isPressed;

        InputState(boolean isPressed) {
            this.isPressed = isPressed;
        }
    }

    //------------------------------------------------------------------------------------------------------------------

    private interface State {

        Standing STANDING = new Standing();
        Jumping JUMPING = new Jumping();
        Falling FALLING = new Falling();
        Running RUNNING = new Running();

        void handleInput(Player player, float dt, ActionInput input, InputState inputState);

        void update(Player player, float dt);

        void enterState(Player player);

        private static boolean handleDirInput(Player player, ActionInput input) {
            if (input != ActionInput.LEFT && input != ActionInput.RIGHT) {
                return false;
            }
            final boolean leftIsPressed = player.currentInputs.contains(ActionInput.LEFT);
            final boolean rightIsPressed = player.currentInputs.contains(ActionInput.RIGHT);
            if (leftIsPressed && rightIsPressed) {
                return true;
            }
            if (rightIsPressed) {
                player.ax = MOVEMENT_AX;
                if (player.vx < 0) {
                    player.vx = 0;
                }
            } else if (leftIsPressed) {
                player.ax = -MOVEMENT_AX;
                if (player.vx > 0) {
                    player.vx = 0;
                }
            } else {
                player.ax = 0;
                player.vx = 0;
            }
            return true;
        }

        //--------------------------------------------------------------------------------------------------------------

        class Standing implements State {
            @Override
            public void handleInput(Player player, float dt, ActionInput input, InputState inputState) {
                if (handleDirInput(player, input)) {
                    return;
                }
                if (input == ActionInput.JUMP && input.isPressed() && input.hasTinyTimeSinceUpdate(player)) {
                    player.vy = JUMPING_INSTANT_VY;
                    player.ay = -FALLING_WHEN_JUMPING_AY;
                    player.state = JUMPING;
                }
            }

            @Override
            public void update(Player player, float dt) {
                player.applyForces(dt);
                if (player.applyVelocityX(dt) != 0) {
                    player.state = RUNNING;
                }
                if (player.applyVelocityY(dt) < 0) {
                    player.state = FALLING;
                }
            }

            @Override
            public void enterState(Player player) {

            }
        }

        //--------------------------------------------------------------------------------------------------------------

        class Jumping implements State {

            @Override
            public void handleInput(Player player, float dt, ActionInput input, InputState inputState) {
                if (handleDirInput(player, input)) {
                    return;
                }
                if (input == ActionInput.JUMP && !input.isPressed()) {
                    player.vy = 0;
                    player.ay = -FALLING_AY;
                    player.setState(FALLING);
                }
            }

            @Override
            public void update(Player player, float dt) {
                player.applyForces(dt);
                int dy = player.applyVelocityY(dt);
                player.applyVelocityX(dt);
                if (dy < 0) {
                    player.ay = -FALLING_AY;
                    player.setState(FALLING);
                }
            }

            @Override
            public void enterState(Player player) {

            }
        }

        //--------------------------------------------------------------------------------------------------------------

        class Falling implements State {

            @Override
            public void handleInput(Player player, float dt, ActionInput input, InputState inputState) {
                if (handleDirInput(player, input)) {
                    return;
                }
                if (
                    input == ActionInput.JUMP && (player.previousState == RUNNING || player.previousState == STANDING)
                        && player.hasTinyTimeSinceStateUpdate()
                        && input.isPressed() && input.hasTinyTimeSinceUpdate(player)
                ) {
                    player.vy = JUMPING_INSTANT_VY;
                    player.ay = -FALLING_WHEN_JUMPING_AY;
                    player.setState(JUMPING);
                }
            }

            @Override
            public void update(Player player, float dt) {
                player.applyForces(dt);
                int dy = player.applyVelocityY(dt);
                if (0 <= dy && player.vy == 0) {
                    player.setState(STANDING);
                }
                player.applyVelocityX(dt);
            }

            @Override
            public void enterState(Player player) {

            }
        }

        //--------------------------------------------------------------------------------------------------------------

        class Running implements State {

            @Override
            public void handleInput(Player player, float dt, ActionInput input, InputState inputState) {
                if (handleDirInput(player, input)) {
                    return;
                }
                if (input == ActionInput.JUMP && input.isPressed() && input.hasTinyTimeSinceUpdate(player)) {
                    player.vy = JUMPING_INSTANT_VY;
                    player.ay = -FALLING_WHEN_JUMPING_AY;
                    player.setState(JUMPING);
                }
            }

            @Override
            public void update(Player player, float dt) {
                player.applyForces(dt);
                int dy = player.applyVelocityY(dt);
                if (player.vx == 0) {
                    player.setState(STANDING);
                }
                player.applyVelocityX(dt);
                if (dy < 0) {
                    player.ay = -FALLING_AY;
                    player.setState(FALLING);
                }
            }

            @Override
            public void enterState(Player player) {

            }
        }
    }
}
