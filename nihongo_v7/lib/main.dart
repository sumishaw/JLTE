import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:translator/translator.dart';

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

class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {

  final translator = GoogleTranslator();

  static const platform =
      MethodChannel('overlay_channel');

  static const whisperChannel =
      MethodChannel('whisper_channel');

  String japaneseText =
      "Waiting for Japanese transcription...";

  String englishText =
      "Waiting for English translation...";

  @override
  void initState() {

    super.initState();

    whisperChannel.setMethodCallHandler(
      (call) async {

        if (call.method == "onTranscription") {

          final japanese =
              call.arguments.toString();

          japaneseText = japanese;

          try {

            final translation =
                await translator.translate(
              japanese,
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
                  "text": englishText
                },
              );

            } catch (_) {}

          } catch (_) {}
        }
      },
    );
  }

  Future<void> startCapture() async {

    try {

      await platform.invokeMethod(
        'startOverlay'
      );

      await platform.invokeMethod(
        'startInternalAudioCapture'
      );

    } catch (_) {}
  }

  @override
  Widget build(BuildContext context) {

    return Scaffold(

      backgroundColor: Colors.black,

      body: Padding(

        padding: const EdgeInsets.all(20),

        child: Column(

          mainAxisAlignment:
              MainAxisAlignment.center,

          crossAxisAlignment:
              CrossAxisAlignment.start,

          children: [

            Text(
              japaneseText,
              style: const TextStyle(
                color: Colors.white,
                fontSize: 24,
              ),
            ),

            const SizedBox(height: 20),

            Text(
              englishText,
              style: const TextStyle(
                color: Colors.greenAccent,
                fontSize: 30,
                fontWeight: FontWeight.bold,
              ),
            ),

            const SizedBox(height: 40),

            ElevatedButton(

              onPressed: startCapture,

              child: const Text(
                "START LIVE TRANSLATION"
              ),
            ),
          ],
        ),
      ),
    );
  }
}
