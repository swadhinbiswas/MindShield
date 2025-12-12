import 'package:flutter/material.dart';
import 'package:lucide_icons/lucide_icons.dart';
import 'dart:math';

class BlockingScreen extends StatelessWidget {
  final String platform;

  const BlockingScreen({super.key, required this.platform});

  @override
  Widget build(BuildContext context) {
    // This is a preview of what the native overlay looks like
    // We'll try to replicate the native overlay style here for consistency
    
    final messages = [
      "Focus! No scrolling right now.",
      "MindShield activated: Stay on task.",
      "Access denied. Your goals come first.",
      "Not today. Stay disciplined.",
      "Distractions blocked. Keep going!"
    ];
    
    final message = messages[Random().nextInt(messages.length)];

    return Scaffold(
      backgroundColor: const Color(0xFF111827),
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(32.0),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              // Shield Icon
              Container(
                width: 180,
                height: 180,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  gradient: LinearGradient(
                    begin: Alignment.topCenter,
                    end: Alignment.bottomCenter,
                    colors: [
                      const Color(0xFF818CF8).withValues(alpha: 0.2),
                      Colors.transparent,
                    ],
                  ),
                ),
                child: const Center(
                  child: Icon(
                    LucideIcons.shield,
                    size: 100,
                    color: Color(0xFF818CF8),
                  ),
                ),
              ),
              const SizedBox(height: 32),
              
              // Category Label
              const Text(
                '🛡️ PROTECTION ACTIVE',
                style: TextStyle(
                  color: Color(0xFF818CF8),
                  fontSize: 14,
                  fontWeight: FontWeight.bold,
                  letterSpacing: 1.2,
                ),
              ),
              const SizedBox(height: 12),
              
              // Message
              Text(
                message,
                textAlign: TextAlign.center,
                style: const TextStyle(
                  color: Color(0xFFF9FAFB),
                  fontSize: 22,
                  fontWeight: FontWeight.bold,
                  height: 1.5,
                ),
              ),
              
              const SizedBox(height: 20),
              
              // Platform Badge
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                decoration: BoxDecoration(
                  color: const Color(0xFF1F2937),
                  borderRadius: BorderRadius.circular(4),
                ),
                child: Text(
                  'Blocked: $platform',
                  style: const TextStyle(
                    color: Color(0xFF9CA3AF),
                    fontSize: 14,
                  ),
                ),
              ),
              
              const SizedBox(height: 48),
              
              // Footer
              const Text(
                'MindShield is protecting your focus',
                style: TextStyle(
                  color: Color(0xFF6B7280),
                  fontSize: 12,
                ),
              ),
              
              const SizedBox(height: 48),
              
              // Close Button (Only for preview)
              OutlinedButton(
                onPressed: () => Navigator.pop(context),
                style: OutlinedButton.styleFrom(
                  side: const BorderSide(color: Color(0xFF4B5563)),
                  foregroundColor: Colors.white,
                ),
                child: const Text('Close Preview'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
