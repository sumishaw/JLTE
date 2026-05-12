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

  String lastSubtitle = "";

  Timer? timer;

  bool processing = false;

  @override
  void initState() {

    super.initState();

    startLiveOCR();
  }

  void startLiveOCR() {

    timer = Timer.periodic(
      const Duration(milliseconds: 1200),
      (_) async {

        if (!processing) {

          processing = true;

          await processLatestFrame();

          processing = false;
        }
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
              "Waiting for captured frames...";
        });

        return;
      }

      setState(() {

        status =
            "Scanning subtitles...";
      });

      final inputImage =
          InputImage.fromFilePath(
            file.path,
          );

      final result =
          await recognizer.processImage(
            inputImage,
          );

      String jp =
          result.text.trim();

      if (jp.isEmpty) {

        setState(() {

          status =
              "No subtitles detected";
        });

        return;
      }

      // Cleanup OCR noise

      jp = cleanupJapaneseText(jp);

      if (jp.isEmpty) return;

      // Duplicate filtering

      if (jp == lastSubtitle) {

        setState(() {

          status =
              "Waiting for new subtitle...";
        });

        return;
      }

      lastSubtitle = jp;

      final translated =
          await translator.translateText(jp);

      setState(() {

        japanese = jp;

        english = translated;

        status =
            "Live subtitle updated";
      });

    } catch (e) {

      setState(() {

        status =
            "Error: ${e.toString()}";
      });
    }
  }

  String cleanupJapaneseText(
      String text) {

    final lines =
        text
            .split("\n")
            .map((e) => e.trim())
            .where((e) {

      if (e.isEmpty) return false;

      // Ignore tiny garbage

      if (e.length < 2) return false;

      // Must contain Japanese chars

      return RegExp(
        r'[\u3040-\u30ff\u4e00-\u9faf]'
      ).hasMatch(e);

    }).toList();

    return lines.join("\n");
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

              "LIVE OCR ENGINE\n\n"
              "• Continuous frame capture\n"
              "• Japanese subtitle filtering\n"
              "• Duplicate subtitle removal\n"
              "• Real-time translation updates\n"
              "• Optimized OCR loop",

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
