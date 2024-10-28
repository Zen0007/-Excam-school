import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class HomeFirts extends StatefulWidget {
  const HomeFirts({super.key});

  @override
  State<HomeFirts> createState() => _HomeFirtsState();
}

class _HomeFirtsState extends State<HomeFirts> {
  final platfom = const MethodChannel("com.example.kioskapp/kiosk");

  Future<void> start() async {
    try {
      await platfom.invokeMethod('startKioskMode');
      print("runig");
    } catch (e, s) {
      debugPrint("$e ---------");
      print("is not working $s");
    }
  }

  Future<void> end() async {
    try {
      await platfom.invokeMethod("endKioskMode");
      print("stop");
    } catch (e, s) {
      debugPrint("$e========================");
      print("is not working $s");
    }
  }

  @override
  void initState() {
    start();
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Center(
        child: ElevatedButton(onPressed: end, child: const Text("EXCITE")),
      ),
    );
  }
}
