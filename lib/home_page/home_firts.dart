import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class HomeFirts extends StatefulWidget {
  const HomeFirts({super.key});

  @override
  State<HomeFirts> createState() => _HomeFirtsState();
}

class _HomeFirtsState extends State<HomeFirts> {
  void start() {
    SystemChrome.setEnabledSystemUIMode(SystemUiMode.leanBack);
  }

  @override
  Widget build(BuildContext context) {
    return const Scaffold(
      body: Center(
        child: Text("home firts"),
      ),
    );
  }
}
