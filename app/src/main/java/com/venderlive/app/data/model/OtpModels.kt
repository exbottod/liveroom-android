package com.venderlive.app.data.model

data class OtpRequest(val phone: String)

data class OtpVerifyRequest(val phone: String, val code: String)
