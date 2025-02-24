//import java.util.*;
//import java.nio.FloatBuffer;
//import java.io.BufferedReader;
//import java.io.FileReader;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//import org.lwjgl.*;
//import org.lwjgl.BufferUtils;
//import org.lwjgl.glfw.*;
//import org.lwjgl.opengl.*;
//import org.lwjgl.system.*;
//import java.nio.*;
//import static org.lwjgl.glfw.GLFW.*;
//import static org.lwjgl.opengl.GL11.*;
//import static org.lwjgl.system.MemoryUtil.*;
//
//public class CelShading {
//    // The window handle
//    private long window;
//    private Model model;
//
//    public void run() {
//        System.out.println("Starting LWJGL " + Version.getVersion() + "!");
//        try {
//            init(); // Initialize GLFW and OpenGL
//
//            if (model == null) {
//                throw new RuntimeException("Model could not be loaded. Exiting...");
//            }
//
//            loop(); // Main rendering loop
//            cleanup(); // Free resources and terminate GLFW
//        } finally {
//            GLFWErrorCallback callback = glfwSetErrorCallback(null);
//            if (callback != null) {
//                callback.free();
//            }
//            glfwTerminate();
//        }
//    }
//
//    private void init() {
//        try {
//            model = loadModel("src/sphere.obj");
//            if (model == null) {
//                throw new RuntimeException("Failed to load the model.");
//            } else {
//                System.out.println("Model loaded successfully.");
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//            throw new RuntimeException("Error loading model", e);
//        }
//
//        if (!glfwInit()) {
//            throw new IllegalStateException("Unable to initialize GLFW");
//        }
//
//        glfwDefaultWindowHints();
//        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
//        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
//
//        window = glfwCreateWindow(800, 600, "Cel Shading with LWJGL", NULL, NULL);
//        if (window == NULL) {
//            throw new RuntimeException("Failed to create the GLFW window.");
//        } else {
//            System.out.println("GLFW window created successfully.");
//        }
//
//        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
//        glfwSetWindowPos(window, (vidmode.width() - 800) / 2, (vidmode.height() - 600) / 2);
//        glfwMakeContextCurrent(window);
//        glfwSwapInterval(1);
//        glfwShowWindow(window);
//    }
//
//    private void loop() {
//        GL.createCapabilities();
//
//        setupLighting();
//        setupMaterial();
//
//        glClearColor(0.0f, 1.0f, 1.0f, 0.0f);
//
//        while (!glfwWindowShouldClose(window)) {
//            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
//            setupCamera();
//
//            // Render the outlines (using silhouette detection, as before)
//            renderOutline(model);
//
//            // Render the model with smooth per-vertex lighting
//            renderModelSmooth(model);
//
//            glfwSwapBuffers(window);
//            glfwPollEvents();
//        }
//    }
//
//    private void cleanup() {
//        if (glfwSetKeyCallback(window, null) != null) {
//            glfwSetKeyCallback(window, null).free();
//        }
//        if (window != NULL) {
//            glfwDestroyWindow(window);
//        }
//    }
//
//    private void setPerspective(float fov, float aspectRatio, float near, float far) {
//        float fH = (float) Math.tan(Math.toRadians(fov / 2)) * near;
//        float fW = fH * aspectRatio;
//        glFrustum(-fW, fW, -fH, fH, near, far);
//    }
//
//    private void gluLookAt(float eyeX, float eyeY, float eyeZ,
//                           float centerX, float centerY, float centerZ,
//                           float upX, float upY, float upZ) {
//        FloatBuffer matrix = BufferUtils.createFloatBuffer(16);
//        float fx = centerX - eyeX;
//        float fy = centerY - eyeY;
//        float fz = centerZ - eyeZ;
//        float rlf = 1.0f / (float) Math.sqrt(fx * fx + fy * fy + fz * fz);
//        fx *= rlf; fy *= rlf; fz *= rlf;
//
//        float sx = upY * fz - upZ * fy;
//        float sy = upZ * fx - upX * fz;
//        float sz = upX * fy - upY * fx;
//        float rls = (float) Math.sqrt(sx * sx + sy * sy + sz * sz);
//        if (rls != 0) { sx /= rls; sy /= rls; sz /= rls; }
//
//        float ux = fy * sz - fz * sy;
//        float uy = fz * sx - fx * sz;
//        float uz = fx * sy - fy * sx;
//
//        matrix.put(new float[]{
//                sx, sy, sz, 0.0f,
//                ux, uy, uz, 0.0f,
//                -fx, -fy, -fz, 0.0f,
//                0.0f, 0.0f, 0.0f, 1.0f
//        });
//        matrix.flip();
//        glMultMatrixf(matrix);
//        glTranslatef(-eyeX, -eyeY, -eyeZ);
//    }
//
//    private void setupCamera() {
//        glMatrixMode(GL_PROJECTION);
//        glLoadIdentity();
//        float aspectRatio = 800.0f / 600.0f;
//        setPerspective(45.0f, aspectRatio, 0.1f, 100.0f);
//        glMatrixMode(GL_MODELVIEW);
//        glLoadIdentity();
//        // Camera at (0, 0, 10) looking at (0, 0, 0)
//        gluLookAt(0.0f, 0.0f, 10.0f,
//                0.0f, 0.0f, 0.0f,
//                0.0f, 1.0f, 0.0f);
//    }
//
//    public void setupLighting() {
//        glEnable(GL_LIGHTING);
//        glEnable(GL_LIGHT0);
//        glEnable(GL_COLOR_MATERIAL);
//        glColorMaterial(GL_FRONT, GL_AMBIENT_AND_DIFFUSE);
//
//        FloatBuffer ambientLight = BufferUtils.createFloatBuffer(4).put(new float[]{0.2f, 0.2f, 0.2f, 1.0f});
//        ambientLight.flip();
//        glLightfv(GL_LIGHT0, GL_AMBIENT, ambientLight);
//
//        FloatBuffer diffuseLight = BufferUtils.createFloatBuffer(4).put(new float[]{0.9f, 0.9f, 0.9f, 1.0f});
//        diffuseLight.flip();
//        glLightfv(GL_LIGHT0, GL_DIFFUSE, diffuseLight);
//
//        FloatBuffer lightPosition = BufferUtils.createFloatBuffer(4).put(new float[]{10.0f, 15.0f, 10.0f, 1.0f});
//        lightPosition.flip();
//        glLightfv(GL_LIGHT0, GL_POSITION, lightPosition);
//
//        FloatBuffer noSpecular = BufferUtils.createFloatBuffer(4).put(new float[]{0.0f, 0.0f, 0.0f, 1.0f});
//        noSpecular.flip();
//        glLightfv(GL_LIGHT0, GL_SPECULAR, noSpecular);
//
//        // Note: For smooth shading we now use GL_SMOOTH in renderModelSmooth.
//        glEnable(GL_DEPTH_TEST);
//        glDepthFunc(GL_LEQUAL);
//    }
//
//    public void setupMaterial() {
//        FloatBuffer matAmbient = BufferUtils.createFloatBuffer(4).put(new float[]{0.2f, 0.2f, 0.2f, 1.0f});
//        FloatBuffer matDiffuse = BufferUtils.createFloatBuffer(4).put(new float[]{0.8f, 0.8f, 0.8f, 1.0f});
//        FloatBuffer matSpecular = BufferUtils.createFloatBuffer(4).put(new float[]{1.0f, 1.0f, 1.0f, 1.0f});
//        float shininess = 32.0f;
//        matAmbient.flip();
//        matDiffuse.flip();
//        matSpecular.flip();
//        glMaterialfv(GL_FRONT, GL_AMBIENT, matAmbient);
//        glMaterialfv(GL_FRONT, GL_DIFFUSE, matDiffuse);
//        glMaterialfv(GL_FRONT, GL_SPECULAR, matSpecular);
//        glMaterialf(GL_FRONT, GL_SHININESS, shininess);
//    }
//
//    /**
//     * Renders the model with per-vertex lighting.
//     * Instead of computing one uniform color per face,
//     * this version uses vertex normals (from the OBJ file if available)
//     * to compute a diffuse light intensity per vertex.
//     * The colors are then interpolated smoothly across the face.
//     */
//    public void renderModelSmooth(Model model) {
//        // Enable smooth shading (interpolation)
//        glShadeModel(GL_SMOOTH);
//        glEnable(GL_LIGHTING);
//        glEnable(GL_DEPTH_TEST);
//        // Light direction (should match the setupLighting)
//        float[] lightDir = {10.0f, 15.0f, 10.0f};
//
//        glBegin(GL_TRIANGLES);
//        for (int[] face : model.faces) {
//            for (int i = 0; i < 3; i++) {
//                int vertexIndex = face[i * 3];
//                int normalIndex = face[i * 3 + 2];
//                float[] vertex = model.vertices.get(vertexIndex);
//                float[] normal = null;
//                if (normalIndex >= 0 && normalIndex < model.normals.size()) {
//                    normal = model.normals.get(normalIndex);
//                } else {
//                    // Fallback: compute face normal if no vertex normal is available
//                    normal = calculateFaceNormal(model, face);
//                }
//                // Compute diffuse light intensity at the vertex
//                float intensity = calculateDiffuseLight(normal, lightDir);
//                // Set the vertex color based on intensity (using a grayscale tone)
//                glColor3f(intensity, intensity, intensity);
//                glNormal3f(normal[0], normal[1], normal[2]);
//                glVertex3f(vertex[0], vertex[1], vertex[2]);
//            }
//        }
//        glEnd();
//    }
//
//    /**
//     * Renders toon outlines using silhouette detection.
//     * For each face, we compute the face center and view vector (from the face center to the camera)
//     * and then check the angle between the face normal and view vector.
//     * If the dot product is below a threshold, the face is considered part of the silhouette.
//     * Blending and line smoothing are enabled so that the outline edges blend smoothly.
//     */
//    public void renderOutline(Model model) {
//        glEnable(GL_BLEND);
//        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
//        glEnable(GL_LINE_SMOOTH);
//        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
//        glLineWidth(3.0f);
//        glColor3f(0.0f, 0.0f, 0.0f); // Outline color
//
//        // Camera position as defined in setupCamera (gluLookAt)
//        float[] cameraPos = {0.0f, 0.0f, 10.0f};
//        final float SILHOUETTE_THRESHOLD = 0.3f; // Cosine threshold
//        final float OUTLINE_OFFSET = 0.05f; // Extrusion amount for outline
//
//        for (int[] face : model.faces) {
//            float[] normal = calculateFaceNormal(model, face);
//            if (normal == null) continue;
//
//            // Compute face center as the average of its three vertices
//            float[] v0 = model.vertices.get(face[0]);
//            float[] v1 = model.vertices.get(face[3]);
//            float[] v2 = model.vertices.get(face[6]);
//            float centerX = (v0[0] + v1[0] + v2[0]) / 3.0f;
//            float centerY = (v0[1] + v1[1] + v2[1]) / 3.0f;
//            float centerZ = (v0[2] + v1[2] + v2[2]) / 3.0f;
//
//            // Compute view vector from face center to camera
//            float viewX = cameraPos[0] - centerX;
//            float viewY = cameraPos[1] - centerY;
//            float viewZ = cameraPos[2] - centerZ;
//            float viewLength = (float) Math.sqrt(viewX * viewX + viewY * viewY + viewZ * viewZ);
//            if (viewLength == 0) continue;
//            viewX /= viewLength; viewY /= viewLength; viewZ /= viewLength;
//
//            // Dot product between face normal and view vector
//            float dot = normal[0] * viewX + normal[1] * viewY + normal[2] * viewZ;
//            if (dot < SILHOUETTE_THRESHOLD) {
//                glBegin(GL_LINE_LOOP);
//                for (int i = 0; i < 3; i++) {
//                    int vertexIndex = face[i * 3];
//                    float[] vertex = model.vertices.get(vertexIndex);
//                    glVertex3f(vertex[0] + normal[0] * OUTLINE_OFFSET,
//                            vertex[1] + normal[1] * OUTLINE_OFFSET,
//                            vertex[2] + normal[2] * OUTLINE_OFFSET);
//                }
//                glEnd();
//            }
//        }
//        glDisable(GL_LINE_SMOOTH);
//        glDisable(GL_BLEND);
//    }
//
//    private float[] calculateFaceNormal(Model model, int[] face) {
//        if (face.length < 9)
//            return null;
//        float[] v0 = model.vertices.get(face[0]);
//        float[] v1 = model.vertices.get(face[3]);
//        float[] v2 = model.vertices.get(face[6]);
//        float[] edge1 = {v1[0] - v0[0], v1[1] - v0[1], v1[2] - v0[2]};
//        float[] edge2 = {v2[0] - v0[0], v2[1] - v0[1], v2[2] - v0[2]};
//        float[] normal = {
//                edge1[1] * edge2[2] - edge1[2] * edge2[1],
//                edge1[2] * edge2[0] - edge1[0] * edge2[2],
//                edge1[0] * edge2[1] - edge1[1] * edge2[0]
//        };
//        float length = (float) Math.sqrt(normal[0]*normal[0] + normal[1]*normal[1] + normal[2]*normal[2]);
//        if (length == 0) {
//            System.out.println("Invalid normal length (zero) for face: " + Arrays.toString(face));
//            return new float[]{0, 0, 1};
//        }
//        return new float[]{normal[0] / length, normal[1] / length, normal[2] / length};
//    }
//
//    private float calculateDiffuseLight(float[] normal, float[] lightDir) {
//        float magnitude = (float) Math.sqrt(lightDir[0]*lightDir[0] +
//                lightDir[1]*lightDir[1] +
//                lightDir[2]*lightDir[2]);
//        float[] normalizedLightDir = {
//                lightDir[0] / magnitude,
//                lightDir[1] / magnitude,
//                lightDir[2] / magnitude
//        };
//        float dotProduct = normal[0]*normalizedLightDir[0] +
//                normal[1]*normalizedLightDir[1] +
//                normal[2]*normalizedLightDir[2];
//        return Math.max(0, dotProduct);
//    }
//
//    public static void main(String[] args) {
//        new CelShading().run();
//    }
//
//    public static class Model {
//        public List<float[]> vertices;
//        public List<float[]> normals;
//        public List<int[]> faces;
//        public Model(List<float[]> vertices, List<float[]> normals, List<int[]> faces) {
//            this.vertices = vertices;
//            this.normals = normals;
//            this.faces = faces;
//        }
//    }
//
//    public static Model loadModel(String filename) throws IOException {
//        List<float[]> vertices = new ArrayList<>();
//        List<float[]> normals = new ArrayList<>();
//        List<int[]> faces = new ArrayList<>();
//        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
//            String line;
//            while ((line = reader.readLine()) != null) {
//                String[] parts = line.split("\\s+");
//                if (parts.length == 0) {
//                    continue;
//                }
//                switch (parts[0]) {
//                    case "v":
//                        if (parts.length < 4) {
//                            System.out.println("Invalid vertex definition: " + line);
//                            continue;
//                        }
//                        float[] vertex = new float[]{
//                                Float.parseFloat(parts[1]),
//                                Float.parseFloat(parts[2]),
//                                Float.parseFloat(parts[3])
//                        };
//                        vertices.add(vertex);
//                        break;
//                    case "vn":
//                        if (parts.length < 4) {
//                            System.out.println("Invalid normal definition: " + line);
//                            continue;
//                        }
//                        float[] normal = new float[]{
//                                Float.parseFloat(parts[1]),
//                                Float.parseFloat(parts[2]),
//                                Float.parseFloat(parts[3])
//                        };
//                        normals.add(normal);
//                        break;
//                    case "f":
//                        int[] face = new int[9]; // Each face: 3 vertices (vertex index, texture index, normal index)
//                        for (int i = 1; i <= 3; i++) {
//                            String[] faceParts = parts[i].split("/");
//                            int vertexIndex = Integer.parseInt(faceParts[0]) - 1;
//                            if (vertexIndex < 0 || vertexIndex >= vertices.size()) {
//                                System.out.println("Invalid vertex index in OBJ file: " + vertexIndex);
//                                continue;
//                            }
//                            face[(i - 1) * 3] = vertexIndex;
//                            if (faceParts.length == 3) {
//                                int normalIndex = Integer.parseInt(faceParts[2]) - 1;
//                                if (normalIndex < 0 || normalIndex >= normals.size()) {
//                                    System.out.println("Invalid normal index in OBJ file: " + normalIndex);
//                                    continue;
//                                }
//                                face[(i - 1) * 3 + 2] = normalIndex;
//                            }
//                        }
//                        faces.add(face);
//                        break;
//                    default:
//                        break;
//                }
//            }
//        }
//        if (vertices.isEmpty()) {
//            System.out.println("No vertices were loaded from the OBJ file.");
//            return null;
//        }
//        if (faces.isEmpty()) {
//            System.out.println("No faces were loaded from the OBJ file.");
//            return null;
//        }
//        return new Model(vertices, normals, faces);
//    }
//}
//
