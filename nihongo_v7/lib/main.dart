import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:google_mlkit_text_recognition/google_mlkit_text_recognition.dart';
import 'package:google_mlkit_translation/google_mlkit_translation.dart';

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
      MethodChannel('nihongo_lens/capture');

  final recognizer = TextRecognizer(
    script: TextRecognitionScript.japanese,
  );

  final translator = OnDeviceTranslator(
    sourceLanguage: TranslateLanguage.japanese,
    targetLanguage: TranslateLanguage.english,
  );

  String japanese = 'Waiting for subtitles...';
  String english = 'Waiting for translation...';

  Future<void> startCapture() async {

    await platform.invokeMethod('startCapture');

    setState(() {
      japanese = 'Screen capture started';
      english = 'OCR pipeline ready';
    });
  }

  @override
  Widget build(BuildContext context) {

    return Scaffold(
      backgroundColor: Colors.black,

      appBar: AppBar(
        title: const Text('Nihongo Lens Live OCR'),
        backgroundColor: Colors.black,
      ),

      floatingActionButton: FloatingActionButton(
        onPressed: startCapture,
        child: const Icon(Icons.play_arrow),
      ),

      body: Padding(
        padding: const EdgeInsets.all(20),

        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,

          children: [

            const Text(
              'Japanese Subtitle',
              style: TextStyle(
                color: Colors.white70,
                fontSize: 18,
              ),
            ),

            const SizedBox(height: 12),

            Container(
              width: double.infinity,
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: Colors.white10,
                borderRadius: BorderRadius.circular(12),
              ),
              child: Text(
                japanese,
                style: const TextStyle(
                  color: Colors.white,
                  fontSize: 24,
                ),
              ),
            ),

            const SizedBox(height: 30),

            const Text(
              'English Translation',
              style: TextStyle(
                color: Colors.greenAccent,
                fontSize: 18,
              ),
            ),

            const SizedBox(height: 12),

            Container(
              width: double.infinity,
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: Colors.green.withOpacity(0.2),
                borderRadius: BorderRadius.circular(12),
              ),
              child: Text(
                english,
                style: const TextStyle(
                  color: Colors.greenAccent,
                  fontSize: 28,
                  fontWeight: FontWeight.bold,
                ),
              ),
            ),

            const Spacer(),

            const Text(
              'Press play button to start live OCR capture.',
              style: TextStyle(
                color: Colors.white54,
              ),
            )
          ],
        ),
      ),
    );
  }
}
