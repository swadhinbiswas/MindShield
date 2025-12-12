import 'package:flutter/material.dart';

class AppTheme {
  static final lightTheme = ThemeData(
    useMaterial3: true,
    brightness: Brightness.light,
    colorScheme: ColorScheme.fromSeed(
      seedColor: const Color(0xFF818CF8),
      brightness: Brightness.light,
    ),
    scaffoldBackgroundColor: const Color(0xFFF3F4F6),
    cardColor: Colors.white,
    textTheme: const TextTheme(
      displayLarge: TextStyle(color: Color(0xFF1F2937), fontWeight: FontWeight.bold),
      bodyLarge: TextStyle(color: Color(0xFF374151)),
      bodyMedium: TextStyle(color: Color(0xFF6B7280)),
    ),
  );

  static final darkTheme = ThemeData(
    useMaterial3: true,
    brightness: Brightness.dark,
    colorScheme: ColorScheme.fromSeed(
      seedColor: const Color(0xFF818CF8),
      brightness: Brightness.dark,
      surface: const Color(0xFF1F2937),
    ),
    scaffoldBackgroundColor: const Color(0xFF111827),
    cardColor: const Color(0xFF1F2937),
    textTheme: const TextTheme(
      displayLarge: TextStyle(color: Color(0xFFF9FAFB), fontWeight: FontWeight.bold),
      bodyLarge: TextStyle(color: Color(0xFFD1D5DB)),
      bodyMedium: TextStyle(color: Color(0xFF9CA3AF)),
    ),
  );
}
