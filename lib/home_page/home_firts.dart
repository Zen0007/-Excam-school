import 'dart:io';

import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class HomeFirts extends StatefulWidget {
  const HomeFirts({super.key});

  @override
  State<HomeFirts> createState() => _HomeFirtsState();
}

class _HomeFirtsState extends State<HomeFirts> with WidgetsBindingObserver {
  static const platform = MethodChannel("kiosk_mode_channel");

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.paused && Platform.isIOS) {
      showCupertinoDialog(
        context: context,
        builder: (context) => CupertinoAlertDialog(
          title: const Text("warning"),
          content: const Text("don't allow to exit this app "),
          actions: [
            CupertinoDialogAction(
              child: const Text("oke"),
              onPressed: () => Navigator.of(context).pop(true),
            ),
          ],
        ),
      );
    }
  }

  Future<void> start() async {
    try {
      await platform.invokeMethod("startKioskMode");
      print("------------------------------------");
    } on PlatformException catch (e, stacktrace) {
      debugPrint('$e');
      print(stacktrace);
    }
  }

  Future<void> end() async {
    try {
      await platform.invokeMethod("endKioskMode");
      print("this working --------------------------------------");
    } on PlatformException catch (e, stacktrace) {
      debugPrint("$e");
      print(stacktrace);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        actions: [
          TextButton(
            style: TextButton.styleFrom(foregroundColor: Colors.blue),
            onPressed: end,
            child: const Text(
              "exit",
              style: TextStyle(
                color: Colors.red,
                fontWeight: FontWeight.bold,
              ),
            ),
          ),
        ],
      ),
      backgroundColor: Colors.amber[400],
      body: Center(
        child: Column(
          children: [
            const SizedBox(
              height: 100,
            ),
            ElevatedButton(onPressed: end, child: const Text("EXCITE")),
            const SizedBox(
              height: 50,
            ),
            ElevatedButton(onPressed: start, child: const Text("start")),
          ],
        ),
      ),
    );
  }
}
