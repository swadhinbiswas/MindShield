import React, { useEffect } from 'react';
import { View, StyleSheet, FlatList, Text } from 'react-native';
import { useBlockingStore } from '../store/blockingStore';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useAppTheme } from '../theme';
import { Facebook, Instagram, Youtube, Video, Ghost, Smartphone } from 'lucide-react-native';

const StatisticsScreen = () => {
    const { recentActivity, fetchStats, themeMode } = useBlockingStore();
    const theme = useAppTheme();

    useEffect(() => {
        fetchStats();
    }, []);

    const getPlatformIcon = (platform: string) => {
        const p = platform.toLowerCase();
        if (p.includes('youtube')) return <Youtube size={20} color="#FF0000" />;
        if (p.includes('instagram')) return <Instagram size={20} color="#E1306C" />;
        if (p.includes('facebook')) return <Facebook size={20} color="#1877F2" />;
        if (p.includes('tiktok')) return <Video size={20} color={themeMode === 'dark' ? "#FFFFFF" : "#000000"} />;
        if (p.includes('snapchat')) return <Ghost size={20} color="#FFFC00" />;
        return <Smartphone size={20} color={theme.textSecondary} />;
    };

    const renderItem = ({ item }: { item: any }) => (
        <View style={[styles.item, { backgroundColor: theme.cardBackground, shadowColor: theme.shadow }]}>
            <View style={[styles.iconContainer, { backgroundColor: theme.background }]}>
                {getPlatformIcon(item.platform)}
            </View>
            <View style={styles.itemContent}>
                <Text style={[styles.platformText, { color: theme.textPrimary }]}>{item.platform}</Text>
                <Text style={[styles.timeText, { color: theme.textSecondary }]}>
                    {new Date(item.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                </Text>
            </View>
        </View>
    );

    return (
        <SafeAreaView style={[styles.container, { backgroundColor: theme.background }]}>
            <View style={[styles.header, { backgroundColor: theme.cardBackground, borderBottomColor: theme.border }]}>
                <Text style={[styles.title, { color: theme.textPrimary }]}>Recent Activity</Text>
            </View>
            <FlatList
                data={recentActivity}
                renderItem={renderItem}
                keyExtractor={(item, index) => index.toString()}
                contentContainerStyle={styles.listContent}
                ListEmptyComponent={
                    <View style={styles.emptyContainer}>
                        <Text style={[styles.emptyText, { color: theme.textSecondary }]}>No blocks yet</Text>
                    </View>
                }
            />
        </SafeAreaView>
    );
};

const styles = StyleSheet.create({
    container: {
        flex: 1,
    },
    header: {
        padding: 24,
        borderBottomWidth: 1,
    },
    title: {
        fontSize: 24,
        fontWeight: 'bold',
    },
    listContent: {
        padding: 16,
    },
    item: {
        flexDirection: 'row',
        alignItems: 'center',
        padding: 16,
        borderRadius: 16,
        marginBottom: 12,
        shadowOffset: { width: 0, height: 2 },
        shadowOpacity: 0.05,
        shadowRadius: 4,
        elevation: 2,
    },
    iconContainer: {
        width: 48,
        height: 48,
        borderRadius: 24,
        justifyContent: 'center',
        alignItems: 'center',
        marginRight: 16,
    },
    itemContent: {
        flex: 1,
    },
    platformText: {
        fontSize: 16,
        fontWeight: '600',
        marginBottom: 4,
    },
    timeText: {
        fontSize: 14,
    },
    emptyContainer: {
        padding: 40,
        alignItems: 'center',
    },
    emptyText: {
        fontSize: 16,
        fontStyle: 'italic',
    },
});

export default StatisticsScreen;
