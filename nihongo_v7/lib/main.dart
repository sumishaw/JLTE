import 'dart:io';

import 'package:flutter/material.dart';
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

  File? imageFile;

  String japaneseText =
      "No subtitle scanned";

  String englishText =
      "No translation";

  Future<void> pickImage() async {

    try {

      final picker = ImagePicker();

      final XFile? image =
          await picker.pickImage(
            source: ImageSource.gallery,
          );

      if (image == null) return;

      setState(() {

        imageFile = File(image.path);

        japaneseText =
            "Japanese subtitle detected successfully";

        englishText =
            "English translation will appear here";
      });

    } catch (e) {

      setState(() {

        japaneseText = "Error";

        englishText = e.toString();
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

        onPressed: pickImage,

        icon: const Icon(Icons.image),

        label: const Text(
          "Select Screenshot",
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

            const Text(

              "HOW TO USE:\n\n"
              "1. Take screenshot of Japanese subtitle\n"
              "2. Press Select Screenshot button\n"
              "3. Choose screenshot from gallery",

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
