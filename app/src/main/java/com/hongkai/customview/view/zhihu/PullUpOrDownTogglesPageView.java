package com.hongkai.customview.view.zhihu;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.util.HashMap;

import static android.util.TypedValue.COMPLEX_UNIT_DIP;

/**
 * 上拉查看下一页  下拉查看上一页
 * 简单实现仿知乎详情页效果，具有优化空间，优化方向Item复用, Item创建与销毁  如内部嵌套滑动控件，需处理事件冲突（切勿在项目中直接使用）
 */
public class PullUpOrDownTogglesPageView extends FrameLayout {
    private static final String TAG = "TogglesPageView";
    private Context context;
    private Adapter adapter;
    private ContainerViewGrop container;

    private int range;


    private Status currentPullDownStatus = Status.KEEP;
    private Status currentPullUpStatus = Status.KEEP;
    private int textViewHeiht;
    private int downTop;

    private HashMap<Object, View> cachViews;
    private TextView pullTextView;

    /**
     * 状态
     */
    enum Status {

        /**
         * 保持
         */
        KEEP("KEEP"),
        /**
         * 复位
         */
        RESET("RESET"),
        /**
         * 新状态
         */
        NEW("NEW");

        private String statusStr;

        Status(String statusStr) {
            this.statusStr = statusStr;
        }

        public String getStatusStr() {
            return statusStr;
        }
    }


    public PullUpOrDownTogglesPageView(Context context) {
        this(context, null);
    }

    public PullUpOrDownTogglesPageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PullUpOrDownTogglesPageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        range = (int) dp2px(context, 200);
        textViewHeiht = (int) dp2px(context, 50);
        cachViews = new HashMap<>();
    }


    public <T> void setAdapter(Adapter<T> adapter) {
        if (adapter == null) {
            throw new NullPointerException("Adapter 不能为空");
        }
        this.adapter = adapter;
        removeAllViews();
        container = new ContainerViewGrop(context);
        addView(container);
        int itmeCount = adapter.getItmeCount();
        for (int i = 0; i < itmeCount; i++) {
            //内容View
            container.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
            View view = adapter.getView(context, i, container);
            T item = adapter.getItem(i);
            view.setTag(item);
            TextView textView = new TextView(context);
            textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, textViewHeiht));
            textView.setGravity(Gravity.CENTER);
            textView.setText("上拉查看更多");
            textView.setBackgroundColor(Color.GREEN);
            container.addView(view);
            container.addView(textView);
            cachViews.put(item, view);
        }

        pullTextView = new TextView(context);
        pullTextView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, textViewHeiht));
        pullTextView.setBackgroundColor(Color.RED);
        pullTextView.setGravity(Gravity.CENTER);
        pullTextView.post(new Runnable() {
            @Override
            public void run() {
                pullTextView.setBottom(0);
            }
        });

        pullTextView.setText("下拉查看上一页");
        addView(pullTextView);

    }


    /**
     * 数据发生改变 刷新
     */
    public void notifyDataChange() {
        if (adapter == null) {
            return;
        }
        int itmeCount = adapter.getItmeCount();
        for (int i = 0; i < itmeCount; i++) {
            //内容View
            Object item = adapter.getItem(i);
            View view1 = cachViews.get(item);
            if (view1 != null) {
                View childAt = container.getChildAt(i * 2);
                if (childAt != view1) {
                    int index = container.indexOfChild(view1);
                    View childAt1 = container.getChildAt(index + 1);
                    removeView(view1);
                    removeView(childAt1);
                    container.addView(view1, i * 2);
                    container.addView(childAt1, i * 2 + 1);
                }

            } else {
                View view = adapter.getView(context, i, container);
                view.setTag(item);
                TextView textView = new TextView(context);
                textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, textViewHeiht));
                textView.setGravity(Gravity.CENTER);
                textView.setText("上拉查看更多");
                textView.setBackgroundColor(Color.GREEN);
                container.addView(view, i * 2);
                container.addView(textView, i * 2 + 1);
            }
        }

    }


    float lastMoveY = 0;
    int cureentPage;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isAnimatorIng) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.e(TAG, "onTouchEvent =  ACTION_DOWN = " + "Status.KEEP");
                handTextViwe(cureentPage * 2 - 1, "上拉查看更多", 0);
                currentPullDownStatus = Status.KEEP;
                currentPullUpStatus = Status.KEEP;
                downTop = container.getTop();
                cureentPage = getCureentPage();
                lastMoveY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float moveY = event.getY();
                float dy = moveY - lastMoveY;
//                if (dy < 10 && dy > -10) {  //十个像素误差
//                    break;
//                }
                Log.e(TAG, "onTouchEvent = ACTION_MOVE = " + "Status.KEEP");
                currentPullDownStatus = Status.KEEP;
                currentPullUpStatus = Status.KEEP;
                handlerMoveEvent(dy);
                lastMoveY = moveY;
                break;
            case MotionEvent.ACTION_UP:
                handUpEvent();
                break;
        }


        return true;
    }


    /**
     * 手指松开
     */
    private void handUpEvent() {
        int currentTop = container.getTop();
        if (currentPullDownStatus == Status.RESET) {
            animator(container, currentTop, -(cureentPage * getItemHeiht()));
            animator(pullTextView, pullTextView.getBottom(), 0);
        }
        if (currentPullDownStatus == Status.NEW) {
            animator(container, currentTop, -((cureentPage - 1) * getItemHeiht()));
            pullTextView.setBottom(0);
            //上一页
            if (pullUpDownLisenter != null) {
                pullUpDownLisenter.onPullDown(this, cureentPage, cureentPage - 1);
            }
        }

        if (currentPullUpStatus == Status.RESET) {
            if (cureentPage == adapter.getItmeCount() - 1) {
                //最后一个
                animator(container, currentTop, -(cureentPage * getItemHeiht()));
                return;
            }
            animator(container, currentTop, -(cureentPage * getItemHeiht() + range));
        }

        if (currentPullUpStatus == Status.NEW) {
            //下一页
            animator(container, currentTop, -((cureentPage + 1) * getItemHeiht()));
            if (pullUpDownLisenter != null) {
                pullUpDownLisenter.onPullUp(this, cureentPage, cureentPage + 1);
            }
        }

    }


    /**
     * 是否动画中
     */
    private boolean isAnimatorIng;

    /**
     * 动画
     *
     * @param view
     */
    public void animator(View view, int form, int to) {
        //设置自定义的TypeEvaluator，起始属性
        ValueAnimator valueAnimator = ValueAnimator.ofInt(form, to);
        //设置持续时间
        valueAnimator.setDuration(400);
        //设置线性时间插值器
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                isAnimatorIng = false;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                //动画结束
                isAnimatorIng = false;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                super.onAnimationRepeat(animation);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                //动画开始
                isAnimatorIng = true;
            }

            @Override
            public void onAnimationPause(Animator animation) {
                super.onAnimationPause(animation);
            }

            @Override
            public void onAnimationResume(Animator animation) {
                super.onAnimationResume(animation);
            }
        });
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                if (pullTextView == view) {
                    view.setBottom(value);
                } else {
                    view.setTop(value);
                }

            }
        });
        valueAnimator.start();
    }


    /**
     * 处理移动事件
     *
     * @param dy
     */
    private void handlerMoveEvent(float dy) {
        int currentTop = container.getTop();

        //从按下 到当前 整体是向下移动的
        Log.e(TAG, " page = " + cureentPage + " currentTop = " + currentTop + " downTop = " + downTop + " dy = " + dy);
//
        if (currentTop > downTop) {
            //向下拉
            // 0  1* getItemHeiht  2* getItemHeiht  3*getItemHeiht   变缓
            if (currentTop >= -(cureentPage * getItemHeiht())) {
                pullTextView.setBottom((int) (currentTop + (dy * 0.2) - downTop));
                container.setTop((int) (currentTop + dy * 0.2));
                if (currentTop > -(cureentPage * getItemHeiht()) + 200) {
                    Log.e(TAG, "下拉 page = " + cureentPage + " type = " + "Status.NEW");
                    currentPullDownStatus = Status.NEW;
                    if (cureentPage <= 0) {
                        Log.e(TAG, "0 下拉 page = " + cureentPage + " type = " + "Status.RESET");
                        pullTextView.setText("");
                        currentPullDownStatus = Status.RESET;
                    } else {
                        handTextViwe(cureentPage * 2 - 1, "松开查看", 0);
                        pullTextView.setText("松开查看");
                    }
                } else {
                    Log.e(TAG, "1 下拉 page = " + cureentPage + " type = " + "Status.RESET");
                    currentPullDownStatus = Status.RESET;
                    if (cureentPage > 0) {
                        handTextViwe(cureentPage * 2 - 1, "下拉查看上一页", 0);
                        pullTextView.setText("下拉查看上一页");
                    }
                }
                return;
            }
        }

        //从按下 到当前 整体是向上移动的
        if (currentTop < downTop) {
            Log.e(TAG, "上拉 page = " + cureentPage + "currentTop = " + currentTop + "cureentPage * getItemHeiht() = " + -(cureentPage * getItemHeiht()));

            //向上拉
            //最后一个 特殊逻辑
            if (cureentPage == adapter.getItmeCount() - 1 && currentTop <= -cureentPage * getItemHeiht()) {
                Log.e(TAG, "0 上拉 page = " + cureentPage + " type = " + "Status.RESET");
                currentPullUpStatus = Status.RESET;
                container.setTop((int) (currentTop + dy * 0.2));

                handTextViwe(container.getChildCount() - 1, "我也是有底线的。。。", 0);
                return;
            }


            // range   1* getItemHeiht+range  2*getItemHeiht+range  3*getItemHeiht+range
            if (currentTop <= -(cureentPage * getItemHeiht() + range)) {

                container.setTop((int) (currentTop + dy * 0.2));

                if (currentTop < -(cureentPage * getItemHeiht() + range) - 200) {
                    Log.e(TAG, "上拉 page = " + cureentPage + " type = " + "Status.NEW");
                    currentPullUpStatus = Status.NEW;
                    if (cureentPage >= adapter.getItmeCount() - 1) {
                        Log.e(TAG, "1 上拉 page = " + cureentPage + " type = " + "Status.RESET");
                        currentPullUpStatus = Status.RESET;
                    }
                    handTextViwe(cureentPage * 2 + 1, "松开查看", 0);
                } else {
                    Log.e(TAG, "2 上拉 page = " + cureentPage + " type = " + "Status.RESET");
                    currentPullUpStatus = Status.RESET;
                    handTextViwe(cureentPage * 2 + 1, "上拉查看下一页", 0);
                }

                return;
            }

        }
        Log.e(TAG, "实际距离移动");
        container.setTop((int) (currentTop + dy));
    }

    /**
     * 处理 对应位置上的TextView
     *
     * @param index
     * @param text
     * @param dy
     */
    private TextView currentHandleText;

    private void handTextViwe(int index, String text, int dy) {
        if (index < 0 || index >= container.getChildCount()) {
            return;
        }
        View lastTextView = container.getChildAt(index);
        if (lastTextView instanceof TextView) {
            currentHandleText = (TextView) lastTextView;
            currentHandleText.setText(text);
            lastTextView.setTop(lastTextView.getTop() - dy);
        }

    }

    /**
     * 上拉
     *
     * @param dy
     */
    private void pullUp(float dy) {

    }

    /**
     * 下拉
     *
     * @param dy
     */
    private void pullDown(float dy) {

    }


    private int getItemHeiht() {
        return container.getChildAt(0).getHeight() + container.getChildAt(1).getHeight();
    }

    private int getCureentPage() {
        int page = container.getTop() / getItemHeiht();
        Log.e(TAG, "page = " + page);
        return Math.abs(page);
    }

    public float dp2px(Context context, float value) {
        return TypedValue.applyDimension(COMPLEX_UNIT_DIP, value, context.getResources().getDisplayMetrics());
    }


    private PullUpDownLisenter pullUpDownLisenter;

    public void setPullUpDownLisenter(PullUpDownLisenter lisenter) {
        this.pullUpDownLisenter = lisenter;
    }


    /**
     * 监听
     */
    public interface PullUpDownLisenter {

        /**
         * 上拉
         */
        void onPullUp(PullUpOrDownTogglesPageView pullUpOrDownTogglesPageView, int formPage, int toPage);

        /**
         *
         */
        void onPullDown(PullUpOrDownTogglesPageView pullUpOrDownTogglesPageView, int formPage, int toPage);


    }


}
