package com.homjay.longsandemo.view;

/**
 * @Author: taimin
 * @Date: 2021/4/26
 * @Description: 显示心电的控件
 * 1.删除原来画折线的代码
 * 2.新增折线、曲线模式
 * 3.修改Y轴初始值为0时不显示，实际把线条颜色设置透明
 */

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;

/**
 * @Author: taimin
 * @Date: 2021/4/26
 * @Description: 显示心电的控件
 * 1.删除原来画折线的代码
 * 2.新增折线、曲线模式
 */

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;

import com.homjay.longsandemo.R;

import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingDeque;


public class HeartView extends View {
    // 数据的最大值
    private int Max;
    // 数据的最小值
    private int Min;
    // 数据一秒钟采集频率，默认100个点一秒种
    private int hz;
    // 控件显示几秒钟的心跳,默认显示2秒钟的心跳
    private float showSeconds;
    // 要画的基准线
    private int baseLine;
    // 每个方格的行数
    private int grid_row;
    // 每个方格的高度
    private int grid_row_height;
    // 心率线条的颜色 默认红色
    private int heartColor;
    // 表格线条的颜色 默认灰色
    private int heart_grid_line_color;
    // 表格边框的颜色 默认灰色
    private int heart_grid_border_color;
    // 心电线的宽度
    private int heart_line_border;
    // 大表格的边框的宽度
    private int heart_grid_border;
    // 每个小格子的线的宽度
    private int heart_grid_line_border;
    // 速度控制
    private float heart_speed;
    //是否画直线或曲线
    private boolean isCurve;

    private int viewHeight = 0;
    private int viewWidth = 0;

    // 画笔
    private Paint paint;
    // 需要画心电的路径
    private Path path = new Path();
    // 根据显示秒数,以及采样频率算出总共需要申请多少个内存的数据
    private int[] showTimeDatas;
    // 待显示的数据队列
    private LinkedBlockingDeque<Integer> dataQueue = new LinkedBlockingDeque<>();
    // 定时运行栈
    private HeartTask heartTask = null;
    // 精准定时器
    private Timer timer = new Timer();

    /**
     * 重要参数，两点之间分为几段描画，数字愈大分段越多，描画的曲线就越精细.
     */
    private static final int STEPS = 12;
    /*Path linePath;
    Path curvePath;*/
    List<Point> points;
    List<Integer> points_x;
    List<Integer> points_y;

    public HeartView(Context context) {
        this(context, null);
    }

    public HeartView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HeartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setColor(Color.RED);
        // 线条交界处，钝化处理，看起来是圆点
        paint.setStrokeJoin(Paint.Join.ROUND);

        /*linePath = new Path();
        curvePath = new Path();*/
        points = new LinkedList<Point>();
        points_x = new LinkedList<Integer>();
        points_y = new LinkedList<Integer>();

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.HeartView);
        // 心电线的宽度
        heart_line_border = typedArray.getDimensionPixelSize(R.styleable.HeartView_heart_line_border, (int) dip2px(context, 1f));
        // 每个表格的行数（就是小格子数,默认5格
        grid_row = typedArray.getInt(R.styleable.HeartView_heart_grid_row, 5);
        // 大表格的边框的宽度
        heart_grid_border = typedArray.getDimensionPixelSize(R.styleable.HeartView_heart_grid_border, (int) dip2px(context, 2f));
        // 每个小格子的宽高
        grid_row_height = typedArray.getDimensionPixelSize(R.styleable.HeartView_heart_grid_row_height, (int) dip2px(context, 10f));
        // 每个小格子的线的宽度
        heart_grid_line_border = typedArray.getDimensionPixelSize(R.styleable.HeartView_heart_grid_line_border, (int) dip2px(context, 1f));
        // 基准线，默认2000
        baseLine = typedArray.getInteger(R.styleable.HeartView_heart_base_line, 2000);
        // 最大值，默认4000
        Max = typedArray.getInteger(R.styleable.HeartView_heart_max, 4096);
        // 最小值，默认0
        Min = typedArray.getInteger(R.styleable.HeartView_heart_min, 0);
        // 数据采集频率，默认100个点一秒钟
        hz = typedArray.getInteger(R.styleable.HeartView_heart_hz, 125);
        // 一个控件，可以显示的心率的时长 ,默认为2秒钟
        showSeconds = typedArray.getFloat(R.styleable.HeartView_heart_show_seconds, 2f);
        // 心率线条的颜色 默认红色
        heartColor = typedArray.getColor(R.styleable.HeartView_heart_color, Color.RED);
        // 表格线条的颜色 默认绿色
        heart_grid_line_color = typedArray.getColor(R.styleable.HeartView_heart_grid_line_color, Color.parseColor("#DBDBDB"));
        // 表格边框的颜色 默认绿色
        heart_grid_border_color = typedArray.getColor(R.styleable.HeartView_heart_grid_border_color, Color.parseColor("#DBDBDB"));
        // 播放速度的控制
        heart_speed = typedArray.getFloat(R.styleable.HeartView_heart_speed, 1.0f);
        // 是否画直线或曲线
        isCurve = typedArray.getBoolean(R.styleable.HeartView_heart_is_curve, false);
        typedArray.recycle();

        // 速度怎么可以小于0
        if (heart_speed < 0) {
            throw new RuntimeException("Attributes heart_speed Can Not < 0 ");
        }
        // 最小值怎么可以大于或等于最大值
        if (Min >= Max) {
            throw new RuntimeException("Attributes heart_min Can Not >= heart_max ");
        }

        showTimeDatas = new int[(int) (showSeconds * hz)];
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        viewHeight = measureHeight(heightMeasureSpec);
        viewWidth = measureWidth(widthMeasureSpec);
        path.moveTo(0, viewHeight);
    }

    /**
     * 重新部署发点任务
     */
    private synchronized void publishJob() {
        // 根据采集的频率，自动算出每一个点之间暂停的时间
        long yield = (int) (1000 / (hz * heart_speed));
        if (heartTask != null) {
            heartTask.cancel();
            heartTask = null;
        }
        heartTask = new HeartTask();
        timer.scheduleAtFixedRate(heartTask, 0, yield);
    }

    /**
     * 设置表格的行数
     *
     * @param grid_row
     */
    public void setGrid_row(int grid_row) {
        this.grid_row = grid_row;
    }

    /**
     * 设置每个小方格的高度
     *
     * @param height
     */
    public void setGrid_row_height(int height) {
        this.grid_row_height = height;
    }

    /**
     * 设置线条颜色
     *
     * @param color
     */
    public void setHeartColor(@ColorInt int color) {
        this.heartColor = color;
    }

    /**
     * 设置画小表格的颜色
     *
     * @param color
     */
    public void setHeartGridLineColor(@ColorInt int color) {
        this.heart_grid_line_color = color;
    }

    /**
     * 设置大表格边框颜色
     *
     * @param color
     */
    public void setHeartGridBorderColor(@ColorInt int color) {
        this.heart_grid_border_color = color;
    }

    /**
     * 设置线条宽度
     *
     * @param border
     */
    public void setHeartLineBorder(@ColorInt int border) {
        this.heart_line_border = border;
    }

    /**
     * 设置大格边框线宽
     *
     * @param border
     */
    public void setHeartGridBorder(int border) {
        this.heart_grid_border = border;
    }

    /**
     * 设置小格线宽
     *
     * @param border
     */
    public void setHeartGridLineBorder(int border) {
        this.heart_grid_line_border = border;
    }

    /**
     * 设置倍速
     *
     * @param speed
     */
    public void setHeartSpeed(@FloatRange(from = 0.0, to = Float.MAX_VALUE) float speed) {
        this.heart_speed = speed;
        // 速度怎么可以小于0
        if (heart_speed < 0) {
            throw new RuntimeException("Attributes heart_speed Can Not < 0 ");
        }
        publishJob();
    }

    /**
     * 添加一个点，会自动依据频率来动态显示
     *
     * @param point
     */
    public synchronized void offer(int point) {
        dataQueue.offer(point);
        if (heartTask == null) {
            publishJob();
        }
    }

    /**
     * 添加一组点，自动依据频率来动态显示
     *
     * @param points
     */
    public void offer(int[] points) {
        for (int i = 0; i < points.length; i++)
            offer(points[i]);
    }

    /**
     * 设置显示死数据，没有动态走动效果
     */
    public synchronized void setData(int[] points) {
        // 如果传过来的数据 比要显示的短，那么先根据数据长度替换，再将尾巴数据清空
        // 传递数据:[5，6]
        // 显示数据:[1,1,1]
        // 替换数据:[5,6,1]
        // 尾巴清空:[5,6,0]
        if (points.length <= showTimeDatas.length) {
            System.arraycopy(points, 0, showTimeDatas, 0, points.length);
            for (int i = points.length; i < showTimeDatas.length; i++) {
                showTimeDatas[i] = 0;
            }
        } else {
            // 如果传过来的数据，比显示的要长，那么以显示的长度为依据进行数据替换
            // 传递数据:[5,6,7]
            // 显示数据:[1,2]
            // 替换数据:[5,6]
            System.arraycopy(points, 0, showTimeDatas, 0, showTimeDatas.length);
        }
        postInvalidate();
    }

    /**
     * 设置每秒的采集频率
     *
     * @param hz
     */
    public synchronized void setHz(int hz) {
        this.hz = hz;
        this.showTimeDatas = new int[(int) (showSeconds * hz)];
        publishJob();
    }

    /**
     * 设置最大值
     *
     * @param max
     */
    public synchronized void setMax(int max) {
        this.Max = max;
        // 最小值怎么可以大于或等于最大值
        if (Min >= Max) {
            throw new RuntimeException("Attributes heart_min Can Not >= heart_max ");
        }
    }

    /**
     * 设置最小值
     *
     * @param min
     */
    public void setMin(int min) {
        Min = min;
        // 最小值怎么可以大于或等于最大值
        if (Min >= Max) {
            throw new RuntimeException("Attributes heart_min Can Not >= heart_max ");
        }
    }

    /**
     * 设置控件显示几秒钟的数据
     *
     * @param showSeconds
     */
    public synchronized void setShowSeconds(float showSeconds) {
        this.showSeconds = showSeconds;
        this.showTimeDatas = new int[(int) (showSeconds * hz)];
    }

    /**
     * 清空图案
     */
    public synchronized void clear() {
        for (int i = 0; i < showTimeDatas.length; i++)
            showTimeDatas[i] = 0;
        postInvalidate();
    }

    /**
     * 设置基准线
     *
     * @param baseLine
     */
    public void setBaseLine(int baseLine) {
        this.baseLine = baseLine;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int[] showDatas = showTimeDatas;
        // 画表格
        int baseY = calculateY(baseLine - Min, Max - Min, viewHeight);
        // 基准线以上
        for (int y = baseY; y > 0; y -= grid_row_height) {
            if ((baseY - y) / grid_row_height % grid_row == 0) {
                paint.setStrokeWidth(heart_grid_border);
                paint.setColor(heart_grid_border_color);
            } else {
                paint.setStrokeWidth(heart_grid_line_border);
                paint.setColor(heart_grid_line_color);
            }
            canvas.drawLine(0, y, viewWidth, y, paint);
        }
        // 基准线以下
        for (int y = baseY; y < viewHeight; y += grid_row_height) {
            if ((y - baseY) / grid_row_height % grid_row == 0) {
                paint.setStrokeWidth(heart_grid_border);
                paint.setColor(heart_grid_border_color);
            } else {
                paint.setStrokeWidth(heart_grid_line_border);
                paint.setColor(heart_grid_line_color);
            }
            canvas.drawLine(0, y, viewWidth, y, paint);
        }
        // 中心线以右
        int centerX = viewWidth / 2;
        for (int x = centerX; x < viewWidth; x += grid_row_height) {
            if ((x - centerX) / grid_row_height % grid_row == 0) {
                paint.setStrokeWidth(heart_grid_border);
                paint.setColor(heart_grid_border_color);
            } else {
                paint.setStrokeWidth(heart_grid_line_border);
                paint.setColor(heart_grid_line_color);
            }
            canvas.drawLine(x, 0, x, viewHeight, paint);
        }
        // 中心线以左
        for (int x = centerX; x > 0; x -= grid_row_height) {
            if ((centerX - x) / grid_row_height % grid_row == 0) {
                paint.setStrokeWidth(heart_grid_border);
                paint.setColor(heart_grid_border_color);
            } else {
                paint.setStrokeWidth(heart_grid_line_border);
                paint.setColor(heart_grid_line_color);
            }
            canvas.drawLine(x, 0, x, viewHeight, paint);
        }


        // 画心电
        paint.setColor(heartColor);
        paint.setStrokeWidth(heart_line_border);
        int firstData = showDatas[0];
        int firstY = calculateY(firstData - Min, Max - Min, viewHeight);
        path.reset();

        //新的开始
        points.clear();
        points.add(new Point(0, firstY));
        for (int i = 0; i < showDatas.length; i++) {
            int value = showDatas[i];
            int x = (int) (((float) i / showDatas.length) * viewWidth);
            int y = calculateY(value - Min, Max - Min, viewHeight);
            points.add(new Point(x, y));
        }
        //判断是否画曲线
        if (isCurve) {
            drawCurve(canvas);
        } else {
            drawLines(canvas);
        }
    }

    /**
     * 画线
     */
    private void drawLines(Canvas canvas) {
        if (points.size() < 2) return; //至少两个点
        path.moveTo(points.get(0).x, points.get(0).y);
        for (int i = 1; i < points.size(); i++) {
            path.lineTo(points.get(i).x, points.get(i).y);
        }
        canvas.drawPath(path, paint);
    }

    /**
     * 画曲线.
     */
    private void drawCurve(Canvas canvas) {
        if (points.size() < 3) return; //至少三个点

        points_x.clear();
        points_y.clear();
        for (int i = 0; i < points.size(); i++) {
            points_x.add(points.get(i).x);
            points_y.add(points.get(i).y);
        }

        List<Cubic> calculate_x = calculate(points_x);
        List<Cubic> calculate_y = calculate(points_y);
        path.moveTo(calculate_x.get(0).eval(0), calculate_y.get(0).eval(0));

        for (int i = 0; i < calculate_x.size(); i++) {
            //利用函数计算每一段曲线中，各小段直线的点
            for (int j = 1; j <= STEPS; j++) {
                float u = j / (float) STEPS;
                path.lineTo(calculate_x.get(i).eval(u), calculate_y.get(i)
                        .eval(u));
            }
        }
        canvas.drawPath(path, paint);
    }

    /**
     * 关键代码：计算曲线
     */
    private List<Cubic> calculate(List<Integer> x) {
        int n = x.size() - 1;
        float[] gamma = new float[n + 1];
        float[] delta = new float[n + 1];
        float[] D = new float[n + 1];
        int i;
        /*
         * We solve the equation [2 1 ] [D[0]] [3(x[1] - x[0]) ] |1 4 1 | |D[1]|
         * |3(x[2] - x[0]) | | 1 4 1 | | . | = | . | | ..... | | . | | . | | 1 4
         * 1| | . | |3(x[n] - x[n-2])| [ 1 2] [D[n]] [3(x[n] - x[n-1])]
         *
         * by using row operations to convert the matrix to upper triangular and
         * then back sustitution. The D[i] are the derivatives at the knots.
         *
         * 通过行变换将矩阵转换为上三角矩阵和
         * 然后sustitution回来。D[i]是结点处的导数。
         */

        gamma[0] = 1.0f / 2.0f;
        for (i = 1; i < n; i++) {
            gamma[i] = 1 / (4 - gamma[i - 1]);
        }
        gamma[n] = 1 / (2 - gamma[n - 1]);

        delta[0] = 3 * (x.get(1) - x.get(0)) * gamma[0];
        for (i = 1; i < n; i++) {
            delta[i] = (3 * (x.get(i + 1) - x.get(i - 1)) - delta[i - 1])
                    * gamma[i];
        }
        delta[n] = (3 * (x.get(n) - x.get(n - 1)) - delta[n - 1]) * gamma[n];

        D[n] = delta[n];
        for (i = n - 1; i >= 0; i--) {
            D[i] = delta[i] - gamma[i] * D[i + 1];
        }

        /*
         *  now compute the coefficients of the cubics
         * 现在计算三次函数
         */
        List<Cubic> cubics = new LinkedList<Cubic>();
        for (i = 0; i < n; i++) {
            Cubic c = new Cubic(x.get(i), D[i], 3 * (x.get(i + 1) - x.get(i))
                    - 2 * D[i] - D[i + 1], 2 * (x.get(i) - x.get(i + 1)) + D[i]
                    + D[i + 1]);
            cubics.add(c);
        }
        return cubics;
    }

    /**
     * this class represents a cubic polynomial
     * 这个类表示一个三次多项式
     */
    public class Cubic {
        float a, b, c, d;         /* a + b*u + c*u^2 +d*u^3 */

        public Cubic(float a, float b, float c, float d) {
            this.a = a;
            this.b = b;
            this.c = c;
            this.d = d;
        }

        /**
         * evaluate cubic
         */
        public float eval(float u) {
            return (((d * u) + c) * u + b) * u + a;
        }
    }


    /**
     * 根据最大值，控件高度，计算出当前值对应的控件的 Y 坐标
     *
     * @param value      参与计算的值
     * @param Region     最大值 - 最小值的区域
     * @param viewHeight 控件高度
     * @return
     */
    private static int calculateY(int value, int Region, int viewHeight) {
        return viewHeight - ((int) (((float) value / Region) * viewHeight));
    }

    /**
     * 测量自定义View的高度
     */
    private int measureHeight(int heightMeasureSpec) {
        int heightResult = 0;
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        switch (heightSpecMode) {
            case MeasureSpec.UNSPECIFIED: {
                heightResult = heightSpecSize;
            }
            break;
            case MeasureSpec.AT_MOST: {
                heightResult = MeasureSpec.getSize(heightMeasureSpec);
            }
            break;
            case MeasureSpec.EXACTLY: {
                heightResult = MeasureSpec.getSize(heightMeasureSpec);
            }
        }
        return heightResult;
    }

    /**
     * 测量自定义View的宽度
     */
    private int measureWidth(int widthMeasureSpec) {
        int widthResult = 0;
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        switch (widthSpecMode) {
            case MeasureSpec.UNSPECIFIED: {
                widthResult = widthSpecSize;
            }
            break;
            case MeasureSpec.AT_MOST: {
                widthResult = MeasureSpec.getSize(widthMeasureSpec);
            }
            break;
            case MeasureSpec.EXACTLY: {
                widthResult = MeasureSpec.getSize(widthMeasureSpec);
            }
        }
        return widthResult;
    }


    /**
     * dp 转 px
     *
     * @param context  上下文
     * @param dipValue dp值
     * @return
     */
    private float dip2px(Context context, float dipValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return dipValue * scale + 0.5f;
    }


    /**
     * 释放资源
     */
    public synchronized void recycle() {
        if (heartTask != null) {
            heartTask.cancel();
        }
        timer.cancel();
    }

    /**
     * 发点的任务
     */
    private class HeartTask extends TimerTask {
        @Override
        public void run() {
            try {
                Integer point = dataQueue.poll();
                if (point != null) {
                    for (int i = 0; i < showTimeDatas.length; i++) {
                        if (i + 1 < showTimeDatas.length) {
                            showTimeDatas[i] = showTimeDatas[i + 1];
                        } else {
                            showTimeDatas[i] = point;
                        }
                    }
                    postInvalidate();
                } else {
                    cancel();
                    heartTask = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
