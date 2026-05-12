import 'dart:async';
import 'dart:io';

import 'package:flutter/material.dart';
import 'package:google_mlkit_text_recognition/google_mlkit_text_recognition.dart';
import 'package:google_mlkit_translation/google_mlkit_translation.dart';
import 'package:path_provider/path_provider.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {

    return MaterialApp(
      debugShowCheckedModeBanner: false,
      theme: ThemeData.dark(),
      home: const HomePage(),
    );
  }
}

class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() =>
      _HomePageState();
}

class _HomePageState extends State<HomePage> {

  final recognizer =
      TextRecognizer(
        script:
            TextRecognitionScript.japanese,
      );

  final translator =
      OnDeviceTranslator(
        sourceLanguage:
            TranslateLanguage.japanese,
        targetLanguage:
            TranslateLanguage.english,
      );

  String japanese =
      "Waiting for subtitles...";

  String english =
      "Waiting for translation...";

  String status =
      "Idle";

  Timer? timer;

  @override
  void initState() {

    super.initState();

    startLiveOCR();
  }

  void startLiveOCR() {

    timer = Timer.periodic(
      const Duration(seconds: 3),
      (_) async {

        await processLatestFrame();
      },
    );
  }

  Future<void> processLatestFrame() async {

    try {

      final dir =
          await getApplicationDocumentsDirectory();

      final file = File(
        "${dir.path}/captures/latest_frame.png",
      );

      if (!await file.exists()) {

        setState(() {

          status =
              "Waiting for captured frame...";
        });

        return;
      }

      setState(() {

        status =
            "Processing frame...";
      });

      final inputImage =
          InputImage.fromFilePath(
            file.path,
          );

      final result =
          await recognizer.processImage(
            inputImage,
          );

      final jp =
          result.text.trim();

      if (jp.isEmpty) {

        setState(() {

          status =
              "No Japanese subtitles detected";
        });

        return;
      }

      final translated =
          await translator.translateText(jp);

      setState(() {

        japanese = jp;

        english = translated;

        status = "Live subtitle updated";
      });

    } catch (e) {

      setState(() {

        status =
            "Error: ${e.toString()}";
      });
    }
  }

  @override
  void dispose() {

    timer?.cancel();

    recognizer.close();

    translator.close();

    super.dispose();
  }

  @override
  Widget build(BuildContext context) {

    return Scaffold(

      appBar: AppBar(
        title: const Text(
          "Nihongo Lens Live",
        ),
      ),

      body: Padding(

        padding: const EdgeInsets.all(20),

        child: Column(
          crossAxisAlignment:
              CrossAxisAlignment.start,

          children: [

            const SizedBox(height: 20),

            const Text(
              "STATUS",

              style: TextStyle(
                fontSize: 18,
                color: Colors.white70,
              ),
            ),

            const SizedBox(height: 10),

            Container(

              width: double.infinity,

              padding:
                  const EdgeInsets.all(16),

              decoration: BoxDecoration(
                color: Colors.white10,
                borderRadius:
                    BorderRadius.circular(
                      12,
                    ),
              ),

              child: Text(

                status,

                style: const TextStyle(
                  fontSize: 20,
                ),
              ),
            ),

            const SizedBox(height: 30),

            const Text(
              "Japanese Subtitle",

              style: TextStyle(
                fontSize: 18,
                color: Colors.white70,
              ),
            ),

            const SizedBox(height: 10),

            Container(

              width: double.infinity,

              padding:
                  const EdgeInsets.all(18),

              decoration: BoxDecoration(
                color: Colors.white10,
                borderRadius:
                    BorderRadius.circular(
                      12,
                    ),
              ),

              child: Text(

                japanese,

                style: const TextStyle(
                  fontSize: 24,
                ),
              ),
            ),

            const SizedBox(height: 30),

            const Text(
              "English Translation",

              style: TextStyle(
                fontSize: 18,
                color: Colors.greenAccent,
              ),
            ),

            const SizedBox(height: 10),

            Container(

              width: double.infinity,

              padding:
                  const EdgeInsets.all(18),

              decoration: BoxDecoration(
                color:
                    Colors.green.withOpacity(
                      0.2,
                    ),
                borderRadius:
                    BorderRadius.circular(
                      12,
                    ),
              ),

              child: Text(

                english,

                style: const TextStyle(
                  fontSize: 28,
                  color:
                      Colors.greenAccent,
                  fontWeight:
                      FontWeight.bold,
                ),
              ),
            ),

            const SizedBox(height: 40),

            const Text(

              "LIVE OCR ENGINE ACTIVE\n\n"
              "• Continuous screen frame capture\n"
              "• Japanese OCR detection\n"
              "• English subtitle translation\n"
              "• Real-time subtitle updates",

              style: TextStyle(
                fontSize: 16,
                color: Colors.white54,
                height: 1.6,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
