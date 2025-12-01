import React, { useEffect } from 'react';
import { View, StyleSheet, TouchableOpacity, Text, AppState, useColorScheme, ScrollView, FlatList } from 'react-native';
import { useBlockingStore } from '../store/blockingStore';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Activity, Shield, ShieldAlert, Timer, Facebook, Instagram, Youtube, Video, Ghost, Smartphone, Eye } from 'lucide-react-native';
import { useAppTheme } from '../theme';
import { useNavigation } from '@react-navigation/native';

const HomeScreen = () => {
    const theme = useAppTheme();
    const { themeMode } = useBlockingStore();
    const isDark = theme.mode === 'dark';
    const navigation = useNavigation<any>();

    const {
        isProtectionActive,
        hasAccessibilityPermission,
        blockedToday,
        recentActivity,
        toggleProtection,
        checkPermission,
        init
    } = useBlockingStore();

    useEffect(() => {
        init();
        const subscription = AppState.addEventListener('change', nextAppState => {
            if (nextAppState === 'active') {
                checkPermission();
            }
        });

        return () => {
            subscription.remove();
        };
    }, []);

    const getPlatformIcon = (platform: string) => {
        const p = platform.toLowerCase();
        if (p.includes('youtube')) return <Youtube size={24} color="#FF0000" />;
        if (p.includes('instagram')) return <Instagram size={24} color="#E1306C" />;
        if (p.includes('facebook')) return <Facebook size={24} color="#1877F2" />;
        if (p.includes('tiktok')) return <Video size={24} color={isDark ? "#FFFFFF" : "#000000"} />; // TikTok doesn't have a standard Lucide icon, using Video
        if (p.includes('snapchat')) return <Ghost size={24} color="#FFFC00" />;
        return <Smartphone size={24} color={theme.textSecondary} />;
    };

    const getPlatformName = (platform: string) => {
        if (platform.includes('YouTube')) return 'YouTube Shorts';
        if (platform.includes('Instagram')) return 'Instagram Reels';
        if (platform.includes('Facebook')) return 'Facebook Reels';
        if (platform.includes('TikTok')) return 'TikTok';
        if (platform.includes('Snapchat')) return 'Snapchat';
        return platform;
    };

    const formatTime = (timestamp: number) => {
        const date = new Date(timestamp);
        return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    };

    return (
        <SafeAreaView style={[styles.container, { backgroundColor: theme.background }]}>
            <View style={styles.header}>
                <Text style={[styles.title, { color: theme.textPrimary }]}>MindShield</Text>
            </View>

            <ScrollView contentContainerStyle={styles.scrollContent}>
                <View style={styles.statusContainer}>
                    <TouchableOpacity
                        style={[
                            styles.toggleButton,
                            isProtectionActive ? styles.activeButton : styles.inactiveButton,
                            { shadowColor: theme.shadow }
                        ]}
                        onPress={toggleProtection}
                    >
                        {isProtectionActive ? (
                            <Shield size={64} color="#FFFFFF" />
                        ) : (
                            <ShieldAlert size={64} color="#FFFFFF" />
                        )}
                    </TouchableOpacity>
                    <Text style={[styles.statusText, { color: theme.textPrimary }]}>
                        {isProtectionActive ? 'Protection Active' : 'Protection Inactive'}
                    </Text>
                    {!hasAccessibilityPermission && (
                        <Text style={styles.warningText}>
                            Accessibility Permission Required
                        </Text>
                    )}
                </View>

                <View style={styles.statsContainer}>
                    <View style={[styles.statCard, { backgroundColor: theme.cardBackground, shadowColor: theme.shadow }]}>
                        <Activity size={24} color="#6366F1" />
                        <Text style={[styles.statValue, { color: theme.textPrimary }]}>{blockedToday}</Text>
                        <Text style={[styles.statLabel, { color: theme.textSecondary }]}>Blocked Today</Text>
                    </View>
                    <View style={[styles.statCard, { backgroundColor: theme.cardBackground, shadowColor: theme.shadow }]}>
                        <Timer size={24} color="#10B981" />
                        <Text style={[styles.statValue, { color: theme.textPrimary }]}>
                            {Math.round(blockedToday * 0.5)}m
                        </Text>
                        <Text style={[styles.statLabel, { color: theme.textSecondary }]}>Time Saved</Text>
                    </View>
                </View>

                {/* Preview Blocking Screen Button */}
                <TouchableOpacity
                    style={[styles.previewButton, { backgroundColor: theme.cardBackground, borderColor: theme.primary }]}
                    onPress={() => navigation.navigate('Blocking', { platform: 'YouTube Shorts' })}
                >
                    <Eye size={20} color={theme.primary} />
                    <Text style={[styles.previewButtonText, { color: theme.primary }]}>
                        Preview Block Screen
                    </Text>
                </TouchableOpacity>

                <View style={styles.recentContainer}>
                    <Text style={[styles.sectionTitle, { color: theme.textPrimary }]}>Recent Activity</Text>
                    {recentActivity.length === 0 ? (
                        <Text style={[styles.emptyText, { color: theme.textSecondary }]}>No blocks yet today</Text>
                    ) : (
                        recentActivity.map((item, index) => (
                            <View key={`${item.timestamp}-${index}`} style={[styles.activityItem, { backgroundColor: theme.cardBackground, borderBottomColor: theme.border }]}>
                                <View style={styles.activityIcon}>
                                    {getPlatformIcon(item.platform)}
                                </View>
                                <View style={styles.activityInfo}>
                                    <Text style={[styles.activityTitle, { color: theme.textPrimary }]}>
                                        Blocked {getPlatformName(item.platform)}
                                    </Text>
                                    <Text style={[styles.activityTime, { color: theme.textSecondary }]}>
                                        {formatTime(item.timestamp)}
                                    </Text>
                                </View>
                            </View>
                        ))
                    )}
                </View>
            </ScrollView>
        </SafeAreaView>
    );
};



const styles = StyleSheet.create({
    container: {
        flex: 1,
    },
    scrollContent: {
        padding: 24,
    },
    header: {
        paddingVertical: 12,
        alignItems: 'center',
    },
    title: {
        fontSize: 24,
        fontWeight: 'bold',
    },
    statusContainer: {
        alignItems: 'center',
        marginTop: 20,
        marginBottom: 40,
    },
    toggleButton: {
        width: 160,
        height: 160,
        borderRadius: 80,
        justifyContent: 'center',
        alignItems: 'center',
        shadowOffset: { width: 0, height: 4 },
        shadowOpacity: 0.3,
        shadowRadius: 8,
        elevation: 8,
        marginBottom: 24,
    },
    activeButton: {
        backgroundColor: '#10B981',
    },
    inactiveButton: {
        backgroundColor: '#EF4444',
    },
    statusText: {
        fontSize: 20,
        fontWeight: '600',
    },
    warningText: {
        marginTop: 8,
        color: '#EF4444',
        fontSize: 14,
    },
    statsContainer: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        marginBottom: 40,
    },
    statCard: {
        flex: 1,
        padding: 20,
        borderRadius: 16,
        marginHorizontal: 8,
        alignItems: 'center',
        shadowOffset: { width: 0, height: 2 },
        shadowOpacity: 0.1,
        shadowRadius: 4,
        elevation: 2,
    },
    statValue: {
        fontSize: 32,
        fontWeight: 'bold',
        marginVertical: 8,
    },
    statLabel: {
        fontSize: 14,
    },
    previewButton: {
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'center',
        paddingVertical: 14,
        paddingHorizontal: 24,
        borderRadius: 12,
        borderWidth: 2,
        marginBottom: 32,
        gap: 8,
    },
    previewButtonText: {
        fontSize: 16,
        fontWeight: '600',
    },
    recentContainer: {
        flex: 1,
    },
    sectionTitle: {
        fontSize: 18,
        fontWeight: '600',
        marginBottom: 16,
    },
    emptyText: {
        textAlign: 'center',
        fontStyle: 'italic',
        marginTop: 20,
    },
    activityItem: {
        flexDirection: 'row',
        alignItems: 'center',
        paddingVertical: 12,
        paddingHorizontal: 16,
        borderRadius: 12,
        marginBottom: 8,
    },
    activityIcon: {
        marginRight: 16,
    },
    activityInfo: {
        flex: 1,
    },
    activityTitle: {
        fontSize: 16,
        fontWeight: '500',
    },
    activityTime: {
        fontSize: 12,
        marginTop: 2,
    },
});

export default HomeScreen;
