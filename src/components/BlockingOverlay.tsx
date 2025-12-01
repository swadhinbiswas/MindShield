import React, { useEffect, useState } from 'react';
import { View, Text, StyleSheet, Dimensions } from 'react-native';
import Svg, { Circle, Path, G, Defs, LinearGradient, Stop, Rect } from 'react-native-svg';
import { useAppTheme } from '../theme';

const { width } = Dimensions.get('window');

// Random warning messages categorized by tone
const warningMessages = {
    firm: [
        "Focus! No scrolling right now.",
        "MindShield activated: Stay on task.",
        "Access denied. Your goals come first.",
        "Not today. Stay disciplined.",
    ],
    friendly: [
        "Hey there! Let's give your brain a break.",
        "Oops! You hit the distraction wall.",
        "Whoa! Almost got you there. 😊",
        "Nice try! But let's stay focused.",
    ],
    motivational: [
        "Your time is precious. Protect it!",
        "Keep your focus sharp. MindShield says no.",
        "Champions don't get distracted. Be one!",
        "Every second counts. Use it wisely!",
        "Your future self will thank you.",
    ],
};

// Get all messages in a flat array with their category
const allMessages = [
    ...warningMessages.firm.map(msg => ({ text: msg, category: 'firm' as const })),
    ...warningMessages.friendly.map(msg => ({ text: msg, category: 'friendly' as const })),
    ...warningMessages.motivational.map(msg => ({ text: msg, category: 'motivational' as const })),
];

const getRandomMessage = () => {
    const randomIndex = Math.floor(Math.random() * allMessages.length);
    return allMessages[randomIndex];
};

// Shield SVG Component
const ShieldIllustration = ({ isDark }: { isDark: boolean }) => {
    const primaryColor = isDark ? '#818CF8' : '#6366F1';
    const secondaryColor = isDark ? '#34D399' : '#10B981';
    const bgColor = isDark ? '#1F2937' : '#E0E7FF';

    return (
        <Svg width={width * 0.6} height={width * 0.6} viewBox="0 0 200 200">
            <Defs>
                <LinearGradient id="shieldGradient" x1="0%" y1="0%" x2="100%" y2="100%">
                    <Stop offset="0%" stopColor={primaryColor} />
                    <Stop offset="100%" stopColor={secondaryColor} />
                </LinearGradient>
                <LinearGradient id="glowGradient" x1="0%" y1="0%" x2="0%" y2="100%">
                    <Stop offset="0%" stopColor={primaryColor} stopOpacity="0.3" />
                    <Stop offset="100%" stopColor={primaryColor} stopOpacity="0" />
                </LinearGradient>
            </Defs>

            {/* Background glow circle */}
            <Circle cx="100" cy="100" r="90" fill="url(#glowGradient)" />

            {/* Outer decorative ring */}
            <Circle
                cx="100"
                cy="100"
                r="80"
                fill="none"
                stroke={bgColor}
                strokeWidth="2"
                strokeDasharray="10 5"
            />

            {/* Shield shape */}
            <Path
                d="M100 25
                   C100 25 150 35 165 50
                   C165 50 170 100 165 130
                   C160 160 130 180 100 190
                   C70 180 40 160 35 130
                   C30 100 35 50 35 50
                   C50 35 100 25 100 25Z"
                fill="url(#shieldGradient)"
            />

            {/* Inner shield highlight */}
            <Path
                d="M100 40
                   C100 40 140 48 152 60
                   C152 60 156 100 152 125
                   C148 150 125 167 100 175
                   C75 167 52 150 48 125
                   C44 100 48 60 48 60
                   C60 48 100 40 100 40Z"
                fill="none"
                stroke="rgba(255,255,255,0.3)"
                strokeWidth="2"
            />

            {/* Checkmark / Block symbol */}
            <G>
                {/* X mark for blocked */}
                <Path
                    d="M75 85 L125 135 M125 85 L75 135"
                    stroke="#FFFFFF"
                    strokeWidth="8"
                    strokeLinecap="round"
                />
            </G>

            {/* Small decorative elements */}
            <Circle cx="45" cy="45" r="4" fill={secondaryColor} opacity="0.6" />
            <Circle cx="155" cy="45" r="4" fill={secondaryColor} opacity="0.6" />
            <Circle cx="30" cy="100" r="3" fill={primaryColor} opacity="0.4" />
            <Circle cx="170" cy="100" r="3" fill={primaryColor} opacity="0.4" />

            {/* Sparkle effects */}
            <Path
                d="M160 60 L165 55 L160 50 L155 55 Z"
                fill="#FFFFFF"
                opacity="0.8"
            />
            <Path
                d="M40 70 L45 65 L40 60 L35 65 Z"
                fill="#FFFFFF"
                opacity="0.6"
            />
        </Svg>
    );
};

interface BlockingOverlayProps {
    platform?: string;
    visible?: boolean;
}

const BlockingOverlay: React.FC<BlockingOverlayProps> = ({ platform, visible = true }) => {
    const theme = useAppTheme();
    const isDark = theme.mode === 'dark';
    const [message, setMessage] = useState(getRandomMessage());

    useEffect(() => {
        // Get a new random message when component mounts or becomes visible
        if (visible) {
            setMessage(getRandomMessage());
        }
    }, [visible]);

    if (!visible) return null;

    const getCategoryEmoji = (category: string) => {
        switch (category) {
            case 'firm': return '🛡️';
            case 'friendly': return '👋';
            case 'motivational': return '💪';
            default: return '🛡️';
        }
    };

    const getCategoryLabel = (category: string) => {
        switch (category) {
            case 'firm': return 'Protection Active';
            case 'friendly': return 'Friendly Reminder';
            case 'motivational': return 'Stay Motivated';
            default: return 'MindShield';
        }
    };

    return (
        <View style={[styles.container, { backgroundColor: theme.background }]}>
            <View style={styles.content}>
                <ShieldIllustration isDark={isDark} />

                <View style={styles.messageContainer}>
                    <Text style={[styles.categoryLabel, { color: theme.primary }]}>
                        {getCategoryEmoji(message.category)} {getCategoryLabel(message.category)}
                    </Text>

                    <Text style={[styles.messageText, { color: theme.textPrimary }]}>
                        {message.text}
                    </Text>

                    {platform && (
                        <View style={[styles.platformBadge, { backgroundColor: theme.cardBackground }]}>
                            <Text style={[styles.platformText, { color: theme.textSecondary }]}>
                                Blocked: {platform}
                            </Text>
                        </View>
                    )}
                </View>

                <Text style={[styles.footerText, { color: theme.textSecondary }]}>
                    MindShield is protecting your focus
                </Text>
            </View>
        </View>
    );
};

const styles = StyleSheet.create({
    container: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
        padding: 24,
    },
    content: {
        alignItems: 'center',
        justifyContent: 'center',
    },
    messageContainer: {
        alignItems: 'center',
        marginTop: 32,
        paddingHorizontal: 24,
    },
    categoryLabel: {
        fontSize: 14,
        fontWeight: '600',
        textTransform: 'uppercase',
        letterSpacing: 1,
        marginBottom: 12,
    },
    messageText: {
        fontSize: 24,
        fontWeight: 'bold',
        textAlign: 'center',
        lineHeight: 32,
    },
    platformBadge: {
        marginTop: 20,
        paddingHorizontal: 16,
        paddingVertical: 8,
        borderRadius: 20,
    },
    platformText: {
        fontSize: 14,
        fontWeight: '500',
    },
    footerText: {
        marginTop: 48,
        fontSize: 12,
    },
});

export default BlockingOverlay;
