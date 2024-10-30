import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:webview_flutter/webview_flutter.dart';

class HomeFirts extends StatefulWidget {
  const HomeFirts({super.key});

  @override
  State<HomeFirts> createState() => _HomeFirtsState();
}

class _HomeFirtsState extends State<HomeFirts> {
  static const MethodChannel platform = MethodChannel('kiosk_mode_channel');
  late WebViewController webViewController;

  @override
  void initState() {
    super.initState();
  }

  void webView() {
    webViewController = WebViewController()
      ..setJavaScriptMode(JavaScriptMode.unrestricted)
      ..setBackgroundColor(Colors.amber)
      ..loadRequest(
        Uri.parse(''),
      );
  }

  void start(BuildContext context) async {
    try {
      await platform.invokeMethod('startKioskMode');
      debugPrint("Kiosk Mode started");

      showDialog(
        // ignore: use_build_context_synchronously
        context: context,
        builder: (BuildContext context) {
          return const AlertDialog(
            title: Text('this kiosk mode is active'),
          );
        },
      );

      Timer(
        const Duration(seconds: 3),
        () {
          Navigator.of(context).pop(); // Closes the dialog
        },
      );
      if (!context.mounted) {
        return;
      }
    } on PlatformException catch (e) {
      debugPrint("Error starting kiosk mode: ${e.message}");
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text("Failed to start kiosk mode: ${e.message}")),
      );
    }
  }

  void end(BuildContext context) async {
    try {
      await platform.invokeMethod("stopKioskMode");
      debugPrint("Kiosk Mode stopped");

      if (!context.mounted) {
        return;
      }
    } on PlatformException catch (e) {
      debugPrint("Error stopping kiosk mode: ${e.message}");
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text("Failed to stop kiosk mode: ${e.message}")),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("Kiosk Mode Example"),
        actions: [
          Row(
            mainAxisAlignment: MainAxisAlignment.end,
            children: [
              ElevatedButton(
                onPressed: () => start(context),
                child: const Text("START KIOSK MODE"),
              ),
              const SizedBox(
                width: 50,
              ),
              ElevatedButton(
                onPressed: () => end(context),
                child: const Text("STOP KIOSK MODE"),
              ),
            ],
          ),
        ],
      ),
      body: WebViewWidget(
        controller: webViewController,
      ),
    );
  }
}
