package com.esaul.snakegame;
import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private SurfaceView surfaceView;
    private SurfaceHolder holder;
    private Button startButton;
    private Handler handler = new Handler();
    private int cellSize;
    private int[][] field = new int[20][20];
    private ArrayList<int[]> snake = new ArrayList<>();
    private int directionX = 1, directionY = 0;
    private boolean isRunning = false;
    private float touchX, touchY;
    private Random random = new Random();
    private int score = 0;

    private Runnable gameRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRunning) {
                updateGame();
                drawGame();
                handler.postDelayed(this, 300);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        surfaceView = new SurfaceView(this);
        startButton = new Button(this);
        startButton.setText("Start / Restart");
        startButton.setVisibility(View.VISIBLE);

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                MainActivity.this.holder = holder;
            }
            @Override public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}
            @Override public void surfaceDestroyed(SurfaceHolder holder) {}
        });

        surfaceView.setOnTouchListener((v, event) -> handleSwipe(event));

        startButton.setOnClickListener(v -> startGame());

        setContentView(new android.widget.LinearLayout(this) {{
            setOrientation(VERTICAL);
            addView(surfaceView, new android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 0, 4));
            addView(startButton, new android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));
        }});

    }

    private void startGame() {
        handler.removeCallbacks(gameRunnable);

        startButton.setVisibility(View.GONE);

        for (int x = 0; x < 20; x++) {
            for (int y = 0; y < 20; y++) {
                field[x][y] = 0;
            }
        }

        snake.clear();
        snake.add(new int[]{10, 10});
        snake.add(new int[]{9, 10});
        snake.add(new int[]{8, 10});
        directionX = 1;
        directionY = 0;
        isRunning = true;
        spawnFood();
        handler.post(gameRunnable);
    }

    private void updateGame() {
        int[] head = snake.get(0);
        int newX = head[0] + directionX;
        int newY = head[1] + directionY;

        if (newX < 0 || newX >= 20 || newY < 0 || newY >= 20 || isSnakeCollision(newX, newY)) {
            isRunning = false;
            startButton.setVisibility(View.VISIBLE);
            return;
        }

        snake.add(0, new int[]{newX, newY});

        if (field[newX][newY] == 1) {
            field[newX][newY] = 0;
            score++;
            spawnFood();
        } else {
            snake.remove(snake.size() - 1);
        }
    }

    private boolean isSnakeCollision(int x, int y) {
        for (int i = 1; i < snake.size(); i++) {
            if (snake.get(i)[0] == x && snake.get(i)[1] == y) {
                return true;
            }
        }
        return false;
    }

    private void spawnFood() {
        int x, y;
        do {
            x = random.nextInt(20);
            y = random.nextInt(20);
        } while (isSnakeCollision(x, y));
        field[x][y] = 1;
    }

    private void drawGame() {
        if (holder == null) return;
        Canvas canvas = holder.lockCanvas();
        if (canvas == null) return;

        canvas.drawColor(Color.BLACK);

        int width = canvas.getWidth();
        int height = canvas.getHeight();


        int fieldWidth = (int) (width * 0.9);
        cellSize = fieldWidth / 20;
        int fieldHeight = cellSize * 20;

        int offsetX = (width - fieldWidth) / 2;
        int offsetY = 100; // Отступ сверху

        Paint paint = new Paint();

        int borderThickness = cellSize;


        paint.setColor(Color.GRAY);
        canvas.drawRect(
                offsetX - borderThickness,
                offsetY - borderThickness,
                offsetX + fieldWidth + borderThickness,
                offsetY,
                paint
        );
        canvas.drawRect(
                offsetX - borderThickness,
                offsetY + fieldHeight,
                offsetX + fieldWidth + borderThickness,
                offsetY + fieldHeight + borderThickness,
                paint
        );
        canvas.drawRect(
                offsetX - borderThickness,
                offsetY,
                offsetX,
                offsetY + fieldHeight,
                paint
        );
        canvas.drawRect(
                offsetX + fieldWidth,
                offsetY,
                offsetX + fieldWidth + borderThickness,
                offsetY + fieldHeight,
                paint
        );

        // змейка
        paint.setColor(Color.parseColor("#049101"));
        for (int[] part : snake) {
            canvas.drawRect(
                    offsetX + part[0] * cellSize,
                    offsetY + part[1] * cellSize,
                    offsetX + (part[0] + 1) * cellSize,
                    offsetY + (part[1] + 1) * cellSize,
                    paint
            );
        }

        // еда
        paint.setColor(Color.parseColor("#ab071b"));
        for (int x = 0; x < 20; x++) {
            for (int y = 0; y < 20; y++) {
                if (field[x][y] == 1) {
                    canvas.drawRect(
                            offsetX + x * cellSize,
                            offsetY + y * cellSize,
                            offsetX + (x + 1) * cellSize,
                            offsetY + (y + 1) * cellSize,
                            paint
                    );
                }
            }
        }
        paint.setColor(Color.WHITE);
        paint.setTextSize(50);
        paint.setTypeface(Typeface.MONOSPACE);

        canvas.drawText("Score: " + score, offsetX, offsetY + fieldHeight + 90, paint);

        holder.unlockCanvasAndPost(canvas);
    }

    @SuppressLint("ClickableViewAccessibility")
    private boolean handleSwipe(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchX = event.getX();
                touchY = event.getY();
                return true;
            case MotionEvent.ACTION_UP:
                float deltaX = event.getX() - touchX;
                float deltaY = event.getY() - touchY;

                if (Math.abs(deltaX) > Math.abs(deltaY)) {
                    if (deltaX > 0 && directionX != -1) {
                        directionX = 1; directionY = 0;
                    } else if (deltaX < 0 && directionX != 1) {
                        directionX = -1; directionY = 0;
                    }
                } else {
                    if (deltaY > 0 && directionY != -1) {
                        directionX = 0; directionY = 1;
                    } else if (deltaY < 0 && directionY != 1) {
                        directionX = 0; directionY = -1;
                    }
                }
                return true;
        }
        return false;
    }
}
