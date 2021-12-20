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
        //物体データ
//        float f0=0.000000f,f1=1.000000f,f2=0.723600f,f3=0.447215f,f4=0.525720f;
//        float f5=0.276385f,f6=0.850640f,f7=0.894425f,f8=0.187597f,f9=0.794651f;
//        float fa=0.577354f,fb=0.607065f,fc=0.794652f,fd=0.491122f,fe=0.356829f;
//        float ff=0.982246f,fg=0.303536f,fh=0.187589f,fi=0.934171f,fj=0.794649f;
//        float fk=0.187587f,fl=0.577359f;
//        float mVertices[]={
//                f0,-f1, f0, f2,-f3, f4,-f5,-f3, f6, f2,-f3, f4, f0,-f1, f0, f2,-f3,-f4,
//                f0,-f1, f0,-f5,-f3, f6,-f7,-f3, f0, f0,-f1, f0,-f7,-f3, f0,-f5,-f3,-f6,
//                f0,-f1, f0,-f5,-f3,-f6, f2,-f3,-f4, f2,-f3, f4, f2,-f3,-f4, f7, f3, f0,
//                -f5,-f3, f6, f2,-f3, f4, f5, f3, f6,-f7,-f3, f0,-f5,-f3, f6,-f2, f3, f4,
//                -f5,-f3,-f6,-f7,-f3, f0,-f2, f3,-f4, f2,-f3,-f4,-f5,-f3,-f6, f5, f3,-f6,
//                f2,-f3, f4, f7, f3, f0, f5, f3, f6,-f5,-f3, f6, f5, f3, f6,-f2, f3, f4,
//                -f7,-f3, f0,-f2, f3, f4,-f2, f3,-f4,-f5,-f3,-f6,-f2, f3,-f4, f5, f3,-f6,
//                f2,-f3,-f4, f5, f3,-f6, f7, f3, f0, f5, f3, f6, f7, f3, f0, f0, f1, f0,
//                -f2, f3, f4, f5, f3, f6, f0, f1, f0,-f2, f3,-f4,-f2, f3, f4, f0, f1, f0,
//                f5, f3,-f6,-f2, f3,-f4, f0, f1, f0, f7, f3, f0, f5, f3,-f6, f0, f1, f0,
//        };
//        float mNormals[]={
//                f8,-f9, fa, f8,-f9, fa, f8,-f9, fa, fb,-fc, f0, fb,-fc, f0, fb,-fc, f0,
//                -fd,-fc, fe,-fd,-fc, fe,-fd,-fc, fe,-fd,-fc,-fe,-fd,-fc,-fe,-fd,-fc,-fe,
//                f8,-f9,-fa, f8,-f9,-fa, f8,-f9,-fa, ff,-f8, f0, ff,-f8, f0, ff,-f8, f0,
//                fg,-fh, fi, fg,-fh, fi, fg,-fh, fi,-fj,-fk, fl,-fj,-fk, fl,-fj,-fk, fl,
//                -fj,-fk,-fl,-fj,-fk,-fl,-fj,-fk,-fl, fg,-fh,-fi, fg,-fh,-fi, fg,-fh,-fi,
//                fj, fk, fl, fj, fk, fl, fj, fk, fl,-fg, fh, fi,-fg, fh, fi,-fg, fh, fi,
//                -ff, f8, f0,-ff, f8, f0,-ff, f8, f0,-fg, fh,-fi,-fg, fh,-fi,-fg, fh,-fi,
//                fj, fk,-fl, fj, fk,-fl, fj, fk,-fl, fd, fc, fe, fd, fc, fe, fd, fc, fe,
//                -f8, f9, fa,-f8, f9, fa,-f8, f9, fa,-fb, fc, f0,-fb, fc, f0,-fb, fc, f0,
//                -f8, f9,-fa,-f8, f9,-fa,-f8, f9,-fa, fd, fc,-fe, fd, fc,-fe, fd, fc,-fe,
//        };
        //物体の頂点を転送
//        FloatBuffer vertBuf = ByteBuffer.allocateDirect(mVertices.length * 4)
//                .order(ByteOrder.nativeOrder()).asFloatBuffer();
//        vertBuf.put(mVertices).position(0);
//        GLES11.glVertexPointer(3,GLES11.GL_FLOAT,0,vertBuf);

        FloatBuffer vertBuf = ByteBuffer.allocateDirect(positions.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertBuf.put(positions).position(0);
        GLES11.glVertexPointer(3,GLES11.GL_FLOAT,0,vertBuf);

        //物体の法線を転送
//        FloatBuffer normalBuf = ByteBuffer.allocateDirect(mNormals.length * 4)
//                .order(ByteOrder.nativeOrder()).asFloatBuffer();
//        normalBuf.put(mNormals).position(0);
//        GLES11.glNormalPointer(GLES11.GL_FLOAT, 0, normalBuf);

        //物体の色を設定
//        GLES11.glMaterialfv(GLES11.GL_FRONT_AND_BACK, GLES11.GL_DIFFUSE,
//                new float[]{1,1,0,0},0);
//        GLES11.glMaterialfv(GLES11.GL_FRONT_AND_BACK, GLES11.GL_AMBIENT,
//                new float[]{.1f,.1f,.1f,.0f},0);
//        GLES11.glMaterialfv(GLES11.GL_FRONT_AND_BACK, GLES11.GL_SPECULAR,
//                new float[]{.5f,.5f,.5f,.5f},0);
//        GLES11.glMaterialfv(GLES11.GL_FRONT_AND_BACK, GLES11.GL_SHININESS,
//                new float[]{64.0f},0);
        GLES11.glMaterialfv(GLES11.GL_FRONT_AND_BACK, GLES11.GL_DIFFUSE,
                new float[]{1,1,0,0},0);

        //カメラの設定
        GLES11.glMatrixMode(GLES11.GL_MODELVIEW);
        GLES11.glLoadIdentity();
        Log.i("MyRenderer", "eyepos[0]=" + eyes[0] + ", eyepos[1]=" + eyes[1] + ", eyepos[2]=" +  eyes[2]);
        GLU.gluLookAt(gl10, eyes[0], eyes[1], eyes[2], 0,0,0, 0,1,0);

        //物体の配置を決める
        GLES11.glRotatef(0, 1.0f, 0.0f, 0.0f);
//        GLES11.glRotatef(rx+=1, 1.0f, 0.0f, 0.0f);

        //描画する
        GLES11.glColor4f(1.0f, 0.0f, 0.0f, 1.0f);
        GLES11.glPointSize(20.0f);
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

//        //ライトの設定
//        GLES11.glEnable(GLES11.GL_LIGHTING);
//        int light = GLES11.GL_LIGHT0;
//        GLES11.glEnable(light);
//        GLES11.glLightfv(light,GLES11.GL_POSITION, new float[]{.0f,.0f,.5f,.0f},0);
//        GLES11.glLightfv(light,GLES11.GL_DIFFUSE,  new float[]{.6f,.6f,.6f,.0f},0);
//        GLES11.glLightfv(light,GLES11.GL_AMBIENT,  new float[]{.6f,.6f,.6f,.0f},0);
//        GLES11.glLightfv(light,GLES11.GL_SPECULAR, new float[]{.5f,.5f,.5f,.0f},0);

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
                Log.i("MyRenderer", "positions[N]=" + positions[N] + ", positions[N+1]=" + positions[N + 1] + ", positions[N+2]=" + positions[N + 2]);
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