import 'package:flutter_test/flutter_test.dart';
import 'package:dartversion/main.dart';

import 'package:sqflite_common_ffi/sqflite_ffi.dart';

void main() {
  setUpAll(() {
    // Initialize FFI
    sqfliteFfiInit();
    // Change the default factory
    databaseFactory = databaseFactoryFfi;
  });

  testWidgets('App builds and shows Home screen', (WidgetTester tester) async {
    // Build our app and trigger a frame.
    await tester.pumpWidget(const MyApp());

    // Verify that the Home screen is displayed
    expect(find.text('MindShield'), findsOneWidget);
    expect(find.text('Protection Inactive'), findsOneWidget);
  });
}
