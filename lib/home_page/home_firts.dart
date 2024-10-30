import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class HomeFirts extends StatefulWidget {
  const HomeFirts({super.key});

  @override
  State<HomeFirts> createState() => _HomeFirtsState();
}

class _HomeFirtsState extends State<HomeFirts> {
  static const MethodChannel platform = MethodChannel('kiosk_mode_channel');
  bool isKioskModeActive = false; // Track the kiosk mode state

  Future<void> start() async {
    try {
      await platform.invokeMethod('startKioskMode');
      setState(() {
        isKioskModeActive = true; // Update the state
      });
      print("Kiosk Mode started");
    } on PlatformException catch (e) {
      debugPrint("Error starting kiosk mode: ${e.message}");
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text("Failed to start kiosk mode: ${e.message}")),
      );
    }
  }

  Future<void> end() async {
    try {
      await platform.invokeMethod("stopKioskMode");
      setState(() {
        isKioskModeActive = false; // Update the state
      });
      print("Kiosk Mode stopped");
    } on PlatformException catch (e) {
      debugPrint("Error stopping kiosk mode: ${e.message}");
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text("Failed to stop kiosk mode: ${e.message}")),
      );
    }
  }

  // @override
  // void initState() {
  //   super.initState();
  //   // Optionally, you can start kiosk mode here if desired
  //   start();
  // }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text("Kiosk Mode Example")),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const SizedBox(height: 100),
            ElevatedButton(
              onPressed:
                  isKioskModeActive ? end : null, // Disable if not active
              child: const Text("STOP KIOSK MODE"),
            ),
            const SizedBox(height: 40),
            ElevatedButton(
              onPressed: isKioskModeActive ? null : start, // Disable if active
              child: const Text("START KIOSK MODE"),
            ),
          ],
        ),
      ),
    );
  }
}
