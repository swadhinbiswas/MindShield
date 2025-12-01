package com.focusguard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Random;

public class BlockOverlayManager {

    private static final String TAG = "BlockOverlayManager";
    private final Context context;
    private final WindowManager windowManager;
    private View overlayView;
    private boolean isOverlayShown = false;
    private final Random random = new Random();

    // Message categories
    private static final String[][] MESSAGES = {
        // Firm messages
        {
            "Focus! No scrolling right now.",
            "MindShield activated: Stay on task.",
            "Access denied. Your goals come first.",
            "Not today. Stay disciplined.",
            "Distractions blocked. Keep going!"
        },
        // Friendly messages
        {
            "Hey there! Let's give your brain a break.",
            "Oops! You hit the distraction wall.",
            "Whoa! Almost got you there. 😊",
            "Nice try! But let's stay focused.",
            "Caught ya! Back to what matters."
        },
        // Motivational messages
        {
            "Your time is precious. Protect it!",
            "Keep your focus sharp. MindShield says no.",
            "Champions don't get distracted. Be one!",
            "Every second counts. Use it wisely!",
            "Your future self will thank you.",
            "Greatness requires focus. You've got this!"
        }
    };

    private static final String[] CATEGORY_LABELS = {
        "🛡️ Protection Active",
        "👋 Friendly Reminder", 
        "💪 Stay Motivated"
    };

    public BlockOverlayManager(Context context) {
        this.context = context;
        this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    private View createOverlayView(String platform) {
        // Pick random category and message
        int categoryIndex = random.nextInt(MESSAGES.length);
        String[] categoryMessages = MESSAGES[categoryIndex];
        String message = categoryMessages[random.nextInt(categoryMessages.length)];
        String categoryLabel = CATEGORY_LABELS[categoryIndex];

        // Main container
        FrameLayout container = new FrameLayout(context);
        container.setBackgroundColor(Color.parseColor("#111827")); // Dark background

        // Content layout
        LinearLayout contentLayout = new LinearLayout(context);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setGravity(Gravity.CENTER);
        contentLayout.setPadding(dpToPx(32), dpToPx(32), dpToPx(32), dpToPx(32));

        // Shield SVG View
        ShieldView shieldView = new ShieldView(context);
        LinearLayout.LayoutParams shieldParams = new LinearLayout.LayoutParams(dpToPx(180), dpToPx(180));
        shieldParams.gravity = Gravity.CENTER;
        contentLayout.addView(shieldView, shieldParams);

        // Category Label
        TextView categoryText = new TextView(context);
        categoryText.setText(categoryLabel);
        categoryText.setTextColor(Color.parseColor("#818CF8")); // Primary purple
        categoryText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        categoryText.setTypeface(Typeface.DEFAULT_BOLD);
        categoryText.setGravity(Gravity.CENTER);
        categoryText.setAllCaps(true);
        categoryText.setLetterSpacing(0.1f);
        LinearLayout.LayoutParams categoryParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        categoryParams.setMargins(0, dpToPx(32), 0, dpToPx(12));
        categoryParams.gravity = Gravity.CENTER;
        contentLayout.addView(categoryText, categoryParams);

        // Main Message
        TextView messageText = new TextView(context);
        messageText.setText(message);
        messageText.setTextColor(Color.parseColor("#F9FAFB")); // Light text
        messageText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        messageText.setTypeface(Typeface.DEFAULT_BOLD);
        messageText.setGravity(Gravity.CENTER);
        messageText.setLineSpacing(dpToPx(4), 1.0f);
        LinearLayout.LayoutParams messageParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        messageParams.gravity = Gravity.CENTER;
        messageParams.setMargins(dpToPx(24), 0, dpToPx(24), 0);
        contentLayout.addView(messageText, messageParams);

        // Platform badge
        if (platform != null && !platform.isEmpty()) {
            TextView platformBadge = new TextView(context);
            platformBadge.setText("Blocked: " + platform);
            platformBadge.setTextColor(Color.parseColor("#9CA3AF")); // Secondary text
            platformBadge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            platformBadge.setGravity(Gravity.CENTER);
            platformBadge.setBackgroundColor(Color.parseColor("#1F2937")); // Card background
            platformBadge.setPadding(dpToPx(16), dpToPx(8), dpToPx(16), dpToPx(8));
            LinearLayout.LayoutParams badgeParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            badgeParams.setMargins(0, dpToPx(20), 0, 0);
            badgeParams.gravity = Gravity.CENTER;
            contentLayout.addView(platformBadge, badgeParams);
        }

        // Footer text
        TextView footerText = new TextView(context);
        footerText.setText("MindShield is protecting your focus");
        footerText.setTextColor(Color.parseColor("#6B7280")); // Muted text
        footerText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        footerText.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams footerParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        footerParams.setMargins(0, dpToPx(48), 0, 0);
        footerParams.gravity = Gravity.CENTER;
        contentLayout.addView(footerText, footerParams);

        FrameLayout.LayoutParams contentParams = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        contentParams.gravity = Gravity.CENTER;
        container.addView(contentLayout, contentParams);

        container.setClickable(true);
        container.setFocusable(true);

        return container;
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    // Custom View for Shield SVG
    private class ShieldView extends View {
        private Paint shieldPaint;
        private Paint highlightPaint;
        private Paint xPaint;
        private Paint glowPaint;
        private Paint dotPaint;
        private Path shieldPath;
        private Path innerShieldPath;

        public ShieldView(Context context) {
            super(context);
            init();
        }

        private void init() {
            // Shield gradient paint
            shieldPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            shieldPaint.setStyle(Paint.Style.FILL);

            // Highlight paint
            highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            highlightPaint.setStyle(Paint.Style.STROKE);
            highlightPaint.setStrokeWidth(4f);
            highlightPaint.setColor(Color.parseColor("#60FFFFFF"));

            // X mark paint
            xPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            xPaint.setStyle(Paint.Style.STROKE);
            xPaint.setStrokeWidth(16f);
            xPaint.setStrokeCap(Paint.Cap.ROUND);
            xPaint.setColor(Color.WHITE);

            // Glow paint
            glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            glowPaint.setStyle(Paint.Style.FILL);

            // Dot paint
            dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            dotPaint.setStyle(Paint.Style.FILL);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            float width = getWidth();
            float height = getHeight();
            float centerX = width / 2;
            float centerY = height / 2;
            float scale = Math.min(width, height) / 200f;

            // Draw glow circle
            glowPaint.setShader(new LinearGradient(
                centerX, centerY - 90 * scale,
                centerX, centerY + 90 * scale,
                Color.parseColor("#4D818CF8"),
                Color.TRANSPARENT,
                Shader.TileMode.CLAMP));
            canvas.drawCircle(centerX, centerY, 90 * scale, glowPaint);

            // Draw decorative dots
            dotPaint.setColor(Color.parseColor("#9934D399"));
            canvas.drawCircle(centerX - 55 * scale, centerY - 55 * scale, 4 * scale, dotPaint);
            canvas.drawCircle(centerX + 55 * scale, centerY - 55 * scale, 4 * scale, dotPaint);
            
            dotPaint.setColor(Color.parseColor("#66818CF8"));
            canvas.drawCircle(centerX - 70 * scale, centerY, 3 * scale, dotPaint);
            canvas.drawCircle(centerX + 70 * scale, centerY, 3 * scale, dotPaint);

            // Shield gradient
            shieldPaint.setShader(new LinearGradient(
                centerX - 65 * scale, centerY - 75 * scale,
                centerX + 65 * scale, centerY + 90 * scale,
                Color.parseColor("#818CF8"),
                Color.parseColor("#34D399"),
                Shader.TileMode.CLAMP));

            // Draw shield shape
            shieldPath = new Path();
            shieldPath.moveTo(centerX, centerY - 75 * scale);
            shieldPath.cubicTo(
                centerX, centerY - 75 * scale,
                centerX + 50 * scale, centerY - 65 * scale,
                centerX + 65 * scale, centerY - 50 * scale);
            shieldPath.cubicTo(
                centerX + 65 * scale, centerY - 50 * scale,
                centerX + 70 * scale, centerY,
                centerX + 65 * scale, centerY + 30 * scale);
            shieldPath.cubicTo(
                centerX + 60 * scale, centerY + 60 * scale,
                centerX + 30 * scale, centerY + 80 * scale,
                centerX, centerY + 90 * scale);
            shieldPath.cubicTo(
                centerX - 30 * scale, centerY + 80 * scale,
                centerX - 60 * scale, centerY + 60 * scale,
                centerX - 65 * scale, centerY + 30 * scale);
            shieldPath.cubicTo(
                centerX - 70 * scale, centerY,
                centerX - 65 * scale, centerY - 50 * scale,
                centerX - 65 * scale, centerY - 50 * scale);
            shieldPath.cubicTo(
                centerX - 50 * scale, centerY - 65 * scale,
                centerX, centerY - 75 * scale,
                centerX, centerY - 75 * scale);
            shieldPath.close();

            canvas.drawPath(shieldPath, shieldPaint);

            // Draw inner highlight
            innerShieldPath = new Path();
            innerShieldPath.moveTo(centerX, centerY - 60 * scale);
            innerShieldPath.cubicTo(
                centerX, centerY - 60 * scale,
                centerX + 40 * scale, centerY - 52 * scale,
                centerX + 52 * scale, centerY - 40 * scale);
            innerShieldPath.cubicTo(
                centerX + 52 * scale, centerY - 40 * scale,
                centerX + 56 * scale, centerY,
                centerX + 52 * scale, centerY + 25 * scale);
            innerShieldPath.cubicTo(
                centerX + 48 * scale, centerY + 50 * scale,
                centerX + 25 * scale, centerY + 67 * scale,
                centerX, centerY + 75 * scale);
            innerShieldPath.cubicTo(
                centerX - 25 * scale, centerY + 67 * scale,
                centerX - 48 * scale, centerY + 50 * scale,
                centerX - 52 * scale, centerY + 25 * scale);
            innerShieldPath.cubicTo(
                centerX - 56 * scale, centerY,
                centerX - 52 * scale, centerY - 40 * scale,
                centerX - 52 * scale, centerY - 40 * scale);
            innerShieldPath.cubicTo(
                centerX - 40 * scale, centerY - 52 * scale,
                centerX, centerY - 60 * scale,
                centerX, centerY - 60 * scale);

            canvas.drawPath(innerShieldPath, highlightPaint);

            // Draw X mark
            float xSize = 25 * scale;
            canvas.drawLine(
                centerX - xSize, centerY - xSize,
                centerX + xSize, centerY + xSize,
                xPaint);
            canvas.drawLine(
                centerX + xSize, centerY - xSize,
                centerX - xSize, centerY + xSize,
                xPaint);

            // Draw sparkles
            Paint sparklePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            sparklePaint.setColor(Color.parseColor("#CCFFFFFF"));
            sparklePaint.setStyle(Paint.Style.FILL);

            Path sparklePath = new Path();
            float sparkleX = centerX + 60 * scale;
            float sparkleY = centerY - 40 * scale;
            sparklePath.moveTo(sparkleX, sparkleY - 5 * scale);
            sparklePath.lineTo(sparkleX + 5 * scale, sparkleY);
            sparklePath.lineTo(sparkleX, sparkleY + 5 * scale);
            sparklePath.lineTo(sparkleX - 5 * scale, sparkleY);
            sparklePath.close();
            canvas.drawPath(sparklePath, sparklePaint);

            sparklePaint.setColor(Color.parseColor("#99FFFFFF"));
            Path sparklePath2 = new Path();
            float sparkle2X = centerX - 60 * scale;
            float sparkle2Y = centerY - 30 * scale;
            sparklePath2.moveTo(sparkle2X, sparkle2Y - 5 * scale);
            sparklePath2.lineTo(sparkle2X + 5 * scale, sparkle2Y);
            sparklePath2.lineTo(sparkle2X, sparkle2Y + 5 * scale);
            sparklePath2.lineTo(sparkle2X - 5 * scale, sparkle2Y);
            sparklePath2.close();
            canvas.drawPath(sparklePath2, sparklePaint);
        }
    }

    public void showBlockOverlay(String platform) {
        if (isOverlayShown) return;

        try {
            // Create fresh overlay with random message each time
            overlayView = createOverlayView(platform);

            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                            | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    PixelFormat.TRANSLUCENT
            );

            params.gravity = Gravity.TOP | Gravity.START;

            windowManager.addView(overlayView, params);
            isOverlayShown = true;

            Log.i(TAG, "Overlay shown for: " + platform);

            // Auto-hide after 2 seconds and perform back action
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                hideBlockOverlay();
                // Trigger back action through the accessibility service
                BlockingAccessibilityService.performBackAction();
            }, 2000);

        } catch (Exception e) {
            Log.e(TAG, "Error showing overlay: " + e.getMessage());
        }
    }

    public void hideBlockOverlay() {
        if (isOverlayShown && overlayView != null) {
            try {
                windowManager.removeView(overlayView);
                isOverlayShown = false;
                overlayView = null;
                Log.i(TAG, "Overlay hidden");
            } catch (Exception e) {
                Log.e(TAG, "Error hiding overlay: " + e.getMessage());
            }
        }
    }
}
