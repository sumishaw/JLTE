import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
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

class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {

  static const platform =
      MethodChannel('nihongo_lens/captions');

  String japaneseText =
      "Waiting for Japanese subtitles...";

  String englishText =
      "Waiting for English translation...";

  @override
  void initState() {
    super.initState();

    platform.setMethodCallHandler(_handleMethod);
  }

  Future<void> _handleMethod(MethodCall call) async {

    if (call.method == "onCaption") {

      final text = call.arguments.toString();

      setState(() {

        japaneseText = text;

        englishText = fakeTranslate(text);
      });
    }
  }

  String fakeTranslate(String text) {

    if (text.contains("うん")) {
      return "Yeah.";
    }

    if (text.contains("大事")) {
      return "Take good care of it.";
    }

    return "English translation coming soon...";
  }

  @override
  Widget build(BuildContext context) {

    return Scaffold(
      backgroundColor: Colors.black,
      appBar: AppBar(
        title: const Text("Nihongo Lens"),
        backgroundColor: Colors.black,
      ),
      body: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment:
              CrossAxisAlignment.start,
          children: [

            const Text(
              "Japanese Subtitle",
              style: TextStyle(
                color: Colors.white70,
                fontSize: 18,
              ),
            ),

            const SizedBox(height: 10),

            Container(
              width: double.infinity,
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: Colors.white10,
                borderRadius:
                    BorderRadius.circular(12),
              ),
              child: Text(
                japaneseText,
                style: const TextStyle(
                  color: Colors.white,
                  fontSize: 26,
                ),
              ),
            ),

            const SizedBox(height: 30),

            const Text(
              "English Translation",
              style: TextStyle(
                color: Colors.greenAccent,
                fontSize: 18,
              ),
            ),

            const SizedBox(height: 10),

            Container(
              width: double.infinity,
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: Colors.green.withOpacity(0.2),
                borderRadius:
                    BorderRadius.circular(12),
              ),
              child: Text(
                englishText,
                style: const TextStyle(
                  color: Colors.greenAccent,
                  fontSize: 30,
                  fontWeight: FontWeight.bold,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
