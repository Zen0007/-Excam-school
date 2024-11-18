import 'package:flutter/material.dart';

class TestMode extends StatefulWidget {
  const TestMode({super.key});

  @override
  State<TestMode> createState() => _TestModeState();
}

class _TestModeState extends State<TestMode> {
  // static const MethodChannel platform = MethodChannel('kiosk_mode_channel');

  void stop() async {
    try {
      // await platform.invokeMethod("stopKioskMode");
    } catch (e, s) {
      debugPrint("$e");
      debugPrint('$s');
    }
  }

  void start() async {
    try {
      // await platform.invokeMethod('startKioskMode');
    } catch (e, s) {
      debugPrint("$e");
      debugPrint('$s');
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.purpleAccent[300],
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          mainAxisSize: MainAxisSize.min,
          children: [
            ElevatedButton(
              onPressed: start,
              child: const Text('start'),
            ),
            ElevatedButton(
              onPressed: stop,
              child: const Text('stop'),
            )
          ],
        ),
      ),
    );
  }
}
