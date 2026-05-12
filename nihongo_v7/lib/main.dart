import 'dart:io';

import 'package:flutter/material.dart';
import 'package:google_mlkit_text_recognition/google_mlkit_text_recognition.dart';
import 'package:google_mlkit_translation/google_mlkit_translation.dart';
import 'package:image/image.dart' as img;
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
      theme: ThemeData.dark(),
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

  File? imageFile;

  String japaneseText =
      "No subtitle scanned";

  String englishText =
      "No translation";

  bool loading = false;

  Future<void> scanSubtitle() async {

    try {

      final XFile? image =
          await picker.pickImage(
            source: ImageSource.gallery,
          );

      if (image == null) return;

      setState(() {

        loading = true;
      });

      final originalFile =
          File(image.path);

      final bytes =
          await originalFile.readAsBytes();

      final decoded =
          img.decodeImage(bytes);

      if (decoded == null) {

        setState(() {

          japaneseText =
              "Image decode failed";

          englishText =
              "Try another screenshot";

          loading = false;
        });

        return;
      }

      // Crop subtitle region

      final cropY =
          (decoded.height * 0.70).toInt();

      final cropHeight =
          (decoded.height * 0.25).toInt();

      final cropped =
          img.copyCrop(
            decoded,
            x: 0,
            y: cropY,
            width: decoded.width,
            height: cropHeight,
          );

      final croppedPath =
          "${originalFile.parent.path}/subtitle_crop.png";

      final croppedFile =
          File(croppedPath);

      await croppedFile.writeAsBytes(
        img.encodePng(cropped),
      );

      imageFile = croppedFile;

      // OCR

      final inputImage =
          InputImage.fromFilePath(
            croppedFile.path,
          );

      final result =
          await recognizer.processImage(
            inputImage,
          );

      final jpText =
          result.text.trim();

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

      // Translation

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

      appBar: AppBar(
        title: const Text(
          "Nihongo Lens",
        ),
      ),

      floatingActionButton:
          FloatingActionButton.extended(

        onPressed: scanSubtitle,

        icon: const Icon(Icons.image),

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

            if (imageFile != null)

              ClipRRect(

                borderRadius:
                    BorderRadius.circular(12),

                child: Image.file(
                  imageFile!,
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

              padding: const EdgeInsets.all(16),

              decoration: BoxDecoration(
                color: Colors.white10,
                borderRadius:
                    BorderRadius.circular(12),
              ),

              child: Text(

                japaneseText,

                style: const TextStyle(
                  fontSize: 24,
                  color: Colors.white,
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

              padding: const EdgeInsets.all(16),

              decoration: BoxDecoration(
                color:
                    Colors.green.withOpacity(0.2),

                borderRadius:
                    BorderRadius.circular(12),
              ),

              child: Text(

                englishText,

                style: const TextStyle(
                  fontSize: 28,
                  color: Colors.greenAccent,
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

              "FEATURES:\n\n"
              "• Auto subtitle crop\n"
              "• Japanese OCR\n"
              "• English translation\n"
              "• Better anime subtitle focus\n"
              "• Stable screenshot workflow",

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
