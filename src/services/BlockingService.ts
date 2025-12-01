import { NativeModules, NativeEventEmitter, Platform } from 'react-native';

const { BlockingModule } = NativeModules;

const blockingEventEmitter = new NativeEventEmitter(BlockingModule);

export interface PlatformSettings {
    youtubeShorts: boolean;
    instagramReels: boolean;
    facebookReels: boolean;
    tiktok: boolean;
    snapchatSpotlight: boolean;
}

export interface BlockEvent {
    platform: string;
    timestamp: number;
}

class BlockingService {
    static async isAccessibilityEnabled(): Promise<boolean> {
        if (Platform.OS !== 'android') return false;
        return await BlockingModule.isAccessibilityServiceEnabled();
    }

    static openAccessibilitySettings(): void {
        if (Platform.OS !== 'android') return;
        BlockingModule.openAccessibilitySettings();
    }

    static async setProtectionActive(active: boolean): Promise<boolean> {
        if (Platform.OS !== 'android') return false;
        return await BlockingModule.setProtectionActive(active);
    }

    static async isProtectionActive(): Promise<boolean> {
        if (Platform.OS !== 'android') return false;
        return await BlockingModule.isProtectionActive();
    }

    static async setBlockedPlatforms(settings: PlatformSettings): Promise<boolean> {
        if (Platform.OS !== 'android') return false;
        return await BlockingModule.setBlockedPlatforms(
            settings.youtubeShorts,
            settings.instagramReels,
            settings.facebookReels,
            settings.tiktok,
            settings.snapchatSpotlight
        );
    }

    static onContentBlocked(callback: (event: BlockEvent) => void) {
        return blockingEventEmitter.addListener('onContentBlocked', callback);
    }
}

export default BlockingService;
