import 'package:flutter/material.dart';

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

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF111111),
      body: Center(
        child: Container(
          padding: const EdgeInsets.all(20),
          child: const Text(
            "Nihongo Lens Running",
            style: TextStyle(
              color: Colors.white,
              fontSize: 24,
            ),
          ),
        ),
      ),
    );
  }
}
