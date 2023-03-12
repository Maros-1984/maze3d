package com.vranec;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.util.SkyFactory;

import java.util.Random;

public class Maze3DVisualisation extends SimpleApplication implements ActionListener {

    private CharacterControl player;
    final private Vector3f walkDirection = new Vector3f();
    private boolean left = false, right = false, up = false, down = false;

    //Temporary vectors used on each frame.
    //They here to avoid instantiating new vectors on each frame
    final private Vector3f camDir = new Vector3f();
    final private Vector3f camLeft = new Vector3f();
    private BulletAppState bulletAppState;
    private Geometry finishGeometry;
    private static final Random random = new Random();


    public static void main(String[] args) {
        Maze3DVisualisation app = new Maze3DVisualisation();
        app.setShowSettings(false); //Settings dialog not supported on mac
        app.start();
    }

    @Override
    public void simpleInitApp() {
        Maze maze = new Maze(13);

        getRootNode().attachChild(SkyFactory.createSky(getAssetManager(), "Textures/BrightSky.dds",
                SkyFactory.EnvMapType.CubeMap));

        // Set up Physics
        this.bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);

        // We re-use the flyby camera for rotation, while positioning is handled by physics
        viewPort.setBackgroundColor(new ColorRGBA(0.7f, 0.8f, 1f, 1f));
        flyCam.setMoveSpeed(100);
        cam.setFrustumNear(0.7f);
        cam.update();
        setUpKeys();
        setUpLight();

        // We set up collision detection for the player by creating
        // a capsule collision shape and a CharacterControl.
        // The CharacterControl offers extra settings for
        // size, step height, falling, and gravity.
        // We also put the player in its starting position.

        for (int x = 0; x <= maze.getSize() + 1; x++) {
            for (int z = 0; z <= maze.getSize() + 1; z++) {
                draw(maze.getBlockAt(x, z));
            }
        }

        CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(1f, 1f, 1);
        player = new CharacterControl(capsuleShape, 0.05f);
        player.setFallSpeed(30);
        player.setPhysicsLocation(new Vector3f(0, -3.8f, 10));

        // We attach the scene and the player to the rootnode and the physics space,
        // to make them appear in the game world.
        bulletAppState.getPhysicsSpace().add(player);
    }

    private void setUpLight() {
        // We add light so we see the scene
        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White.mult(1.3f));
        rootNode.addLight(al);

        DirectionalLight dl = new DirectionalLight();
        dl.setColor(ColorRGBA.White);
        dl.setDirection(new Vector3f(2.8f, -2.8f, -2.8f).normalizeLocal());
        rootNode.addLight(dl);
    }

    /**
     * We over-write some navigational key mappings here, so we can add physics-controlled walking:
     */
    private void setUpKeys() {
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addListener(this, "Left");
        inputManager.addListener(this, "Right");
        inputManager.addListener(this, "Up");
        inputManager.addListener(this, "Down");
    }

    private void draw(MazeBlock block) {
        int x = block.getX();
        int z = block.getY();

        Vector3f position = new Vector3f(x * 2 - 2, -4, 12 - 2 * z);

        if (block.isSolidWall()) {
            addWall(position);
        } else if (block.isFinishBlock()) {
            addFinishBlock(position);
            addFloor(position);
        } else {
            addFloor(position);
        }
    }

    private void addFloor(Vector3f position) {
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setTexture("ColorMap", assetManager.loadTexture("Textures/floor.jpg"));

        Box b = new Box(1, 0.1f, 1);
        Geometry geom = new Geometry("Box", b);

        addToScene(position.add(0f, -1.1f, 0f), mat, geom);
    }

    private void addFinishBlock(Vector3f position) {
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setTexture("ColorMap", assetManager.loadTexture("Textures/finish.jpg"));

        finishGeometry = new Geometry("Sphere", new Sphere(32, 32, 1));

        addToScene(position, mat, finishGeometry);
    }

    private void addToScene(Vector3f position, Material mat, Geometry geom) {
        geom.setMaterial(mat);
        geom.setLocalTranslation(position);

        // We set up collision detection for the scene by creating a
        // compound collision shape and a static RigidBodyControl with mass zero.
        CollisionShape sceneShape = CollisionShapeFactory.createMeshShape(geom);
        RigidBodyControl landscape = new RigidBodyControl(sceneShape, 0);
        geom.addControl(landscape);
        bulletAppState.getPhysicsSpace().add(landscape);

        rootNode.attachChild(geom);
    }

    private void addWall(Vector3f position) {
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setTexture("ColorMap", assetManager.loadTexture("Textures/wall.jpg"));

        Box b = new Box(1, 1, 1);
        Geometry geom = new Geometry("Box", b);

        addToScene(position, mat, geom);
    }

    /**
     * This is the main event loop--walking happens here. We check in which direction the player is walking by
     * interpreting the camera direction forward (camDir) and to the side (camLeft). The setWalkDirection() command is
     * what lets a physics-controlled player walk. We also make sure here that the camera moves with player.
     */
    @Override
    public void simpleUpdate(float tpf) {
        camDir.set(cam.getDirection()).multLocal(0.16f);
        camLeft.set(cam.getLeft()).multLocal(0.10f);
        walkDirection.set(0, 0, 0);
        if (left) {
            walkDirection.addLocal(camLeft);
        }
        if (right) {
            walkDirection.addLocal(camLeft.negate());
        }
        if (up) {
            walkDirection.addLocal(camDir);
        }
        if (down) {
            walkDirection.addLocal(camDir.negate());
        }
        player.setWalkDirection(walkDirection);
        cam.setLocation(player.getPhysicsLocation());
        finishGeometry.rotate(0.002f * random.nextFloat(), 0.002f * random.nextFloat(),
                0.002f * random.nextFloat());
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //add render code here (if any)
    }

    /**
     * These are our custom actions triggered by key presses. We do not walk yet, we just keep track of the direction
     * the user pressed.
     */
    @Override
    public void onAction(String binding, boolean value, float tpf) {
        switch (binding) {
            case "Left":
                left = value;
                break;
            case "Right":
                right = value;
                break;
            case "Up":
                up = value;
                break;
            case "Down":
                down = value;
                break;
        }
    }
}
