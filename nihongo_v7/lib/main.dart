import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import 'package:google_translator/google_translator.dart';

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

  static const platform =
      MethodChannel(
        'overlay_channel',
      );

  final translator =
      GoogleTranslator();

  String japaneseText =
      "Waiting for Japanese subtitles...";

  String englishText =
      "Waiting for English translation...";

  Timer? timer;

  bool running = false;

  String lastTranslated =
      "";

  Future<void> startListening()
  async {

    if (running) return;

    running = true;

    try {

      await platform.invokeMethod(
        'requestOverlayPermission',
      );

    } catch (e) {

      print(
        "PERMISSION ERROR: $e",
      );
    }

    timer = Timer.periodic(

      const Duration(
        seconds: 2,
      ),

      (_) async {

        await fetchSubtitle();
      },
    );

    ScaffoldMessenger.of(
      context,
    ).showSnackBar(

      const SnackBar(

        content: Text(
          "Live translation started",
        ),
      ),
    );
  }

  Future<void> fetchSubtitle()
  async {

    try {

      final dynamic result =
          await platform.invokeMethod(
        'getSubtitleText',
      );

      if (result == null) {
        return;
      }

      final jp =
          result.toString().trim();

      if (jp.isEmpty) {
        return;
      }

      if (jp == lastTranslated) {
        return;
      }

      lastTranslated = jp;

      japaneseText = jp;

      setState(() {});

      print(
        "JAPANESE DETECTED: $jp",
      );

      String translatedText =
          "";

      try {

        translatedText =
            await translator.translate(
          jp,
          from: 'ja',
          to: 'en',
        );

      } catch (e) {

        print(
          "TRANSLATION ERROR: $e",
        );

        translatedText =
            "Translation failed";
      }

      if (
          translatedText.trim().isEmpty
      ) {

        translatedText =
            "No translation";
      }

      englishText =
          translatedText;

      setState(() {});

      print(
        "ENGLISH: $englishText",
      );

      try {

        await platform.invokeMethod(
          'showOverlay',
          {
            "text": englishText,
          },
        );

      } catch (e) {

        print(
          "OVERLAY ERROR: $e",
        );
      }

    } catch (e) {

      print(
        "FETCH ERROR: $e",
      );
    }
  }

  @override
  void dispose() {

    timer?.cancel();

    super.dispose();
  }

  @override
  Widget build(BuildContext context) {

    return Scaffold(

      backgroundColor:
      Colors.black,

      body: SafeArea(

        child: Padding(

          padding:
          const EdgeInsets.all(
            20,
          ),

          child: Column(

            crossAxisAlignment:
            CrossAxisAlignment.start,

            mainAxisAlignment:
            MainAxisAlignment.center,

            children: [

              const Text(

                "Japanese Subtitle",

                style: TextStyle(

                  color:
                  Colors.white70,

                  fontSize: 18,
                ),
              ),

              const SizedBox(
                height: 10,
              ),

              Container(

                width: double.infinity,

                padding:
                const EdgeInsets.all(
                  16,
                ),

                decoration:
                BoxDecoration(

                  color:
                  Colors.white10,

                  borderRadius:
                  BorderRadius.circular(
                    16,
                  ),
                ),

                child: Text(

                  japaneseText,

                  style:
                  const TextStyle(

                    color:
                    Colors.white,

                    fontSize: 24,
                  ),
                ),
              ),

              const SizedBox(
                height: 40,
              ),

              const Text(

                "English Translation",

                style: TextStyle(

                  color:
                  Colors.greenAccent,

                  fontSize: 18,
                ),
              ),

              const SizedBox(
                height: 10,
              ),

              Container(

                width: double.infinity,

                padding:
                const EdgeInsets.all(
                  16,
                ),

                decoration:
                BoxDecoration(

                  color:
                  Colors.green
                      .withOpacity(
                    0.15,
                  ),

                  borderRadius:
                  BorderRadius.circular(
                    16,
                  ),
                ),

                child: Text(

                  englishText,

                  style:
                  const TextStyle(

                    color:
                    Colors.greenAccent,

                    fontSize: 30,

                    fontWeight:
                    FontWeight.bold,
                  ),
                ),
              ),

              const SizedBox(
                height: 50,
              ),

              Center(

                child:
                ElevatedButton(

                  onPressed:
                  startListening,

                  child: const Text(
                    "START LIVE TRANSLATION",
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
