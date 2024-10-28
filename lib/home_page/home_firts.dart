import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class HomeFirts extends StatefulWidget {
  const HomeFirts({super.key});

  @override
  State<HomeFirts> createState() => _HomeFirtsState();
}

class _HomeFirtsState extends State<HomeFirts> {
  final platfom = const MethodChannel("kiosk_mode_channel");

  Future<void> start() async {
    try {
      await platfom.invokeMethod('startKioskMode');
      print("runig");
    } on PlatformException catch (e, s) {
      debugPrint("${e.message} ---------");
      print("is not working $s");
    }
  }

  Future<void> end() async {
    try {
      await platfom.invokeMethod("endKioskMode");
      print("stop");
    } on PlatformException catch (e, s) {
      debugPrint("${e.message}========================");
      print("is not working $s");
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Center(
        child: Column(
          children: [
            const SizedBox(
              height: 100,
            ),
            ElevatedButton(onPressed: end, child: const Text("EXCITE")),
            const SizedBox(
              height: 30,
            ),
            ElevatedButton(onPressed: start, child: const Text("start")),
          ],
        ),
      ),
    );
  }
}
