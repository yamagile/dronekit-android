package org.droidplanner.sample.hellodrone;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.opengl.GLES11;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.util.Log;

public class MyRenderer implements GLSurfaceView.Renderer {
    float rx=0;
    private final float[] eyes = new float[3];
    private float latitudeF = 0;
    private float longitudeF = 0;
    private float altitudeF = 0;
    private float[] positions = new float[]{0, 0, 0};
    public void onDrawFrame(GL10 gl10) {
        // バッファクリア
        GLES11.glClear(GLES11.GL_COLOR_BUFFER_BIT | GLES11.GL_DEPTH_BUFFER_BIT);

        //物体データ
        float mVertices[] = { 0, 1, 0, -1, 0, 0,  1, 0, 0, };
        float mNormals[]  = { 0, 0, 1,  0, 0, 1,  0, 0, 1, };

        FloatBuffer vertBuf = ByteBuffer.allocateDirect(positions.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertBuf.put(positions).position(0);
        GLES11.glVertexPointer(3,GLES11.GL_FLOAT,0,vertBuf);

        //物体の法線を転送
        GLES11.glMaterialfv(GLES11.GL_FRONT_AND_BACK, GLES11.GL_DIFFUSE,
                new float[]{1,1,0,0},0);

        //カメラの設定
        GLES11.glMatrixMode(GLES11.GL_MODELVIEW);
        GLES11.glLoadIdentity();
        Log.i("MyRenderer", "eyepos[0]=" + eyes[0] + ", eyepos[1]=" + eyes[1] + ", eyepos[2]=" +  eyes[2]);
        GLU.gluLookAt(gl10, eyes[0], eyes[1], eyes[2], 0,0,0, 0,1,0);

        //物体の配置を決める
        GLES11.glRotatef(0, 1.0f, 0.0f, 0.0f);

        //描画する
        GLES11.glColor4f(1.0f, 0.0f, 0.0f, 1.0f);
        GLES11.glPointSize(10.0f);
        GLES11.glDrawArrays(GLES11.GL_POINTS,0,positions.length/3);
    }

    public void onSurfaceChanged(GL10 gl10, int mWidth, int mHeight) {
        // ビューポート設定
        GLES11.glViewport(0, 0, mWidth, mHeight);

        // 透視変換設定
        GLES11.glMatrixMode(GLES11.GL_PROJECTION);
        GLES11.glLoadIdentity();
        GLES11.glFrustumf(-1,1,-1,1,1f,100f);
    }
    public void onSurfaceCreated(GL10 arg0, EGLConfig arg1) {
        // 各配列の有効化
        GLES11.glEnableClientState(GLES11.GL_VERTEX_ARRAY);
        GLES11.glEnableClientState(GLES11.GL_NORMAL_ARRAY);

        // カリング設定
        GLES11.glEnable(GLES11.GL_CULL_FACE);
        GLES11.glFrontFace(GLES11.GL_CCW);

        eyes[0] = 0;
        eyes[1] = 0;
        eyes[2] = 3;
    }
    // movement of camera
    public synchronized void setMotion(float xDistance, float yDistance) {
        eyes[0] += xDistance * 0.01;
        eyes[1] += yDistance * 0.01;
    }

    // movement of position
    public synchronized void setPositions(float latitude, float longitude, float altitude) {
        if (latitudeF == 0 ) latitudeF = latitude;
        if (longitudeF == 0 ) longitudeF = longitude;
        if (altitudeF == 0 ) altitudeF = altitude;
        if (latitudeF != latitude || longitudeF != longitude || altitudeF != altitude) {
            int N = positions.length;
            if (Math.abs(positions[N-3] - (latitude - latitudeF) / 0.00002F) > 0.2F ||
                Math.abs(positions[N-1] - (longitude - longitudeF) / 0.00002F) > 0.2F ||
                Math.abs(positions[N-2] - (altitude - altitudeF) / 10.00F) > 0.2F) {
                positions = Arrays.copyOf(positions, N + 3);
                positions[N] = (latitude - latitudeF) / 0.00002F;
                positions[N + 2] = (longitude - longitudeF) / 0.00002F;
                positions[N + 1] = (altitude - altitudeF) / 10.00F;
//                Log.i("MyRenderer", "positions[N]=" + positions[N] + ", positions[N+1]=" + positions[N + 1] + ", positions[N+2]=" + positions[N + 2]);
            }
        }
    }

    public synchronized void clearGLView() {
        latitudeF = 0;
        longitudeF = 0;
        altitudeF = 0;
        positions = new float[]{0, 0, 0};
    }
}