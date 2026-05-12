import 'dart:io';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
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

        imageFile = File(image.path);
      });

      final bytes =
          await imageFile!.readAsBytes();

      final original =
          img.decodeImage(bytes);

      if (original == null) {

        setState(() {

          japaneseText =
              "Image decode failed";

          englishText =
              "Try another screenshot";

          loading = false;
        });

        return;
      }

      // Auto crop bottom subtitle area

      final cropY =
          (original.height * 0.70).toInt();

      final cropHeight =
          (original.height * 0.25).toInt();

      final cropped =
          img.copyCrop(
            original,
            x: 0,
            y: cropY,
            width: original.width,
            height: cropHeight,
          );

      // Save cropped preview

      final croppedPath =
          "${imageFile!.parent.path}/cropped_subtitle.png";

      final croppedFile =
          File(croppedPath);

      await croppedFile.writeAsBytes(
        img.encodePng(cropped),
      );

      // Fake subtitle extraction placeholder

      setState(() {

        japaneseText =
            "Subtitle region cropped successfully";

        englishText =
            "Ready for OCR integration";

        imageFile = croppedFile;

        loading = false;
      });

    } catch (e) {

      setState(() {

        japaneseText = "Processing failed";

        englishText = e.toString();

        loading = false;
      });
    }
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

              "NEW FEATURES:\n\n"
              "• Auto subtitle area crop\n"
              "• Faster subtitle scanning\n"
              "• Cleaner OCR preparation\n"
              "• Better anime/movie subtitle focus",

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
