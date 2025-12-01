import { useColorScheme } from 'react-native';
import { useBlockingStore } from '../store/blockingStore';

export const lightTheme = {
    mode: 'light',
    background: '#F3F4F6', // Slightly darker gray for better contrast
    cardBackground: '#FFFFFF',
    textPrimary: '#111827',
    textSecondary: '#6B7280',
    border: '#E5E7EB',
    primary: '#6366F1',
    success: '#10B981',
    danger: '#EF4444',
    shadow: '#000000',
    tabBar: '#FFFFFF',
    tabBarBorder: '#E5E7EB',
};

export const darkTheme = {
    mode: 'dark',
    background: '#111827', // Deep blue-gray
    cardBackground: '#1F2937',
    textPrimary: '#F9FAFB',
    textSecondary: '#9CA3AF',
    border: '#374151',
    primary: '#818CF8',
    success: '#34D399',
    danger: '#F87171',
    shadow: '#000000',
    tabBar: '#1F2937',
    tabBarBorder: '#374151',
};

export const useAppTheme = () => {
    const systemScheme = useColorScheme();
    const { themeMode } = useBlockingStore();

    if (themeMode === 'system') {
        return systemScheme === 'dark' ? darkTheme : lightTheme;
    }

    return themeMode === 'dark' ? darkTheme : lightTheme;
};
