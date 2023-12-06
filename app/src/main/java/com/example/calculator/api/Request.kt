package com.example.calculator.api

class Error (
    var code: Int,
    var type: String,
    var info: String,
)

class Request (
    var success: Boolean,
    var timestamp: Int,
    var base: String,
    var date: String,
    var rates: (Map<String?, Double?>),
    var error: Error,
)