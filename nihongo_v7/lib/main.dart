import 'dart:async';
import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:google_mlkit_text_recognition/google_mlkit_text_recognition.dart';
import 'package:google_mlkit_translation/google_mlkit_translation.dart';
import 'package:image_picker/image_picker.dart';

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

  String japaneseText =
      "No Japanese subtitle detected";

  String englishText =
      "No English translation";

  bool loading = false;

  final textRecognizer =
      TextRecognizer(
        script: TextRecognitionScript.japanese,
      );

  final translator = OnDeviceTranslator(
    sourceLanguage: TranslateLanguage.japanese,
    targetLanguage: TranslateLanguage.english,
  );

  Future<void> pickAndTranslateImage() async {

    try {

      final picker = ImagePicker();

      final XFile? image =
          await picker.pickImage(
            source: ImageSource.gallery,
          );

      if (image == null) return;

      setState(() {
        loading = true;
      });

      final inputImage =
          InputImage.fromFilePath(image.path);

      final RecognizedText recognizedText =
          await textRecognizer.processImage(
            inputImage,
          );

      final jpText =
          recognizedText.text.trim();

      if (jpText.isEmpty) {

        setState(() {

          japaneseText =
              "No Japanese subtitle found";

          englishText =
              "Translation unavailable";

          loading = false;
        });

        return;
      }

      final translated =
          await translator.translateText(jpText);

      setState(() {

        japaneseText = jpText;

        englishText = translated;

        loading = false;
      });

    } catch (e) {

      setState(() {

        japaneseText = "OCR failed";

        englishText = e.toString();

        loading = false;
      });
    }
  }

  @override
  void dispose() {

    textRecognizer.close();

    translator.close();

    super.dispose();
  }

  @override
  Widget build(BuildContext context) {

    return Scaffold(
      backgroundColor: Colors.black,

      appBar: AppBar(
        backgroundColor: Colors.black,
        title: const Text(
          "Nihongo Lens OCR",
        ),
      ),

      floatingActionButton: FloatingActionButton(
        onPressed: pickAndTranslateImage,
        child: const Icon(Icons.image),
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
                    BorderRadius.circular(14),
              ),

              child: Text(
                japaneseText,

                style: const TextStyle(
                  color: Colors.white,
                  fontSize: 28,
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
                    BorderRadius.circular(14),
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

            const SizedBox(height: 40),

            if (loading)
              const Center(
                child: CircularProgressIndicator(),
              ),

            const Spacer(),

            const Text(
              "Tap image button and select screenshot with Japanese subtitles.",
              style: TextStyle(
                color: Colors.white54,
                fontSize: 16,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
