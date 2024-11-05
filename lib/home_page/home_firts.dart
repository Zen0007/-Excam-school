import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class HomeFirts extends StatefulWidget {
  const HomeFirts({super.key});

  @override
  State<HomeFirts> createState() => _HomeFirtsState();
}

class _HomeFirtsState extends State<HomeFirts> {
  static const platform = MethodChannel('com.example.app');

  Future<void> toggleGestureDetection() async {
    try {
      await platform.invokeMethod('true');
    } on PlatformException catch (e) {
      print("Failed to toggle gesture detection: '${e.message}'.");
    }
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: Text('Gesture Detection App'),
        ),
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              ElevatedButton(
                onPressed: toggleGestureDetection,
                child: Text('Start Gesture Detection'),
              ),
              SizedBox(height: 20),
              Text('Gesture Detection is '),
            ],
          ),
        ),
      ),
    );
  }

  // static const MethodChannel platform =
  //     MethodChannel('com.example.excam/app_control');
  // // late WebViewController webViewController;

  // // @override
  // // void initState() {
  // //   super.initState();
  // // }

  // // void webView() {
  // //   webViewController = WebViewController()
  // //     ..setJavaScriptMode(JavaScriptMode.unrestricted)
  // //     ..setBackgroundColor(Colors.amber)
  // //     ..loadRequest(
  // //       Uri.parse(''),
  // //     );
  // // }

  // void start(BuildContext context) async {
  //   try {
  //     await platform.invokeMethod('bringAppToForeground');
  //     debugPrint("Kiosk Mode started");

  //     showDialog(
  //       // ignore: use_build_context_synchronously
  //       context: context,
  //       builder: (BuildContext context) {
  //         return const AlertDialog(
  //           title: Text('this kiosk mode is active'),
  //         );
  //       },
  //     );

  //     if (!context.mounted) {
  //       return;
  //     }
  //   } on PlatformException catch (e) {
  //     debugPrint("Error starting kiosk mode: ${e.message}");
  //     ScaffoldMessenger.of(context).showSnackBar(
  //       SnackBar(content: Text("Failed to start kiosk mode: ${e.message}")),
  //     );
  //   }
  // }

  // // void end(BuildContext context) async {
  // //   try {
  // //     await platform.invokeMethod("stopKioskMode");
  // //     debugPrint("Kiosk Mode stopped");

  // //     if (!context.mounted) {
  // //       return;
  // //     }
  // //   } on PlatformException catch (e) {
  // //     debugPrint("Error stopping kiosk mode: ${e.message}");
  // //     ScaffoldMessenger.of(context).showSnackBar(
  // //       SnackBar(content: Text("Failed to stop kiosk mode: ${e.message}")),
  // //     );
  // //   }
  // // }

  // @override
  // Widget build(BuildContext context) {
  //   return Scaffold(
  //     appBar: AppBar(
  //       title: const Text("Kiosk Mode Example"),
  //     ),
  //     backgroundColor: Colors.indigo[400],
  //     body: Center(
  //       child: ElevatedButton(
  //         onPressed: () => start(context),
  //         child: const Text("START KIOSK MODE"),
  //       ),
  //     ),
  //   );
  // }
}
