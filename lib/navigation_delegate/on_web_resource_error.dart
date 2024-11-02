import 'package:excam/home_page/home_page_web.dart';
import 'package:flutter/material.dart';
import 'package:webview_flutter/webview_flutter.dart';

class OnWebResourceError extends StatelessWidget {
  const OnWebResourceError(
      {super.key, required this.audio, required this.error});
  final VoidCallback audio;
  final WebResourceError error;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Colors.blue,
        title: const Text("EXCAM APP"),
        actions: [
          Row(
            children: [
              ElevatedButton(
                onPressed: audio,
                child: const Text("EXIT"),
              ),
            ],
          ),
        ],
      ),
      body: AlertDialog(
        title: const Text("on error "),
        content: Text("this error exception ${error.description} web resource"),
        actions: [
          FloatingActionButton(
            onPressed: () {
              Navigator.pop(context);
            },
            child: const Text("yes"),
          )
        ],
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () {
          Navigator.push(
            context,
            MaterialPageRoute(
              builder: (context) => const HomePageWeb(),
            ),
          );
        },
        child: const Icon(Icons.arrow_back_ios),
      ),
    );
  }
}
