import 'package:flutter/material.dart';

class HomeFirts extends StatefulWidget {
  const HomeFirts({super.key});

  @override
  State<HomeFirts> createState() => _HomeFirtsState();
}

class _HomeFirtsState extends State<HomeFirts> {
  final PageController _pageController = PageController();
  bool _isSwipeEnabled = false;

  void _toggleSwipe() {
    setState(() {
      _isSwipeEnabled = !_isSwipeEnabled;
    });
  }

  Future<bool> _onBackPressed() async {
    // If swipe is not enabled, do not allow the user to exit the app
    if (!_isSwipeEnabled) {
      // Optionally show a message to the user
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Swipe navigation is disabled.')),
      );
      return false; // Prevent the back navigation
    }
    return true; // Allow back navigation
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Swipe Navigation Example'),
        actions: [
          IconButton(
            icon: Icon(_isSwipeEnabled ? Icons.stop : Icons.play_arrow),
            onPressed: _toggleSwipe,
          ),
        ],
      ),
      body: GestureDetector(
        onVerticalDragDown: (details) {
          if (!_isSwipeEnabled) {
            // Prevent swipe navigation if not enabled
            return;
          }
        },
        child: PageView(
          controller: _pageController,
          physics: _isSwipeEnabled
              ? const AlwaysScrollableScrollPhysics()
              : const NeverScrollableScrollPhysics(),
          children: <Widget>[
            Container(
              color: Colors.red,
              child: const Center(
                  child: Text('Page 1', style: TextStyle(fontSize: 24))),
            ),
            Container(
              color: Colors.green,
              child: const Center(
                  child: Text('Page 2', style: TextStyle(fontSize: 24))),
            ),
            Container(
              color: Colors.blue,
              child: const Center(
                  child: Text('Page 3', style: TextStyle(fontSize: 24))),
            ),
          ],
        ),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () async {
          // Handle back button manually
          bool canPop = await _onBackPressed();

          if (!context.mounted) {
            return;
          }
          if (canPop) {
            // If allowed, navigate back
            Navigator.of(context).pop();
          }
        },
        child: const Icon(Icons.arrow_back),
      ),
    );
  }
}
