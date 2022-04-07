package example01;

import lwjglutils.*;
import org.lwjgl.glfw.*;
import transforms.*;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11C.glClear;
import static org.lwjgl.opengl.GL11C.glClearColor;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;


public class Renderer extends AbstractRenderer{

    // Shaders
    private int shaderProgramMain, shaderProgramPost;
    // Buffers
    private OGLBuffers buffersMain, buffersPost;
    // Camera
    private Camera camera;
    // Textures
    private ArrayList<OGLTexture2D> textures;
    private OGLTexture2D currentTexture;

    // Locations
    private int viewLocation, projectionLocation, colorLocation, modelLocation, timeLocation;
    private int lightLocation;

    private Mat4 projection, model, rotation, translation;

    // Window controls
    public static final double SPEED_OF_WASD = 0.05;

    private boolean mousePressed = false;
    private boolean startToMove = false;
    private boolean showHelp = false;
    private boolean showMultipleObjects = false;

    private int colorType = 0;
    private int objectType = 0;
    private int fillType = 0;
    private int textureType = 0;
    private int buttonPressed;

    private int currentKey;
    private double oldMx, oldMy;
    private OGLRenderTarget renderTarget;
    private OGLTexture2D.Viewer viewer;
    private int typeLocation;

    // Light
    private Vec3D lightPoint;

    // Object movement
    private float moving = 0f;


    @Override
    public void init() {
        printingOGLUParameters();

        glClearColor(0.1f, 0.1f, 0.1f, 1f);

        fillPolygon(0);

        shaderProgramMain = ShaderUtils.loadProgram("/example01/main");
        viewLocation = glGetUniformLocation(shaderProgramMain, "view");
        projectionLocation = glGetUniformLocation(shaderProgramMain, "projection");

        typeLocation = glGetUniformLocation(shaderProgramMain, "type");
        timeLocation = glGetUniformLocation(shaderProgramMain, "time");
        colorLocation = glGetUniformLocation(shaderProgramMain, "color");
        modelLocation = glGetUniformLocation(shaderProgramMain, "model");

        // Light
        lightLocation = glGetUniformLocation(shaderProgramMain, "light");


        shaderProgramPost = ShaderUtils.loadProgram("/example01/post");


        resetCamera();
        lightPoint = new Vec3D(0,1,2);
        projection = setProjectionPerspective();
        model = new Mat4Identity();
        rotation = new Mat4Identity();
        translation = new Mat4Identity();

        getTextures();

        buffersMain = GridFactory.generateGrid(50,50);
        buffersPost = GridFactory.generateGrid(2,2);

        renderTarget = new OGLRenderTarget(800, 800);

        getTextures();
        currentTexture = textures.get(0);

        viewer = new OGLTexture2D.Viewer();
        textRenderer = new OGLTextRenderer(LwjglWindow.WIDTH, LwjglWindow.HEIGHT);
        textRenderer.setBackgroundColor(new Color(0.0f, 0.0f, 0.0f, 0.0f));
    }

    private void fillPolygon(int type) {
        switch (type) {
            case 0: glPolygonMode(GL_FRONT_AND_BACK, GL_FILL); break;
            case 1: glPolygonMode(GL_FRONT_AND_BACK, GL_LINE); break;
            case 2: glPolygonMode(GL_FRONT_AND_BACK, GL_POINT); break;
        }
    }


    private void getTextures() {
        File[] files = new File("./res/").listFiles();
        textures = new ArrayList<>();

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    try {
                        textures.add(new OGLTexture2D(file.getName()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void display() {
        glEnable(GL_DEPTH_TEST); // Text renderer closing ZBuffer!

       fillPolygon(fillType);

        renderMain();
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        renderPostProcessing();

        glDisable(GL_DEPTH_TEST);
        viewer.view(renderTarget.getColorTexture(), -1, -0.5, 0.5);
        viewer.view(renderTarget.getDepthTexture(), -1, 0, 0.5);
        viewer.view(currentTexture, -1, -1, 0.5);
        text();
    }

    private void renderMain() {
        glUseProgram(shaderProgramMain);
        renderTarget.bind();

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glUniformMatrix4fv(viewLocation, false,  camera.getViewMatrix().floatArray());
        glUniformMatrix4fv(projectionLocation, false, projection.floatArray());
        glUniformMatrix4fv(modelLocation, false, model.floatArray());

        glUniform1f(colorLocation, colorType);
        glUniform1f(typeLocation, objectType);

        float step = 0.01f;
        if (startToMove) moving += step;
        glUniform1f(timeLocation, moving);

        currentTexture = textures.get(textureType);
        currentTexture.bind(shaderProgramMain, "currentTexture", 0);

        buffersMain.draw(GL_TRIANGLES, shaderProgramMain);

        if(showMultipleObjects) {
            glUniform1f(typeLocation, 1f);
            buffersMain.draw(GL_TRIANGLES, shaderProgramMain);
        }
    }

    private void renderPostProcessing() {
        glUseProgram(shaderProgramPost);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glViewport(0,0, LwjglWindow.WIDTH, LwjglWindow.HEIGHT);

        renderTarget.getColorTexture().bind(shaderProgramPost, "textureRendered", 0);
        buffersPost.draw(GL_TRIANGLES, shaderProgramPost);
    }

    private void clickKey(int key) {
        switch (key) {
            case GLFW_KEY_R: // RESET
                resetCamera();
                projection = setProjectionPerspective();
                break;
            case GLFW_KEY_W:
                camera = camera.down(SPEED_OF_WASD);
                break;
            case GLFW_KEY_S:
                camera = camera.up(SPEED_OF_WASD);
                break;
            case GLFW_KEY_A:
                camera = camera.right(SPEED_OF_WASD);
                break;
            case GLFW_KEY_D:
                camera = camera.left(SPEED_OF_WASD);
                break;
            case GLFW_KEY_O:
                projection = setProjectionOrthogonal();
                break;
            case GLFW_KEY_P:
                projection = setProjectionPerspective();
            case GLFW_KEY_T:
                if (colorType < 1) colorType++;
                break;
            case GLFW_KEY_Y:
                if (colorType > 0) colorType--;
                break;
            case GLFW_KEY_K:
                if (textureType < 10) textureType++;
                break;
            case GLFW_KEY_L:
                if (textureType > 0) textureType--;
                break;
            case GLFW_KEY_Q:
                if (fillType < 2) fillType++;
                break;
            case GLFW_KEY_E:
                if (fillType > 0) fillType--;
                break;
            case GLFW_KEY_U:
                if (objectType < 7) objectType++;
                System.out.println(objectType);
                break;
            case GLFW_KEY_I:
                if (objectType > 0) objectType--;
                break;
            case GLFW_KEY_8:
                // TODO rotation up
                break;
            case GLFW_KEY_2:
                // TODO rotation down
                break;
            case GLFW_KEY_4:
                // TODO rotation left
                break;
            case GLFW_KEY_6:
                // TODO rotation right
                break;
            case GLFW_KEY_G:
                startToMove = !startToMove;
                break;
            case GLFW_KEY_H:
                currentKey = GLFW_KEY_H;
                showHelp = !showHelp;
                break;
            case GLFW_KEY_M:
                showMultipleObjects = !showMultipleObjects;
                break;
            default:
                System.err.println("Unknown key detected");
                break;
        }
    }

    private final GLFWCursorPosCallback cursorPosCallback = new GLFWCursorPosCallback() {
        @Override
        public void invoke(long window, double x, double y) {
            if (mousePressed) {
                switch (buttonPressed) {
                    case GLFW_MOUSE_BUTTON_LEFT:
                        System.out.println("Left button to move click.");
                        camera = camera.addAzimuth(Math.PI / 2 * (oldMx - x) / LwjglWindow.WIDTH);
                        camera = camera.addZenith(Math.PI / 2 * (oldMy - y) / LwjglWindow.HEIGHT);
                        oldMx = x;
                        oldMy = y;
                        break;
                    case GLFW_MOUSE_BUTTON_RIGHT:
                        rotation = rotation.mul(new Mat4RotXYZ(0, (oldMy - y) / 300, (oldMx - x) / 300));
                        model = rotation.mul(translation);
                        oldMx = x;
                        oldMy = y;
                        break;
                    default:
                        System.out.println("Cursor not handled - " + buttonPressed);
                        break;
                }
            }
        }
    };

    private final GLFWMouseButtonCallback mouseButtonCallback = new GLFWMouseButtonCallback() {
        @Override
        public void invoke(long window, int button, int action, int mods) {
            double[] xPos = new double[1];
            double[] yPos = new double[1];
            glfwGetCursorPos(window, xPos, yPos);
            oldMx = xPos[0];
            oldMy = yPos[0];
            mousePressed = action == GLFW_PRESS;
            buttonPressed = button;
        }
    };

    private final GLFWScrollCallback scrollCallback = new GLFWScrollCallback() {
        @Override
        public void invoke(long window, double dx, double dy) {
                if (dy < 0) {
                    camera = camera.backward(0.1);
                } else {
                    camera = camera.forward(0.1);
                }
        }
    };


    private final GLFWWindowSizeCallback wsCallback = new GLFWWindowSizeCallback() {
        @Override
        public void invoke(long window, int w, int h) {
            if (w > 0 && h > 0) {
                width = w;
                height = h;
                if (textRenderer != null) textRenderer.resize(width, height);
            }
        }
    };


    private final GLFWKeyCallback keyCallback = new GLFWKeyCallback() {
        @Override
        public void invoke(long window, int key, int scancode, int action, int mods) {
            if (GLFW_RELEASE == action) {
                clickKey(key);
            }
        }
    };





    private void text() {
        textRenderer.addStr2D(10, 20, "GPU: " + glGetString(GL_RENDERER));
        textRenderer.addStr2D(10, 40, "PUSH \"H\" FOR SHOW HELP");

        if (mousePressed) {
            textRenderer.addStr2D(LwjglWindow.WIDTH - 150,LwjglWindow.HEIGHT - 10, "Mouse is pressed");
        }

        if (currentKey == GLFW_KEY_H) {
            int indexW = LwjglWindow.WIDTH - 180;
            if (showHelp) {
                textRenderer.addStr2D(LwjglWindow.WIDTH - 200, 20, "Help:");
                textRenderer.addStr2D(indexW, 40, "Q & E - fill / line / point");
                textRenderer.addStr2D(indexW, 60, "U & I - Change objects");
                textRenderer.addStr2D(indexW, 80, "U & I - Change objects");
                textRenderer.addStr2D(indexW, 100, "U & I - Change objects");
                textRenderer.addStr2D(indexW, 120, "U & I - Change objects");
                textRenderer.addStr2D(indexW, 140, "U & I - Change objects");
                textRenderer.addStr2D(indexW, 160, "U & I - Change objects");
                textRenderer.addStr2D(indexW, 180, "M - show multiple objects");
            }
        }
    }

    private void printingOGLUParameters() {
        OGLUtils.printOGLparameters();
        OGLUtils.printLWJLparameters();
        OGLUtils.printJAVAparameters();
        OGLUtils.shaderCheck();
    }

    private void resetCamera() {
        camera = new Camera().withPosition(new Vec3D(2, 2, 2)).withAzimuth(5 / 4f * Math.PI).withZenith(-1 / 5f * Math.PI);
    }

    private Mat4 setProjectionPerspective() {
        return new Mat4PerspRH(Math.PI / 3,LwjglWindow.HEIGHT / (float) LwjglWindow.WIDTH,0.1,20);
    }

    private Mat4 setProjectionOrthogonal() {
        return new  Mat4OrthoRH(3, 3, 0.1, 20);
    }


    @Override
    public GLFWKeyCallback getKeyCallback() { return keyCallback; }

    @Override
    public GLFWCursorPosCallback getCursorCallback() { return cursorPosCallback; }

    @Override
    public GLFWMouseButtonCallback getMouseCallback() { return mouseButtonCallback; }

    @Override
    public GLFWScrollCallback getScrollCallback() { return scrollCallback; }

    @Override
    public GLFWWindowSizeCallback getWsCallback() { return wsCallback; }
}