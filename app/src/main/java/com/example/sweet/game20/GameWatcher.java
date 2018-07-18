package com.example.sweet.game20;

import android.app.Activity;
import android.app.ActivityManager;
import android.opengl.GLSurfaceView;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.os.Bundle;
import android.os.Build;
import android.graphics.Point;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;

import static android.view.View.SYSTEM_UI_FLAG_FULLSCREEN;
import static android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
import static android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;

public class GameWatcher extends Activity
{
    private GLSurfaceView glSurfaceView;

    private boolean isRendererSet = false;

    private GameRenderer gameRender;

    private static final int INVALID_POINTER_ID = -1;

    private int
            movementPointerId = INVALID_POINTER_ID,
            shootingPointerId = INVALID_POINTER_ID;

    private boolean
            movementDown = false,
            shootingDown = false,
            pause = false;

    private long lastTapShooting = 0;
    private long lastTapMoving = 0;
    private long pauseCoolDownLength = 800;
    private long pauseCoolDownStart = 0;
    private static final long doubleTapLength = 300;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        glSurfaceView = new GLSurfaceView(this);

        final ActivityManager activityManager =
                (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo =
                activityManager.getDeviceConfigurationInfo();

        final boolean supportsEs2 =
                configurationInfo.reqGlEsVersion >= 0x20000
                        || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
                        && (Build.FINGERPRINT.startsWith("generic")
                        || Build.FINGERPRINT.startsWith("unknown")
                        || Build.MODEL.contains("google_sdk")
                        || Build.MODEL.contains("Emulator")
                        || Build.MODEL.contains("Android SDK built for x86"))
                );

        if(supportsEs2)
        {
            glSurfaceView.setEGLContextClientVersion(2);

            isRendererSet = true;
        }
        else
        {
            return;
        }

        Display display = getWindowManager().getDefaultDisplay();


        Point size = new Point();
        display.getSize(size);

        getWindow().getDecorView().setSystemUiVisibility(
                        SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                        SYSTEM_UI_FLAG_FULLSCREEN |
                        SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );

        gameRender = new GameRenderer(this);
        gameRender.setExitListener(new GameRenderer.ExitListener()
        {
            @Override
            public void onExit()
            {
                finish();
                //System.exit(0);
            }
        });

        glSurfaceView.setEGLConfigChooser(8,8,8,8,16,0);
        glSurfaceView.setPreserveEGLContextOnPause(true);
        glSurfaceView.setRenderer(gameRender);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        //glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        glSurfaceView.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {

                final int action = event.getAction();
                final int pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK)
                        >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                final int pointerId = event.getPointerId(pointerIndex);

                switch (action & MotionEvent.ACTION_MASK)
                {
                    case MotionEvent.ACTION_DOWN:
                    {
                        if (System.currentTimeMillis() - pauseCoolDownStart > pauseCoolDownLength)
                        {
                            if (System.currentTimeMillis() - lastTapMoving < doubleTapLength)
                            {
                                if (!pause)
                                {
                                    pause = true;
                                    gameRender.inGamePause();
                                    pauseCoolDownStart = System.currentTimeMillis();
                                }
                            }
                            lastTapMoving = System.currentTimeMillis();
                        }

                        movementPointerId = event.getPointerId(0);
                        final float normX = ((event.getX(event.findPointerIndex(movementPointerId)) / v.getWidth()) * 2 - 1) / gameRender.xScale;
                        final float normY = ((event.getY(event.findPointerIndex(movementPointerId)) / v.getHeight()) * 2 - 1) / gameRender.yScale;

                        if(!pause)
                        {
                            movementDown = true;

                            gameRender.ui.setMovementDown(true);
                            gameRender.player1.movementDown = true;

                            gameRender.ui.movementOnDown.set(normX, normY);
                            gameRender.ui.movementOnMove.set(normX, normY);
                            gameRender.player1.movementOnDownX = normX;
                            gameRender.player1.movementOnDownY = normY;
                            gameRender.player1.movementOnMoveX = normX;
                            gameRender.player1.movementOnMoveY = normY;
                        }
                        else
                        {
                            gameRender.ui.menuPointerDown = true;
                            gameRender.ui.menuOnDown.set(normX, normY);
                            gameRender.ui.menuOnMove.set(normX, normY);
                            gameRender.ui.pointerDown();
                            //gameRender.triggerActionDown();
                        }
                        break;
                    }
                    case MotionEvent.ACTION_UP:
                    {
                        if(!pause)
                        {
                            if(movementDown)
                            {
                                movementPointerId = event.INVALID_POINTER_ID;
                                movementDown = false;
                                gameRender.ui.setMovementDown(false);
                                gameRender.player1.movementDown = false;
                            }
                            if(shootingDown)
                            {
                                shootingPointerId = event.INVALID_POINTER_ID;
                                shootingDown = false;
                                gameRender.ui.setShootingDown(false);
                                gameRender.player1.shootingDown = false;
                            }
                        }
                        else
                        {
                            if(gameRender.ui.menuPointerDown)
                            {
                                final float normX = ((event.getX() / v.getWidth()) * 2 - 1) / gameRender.xScale;
                                final float normY = ((event.getY() / v.getHeight()) * 2 - 1) / gameRender.yScale;
                                gameRender.ui.menuPointerDown = false;
                                gameRender.ui.pointerUp();
                                if (System.currentTimeMillis() - pauseCoolDownStart > pauseCoolDownLength)
                                {
                                    pause = gameRender.ui.checkPause();
                                    if(!pause)
                                    {
                                        gameRender.inGameUnpause();
                                        pauseCoolDownStart = System.currentTimeMillis();
                                    }
                                }
                            }
                        }
                        break;
                    }
                    case MotionEvent.ACTION_POINTER_DOWN:
                    {
                        if(!pause)
                        {
                            if (movementDown && !shootingDown)
                            {
                                if (System.currentTimeMillis() - pauseCoolDownStart > pauseCoolDownLength)
                                {
                                    if (System.currentTimeMillis() - lastTapShooting < doubleTapLength)
                                    {
                                        if (!pause)
                                        {
                                            pause = true;
                                            gameRender.inGamePause();
                                            pauseCoolDownStart = System.currentTimeMillis();
                                        }
                                    }
                                    lastTapShooting = System.currentTimeMillis();
                                }

                                shootingPointerId = pointerId;
                                shootingDown = true;

                                gameRender.ui.setShootingDown(true);
                                gameRender.player1.shootingDown = true;

                                final float normX = ((event.getX(event.findPointerIndex(shootingPointerId)) / v.getWidth()) * 2 - 1) / gameRender.xScale;
                                final float normY = ((event.getY(event.findPointerIndex(shootingPointerId)) / v.getHeight()) * 2 - 1) / gameRender.yScale;

                                gameRender.ui.shootingOnDown.set(normX, normY);
                                gameRender.ui.shootingOnMove.set(normX, normY);
                                gameRender.player1.shootingOnDownX = normX;
                                gameRender.player1.shootingOnDownY = normY;
                                gameRender.player1.shootingOnMoveX = normX;
                                gameRender.player1.shootingOnMoveY = normY;
                            }
                            else if (shootingDown && !movementDown)
                            {
                                if (System.currentTimeMillis() - pauseCoolDownStart > pauseCoolDownLength)
                                {
                                    if (System.currentTimeMillis() - lastTapMoving < doubleTapLength)
                                    {
                                        if (!pause)
                                        {
                                            pause = true;
                                            gameRender.inGamePause();
                                            pauseCoolDownStart = System.currentTimeMillis();
                                        }
                                    }
                                }
                                lastTapMoving = System.currentTimeMillis();

                                movementPointerId = pointerId;
                                movementDown = true;

                                gameRender.ui.setMovementDown(true);
                                gameRender.player1.movementDown = true;

                                final float normX = ((event.getX(event.findPointerIndex(movementPointerId)) / v.getWidth()) * 2 - 1) / gameRender.xScale;
                                final float normY = ((event.getY(event.findPointerIndex(movementPointerId)) / v.getHeight()) * 2 - 1) / gameRender.yScale;

                                gameRender.ui.movementOnDown.set(normX, normY);
                                gameRender.ui.movementOnMove.set(normX, normY);
                                gameRender.player1.movementOnDownX = normX;
                                gameRender.player1.movementOnDownY = normY;
                                gameRender.player1.movementOnMoveX = normX;
                                gameRender.player1.movementOnMoveY = normY;
                            }
                        }
                        break;
                    }
                    case MotionEvent.ACTION_POINTER_UP:
                    {
                        if (pointerId == shootingPointerId && shootingDown)
                        {
                            shootingPointerId = event.INVALID_POINTER_ID;
                            shootingDown = false;
                            gameRender.ui.setShootingDown(false);
                            gameRender.player1.shootingDown = false;
                        }
                        else if (pointerId == movementPointerId && movementDown)
                        {
                            movementPointerId = event.INVALID_POINTER_ID;
                            movementDown = false;
                            //gameRender.movementDown = false;
                            gameRender.ui.setMovementDown(false);
                            gameRender.player1.movementDown = false;
                        }
                        break;
                    }
                    case MotionEvent.ACTION_MOVE:
                    {
                        // Find the index of the active pointer and fetch its position
                        if(!pause)
                        {
                            if (shootingDown)
                            {
                                //System.out.println("moving shooting--------------------------------------" + shootingPointerId);
                                final float normX = ((event.getX(event.findPointerIndex(shootingPointerId)) / v.getWidth()) * 2 - 1) / gameRender.xScale;
                                final float normY = ((event.getY(event.findPointerIndex(shootingPointerId)) / v.getHeight()) * 2 - 1) / gameRender.yScale;
                                gameRender.ui.shootingOnMove.set(normX, normY);
                                gameRender.player1.shootingOnMoveX = normX;
                                gameRender.player1.shootingOnMoveY = normY;
                            }
                            if (movementDown)
                            {
                                final float normX = ((event.getX(event.findPointerIndex(movementPointerId)) / v.getWidth()) * 2 - 1) / gameRender.xScale;
                                final float normY = ((event.getY(event.findPointerIndex(movementPointerId)) / v.getHeight()) * 2 - 1) / gameRender.yScale;
                                gameRender.ui.movementOnMove.set(normX, normY);
                                gameRender.player1.movementOnMoveX = normX;
                                gameRender.player1.movementOnMoveY = normY;
                            }
                        }
                        else
                        {
                            final float normX = ((event.getX() / v.getWidth()) * 2 - 1) / gameRender.xScale;
                            final float normY = ((event.getY() / v.getHeight()) * 2 - 1) / gameRender.yScale;
                            gameRender.ui.menuOnMove.set(normX, normY);
                        }
                        break;
                    }
                }

                return true;
            }
        });

        if(isRendererSet)
        {
            setContentView(glSurfaceView);
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        System.out.println("PAUSED");
        gameRender.pauseThreads();
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        if (isRendererSet)
        {
            glSurfaceView.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().getDecorView().setSystemUiVisibility(
                SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                        SYSTEM_UI_FLAG_FULLSCREEN |
                        SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        gameRender.unpauseThreads();
        if (isRendererSet) {
            glSurfaceView.onResume();
        }
    }
}
