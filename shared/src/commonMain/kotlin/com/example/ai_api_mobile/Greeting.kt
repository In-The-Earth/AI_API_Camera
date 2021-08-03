package com.example.ai_api_mobile

class Greeting {
    fun greeting(): String {
        return "Hello, ${Platform().platform}!"
    }
}