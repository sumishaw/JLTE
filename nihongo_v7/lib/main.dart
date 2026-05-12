import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

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

  static const platform =
      MethodChannel(
        "nihongo_lens/capture",
      );

  String status =
      "Live subtitle capture not started";

  bool running = false;

  Future<void> startCapture() async {

    try {

      await platform.invokeMethod(
        "startCapture",
      );

      setState(() {

        running = true;

        status =
            "Waiting for screen capture permission...";
      });

    } catch (e) {

      setState(() {

        status =
            "Error: ${e.toString()}";
      });
    }
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

        onPressed:
            running
                ? null
                : startCapture,

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

            const SizedBox(height: 12),

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

                status,

                style: const TextStyle(
                  fontSize: 24,
                  color: Colors.white,
                ),
              ),
            ),

            const SizedBox(height: 40),

            const Text(

              "LIVE OCR PIPELINE\n\n"
              "• MediaProjection capture\n"
              "• Continuous frame extraction\n"
              "• OCR subtitle detection\n"
              "• English translation overlay\n"
              "• Real-time anime subtitle support",

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
