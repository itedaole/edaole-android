package com.john.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;

public class UIUtil {

    private static ProgressDialog mDialog;

    /**
     * 显示等待信息
     * 
     * @param context
     *            上下文
     * @param message
     *            显示的信息
     */
    public static void showProgressDialog(Context context, CharSequence message) {
        if (mDialog == null) {
            mDialog = new ProgressDialog(context);
            mDialog.setOnDismissListener(new OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    LogUtil.debug("progress dialog dismiss!");
                    mDialog = null;
                }
            });
        }
        mDialog.setOwnerActivity((Activity) context);
        mDialog.setMessage(message);
        mDialog.show();
    }

    /**
     * 显示进度条对话框
     * 
     * @param context
     * @param resId
     *            {@link string.xml}
     */
    public static void showProgressDialog(Context context, int resId) {
        if (mDialog == null) {
            mDialog = new ProgressDialog(context);
            mDialog.setOnDismissListener(new OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    LogUtil.debug("progress dialog dismiss!");
                    mDialog = null;
                }
            });
        }
        mDialog.setMessage(context.getString(resId));
        mDialog.show();
    }

    /**
     * 关闭等待信息
     */
    public static void dismissProgressDialog() {
        if (mDialog != null) {
            if (mDialog.isShowing()) {
                mDialog.dismiss();
            }
        }
    }

    /**
     * 图片圆角
     * 
     * @param bitmap
     * @return
     */
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPx) {
        Bitmap output = null;
        try {
            if (bitmap == null) {
                return null;
            }
            output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
            Canvas canvas = new Canvas(output);

            final int color = Color.WHITE;
            final Paint paint = new Paint();
            final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
            final RectF rectF = new RectF(rect);

            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(color);
            canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

            paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
            canvas.drawBitmap(bitmap, rect, rect, paint);
            bitmap.recycle();
        } catch (java.lang.OutOfMemoryError e) {
            System.gc();
        } catch (Exception e) {
            Log.e("UIUtils", "get resource error.", e);
        }
        return output;
    }

    /***
     * 图片缩放
     * 
     * @param bitMap
     *            源图片资源
     * @param newWidth
     *            缩放后宽度
     * @param newHeight
     *            缩放后高度
     * @return 缩放后的图片,如果为null,则返回null
     */

    public static Bitmap zoomImage(Bitmap bitMap, int newWidth, int newHeight) {
        // File file = new File("");
        // Bitmap b = B
        if (bitMap == null) {
            return null;
        }
        // 获取这个图片的宽和高
        int width = bitMap.getWidth();
        int height = bitMap.getHeight();
        // 创建操作图片用的matrix对象
        Matrix matrix = new Matrix();
        // 计算缩放率，新尺寸除原始尺寸
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 新尺寸大于原始尺寸则不缩放
        if (scaleWidth > 1.0f || scaleHeight > 1.0f) {
            return bitMap;
        }
        // 缩放图片动作
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap bitmap = Bitmap.createBitmap(bitMap, 0, 0, width, height, matrix, false);
        return bitmap;
    }

    /**
     * 设置view显示或不显示时的动画效果 *
     * 
     * @param v
     */
    public static void animOnToggle(View v) {
        final View view = v;
        final Context context = v.getContext();
        if (view.getAnimation() != null) {
            if (!view.getAnimation().hasEnded())
                return;
        }
        Animation anim = null;
        if (view.getVisibility() == View.GONE) {
            anim = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
            anim.setAnimationListener(new AnimationListener() {
                public void onAnimationStart(Animation arg0) {
                    view.setVisibility(View.VISIBLE);
                }

                public void onAnimationRepeat(Animation arg0) {
                }

                public void onAnimationEnd(Animation arg0) {
                }
            });
        } else {
            anim = AnimationUtils.loadAnimation(context, android.R.anim.fade_out);
            anim.setAnimationListener(new AnimationListener() {
                public void onAnimationStart(Animation arg0) {
                }

                public void onAnimationRepeat(Animation arg0) {
                }

                public void onAnimationEnd(Animation arg0) {
                    view.setVisibility(View.GONE);
                }
            });
        }
        if (anim != null) {
            view.startAnimation(anim);
        }
    }

    /**
     * 跳转到手机主屏
     * 
     * @param context
     */
    public static void toHome(Activity context) {
        Intent home = new Intent(Intent.ACTION_MAIN);
        home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        home.addCategory(Intent.CATEGORY_HOME);
        context.startActivity(home);
    }

}
