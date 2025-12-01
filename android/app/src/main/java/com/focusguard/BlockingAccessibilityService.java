package com.focusguard;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.util.Log;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.List;
import java.util.ArrayList;

public class BlockingAccessibilityService extends AccessibilityService {

    private static final String TAG = "BlockingService";
    private static final long CHECK_INTERVAL_MS = 500;
    private long lastCheckTime = 0;
    private boolean isProtectionActive = false;
    
    // Static reference for overlay control
    private static BlockingAccessibilityService instance;
    
    // Platform flags
    private boolean blockYouTubeShorts = true;
    private boolean blockInstagramReels = true;
    private boolean blockFacebookReels = true;
    private boolean blockTikTok = true;
    private boolean blockSnapchatSpotlight = true;

    // Overlay manager reference
    private BlockOverlayManager overlayManager;

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "Service Connected");
        instance = this;
        
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED | 
                          AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED |
                          AccessibilityEvent.TYPE_VIEW_SCROLLED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS | 
                     AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;
        info.notificationTimeout = 100;
        setServiceInfo(info);
        
        // Initialize overlay manager
        overlayManager = new BlockOverlayManager(this);
        
        loadPreferences();
    }

    private void loadPreferences() {
        SharedPreferences prefs = getSharedPreferences("FocusGuardPrefs", MODE_PRIVATE);
        isProtectionActive = prefs.getBoolean("protection_active", false);
        blockYouTubeShorts = prefs.getBoolean("block_youtube_shorts", true);
        blockInstagramReels = prefs.getBoolean("block_instagram_reels", true);
        blockFacebookReels = prefs.getBoolean("block_facebook_reels", true);
        blockTikTok = prefs.getBoolean("block_tiktok", true);
        blockSnapchatSpotlight = prefs.getBoolean("block_snapchat_spotlight", true);
        
        Log.i(TAG, "=== FocusGuard Service State ===");
        Log.i(TAG, "Protection Active: " + isProtectionActive);
        Log.i(TAG, "Block YouTube Shorts: " + blockYouTubeShorts);
        Log.i(TAG, "Block Instagram Reels: " + blockInstagramReels);
        Log.i(TAG, "Block Facebook Reels: " + blockFacebookReels);
        Log.i(TAG, "Block TikTok: " + blockTikTok);
        Log.i(TAG, "Block Snapchat Spotlight: " + blockSnapchatSpotlight);
        Log.i(TAG, "================================");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        String packageName = event.getPackageName() != null ? event.getPackageName().toString() : "";
        
        // Early exit: Only process relevant apps to save battery
        if (!isRelevantApp(packageName)) {
            return;
        }
        
        Log.d(TAG, "Event received from: " + packageName + " | Protection active: " + isProtectionActive);
        
        if (!isProtectionActive) {
            Log.d(TAG, "Protection is NOT active, skipping");
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCheckTime < CHECK_INTERVAL_MS) {
            return;
        }
        lastCheckTime = currentTime;

        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        
        if (rootNode == null) {
            Log.w(TAG, "Root node is null for package: " + packageName);
            return;
        }

        try {
            // YouTube and YouTube alternatives (ReVanced, Vanced, NewPipe, etc.)
            if (isYouTubeApp(packageName) && blockYouTubeShorts) {
                Log.i(TAG, "Checking YouTube/ReVanced for active Shorts tab...");
                if (isOnShortsTab(rootNode)) {
                    performBlockAction("YouTube Shorts");
                }
            } else if (packageName.equals("com.instagram.android") && blockInstagramReels) {
                Log.i(TAG, "Checking Instagram for active Reels tab...");
                if (isOnReelsTab(rootNode)) {
                    performBlockAction("Instagram Reels");
                }
            } else if (packageName.equals("com.facebook.katana") && blockFacebookReels) {
                Log.i(TAG, "Checking Facebook for active Reels section...");
                if (isOnReelsTab(rootNode)) {
                    performBlockAction("Facebook Reels");
                }
            } else if ((packageName.contains("tiktok") || packageName.equals("com.zhiliaoapp.musically")) && blockTikTok) {
                // TikTok is almost entirely short-form, so we check if on "For You" page
                // This is still quite aggressive, but TikTok's nature makes it hard to be more selective
                Log.i(TAG, "Checking TikTok for active For You page...");
                if (isOnForYouPage(rootNode)) {
                    performBlockAction("TikTok");
                }
            } else if (packageName.equals("com.snapchat.android") && blockSnapchatSpotlight) {
                Log.i(TAG, "Checking Snapchat for active Spotlight...");
                if (isOnSpotlightTab(rootNode)) {
                    performBlockAction("Snapchat Spotlight");
                }
            }
        } finally {
            rootNode.recycle();
        }
    }

    // Check if user is actively viewing Shorts content (Player or Tab)
    private boolean isOnShortsTab(AccessibilityNodeInfo root) {
        // 1. Check if the "Shorts" tab is selected (Bottom Navigation)
        if (checkForSelectedTab(root, "Shorts")) return true;
        
        // 2. Check if the Shorts PLAYER is visible (e.g. from timeline)
        // Look for specific Shorts player UI elements
        return isShortsPlayerVisible(root);
    }

    // Check if user is actively viewing Reels content (Player or Tab)
    private boolean isOnReelsTab(AccessibilityNodeInfo root) {
        // 1. Check if the "Reels" tab is selected
        // Note: We need to be careful not to match the unselected tab button
        if (checkForSelectedTab(root, "Reels")) return true;
        
        // 2. Check if the Reels PLAYER is visible
        return isReelsPlayerVisible(root);
    }

    private boolean isShortsPlayerVisible(AccessibilityNodeInfo root) {
        // 1. STRICT CHECK: Specific "Like this Short" button
        if (checkForKeywords(root, "Like this Short") || checkForKeywords(root, "Dislike this Short")) {
            Log.i(TAG, "Detected Shorts Player via specific buttons");
            return true;
        }

        // 2. LAYOUT HEURISTIC (The "Senior Engineer" Approach):
        // Shorts have the "Like" button on the RIGHT side of the screen (vertical stack).
        // Regular videos have the "Like" button on the LEFT/CENTER (horizontal row below video).
        // User Insight: Shorts have "Search" icon on top, regular videos might not (in player view).
        
        boolean hasSearch = checkForKeywords(root, "Search");
        
        AccessibilityNodeInfo likeButton = findNodeByKeyword(root, "Like");
        if (likeButton != null) {
            android.graphics.Rect buttonBounds = new android.graphics.Rect();
            likeButton.getBoundsInScreen(buttonBounds);
            
            android.graphics.Rect rootBounds = new android.graphics.Rect();
            root.getBoundsInScreen(rootBounds);
            
            int screenWidth = rootBounds.width();
            if (screenWidth == 0) {
                screenWidth = android.content.res.Resources.getSystem().getDisplayMetrics().widthPixels;
            }
            
            // Check if Like button is on the right 35% of the screen
            boolean isOnRightSide = buttonBounds.left > (screenWidth * 0.65);
            
            int screenHeight = rootBounds.height();
            if (screenHeight == 0) {
                screenHeight = android.content.res.Resources.getSystem().getDisplayMetrics().heightPixels;
            }
            boolean isBottomHalf = buttonBounds.top > (screenHeight * 0.25);

            likeButton.recycle();

            if (isOnRightSide && isBottomHalf) {
                // Strongest signal: Layout matches Shorts
                Log.i(TAG, "Detected Shorts Player via Layout (Like button on right side)");
                return true;
            }
        }
        
        // 3. FALLBACK BUTTON PATTERN (If Layout check fails or Like button not found):
        // - MUST have: Like, Dislike, Share, Comment
        // - MUST NOT have: Download, Clip, Save, Thanks
        // - BONUS: Has "Search" (User suggestion)
        
        boolean hasLike = checkForKeywords(root, "Like");
        boolean hasDislike = checkForKeywords(root, "Dislike");
        boolean hasShare = checkForKeywords(root, "Share");
        boolean hasComment = checkForKeywords(root, "Comment") || checkForKeywords(root, "Comments");
        
        boolean hasDownload = checkForKeywords(root, "Download");
        boolean hasClip = checkForKeywords(root, "Clip");
        boolean hasSave = checkForKeywords(root, "Save");
        boolean hasThanks = checkForKeywords(root, "Thanks");
        
        if (hasLike && hasDislike && hasShare && hasComment) {
            if (!hasDownload && !hasClip && !hasSave && !hasThanks) {
                // If we also have Search, it increases confidence
                if (hasSearch) {
                    Log.i(TAG, "Detected Shorts Player via Button Pattern + Search Icon");
                    return true;
                }
                // Even without Search, the pattern is strong
                Log.i(TAG, "Detected Shorts Player via Button Pattern");
                return true;
            }
        }
        
        return false;
    }

    private AccessibilityNodeInfo findNodeByKeyword(AccessibilityNodeInfo node, String keyword) {
        if (node == null) return null;
        
        if (!node.isVisibleToUser()) return null;

        // Check text
        if (node.getText() != null && node.getText().toString().contains(keyword)) {
            return node; // Caller must recycle!
        }

        // Check content description
        if (node.getContentDescription() != null && node.getContentDescription().toString().contains(keyword)) {
            return node; // Caller must recycle!
        }

        // Recursive check
        int childCount = node.getChildCount();
        for (int i = 0; i < childCount; i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                AccessibilityNodeInfo result = findNodeByKeyword(child, keyword);
                if (result != null) {
                    // Don't recycle child if it's the result, but recycle others? 
                    // Actually, findNodeByKeyword returns a node that needs recycling.
                    // The child node itself is recycled by the loop if not returned?
                    // No, node.getChild(i) returns a NEW node.
                    // If result is found, we return it. We should NOT recycle 'child' if it IS the result.
                    // If result is NOT found, we MUST recycle 'child'.
                    
                    // Wait, 'result' is either 'child' or a descendant.
                    // If 'result' == 'child', we return it.
                    // If 'result' is a descendant, 'child' is just a parent of result.
                    // We can recycle 'child' safely if 'result' is returned? 
                    // No, if result is a child of 'child', recycling 'child' might invalidate 'result'?
                    // AccessibilityNodeInfo objects are independent handles. Recycling parent doesn't invalidate child handle.
                    
                    if (result != child) {
                        child.recycle();
                    }
                    return result;
                }
                child.recycle();
            }
        }

        return null;
    }

    private boolean checkForKeywords(AccessibilityNodeInfo node, String keyword) {
        AccessibilityNodeInfo found = findNodeByKeyword(node, keyword);
        if (found != null) {
            found.recycle();
            return true;
        }
        return false;
    }

    private boolean isReelsPlayerVisible(AccessibilityNodeInfo root) {
        // 1. Existing check (Reels Tab)
        boolean hasReelsIndicator = checkForKeywords(root, "Reels");
        boolean hasCamera = checkForKeywords(root, "Camera");
        
        if (hasReelsIndicator && hasCamera) {
             Log.i(TAG, "Detected Reels Player via Header + Camera");
             return true;
        }

        // 2. LAYOUT HEURISTIC (For Reels from Search/Explore):
        // Reels have the "Like" button on the RIGHT side (vertical stack).
        // Regular posts have "Like" on the LEFT (horizontal row).
        
        AccessibilityNodeInfo likeButton = findNodeByKeyword(root, "Like");
        if (likeButton != null) {
            android.graphics.Rect buttonBounds = new android.graphics.Rect();
            likeButton.getBoundsInScreen(buttonBounds);
            
            android.graphics.Rect rootBounds = new android.graphics.Rect();
            root.getBoundsInScreen(rootBounds);
            
            int screenWidth = rootBounds.width();
            if (screenWidth == 0) {
                screenWidth = android.content.res.Resources.getSystem().getDisplayMetrics().widthPixels;
            }
            
            // Check if Like button is on the right 35% of the screen
            boolean isOnRightSide = buttonBounds.left > (screenWidth * 0.65);
            
            likeButton.recycle();

            if (isOnRightSide) {
                // Distinguish from Stories (which have Like on right but usually no "Comment" button, just a text field)
                // Reels have a specific "Comment" button.
                boolean hasComment = checkForKeywords(root, "Comment");
                
                if (hasComment) {
                     Log.i(TAG, "Detected Reels Player via Layout (Like on Right + Comment)");
                     return true;
                }
            }
        }
        
        return false; 
    }

    // Check if user is on TikTok's For You page
    private boolean isOnForYouPage(AccessibilityNodeInfo root) {
        // TikTok is mostly short-form, but we can try to detect the For You page
        return checkForKeywords(root, "For You") || checkForKeywords(root, "Following");
    }

    // Check if user is on Snapchat Spotlight tab
    private boolean isOnSpotlightTab(AccessibilityNodeInfo root) {
        return checkForSelectedTab(root, "Spotlight");
    }

    // Check if the package is YouTube or a YouTube alternative (ReVanced, Vanced, NewPipe, etc.)
    private boolean isYouTubeApp(String packageName) {
        // Official YouTube
        if (packageName.equals("com.google.android.youtube")) return true;
        
        // ReVanced (various package names)
        if (packageName.equals("app.revanced.android.youtube")) return true;
        if (packageName.equals("app.rvx.android.youtube")) return true;
        if (packageName.contains("revanced")) return true;
        
        // YouTube Vanced (legacy, but some still use it)
        if (packageName.equals("com.vanced.android.youtube")) return true;
        if (packageName.contains("vanced.youtube")) return true;
        
        // NewPipe (open source YouTube client)
        if (packageName.equals("org.schabi.newpipe")) return true;
        if (packageName.contains("newpipe")) return true;
        
        // LibreTube
        if (packageName.equals("dev.libre.tube")) return true;
        if (packageName.contains("libretube")) return true;
        
        // SkyTube
        if (packageName.equals("free.rm.skytube.oss")) return true;
        if (packageName.equals("free.rm.skytube.extra")) return true;
        if (packageName.contains("skytube")) return true;
        
        // YouTube Music ReVanced
        if (packageName.equals("app.revanced.android.youtube.music")) return true;
        
        // Other common YouTube alternatives
        if (packageName.contains("youtube") && !packageName.equals("com.google.android.youtube")) return true;
        
        return false;
    }

    // Check if the app is relevant for monitoring (to save battery)
    private boolean isRelevantApp(String packageName) {
        // YouTube and alternatives
        if (isYouTubeApp(packageName)) return true;
        
        // Instagram
        if (packageName.equals("com.instagram.android")) return true;
        
        // Facebook
        if (packageName.equals("com.facebook.katana")) return true;
        
        // TikTok (various package names)
        if (packageName.contains("tiktok")) return true;
        if (packageName.equals("com.zhiliaoapp.musically")) return true;
        if (packageName.equals("com.ss.android.ugc.trill")) return true;
        
        // Snapchat
        if (packageName.equals("com.snapchat.android")) return true;
        
        return false;
    }

    // Check if a tab with the given keyword is currently selected/active
    private boolean checkForSelectedTab(AccessibilityNodeInfo node, String keyword) {
        if (node == null) return false;

        // Check if this node contains the keyword
        boolean hasKeyword = false;
        if (node.getText() != null && node.getText().toString().contains(keyword)) {
            hasKeyword = true;
        }
        if (node.getContentDescription() != null && node.getContentDescription().toString().contains(keyword)) {
            hasKeyword = true;
        }

        // CRITICAL: Only return true if it is explicitly SELECTED or CHECKED
        // This prevents matching the unselected tab button in the nav bar
        if (hasKeyword) {
            if (node.isSelected()) {
                Log.i(TAG, "Found SELECTED tab: " + keyword);
                return true;
            }
            if (node.isChecked()) {
                Log.i(TAG, "Found CHECKED tab: " + keyword);
                return true;
            }
        }

        // Recursive check through children
        int childCount = node.getChildCount();
        for (int i = 0; i < childCount; i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                boolean found = checkForSelectedTab(child, keyword);
                child.recycle();
                if (found) return true;
            }
        }

        return false;
    }



    private void performBlockAction(String platform) {
        Log.i(TAG, "Blocking content from: " + platform);
        
        // Show overlay to block the content
        if (overlayManager != null) {
            overlayManager.showBlockOverlay(platform);
        }
        
        // Notify React Native
        BlockingModule.emitBlockEvent(platform, System.currentTimeMillis());
    }

    // Static method to perform back action from overlay service
    public static void performBackAction() {
        if (instance != null) {
            instance.performGlobalAction(GLOBAL_ACTION_BACK);
            // Double tap back to be sure
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                if (instance != null) {
                    instance.performGlobalAction(GLOBAL_ACTION_BACK);
                }
            }, 300);
        }
    }

    @Override
    public void onInterrupt() {
        Log.e(TAG, "Service Interrupted");
    }
    
    public static void updateState() {
        if (instance != null) {
            instance.loadPreferences();
            Log.i(TAG, "Preferences updated via static call");
        }
    }
}
