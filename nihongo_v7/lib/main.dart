import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import 'package:google_mlkit_text_recognition/google_mlkit_text_recognition.dart';

import 'package:translator/translator.dart';

void main() {

  WidgetsFlutterBinding.ensureInitialized();

  runApp(
    const MyApp(),
  );
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
  State<HomePage> createState() =>
      _HomePageState();
}

class _HomePageState
    extends State<HomePage> {

  final translator =
      GoogleTranslator();

  final textRecognizer =
      TextRecognizer(
        script:
        TextRecognitionScript.japanese,
      );

  static const platform =
      MethodChannel(
        'overlay_channel',
      );

  String japaneseText =
      "Waiting for Japanese subtitles...";

  String englishText =
      "Waiting for English translation...";

  Timer? timer;

  bool isRunning = false;

  Future<void> startSystem() async {

    if (isRunning) return;

    isRunning = true;

    try {

      await platform.invokeMethod(
        'startOverlay',
      );

    } catch (e) {

      print(e);
    }

    timer = Timer.periodic(

      const Duration(
        seconds: 3,
      ),

      (_) async {

        await detectJapaneseText();
      },
    );
  }

  Future<void> detectJapaneseText() async {

    try {

      // PLACEHOLDER:
      // Replace later with real screenshot OCR

      final fakeJapanese =
          "こんにちは";

      japaneseText =
          fakeJapanese;

      final translation =
          await translator.translate(
        fakeJapanese,
        from: 'ja',
        to: 'en',
      );

      englishText =
          translation.text;

      setState(() {});

      try {

        await platform.invokeMethod(
          'updateOverlay',
          {
            "text":
            englishText,
          },
        );

      } catch (e) {

        print(e);
      }

    } catch (e) {

      print(e);
    }
  }

  @override
  void dispose() {

    timer?.cancel();

    textRecognizer.close();

    super.dispose();
  }

  @override
  Widget build(BuildContext context) {

    return Scaffold(

      backgroundColor:
      Colors.black,

      body: Padding(

        padding:
        const EdgeInsets.all(
          20,
        ),

        child: Column(

          mainAxisAlignment:
          MainAxisAlignment
              .center,

          crossAxisAlignment:
          CrossAxisAlignment
              .start,

          children: [

            Text(

              japaneseText,

              style:
              const TextStyle(

                color:
                Colors.white,

                fontSize: 26,
              ),
            ),

            const SizedBox(
              height: 30,
            ),

            Text(

              englishText,

              style:
              const TextStyle(

                color: Colors
                    .greenAccent,

                fontSize: 34,

                fontWeight:
                FontWeight.bold,
              ),
            ),

            const SizedBox(
              height: 50,
            ),

            ElevatedButton(

              onPressed:
              startSystem,

              child: const Text(
                "START OCR TRANSLATOR",
              ),
            ),
          ],
        ),
      ),
    );
  }
}
