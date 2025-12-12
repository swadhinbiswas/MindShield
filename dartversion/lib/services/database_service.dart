import 'package:sqflite/sqflite.dart';
import 'package:path/path.dart';

class BlockEventRecord {
  final int? id;
  final String platform;
  final int timestamp;

  BlockEventRecord({this.id, required this.platform, required this.timestamp});

  Map<String, dynamic> toMap() {
    return {
      'id': id,
      'platform': platform,
      'timestamp': timestamp,
    };
  }

  factory BlockEventRecord.fromMap(Map<String, dynamic> map) {
    return BlockEventRecord(
      id: map['id'],
      platform: map['platform'],
      timestamp: map['timestamp'],
    );
  }
}

class DatabaseService {
  static final DatabaseService _instance = DatabaseService._internal();
  static Database? _database;

  factory DatabaseService() {
    return _instance;
  }

  DatabaseService._internal();

  Future<Database> get database async {
    if (_database != null) return _database!;
    _database = await _initDatabase();
    return _database!;
  }

  Future<Database> _initDatabase() async {
    String path = join(await getDatabasesPath(), 'FocusGuard.db');
    return await openDatabase(
      path,
      version: 1,
      onCreate: (db, version) async {
        await db.execute('''
          CREATE TABLE block_events (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            platform TEXT NOT NULL,
            timestamp INTEGER NOT NULL
          )
        ''');
      },
    );
  }

  Future<void> addBlockEvent(String platform, int timestamp) async {
    final db = await database;
    await db.insert(
      'block_events',
      {'platform': platform, 'timestamp': timestamp},
      conflictAlgorithm: ConflictAlgorithm.replace,
    );
  }

  Future<List<BlockEventRecord>> getBlockEvents({int limit = 50}) async {
    final db = await database;
    final List<Map<String, dynamic>> maps = await db.query(
      'block_events',
      orderBy: 'timestamp DESC',
      limit: limit,
    );

    return List.generate(maps.length, (i) {
      return BlockEventRecord.fromMap(maps[i]);
    });
  }

  Future<int> getTodayBlockCount() async {
    final db = await database;
    final now = DateTime.now();
    final startOfDay = DateTime(now.year, now.month, now.day).millisecondsSinceEpoch;

    final result = await db.rawQuery(
      'SELECT COUNT(*) as count FROM block_events WHERE timestamp >= ?',
      [startOfDay],
    );

    return Sqflite.firstIntValue(result) ?? 0;
  }
}
