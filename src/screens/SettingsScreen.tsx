import React from 'react';
import { View, StyleSheet, Switch, Text, ScrollView, TouchableOpacity } from 'react-native';
import { useBlockingStore } from '../store/blockingStore';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useAppTheme } from '../theme';
import { Moon, Sun, Smartphone } from 'lucide-react-native';

const SettingsScreen = () => {
    const { settings, updateSettings, themeMode, setThemeMode } = useBlockingStore();
    const theme = useAppTheme();

    const platforms = [
        { key: 'youtubeShorts', label: 'YouTube Shorts' },
        { key: 'instagramReels', label: 'Instagram Reels' },
        { key: 'facebookReels', label: 'Facebook Reels' },
        { key: 'tiktok', label: 'TikTok' },
        { key: 'snapchatSpotlight', label: 'Snapchat Spotlight' },
    ];

    const ThemeOption = ({ mode, icon: Icon, label }: { mode: 'system' | 'light' | 'dark', icon: any, label: string }) => (
        <TouchableOpacity
            style={[
                styles.themeOption,
                {
                    backgroundColor: themeMode === mode ? theme.primary : theme.cardBackground,
                    borderColor: theme.border,
                    borderWidth: 1
                }
            ]}
            onPress={() => setThemeMode(mode)}
        >
            <Icon size={20} color={themeMode === mode ? '#FFFFFF' : theme.textPrimary} />
            <Text style={[
                styles.themeLabel,
                { color: themeMode === mode ? '#FFFFFF' : theme.textPrimary }
            ]}>
                {label}
            </Text>
        </TouchableOpacity>
    );

    return (
        <SafeAreaView style={[styles.container, { backgroundColor: theme.background }]}>
            <View style={[styles.header, { backgroundColor: theme.cardBackground, borderBottomColor: theme.border }]}>
                <Text style={[styles.title, { color: theme.textPrimary }]}>Settings</Text>
            </View>
            <ScrollView contentContainerStyle={styles.content}>

                <Text style={[styles.sectionTitle, { color: theme.textSecondary }]}>Appearance</Text>
                <View style={styles.themeContainer}>
                    <ThemeOption mode="light" icon={Sun} label="Light" />
                    <ThemeOption mode="dark" icon={Moon} label="Dark" />
                    <ThemeOption mode="system" icon={Smartphone} label="System" />
                </View>

                <Text style={[styles.sectionTitle, { color: theme.textSecondary }]}>Blocked Platforms</Text>
                <View style={[styles.card, { backgroundColor: theme.cardBackground, shadowColor: theme.shadow }]}>
                    {platforms.map((platform, index) => (
                        <View key={platform.key} style={[
                            styles.row,
                            { borderBottomColor: theme.border },
                            index < platforms.length - 1 && styles.borderBottom
                        ]}>
                            <Text style={[styles.label, { color: theme.textPrimary }]}>{platform.label}</Text>
                            <Switch
                                value={settings[platform.key as keyof typeof settings]}
                                onValueChange={(value) => updateSettings(platform.key as any, value)}
                                trackColor={{ false: theme.border, true: theme.primary }}
                                thumbColor={'#FFFFFF'}
                            />
                        </View>
                    ))}
                </View>

                <Text style={[styles.sectionTitle, { color: theme.textSecondary }]}>About</Text>
                <View style={[styles.card, { backgroundColor: theme.cardBackground, shadowColor: theme.shadow }]}>
                    <View style={styles.row}>
                        <Text style={[styles.label, { color: theme.textPrimary }]}>Version</Text>
                        <Text style={[styles.value, { color: theme.textSecondary }]}>1.0.0</Text>
                    </View>
                </View>
            </ScrollView>
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
    content: {
        padding: 24,
    },
    sectionTitle: {
        fontSize: 14,
        fontWeight: '600',
        marginBottom: 12,
        marginTop: 24,
        textTransform: 'uppercase',
        letterSpacing: 1,
    },
    card: {
        borderRadius: 16,
        overflow: 'hidden',
        shadowOffset: { width: 0, height: 2 },
        shadowOpacity: 0.05,
        shadowRadius: 4,
        elevation: 2,
    },
    row: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        padding: 16,
    },
    borderBottom: {
        borderBottomWidth: 1,
    },
    label: {
        fontSize: 16,
        fontWeight: '500',
    },
    value: {
        fontSize: 16,
    },
    themeContainer: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        marginBottom: 8,
    },
    themeOption: {
        flex: 1,
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'center',
        paddingVertical: 12,
        borderRadius: 12,
        marginHorizontal: 4,
    },
    themeLabel: {
        marginLeft: 8,
        fontWeight: '600',
        fontSize: 14,
    },
});

export default SettingsScreen;
