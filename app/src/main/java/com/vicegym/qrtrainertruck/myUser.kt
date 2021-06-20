package com.vicegym.qrtrainertruck

object myUser {
    var id: String? = null
    var name: String? = null
    var email: String? = null
    var phoneNumber: String? = null
    var address: String? = null
    var profilePicture: String = "android.resource://com.vicegym.qrtrainertruck/" + R.drawable.ic_name
    var acceptedTermsAndConditions: Boolean = false
    var rank: String = "Újonc"
    var score: Number = 0
    var trainingList: MutableList<TrainingData> = mutableListOf()
}