import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../services/database_service.dart';

class PlatformSettings {
  bool youtubeShorts;
  bool instagramReels;
  bool facebookReels;
  bool tiktok;
  bool snapchatSpotlight;

  PlatformSettings({
    this.youtubeShorts = true,
    this.instagramReels = true,
    this.facebookReels = true,
    this.tiktok = true,
    this.snapchatSpotlight = true,
  });

  Map<String, bool> toMap() {
    return {
      'youtubeShorts': youtubeShorts,
      'instagramReels': instagramReels,
      'facebookReels': facebookReels,
      'tiktok': tiktok,
      'snapchatSpotlight': snapchatSpotlight,
    };
  }
}

class BlockingProvider with ChangeNotifier {
  static const platform = MethodChannel('com.focusguard/blocking');
  static const eventChannel = EventChannel('com.focusguard/blocking_events');

  bool _isProtectionActive = false;
  bool _hasAccessibilityPermission = false;
  int _blockedToday = 0;
  List<BlockEventRecord> _recentActivity = [];
  PlatformSettings _settings = PlatformSettings();
  ThemeMode _themeMode = ThemeMode.system;

  bool get isProtectionActive => _isProtectionActive;
  bool get hasAccessibilityPermission => _hasAccessibilityPermission;
  int get blockedToday => _blockedToday;
  List<BlockEventRecord> get recentActivity => _recentActivity;
  PlatformSettings get settings => _settings;
  ThemeMode get themeMode => _themeMode;

  final DatabaseService _dbService = DatabaseService();

  BlockingProvider() {
    _init();
  }

  Future<void> _init() async {
    await _loadSettings();
    await checkPermission();
    await _checkProtectionStatus();
    await fetchStats();
    
    eventChannel.receiveBroadcastStream().listen((event) {
      if (event is Map) {
        final platform = event['platform'] as String;
        final timestamp = (event['timestamp'] as num).toInt();
        _handleBlockEvent(platform, timestamp);
      }
    });
  }

  Future<void> _loadSettings() async {
    final prefs = await SharedPreferences.getInstance();
    _settings.youtubeShorts = prefs.getBool('block_youtube_shorts') ?? true;
    _settings.instagramReels = prefs.getBool('block_instagram_reels') ?? true;
    _settings.facebookReels = prefs.getBool('block_facebook_reels') ?? true;
    _settings.tiktok = prefs.getBool('block_tiktok') ?? true;
    _settings.snapchatSpotlight = prefs.getBool('block_snapchat_spotlight') ?? true;
    
    // Theme
    final themeIndex = prefs.getInt('theme_mode') ?? 0; // 0: system, 1: light, 2: dark
    _themeMode = ThemeMode.values[themeIndex];
    
    notifyListeners();
  }

  Future<void> setThemeMode(ThemeMode mode) async {
    _themeMode = mode;
    final prefs = await SharedPreferences.getInstance();
    await prefs.setInt('theme_mode', mode.index);
    notifyListeners();
  }

  Future<void> checkPermission() async {
    try {
      final bool result = await platform.invokeMethod('isAccessibilityServiceEnabled');
      _hasAccessibilityPermission = result;
      notifyListeners();
    } on PlatformException catch (e) {
      debugPrint("Failed to check permission: '${e.message}'.");
    }
  }

  Future<void> _checkProtectionStatus() async {
    try {
      final bool result = await platform.invokeMethod('isProtectionActive');
      _isProtectionActive = result;
      notifyListeners();
    } on PlatformException catch (e) {
      debugPrint("Failed to check protection status: '${e.message}'.");
    }
  }

  Future<void> toggleProtection() async {
    if (!_hasAccessibilityPermission) {
      await platform.invokeMethod('openAccessibilitySettings');
      return;
    }

    try {
      final newState = !_isProtectionActive;
      await platform.invokeMethod('setProtectionActive', {'active': newState});
      _isProtectionActive = newState;
      notifyListeners();
    } on PlatformException catch (e) {
      debugPrint("Failed to toggle protection: '${e.message}'.");
    }
  }

  Future<void> updateSettings(String key, bool value) async {
    switch (key) {
      case 'youtubeShorts': _settings.youtubeShorts = value; break;
      case 'instagramReels': _settings.instagramReels = value; break;
      case 'facebookReels': _settings.facebookReels = value; break;
      case 'tiktok': _settings.tiktok = value; break;
      case 'snapchatSpotlight': _settings.snapchatSpotlight = value; break;
    }
    notifyListeners();

    final prefs = await SharedPreferences.getInstance();
    String prefKey = '';
    switch (key) {
      case 'youtubeShorts': prefKey = 'block_youtube_shorts'; break;
      case 'instagramReels': prefKey = 'block_instagram_reels'; break;
      case 'facebookReels': prefKey = 'block_facebook_reels'; break;
      case 'tiktok': prefKey = 'block_tiktok'; break;
      case 'snapchatSpotlight': prefKey = 'block_snapchat_spotlight'; break;
    }
    if (prefKey.isNotEmpty) {
      await prefs.setBool(prefKey, value);
    }

    // Sync with native
    try {
      await platform.invokeMethod('setBlockedPlatforms', {
        'youtube': _settings.youtubeShorts,
        'instagram': _settings.instagramReels,
        'facebook': _settings.facebookReels,
        'tiktok': _settings.tiktok,
        'snapchat': _settings.snapchatSpotlight,
      });
    } on PlatformException catch (e) {
      debugPrint("Failed to sync settings: '${e.message}'.");
    }
  }

  Future<void> fetchStats() async {
    _blockedToday = await _dbService.getTodayBlockCount();
    _recentActivity = await _dbService.getBlockEvents(limit: 10);
    notifyListeners();
  }

  Future<void> _handleBlockEvent(String platform, int timestamp) async {
    await _dbService.addBlockEvent(platform, timestamp);
    await fetchStats();
  }
}
