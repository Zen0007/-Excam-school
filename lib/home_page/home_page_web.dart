import 'dart:async';

import 'package:flutter/material.dart';
import 'package:just_audio/just_audio.dart';

class HomePageWeb extends StatefulWidget {
  const HomePageWeb({super.key});

  @override
  State<HomePageWeb> createState() => _HomePageWebState();
}

class _HomePageWebState extends State<HomePageWeb> {
  final player = AudioPlayer();

  @override
  void dispose() {
    player.dispose();
    super.dispose();
  }

  void sog() async {
    await player.setAsset('assets/song/alert-audio.mp3');
    await player.play();
    await player.setVolume(0.5);

    Timer(
      const Duration(seconds: 4000),
      () async {
        await player.stop();
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Home web'),
        actions: [
          Row(
            children: [
              ElevatedButton(
                onPressed: () {},
                child: const Text("EXIT"),
              ),
            ],
          )
        ],
      ),
      body: Container(
        color: Colors.amber[600],
        child: const Center(
          child: Text('home'),
        ),
      ),
    );
  }
}
