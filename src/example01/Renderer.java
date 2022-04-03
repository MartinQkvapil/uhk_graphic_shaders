package example01;

import lwjglutils.OGLBuffers;
import lwjglutils.OGLTexture2D;
import lwjglutils.OGLUtils;
import lwjglutils.ShaderUtils;
import org.lwjgl.glfw.*;
import transforms.*;

import java.io.IOException;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11C.glClear;
import static org.lwjgl.opengl.GL11C.glClearColor;
import static org.lwjgl.opengl.GL20.*;


public class Renderer extends AbstractRenderer{

    private int shaderProgramMain;
    private OGLBuffers buffers;
    private int viewLocation;
    private int projectionLocation;
    private Camera camera;
    private Mat4 projection;
    private OGLTexture2D textureMosaic;


    @Override
    public void init() {
        OGLUtils.printOGLparameters();
        OGLUtils.printLWJLparameters();
        OGLUtils.printJAVAparameters();
        OGLUtils.shaderCheck();

        glClearColor(0.1f, 0.1f, 0.1f, 1f);
        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);

        shaderProgramMain = ShaderUtils.loadProgram("/example01/main");

        // LOCATORS
        viewLocation = glGetUniformLocation(shaderProgramMain, "view");
        projectionLocation = glGetUniformLocation(shaderProgramMain, "projection");

        // DATA
        camera = new Camera()
                .withPosition(new Vec3D(3.5,3.5,3.5))
                .withAzimuth(5 / 4f * Math.PI)
                .withZenith(-1 / 5f * Math.PI);

        projection = new Mat4PerspRH(
                Math.PI / 3,
                LwjglWindow.HEIGHT / (float) LwjglWindow.WIDTH,
                0.1,
                20
        );
        buffers = GridFactory.generateGrid(50,50);
        try {
           textureMosaic = new OGLTexture2D("./mosaic.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void display() {
        glUseProgram(shaderProgramMain);

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glUniformMatrix4fv(viewLocation, false,  camera.getViewMatrix().floatArray());
        glUniformMatrix4fv(projectionLocation, false, projection.floatArray());

        textureMosaic.bind(shaderProgramMain, "textureMosaic", 0);

        buffers.draw(GL_TRIANGLES, shaderProgramMain);
    }


    // Controls
    private boolean mousePressed = false;
    private int pressedButton;
    private double oldMx, oldMy;


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
//    private GLFWCursorPosCallback cpCallbacknew = new GLFWCursorPosCallback() {
//        @Override
//        public void invoke(long window, double x, double y) {
//    	}
//    };
//
//    private GLFWScrollCallback scrollCallback = new GLFWScrollCallback() {
//        @Override public void invoke (long window, double dx, double dy) {
//        }
//    };


    private final GLFWCursorPosCallback cursorPosCallback = new GLFWCursorPosCallback() {
        @Override
        public void invoke(long window, double x, double y) {
            if (mousePressed) {
                switch (pressedButton) {
                    case GLFW_MOUSE_BUTTON_LEFT:
                        System.out.println("Mouse pressed.");
                        camera = camera.addAzimuth(Math.PI / 10 * (oldMx - x) / width);
                        camera = camera.addZenith(Math.PI / 10 * (oldMy - y) / height);
                        oldMx = x;
                        oldMy = y;
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
            pressedButton = button;
        }
    };


//    @Override
//    public GLFWKeyCallback getKeyCallback() { return keyCallback; }

    @Override
    public GLFWCursorPosCallback getCursorCallback() { return cursorPosCallback; }

    @Override
    public GLFWMouseButtonCallback getMouseCallback() { return mouseButtonCallback; }

//    @Override
//    public GLFWScrollCallback getScrollCallback() { return scrollCallback; }
//
//    @Override
//    public GLFWWindowSizeCallback getWsCallback() { return wsCallback; }



}