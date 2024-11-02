import 'package:flutter/material.dart';

class MainTemplate extends StatelessWidget {
  const MainTemplate({super.key, required this.templateWigate});
  final Widget templateWigate;

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
        useMaterial3: true,
      ),
      color: Colors.amber[600],
      home: templateWigate,
    );
  }
}
