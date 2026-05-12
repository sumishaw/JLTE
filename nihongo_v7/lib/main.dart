import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

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

  Future<void> startListening()
  async {

    if (running) return;

    running = true;

    await platform.invokeMethod(
      'requestOverlayPermission',
    );

    timer = Timer.periodic(

      const Duration(
        seconds: 2,
      ),

      (_) async {

        await fetchSubtitle();
      },
    );
  }

  Future<void> fetchSubtitle()
  async {

    try {

      final text =
          await platform.invokeMethod(
        'getSubtitleText',
      );

      if (
          text == null ||
          text.toString().trim().isEmpty
      ) {
        return;
      }

      final jp =
          text.toString();

      if (
          jp == japaneseText
      ) {
        return;
      }

      japaneseText = jp;

      final translated =
          await translator.translate(
        jp,
        from: 'ja',
        to: 'en',
      );

      englishText =
          translated.text;

      setState(() {});

    } catch (e) {

      print(e);
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

      body: Padding(

        padding:
        const EdgeInsets.all(
          20,
        ),

        child: Column(

          mainAxisAlignment:
          MainAxisAlignment.center,

          crossAxisAlignment:
          CrossAxisAlignment.start,

          children: [

            Text(

              japaneseText,

              style:
              const TextStyle(

                color:
                Colors.white,

                fontSize: 24,
              ),
            ),

            const SizedBox(
              height: 30,
            ),

            Text(

              englishText,

              style:
              const TextStyle(

                color:
                Colors.greenAccent,

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
              startListening,

              child: const Text(
                "START LIVE TRANSLATION",
              ),
            ),
          ],
        ),
      ),
    );
  }
}
