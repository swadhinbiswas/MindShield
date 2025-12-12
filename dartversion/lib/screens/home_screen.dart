import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:lucide_icons/lucide_icons.dart';
import '../providers/blocking_provider.dart';

import 'package:intl/intl.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> with WidgetsBindingObserver {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.resumed) {
      context.read<BlockingProvider>().checkPermission();
    }
  }

  IconData _getPlatformIcon(String platform) {
    final p = platform.toLowerCase();
    if (p.contains('youtube')) return LucideIcons.youtube;
    if (p.contains('instagram')) return LucideIcons.instagram;
    if (p.contains('facebook')) return LucideIcons.facebook;
    if (p.contains('tiktok')) return LucideIcons.video;
    if (p.contains('snapchat')) return LucideIcons.ghost;
    return LucideIcons.smartphone;
  }

  Color _getPlatformColor(String platform, bool isDark) {
    final p = platform.toLowerCase();
    if (p.contains('youtube')) return Colors.red;
    if (p.contains('instagram')) return const Color(0xFFE1306C);
    if (p.contains('facebook')) return const Color(0xFF1877F2);
    if (p.contains('tiktok')) return isDark ? Colors.white : Colors.black;
    if (p.contains('snapchat')) return const Color(0xFFFFFC00);
    return Colors.grey;
  }

  String _getPlatformName(String platform) {
    if (platform.contains('YouTube')) return 'YouTube Shorts';
    if (platform.contains('Instagram')) return 'Instagram Reels';
    if (platform.contains('Facebook')) return 'Facebook Reels';
    if (platform.contains('TikTok')) return 'TikTok';
    if (platform.contains('Snapchat')) return 'Snapchat';
    return platform;
  }

  String _formatTime(int timestamp) {
    return DateFormat.jm().format(DateTime.fromMillisecondsSinceEpoch(timestamp));
  }

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<BlockingProvider>();
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;

    return Scaffold(
      body: SafeArea(
        child: CustomScrollView(
          slivers: [
            SliverToBoxAdapter(
              child: Padding(
                padding: const EdgeInsets.all(24.0),
                child: Column(
                  children: [
                    Text(
                      'MindShield',
                      style: theme.textTheme.headlineMedium?.copyWith(
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    const SizedBox(height: 20),
                    
                    // Status Toggle
                    GestureDetector(
                      onTap: provider.toggleProtection,
                      child: Container(
                        width: 160,
                        height: 160,
                        decoration: BoxDecoration(
                          shape: BoxShape.circle,
                          color: provider.isProtectionActive
                              ? const Color(0xFF10B981)
                              : const Color(0xFFEF4444),
                          boxShadow: [
                            BoxShadow(
                              color: (provider.isProtectionActive
                                      ? const Color(0xFF10B981)
                                      : const Color(0xFFEF4444))
                                  .withValues(alpha: 0.1),
                              blurRadius: 8,
                              offset: const Offset(0, 4),
                            ),
                          ],
                        ),
                        child: Center(
                          child: Icon(
                            provider.isProtectionActive
                                ? LucideIcons.shield
                                : LucideIcons.shieldAlert,
                            size: 64,
                            color: Colors.white,
                          ),
                        ),
                      ),
                    ),
                    const SizedBox(height: 24),
                    Text(
                      provider.isProtectionActive
                          ? 'Protection Active'
                          : 'Protection Inactive',
                      style: theme.textTheme.titleLarge?.copyWith(
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                    if (!provider.hasAccessibilityPermission)
                      Padding(
                        padding: const EdgeInsets.only(top: 8.0),
                        child: Text(
                          'Accessibility Permission Required',
                          style: TextStyle(
                            color: const Color(0xFFEF4444),
                            fontSize: 14,
                          ),
                        ),
                      ),
                    const SizedBox(height: 40),

                    // Stats
                    Row(
                      children: [
                        Expanded(
                          child: _buildStatCard(
                            context,
                            icon: LucideIcons.activity,
                            iconColor: const Color(0xFF6366F1),
                            value: provider.blockedToday.toString(),
                            label: 'Blocked Today',
                          ),
                        ),
                        const SizedBox(width: 16),
                        Expanded(
                          child: _buildStatCard(
                            context,
                            icon: LucideIcons.timer,
                            iconColor: const Color(0xFF10B981),
                            value: '${(provider.blockedToday * 0.5).round()}m',
                            label: 'Time Saved',
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 32),

                    // Preview Button
                    OutlinedButton.icon(
                      onPressed: () {
                        Navigator.pushNamed(context, '/blocking', arguments: 'YouTube Shorts');
                      },
                      icon: Icon(LucideIcons.eye, size: 20),
                      label: const Text('Preview Block Screen'),
                      style: OutlinedButton.styleFrom(
                        padding: const EdgeInsets.symmetric(vertical: 14, horizontal: 24),
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(12),
                        ),
                      ),
                    ),
                    const SizedBox(height: 32),

                    // Recent Activity
                    Align(
                      alignment: Alignment.centerLeft,
                      child: Text(
                        'Recent Activity',
                        style: theme.textTheme.titleMedium?.copyWith(
                          fontWeight: FontWeight.w600,
                        ),
                      ),
                    ),
                    const SizedBox(height: 16),
                  ],
                ),
              ),
            ),
            
            if (provider.recentActivity.isEmpty)
              SliverToBoxAdapter(
                child: Padding(
                  padding: const EdgeInsets.all(24.0),
                  child: Center(
                    child: Text(
                      'No blocks yet today',
                      style: theme.textTheme.bodyMedium?.copyWith(
                        fontStyle: FontStyle.italic,
                      ),
                    ),
                  ),
                ),
              )
            else
              SliverList(
                delegate: SliverChildBuilderDelegate(
                  (context, index) {
                    final item = provider.recentActivity[index];
                    return Padding(
                      padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 4),
                      child: Container(
                        decoration: BoxDecoration(
                          color: theme.cardColor,
                          borderRadius: BorderRadius.circular(12),
                          border: Border(
                            bottom: BorderSide(
                              color: theme.dividerColor.withValues(alpha: 0.1),
                            ),
                          ),
                        ),
                        padding: const EdgeInsets.all(12),
                        child: Row(
                          children: [
                            Icon(
                              _getPlatformIcon(item.platform),
                              color: _getPlatformColor(item.platform, isDark),
                              size: 24,
                            ),
                            const SizedBox(width: 16),
                            Expanded(
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  Text(
                                    'Blocked ${_getPlatformName(item.platform)}',
                                    style: theme.textTheme.bodyLarge?.copyWith(
                                      fontWeight: FontWeight.w500,
                                    ),
                                  ),
                                  const SizedBox(height: 2),
                                  Text(
                                    _formatTime(item.timestamp),
                                    style: theme.textTheme.bodySmall,
                                  ),
                                ],
                              ),
                            ),
                          ],
                        ),
                      ),
                    );
                  },
                  childCount: provider.recentActivity.length,
                ),
              ),
            const SliverPadding(padding: EdgeInsets.only(bottom: 24)),
          ],
        ),
      ),
    );
  }

  Widget _buildStatCard(
    BuildContext context, {
    required IconData icon,
    required Color iconColor,
    required String value,
    required String label,
  }) {
    final theme = Theme.of(context);
    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: theme.cardColor,
        borderRadius: BorderRadius.circular(16),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.05),
            blurRadius: 4,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: Column(
        children: [
          Icon(icon, color: iconColor, size: 24),
          const SizedBox(height: 8),
          Text(
            value,
            style: theme.textTheme.headlineMedium?.copyWith(
              fontWeight: FontWeight.bold,
              fontSize: 32,
            ),
          ),
          Text(
            label,
            style: theme.textTheme.bodyMedium,
          ),
        ],
      ),
    );
  }
}
