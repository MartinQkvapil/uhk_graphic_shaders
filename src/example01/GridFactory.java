package example01;

import lwjglutils.OGLBuffers;

public class GridFactory {

    /**
     * List of triplets
     * @param e - edges in row.
     * @param v - vertex in column.
     */
    static OGLBuffers generateGrid(int e, int v) {
        float[] vb = new float[e * v * 2];
        int index = 0;

        for (int i = 0; i < v; i++) {
            for (int j = 0; j < e; j++) {
                vb[index++] = j / (float) (e - 1);
                vb[index++] = i / (float) (v - 1);
            }
        }

        int[] ib = new int[2 * 3 * (e - 1) * (v - 1)];
        int index2 = 0;

        for (int i = 0; i < v - 1; i++) {
            int rowOffset = i * e;
            for (int j = 0; j < e - 1; j++) {
                ib[index2++] = j + rowOffset;
                ib[index2++] = j + e + rowOffset;
                ib[index2++] = j + 1 + rowOffset;

                ib[index2++] = j + 1 + rowOffset;
                ib[index2++] = j + e + rowOffset;
                ib[index2++] = j + e + 1 + rowOffset;
            }
        }

        OGLBuffers.Attrib[] attributes = {
                new OGLBuffers.Attrib("inPosition", 2)
        };
        return new OGLBuffers(vb, attributes, ib);
    }

    /**
     * Belt of triangles
     * @param e - edges in row.
     * @param v - vertex in column.
     */
    static OGLBuffers generateTriangleStrips(int e, int v) {
        float[] vb = new float[e * v * 2];
        int index = 0;

        for (int i = 0; i < v; i++) {
            for (int j = 0; j < e; j++) {
                vb[index++] = i / (float) (v - 1);
                vb[index++] = j / (float) (e - 1);
            }
        }

        int[] ib = new int[ v * e + (v-1) + (v-1) * (e-1) - 1 ];
        int index2 = 0;

        for (int r = 0; r < v - 1; r++) {
            int rowOffset = r * e;
            if(r % 2 == 0){
                for (int c = 0; c < e; c++) {
                    ib[index2++] = rowOffset + c;
                    ib[index2++] = rowOffset + c + e;
                    if(c == e - 1) ib[index2++] = rowOffset + c + e;
                }
            } else {
                for (int c2 = e - 1; c2 >= 0; c2--) {
                    ib[index2++] = rowOffset + c2;
                    ib[index2++] = rowOffset + c2 + e;
                    if(c2 == 0) ib[index2++] = rowOffset + c2 + e;
                }
            }
        }


        OGLBuffers.Attrib[] attributes = {
                new OGLBuffers.Attrib("inPosition", 2)
        };
        return new OGLBuffers(vb, attributes, ib);
    }
    
    public static void main(String[] args) {
        generateTriangleStrips(10,10);
    }
}
