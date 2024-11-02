import 'package:excam/home_page/home_page_web.dart';
import 'package:flutter/material.dart';

class OnProgress extends StatelessWidget {
  const OnProgress({super.key, required this.audio, required this.progress});
  final VoidCallback audio;
  final int progress;

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
      body: Center(
        child: Container(
          decoration: BoxDecoration(
              borderRadius: BorderRadius.circular(40), color: Colors.blue[200]),
          width: 200,
          height: 200,
          child: Center(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                const Text(
                  "this prograse",
                  style: TextStyle(
                      fontSize: 20, color: Color.fromARGB(255, 0, 0, 0)),
                ),
                const SizedBox(
                  height: 20,
                ),
                Text(
                  "$progress",
                  style: const TextStyle(
                      fontSize: 40, color: Color.fromARGB(255, 0, 0, 0)),
                ),
              ],
            ),
          ),
        ),
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
        child: const Icon(Icons.arrow_back_ios_new_sharp),
      ),
    );
  }
}
