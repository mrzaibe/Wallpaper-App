package com.example.mvvmtask.utils

import coil.transform.Transformation

enum class Filters(val filtersTitle: String, val filterType: Transformation) {

    Default("Default", DefaultTransformation()),
    GrayScale("GrayScale", GrayScaleTransformation()),
    Sepia("Sepia", SepiaTransformation()),
    InvertColors("Invert", InvertColorsTransformation()),
    BlackWhite("BlackWhite", BlackWhiteTransformation()),
    Contrast("Contrast", ContrastTransformation()),
    Cool("Cool", CoolTransformation()),
    Brightness("Brightness", BrightnessTransformation()),
    Negative("Negative", NegativeTransformation()),
    Saturation("Saturation", SaturationTransformation()),
    Vintage("Vintage", VintageTransformation()),
    Warmth("Warmth", WarmthTransformation()),
}