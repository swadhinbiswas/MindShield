import SQLite from 'react-native-sqlite-storage';

SQLite.enablePromise(true);

const DB_NAME = 'FocusGuard.db';

export interface BlockEventRecord {
    id: number;
    platform: string;
    timestamp: number;
}

class DatabaseService {
    private db: SQLite.SQLiteDatabase | null = null;

    async init() {
        try {
            this.db = await SQLite.openDatabase({ name: DB_NAME, location: 'default' });
            await this.createTables();
        } catch (error) {
            console.error('Failed to open database', error);
        }
    }

    private async createTables() {
        if (!this.db) return;
        const query = `
      CREATE TABLE IF NOT EXISTS block_events (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        platform TEXT NOT NULL,
        timestamp INTEGER NOT NULL
      );
    `;
        await this.db.executeSql(query);
    }

    async addBlockEvent(platform: string, timestamp: number) {
        if (!this.db) await this.init();
        if (!this.db) return;

        const query = `INSERT INTO block_events (platform, timestamp) VALUES (?, ?)`;
        await this.db.executeSql(query, [platform, timestamp]);
    }

    async getBlockEvents(limit: number = 50): Promise<BlockEventRecord[]> {
        if (!this.db) await this.init();
        if (!this.db) return [];

        const query = `SELECT * FROM block_events ORDER BY timestamp DESC LIMIT ?`;
        const [results] = await this.db.executeSql(query, [limit]);

        const events: BlockEventRecord[] = [];
        for (let i = 0; i < results.rows.length; i++) {
            events.push(results.rows.item(i));
        }
        return events;
    }

    async getTodayBlockCount(): Promise<number> {
        if (!this.db) await this.init();
        if (!this.db) return 0;

        const startOfDay = new Date();
        startOfDay.setHours(0, 0, 0, 0);
        const timestamp = startOfDay.getTime();

        const query = `SELECT COUNT(*) as count FROM block_events WHERE timestamp >= ?`;
        const [results] = await this.db.executeSql(query, [timestamp]);
        return results.rows.item(0).count;
    }
}

export default new DatabaseService();
