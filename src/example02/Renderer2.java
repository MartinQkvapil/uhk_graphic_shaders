package example02;

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


public class Renderer2 extends AbstractRenderer2 {

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
    private int lightLocation, lightPartLocation, spotLightLocation;
    private int filterLocation, timeLocationFilter;

    private Mat4 projection, model, rotation, translation;

    // Window controls
    public static final double SPEED_OF_WASD = 0.05;
    public static final float SUN = 666;

    private boolean mousePressed = false;
    private boolean startToMove = false;
    private boolean showHelp = false;
    private boolean showMultipleObjects = false;
    private boolean showTrianglesStrips = false; // N
    private boolean showLight = false; // N
    private boolean lightToMove = true; // N

    private int lightPart = 0;
    private int showFilter = 0;
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
    private Vec3D lightPointPosition = new Vec3D(0, 4, 0);

    // Object movement
    private float moving = 0f;
    private float lightMoving = 0f;
    private float spotLight = 0.97f;


    @Override
    public void init() {
        printingOGLUParameters();

        glClearColor(0.1f, 0.1f, 0.1f, 1f);

        fillPolygon(0);

        // Shader main program
        shaderProgramMain = ShaderUtils.loadProgram("/example02/first");
        viewLocation = glGetUniformLocation(shaderProgramMain, "view");
        projectionLocation = glGetUniformLocation(shaderProgramMain, "projection");
        typeLocation = glGetUniformLocation(shaderProgramMain, "type");
        timeLocation = glGetUniformLocation(shaderProgramMain, "time");
        colorLocation = glGetUniformLocation(shaderProgramMain, "color");
        modelLocation = glGetUniformLocation(shaderProgramMain, "model");
        lightPartLocation = glGetUniformLocation(shaderProgramMain, "lightType");
        spotLightLocation = glGetUniformLocation(shaderProgramMain, "lightSize");


        // Light
        lightLocation = glGetUniformLocation(shaderProgramMain, "light");

        // Postprocessing
        shaderProgramPost = ShaderUtils.loadProgram("/example02/second");
        filterLocation = glGetUniformLocation(shaderProgramPost, "showFilter");
        timeLocationFilter = glGetUniformLocation(shaderProgramPost, "timeFilter");


        resetCamera();
        projection = setProjectionPerspective();
        model = new Mat4Identity();
        rotation = new Mat4Identity();
        translation = new Mat4Identity();

        generateGridOrTriangleStrip();
       // createBuffers();
        renderTarget = new OGLRenderTarget(1024, 1024);

        getTextures();
        currentTexture = textures.get(0);

        viewer = new OGLTexture2D.Viewer(); // Control
        textRenderer = new OGLTextRenderer(LwjglWindow2.WIDTH, LwjglWindow2.HEIGHT); // Text writing
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
    public void display() { // Render cca every 17ms
        glEnable(GL_DEPTH_TEST); // Text renderer closing ZBuffer!!!

        fillPolygon(fillType);

        renderMain();
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

        renderPostProcessing();

        glDisable(GL_DEPTH_TEST);
        viewer.view(renderTarget.getColorTexture(), -1, -0.5, 0.5); // Depth texture
        viewer.view(renderTarget.getDepthTexture(), -1, 0, 0.5); // Color texture from first
        viewer.view(currentTexture, -1, -1, 0.5); // Texture
        text();
    }

    private void renderMain() {
        glUseProgram(shaderProgramMain);
        renderTarget.bind(); // render to texture

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glUniformMatrix4fv(viewLocation, false,  camera.getViewMatrix().floatArray());
        glUniformMatrix4fv(projectionLocation, false, projection.floatArray());
        glUniform3fv(lightLocation, ToFloatArray.convert(lightPointPosition));
        glUniformMatrix4fv(modelLocation, false, model.floatArray());

        glUniform1f(colorLocation, colorType);
        glUniform1f(lightPartLocation, lightPart);
        glUniform1f(typeLocation, objectType);

        float step = 0.01f;
        if (startToMove) moving += step;
        glUniform1f(timeLocation, moving);

        currentTexture = textures.get(textureType);
        currentTexture.bind(shaderProgramMain, "currentTexture", 0);

        draw(buffersMain, shaderProgramMain);

        if(showMultipleObjects) {
            glUniform1f(typeLocation, 1f);
            draw(buffersMain, shaderProgramMain);
        }

        if (showLight) {
            camera = new Camera().withPosition(new Vec3D(5, 5, 5)).withAzimuth(5 / 4f * Math.PI).withZenith(-1 / 5f * Math.PI);
            glUniform1f(typeLocation, SUN);
            glUniform1f(colorLocation, SUN); // yellow
            glUniform1f(timeLocation, 0f);
            glUniform1f(spotLightLocation, spotLight);

            if (lightToMove) {
                lightMoving += step;
                lightPointPosition = new Vec3D(4 * Math.sin(lightMoving), 4 *Math.cos(lightMoving), 0);
                glUniform1f(timeLocation, lightMoving); // 0
            }
            glUniformMatrix4fv(modelLocation, false, new Mat4Transl(lightPointPosition).floatArray()); // move light to new position
            draw(buffersMain, shaderProgramMain);
        }
    }

    private void renderPostProcessing() {
        glUseProgram(shaderProgramPost);
        glBindFramebuffer(GL_FRAMEBUFFER, 0); // render to window
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glViewport(0,0, LwjglWindow2.WIDTH, LwjglWindow2.HEIGHT);

        glUniform1f(filterLocation, showFilter);
        glUniform1f(timeLocationFilter, moving);

        renderTarget.getColorTexture().bind(shaderProgramPost, "textureRendered", 0);

        draw(buffersPost, shaderProgramMain);
    }

    private void clickKey(int key) {
        switch (key) {
            case GLFW_KEY_R: // RESET
                resetCamera();
                startToMove = false;
                showFilter = 1;
                showLight = false;
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
                break;
            case GLFW_KEY_T:
                if (colorType < 6) colorType++;
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
            case GLFW_KEY_7:
                if (lightPart > 4) lightPart++; // Part of light
                break;
            case GLFW_KEY_9:
                if (lightPart > 0) lightPart--;
                break;
            case GLFW_KEY_X:
                spotLight+=0.001;
                System.out.println(spotLight);
                break;
            case GLFW_KEY_C:
                spotLight-=0.001;
                System.out.println(spotLight);
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
            case GLFW_KEY_N:
                showFilter ^= 1;
                break;
            case GLFW_KEY_F:
                showTrianglesStrips = !showTrianglesStrips;
                generateGridOrTriangleStrip();
                break;
            case GLFW_KEY_B: // Toggle light
                showLight = !showLight;
                break;
            case GLFW_KEY_V:
                lightToMove = !lightToMove;
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
                        camera = camera.addAzimuth(Math.PI / 5 * (oldMx - x) / LwjglWindow2.WIDTH);
                        camera = camera.addZenith(Math.PI / 5 * (oldMy - y) / LwjglWindow2.HEIGHT);
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
                        System.err.println("Cursor not handled - " + buttonPressed);
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
            textRenderer.addStr2D(LwjglWindow2.WIDTH - 150, LwjglWindow2.HEIGHT - 10, "Mouse is pressed");
        }

        if (currentKey == GLFW_KEY_H) {
            int indexW = LwjglWindow2.WIDTH - 180;
            if (showHelp) {
                textRenderer.addStr2D(LwjglWindow2.WIDTH - 200, 20, "Help:");
                textRenderer.addStr2D(indexW, 40, "Q & E - fill / line / point");
                textRenderer.addStr2D(indexW, 60, "G - animate object");
                textRenderer.addStr2D(indexW, 80, "O - orthogonal");
                textRenderer.addStr2D(indexW, 100, "P - perspective");
                textRenderer.addStr2D(indexW, 120, "T & Y - Change color");
                textRenderer.addStr2D(indexW, 140, "K & L - Change textures");
                textRenderer.addStr2D(indexW, 160, "U & I - Change objects");
                textRenderer.addStr2D(indexW, 180, "N - Show (postprocessing) gray");
                textRenderer.addStr2D(indexW, 200, "M - show multiple objects");
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
        camera = new Camera().withPosition(new Vec3D(3, 3, 2)).withAzimuth(5 / 4f * Math.PI).withZenith(-1 / 5f * Math.PI);
    }

    private Mat4 setProjectionPerspective() {
        return new Mat4PerspRH(Math.PI / 3, LwjglWindow2.HEIGHT / (float) LwjglWindow2.WIDTH,0.1,15);
    }

    private Mat4 setProjectionOrthogonal() {
        return new  Mat4OrthoRH(3, 3, 0.1, 20);
    }

    private void draw(OGLBuffers buffer, int shaderProgram) {
        if (!showTrianglesStrips) {
            buffer.draw(GL_TRIANGLES, shaderProgram);
        } else {
            buffer.draw(GL_TRIANGLE_STRIP, shaderProgram);
        }
    }

    private void generateGridOrTriangleStrip() {
        if (!showTrianglesStrips) {
            buffersMain = GridFactory2.generateGrid(50, 50);
            buffersPost = GridFactory2.generateGrid(2, 2);
        } else {
            buffersMain = GridFactory2.generateTriangleStrips(50, 50);
            buffersPost = GridFactory2.generateTriangleStrips(2,2);
        }
    }

    void createBuffers() {
        float[] cube = {
                // bottom (z-) face
                1, 0, 0,	0, 0, -1,
                0, 0, 0,	0, 0, -1,
                1, 1, 0,	0, 0, -1,
                0, 1, 0,	0, 0, -1,
                // top (z+) face
                1, 0, 1,	0, 0, 1,
                0, 0, 1,	0, 0, 1,
                1, 1, 1,	0, 0, 1,
                0, 1, 1,	0, 0, 1,
                // x+ face
                1, 1, 0,	1, 0, 0,
                1, 0, 0,	1, 0, 0,
                1, 1, 1,	1, 0, 0,
                1, 0, 1,	1, 0, 0,
                // x- face
                0, 1, 0,	-1, 0, 0,
                0, 0, 0,	-1, 0, 0,
                0, 1, 1,	-1, 0, 0,
                0, 0, 1,	-1, 0, 0,
                // y+ face
                1, 1, 0,	0, 1, 0,
                0, 1, 0,	0, 1, 0,
                1, 1, 1,	0, 1, 0,
                0, 1, 1,	0, 1, 0,
                // y- face
                1, 0, 0,	0, -1, 0,
                0, 0, 0,	0, -1, 0,
                1, 0, 1,	0, -1, 0,
                0, 0, 1,	0, -1, 0
        };

        int[] indexBufferData = new int[36];
        for (int i = 0; i<6; i++){
            indexBufferData[i*6] = i*4;
            indexBufferData[i*6 + 1] = i*4 + 1;
            indexBufferData[i*6 + 2] = i*4 + 2;
            indexBufferData[i*6 + 3] = i*4 + 1;
            indexBufferData[i*6 + 4] = i*4 + 2;
            indexBufferData[i*6 + 5] = i*4 + 3;
        }
        OGLBuffers.Attrib[] attributes = {
                new OGLBuffers.Attrib("inPosition", 3),
                new OGLBuffers.Attrib("inNormal", 3)
        };

        buffersMain = new OGLBuffers(cube, attributes, indexBufferData);
        System.out.println(buffersMain.toString());
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