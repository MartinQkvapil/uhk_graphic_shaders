package example01;

import lwjglutils.OGLBuffers;
import lwjglutils.OGLUtils;
import lwjglutils.ShaderUtils;
import org.lwjgl.glfw.*;
import transforms.Camera;
import transforms.Mat4PerspRH;
import transforms.Vec3D;

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
    private Mat4PerspRH projection;

    @Override
    public void init() {
        OGLUtils.printOGLparameters();
        OGLUtils.printLWJLparameters();
        OGLUtils.printJAVAparameters();
        OGLUtils.shaderCheck();

        glClearColor(0.1f, 0.1f, 0.1f, 1f);

        shaderProgramMain = ShaderUtils.loadProgram("/example01/main");

        // LOCATORS
        viewLocation = glGetUniformLocation(shaderProgramMain, "view");
        projectionLocation = glGetUniformLocation(shaderProgramMain, "projection");

        // DATA
        camera = new Camera()
                .withPosition(new Vec3D(5,5,5))
                .withAzimuth(5 / 4f * Math.PI)
                .withZenith(-1 / 5f * Math.PI);

        projection = new Mat4PerspRH(
                Math.PI / 3,
                LwjglWindow.HEIGHT / (float) LwjglWindow.WIDTH,
                0.1,
                50
        );

//        float[] vertexBufferData = { // color
//                -1, -1, 	0.7f, 0, 0,
//                1,  0,		0, 0.7f, 0,
//                0,  1,		0, 0, 0.7f
//        };
        float[] vertexBufferData = { // color
                -1, -1,
                1,  0,
                0,  1,
        };
        int[] indexBufferData = { 0, 1, 2 };

        // vertex binding description, concise version
        OGLBuffers.Attrib[] attributes = {
                new OGLBuffers.Attrib("inPosition", 2),
//                new OGLBuffers.Attrib("inColor", 3) // 3 floats
        };
        buffers = new OGLBuffers(vertexBufferData, attributes, indexBufferData);
    }

    @Override
    public void display() {
        glUseProgram(shaderProgramMain);

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glUniform4fv(viewLocation, camera.getViewMatrix().floatArray());
        glUniform4fv(projectionLocation, camera.getViewMatrix().floatArray());

        buffers.draw(GL_TRIANGLES, shaderProgramMain);
    }

	private GLFWKeyCallback   keyCallback = new GLFWKeyCallback() {
		@Override
		public void invoke(long window, int key, int scancode, int action, int mods) {
		}
	};
    
    private GLFWWindowSizeCallback wsCallback = new GLFWWindowSizeCallback() {
        @Override
        public void invoke(long window, int w, int h) {
        }
    };
    
    private GLFWMouseButtonCallback mbCallback = new GLFWMouseButtonCallback () {
		@Override
		public void invoke(long window, int button, int action, int mods) {
		}
		
	};
	
    private GLFWCursorPosCallback cpCallbacknew = new GLFWCursorPosCallback() {
        @Override
        public void invoke(long window, double x, double y) {
    	}
    };
    
    private GLFWScrollCallback scrollCallback = new GLFWScrollCallback() {
        @Override public void invoke (long window, double dx, double dy) {
        }
    };


    private boolean mousePressed = false;
    private double oldMx, oldMy;

	@Override
	public GLFWKeyCallback getKeyCallback() {
		return keyCallback;
	}

	@Override
	public GLFWWindowSizeCallback getWsCallback() {
		return wsCallback;
	}

	@Override
	public GLFWMouseButtonCallback getMouseCallback() {
		return mbCallback;
	}

	@Override
	public GLFWCursorPosCallback getCursorCallback() {
		return cpCallbacknew;
	}

	@Override
	public GLFWScrollCallback getScrollCallback() {
		return scrollCallback;
	}



}