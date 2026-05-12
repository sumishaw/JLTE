import 'dart:io';

import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
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

  final picker = ImagePicker();

  final recognizer = TextRecognizer(
    script: TextRecognitionScript.japanese,
  );

  final translator = OnDeviceTranslator(
    sourceLanguage: TranslateLanguage.japanese,
    targetLanguage: TranslateLanguage.english,
  );

  String japaneseText =
      "Tap capture button to scan subtitles";

  String englishText =
      "English translation will appear here";

  File? selectedImage;

  bool loading = false;

  Future<void> captureAndTranslate() async {

    try {

      final XFile? image =
          await picker.pickImage(
            source: ImageSource.gallery,
          );

      if (image == null) return;

      setState(() {

        loading = true;

        selectedImage = File(image.path);
      });

      final inputImage =
          InputImage.fromFilePath(image.path);

      final recognized =
          await recognizer.processImage(
            inputImage,
          );

      final jpText =
          recognized.text.trim();

      if (jpText.isEmpty) {

        setState(() {

          japaneseText =
              "No Japanese subtitle detected";

          englishText =
              "Translation unavailable";

          loading = false;
        });

        return;
      }

      final translated =
          await translator.translateText(
            jpText,
          );

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

    recognizer.close();

    translator.close();

    super.dispose();
  }

  @override
  Widget build(BuildContext context) {

    return Scaffold(

      backgroundColor: Colors.black,

      appBar: AppBar(
        title: const Text(
          "Nihongo Lens",
        ),
        backgroundColor: Colors.black,
      ),

      floatingActionButton:
          FloatingActionButton.extended(

        onPressed: captureAndTranslate,

        backgroundColor: Colors.green,

        icon: const Icon(Icons.camera),

        label: const Text(
          "Scan Subtitle",
        ),
      ),

      body: SingleChildScrollView(

        padding: const EdgeInsets.all(20),

        child: Column(
          crossAxisAlignment:
              CrossAxisAlignment.start,

          children: [

            if (selectedImage != null)

              ClipRRect(

                borderRadius:
                    BorderRadius.circular(12),

                child: Image.file(
                  selectedImage!,
                ),
              ),

            const SizedBox(height: 25),

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
                  fontSize: 24,
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
                color:
                    Colors.green.withOpacity(0.2),

                borderRadius:
                    BorderRadius.circular(14),
              ),

              child: Text(

                englishText,

                style: const TextStyle(
                  color: Colors.greenAccent,
                  fontSize: 28,
                  fontWeight: FontWeight.bold,
                ),
              ),
            ),

            const SizedBox(height: 40),

            if (loading)

              const Center(
                child:
                    CircularProgressIndicator(),
              ),

            const SizedBox(height: 40),

            const Text(

              "HOW TO USE:\n\n"
              "1. Take screenshot of Japanese subtitles\n"
              "2. Press Scan Subtitle button\n"
              "3. Select screenshot\n"
              "4. Instant English translation appears",

              style: TextStyle(
                color: Colors.white54,
                fontSize: 16,
                height: 1.6,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
