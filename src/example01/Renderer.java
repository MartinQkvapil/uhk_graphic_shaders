package example01;

import lwjglutils.*;
import org.lwjgl.glfw.*;
import transforms.*;

import java.awt.*;
import java.io.IOException;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11C.glClear;
import static org.lwjgl.opengl.GL11C.glClearColor;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;


public class Renderer extends AbstractRenderer{

    private int shaderProgramMain, shaderProgramPost;
    private OGLBuffers buffersMain;
    private int viewLocation;
    private int projectionLocation;
    private Camera camera;
    private Mat4 projection;
    private OGLTexture2D textureMosaic;
    private OGLBuffers buffersPost;

    // Controls
    private boolean cursorPressed = false;
    private int buttonPressed;
    private double oldMx, oldMy;
    private OGLRenderTarget renderTarget;
    private OGLTexture2D.Viewer viewer;
    private int typeLocation;


    @Override
    public void init() {
        OGLUtils.printOGLparameters();
        OGLUtils.printLWJLparameters();
        OGLUtils.printJAVAparameters();
        OGLUtils.shaderCheck();

        glClearColor(0.1f, 0.1f, 0.1f, 1f);
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

        shaderProgramMain = ShaderUtils.loadProgram("/example01/main");
        viewLocation = glGetUniformLocation(shaderProgramMain, "view");
        projectionLocation = glGetUniformLocation(shaderProgramMain, "projection");
        typeLocation = glGetUniformLocation(shaderProgramMain, "type");

        shaderProgramPost = ShaderUtils.loadProgram("/example01/post");


        // DATA
        camera = new Camera().withPosition(new Vec3D(2, 2, 2)).withAzimuth(5 / 4f * Math.PI).withZenith(-1 / 5f * Math.PI);

        projection = new Mat4PerspRH(
                Math.PI / 3,
                LwjglWindow.HEIGHT / (float) LwjglWindow.WIDTH,
                0.1,
                20
        );


        buffersMain = GridFactory.generateGrid(50,50);
        buffersPost = GridFactory.generateGrid(2,2);

        renderTarget = new OGLRenderTarget(1024, 1024);

        loadTextures();

        try {
           textureMosaic = new OGLTexture2D("./mosaic.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }

        viewer = new OGLTexture2D.Viewer();
        textRenderer = new OGLTextRenderer(LwjglWindow.WIDTH, LwjglWindow.HEIGHT);
        textRenderer = new OGLTextRenderer(LwjglWindow.WIDTH, LwjglWindow.HEIGHT);
        textRenderer.setBackgroundColor(new Color(0.0f, 0.0f, 0.0f, 0.0f));
    }

    private void loadTextures() {
    }

    @Override
    public void display() {
        glEnable(GL_DEPTH_TEST); // Text renderer closing ZBuffer!
        renderMain();
        renderPostProcessing();

        glDisable(GL_DEPTH_TEST);
        viewer.view(textureMosaic, -1, -1, 0.5);
        viewer.view(renderTarget.getColorTexture(), -1, -0.5, 0.5);
        viewer.view(renderTarget.getDepthTexture(), -1, 0, 0.5);

        text();
    }

    private void renderMain() {
        glUseProgram(shaderProgramMain);
        renderTarget.bind();

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glUniformMatrix4fv(viewLocation, false,  camera.getViewMatrix().floatArray());
        glUniformMatrix4fv(projectionLocation, false, projection.floatArray());

        textureMosaic.bind(shaderProgramMain, "textureMosaic", 0);

        glUniform1f(typeLocation, 0f);
        buffersMain.draw(GL_TRIANGLES, shaderProgramMain);

        glUniform1f(typeLocation, 1f);
        buffersMain.draw(GL_TRIANGLES, shaderProgramMain);
    }

    private void renderPostProcessing() {
        glUseProgram(shaderProgramPost);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glViewport(0,0, LwjglWindow.WIDTH, LwjglWindow.HEIGHT);

        renderTarget.getColorTexture().bind(shaderProgramPost, "textureRendered", 0);
        buffersPost.draw(GL_TRIANGLES, shaderProgramPost);


    }



//	private GLFWKeyCallback   keyCallback = new GLFWKeyCallback() {
//		@Override
//		public void invoke(long window, int key, int scancode, int action, int mods) {
//		}
//	};
//
//    private GLFWWindowSizeCallback wsCallback = new GLFWWindowSizeCallback() {
//        @Override
//        public void invoke(long window, int w, int h) {
//        }
//    };
//
//    private GLFWMouseButtonCallback mbCallback = new GLFWMouseButtonCallback () {
//		@Override
//		public void invoke(long window, int button, int action, int mods) {
//		}
//
//	};

//
//    private GLFWScrollCallback scrollCallback = new GLFWScrollCallback() {
//        @Override public void invoke (long window, double dx, double dy) {
//        }
//    };


    private final GLFWCursorPosCallback cursorPosCallback = new GLFWCursorPosCallback() {
        @Override
        public void invoke(long window, double x, double y) {
            if (cursorPressed) {
                switch (buttonPressed) {
                    case GLFW_MOUSE_BUTTON_LEFT:
                        System.out.println("Left button to move click.");
                        camera = camera.addAzimuth(Math.PI / 10 * (oldMx - x) / LwjglWindow.WIDTH);
                        camera = camera.addZenith(Math.PI / 10 * (oldMy - y) / LwjglWindow.HEIGHT);
                        oldMx = x;
                        oldMy = y;
                        break;
                    default:
                        System.err.println("ALERT - Undetected button pressed");
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
            cursorPressed = action == GLFW_PRESS;
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

    private void text() {
        textRenderer.addStr2D(10, 20, "GPU: " + glGetString(GL_RENDERER));

        if (cursorPressed) textRenderer.addStr2D(width - 150,50, "Mouse is pressed");
    }



}