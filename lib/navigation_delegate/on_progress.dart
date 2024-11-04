import 'package:flutter/material.dart';
import 'package:webview_flutter/webview_flutter.dart';

class WebView extends StatelessWidget {
  const WebView(
      {super.key, required this.audio, required this.webViewController});
  final VoidCallback audio;
  final WebViewController webViewController;

  @override
  Widget build(BuildContext context) {
    final size = MediaQuery.of(context).size;
    return Scaffold(
      body: SingleChildScrollView(
        child: Column(
          children: [
            Container(
              width: size.width,
              height: size.height,
              color: Colors.blue[600],
              child: WebViewWidget(controller: webViewController),
            )
          ],
        ),
      ),
    );
  }
}
