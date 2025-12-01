import { create } from 'zustand';
import BlockingService, { PlatformSettings, BlockEvent } from '../services/BlockingService';
import DatabaseService from '../services/DatabaseService';

interface BlockingState {
    isProtectionActive: boolean;
    hasAccessibilityPermission: boolean;
    blockedToday: number;
    recentActivity: BlockEvent[];
    settings: PlatformSettings;

    themeMode: 'system' | 'light' | 'dark';
    setThemeMode: (mode: 'system' | 'light' | 'dark') => void;

    checkPermission: () => Promise<void>;
    toggleProtection: () => Promise<void>;
    updateSettings: (key: keyof PlatformSettings, value: boolean) => Promise<void>;
    fetchStats: () => Promise<void>;
    addBlockEvent: (event: BlockEvent) => void;
    init: () => Promise<void>;
}

export const useBlockingStore = create<BlockingState>((set, get) => ({
    isProtectionActive: false,
    hasAccessibilityPermission: false,
    blockedToday: 0,
    recentActivity: [],
    themeMode: 'system',
    settings: {
        youtubeShorts: true,
        instagramReels: true,
        facebookReels: true,
        tiktok: true,
        snapchatSpotlight: true,
    },

    setThemeMode: (mode) => set({ themeMode: mode }),

    checkPermission: async () => {
        const hasPermission = await BlockingService.isAccessibilityEnabled();
        set({ hasAccessibilityPermission: hasPermission });
    },

    toggleProtection: async () => {
        const { isProtectionActive, hasAccessibilityPermission } = get();
        if (!hasAccessibilityPermission) {
            BlockingService.openAccessibilitySettings();
            return;
        }

        const newState = !isProtectionActive;
        await BlockingService.setProtectionActive(newState);
        set({ isProtectionActive: newState });
    },

    updateSettings: async (key, value) => {
        const newSettings = { ...get().settings, [key]: value };
        set({ settings: newSettings });
        await BlockingService.setBlockedPlatforms(newSettings);
    },

    fetchStats: async () => {
        const count = await DatabaseService.getTodayBlockCount();
        const events = await DatabaseService.getBlockEvents(10);
        set({ blockedToday: count, recentActivity: events });
    },

    addBlockEvent: (event) => {
        set((state) => ({
            blockedToday: state.blockedToday + 1,
            recentActivity: [event, ...state.recentActivity].slice(0, 10),
        }));
    },

    init: async () => {
        await DatabaseService.init();
        await get().checkPermission();
        const isActive = await BlockingService.isProtectionActive();
        set({ isProtectionActive: isActive });
        await get().fetchStats();

        // Listen for events
        BlockingService.onContentBlocked(async (event) => {
            await DatabaseService.addBlockEvent(event.platform, event.timestamp);
            get().addBlockEvent(event);
        });
    },
}));
