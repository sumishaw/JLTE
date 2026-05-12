import 'dart:async';
import 'dart:collection';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
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

  static const methodChannel =
      MethodChannel(
        "nihongo_lens/capture",
      );

  static const eventChannel =
      EventChannel(
        "nihongo_lens/subtitles",
      );

  late final OnDeviceTranslator
      translator;

  final Map<String, String>
      translationCache = HashMap();

  StreamSubscription? subscription;

  String japanese =
      "Waiting for subtitles...";

  String english =
      "Waiting for translation...";

  String status =
      "Initializing...";

  String lastSubtitle = "";

  bool translating = false;

  @override
  void initState() {

    super.initState();

    initTranslator();

    startSubtitleStream();
  }

  Future<void> initTranslator() async {

    translator = OnDeviceTranslator(
      sourceLanguage:
          TranslateLanguage.japanese,
      targetLanguage:
          TranslateLanguage.english,
    );

    setState(() {

      status =
          "Translation engine ready";
    });
  }

  void startSubtitleStream() {

    subscription =
        eventChannel
            .receiveBroadcastStream()
            .listen(

      (event) async {

        if (translating) return;

        translating = true;

        try {

          final jp =
              event.toString().trim();

          if (jp.isEmpty) {

            translating = false;

            return;
          }

          if (jp == lastSubtitle) {

            translating = false;

            return;
          }

          lastSubtitle = jp;

          setState(() {

            japanese = jp;

            status =
                "Translating subtitle...";
          });

          String translated;

          if (
              translationCache
                  .containsKey(jp)
          ) {

            translated =
                translationCache[jp]!;

          } else {

            try {

              translated =
                  await translator
                      .translateText(
                        jp,
                      );

            } catch (e) {

              translated =
                  "Translation failed";
            }

            translationCache[jp] =
                translated;
          }

          setState(() {

            english = translated;

            status =
                "Live subtitle updated";
          });

        } catch (e) {

          setState(() {

            status =
                "Error: ${e.toString()}";
          });

        } finally {

          translating = false;
        }
      },

      onError: (error) {

        setState(() {

          status =
              "Subtitle stream failed";
        });
      },
    );
  }

  Future<void> startCapture() async {

    try {

      await methodChannel.invokeMethod(
        "startCapture",
      );

      setState(() {

        status =
            "Waiting for live subtitles...";
      });

    } catch (e) {

      setState(() {

        status =
            "Capture failed";
      });
    }
  }

  @override
  void dispose() {

    subscription?.cancel();

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

      floatingActionButton:
          FloatingActionButton.extended(

        onPressed: startCapture,

        icon: const Icon(
          Icons.play_arrow,
        ),

        label: const Text(
          "Start Live Capture",
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

              "REAL-TIME LIVE OCR ENGINE\n\n"
              "• Native Kotlin OCR\n"
              "• EventChannel subtitle streaming\n"
              "• Real-time Japanese detection\n"
              "• Live English translation\n"
              "• Translation caching\n"
              "• Duplicate subtitle filtering",

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
