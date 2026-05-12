import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      home: const HomePage(),
    );
  }
}

class HomePage extends StatelessWidget {
  const HomePage({super.key});

  static const platform =
      MethodChannel('overlay_channel');

  Future<void> startOverlay() async {

    try {
      await platform.invokeMethod(
        'startOverlay'
      );
    } catch (_) {}
  }

  @override
  Widget build(BuildContext context) {

    return Scaffold(
      backgroundColor: Colors.black,

      body: Center(
        child: ElevatedButton(

          onPressed: startOverlay,

          child: const Text(
            "START OVERLAY"
          ),
        ),
      ),
    );
  }
}
