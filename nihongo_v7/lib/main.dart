import 'package:flutter/material.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:speech_to_text/speech_to_text.dart';
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

  final SpeechToText speech = SpeechToText();
  final translator = GoogleTranslator();

  String japaneseText = "Waiting for Japanese speech...";
  String englishText = "English translation will appear here";

  bool isListening = false;

  Future<void> startListening() async {

    await Permission.microphone.request();

    bool available = await speech.initialize();

    if (!available) {
      return;
    }

    setState(() {
      isListening = true;
    });

    speech.listen(
      localeId: 'ja_JP',
      listenMode: ListenMode.dictation,
      partialResults: true,
      onResult: (result) async {

        japaneseText = result.recognizedWords;

        if (japaneseText.isNotEmpty) {

          try {

            final translation =
                await translator.translate(
              japaneseText,
              from: 'ja',
              to: 'en',
            );

            setState(() {
              englishText = translation.text;
            });

          } catch (_) {}
        }

        setState(() {});
      },
    );
  }

  void stopListening() {

    speech.stop();

    setState(() {
      isListening = false;
    });
  }

  @override
  Widget build(BuildContext context) {

    return Scaffold(
      backgroundColor: Colors.black,

      body: Padding(
        padding: const EdgeInsets.all(20),

        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [

            Text(
              japaneseText,
              textAlign: TextAlign.center,
              style: const TextStyle(
                color: Colors.white,
                fontSize: 24,
              ),
            ),

            const SizedBox(height: 30),

            Text(
              englishText,
              textAlign: TextAlign.center,
              style: const TextStyle(
                color: Colors.greenAccent,
                fontSize: 28,
                fontWeight: FontWeight.bold,
              ),
            ),

            const SizedBox(height: 40),

            ElevatedButton(
              onPressed: isListening
                  ? stopListening
                  : startListening,

              child: Text(
                isListening
                    ? 'STOP LISTENING'
                    : 'START LIVE TRANSLATION',
              ),
            ),
          ],
        ),
      ),
    );
  }
}
