import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:lucide_icons/lucide_icons.dart';
import '../providers/blocking_provider.dart';

class SettingsScreen extends StatelessWidget {
  const SettingsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<BlockingProvider>();
    final theme = Theme.of(context);

    return Scaffold(
      body: SafeArea(
        child: CustomScrollView(
          slivers: [
            SliverToBoxAdapter(
              child: Padding(
                padding: const EdgeInsets.all(24.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Settings',
                      style: theme.textTheme.headlineMedium?.copyWith(
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    const SizedBox(height: 32),

                    // Appearance Section
                    _buildSectionHeader(context, 'Appearance'),
                    const SizedBox(height: 16),
                    Container(
                      decoration: BoxDecoration(
                        color: theme.cardColor,
                        borderRadius: BorderRadius.circular(16),
                      ),
                      child: Column(
                        children: [
                          _buildThemeOption(
                            context,
                            title: 'System',
                            mode: ThemeMode.system,
                            currentMode: provider.themeMode,
                            onTap: () => provider.setThemeMode(ThemeMode.system),
                          ),
                          Divider(height: 1, color: theme.dividerColor.withOpacity(0.1)),
                          _buildThemeOption(
                            context,
                            title: 'Light',
                            mode: ThemeMode.light,
                            currentMode: provider.themeMode,
                            onTap: () => provider.setThemeMode(ThemeMode.light),
                          ),
                          Divider(height: 1, color: theme.dividerColor.withOpacity(0.1)),
                          _buildThemeOption(
                            context,
                            title: 'Dark',
                            mode: ThemeMode.dark,
                            currentMode: provider.themeMode,
                            onTap: () => provider.setThemeMode(ThemeMode.dark),
                          ),
                        ],
                      ),
                    ),
                    const SizedBox(height: 32),

                    // Blocking Rules Section
                    _buildSectionHeader(context, 'Blocking Rules'),
                    const SizedBox(height: 16),
                    Container(
                      decoration: BoxDecoration(
                        color: theme.cardColor,
                        borderRadius: BorderRadius.circular(16),
                      ),
                      child: Column(
                        children: [
                          _buildSwitchTile(
                            context,
                            title: 'YouTube Shorts',
                            value: provider.settings.youtubeShorts,
                            onChanged: (v) => provider.updateSettings('youtubeShorts', v),
                            icon: LucideIcons.youtube,
                            iconColor: Colors.red,
                          ),
                          Divider(height: 1, color: theme.dividerColor.withOpacity(0.1)),
                          _buildSwitchTile(
                            context,
                            title: 'Instagram Reels',
                            value: provider.settings.instagramReels,
                            onChanged: (v) => provider.updateSettings('instagramReels', v),
                            icon: LucideIcons.instagram,
                            iconColor: const Color(0xFFE1306C),
                          ),
                          Divider(height: 1, color: theme.dividerColor.withOpacity(0.1)),
                          _buildSwitchTile(
                            context,
                            title: 'Facebook Reels',
                            value: provider.settings.facebookReels,
                            onChanged: (v) => provider.updateSettings('facebookReels', v),
                            icon: LucideIcons.facebook,
                            iconColor: const Color(0xFF1877F2),
                          ),
                          Divider(height: 1, color: theme.dividerColor.withOpacity(0.1)),
                          _buildSwitchTile(
                            context,
                            title: 'TikTok',
                            value: provider.settings.tiktok,
                            onChanged: (v) => provider.updateSettings('tiktok', v),
                            icon: LucideIcons.video,
                            iconColor: theme.brightness == Brightness.dark ? Colors.white : Colors.black,
                          ),
                          Divider(height: 1, color: theme.dividerColor.withOpacity(0.1)),
                          _buildSwitchTile(
                            context,
                            title: 'Snapchat Spotlight',
                            value: provider.settings.snapchatSpotlight,
                            onChanged: (v) => provider.updateSettings('snapchatSpotlight', v),
                            icon: LucideIcons.ghost,
                            iconColor: const Color(0xFFFFFC00),
                          ),
                        ],
                      ),
                    ),
                    
                    const SizedBox(height: 32),
                    Center(
                      child: Text(
                        'Version 1.0.0',
                        style: theme.textTheme.bodySmall,
                      ),
                    ),
                    const SizedBox(height: 32),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildSectionHeader(BuildContext context, String title) {
    return Text(
      title.toUpperCase(),
      style: Theme.of(context).textTheme.labelSmall?.copyWith(
        fontWeight: FontWeight.bold,
        letterSpacing: 1.2,
        color: Theme.of(context).textTheme.bodyMedium?.color,
      ),
    );
  }

  Widget _buildThemeOption(
    BuildContext context, {
    required String title,
    required ThemeMode mode,
    required ThemeMode currentMode,
    required VoidCallback onTap,
  }) {
    final isSelected = mode == currentMode;
    final theme = Theme.of(context);
    
    return InkWell(
      onTap: onTap,
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 16),
        child: Row(
          children: [
            Text(
              title,
              style: theme.textTheme.bodyLarge?.copyWith(
                fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
                color: isSelected ? theme.primaryColor : theme.textTheme.bodyLarge?.color,
              ),
            ),
            const Spacer(),
            if (isSelected)
              Icon(LucideIcons.check, color: theme.primaryColor, size: 20),
          ],
        ),
      ),
    );
  }

  Widget _buildSwitchTile(
    BuildContext context, {
    required String title,
    required bool value,
    required ValueChanged<bool> onChanged,
    required IconData icon,
    required Color iconColor,
  }) {
    final theme = Theme.of(context);
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 12),
      child: Row(
        children: [
          Icon(icon, color: iconColor, size: 24),
          const SizedBox(width: 16),
          Expanded(
            child: Text(
              title,
              style: theme.textTheme.bodyLarge,
            ),
          ),
          Switch(
            value: value,
            onChanged: onChanged,
            activeColor: theme.primaryColor,
          ),
        ],
      ),
    );
  }
}
