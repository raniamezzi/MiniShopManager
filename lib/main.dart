import 'package:flutter/material.dart';
import 'catalogue_page.dart';

void main() {
  runApp(const MiniShopApp());
}

class MiniShopApp extends StatelessWidget {
  const MiniShopApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: "MiniShop Manager",
      theme: ThemeData(primarySwatch: Colors.teal),
      home: const CataloguePage(),
    );
  }
}